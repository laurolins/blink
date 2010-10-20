package linsoft;


public class UnorderedPair  {

    /**
     *  The first element.
     *
     *  @serial
     */
    private Object first;

    /**
     *  The second element.
     *
     *  @serial
     */
    private Object second;


    public UnorderedPair() {
        this(null, null);
    }

    public UnorderedPair(Object first, Object second) {
        super();
        this.first = first;
        this.second = second;
    }


    ////////////////////////////////////////
    // Convenience accessors
    ////////////////////////////////////////


    public Object getFirst() {
        return first;
    }


    public void setFirst(Object first) {
        this.first = first;
    }


    public Object getSecond() {
        return second;
    }


    public void setSecond(Object second) {
        this.second = second;
    }


    ////////////////////////////////////////
    // Collection
    ////////////////////////////////////////


    public int size() {
        return 2;
    }


    public boolean isEmpty() {
        return false;
    }


    public boolean contains(Object object) {
        if (object == null) {
            return first == null || second == null;
        }
        return object.equals(first) || object.equals(second);
    }


    public Object[] toArray() {
        return new Object[] {first, second};
    }


    /**
     *  To conform to the contracts for <code>Set</code>,
     *  <code>List</code>, and other potential types of
     *  <code>Collections</code>, instances of this class can only be
     *  <code>.equals()</code> to other instances of this class.
     */
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof UnorderedPair)) {
            return false;
        }
        UnorderedPair pair = (UnorderedPair) object;
        return (equals(first, pair.first) && equals(second, pair.second))
                || (equals(first, pair.second) && equals(second, pair.first));
    }


    public int hashCode() {
        return (first == null ? 0 : first.hashCode())
                ^ (second == null ? 0 : second.hashCode());
    }


    private static final boolean equals(Object a, Object b) {
        return (a == null) ? (b == null) : a.equals(b);
    }

}
