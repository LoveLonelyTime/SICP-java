package evaluator;

public class TrivialEvaluation implements Evaluation{
    private Primitive primitive;

    public TrivialEvaluation(Primitive primitive) {
        this.primitive = primitive;
    }

    public Primitive getPrimitive() {
        return primitive;
    }

    public void setPrimitive(Primitive primitive) {
        this.primitive = primitive;
    }

    @Override
    public String toString() {
        return primitive.toString();
    }

    @Override
    public <T, R> R accept(EvaluationVisitor<T, R> visitor, T t) {
        return visitor.visit(this, t);
    }
}
