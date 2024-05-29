package evaluator.neval;


public interface Primitive {
    <T> T accept(PrimitiveVisitor<T> visitor);
}
