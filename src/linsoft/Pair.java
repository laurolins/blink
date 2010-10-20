package linsoft;

public class Pair  {
    private java.lang.Object obj1 ;
    private java.lang.Object obj2 ;

    public Pair( java.lang.Object obj1, java.lang.Object obj2 )
    {
        this.obj1 = obj1 ;
        this.obj2 = obj2 ;
    }

    public Object getFirst() {
        return obj1;
    }

    public Object getSecond() {
        return obj2;
    }
    public boolean equals( java.lang.Object obj )
    {
        if (!(obj instanceof Pair))
            return false ;

        Pair other = (Pair)obj ;
        return other.obj1 == obj1 && other.obj2 == obj2 ;
    }

    public int hashCode()
    {
        return System.identityHashCode( obj1 ) ^
            System.identityHashCode( obj2 ) ;
    }
}

