package evaluator.eval;

public interface Primitive {
    <T> T accept(PrimitiveVisitor<T> visitor);
}
