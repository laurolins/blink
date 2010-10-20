package linsoft.netsimplex;

/**
 * Node of a network
 */
public class Node {
    // node index on the network.
    private int _index;

    // net-flow or excess of the node.
    private int _b;

    // potential (the dual variable correpondent to the nodes).
    private int _y;

    // depth of the node on the tree.
    private int _depth;

    // predecessor node on the tree.
    private Node _predecessor;

    // arc (on tree) from predecessor node to this node.
    // this will be null only on the root of the tree.
    private Arc _treeArcToThisNode;

    // sucessor on the pre-order of the tree.
    private Node _sucessor;

    // current net-flow
    private int _netFlow;

    // first outgoing arc index
    private int _firstOutgoingArcIndex;

    // last outgoing arc index
    private int _numOutgoingArcs;

    public Node(int index, int b) {
        _index = index;
        _b = b;
    }

    public void reset() {
        _y = 0;
        _depth = 0;
        _treeArcToThisNode = null;
        _predecessor = null;
        _sucessor = null;
        _netFlow = 0;
        _numOutgoingArcs = 0;
        _firstOutgoingArcIndex = -1;
    }

    public void setOutgoingsArcsIndexInterval(int first, int numOutgoingArcs) {
        _firstOutgoingArcIndex = first;
        _numOutgoingArcs = numOutgoingArcs;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Properties get methods
    public int get_b() {
        return _b;
    }
    public int get_depth() {
        return _depth;
    }
    public Node get_predecessor() {
        return _predecessor;
    }
    public Node get_sucessor() {
        return _sucessor;
    }
    public int get_y() {
        return _y;
    }
    public int get_index() {
        return _index;
    }
    public int get_netFlow() {
        return _netFlow;
    }

    public Arc get_treeArcToThisNode() {
        return _treeArcToThisNode;
    }

    public int get_firstOutgoingArcIndex() {
        return _firstOutgoingArcIndex;
    }

    public int get_numOutgoingArcs() {
        return _numOutgoingArcs;
    }

    public String getLabel() {
        return (_object != null ? "["+_object.toString()+"]," : "") + _index+",b"+_b+",s"+
                (_sucessor != null ? ""+_sucessor.get_index() : "-")+
                ",d"+_depth+",y"+_y+",p"+
                (_predecessor != null ? ""+_predecessor.get_index() : "-");
    }
    // Properties get methods
    ///////////////////////////////////////////////////////////////////////////


    ///////////////////////////////////////////////////////////////////////////
    // Properties set methods
    public void set_sucessor(Node _sucessor) {
      this._sucessor = _sucessor;
    }
    public void set_predecessor(Node _predecessor) {
      this._predecessor = _predecessor;
    }
    public void set_depth(int _depth) {
      this._depth = _depth;
    }
    public void set_y(int _y) {
      this._y = _y;
    }
    public void set_netFlow(int netFlow) {
      this._netFlow = netFlow;
    }
    public void set_b(int b) {
      this._b = b;
    }

    public void set_treeArcToThisNode(Arc _treeArcToThisNode) {
        this._treeArcToThisNode = _treeArcToThisNode;
    }

    public void addConstantToDepth(int c) {
      _depth += c;
    }
    public void addConstantToNetFlow(int c) {
      _netFlow += c;
    }
    public void addConstantToY(int c) {
      _y += c;
    }
    // Properties set methods
    ///////////////////////////////////////////////////////////////////////////

    private Object _object;
    public void setObject(Object o) {
        _object = o;
    }
    public Object getObject() {
        return _object;
    }
}
