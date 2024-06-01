package compiler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class InstSequenceBuilder {
    private final InstSequence instSequence = new InstSequence(new HashSet<>(), new HashSet<>(), new ArrayList<>());

    public InstSequenceBuilder addNeeds(String... needs){
        instSequence.getNeeds().addAll(List.of(needs));
        return this;
    }

    public InstSequenceBuilder addModifies(String... needs){
        instSequence.getModifies().addAll(List.of(needs));
        return this;
    }

    public InstSequenceBuilder addInst(String inst){
        instSequence.getStatements().add(inst);
        return this;
    }

    // append-instruction-sequences
    public InstSequenceBuilder merge(InstSequence o){
        instSequence.getModifies().addAll(o.getModifies());
        for(String reg : o.getNeeds()){
            if(!instSequence.getModifies().contains(reg)){
                instSequence.getNeeds().add(reg);
            }
        }
        instSequence.getStatements().addAll(o.getStatements());
        return this;
    }

    public InstSequence build(){
        return instSequence;
    }
}
