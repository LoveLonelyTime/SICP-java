package llist;

import java.util.function.Supplier;

public class LazyList<E> {
    private final E head;
    private final Supplier<LazyList<E>> tail;

    public LazyList(E head, Supplier<LazyList<E>> tail){
        this.head = head;
        this.tail = tail;
    }

    public E get(int index){
        if(index == 0) {
            return head;
        }else{
            return tail.get().get(index - 1);
        }
    }

    public LazyList<E> getTail(){
        return tail.get();
    }

    @Override
    public String toString() {
        return "(" + head + ", " + tail + ")";
    }
}
