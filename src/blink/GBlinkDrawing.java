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
import java.util.ArrayList;

import linsoft.Pair;
import linsoft.graph.OrthogonalLayout;
import linsoft.graph.PREdge;
import linsoft.graph.PRVertex;
import linsoft.graph.PlanarRepresentation;

/**
 * Draw link with Tamassia's algorithm
 */
public class GBlinkDrawing {
    private GBlink _gblink;
    private ArrayList<ColoredGeneralPath> _edges = new ArrayList<ColoredGeneralPath>();
    private ArrayList<Vertex> _vertices = new ArrayList<Vertex>();
    // private HashMap<

    class Vertex {
        Point2D.Double _position;
        private int _label;
        public Vertex(Point2D.Double position, int label) {
            _position = position;
            _label = label;
        }
        public Point2D.Double getPosition() {
            return _position;
        }
        public int getLabel() {
            return _label;
        }
        public void transform(AffineTransform T) {
            T.transform(_position,_position);
        }
    }


    class ColoredGeneralPath {
        private GeneralPath _path;
        private GBlinkEdgeType _type;
        public ColoredGeneralPath(GeneralPath path, GBlinkEdgeType type) {
            _path = path;
            _type = type;
        }
        public GeneralPath getPath() {
            return _path;
        }
        public void setPath(GeneralPath path) {
            _path = path;
        }
        public GBlinkEdgeType getType() {
            return _type;
        }

        public Color getColor() {
            if (_type == GBlinkEdgeType.edge) {
                return Color.cyan;
            }
            else if (_type == GBlinkEdgeType.vertex) {
                return new Color(80,80,80);
            }
            else /* if (_type == GBlinkEdgeType.face) */ {
                return Color.orange;
            }
        }
        public Color getEPSColor() {
            if (_type == GBlinkEdgeType.edge) {
                return Color.black;
            }
            else if (_type == GBlinkEdgeType.vertex) {
                return new Color(200,200,200);
            }
            else /* if (_type == GBlinkEdgeType.face) */ {
                return Color.black;
            }
        }

    }

    /**
     * A negative face indicates the default external face:
     * the one with the most edges and first to appear.
     */
    public GBlinkDrawing(GBlink gblink, int smooth, int face) {
        _gblink = gblink;
        PlanarRepresentation P = Library.getGBlinkPlanarRepresentation(_gblink, face);
        new OrthogonalLayout(P);


        double bounds[] = P.getBounds();
        double minX = bounds[0];
        double minY = bounds[1];
        double maxX = bounds[2];
        double maxY = bounds[3];

        for (GBlinkVertex u : gblink.getVertices()) {

            PRVertex uu = P.findVertexByObject(u);
            _vertices.add(new Vertex(new Point2D.Double(uu.getX(),uu.getY()),u.getLabel()));

            if (u.hasEvenLabel())
                continue;

            for (GBlinkEdgeType t: new GBlinkEdgeType[]{GBlinkEdgeType.edge,GBlinkEdgeType.face,GBlinkEdgeType.vertex}) {
                Pair p = new Pair(u, t);
                PREdge e = P.findEdgeByObject(p);
                double[] path = P.getPathOfEdge(e.getId());
                GeneralPath gp = new GeneralPath();
                gp.moveTo((float) path[0], (float) path[1]);
                for (int i = 2; i < path.length; i += 2)
                    gp.lineTo((float) path[i], (float) path[i + 1]);

                // save general path
                _edges.add(new ColoredGeneralPath(gp,t));
            }
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
        for (ColoredGeneralPath p : _edges) {
            p.getPath().transform(T);
            if (smooth == 1)
                p.setPath(Library.smooth(p.getPath()));
            else if (smooth == 2)
                p.setPath(Library.smooth2(p.getPath()));
        }
        for (Vertex v : _vertices)
            v.transform(T);
    }

    public void draw(Graphics2D g, double x0, double y0, double w, double h, double margin) {

        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        AffineTransform T = new AffineTransform();
        T.translate(x0 + margin*w,y0 + h*(1.0-1.0*margin));
        T.scale(w*(1.0-2.0*margin),-h*(1.0-2.0*margin));
        //T.translate(x0 + margin*w,y0 + h*margin);
        //T.scale(w*(1.0-2.0*margin),h*(1.0-2.0*margin));
        for (ColoredGeneralPath p: _edges) {
            GeneralPath pp = (GeneralPath) p.getPath().clone();
            pp.transform(T);
            g.setColor(p.getColor());
            g.setStroke(new BasicStroke(2));
            g.draw(pp);
        }

        for (Vertex v: _vertices) {
            Point2D pp = (Point2D) v.getPosition().clone();
            T.transform(pp,pp);
            g.setStroke(new BasicStroke(1));
            double r = 3;
            Ellipse2D e = new Ellipse2D.Double(pp.getX()-r,pp.getY()-r,2*r,2*r);
            if (v.getLabel() % 2 == 0) {
                g.setColor(Color.white);
            }
            else {
                g.setColor(Color.black);
            }
            g.fill(e);
            g.setColor(Color.yellow);
            g.draw(e);
        }
    }

    public void drawEPS(PrintWriter pw, double x0, double y0, double w, double h, double margin) {

        AffineTransform T = new AffineTransform();
        T.translate(x0 + margin*w,y0 + h*(1.0-1.0*margin));
        T.scale(w*(1.0-2.0*margin),-h*(1.0-2.0*margin));
        // T.translate(x0 + margin*w,y0 + margin*h);
        // T.scale(w*(1.0-2.0*margin),h*(1.0-2.0*margin));

        pw.println("gsave");
        for (ColoredGeneralPath p: _edges) {
            GeneralPath pp = (GeneralPath) p.getPath().clone();
            pp.transform(T);

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

            Color color = p.getEPSColor();
            pw.println(String.format("%.2f %.2f %.2f setrgbcolor",
                                     color.getRed()/255.0,
                                     color.getGreen()/255.0,
                                     color.getBlue()/255.0));

            // pw.println("stroke");
            pw.println("stroke");
        }

        for (Vertex v: _vertices) {
            Point2D pp = (Point2D) v.getPosition().clone();
            T.transform(pp,pp);
            double r = 4 * 25.4/72.0;
            pw.println("newpath");
            // pw.println(String.format("%.4f %.4f moveto", pp.getX(), pp.getY()));
            pw.println(String.format("%.4f %.4f %.4f 0 360 arc", pp.getX(), pp.getY(), r));
            pw.println("closepath");
            pw.println("gsave");
            if (v.getLabel() % 2 == 0) {
                pw.println("1 1 1 setrgbcolor");
                pw.println("fill");
            }
            else {
                pw.println("0 0 0 setrgbcolor");
                pw.println("fill");
            }
            pw.println("grestore");
            pw.println("0.5 setlinewidth");
            pw.println("0 0 0 setrgbcolor");
            pw.println("stroke");
        }

        pw.println("grestore");

    }


}
