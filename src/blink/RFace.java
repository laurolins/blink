package blink;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.StringTokenizer;

import edu.uci.ics.jung.utils.Pair;

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
public class RFace {

    private RPoint _a;
    private RPoint _b;
    private RPoint _c;

    private RArc _ab;
    private RArc _bc;
    private RArc _ca;

    private ArrayList<RTriangle> _triangles = new ArrayList<RTriangle>();

    private GemColor _color;

    public RFace(RPoint a, RPoint b, RPoint c,
                 RArc ab, RArc bc, RArc ca, GemColor color) {
        _a = a;
        _b = b;
        _c = c;
        _ab = ab;
        _bc = bc;
        _ca = ca;
        _color = color;
    }

    public GemColor getColor() { return _color; }

    public RPoint getA() { return _a; }
    public RPoint getB() { return _b; }
    public RPoint getC() { return _c; }
    public RArc getAB() { return _ab; }
    public RArc getBC() { return _bc; }
    public RArc getCA() { return _ca; }

    public boolean containsTriangleWithSegments(RSegment ... list) {
        for (RTriangle t: _triangles) {
            boolean found = true;
            for (RSegment s: list) {
                if (!t.containsSegment(s)) {
                    found = false;
                    break;
                }
            }
            if (found) return true;
        }
        return false;
    }

    public boolean containsPointsAsArcEnds(RPoint ... points) {
        for (RPoint p: points) {
            if (!_a.equals(p) && !_b.equals(p) && !_c.equals(p))
                return false;
        }
        return true;
    }

    public boolean containsTriangleThatContainsSegment(RSegment s) {
        for (RTriangle t: _triangles) {
            // System.out.println("Testing "+t.toString()+" with "+s);
            if (t.hitTest(s.getA()) && t.hitTest(s.getB()))
                return true;
        }
        return false;
    }

    public RTriangle getTriangleThatContainsSegments(RSegment ... list) {
        for (RTriangle t: _triangles) {
            // System.out.println("Testing "+t.toString()+" with "+s);
            boolean found = true;
            for (RSegment s : list)
                if (!t.hitTest(s.getA()) || !t.hitTest(s.getB())) {
                    found = false;
                    break;
                }
            if (found)
                return t;
        }
        return null;
    }

    public RArc getArc(RPoint a, RPoint b) {
        if ((_ab.getFirstPoint().samePosition(a) && _ab.getLastPoint().samePosition(b)) ||
            (_ab.getFirstPoint().samePosition(b) && _ab.getLastPoint().samePosition(a))) {
            return _ab;
        }
        else if (
            (_bc.getFirstPoint().samePosition(a) && _bc.getLastPoint().samePosition(b)) ||
            (_bc.getFirstPoint().samePosition(b) && _bc.getLastPoint().samePosition(a))) {
            return _bc;
        }
        else if (
            (_ca.getFirstPoint().samePosition(a) && _ca.getLastPoint().samePosition(b)) ||
            (_ca.getFirstPoint().samePosition(b) && _ca.getLastPoint().samePosition(a))) {
            return _ca;
        }
        else throw new RuntimeException();
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("Face: " +
                  _a.getNameIfExistsOtherwiseId() + " " +
                  _b.getNameIfExistsOtherwiseId() + " " +
                  _c.getNameIfExistsOtherwiseId()+"\n");
        for (RTriangle t: getTriangles()) {
            sb.append(t.toString()+"\n");
        }
        sb.append(_ab);
        sb.append(_bc);
        sb.append(_ca);
        return sb.toString();
    }


    public void replace(RArc arc, HashMap<RSegment,Object> map) {

        // the arc replacement is easy...
        if (_ab.sameEndPoints(arc)) _ab = arc;
        else if (_bc.sameEndPoints(arc)) _bc = arc;
        else if (_ca.sameEndPoints(arc)) _ca = arc;
        else throw new RuntimeException();

        // replace point instances on all segments
        HashSet<RPoint> points = arc.getPoints();
        for (RSegment s: this.getSegments()) {
            s.replacePointInstance(points);
        }

        //
        for (int i=_triangles.size()-1;i>=0;i--) {
            RTriangle t = _triangles.get(i);
            for (RSegment s: t.getSegments()) {
                Object o = map.get(s);
                if (o instanceof RSegment) {
                    RSegment ss = (RSegment) o;
                    t.replaceSegment(ss);
                }
                else if (o instanceof Pair) {
                    RSegment s1 = (RSegment) ((Pair) o).getFirst();
                    RSegment s2 = (RSegment) ((Pair) o).getSecond();

                    RPoint opposite = t.getDifferentPoint(
                            s1.getA(),s1.getB(),
                            s2.getA(),s2.getB());

                    RPoint middlePoint = RSegment.getCommonPoint(s1,s2);

                    RPoint p1 = s1.getOpposite(middlePoint);
                    RPoint p2 = s2.getOpposite(middlePoint);

                    RSegment s3 = t.getSegment(p1,opposite);
                    RSegment s4 = t.getSegment(p2,opposite);

                    RSegment newS = new RSegment(opposite,middlePoint);

                    /**
                     * @todo take care of the normals?
                     */
                    RTriangle t1 = new RTriangle(s1,s3,newS);
                    RTriangle t2 = new RTriangle(s2,s4,newS);

                    _triangles.remove(i);
                    _triangles.add(t1);
                    _triangles.add(t2);

                    if (Log.MAX_LEVEL >=4 ) {
                        Log.log(4,"Create 2 triangles from "+t1+" "+t2);
                    }
                }
            }
        }
    }

    public RPoint getOppositeVertice(RArc arc) {
        if (arc != _ab && arc != _bc && arc != _ca)
            throw new RuntimeException();

        // arc
        if (_a != arc.getFirstPoint() && _a != arc.getLastPoint()) {
            return _a;
        }
        else if (_b != arc.getFirstPoint() && _b != arc.getLastPoint()) {
            return _b;
        }
        else if (_c != arc.getFirstPoint() && _c != arc.getLastPoint()) {
            return _c;
        }
        else throw new RuntimeException("");
    }

    public void addTriangle(RTriangle t) {
        _triangles.add(t);
    }

    public void addTriangles(ArrayList<RTriangle> list) {
        for (RTriangle t: list)
            this.addTriangle(t);
    }

