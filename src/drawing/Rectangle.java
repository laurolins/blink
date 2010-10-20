package drawing;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;

public class Rectangle extends AbstractDrawing {
	/**
	 * The spec of the rectangle in terms of it's parent 
	 * cordinate system.
	 */
	private double _x0, _y0, _theta, _width, _height;
	
	public Rectangle(IGroup parent, double x0, double y0, double theta, double width, double height) {
		super(parent, x0, y0, theta, width, height);
		_x0 = x0;
		_y0 = y0;
		_theta = theta;
		_width = width;
		_height = height;
	}
	
	/**
	 * Set transform to parent.
	 */
	public void setTransformToParent(
			double tx, 
			double ty,
			double theta,
			double sx, 
			double sy) {
		super.setTransformToParent(tx, ty, theta, sx, sy);
		_x0 = tx;
		_y0 = ty;
		_theta = theta;
		_width = sx;
		_height = sy;
	}	
	
	public double get_height() {
		return _height;
	}

	public double get_theta() {
		return _theta;
	}

	public double get_width() {
		return _width;
	}

	public double get_x0() {
		return _x0;
	}

	public double get_y0() {
		return _y0;
	}

	/**
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

	public void draw(Graphics2D G, AffineTransform T) {	
		
		if (!this.isVisible())
			return;
			
		double[] points = {0,0,1,0,1,1,0,1};

		this.transformToParent(points);
		// System.out.println(String.format("Rect Before %.2f %.2f %.2f %.2f",points[0],points[1],points[2],points[3]));
		
		T.transform(points,0,points,0,4);
		// System.out.println(String.format("Rect After %.2f %.2f %.2f %.2f",points[0],points[1],points[2],points[3]));

		// define the shape  
		GeneralPath.Double P = new GeneralPath.Double();
		P.moveTo(points[0], points[1]);
		P.lineTo(points[2], points[3]);
		P.lineTo(points[4], points[5]);
		P.lineTo(points[6], points[7]);
		P.closePath();
		
		// draw the shape
		G.setColor(new Color(255,255,0,128));
		G.fill(P);
		G.setColor(Color.black);
		G.draw(P);
	}

}
