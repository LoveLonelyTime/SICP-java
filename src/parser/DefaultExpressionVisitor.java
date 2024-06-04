package parser;

public abstract class DefaultExpressionVisitor<T> implements ExpressionVisitor<T> {
    public static final ExpressionVisitor<PairExpression> expectPairExpression = new DefaultExpressionVisitor<>() {
        @Override
        public PairExpression visit(PairExpression expression) {
            return expression;
        }
    };

    public static final ExpressionVisitor<SymbolExpression> expectSymbolExpression = new DefaultExpressionVisitor<>() {
        @Override
        public SymbolExpression visit(SymbolExpression expression) {
            return expression;
        }
    };

    public static final ExpressionVisitor<NilExpression> expectNilExpression = new DefaultExpressionVisitor<>() {
        @Override
        public NilExpression visit(NilExpression expression) {
            return expression;
        }
    };

    public static final ExpressionVisitor<ListExpression> expectListExpression = new DefaultExpressionVisitor<>() {
        @Override
        public ListExpression visit(PairExpression expression) {
            return expression;
        }

        @Override
        public ListExpression visit(NilExpression expression) {
            return expression;
        }
    };

    @Override
    public T visit(PairExpression expression) {
        throw new IllegalStateException("No implemented: " + expression);
    }

    @Override
    public T visit(SymbolExpression expression) {
        throw new IllegalStateException("No implemented: " + expression);
    }

    @Override
    public T visit(NilExpression expression) {
        throw new IllegalStateException("No implemented: " + expression);
    }
}