    public ArrayList<RTriangle> getTriangles() {
        return _triangles;
    }

    public HashSet<RSegment> getArcSegments() {
        HashSet<RSegment> S = new HashSet<RSegment>();
        S.addAll(_ab.getSegments());
        S.addAll(_bc.getSegments());
        S.addAll(_ca.getSegments());
        return S;
    }

    public static RArc getCommonArc(RFace f1, RFace f2) {

        // list 1
        ArrayList<RArc> list1 = new ArrayList<RArc>();
        list1.add(f1.getAB()); list1.add(f1.getBC()); list1.add(f1.getCA());

        // list 2
        ArrayList<RArc> list2 = new ArrayList<RArc>();
        list2.add(f2.getAB()); list2.add(f2.getBC()); list2.add(f2.getCA());

        //
        RArc a = null;
        for (RArc a1: list1) {
            for (RArc a2: list2) {
                if (a1 == a2) {
                    if (a == null)
                        a = a1;
                    else return null; // more than one common arc
                }
            }
        }
        return a;
    }

    public RTriangle findTriangleContainingPoints(RPoint ... points) {
        for (RTriangle t: _triangles) {
            boolean found = true;
            for (RPoint p : points) {
                if (!t.hitTest(p))  {
                    found = false;
                    break;
                }
            }
            if (found) return t;
        }
        return null;
    }

    public HashSet<RSegment> getInternalSegments() {
        HashSet<RSegment> S = new HashSet<RSegment>();
        for (RTriangle t: _triangles) {
            S.add(t.getS1());
            S.add(t.getS2());
            S.add(t.getS3());
        }

        for (int i=0;i<_ab.getNumberOfSegments();i++) {
            RSegment s = _ab.getSegment(i);
            S.remove(s);
        }

        for (int i=0;i<_bc.getNumberOfSegments();i++) {
            RSegment s = _bc.getSegment(i);
            S.remove(s);
        }

        for (int i=0;i<_ca.getNumberOfSegments();i++) {
            RSegment s = _ca.getSegment(i);
            S.remove(s);
        }

        return S;
    }

    public HashSet<RSegment> getSegments() {
        HashSet<RSegment> S = new HashSet<RSegment>();
        for (RTriangle t: _triangles) {
            S.add(t.getS1());
            S.add(t.getS2());
            S.add(t.getS3());
        }
        return S;
    }

    public HashSet<RPoint> getPoints() {
        HashSet<RPoint> S = new HashSet<RPoint>();
        for (RTriangle t: _triangles) {
            S.add(t.getA());
            S.add(t.getB());
            S.add(t.getC());
        }
        return S;
    }

    public RArc[] getArcsIncidentToVertice(RPoint p) {

        if (p.getType() != RPointType.vertice)
            throw new RuntimeException();

        // arcs
        int i=0;
        RArc[] result = { null, null };
        if (_ab.contains(p))
            result[i++] = _ab;
        if (_bc.contains(p))
            result[i++] = _bc;
        if (_ca.contains(p))
            result[i++] = _ca;

        // return result
        return result;

    }

    public RTriangle findAnyTriangleContainingSegment(RSegment s) {
        for (RTriangle t: _triangles)
            if (t.containsSegment(s))
                return t;
        return null;
    }

    public RTriangle findTriangleWithGivenSegmentAndDifferentOf(RSegment s, RTriangle tt) {
        for (RTriangle t: _triangles) {
            if (t.containsSegment(s) && t != tt)
                return t;
        }
        return null;
    }

}

class NormalRepository {
    private HashMap<RPoint,NormalAverage> _mapPoints ;
    private HashMap<RSegment,NormalAverage> _mapSegments;

    public NormalRepository() {
        _mapPoints = new HashMap<RPoint,NormalAverage>();
        _mapSegments = new HashMap<RSegment,NormalAverage>();
    }

    public void addNormal(RPoint p, RPoint normal) {
        normal = normal.approxNormalize();
        NormalAverage na = _mapPoints.get(p);
        if (na == null) {
            na = new NormalAveragePoint(p);
            _mapPoints.put(p,na);
        }
        na.add(normal);
    }

    public void addNormal(RSegment s, RPoint normal) {
        normal = normal.approxNormalize();
        NormalAverage na = _mapSegments.get(s);
        if (na == null) {
            na = new NormalAverageSegment(s);
            _mapSegments.put(s,na);
        }
        na.add(normal);
    }

    public RPoint getNormal(RPoint p) {
        NormalAverage na = _mapPoints.get(p);
        if (na == null) throw new RuntimeException();
        return na.getNormalAverage();
    }

    public RPoint getNormal(RSegment s) {
        NormalAverage na = _mapSegments.get(s);
        if (na == null) throw new RuntimeException();
        return na.getNormalAverage();
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("\n\nNormal Repository\nPoints\n");
        for (NormalAverage p: _mapPoints.values()) {
            sb.append(p+"\n");
        }
        sb.append("\nSegments\n");
        for (NormalAverage p: _mapSegments.values()) {
            sb.append(p+"\n");
        }
        return sb.toString();
    }
}

class NormalAveragePoint extends NormalAverage {
    private RPoint _point;
    public NormalAveragePoint(RPoint p) {
        super();
        _point = p;
    }
    public String toString() {
        StringBuffer sb = new StringBuffer(
                ""+_point.getNameIfExistsOtherwiseId()+" "+
                this.getCount()+" "+
                this.getNormalAverage());
        return sb.toString();
    }
}

class NormalAverageSegment extends NormalAverage {
    private RSegment _segment;
    public NormalAverageSegment(RSegment s) {
        super();
        _segment = s;
    }
    public String toString() {
        StringBuffer sb = new StringBuffer(
                ""+_segment.getStringWithPointNamesIfExists()+" "+
                this.getCount()+" "+
                this.getNormalAverage());
        return sb.toString();
    }
}

abstract class NormalAverage {
    private int _count = 0;
    private RPoint _normal =  new RPoint(0,0,0);
    private ArrayList<RPoint> _normals;
    public NormalAverage() {
        _normals = new ArrayList<RPoint>();
    }
    public int getCount() {
        return _count;
    }
    public void add(RPoint p) {
        _normal = _normal.add(p);
        _normals.add(p);
        _count++;
        // log
        // Log.log(1,this.toString()+" "+p.toString());
    }

