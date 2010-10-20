package blink.cli;

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
public class NodeObject extends Node {

    private Object _object;

    public NodeObject(Object object) {
        _object = object;
    }

    public Object getObject() {
        return _object;
    }

    public Object evaluate() throws EvaluationException {
        return _object;
    }

}
