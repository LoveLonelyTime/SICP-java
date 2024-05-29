package evaluator.eval;

public interface EvaluationEvaluator {
    Primitive eval(Evaluation e);
    Primitive eval(BeginEvaluation e, Environment env);
    Primitive eval(DefineEvaluation e, Environment env);
    Primitive eval(IfEvaluation e, Environment env);
    Primitive eval(LambdaEvaluation e, Environment env);
    Primitive eval(NumberEvaluation e, Environment env);
    Primitive eval(ProcedureEvaluation e, Environment env);
    Primitive eval(QuoteEvaluation e, Environment env);
    Primitive eval(VariableEvaluation e, Environment env);
}
