package machine;

import parser.Cursor;
import parser.Expression;
import parser.LispParser;
import parser.ListExpression;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class RegisterMachine {
    private int pc = 0;
    private boolean flag = false;
    private final Map<String, Primitive> registers = new HashMap<>();
    private final Deque<Primitive> stack = new ArrayDeque<>();
    private final List<Instruction> instructions = new ArrayList<>();
    private final Map<String, Integer> labels = new HashMap<>();

    private final Map<String, Operation> ops = new HashMap<>();

    // Dynamic memory
    private final Map<String, Primitive[]> vectors = new HashMap<>();

    private final Map<String, BiFunction<Primitive, Primitive, Primitive>> builtin = new HashMap<>();

    public RegisterMachine(Expression machine) {
        vectors.put("the-cars", new Primitive[50]);
        vectors.put("the-cdrs", new Primitive[50]);
        vectors.put("new-cars", new Primitive[50]);
        vectors.put("new-cdrs", new Primitive[50]);

        builtin.put("+", (parameter, env) -> {
            int x = parameter.
                    accept(DefaultPrimitiveVisitor.getPair).
                    getCar().
                    accept(DefaultPrimitiveVisitor.getNumber).
                    getValue();
            int y = parameter.
                    accept(DefaultPrimitiveVisitor.getPair).
                    getCdr().
                    accept(DefaultPrimitiveVisitor.getPair).
                    getCar().
                    accept(DefaultPrimitiveVisitor.getNumber).
                    getValue();
            return new NumberPrimitive(x + y);
        });

        builtin.put("-", (parameter, env) -> {
            int x = parameter.
                    accept(DefaultPrimitiveVisitor.getPair).
                    getCar().
                    accept(DefaultPrimitiveVisitor.getNumber).
                    getValue();
            int y = parameter.
                    accept(DefaultPrimitiveVisitor.getPair).
                    getCdr().
                    accept(DefaultPrimitiveVisitor.getPair).
                    getCar().
                    accept(DefaultPrimitiveVisitor.getNumber).
                    getValue();
            return new NumberPrimitive(x - y);
        });

        builtin.put("*", (parameter, env) -> {
            int x = parameter.
                    accept(DefaultPrimitiveVisitor.getPair).
                    getCar().
                    accept(DefaultPrimitiveVisitor.getNumber).
                    getValue();
            int y = parameter.
                    accept(DefaultPrimitiveVisitor.getPair).
                    getCdr().
                    accept(DefaultPrimitiveVisitor.getPair).
                    getCar().
                    accept(DefaultPrimitiveVisitor.getNumber).
                    getValue();
            return new NumberPrimitive(x * y);
        });

        builtin.put("=", (parameter, env) -> {
            int x = parameter.
                    accept(DefaultPrimitiveVisitor.getPair).
                    getCar().
                    accept(DefaultPrimitiveVisitor.getNumber).
                    getValue();
            int y = parameter.
                    accept(DefaultPrimitiveVisitor.getPair).
                    getCdr().
                    accept(DefaultPrimitiveVisitor.getPair).
                    getCar().
                    accept(DefaultPrimitiveVisitor.getNumber).
                    getValue();
            return new BooleanPrimitive(x == y);
        });

        builtin.put("set!", (parameter, env) -> {
            String name = parameter.
                    accept(DefaultPrimitiveVisitor.getPair).
                    getCar().
                    accept(DefaultPrimitiveVisitor.getQuote).
                    getQuote();
            Primitive value = parameter.
                    accept(DefaultPrimitiveVisitor.getPair).
                    getCdr().
                    accept(DefaultPrimitiveVisitor.getPair).
                    getCar();

            lookupVariable(name, env).accept(DefaultPrimitiveVisitor.getPair).setCdr(value);
            return value;
        });

        ops.put("print!", parameters -> {
            parameters.forEach(System.out::println);
            return NothingPrimitive.SINGLETON;
        });

        ops.put("nil", parameters -> NothingPrimitive.SINGLETON);

        ops.put("list", parameters -> PairPrimitive.fromParameter(parameters.toArray(new Primitive[0])));

        ops.put("cons", parameters -> new PairPrimitive(parameters.get(0), parameters.get(1)));

        ops.put("=", new ArithmeticOperation() {
            @Override
            Primitive eval(NumberPrimitive x, NumberPrimitive y) {
                return new BooleanPrimitive(x.getValue() == y.getValue());
            }
        });

        ops.put("+", new ArithmeticOperation() {
            @Override
            Primitive eval(NumberPrimitive x, NumberPrimitive y) {
                return new NumberPrimitive(x.getValue() + y.getValue());
            }
        });

        ops.put("-", new ArithmeticOperation() {
            @Override
            Primitive eval(NumberPrimitive x, NumberPrimitive y) {
                return new NumberPrimitive(x.getValue() - y.getValue());
            }
        });

        ops.put("*", new ArithmeticOperation() {
            @Override
            Primitive eval(NumberPrimitive x, NumberPrimitive y) {
                return new NumberPrimitive(x.getValue() * y.getValue());
            }
        });

        ops.put("vector-ref", parameters -> {
            String name = parameters.get(0).accept(DefaultPrimitiveVisitor.getQuote).getQuote();
            int idx = parameters.get(1).accept(DefaultPrimitiveVisitor.getNumber).getValue();
            return vectors.get(name)[idx];
        });

        ops.put("vector-set!", parameters -> {
            String name = parameters.get(0).accept(DefaultPrimitiveVisitor.getQuote).getQuote();
            int idx = parameters.get(1).accept(DefaultPrimitiveVisitor.getNumber).getValue();
            return vectors.get(name)[idx] = parameters.get(2);
        });

        ops.put("pointer-to-pair?", parameters -> new BooleanPrimitive(parameters.get(0).accept(new DefaultPrimitiveVisitor<>() {
            @Override
            public Boolean visit(NumberPrimitive primitive) {
                return true;
            }

            @Override
            public Boolean visit(QuotePrimitive primitive) {
                return false;
            }

            @Override
            public Boolean visit(BooleanPrimitive primitive) {
                return false;
            }

            @Override
            public Boolean visit(NothingPrimitive primitive) {
                return false;
            }

            @Override
            public Boolean visit(PairPrimitive primitive) {
                return false;
            }
        })));

        ops.put("broken-heart?", parameters -> new BooleanPrimitive(parameters.get(0).accept(new DefaultPrimitiveVisitor<>() {
            @Override
            public Boolean visit(NumberPrimitive primitive) {
                return false;
            }

            @Override
            public Boolean visit(QuotePrimitive primitive) {
                return primitive.getQuote().equals("broken-heart");
            }

            @Override
            public Boolean visit(BooleanPrimitive primitive) {
                return false;
            }

            @Override
            public Boolean visit(NothingPrimitive primitive) {
                return false;
            }

            @Override
            public Boolean visit(PairPrimitive primitive) {
                return false;
            }
        })));

        ops.put("read", parameters -> {
            try {
                String source = Files.readString(Path.of("test.sch"));
                Optional<Expression> optionalExpression = LispParser.parseExpression(new Cursor(source));
                if (optionalExpression.isPresent()) {
                    return expressionToPrimitive(optionalExpression.get());
                } else {
                    throw new IllegalStateException("Read failed: syntax error.");
                }
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        });

        ops.put("get-global-environment", parameters -> {
            Primitive cur = NothingPrimitive.SINGLETON;
            for(String key : builtin.keySet()){
                Primitive val = new PairPrimitive(new QuotePrimitive("primitive"), new QuotePrimitive(key));
                cur = new PairPrimitive(new PairPrimitive(new QuotePrimitive(key), val), cur);
            }
            return new PairPrimitive(cur, NothingPrimitive.SINGLETON);
        });

        ops.put("self-evaluating?", parameters -> new BooleanPrimitive(parameters.get(0).accept(testExpression((pair) -> false, (quote) -> quote.getQuote().chars().allMatch(Character::isDigit)))));
        ops.put("variable?", parameters -> new BooleanPrimitive(parameters.get(0).accept(testExpression((pair) -> false, (quote) -> true))));
        ops.put("quoted?", parameters -> new BooleanPrimitive(parameters.get(0).accept(testPair("quote"))));
        ops.put("definition?", parameters -> new BooleanPrimitive(parameters.get(0).accept(testPair("define"))));
        ops.put("if?", parameters -> new BooleanPrimitive(parameters.get(0).accept(testPair("if"))));
        ops.put("lambda?", parameters -> new BooleanPrimitive(parameters.get(0).accept(testPair("lambda"))));
        ops.put("begin?", parameters -> new BooleanPrimitive(parameters.get(0).accept(testPair("begin"))));
        ops.put("application?", parameters -> new BooleanPrimitive(parameters.get(0).accept(testExpression((pair) -> true, (quote) -> false))));
        ops.put("assignment?", parameters -> new BooleanPrimitive(parameters.get(0).accept(testPair("set!"))));

        ops.put("lookup-variable-value", parameters -> {
            String name = parameters.get(0).accept(DefaultPrimitiveVisitor.getQuote).getQuote();
            return lookupVariable(name, parameters.get(1)).accept(DefaultPrimitiveVisitor.getPair).getCdr();
        });

        ops.put("text-of-quotation", parameters -> parameters.get(0).accept(DefaultPrimitiveVisitor.getQuote));
        ops.put("text-of-number", parameters -> new NumberPrimitive(Integer.parseInt(parameters.get(0).accept(DefaultPrimitiveVisitor.getQuote).getQuote())));

        ops.put("lambda-parameters", parameters -> parameters.get(0).
                accept(DefaultPrimitiveVisitor.getPair).
                getCdr().
                accept(DefaultPrimitiveVisitor.getPair).
                getCar());

        ops.put("lambda-body", parameters -> parameters.get(0).
                accept(DefaultPrimitiveVisitor.getPair).
                getCdr().
                accept(DefaultPrimitiveVisitor.getPair).
                getCdr());

        ops.put("make-procedure", parameters -> PairPrimitive.fromParameter(new QuotePrimitive("lambda"), parameters.get(0), parameters.get(1), parameters.get(2)));

        ops.put("operator", parameters -> parameters.get(0).
                accept(DefaultPrimitiveVisitor.getPair).
                getCar());

        ops.put("operands", parameters -> parameters.get(0).
                accept(DefaultPrimitiveVisitor.getPair).
                getCdr());

        ops.put("empty-arglist", parameters -> NothingPrimitive.SINGLETON);

        ops.put("no-operands?", parameters -> new BooleanPrimitive(parameters.get(0).accept(new DefaultPrimitiveVisitor<>() {
            @Override
            public Boolean visit(PairPrimitive primitive) {
                return false;
            }

            @Override
            public Boolean visit(NothingPrimitive primitive) {
                return true;
            }
        })));

        ops.put("first-operand", parameters -> parameters.get(0).
                accept(DefaultPrimitiveVisitor.getPair).
                getCar());

        ops.put("last-operand?", parameters -> new BooleanPrimitive(parameters.get(0).
                accept(DefaultPrimitiveVisitor.getPair).
                getCdr().
                accept(new DefaultPrimitiveVisitor<>() {
                    @Override
                    public Boolean visit(NothingPrimitive primitive) {
                        return true;
                    }

                    @Override
                    public Boolean visit(PairPrimitive primitive) {
                        return false;
                    }
                })));

        ops.put("adjoin-arg", parameters -> append(parameters.get(1), PairPrimitive.fromParameter(parameters.get(0))));

        ops.put("rest-operands", parameters -> parameters.get(0).
                accept(DefaultPrimitiveVisitor.getPair).
                getCdr());

        ops.put("primitive-procedure?", parameters -> new BooleanPrimitive(
            parameters.get(0).
                    accept(DefaultPrimitiveVisitor.getPair).
                    getCar().
                    accept(DefaultPrimitiveVisitor.getQuote).
                    getQuote().
                    equals("primitive")
        ));

        ops.put("compound-procedure?", parameters -> new BooleanPrimitive(
                parameters.get(0).
                        accept(DefaultPrimitiveVisitor.getPair).
                        getCar().
                        accept(DefaultPrimitiveVisitor.getQuote).
                        getQuote().
                        equals("lambda")
        ));

        ops.put("apply-primitive-procedure", parameters -> {
            String name = parameters.get(0).accept(DefaultPrimitiveVisitor.getPair).getCdr().accept(DefaultPrimitiveVisitor.getQuote).getQuote();
            return builtin.get(name).apply(parameters.get(1), parameters.get(2));
        });

        ops.put("procedure-parameters", parameters ->
                parameters.get(0).
                accept(DefaultPrimitiveVisitor.getPair).
                getCdr().
                accept(DefaultPrimitiveVisitor.getPair).
                getCar());

        ops.put("procedure-body", parameters ->
                parameters.get(0).
                        accept(DefaultPrimitiveVisitor.getPair).
                        getCdr().
                        accept(DefaultPrimitiveVisitor.getPair).
                        getCdr().
                        accept(DefaultPrimitiveVisitor.getPair).
                        getCar());

        ops.put("procedure-environment", parameters ->
                parameters.get(0).
                        accept(DefaultPrimitiveVisitor.getPair).
                        getCdr().
                        accept(DefaultPrimitiveVisitor.getPair).
                        getCdr().
                        accept(DefaultPrimitiveVisitor.getPair).
                        getCdr().
                        accept(DefaultPrimitiveVisitor.getPair).
                        getCar());

        ops.put("extend-environment", parameters -> new PairPrimitive(putParameterEnv(parameters.get(0), parameters.get(1), NothingPrimitive.SINGLETON), parameters.get(2)));

        ops.put("begin-actions", parameters ->
                parameters.get(0).
                        accept(DefaultPrimitiveVisitor.getPair).
                        getCdr());

        ops.put("first-exp", parameters ->
                parameters.get(0).
                        accept(DefaultPrimitiveVisitor.getPair).
                        getCar());

        ops.put("last-exp?", parameters -> new BooleanPrimitive(parameters.get(0).
                accept(DefaultPrimitiveVisitor.getPair).
                getCdr().
                accept(new DefaultPrimitiveVisitor<>() {
                    @Override
                    public Boolean visit(NothingPrimitive primitive) {
                        return true;
                    }

                    @Override
                    public Boolean visit(PairPrimitive primitive) {
                        return false;
                    }
                })));

        ops.put("rest-exps", parameters -> parameters.get(0).
                accept(DefaultPrimitiveVisitor.getPair).
                getCdr());

        ops.put("if-predicate", parameters -> parameters.get(0).
                accept(DefaultPrimitiveVisitor.getPair).
                getCdr().
                accept(DefaultPrimitiveVisitor.getPair).
                getCar()
        );

        ops.put("if-consequent", parameters -> parameters.get(0).
                accept(DefaultPrimitiveVisitor.getPair).
                getCdr().
                accept(DefaultPrimitiveVisitor.getPair).
                getCdr().
                accept(DefaultPrimitiveVisitor.getPair).
                getCar()
        );

        ops.put("if-alternative", parameters -> parameters.get(0).
                accept(DefaultPrimitiveVisitor.getPair).
                getCdr().
                accept(DefaultPrimitiveVisitor.getPair).
                getCdr().
                accept(DefaultPrimitiveVisitor.getPair).
                getCdr().
                accept(DefaultPrimitiveVisitor.getPair).
                getCar()
        );

        ops.put("assignment-variable", parameters -> parameters.get(0).
                accept(DefaultPrimitiveVisitor.getPair).
                getCdr().
                accept(DefaultPrimitiveVisitor.getPair).
                getCar()
        );

        ops.put("assignment-value", parameters -> parameters.get(0).
                accept(DefaultPrimitiveVisitor.getPair).
                getCdr().
                accept(DefaultPrimitiveVisitor.getPair).
                getCdr().
                accept(DefaultPrimitiveVisitor.getPair).
                getCar()
        );

        ops.put("set-variable-value!", parameters -> {
            String name = parameters.get(0).accept(DefaultPrimitiveVisitor.getQuote).getQuote();
            lookupVariable(name, parameters.get(2)).accept(DefaultPrimitiveVisitor.getPair).setCdr(parameters.get(1));
            return parameters.get(1);
        });

        ops.put("definition-variable", parameters -> parameters.get(0).
                accept(DefaultPrimitiveVisitor.getPair).
                getCdr().
                accept(DefaultPrimitiveVisitor.getPair).
                getCar()
        );

        ops.put("definition-value", parameters -> parameters.get(0).
                accept(DefaultPrimitiveVisitor.getPair).
                getCdr().
                accept(DefaultPrimitiveVisitor.getPair).
                getCdr().
                accept(DefaultPrimitiveVisitor.getPair).
                getCar()
        );

        ops.put("define-variable!", parameters ->{
            PairPrimitive pair = new PairPrimitive(parameters.get(0), parameters.get(1));
            PairPrimitive env = parameters.get(2).accept(DefaultPrimitiveVisitor.getPair);
            env.setCar(new PairPrimitive(pair, env.getCar()));
            return env;
        });

        ops.put("make-compiled-procedure", parameters -> PairPrimitive.fromParameter(new QuotePrimitive("lambda"), parameters.get(0), parameters.get(1)));
        ops.put("compiled-procedure-entry", parameters -> parameters.get(0).
                accept(DefaultPrimitiveVisitor.getPair).
                getCdr().
                accept(DefaultPrimitiveVisitor.getPair).
                getCar()
        );
        ops.put("compiled-procedure-env", parameters -> parameters.get(0).
                accept(DefaultPrimitiveVisitor.getPair).
                getCdr().
                accept(DefaultPrimitiveVisitor.getPair).
                getCdr().
                accept(DefaultPrimitiveVisitor.getPair).
                getCar()
        );

        ops.put("false?", parameters -> new BooleanPrimitive(!parameters.get(0).accept(DefaultPrimitiveVisitor.getBoolean).isValue()));

        // Init registers
        machine.getHead().getList().stream().map(Expression::getSymbol).forEach((name) -> registers.put(name, new NumberPrimitive(0)));

        // Init instructions
        machine.getTail().getHead().getList().forEach(this::handleTextInstruction);
    }

    private Primitive append(Primitive a, Primitive b){
        return a.accept(new DefaultPrimitiveVisitor<>() {
            @Override
            public Primitive visit(PairPrimitive primitive) {
                return new PairPrimitive(primitive.getCar(), append(primitive.getCdr(), b));
            }

            @Override
            public Primitive visit(NothingPrimitive primitive) {
                return b;
            }
        });
    }

    private Primitive putParameterEnv(Primitive arguments, Primitive parameters, Primitive env){
        return arguments.accept(new DefaultPrimitiveVisitor<>() {
            @Override
            public Primitive visit(PairPrimitive primitive) {
                Primitive pair = new PairPrimitive(primitive.getCar(), parameters.accept(DefaultPrimitiveVisitor.getPair).getCar());
                return new PairPrimitive(pair, putParameterEnv(primitive.getCdr(), parameters.accept(DefaultPrimitiveVisitor.getPair).getCdr(), env));
            }

            @Override
            public Primitive visit(NothingPrimitive primitive) {
                return env;
            }
        });
    }

    private Optional<Primitive> lookEnv(String name, Primitive env){
        return env.accept(new DefaultPrimitiveVisitor<>() {
            @Override
            public Optional<Primitive> visit(PairPrimitive primitive) {
                PairPrimitive item = primitive.getCar().accept(DefaultPrimitiveVisitor.getPair);
                if(item.getCar().accept(DefaultPrimitiveVisitor.getQuote).getQuote().equals(name)){
                    return Optional.of(item);
                } else {
                    return lookEnv(name, primitive.getCdr());
                }
            }

            @Override
            public Optional<Primitive> visit(NothingPrimitive primitive) {
                return Optional.empty();
            }
        });
    }

    private Primitive lookupVariable(String name, Primitive envs) {
        return envs.accept(new DefaultPrimitiveVisitor<>() {
            @Override
            public Primitive visit(PairPrimitive primitive) {
                return lookEnv(name, primitive.getCar()).orElseGet(() -> lookupVariable(name, primitive.getCdr()));
            }

            @Override
            public Primitive visit(NothingPrimitive primitive) {
                return NothingPrimitive.SINGLETON;
            }
        });
    }

    private PrimitiveVisitor<Boolean> testPair(String name) {
        return testExpression((pair) -> pair.getCar().accept(new DefaultPrimitiveVisitor<>() {
            @Override
            public Boolean visit(QuotePrimitive primitive) {
                return primitive.getQuote().equals(name);
            }

            @Override
            public Boolean visit(PairPrimitive primitive) {
                return false;
            }

            @Override
            public Boolean visit(NothingPrimitive primitive) {
                return false;
            }
        }), (symbol) -> false);
    }

    private PrimitiveVisitor<Boolean> testExpression(Predicate<PairPrimitive> pair, Predicate<QuotePrimitive> quote) {
        return new DefaultPrimitiveVisitor<>() {
            @Override
            public Boolean visit(PairPrimitive primitive) {
                return pair.test(primitive);
            }

            @Override
            public Boolean visit(QuotePrimitive primitive) {
                return quote.test(primitive);
            }

            @Override
            public Boolean visit(NothingPrimitive primitive) {
                return false;
            }
        };
    }

    private Primitive expressionToPrimitive(Expression e) {
        return e.visit((list) -> {
            if (list.isNil()) {
                return NothingPrimitive.SINGLETON;
            } else {
                return new PairPrimitive(expressionToPrimitive(e.getHead()), expressionToPrimitive(e.getTail()));
            }
        }, (symbol) -> new QuotePrimitive(symbol.getSymbol()));
    }

    public void execute() {
        while (pc < instructions.size()) {
            instructions.get(pc).execute();
        }
    }

    private Instruction buildAssign(ListExpression list) {
        String reg = list.get(1).getSymbol();
        ListExpression body = list.get(2).getList();
        Supplier<Primitive> supplier = body.testHead("op"::equals) ? perform(list.getTail().getTail()) : supply(body);

        return () -> {
            registers.put(reg, supplier.get());
            nextPC();
        };
    }

    private Instruction buildPerform(ListExpression list) {
        Supplier<Primitive> supplier = perform(list.getTail());
        return () -> {
            supplier.get();
            nextPC();
        };
    }

    private Instruction buildTest(ListExpression list) {
        Supplier<Primitive> supplier = perform(list.getTail());
        return () -> {
            flag = supplier.get().accept(DefaultPrimitiveVisitor.getBoolean).isValue();
            nextPC();
        };
    }

    private Instruction buildBranch(ListExpression list) {
        Supplier<Primitive> target = supply(list.get(1).getList());

        return () -> {
            if (flag) {
                pc = target.get().accept(DefaultPrimitiveVisitor.getNumber).getValue();
            } else {
                nextPC();
            }
        };
    }

    private Instruction buildGoto(ListExpression list) {
        Supplier<Primitive> target = supply(list.get(1).getList());

        return () -> pc = target.get().accept(DefaultPrimitiveVisitor.getNumber).getValue();
    }

    private Supplier<Primitive> supply(ListExpression list) {
        if (list.testHead("label"::equals)) { // Label
            String name = list.get(1).getSymbol();
            return () -> new NumberPrimitive(labels.get(name));
        } else if (list.testHead("reg"::equals)) { // Reg
            String reg = list.get(1).getSymbol();
            return () -> registers.get(reg);
        } else if (list.testHead("const"::equals)) { // Constant
            Primitive constant = buildConstant(list);
            return () -> constant;
        } else {
            throw new IllegalStateException("Unexpected: " + list);
        }
    }

    private Primitive buildConstant(ListExpression list) {
        try {
            return new NumberPrimitive(Integer.parseInt(list.get(1).getSymbol()));
        } catch (NumberFormatException e) {
            return new QuotePrimitive(list.get(1).getSymbol());
        }
    }

    private Supplier<Primitive> perform(ListExpression list) {
        String name = list.getHead().getList().get(1).getSymbol();
        List<Supplier<Primitive>> parameters = list.getTail().stream().map(Expression::getList).map(this::supply).collect(Collectors.toList());
        Operation op = ops.get(name);
        return () -> op.perform(parameters.stream().map(Supplier::get).collect(Collectors.toList()));
    }

    private Instruction buildSave(ListExpression list) {
        String reg = list.get(1).getSymbol();
        return () -> {
            stack.push(registers.get(reg));
            nextPC();
        };
    }

    private Instruction buildRestore(ListExpression list) {
        String reg = list.get(1).getSymbol();
        return () -> {
            registers.put(reg, stack.pop());
            nextPC();
        };
    }

    private void nextPC() {
        pc = pc + 1;
    }

    private void handleTextInstruction(Expression text) {
        if (text.isSymbol()) { // Label
            labels.put(text.getSymbol(), instructions.size());
        } else { // Instruction
            ListExpression list = text.getList();
            if (list.testHead("assign"::equals)) {
                instructions.add(buildAssign(list));
            } else if (list.testHead("perform"::equals)) {
                instructions.add(buildPerform(list));
            } else if (list.testHead("test"::equals)) {
                instructions.add(buildTest(list));
            } else if (list.testHead("branch"::equals)) {
                instructions.add(buildBranch(list));
            } else if (list.testHead("goto"::equals)) {
                instructions.add(buildGoto(list));
            } else if (list.testHead("save"::equals)) {
                instructions.add(buildSave(list));
            } else if (list.testHead("restore"::equals)) {
                instructions.add(buildRestore(list));
            } else {
                throw new IllegalStateException("Unexpected: " + list);
            }
        }
    }
}
