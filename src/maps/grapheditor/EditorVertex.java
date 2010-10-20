package maps.grapheditor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashMap;

import util.Library;

import maps.EmbeddedGraph;
import drawing.AbstractDrawing;
import drawing.Text;

public class EditorVertex extends AbstractDrawing {
	private EmbeddedGraph.Vertex _vertex;
	// private VertexDecoration _decoration;
	
	public EditorVertex(EmbeddedGraph.Vertex vertex) {
		super(null,vertex.getX(),vertex.getY(),0,1,1);
		_vertex = vertex;
	}

//	public static class VertexDecoration {
//		private double _x = 0;
//		private double _y = 0;
//		private double _radius = 0.5;
//		public VertexDecoration() {
//		}
//	}
	
	public String getNome() {
		return ""+_vertex.getId();
	}

	public EmbeddedGraph.Vertex getVertex() {
		return _vertex;
	}
	
	public String getDescription() {
		return ""+_vertex.getId();
	}

	public void setVertex(EmbeddedGraph.Vertex vertex) {
		_vertex = vertex;
	}

	/**
	 * Change the position of the vertex
	 */
	public void setPosition(double x, double y) {
		_vertex.setPosition(x, y);
		this.setTransformToParent(x, y, 0, 1, 1);
	}

	/**                     
   5     42,                                                            
   6     79,                    500                                     
   7     14,                    500                                     
   8     81,                   13.8                                     
   9     82,                   13.8                                     
  10     84,                    500                                     
  11     87,                    500                                     
  12     90,                    500                                     
  13   1 91,                   13.8                                     
  14   1 92,                                                            
  15   1 93,                   13.8                                     
  16   1 94,                                                            
9999

(-------------------------- Dados de Circuito -------------------------------
 37
(BF  C   BT  NC T  R1    X1    R0    X0    CN   S1   S0   TAP  TB  TCIA DEFI
(--- -  ----====-======------======------======-----=====-----=====--===---=
   1       9          0     1     0     12-82                               
   1       8          0     1     0     12-81                               
   4       2          0     1     0     130-9                               
   1       4          0     1     0     12-30                               
   4      14          0     0     0     030-92                              
   2      14          0     0     0     09-92                
	 * The easiest implementation of an intersection.
	 */
	public boolean containsPoint(double x, double y) {
		// transform the coordinates in parent space to local space 
		double point[] = {x,y};
		this.transformToObject(point);
		
		// the point in local coordinates will be (xx,yy)
		double xx = point[0];
		double yy = point[1];
		
		if (0 <= xx && xx <= 1.0 && 0 <= yy && yy <= 1.0) {
			return true;
		}
		else return false;
	}

	public double getX() {
//		return _vertex.getEditorX();
		return _vertex.getX();
	}
	
	public double getY() {
//		return _vertex.getEditorY();
		return _vertex.getY();
	}

	public void draw(Graphics2D G, AffineTransform T) {	
		
		if (!this.isVisible())
			return;
			
		double[] points = {0,0,1,0,1,1,0,1};

		this.transformToParent(points);
		//System.out.println(String.format("Rect Before %.2f %.2f %.2f %.2f",points[0],points[1],points[2],points[3]));
		
		T.transform(points,0,points,0,4);
		//System.out.println(String.format("Rect After %.2f %.2f %.2f %.2f",points[0],points[1],points[2],points[3]));

		// define the shape  
		GeneralPath.Double P = new GeneralPath.Double();
		P.moveTo(points[0], points[1]);
		P.lineTo(points[2], points[3]);
		P.lineTo(points[4], points[5]);
		P.lineTo(points[6], points[7]);
		P.closePath();
		
		Ellipse2D.Double E = new Ellipse2D.Double ();
		E.setFrame(points[0], points[1], points[4]-points[0], points[5]-points[1]);
		
		// bounds
		Rectangle2D bounds = P.getBounds2D(); 
		if (
				bounds.getMaxX() < 0 ||
				bounds.getMaxY() < 0 ||
				bounds.getMinX() > 3000 ||
				bounds.getMinY() > 3000
		)
			return;
		
		
		// colors
		double fontSize = 0.7; // World Coordinates
        Color fgcolor = Color.black;
        Color bgcolor = Color.lightGray;
        try { fgcolor = Library.getColorFromString((String) _vertex.getProperty("fgcolor")); } 
        catch (Exception xx) {}
        try { fgcolor = Library.getColorFromString((String) _vertex.getProperty("fgcolor")); } 
        catch (Exception xx) {}
        
        
		
		
		G.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
		
		// draw the shape
		G.setColor(bgcolor);
		G.fill(E);
		Editor e = (Editor) this.getPropertyValue("editor");
		Stroke stroke = G.getStroke();
		if (e != null && e.isSelected(this)) {
			G.setStroke(new BasicStroke(5));
		}
		G.setColor(fgcolor);
		G.draw(E);
		G.setStroke(stroke);

		//
		// this.drawIcon(G, T);
		
//		if (this.getNome().length() > 0) {
//			if (bounds.getWidth() < 10 || bounds.getHeight() < 10)
//				return;
//			Text t = new Text(null,this.getNome(),0.5,0.5,0,fontSize);
//			AffineTransform TT = new AffineTransform(T);
//			TT.concatenate(this.getTransformToParent());
//			t.draw(G, TT);
//		}

		// bgcolor
		// fgcolor
		// label1, x1, y1, color1, size1
		// label2, x2, y2, color2, size2
		// ...
		HashMap<String,Object> properties = _vertex.getProperties();
		ArrayList<String> sufixos = new ArrayList<String>();
		for (String key: properties.keySet()) {
			if (key.indexOf("label") == 0) {
				String value = (String) properties.get(key);
				if (value != null && value.trim().length() > 0) {
					sufixos.add(key.substring(5));
				}
			}
		}
		
		if (sufixos.size() == 0 || !sufixos.contains("")) {
			G.setColor(fgcolor);
			Text t = new Text(null,_vertex.getId()+"",0.5,0.5,0,fontSize);
			AffineTransform TT = new AffineTransform(T);
			TT.concatenate(this.getTransformToParent());
			t.draw(G, TT);
		}
		
		if (sufixos.size() > 0) {
			// draw labels
			for (String index: sufixos) {
				String label = (String) properties.get("label"+index);
				String stx = (String) properties.get("x"+index);
				String sty = (String) properties.get("y"+index);
				String stcolor = (String) properties.get("color"+index);
				String stsize = (String) properties.get("size"+index);

				double x = 0.5;
				double y = 0.5;
				Color color = fgcolor;
				double size = fontSize;

				try { x = 0.5 + Double.parseDouble(stx); } catch (Exception xx) {}
				try { y = 0.5 + Double.parseDouble(sty); } catch (Exception xx) {}
				try { size = Double.parseDouble(stsize); } catch (Exception xx) {}
		        try { color = Library.getColorFromString(stcolor); } 
		        catch (Exception xx) {}

		        G.setColor(color);
				Text t = new Text(null,label,x,y,0,size);
				AffineTransform TT = new AffineTransform(T);
				TT.concatenate(this.getTransformToParent());
				t.draw(G, TT);
			}
		}
	}
}
