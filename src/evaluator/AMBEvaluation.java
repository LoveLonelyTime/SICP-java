package evaluator;

import java.util.List;

public class AMBEvaluation implements Evaluation {
    private List<Evaluation> alternatives;

    public AMBEvaluation(List<Evaluation> alternatives) {
        this.alternatives = alternatives;
    }

    public List<Evaluation> getAlternatives() {
        return alternatives;
    }

    public void setAlternatives(List<Evaluation> alternatives) {
        this.alternatives = alternatives;
    }

    @Override
    public String toString() {
        return "AMBEvaluation{" +
                "alternatives=" + alternatives +
                '}';
    }

    @Override
    public <T, R> R accept(EvaluationVisitor<T, R> visitor, T t) {
        return visitor.visit(this, t);
    }
}
