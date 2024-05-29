package evaluator.eval;

public class DefaultPrimitiveVisitor<T> implements PrimitiveVisitor<T> {
    @Override
    public T visit(BooleanPrimitive primitive) {
        throw new IllegalStateException("No implemented.");
    }

    @Override
    public T visit(BuiltinPrimitive primitive) {
        throw new IllegalStateException("No implemented.");
    }

    @Override
    public T visit(LambdaPrimitive primitive) {
        throw new IllegalStateException("No implemented.");
    }

    @Override
    public T visit(NothingPrimitive primitive) {
        throw new IllegalStateException("No implemented.");
    }

    @Override
    public T visit(NumberPrimitive primitive) {
        throw new IllegalStateException("No implemented.");
    }

    @Override
    public T visit(QuotePrimitive primitive) {
        throw new IllegalStateException("No implemented.");
    }

    @Override
    public T visit(ThunkPrimitive primitive) {
        throw new IllegalStateException("No implemented.");
    }
}
