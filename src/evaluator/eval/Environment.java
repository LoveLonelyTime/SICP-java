package evaluator.eval;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Environment{
    private final Map<String, Primitive> table = new HashMap<>();
    private Environment topLayer;

    public Environment(Environment topLayer) {
        this.topLayer = topLayer;
    }

    public Environment getTopLayer() {
        return topLayer;
    }

    public void setTopLayer(Environment topLayer) {
        this.topLayer = topLayer;
    }

    public Primitive lookup(String name){
        if(table.containsKey(name)){
            return table.get(name);
        } else if (topLayer != null){
            return topLayer.lookup(name);
        } else {
            return NothingPrimitive.SINGLETON;
        }
    }

    public Environment extend(){
        return new Environment(this);
    }

    public Primitive define(String name, Primitive value){
        table.put(name, value);
        return value;
    }

    public Primitive set(String name, Primitive value){
        if(table.containsKey(name)){
            return table.put(name, value);
        } else if (topLayer != null){
            return topLayer.set(name, value);
        } else {
            return NothingPrimitive.SINGLETON;
        }
    }

    public void putParameters(List<String> arguments, List<Primitive> parameters){
        for(int i = 0;i < arguments.size();i++){
            if(i < parameters.size()){
                define(arguments.get(i), parameters.get(i));
            }else{
                define(arguments.get(i), NothingPrimitive.SINGLETON);
            }
        }
    }

    public static Environment createGlobalEnvironment(){
        Environment globalEnv = new Environment(null);
        globalEnv.define("nothing", NothingPrimitive.SINGLETON);
        globalEnv.define("true", BooleanPrimitive.TRUE_SINGLETON);
        globalEnv.define("false", BooleanPrimitive.FALSE_SINGLETON);
        globalEnv.define("set!", new BuiltinPrimitive() {
            @Override
            Primitive eval(List<Primitive> parameters, Environment env) {
                String name = parameters.get(0).accept(new DefaultPrimitiveVisitor<>(){
                    @Override
                    public String visit(QuotePrimitive primitive) {
                        return primitive.getQuote();
                    }
                });

                return env.set(name, parameters.get(1));
            }
        });

        globalEnv.define("+", new ArithmeticBuiltinPrimitive() {
            @Override
            Primitive eval(NumberPrimitive x, NumberPrimitive y) {
                return new NumberPrimitive(x.getValue() + y.getValue());
            }
        });

        globalEnv.define("-", new ArithmeticBuiltinPrimitive() {
            @Override
            Primitive eval(NumberPrimitive x, NumberPrimitive y) {
                return new NumberPrimitive(x.getValue() - y.getValue());
            }
        });

        globalEnv.define("*", new ArithmeticBuiltinPrimitive() {
            @Override
            Primitive eval(NumberPrimitive x, NumberPrimitive y) {
                return new NumberPrimitive(x.getValue() * y.getValue());
            }
        });

        globalEnv.define("/", new ArithmeticBuiltinPrimitive() {
            @Override
            Primitive eval(NumberPrimitive x, NumberPrimitive y) {
                return new NumberPrimitive(x.getValue() / y.getValue());
            }
        });

        globalEnv.define(">", new ArithmeticBuiltinPrimitive() {
            @Override
            Primitive eval(NumberPrimitive x, NumberPrimitive y) {
                return new BooleanPrimitive(x.getValue() > y.getValue());
            }
        });

        globalEnv.define("<", new ArithmeticBuiltinPrimitive() {
            @Override
            Primitive eval(NumberPrimitive x, NumberPrimitive y) {
                return new BooleanPrimitive(x.getValue() < y.getValue());
            }
        });

        globalEnv.define("=", new ArithmeticBuiltinPrimitive() {
            @Override
            Primitive eval(NumberPrimitive x, NumberPrimitive y) {
                return new BooleanPrimitive(x.getValue() == y.getValue());
            }
        });

        globalEnv.define("print!", new BuiltinPrimitive() {
            @Override
            Primitive eval(List<Primitive> parameters, Environment env) {
                parameters.forEach(System.out::println);
                return NothingPrimitive.SINGLETON;
            }
        });

        return globalEnv;
    }

    @Override
    public String toString() {
        return "Environment{" +
                "table=" + table +
                "topLayer=" + topLayer.hashCode() +
                '}';
    }
}
