package evaluator.eval;

public class ThunkPrimitive implements Primitive {
    private Evaluation evaluation;
    private Environment closure;

    public ThunkPrimitive(Evaluation evaluation, Environment closure) {
        this.evaluation = evaluation;
        this.closure = closure;
    }

    public Evaluation getEvaluation() {
        return evaluation;
    }

    public void setEvaluation(Evaluation evaluation) {
        this.evaluation = evaluation;
    }

    public Environment getClosure() {
        return closure;
    }

    public void setClosure(Environment closure) {
        this.closure = closure;
    }

    @Override
    public String toString() {
        return "ThunkPrimitive{" +
                "evaluation=" + evaluation +
                "closure=" + closure.hashCode() +
                '}';
    }

    @Override
    public <T> T accept(PrimitiveVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
