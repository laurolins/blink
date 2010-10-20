package linsoft.graph;

import java.util.ArrayList;

public class PREdge implements Comparable {
    private ArrayList<PRFace> _faces = new ArrayList<PRFace>();
    private int _id;
    private Object _object;
    private PRVertex _v1;
    private PRVertex _v2;
    private int _length;
    private int _originalId;

    public PREdge(int id, Object o, PRVertex v1, PRVertex v2) {
        _id = id;
        _originalId = id;
        _object = o;
        _v1 = v1;
        _v2 = v2;
    }

    public void setLength(int length) {
        _length = length;
    }

    public int getLength() {
        return _length;
    }

    public void setOriginalId(int originalId) {
        _originalId = originalId;
    }
    public int getOriginalId() {
        return _originalId;
    }


    public boolean _degreeAdjustmentEdge;
    public void setDegreeAdjustementEdge(boolean b) {
        _degreeAdjustmentEdge = b;
    }
    public boolean isDegreeAdjustementEdge() {
        return _degreeAdjustmentEdge;
    }

    public PRVertex getACommonVertex(PREdge e) {
        if (this.getV1() == e.getV1() || this.getV1() == e.getV2())
            return this.getV1();
        else if (this.getV2() == e.getV1() || this.getV1() == e.getV2())
            return this.getV2();
        else return null;
    }

    public void removeFace(PRFace f) {
        _faces.remove(f);
    }

    public void addFace(PRFace f) {
        if (!f.contains(this))
            throw new RuntimeException();
        _faces.add(f);
    }

    public void makeFirstVertex(PRVertex v) {
        if (_v2 == v) {
            this.swapVertices();
        }
        else if (_v1 != v) {
            throw new RuntimeException("OOoooppssss");
        }
    }

    public void makeSecondVertex(PRVertex v) {
        if (_v1 == v) {
            this.swapVertices();
        }
        else if (_v2 != v) {
            throw new RuntimeException("OOoooppssss");
        }
    }

    public void swapVertices() {
        PRVertex aux = _v1;
        _v1 = _v2;
        _v2 = aux;
    }

    public PRFace getOtherFace(PRFace f) {
        if (this.isBridge())
            throw new RuntimeException();
        if (_faces.get(0) == f)
            return _faces.get(1);
        else if (_faces.get(1) == f)
            return _faces.get(0);
        else throw new RuntimeException();
    }

    public int getId() {
        return _id;
    }

    public boolean isBridge() {
        return _faces.get(0).equals(_faces.get(1));
    }

    public Object getObject() {
        return _object;
    }

    public PRVertex getV1() {
        return _v1;
    }

    public PRVertex getV2() {
        return _v2;
    }

    public void replaceV1(PRVertex v) {
        _v1 = v;
    }

    public void replaceV2(PRVertex v) {
        _v2 = v;
    }

    public PRVertex getOpposite(PRVertex v) {
        PRVertex result = (v == _v2 ? _v1 : (v == _v1 ? _v2 : null));
        if (result == null)
            throw new RuntimeException();
        return result;
    }

    public ArrayList<PRFace> getFaces() {
        return _faces;
    }

    public PRFace getFace(int index) {
        return _faces.get(index);
    }

    public int compareTo(Object o) {
        return this.getId() - ((PREdge) o).getId();
    }
}
