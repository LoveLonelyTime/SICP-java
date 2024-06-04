package evaluator;

public abstract class DefaultPrimitiveVisitor<T> implements PrimitiveVisitor<T> {

    public static final PrimitiveVisitor<BooleanPrimitive> expectBooleanPrimitive = new DefaultPrimitiveVisitor<>() {
        @Override
        public BooleanPrimitive visit(BooleanPrimitive primitive) {
            return primitive;
        }
    };

    public static final PrimitiveVisitor<BuiltinPrimitive> expectBuiltinPrimitive = new DefaultPrimitiveVisitor<>() {
        @Override
        public BuiltinPrimitive visit(BuiltinPrimitive primitive) {
            return primitive;
        }
    };

    public static final PrimitiveVisitor<LambdaPrimitive> expectLambdaPrimitive = new DefaultPrimitiveVisitor<>() {
        @Override
        public LambdaPrimitive visit(LambdaPrimitive primitive) {
            return primitive;
        }
    };

    public static final PrimitiveVisitor<NothingPrimitive> expectNothingPrimitive = new DefaultPrimitiveVisitor<>() {
        @Override
        public NothingPrimitive visit(NothingPrimitive primitive) {
            return primitive;
        }
    };

    public static final PrimitiveVisitor<NumberPrimitive> expectNumberPrimitive = new DefaultPrimitiveVisitor<>() {
        @Override
        public NumberPrimitive visit(NumberPrimitive primitive) {
            return primitive;
        }
    };

    public static final PrimitiveVisitor<SymbolPrimitive> expectSymbolPrimitive = new DefaultPrimitiveVisitor<>() {
        @Override
        public SymbolPrimitive visit(SymbolPrimitive primitive) {
            return primitive;
        }
    };

    public static final PrimitiveVisitor<ThunkPrimitive> expectThunkPrimitive = new DefaultPrimitiveVisitor<>() {
        @Override
        public ThunkPrimitive visit(ThunkPrimitive primitive) {
            return primitive;
        }
    };

    public static final PrimitiveVisitor<PairPrimitive> expectPairPrimitive = new DefaultPrimitiveVisitor<>() {
        @Override
        public PairPrimitive visit(PairPrimitive primitive) {
            return primitive;
        }
    };

    public static final PrimitiveVisitor<ListPrimitive> expectListPrimitive = new DefaultPrimitiveVisitor<>() {
        @Override
        public ListPrimitive visit(PairPrimitive primitive) {
            return primitive;
        }

        @Override
        public ListPrimitive visit(NothingPrimitive primitive) {
            return primitive;
        }
    };

    @Override
    public T visit(BooleanPrimitive primitive) {
        throw new IllegalStateException("No implemented: " + primitive);
    }

    @Override
    public T visit(BuiltinPrimitive primitive) {
        throw new IllegalStateException("No implemented: " + primitive);
    }

    @Override
    public T visit(LambdaPrimitive primitive) {
        throw new IllegalStateException("No implemented: " + primitive);
    }

    @Override
    public T visit(NothingPrimitive primitive) {
        throw new IllegalStateException("No implemented: " + primitive);
    }

    @Override
    public T visit(NumberPrimitive primitive) {
        throw new IllegalStateException("No implemented: " + primitive);
    }

    @Override
    public T visit(SymbolPrimitive primitive) {
        throw new IllegalStateException("No implemented: " + primitive);
    }

    @Override
    public T visit(ThunkPrimitive primitive) {
        throw new IllegalStateException("No implemented: " + primitive);
    }

    @Override
    public T visit(PairPrimitive primitive) {
        throw new IllegalStateException("No implemented: " + primitive);
    }
}
