package blink;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.io.PrintWriter;
import java.util.HashMap;

import linsoft.Pair;
import linsoft.graph.OrthogonalLayout;
import linsoft.graph.PREdge;
import linsoft.graph.PRVertex;
import linsoft.graph.PlanarRepresentation;

/**
 * Draw link with Tamassia's algorithm
 */
public class BlinkDrawing {
    private GBlink _gblink;
    private HashMap<GBlinkVertex,ColoredGeneralPath> _mapEdges = new HashMap<GBlinkVertex,ColoredGeneralPath>();
    private HashMap<GBlinkVertex,Point2D.Double> _mapVertices = new HashMap<GBlinkVertex,Point2D.Double>();
    // private HashMap<

    class ColoredGeneralPath {
        private GeneralPath _path;
        private Color _color;
        public ColoredGeneralPath(GeneralPath path, Color color) {
            _path = path;
            _color = color;
        }
        public GeneralPath getPath() {
            return _path;
        }
        public void setPath(GeneralPath path) {
            _path = path;
        }
        public Color getColor() {
            return _color;
        }
    }


    public BlinkDrawing(GBlink gblink, int smooth, int face) {
        _gblink = gblink;
        PlanarRepresentation P = Library.getBlinkPlanarRepresentation(_gblink, face);
        P.createFacesOnVerticesWithDegreeGreaterThan4();
        new OrthogonalLayout(P);

        double minX = +1e+20;
        double minY = +1e+20;
        double maxX = -1e+20;
        double maxY = -1e+20;

        _mapEdges = new HashMap<GBlinkVertex, ColoredGeneralPath>();
        for (Variable gEdge : gblink.getGEdges()) {
            GBlinkVertex u = gEdge.getVertex(0);
            if (u.hasEvenLabel())
                u = u.getNeighbour(GBlinkEdgeType.face);
            GBlinkVertex v = u.getNeighbour(GBlinkEdgeType.diagonal);
            if (u.getLabel() > v.getLabel()) {
                GBlinkVertex aux = u;
                u = v;
                v = aux;
            }
            Pair p = new Pair(u, v);
            PREdge e = P.findEdgeByObject(new Pair(u, v));
            double[] path = P.getPathOfEdge(e.getId());
            GeneralPath gp = new GeneralPath();
            gp.moveTo((float) path[0], (float) path[1]);
            for (int i = 2; i < path.length; i += 2)
                gp.lineTo((float) path[i], (float) path[i + 1]);

            // path
            for (int i = 0; i < path.length; i += 2) {
                if (path[i] > maxX)
                    maxX = path[i];
                if (path[i] < minX)
                    minX = path[i];
                if (path[i + 1] > maxY)
                    maxY = path[i + 1];
                if (path[i + 1] < minY)
                    minY = path[i + 1];
            }

            // save general path
            _mapEdges.put(u, new ColoredGeneralPath(gp, u.isGreen() ? Color.green : Color.red));
        }

        // save crossings...
        _mapVertices = new HashMap<GBlinkVertex, Point2D.Double>();
        for (Variable gVertex : gblink.getGVertices()) {
            GBlinkVertex u = null;
            PRVertex v = null;
            int i=0;
            while (v == null && i < gVertex.size()) {
                u = gVertex.getVertex(i);
                v = P.findVertexByObject(u);
                i++;
            }
            if (v == null)
                throw new RuntimeException("OOOooopss");

            // save general path
            _mapVertices.put(u, new Point2D.Double(v.getX(), v.getY()));
        }

        // apply transform
        if (minX == maxX) {
            minX = minX-0.5;
            maxX = maxX+0.5;
        }
        if (minY == maxY) {
            minY = minY-0.5;
            maxY = maxY+0.5;
        }        AffineTransform T = new AffineTransform();
        T.scale(1.0 / (maxX - minX), 1.0 / (maxY - minY));
        T.translate( -minX, -minY);
        for (ColoredGeneralPath p : _mapEdges.values()) {
            p.getPath().transform(T);
            if (smooth == 1)
                p.setPath(Library.smooth(p.getPath()));
            else if (smooth == 2)
                p.setPath(Library.smooth2(p.getPath()));
        }
        for (Point2D p : _mapVertices.values())
            T.transform(p, p);
    }

