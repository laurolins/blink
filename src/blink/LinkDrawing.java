package blink;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
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
public class LinkDrawing {
    private GBlink _gblink;
    private HashMap<GBlinkVertex,ColoredPath> _mapEdges = new HashMap<GBlinkVertex,ColoredPath>();
    private HashMap<Integer,Point2D.Double> _edgeLabelToPosition = new HashMap<Integer,Point2D.Double>();

    public LinkDrawing(GBlink gblink, int smooth, int crossSpace, int face) {
        _gblink = gblink;
        PlanarRepresentation P = Library.getLinkPlanarRepresentation(_gblink, face);
        new OrthogonalLayout(P);

        double[] bounds = P.getBounds();
        double minX = +1e+20;
        double minY = +1e+20;
        double maxX = -1e+20;
        double maxY = -1e+20;

        // tag zigzags
        Color palette[] = {Color.cyan,Color.orange,Color.magenta,Color.pink,Color.blue,Color.green,Color.red};
        Color epsPalette[] = {Color.blue,Color.magenta,Color.cyan,new Color(212,132,47),new Color(113,225,51),Color.red,Color.gray};

        HashMap<GBlinkVertex,Color> colorMap = new HashMap<GBlinkVertex,Color>();
        HashMap<GBlinkVertex,Color> colorEPSMap = new HashMap<GBlinkVertex,Color>();
        int colorIndex=0;
        for (Variable z: gblink.getGZigZags()) {
            for (GBlinkVertex v: z.getVertices()) {
                colorMap.put(v,palette[colorIndex]);
                colorEPSMap.put(v,epsPalette[colorIndex]);
            }
            colorIndex = (colorIndex+1) % palette.length;
        }

        // map
        HashMap<GBlinkVertex,Point2D.Double> mapGBlinkVertex2Point = new HashMap<GBlinkVertex,Point2D.Double>();
        for (GBlinkVertex v: _gblink.getVertices()) {
            GBlinkVertex vv = v.getVertexAtTheSameGEdgeWithMinLabel();
            PRVertex pv = P.findVertexByObject(vv);
            mapGBlinkVertex2Point.put(v,new Point2D.Double(pv.getX(),pv.getY()));
        }

        _mapEdges = new HashMap<GBlinkVertex,ColoredPath>();
        for (GBlinkVertex u: gblink.getVertices()) {
            if (u.hasEvenLabel())
                continue;
            GBlinkVertex v = u.getNeighbour(GBlinkEdgeType.edge);
            PREdge e = P.findEdgeByObject(new Pair(u,v));

            double[] path = P.getPathOfEdge(e.getId());

            Point2D.Double uLocation = mapGBlinkVertex2Point.get(u);
            Point2D.Double vLocation = mapGBlinkVertex2Point.get(v);
            int n = path.length;
            double r = Math.min(crossSpace*(bounds[2]-bounds[0])/100.0,crossSpace*(bounds[3]-bounds[1])/100.0);
            if (u.undercross()) {
               // if (path[0] == uLocation.getX() && path[1] == uLocation.getY()) {
                   double dx = path[2] - path[0];
                   double dy = path[3] - path[1];
                   double length = Math.sqrt(dx *dx + dy * dy);
                   path[0] = path[0] + dx/length*r;
                   path[1] = path[1] + dy/length*r;
                   /*
               }
               else if (path[n-2] == uLocation.getX() && path[n-1] == uLocation.getY()) {
                   double dx = path[n-4] - path[n-2];
                   double dy = path[n-3] - path[n-1];
                   double length = Math.sqrt(dx *dx + dy * dy);
                   path[n-2] = path[n-2] + dx/length*r;
                   path[n-1] = path[n-1] + dy/length*r;
               }
               else throw new RuntimeException();*/
            }
            if (v.undercross()) {
                /*if (path[0] == vLocation.getX() && path[1] == vLocation.getY()) {
                    double dx = path[2] - path[0];
                    double dy = path[3] - path[1];
                    double length = Math.sqrt(dx *dx + dy * dy);
                    path[0] = path[0] + dx/length*r;
                    path[1] = path[1] + dy/length*r;
                }
                else if (path[n-2] == vLocation.getX() && path[n-1] == vLocation.getY()) {*/
                    double dx = path[n-4] - path[n-2];
                    double dy = path[n-3] - path[n-1];
                    double length = Math.sqrt(dx *dx + dy * dy);
                    path[n-2] = path[n-2] + dx/length*r;
                    path[n-1] = path[n-1] + dy/length*r;
                /*}
                else throw new RuntimeException();*/
          }

            GeneralPath gp = new GeneralPath();
            gp.moveTo((float)path[0],(float)path[1]);
            for (int i=2;i<path.length;i+=2)
                gp.lineTo((float)path[i],(float)path[i+1]);

            // path
            for (int i=0;i<path.length;i+=2) {
                if (path[i] > maxX) maxX = path[i];
                if (path[i] < minX) minX = path[i];
                if (path[i+1] > maxY) maxY = path[i+1];
                if (path[i+1] < minY) minY = path[i+1];
            }


            // save general path
            _mapEdges.put(v,new ColoredPath(gp,colorMap.get(v),colorEPSMap.get(v)));
        }

        
        // get crossing positions

        for (Variable gEdge: this._gblink.getGEdges()) {
        	int edgeLabel = gEdge.getVertex(0).getEdgeLabel();
        	Point2D.Double uLocation = mapGBlinkVertex2Point.get(gEdge.getVertex(0));
            Point2D.Double vLocation = mapGBlinkVertex2Point.get(gEdge.getVertex(2));
            _edgeLabelToPosition.put(edgeLabel,new Point2D.Double((uLocation.x + vLocation.x)/2.0,(uLocation.y + vLocation.y)/2.0));
        }
        
        
        
        // apply transform
        AffineTransform T = new AffineTransform();

        if (minX == maxX) {
            minX = minX-0.5;
            maxX = maxX+0.5;
        }
        if (minY == maxY) {
            minY = minY-0.5;
            maxY = maxY+0.5;
        }
        T.scale(1.0/(maxX-minX),1.0/(maxY-minY));
        T.translate(-minX,-minY);
        for (ColoredPath p: _mapEdges.values()) {
            GeneralPath newPath = p.getPath();
            newPath.transform(T);
            p.setPath(newPath);
            if (smooth == 1)
                p.setPath(Library.smooth(newPath));
            else if (smooth == 2)
                p.setPath(Library.smooth2(newPath));
        }
        
        for (Integer edgeLabel: _edgeLabelToPosition.keySet()) {
        	Point2D.Double p = _edgeLabelToPosition.get(edgeLabel);
        	T.transform(p, p);
        	_edgeLabelToPosition.put(edgeLabel, p);
        }
        
        /*
        for (Crossing c: _crossings) {
            c.transform(T);
        }*/

    }


