package drawing;

import java.util.List;


/**
 * The group is a IDrawing that can contain other drawings in it.
 * The IGroup object may or may not have some graphical 
 * representation. If it doesn't have it's intersects (inherited 
 * from IDrawing) returns false. Even though some of it's childs
 * may intersect the given point.
 * 
 * @author lauro
 */
public interface IGroup extends IDrawing {
	
	/**
	 * Returns the first IDrawing object (top most) that
	 * intersects the given point.
	 * 
	 * @param x x-coordinate in this group's parent coordinate system.
	 * @param y y-coordinate in this group's parent coordinate system.
	 * 
	 * @return null if no drawing in this group contains this point
	 * otherwise the IDrawing object inside this IGroup that contains this 
	 * point.
	 */
	public IDrawing getTopMostDrawingThatContainsPoint(double x, double y);

	
	/**
	 * Returns true if some object was included in the resulting 
	 * outputList.
	 * 
	 * @param x x-coordinate in this group's parent coordinate system.
	 * @param y y-coordinate in this group's parent coordinate system.
	 * 
	 * @return false if no drawing in this group contains this point
	 * otherwise true. If true, the outputList is added by the IDrawings
	 * in top-to-bottom fashion.
	 */
	public boolean getAllDrawingsThatContainsPoint(double x, double y, List<IDrawing> outputList);
}
