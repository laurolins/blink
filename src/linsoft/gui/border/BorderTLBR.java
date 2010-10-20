package linsoft.gui.border;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Insets;

import javax.swing.border.AbstractBorder;

/**
 * A class which implements a line border of arbitrary thickness
 * and of a single color.
 * <p>
 * <strong>Warning:</strong>
 * Serialized objects of this class will not be compatible with
 * future Swing releases. The current serialization support is
 * appropriate for short term storage or RMI between applications running
 * the same version of Swing.  As of 1.4, support for long term storage
 * of all JavaBeans<sup><font size="-2">TM</font></sup>
 * has been added to the <code>java.beans</code> package.
 * Please see {@link java.beans.XMLEncoder}.
 *
 * @version 1.21 12/03/01
 * @author David Kloba
 */
public class BorderTLBR extends AbstractBorder
{
    private boolean _left = false;
    private boolean _right = false;
    private boolean _top = false;
    private boolean _bottom = false;
    private int _thickness = 1;
    private Color _lineColor = Color.red;

    /**
     * Creates a line border with the specified color, thickness,
     * and corner shape.
     * @param color the color of the border
     * @param thickness the thickness of the border
     * @param roundedCorners whether or not border corners should be round
     * @since 1.3
     */
    public BorderTLBR(Color color, int thickness, boolean left, boolean top, boolean right, boolean bottom)  {
        _lineColor = color;
        _thickness = thickness;
        _left = left;
        _top = top;
        _right = right;
        _bottom = bottom;
    }

    /**
     * Paints the border for the specified component with the
     * specified position and size.
     * @param c the component for which this border is being painted
     * @param g the paint graphics
     * @param x the x position of the painted border
     * @param y the y position of the painted border
     * @param width the width of the painted border
     * @param height the height of the painted border
     */
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        Color oldColor = g.getColor();
        int i;

    /// PENDING(klobad) How/should do we support Roundtangles?
        g.setColor(_lineColor);
        for (i = 0; i < _thickness; i++) {
            int l = x;
            int r = x + width - 1;
            int t = y;
            int b = y + height - 1;
            if (_left) { g.drawLine(l+i, t, l + i, b); }
            if (_right) { g.drawLine(r-i, t, r-i, b); }
            if (_top) { g.drawLine(l, t + i, r, t+i); }
            if (_bottom) { g.drawLine(l, b-i, r, b-i); }
        }
        g.setColor(oldColor);
    }

    /**
     * Returns the insets of the border.
     * @param c the component for which this border insets value applies
     */
    public Insets getBorderInsets(Component c)       {
        return new Insets(_top ? _thickness : 0, _left ? _thickness : 0, _bottom ? _thickness : 0,_right ? _thickness : 0);
    }

    /**
     * Reinitialize the insets parameter with this Border's current Insets.
     * @param c the component for which this border insets value applies
     * @param insets the object to be reinitialized
     */
    public Insets getBorderInsets(Component c, Insets insets) {
        insets.left = _left ? _thickness : 0;
        insets.right = _right ? _thickness : 0;
        insets.top = _top ? _thickness : 0;
        insets.bottom = _bottom ? _thickness : 0;
        return insets;
    }

    /**
     * Returns the color of the border.
     */
    public Color getLineColor()     {
        return _lineColor;
    }

    /**
     * Returns the thickness of the border.
     */
    public int getThickness()       {
        return _thickness;
    }

    /**
     * Returns whether or not the border is opaque.
     */
    public boolean isBorderOpaque() {
        return true;
    }

    public void setVisibleBorderLines(boolean left, boolean right, boolean top, boolean bottom) {
        _left = left; _right = right; _top = top; _bottom = bottom;
    }

    public void setColor(Color color) {
        _lineColor = color;
    }
}
