package evaluator;

import java.util.Objects;

public class SymbolPrimitive implements Primitive {
    private String symbol;

    public SymbolPrimitive(String symbol) {
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SymbolPrimitive that = (SymbolPrimitive) o;
        return Objects.equals(symbol, that.symbol);
    }

    @Override
    public <T> T accept(PrimitiveVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
