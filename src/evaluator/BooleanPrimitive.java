package evaluator;

public class BooleanPrimitive implements Primitive {
    public static final BooleanPrimitive TRUE_SINGLETON = new BooleanPrimitive(true);
    public static final BooleanPrimitive FALSE_SINGLETON = new BooleanPrimitive(false);

    private final boolean value;

    private BooleanPrimitive(boolean value) {
        this.value = value;
    }

    public static BooleanPrimitive getInstance(boolean value) {
        return value ? TRUE_SINGLETON : FALSE_SINGLETON;
    }

    public boolean getValue() {
        return value;
    }

    @Override
    public String toString() {
        return Boolean.toString(value);
    }

    @Override
    public <T> T accept(PrimitiveVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
