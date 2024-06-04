package compiler;

import evaluator.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static compiler.Context.LINKAGE_NEXT;
import static compiler.Context.LINKAGE_RETURN;
import static compiler.InstSequence.*;
import static compiler.InstType.*;
import static compiler.Parameter.*;
import static compiler.Reg.*;

public class Compiler implements EvaluationVisitor<Context, InstSequence> {
    private int counter = 0;

    private InstSequence compileLinkage(String linkage) {
        if (linkage.equals(LINKAGE_RETURN)) {
            return new InstSequenceBuilder().
                    setType(GOTO).
                    addParameter(reg(CONTINUE)).
                    build();
        } else if (linkage.equals(LINKAGE_NEXT)) {
            return new InstSequence();
        } else {
            return new InstSequenceBuilder().
                    setType(GOTO).
                    addParameter(label(linkage)).
                    build();
        }
    }

    private InstSequence endWithLinkage(String linkage, InstSequence instSequence) {
        return preserving(instSequence, compileLinkage(linkage), CONTINUE);
    }

    private String makeLabel(String label) {
        counter++;
        return label + "-" + counter;
    }

    @Override
    public InstSequence visit(Evaluation e) {
        return append(
                new InstSequenceBuilder().
                        setType(ASSIGN).
                        setReg(ENV).
                        addParameter(op("get-global-environment")).
                        build(),
                e.accept(this, new Context(VAL, Context.LINKAGE_NEXT)),
                new InstSequenceBuilder().
                        setType(PERFORM).
                        addParameter(op("print")).
                        addParameter(reg(VAL)).
                        build()
        );
    }

    @Override
    public InstSequence visit(BeginEvaluation e, Context context) {
        InstSequence instSequence = new InstSequence();
        for (int i = e.getEvaluations().size() - 1; i >= 0; i--) {
            InstSequence block = e.getEvaluations().get(i).accept(this, new Context(context.getTarget(), LINKAGE_NEXT));
            instSequence = preserving(block, instSequence, ENV);
        }
        return endWithLinkage(context.getLinkage(), instSequence);
    }

    @Override
    public InstSequence visit(DefineEvaluation e, Context context) {
        InstSequence getValueInsts = e.getValue().accept(this, new Context(VAL, LINKAGE_NEXT));
        InstSequence instSequence = append(
                new InstSequenceBuilder().
                        setType(PERFORM).
                        addParameter(op("define-variable!")).
                        addParameter(conzt("(quote "+e.getName()+")")).
                        addParameter(reg(VAL)).
                        addParameter(reg(ENV)).
                        build(),
                new InstSequenceBuilder().
                        setType(ASSIGN).
                        setReg(VAL).
                        addParameter(conzt("(quote ok)")).
                        build()
        );
        return endWithLinkage(context.getLinkage(), preserving(getValueInsts, instSequence, ENV));
    }

    @Override
    public InstSequence visit(IfEvaluation e, Context context) {
        String afterIf = makeLabel("after-if");
        String trueBranch = makeLabel("true-branch");
        String falseBranch = makeLabel("false-branch");

        InstSequence getConditionInsts = e.getCondition().accept(this, new Context(VAL, Context.LINKAGE_NEXT));
        InstSequence consequentInsts = e.getConsequent().accept(this, new Context(context.getTarget(), context.getLinkage().equals(LINKAGE_NEXT) ? afterIf : context.getLinkage()));
        InstSequence alternativeInsts = e.getAlternative().accept(this, new Context(context.getTarget(), context.getLinkage()));

        InstSequence instSequence = append(
                append(
                        new InstSequenceBuilder().
                                setType(TEST).
                                addParameter(op("false?")).
                                addParameter(reg(VAL)).
                                build(),
                        new InstSequenceBuilder().
                                setType(BRANCH).
                                addParameter(label(falseBranch)).
                                build()
                ),
                parallel(
                        attach(trueBranch, consequentInsts),
                        attach(falseBranch, alternativeInsts)
                ),
                attach(afterIf)
        );

        return preserving(getConditionInsts, instSequence, ENV, CONTINUE);
    }

    @Override
    public InstSequence visit(LambdaEvaluation e, Context context) {
        String entry = makeLabel("lambda-entry");
        String afterLambda = makeLabel("after-lambda");

        InstSequence body = buildLambdaBody(e, entry);

        InstSequence instSequence = new InstSequenceBuilder().
                setType(ASSIGN).
                setReg(context.getTarget()).
                addParameter(op("make-compiled-procedure")).
                addParameter(label(entry)).
                addParameter(reg(ENV)).
                build();

        return tackOn(
                endWithLinkage(context.getLinkage().equals(Context.LINKAGE_NEXT) ? afterLambda : context.getLinkage(), instSequence),
                append(
                        body,
                        attach(afterLambda)
                )
        );
    }

