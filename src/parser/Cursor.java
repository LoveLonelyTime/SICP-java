package parser;

public class Cursor {
    private final String content;
    private int cur;

    public Cursor(String content) {
        this(content, 0);
    }

    public Cursor(String content, int cur) {
        this.content = content;
        this.cur = cur;
    }

    public char top() {
        return content.charAt(cur);
    }

    public boolean expectChar(char c) {
        if (!eof() && top() == c) {
            next();
            return true;
        }

        return false;
    }

    public void next() {
        cur++;
    }

    public boolean eof() {
        return cur >= content.length();
    }

    @Override
    public String toString() {
        return "Cursor{" +
                "content='" + content + '\'' +
                ", cur=" + cur +
                '}';
    }
}
