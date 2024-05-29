package evaluator.eval;

public class NumberEvaluation implements Evaluation {
    private double value;

    public NumberEvaluation(double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return Double.toString(value);
    }

    @Override
    public Primitive accept(EvaluationEvaluator evaluator, Environment env) {
        return evaluator.eval(this, env);
    }
}
