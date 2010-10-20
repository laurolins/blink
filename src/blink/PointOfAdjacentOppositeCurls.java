package blink;

/**
 * Class that identify a point of Reidemeister Type III move.
 */
public class PointOfAdjacentOppositeCurls {
    private int _baseVertexLabel;
    private GBlinkEdgeType _type;

    public PointOfAdjacentOppositeCurls(int baseVertexLabel, GBlinkEdgeType type) {
        _baseVertexLabel = baseVertexLabel;
        _type = type;
    }

    public PointOfAdjacentOppositeCurls(int baseVertexLabel, boolean vertexTrueFaceFalse) {
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

        GBlinkEdgeType t2 = (t1 == GBlinkEdgeType.face ? GBlinkEdgeType.vertex : GBlinkEdgeType.face);

        GBlinkVertex a = v;
        GBlinkVertex b = a.getNeighbour(t1);
        GBlinkVertex c = b.getNeighbour(t2);

        // is it an hexagon?
        if (c.getNeighbour(GBlinkEdgeType.edge) != b)
            return false;

        GBlinkVertex d = c.getNeighbour(t1);
        GBlinkVertex e = d.getNeighbour(GBlinkEdgeType.edge);
        GBlinkVertex f = e.getNeighbour(t1);
        GBlinkVertex g = f.getNeighbour(t2);
        GBlinkVertex h = g.getNeighbour(t1);

        // case 1
        if (e.getNeighbour(t2) == h &&
            f.getNeighbour(GBlinkEdgeType.edge) == g &&
            d.overcross() == e.overcross()) {
            return true;
        } else if (e.getNeighbour(t2) == f &&
                   h.getNeighbour(GBlinkEdgeType.edge) == g &&
                   d.overcross() != e.overcross()) {
            return true;
        }

        //
        return false;
    }

    public String basePointDescription(GBlink G) {
        GBlinkEdgeType t1 = _type;

        GBlinkVertex a = G.findVertex(_baseVertexLabel);

        return String.format(
                "%3d \"%s\"",
                a.getLabel(),
                _type.toString());
    }
}
