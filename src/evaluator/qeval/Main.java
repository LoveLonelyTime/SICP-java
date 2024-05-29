package evaluator.qeval;

import parser.Cursor;
import parser.LispParser;

import java.util.Scanner;

// 4.4 Logic Programming
// Have fun!
public class Main {
    public static void main(String[] args) {
        QueryEvaluator evaluator = new QueryEvaluator();
        Scanner input = new Scanner(System.in);
        while (true) {
            System.out.print("?>>");
            LispParser.parseExpression(new Cursor(input.nextLine())).ifPresentOrElse((e) ->
                            evaluator.eval(e).stream().
                                    map((f) -> evaluator.instantiate(e, f)).
                                    forEach(System.out::println),
                    () -> System.out.println("Syntax error"));
        }
    }
}
