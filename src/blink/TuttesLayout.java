package blink;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;

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
public class TuttesLayout {

    public static HashMap<GemVertex,Point2D.Double> tutteLayout(Gem g, GemColor outColor,
                                                                double tx,
                                                                double ty,
                                                                double scaleX,
                                                                double scaleY) {
        HashMap<GemVertex,Point2D.Double> map = new HashMap<GemVertex,Point2D.Double>();

        // colors considered
        GemColor colors[] = GemColor.getColorsOfColorSet(
            GemColor.difference(GemColor.COLORSET_ALL_COLORS,
                                 GemColor.getColorSet(outColor)));

        //System.out.println("TUTTE LAYOUT");
        //System.out.println(g.getComponentRepository().toStringWithOriginalLabels());

        // triballs
        ArrayList<Component> triballs = g.getComponentRepository().
                                        getTriballsOn(colors[0], colors[1], colors[2]);

        //
        for (int t=0;t<triballs.size();t++) {
            Component triball = triballs.get(t);

            // System.out.println(""+triball.toStringWithOriginalLabels());

            ArrayList<GemVertex> listVertices = triball.getVertices();
            // ArrayList<GemVertex> listVertices = g.getVertices();

            HashMap<GemVertex,Integer> mapV2I = new HashMap<GemVertex,Integer>();

            //
            Component bigon = g.getComponentRepository().
                              getAnyLargestBigonWithoutColorAndWithVerticeIn(outColor,new HashSet<GemVertex>(listVertices));
            //Component bigon = g.getComponentRepository().
            //                  getAnyLargestBigonWithoutColor(outColor);

            // vertices list
            int i = 0;
            //System.out.println("Triball "+t);
            for (GemVertex v : listVertices) {
                // System.out.print(" "+v.getOriginalLabel());
                mapV2I.put(v, i++);
            }
            //System.out.println();

            ArrayList<GemVertex> bigonVertices = bigon.getVerticesFromBigon();
            /*
            System.out.println("Bigon");
            for (GemVertex v : bigonVertices) {
                System.out.print(" "+v.getOriginalLabel());
            }
            System.out.println();*/


            int n = listVertices.size();

            // prepare Matrix
            DoubleMatrix2D X = new DenseDoubleMatrix2D(n, n);
            DoubleMatrix2D Y = new DenseDoubleMatrix2D(n, n);
            DoubleMatrix2D Bx = new DenseDoubleMatrix2D(n, 1);
            DoubleMatrix2D By = new DenseDoubleMatrix2D(n, 1);

            // prepare fixed values on system of equations
            i = 0;
            for (GemVertex v : bigonVertices) {
                double theta = i * (Math.PI * 2) / bigonVertices.size();
                // System.out.println("Read: "+v.getOriginalLabel());
                int index = mapV2I.get(v);

                X.set(index, index, 1);
                Bx.set(index, 0, 0.5 * Math.cos(theta));

                Y.set(index, index, 1);
                By.set(index, 0, 0.5 * Math.sin(theta));

                i++;
            }

            // prepare other vertices
            for (GemVertex v : listVertices) {
                // vertice already considered
                if (bigonVertices.contains(v))
                    continue;

                int vi = mapV2I.get(v);

                //
                X.set(vi, vi, 1);
                Y.set(vi, vi, 1);
                Bx.set(vi, 0, 0);
                By.set(vi, 0, 0);

                for (GemColor cc : colors) {
                    GemVertex vn = v.getNeighbour(cc); // v neighbour
                    // System.out.println("Neighbour: "+vn.getOriginalLabel()+" by color "+cc);

                    int vni = mapV2I.get(vn);
                    X.set(vi, vni, -1.0 / 3.0);
                    Y.set(vi, vni, -1.0 / 3.0);
                }
            }

            DoubleMatrix2D Rx = Algebra.DEFAULT.solve(X, Bx);
            DoubleMatrix2D Ry = Algebra.DEFAULT.solve(Y, By);

            for (GemVertex v : listVertices) {
                int vi = mapV2I.get(v);
                double x = t*scaleX + 2*t*tx + tx + scaleX / 2.0 + Rx.get(vi, 0) * scaleX;
                double y = ty + scaleY / 2.0 + Ry.get(vi, 0) * scaleY;
                // System.out.println(String.format("%d %5.3f %5.3f",vi,x,y));
                map.put(v, new Point2D.Double(x, y));
            }
        }
        return map;
    }


    public static HashMap<GBlinkVertex, Point2D.Double> mapLayout(GBlink b,
                                                                 double tx,
                                                                 double ty,
                                                                 double scaleX,
                                                                 double scaleY) {



        HashMap<GBlinkVertex, Point2D.Double> map = new HashMap<GBlinkVertex, Point2D.Double>();

        // map to index the blink vertices
        HashMap<GBlinkVertex, Integer> mapV2I = new HashMap<GBlinkVertex, Integer>();

        // list of all vertices of the map
        ArrayList<GBlinkVertex> listVertices = b.getVertices();

        // vertices list
        int i = 0;
        //System.out.println("Triball "+t);
        for (GBlinkVertex v : listVertices) {
            // System.out.print(" "+v.getOriginalLabel());
            mapV2I.put(v, i++);
        }
        //System.out.println();

        // get largest face to be the external ball
        Variable extFace = b.getLargestFace();
        int n = listVertices.size();

        // prepare Matrix
        DoubleMatrix2D X = new DenseDoubleMatrix2D(n, n);
        DoubleMatrix2D Y = new DenseDoubleMatrix2D(n, n);
        DoubleMatrix2D Bx = new DenseDoubleMatrix2D(n, 1);
        DoubleMatrix2D By = new DenseDoubleMatrix2D(n, 1);

        // prepare fixed values on system of equations
        i = 0;
        for (GBlinkVertex v : extFace.getVertices()) {
            double theta = i * (Math.PI * 2) / extFace.size();
            // System.out.println("Read: "+v.getOriginalLabel());
            int index = mapV2I.get(v);

            X.set(index, index, 1);
            Bx.set(index, 0, 0.5 * Math.cos(theta));

            Y.set(index, index, 1);
            By.set(index, 0, 0.5 * Math.sin(theta));

            i++;
        }

        // prepare other vertices
        for (GBlinkVertex v : listVertices) {

            // vertice already considered
            if (extFace.contains(v))
                continue;

            int vi = mapV2I.get(v);

            //
            X.set(vi, vi, 1);
            Y.set(vi, vi, 1);
            Bx.set(vi, 0, 0);
            By.set(vi, 0, 0);

            for (GBlinkEdgeType t: new GBlinkEdgeType[] {GBlinkEdgeType.face,GBlinkEdgeType.vertex,GBlinkEdgeType.edge}) {
                int vni = mapV2I.get(v.getNeighbour(t));
                X.set(vi, vni, -1.0 / 3.0);
                Y.set(vi, vni, -1.0 / 3.0);
            }
        }

        DoubleMatrix2D Rx = Algebra.DEFAULT.solve(X, Bx);
        DoubleMatrix2D Ry = Algebra.DEFAULT.solve(Y, By);

        for (GBlinkVertex v : listVertices) {
            int vi = mapV2I.get(v);
            double x = tx + scaleX / 2.0 + Rx.get(vi, 0) * scaleX;
            double y = ty + scaleY / 2.0 + Ry.get(vi, 0) * scaleY;
            // System.out.println(String.format("%d %5.3f %5.3f",vi,x,y));
            map.put(v, new Point2D.Double(x, y));
        }

        return map;

    }




