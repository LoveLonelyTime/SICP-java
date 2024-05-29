package evaluator.neval;

import java.util.List;

public abstract class BuiltinPrimitive implements Primitive {
    abstract Primitive eval(List<Primitive> parameters, Environment env, Successful successful, Failed failed);

    @Override
    public String toString() {
        return "BuiltinPrimitive{}";
    }

    @Override
    public <T> T accept(PrimitiveVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
