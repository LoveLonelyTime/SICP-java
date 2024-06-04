package query;

import parser.ListExpression;

import java.util.List;

public interface QueryProcedure {
    List<Frame> query(ListExpression body, List<Frame> frames);
}
