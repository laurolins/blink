package blink;

public class RhoPair {
    GemVertex _u;
    GemVertex _v;
    GemColor _color;
    int _foundAsA;
    public RhoPair(GemVertex u, GemVertex v, GemColor c, int foundAsA) {
        _u = u;
        _v = v;
        _color = c;
        _foundAsA = foundAsA;
    }
    public GemVertex getU() {
        return _u;
    }
    public GemVertex getV() {
        return _v;
    }
    public GemColor getColor() {
        return _color;
    }
    public int foundAsA() {
        return _foundAsA;
    }
    public String toString() {
        return String.format("Rho %d Pair: %6d %6d %10s",_foundAsA,_u.getLabel(),_v.getLabel(),_color);
    }
}
