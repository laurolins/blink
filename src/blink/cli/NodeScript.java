package blink.cli;

import java.util.ArrayList;

public class NodeScript extends Node {

    private ArrayList<Node> _childs;

    public NodeScript() {
        _childs = new ArrayList<Node>();
    }

    public void addChild(Node node) {
        _childs.add(node);
    }

    public int getNumberOfChilds() {
        return _childs.size();
    }

    public ArrayList<Node> getChilds() {
        return _childs;
    }

    public Node getChild(int index) {
        return _childs.get(index);
    }

    public Object evaluate() throws EvaluationException {
        return CommandLineInterface.getInstance().evaluateNodeScript(this);
    }

}
