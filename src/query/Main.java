package query;

import parser.Cursor;
import parser.Expression;
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
            String source = input.nextLine();
            if (source.equals("exit")) break;
            Expression expression = LispParser.parseExpression(new Cursor(source));
            evaluator.
                    query(expression).
                    stream().
                    map(f -> evaluator.instantiate(expression, f)).
                    forEach(System.out::println);
        }
    }
}
