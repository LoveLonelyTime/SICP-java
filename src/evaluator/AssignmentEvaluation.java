package evaluator;

public class AssignmentEvaluation implements Evaluation {
    private String name;
    private Evaluation value;

    public AssignmentEvaluation(String name, Evaluation value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Evaluation getValue() {
        return value;
    }

    public void setValue(Evaluation value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "AssignmentEvaluation{" +
                "name='" + name + '\'' +
                ", value=" + value +
                '}';
    }

    @Override
    public <T, R> R accept(EvaluationVisitor<T, R> visitor, T t) {
        return visitor.visit(this, t);
    }
}
