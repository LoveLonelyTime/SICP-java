package evaluator.neval;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AMBEvaluator implements EvaluationEvaluator {
    private final Environment globalEnv = Environment.createGlobalEnvironment();

    @Override
    public Primitive eval(Evaluation e, Successful successful, Failed failed) {
        return e.accept(this, globalEnv, successful, failed);
    }

    private Primitive evalList(List<Evaluation> evaluations, Environment env, Successful successful, Failed failed){
        if(evaluations.isEmpty()) {
            return successful.call(NothingPrimitive.SINGLETON, failed);
        } else if(evaluations.size() == 1){
            return evaluations.get(0).accept(this, env, successful, failed);
        }
        return evaluations.get(0).accept(this, env, (v, f) -> evalList(evaluations.subList(1, evaluations.size()), env, successful, f), failed);
    }

    @Override
    public Primitive eval(BeginEvaluation e, Environment env, Successful successful, Failed failed) {
        return evalList(e.getEvaluations(), env, successful, failed);
    }

    @Override
    public Primitive eval(DefineEvaluation e, Environment env, Successful successful, Failed failed) {
        return e.getValue().accept(this, env, (v ,f)-> successful.call(env.define(e.getName(), v), f), failed);
    }

    @Override
    public Primitive eval(IfEvaluation e, Environment env, Successful successful, Failed failed) {
        return e.getCondition().accept(this, env, (v, f)->{
            if(v.accept(new DefaultPrimitiveVisitor<>(){
                @Override
                public Boolean visit(BooleanPrimitive primitive) {
                    return primitive.isValue();
                }
            })){
                return e.getConsequent().accept(this, env, successful, f);
            } else {
                return e.getAlternative().accept(this, env, successful, f);
            }
        }, failed);
    }

    @Override
    public Primitive eval(LambdaEvaluation e, Environment env, Successful successful, Failed failed) {
        return successful.call(new LambdaPrimitive(e.getArguments(), e.getBody(), env), failed);
    }

    @Override
    public Primitive eval(NumberEvaluation e, Environment env, Successful successful, Failed failed) {
        return successful.call(new NumberPrimitive(e.getValue()), failed);
    }

    private Primitive evalParameters(Primitive operator, List<Evaluation> parameters, List<Primitive> evaluatedParameters, Environment env, Successful successful, Failed failed){
        if(parameters.isEmpty()){
            return operator.accept(new DefaultPrimitiveVisitor<>(){
                @Override
                public Primitive visit(LambdaPrimitive primitive) {
                    Environment closure = primitive.getClosure().extend();
                    closure.putParameters(primitive.getArguments(), evaluatedParameters);
                    return primitive.getEvaluation().accept(AMBEvaluator.this, closure, successful, failed);
                }

                @Override
                public Primitive visit(BuiltinPrimitive primitive) {
                    return primitive.eval(evaluatedParameters, env, successful, failed);
                }
            });
        }else{
            return parameters.get(0).accept(this, env, (v, f)-> evalParameters(operator, parameters.subList(1, parameters.size()), Stream.concat(evaluatedParameters.stream(), Stream.of(v)).collect(Collectors.toList()), env, successful, f), failed);
        }
    }

    @Override
    public Primitive eval(ProcedureEvaluation e, Environment env, Successful successful, Failed failed) {
        return e.getOperator().accept(this, env, (v, f)-> evalParameters(v, e.getParameters(), new ArrayList<>(), env, successful, f), failed);
    }

    @Override
    public Primitive eval(QuoteEvaluation e, Environment env, Successful successful, Failed failed) {
        return successful.call(new QuotePrimitive(e.getName()), failed);
    }

    @Override
    public Primitive eval(VariableEvaluation e, Environment env, Successful successful, Failed failed) {
        return successful.call(env.lookup(e.getName()), failed);
    }

    public Primitive chooseAMB(List<Evaluation> alternatives, Environment env, Successful successful, Failed failed){
        if(alternatives.isEmpty()){
            return failed.call();
        }else{
            return alternatives.get(0).accept(this, env, successful, () ->
                    chooseAMB(alternatives.subList(1,alternatives.size()), env, successful, failed));
        }
    }

    @Override
    public Primitive eval(AMBEvaluation e, Environment env, Successful successful, Failed failed) {
        return chooseAMB(e.getAlternatives(), env, successful, failed);
    }
}
