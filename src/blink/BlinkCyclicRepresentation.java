package blink;

import java.io.PrintStream;
import java.util.HashMap;

public class BlinkCyclicRepresentation {
    int[][] _cyclicRepresentation;
    int[] _reds;
    public BlinkCyclicRepresentation(int[][] bcr, int reds[]) {
        _cyclicRepresentation = bcr;
        _reds = reds;
    }
    public String toString() {
        StringBuffer s = new StringBuffer();
        for (int i = 0; i < _cyclicRepresentation.length; i++) {
            for (int j = 0; j < _cyclicRepresentation[i].length; j++) {
                s.append(_cyclicRepresentation[i][j]);
                s.append(' ');
            }
            s.append('\n');
        }
        if (_reds.length > 0) {
            s.append("R ");
            for (int i = 0; i < _reds.length; i++) {
                s.append(_reds[i]);
                s.append(' ');
            }
            s.append('\n');
        }
        return s.toString();
    }

    public void pigale(PrintStream ps) {
        HashMap<Integer,Integer[]> map = new HashMap<Integer,Integer[]>();
        for (int i=0;i<_cyclicRepresentation.length;i++) {
            for (int j=0;j<_cyclicRepresentation[i].length;j++) {
                int e = _cyclicRepresentation[i][j];
                Integer[] o = map.get(e);
                if (o == null) {
                    o = new Integer[] {i+1,-1};
                    map.put(e,o);
                }
                else {
                    o[1] = i+1;
                }
            }
        }

        ps.println("PIG:0 XXX");
        for (Integer i: map.keySet()) {
            Integer[] o = map.get(i);
            ps.println(o[0]+" "+o[1]);
        }
        ps.println("0 0");
        ps.flush();
    }
}
