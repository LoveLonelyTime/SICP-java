package machine;

import evaluator.*;
import parser.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class RegisterMachine {
    private int pc = 0;
    private boolean flag = false;
    private final Map<String, Primitive> registers = new HashMap<>();
    private final Deque<Primitive> stack = new ArrayDeque<>();
    private final List<Instruction> instructions = new ArrayList<>();
    private final Map<String, Integer> labels = new HashMap<>();
    private final ApplicativeEvaluator lispEvaluator = new ApplicativeEvaluator();

    public RegisterMachine() throws IOException {
        lispEvaluator.getGlobalEnv().define("read-expression", new BuiltinPrimitive() {
            @Override
            public Primitive eval(List<Primitive> parameters) {
                Expression e = LispParser.parseExpression(new Cursor(new Scanner(System.in).nextLine()));
                return lispEvaluator.visit(new QuoteEvaluation(e));
            }
        });

        lispEvaluator.getGlobalEnv().define("apply", new BuiltinPrimitive() {
            @Override
            public Primitive eval(List<Primitive> parameters) {
                List<Primitive> args = parameters.get(1).accept(DefaultPrimitiveVisitor.expectListPrimitive).toList();
                return parameters.get(0).accept(new DefaultPrimitiveVisitor<>() {
                    @Override
                    public Primitive visit(BuiltinPrimitive primitive) {
                        return primitive.eval(args);
                    }

                    @Override
                    public Primitive visit(LambdaPrimitive primitive) {
                        Environment closure = primitive.getClosure().extend();
                        closure.putParameters(primitive.getArguments(), args);
                        return primitive.getEvaluation().accept(lispEvaluator, closure);
                    }
                });
            }
        });

        // Init builtins
        String source = Files.readString(Path.of("builtins.sch"));
        lispEvaluator.visit(EvaluationBuilder.build(LispParser.parseExpression(new Cursor(source))));
    }

    public void execute() {
        while (pc < instructions.size()) {
            instructions.get(pc).execute();
        }
    }

    public void appendCode(Expression code){
        PairExpression pair = code.accept(DefaultExpressionVisitor.expectPairExpression);

        // Init registers
        pair.
                get(0).
                accept(DefaultExpressionVisitor.expectListExpression).
                toList().stream().
                map(item -> item.accept(DefaultExpressionVisitor.expectSymbolExpression).getSymbol()).
                forEach(reg -> {
                    if(!registers.containsKey(reg)) registers.put(reg, NothingPrimitive.SINGLETON);
                });

        // Init instructions
        pair.
                get(1).
                accept(DefaultExpressionVisitor.expectListExpression).
                toList().
                forEach(this::handleTextInstruction);
    }

    private void handleTextInstruction(Expression text) {
        text.accept(new ExpressionVisitor<>() {
            @Override
            public Object visit(SymbolExpression expression) { // Label
                if(labels.containsKey(expression.getSymbol())){
                    throw new IllegalStateException("Duplicate label:" + expression.getSymbol());
                } else {
                    labels.put(expression.getSymbol(), instructions.size());
                }
                return null;
            }

            @Override
            public Object visit(PairExpression expression) { // Instruction
                if (expression.testHead("assign"::equals)) {
                    instructions.add(buildAssign(expression));
                } else if (expression.testHead("perform"::equals)) {
                    instructions.add(buildPerform(expression));
                } else if (expression.testHead("test"::equals)) {
                    instructions.add(buildTest(expression));
                } else if (expression.testHead("branch"::equals)) {
                    instructions.add(buildBranch(expression));
                } else if (expression.testHead("goto"::equals)) {
                    instructions.add(buildGoto(expression));
                } else if (expression.testHead("save"::equals)) {
                    instructions.add(buildSave(expression));
                } else if (expression.testHead("restore"::equals)) {
                    instructions.add(buildRestore(expression));
                } else {
                    throw new IllegalStateException("Unexpected: " + expression);
                }
                return null;
            }

            @Override
            public Object visit(NilExpression expression) {
                throw new IllegalStateException("Add a NilExpression.");
            }
        });
    }

    // (assign reg value...)
    private Instruction buildAssign(PairExpression expression) {
        String reg = expression.get(1).accept(DefaultExpressionVisitor.expectSymbolExpression).getSymbol();
        PairExpression value = expression. // (assign reg value...)
                getTail(). // reg value...)
                accept(DefaultExpressionVisitor.expectPairExpression).
                getTail(). // value...)
                accept(DefaultExpressionVisitor.expectPairExpression);

        Supplier<Primitive> supplier = supply(value);

        return () -> {
            registers.put(reg, supplier.get());
            nextPC();
        };
    }

    // (perform value...)
    private Instruction buildPerform(PairExpression expression) {
        Supplier<Primitive> supplier = supply(expression.getTail().accept(DefaultExpressionVisitor.expectPairExpression));
        return () -> {
            supplier.get();
            nextPC();
        };
    }

    // (test value...)
    private Instruction buildTest(PairExpression expression) {
        Supplier<Primitive> supplier = supply(expression.getTail().accept(DefaultExpressionVisitor.expectPairExpression));
        return () -> {
            flag = supplier.get().accept(DefaultPrimitiveVisitor.expectBooleanPrimitive).getValue();
            nextPC();
        };
    }

    // (branch label...)
    private Instruction buildBranch(PairExpression expression) {
        Supplier<Primitive> target = supply(expression.getTail().accept(DefaultExpressionVisitor.expectPairExpression));

        return () -> {
            if (flag) {
                pc = (int) target.get().accept(DefaultPrimitiveVisitor.expectNumberPrimitive).getValue();
            } else {
                nextPC();
            }
        };
    }

    // (goto label...)
    private Instruction buildGoto(PairExpression expression) {
        Supplier<Primitive> target = supply(expression.getTail().accept(DefaultExpressionVisitor.expectPairExpression));

        return () -> pc = (int) target.get().accept(DefaultPrimitiveVisitor.expectNumberPrimitive).getValue();
    }

    // (save reg)
    private Instruction buildSave(PairExpression expression) {
        String reg = expression.get(1).accept(DefaultExpressionVisitor.expectSymbolExpression).getSymbol();
        return () -> {
            stack.push(registers.get(reg));
            nextPC();
        };
    }

    // (restore reg)
    private Instruction buildRestore(PairExpression expression) {
        String reg = expression.get(1).accept(DefaultExpressionVisitor.expectSymbolExpression).getSymbol();
        return () -> {
            registers.put(reg, stack.pop());
            nextPC();
        };
    }

    /*
     * ((label name))
     * ((reg name))
     * ((const value))
     * ((op name) value...)
     */
    private Supplier<Primitive> supply(PairExpression expression) {
        PairExpression head = expression.getHead().accept(DefaultExpressionVisitor.expectPairExpression);

        if (head.testHead("label"::equals)) { // (label name)
            String name = head.get(1).accept(DefaultExpressionVisitor.expectSymbolExpression).getSymbol();
            return () -> new NumberPrimitive(labels.get(name));
        } else if (head.testHead("reg"::equals)) { // (reg name)
            String reg = head.get(1).accept(DefaultExpressionVisitor.expectSymbolExpression).getSymbol();
            return () -> registers.get(reg);
        } else if (head.testHead("const"::equals)) { // (const value)
            Primitive constant = buildConstant(head.get(1));
            return () -> constant;
        } else if(head.testHead("op"::equals)) { // (op name)
            String name = head.get(1).accept(DefaultExpressionVisitor.expectSymbolExpression).getSymbol();
            List<Supplier<Primitive>> parameters = expression. // ((op name) value...)
                    getTail(). // value...)
                    accept(DefaultExpressionVisitor.expectListExpression).
                    toList().stream().
                    map(item -> new PairExpression(item.accept(DefaultExpressionVisitor.expectPairExpression), NilExpression.SINGLETON)).
                    map(this::supply).
                    collect(Collectors.toList());

            return () -> {
                ProcedureEvaluation procedureEvaluation = new ProcedureEvaluation(new VariableEvaluation(name),
                        parameters.stream().
                        map(Supplier::get).
                        map(TrivialEvaluation::new).
                        collect(Collectors.toList())
                );
                return lispEvaluator.visit(procedureEvaluation);
            };
        } else {
            throw new IllegalStateException("Unexpected: " + expression);
        }
    }

    private Primitive buildConstant(Expression expression) {
        return lispEvaluator.visit(EvaluationBuilder.build(expression));
    }

    private void nextPC() {
        pc = pc + 1;
    }
}
