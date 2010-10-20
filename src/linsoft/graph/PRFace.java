package linsoft.graph;

import java.util.ArrayList;

public class PRFace implements Comparable {
    private int _id;
    private Object _object;
    private ArrayList<PRFaceEdge> _faceEdges = new ArrayList<PRFaceEdge>();

    public PRFace(int id, Object o) {
        _id = id;
        _object = o;
    }

    private int _originalId;
    public void setOriginalId(int originalId) {
        _originalId = originalId;
    }
    public int getOriginalId() {
        return _originalId;
    }

    private boolean _isAdjustmentDegreeFace;
    public void setIsAdjustmentDegreeFace(boolean b) {
        _isAdjustmentDegreeFace = b;
    }
    public boolean isAdjustmentDegreeFace() {
        return _isAdjustmentDegreeFace;
    }

    public int[] getIndexesOfFirstBridge() {
        for (int i=0;i<_faceEdges.size();i++) {
            PREdge e = _faceEdges.get(i).getEdge();
            if (e.isBridge()) {
                for (int j=i+1;j<_faceEdges.size();j++) {
                    PREdge ee = _faceEdges.get(j).getEdge();
                    if (e == ee) {
                        return new int[] {i,j};
                    }
                }
            }
        }
        throw new RuntimeException("No Bridge!");
    }

    public int getFirstIndexeOfEdge(PREdge e) {
        for (int i=0;i<_faceEdges.size();i++) {
            if (_faceEdges.get(i).getEdge() == e)
                return i;
        }
        throw new RuntimeException("No Bridge!");
    }

    public int getAnotherIndexeOfBridge(int indexOfBridgeEdge) {
        PRFaceEdge fe = _faceEdges.get(indexOfBridgeEdge);
        if (!fe.getEdge().isBridge())
            throw new RuntimeException();
        for (int i=0;i<_faceEdges.size();i++) {
            if (i == indexOfBridgeEdge)
                continue;
            if (this.getEdge(i) == fe.getEdge()) {
                return i;
            }
        }
        throw new RuntimeException("No Bridge!");
    }

    public void insertNewEdge(PREdge edge, boolean positive, int index) {
        _faceEdges.add(index,new PRFaceEdge(edge,positive));
    }

    public void assureOrientationFromV1ToV2OnEdge(int indexOfEdge) {
        if (this.size() <= 2)
            return;
        PREdge e = this.getEdge(indexOfEdge);
        PREdge ee = this.getEdge((indexOfEdge+1)%this.size());
        PRVertex v = e.getACommonVertex(ee);
        if (v == e.getV1()) {
            e.swapVertices();
        }
    }

    public boolean contains(PREdge e) {
        for (int i=0;i<_faceEdges.size();i++)
            if (this.getEdge(i) == e)
                return true;
        return false;
    }

    public int indexOf(PREdge e) {
        for (int i=0;i<_faceEdges.size();i++)
            if (this.getEdge(i) == e)
                return i;
        return -1;
    }

    public int indexOf(PRFaceEdge e) {
        return _faceEdges.indexOf(e);
    }

    public int getId() {
        return _id;
    }

    public Object getObject() {
        return _object;
    }

    public void add(PREdge edge, boolean positive) {
        _faceEdges.add(new PRFaceEdge(edge,positive));
    }

    public void add(PREdge edge, boolean positive, int transition) {
        PRFaceEdge e = new PRFaceEdge(edge,positive);
        e.setVertexTransition(transition);
        _faceEdges.add(e);
    }

    public ArrayList<PREdge> getEdges() {
        ArrayList<PREdge> list = new ArrayList<PREdge>();
        for (PRFaceEdge fe: _faceEdges)
            list.add(fe.getEdge());
        return list;
    }

    public ArrayList<PRFaceEdge> getFaceEdges() {
        return (ArrayList<PRFaceEdge>)_faceEdges.clone();
    }

    public PREdge getEdge(int index) {
        return _faceEdges.get(index).getEdge();
    }

    public PRFaceEdge getFaceEdge(int index) {
        return _faceEdges.get(index);
    }

    public boolean isFaceEdgePositive(int index) {
        return getFaceEdge(index).isPositive();
    }