    public static HashMap<RefineMapVertex, Point2D.Double> refineMapLayout(RefineMap b,
                                                                 double tx,
                                                                 double ty,
                                                                 double scaleX,
                                                                 double scaleY) {

        HashMap<RefineMapVertex, Point2D.Double> map = new HashMap<RefineMapVertex, Point2D.Double>();

        // map to index the blink vertices
        HashMap<RefineMapVertex, Integer> mapV2I = new HashMap<RefineMapVertex, Integer>();

        // list of all vertices of the map
        ArrayList<RefineMapVertex> listVertices = b.getVertices();

        // vertices list
        int i = 0;
        //System.out.println("Triball "+t);
        for (RefineMapVertex v : listVertices) {
            // System.out.print(" "+v.getOriginalLabel());
            mapV2I.put(v, i++);
        }
        //System.out.println();

        // get largest face to be the external ball
        RefineMapFace extFace = b.getExternalFace();
        int n = listVertices.size();

        // prepare Matrix
        DoubleMatrix2D X = new DenseDoubleMatrix2D(n, n);
        DoubleMatrix2D Y = new DenseDoubleMatrix2D(n, n);
        DoubleMatrix2D Bx = new DenseDoubleMatrix2D(n, 1);
        DoubleMatrix2D By = new DenseDoubleMatrix2D(n, 1);

        // prepare fixed values on system of equations
        i = 0;
        for (RefineMapVertex v : extFace.getVertices()) {
            double theta = i * (Math.PI * 2) / extFace.size();
            // System.out.println("Read: "+v.getOriginalLabel());
            int index = mapV2I.get(v);

            X.set(index, index, 1);
            Bx.set(index, 0, 0.5 * Math.cos(theta));

            Y.set(index, index, 1);
            By.set(index, 0, 0.5 * Math.sin(theta));

            i++;
        }

        // prepare other vertices
        for (RefineMapVertex v : listVertices) {

            // vertice already considered
            if (extFace.contains(v))
                continue;

            int vi = mapV2I.get(v);

            //
            X.set(vi, vi, 1);
            Y.set(vi, vi, 1);
            Bx.set(vi, 0, 0);
            By.set(vi, 0, 0);

            for (RefineMapNeighbourType t : new RefineMapNeighbourType[] {RefineMapNeighbourType.face, RefineMapNeighbourType.vertex,
                 RefineMapNeighbourType.angle}) {
                int vni = mapV2I.get(v.getNeighbour(t));
                X.set(vi, vni, -1.0 / 3.0);
                Y.set(vi, vni, -1.0 / 3.0);
            }
        }

        DoubleMatrix2D Rx = Algebra.DEFAULT.solve(X, Bx);
        DoubleMatrix2D Ry = Algebra.DEFAULT.solve(Y, By);

        for (RefineMapVertex v : listVertices) {
            int vi = mapV2I.get(v);
            double x = tx + scaleX / 2.0 + Rx.get(vi, 0) * scaleX;
            double y = ty + scaleY / 2.0 + Ry.get(vi, 0) * scaleY;
            // System.out.println(String.format("%d %5.3f %5.3f",vi,x,y));
            map.put(v, new Point2D.Double(x, y));
        }

        return map;

    }











    public static void blinkEPS(PrintWriter pw, GBlink b) {
        float CONVERSION = 72f / 25.4f;

        int W = (int) (CONVERSION * 50);
        int H = (int) (CONVERSION * 50);

        pw.println("%!PS-Adobe-3.0 EPSF-3.0");
        pw.println(String.format("%%%%BoundingBox: 0 0 %d %d", W, H));
        pw.println("0.01 setlinewidth");
        // pw.println(String.format("0 %d translate", H));
        pw.println(String.format("%.4f %.4f scale", (float)W, (float)H));

        HashMap<GBlinkVertex, Point2D.Double> map = TuttesLayout.mapLayout(b,0,0,1,1);


        HashMap<Variable,Point2D.Double> mapVPos = new HashMap<Variable,Point2D.Double>();

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

            //pw.println(String.format("%.4f %.4f lineto", p2.getX(), p2.getY()));
            //pw.println(String.format("%.4f %.4f lineto", p3.getX(), p3.getY()));
            //pw.println(String.format("%.4f %.4f lineto", p4.getX(), p4.getY()));
            //pw.println(String.format("%.4f %.4f moveto", p1.getX(), p1.getY()));
            //pw.println(String.format("%.4f %.4f lineto", p2.getX(), p2.getY()));
            //pw.println(String.format("%.4f %.4f lineto", p3.getX(), p3.getY()));
            //pw.println(String.format("%.4f %.4f lineto", p4.getX(), p4.getY()));
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
            pw.println(String.format("%.4f %.4f %.4f 0 360 arc", p.getX(), p.getY(), 0.015f));
            pw.println("closepath");
            pw.println("0 0 0 setrgbcolor");
            pw.println("fill");
        }

