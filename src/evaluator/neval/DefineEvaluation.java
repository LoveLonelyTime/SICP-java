package evaluator.neval;

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
    public Primitive accept(EvaluationEvaluator evaluator, Environment env, Successful successful, Failed failed) {
        return evaluator.eval(this, env, successful, failed);
    }
}
