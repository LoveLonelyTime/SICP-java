package evaluator.neval;

import parser.Cursor;
import parser.LispParser;

import java.util.Scanner;

// 4.3 Nondeterministic Evaluator
// Have fun!
public class Main {
    public static void main(String[] args) {
        EvaluationEvaluator evaluator = new AMBEvaluator();
        Scanner input = new Scanner(System.in);

        Successful successful = (value, failed) -> {
            System.out.println(value);
            System.out.print("next?>>");
            if(input.nextLine().equals("y")){
                return failed.call();
            } else {
                return value;
            }
        };

        Failed failed = () -> {
            System.out.println("Exhausted.");
            return NothingPrimitive.SINGLETON;
        };


        while (true){
            System.out.print("?>>");
            LispParser.parseExpression(new Cursor(input.nextLine()))
                    .ifPresentOrElse((e) -> evaluator.eval(EvaluationBuilder.build(e), successful, failed),
                            () -> System.out.println("Syntax error"));
        }
    }
}
