package drawing;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

public interface IDrawing {
	
	/**
	 * The parent node of this drawing. Can be null
	 * if there is no parent to this drawing. 
	 */
	public IGroup getParent();	
	

	/**
	 * Set the parent node of this drawing. 
	 */
	public void setParent(IGroup parent);
	
	
	/**
	 * Draw in graphics G. Current transform is T. 
	 */
	public void draw(Graphics2D G, AffineTransform T);
	
	/**
	 * Returns true if this object contains the point (x,y) 
	 * coordinate (in parent space coordinate-system) 
	 * otherwise false.
	 * 
	 * @param x x-coordinate in this group's parent coordinate system.
	 * @param y y-coordinate in this group's parent coordinate system.
	 * @return true if this object instersects with point (x,y) 
	 * otherwise false.
	 */
	public boolean containsPoint(double x, double y);
			
	/**
	 * This drawing is visible?
	 */
	public boolean isVisible();
	
	/**
	 * The two coordiante system transformations
	 */
	public AffineTransform getTransformToParent();
	public AffineTransform getTransformToObject();
	
	/**
	 * Get and set properties
	 */
	public void setPropertyValue(String propertyName, Object value);
	public Object getPropertyValue(String propertyName);
}
