package evaluator.eval;

public class DefineEvaluation implements Evaluation {
    private String name;
    private Evaluation value;

    public DefineEvaluation(String name, Evaluation value) {
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
        return "DefineEvaluation{" +
                "name='" + name + '\'' +
                ", value=" + value +
                '}';
    }


    @Override
    public <T, R> R accept(EvaluationVisitor<T, R> visitor, T t) {
        return visitor.visit(this, t);
    }
}
