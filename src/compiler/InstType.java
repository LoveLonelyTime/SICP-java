package compiler;

import static compiler.RegProperty.*;

public enum InstType {
    ASSIGN(WRITE, "assign"),
    PERFORM(NOTHING, "perform"),
    TEST(NOTHING, "test"),
    BRANCH(NOTHING, "branch"),
    GOTO(NOTHING, "goto"),
    SAVE(READ, "save"),
    RESTORE(WRITE, "restore");

    public final RegProperty regProperty;
    public final String name;
    InstType(RegProperty regProperty, String name){
        this.regProperty = regProperty;
        this.name = name;
    }
}
