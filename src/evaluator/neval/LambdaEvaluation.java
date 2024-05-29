package evaluator.neval;

import java.util.List;

public class LambdaEvaluation implements Evaluation {
    private List<String> arguments;
    private Evaluation body;

    public LambdaEvaluation(List<String> arguments, Evaluation body) {
        this.arguments = arguments;
        this.body = body;
    }

    public List<String> getArguments() {
        return arguments;
    }

    public void setArguments(List<String> arguments) {
        this.arguments = arguments;
    }

    public Evaluation getBody() {
        return body;
    }

    public void setBody(Evaluation body) {
        this.body = body;
    }

    @Override
    public String toString() {
        return "LambdaEvaluation{" +
                "arguments=" + arguments +
                ", body=" + body +
                '}';
    }

    @Override
    public Primitive accept(EvaluationEvaluator evaluator, Environment env, Successful successful, Failed failed) {
        return evaluator.eval(this, env, successful, failed);
    }
}
