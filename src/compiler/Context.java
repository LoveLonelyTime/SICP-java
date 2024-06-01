package compiler;

public class Context {
    public static final String LINKAGE_RETURN = "return";
    public static final String LINKAGE_NEXT = "next";
    private String target;
    private String linkage;

    public Context(String target, String linkage) {
        this.target = target;
        this.linkage = linkage;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
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
                "target='" + target + '\'' +
                ", linkage='" + linkage + '\'' +
                '}';
    }
}
