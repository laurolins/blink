package blink;

import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.StringTokenizer;

import linsoft.Pair;
import linsoft.graph.PlanarRepresentation;
import sun.audio.AudioPlayer;
import sun.audio.AudioStream;

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
public class Library {
    public Library() {
    }
    public static String intArrayToString(int ... array) {
        StringBuffer sb = new StringBuffer();
        for (int i=0;i<array.length;i++) {
            sb.append(array[i]);
            if (i < array.length-1)
                sb.append(",");
        }
        return sb.toString();
    }
    public static int[] arrayListToArray(ArrayList<Integer> list) {
        int[] result = new int[list.size()];
        int i=0;
        for (int x: list) {
            result[i++] = x;
        }
        return result;
    }
    public static int[] stringToIntArray(String s, String sep) {
        StringTokenizer st = new StringTokenizer(s,sep);
        int[] result = new int[st.countTokens()];
        int i=0;
        while (st.hasMoreTokens()) {
            result[i++] =  Integer.parseInt(st.nextToken());
        }
        return result;
    }

    /**
     * Takes a 001000111000
     * @param colorsSt String
     * @return int[]
     */
    public static int[] setOfIntegersFromSetBinaryString(String colorsSt) {
        ArrayList<Integer> list = new ArrayList<Integer>();
        for (int i = 0; i < colorsSt.length(); i++) {
            int d = Integer.parseInt("" + colorsSt.charAt(i));
            if (d == 1)
                list.add(i+1);
        }
        int[] elements = new int[list.size()];
        int k = 0;
        for (int i: list)
            elements[k++] = i;

        return elements;
    }


    public static String collectionToString(Collection list, char separator) {
        StringBuffer sb = new StringBuffer();
        boolean first = true;
        for (Object o: list) {
            if (!first)
                sb.append(separator);
            sb.append(o);
            first=false;
        }
        return sb.toString();
    }

    /**
     * Create
     */
    public static String fillStringWithChar(String input, int length, char fillChar, boolean leftAlignment) {
        String fill = "";
        for (int i=0;i<length-input.length();i++)
            fill+=""+fillChar;
        if (leftAlignment)
            return fill+input;
        else
            return input+fill;
    }

