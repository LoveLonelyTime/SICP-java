package parser;

import java.util.Collections;
import java.util.List;

public class NilExpression implements ListExpression {
    public static final NilExpression SINGLETON = new NilExpression();

    private NilExpression() {
    }

    @Override
    public String toString() {
        return "nil";
    }


    @Override
    public <T> T accept(ExpressionVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override
    public List<Expression> toList() {
        return Collections.emptyList();
    }

    @Override
    public boolean isNil() {
        return true;
    }
}
