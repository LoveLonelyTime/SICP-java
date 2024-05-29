package parser;

public class Cursor {
    private final String content;
    private int cur;

    public Cursor(String content){
        this(content,0);
    }

    public Cursor(String content, int cur) {
        this.content = content;
        this.cur = cur;
    }

    public char top(){
        return content.charAt(cur);
    }

    public char next(){
        char t = content.charAt(cur);
        cur++;
        return t;
    }

    public boolean hasNext(){
        return cur != content.length();
    }

    public Cursor branch(){
        return new Cursor(content, cur);
    }

    public void merge(Cursor c){
        this.cur = c.cur;
    }
}
