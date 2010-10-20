package blink;

import java.awt.geom.Point2D;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import org.jscience.mathematics.matrices.Operable;
import org.jscience.mathematics.matrices.Vector;
import org.jscience.mathematics.numbers.Float64;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;

class FacesToEPS {
    float CONVERSION = 72f / 2.54f;

    // all of these are in cm
    private double _width = 18;
    private double _height = 8;
    private double _margin = 1;

    private RFace _f1;
    private RFace _f2;

    HashMap<GemVertex, Point2D.Double> _map;

    private RPoint _a,_b,_c,_d;

    public FacesToEPS(RFace[] pairs, int level,String label,
            RPoint a, RPoint b, RPoint c, RPoint d) throws FileNotFoundException {

        // point a, b, c, d
        _a = a; _b = b; _c = c; _d = d;

        // create first page
        PrintWriter pw = new PrintWriter(String.format("c:/workspace/blink/tex/faces"+ level + ".eps"));
        this.printHeader(pw);

        pw.println("gsave");
        pw.println("/Helvetica findfont 12 scalefont setfont");
        pw.println(String.format("%.4f %.4f translate", v(0.5), v(0.6)));
        pw.println(String.format("1 -1 scale %.4f %.4f moveto", 0.0f,0.0f));
        pw.println(String.format("(%s) show", label +" level "+level));
        pw.println("grestore");

        double header = 0.75;

        double marginX = 0.3;
        double marginY = 0.3;

        int columns = 3;
        int rows = pairs.length / columns;

        double cellX = _width / columns;
        double cellY = (_height-header) / rows;

        int col = 0;
        int row = 0;
        for (int i=0;i<pairs.length;i+=2) {
            double tx = col * cellX + marginX;
            double ty = header + row * cellY + marginY;
            double sx = cellX - 2 * marginX;
            double sy = cellY - 2 * marginY;
            this.draw(pw, pairs[i],pairs[i+1],tx, ty, sx, sy);
            col = col + 1;
            if (col >= columns) {
                row++;
                col = 0;
            }
        }
        this.printFooter(pw);
        pw.close();
    }

