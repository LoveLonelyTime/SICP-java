package machine;

import java.util.List;

public abstract class ArithmeticOperation implements Operation {

    abstract Primitive eval(NumberPrimitive x, NumberPrimitive y);

    @Override
    public Primitive perform(List<Primitive> parameters) {
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
