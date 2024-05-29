package evaluator.neval;

public interface PrimitiveVisitor<T> {
    T visit(BooleanPrimitive primitive);
    T visit(BuiltinPrimitive primitive);
    T visit(LambdaPrimitive primitive);
    T visit(NothingPrimitive primitive);
    T visit(NumberPrimitive primitive);
    T visit(QuotePrimitive primitive);
}
