package parser;

import java.util.ArrayList;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ListExpression extends ArrayList<Expression> implements Expression{
    @Override
    public String getSymbol() {
        throw new IllegalStateException("ListExpression has no symbol.");
    }

    @Override
    public ListExpression getList() {
        return this;
    }

    @Override
    public Expression getHead() {
        return get(0);
    }

    public ListExpression getTail(){
        ListExpression e = new ListExpression();
        e.addAll(subList(1, size()));
        return e;
    }

    @Override
    public boolean isNil() {
        return isEmpty();
    }

    public boolean testHead(Function<String, Boolean> predictor){
        if(isNil() || !getHead().isSymbol()) return false;
        return predictor.apply(getHead().getSymbol());
    }

    @Override
    public <T> T visit(Function<ListExpression, T> listExpressionTFunction, Function<SymbolExpression, T> symbolExpressionTFunction) {
        return listExpressionTFunction.apply(this);
    }

    @Override
    public String toString() {
        return "(" + stream().map(Object::toString).collect(Collectors.joining(", ")) + ")";
    }
}
