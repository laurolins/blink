package blink;

import java.io.PrintWriter;
import java.util.HashSet;

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
public class DXF {
    public DXF() {
    }
    public static void printDXFHeader(PrintWriter pw) {
        pw.println(" 0");
        pw.println("SECTION");
        pw.println(" 2");
        pw.println("ENTITIES");
    }

    public static void printDXFFooter(PrintWriter pw) {
        pw.println(" 0");
        pw.println("ENDSEC");
        pw.println(" 0");
        pw.println("EOF");
    }

    public static void printDXFTablesSection(PrintWriter pw) {
        pw.println("  0");
        pw.println("SECTION");
        pw.println("  2");
        pw.println("TABLES");
        pw.println("  0");
        pw.println("TABLE");
        pw.println("  2");
        pw.println("LAYER");
        pw.println("  70");
        pw.println("4");
        pw.println("  0");
        pw.println("LAYER");
        pw.println("  2");
        pw.println("PATH");
        pw.println("  70");
        pw.println("0");
        pw.println("  62");
        pw.println("1");
        pw.println("  6");
        pw.println("CONTINUOUS");
        pw.println("  0");
        pw.println("LAYER");
        pw.println("  2");
        pw.println("ARC");
        pw.println("  70");
        pw.println("0");
        pw.println("  62");
        pw.println("7");
        pw.println("  6");
        pw.println("CONTINUOUS");
        pw.println("  0");
        pw.println("LAYER");
        pw.println("  2");
        pw.println("INTERNAL");
        pw.println("  70");
        pw.println("0");
        pw.println("  62");
        pw.println("2");
        pw.println("  6");
        pw.println("CONTINUOUS");
        pw.println("  0");
        pw.println("LAYER");
        pw.println("  2");
        pw.println("NORMAL");
        pw.println("  70");
        pw.println("0");
        pw.println("  62");
        pw.println("6");
        pw.println("  6");
        pw.println("CONTINUOUS");
        pw.println("  0");
        pw.println("ENDTAB");
        pw.println("  0");
        pw.println("ENDSEC");
    }

    public static void printSegmentDXF(PrintWriter pw, RSegment s, String layer) {
        RPoint a = s.getA();
        RPoint b = s.getB();
        pw.println(" 0");
        pw.println("LINE");
        pw.println(" 8");
        pw.println(layer);
        pw.println(" 10");
        pw.println(a.getX().toStringDot(5));
        pw.println(" 20");
        pw.println(a.getY().toStringDot(5));
        pw.println(" 30");
        pw.println(a.getZ().toStringDot(5));
        pw.println(" 11");
        pw.println(b.getX().toStringDot(5));
        pw.println(" 21");
        pw.println(b.getY().toStringDot(5));
        pw.println(" 31");
        pw.println(b.getZ().toStringDot(5));
    }

    public static void printDXF(PrintWriter pw, RFace f1, RFace f2) {
        pw.println(" 0");
        pw.println("SECTION");
        pw.println(" 2");
        pw.println("ENTITIES");


        HashSet<RSegment> S = new HashSet<RSegment>();
        HashSet<RTriangle> T = new HashSet<RTriangle>();
        S.addAll(f1.getArcSegments());
        S.addAll(f2.getArcSegments());
        T.addAll(f1.getTriangles());
        T.addAll(f2.getTriangles());

        for (RSegment s: S) {
            RPoint a = s.getA();
            RPoint b = s.getB();

            pw.println(" 0");
            pw.println("LINE");
            pw.println(" 8");
            pw.println("A12");
            pw.println(" 10");
            pw.println(a.getX().toStringDot(5));
            pw.println(" 20");
            pw.println(a.getY().toStringDot(5));
            pw.println(" 30");
            pw.println(a.getZ().toStringDot(5));
            pw.println(" 11");
            pw.println(b.getX().toStringDot(5));
            pw.println(" 21");
            pw.println(b.getY().toStringDot(5));
            pw.println(" 31");
            pw.println(b.getZ().toStringDot(5));
        }

        for (RTriangle t: T) {
            pw.println(" 0");
            pw.println("3DFACE");
            pw.println(" 8");
            pw.println("TRIANGULOS");
            pw.println(" 10");
            pw.println(t.getA().getX().toStringDot(5));
            pw.println(" 20");
            pw.println(t.getA().getY().toStringDot(5));
            pw.println(" 30");
            pw.println(t.getA().getZ().toStringDot(5));
            pw.println(" 11");
            pw.println(t.getB().getX().toStringDot(5));
            pw.println(" 21");
            pw.println(t.getB().getY().toStringDot(5));
            pw.println(" 31");
            pw.println(t.getB().getZ().toStringDot(5));
            pw.println(" 12");
            pw.println(t.getC().getX().toStringDot(5));
            pw.println(" 22");
            pw.println(t.getC().getY().toStringDot(5));
            pw.println(" 32");
            pw.println(t.getC().getZ().toStringDot(5));
            pw.println(" 13");
            pw.println(t.getC().getX().toStringDot(5));
            pw.println(" 23");
            pw.println(t.getC().getY().toStringDot(5));
            pw.println(" 33");
            pw.println(t.getC().getZ().toStringDot(5));
        }

        pw.println(" 0");
        pw.println("ENDSEC");
        pw.println(" 0");
        pw.println("EOF");
    }
    public static void printDXFLayersTableEnd(PrintWriter pw) {
        pw.println("  0");
        pw.println("ENDTAB");
        pw.println("  0");
        pw.println("ENDSEC");
    }

