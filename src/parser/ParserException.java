package parser;

public class ParserException extends RuntimeException {
    public ParserException(Cursor cursor, String error) {
        super(cursor.toString() + " got a error: " + error);
    }

    public ParserException(Cursor cursor) {
        this(cursor, "Unexpected.");
    }
}
