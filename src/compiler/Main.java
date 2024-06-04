package compiler;

import evaluator.Evaluation;
import evaluator.EvaluationBuilder;
import evaluator.EvaluationVisitor;
import parser.Cursor;
import parser.Expression;
import parser.LispParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

// 5.5 Compiler
// Have fun!
public class Main {
    public static void main(String[] args) throws IOException {
        EvaluationVisitor<Context, InstSequence> compiler = new Compiler();
        String source = Files.readString(Path.of("test.sch"));
        Expression expression = LispParser.parseExpression(new Cursor(source));
        Evaluation evaluation = EvaluationBuilder.build(expression);
        InstSequence instSequence = compiler.visit(evaluation);
        instSequence.arrange().forEach(System.out::println);
    }
}
