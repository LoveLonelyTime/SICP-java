package evaluator.eval;

import parser.Expression;
import parser.ListExpression;
import parser.SymbolExpression;

import java.util.List;
import java.util.stream.Collectors;

public class EvaluationBuilder {
    private EvaluationBuilder() {
    }

    private static Evaluation buildDefine(ListExpression exp) {
        Expression define = exp.get(1);
        BeginEvaluation body = new BeginEvaluation(exp.getTail().getTail().stream().map(EvaluationBuilder::build).collect(Collectors.toList()));

        return define.visit((list) -> { // Procedure
            List<String> arguments = list.stream().map(Expression::getSymbol).collect(Collectors.toList());
            return new DefineEvaluation(arguments.get(0), new LambdaEvaluation(arguments.subList(1, arguments.size()), body));
        }, (symbol) -> new DefineEvaluation(symbol.getSymbol(), body)); // Variable
    }

    private static Evaluation buildIf(ListExpression exp) {
        return new IfEvaluation(build(exp.get(1)), build(exp.get(2)), build(exp.get(3)));
    }

    private static Evaluation buildBegin(ListExpression exp) {
        return new BeginEvaluation(exp.getTail().stream().map(EvaluationBuilder::build).collect(Collectors.toList()));
    }

    private static Evaluation buildLambda(ListExpression exp) {
        List<String> arguments = exp.get(1).getList().stream().map(Expression::getSymbol).collect(Collectors.toList());
        BeginEvaluation body = new BeginEvaluation(exp.getTail().getTail().stream().map(EvaluationBuilder::build).collect(Collectors.toList()));
        return new LambdaEvaluation(arguments, body);
    }

    private static Evaluation buildQuote(ListExpression exp) {
        return new QuoteEvaluation(exp.get(1).getSymbol());
    }

    private static Evaluation buildProcedure(ListExpression exp){
        return new ProcedureEvaluation(build(exp.get(0)), exp.getTail().stream().map(EvaluationBuilder::build).collect(Collectors.toList()));
    }

    private static Evaluation buildFromListExpression(ListExpression exp) {
        if (exp.isNil()) return new VariableEvaluation("nothing");

        if(exp.testHead("define"::equals)){
            return buildDefine(exp);
        } else if(exp.testHead("if"::equals)){
            return buildIf(exp);
        } else if(exp.testHead("begin"::equals)){
            return buildBegin(exp);
        } else if(exp.testHead("lambda"::equals)){
            return buildLambda(exp);
        } else if(exp.testHead("quote"::equals)){
            return buildQuote(exp);
        } else {
            return buildProcedure(exp);
        }
    }

    private static Evaluation buildFromSymbolExpression(SymbolExpression exp) {
        try{
            return new NumberEvaluation(Double.parseDouble(exp.getSymbol()));
        }catch (NumberFormatException e){
            return new VariableEvaluation(exp.getSymbol());
        }
    }

    public static Evaluation build(Expression exp) {
        return exp.visit(EvaluationBuilder::buildFromListExpression, EvaluationBuilder::buildFromSymbolExpression);
    }
}