    public void draw(Graphics2D g, double x0, double y0, double w, double h, double margin) {

        g.setRenderingHint(
           RenderingHints.KEY_TEXT_ANTIALIASING,
               RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g.setRenderingHint(
                   RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        AffineTransform T = new AffineTransform();
        // T.translate(x0 + margin*w,y0 + margin*h);
        // T.scale(w*(1.0-2.0*margin),h*(1.0-2.0*margin));

        T.translate(x0 + margin*w,y0 + h*(1.0-1.0*margin));
        T.scale(w*(1.0-2.0*margin),-h*(1.0-2.0*margin));
        for (ColoredPath p: _mapEdges.values()) {
            GeneralPath pp = (GeneralPath) p.getPath().clone();
            pp.transform(T);
            g.setColor(p.getColor());
            g.setStroke(new BasicStroke(2));
            g.draw(pp);
        }
        
        for (int edgeLabel: _edgeLabelToPosition.keySet()) {
        	Point2D.Double p = _edgeLabelToPosition.get(edgeLabel);
        	Point2D.Double pp = new Point2D.Double();
        	T.transform(p, pp);        	
        	g.setColor(Color.white);
        	int gap = 3;
        	g.drawString(""+edgeLabel, (int) pp.x + gap, (int) pp.y + gap);
        }
    
    
    }

    static class ColoredPath {
        GeneralPath _path;
        Color _color;
        Color _epsColor;
        public ColoredPath(GeneralPath p, Color c, Color epsColor) {
            _path = p;
            _color = c;
            _epsColor = epsColor;
        }
        public void setPath(GeneralPath p) {
            _path = p;
        }
        public Color getColor() {
            return _color;
        }
        public Color getEPSColor() {
            return _epsColor;
        }
        public GeneralPath getPath() {
            return _path;
        }
    }

    private double _EPSLineWidth = 0.9;
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

        pw.println(String.format("%.3f setlinewidth",_EPSLineWidth));

        for (ColoredPath p: _mapEdges.values()) {
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

            //g.setColor(p.getColor());
            //g.setStroke(new BasicStroke(2));
            //g.draw(pp);
            pw.println(String.format("%.2f %.2f %.2f setrgbcolor",
                                     p.getEPSColor().getRed()/255.0,
                                     p.getEPSColor().getGreen()/255.0,
                                     p.getEPSColor().getBlue()/255.0));
            // pw.println("stroke");
            pw.println("stroke");
        }

        // draw labels
        pw.println("/Helvetica findfont 30 scalefont setfont");
        pw.println(String.format("%.2f %.2f %.2f setrgbcolor",0.0,0.0,0.0));

        // for (int edgeLabel: _edgeLabelToPosition.keySet()) {
        // 	Point2D.Double p = _edgeLabelToPosition.get(edgeLabel);
        // 	Point2D.Double pp = new Point2D.Double();
        // 	T.transform(p, pp);        	
        //     pw.println(String.format("%.4f %.4f moveto", pp.x, pp.y));
        //     pw.println("gsave");
        //     pw.println("1 -1 scale");
        //     pw.println(String.format("(%d) show", edgeLabel));
        //     pw.println("grestore");
        // }

        pw.println("grestore");
    }
}




