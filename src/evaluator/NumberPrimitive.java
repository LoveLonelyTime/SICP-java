package evaluator;

public class NumberPrimitive implements Primitive {
    private double value;

    public NumberPrimitive(double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return Double.toString(value);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NumberPrimitive that = (NumberPrimitive) o;
        return Double.compare(that.value, value) == 0;
    }

    @Override
    public <T> T accept(PrimitiveVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