    public RPoint getNormalAverage() {

        if (_count == 0)
            return _normal;

        else if (_count > 2) {
            try {
                PrintWriter pw = new PrintWriter("model.lp");
                pw.println("\n\nMaximize\n");
                pw.println("value: s\n\n");
                pw.println("Subject To\n");
                pw.println("ineqs: s < 1");
                int i = 1;
                for (RPoint n : _normals) {
                    pw.println(String.format("ineq%d: %s x %s %s y %s %s z > 1",
                                             i++,
                                             n.getX().toStringDot(10),
                                             (n.getY().compareTo(BigInteger.ZERO) < 0 ? "" : "+"),
                                             n.getY().toStringDot(10),
                                             (n.getZ().compareTo(BigInteger.ZERO) < 0 ? "" : "+"),
                                             n.getZ().toStringDot(10)));
                }
                pw.println("\nBounds\n");
                pw.println("x free");
                pw.println("y free");
                pw.println("z free");
                pw.println("s free");
                pw.println("\nEnd\n\n");
                pw.flush();
                pw.close();

                String systemCall = "glpsol --cpxlp model.lp -o output.txt";
                // Process p = Runtime.getRuntime().exec(systemCall);
                // int exitVal = p.waitFor();

                Runtime rt = Runtime.getRuntime();
                Process proc = rt.exec(systemCall);

                // any error message?
                StreamGobbler errorGobbler = new
                                             StreamGobbler(proc.getErrorStream(), "ERROR");

                // any output?
                StreamGobbler outputGobbler = new
                                              StreamGobbler(proc.getInputStream(), "OUTPUT");

                // kick them off
                errorGobbler.start();
                outputGobbler.start();

                // any error???
                int exitVal = proc.waitFor();
                System.out.println("ExitValue: " + exitVal);

                BufferedReader b = new BufferedReader(new FileReader("output.txt"));
                String st;
                double x = 0, y = 0, z = 0;
                while ((st = b.readLine()) != null) {
                    // System.out.println("Processing: "+st);
                    StringTokenizer t = new StringTokenizer(st," *");
                    if (t.countTokens() == 4) {
                        String token = t.nextToken();
                        token = t.nextToken();
                        if ("x".equals(token)) {
                            token = t.nextToken();
                            x = Double.parseDouble(t.nextToken());
                        }
                        else if ("y".equals(token)) {
                            token = t.nextToken();
                            y = Double.parseDouble(t.nextToken());
                        }
                        else if ("z".equals(token)) {
                            token = t.nextToken();
                            z = Double.parseDouble(t.nextToken());
                        }
                    }
                }
                b.close();

                RPoint normalAverage = new RPoint(
                        new BigRational((long)(x*10000000L),10000000L),
                        new BigRational((long)(y*10000000L),10000000L),
                        new BigRational((long)(z*10000000L),10000000L)).approxNormalize();

                System.out.println("Average: "+normalAverage);
                return normalAverage;

            } catch (Exception ex) {
                ex.printStackTrace();
                throw new RuntimeException();
            }
        }

        else { // if (_count == 2) {
            return new RPoint(_normal.getX().div(_count),
                              _normal.getY().div(_count),
                              _normal.getZ().div(_count));
        }

    }
}


class RefineGraph {
    private ArrayList<RefineEdge> _edges = new ArrayList<RefineEdge>();
    private ArrayList<RefineVertex> _vertices = new ArrayList<RefineVertex>();

    public RefineGraph() {
    }

    public ArrayList<RefineEdge> getEdges() { return _edges; }
    public ArrayList<RSegment> getSegments() {
        ArrayList<RSegment> result= new ArrayList<RSegment>();
        for (RefineEdge e: _edges) {
            result.add(e.getSegment());
        }
        return result;
    }

    public ArrayList<RPoint> getPoints() {
        ArrayList<RPoint> result= new ArrayList<RPoint>();
        for (RefineVertex e: _vertices) {
            result.add(e.getPoint());
        }
        return result;
    }

    /**
     * Creates a copy of everything that is not fixed.
     * The shared elements are preserved.
     * @return RefineGraph
     */
    public RefineGraph copy() {
        RefineGraph copy = new RefineGraph();

        HashMap<RefineVertex,RefineVertex> map = new HashMap<RefineVertex,RefineVertex>();
        for (RefineVertex v: _vertices) {
            map.put(v,v.copyWithoutEdges());
            copy._vertices.add(map.get(v));
        }

        HashMap<RefineEdge,RefineEdge> mape = new HashMap<RefineEdge,RefineEdge>();
        for (RefineEdge e: _edges) {
            RefineVertex a = e.getA();
            RefineVertex b = e.getB();
            RefineVertex aa = map.get(a);
            RefineVertex bb = map.get(b);

            RefineEdge ee;
            if (a.isFixed() && b.isFixed()) {
                ee = new RefineEdge(e.getSegment(),aa,bb);
            }
            else {
                ee = new RefineEdge(new RSegment(aa.getPoint(),bb.getPoint()),aa,bb);
            }
            ee.setUVRefineEdgeTag(e.getUVTag());
            ee.setXYRefineEdgeTag(e.getXYTag());
            ee.setArcEdge(e.isArcEdge());
            mape.put(e,ee);
            copy._edges.add(ee);
        }

        // add edges
        for (RefineVertex v: _vertices) {
            RefineVertex vv = map.get(v);
            for (RefineEdge e : v.getEdges()) {
                RefineEdge ee = mape.get(e);
                vv.addEdge(ee);
            }
        }

        // copy
        return copy;
    }

    public RSegment findSegmentInstance(RSegment s) {
        for (RefineEdge e: _edges) {
            if (e.getSegment().equals(s))
                return e.getSegment();
        }
        throw new RuntimeException();
    }

    public void lift(NormalRepository R, BigRational epsilon) {
        for (RefineVertex v: _vertices) {
            if (!v.isFixed()) {
                RPoint p = v.getPoint();

                RPoint normal = R.getNormal(p);

                RPoint liftVector = normal.approxNormalize().scale(epsilon);
                if (Log.MAX_LEVEL >= 4) {
                    Log.log(4,"Lift "+p.getId()+" by modulus="+liftVector.approxModulus()+" vec="+liftVector.toString());
                }

                // sum epsilon vector on the normal direction

                p.set(p.add(liftVector));
            }
        }
    }

