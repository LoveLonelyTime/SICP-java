package machine;

import parser.Cursor;
import parser.Expression;
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
        RegisterMachine registerMachine = new RegisterMachine();

        String source = Files.readString(Path.of("explicit-evaluator.sch"));
        Expression code = LispParser.parseExpression(new Cursor(source));
        registerMachine.appendCode(code);

        registerMachine.execute();
    }
}