    /**
     * Returns v1,v2,v3,v4... where v1 is the
     * head of e1, v2 is the head of e2, etc
     */
    public PRVertex[] getVertices() {
        if (_faceEdges.size() == 1) {
            PRFaceEdge fe = this.getFaceEdge(0);
            return new PRVertex[] { fe.getV1() };
        }
        else if (_faceEdges.size() == 2) {
            PRFaceEdge fe = this.getFaceEdge(0);
            return new PRVertex[] { fe.getV2(), fe.getV1() };
        }
        else { // if (_edges.size() > 2) {
            PRVertex result[] = new PRVertex[_faceEdges.size()];
            for (int i = 0; i < this.size(); i++) {
                PRFaceEdge fe = this.getFaceEdge(i);
                result[i] = fe.getV2();
            }
            return result;
        }
    }

    public int size() {
        return _faceEdges.size();
    }

    public int getTransition(int index) {
        return getFaceEdge(index).getVertexTransition();
    }

    public int[] getBends(int index) {
        return getFaceEdge(index).getBends();
    }

    public int getNumberOfBends(int index) {
        return getFaceEdge(index).getBends().length;
    }

    public void setTransition(int index, int t) {
        getFaceEdge(index).setVertexTransition(t);
    }

    public void setBends(int index, int ... bends) {
        getFaceEdge(index).setBends(bends);
    }

    public String getOrthogonalDescription(boolean includeSideInformation) {
        StringBuffer sb = new StringBuffer();
        boolean first = true;
        sb.append("f"+this.getId()+" ");
        for (int i=0;i<this.size();i++) {
            PRFaceEdge fe = this.getFaceEdge(i);
            PREdge e = fe.getEdge();
            if (!first)
                sb.append(", ");
            if (includeSideInformation) {
                sb.append(String.format("(%s%d (%d->%d), %s, %d, %d)", (fe.isNegative() ? "-" : "+"), e.getId(),
                                        e.getV1().getId(),e.getV2().getId(),
                                        linsoft.Library.intArrayToString("", fe.getBends()), fe.getVertexTransition(), fe.getSide()));
            }
            else {

                sb.append(String.format("(%s%d (%d->%d), %s, %d)", (fe.isNegative() ? "-" : "+"), e.getId(),
                                        e.getV1().getId(),e.getV2().getId(),
                                        linsoft.Library.intArrayToString("", fe.getBends()), fe.getVertexTransition()));

            }
            first = false;
        }
        return sb.toString();
    }

    public void removeEdges(int index0, int index1) {
        ArrayList<PRFaceEdge> newFaceEdges = new ArrayList<PRFaceEdge>();
        if (index0 < index1) {
            for (int i = 0; i < this.size(); i++) {
                if (i < index0 || i > index1) {
                    newFaceEdges.add(_faceEdges.get(i));
                }
            }
        } else {
            for (int i = 0; i < this.size(); i++) {
                if (i > index1 && i < index0) {
                    newFaceEdges.add(_faceEdges.get(i));
                }
            }
        }
        _faceEdges = newFaceEdges;
    }

    public void removeFaceEdges(ArrayList<PRFaceEdge> list) {
        _faceEdges.removeAll(list);
    }

    /**
     * Circular interval
     */
    public ArrayList<PRFaceEdge> getFaceEdges(int index0, int index1) {
        ArrayList<PRFaceEdge> result = new ArrayList<PRFaceEdge>();
        if (index0 < index1) {
            for (int i = 0; i < this.size(); i++) {
                if (index0 <= i && i <= index1) {
                    result.add(_faceEdges.get(i));
                }
            }
        } else {
            for (int i = index0; i < this.size(); i++) {
                result.add(_faceEdges.get(i));
            }
            for (int i = 0; i <= index1; i++) {
                result.add(_faceEdges.get(i));
            }
        }
        return result;
    }

    public int compareTo(Object o) {
        return this.getId() - ((PRFace) o).getId();
    }

    public ArrayList<PRTransition> getTransitions(PRVertex v) {
        int n = _faceEdges.size();
        ArrayList<PRTransition> result = new ArrayList<PRTransition>();
        for (int i = 0; i < n; i++) {
            PRFaceEdge ei = _faceEdges.get(i);
            PRFaceEdge next = _faceEdges.get((i + 1) % n);
            if (ei.getV2() == v) {
                result.add(new PRTransition(this,ei,next));
            }
        }
        return result;
    }

    public String toString() {
        return "f"+this.getId();
    }

}
