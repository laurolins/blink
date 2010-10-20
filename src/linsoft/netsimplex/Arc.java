package linsoft.netsimplex;

/**
 * Arc of a network
 */
public class Arc {

    // index of the arc
    private int _index;

    // tail of the arc
    private Node _tail;

    // head of the arc
    private Node _head;

    // minimum capacity of the arc (a non-negative integer)
    private int _minFlow;

    // type of the arc: can TREE or NON_TREE
    public static final int INFINITE_CAPACITY = Integer.MAX_VALUE;

    // maximum capacity of the arc (a "non-negative integer greater or equal _mincapacity" or "infinite")
    private int _maxFlow;

    // flow of the arc
    private int _flow;

    // cost of the arc
    private int _cost;

    // type of the arc: can be TREE or NON_TREE
    private boolean _type;

    // type of the arc: can be TREE or NON_TREE
    public static final boolean ARC_TYPE_TREE = true;

    // type of the arc: can TREE or NON_TREE
    public static final boolean ARC_TYPE_NON_TREE = false;

    // next arc of the same type
    private Arc _nextArcOfTheSameType;

    // previous arc of the same type
    private Arc _previousArcOfTheSameType;

    public Arc(int index, Node tail, Node head, int cost, int minFlow, int maxFlow) {
        _tail = tail;
        _head = head;
        _cost = cost;
        _minFlow = minFlow;
        _maxFlow = maxFlow;
        _flow = _minFlow;
        _index = index;
    }

    public void reset() {
        _nextArcOfTheSameType = null;
        _previousArcOfTheSameType = null;
        _flow = _minFlow;
        _type = ARC_TYPE_NON_TREE;
    }

    public Arc(int index, Node tail, Node head, int cost, int maxFlow) {
        this(index,tail,head,cost,0,maxFlow);
    }

    ///////////////////////////////////////////////////////////////////////////
    // Properties get methods

    public int get_cost() {
        return _cost;
    }

    public int get_reducedCost() {
        return _cost + _tail.get_y() - _head.get_y();
    }

    public int get_flow() {
        return _flow;
    }
    public Node get_head() {
        return _head;
    }
    public int get_maxFlow() {
        return _maxFlow;
    }
    public int get_minFlow() {
        return _minFlow;
    }
    public Arc get_nextArcOfTheSameType() {
        return _nextArcOfTheSameType;
    }
    public Arc get_previousArcOfTheSameType() {
        return _previousArcOfTheSameType;
    }
    public Node get_tail() {
        return _tail;
    }

    public boolean get_type() {
        return _type;
    }

    public int get_index() {
        return _index;
    }

    public boolean isTreeArc() {
        return _type == ARC_TYPE_TREE;
    }

    public boolean isNonTreeArc() {
        return _type == ARC_TYPE_NON_TREE;
    }

    public boolean isFlowOnMinimum() {
        return _flow == _minFlow;
    }

    public boolean isFlowOnMaximum() {
        return _flow == _maxFlow;
    }

    public boolean isCapacityUnbounded() {
        return _maxFlow == INFINITE_CAPACITY;
    }

    public Node getShallowestNode() {
        if (_tail.get_depth() <= _head.get_depth())
            return _tail;
        else
            return _head;
    }

    public Node getDeepestNode() {
        if (_tail.get_depth() <= _head.get_depth())
            return _head;
        else
            return _tail;
    }

    /**
     * An arc is forward (with respect to it's cycle) if
     * it's tail has a depth smaller than it's head
     * if eWasNotSeenYet or the contrary if eWasAleareySeen.
     * This must be called ony on tree arcs.
     */
    public boolean isForward(Arc e, boolean eWasNotSeenYet) {
        if (this != e) {
            if (eWasNotSeenYet)
                return _tail.get_depth() < _head.get_depth();
            else
                return _head.get_depth() < _tail.get_depth();
        }
        else {
            return this.isFlowOnMinimum();
        }
    }

    /**
     * An arc is reverse if it is not forward.
     */
    public boolean isReverse(Arc e, boolean comesBefore_e) {
        return !isForward(e,comesBefore_e);
    }

    public String getLabel() {
        return _cost+","+this.get_reducedCost()+",[" + _minFlow + "," + (_maxFlow != INFINITE_CAPACITY ? ""+_maxFlow : ""+'\u221e')  + "]," + _flow;
    }