        pw.println("showpage");
        pw.println("%%EOF");
        pw.println("%%EndDocument");
    }




    public static void main(String[] args) throws FileNotFoundException, SQLException {
        /*
        ArrayList<BlinkEntry> blinks = App.getRepositorio().getBlinks(8,9);
        for (BlinkEntry be: blinks) {
            PrintWriter pw = new PrintWriter("c:/blinks/"+be.get_id()+".eps");
            blinkEPS(pw,be.getBlink());
            pw.close();
        }*/

        new GenerateDocument3();
        // new GenerateBlinks();
        // new GenerateDocument2();

        //Blink b = new Blink(new int[]{6,16,2,18,4,10,5,11,8,14,9,17,12,1,13,3,15,7},0);
        // new GemToEPS(new Gem(2,3,1,1));
        //Gem g = b.getGem();
        // g.applyRhoPair(g.findAnyRho2Pair());
        // g.applyRhoPair(g.findAnyRho3Pair());
        // g.cancelDipole(g.findAnyDipole());
        //new GemToEPS(g);

        // desenhar o mapa
        //JFrame f = new JFrame("Reduction Graph");
        //f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //f.setSize(new Dimension(1024, 768));
        //f.setContentPane(new PanelBlinkViewer(b));
        //f.setVisible(true);
        // desenhar o mapa
        System.exit(0);
    }
}

class GenerateDocument {
    float CONVERSION = 72f / 2.54f;

    // all of these are in cm
    private double _pageWidth = 19;
    private double _pageHeight = 27;
    private double _cellWidth = 1.9;
    private double _cellHeight = 1.9;
    private double _cellMargin = 0.1;

    // map id to qi
    HashMap<Long,QI> _mapQIs = new HashMap<Long,QI>();
    HashMap<Long,GemEntry> _mapGems = new HashMap<Long,GemEntry>();

    public GenerateDocument() throws SQLException, FileNotFoundException {

        int columns = (int) (_pageWidth / _cellWidth);
        int rows = (int) (_pageHeight / _cellHeight);

        int page = 1;
        int i=0;
        int j=0;

        HashSet<Long> filter = new HashSet<Long>();
        try {
            // build map of gems
            ArrayList<GemEntry> gems = App.getRepositorio().getGems();
            for (GemEntry ge : gems) {
                _mapGems.put(ge.getId(), ge);
            }

            // build map of QIs
            ArrayList<QI> qis = App.getRepositorio().getQIs();
            for (QI qi : qis) {
                _mapQIs.put(qi.get_id(), qi);
            }

            for (QI qi : qis) {
                // if (!qi.allEntriesAreReal()) {
                if (qi.allEntriesAreReal() && !qi.allEntriesAreInteger()) {
                    filter.add(qi.get_id());
                }
            }
        } catch (ClassNotFoundException ex1) {
        } catch (IOException ex1) {
        } catch (SQLException ex1) {
        }

        ArrayList<ClassHGQI> list = App.getRepositorio().getHGQIClasses(App.MAX_EDGES);
        /*for (int ii = list.size() - 1; ii >= 0; ii--) {
            ClassHGQI c = list.get(ii);
            c.load();
            if (!filter.contains(c.get_qi()))
                list.remove(ii);
        };*/

        Collections.sort(list,new Comparator(){
            public int compare(Object a, Object b) {
                ClassHGQI ca = (ClassHGQI) a;
                ClassHGQI cb = (ClassHGQI) b;
                try {
                    return ca.getMaxCodeMinEdgesBlinkEntry().getBlink().compareTo(cb.getMaxCodeMinEdgesBlinkEntry().getBlink());
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    return 0;
                }
            }
        });

        //
        Comparator blinkComparator = new Comparator(){
            public int compare(Object a, Object b) {
                BlinkEntry ba = (BlinkEntry) a;
                BlinkEntry bb = (BlinkEntry) b;
                return (int)(ba.get_id() - bb.get_id());
            }
        };

        Iterator<ClassHGQI> classIterator = list.iterator();
        ClassHGQI C = null;
        Iterator<BlinkEntry> blinkIterator = null;

        // create first page
        PrintWriter pw = new PrintWriter(String.format("tex/page" + (page) + ".eps"));
        this.printHeader(pw);

        while (true) {
            System.out.println("Drawing page... "+page+" cell "+i+" "+j);

            if (C != null && blinkIterator.hasNext()) {
                BlinkEntry be = blinkIterator.next();

                // draw blink on cell
                this.printBlink(pw,be,i,j);

            }
            else if (classIterator.hasNext()) {
                // get next class write down class information on the cell

                C = classIterator.next();
                C.load();
                ArrayList<BlinkEntry> listBEs = new ArrayList<BlinkEntry>();
                listBEs.add(C.getMaxCodeMinEdgesBlinkEntry());
                Collections.sort(listBEs,blinkComparator);
                blinkIterator = listBEs.iterator();

                // draw class information
                this.printClass(pw,C,i,j);

            }
            else { // nothing else
                this.printFooter(pw);
                pw.close();
                break; // end here
            }

            // advance
            j = (j + 1) % columns;
            if (j == 0)
                i = (i + 1) % rows;
            if (i == 0 && j == 0) {
                this.printFooter(pw);
                pw.close();
                page++;
                pw = new PrintWriter(String.format("tex/page" + (page) + ".eps"));
                this.printHeader(pw);
            }
        }
    }

