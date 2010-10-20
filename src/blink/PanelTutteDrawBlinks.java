package blink;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;

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
public class PanelTutteDrawBlinks extends JPanel {

    private ArrayList<GBlink> _blinks;
    private int _rows;
    private int _cols;

    public PanelTutteDrawBlinks(ArrayList<GBlink> list, int rows, int cols) {
        _blinks = list;
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
        for (GBlink G: _blinks) {
            this.drawBlink((Graphics2D) g,G,c*cellWidth,r*cellHeight,cellWidth,cellHeight);
            c++;
            if (c == _cols) {
                r++;
                c=0;
            }
        }
    }

    private void drawBlink(Graphics2D g, GBlink G, double x0, double y0, double ww, double hh) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        double topMargin = 10; // space to info
        double leftMargin = 10;
        double bottomMargin = 10; // space to info
        double rightMargin = 10;
        double w = ww - leftMargin - rightMargin;
        double h = hh - topMargin - bottomMargin;

        g.setStroke(new BasicStroke(3));

        HashMap<GBlinkVertex, Point2D.Double> map = TuttesLayout.mapLayout(G, 0, 0, 1, 1);

        HashMap<Variable, Point2D.Double> mapVPos = new HashMap<Variable, Point2D.Double>();

        // translate
        AffineTransform t0 = new AffineTransform();

        t0.translate(x0+leftMargin,y0+hh-topMargin);
        t0.scale(w,-h);

        ArrayList<Variable> varVertices = G.getGVertices();
        for (Variable var : varVertices) {
            Point2D.Double p = new Point2D.Double(0.0, 0.0);
            for (GBlinkVertex vv : var.getVertices()) {
                Point2D.Double pAux = map.get(vv);
                t0.transform(pAux,pAux);
                p.setLocation(p.getX() + pAux.getX(), p.getY() + pAux.getY());
            }
            if (var.size() == 0) {
                p.setLocation(0.5, 0.5);
                t0.transform(p,p);
            } else {
                p.setLocation(p.getX() / var.size(), p.getY() / var.size());
            }
            mapVPos.put(var, p);
        }

        for (int i = 1; i <= G.getNumberOfGEdges(); i++) {
            GBlinkVertex va = G.findVertex((i - 1) * 4 + 1);
            GBlinkVertex vb = G.findVertex((i - 1) * 4 + 2);
            GBlinkVertex vc = G.findVertex((i - 1) * 4 + 3);
            GBlinkVertex vd = G.findVertex((i - 1) * 4 + 4);

            Point2D.Double p1 = mapVPos.get(G.findVariable(va, Variable.G_VERTICE));
            Point2D.Double p2 = new Point2D.Double(0, 0);
            p2.setLocation((map.get(va).getX() + map.get(vb).getX()) / 2.0,
                           (map.get(va).getY() + map.get(vb).getY()) / 2.0);
            Point2D.Double p3 = new Point2D.Double(0, 0);
            p3.setLocation((map.get(vc).getX() + map.get(vd).getX()) / 2.0,
                           (map.get(vc).getY() + map.get(vd).getY()) / 2.0);
            Point2D.Double p4 = mapVPos.get(G.findVariable(vc, Variable.G_VERTICE));

            GeneralPath path = new GeneralPath();
            path.moveTo((float)p1.getX(),(float)p1.getY());
            path.curveTo(
                (float)p2.getX(),(float)p2.getY(),
                (float)p3.getX(),(float)p3.getY(),
                (float)p4.getX(),(float)p4.getY());

            if (G.getColor(i) == BlinkColor.green)
                g.setColor(Color.green);
            else
                g.setColor(Color.red);
            g.draw(path);
        }

        // draw vertices as black dots
        for (Variable var : varVertices) {
            Point2D.Double p = mapVPos.get(var);

            float radius = 3.5f;

            Ellipse2D.Double circle = new Ellipse2D.Double(0,0,1,1);
            circle.setFrame(p.getX()-radius,p.getY()-radius,2*radius,2*radius);

            g.setColor(Color.white);
            g.fill(circle);

        }

        // g.setTransform(t0);

    }

}
