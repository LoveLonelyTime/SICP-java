package machine;

public abstract class DefaultPrimitiveVisitor<T> implements PrimitiveVisitor<T> {

    public static final PrimitiveVisitor<NumberPrimitive> getNumber = new DefaultPrimitiveVisitor<>() {
        @Override
        public NumberPrimitive visit(NumberPrimitive primitive) {
            return primitive;
        }
    };

    public static final PrimitiveVisitor<BooleanPrimitive> getBoolean = new DefaultPrimitiveVisitor<>() {
        @Override
        public BooleanPrimitive visit(BooleanPrimitive primitive) {
            return primitive;
        }
    };

    public static final PrimitiveVisitor<NothingPrimitive> getNothing = new DefaultPrimitiveVisitor<>() {
        @Override
        public NothingPrimitive visit(NothingPrimitive primitive) {
            return primitive;
        }
    };

    public static final PrimitiveVisitor<PairPrimitive> getPair = new DefaultPrimitiveVisitor<>() {
        @Override
        public PairPrimitive visit(PairPrimitive primitive) {
            return primitive;
        }
    };

    public static final PrimitiveVisitor<QuotePrimitive> getQuote = new DefaultPrimitiveVisitor<>() {
        @Override
        public QuotePrimitive visit(QuotePrimitive primitive) {
            return primitive;
        }
    };

    @Override
    public T visit(NumberPrimitive primitive) {
        throw new IllegalStateException("No implemented.");
    }

    @Override
    public T visit(BooleanPrimitive primitive) {
        throw new IllegalStateException("No implemented.");
    }

    @Override
    public T visit(NothingPrimitive primitive) {
        throw new IllegalStateException("No implemented.");
    }

    @Override
    public T visit(PairPrimitive primitive) {
        throw new IllegalStateException("No implemented.");
    }

    @Override
    public T visit(QuotePrimitive primitive) {
        throw new IllegalStateException("No implemented.");
    }
}