    private void draw(
            PrintWriter pw,
            RFace f1, RFace f2,
            double tx, double ty,
            double sx, double sy) {

        GemColor c1 = f1.getColor();
        GemColor c2 = f2.getColor();

        // find out common arc
        RArc commonArc = RFace.getCommonArc(f1, f2);
        if (commonArc == null)
            throw new RuntimeException("ooooooooopssss");

        RPoint x = commonArc.getFirstPoint();
        RPoint y = commonArc.getLastPoint();
        RPoint u = f1.getOppositeVertice(commonArc);
        RPoint v = f2.getOppositeVertice(commonArc);

        HashMap<RPoint, Vector> map = new HashMap<RPoint, Vector>();

        if ((u.equals(_a) && v.equals(_c)) || (u.equals(_c) && v.equals(_a)) ||
            (x.equals(_b) && y.equals(_d)) || (x.equals(_d) && y.equals(_b)) ||
            (u.equals(_b) && v.equals(_d)) || (u.equals(_d) && v.equals(_b)) ||
            (x.equals(_a) && y.equals(_c)) || (x.equals(_c) && y.equals(_a)) ) {
            map.put(_a, Vector.valueOf(new double[] {0, sy / 2.0}));
            map.put(_c, Vector.valueOf(new double[] {sx, sy / 2.0}));
            map.put(_b, Vector.valueOf(new double[] {sx / 2.0, 0}));
            map.put(_d, Vector.valueOf(new double[] {sx / 2.0, sy}));
        }
        else if
           ((u.equals(_a) && v.equals(_d)) || (u.equals(_d) && v.equals(_a)) ||
            (x.equals(_b) && y.equals(_c)) || (x.equals(_c) && y.equals(_b)) ||
            (u.equals(_b) && v.equals(_c)) || (u.equals(_c) && v.equals(_b)) ||
            (x.equals(_a) && y.equals(_d)) || (x.equals(_d) && y.equals(_a))) {
            map.put(_a, Vector.valueOf(new double[] {0, sy / 2.0}));
            map.put(_d, Vector.valueOf(new double[] {sx, sy / 2.0}));
            map.put(_b, Vector.valueOf(new double[] {sx / 2.0, 0}));
            map.put(_c, Vector.valueOf(new double[] {sx / 2.0, sy}));
        }
        else throw new RuntimeException();

        RArc arcs[] = {
                      f1.getArc(u, x),
                      f1.getArc(u, y),
                      // f1.getArc(x, y),
                      f2.getArc(x, v),
                      f2.getArc(y, v)};

        for (RArc a : arcs) {
            RPoint p1 = a.getFirstPoint();
            RPoint p2 = a.getLastPoint();
            Vector v1 = map.get(p1);
            Vector v2 = map.get(p2);
            ArrayList<RPoint> points = a.getPointsInOrder(p1);
            for (int i = 1; i < points.size() - 1; i++) {
                RPoint p = points.get(i);
                Vector vp = v1.plus(v2.minus(v1).
                                    times(Float64.valueOf((double) i / (points.size() - 1))));
                map.put(p, vp);
            }
        }

        // define S as all segments
        HashSet<RSegment> S = new HashSet<RSegment>();
        S.addAll(f1.getSegments());
        S.addAll(f2.getSegments());

        // all points
        HashSet<RPoint> P = new HashSet<RPoint>();
        for (RSegment s : S) {
            P.add(s.getA());
            P.add(s.getB());
        }

        //
        tutteLayout(P,S,map);

        // translate
        pw.println("gsave");
        pw.println(String.format("%.4f %.4f translate", v(tx), v(ty)));

        // print face
        for (RFace f : new RFace[] {f1,f2}) {
            for (RTriangle t : f.getTriangles()) {
                Vector pa = map.get(t.getA());
                Vector pb = map.get(t.getB());
                Vector pc = map.get(t.getC());

                pw.println("newpath");
                pw.println(String.format("%.4f %.4f moveto", v(pa.get(0)), v(pa.get(1))));
                pw.println(String.format("%.4f %.4f lineto", v(pb.get(0)), v(pb.get(1))));
                pw.println(String.format("%.4f %.4f lineto", v(pc.get(0)), v(pc.get(1))));
                pw.println(String.format("closepath"));

                GemColor color = f.getColor();
                if (color == GemColor.green)
                    pw.println("0 1 0 setrgbcolor");
                else if (color == GemColor.red)
                    pw.println("1 0 0 setrgbcolor");
                else if (color == GemColor.blue)
                    pw.println("0 0 1 setrgbcolor");
                else if (color == GemColor.yellow)
                    pw.println("1 1 0 setrgbcolor");

                pw.println(String.format("fill"));
                //pw.println("0 0 0 setrgbcolor");
                //pw.println(String.format("stroke"));
            }
        }

        // print segments
        for (RSegment s : S) {
            Vector pa = map.get(s.getA());
            Vector pb = map.get(s.getB());

            if (pa == null || pb == null)
                continue;

            pw.println("newpath");
            pw.println(String.format("%.4f %.4f moveto", v(pa.get(0)), v(pa.get(1))));
            pw.println(String.format("%.4f %.4f lineto", v(pb.get(0)), v(pb.get(1))));
            pw.println("0.5 0.5 0.5 setrgbcolor");
            pw.println(String.format("stroke"));
        }

        // print points
        for (RPoint p : P) {
            Vector pp = map.get(p);

            if (pp == null)
                continue;

            pw.println("newpath");
            pw.println(String.format("%.4f %.4f moveto", v(pp.get(0)), v(pp.get(1))));
            pw.println(String.format("%.4f %.4f %.4f 0 360 arc", v(pp.get(0)), v(pp.get(1)), v(0.06)));
            pw.println("closepath");
            pw.println("0 0 0 setrgbcolor");
            pw.println("fill");

            pw.println("gsave");
            pw.println("/Helvetica findfont 8 scalefont setfont");
            pw.println(String.format("%.4f %.4f translate", v(pp.get(0)), v(pp.get(1))));
            pw.println(String.format("1 -1 scale %.4f %.4f moveto", v(0.07), v(0.07)));
            pw.println(String.format("(%s) show", p.getNameIfExistsOtherwiseId()));
            pw.println("grestore");

        }

        // restore
        pw.println("grestore");

    }

