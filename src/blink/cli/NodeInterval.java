package blink.cli;

import java.util.ArrayList;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class NodeInterval extends Node {
    int _from;
    int _to;
    public NodeInterval(int from, int to) {
        _from = from;
        _to = to;
    }
    public int getFrom() {
        return _from;
    }
    public int getTo() {
        return _to;
    }
    public Object evaluate() throws EvaluationException {
        ArrayList<Integer> result = new ArrayList<Integer>();
        int inc = (_from < _to ? 1 : -1);
        for (int i=_from;i!=_to+inc;i+=inc) {
            result.add(i);
        }
        return result;
    }
}
