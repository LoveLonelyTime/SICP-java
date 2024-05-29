package evaluator.neval;

public interface Evaluation {
    Primitive accept(EvaluationEvaluator evaluator, Environment env, Successful successful, Failed failed);
}
