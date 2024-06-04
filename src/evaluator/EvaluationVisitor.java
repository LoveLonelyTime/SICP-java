package evaluator;

public interface EvaluationVisitor<T, R> {
    R visit(Evaluation e);

    R visit(BeginEvaluation e, T t);

    R visit(DefineEvaluation e, T t);

    R visit(IfEvaluation e, T t);

    R visit(LambdaEvaluation e, T t);

    R visit(NumberEvaluation e, T t);

    R visit(ProcedureEvaluation e, T t);

    R visit(QuoteEvaluation e, T t);

    R visit(VariableEvaluation e, T t);

    R visit(AssignmentEvaluation e, T t);

    R visit(AMBEvaluation e, T t);
    R visit(TrivialEvaluation e, T t);
}
