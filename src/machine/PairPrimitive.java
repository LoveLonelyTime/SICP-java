package machine;

import java.util.List;
import java.util.stream.Stream;

public class PairPrimitive implements Primitive{
    private Primitive car;
    private Primitive cdr;

    public PairPrimitive(Primitive car, Primitive cdr) {
        this.car = car;
        this.cdr = cdr;
    }

    public static Primitive fromParameter(Primitive... primitives){
        Primitive cur = NothingPrimitive.SINGLETON;
        for(int i = primitives.length - 1; i >= 0; i--){
            cur = new PairPrimitive(primitives[i], cur);
        }
        return cur;
    }

    public Primitive getCar() {
        return car;
    }

    public void setCar(Primitive car) {
        this.car = car;
    }

    public Primitive getCdr() {
        return cdr;
    }

    public void setCdr(Primitive cdr) {
        this.cdr = cdr;
    }

    @Override
    public String toString() {
        return "PairPrimitive{" +
                "car=" + car +
                ", cdr=" + cdr +
                '}';
    }

    @Override
    public <T> T accept(PrimitiveVisitor<T> visitor) {
        return visitor.visit(this);
    }
}
