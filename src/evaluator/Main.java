package evaluator;

import parser.Cursor;
import parser.Expression;
import parser.LispParser;

import java.util.Scanner;

// 4.1 Metacircular Evaluator
// 4.2 Lazy Evaluator
// 4.3 Nondeterministic Evaluator
// Have fun!
public class Main {
    public static void main(String[] args) {
        EvaluationVisitor<Environment, Primitive> evaluator = new ApplicativeEvaluator();
        // or new LazyEvaluator();
        // or new AMBEvaluator();
        Scanner input = new Scanner(System.in);
        while (true) {
            System.out.print("?>>");
            String source = input.nextLine();
            if (source.equals("exit")) break;
            Expression expression = LispParser.parseExpression(new Cursor(source));
            Evaluation evaluation = EvaluationBuilder.build(expression);
            System.out.println(evaluator.visit(evaluation));
        }
    }
}
