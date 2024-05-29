package parser;

import java.util.function.Function;

public class SymbolExpression implements Expression{
    private String symbol;

    public SymbolExpression(){}

    public SymbolExpression(String symbol) {
        this.symbol = symbol;
    }

    @Override
    public String getSymbol() {
        return symbol;
    }


    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    @Override
    public String toString() {
        return symbol;
    }

    @Override
    public ListExpression getList() {
        throw new IllegalStateException("SymbolExpression has no head.");
    }

    @Override
    public Expression getHead() {
        throw new IllegalStateException("SymbolExpression has no head.");
    }

    @Override
    public ListExpression getTail() {
        throw new IllegalStateException("SymbolExpression has no tail.");
    }

    @Override
    public boolean isNil() {
        throw new IllegalStateException("SymbolExpression can't be nil.");
    }

    @Override
    public <T> T visit(Function<ListExpression, T> listExpressionTFunction, Function<SymbolExpression, T> symbolExpressionTFunction) {
        return symbolExpressionTFunction.apply(this);
    }
}
