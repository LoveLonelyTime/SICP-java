package machine;

public class NothingPrimitive implements Primitive{
    public static final NothingPrimitive SINGLETON = new NothingPrimitive();


    @Override
    public String toString() {
        return "nothing";
    }

    @Override
    public <T> T accept(PrimitiveVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
