package evaluator.eval;

public class BooleanPrimitive implements Primitive {
    public static final BooleanPrimitive TRUE_SINGLETON = new BooleanPrimitive(true);
    public static final BooleanPrimitive FALSE_SINGLETON = new BooleanPrimitive(false);

    private boolean value;

    public BooleanPrimitive(boolean value) {
        this.value = value;
    }

    public boolean isValue() {
        return value;
    }

    public void setValue(boolean value) {
        this.value = value;
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
