package llist;

import java.util.function.Predicate;

// 3.5 Stream
// By Java
public class Main {
    public static LazyList<Integer> sieve(LazyList<Integer> remainder){
        return new LazyList<>(remainder.get(0), ()->filter(remainder.getTail(), (x)->x % remainder.get(0) != 0));
    }

    public static LazyList<Integer> primes(){
        return sieve(infinityRange(2));
    }
    public static <E> LazyList<E> filter(LazyList<E> list, Predicate<E> pred){
        if(pred.test(list.get(0))){
            return new LazyList<>(list.get(0), ()->filter(list.getTail(), pred));
        }else{
            return filter(list.getTail(),pred);
        }
    }
    public static LazyList<Integer> fibsCons(int a, int b){
        return new LazyList<>(a, ()->fibsCons(b, a + b));
    }

    public static LazyList<Integer> fibs(){
        return fibsCons(0, 1);
    }

    public static LazyList<Integer> infinityRange(int a){
        return new LazyList<>(a, ()->infinityRange(a + 1));
    }

    public static LazyList<Integer> lazyRange(int a, int b){
        if(a > b)
            return null;
        else
            return new LazyList<>(a ,()->lazyRange(a + 1, b));
    }

    public static void main(String[] args) {
        // Get the 50th prime number
        System.out.println(primes().get(50));
        // Get the 10th fib number
        System.out.println(fibs().get(10));
    }
}
