package evaluator;

import java.util.List;

public interface ListPrimitive extends Primitive{
    List<Primitive> toList();

    boolean isNil();
}