    private void printBlink(PrintWriter pw, BlinkEntry be, int row, int column) {

        GBlink b = be.getBlink();

        double m = CONVERSION * _cellMargin;
        double w = CONVERSION*_cellWidth - 2*m;
        double h = CONVERSION*_cellHeight - 2*m;

        HashMap<GBlinkVertex, Point2D.Double> map = TuttesLayout.mapLayout(b,m,m,w,h);

        HashMap<Variable,Point2D.Double> mapVPos = new HashMap<Variable,Point2D.Double>();

        pw.println("gsave");
        pw.println("/Helvetica findfont 8 scalefont setfont");
        pw.println(String.format("%.4f %.4f translate",
                                 column *_cellWidth*CONVERSION + 0.05*CONVERSION,
                                 row *_cellHeight * CONVERSION + 0.3*CONVERSION));
        pw.println("1 -1 scale 0 0 moveto");
        pw.println(String.format("(%s) show",
                                 "b"+be.get_id()+
                                 " g"+be.getMinGem()+
                                 " z"+be.getBlink().getNumberOfGZigZags()));
        pw.println("grestore");



        // translate
        pw.println("gsave");
        pw.println(String.format("%.4f %.4f translate",
                                 column*_cellWidth*CONVERSION,
                                 row*_cellHeight*CONVERSION));


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
            pw.println(String.format("%.4f %.4f %.4f 0 360 arc", p.getX(), p.getY(), 0.06*CONVERSION));
            pw.println("closepath");
            pw.println("0 0 0 setrgbcolor");
            pw.println("fill");
        }
        pw.println("grestore");

    }

    private void printClass(PrintWriter pw, ClassHGQI C, int row, int column) {
        pw.println("gsave");
        pw.println("/Helvetica findfont 8 scalefont setfont");
        pw.println(String.format("%.4f %.4f translate",
                                 column *_cellWidth*CONVERSION + 0.3*CONVERSION,
                                 row *_cellHeight * CONVERSION + 0.3*CONVERSION));
        pw.println("1 -1 scale 0 0 moveto");
        pw.println(String.format("(%s) show", "HG: " + C.get_hg()));
        pw.println(String.format("0 %.4f moveto",-0.3*CONVERSION));
        pw.println(String.format("(%s) show","QI: "+C.get_qi()));
        pw.println(String.format("0 %.4f moveto", -0.6 * CONVERSION));
        pw.println(String.format("(%s) show","Elements: "+C.get_numElements()));
        pw.println(String.format("0 %.4f moveto", -0.9 * CONVERSION));
        pw.println(String.format("(%s) show","TS-gem:"));
        pw.println(String.format("0 %.4f moveto", -1.2 * CONVERSION));
        pw.println(String.format("(%s) show",C.getMinGemCodes()));
        pw.println(String.format("0 %.4f moveto", -1.5 * CONVERSION));
        pw.println(String.format("(%s) show","qi <= "+_mapQIs.get(C.get_qi()).get_rmax()));
        pw.println("grestore");
    }

    private void printHeader(PrintWriter pw) {

        int W = (int) (CONVERSION * _pageWidth);
        int H = (int) (CONVERSION * _pageHeight);

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




class GenerateDocument2 {
    float CONVERSION = 72f / 2.54f;

    // all of these are in cm
    private double _pageWidth = 19;
    private double _pageHeight = 27;
    private double _cellWidth = 1.9;
    private double _cellHeight = 1.9;
    private double _cellMargin = 0.1;

    // map id to qi
    HashMap<Long,QI> _mapQIs = new HashMap<Long,QI>();
    HashMap<Long,GemEntry> _mapGems = new HashMap<Long,GemEntry>();

    public GenerateDocument2() throws SQLException, FileNotFoundException {

        int columns = (int) (_pageWidth / _cellWidth);
        int rows = (int) (_pageHeight / _cellHeight);

        int page = 1;
        int i=0;
        int j=0;

        HashMap<Long,Manifold> map = new HashMap<Long,Manifold>();
        try {
            // build map of gems
            ArrayList<BlinkEntry> gems = App.getRepositorio().getBlinks(1,8);
            for (BlinkEntry be : gems) {
                long gemId = be.getMinGem();
                Manifold M = map.get(gemId);
                if (M == null) {
                    M = new Manifold(gemId);
                    map.put(gemId,M);

                }
                M.add(be);
            }

            // build map of QIs
            ArrayList<QI> qis = App.getRepositorio().getQIs();
            for (QI qi : qis) {
                _mapQIs.put(qi.get_id(), qi);
            }
        } catch (ClassNotFoundException ex) {
        } catch (IOException ex) {
        } catch (SQLException ex) {
        }

        ArrayList<Manifold> list = new ArrayList<Manifold>(map.values());

        for (int ii=list.size()-1;ii>=0;ii--) {
            Manifold m = list.get(ii);
            try {
                GemEntry ge = App.getRepositorio().getGemById(m.getGemId());
                if (ge.getHandleNumber() > 0) {
                    list.remove(ii);
                }
            } catch (ClassNotFoundException ex1) {
            } catch (IOException ex1) {
            } catch (SQLException ex1) {
            }
        }

        Collections.sort(list,new Comparator(){
            public int compare(Object a, Object b) {
                Manifold ca = (Manifold) a;
                Manifold cb = (Manifold) b;
                return ca.getMaxCodeMinEdgesBlinkEntry().getBlink().compareTo(cb.getMaxCodeMinEdgesBlinkEntry().
                                                                              getBlink());
            }
        });

        for (int ii=0;ii<list.size();ii++) {
            Manifold m = list.get(ii);
            m.setNumber(ii+1);
        }



        Iterator<Manifold> classIterator = list.iterator();
        Manifold M = null;
        Iterator<BlinkEntry> blinkIterator = null;

        // create first page
        PrintWriter pw = new PrintWriter(String.format("tex/page" + (page) + ".eps"));
        this.printHeader(pw);

        while (true) {
            System.out.println("Drawing page... "+page+" cell "+i+" "+j);

            if (M != null && blinkIterator.hasNext()) {
                BlinkEntry be = blinkIterator.next();

                // draw blink on cell
                this.printBlink(pw,be,i,j);

            }
            else if (classIterator.hasNext()) {
                // get next class write down class information on the cell

                M = classIterator.next();
                ArrayList<BlinkEntry> listBEs = new ArrayList<BlinkEntry>();
                listBEs.add(M.getMaxCodeMinEdgesBlinkEntry());
                blinkIterator = listBEs.iterator();

                // draw class information
                this.printClass(pw,M,i,j);

            }
            else { // nothing else
                this.printFooter(pw);
                pw.close();
                break; // end here
            }

            // advance
            j = (j + 1) % columns;
            if (j == 0)
                i = (i + 1) % rows;
            if (i == 0 && j == 0) {
                this.printFooter(pw);
                pw.close();
                page++;
                pw = new PrintWriter(String.format("tex/page" + (page) + ".eps"));
                this.printHeader(pw);
            }
        }
    }

    private void printBlink(PrintWriter pw, BlinkEntry be, int row, int column) {

        GBlink b = be.getBlink();

        double m = CONVERSION * _cellMargin;
        double w = CONVERSION*_cellWidth - 2*m;
        double h = CONVERSION*_cellHeight - 2*m;

        HashMap<GBlinkVertex, Point2D.Double> map = TuttesLayout.mapLayout(b,m,m,w,h);

        HashMap<Variable,Point2D.Double> mapVPos = new HashMap<Variable,Point2D.Double>();

        pw.println("gsave");
        pw.println("/Helvetica findfont 8 scalefont setfont");
        pw.println(String.format("%.4f %.4f translate",
                                 column *_cellWidth*CONVERSION + 0.05*CONVERSION,
                                 row *_cellHeight * CONVERSION + 0.3*CONVERSION));
        pw.println("1 -1 scale 0 0 moveto");
        pw.println(String.format("(%s) show",
                                 "b"+be.get_id()+
                                 " g"+be.getMinGem()+
                                 " z"+be.getBlink().getNumberOfGZigZags()));
        pw.println("grestore");



        // translate
        pw.println("gsave");
        pw.println(String.format("%.4f %.4f translate",
                                 column*_cellWidth*CONVERSION,
                                 row*_cellHeight*CONVERSION));


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
            pw.println(String.format("%.4f %.4f %.4f 0 360 arc", p.getX(), p.getY(), 0.06*CONVERSION));
            pw.println("closepath");
            pw.println("0 0 0 setrgbcolor");
            pw.println("fill");
        }
        pw.println("grestore");

    }

    private void printClass(PrintWriter pw, Manifold M, int row, int column) {
        pw.println("gsave");
        pw.println("/Helvetica findfont 8 scalefont setfont");
        pw.println(String.format("%.4f %.4f translate",
                                 column *_cellWidth*CONVERSION + 0.3*CONVERSION,
                                 row *_cellHeight * CONVERSION + 0.3*CONVERSION));
        pw.println("1 -1 scale 0 0 moveto");
        pw.println(String.format("(%s) show", M.getNumber()+" HG: " + M.get_hg()));
        pw.println(String.format("0 %.4f moveto",-0.3*CONVERSION));
        pw.println(String.format("(%s) show","QI: "+M.get_qi()));
        pw.println(String.format("0 %.4f moveto", -0.6 * CONVERSION));
        pw.println(String.format("(%s) show","Elements: "+M.get_numElements()));
        pw.println(String.format("0 %.4f moveto", -0.9 * CONVERSION));
        pw.println(String.format("(%s) show","TS-gem:"));
        pw.println(String.format("0 %.4f moveto", -1.2 * CONVERSION));
        pw.println(String.format("(%s) show",M.getMinGemCodes()));
        pw.println(String.format("0 %.4f moveto", -1.5 * CONVERSION));
        pw.println(String.format("(%s) show","qi <= "+_mapQIs.get(M.get_qi()).get_rmax()));
        pw.println("grestore");
    }

    private void printHeader(PrintWriter pw) {

        int W = (int) (CONVERSION * _pageWidth);
        int H = (int) (CONVERSION * _pageHeight);

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

class GenerateDocument3 {
    float CONVERSION = 72f / 2.54f;

    // all of these are in cm
    private double _pageWidth = 14.3;
    private double _pageHeight = 17.6;
    private double _cellWidth = 1.1;
    private double _cellHeight = 1.1;
    private double _cellMargin = 0.1;

    // map id to qi
    HashMap<Long,QI> _mapQIs = new HashMap<Long,QI>();
    HashMap<Long,GemEntry> _mapGems = new HashMap<Long,GemEntry>();

    public GenerateDocument3() throws SQLException, FileNotFoundException {

        int columns = (int) (_pageWidth / _cellWidth);
        int rows = (int) (_pageHeight / _cellHeight);

        int page = 1;
        int i=0;
        int j=0;

        HashMap<Long,Manifold> map = new HashMap<Long,Manifold>();
        try {
            // build map of gems
            ArrayList<BlinkEntry> gems = App.getRepositorio().getBlinks(1,8);
            for (BlinkEntry be : gems) {
                long gemId = be.getMinGem();
                Manifold M = map.get(gemId);
                if (M == null) {
                    M = new Manifold(gemId);
                    map.put(gemId,M);

                }
                M.add(be);
            }

            // build map of QIs
            ArrayList<QI> qis = App.getRepositorio().getQIs();
            for (QI qi : qis) {
                _mapQIs.put(qi.get_id(), qi);
            }
        } catch (ClassNotFoundException ex) {
        } catch (IOException ex) {
        } catch (SQLException ex) {
        }

        ArrayList<Manifold> list = new ArrayList<Manifold>(map.values());

        for (int ii=list.size()-1;ii>=0;ii--) {
            Manifold m = list.get(ii);
            try {
                GemEntry ge = App.getRepositorio().getGemById(m.getGemId());
                if (ge.getHandleNumber() > 0) {
                    list.remove(ii);
                }
            } catch (ClassNotFoundException ex1) {
            } catch (IOException ex1) {
            } catch (SQLException ex1) {
            }
        }

        Collections.sort(list,new Comparator(){
            public int compare(Object a, Object b) {
                Manifold ca = (Manifold) a;
                Manifold cb = (Manifold) b;
                return ca.getMaxCodeMinEdgesBlinkEntry().getBlink().compareTo(cb.getMaxCodeMinEdgesBlinkEntry().
                                                                              getBlink());
            }
        });

        for (int ii=0;ii<list.size();ii++) {
            Manifold m = list.get(ii);
            m.setNumber(ii+1);
        }



        Iterator<Manifold> classIterator = list.iterator();
        Manifold M = null;
        Iterator<BlinkEntry> blinkIterator = null;

        // create first page
        PrintWriter pw = new PrintWriter(String.format("tex/page" + (page) + ".eps"));
        this.printHeader(pw);

        while (true) {
            System.out.println("Drawing page... "+page+" cell "+i+" "+j);

            boolean advance =true;

            if (M != null && blinkIterator.hasNext()) {
                BlinkEntry be = blinkIterator.next();

                // draw blink on cell
                this.printBlink(pw,be,i,j);

            }
            else if (classIterator.hasNext()) {
                // get next class write down class information on the cell

                M = classIterator.next();
                ArrayList<BlinkEntry> listBEs = new ArrayList<BlinkEntry>();
                listBEs.add(M.getMaxCodeMinEdgesBlinkEntry());
                blinkIterator = listBEs.iterator();

                // draw class information
                advance = false;
                //this.printClass(pw,M,i,j);

            }
            else { // nothing else
                this.printFooter(pw);
                pw.close();
                break; // end here
            }

            // advance
            if (advance) {
                j = (j + 1) % columns;
                if (j == 0)
                    i = (i + 1) % rows;
                if (i == 0 && j == 0) {
                    this.printFooter(pw);
                    pw.close();
                    page++;
                    pw = new PrintWriter(String.format("tex/page" + (page) + ".eps"));
                    this.printHeader(pw);
                }
            }
        }
    }

    private void printBlink(PrintWriter pw, BlinkEntry be, int row, int column) {

        GBlink b = be.getBlink();

        double m = CONVERSION * _cellMargin;
        double w = CONVERSION*_cellWidth - 2*m;
        double h = CONVERSION*_cellHeight - 2*m;

        HashMap<GBlinkVertex, Point2D.Double> map = TuttesLayout.mapLayout(b,m,m,w,h);

        HashMap<Variable,Point2D.Double> mapVPos = new HashMap<Variable,Point2D.Double>();

        /*
        pw.println("gsave");
        pw.println("/Helvetica findfont 8 scalefont setfont");
        pw.println(String.format("%.4f %.4f translate",
                                 column *_cellWidth*CONVERSION + 0.05*CONVERSION,
                                 row *_cellHeight * CONVERSION + 0.3*CONVERSION));
        pw.println("1 -1 scale 0 0 moveto");
        pw.println(String.format("(%s) show",
                                 "b"+be.get_id()+
                                 " g"+be.getMinGem()+
                                 " z"+be.getBlink().getNumberOfZigZags()));
        pw.println("grestore");*/



        // translate
        pw.println("gsave");
        pw.println(String.format("%.4f %.4f translate",
                                 column*_cellWidth*CONVERSION,
                                 row*_cellHeight*CONVERSION));


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
            pw.println(String.format("%.4f %.4f %.4f 0 360 arc", p.getX(), p.getY(), 0.04*CONVERSION));
            pw.println("closepath");
            pw.println("0 0 0 setrgbcolor");
            pw.println("fill");
        }
        pw.println("grestore");

    }

    private void printClass(PrintWriter pw, Manifold M, int row, int column) {
        pw.println("gsave");
        pw.println("/Helvetica findfont 8 scalefont setfont");
        pw.println(String.format("%.4f %.4f translate",
                                 column *_cellWidth*CONVERSION + 0.3*CONVERSION,
                                 row *_cellHeight * CONVERSION + 0.3*CONVERSION));
        pw.println("1 -1 scale 0 0 moveto");
        pw.println(String.format("(%s) show", M.getNumber()+" HG: " + M.get_hg()));
        pw.println(String.format("0 %.4f moveto",-0.3*CONVERSION));
        pw.println(String.format("(%s) show","QI: "+M.get_qi()));
        pw.println(String.format("0 %.4f moveto", -0.6 * CONVERSION));
        pw.println(String.format("(%s) show","Elements: "+M.get_numElements()));
        pw.println(String.format("0 %.4f moveto", -0.9 * CONVERSION));
        pw.println(String.format("(%s) show","TS-gem:"));
        pw.println(String.format("0 %.4f moveto", -1.2 * CONVERSION));
        pw.println(String.format("(%s) show",M.getMinGemCodes()));
        pw.println(String.format("0 %.4f moveto", -1.5 * CONVERSION));
        pw.println(String.format("(%s) show","qi <= "+_mapQIs.get(M.get_qi()).get_rmax()));
        pw.println("grestore");
    }

    private void printHeader(PrintWriter pw) {

        int W = (int) (CONVERSION * _pageWidth);
        int H = (int) (CONVERSION * _pageHeight);

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

class GenerateBlinks {
    float CONVERSION = 72f / 2.54f;

    // all of these are in cm
    private double _pageWidth = 1;
    private double _pageHeight = 1;
    private double _cellWidth = 1;
    private double _cellHeight = 1;
    private double _cellMargin = 0.05;

    public GenerateBlinks() throws SQLException, FileNotFoundException {
        ArrayList<BlinkEntry> blinks = App.getRepositorio().getBlinks(1,8);

        // create first page
        for (BlinkEntry be: blinks) {
            PrintWriter pw = new PrintWriter(String.format("eps/" + be.get_id() + ".eps"));
            this.printHeader(pw);
            this.printBlink(pw,be,0,0);
            this.printFooter(pw);
            pw.close();
        }
    }

    private void printBlink(PrintWriter pw, BlinkEntry be, int row, int column) {

        GBlink b = be.getBlink();

        double m = CONVERSION * _cellMargin;
        double w = CONVERSION*_cellWidth - 2*m;
        double h = CONVERSION*_cellHeight - 2*m;

        HashMap<GBlinkVertex, Point2D.Double> map = TuttesLayout.mapLayout(b,m,m,w,h);

        HashMap<Variable,Point2D.Double> mapVPos = new HashMap<Variable,Point2D.Double>();

        /*
        pw.println("gsave");
        pw.println("/Helvetica findfont 8 scalefont setfont");
        pw.println(String.format("%.4f %.4f translate",
                                 column *_cellWidth*CONVERSION + 0.05*CONVERSION,
                                 row *_cellHeight * CONVERSION + 0.3*CONVERSION));
        pw.println("1 -1 scale 0 0 moveto");
        pw.println(String.format("(%s) show",
                                 "b"+be.get_id()+
                                 " g"+be.getMinGem()+
                                 " z"+be.getBlink().getNumberOfZigZags()));
        pw.println("grestore");*/



        // translate
        pw.println("gsave");
        pw.println(String.format("%.4f %.4f translate",
                                 column*_cellWidth*CONVERSION,
                                 row*_cellHeight*CONVERSION));


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

class GemToEPS {
    private double CONVERSION       = 72f / 2.54f;
    private double MARK_SEPARATION  = 0.75; // in points
    private double HALF_MARK_LENGTH = 1.4;    // in points
    private double VERTEX_RADIUS    = 1.4;    // in points
    private double LINE_WIDTH       = 0.3;  // in points

    // all of these are in cm
    private double _width = 5;
    private double _height = 5;
    private double _margin = 0.25;
    private boolean _colors = false;
    private GemColor _specialColor = GemColor.yellow;


    private Gem _gem;

    HashMap<GemVertex, Point2D.Double> _map;

    public GemToEPS(Gem g, File file) throws FileNotFoundException {
        _gem = g;

        // create first page
        PrintWriter pw = new PrintWriter(file);
        this.printHeader(pw);
        drawGem(pw);
        this.printFooter(pw);
        pw.close();
    }

    private void drawGem(PrintWriter pw) {

        // the coordinates of the vertices of the gem
        // comes in points (72 points = 2.54 cm)
        _map = TuttesLayout.tutteLayout(
            _gem,
            _specialColor,
            CONVERSION * _margin,
            CONVERSION * _margin,
            CONVERSION * (_width - 2*_margin),
            CONVERSION * (_height - 2*_margin));

        // only process these colors
        GemColor[] colors = GemColor.getComplementColors(_specialColor);

        // prepare connections
        ArrayList<Connection> connections = new ArrayList<Connection>();
        for (GemVertex v: _gem.getVertices()) {
            for (GemColor c: colors) {
                GemVertex u = v.getNeighbour(c);
                Connection con = null;
                for (Connection icon: connections) {
                    if (icon.connects(u,v)) {
                        con = icon;
                        break;
                    }
                }
                if (con == null) {
                    con = new Connection(v,u);
                    connections.add(con);
                }
                con.addColor(c);
            }
        }

        // connection
        for (Connection con: connections) {
            for (ConnectionArc a: con.getConnectionArcs()) {
                a.printArc(pw);
            }
        }

        //
        /*
        HashSet<GemVertex> processedVertices = new HashSet<GemVertex>();
        for (GemColor c: colors) {

            processedVertices.clear();

            for (GemVertex v : _gem.getVertices()) {

                GemVertex u = v.getNeighbour(c);

                if (processedVertices.contains(u))
                    continue;

                processedVertices.add(v);

                Point2D.Double p0 = _map.get(v);
                Point2D.Double p1 = _map.get(u);

                pw.println("newpath");
                pw.println(String.format("%.4f %.4f moveto", p0.getX(), p0.getY()));
                pw.println(String.format("%.4f %.4f lineto", p1.getX(), p1.getY()));

                // setup color
                if (_colors) {
                    if (c == GemColor.green) pw.println("0 180 0 setrgbcolor");
                    else if (c == GemColor.red) pw.println("180 0 0 setrgbcolor");
                    else if (c == GemColor.blue) pw.println("0 0 180 setrgbcolor");
                    else if (c == GemColor.yellow) pw.println("180 180 0 setrgbcolor");
                }
                else {
                    pw.println("0 0 0 setrgbcolor");
                }
                pw.println("stroke");

                // if no color, put the marks
                if (!_colors) {
                    Point2D.Double pm =  scale(add(p0,p1),0.5);
                    Point2D.Double v1 =  sub(p1,p0);
                    v1 = scale(v1,1.0/modulus(v1));
                    // System.out.println("Modulus v1 "+modulus(v1));
                    Point2D.Double v2 =  perp(v1);
                    // System.out.println("Modulus v2 "+modulus(v2));

                    int marks = c.getNumber();
                    double segLength = (marks-1)*MARK_SEPARATION;

                    Point2D.Double pm0 = add(pm,scale(v1,-segLength/2));

                    for (int i=0;i<marks;i++) {
                        Point2D.Double pmi = add(pm0,scale(v1,i*MARK_SEPARATION));
                        Point2D.Double pmia = add(pmi,scale(v2, HALF_MARK_LENGTH));
                        Point2D.Double pmib = add(pmi,scale(v2,-HALF_MARK_LENGTH));

                        pw.println("newpath");
                        pw.println(String.format("%.4f %.4f moveto", pmia.getX(), pmia.getY()));
                        pw.println(String.format("%.4f %.4f lineto", pmib.getX(), pmib.getY()));
                        pw.println("stroke");
                    }
                }
            }
        }*/

        for (GemVertex v : _gem.getVertices()) {
            Point2D.Double p = _map.get(v);
            pw.println("newpath");
            pw.println(String.format("%.4f %.4f moveto", p.getX(), p.getY()));
            pw.println(String.format("%.4f %.4f %.4f 0 360 arc", p.getX(), p.getY(), VERTEX_RADIUS));
            pw.println("closepath");
            pw.println("0 0 0 setrgbcolor");
            pw.println("fill");
        }
    }

    private Point2D.Double scale(Point2D.Double p, double s) {
        return new Point2D.Double(p.getX()*s,p.getY()*s);
    }

    private Point2D.Double add(Point2D.Double p, Point2D.Double q) {
        return new Point2D.Double(p.getX()+q.getX(),p.getY()+q.getY());
    }

    private Point2D.Double perp(Point2D.Double p) {
        return new Point2D.Double(-p.getY(),p.getX());
    }

    private Point2D.Double sub(Point2D.Double p, Point2D.Double q) {
        return new Point2D.Double(p.getX()-q.getX(),p.getY()-q.getY());
    }

    private double modulus(Point2D.Double p) {
        return p.distance(0,0);
    }

    private void printHeader(PrintWriter pw) {

        int W = (int) (CONVERSION * _width);
        int H = (int) (CONVERSION * _height);

        pw.println("%!PS-Adobe-3.0 EPSF-3.0");
        pw.println(String.format("%%%%BoundingBox: 0 0 %d %d", W, H));
        pw.println("/HelveticaItalic findfont dup length dict begin { 1 index /FID ne {def} {pop pop} ifelse} forall /Encoding ISOLatin1Encoding def currentdict end /HelveticaItalic-ISOLatin1 exch definefont pop"); // install ISOLatinEncoding
        pw.println(String.format("%.4f setlinewidth",LINE_WIDTH));
        pw.println(String.format("0 %d translate", H));
        pw.println("1 -1 scale");
    }

    public void printFooter(PrintWriter pw) {
        pw.println("showpage");
        pw.println("%%EOF");
        pw.println("%%EndDocument");
    }

    class Connection {
        private double SEPARATION_ARCS = 7.5;
        GemVertex _u;
        GemVertex _v;
        ArrayList<GemColor> _colors = new ArrayList<GemColor>(1);
        ArrayList<ConnectionArc> _arcs = null;
        public Connection(GemVertex u, GemVertex v) {
            _u = u;
            _v = v;
        }
        public boolean connects(GemVertex uu, GemVertex vv) {
            return ((_u == uu) && (_v == vv)) || ((_u == vv) && (_v == uu));
        }
        public GemVertex getU() { return _u; }
        public GemVertex getV() { return _v; }
        public Point2D.Double getPosU() { return _map.get(_u); }
        public Point2D.Double getPosV() { return _map.get(_v); }
        public void addColor(GemColor c) {
            if (!_colors.contains(c))
                _colors.add(c);
        }
        public void prepareConnectionArcs() {
            if (_arcs == null) {
                _arcs = new ArrayList<ConnectionArc>(1);
                Point2D.Double p0 = this.getPosU();
                Point2D.Double p3 = this.getPosV();
                if (_colors.size() == 0) {
                    throw new RuntimeException();
                } else if (_colors.size() == 1) {
                    _arcs.add(new ConnectionArc(this,_colors.get(0)));
                } else {
                    Point2D.Double p0p3 = sub(p3, p0);
                    p0p3 = scale(p0p3, 1.0 / modulus(p0p3));
                    Point2D.Double p0p3perp = perp(p0p3);

                    int arcs = _colors.size();
                    double length = (arcs-1) * SEPARATION_ARCS;

                    Point2D.Double p10 = add(p0, scale(p0p3perp, -length / 2));
                    Point2D.Double p20 = add(p3, scale(p0p3perp, -length / 2));

                    for (int i = 0; i < arcs; i++) {
                        Point2D.Double p1i = add(p10, scale(p0p3perp, i * SEPARATION_ARCS));
                        Point2D.Double p2i = add(p20, scale(p0p3perp, i * SEPARATION_ARCS));
                        _arcs.add(new ConnectionArc(this,_colors.get(i), p1i, p2i));
                    }
                }
            }
        }
        public int getNumberOfConnectionArcs() {
            this.prepareConnectionArcs();
            return _arcs.size();
        }
        public ConnectionArc getConnectionArc(int index) {
            this.prepareConnectionArcs();
            return _arcs.get(index);
        }
        public ArrayList<ConnectionArc> getConnectionArcs() {
            this.prepareConnectionArcs();
            return (ArrayList<ConnectionArc>) _arcs.clone();
        }
    }

    class ConnectionArc {
        Connection _connection;
        private Point2D.Double _p1;
        private Point2D.Double _p2;
        GemColor _color;
        public ConnectionArc(Connection connection, GemColor color) {
            this(connection,color,null,null);
        }
        public ConnectionArc(Connection connection, GemColor color, Point2D.Double p1, Point2D.Double p2) {
            _connection = connection;
            _color = color;
            _p1 = p1;
            _p2 = p2; // bezier control points
            prepareGeometry();
        }
        public boolean isLinear() {
            return _p1 == null;
        }
        public double length(double t0, double t1, int segments) {
            double segLength = (t1-t0)/segments;
            double result = 0;
            for (int i=0;i<segments;i++) {
                Point2D.Double pi = getCurvePoint(t0+i*segLength);
                Point2D.Double pim1 = getCurvePoint(t0+(i+1)*segLength);
                result += pi.distance(pim1);
            }
            return result;
        }
        public double binarySearch(double t0, double distance, int segments) {
            double l,r;
            if (distance < 0) {
                l = 0;
                r = t0;
                while (-distance > length(t0,l,segments))
                    l = (l-1)*10;
            }
            else if (distance > 0) {
                l = t0;
                r = 1;
                while (distance > length(r,t0,segments))
                    r = 10 * r;
            }
            else return t0;

            double absDistance = Math.abs(distance);
            double tResult = 0;
            while (true) {
                double m = (l+r)/2.0;
                double currDistance = length(m,t0,segments);
                if (Math.abs(currDistance - absDistance) < 1.0e-4) {
                    tResult = m;
                    break;
                }
                if (distance > 0) {
                    if (currDistance < absDistance)
                        l = m;
                    else
                        r = m;
                }
                else {
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
        private double ax,ay,bx,by,cx,cy;
        private void prepareGeometry() {
            // prepare vectors
            p = new Point2D.Double[] {_connection.getPosU(), _p1, _p2, _connection.getPosV()};
            if (!this.isLinear()) {
                x = new double[] { p[0].getX(), p[1].getX(), p[2].getX(), p[3].getX() };
                y = new double[] { p[0].getY(), p[1].getY(), p[2].getY(), p[3].getY() };
                cx = 3*x[1] - 3*x[0];
                cy = 3*y[1] - 3*y[0];
                bx = 3*x[2] - 6*x[1] + 3*x[0];
                by = 3*y[2] - 6*y[1] + 3*y[0];
                ax = x[3] - x[0] + 3*x[1] - 3*x[2];
                ay = y[3] - y[0] + 3*y[1] - 3*y[2];
            }
            else {
                x = new double[] { p[0].getX(), p[0].getX(), p[0].getX(), p[3].getX() };
                y = new double[] { p[0].getY(), p[0].getX(), p[0].getX(), p[3].getY() };
                ax = ay = bx = by = 0;
                cx = x[3] - x[0];
                cy = y[3] - y[0];
            }
        }

        public Point2D.Double getCurvePoint(double t) {
            return new Point2D.Double(ax*t*t*t + bx*t*t + cx*t + x[0], ay*t*t*t + by*t*t + cy*t + y[0]);
        }

        public Point2D.Double getCurveTangent(double t) {
            return new Point2D.Double(3*ax*t*t + 2*bx*t + cx, 3*ay*t*t + 2*by*t + cy);
        }

        public void printArc(PrintWriter pw) {

            // setup color
            if (_colors) {
                if (_color == GemColor.green) pw.println("0 180 0 setrgbcolor");
                else if (_color == GemColor.red) pw.println("180 0 0 setrgbcolor");
                else if (_color == GemColor.blue) pw.println("0 0 180 setrgbcolor");
                else if (_color == GemColor.yellow) pw.println("180 180 0 setrgbcolor");
            }
            else {
                pw.println("0 0 0 setrgbcolor");
            }

            // draw the arc segment
            pw.println("newpath");
            pw.println(String.format("%.4f %.4f moveto", x[0], y[0]));
            if (isLinear()) {
                pw.println(String.format("%.4f %.4f lineto", x[3], y[3]));
            }
            else {
                pw.println(String.format("%.4f %.4f %.4f %.4f %.4f %.4f curveto", x[1], y[1] ,x[2], y[2], x[3], y[3]));
            }
            pw.println("stroke");

            // if no color, put the marks
            if (!_colors) {
                int marks = _color.getNumber();
                double segLength = (marks-1)*MARK_SEPARATION;
                for (int i=0;i<marks;i++) {
                    double distance = -segLength/2.0+i*MARK_SEPARATION;
                    double t = binarySearch(0.5,distance,10);
                    System.out.println("distance "+length(0.5,t,10)+" should be "+distance);
                    Point2D.Double pi = this.getCurvePoint(t);
                    Point2D.Double ti = this.getCurveTangent(t);
                    ti=scale(ti,1/modulus(ti));
                    ti=perp(ti);

                    Point2D.Double pia = add(pi,scale(ti, HALF_MARK_LENGTH));
                    Point2D.Double pib = add(pi,scale(ti,-HALF_MARK_LENGTH));

                    pw.println("newpath");
                    pw.println(String.format("%.4f %.4f moveto", pia.getX(), pia.getY()));
                    pw.println(String.format("%.4f %.4f lineto", pib.getX(), pib.getY()));
                    pw.println("stroke");
                }
            }
        }
    }
}







