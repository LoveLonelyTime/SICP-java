package compiler;

import java.util.Collections;
import java.util.List;

public interface Parameter {
    List<Reg> getNeeds();
    String toInst();

    static Parameter reg(Reg reg){
        return new Parameter() {
            @Override
            public List<Reg> getNeeds() {
                return List.of(reg);
            }

            @Override
            public String toInst() {
                return "(reg " + reg.name + ")";
            }
        };
    }

    static Parameter label(String label){
        return new Parameter() {
            @Override
            public List<Reg> getNeeds() {
                return Collections.emptyList();
            }

            @Override
            public String toInst() {
                return "(label " + label + ")";
            }
        };
    }

    static Parameter op(String op){
        return new Parameter() {
            @Override
            public List<Reg> getNeeds() {
                return Collections.emptyList();
            }

            @Override
            public String toInst() {
                return "(op " + op + ")";
            }
        };
    }

    static Parameter conzt(String value){
        return new Parameter() {
            @Override
            public List<Reg> getNeeds() {
                return Collections.emptyList();
            }

            @Override
            public String toInst() {
                return "(const " + value + ")";
            }
        };
    }
}
