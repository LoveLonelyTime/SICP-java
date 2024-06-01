package compiler;

import java.util.List;
import java.util.Set;

public class InstSequence {
    private Set<String> needs;
    private Set<String> modifies;
    private List<String> statements;

    public InstSequence(Set<String> needs, Set<String> modifies, List<String> statements) {
        this.needs = needs;
        this.modifies = modifies;
        this.statements = statements;
    }

    public Set<String> getNeeds() {
        return needs;
    }

    public void setNeeds(Set<String> needs) {
        this.needs = needs;
    }

    public Set<String> getModifies() {
        return modifies;
    }

    public void setModifies(Set<String> modifies) {
        this.modifies = modifies;
    }

    public List<String> getStatements() {
        return statements;
    }

    public void setStatements(List<String> statements) {
        this.statements = statements;
    }
}