    public void draw(Graphics2D g, double x0, double y0, double w, double h, double margin) {

        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        AffineTransform T = new AffineTransform();
        //T.translate(x0 + margin*w,y0 + margin*h);
        //T.scale(w*(1.0-2.0*margin),h*(1.0-2.0*margin));
        T.translate(x0 + margin*w,y0 + h*(1.0-1.0*margin));
        T.scale(w*(1.0-2.0*margin),-h*(1.0-2.0*margin));
        for (ColoredGeneralPath p: _mapEdges.values()) {
            GeneralPath pp = (GeneralPath) p.getPath().clone();
            pp.transform(T);
            g.setColor(p.getColor());
            g.setStroke(new BasicStroke(2));
            g.draw(pp);
        }

        for (Point2D p: _mapVertices.values()) {
            Point2D pp = (Point2D) p.clone();
            T.transform(pp,pp);
            g.setColor(Color.white);
            double r = _pixelRadius;
            Ellipse2D e = new Ellipse2D.Double(pp.getX()-r,pp.getY()-r,2*r,2*r);
            g.fill(e);
        }
    }

    private double _pixelRadius = 3;
    private double _EPSRadiusInMM = 0.625 * 72.0/25.4;
    public void setPixelRadius(double r) {
        _pixelRadius = r;
    }
    public void setEPSRadiusInMM(double r) {
        _EPSRadiusInMM = r * 72.0/25.4;
    }
    private double _EPSLineWidth = 1;
    public void setEPSLineWidthInMM(double w) {
        _EPSLineWidth = w * 72.0/25.4;
    }

    public void drawEPS(PrintWriter pw, double x0, double y0, double w, double h, double margin) {

        AffineTransform T = new AffineTransform();
        //T.translate(x0 + margin*w,y0 + margin*h);
        //T.scale(w*(1.0-2.0*margin),h*(1.0-2.0*margin));
        T.translate(x0 + margin*w,y0 + h*(1.0-1.0*margin));
        T.scale(w*(1.0-2.0*margin),-h*(1.0-2.0*margin));

        pw.println("gsave");
        for (ColoredGeneralPath p: _mapEdges.values()) {
            GeneralPath pp = (GeneralPath) p.getPath().clone();
            pp.transform(T);

            pw.println(String.format("%.3f setlinewidth",_EPSLineWidth));

            PathIterator iterator = pp.getPathIterator(new AffineTransform());

            pw.println("newpath");

            double current[] = {0,0,0,0,0,0};
            while (!iterator.isDone()) {
                int code = iterator.currentSegment(current);
                if (code == PathIterator.SEG_MOVETO) {
                    pw.println(String.format("%.4f %.4f moveto", current[0], current[1]));
                }
                else if (code == PathIterator.SEG_LINETO) {
                    pw.println(String.format("%.4f %.4f lineto", current[0], current[1]));
                }
                else if (code == PathIterator.SEG_QUADTO) {
                    pw.println(String.format("%.4f %.4f %.4f %.4f %.4f %.4f curveto",
                                             current[0],
                                             current[1],
                                             current[0],
                                             current[1],
                                             current[2],
                                             current[3]));
                }
                else if (code == PathIterator.SEG_CUBICTO) {
                    pw.println(String.format("%.4f %.4f %.4f %.4f %.4f %.4f curveto",
                                             current[0],
                                             current[1],
                                             current[2],
                                             current[3],
                                             current[4],
                                             current[5]));
                }
                else if (code == PathIterator.SEG_CLOSE) {
                    pw.println(String.format("closepath"));
                }
                iterator.next();
            }

            pw.println(String.format("%.2f %.2f %.2f setrgbcolor",
                                     p.getColor().getRed()/255.0,
                                     p.getColor().getGreen()/255.0,
                                     p.getColor().getBlue()/255.0));

            // pw.println("stroke");
            pw.println("stroke");
        }

        for (Point2D p: _mapVertices.values()) {
            Point2D pp = (Point2D) p.clone();
            T.transform(pp,pp);
            //double r = 3 * 25.4/72.0;
            double r = _EPSRadiusInMM;
            pw.println("newpath");
            // pw.println(String.format("%.4f %.4f moveto", pp.getX(), pp.getY()));
            pw.println(String.format("%.4f %.4f %.4f 0 360 arc", pp.getX(), pp.getY(), r));
            pw.println("closepath");
            pw.println("0 0 0 setrgbcolor");
            pw.println("fill");
        }

        pw.println("grestore");

    }


}