    public double v(Operable o) {
        return ((Float64) o).doubleValue() * CONVERSION;
    }

    public double v(double d) {
        return d * CONVERSION;
    }

    /*
        private void drawGem(PrintWriter pw) {
            _map = TuttesLayout.tutteLayout(
                _gem,
                GemColor.yellow,
                CONVERSION * _margin,
                CONVERSION * _margin,
                CONVERSION * (_width - 2*_margin),
                CONVERSION * (_height - 2*_margin));

            //
            HashSet<GemVertex> processedVertices = new HashSet<GemVertex>();
            HashMap<Pair,Integer> connections = new HashMap<Pair,Integer>();
            for (GemColor c: new GemColor[] {GemColor.yellow,GemColor.green,GemColor.red,GemColor.blue}) {

                processedVertices.clear();

                for (GemVertex v : _gem.getVertices()) {

                    GemVertex u = v.getNeighbour(c);

                    if (processedVertices.contains(u))
                        continue;

                    processedVertices.add(v);

                    Pair p = new Pair(v,u);
                    Integer k = connections.get(p);
                    k = (k == null ? 1 : k+1);
                    connections.put(p,k);

                    Point2D.Double p0 = _map.get(v);
                    Point2D.Double p3 = _map.get(u);

                    Point2D.Double delta = new Point2D.Double(p3.getX() - p0.getX(),
                                                              p3.getY() - p0.getY());

                    Point2D.Double deltaPerp = new Point2D.Double(
                            Math.cos(Math.PI/2.0)*delta.getX() -  Math.sin(Math.PI/2.0)*delta.getY(),
                            Math.sin(Math.PI/2.0)*delta.getX() +  Math.cos(Math.PI/2.0)*delta.getY()
                    );


                    Point2D.Double p1 = new Point2D.Double(
                            p0.getX() +
                            deltaPerp.getX() * (k-1)/10.0
                            ,
                            p0.getY() +
                            deltaPerp.getY() * (k-1)/10.0);

                    Point2D.Double p2 = new Point2D.Double(
                            p3.getX() +
                            deltaPerp.getX() * (k-1)/10.0
                            ,
                            p3.getY() +
                            deltaPerp.getY() * (k-1)/10.0);

                    pw.println("newpath");
                    pw.println(String.format("%.4f %.4f moveto", p0.getX(), p0.getY()));
                    pw.println(String.format("%.4f %.4f %.4f %.4f %.4f %.4f curveto",
                                             p1.getX(),
                                             p1.getY(),
                                             p2.getX(),
                                             p2.getY(),
                                             p3.getX(),
                                             p3.getY()));

                    if (c == GemColor.green) pw.println("0 180 0 setrgbcolor");
                    else if (c == GemColor.red) pw.println("180 0 0 setrgbcolor");
                    else if (c == GemColor.blue) pw.println("0 0 180 setrgbcolor");
                    else if (c == GemColor.yellow) pw.println("180 180 0 setrgbcolor");
                    pw.println("stroke");

                }
            }

            for (GemVertex v : _gem.getVertices()) {
                Point2D.Double p = _map.get(v);
                pw.println("newpath");
                pw.println(String.format("%.4f %.4f moveto", p.getX(), p.getY()));
                pw.println(String.format("%.4f %.4f %.4f 0 360 arc", p.getX(), p.getY(), 0.06*CONVERSION));
                pw.println("closepath");
                pw.println("0 0 0 setrgbcolor");
                pw.println("fill");
            }



        }
     */


