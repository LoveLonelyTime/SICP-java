package evaluator.eval;

public class IfEvaluation implements Evaluation {
    private Evaluation condition;
    private Evaluation consequent;
    private Evaluation alternative;

    public IfEvaluation(Evaluation condition, Evaluation consequent, Evaluation alternative) {
        this.condition = condition;
        this.consequent = consequent;
        this.alternative = alternative;
    }

    public Evaluation getCondition() {
        return condition;
    }

    public void setCondition(Evaluation condition) {
        this.condition = condition;
    }

    public Evaluation getConsequent() {
        return consequent;
    }

    public void setConsequent(Evaluation consequent) {
        this.consequent = consequent;
    }

    public Evaluation getAlternative() {
        return alternative;
    }

    public void setAlternative(Evaluation alternative) {
        this.alternative = alternative;
    }

    @Override
    public String toString() {
        return "IfEvaluation{" +
                "condition=" + condition +
                ", consequent=" + consequent +
                ", alternative=" + alternative +
                '}';
    }


    @Override
    public <T, R> R accept(EvaluationVisitor<T, R> visitor, T t) {
        return visitor.visit(this, t);
    }
}
