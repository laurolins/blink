package linsoft.graph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

public class PRVertex implements Comparable {
    private ArrayList<PREdge> _edges = new ArrayList<PREdge>();
    private int _id;
    private Object _object;

    public PRVertex(int id, Object o) {
        _id = id;
        _object = o;
    }

    private boolean _extraVertex = false;
    public void setExtraVertex(boolean b) {
        _extraVertex = b;
    }
    public boolean istExtraVertex() {
        return _extraVertex;
    }

    private PRVertex _degreeAdjustedVertex = null;
    public void setDegreeAdjustedVertex(PRVertex v) {
        _degreeAdjustedVertex = v;
    }
    public boolean isDegreeAdjustedVertex() {
        return _degreeAdjustedVertex != null;
    }
    public PRVertex getDegreeAdjustedVertex() {
        return _degreeAdjustedVertex;
    }

    private boolean _isCornerDegreeAdjustmentVertex = false;
    public void setIsCornerDegreeAdjustmentVertex(boolean b) {
        _isCornerDegreeAdjustmentVertex = b;
    }
    public boolean isCornerDegreeAdjustmentVertex() {
        return _isCornerDegreeAdjustmentVertex;
    }

    public boolean isOriginalVertex() {
        return !_extraVertex;
    }

    public void addEdge(PREdge e) {
        if (e.getV1() != this && e.getV2() != this)
            throw new RuntimeException();
        _edges.add(e);
    }

    public void removeAllEdges() {
        _edges.clear();
    }

    public void removeEdge(PREdge e) {
        _edges.remove(e);
    }

    public int getId() {
        return _id;
    }

    public Object getObject() {
        return _object;
    }

    public int getDegree() {
        return _edges.size();
    }

    public ArrayList<PREdge> getEdges() {
        return (ArrayList<PREdge>) _edges.clone();
    }

    public int compareTo(Object o) {
        return this.getId() - ((PRVertex) o).getId();
    }

    private double _x;
    private double _y;

    public void setPosition(double x, double y) {
        _x = x;
        _y = y;
    }

    public double getX() {
        return _x;
    }

    public double getY() {
        return _y;
    }

    public ArrayList<PRTransition> getTransitions() {
        ArrayList<PRFace> faces = new ArrayList<PRFace>();
        for (PREdge e: _edges) {
            for (PRFace f: e.getFaces())
                if (!faces.contains(f))
                    faces.add(f);
        }
        Collections.sort(faces);

        // get transitions through vertex
        LinkedList<PRTransition> transitions = new LinkedList<PRTransition>();
        for (PRFace f: faces) {
            transitions.addAll(f.getTransitions(this));
        }

        // sort the transitions
        ArrayList<PRTransition> result = new ArrayList<PRTransition>();

        PRTransition head = transitions.poll();
        result.add(head);
        while (transitions.size() > 0) {
            for (int i=0;i<transitions.size();i++) {
                PRTransition ti = transitions.get(i);
                if (head.fitsAfter(ti)) {
                    result.add(ti);
                    head = ti;
                    transitions.remove(i);
                    break;
                }
            }
        }

        return result;
    }

    public String toString() {
        return "v"+this.getId();
    }

}
