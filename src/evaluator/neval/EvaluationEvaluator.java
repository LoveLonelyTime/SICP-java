package evaluator.neval;

public interface EvaluationEvaluator {
    Primitive eval(Evaluation e, Successful successful, Failed failed);
    Primitive eval(BeginEvaluation e, Environment env, Successful successful, Failed failed);
    Primitive eval(DefineEvaluation e, Environment env, Successful successful, Failed failed);
    Primitive eval(IfEvaluation e, Environment env, Successful successful, Failed failed);
    Primitive eval(LambdaEvaluation e, Environment env, Successful successful, Failed failed);
    Primitive eval(NumberEvaluation e, Environment env, Successful successful, Failed failed);
    Primitive eval(ProcedureEvaluation e, Environment env, Successful successful, Failed failed);
    Primitive eval(QuoteEvaluation e, Environment env, Successful successful, Failed failed);
    Primitive eval(VariableEvaluation e, Environment env, Successful successful, Failed failed);
    Primitive eval(AMBEvaluation e, Environment env, Successful successful, Failed failed);
}
