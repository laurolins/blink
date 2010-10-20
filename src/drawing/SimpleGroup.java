package drawing;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;

public class SimpleGroup extends AbstractDrawing implements IGroup {
    
    private ArrayList<IDrawing> _childs;
    
    // private ArrayList
    public SimpleGroup(IGroup parent) {
        super(parent);
        _childs = new ArrayList<IDrawing>();
    }
    
    public SimpleGroup(IGroup parent,
            double tx,
            double ty,
            double theta,
            double sx,
            double sy) {
        super(parent,tx,ty,theta,sx,sy);
        _childs = new ArrayList<IDrawing>();
    }
    
    public void draw(Graphics2D G, AffineTransform T) {
        AffineTransform transormToTopLevel = new AffineTransform(T);
        transormToTopLevel.concatenate(this.getTransformToParent());
        for (IDrawing c: _childs) {
            if (c.isVisible())
                c.draw(G, transormToTopLevel);
        }
    }
    
    public void addChild(IDrawing d) {
        _childs.add(d);
        d.setParent(this);
    }
    
    public void deleteChild(IDrawing d) {
        int index = _childs.indexOf(d);
        if (index >= -1) {
            d.setParent(null);
            _childs.remove(d);
        }
    }
    
    public void insertChild(IDrawing d) {
        _childs.add(0,d);
        d.setParent(this);
    }
    
    public boolean containsPoint(double x, double y) {
        // it doesn't hit anything
        return false;
    }
    
    public boolean getAllDrawingsThatContainsPoint(double x, double y, List<IDrawing> outputList) {
        
        // transform the coordinates in parent space to local space
        double point[] = {x,y};
        this.transformToObject(point);
        
        // the point in local coordinates will be (xx,yy)
        double xx = point[0];
        double yy = point[1];
        
        // collect all intersections
        boolean result = false;
        for (IDrawing c: _childs) {
            if (!c.isVisible())
                continue;
            
            if (c instanceof IGroup) {
                result  = result || ((IGroup) c).getAllDrawingsThatContainsPoint(xx, yy, outputList);
            } else if (c.containsPoint(xx, yy)) {
                outputList.add(c);
                result = true;
            }
        }
        
        // see if this object intersects xx,yy
        if (this.containsPoint(xx, yy)) {
            outputList.add(this);
            result = true;
        }
        
        return result;
    }
    
    public IDrawing getTopMostDrawingThatContainsPoint(double x, double y) {
        
        // transform the coordinates in parent space to local space
        double point[] = {x,y};
        this.transformToObject(point);
        
        // the point in local coordinates will be (xx,yy)
        double xx = point[0];
        double yy = point[1];
        
        // System.out.println("getTopMostDrawingThatContainsPoint parent: "+x+" "+y+"   local: "+xx+" "+yy);

        // collect all intersections
        IDrawing result  = null;
        for (int i=_childs.size()-1;i>=0;i--) {
            IDrawing c = _childs.get(i);
            if (!c.isVisible())
                continue;
            
            if (c instanceof IGroup) {
                result = ((IGroup) c).getTopMostDrawingThatContainsPoint(xx, yy);
                if (result != null)
                    break;
            } else if (c.containsPoint(xx, yy)) {
                result = c;
                break;
            }
        }
        
        // see if this object intersects xx,yy (the group object is the
        // bottom-most object aming all it's childs
        if (result == null && this.containsPoint(xx, yy)) {
            result = this;
        }
        
        return result;
    }
    
}
