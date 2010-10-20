package blink;

import java.awt.geom.Point2D;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

class DrawBlinkEPS {
    float CONVERSION = 72f / 2.54f;

    // all of these are in cm
    private double _pageWidth = 6;
    private double _pageHeight = 6;
    private double _cellWidth = 6;
    private double _cellHeight = 6;
    private double _cellMargin = 0.05;

    public DrawBlinkEPS(GBlink G, String fileName) throws SQLException, FileNotFoundException {
        PrintWriter pw = new PrintWriter(fileName);
        this.printHeader(pw);
        this.printBlink(pw, G);
        this.printFooter(pw);
        pw.close();
    }

    private void printBlink(PrintWriter pw, GBlink b) {

        double m = CONVERSION * _cellMargin;
        double w = CONVERSION*_cellWidth - 2*m;
        double h = CONVERSION*_cellHeight - 2*m;

        HashMap<GBlinkVertex, Point2D.Double> map = TuttesLayout.mapLayout(b,m,m,w,h);

        HashMap<Variable,Point2D.Double> mapVPos = new HashMap<Variable,Point2D.Double>();

        // translate
        pw.println("gsave");
        ArrayList<Variable> varFaces = b.getGVertices();
        for (Variable var: varFaces) {
            Point2D.Double p = new Point2D.Double(0,0);
            for (GBlinkVertex vv: var.getVertices()) {
                Point2D.Double pAux = map.get(vv);
                p.setLocation(p.getX()+pAux.getX(),p.getY()+pAux.getY());
            }
            p.setLocation(p.getX()/var.size(),p.getY()/var.size());
            mapVPos.put(var,p);
        }

        for (int i=1;i<=b.getNumberOfGEdges();i++) {
            GBlinkVertex va = b.findVertex((i-1)*4+1);
            GBlinkVertex vb = b.findVertex((i-1)*4+2);
            GBlinkVertex vc = b.findVertex((i-1)*4+3);
            GBlinkVertex vd = b.findVertex((i-1)*4+4);

            Point2D.Double p1 = mapVPos.get(b.findVariable(va,Variable.G_VERTICE));
            Point2D.Double p2 = new Point2D.Double(0,0);
            p2.setLocation((map.get(va).getX() + map.get(vb).getX()) / 2.0,
                           (map.get(va).getY() + map.get(vb).getY()) / 2.0);
            Point2D.Double p3 = new Point2D.Double(0,0);
            p3.setLocation((map.get(vc).getX() + map.get(vd).getX()) / 2.0,
                           (map.get(vc).getY() + map.get(vd).getY()) / 2.0);
            Point2D.Double p4 = mapVPos.get(b.findVariable(vc,Variable.G_VERTICE));

            pw.println("newpath");
            pw.println(String.format("%.4f %.4f moveto", p1.getX(), p1.getY()));
            pw.println(String.format("%.4f %.4f %.4f %.4f %.4f %.4f curveto",
                                     p2.getX(),
                                     p2.getY(),
                                     p3.getX(),
                                     p3.getY(),
                                     p4.getX(),
                                     p4.getY()));

            if (b.getColor(i) == BlinkColor.green)
                pw.println("0 180 0 setrgbcolor");
            else
                pw.println("180 0 0 setrgbcolor");
            pw.println("stroke");
        }

        // draw vertices as black dots
        for (Variable var: varFaces) {
            Point2D.Double p = mapVPos.get(var);
            pw.println("newpath");
            pw.println(String.format("%.4f %.4f moveto", p.getX(), p.getY()));
            pw.println(String.format("%.4f %.4f %.4f 0 360 arc", p.getX(), p.getY(), 0.03*CONVERSION));
            pw.println("closepath");
            pw.println("0 0 0 setrgbcolor");
            pw.println("fill");
        }
        pw.println("grestore");
    }

    private void printHeader(PrintWriter pw) {

        int W = (int) (CONVERSION * _pageWidth);
        int H = (int) (CONVERSION * _pageHeight);

        pw.println("%!PS-Adobe-3.0 EPSF-3.0");
        pw.println(String.format("%%%%BoundingBox: 0 0 %d %d", W, H));
        pw.println("/HelveticaItalic findfont dup length dict begin { 1 index /FID ne {def} {pop pop} ifelse} forall /Encoding ISOLatin1Encoding def currentdict end /HelveticaItalic-ISOLatin1 exch definefont pop"); // install ISOLatinEncoding
        pw.println("1 setlinewidth");
        pw.println(String.format("0 %d translate", H));
        pw.println("1 -1 scale");
    }

    public void printFooter(PrintWriter pw) {
        pw.println("showpage");
        pw.println("%%EOF");
        pw.println("%%EndDocument");
    }
}
