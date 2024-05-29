package evaluator.eval;

import java.util.List;
import java.util.stream.Collectors;

public class ApplicativeEvaluator implements EvaluationEvaluator {
    private final Environment globalEnv = Environment.createGlobalEnvironment();

    @Override
    public Primitive eval(Evaluation e){
        return e.accept(this, globalEnv);
    }

    @Override
    public Primitive eval(BeginEvaluation e, Environment env) {
        return e.getEvaluations().stream().
                map(exp->exp.accept(this, env)).
                reduce((f,s)->s). // Get last
                orElse(NothingPrimitive.SINGLETON);
    }

    @Override
    public Primitive eval(DefineEvaluation e, Environment env) {
        return env.define(e.getName(), e.getValue().accept(this, env));
    }

    @Override
    public Primitive eval(IfEvaluation e, Environment env) {
        if(e.getCondition().accept(this, env).accept(new DefaultPrimitiveVisitor<>() {
            @Override
            public Boolean visit(BooleanPrimitive primitive) {
                return primitive.isValue();
            }
        })){
            return e.getConsequent().accept(this, env);
        } else {
            return e.getAlternative().accept(this, env);
        }
    }

    @Override
    public Primitive eval(LambdaEvaluation e, Environment env) {
        return new LambdaPrimitive(e.getArguments(), e.getBody(), env);
    }

    @Override
    public Primitive eval(NumberEvaluation e, Environment env) {
        return new NumberPrimitive(e.getValue());
    }

    @Override
    public Primitive eval(ProcedureEvaluation e, Environment env) {
        Primitive operator = e.getOperator().accept(this, env);
        List<Primitive> parameters = e.getParameters().stream().map((exp)->exp.accept(this, env)).collect(Collectors.toList());

        return operator.accept(new DefaultPrimitiveVisitor<>(){
            @Override
            public Primitive visit(LambdaPrimitive primitive) {
                Environment closure = primitive.getClosure().extend();
                closure.putParameters(primitive.getArguments(), parameters);
                return primitive.getEvaluation().accept(ApplicativeEvaluator.this, closure);
            }

            @Override
            public Primitive visit(BuiltinPrimitive primitive) {
                return primitive.eval(parameters, env);
            }
        });
    }

    @Override
    public Primitive eval(QuoteEvaluation e, Environment env) {
        return new QuotePrimitive(e.getName());
    }

    @Override
    public Primitive eval(VariableEvaluation e, Environment env) {
        return env.lookup(e.getName());
    }
}
