package maps.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;

import maps.Graph;
import drawing.AbstractDrawing;

public class EditorEdge extends AbstractDrawing {
    
    private Graph.Edge _connection;
    
    private EditorVertex _editorVertexA;
    
    private EditorVertex _editorVertexB;
    
    public EditorEdge(Graph.Edge connection, EditorVertex editorVertexA, EditorVertex editorVertexB) {
        super(null);
        _connection = connection;
        _editorVertexA = editorVertexA;
        _editorVertexB = editorVertexB;
    }
    
    public EditorVertex getOtherEnd(EditorVertex n) {
        if (n == _editorVertexA) return _editorVertexB;
        else if (n == _editorVertexB) return _editorVertexA;
        else throw new RuntimeException();
    }
    
    public Graph.Edge getEdge() {
        return _connection;
    }
    
    public EditorVertex getEditorVertexA() {
        return _editorVertexA;
    }
    
    public EditorVertex getEditorVertexB() {
        return _editorVertexB;
    }
    
    public boolean containsPoint(double x, double y) {
        EditorVertex a = getEditorVertexA();
        EditorVertex b = getEditorVertexB();
        double pA[] = {0.5, 0.5};
        double pB[] = {0.5, 0.5};
        
        AffineTransform aT = a.getTransformToParent();
        aT.transform(pA,0,pA,0,1);
        
        AffineTransform bT = b.getTransformToParent();
        bT.transform(pB,0,pB,0,1);
        
        double xA = pA[0]; double yA = pA[1];
        double xB = pB[0]; double yB = pB[1];
        
        double tolerance = 0.2;
        
        double ux = xA - xB;
        double uy = yA - yB;
        double vx = x - xA;
        double vy = y - yA;
        double umod = Math.sqrt(ux * ux + uy * uy);
        double vmod = Math.sqrt(vx * vx + vy * vy);
        
        double uv = ux * vx + uy * vy;
        
        double argument = Math.sqrt( vmod*vmod - uv*uv/(umod*umod));
        
        if (Math.abs(argument) <= tolerance)
            return true;
        
        return false;
    }
    
    public int getArcoIndexA() {
        return 0; //@todo this.getArco().getArcoIndexA();
    }
    
    public int getArcoIndexB() {
        return 0; //@todo this.getArco().getArcoIndexB();
    }
    
    public void draw(Graphics2D G, AffineTransform T) {
        // TODO Auto-generated method stub
        EditorVertex a = getEditorVertexA();
        EditorVertex b = getEditorVertexB();
        
        double pA[] = {0.5, 0.5};
        double pB[] = {0.5, 0.5};
        
        AffineTransform aT = a.getTransformToParent();
        aT.transform(pA,0,pA,0,1);
        T.transform(pA,0,pA,0,1);
        
        AffineTransform bT = b.getTransformToParent();
        bT.transform(pB,0,pB,0,1);
        T.transform(pB,0,pB,0,1);
        
        double minX = Math.min(pA[0], pB[0]);
        double minY = Math.min(pA[1], pB[1]);
        
        //QuadCurve2D.Double Q = new QuadCurve2D.Double(pA[0],pA[1],pA[0],pB[1],pB[0],pB[1]);
        Line2D.Double Q = new Line2D.Double(pA[0],pA[1],pB[0],pB[1]);
        
//        double ux = pB[0] - pA[0];
//        double uy = pB[1] - pA[1];
//        double midx = pA[0] + ux/2.0;
//        double midy = pA[1] + uy/2.0;
//        double normu = Math.sqrt(ux * ux + uy * uy);
//        ux = ux/normu;
//        uy = uy/normu;
//        double vx = -uy;
//        double vy = ux;
//        double normv = Math.sqrt(vx * vx + vy * vy);
//        vx = vx / normv;
//        vy = vy / normv;
//        
//        Line2D.Double Q2 = new Line2D.Double(midx + vx*5 -ux*5, midy + vy*5 -uy*5, midx,midy);
//        Line2D.Double Q3 = new Line2D.Double(midx - vx*5 -ux*5, midy - vy*5 -uy*5, midx,midy);
        
        Editor e = (Editor) this.getPropertyValue("editor");
        Stroke stroke = G.getStroke();
        if (e != null && e.isSelected(this)) {
            G.setStroke(new BasicStroke(4));
            G.setColor(new Color(0,255,255,100));
        } else {
            G.setStroke(new BasicStroke(2));
            G.setColor(new Color(0,0,0,100));
        }
        G.draw(Q);
//        G.draw(Q2);
//        G.draw(Q3);
        G.setStroke(stroke);
    }
    
}
