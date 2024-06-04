package evaluator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Environment {
    private final Map<String, Primitive> table = new HashMap<>();
    private Environment topLayer;

    public Environment(Environment topLayer) {
        this.topLayer = topLayer;
    }

    public static Environment createGlobalEnvironment() {
        Environment globalEnv = new Environment(null);
        globalEnv.define("nothing", NothingPrimitive.SINGLETON);
        globalEnv.define("true", BooleanPrimitive.TRUE_SINGLETON);
        globalEnv.define("else", BooleanPrimitive.TRUE_SINGLETON);
        globalEnv.define("false", BooleanPrimitive.FALSE_SINGLETON);

        globalEnv.define("eq?", new BuiltinPrimitive() {
            @Override
            public Primitive eval(List<Primitive> parameters) {
                return BooleanPrimitive.getInstance(parameters.get(0).equals(parameters.get(1)));
            }
        });

        globalEnv.define("nothing?", new BuiltinPrimitive() {
            @Override
            public Primitive eval(List<Primitive> parameters) {
                return BooleanPrimitive.getInstance(parameters.get(0) == NothingPrimitive.SINGLETON);
            }
        });

        globalEnv.define("true?", new BuiltinPrimitive() {
            @Override
            public Primitive eval(List<Primitive> parameters) {
                return BooleanPrimitive.getInstance(parameters.get(0) == BooleanPrimitive.TRUE_SINGLETON);
            }
        });

        globalEnv.define("false?", new BuiltinPrimitive() {
            @Override
            public Primitive eval(List<Primitive> parameters) {
                return BooleanPrimitive.getInstance(parameters.get(0) == BooleanPrimitive.FALSE_SINGLETON);
            }
        });

        globalEnv.define("number?", new BuiltinPrimitive() {
            @Override
            public Primitive eval(List<Primitive> parameters) {
                return BooleanPrimitive.getInstance(parameters.get(0) instanceof NumberPrimitive);
            }
        });

        globalEnv.define("pair?", new BuiltinPrimitive() {
            @Override
            public Primitive eval(List<Primitive> parameters) {
                return BooleanPrimitive.getInstance(parameters.get(0) instanceof PairPrimitive);
            }
        });

        globalEnv.define("symbol?", new BuiltinPrimitive() {
            @Override
            public Primitive eval(List<Primitive> parameters) {
                return BooleanPrimitive.getInstance(parameters.get(0) instanceof SymbolPrimitive);
            }
        });

        globalEnv.define("not", new BuiltinPrimitive() {
            @Override
            public Primitive eval(List<Primitive> parameters) {
                boolean x = parameters.get(0).accept(DefaultPrimitiveVisitor.expectBooleanPrimitive).getValue();

                return BooleanPrimitive.getInstance(!x);
            }
        });

        globalEnv.define("and", new BuiltinPrimitive() {
            @Override
            public Primitive eval(List<Primitive> parameters) {
                boolean x = parameters.get(0).accept(DefaultPrimitiveVisitor.expectBooleanPrimitive).getValue();
                boolean y = parameters.get(1).accept(DefaultPrimitiveVisitor.expectBooleanPrimitive).getValue();

                return BooleanPrimitive.getInstance(x && y);
            }
        });

        globalEnv.define("or", new BuiltinPrimitive() {
            @Override
            public Primitive eval(List<Primitive> parameters) {
                boolean x = parameters.get(0).accept(DefaultPrimitiveVisitor.expectBooleanPrimitive).getValue();
                boolean y = parameters.get(1).accept(DefaultPrimitiveVisitor.expectBooleanPrimitive).getValue();

                return BooleanPrimitive.getInstance(x || y);
            }
        });


        globalEnv.define("+", new BuiltinPrimitive() {
            @Override
            public Primitive eval(List<Primitive> parameters) {
                double x = parameters.get(0).accept(DefaultPrimitiveVisitor.expectNumberPrimitive).getValue();
                double y = parameters.get(1).accept(DefaultPrimitiveVisitor.expectNumberPrimitive).getValue();

                return new NumberPrimitive(x + y);
            }
        });

        globalEnv.define("-", new BuiltinPrimitive() {
            @Override
            public Primitive eval(List<Primitive> parameters) {
                double x = parameters.get(0).accept(DefaultPrimitiveVisitor.expectNumberPrimitive).getValue();
                double y = parameters.get(1).accept(DefaultPrimitiveVisitor.expectNumberPrimitive).getValue();

                return new NumberPrimitive(x - y);
            }
        });

        globalEnv.define("*", new BuiltinPrimitive() {
            @Override
            public Primitive eval(List<Primitive> parameters) {
                double x = parameters.get(0).accept(DefaultPrimitiveVisitor.expectNumberPrimitive).getValue();
                double y = parameters.get(1).accept(DefaultPrimitiveVisitor.expectNumberPrimitive).getValue();

                return new NumberPrimitive(x * y);
            }
        });

        globalEnv.define("/", new BuiltinPrimitive() {
            @Override
            public Primitive eval(List<Primitive> parameters) {
                double x = parameters.get(0).accept(DefaultPrimitiveVisitor.expectNumberPrimitive).getValue();
                double y = parameters.get(1).accept(DefaultPrimitiveVisitor.expectNumberPrimitive).getValue();

                return new NumberPrimitive(x / y);
            }
        });

        globalEnv.define("=", new BuiltinPrimitive() {
            @Override
            public Primitive eval(List<Primitive> parameters) {
                double x = parameters.get(0).accept(DefaultPrimitiveVisitor.expectNumberPrimitive).getValue();
                double y = parameters.get(1).accept(DefaultPrimitiveVisitor.expectNumberPrimitive).getValue();

                return BooleanPrimitive.getInstance(Double.compare(x, y) == 0);
            }
        });

        globalEnv.define(">", new BuiltinPrimitive() {
            @Override
            public Primitive eval(List<Primitive> parameters) {
                double x = parameters.get(0).accept(DefaultPrimitiveVisitor.expectNumberPrimitive).getValue();
                double y = parameters.get(1).accept(DefaultPrimitiveVisitor.expectNumberPrimitive).getValue();

                return BooleanPrimitive.getInstance(x > y);
            }
        });

        globalEnv.define("<", new BuiltinPrimitive() {
            @Override
            public Primitive eval(List<Primitive> parameters) {
                double x = parameters.get(0).accept(DefaultPrimitiveVisitor.expectNumberPrimitive).getValue();
                double y = parameters.get(1).accept(DefaultPrimitiveVisitor.expectNumberPrimitive).getValue();

                return BooleanPrimitive.getInstance(x < y);
            }
        });

        globalEnv.define(">=", new BuiltinPrimitive() {
            @Override
            public Primitive eval(List<Primitive> parameters) {
                double x = parameters.get(0).accept(DefaultPrimitiveVisitor.expectNumberPrimitive).getValue();
                double y = parameters.get(1).accept(DefaultPrimitiveVisitor.expectNumberPrimitive).getValue();

                return BooleanPrimitive.getInstance(x >= y);
            }
        });

        globalEnv.define("<=", new BuiltinPrimitive() {
            @Override
            public Primitive eval(List<Primitive> parameters) {
                double x = parameters.get(0).accept(DefaultPrimitiveVisitor.expectNumberPrimitive).getValue();
                double y = parameters.get(1).accept(DefaultPrimitiveVisitor.expectNumberPrimitive).getValue();

                return BooleanPrimitive.getInstance(x <= y);
            }
        });

        globalEnv.define("cons", new BuiltinPrimitive() {
            @Override
            public Primitive eval(List<Primitive> parameters) {
                return new PairPrimitive(parameters.get(0), parameters.get(1));
            }
        });

        globalEnv.define("list", new BuiltinPrimitive() {
            @Override
            public Primitive eval(List<Primitive> parameters) {
                return PairPrimitive.fromList(parameters);
            }
        });

        globalEnv.define("car", new BuiltinPrimitive() {
            @Override
            public Primitive eval(List<Primitive> parameters) {
                return parameters.get(0).accept(DefaultPrimitiveVisitor.expectPairPrimitive).getHead();
            }
        });

        globalEnv.define("cdr", new BuiltinPrimitive() {
            @Override
            public Primitive eval(List<Primitive> parameters) {
                return parameters.get(0).accept(DefaultPrimitiveVisitor.expectPairPrimitive).getTail();
            }
        });

        globalEnv.define("set-car!", new BuiltinPrimitive() {
            @Override
            public Primitive eval(List<Primitive> parameters) {
                parameters.get(0).accept(DefaultPrimitiveVisitor.expectPairPrimitive).setHead(parameters.get(1));
                return parameters.get(1);
            }
        });

        globalEnv.define("set-cdr!", new BuiltinPrimitive() {
            @Override
            public Primitive eval(List<Primitive> parameters) {
                parameters.get(0).accept(DefaultPrimitiveVisitor.expectPairPrimitive).setTail(parameters.get(1));
                return parameters.get(1);
            }
        });

        globalEnv.define("error", new BuiltinPrimitive() {
            @Override
            public Primitive eval(List<Primitive> parameters) {
                String msg = parameters.get(0).accept(DefaultPrimitiveVisitor.expectSymbolPrimitive).getSymbol();
                throw new IllegalStateException("Error: " + msg);
            }
        });

        globalEnv.define("print", new BuiltinPrimitive() {
            @Override
            public Primitive eval(List<Primitive> parameters) {
                parameters.forEach(System.out::println);
                return NothingPrimitive.SINGLETON;
            }
        });

        globalEnv.define("can-cast-number?", new BuiltinPrimitive() {
            @Override
            public Primitive eval(List<Primitive> parameters) {
                String str = parameters.get(0).accept(DefaultPrimitiveVisitor.expectSymbolPrimitive).getSymbol();
                try{
                    Double.parseDouble(str);
                    return BooleanPrimitive.TRUE_SINGLETON;
                } catch (NumberFormatException e){
                    return BooleanPrimitive.FALSE_SINGLETON;
                }
            }
        });

        globalEnv.define("cast-number", new BuiltinPrimitive() {
            @Override
            public Primitive eval(List<Primitive> parameters) {
                String str = parameters.get(0).accept(DefaultPrimitiveVisitor.expectSymbolPrimitive).getSymbol();
                return new NumberPrimitive(Double.parseDouble(str));
            }
        });

        globalEnv.define("debug", new BuiltinPrimitive() {
            @Override
            public Primitive eval(List<Primitive> parameters) {
                throw new IllegalStateException("Breakpoint");
            }
        });

        return globalEnv;
    }

    public Environment getTopLayer() {
        return topLayer;
    }

    public void setTopLayer(Environment topLayer) {
        this.topLayer = topLayer;
    }

    public Primitive lookup(String name) {
        if (table.containsKey(name)) {
            return table.get(name);
        } else if (topLayer != null) {
            return topLayer.lookup(name);
        } else {
            throw new IllegalStateException("Undefined variable: " + name);
        }
    }

    public Environment extend() {
        return new Environment(this);
    }

    public Primitive define(String name, Primitive value) {
        table.put(name, value);
        return value;
    }

    public Primitive set(String name, Primitive value) {
        if (table.containsKey(name)) {
            return table.put(name, value);
        } else if (topLayer != null) {
            return topLayer.set(name, value);
        } else {
            throw new IllegalStateException("Undefined variable: " + name);
        }
    }

    public void putParameters(List<String> arguments, List<Primitive> parameters) {
        if (arguments.size() != parameters.size()) {
            throw new IllegalStateException("Unequal number of parameters:" + arguments.size() + ", but " + parameters.size());
        }
        for (int i = 0; i < arguments.size(); i++) {
            define(arguments.get(i), parameters.get(i));
        }
    }

    @Override
    public String toString() {
        return "Environment{" +
                "table=" + table +
                ", @topLayer=" + topLayer.hashCode() +
                '}';
    }
}
