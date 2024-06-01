package evaluator.eval;

public interface Evaluation {
    <T, R> R accept(EvaluationVisitor<T, R> visitor, T t);
}
