package blink;

import java.awt.geom.Point2D;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

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
public class DrawBlackboardFramedLink {
    public static final double POINT_TO_MM = 25.4/72;
    public static final double MM_TO_POINT = 72/25.4;
    private double _margin = 5 * MM_TO_POINT;
    private double _width = 60 * MM_TO_POINT;
    private double _height = 60 * MM_TO_POINT;

    public DrawBlackboardFramedLink(GBlink G, String fileName) throws IOException {

        // map
        HashMap<GBlinkVertex,Point2D.Double> map = TuttesLayout.mapLayout(G,_margin,_margin,_width-2*_margin,_height-2*_margin);

        // each GEdge is a crossing. Let's define each crossing
        // position as the center of each GEdge polygon.
        ArrayList<Variable> gEdges = G.getGEdges();
        HashMap<GBlinkVertex,Point2D.Double> mapVertexToCrossCenter = new HashMap<GBlinkVertex,Point2D.Double>();
        for (Variable gEdge: gEdges) {
            Point2D.Double center = new Point2D.Double();
            for (GBlinkVertex v: gEdge.getVertices()) {
                Point2D.Double vPos = map.get(v);
                center.setLocation(center.getX()+vPos.getX(),center.getY()+vPos.getY());
            }
            double n = gEdge.size();
            center.setLocation(center.getX()/n,center.getY()/n);
            for (GBlinkVertex v: gEdge.getVertices()) {
                mapVertexToCrossCenter.put(v,center);
            }
        }

        ArrayList<ConnectionArc> arcs = new ArrayList<ConnectionArc>();
        for (GBlinkVertex a: G.getVertices()) {
            GBlinkVertex b = a.getNeighbour(GBlinkEdgeType.edge);
            if (!(a.getLabel() < b.getLabel()))
                continue;
            Point2D.Double p0 = mapVertexToCrossCenter.get(a);
            Point2D.Double p1 = map.get(a);
            Point2D.Double p2 = map.get(b);
            Point2D.Double p3 = mapVertexToCrossCenter.get(b);
            ConnectionArc arc = new ConnectionArc(p0,p1,p2,p3,a.undercross(),b.undercross());
            arcs.add(arc);
        }



        PrintWriter pw = new PrintWriter(new FileWriter(fileName));
        printHeader(pw);
        for (ConnectionArc arc: arcs) {
            arc.printArc(pw);
        }
        printFooter(pw);
        pw.close();
        //PrintWriter pw = new PrintWriter("");
    }



    class ConnectionArc {
        private boolean _startingUndercross;
        private boolean _endingUndercross;
        private Point2D.Double _p0;
        private Point2D.Double _p1;
        private Point2D.Double _p2;
        private Point2D.Double _p3;
        public ConnectionArc(Point2D.Double p0, Point2D.Double p1, Point2D.Double p2, Point2D.Double p3,
                             boolean startingUndercross, boolean endingUndercross) {
            _p0 = p0;
            _p1 = p1;
            _p2 = p2;
            _p3 = p3;
            _startingUndercross = startingUndercross;
            _endingUndercross = endingUndercross;

            //System.out.println("StartU = "+_startingUndercross+" EndU="+_endingUndercross);

            prepareGeometry();
        }

        private Point2D.Double scale(Point2D.Double p, double s) {
            return new Point2D.Double(p.getX() * s, p.getY() * s);
        }

        private Point2D.Double add(Point2D.Double p, Point2D.Double q) {
            return new Point2D.Double(p.getX() + q.getX(), p.getY() + q.getY());
        }

        private Point2D.Double perp(Point2D.Double p) {
            return new Point2D.Double( -p.getY(), p.getX());
        }

        private Point2D.Double sub(Point2D.Double p, Point2D.Double q) {
            return new Point2D.Double(p.getX() - q.getX(), p.getY() - q.getY());
        }

        private double modulus(Point2D.Double p) {
            return p.distance(0, 0);
        }

        public double length(double t0, double t1, int segments) {
            double segLength = (t1 - t0) / segments;
            double result = 0;
            for (int i = 0; i < segments; i++) {
                Point2D.Double pi = getCurvePoint(t0 + i * segLength);
                Point2D.Double pim1 = getCurvePoint(t0 + (i + 1) * segLength);
                result += pi.distance(pim1);
            }
            return result;
        }

