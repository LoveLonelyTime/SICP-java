package evaluator.eval;

import java.util.List;
import java.util.stream.Collectors;

public class LazyEvaluator implements EvaluationVisitor<Environment, Primitive> {
    private final Environment globalEnv = Environment.createGlobalEnvironment();

    @Override
    public Primitive visit(Evaluation e) {
        return force(e.accept(this, globalEnv));
    }

    private Primitive force(Primitive p) {
        return p.accept(new PrimitiveVisitor<>() {
            @Override
            public Primitive visit(BooleanPrimitive primitive) {
                return primitive;
            }

            @Override
            public Primitive visit(BuiltinPrimitive primitive) {
                return primitive;
            }

            @Override
            public Primitive visit(LambdaPrimitive primitive) {
                return primitive;
            }

            @Override
            public Primitive visit(NothingPrimitive primitive) {
                return primitive;
            }

            @Override
            public Primitive visit(NumberPrimitive primitive) {
                return primitive;
            }

            @Override
            public Primitive visit(QuotePrimitive primitive) {
                return primitive;
            }

            @Override
            public Primitive visit(ThunkPrimitive primitive) {
                return force(primitive.getEvaluation().accept(LazyEvaluator.this, primitive.getClosure()));
            }
        });
    }

    @Override
    public Primitive visit(BeginEvaluation e, Environment env) {
        return e.getEvaluations().stream().
                map(exp -> exp.accept(this, env)).
                reduce((f, s) -> s) // Get last
                .orElse(NothingPrimitive.SINGLETON);
    }

    @Override
    public Primitive visit(DefineEvaluation e, Environment env) {
        return env.define(e.getName(), e.getValue().accept(this, env));
    }

    @Override
    public Primitive visit(IfEvaluation e, Environment env) {
        if (force(e.getCondition().accept(this, env)).accept(new DefaultPrimitiveVisitor<>() {
            @Override
            public Boolean visit(BooleanPrimitive primitive) {
                return primitive.isValue();
            }
        })) {
            return e.getConsequent().accept(this, env);
        } else {
            return e.getAlternative().accept(this, env);
        }
    }

    @Override
    public Primitive visit(LambdaEvaluation e, Environment env) {
        return new LambdaPrimitive(e.getArguments(), e.getBody(), env);
    }

    @Override
    public Primitive visit(NumberEvaluation e, Environment env) {
        return new NumberPrimitive(e.getValue());
    }

    @Override
    public Primitive visit(ProcedureEvaluation e, Environment env) {
        Primitive operator = e.getOperator().accept(this, env);

        return operator.accept(new DefaultPrimitiveVisitor<>() {
            @Override
            public Primitive visit(LambdaPrimitive primitive) {
                // Thunk
                List<Primitive> thunkParameters = e.getParameters().stream().
                        map((exp) -> new ThunkPrimitive(exp, env)).
                        collect(Collectors.toList());

                Environment closure = primitive.getClosure().extend();
                closure.putParameters(primitive.getArguments(), thunkParameters);
                return primitive.getEvaluation().accept(LazyEvaluator.this, closure);
            }

            @Override
            public Primitive visit(BuiltinPrimitive primitive) {
                List<Primitive> parameters = e.getParameters().stream().
                        map((exp) -> exp.accept(LazyEvaluator.this, env)).
                        map(LazyEvaluator.this::force). // Force
                                collect(Collectors.toList());
                return primitive.eval(parameters, env);
            }
        });
    }

    @Override
    public Primitive visit(QuoteEvaluation e, Environment env) {
        return new QuotePrimitive(e.getName());
    }

    @Override
    public Primitive visit(VariableEvaluation e, Environment env) {
        return env.lookup(e.getName());
    }
}
