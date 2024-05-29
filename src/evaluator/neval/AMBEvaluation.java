package evaluator.neval;

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
    public Primitive accept(EvaluationEvaluator evaluator, Environment env, Successful successful, Failed failed) {
        return evaluator.eval(this, env, successful, failed);
    }
}