    public String getHeadTailString() {
        return _tail.get_index()+" -> "+_head.get_index();
    }

    // Properties get methods
    ///////////////////////////////////////////////////////////////////////////


    ///////////////////////////////////////////////////////////////////////////
    // updates

    public void set_head(Node head) {
        _head = head;
    }
    public void set_flow(int newFlow) {
        _flow = newFlow;
    }
    public void set_type(boolean _type) {
        this._type = _type;
    }
    public void set_cost(int _cost) {
        this._cost = _cost;
    }
    public void set_nextArcOfTheSameType(Arc _nextArcOfTheSameType) {
        this._nextArcOfTheSameType = _nextArcOfTheSameType;
    }
    public void set_previousArcOfTheSameType(Arc _previousArcOfTheSameType) {
        this._previousArcOfTheSameType = _previousArcOfTheSameType;
    }

    public void set_maxFlow(int maxFlow) {
        this._maxFlow = maxFlow;
    }

    public void set_index(int _index) {
        this._index = _index;
    }

    public void set_tail(Node _tail) {
        this._tail = _tail;
    }

    // updates
    ///////////////////////////////////////////////////////////////////////////

    ///////////////////////////////////////////////////////////////////////////
    // Non-recursive Quicksort of arcs

    static final int MAXSTACKSIZE = 10000;
    static final int THRESHOLD = 0;
    static int[] Stack = new int[MAXSTACKSIZE]; // Stack for array bounds
    public static void qsort(Arc[] array, int oi, int oj) {
        int listsize = oj - oi + 1;
        int top = -1;
        Arc pivot;
        int pivotindex, l, r;

        Stack[++top] = oi; // Initialize stack
        Stack[++top] = oj;

        while (top > 0) { // While there are unprocessed subarrays
            // Pop Stack
            int j = Stack[top--];
            int i = Stack[top--];

            // Findpivot
            pivotindex = (i + j) / 2;
            pivot = array[pivotindex];
            Arc.swap(array, pivotindex, j); // Stick pivot at end

            // Partition
            l = i - 1;
            r = j;
            do {
                while (Arc.lessThan(array[++l],pivot));
                while ( (r != 0) && (Arc.greaterThan(array[--r],pivot)));
                Arc.swap(array, l, r);
            }
            while (l < r);
            Arc.swap(array, l, r); // Undo final swap
            Arc.swap(array, l, j); // Put pivot value in place

            // Put new subarrays onto Stack if they are small
            if ( (l - i) > THRESHOLD) { // Left partition
                Stack[++top] = i;
                Stack[++top] = l - 1;
            }
            if ( (j - l) > THRESHOLD) { // Right partition
                Stack[++top] = l + 1;
                Stack[++top] = j;
            }
        }
        inssort(array,oi,oj); // Final Insertion Sort
    }

    public static void inssort(Arc[] array, int a, int b) {  // Insertion Sort
      for (int i=a+1; i<=b; i++) // Insert i'th record
        for (int j=i; (j>0) && (Arc.lessThan(array[j],array[j-1])); j--)
          Arc.swap(array, j, j-1);
    }

    public static boolean lessThan(Arc a, Arc b) {
        if (a.get_tail().get_index() < b.get_tail().get_index() ||
            (a.get_tail().get_index() == b.get_tail().get_index() && a.get_head().get_index() < b.get_head().get_index()))
            return true;
        else
            return false;
    }
    public static boolean greaterThan(Arc a, Arc b) {
        if (a.get_tail().get_index() > b.get_tail().get_index() ||
            (a.get_tail().get_index() == b.get_tail().get_index() && a.get_head().get_index() > b.get_head().get_index()))
            return true;
        else
            return false;
    }
    public static void swap(Arc[] array, int i, int j) {
        Arc aux = array[i];
        array[i] = array[j];
        array[j] = aux;
    }


    public static boolean check(Arc[] array, int a, int b) {
        for (int i=a;i<b-1;i++) {
            if (Arc.greaterThan(array[i],array[i+1]))
                return false;
        }
        return true;
    }
    // Non-recursive Quicksort of arcs
    ///////////////////////////////////////////////////////////////////////////


}