        public double binarySearch(double t0, double distance, int segments) {
            double l, r;
            if (distance < 0) {
                l = 0;
                r = t0;
                while ( -distance > length(t0, l, segments))
                    l = (l - 1) * 10;
            } else if (distance > 0) {
                l = t0;
                r = 1;
                while (distance > length(r, t0, segments))
                    r = 10 * r;
            } else
                return t0;

            double absDistance = Math.abs(distance);
            double tResult = 0;
            while (true) {
                double m = (l + r) / 2.0;
                double currDistance = length(m, t0, segments);
                if (Math.abs(currDistance - absDistance) < 1.0e-4) {
                    tResult = m;
                    break;
                }
                if (distance > 0) {
                    if (currDistance < absDistance)
                        l = m;
                    else
                        r = m;
                } else {
                    if (currDistance < absDistance)
                        r = m;
                    else
                        l = m;
                }
            }
            return tResult;
        }

        private Point2D.Double p[];
        private double x[];
        private double y[];
        private double ax, ay, bx, by, cx, cy;
        private void prepareGeometry() {
            // prepare vectors
            p = new Point2D.Double[] {(Point2D.Double)_p0.clone(), (Point2D.Double)_p1.clone(), (Point2D.Double)_p2.clone(), (Point2D.Double)_p3.clone()};
            x = new double[] {p[0].getX(), p[1].getX(), p[2].getX(), p[3].getX()};
            y = new double[] {p[0].getY(), p[1].getY(), p[2].getY(), p[3].getY()};
            cx = 3 * x[1] - 3 * x[0];
            cy = 3 * y[1] - 3 * y[0];
            bx = 3 * x[2] - 6 * x[1] + 3 * x[0];
            by = 3 * y[2] - 6 * y[1] + 3 * y[0];
            ax = x[3] - x[0] + 3 * x[1] - 3 * x[2];
            ay = y[3] - y[0] + 3 * y[1] - 3 * y[2];

            double open = 1 * MM_TO_POINT;

            double t0 = 0;
            double t1 = 1;
            if (_startingUndercross) {
                // t0 = 0.1;
                t0 = binarySearch(0,open,100);
            }

            if (_endingUndercross) {
                //t1 = 0.9;
                t1 = binarySearch(1,-open,100);
            }
            adjustPolynomial(t0,t1);
        }


        /**
         * The adjusted polynomial for the same cubic only adjusted to be
         * evaluated on 0 to (x(t0),y(t0)) and on 1 (x(t1),y(t1)). This was
         * given by the Maple by the command: collect(Exp,{t})
         *
         * a*(t1-t0)^3   *    t^3  +
         * (3*a*t0*(t1-t0)^2+b*(t1-t0)^2)  *  t^2  +
         * (3*a*t0^2*(t1-t0)+c*(t1-t0)+2*b*t0*(t1-t0)) * t +
         * a*t0^3+x0+c*t0+b*t0^2
         */
        private void adjustPolynomial(double t0, double t1) {

            double aax = ax*(t1-t0)*(t1-t0)*(t1-t0);
            double bbx = (3*ax*t0*(t1-t0)*(t1-t0)+bx*(t1-t0)*(t1-t0));
            double ccx = (3*ax*t0*t0*(t1-t0)+cx*(t1-t0)+2*bx*t0*(t1-t0));
            double xx0 = ax*t0*t0*t0+bx*t0*t0+cx*t0+x[0];

            double aay = ay*(t1-t0)*(t1-t0)*(t1-t0);
            double bby = (3*ay*t0*(t1-t0)*(t1-t0)+by*(t1-t0)*(t1-t0));
            double ccy = (3*ay*t0*t0*(t1-t0)+cy*(t1-t0)+2*by*t0*(t1-t0));
            double yy0 = ay*t0*t0*t0+by*t0*t0+cy*t0+y[0];

            ax = aax; ay = aay;
            bx = bbx; by = bby;
            cx = ccx; cy = ccy;

            x[0] = xx0;
            x[1] = x[0] + cx/3.0;
            x[2] = x[1] + (cx + bx)/3.0;
            x[3] = ax+bx+cx+x[0];

            y[0] = yy0;
            y[1] = y[0] + cy/3.0;
            y[2] = y[1] + (cy + by)/3.0;
            y[3] = ay+by+cy+y[0];

            p[0].setLocation(x[0],y[0]);
            p[1].setLocation(x[1],y[1]);
            p[2].setLocation(x[2],y[2]);
            p[3].setLocation(x[3],y[3]);

        }



        public Point2D.Double getCurvePoint(double t) {
            return new Point2D.Double(ax * t * t * t + bx * t * t + cx * t + x[0],
                                      ay * t * t * t + by * t * t + cy * t + y[0]);
        }

