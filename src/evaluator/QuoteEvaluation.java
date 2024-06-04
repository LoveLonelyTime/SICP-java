package evaluator;

import parser.Expression;

public class QuoteEvaluation implements Evaluation {
    private Expression quote;

    public QuoteEvaluation(Expression quote) {
        this.quote = quote;
    }

    public Expression getQuote() {
        return quote;
    }

    public void setQuote(Expression quote) {
        this.quote = quote;
    }

    @Override
    public String toString() {
        return quote.toString();
    }


    @Override
    public <T, R> R accept(EvaluationVisitor<T, R> visitor, T t) {
        return visitor.visit(this, t);
    }
}
