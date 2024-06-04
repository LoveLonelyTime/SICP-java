package evaluator;

import parser.ExpressionVisitor;
import parser.NilExpression;
import parser.PairExpression;
import parser.SymbolExpression;

import java.util.List;
import java.util.Scanner;

public class AMBEvaluator implements EvaluationVisitor<AMBPath, Primitive> {

    private final Environment globalEnv = Environment.createGlobalEnvironment();

    @Override
    public Primitive visit(Evaluation e) {
        return e.accept(this, new AMBPath(globalEnv, (value, failed) -> {
            System.out.println("-> " + value);
            System.out.print("next?>>");
            String answer = new Scanner(System.in).nextLine();
            if (answer.equals("y")) {
                return failed.call();
            } else {
                return value;
            }
        }, () -> {
            System.out.println("-> Exhausted.");
            return NothingPrimitive.SINGLETON;
        }));
    }

    private Primitive evalList(List<Evaluation> evaluations, int idx, Environment env, Successful successful, Failed failed) {
        if (idx >= evaluations.size()) {
            return successful.call(NothingPrimitive.SINGLETON, failed);
        } else if (idx == evaluations.size() - 1) {
            return evaluations.get(idx).accept(this, new AMBPath(env, successful, failed));
        } else {
            return evaluations.get(idx).accept(this, new AMBPath(env,
                    (value, wrappedFailed) -> evalList(evaluations, idx + 1, env, successful, wrappedFailed)
                    , failed));
        }
    }

    @Override
    public Primitive visit(BeginEvaluation e, AMBPath ambPath) {
        return evalList(e.getEvaluations(), 0, ambPath.getEnvironment(), ambPath.getSuccessful(), ambPath.getFailed());
    }

    @Override
    public Primitive visit(DefineEvaluation e, AMBPath ambPath) {
        return e.getValue().accept(this, new AMBPath(ambPath.getEnvironment(),
                (value, failed) ->
                        ambPath.getSuccessful().call(ambPath.getEnvironment().define(e.getName(), value), failed)
                , ambPath.getFailed()));
    }

    @Override
    public Primitive visit(IfEvaluation e, AMBPath ambPath) {
        return e.getCondition().accept(this, new AMBPath(ambPath.getEnvironment(),
                (value, failed) -> {
                    if (value.accept(DefaultPrimitiveVisitor.expectBooleanPrimitive).getValue()) {
                        return e.getCondition().accept(this, new AMBPath(ambPath.getEnvironment(), ambPath.getSuccessful(), failed));
                    } else {
                        return e.getAlternative().accept(this, new AMBPath(ambPath.getEnvironment(), ambPath.getSuccessful(), failed));
                    }
                }, ambPath.getFailed()));
    }

    @Override
    public Primitive visit(LambdaEvaluation e, AMBPath ambPath) {
        return ambPath.getSuccessful().call(new LambdaPrimitive(e.getArguments(), e.getBody(), ambPath.getEnvironment()), ambPath.getFailed());
    }

    @Override
    public Primitive visit(NumberEvaluation e, AMBPath ambPath) {
        return ambPath.getSuccessful().call(new NumberPrimitive(e.getValue()), ambPath.getFailed());
    }

    private Primitive evalParameters(Primitive operator, List<Evaluation> parameters, Primitive[] evaluated, int idx, Environment env, Successful successful, Failed failed) {
        if (idx >= parameters.size()) {
            return operator.accept(new DefaultPrimitiveVisitor<>() {
                @Override
                public Primitive visit(LambdaPrimitive primitive) {
                    Environment closure = primitive.getClosure().extend();
                    closure.putParameters(primitive.getArguments(), List.of(evaluated));
                    return primitive.getEvaluation().accept(AMBEvaluator.this, new AMBPath(env, successful, failed));
                }

                @Override
                public Primitive visit(BuiltinPrimitive primitive) {
                    return successful.call(primitive.eval(List.of(evaluated)), failed);
                }
            });
        } else {
            return parameters.get(idx).accept(this, new AMBPath(env,
                    (value, wrappedFailed) -> {
                        evaluated[idx] = value;
                        return evalParameters(operator, parameters, evaluated, idx + 1, env, successful, wrappedFailed);
                    }, failed));
        }
    }

    @Override
    public Primitive visit(ProcedureEvaluation e, AMBPath ambPath) {
        Primitive[] evaluated = new Primitive[e.getParameters().size()];
        return e.getOperator().accept(this, new AMBPath(ambPath.getEnvironment(),
                (value, failed) -> evalParameters(value, e.getParameters(), evaluated, 0, ambPath.getEnvironment(), ambPath.getSuccessful(), failed)
                , ambPath.getFailed()));
    }

    @Override
    public Primitive visit(QuoteEvaluation e, AMBPath ambPath) {
        ExpressionVisitor<Primitive> convertor = new ExpressionVisitor<>() {
            @Override
            public Primitive visit(SymbolExpression expression) {
                return new SymbolPrimitive(expression.getSymbol());
            }

            @Override
            public Primitive visit(PairExpression expression) {
                return new PairPrimitive(expression.getHead().accept(this), expression.getTail().accept(this));
            }

            @Override
            public Primitive visit(NilExpression expression) {
                return NothingPrimitive.SINGLETON;
            }
        };

        return ambPath.getSuccessful().call(e.getQuote().accept(convertor), ambPath.getFailed());
    }

    @Override
    public Primitive visit(VariableEvaluation e, AMBPath ambPath) {
        return ambPath.getSuccessful().call(ambPath.getEnvironment().lookup(e.getName()), ambPath.getFailed());
    }

    @Override
    public Primitive visit(AssignmentEvaluation e, AMBPath ambPath) {
        Primitive origin = ambPath.getEnvironment().lookup(e.getName());
        return e.getValue().accept(this, new AMBPath(ambPath.getEnvironment(),
                (value, failed) -> ambPath.getSuccessful().call(ambPath.getEnvironment().set(e.getName(), value),
                        () -> { // Backtrack
                            ambPath.getEnvironment().set(e.getName(), origin);
                            return failed.call();
                        }
                ), ambPath.getFailed()));
    }

    private Primitive chooseAMB(List<Evaluation> alternatives, int idx, Environment env, Successful successful, Failed failed) {
        if (idx >= alternatives.size()) {
            return failed.call();
        } else {
            return alternatives.get(idx).accept(this, new AMBPath(env, successful, () -> chooseAMB(alternatives, idx + 1, env, successful, failed)));
        }
    }

    @Override
    public Primitive visit(AMBEvaluation e, AMBPath ambPath) {
        return chooseAMB(e.getAlternatives(), 0, ambPath.getEnvironment(), ambPath.getSuccessful(), ambPath.getFailed());
    }

    @Override
    public Primitive visit(TrivialEvaluation e, AMBPath ambPath) {
        return ambPath.getSuccessful().call(e.getPrimitive(), ambPath.getFailed());
    }

    public Environment getGlobalEnv() {
        return globalEnv;
    }
}
