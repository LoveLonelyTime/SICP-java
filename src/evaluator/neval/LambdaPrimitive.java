package evaluator.neval;

import java.util.List;

public class LambdaPrimitive implements Primitive{
    private List<String> arguments;
    private Evaluation evaluation;
    private Environment closure;

    public LambdaPrimitive(List<String> arguments, Evaluation evaluation, Environment closure) {
        this.arguments = arguments;
        this.evaluation = evaluation;
        this.closure = closure;
    }

    public List<String> getArguments() {
        return arguments;
    }

    public void setArguments(List<String> arguments) {
        this.arguments = arguments;
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
        return "LambdaPrimitive{" +
                "arguments=" + arguments +
                ", evaluation=" + evaluation +
                ", closure=" + closure.hashCode() +
                '}';
    }

    @Override
    public <T> T accept(PrimitiveVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
