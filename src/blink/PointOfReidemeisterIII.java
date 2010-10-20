package blink;

/**
 * Class that identify a point of Reidemeister Type III move.
 */
public class PointOfReidemeisterIII {
    private int _baseVertexLabel;
    private GBlinkEdgeType _type;

    public PointOfReidemeisterIII(int baseVertexLabel, GBlinkEdgeType type) {
        _baseVertexLabel = baseVertexLabel;
        _type = type;
    }

    public PointOfReidemeisterIII(int baseVertexLabel, boolean vertexTrueFaceFalse) {
        _baseVertexLabel = baseVertexLabel;
        _type = vertexTrueFaceFalse ? GBlinkEdgeType.vertex : GBlinkEdgeType.face;
    }

    /**
     * this should always be called from the GBlink class
     */
    public void apply(GBlink G) {

        GBlinkEdgeType t1 = _type;
        GBlinkEdgeType t2 = (t1 == GBlinkEdgeType.face ? GBlinkEdgeType.vertex : GBlinkEdgeType.face);

        GBlinkVertex a = G.findVertex(_baseVertexLabel);
        GBlinkVertex b = a.getNeighbour(t1);
        GBlinkVertex c = b.getNeighbour(GBlinkEdgeType.edge);
        GBlinkVertex d = c.getNeighbour(t1);
        GBlinkVertex e = d.getNeighbour(GBlinkEdgeType.edge);
        GBlinkVertex f = e.getNeighbour(t1);

        // aa = a prime on the notebook
        GBlinkVertex aa = a.getNeighbour(t2);
        GBlinkVertex bb = b.getNeighbour(t2);
        GBlinkVertex cc = c.getNeighbour(t2);
        GBlinkVertex dd = d.getNeighbour(t2);
        GBlinkVertex ee = e.getNeighbour(t2);
        GBlinkVertex ff = f.getNeighbour(t2);

        // save info
        boolean eIsOvercross = e.overcross();

        // adjust crossings: t1
        GBlinkVertex.setNeighbours(a,aa,t1);
        GBlinkVertex.setNeighbours(b,bb,t1);
        GBlinkVertex.setNeighbours(c,cc,t1);
        GBlinkVertex.setNeighbours(d,dd,t1);
        GBlinkVertex.setNeighbours(e,ee,t1);
        GBlinkVertex.setNeighbours(f,ff,t1);

        // adjust crossings: t2
        GBlinkVertex.setNeighbours(a,f,t2);
        GBlinkVertex.setNeighbours(aa,ff,t2);
        GBlinkVertex.setNeighbours(b,c,t2);
        GBlinkVertex.setNeighbours(bb,cc,t2);
        GBlinkVertex.setNeighbours(d,e,t2);
        GBlinkVertex.setNeighbours(dd,ee,t2);

        // adjust crossings: edge or angle type
        GBlinkVertex.setNeighbours(e,f,GBlinkEdgeType.edge);
        GBlinkVertex.setNeighbours(a,b,GBlinkEdgeType.edge);
        GBlinkVertex.setNeighbours(c,d,GBlinkEdgeType.edge);

        //
        a.setUndercross(true);
        ff.setUndercross(true);
        d.setUndercross(true);
        ee.setUndercross(true);

        aa.setOvercross(true);
        f.setOvercross(true);
        dd.setOvercross(true);
        e.setOvercross(true);

        if (eIsOvercross) {
            b.setOvercross(true);
            bb.setUndercross(true);
            c.setUndercross(true);
            cc.setOvercross(true);
        }
        else {
            b.setUndercross(true);
            bb.setOvercross(true);
            c.setOvercross(true);
            cc.setUndercross(true);
        }
    }

    public String basePointDescription(GBlink G) {
        GBlinkEdgeType t1 = _type;
        GBlinkEdgeType t2 = (t1 == GBlinkEdgeType.face ? GBlinkEdgeType.vertex : GBlinkEdgeType.face);

        GBlinkVertex a = G.findVertex(_baseVertexLabel);

        return String.format(
            "%3d \"%s\"",
            a.getLabel(),
            _type.toString());
    }


    public String description(GBlink G) {
        GBlinkEdgeType t1 = _type;
        GBlinkEdgeType t2 = (t1 == GBlinkEdgeType.face ? GBlinkEdgeType.vertex : GBlinkEdgeType.face);

        GBlinkVertex a = G.findVertex(_baseVertexLabel);
        GBlinkVertex b = a.getNeighbour(t1);
        GBlinkVertex c = b.getNeighbour(GBlinkEdgeType.edge);
        GBlinkVertex d = c.getNeighbour(t1);
        GBlinkVertex e = d.getNeighbour(GBlinkEdgeType.edge);
        GBlinkVertex f = e.getNeighbour(t1);

        // aa = a prime on the notebook
        GBlinkVertex aa = a.getNeighbour(t2);
        GBlinkVertex bb = b.getNeighbour(t2);
        GBlinkVertex cc = c.getNeighbour(t2);
        GBlinkVertex dd = d.getNeighbour(t2);
        GBlinkVertex ee = e.getNeighbour(t2);
        GBlinkVertex ff = f.getNeighbour(t2);

        return String.format(
            "a=%3d aa=%3d b=%3d bb=%3d c=%3d cc=%3d d=%3d dd=%3d e=%3d ee=%3d f=%3d ff=%3d type=%-8s",
            a.getLabel(), aa.getLabel(),
            b.getLabel(), bb.getLabel(),
            c.getLabel(), cc.getLabel(),
            d.getLabel(), dd.getLabel(),
            e.getLabel(), ee.getLabel(),
            f.getLabel(), ff.getLabel(),
            _type.toString());
    }


    public static boolean test(GBlinkVertex v, GBlinkEdgeType t1) {
        if (t1 != GBlinkEdgeType.face && t1 != GBlinkEdgeType.vertex)
            throw new RuntimeException();

        GBlinkVertex a = v;
        GBlinkVertex b = a.getNeighbour(t1);
        GBlinkVertex c = b.getNeighbour(GBlinkEdgeType.edge);
        GBlinkVertex d = c.getNeighbour(t1);
        GBlinkVertex e = d.getNeighbour(GBlinkEdgeType.edge);
        GBlinkVertex f = e.getNeighbour(t1);
        GBlinkVertex g = f.getNeighbour(GBlinkEdgeType.edge);

        // is it an hexagon?
        if (a != g)
            return false;

        // type t2
        GBlinkEdgeType t2 = (t1 == GBlinkEdgeType.face ? GBlinkEdgeType.vertex : GBlinkEdgeType.face);

        // aa = a prime on the notebook
        GBlinkVertex aa = a.getNeighbour(t2);
        GBlinkVertex bb = b.getNeighbour(t2);
        GBlinkVertex cc = c.getNeighbour(t2);
        GBlinkVertex dd = d.getNeighbour(t2);
        GBlinkVertex ee = e.getNeighbour(t2);
        GBlinkVertex ff = f.getNeighbour(t2);

        int min1 = (int) Math.min(a.getLabel(),bb.getLabel());
        int min2 = (int) Math.min(c.getLabel(),dd.getLabel());
        int min3 = (int) Math.min(e.getLabel(),ff.getLabel());
        if (min1 == min2 || min1 == min3 || min2==min3)
            return false;

        // continue test: all odd vertices are different?
        //if (a==dd || a==ff || bb==c || bb==dd || bb==e || bb==f || c==ff || dd==e || dd==ff)
        //    return false;

        // not a normalized base point
        if (!(aa.overcross() && b.overcross() && c.overcross() && dd.overcross()))
            return false;

        //
        return true;
    }
}
