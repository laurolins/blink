package blink;

/**
 * Class that identify a point of Reidemeister Type III move.
 */
public class PointOfReidemeisterII {
    private int _baseVertexLabel;
    private GBlinkEdgeType _type;

    public PointOfReidemeisterII(int baseVertexLabel, GBlinkEdgeType type) {
        _baseVertexLabel = baseVertexLabel;
        _type = type;
    }

    public PointOfReidemeisterII(int baseVertexLabel, boolean vertexTrueFaceFalse) {
        _baseVertexLabel = baseVertexLabel;
        _type = vertexTrueFaceFalse ? GBlinkEdgeType.vertex : GBlinkEdgeType.face;
    }

    public int getBaseVertexLabel() {
        return _baseVertexLabel;
    }

    public GBlinkEdgeType getType() {
        return _type;
    }

    public static boolean test(GBlinkVertex v, GBlinkEdgeType t1) {
        if (t1 != GBlinkEdgeType.face && t1 != GBlinkEdgeType.vertex)
            throw new RuntimeException();

        GBlinkVertex a = v;
        GBlinkVertex b = a.getNeighbour(t1);
        GBlinkVertex c = b.getNeighbour(GBlinkEdgeType.edge);
        GBlinkVertex d = c.getNeighbour(t1);
        GBlinkVertex e = d.getNeighbour(GBlinkEdgeType.edge);

        // is it an hexagon?
        if (a != e || !(a!=c && b!=d) )
            return false;

        // type t2
        GBlinkEdgeType t2 = (t1 == GBlinkEdgeType.face ? GBlinkEdgeType.vertex : GBlinkEdgeType.face);

        // aa = a prime on the notebook
        GBlinkVertex aa = a.getNeighbour(t2);
        GBlinkVertex bb = b.getNeighbour(t2);
        GBlinkVertex cc = c.getNeighbour(t2);
        GBlinkVertex dd = d.getNeighbour(t2);

        // not a normalized base point
        if (!(aa.overcross() && b.overcross() && c.overcross() && dd.overcross()))
            return false;

        //
        return true;
    }

    public String basePointDescription(GBlink G) {
        GBlinkEdgeType t1 = _type;

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


}
