package blink;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JPanel;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class DrawPanel extends JPanel {
    MapD _mapD;
    public DrawPanel(MapD m) {
        super();
        _mapD = m;
    }
    public void paint(Graphics g) {
        super.paint(g);
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        _mapD.draw((Graphics2D) g, this.getWidth(),this.getHeight(),0,0);
    }
}