    public static void printDXFTetrahedronLayerInfo(PrintWriter pw, RTetrahedron t) {

        String lbl = "T"+t.getLabel();
        String layerNames[] = {lbl+"A",lbl+"B",lbl+"C",lbl+"D",     // face layer
                               lbl+"AN",lbl+"BN",lbl+"CN",lbl+"DN", // normal layer
                               lbl+"AP",lbl+"BP",lbl+"CP",lbl+"DP",  // path layers
                               lbl+"ACP",lbl+"ADP",lbl+"BCP",lbl+"BDP"  // path layers
        };
        int layerColors[] = {2,5,1,3,
                             4,4,4,4,
                             2,5,1,3,
                             2,5,1,3};

        for (int i=0;i<layerNames.length;i++) {
            pw.println("  0");
            pw.println("LAYER");
            pw.println("  2");
            pw.println(layerNames[i]);
            pw.println("  70");
            pw.println("0");
            pw.println("  62");
            pw.println(""+layerColors[i]);
            pw.println("  6");
            pw.println("CONTINUOUS");
        }
    }
    public static void printDXFTetrahedronFace(PrintWriter pw, RTetrahedron tet) {
        for (GemColor c: new GemColor[] {GemColor.yellow,GemColor.blue,GemColor.red,GemColor.green}) {
            RFace f = tet.getFace(c);
            for (RTriangle t : f.getTriangles()) {
                RPoint p[] = {t.getA(),t.getB(),t.getC(),t.getC()};
                pw.println(" 0");
                pw.println("3DFACE");
                pw.println(" 8");
                pw.println("T"+tet.getLabel()+GemColor.getColorsCompactStringABCD(c));
                for (int i=0;i<4;i++) {
                    pw.println(" " + (10 + i));
                    pw.println(p[i].getX().toStringDot(5));
                    pw.println(" " + (20 + i));
                    pw.println(p[i].getY().toStringDot(5));
                    pw.println(" " + (30 + i));
                    pw.println(p[i].getZ().toStringDot(5));
                }

                // print normal segment
                RPoint normal = t.getNormal().approxNormalize();
                if (tet.invertNormal(c))
                    normal.invert();
                normal = normal.scale(new BigRational(GemEmbedding._U));
                printSegmentDXF(pw,new RSegment(t.getMiddle(),t.getMiddle().add(normal)),"T"+tet.getLabel()+GemColor.getColorsCompactStringABCD(c)+"N");
            }
        }
    }

    public static void printDXFLayersTableBegin(PrintWriter pw, int numLayers) {
        pw.println("  0");
        pw.println("SECTION");
        pw.println("  2");
        pw.println("TABLES");
        pw.println("  0");
        pw.println("TABLE");
        pw.println("  2");
        pw.println("LAYER");
        pw.println("  70");
        pw.println(""+numLayers);
    }



}
