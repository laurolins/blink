package linsoft.graph;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.util.ArrayList;

import javax.swing.JCheckBox;
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
public class PanelPlanarRepresentation extends JPanel {

    private PlanarRepresentation _P;
    private JCheckBox _showAllVertices = new JCheckBox("AllVert",true);
    private JCheckBox _showAllEdges = new JCheckBox("AllEdge",true);
    private JCheckBox _showVertexLabels = new JCheckBox("LblVert",true);
    private JCheckBox _showEdgeLabels = new JCheckBox("LblEdge",true);
    private JPanel _panelDraw;

    public PanelPlanarRepresentation(PlanarRepresentation P) {
        super();
        _P = P;
        this.setLayout(new BorderLayout(2,2));
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(_showAllVertices);
        buttonPanel.add(_showAllEdges);
        buttonPanel.add(_showVertexLabels);
        buttonPanel.add(_showEdgeLabels);
        this.add(buttonPanel,BorderLayout.SOUTH);
        _panelDraw = new JPanel() {
            public void paint(Graphics g) {
                super.paint(g);
                int width = _panelDraw.getWidth();
                int height = _panelDraw.getHeight();
                draw((Graphics2D) g,width,height);
            }
        };
        this.add(_panelDraw,BorderLayout.CENTER);


        ActionListener a = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                repaint();
            }
        };

        _showAllVertices.addActionListener(a);
        _showAllEdges.addActionListener(a);
        _showEdgeLabels.addActionListener(a);
        _showVertexLabels.addActionListener(a);
    }

    public void draw(Graphics2D g, int width, int height) {

        g.setRenderingHint(
           RenderingHints.KEY_TEXT_ANTIALIASING,
               RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(
                   RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // System.out.println(width+" x "+height);
        ArrayList<PRVertex> listVertices;
        if (_showAllVertices.isSelected()) {
            listVertices = _P.getVertices();
        }
        else listVertices = _P.getOriginalVertices();
        ArrayList<PREdge> listEdges;
        if (_showAllEdges.isSelected()) {
            listEdges = _P.getEdges();
        }
        else listEdges = _P.getOriginalOrDegreeAdjustmentEdges();

        double maxX = 0;
        double maxY = 0;
        double minX = Integer.MAX_VALUE;
        double minY = Integer.MAX_VALUE;
        for (PRVertex v: _P.getVertices()) {
            if (v.getX() > maxX) maxX = v.getX();
            if (v.getY() > maxY) maxY = v.getY();
            if (v.getX() < minX) minX = v.getX();
            if (v.getY() < minY) minY = v.getY();
        }

        if (maxX == minX) {
            minX = -1;
            minX = 1;
        }
        if (maxY == minY) {
            minY = -1;
            minY = 1;
        }


        double margin = 0.075;
        double x0 = margin * width;
        double y0 = margin * height;
        double sx = (width - 2.0 * margin * width)/(double) (maxX - minX);
        double sy = (height - 2.0 * margin * height)/(double) (maxY - minY);

        Line2D.Double l = new Line2D.Double();
        for (PREdge edge: listEdges) {
            double xA = x0 + edge.getV1().getX()*sx;
            double yA = height - y0 - edge.getV1().getY()*sy;
            double xB = x0 + edge.getV2().getX()*sx;
            double yB = height - y0 - edge.getV2().getY()*sy;
            l.setLine(xA,yA,xB,yB);
            if (edge.isDegreeAdjustementEdge()) {
                g.setColor(Color.black);
            }
            else if (edge.getOriginalId() == -1) {
                g.setColor(Color.magenta);
            }
            else {
                g.setColor(Color.black);
            }
            g.draw(l);

            if (_showEdgeLabels.isSelected()) {
                g.setFont(new Font("Tahoma", Font.BOLD, 12));
                g.drawString("" + edge.getId()+" "+edge.getOriginalId(), (int) ((xA + xB) / 2.0), (int) ((yA + yB) / 2.0));
            }
        }

        Ellipse2D.Double e = new Ellipse2D.Double();
        for (PRVertex v: listVertices) {
            double x = x0 + v.getX()*sx;
            double y = height - y0 - v.getY()*sy;
            double r = 15;
            e.setFrame(x-r,y-r,2*r,2*r);
            if (v.istExtraVertex()) {
                g.setColor(Color.red);
                g.fill(e);
                g.setColor(Color.black);
                g.draw(e);
            }
            else if (v.isDegreeAdjustedVertex()) {
                g.setColor(Color.yellow);
                g.fill(e);
                g.setColor(Color.black);
                g.draw(e);
            }
            else {
                g.setColor(Color.blue);
                g.fill(e);
                g.setColor(Color.black);
                g.draw(e);
            }
            if (_showVertexLabels.isSelected()) {
                g.setFont(new Font("Tahoma", Font.BOLD, 14));
                g.drawString("" + v.getId(), (int) (x - 8), (int) (y + 3));
            }
        }
    }
}