    private InstSequence buildLambdaBody(LambdaEvaluation e, String entry) {
        InstSequenceBuilder setParametersInsts = new InstSequenceBuilder().
                setType(ASSIGN).
                setReg(VAL).
                addParameter(op("list"));
        e.getArguments().forEach(name -> setParametersInsts.addParameter(conzt("(quote " + name + ")")));

        InstSequence instSequence = e.getBody().accept(this, new Context(VAL, Context.LINKAGE_RETURN));

        return attach(
                entry,
                append(new InstSequenceBuilder().
                                setType(ASSIGN).
                                setReg(ENV).
                                addParameter(op("compiled-procedure-env")).
                                addParameter(reg(PROC)).
                                build(),
                        setParametersInsts.build(),
                        new InstSequenceBuilder().
                                setType(ASSIGN).
                                setReg(ENV).
                                addParameter(op("extend-environment")).
                                addParameter(reg(VAL)).
                                addParameter(reg(ARGL)).
                                addParameter(reg(ENV)).
                                build(),
                        instSequence
                )
        );
    }

    @Override
    public InstSequence visit(NumberEvaluation e, Context context) {
        InstSequence instSequence = new InstSequenceBuilder().
                setType(ASSIGN).
                setReg(context.getTarget()).
                addParameter(conzt(Double.toString(e.getValue()))).
                build();
        return endWithLinkage(context.getLinkage(), instSequence);
    }

    @Override
    public InstSequence visit(ProcedureEvaluation e, Context context) {
        InstSequence operatorInsts = e.getOperator().accept(this, new Context(PROC, Context.LINKAGE_NEXT));
        List<Evaluation> parameters = new ArrayList<>(e.getParameters()); // Reverse
        Collections.reverse(parameters);
        InstSequence constructArgListInsts = constructArgList(parameters);
        InstSequence proc = procedureCall(context);
        return preserving(operatorInsts,
                preserving(constructArgListInsts,
                        proc,
                        PROC, ENV, CONTINUE),
                ENV, CONTINUE);
    }

    private InstSequence constructArgList(List<Evaluation> parameters) {
        if (parameters.isEmpty()) {
            return new InstSequenceBuilder().
                    setType(ASSIGN).
                    setReg(ARGL).
                    addParameter(op("nil")).
                    build();
        } else {
            InstSequence getLastArg = append(
                    parameters.get(0).accept(this, new Context(VAL, Context.LINKAGE_NEXT)),
                    new InstSequenceBuilder().
                            setType(ASSIGN).
                            setReg(ARGL).
                            addParameter(op("nil")).
                            build(),
                    new InstSequenceBuilder().
                            setType(ASSIGN).
                            setReg(ARGL).
                            addParameter(op("cons")).
                            addParameter(reg(VAL)).
                            addParameter(reg(ARGL)).
                            build()
            );
            if (parameters.size() == 1) {
                return getLastArg;
            } else {
                return preserving(getLastArg, constructRestArgList(parameters.subList(1, parameters.size())), ENV);
            }
        }
    }

    private InstSequence constructRestArgList(List<Evaluation> parameters) {
        InstSequence instSequence = preserving(
                parameters.get(0).accept(this, new Context(VAL, Context.LINKAGE_NEXT)),
                new InstSequenceBuilder().
                        setType(ASSIGN).
                        setReg(ARGL).
                        addParameter(op("cons")).
                        addParameter(reg(VAL)).
                        addParameter(reg(ARGL)).
                        build(),
                ARGL);
        if (parameters.size() == 1) {
            return instSequence;
        } else {
            return preserving(instSequence, constructRestArgList(parameters.subList(1, parameters.size())), ENV);
        }
    }

    private InstSequence procedureCall(Context context) {
        String primitiveBranch = makeLabel("primitive-branch");
        String compiledBranch = makeLabel("compiled-branch");
        String afterCall = makeLabel("after-call");

        InstSequence primitiveCall = new InstSequenceBuilder().
                setType(ASSIGN).
                setReg(context.getTarget()).
                addParameter(op("apply-primitive-procedure")).
                addParameter(reg(PROC)).
                addParameter(reg(ARGL)).
                build();
        return append(
                new InstSequenceBuilder().
                        setType(TEST).
                        addParameter(op("primitive-procedure?")).
                        addParameter(reg(PROC)).
                        build(),
                new InstSequenceBuilder().
                        setType(BRANCH).
                        addParameter(label(primitiveBranch)).
                        build(),
                parallel(
                        attach(compiledBranch, compileCall(new Context(context.getTarget(), context.getLinkage().equals(LINKAGE_NEXT) ? afterCall : context.getLinkage()))),
                        endWithLinkage(context.getLinkage(), attach(primitiveBranch, primitiveCall))
                ),
                attach(afterCall)
        );
    }

