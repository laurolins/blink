package blink;

import java.util.HashMap;
import java.util.HashSet;

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
public class RTetrahedron {
    private int _label;
    private RFace _a;
    private RFace _b;
    private RFace _c;
    private RFace _d;
    private boolean _invertNormalA;
    private boolean _invertNormalB;
    private boolean _invertNormalC;
    private boolean _invertNormalD;

    public RTetrahedron(int label) {
        _label = label;
    }

    public void replace(RArc arc, HashMap<RSegment,Object> map) {
        RPoint x = arc.getFirstPoint();
        RPoint y = arc.getLastPoint();
        if (_a.containsPointsAsArcEnds(x,y)) {
            if (Log.MAX_LEVEL >= 4)
                Log.log(4, "Adjust face A on tetrahedra "+this.getLabel());
            _a.replace(arc, map);
        }
        if (_b.containsPointsAsArcEnds(x,y)) {
            if (Log.MAX_LEVEL >= 4)
                Log.log(4, "Adjust face B on tetrahedra "+this.getLabel());
            _b.replace(arc,map);
        }
        if (_c.containsPointsAsArcEnds(x,y)) {
            if (Log.MAX_LEVEL >= 4)
                Log.log(4, "Adjust face C on tetrahedra "+this.getLabel());
            _c.replace(arc, map);
        }
        if (_d.containsPointsAsArcEnds(x,y)) {
            if (Log.MAX_LEVEL >= 4)
                Log.log(4, "Adjust face D on tetrahedra "+this.getLabel());
            _d.replace(arc, map);
        }
    }

    public void setFace(GemColor c, RFace f, boolean invertNormal) {
        if (c == GemColor.yellow) {
            _a = f;
            _invertNormalA = invertNormal;
        } else if (c == GemColor.blue) {
            _b = f;
            _invertNormalB = invertNormal;
        }
        else if (c == GemColor.red) {
            _c = f;
            _invertNormalC = invertNormal;
        } else if (c == GemColor.green) {
            _d = f;
            _invertNormalD = invertNormal;
        }
        else throw new RuntimeException();
    }

    public RTetrahedron(int label,
                        RFace a,
                        boolean invertNormalA,
                        RFace b,
                        boolean invertNormalB,
                        RFace c,
                        boolean invertNormalC,
                        RFace d,
                        boolean invertNormalD) {
        _label = label;
        _a = a;
        _b = b;
        _c = c;
        _d = d;
        _invertNormalA = invertNormalA;
        _invertNormalB = invertNormalB;
        _invertNormalC = invertNormalC;
        _invertNormalD = invertNormalD;
    }

    public boolean invertNormal(GemColor c) {
        if (c == GemColor.yellow) return _invertNormalA;
        else if (c == GemColor.blue) return _invertNormalB;
        else if (c == GemColor.red) return _invertNormalC;
        else if (c == GemColor.green) return _invertNormalD;
        else throw new RuntimeException();
    }

    public void setInvertNormal(GemColor c, boolean i) {
        if (c == GemColor.yellow) _invertNormalA = i;
        else if (c == GemColor.blue) _invertNormalB = i;
        else if (c == GemColor.red) _invertNormalC = i;
        else if (c == GemColor.green) _invertNormalD = i;
        else throw new RuntimeException();
    }

    public RFace getFace(GemColor c) {
        if (c == GemColor.yellow) return _a;
        else if (c == GemColor.blue) return _b;
        else if (c == GemColor.red) return _c;
        else if (c == GemColor.green) return _d;
        else throw new RuntimeException();
    }

    public int getLabel() { return _label; }
    public RFace   getA() { return _a; }
    public RFace   getB() { return _b; }
    public RFace   getC() { return _c; }
    public RFace   getD() { return _d; }

    public HashSet<RSegment> getSegments() {
        HashSet<RSegment> result = new HashSet<RSegment>();
        for (RFace f: new RFace[] {_a,_b,_c,_d}) {
            for (RTriangle t : f.getTriangles()) {
                result.add(t.getS1());
                result.add(t.getS2());
                result.add(t.getS3());
            }
        }
        return result;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Tetrahedron: "+getLabel()+"\n");
        sb.append("Yellow: "+getA()+"\n");
        sb.append("  Blue: "+getB()+"\n");
        sb.append("   Red: "+getC()+"\n");
        sb.append(" Green: "+getD()+"\n");
        return sb.toString();
    }
}
