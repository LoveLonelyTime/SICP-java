package evaluator;

public class AMBPath {
    private Environment environment;
    private Successful successful;
    private Failed failed;

    public AMBPath(Environment environment, Successful successful, Failed failed) {
        this.environment = environment;
        this.successful = successful;
        this.failed = failed;
    }

    public Environment getEnvironment() {
        return environment;
    }

    public void setEnvironment(Environment environment) {
        this.environment = environment;
    }

    public Successful getSuccessful() {
        return successful;
    }

    public void setSuccessful(Successful successful) {
        this.successful = successful;
    }

    public Failed getFailed() {
        return failed;
    }

    public void setFailed(Failed failed) {
        this.failed = failed;
    }

    @Override
    public String toString() {
        return "AMBPath{" +
                "environment=" + environment +
                ", successful=" + successful +
                ", failed=" + failed +
                '}';
    }
}
