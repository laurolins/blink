//
//
//  Generated by StarUML(tm) Java Add-In
//
//  @ Project : Untitled
//  @ File Name : Function.java
//  @ Date : 10/30/2006
//  @ Author :
//
//
package blink.cli;

import java.util.ArrayList;

public class NodeFunction extends Node {

    private String _identifier;

    private NodeIndex _index;

    private ArrayList<Node> _childs;

    public NodeFunction(String identifier, NodeIndex index) {
        _identifier = identifier;
        _index = index;
        _childs = new ArrayList<Node>();
    }

    public String getIdentifier() {
        return _identifier;
    }

    public NodeIndex getIndex() {
        return _index;
    }

    public Object evaluate() throws EvaluationException {
        return CommandLineInterface.getInstance().evaluateNodeFunction(this);
    }

    public void addChild(Node node) {
        _childs.add(node);
    }

    public int getNumberOfChilds() {
        return _childs.size();
    }

    public Node getChild(int index) {
        return _childs.get(index);
    }

    public ArrayList<Node> getChilds() {
        return _childs;
    }


}