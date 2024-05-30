package machine;

public interface PrimitiveVisitor<T> {
    T visit(NumberPrimitive primitive);
    T visit(BooleanPrimitive primitive);
    T visit(NothingPrimitive primitive);
    T visit(PairPrimitive primitive);
    T visit(QuotePrimitive primitive);
}