    public void tagVerticesAtSamePositionAsUV(RPoint p) {
        for (RefineVertex v: _vertices) {
            if (v.samePosition(p))
                v.setAsUVVertex(true);
        }
    }

    public void tagVerticesAtSamePositionAsXY(RPoint p) {
        for (RefineVertex v: _vertices) {
            if (v.samePosition(p))
                v.setAsXYVertex(true);
        }
    }

    public RefineVertex getVertex(RPoint p) {
        for (RefineVertex v: _vertices) {
            if (v.samePosition(p)) {
                return v;
            }
        }
        RefineVertex newV = new RefineVertex(p);
        _vertices.add(newV);
        return newV;
    }

    public RefineEdge newEdge(RSegment s, RefineVertex a, RefineVertex b) {
        RefineEdge e = new RefineEdge(s,a,b);
        a.addEdge(e);
        b.addEdge(e);
        _edges.add(e);
        return e;
    }

    private void dfsUV(
            RefineVertex v,
            XYRefineEdgeTag t,
            HashSet<RefineVertex> visited) {

        // XY frontier is UV arc
        if (v.isUVVertex()) return;

        //
        visited.add(v);
        for (RefineEdge e: v.getEdges()) {
            RefineVertex w = e.getOpposite(v);
            e.setXYRefineEdgeTag(t);
            System.out.println(""+e.toString()+" is "+t);
            if (!visited.contains(w))
                this.dfsUV(w,t,visited);
        }
    }

    private void dfsXY(RefineVertex v,
                       UVRefineEdgeTag t,
                       HashSet<RefineVertex> visited) {
        // UV frontier is XY arc
        if (v.isXYVertex()) return;

        visited.add(v);
        for (RefineEdge e: v.getEdges()) {
            RefineVertex w = e.getOpposite(v);
            e.setUVRefineEdgeTag(t);
            if (!visited.contains(w))
                this.dfsXY(w,t,visited);
        }
    }

    public void tagEdges(
            RefineVertex u,
            RefineVertex v,
            RefineVertex x,
            RefineVertex y) {

        for (RefineEdge e: _edges) {
            RefineVertex va = e.getA();
            RefineVertex vb = e.getB();
            if (va.isUVVertex() && vb.isUVVertex())
                e.setUVRefineEdgeTag(UVRefineEdgeTag.both);
            if (va.isXYVertex() && vb.isXYVertex())
                e.setXYRefineEdgeTag(XYRefineEdgeTag.both);
        }

        // tag the segments
        dfsUV(x, XYRefineEdgeTag.xSegment, new HashSet<RefineVertex>());
        dfsUV(y, XYRefineEdgeTag.ySegment, new HashSet<RefineVertex>());
        dfsXY(u, UVRefineEdgeTag.uSegment, new HashSet<RefineVertex>());
        dfsXY(v, UVRefineEdgeTag.vSegment, new HashSet<RefineVertex>());
    }

    public void triangulate() {
        for (RefineVertex v: _vertices) {
            ArrayList<RefineEdge> edges = v.getEdges();
            for (int i=0;i<edges.size();i++) {
                RefineEdge ei = edges.get(i);
                RefineVertex vi = ei.getOpposite(v);
                for (int j=i+1;j<edges.size();j++) {
                    RefineEdge ej = edges.get(j);
                    RefineVertex vj = ej.getOpposite(v);

                    // v is adjacent to vi
                    // v is adjacent to vj
                    // vi is not adjacent to vj
                    // so connect vi to vj
                    if (!vi.isAdjacentTo(vj)) {
                        RSegment s = new RSegment(vi.getPoint(),vj.getPoint());
                        this.newEdge(s,vi,vj);
                    }
                }
            }
        }
    }

    public void report() {
        for (RefineVertex v: _vertices) {
            System.out.println(""+v.toString());
        }
        for (RefineEdge e: _edges) {
            System.out.println(""+e.toString());
        }
    }

    public void defineCyclicOrderingBasedOnPlanarMap(
            RFace f1, boolean invert1,
            RFace f2, boolean invert2) {
        for (RefineVertex v : _vertices) {
            v.sortEdgesToCyclicOrdering(f1,invert1,f2,invert2);
        }
    }


    public void defineCyclicOrderingBasedOnPlanarMapOld(NormalRepository R) {
        for (RefineVertex v : _vertices) {
            RPoint origin = v.getPoint();
            RPoint normal = R.getNormal(origin);
            v.setProjectedPoint(origin);
            for (RefineEdge e: v.getEdges()) {
                RefineVertex neighbour = e.getOpposite(v);
                RPoint neighbourPoint = neighbour.getPoint();
                LineAndPlanIntersection L =
                   new LineAndPlanIntersection(
                      origin, // plane passes through v
                      normal, // and it's normal is the average
                      new RSegment(neighbourPoint,
                                   neighbourPoint.add(normal)));
                neighbour.setProjectedPoint(L.getIntersectionPoint());
            }
            v.sortEdgesBasedOnProjection(normal);
        }
    }

    public void defineCyclicOrderingBasedOnPlanarOld() {
        RPoint zero = new RPoint(0,0,0);
        RPoint random = new RPoint(5000-(int)(Math.random()*10000),
                                   5000-(int)(Math.random()*10000),
                                   5000-(int)(Math.random()*10000));


        for (RefineVertex v : _vertices) {
            LineAndPlanIntersection L =
                    new LineAndPlanIntersection(zero, random,
                                                new RSegment(v.getPoint(), v.getPoint().add(random)));
            v.setProjectedPoint(L.getIntersectionPoint());
        }

        // todo project all vertices on some plane
        // save it's projected positions, then
        // sort the edges to satisfy a clock-wise
        // ordering... this is needed to triangulate
        // the faces
        for (RefineVertex v : _vertices) {
            v.sortEdgesBasedOnProjection(random);
        }

    }

    public RefineEdge findEdge(RefineVertex a, RefineVertex b) {
        for (RefineEdge e: _edges) {
            if ((e.getA() == a && e.getB() == b) ||
                (e.getA() == b && e.getB() == a))
                return e;
        }
        throw new RuntimeException();
    }

