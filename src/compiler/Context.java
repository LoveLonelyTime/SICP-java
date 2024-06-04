package compiler;

public class Context {
    public static final String LINKAGE_RETURN = "return";
    public static final String LINKAGE_NEXT = "next";
    private Reg target;
    private String linkage;

    public Context(Reg target, String linkage) {
        this.target = target;
        this.linkage = linkage;
    }

    public Reg getTarget() {
        return target;
    }

    public void setTarget(Reg target) {
        this.target = target;
    }

    public String getLinkage() {
        return linkage;
    }

    public void setLinkage(String linkage) {
        this.linkage = linkage;
    }

    @Override
    public String toString() {
        return "Context{" +
                "target=" + target +
                ", linkage='" + linkage + '\'' +
                '}';
    }
}
