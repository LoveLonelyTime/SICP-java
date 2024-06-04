package evaluator;

import java.util.List;

public class ProcedureEvaluation implements Evaluation {
    private Evaluation operator;
    private List<Evaluation> parameters;

    public ProcedureEvaluation(Evaluation operator, List<Evaluation> parameters) {
        this.operator = operator;
        this.parameters = parameters;
    }

    public Evaluation getOperator() {
        return operator;
    }

    public void setOperator(Evaluation operator) {
        this.operator = operator;
    }

    public List<Evaluation> getParameters() {
        return parameters;
    }

    public void setParameters(List<Evaluation> parameters) {
        this.parameters = parameters;
    }

    @Override
    public String toString() {
        return "ProcedureEvaluation{" +
                "operator=" + operator +
                ", parameters=" + parameters +
                '}';
    }


    @Override
    public <T, R> R accept(EvaluationVisitor<T, R> visitor, T t) {
        return visitor.visit(this, t);
    }
}
