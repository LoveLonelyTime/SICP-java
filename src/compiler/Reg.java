package compiler;

public enum Reg {
    ENV("env"),
    VAL("val"),
    PROC("proc"),
    ARGL("argl"),
    CONTINUE("continue");

    public final String name;

    Reg(String name){
        this.name = name;
    }
}
