package blink;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class Twistor {
    GemVertex _u;
    GemVertex _v;
    GemColor _color;
    public Twistor(GemVertex u, GemVertex v, GemColor color) {
        _u = u;
        _v = v;
        _color = color;
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
    public boolean isAdjacent() {
        GemColor c0 = GemColor.yellow;
        GemColor c1 = _color;
        GemColor compColors[] = GemColor.getComplementColors(c0,c1);
        GemColor c2 = compColors[0];
        GemColor c3 = compColors[1];
        return
                (_u.getNeighbour(c0).getNeighbour(c1) == _v) ||
                (_u.getNeighbour(c1).getNeighbour(c0) == _v) ||
                (_u.getNeighbour(c2).getNeighbour(c3) == _v) ||
                (_u.getNeighbour(c3).getNeighbour(c2) == _v);

    }
    public String toString() {
        return String.format("%s-twistor  on %3d %3d     %s",
                             (_color == GemColor.blue ? "1" :
                              _color == GemColor.red ? "2" : "3"),
                             _u.getLabel(),
                             _v.getLabel(),
                             isAdjacent() ? "adj" : "");
    }
}