    private void tutteLayout(HashSet<RPoint> P, HashSet<RSegment> S, HashMap<RPoint,Vector> map) {

        // prepare point neighborhood
        HashMap<RPoint, PointNeighbourhood> mapN = new HashMap<RPoint, PointNeighbourhood>();
        for (RPoint p : P) {
            mapN.put(p, new PointNeighbourhood(p, map));
        }
        for (RSegment s : S) {
            RPoint a = s.getA();
            RPoint b = s.getB();
            PointNeighbourhood na = mapN.get(a);
            PointNeighbourhood nb = mapN.get(b);
            if (na != null)
                na.addNeighbour(b);
            if (nb != null)
                nb.addNeighbour(a);
        }

        // map to index the blink vertices
        HashMap<RPoint, Integer> mapV2I = new HashMap<RPoint, Integer>();

        // vertices list
        int i = 0;
        //System.out.println("Triball "+t);
        for (RPoint v : P) {
            // System.out.print(" "+v.getOriginalLabel());
            mapV2I.put(v, i++);
        }
        //System.out.println();

        // get largest face to be the external ball
        int n = P.size();

        // prepare Matrix
        DoubleMatrix2D X = new DenseDoubleMatrix2D(n, n);
        DoubleMatrix2D Y = new DenseDoubleMatrix2D(n, n);
        DoubleMatrix2D Bx = new DenseDoubleMatrix2D(n, 1);
        DoubleMatrix2D By = new DenseDoubleMatrix2D(n, 1);

        // prepare fixed values on system of equations
        i = 0;
        for (RPoint p : P) {
            Vector v = map.get(p);
            if (v == null)
                continue;

            int index = mapV2I.get(p);

            X.set(index, index, 1);
            Bx.set(index, 0, ((Float64) v.get(0)).doubleValue());

            Y.set(index, index, 1);
            By.set(index, 0, ((Float64) v.get(1)).doubleValue());

            i++;
        }

        // prepare other vertices
        for (RPoint p : P) {

            // vertice already considered
            if (map.get(p) != null)
                continue;

            int vi = mapV2I.get(p);

            //
            X.set(vi, vi, 1);
            Y.set(vi, vi, 1);
            Bx.set(vi, 0, 0);
            By.set(vi, 0, 0);

            PointNeighbourhood pn = mapN.get(p);
            ArrayList<RPoint> listN = pn.getNeighbours();
            int k = listN.size();

            for (RPoint pp: listN) {
                int vni = mapV2I.get(pp);
                X.set(vi, vni, -1.0 / k);
                Y.set(vi, vni, -1.0 / k);
            }
        }

        DoubleMatrix2D Rx = Algebra.DEFAULT.solve(X, Bx);
        DoubleMatrix2D Ry = Algebra.DEFAULT.solve(Y, By);

        for (RPoint p : P) {
            int vi = mapV2I.get(p);
            double x = Rx.get(vi, 0);
            double y = Ry.get(vi, 0);
            map.put(p,Vector.valueOf(new double[] {x,y}));
        }
    }

    private void printHeader(PrintWriter pw) {

        int W = (int) (CONVERSION * _width);
        int H = (int) (CONVERSION * _height);

        pw.println("%!PS-Adobe-3.0 EPSF-3.0");
        pw.println(String.format("%%%%BoundingBox: 0 0 %d %d", W, H));
        pw.println("/HelveticaItalic findfont dup length dict begin { 1 index /FID ne {def} {pop pop} ifelse} forall /Encoding ISOLatin1Encoding def currentdict end /HelveticaItalic-ISOLatin1 exch definefont pop"); // install ISOLatinEncoding
        // pw.println("0.01 setlinewidth");
        pw.println(String.format("0 %d translate", H));
        pw.println("1 -1 scale");
    }

    public void printFooter(PrintWriter pw) {
        pw.println("showpage");
        pw.println("%%EOF");
        pw.println("%%EndDocument");
    }
}
