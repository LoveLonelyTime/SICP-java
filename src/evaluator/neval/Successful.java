package evaluator.neval;

public interface Successful {
    Primitive call(Primitive value, Failed failed);
}
