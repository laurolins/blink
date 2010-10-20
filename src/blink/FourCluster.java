package blink;

public class FourCluster {
    GemVertex _u;
    public FourCluster(GemVertex u) {
        _u = u;
    }
    public GemVertex getU() {
        return _u;
    }
    public String toString() {
        return String.format("4-cluster on %3d",_u.getLabel());
    }
}
