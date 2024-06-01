package compiler;

import evaluator.eval.*;
import parser.Cursor;
import parser.LispParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) throws IOException {
        EvaluationVisitor<Context, InstSequence> compiler = new Compiler();
        String source = Files.readString(Path.of("test.sch"));
        LispParser.parseExpression(new Cursor(source))
                .ifPresentOrElse((e) -> compiler.visit(EvaluationBuilder.build(e)).getStatements().forEach(System.out::println),
                        () -> System.out.println("Syntax error"));
    }
}
