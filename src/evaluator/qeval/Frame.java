package evaluator.qeval;

import parser.Expression;

import java.util.HashMap;

public class Frame extends HashMap<String, Expression> {
    public Frame() {
    }

    public Frame(Frame old){
        this.putAll(old);
    }
}
