package machine;

import java.util.List;

public interface Operation {
    Primitive perform(List<Primitive> parameters);
}
