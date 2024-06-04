package evaluator;

public interface Primitive {
    <T> T accept(PrimitiveVisitor<T> visitor);
}
