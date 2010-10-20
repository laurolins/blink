package blink;

public class MapPackedWord implements Comparable {
    private int[] _packedWord;
    private boolean _containsSimpleLoop;
    public MapPackedWord(int packedWord[], boolean simpleLoops) {
        _packedWord = packedWord;
        _containsSimpleLoop=simpleLoops;
    }
    public void setContainsSimpleLoop(boolean f) {
        _containsSimpleLoop = f;
    }
    public boolean containsSimpleLoop() {
        return _containsSimpleLoop;
    }

    public int size() {
        return _packedWord.length;
    }
    public int compareTo(Object x) {
       MapPackedWord mpw = (MapPackedWord) x;
       if (this.size() < mpw.size())
           return -1;
       else if (this.size() > mpw.size())
           return 1;
       else {
           for (int i=0;i<_packedWord.length;i++) {
               if (_packedWord[i] < mpw._packedWord[i])
                   return -1;
               else if (_packedWord[i] > mpw._packedWord[i])
                   return 1;
           }

           // they are the same!
           return 0;
       }
    }

    public int[] getPackedWord() {
        return _packedWord;
    }

    public int hashCode() {
        int c = 0;
        for (int i=0;i<this.size()-1;i+=2) {
            c+=_packedWord[i]-_packedWord[i+1];
        }
        return c * this.size();
    }

    public boolean equals(Object o) {
        return this.compareTo(o) == 0;
    }

    public String toString() {
        String result = "";
        boolean first = true;
        for (int i: _packedWord) {
            if (!first) {
                result += ",";
            }
            result+=""+i;
            first = false;
        }
        return result;
    }
}
