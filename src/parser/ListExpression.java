package parser;

import java.util.List;

public interface ListExpression extends Expression {
    List<Expression> toList();

    boolean isNil();
}
