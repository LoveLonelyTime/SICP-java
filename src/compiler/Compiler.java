package compiler;

import evaluator.eval.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class Compiler implements EvaluationVisitor<Context, InstSequence> {

    private int counter = 0;
    private InstSequence compileLinkage(String linkage){
        if(linkage.equals(Context.LINKAGE_RETURN)){
            return new InstSequenceBuilder().
                    addNeeds("continue").
                    addInst("(goto (reg continue))").
                    build();
        } else if(linkage.equals(Context.LINKAGE_NEXT)){
            return new InstSequenceBuilder().
                    build();
        } else {
            return new InstSequenceBuilder().
                    addInst(String.format("(goto (label %s))", linkage)).
                    build();
        }
    }

    private InstSequence endWithLinkage(String linkage, InstSequence instSequence){
        return preserving(instSequence, compileLinkage(linkage), "continue");
    }

    private InstSequence preserving(InstSequence i1, InstSequence i2, String... preserved){
        if (preserved.length == 0){
            return new InstSequenceBuilder().merge(i1).merge(i2).build();
        } else {
            if(i1.getModifies().contains(preserved[0]) && i2.getNeeds().contains(preserved[0])){
                InstSequence wrapped = new InstSequenceBuilder().
                        addInst(String.format("(save %s)", preserved[0])).
                        merge(i1).
                        addInst(String.format("(restore %s)", preserved[0])).
                        build();
                wrapped.getNeeds().add(preserved[0]);
                wrapped.getModifies().remove(preserved[0]);
                return preserving(wrapped, i2, Arrays.copyOfRange(preserved, 1, preserved.length));
            } else {
                return preserving(i1, i2, Arrays.copyOfRange(preserved, 1, preserved.length));
            }
        }
    }

    private InstSequence tackOnInstSequence(InstSequence i1, InstSequence i2){
        InstSequence instSequence = new InstSequenceBuilder().merge(i1).build();
        instSequence.getStatements().addAll(i2.getStatements());
        return instSequence;
    }

    private InstSequence parallelInstSequence(InstSequence i1, InstSequence i2){
        InstSequence instSequence = new InstSequenceBuilder().merge(i1).build();
        instSequence.getNeeds().addAll(i2.getNeeds());
        instSequence.getModifies().addAll(i2.getModifies());
        instSequence.getStatements().addAll(i2.getStatements());
        return instSequence;
    }

    private String makeLabel(String label){
        counter++;
        return label + "-" + counter;
    }

    @Override
    public InstSequence visit(Evaluation e) {
        return new InstSequenceBuilder().
                addModifies("env").
                addInst("(assign env (op get-global-environment))").
                merge(e.accept(this, new Context("val", Context.LINKAGE_NEXT))).
                addNeeds("val").
                addInst("(perform (op print!) (reg val))").
                build();
    }

    @Override
    public InstSequence visit(BeginEvaluation e, Context context) {
        InstSequence instSequence = new InstSequenceBuilder().build();
        for(int i = e.getEvaluations().size() - 1 ; i >= 0; i--){
            InstSequence parallel = e.getEvaluations().get(i).accept(this, new Context(context.getTarget(), Context.LINKAGE_NEXT));
            instSequence = preserving(parallel, instSequence, "env");
        }
        return endWithLinkage(context.getLinkage(), instSequence);
    }

    @Override
    public InstSequence visit(DefineEvaluation e, Context context) {
        InstSequence getValueInsts = e.getValue().accept(this, new Context("val", Context.LINKAGE_NEXT));
        InstSequence instSequence = new InstSequenceBuilder().
                addNeeds("env", "val").
                addModifies(context.getTarget()).
                addInst(String.format("(perform (op define-variable!) (const %s) (reg val) (reg env))", e.getName())).
                addInst(String.format("(assign %s (const ok))", context.getTarget())).
                build();

        return endWithLinkage(context.getLinkage(), preserving(getValueInsts, instSequence, "env"));
    }

    @Override
    public InstSequence visit(IfEvaluation e, Context context) {
        String afterIf = makeLabel("after-if");
        String trueBranch = makeLabel("true-branch");
        String falseBranch = makeLabel("false-branch");
        InstSequence getConditionInsts = e.getCondition().accept(this, new Context("val", Context.LINKAGE_NEXT));
        InstSequence consequentInsts = e.getConsequent().accept(this, new Context(context.getTarget(), context.getLinkage().equals(Context.LINKAGE_NEXT) ? afterIf : context.getLinkage()));
        InstSequence alternativeInsts = e.getAlternative().accept(this, new Context(context.getTarget(), context.getLinkage()));

        consequentInsts = new InstSequenceBuilder().addInst(trueBranch).merge(consequentInsts).build();
        alternativeInsts = new InstSequenceBuilder().addInst(falseBranch).merge(alternativeInsts).build();

        InstSequence instSequence = new InstSequenceBuilder().
                addNeeds("val").
                addInst("(test (op false?) (reg val))").
                addInst(String.format("(branch (label %s))", falseBranch)).
                merge(parallelInstSequence(consequentInsts, alternativeInsts)).
                addInst(afterIf).
                build();
        return preserving(getConditionInsts, instSequence, "env", "continue");
    }

    @Override
    public InstSequence visit(LambdaEvaluation e, Context context) {
        String entry = makeLabel("lambda-entry");
        String afterLambda = makeLabel("after-lambda");

        InstSequence instSequence = new InstSequenceBuilder().
                addNeeds("env").
                addModifies(context.getTarget()).
                addInst(String.format("(assign %s (op make-compiled-procedure) (label %s) (reg env))", context.getTarget(), entry)).
                build();

        instSequence = endWithLinkage(context.getLinkage().equals(Context.LINKAGE_NEXT) ? afterLambda : context.getLinkage(), instSequence);
        instSequence = tackOnInstSequence(instSequence, buildLambdaBody(e, entry));
        return new InstSequenceBuilder().merge(instSequence).addInst(afterLambda).build();
    }

    private InstSequence buildLambdaBody(LambdaEvaluation e, String entry){
        String arguments = e.getArguments().stream().map((name) -> String.format("(const %s)",name)).collect(Collectors.joining(" "));
        InstSequence instSequence = e.getBody().accept(this, new Context("val", Context.LINKAGE_RETURN));
        return new InstSequenceBuilder().
                addNeeds("proc", "argl").
                addModifies("env", "val").
                addInst(entry).
                addInst("(assign env (op compiled-procedure-env) (reg proc))").
                addInst(String.format("(assign val (op list) %s)", arguments)).
                addInst("(assign env (op extend-environment) (reg val) (reg argl) (reg env))").
                merge(instSequence).
                build();
    }

    @Override
    public InstSequence visit(NumberEvaluation e, Context context) {
        // Warning: Double -> Int
        InstSequence instSequence = new InstSequenceBuilder().
                addModifies(context.getTarget()).
                addInst(String.format("(assign %s (const %s))", context.getTarget(), (int) e.getValue())).
                build();
        return endWithLinkage(context.getLinkage(), instSequence);
    }

    @Override
    public InstSequence visit(ProcedureEvaluation e, Context context) {
        InstSequence operatorInsts = e.getOperator().accept(this, new Context("proc", Context.LINKAGE_NEXT));
        List<Evaluation> parameters = new ArrayList<>(e.getParameters()); // Reverse
        Collections.reverse(parameters);
        InstSequence constructArgListInsts = constructArgList(parameters);
        InstSequence proc = procedureCall(context);
        return preserving(operatorInsts, preserving(constructArgListInsts, proc, "proc", "continue", "env"), "env", "continue");
    }

    private InstSequence constructArgList(List<Evaluation> parameters){
        if(parameters.isEmpty()){
            return new InstSequenceBuilder().
                addModifies("argl").
                addInst("(assign argl (op nil))").
                build();
        } else {
            InstSequence getLastArg = new InstSequenceBuilder().
                    addNeeds("val").
                    addModifies("argl").
                    merge(parameters.get(0).accept(this, new Context("val", Context.LINKAGE_NEXT))).
                    addInst("(assign argl (op nil))").
                    addInst("(assign argl (op cons) (reg val) (reg argl))").
                    build();
            if(parameters.size() == 1){
                return getLastArg;
            } else {
                return preserving(getLastArg, constructRestArgList(parameters.subList(1, parameters.size())), "env");
            }
        }
    }

    private InstSequence constructRestArgList(List<Evaluation> parameters){
        InstSequence instSequence = preserving(parameters.get(0).accept(this, new Context("val", Context.LINKAGE_NEXT)),
                new InstSequenceBuilder().
                    addNeeds("val", "argl").
                    addModifies("argl").
                    addInst("(assign argl (op cons) (reg val) (reg argl))").
                    build(), "argl");
        if(parameters.size() == 1){
            return instSequence;
        } else {
            return preserving(instSequence, constructRestArgList(parameters.subList(1, parameters.size())), "env");
        }
    }

    private InstSequence procedureCall(Context context){
        String primitiveBranch = makeLabel("primitive-branch");
        String compiledBranch = makeLabel("compiled-branch");
        String afterCall = makeLabel("after-call");

        InstSequence compiledCall = new InstSequenceBuilder().
                addInst(compiledBranch).
                merge(compileCall(new Context(context.getTarget(), context.getLinkage().equals(Context.LINKAGE_NEXT) ? afterCall : context.getLinkage()))).
                build();

        InstSequence primitiveCall = new InstSequenceBuilder().
                addNeeds("argl", "env").
                addModifies("target").
                addInst(primitiveBranch).
                addInst(String.format("(assign %s (op apply-primitive-procedure) (reg proc) (reg argl) (reg env))", context.getTarget())).
                build();

        return new InstSequenceBuilder().
                addNeeds("proc").
                addInst("(test (op primitive-procedure?) (reg proc))").
                addInst(String.format("(branch (label %s))", primitiveBranch)).
                merge(parallelInstSequence(compiledCall, endWithLinkage(context.getLinkage(), primitiveCall))).
                addInst(afterCall).
                build();
    }

    private InstSequence compileCall(Context context){
        if(context.getTarget().equals("val") && !context.getLinkage().equals(Context.LINKAGE_RETURN)){
            return new InstSequenceBuilder().
                    addNeeds("proc").
                    addModifies("env", "val", "proc", "argl", "continue"). // all regs
                    addInst(String.format("(assign continue (label %s))", context.getLinkage())).
                    addInst("(assign val (op compiled-procedure-entry) (reg proc))").
                    addInst("(goto (reg val))").
                    build();
        } else if(!context.getTarget().equals("val") && !context.getLinkage().equals(Context.LINKAGE_RETURN)) {
            String procReturn = makeLabel("proc-return");
            return new InstSequenceBuilder().
                    addNeeds("proc").
                    addModifies("env", "val", "proc", "argl", "continue"). // all regs
                    addInst(String.format("(assign continue (label %s))", procReturn)).
                    addInst("(assign val (op compiled-procedure-entry) (reg proc))").
                    addInst("(goto (reg val))").
                    addInst(procReturn).
                    addInst(String.format("(assign %s (reg val))", context.getTarget())).
                    addInst(String.format("(goto (label %s))", context.getLinkage())).
                    build();
        } else if(context.getTarget().equals("val") && context.getLinkage().equals(Context.LINKAGE_RETURN)){
            return new InstSequenceBuilder().
                    addNeeds("proc", "continue").
                    addModifies("env", "val", "proc", "argl", "continue"). // all regs
                    addInst("(assign val (op compiled-procedure-entry) (reg proc))").
                    addInst("(goto (reg val))").
                    build();
        } else {
            String procReturn = makeLabel("proc-return");
            return new InstSequenceBuilder().
                    addNeeds("proc", "continue").
                    addModifies("env", "val", "proc", "argl", "continue"). // all regs
                    addInst("(save continue)").
                    addInst(String.format("(assign continue (label %s))", procReturn)).
                    addInst("(assign val (op compiled-procedure-entry) (reg proc))").
                    addInst("(goto (reg val))").
                    addInst(procReturn).
                    addInst(String.format("(assign %s (reg val))", context.getTarget())).
                    addInst("(restore continue)").
                    addInst("(goto (reg continue))").
                    build();
        }
    }

    @Override
    public InstSequence visit(QuoteEvaluation e, Context context) {
        InstSequence instSequence = new InstSequenceBuilder().
                addModifies(context.getTarget()).
                addInst(String.format("(assign %s (const %s))", context.getTarget(), e.getName())).
                build();
        return endWithLinkage(context.getLinkage(), instSequence);
    }

    @Override
    public InstSequence visit(VariableEvaluation e, Context context) {
        InstSequence instSequence = new InstSequenceBuilder().
                addNeeds("env").
                addModifies(context.getTarget()).
                addInst(String.format("(assign %s (op lookup-variable-value) (const %s) (reg env))", context.getTarget(), e.getName())).
                build();
        return endWithLinkage(context.getLinkage(), instSequence);
    }
}
