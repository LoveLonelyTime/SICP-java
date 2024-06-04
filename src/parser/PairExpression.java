package parser;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class PairExpression implements ListExpression {
    private Expression head;
    private Expression tail;

    public PairExpression(Expression head, Expression tail) {
        this.head = head;
        this.tail = tail;
    }

    public Expression getHead() {
        return head;
    }

    public void setHead(Expression head) {
        this.head = head;
    }

    public Expression getTail() {
        return tail;
    }

    public void setTail(Expression tail) {
        this.tail = tail;
    }

    public Expression get(int index) {
        if (index == 0) return head;

        return tail.accept(DefaultExpressionVisitor.expectPairExpression).get(index - 1);
    }

    public boolean testHead(Predicate<String> predicate) {
        return head.accept(new ExpressionVisitor<>() {
            @Override
            public Boolean visit(SymbolExpression expression) {
                return predicate.test(expression.getSymbol());
            }

            @Override
            public Boolean visit(PairExpression expression) {
                return false;
            }

            @Override
            public Boolean visit(NilExpression expression) {
                return false;
            }
        });
    }

    @Override
    public List<Expression> toList() {
        List<Expression> expressions = new ArrayList<>();

        PairExpression pair = this;
        while (true) {
            expressions.add(pair.getHead());
            if (pair.getTail() != NilExpression.SINGLETON) {
                pair = pair.getTail().accept(DefaultExpressionVisitor.expectPairExpression);
            } else {
                break;
            }
        }

        return expressions;
    }

    @Override
    public boolean isNil() {
        return false;
    }

    @Override
    public String toString() {
        return "(" + head + ", " + tail + ")";
    }

    @Override
    public <T> T accept(ExpressionVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
