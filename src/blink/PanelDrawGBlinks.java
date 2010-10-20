package blink;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.util.ArrayList;

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
public class PanelDrawGBlinks extends JPanel {

    private ArrayList<GBlinkDrawing> _drawings;
    private int _rows;
    private int _cols;

    public PanelDrawGBlinks(ArrayList<GBlink> list, int rows, int cols, int smooth) {
        _drawings = new ArrayList<GBlinkDrawing>();
        for (GBlink b: list) {
            _drawings.add(new GBlinkDrawing(b,smooth,-1));
        }
        _rows = rows;
        _cols = cols;
    }

    public PanelDrawGBlinks(ArrayList<GBlinkDrawing> drawings, int rows, int cols) {
        _drawings = (ArrayList<GBlinkDrawing>) drawings.clone();
        _rows = rows;
        _cols = cols;
    }

    public void paint(Graphics g) {
        super.paint(g);

        int width = this.getWidth();
        int height = this.getHeight();

        g.setColor(Color.black);
        g.fillRect(0,0,width+1,height+1);

        double cellWidth = (double) width / _cols;
        double cellHeight = (double) height / _rows;

        int r = 0; int c = 0;
        for (GBlinkDrawing ld: _drawings) {
            ld.draw((Graphics2D) g,c*cellWidth,r*cellHeight,cellWidth,cellHeight,0.1);
            c++;
            if (c == _cols) {
                r++;
                c=0;
            }
        }
    }
}
