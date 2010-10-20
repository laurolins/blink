package blink;

/**
 * Class that identify a point of Reidemeister Type III move.
 */
public class PointOfAlpha1Move {
    private int _baseVertexLabel;
    private GBlinkEdgeType _type;
    private int _caseOfMove;

    public PointOfAlpha1Move(int baseVertexLabel, GBlinkEdgeType type, int caseOfMove) {
        _baseVertexLabel = baseVertexLabel;
        _type = type;
        _caseOfMove = caseOfMove;
    }

    public PointOfAlpha1Move(int baseVertexLabel, boolean vertexTrueFaceFalse, int caseOfMove) {
        _baseVertexLabel = baseVertexLabel;
        _type = vertexTrueFaceFalse ? GBlinkEdgeType.vertex : GBlinkEdgeType.face;
        _caseOfMove = caseOfMove;
    }

    public int getCaseOfMove() {
        return _caseOfMove;
    }

    public int getBaseVertexLabel() {
        return _baseVertexLabel;
    }

    public GBlinkEdgeType getType() {
        return _type;
    }

    public static int test(GBlinkVertex v, GBlinkEdgeType t1) {
        if (t1 != GBlinkEdgeType.face && t1 != GBlinkEdgeType.vertex)
            throw new RuntimeException();

        GBlinkEdgeType t2 = (t1 == GBlinkEdgeType.face ? GBlinkEdgeType.vertex : GBlinkEdgeType.face);

        GBlinkVertex a = v;
        if (!a.overcross())
            return 0;

        GBlinkVertex b = a.getNeighbour(t1);
        GBlinkVertex c = b.getNeighbour(t2);
        GBlinkVertex d = c.getNeighbour(t1);
        GBlinkVertex e = d.getNeighbour(GBlinkEdgeType.edge);



        while (true) { // test if it is case 1
            //boolean eIsOvercross = e.overcross();

            GBlinkVertex f = e.getNeighbour(t2);
            GBlinkVertex g = f.getNeighbour(t1);
            if (f.getNeighbour(GBlinkEdgeType.edge) != g)
                break;

            GBlinkVertex h = g.getNeighbour(t2);
            GBlinkVertex i = h.getNeighbour(GBlinkEdgeType.edge);
            if (!i.overcross())
                break;

            GBlinkVertex j = i.getNeighbour(t1);
            if (c.getNeighbour(GBlinkEdgeType.edge) != j)
                break;

            GBlinkVertex k = j.getNeighbour(t2);
            if (b.getNeighbour(GBlinkEdgeType.edge) != k)
                break;

            GBlinkVertex min1 = (a.getLabel() < c.getLabel() ? a : c);
            GBlinkVertex min2 = (e.getLabel() < g.getLabel() ? e : g);
            GBlinkVertex min3 = (i.getLabel() < k.getLabel() ? i : k);
            if (min1 == min2 || min1 == min3 || min2 == min3)
                return 0;

            // GBlinkVertex l = k.getNeighbour(t1);
            return 1;
        }

        { // test if it is case 2
            //if (!e.undercross()) return 0;

            GBlinkVertex f = e.getNeighbour(t1);
            GBlinkVertex g = f.getNeighbour(t2);
            if (f.getNeighbour(GBlinkEdgeType.edge) != g)
                return 0;

            GBlinkVertex h = g.getNeighbour(t1);
            GBlinkVertex i = h.getNeighbour(GBlinkEdgeType.edge);
            if (!i.overcross())
                return 0;

            if (i == e || i == g || i == b)
                return 0;

            GBlinkVertex j = i.getNeighbour(t1);
            if (c.getNeighbour(GBlinkEdgeType.edge) != j)
                return 0;

            GBlinkVertex k = j.getNeighbour(t2);
            if (b.getNeighbour(GBlinkEdgeType.edge) != k)
                return 0;

            GBlinkVertex min1 = (a.getLabel() < c.getLabel() ? a : c);
            GBlinkVertex min2 = (e.getLabel() < g.getLabel() ? e : g);
            GBlinkVertex min3 = (i.getLabel() < k.getLabel() ? i : k);
            if (min1 == min2 || min1 == min3 || min2 == min3)
                return 0;

            // GBlinkVertex l = k.getNeighbour(t1);
            return 2;
        }
    }

    public String basePointDescription(GBlink G) {
        GBlinkEdgeType t1 = _type;

        GBlinkVertex a = G.findVertex(_baseVertexLabel);

        return String.format(
                "%3d \"%s\"  case: %d",
                a.getLabel(),
                _type.toString(),
                this.getCaseOfMove());
    }
}
