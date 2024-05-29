package parser;

import java.util.Optional;

public class LispParser {
    private LispParser() {}
    private static void skipSpace(Cursor cursor){
        while (cursor.hasNext() && Character.isWhitespace(cursor.top())){
            cursor.next();
        }
    }

    private static boolean isNameChar(char c){
        return c != '(' && c != ')' && !Character.isWhitespace(c);
    }

    private static Optional<Expression> parseSymbol(Cursor cursor){
        Cursor tryCursor = cursor.branch();

        skipSpace(tryCursor);
        if(!tryCursor.hasNext()) return Optional.empty();

        StringBuilder name = new StringBuilder();

        while(tryCursor.hasNext() && isNameChar(tryCursor.top())){
            name.append(tryCursor.top());
            tryCursor.next();
        }

        if(name.length() == 0) return Optional.empty();
        cursor.merge(tryCursor);
        return Optional.of(new SymbolExpression(name.toString()));
    }

    public static Optional<Expression> parseExpression(Cursor cursor){
        Cursor tryCursor = cursor.branch();

        skipSpace(tryCursor);
        if(!tryCursor.hasNext()) return Optional.empty();

        if(tryCursor.top() == '('){
            tryCursor.next();
            ListExpression list = new ListExpression();
            while (true){
                Optional<Expression> exp = parseExpression(tryCursor);
                if(exp.isPresent()){
                    list.add(exp.get());
                }else{
                    break;
                }
            }

            skipSpace(tryCursor);
            if(!tryCursor.hasNext()) return Optional.empty();
            if(tryCursor.top() != ')') return Optional.empty();
            tryCursor.next();
            cursor.merge(tryCursor);
            return Optional.of(list);
        }else{
            return parseSymbol(cursor);
        }
    }
}
