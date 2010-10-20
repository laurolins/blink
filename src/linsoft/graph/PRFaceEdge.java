package linsoft.graph;

public class PRFaceEdge {
    private PREdge _edge;
    private boolean _positive;
    private int _vertexTransition;
    private int[] _bends ;
    private int _side;
    public PRFaceEdge(PREdge e, boolean signPositive) {
        _edge = e;
        _positive = signPositive;
        _vertexTransition = 90;
        _bends = new int[0];
    }
    public void setSide(int side) {
        _side = side;
    }
    public int getSide() {
        return _side;
    }
    public void setVertexTransition(int t) {
        if (t == 0)
            throw new RuntimeException();
        _vertexTransition = t;
    }
    public void setBends(int ... bends) {
        _bends = bends;
    }
    public int getVertexTransition() {
        return _vertexTransition;
    }
    public int[] getBends() {
        return _bends;
    }
    public PREdge getEdge() {
        return _edge;
    }
    public boolean isPositive() {
        return _positive;
    }
    public boolean isNegative() {
        return !_positive;
    }
    public PRVertex getV1() {
        if (this.isPositive())
            return _edge.getV1();
        else
            return _edge.getV2();
    }
    public PRVertex getV2() {
        if (this.isPositive())
            return _edge.getV2();
        else
            return _edge.getV1();
    }
    public void replaceV1(PRVertex v) {
        if (this.isPositive())
            _edge.replaceV1(v);
        else
            _edge.replaceV2(v);
    }
    public void replaceV2(PRVertex v) {
        if (this.isPositive())
            _edge.replaceV2(v);
        else
            _edge.replaceV1(v);
    }

}