    public ArrayList<RSegment> getSegmentsUV(RPoint u, RPoint v) {
        ArrayList<RSegment> result = new ArrayList<RSegment>();
        RefineVertex uu = this.getVertex(u);
        RefineVertex vv = this.getVertex(v);
        RefineVertex previous = null;
        RefineVertex ww = uu;

        System.out.println("\n\nUV segments...");
        while (ww != vv) {
            RefineEdge e = ww.getEdgeToUVNeighbourNotEqual(previous);
            result.add(e.getSegment());
            previous = ww;
            ww = e.getOpposite(ww);
            System.out.println(""+e.getSegment().getStringWithPointNamesIfExists());
        }
        System.out.println("\n\n");
        return result;
    }

    public ArrayList<RSegment> getSegmentsXY(RPoint x, RPoint y) {
        ArrayList<RSegment> result = new ArrayList<RSegment>();
        RefineVertex xx = this.getVertex(x);
        RefineVertex yy = this.getVertex(y);
        RefineVertex previous = null;
        RefineVertex ww = xx;

        System.out.println("\n\nXY segments...");
        while (ww != yy) {
            RefineEdge e = ww.getEdgeToXYNeighbourNotEqual(previous);
            result.add(e.getSegment());
            previous = ww;
            ww = e.getOpposite(ww);
            System.out.println(""+e.getSegment().getStringWithPointNamesIfExists());
        }
        System.out.println("\n\n");
        return result;
    }

    /**
     * The triangles
     * if xy==true return [X triangles list,  Y triangles list]
     * else return [U triangles list,  V triangles list]
     */
    public ArrayList[] getTriangles(NormalRepository N, boolean xy) {
        ArrayList result[] = {new ArrayList<RTriangle>(), new ArrayList<RTriangle>()};

        // define refine map
        RefineMap M = this.createMap();
        for (RefineMapFace f: M.getFaces()) {

            // original graph size
            if (f.getOriginalGraphSize() != 3)
                continue;

            // vertices
            ArrayList<RefineVertex> vertices = f.getGraphVertices();
            RefineVertex a = vertices.get(0);
            RefineVertex b = vertices.get(1);
            RefineVertex c = vertices.get(2);
            RefineEdge s1 = this.findEdge(a,b);
            RefineEdge s2 = this.findEdge(b,c);
            RefineEdge s3 = this.findEdge(c,a);
            RTriangle t = new RTriangle(
                    s1.getSegment(),
                    s2.getSegment(),
                    s3.getSegment());

            // see if it is on the right side
            if (RPoint.dotProduct(N.getNormal(a.getPoint()), t.getNormal()).
                compareTo(BigRational.ZERO) < 0) {
                t.invertOrientation();
            }

            // is it "X triangle"?
            if (xy) {
                if (s1.getXYTag() == XYRefineEdgeTag.xSegment ||
                    s2.getXYTag() == XYRefineEdgeTag.xSegment ||
                    s3.getXYTag() == XYRefineEdgeTag.xSegment) {
                    result[0].add(t);
                    System.out.println("X Triangle: " + t);
                }

                // is it "Y triangle"?
                else if (s1.getXYTag() == XYRefineEdgeTag.ySegment ||
                         s2.getXYTag() == XYRefineEdgeTag.ySegment ||
                         s3.getXYTag() == XYRefineEdgeTag.ySegment) {
                    result[1].add(t);
                    System.out.println("Y Triangle: " + t);
                }
            }
            else {
                if (s1.getUVTag() == UVRefineEdgeTag.uSegment ||
                    s2.getUVTag() == UVRefineEdgeTag.uSegment ||
                    s3.getUVTag() == UVRefineEdgeTag.uSegment) {
                    result[0].add(t);
                    System.out.println("U Triangle: " + t);
                }

                // is it "Y triangle"?
                else if (s1.getUVTag() == UVRefineEdgeTag.vSegment ||
                         s2.getUVTag() == UVRefineEdgeTag.vSegment ||
                         s3.getUVTag() == UVRefineEdgeTag.vSegment) {
                    result[1].add(t);
                    System.out.println("V Triangle: " + t);
                }
            }

        }

        /*
        // log things
        System.out.println(""+M.toString());
        System.out.println("\n\n--------\nFaces");
        ArrayList<RefineMapFace> faces = M.getFaces();
        for (RefineMapFace f: faces)
            System.out.println(""+f.graphFaceReport());
        System.out.println("\n\n");
        // log things */

        return result;
    }







    public RefineMap createMap() {
        RefineMap M = new RefineMap();

        // define vertices on the map
        for (RefineVertex v: _vertices) {
            int index = 0;
            for (RefineEdge e: v.getEdges()) {
                M.newMapVerticesFromGraphVertex(v,index++);
            }
        }

        // fill face and angle neighbourhood
        M.fillFaceAngleNeighbourhood();

        // for each edge connect them on
        // the map as "vertex" neighbours
        for (RefineEdge e: _edges) {
            M.addVertexNeighboursFromGraphEdge(e);
        }

        return M;
    }









