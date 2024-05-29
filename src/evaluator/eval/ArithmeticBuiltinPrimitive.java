package evaluator.eval;

import java.util.List;

public abstract class ArithmeticBuiltinPrimitive extends BuiltinPrimitive{
    abstract Primitive eval(NumberPrimitive x, NumberPrimitive y);

    @Override
    Primitive eval(List<Primitive> parameters, Environment env) {
        PrimitiveVisitor<NumberPrimitive> visitor = new DefaultPrimitiveVisitor<>(){
            @Override
            public NumberPrimitive visit(NumberPrimitive primitive) {
                return primitive;
            }
        };
        NumberPrimitive x = parameters.get(0).accept(visitor);
        NumberPrimitive y = parameters.get(1).accept(visitor);
        return eval(x, y);
    }
}