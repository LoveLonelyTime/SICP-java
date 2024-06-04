package parser;

public interface ExpressionVisitor<T> {
    T visit(SymbolExpression expression);

    T visit(PairExpression expression);

    T visit(NilExpression expression);
}