    private InstSequence compileCall(Context context) {
        if (context.getTarget().equals(VAL) && !context.getLinkage().equals(LINKAGE_RETURN)) {
            // Set 'val' and go to label
            return append(
                    new InstSequenceBuilder().
                            setType(ASSIGN).
                            setReg(VAL).
                            addParameter(op("compiled-procedure-entry")).
                            addParameter(reg(PROC)).
                            build(),
                    new InstSequenceBuilder().
                            setType(ASSIGN).
                            setReg(CONTINUE).
                            addParameter(label(context.getLinkage())).
                            build(),
                    call(new InstSequenceBuilder().setType(GOTO).addParameter(reg(VAL)).build())
            );
        } else if (!context.getTarget().equals(VAL) && !context.getLinkage().equals(Context.LINKAGE_RETURN)) {
            // Set target and go to label
            String procReturn = makeLabel("proc-return");
            return append(
                    new InstSequenceBuilder().
                            setType(ASSIGN).
                            setReg(VAL).
                            addParameter(op("compiled-procedure-entry")).
                            addParameter(reg(PROC)).
                            build(),
                    new InstSequenceBuilder().
                            setType(ASSIGN).
                            setReg(CONTINUE).
                            addParameter(label(procReturn)).
                            build(),
                    call(new InstSequenceBuilder().setType(GOTO).addParameter(reg(VAL)).build()),
                    attach(procReturn),
                    new InstSequenceBuilder().
                            setType(ASSIGN).
                            setReg(context.getTarget()).
                            addParameter(reg(VAL)).
                            build(),
                    new InstSequenceBuilder().
                            setType(GOTO).
                            addParameter(label(context.getLinkage())).
                            build()
            );
        } else if (context.getTarget().equals(VAL) && context.getLinkage().equals(Context.LINKAGE_RETURN)) {
            // Set 'VAL' and return
            return append(
                    new InstSequenceBuilder().
                            setType(ASSIGN).
                            setReg(VAL).
                            addParameter(op("compiled-procedure-entry")).
                            addParameter(reg(PROC)).
                            build(),
                    call(new InstSequenceBuilder().setType(GOTO).addParameter(reg(VAL)).build())
            );
        } else {
            // Set Target and return
            throw new IllegalStateException("Impossible");
        }
    }

    @Override
    public InstSequence visit(QuoteEvaluation e, Context context) {
        InstSequence instSequence = new InstSequenceBuilder().
                setType(ASSIGN).
                setReg(context.getTarget()).
                addParameter(conzt("(quote " + e.toString() + ")")).
                build();
        return endWithLinkage(context.getLinkage(), instSequence);
    }

    @Override
    public InstSequence visit(VariableEvaluation e, Context context) {
        InstSequence instSequence = new InstSequenceBuilder().
                setType(ASSIGN).
                setReg(context.getTarget()).
                addParameter(op("lookup-variable-value")).
                addParameter(conzt("(quote " + e.getName() + ")")).
                addParameter(reg(ENV)).
                build();
        return endWithLinkage(context.getLinkage(), instSequence);
    }

    @Override
    public InstSequence visit(AssignmentEvaluation e, Context context) {
        InstSequence getValueInsts = e.getValue().accept(this, new Context(VAL, LINKAGE_NEXT));

        InstSequence instSequence = append(
                new InstSequenceBuilder().
                        setType(PERFORM).
                        addParameter(op("set-variable-value!")).
                        addParameter(conzt("(quote "+e.getName()+")")).
                        addParameter(reg(VAL)).
                        addParameter(reg(ENV)).
                        build(),
                new InstSequenceBuilder().
                        setType(ASSIGN).
                        setReg(context.getTarget()).
                        addParameter(conzt("(quote ok)")).
                        build()
        );
        return endWithLinkage(context.getLinkage(), preserving(getValueInsts, instSequence, ENV));
    }

    @Override
    public InstSequence visit(AMBEvaluation e, Context context) {
        throw new IllegalStateException("Compile an AMBEvaluation.");
    }

    @Override
    public InstSequence visit(TrivialEvaluation e, Context context) {
        throw new IllegalStateException("Compile a TrivialEvaluation.");
    }
}