    public void printDXF(PrintWriter pw) {
        pw.println(" 0");
        pw.println("SECTION");
        pw.println(" 2");
        pw.println("ENTITIES");

        for (RefineEdge e: _edges) {
            RPoint a = e.getA().getPoint();
            RPoint b = e.getB().getPoint();

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

        pw.println(" 0");
        pw.println("ENDSEC");
        pw.println(" 0");
        pw.println("EOF");

    }

    public void printProjectionDXF(PrintWriter pw) {
        pw.println(" 0");
        pw.println("SECTION");
        pw.println(" 2");
        pw.println("ENTITIES");

        for (RefineEdge e: _edges) {
            RPoint a = e.getA().getProjectedPoint();
            RPoint b = e.getB().getProjectedPoint();

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

        pw.println(" 0");
        pw.println("ENDSEC");
        pw.println(" 0");
        pw.println("EOF");

    }


}

class RefineVertex {
    private boolean _isFixed;
    private RPoint _point;
    private RPoint _projectedPoint;
    private ArrayList<RefineEdge> _edges = new ArrayList<RefineEdge>();
    private boolean _uvVertex;
    private boolean _xyVertex;
    public RefineVertex(RPoint p) {
        _point = p;
    }
    public RefineVertex copyWithoutEdges() {
        RefineVertex copy;
        if (_isFixed) copy = new RefineVertex(_point);
        else copy = new RefineVertex(_point.copy());

        // update some flags
        copy._uvVertex = _uvVertex;
        copy._xyVertex = _xyVertex;
        copy._isFixed = _isFixed;
        return copy;
    }
    public boolean isFixed() { return _isFixed; }
    public void setFixed(boolean f) { _isFixed = f; }
    public boolean samePosition(RPoint p) {
        return _point.samePosition(p);
    }
    public ArrayList<RefineEdge> getEdges() {
        return _edges;
    }
    public int getNumberOfEdges() { return _edges.size(); }
    public void setProjectedPoint(RPoint p) { _projectedPoint = p; }
    public RPoint getProjectedPoint() { return _projectedPoint; }
    public RPoint getPoint() { return _point; }
    public void addEdge(RefineEdge e) { _edges.add(e); }
    public boolean isAdjacentTo(RefineVertex v) {
        for (RefineEdge e: _edges) {
            if (e.getOpposite(this) == v)
                return true;
        }
        return false;
    }
    public void setAsUVVertex(boolean b) { _uvVertex = b; }
    public void setAsXYVertex(boolean b) { _xyVertex = b; }
    public boolean isUVVertex() { return _uvVertex; }
    public boolean isXYVertex() { return _xyVertex; }
    public String toString() {
        StringBuffer b = new StringBuffer();
        b.append("vertice "+_point.getNameIfExistsOtherwiseId()
                 +" UV="+_uvVertex+" XY="+_xyVertex+" address="+
                 Integer.toHexString(_point.hashCode()));
        return b.toString();
    }

    /**
     * Returns an edge to the only
     * vertex that is on the UV path.
     */
    public RefineEdge getEdgeToUVNeighbourNotEqual(RefineVertex previous) {
        RefineEdge result = null;
        for (RefineEdge e : _edges) {
            RefineVertex v = e.getOpposite(this);
            if (v.isUVVertex() && v != previous)
                if (result == null)
                    result = e;
                else
                    throw new RuntimeException("Should not happen");
        }
        return result;
    }

    /**
     * Returns an edge to the only
     * vertex that is on the UV path.
     */
    public RefineEdge getEdgeToXYNeighbourNotEqual(RefineVertex previous) {
        RefineEdge result = null;
        for (RefineEdge e : _edges) {
            RefineVertex v = e.getOpposite(this);
            if (v.isXYVertex() && v != previous)
                if (result == null)
                    result = e;
                else
                    throw new RuntimeException("Should not happen");
        }
        if (result == null)
            throw new RuntimeException("Should not happen");
        return result;
    }

    public void sortEdgesBasedOnProjection(RPoint planDirection) {
        if (_edges.size() == 0) return;

        RPoint zero = new RPoint(0,0,0);

        // first edge is the base
        RSegment s0 = new RSegment(this.getProjectedPoint(),
                                   _edges.get(0).getOpposite(this).
                                   getProjectedPoint());
        double s0Modulus = s0.getApproxLength();
        RPoint s0perp = RPoint.crossProduct(s0.getVector(),planDirection);

        HashMap<RefineEdge,Double> map = new HashMap<RefineEdge,Double>();
        ArrayList<RefineEdge> sortedList = new ArrayList<RefineEdge>();
        for (RefineEdge e: _edges) {

            double theta;
            if (e != _edges.get(0)) {
                // first edge is the base
                RSegment si = new RSegment(
                        this.getProjectedPoint(),
                        e.getOpposite(this).getProjectedPoint());

                double dotProduct = RSegment.approxDotProduct(s0, si);
                double siModulus = si.getApproxLength();
                theta = Math.acos(dotProduct / (s0Modulus * siModulus));

                if (!RTriangle.sameSide(si.getVector(), s0perp, zero, s0.getVector()))
                    theta = Math.PI * 2 - theta;
            }
            else theta = 0.0;

            // put theta
            map.put(e,theta);

            // include on sorted list
            boolean added = false;
            for (int i=0;i<sortedList.size();i++) {
                if (theta < map.get(sortedList.get(i))) {
                    added = true;
                    sortedList.add(i, e);
                    break;
                }
            }
            if (!added) sortedList.add(e);
        }

        //
        _edges = sortedList;

        // refine edge
        System.out.println("\nCyclic ordering of "+this.toString());
        System.out.println("Normal "+planDirection);
        for (RefineEdge e: _edges) {
            System.out.println(
                    ""+e.getOpposite(this)+
                    " "+e.getOpposite(this).getProjectedPoint().toString()+
                    " theta= "+
                    (180*map.get(e)/Math.PI));
        }
    }

    public void permuteEdges(ArrayList<RefineEdge> p) {
        if (_edges.containsAll(p) && p.containsAll(_edges) &&
            p.size() == _edges.size()){
            _edges.clear();
            _edges.addAll(p);
        }
        else throw new RuntimeException();
    }

    public int getCyclicIndex(RefineEdge e) {
        return _edges.indexOf(e);
    }

    public void sortEdgesToCyclicOrdering(
            RFace f1, boolean invert1,
            RFace f2, boolean invert2) {
        RefineVertex v = this;

        // set of edges from v
        HashSet<RefineEdge> E = new HashSet<RefineEdge>(v.getEdges());

        // sorted list
        ArrayList<RefineEdge> S = new ArrayList<RefineEdge>();

        System.out.println("\n\nSORT "+v);
        for (RefineEdge e: E) {
            System.out.println(e);
        }
        System.out.println("\n\n");

        // get a first vertex
        RefineEdge u = null;
        for (RefineEdge e: E) {
            if (e.isArcEdge()) {
                u = e;
                break;
            }
        }
        if (u == null)
            u = E.iterator().next();
        E.remove(u);
        S.add(u);


        while (!E.isEmpty()) {
            boolean found = false;

            // search for some edge that
            // makes a triangle with u
            for (RefineEdge e: E) {
                if (e.getOpposite(v).isAdjacentTo(u.getOpposite(v))) {
                    E.remove(e);
                    S.add(e);
                    u = e;
                    found = true;
                    break;
                }
            }

            if (!found) { // found?
                throw new RuntimeException();
            }
        }

        RefineEdge s0 = S.get(0);
        RefineEdge s1 = S.get(1);

        RPoint a = s0.getOpposite(v).getPoint();
        RPoint b = s1.getOpposite(v).getPoint();
        RPoint cross = RPoint.crossProduct(a.sub(v.getPoint()),b.sub(v.getPoint()));

        RTriangle t = null;
        RSegment s = new RSegment(a,b);
        RPoint normal = null;
        if ((t = f1.getTriangleThatContainsSegments(s,s0.getSegment(),s1.getSegment()))!= null) {
            normal = t.getNormal();
            if (invert1) normal.invert();
        }
        else if ((t = f2.getTriangleThatContainsSegments(s,s0.getSegment(),s1.getSegment()))!= null) {
            normal = t.getNormal();
            if (invert2) normal.invert();
        }
        else throw new RuntimeException();

        if (RPoint.dotProduct(cross,normal).compareTo(BigRational.ZERO) < 0) {
            Collections.reverse(S);
        }
        v.permuteEdges(S);

        // refine edge
        System.out.println("\nCyclic ordering of "+v.getPoint().getNameIfExistsOtherwiseId());
        System.out.println("Normal "+normal);
        for (RefineEdge e: S) {
            System.out.println(e.getOpposite(v).getPoint().getNameIfExistsOtherwiseId());
        }
    }


}

enum XYRefineEdgeTag {
    xSegment, ySegment, both
}

enum UVRefineEdgeTag {
    uSegment, vSegment, both
}

class RefineEdge {
    private RSegment _segment;
    private RefineVertex _a;
    private RefineVertex _b;

    private XYRefineEdgeTag _xyTag;
    private UVRefineEdgeTag _uvTag;
    private boolean _isArcEdge;

    public boolean isArcEdge() { return _isArcEdge; }
    public void setArcEdge(boolean isArcEdge) { _isArcEdge = isArcEdge; }

    public RefineEdge(RSegment s, RefineVertex a, RefineVertex b) {
        _segment = s;
        _a = a;
        _b = b;
    }
    public RefineVertex getOpposite(RefineVertex v) {
        if (_a == v) return _b;
        else if (_b == v) return _a;
        else throw new RuntimeException();
    }
    public RefineVertex getA() { return _a; }
    public RefineVertex getB() { return _b; }
    public RSegment getSegment() {
        return _segment;
    }
    public void setXYRefineEdgeTag(XYRefineEdgeTag tag) { _xyTag = tag; }
    public void setUVRefineEdgeTag(UVRefineEdgeTag tag) { _uvTag = tag; }
    public XYRefineEdgeTag getXYTag() { return _xyTag; }
    public UVRefineEdgeTag getUVTag() { return _uvTag; }

    public String toString() {
        StringBuffer b = new StringBuffer();
        b.append("edge "+_segment.getStringWithPointNamesIfExists()+
                 " "+_uvTag+" "+_xyTag+" address="+
                 Integer.toHexString(_segment.hashCode())+" arc="+this.isArcEdge());
        return b.toString();
    }
}




class RefineMap {
    private ArrayList<RefineMapVertex> _vertices;

    public RefineMap() {
        _vertices = new ArrayList<RefineMapVertex>();
    }

    public RefineMapFace getExternalFace() {
        RefineMapFace largest = null;
        for (RefineMapFace f: this.getFaces()) {
            if (largest == null || f.size() > largest.size())
                largest = f;
        }
        return largest;
    }

    public ArrayList<RefineMapVertex> getVertices() {
        return _vertices;
    }
    public void newMapVerticesFromGraphVertex(RefineVertex graphVertex, int cyclicIndex) {
        RefineMapVertex v1 = new RefineMapVertex(graphVertex,2*cyclicIndex);
        RefineMapVertex v2 = new RefineMapVertex(graphVertex,2*cyclicIndex+1);
        _vertices.add(v1);
        _vertices.add(v2);
    }

    public static void setNeighbours(RefineMapVertex v1, RefineMapVertex v2, RefineMapNeighbourType t) {
        // System.out.print("neighbours = "+t+"   -   "+v1.getGraphVertex().getPoint().getNameIfExistsOtherwiseLocation()+v1.getIndex());
        // System.out.print(" "+v2.getGraphVertex().getPoint().getNameIfExistsOtherwiseLocation()+v2.getIndex());
        // System.out.println();
        v1.setNeighbour(v2,t);
        v2.setNeighbour(v1,t);
    }

    public void fillFaceAngleNeighbourhood() {
        for (int i=0;i<_vertices.size();i++) {
            RefineMapVertex vi = _vertices.get(i);
            int index_i = vi.getIndex();
            for (int j=i+1;j<_vertices.size();j++) {
                RefineMapVertex vj = _vertices.get(j);
                int index_j = vj.getIndex();

                if (vi.getGraphVertex() != vj.getGraphVertex())
                    continue;

                int n = vi.getGraphVertex().getNumberOfEdges();

                if (vi.getCyclicIndex() == vj.getCyclicIndex())
                    setNeighbours(vi,vj,RefineMapNeighbourType.face);
                else if (
                        (Math.abs(index_i - index_j) == 1) ||
                        (index_i == 0 && index_j == 2 * n - 1) ||
                        (index_j == 0 && index_i == 2 * n - 1) ) {
                    setNeighbours(vi, vj, RefineMapNeighbourType.angle);
                }
            }
        }
    }

    public void addVertexNeighboursFromGraphEdge(RefineEdge e) {

        //
        RefineVertex va = e.getA();
        int indexa = va.getCyclicIndex(e);
        RefineVertex vb = e.getB();
        int indexb = vb.getCyclicIndex(e);

        RefineMapVertex odda=null, evena=null, oddb=null, evenb=null;
        for (RefineMapVertex v: _vertices) {
            if (v.getGraphVertex() == va && v.getCyclicIndex() == indexa) {
                if (v.getIndex() % 2 == 0) evena = v;
                else odda = v;
            }
            else if (v.getGraphVertex() == vb && v.getCyclicIndex() == indexb) {
                if (v.getIndex() % 2 == 0) evenb = v;
                else oddb = v;
            }
        }

        setNeighbours(odda,evenb,RefineMapNeighbourType.vertex);
        setNeighbours(oddb,evena,RefineMapNeighbourType.vertex);
    }

    /**
     * getFaces()
     * @return ArrayList
     */
    public ArrayList<RefineMapFace> getFaces() {
        ArrayList<RefineMapFace> result = new ArrayList<RefineMapFace>();

        for (RefineMapVertex v: _vertices)
            v.setTag(false);

        for (RefineMapVertex v: _vertices) {
            if (!v.isTagged()) {
                RefineMapFace f = new RefineMapFace();
                f.add(v);
                v.setTag(true);

                RefineMapNeighbourType t = RefineMapNeighbourType.angle;
                RefineMapVertex u = v.getNeighbour(t);
                while (u != v) {
                    // add "u" to the face "f"
                    f.add(u);
                    u.setTag(true);

                    // type of neighbourhood
                    t = (t == RefineMapNeighbourType.angle ?
                         RefineMapNeighbourType.vertex :
                         RefineMapNeighbourType.angle);

                    // u becomes it's t neighbour
                    u = u.getNeighbour(t);
                }

                // add face
                result.add(f);
            }
        }
        return result;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (RefineMapVertex v: _vertices) {
            sb.append(v.toString()+"\n");
        }
        return sb.toString();
    }

}

class RefineMapFace {
    private ArrayList<RefineMapVertex> _vertices;
    public RefineMapFace() {
        _vertices = new ArrayList<RefineMapVertex>();
    }
    public void add(RefineMapVertex v) { _vertices.add(v); }
    public int size() { return _vertices.size(); }
    public int getOriginalGraphSize() { return _vertices.size()/2; }
    public ArrayList<RefineVertex> getGraphVertices() {
        ArrayList<RefineVertex> list = new ArrayList<RefineVertex>();
        RefineVertex rv = null;
        for (RefineMapVertex v: _vertices) {
            if (v.getGraphVertex() != rv) {
                list.add(v.getGraphVertex());
                rv = v.getGraphVertex();
            }
        }
        return list;
    }

    public ArrayList<RefineMapVertex> getVertices() {
        return _vertices;
    }

    public boolean contains(RefineMapVertex v) {
        return _vertices.contains(v);
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("face "+this.size()+" edges ");
        for (RefineMapVertex v: _vertices) {
            sb.append(v.toString()+" ");
        }
        return sb.toString();
    }
    public String graphFaceReport() {
        StringBuffer sb = new StringBuffer();
        sb.append("face "+(this.size()/2)+" edges ");
        for (RefineVertex v: this.getGraphVertices()) {
            sb.append(v.getPoint().getNameIfExistsOtherwiseId()+" ");
        }
        return sb.toString();
    }

    public HashSet<RPoint> getPoints() {
        HashSet<RPoint> P = new HashSet<RPoint>();
        for (RefineMapVertex p: this._vertices) {
            P.add(p.getGraphVertex().getPoint());
        }
        return P;
    }


    public boolean contains(RPoint ... points) {
        HashSet<RPoint> P = this.getPoints();
        for (RPoint p: points) {
            if (!P.contains(p))
                return false;
        }
        return true;
    }


}

enum RefineMapNeighbourType {
    face, angle, vertex
}

class RefineMapVertex {
    private int _index;
    private RefineVertex _graphVertex;
    private RefineMapVertex _vertexNeighbour;
    private RefineMapVertex _faceNeighbour;
    private RefineMapVertex _angleNeighbour;
    public RefineMapVertex(RefineVertex graphVertex, int index) {
        _graphVertex = graphVertex;
        _index = index;
    }
    public RefineVertex getGraphVertex() { return _graphVertex; }
    public int getCyclicIndex() { return _index/2; }
    public int getIndex() { return _index; }
    public void setVertexNeighbour(RefineMapVertex v) { _vertexNeighbour = v; }
    public void setFaceNeighbour(RefineMapVertex v) { _faceNeighbour = v; }
    public void setAngleNeighbour(RefineMapVertex v) { _angleNeighbour = v; }
    public RefineMapVertex setVertexNeighbour() { return _vertexNeighbour; }
    public RefineMapVertex setFacexNeighbour() { return _faceNeighbour; }
    public RefineMapVertex setAngleNeighbour() { return _angleNeighbour; }
    public RefineMapVertex getNeighbour(RefineMapNeighbourType t) {
        if (t == RefineMapNeighbourType.face) return _faceNeighbour;
        else if (t == RefineMapNeighbourType.angle) return _angleNeighbour;
        else if (t == RefineMapNeighbourType.vertex) return _vertexNeighbour;
        else throw new RuntimeException();
    }
    public void setNeighbour(RefineMapVertex v, RefineMapNeighbourType t) {
        if (t == RefineMapNeighbourType.face) _faceNeighbour = v;
        else if (t == RefineMapNeighbourType.angle) _angleNeighbour = v;
        else if (t == RefineMapNeighbourType.vertex) _vertexNeighbour = v;
        else throw new RuntimeException();
    }

    /**
     * tag infrastructure
     */
    private boolean _tag;
    public void setTag(boolean tag) { _tag = tag; }
    public boolean isTagged() { return _tag; }

    public String toString() {
        return _graphVertex.getPoint().getNameIfExistsOtherwiseId()+" "+_index+
                " v: "+
                _vertexNeighbour.getGraphVertex().getPoint().getNameIfExistsOtherwiseId()+"."+
                _vertexNeighbour.getIndex()+
                " a: "+
                _angleNeighbour.getGraphVertex().getPoint().getNameIfExistsOtherwiseId()+"."+
                _angleNeighbour.getIndex()+
                " f: "+
                _faceNeighbour.getGraphVertex().getPoint().getNameIfExistsOtherwiseId()+"."+
                _faceNeighbour.getIndex();
    }

}


class StreamGobbler extends Thread {
    InputStream is;
    String type;

    StreamGobbler(InputStream is, String type) {
        this.is = is;
        this.type = type;
    }

    public void run() {
        try {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);
            String line = null;
            while ((line = br.readLine()) != null)
                System.out.println(type + ">" + line);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
