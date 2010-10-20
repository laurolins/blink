package blink;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.PrintWriter;
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
public class EPSLibrary {
    public EPSLibrary() {
    }

    /**
     * The lengths are given in points.
     */
    public static void printBlink(PrintWriter pw, GBlink G, double x0, double y0, double ww, double hh, double margin) {

        double topMargin = margin; // space to info
        double leftMargin = margin;
        double bottomMargin = margin; // space to info
        double rightMargin = margin;
        double w = ww - leftMargin - rightMargin;
        double h = hh - topMargin - bottomMargin;

        HashMap<GBlinkVertex, Point2D.Double> map = TuttesLayout.mapLayout(G, 0, 0, 1, 1);

        HashMap<Variable, Point2D.Double> mapVPos = new HashMap<Variable, Point2D.Double>();

        AffineTransform T = new AffineTransform();
        T.translate(x0+leftMargin,y0+hh-topMargin);
        T.scale(w,-h);

        // translate
        pw.println("gsave");
        // pw.println(String.format("%.4f %.4f translate", x0, y0));

        ArrayList<Variable> varVertices = G.getGVertices();
        for (Variable var : varVertices) {
            Point2D.Double p = new Point2D.Double(0, 0);
            for (GBlinkVertex vv : var.getVertices()) {
                Point2D.Double pAux = map.get(vv);
                T.transform(pAux,pAux);
                p.setLocation(p.getX() + pAux.getX(), p.getY() + pAux.getY());
            }
            if (var.size() == 0) {
                p.setLocation(0.5,0.5);
                T.transform(p,p);
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

            pw.println("newpath");
            pw.println(String.format("%.4f %.4f moveto", p1.getX(), p1.getY()));
            pw.println(String.format("%.4f %.4f %.4f %.4f %.4f %.4f curveto",
                                     p2.getX(),
                                     p2.getY(),
                                     p3.getX(),
                                     p3.getY(),
                                     p4.getX(),
                                     p4.getY()));

            if (G.getColor(i) == BlinkColor.green)
                pw.println("0 180 0 setrgbcolor");
            else
                pw.println("180 0 0 setrgbcolor");
            pw.println("stroke");
        }

        // draw vertices as black dots
        for (Variable var : varVertices) {
            Point2D.Double p = mapVPos.get(var);
            pw.println("newpath");
            // pw.println(String.format("%.4f %.4f moveto", p.getX(), p.getY()));
            pw.println(String.format("%.4f %.4f %.4f 0 360 arc", p.getX(), p.getY(), 1.0));
            pw.println("closepath");
            pw.println("0 0 0 setrgbcolor");
            pw.println("fill");
        }
        pw.println("grestore");

    }

    /**
     * header
     */
    public static void printHeader(PrintWriter pw, double pageWidth, double pageHeight) {
        int W = (int) (pageWidth);
        int H = (int) (pageHeight);

        pw.println("%!PS-Adobe-3.0 EPSF-3.0");
        pw.println(String.format("%%%%BoundingBox: -2 -2 %d %d", W+4, H+4));

        pw.println("/FSD {findfont exch scalefont def} bind def");
        pw.println("/SMS {setfont moveto show} bind def");
        pw.println("/MS {moveto show} bind def");

        int fontSizes[] = {3,4,5,6,7,8,9,10,11,12,14,16};
        for (int fs: fontSizes) {
            pw.println("/Helvetica"+fs+" "+fs+" /Helvetica FSD");
            pw.println("/Times"+fs+" "+fs+" /Times FSD");
            pw.println("/Courier"+fs+" "+fs+" /Courier FSD");
            pw.println("/Symbol"+fs+" "+fs+" /Symbol FSD");
            pw.println("/Helvetica-Oblique"+fs+" "+fs+" /Helvetica-Oblique FSD");
            pw.println("/Times-Oblique"+fs+" "+fs+" /Times-Oblique FSD");
            pw.println("/Courier-Oblique"+fs+" "+fs+" /Courier-Oblique FSD");
            pw.println("/Helvetica-Bold"+fs+" "+fs+" /Helvetica-Bold FSD");
            pw.println("/Times-Bold"+fs+" "+fs+" /Times-Bold FSD");
            pw.println("/Courier-Bold"+fs+" "+fs+" /Courier-Bold FSD");
        }

        // pw.println("0.01 setlinewidth");
        pw.println(String.format("0 %d translate", H));
        pw.println("1 -1 scale");
    }

    /**
     * footer
     */
   public static void printFooter(PrintWriter pw) {
        pw.println("showpage");
        pw.println("%%EOF");
        pw.println("%%EndDocument");
    }

}
