package evaluator;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PairPrimitive implements ListPrimitive {
    private Primitive head;
    private Primitive tail;

    public PairPrimitive(Primitive head, Primitive tail) {
        this.head = head;
        this.tail = tail;
    }

    public Primitive getHead() {
        return head;
    }

    public void setHead(Primitive head) {
        this.head = head;
    }

    public Primitive getTail() {
        return tail;
    }

    public void setTail(Primitive tail) {
        this.tail = tail;
    }

    public Primitive get(int index) {
        if (index == 0) return head;

        return tail.accept(DefaultPrimitiveVisitor.expectPairPrimitive).get(index - 1);
    }

    @Override
    public List<Primitive> toList() {
        List<Primitive> primitives = new ArrayList<>();

        PairPrimitive pair = this;
        while (true) {
            primitives.add(pair.getHead());
            if (pair.getTail() != NothingPrimitive.SINGLETON) {
                pair = pair.getTail().accept(DefaultPrimitiveVisitor.expectPairPrimitive);
            } else {
                break;
            }
        }

        return primitives;
    }

    @Override
    public boolean isNil() {
        return false;
    }


    @Override
    public String toString() {
        return "(" + head + ", " + tail + ")";
    }

    public static ListPrimitive fromList(List<Primitive> primitives){
        ListPrimitive cur = NothingPrimitive.SINGLETON;
        for(int i = primitives.size() - 1;i >= 0 ;i--){
            cur = new PairPrimitive(primitives.get(i) ,cur);
        }

        return cur;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PairPrimitive that = (PairPrimitive) o;
        return Objects.equals(head, that.head) && Objects.equals(tail, that.tail);
    }

    @Override
    public <T> T accept(PrimitiveVisitor<T> visitor) {
        return visitor.visit(this);
    }

}
