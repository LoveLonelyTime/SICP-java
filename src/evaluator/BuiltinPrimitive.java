package evaluator;

import java.util.List;

public abstract class BuiltinPrimitive implements Primitive {
    public abstract Primitive eval(List<Primitive> parameters);

    @Override
    public String toString() {
        return "BuiltinPrimitive@" + hashCode();
    }

    @Override
    public <T> T accept(PrimitiveVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
