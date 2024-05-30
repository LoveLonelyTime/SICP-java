package machine;

import parser.Cursor;
import parser.LispParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

// 5.2 Register-Machine
// 5.3 Allocation and Garbage Collection
// 5.4 Explicit-Control Evaluator
// Have fun!
public class Main {
    public static void main(String[] args) throws IOException {
        String source = Files.readString(Path.of("explicit-evaluator.sch"));

        LispParser.parseExpression(new Cursor(source))
                .ifPresentOrElse((e) -> {
                    RegisterMachine machine = new RegisterMachine(e);
                    machine.execute();
                }, () -> System.out.println("Syntax error"));
    }
}
