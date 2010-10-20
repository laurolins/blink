package drawing;

import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.util.HashMap;

/**
 * An useful base class for all IDrawing objects.
 * 
 * @author lauro
 */
public abstract class AbstractDrawing implements IDrawing {

	/**
	 * The coordinate system conversion matrixes.
	 * 
	 * _transformToParent converts the coordinates of a point 
	 * in this drawing's coordindate system in the parent's
	 * coordinate system. The other is the contrary.
	 */
	private AffineTransform _transformToParent, _transformToObject;
	
	/**
	 * The parent coordinate system. 
	 */
	private IGroup _parent;
	
	/**
	 * This object is visible? 
	 */
	private boolean _isVisible = true;
	
	
	public boolean isVisible() {
		return _isVisible;
	}
	
	public void setVisible(boolean v) {
		_isVisible = v;
	}
	
	/**
	 * Simple drawing with identity transform to parent
	 */
	public AbstractDrawing(IGroup parent) {
		_parent = parent;
		
		// identity
		_transformToParent = new AffineTransform();
	}
	
	public AbstractDrawing(IGroup parent, 
			double tx, 
			double ty,
			double theta,
			double sx, 
			double sy) {
		_parent = parent;
		
		// identity
		_transformToParent = new AffineTransform();
		_transformToParent.translate(tx, ty);
		_transformToParent.rotate(theta);
		_transformToParent.scale(sx, sy);
	}
	
	/**
	 * Set transform to parent.
	 */
	protected void setTransformToParent(
			double tx, 
			double ty,
			double theta,
			double sx, 
			double sy) {
		// identity
		_transformToParent = new AffineTransform();
		_transformToParent.translate(tx, ty);
		_transformToParent.rotate(theta);
		_transformToParent.scale(sx, sy);
		_transformToObject = null;
	}
	
	/**
	 * Get the parent of this object.
	 */
	public IGroup getParent() {
		return _parent;
	}
	
	/**
	 * Set the parent of this object.
	 */
	public void setParent(IGroup parent) {
		_parent = parent;
	}
	
	/**
	 * Get _transformToParent transform.
	 */
	public AffineTransform getTransformToParent() {
		return _transformToParent;
	}

	/**
	 * Get _transformToObject transform.
	 */
	public AffineTransform getTransformToObject() {
		if (_transformToObject == null)
			try {
				_transformToObject = _transformToParent.createInverse();
			} catch (NoninvertibleTransformException e) {
				e.printStackTrace();
				throw new RuntimeException("Ooooopsss!");
			}
		return _transformToObject;
	}
	
	
	public void transformToParent(double points[]) {
		AffineTransform T = this.getTransformToParent();
		T.transform(points,0,points,0,points.length/2);
	}
	
	public void transformToObject(double points[]) {
		AffineTransform T = this.getTransformToObject();
		T.transform(points,0,points,0,points.length/2);
	}
	
	/**
	 * Properties support. Flexible way
	 */
	private HashMap<String,Object> _properties;
	public void setPropertyValue(String key, Object value) {
		if (_properties == null)
			_properties = new HashMap<String,Object>();
		_properties.put(key, value);
	}

	public Object getPropertyValue(String key) { 
		if (_properties == null)
			return null;
		return _properties.get(key);
	}
}
