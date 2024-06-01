package evaluator.eval;

import parser.Cursor;
import parser.LispParser;

import java.util.Scanner;

// 4.1 Metacircular Evaluator
// 4.2 Lazy Evaluator
// Have fun!
public class Main {
    public static void main(String[] args) {
        EvaluationVisitor<Environment, Primitive> evaluator = new ApplicativeEvaluator();
        // or EvaluationEvaluator evaluator = new LazyEvaluator();
        Scanner input = new Scanner(System.in);
        while (true){
            System.out.print("?>>");
            LispParser.parseExpression(new Cursor(input.nextLine()))
                    .ifPresentOrElse((e) -> System.out.println(evaluator.visit(EvaluationBuilder.build(e))),
                    () -> System.out.println("Syntax error"));
        }
    }
}
