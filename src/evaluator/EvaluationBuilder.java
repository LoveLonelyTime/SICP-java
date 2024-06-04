package evaluator;

import parser.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EvaluationBuilder {
    private EvaluationBuilder() {
    }

    /*
     * (define name body...) -> Define a variable
     * (define (name arguments...) body...) -> Define a procedure
     */
    private static Evaluation buildDefine(PairExpression exp) {
        Expression define = exp.get(1);

        BeginEvaluation body = new BeginEvaluation(exp. // (define name body...)
                getTail(). // name body...)
                accept(DefaultExpressionVisitor.expectPairExpression).
                getTail(). // body...)
                        accept(DefaultExpressionVisitor.expectPairExpression).
                toList().stream().
                map(EvaluationBuilder::build).
                collect(Collectors.toList())
        );

        return define.accept(new DefaultExpressionVisitor<>() {
            @Override
            public Evaluation visit(PairExpression expression) { // Procedure
                List<String> nameAndArguments = expression.toList().stream().
                        map((arg) -> arg.accept(DefaultExpressionVisitor.expectSymbolExpression).getSymbol()).
                        collect(Collectors.toList());

                return new DefineEvaluation(
                        nameAndArguments.get(0),
                        new LambdaEvaluation(nameAndArguments.subList(1, nameAndArguments.size()), body)
                );
            }

            @Override
            public Evaluation visit(SymbolExpression expression) { // Variable
                return new DefineEvaluation(expression.getSymbol(), body);
            }
        });
    }

    /*
     * (if condition consequent alternative)
     */
    private static Evaluation buildIf(PairExpression exp) {
        return new IfEvaluation(build(exp.get(1)), build(exp.get(2)), build(exp.get(3)));
    }

    /*
     * (begin body...)
     */
    private static Evaluation buildBegin(PairExpression exp) {
        return new BeginEvaluation(exp. // (begin body...)
                getTail(). // body...)
                accept(DefaultExpressionVisitor.expectPairExpression).
                toList().stream().
                map(EvaluationBuilder::build).
                collect(Collectors.toList())
        );
    }

    /*
     * (lambda (arguments...) body...)
     */
    private static Evaluation buildLambda(PairExpression exp) {
        List<String> arguments = exp. // (lambda (arguments...) body...)
                get(1). // (arguments...)
                accept(DefaultExpressionVisitor.expectListExpression).
                toList().stream().
                map((arg) -> arg.accept(DefaultExpressionVisitor.expectSymbolExpression).getSymbol()).
                collect(Collectors.toList());

        BeginEvaluation body = new BeginEvaluation(exp. // (lambda (arguments...) body...)
                getTail(). // (arguments...) body...)
                accept(DefaultExpressionVisitor.expectPairExpression).
                getTail(). // body...)
                        accept(DefaultExpressionVisitor.expectPairExpression).
                toList().stream().
                map(EvaluationBuilder::build).
                collect(Collectors.toList())
        );

        return new LambdaEvaluation(arguments, body);
    }

    /*
     * (set! name value)
     */
    private static Evaluation buildAssignment(PairExpression exp) {
        String name = exp.get(1).accept(DefaultExpressionVisitor.expectSymbolExpression).getSymbol();
        Evaluation value = build(exp.get(2));
        return new AssignmentEvaluation(name, value);
    }

    /*
     * (quote body)
     */
    private static Evaluation buildQuote(PairExpression exp) {
        return new QuoteEvaluation(exp.get(1));
    }

    /*
     * (proc parameters...)
     */
    private static Evaluation buildProcedure(PairExpression exp) {
        Evaluation proc = build(exp.get(0));
        List<Evaluation> parameters = exp. // (proc parameters...)
                getTail(). // parameters...)
                accept(DefaultExpressionVisitor.expectListExpression).
                toList().stream().
                map(EvaluationBuilder::build).
                collect(Collectors.toList());

        return new ProcedureEvaluation(proc, parameters);
    }

    /*
     * (cond value)
     */
    private static Evaluation buildCondBranch(List<PairExpression> exps) {
        if (exps.isEmpty()) {
            // (error (quote Cond-Exhausted))
            return new ProcedureEvaluation(new VariableEvaluation("error"), List.of(new QuoteEvaluation(new SymbolExpression("Cond-Exhausted"))));
        } else {
            PairExpression item = exps.get(0);
            return new IfEvaluation(build(item.get(0)),
                    build(item.get(1)),
                    buildCondBranch(exps.subList(1, exps.size())));
        }
    }

    /*
     * (cond body...)
     */
    private static Evaluation buildCond(PairExpression exp) {
        return buildCondBranch(exp. // (cond body...)
                getTail(). // body...)
                accept(DefaultExpressionVisitor.expectPairExpression).
                toList().stream().
                map((cond) -> cond.accept(DefaultExpressionVisitor.expectPairExpression)).
                collect(Collectors.toList())
        );
    }

    /*
     * (let ((name value)...) body...)
     */
    private static Evaluation buildLet(PairExpression exp) {
        List<Map.Entry<String, Evaluation>> variables = exp.
                get(1).
                accept(DefaultExpressionVisitor.expectListExpression).
                toList().stream().
                map(item -> item.accept(DefaultExpressionVisitor.expectPairExpression)).
                map(item -> Map.entry(item.get(0).accept(DefaultExpressionVisitor.expectSymbolExpression).toString(), build(item.get(1)))).
                collect(Collectors.toList());

        BeginEvaluation body = new BeginEvaluation(exp. // (let ((name value)...) body...)
                getTail(). // ((name value)...) body...)
                accept(DefaultExpressionVisitor.expectPairExpression).
                getTail(). // body...)
                        accept(DefaultExpressionVisitor.expectPairExpression).
                toList().stream().
                map(EvaluationBuilder::build).
                collect(Collectors.toList())
        );
        LambdaEvaluation lambda = new LambdaEvaluation(variables.stream().map(Map.Entry::getKey).collect(Collectors.toList()), body);

        return new ProcedureEvaluation(lambda, variables.stream().map(Map.Entry::getValue).collect(Collectors.toList()));
    }

    /*
     * (amb body...)
     */
    private static Evaluation buildAMB(PairExpression exp) {
        return new AMBEvaluation(exp. // (amb body...)
                getTail(). // body...)
                accept(DefaultExpressionVisitor.expectListExpression).
                toList().stream().
                map(EvaluationBuilder::build).
                collect(Collectors.toList())
        );
    }

    private static Evaluation buildFromPairExpression(PairExpression exp) {
        if (exp.testHead("define"::equals)) {
            return buildDefine(exp);
        } else if (exp.testHead("if"::equals)) {
            return buildIf(exp);
        } else if (exp.testHead("begin"::equals)) {
            return buildBegin(exp);
        } else if (exp.testHead("lambda"::equals)) {
            return buildLambda(exp);
        } else if (exp.testHead("quote"::equals)) {
            return buildQuote(exp);
        } else if (exp.testHead("set!"::equals)) {
            return buildAssignment(exp);
        } else if (exp.testHead("cond"::equals)) {
            return buildCond(exp);
        } else if (exp.testHead("let"::equals)) {
            return buildLet(exp);
        } else if (exp.testHead("amb"::equals)) {
            return buildAMB(exp);
        } else {
            return buildProcedure(exp);
        }
    }

    private static Evaluation buildFromSymbolExpression(SymbolExpression exp) {
        try {
            return new NumberEvaluation(Double.parseDouble(exp.getSymbol()));
        } catch (NumberFormatException e) {
            return new VariableEvaluation(exp.getSymbol());
        }
    }

    public static Evaluation build(Expression exp) {
        return exp.accept(new ExpressionVisitor<>() {
            @Override
            public Evaluation visit(SymbolExpression expression) {
                return buildFromSymbolExpression(expression);
            }

            @Override
            public Evaluation visit(PairExpression expression) {
                return buildFromPairExpression(expression);
            }

            @Override
            public Evaluation visit(NilExpression expression) {
                throw new IllegalStateException("Build a NilExpression.");
            }
        });
    }
}