        public Point2D.Double getCurveTangent(double t) {
            return new Point2D.Double(3 * ax * t * t + 2 * bx * t + cx, 3 * ay * t * t + 2 * by * t + cy);
        }

        public void printArc(PrintWriter pw) {
            // setup color
            pw.println("0 0 0 setrgbcolor");
            pw.println("1 setlinewidth");

            // draw the arc segment
            pw.println("newpath");
            pw.println(String.format("%.4f %.4f moveto", x[0], y[0]));
            pw.println(String.format("%.4f %.4f %.4f %.4f %.4f %.4f curveto", x[1], y[1], x[2], y[2], x[3], y[3]));
            //pw.println(String.format("%.4f %.4f moveto", x[0], y[0]));
            //pw.println(String.format("%.4f %.4f lineto", x[3], y[3]));
            pw.println("stroke");
        }
    }

    /**
     * Print the header of an EPS file using _width and _height.
     */
    private void printHeader(PrintWriter pw) {
        int W = (int) (_width);
        int H = (int) (_height);

        pw.println("%!PS-Adobe-3.0 EPSF-3.0");
        pw.println(String.format("%%%%BoundingBox: -2 -2 %d %d", W + 4, H + 4));

        pw.println("/FSD {findfont exch scalefont def} bind def");
        pw.println("/SMS {setfont moveto show} bind def");
        pw.println("/MS {moveto show} bind def");

        int fontSizes[] = {3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 14, 16};
        for (int fs : fontSizes) {
            pw.println("/Helvetica" + fs + " " + fs + " /Helvetica FSD");
            pw.println("/Times" + fs + " " + fs + " /Times FSD");
            pw.println("/Courier" + fs + " " + fs + " /Courier FSD");
            pw.println("/Symbol" + fs + " " + fs + " /Symbol FSD");
            pw.println("/Helvetica-Oblique" + fs + " " + fs + " /Helvetica-Oblique FSD");
            pw.println("/Times-Oblique" + fs + " " + fs + " /Times-Oblique FSD");
            pw.println("/Courier-Oblique" + fs + " " + fs + " /Courier-Oblique FSD");
            pw.println("/Helvetica-Bold" + fs + " " + fs + " /Helvetica-Bold FSD");
            pw.println("/Times-Bold" + fs + " " + fs + " /Times-Bold FSD");
            pw.println("/Courier-Bold" + fs + " " + fs + " /Courier-Bold FSD");
        }

        // pw.println("0.01 setlinewidth");
        pw.println(String.format("0 %d translate", H));
        pw.println("1 -1 scale");
    }

    /**
     * Print the footer of an EPS file
     */
    public void printFooter(PrintWriter pw) {
        pw.println("showpage");
        pw.println("%%EOF");
        pw.println("%%EndDocument");
    }

    public void printLine(PrintWriter pw, double x0, double y0, double w, double h) {
        { // show boundary lines
            pw.println("gsave");
            pw.println("0.25 setlinewidth");
            pw.println("newpath");
            pw.println(String.format("%.4f %.4f moveto", x0, y0));
            pw.println(String.format("%.4f %.4f lineto", x0 + w, y0 + h));
            pw.println("0.5 setgray");
            pw.println("stroke");
            pw.println("grestore");
        } // end: show boundary lines
    }

    public void printRecangle(PrintWriter pw, double x0, double y0, double w, double h) {
        { // show boundary lines
            pw.println("gsave");
            pw.println("0.25 setlinewidth");
            pw.println("newpath");
            pw.println(String.format("%.4f %.4f moveto", x0, y0));
            pw.println(String.format("%.4f %.4f lineto", x0, y0 + h));
            pw.println(String.format("%.4f %.4f lineto", x0 + w, y0 + h));
            pw.println(String.format("%.4f %.4f lineto", x0 + w, y0));
            pw.println("closepath");
            pw.println("0.5 setgray");
            pw.println("stroke");
            pw.println("grestore");
        } // end: show boundary lines
    }

    public static void main(String[] args) throws SQLException, IOException {


        ArrayList<BlinkEntry> bes = App.getRepositorio().getBlinks(1,9);
        for (BlinkEntry be: bes) {
            GBlink G = be.getBlink();
            DrawBlackboardFramedLink D = new DrawBlackboardFramedLink(G,"log/knots/knot_"+be.get_id()+".eps");
            System.out.println("Processing "+be.get_id());
        }
        System.exit(0);
    }

}
