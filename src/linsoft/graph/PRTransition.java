package linsoft.graph;

/**
 * Two subsequent face edges on the same face
 * that meet a vertex.
 */
public class PRTransition {
    private PRFace _face;
    private PRFaceEdge _faceEdge1;
    private PRFaceEdge _faceEdge2;
    public PRTransition(PRFace f, PRFaceEdge faceEdge1, PRFaceEdge faceEdge2) {
        _face = f;
        _faceEdge1 = faceEdge1;
        _faceEdge2 = faceEdge2;
    }
    public PRFaceEdge getFaceEdge1() {
        return _faceEdge1;
    }
    public PRFaceEdge getFaceEdge2() {
        return _faceEdge2;
    }
    public PRFace getFace() {
        return _face;
    }
    public boolean fitsBefore(PRTransition t) {
        PRFaceEdge a = t.getFaceEdge2();
        PRFaceEdge b = this.getFaceEdge1();
        if (a.getEdge() == b.getEdge() && a.isPositive() != b.isPositive()) {
            return true;
        }
        else return false;
    }
    public boolean fitsAfter(PRTransition t) {
        PRFaceEdge a = this.getFaceEdge2();
        PRFaceEdge b = t.getFaceEdge1();
        if (a.getEdge() == b.getEdge() && a.isPositive() != b.isPositive()) {
            return true;
        }
        else return false;
    }

    public String toString() {
        return String.format("(f%d, %s%d, %s%d)",
                             _face.getId(),
                             _faceEdge1.isPositive()?"+":"-",
                             _faceEdge1.getEdge().getId(),
                             _faceEdge2.isPositive()?"+":"-",
                             _faceEdge2.getEdge().getId());
    }

}
