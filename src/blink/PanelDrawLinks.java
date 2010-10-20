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
public class PanelDrawLinks extends JPanel {

    private ArrayList<LinkDrawing> _drawings;
    private int _rows;
    private int _cols;

    public PanelDrawLinks(ArrayList<GBlink> list, int rows, int cols, int smooth, int crossingSpace) {
        _drawings = new ArrayList<LinkDrawing>();
        for (GBlink b: list) {
            _drawings.add(new LinkDrawing(b,smooth,crossingSpace,-1));
        }
        _rows = rows;
        _cols = cols;
    }

    public PanelDrawLinks(ArrayList<LinkDrawing> drawings, int rows, int cols) {
        _drawings = (ArrayList<LinkDrawing>) drawings.clone();
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
        for (LinkDrawing ld: _drawings) {
            ld.draw((Graphics2D) g,c*cellWidth,r*cellHeight,cellWidth,cellHeight,0.1);
            c++;
            if (c == _cols) {
                r++;
                c=0;
            }
        }
    }
}
