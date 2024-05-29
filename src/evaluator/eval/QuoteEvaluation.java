package evaluator.eval;

public class QuoteEvaluation implements Evaluation {
    private String name;

    public QuoteEvaluation(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public Primitive accept(EvaluationEvaluator evaluator, Environment env) {
        return evaluator.eval(this, env);
    }
}
