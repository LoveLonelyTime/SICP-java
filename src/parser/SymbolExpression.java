package parser;

public class SymbolExpression implements Expression {
    private String symbol;

    public SymbolExpression(String symbol) {
        this.symbol = symbol;
    }

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
    public <T> T accept(ExpressionVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
