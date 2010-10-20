package maps.grapheditor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.HashMap;

import util.Library;
import util.Vetor;

import maps.EmbeddedGraph;
import drawing.AbstractDrawing;
import drawing.Text;

public class EditorEdge extends AbstractDrawing {
    
    private EmbeddedGraph.Edge _connection;
    
    private EditorVertex _editorVertexA;
    
    private EditorVertex _editorVertexB;
    
    public EditorEdge(EmbeddedGraph.Edge connection, EditorVertex editorVertexA, EditorVertex editorVertexB) {
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
    
    public EmbeddedGraph.Edge getEdge() {
        return _connection;
    }
    
    public EditorVertex getEditorVertexA() {
        return _editorVertexA;
    }
    
    public EditorVertex getEditorVertexB() {
        return _editorVertexB;
    }
    
    public boolean containsPoint(double x, double y) {
		for (int i=1;i<_connection.getPathSize();i++) {
			double pA[] = {_connection.getX(i-1),_connection.getY(i-1)};
			double pB[] = {_connection.getX(i),_connection.getY(i)};

			double tolerance = 0.2;
			
			Vetor ab = new Vetor(pB[0] - pA[0], pB[1] - pA[1]);
			Vetor am = new Vetor(x - pA[0], y - pA[1]);
			Vetor proj = am.projecaoSobre(ab);
			
			if (proj.modulo() <= ab.modulo()){
				if (ab.subtracao(proj).modulo() <= ab.modulo()){
					double ort = am.subtracao(proj).modulo();
					
					if (ort <= tolerance)
						return true;
				}
			}
		}
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
        
		Path2D.Double path = new Path2D.Double();
        
//        AffineTransform aT = a.getTransformToParent();
//        aT.transform(pA,0,pA,0,1);
//        T.transform(pA,0,pA,0,1);
//        path.moveTo(pA[0],pA[1]);

		boolean first = true;
		for (int i=0;i<_connection.getPathSize();i++) {
			double pI[] = {_connection.getX(i),_connection.getY(i)};
			T.transform(pI,0,pI,0,1);
			if (first)
				path.moveTo(pI[0],pI[1]);
			else
				path.lineTo(pI[0],pI[1]);
			first = false;
		}
                     
//        AffineTransform bT = b.getTransformToParent();
//        bT.transform(pB,0,pB,0,1);
//        T.transform(pB,0,pB,0,1);
//
//        path.lineTo(pB[0],pB[1]);
		double xtext, ytext;
		if (_connection.getPathSize() % 2 == 1) {
			int middlePointIndex = _connection.getPathSize() / 2;
			double middlePoint[] = {
					_connection.getX(middlePointIndex),
					_connection.getY(middlePointIndex)};
			xtext = middlePoint[0];
			ytext = middlePoint[1];
		}	
		else {
			int middlePointIndex = _connection.getPathSize() / 2;
			double middlePoint[] = {
					(_connection.getX(middlePointIndex-1) + _connection.getX(middlePointIndex)) / 2.0,
					(_connection.getY(middlePointIndex-1) + _connection.getY(middlePointIndex)) / 2.0};
			xtext = middlePoint[0];
			ytext = middlePoint[1];
		}
		
		
        double minX = Math.min(pA[0], pB[0]);
        double minY = Math.min(pA[1], pB[1]);
        
        //QuadCurve2D.Double Q = new QuadCurve2D.Double(pA[0],pA[1],pA[0],pB[1],pB[0],pB[1]);
        // Line2D.Double Q = new Line2D.Double(pA[0],pA[1],pB[0],pB[1]);
        
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

        
        Color fgcolor = new Color(0,0,0,100);
        try { fgcolor = Library.getColorFromString((String) _connection.getProperty("fgcolor")); } 
        catch (Exception xx) {}
        
        
        Editor e = (Editor) this.getPropertyValue("editor");
        Stroke stroke = G.getStroke();
        if (e != null && e.isSelected(this)) {
            G.setStroke(new BasicStroke(4));
            //G.setColor(new Color(0,255,255,100));
            G.setColor(fgcolor);
        } else {
            G.setStroke(new BasicStroke(2));
            //G.setColor(new Color(0,0,0,100));
            G.setColor(fgcolor);
        }
        G.draw(path);
//        G.draw(Q);
//        G.draw(Q2);
//        G.draw(Q3);
        
        Color foreground = Color.black;
        double fontSize = 0.8;

		// bgcolor
		// fgcolor
		// label1, x1, y1, color1, size1
		// label2, x2, y2, color2, size2
		// ...
		HashMap<String,Object> properties = _connection.getProperties();
		ArrayList<String> sufixos = new ArrayList<String>();
		for (String key: properties.keySet()) {
			if (key.indexOf("label") == 0) {
				String value = properties.get(key).toString();
				if (value != null && value.trim().length() > 0) {
					sufixos.add(key.substring(5));
				}
			}
		}
		
		if (sufixos.size() == 0) {
			G.setColor(foreground);
			Text t = new Text(null,_connection.getId()+"",xtext,ytext,0,fontSize);
			AffineTransform TT = new AffineTransform(T);
			TT.concatenate(this.getTransformToParent());
			t.draw(G, TT);
		}
		else {
			// draw labels
			for (String index: sufixos) {
				String label = properties.get("label"+index).toString();
				String stx = (String) properties.get("x"+index);
				String sty = (String) properties.get("y"+index);
				String stcolor = (String) properties.get("color"+index);
				String stsize = (String) properties.get("color"+index);

				double x = xtext;
				double y = ytext;
				Color color = foreground;
				double size = fontSize;

				try { x = 0.5 + Double.parseDouble(stx); } catch (Exception xx) {}
				try { y = 0.5 + Double.parseDouble(sty); } catch (Exception xx) {}
				try { size = Double.parseDouble(stsize); } catch (Exception xx) {}
				try { color = Library.getColorFromString(stcolor); } catch (Exception xx) {}

				G.setColor(color);
				Text t = new Text(null,label,x,y,0,fontSize);
				AffineTransform TT = new AffineTransform(T);
				TT.concatenate(this.getTransformToParent());
				t.draw(G, TT);
			}
		}
        
        // labels
//        
//        G.setColor(Color.blue);
//		// TODO como fazer pra esse próximo comando (new TextLayout) não detonar a memória
//        G.setFont(new Font(Font.DIALOG,Font.BOLD,14));
//		TextLayout tLayout = new TextLayout(""+_connection.getId(),
//				G.getFont(),G.getFontRenderContext());
//		// if centered text
//		tLayout.draw(G,(float)xtext,(float)ytext);        
        
        G.setStroke(stroke);
    }
    
}
