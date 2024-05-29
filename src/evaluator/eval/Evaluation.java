package evaluator.eval;

public interface Evaluation {
    Primitive accept(EvaluationEvaluator evaluator, Environment env);
}
