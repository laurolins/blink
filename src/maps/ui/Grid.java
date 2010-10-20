package maps.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;

import drawing.AbstractDrawing;

public class Grid extends AbstractDrawing {
    double _x0;
    double _y0;
    double _spacingX;
    double _spacingY;
    
    public Grid(double x0, double y0, double spacingX, double spacingY) {
        super(null);
        _x0 = x0;
        _y0 = y0;
        _spacingX = spacingX;
        _spacingY = spacingY;
    }
    
    public void update(double x0, double y0, double spacingX, double spacingY) {
        _x0 = x0;
        _y0 = y0;
        _spacingX = spacingX;
        _spacingY = spacingY;
    }
    
    
    public double[] snap(double x, double y) {
        double xx = _x0+Math.round((x-_x0)/_spacingX)*_spacingX;
        double yy = _y0+Math.round((y-_y0)/_spacingY)*_spacingY;
        return new double[] {xx,yy};
    }
    
    public boolean containsPoint(double x, double y) {
        return false;
    }
    
    public void draw(Graphics2D G, AffineTransform T) {
        
        for (int i=-5000;i<10000;i++) {
            double pA[] = {_x0 + i*_spacingX,-100000,_x0 + i*_spacingX, 100000};
            T.transform(pA,0,pA,0,2);
            Stroke stroke = G.getStroke();
            G.setStroke(new BasicStroke(0.5f));
            G.setColor(Color.darkGray);
            Line2D.Double line = new Line2D.Double(pA[0],pA[1],pA[2],pA[3]);
            
            // bounds
            Rectangle2D bounds = line.getBounds2D();
            if (
                    bounds.getMaxX() < 0 ||
                    bounds.getMaxY() < 0 ||
                    bounds.getMinX() > 3000 ||
                    bounds.getMinY() > 3000
                    )
                continue;
            
            G.draw(line);
            G.setStroke(stroke);
        }
        
        for (int i=-5000;i<10000;i++) {
            double pA[] = {-100000,_y0 + i*_spacingY,100000,_y0 + i*_spacingY};
            T.transform(pA,0,pA,0,2);
            Stroke stroke = G.getStroke();
            G.setStroke(new BasicStroke(0.5f));
            G.setColor(Color.darkGray);
            Line2D.Double line = new Line2D.Double(pA[0],pA[1],pA[2],pA[3]);
            
            // bounds
            Rectangle2D bounds = line.getBounds2D();
            if (
                    bounds.getMaxX() < 0 ||
                    bounds.getMaxY() < 0 ||
                    bounds.getMinX() > 3000 ||
                    bounds.getMinY() > 3000
                    )
                continue;
            
            G.draw(line);
            G.setStroke(stroke);
        }
    }
}