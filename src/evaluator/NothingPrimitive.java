package evaluator;

import java.util.Collections;
import java.util.List;

public class NothingPrimitive implements ListPrimitive {
    public static final NothingPrimitive SINGLETON = new NothingPrimitive();

    private NothingPrimitive() {
    }

    @Override
    public List<Primitive> toList() {
        return Collections.emptyList();
    }

    @Override
    public boolean isNil() {
        return true;
    }

    @Override
    public String toString() {
        return "nothing";
    }

    @Override
    public <T> T accept(PrimitiveVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
