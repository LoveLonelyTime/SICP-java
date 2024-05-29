package evaluator.neval;

import java.util.List;

public class BeginEvaluation implements Evaluation {
    private List<Evaluation> evaluations;

    public BeginEvaluation(List<Evaluation> evaluations) {
        this.evaluations = evaluations;
    }

    public List<Evaluation> getEvaluations() {
        return evaluations;
    }

    public void setEvaluations(List<Evaluation> evaluations) {
        this.evaluations = evaluations;
    }

    @Override
    public String toString() {
        return "BeginEvaluation{" +
                "evaluations=" + evaluations +
                '}';
    }

    @Override
    public Primitive accept(EvaluationEvaluator evaluator, Environment env, Successful successful, Failed failed) {
        return evaluator.eval(this, env, successful, failed);
    }
}
