package evaluator;

import parser.ExpressionVisitor;
import parser.NilExpression;
import parser.PairExpression;
import parser.SymbolExpression;

import java.util.List;
import java.util.stream.Collectors;

public class ApplicativeEvaluator implements EvaluationVisitor<Environment, Primitive> {
    private final Environment globalEnv = Environment.createGlobalEnvironment();

    @Override
    public Primitive visit(Evaluation e) {
        return e.accept(this, globalEnv);
    }

    @Override
    public Primitive visit(BeginEvaluation e, Environment env) {
        return e.getEvaluations().stream().
                map(exp -> exp.accept(this, env)).
                reduce((f, s) -> s). // Get last
                        orElse(NothingPrimitive.SINGLETON);
    }

    @Override
    public Primitive visit(DefineEvaluation e, Environment env) {
        return env.define(e.getName(), e.getValue().accept(this, env));
    }

    @Override
    public Primitive visit(IfEvaluation e, Environment env) {
        if (e.getCondition().accept(this, env).accept(DefaultPrimitiveVisitor.expectBooleanPrimitive).getValue()) {
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
        List<Primitive> parameters = e.getParameters().stream().map((exp) -> exp.accept(this, env)).collect(Collectors.toList());
        return operator.accept(new DefaultPrimitiveVisitor<>() {
            @Override
            public Primitive visit(LambdaPrimitive primitive) {
                Environment closure = primitive.getClosure().extend();
                closure.putParameters(primitive.getArguments(), parameters);
                return primitive.getEvaluation().accept(ApplicativeEvaluator.this, closure);
            }

            @Override
            public Primitive visit(BuiltinPrimitive primitive) {
                return primitive.eval(parameters);
            }
        });
    }

    @Override
    public Primitive visit(QuoteEvaluation e, Environment env) {
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

        return e.getQuote().accept(convertor);
    }

    @Override
    public Primitive visit(VariableEvaluation e, Environment env) {
        return env.lookup(e.getName());
    }

    @Override
    public Primitive visit(AssignmentEvaluation e, Environment env) {
        return env.set(e.getName(), e.getValue().accept(this, env));
    }

    @Override
    public Primitive visit(AMBEvaluation e, Environment env) {
        throw new IllegalStateException("AMB operation is not supported by ApplicativeEvaluator.");
    }

    @Override
    public Primitive visit(TrivialEvaluation e, Environment environment) {
        return e.getPrimitive();
    }

    public Environment getGlobalEnv() {
        return globalEnv;
    }
}
