package blink;

/**
 * Four edges on colors 0,1,2,3 that are 2 by 2 on the same bigon;
 */
public class Quartet {
    private GemVertex[] _vertices;
    public Quartet(GemVertex v0, GemVertex v1, GemVertex v2, GemVertex v3) {
        _vertices = new GemVertex[] {v0,v1,v2,v3};
    }
    public GemVertex getV0() { return _vertices[0]; }
    public GemVertex getV1() { return _vertices[1]; }
    public GemVertex getV2() { return _vertices[2]; }
    public GemVertex getV3() { return _vertices[3]; }
    public GemVertex getV(int index) { return _vertices[index]; }
    public GemVertex getV(GemColor c) { return getV(c.getNumber()); }
    public String toString() {
        return String.format("Quartet (v0,v1,v2,v3): %d %d %d %d",
                             getV0().getLabel(), getV1().getLabel(),
                             getV2().getLabel(), getV3().getLabel());
    }
}

