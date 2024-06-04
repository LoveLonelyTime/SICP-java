package parser;

public class LispParser {
    private LispParser() {
    }

    private static void skipSpace(Cursor cursor) {
        while (!cursor.eof() && Character.isWhitespace(cursor.top())) {
            cursor.next();
        }
    }

    private static boolean isSymbolChar(Cursor cursor) {
        return !cursor.eof() &&
                cursor.top() != '(' &&
                cursor.top() != ')' &&
                cursor.top() != '.' &&
                !Character.isWhitespace(cursor.top());
    }

    private static SymbolExpression parseSymbol(Cursor cursor) {
        skipSpace(cursor);

        StringBuilder symbol = new StringBuilder();
        while (isSymbolChar(cursor)) {
            symbol.append(cursor.top());
            cursor.next();
        }

        if (symbol.length() == 0) throw new ParserException(cursor, "Empty symbol.");
        return new SymbolExpression(symbol.toString());
    }

    /*
     * Rest -> . Expression )
     * Rest -> )
     * Rest -> Expression Rest
     */
    private static Expression parseRest(Cursor cursor) {
        skipSpace(cursor);

        if (cursor.expectChar('.')) { // Rest -> . Expression )
            Expression e = parseExpression(cursor);

            skipSpace(cursor);
            if (cursor.expectChar(')')) { // OK
                return e;
            } else {
                throw new ParserException(cursor, "'.' isn't the last one.");
            }
        } else if (cursor.expectChar(')')) { // Rest -> )
            return NilExpression.SINGLETON;
        } else { // Rest -> Expression Rest
            Expression head = parseExpression(cursor);
            Expression tail = parseRest(cursor);
            return new PairExpression(head, tail);
        }
    }

    /*
     * Expression -> ( Rest
     * Expression -> Symbol
     */
    public static Expression parseExpression(Cursor cursor) {
        skipSpace(cursor);

        if (cursor.expectChar('(')) { // Expression -> ( Rest
            return parseRest(cursor);
        } else { // Expression -> Symbol
            return parseSymbol(cursor);
        }
    }
}
