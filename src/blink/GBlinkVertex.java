package blink;

public class GBlinkVertex implements Comparable {

    /**
     * The neighbors of this vertex. The zigzag neighbour is implicit:
     *      this <-> this.faceNeighbour().vertexNeighbour()
     */
    private GBlinkVertex _edgeNeighbour;
    private GBlinkVertex _faceNeighbour;
    private GBlinkVertex _vertexNeighbour;

    /**
     * if the zigzag edge that incides tho this vertex
     * is an overcross then this flag is true else it is false.
     */
    private boolean _overcross;

    /**
     * The label of this vertex.
     */
    private int _label = -1;

    public GBlinkVertex() {}

    public boolean isGreen() {
        if ((hasOddLabel() && overcross()) || (hasEvenLabel() && undercross()))
            return true;
        else
            return false;
    }

    public boolean isRed() {
        return !isGreen();
    }

    public boolean overcross() {
        return _overcross;
    }

    public boolean undercross() {
        return !_overcross;
    }

    public void setOvercross(boolean b) {
        _overcross = b;
    }

    public void setUndercross(boolean b) {
        _overcross = !b;
    }

    /**
     * crossing edge color
     */
    public BlinkColor getCrossingEdgeColor() {
        if ((hasEvenLabel() && this.overcross()) ||
            (hasOddLabel() && this.undercross()))
            return BlinkColor.red;
        else
            return BlinkColor.green;

    }

    public GBlinkVertex getVertexAtTheSameGEdgeWithMinLabel() {
        GBlinkVertex result = this;
        if (_faceNeighbour.getLabel() > result.getLabel())
            result = _faceNeighbour;
        if (_vertexNeighbour.getLabel() > result.getLabel())
            result = _vertexNeighbour;
        if (this.getNeighbour(GBlinkEdgeType.diagonal).getLabel() > result.getLabel())
            result = this.getNeighbour(GBlinkEdgeType.diagonal);
        return result;
    }



    public static void setNeighbours(GBlinkVertex a, GBlinkVertex b, GBlinkEdgeType t) {
        a.setNeighbour(b, t);
        b.setNeighbour(a, t);
    }

    public void setNeighbours(GBlinkVertex en, GBlinkVertex fn, GBlinkVertex vn) {
        _edgeNeighbour = en;
        _faceNeighbour = fn;
        _vertexNeighbour = vn;
    }

    public void setNeighbour(GBlinkVertex v, GBlinkEdgeType type) {
        if (type == GBlinkEdgeType.edge)
            _edgeNeighbour = v;
        else if (type == GBlinkEdgeType.face)
            _faceNeighbour = v;
        else // if (type == EdgeType.vertex)
            _vertexNeighbour = v;
    }

    public void setLabel(int label) {
        _label = label;
    }

    public int getLabel() {
        return _label;
    }

    public boolean hasOddLabel() {
        return ((_label % 2) == 1);
    }

    public boolean hasEvenLabel() {
        return ((_label % 2) == 0);
    }

    public int labelParity() {
        return _label % 2;
    }

    public int getEdgeLabel() {
        return (_label+3)/4;
    }

    public GBlinkVertex getNeighbour(GBlinkEdgeType type) {
        if (type == GBlinkEdgeType.edge)
            return _edgeNeighbour;
        else if (type == GBlinkEdgeType.face)
            return _faceNeighbour;
        else if (type == GBlinkEdgeType.diagonal)
            return _faceNeighbour.getNeighbour(GBlinkEdgeType.vertex);
        else // if (type == EdgeType.vertex)
            return _vertexNeighbour;
    }

    public int compareTo(Object x) {
        GBlinkVertex v = (GBlinkVertex) x;
        return this.getLabel() - v.getLabel();
    }

    // -- flag --------------------------------
    private boolean _flag;
    public boolean getFlag() {
        return _flag;
    }
    public void setFlag(boolean flag) {
        _flag = flag;
    }
    // -- flag --------------------------------


    // -- angle label -------------------------
    private int _angleLabel = -1;
    public int getAngleLabel() {
        return _angleLabel;
    }
    public boolean hasAngleLabelDefined() {
        return _angleLabel != -1;
    }
    public void setAngleLabel(int al) {
        _angleLabel = al;
    }
    public void setAngleLabelAsUndefined() {
        _angleLabel = -1;
    }
    // -- angle label -------------------------

}
