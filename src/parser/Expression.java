package parser;

import java.util.function.Function;

public interface Expression {
    String getSymbol();
    ListExpression getList();
    Expression getHead();
    ListExpression getTail();
    boolean isNil();

    default boolean isList(){
        return this instanceof ListExpression;
    }
    default boolean isSymbol(){
        return this instanceof SymbolExpression;
    }

    <T> T visit(Function<ListExpression, T> listExpressionTFunction, Function<SymbolExpression, T> symbolExpressionTFunction);
}