    public static void playSound(String fileName, long waitPeriod) {
        try {
            InputStream in = new FileInputStream(fileName);
            AudioStream as = new AudioStream(in);
            AudioPlayer.player.start(as);
            Thread.sleep(waitPeriod);
            AudioPlayer.player.stop(as);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void main(String[] args) {
        playSound("solucao.wav",2000);
    }

    public static String getLinkInGMLFormat(GBlink G) {
        StringBuffer sb = new StringBuffer();
        sb.append("graph [\n");
        sb.append("   # gblink: "+G.getBlinkWord().toString()+"\n");
        sb.append("   directed 0\n");

        for (int i=1;i<=G.getNumberOfGEdges();i++) {
            sb.append("   node [ id "+i+" label \""+i+"\" ]\n");
        }

        for (GBlinkVertex v: G.getVertices()) {
            if (v.hasEvenLabel())
                continue;
            GBlinkVertex u = v.getNeighbour(GBlinkEdgeType.edge);
            int lblU = (int) Math.min(u.getEdgeLabel(),v.getEdgeLabel());
            int lblV = (int) Math.max(u.getEdgeLabel(),v.getEdgeLabel());
            sb.append("   edge [ source "+lblU+" target "+lblV+" ]\n");
        }

        sb.append("]\n");
        return sb.toString();
    }

    public static String getGBlinkInGMLFormat(GBlink G) {
        StringBuffer sb = new StringBuffer();
        sb.append("graph [\n");
        sb.append("   # gblink: "+G.getBlinkWord().toString()+"\n");
        sb.append("   directed 0\n");


        HashMap<GBlinkVertex,Point2D.Double> map = TuttesLayout.mapLayout(G,0,0,200,200);

        for (GBlinkVertex v: G.getVertices()) {
            sb.append(String.format("   node [ id %d label \"%s\" graphics [ x %.4f y %.4f ] ]\n",
                                    v.getLabel(),
                                    ""+v.getLabel(),
                                    map.get(v).getX(),
                                    map.get(v).getY()
                      ));
        }


        for (GBlinkVertex v: G.getVertices()) {
            if (v.hasEvenLabel())
                continue;
            GBlinkVertex u = v.getNeighbour(GBlinkEdgeType.edge);
            int lblU = (int) Math.min(u.getLabel(),v.getLabel());
            int lblV = (int) Math.max(u.getLabel(),v.getLabel());
            sb.append("   edge [ source "+lblU+" target "+lblV+" ]\n");

            u = v.getNeighbour(GBlinkEdgeType.face);
            lblU = (int) Math.min(u.getLabel(),v.getLabel());
            lblV = (int) Math.max(u.getLabel(),v.getLabel());
            sb.append("   edge [ source "+lblU+" target "+lblV+" ]\n");

            u = v.getNeighbour(GBlinkEdgeType.vertex);
            lblU = (int) Math.min(u.getLabel(),v.getLabel());
            lblV = (int) Math.max(u.getLabel(),v.getLabel());
            sb.append("   edge [ source "+lblU+" target "+lblV+" ]\n");
        }

        sb.append("]\n");
        return sb.toString();
    }

    public static String getBlinkInGMLFormat(GBlink G) {
        StringBuffer sb = new StringBuffer();
        sb.append("graph [\n");
        sb.append("   # gblink: "+G.getBlinkWord().toString()+"\n");
        sb.append("   directed 0\n");


        double width = 300;
        double height = 300;

        HashMap<GBlinkVertex,Point2D.Double> mapPositions = TuttesLayout.mapLayout(G,0,0,width,height);

        HashMap<Variable,Point2D.Double> mapVertices = new HashMap<Variable,Point2D.Double>();

        ArrayList<Variable> varVertices = G.getGVertices();
        for (Variable var : varVertices) {
            Point2D.Double p = new Point2D.Double(0, 0);
            for (GBlinkVertex vv : var.getVertices()) {
                Point2D.Double pAux = mapPositions.get(vv);
                p.setLocation(pAux.getX(),pAux.getY());
            }
            if (var.size() == 0) {
                p.setLocation(width/2.0, height/2.0);
            } else {
                p.setLocation(width / var.size(), height / var.size());
            }
            mapVertices.put(var, p);
        }


        HashMap<GBlinkVertex,Integer> map = new HashMap<GBlinkVertex,Integer>();
        int i = 1;
        for (Variable f: varVertices) {
            Point2D.Double p = mapVertices.get(f);
            sb.append(String.format("   node [ id %d label \"%s\" graphics [ x %.4f y %.4f ] ]\n",
                                    i,
                                    ""+i,
                                    p.getX(),
                                    p.getY()
                      ));
            // sb.append("   node [ id "+i+" label \""+i+"\" ]\n");
            for (GBlinkVertex v: f.getVertices()) {
                map.put(v,i);
            }
            i++;
        }

        for (Variable e: G.getGEdges()) {
            GBlinkVertex u = e.getVertex(0);
            GBlinkVertex v = e.getVertex(2);
            int lblU = (int) Math.min(map.get(u),map.get(v));
            int lblV = (int) Math.max(map.get(u),map.get(v));
            sb.append("   edge [ source "+lblU+" target "+lblV+" ]\n");
        }
        sb.append("]\n");
        return sb.toString();
    }


    public static PlanarRepresentation getLinkPlanarRepresentation(GBlink G, int face) {

        PlanarRepresentation P = new PlanarRepresentation();

        HashMap<GBlinkVertex,Integer> mapVertices = new HashMap<GBlinkVertex,Integer>();
        int idVertex = 1;
        for (Variable gEdge: G.getGEdges()) {
            P.newVertex(idVertex,gEdge.getVertex(0).getVertexAtTheSameGEdgeWithMinLabel());
            for (GBlinkVertex v : gEdge.getVertices())
                mapVertices.put(v, idVertex);
            idVertex++;
        }

        HashMap<Pair,Integer> mapEdges = new HashMap<Pair,Integer>();
        int idEdge = 1;
        for (GBlinkVertex u: G.getVertices()) {
            if (u.hasEvenLabel())
                continue;
            GBlinkVertex v = u.getNeighbour(GBlinkEdgeType.edge);
            int vu = mapVertices.get(u);
            int vv = mapVertices.get(v);
            Pair p = new Pair(u,v);
            P.newEdge(idEdge,p,vu,vv);
            mapEdges.put(p,idEdge);
            idEdge++;
        }

        // now the gVertex
        int idFace = 1;
        for (Variable gVertex: G.getGVertices()) {
            P.newFace(idFace,gVertex);
            ArrayList<GBlinkVertex> vertices = gVertex.getVertices();
            int n = vertices.size();
            GBlinkEdgeType t = GBlinkEdgeType.edge;
            for (int i=0;i<vertices.size();i++) {
                if (t == GBlinkEdgeType.edge) {
                    GBlinkVertex u = vertices.get(i);
                    GBlinkVertex v = vertices.get((i + 1) % n);
                    boolean positive = true;
                    Integer edgeId = mapEdges.get(new Pair(u, v));
                    if (edgeId == null) {
                        edgeId = mapEdges.get(new Pair(v, u));
                        positive = false;
                    }
                    P.addEdgeToFace(idFace, edgeId, positive);
                }
                t = (t == GBlinkEdgeType.edge ? GBlinkEdgeType.face : GBlinkEdgeType.edge);
            }
            idFace++;
        }

        // now the gVertex
        ArrayList<Variable> gFaces = G.getGFaces();
        for (int k=0;k<gFaces.size();k++) {
            Variable gFace = gFaces.get(k);
            P.newFace(idFace,gFace);
            ArrayList<GBlinkVertex> vertices = gFace.getVertices();
            int n = vertices.size();
            GBlinkEdgeType t = GBlinkEdgeType.vertex;
            for (int i=0;i<vertices.size();i++) {
                if (t == GBlinkEdgeType.edge) {
                    GBlinkVertex u = vertices.get(i);
                    GBlinkVertex v = vertices.get((i + 1) % n);
                    boolean positive = true;
                    Integer edgeId = mapEdges.get(new Pair(u, v));
                    if (edgeId == null) {
                        edgeId = mapEdges.get(new Pair(v, u));
                        positive = false;
                    }
                    P.addEdgeToFace(idFace, edgeId, positive);
                }
                t = (t == GBlinkEdgeType.edge ? GBlinkEdgeType.vertex : GBlinkEdgeType.edge);
            }
            if (G.getLargestFace() == gFace)
                P.setExternalFace(idFace);

            if (face < 0 && G.getLargestFace() == gFace)
                P.setExternalFace(idFace);
            else if (k == (face % gFaces.size()))
                P.setExternalFace(idFace);

            idFace++;
        }

        return P;

    }

    public static PlanarRepresentation getGBlinkPlanarRepresentation(GBlink G, int face) {

        PlanarRepresentation P = new PlanarRepresentation();

        HashMap<GBlinkVertex,Integer> mapVertices = new HashMap<GBlinkVertex,Integer>();
        int idVertex = 1;
        for (GBlinkVertex v: G.getVertices()) {
            P.newVertex(idVertex,v);
            mapVertices.put(v, idVertex);
            idVertex++;
        }

        HashMap<Pair,Integer> mapEdges = new HashMap<Pair,Integer>();
        int idEdge = 1;
        for (GBlinkVertex u: G.getVertices()) {
            if (u.hasEvenLabel())
                continue;

            { // edge
                GBlinkVertex v = u.getNeighbour(GBlinkEdgeType.edge);
                int vu = mapVertices.get(u);
                int vv = mapVertices.get(v);
                Pair p = new Pair(u, GBlinkEdgeType.edge);
                P.newEdge(idEdge, p, vu, vv);
                mapEdges.put(p, idEdge);
                idEdge++;
            }

            { // edge
                GBlinkVertex v = u.getNeighbour(GBlinkEdgeType.face);
                int vu = mapVertices.get(u);
                int vv = mapVertices.get(v);
                Pair p = new Pair(u, GBlinkEdgeType.face);
                P.newEdge(idEdge, p, vu, vv);
                mapEdges.put(p, idEdge);
                idEdge++;
            }

            { // vertex
                GBlinkVertex v = u.getNeighbour(GBlinkEdgeType.vertex);
                int vu = mapVertices.get(u);
                int vv = mapVertices.get(v);
                Pair p = new Pair(u, GBlinkEdgeType.vertex);
                P.newEdge(idEdge, p, vu, vv);
                mapEdges.put(p, idEdge);
                idEdge++;
            }
            idEdge++;
        }

        // now the gVertex
        int idFace = 1;
        for (Variable gVertex: G.getGVertices()) {
            P.newFace(idFace,gVertex);
            ArrayList<GBlinkVertex> vertices = gVertex.getVertices();
            int n = vertices.size();
            GBlinkEdgeType t = GBlinkEdgeType.edge;
            for (int i=0;i<vertices.size();i++) {
                GBlinkVertex u = vertices.get(i);
                GBlinkVertex v = vertices.get((i + 1) % n);
                boolean positive = true;
                Integer edgeId = mapEdges.get(new Pair(u, t));
                if (edgeId == null) {
                    edgeId = mapEdges.get(new Pair(v, t));
                    positive = false;
                }
                P.addEdgeToFace(idFace, edgeId, positive);
                t = (t == GBlinkEdgeType.edge ? GBlinkEdgeType.face : GBlinkEdgeType.edge);
            }
            idFace++;
        }

        // now the gVertex
        ArrayList<Variable> gFaces = G.getGFaces();
        for (int k=0;k<gFaces.size();k++) {
            Variable gFace = gFaces.get(k);
            P.newFace(idFace,gFace);
            ArrayList<GBlinkVertex> vertices = gFace.getVertices();
            int n = vertices.size();
            GBlinkEdgeType t = GBlinkEdgeType.vertex;
            for (int i=0;i<vertices.size();i++) {
                GBlinkVertex u = vertices.get(i);
                GBlinkVertex v = vertices.get((i + 1) % n);
                boolean positive = true;
                Integer edgeId = mapEdges.get(new Pair(u, t));
                if (edgeId == null) {
                    edgeId = mapEdges.get(new Pair(v, t));
                    positive = false;
                }
                P.addEdgeToFace(idFace, edgeId, positive);
                t = (t == GBlinkEdgeType.edge ? GBlinkEdgeType.vertex : GBlinkEdgeType.edge);
            }
            if (G.getLargestFace() == gFace)
                P.setExternalFace(idFace);

            if (face < 0 && G.getLargestFace() == gFace)
                P.setExternalFace(idFace);
            else if (k == (face % gFaces.size()))
                P.setExternalFace(idFace);

            idFace++;
        }

        // now the gEdge
        for (Variable gEdge: G.getGEdges()) {
            P.newFace(idFace,gEdge);
            ArrayList<GBlinkVertex> vertices = gEdge.getVertices();
            int n = vertices.size();
            GBlinkEdgeType t = GBlinkEdgeType.face;
            for (int i=0;i<vertices.size();i++) {
                GBlinkVertex u = vertices.get(i);
                GBlinkVertex v = vertices.get((i + 1) % n);
                boolean positive = true;
                Integer edgeId = mapEdges.get(new Pair(u, t));
                if (edgeId == null) {
                    edgeId = mapEdges.get(new Pair(v, t));
                    positive = false;
                }
                P.addEdgeToFace(idFace, edgeId, positive);
                t = (t == GBlinkEdgeType.face ? GBlinkEdgeType.vertex : GBlinkEdgeType.face);
            }
            idFace++;
        }

        return P;

    }

    public static PlanarRepresentation getBlinkPlanarRepresentation(GBlink G, int face) {

        PlanarRepresentation P = new PlanarRepresentation();

        HashMap<GBlinkVertex,Integer> mapVertices = new HashMap<GBlinkVertex,Integer>();
        int idVertex = 1;
        for (Variable gVertex: G.getGVertices()) {
            P.newVertex(idVertex,gVertex.getVertex(0));
            for (GBlinkVertex v : gVertex.getVertices())
                mapVertices.put(v, idVertex);
            idVertex++;
        }

        HashMap<Pair,Integer> mapEdges = new HashMap<Pair,Integer>();
        int idEdge = 1;
        for (Variable gEdge: G.getGEdges()) {
            GBlinkVertex u = gEdge.getVertex(0);
            if (u.hasEvenLabel())
                u = u.getNeighbour(GBlinkEdgeType.face);
            GBlinkVertex v = u.getNeighbour(GBlinkEdgeType.diagonal);
            if (u.getLabel() > v.getLabel()) {
                GBlinkVertex aux = u;
                u = v;
                v = aux;
            }
            int vu = mapVertices.get(u);
            int vv = mapVertices.get(v);
            Pair p = new Pair(u,v);
            P.newEdge(idEdge,p,vu,vv);
            mapEdges.put(p,idEdge);
            idEdge++;
        }

        // now the gVertex
        int idFace = 1;
        ArrayList<Variable> gFaces = G.getGFaces();
        for (int k=0;k<gFaces.size();k++) {
            Variable gFace = gFaces.get(k);
            P.newFace(idFace,gFace.getVertex(0));
            ArrayList<GBlinkVertex> vertices = gFace.getVertices();
            int n = vertices.size();
            GBlinkEdgeType t = GBlinkEdgeType.vertex;
            for (int i=0;i<vertices.size();i++) {
                if (t == GBlinkEdgeType.vertex) {
                    GBlinkVertex u = vertices.get(i);
                    GBlinkVertex v = vertices.get((i + 1) % n);
                    if (u.hasEvenLabel()) u = u.getNeighbour(GBlinkEdgeType.face);
                    if (v.hasEvenLabel()) v = v.getNeighbour(GBlinkEdgeType.face);
                    boolean positive = true;
                    Integer edgeId = mapEdges.get(new Pair(u, v));
                    if (edgeId == null) {
                        edgeId = mapEdges.get(new Pair(v, u));
                        positive = false;
                    }
                    P.addEdgeToFace(idFace, edgeId, positive);
                }
                t = (t == GBlinkEdgeType.vertex ? GBlinkEdgeType.edge : GBlinkEdgeType.vertex);
            }

            if (face < 0 && G.getLargestFace() == gFace)
                P.setExternalFace(idFace);
            else if (k == (face % gFaces.size()))
                P.setExternalFace(idFace);

            idFace++;
        }

        //P.setExternalFace(1);

        return P;

    }

    public static GeneralPath smooth(GeneralPath p) {
        PathIterator iterator = p.getPathIterator(new AffineTransform());
        GeneralPath output = new GeneralPath();
        double points[] = {0,0,0,0,0,0};
        double current[] = {0,0,0,0,0,0};
        int acum = 0;
        while (!iterator.isDone()) {
            int code = iterator.currentSegment(current);
            if (code == PathIterator.SEG_MOVETO) {
                output.moveTo((float)current[0],(float)current[1]);
            }
            else if (code == PathIterator.SEG_LINETO) {
                points[2*acum] = current[0];
                points[2*acum+1] = current[1];
                acum++;
                if (acum == 3) {
                    output.curveTo((float)points[0],(float)points[1],(float)points[2],
                                   (float)points[3],(float)points[4],(float)points[5]);
                    acum = 0;
                }
            }
            else throw new RuntimeException();
            iterator.next();
        }
        if (acum == 1) {
            output.lineTo((float)points[0],(float)points[1]);
        }
        else if (acum == 2) {
            output.quadTo((float)points[0],(float)points[1],(float)points[2],(float)points[3]);
        }
        else if (acum == 3) {
            output.curveTo((float)points[0],(float)points[1],
                           (float)points[2],(float)points[3],
                           (float)points[4],(float)points[5]);
        }
        return output;
    }


    public static GeneralPath smooth2(GeneralPath p) {
        PathIterator iterator = p.getPathIterator(new AffineTransform());
        ArrayList<Double> points = new ArrayList<Double>();

        double current[] = {0,0,0,0,0,0};
        int n = 0;
        while (!iterator.isDone()) {
            int code = iterator.currentSegment(current);
            if (code == PathIterator.SEG_MOVETO) {
                points.add(current[0]);
                points.add(current[1]);
                // System.out.println(String.format("moveto: %9.4f %9.4f",current[0],current[1]));

                n++;
            }
            else if (code == PathIterator.SEG_LINETO) {
                double lastX = points.get(points.size()-2);
                double lastY = points.get(points.size()-1);
                points.add((lastX+current[0])/2.0);
                points.add((lastY+current[1])/2.0);
                points.add(current[0]);
                points.add(current[1]);
                // System.out.println(String.format("lineto: %9.4f %9.4f",current[0],current[1]));
                n+=2;
            }
            else throw new RuntimeException();
            iterator.next();
        }

        GeneralPath output = new GeneralPath();
        output.moveTo(points.get(0).floatValue(),points.get(1).floatValue());
        if (n == 3) {
            output.lineTo(points.get(4).floatValue(),points.get(5).floatValue());
        }
        else {
            output.lineTo(points.get(2).floatValue(),points.get(3).floatValue());
            for (int i = 1; i < n-2; i+=2) {
                double x1 = points.get(2*(i+1));
                double y1 = points.get(2*(i+1)+1);
                double x2 = points.get(2*(i+2));
                double y2 = points.get(2*(i+2)+1);
                output.quadTo((float) x1, (float) y1, (float) x2, (float) y2);
            }
            output.lineTo(points.get(points.size()-2).floatValue(),
                          points.get(points.size()-1).floatValue());
        }
        return output;
    }



}
