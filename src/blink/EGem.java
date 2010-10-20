package blink;

import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import cern.colt.matrix.DoubleMatrix2D;
import cern.colt.matrix.impl.DenseDoubleMatrix2D;
import cern.colt.matrix.linalg.Algebra;
import edu.uci.ics.jung.graph.Edge;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.graph.impl.SparseGraph;
import edu.uci.ics.jung.graph.impl.SparseVertex;
import edu.uci.ics.jung.graph.impl.UndirectedSparseEdge;
import edu.uci.ics.jung.utils.Pair;
import edu.uci.ics.jung.utils.UserData;

/**
 * Embedded Gem
 */
public class EGem {
    private Gem _gem;
    private ArrayList<EGemVertex> _vertices;
    private ArrayList<EGemEdge> _edges;
    HashMap<Component,EGemVertex> _map = new HashMap<Component,EGemVertex>();

    public EGem(Gem gem) {
        _gem = gem;
        _vertices = new ArrayList<EGemVertex>();
        _edges = new ArrayList<EGemEdge>();

        // create vertex for each 3-residue of the gem
        ArrayList<Component> triballs = _gem.getComponentRepository().getTriballs();
        int k = 1;
        for (Component c: triballs) {
            // System.out.println(c.toString());
            EGemVertex v = this.newVertex(c);
            _map.put(c,v);
        }

        // create edge for each 2-residue 3-balls
        GemColor[][] bigonColors = {
                                   {GemColor.yellow,GemColor.blue},
                                   {GemColor.yellow,GemColor.red},
                                   {GemColor.yellow,GemColor.green},
                                   {GemColor.blue,GemColor.red},
                                   {GemColor.blue,GemColor.green},
                                   {GemColor.red,GemColor.green}
        };

        // add an edge...
        for (GemColor[] bc: bigonColors) {
            GemColor complementColors[] = GemColor.getComplementColors(bc);
            ArrayList<Component> bigons = _gem.getComponentRepository().getBigons(bc[0],bc[1]);
            for (Component bigon: bigons) {
                GemVertex v = bigon.getVertex();
                int colorSetTriball1 = GemColor.getColorSet(bc[0], bc[1], complementColors[0]);
                Component t1 = v.getComponent(colorSetTriball1);
                int colorSetTriball2 = GemColor.getColorSet(bc[0], bc[1], complementColors[1]);
                Component t2 = v.getComponent(colorSetTriball2);
                EGemVertex v1 = _map.get(t1);
                EGemVertex v2 = _map.get(t2);
                this.newEdge(v1, v2, bigon);
            }
        }

        // define boundary positions
        this.defineBoundaryPositions();

    }

    public void pigale(PrintStream ps) {
        HashSet<EGemVertex> boundary = (HashSet<EGemVertex>) this.getBoundaryPointsAndRemovedPoint()[1];

        HashMap<EGemVertex,Integer> map = new HashMap<EGemVertex,Integer>();
        int k=1;
        for (EGemEdge e: _edges) {
            EGemVertex u = (EGemVertex) e.getVertices().getFirst();
            EGemVertex v = (EGemVertex) e.getVertices().getSecond();
            if (boundary.contains(u) && boundary.contains(v)) {
                if (!map.containsKey(u)) map.put(u,k++);
                if (!map.containsKey(v)) map.put(v,k++);
            }
        }


        ps.println("PIG:0 XXX");
        for (EGemEdge e: _edges) {
            EGemVertex u = (EGemVertex) e.getVertices().getFirst();
            EGemVertex v = (EGemVertex) e.getVertices().getSecond();
            if (boundary.contains(u) && boundary.contains(v)) {
                ps.println(map.get(u)+" "+map.get(v));
            }
        }
        ps.println("0 0");
        ps.flush();
    }


    public int getNumVertices() {
        return _vertices.size();
    }

    public int getNumEdges() {
        return _edges.size();
    }

    public void delete(EGemVertex v) {
        _vertices.remove(v);
        for (EGemVertex u: _vertices) {
            u.removeEdgesWithVertex(v);
        }
    }

    public ArrayList<EGemVertex> getVertices() {
        return _vertices;
    }

    public void delete(EGemEdge e) {
        ((EGemVertex)e.getVertices().getFirst()).removeEdge(e);
        ((EGemVertex)e.getVertices().getSecond()).removeEdge(e);
    }

    public EGemEdge newEdge(EGemVertex u, EGemVertex v, Component bigon) {
        EGemEdge e = new EGemEdge(u,v,bigon);
        u.addEdge(e);
        v.addEdge(e);
        _edges.add(e);
        return e;
    }

    public HashSet<EGemVertex> getPointsFromOriginalVertex() {
        HashSet<EGemVertex> S = new HashSet<EGemVertex>();
        if (_gem instanceof ThickenedGem) {
            ThickenedGem tg = (ThickenedGem) _gem;
            for (EGemVertex v : _vertices) {
                for (GemVertex vv : v.getTriball().getVertices()) {
                    ThickenedGemVertex tgv = tg.getThickenedGemVertex(vv);
                    if (tgv.getOriginalLabel() == 1) {
                        S.add(v);
                        break;
                    }
                }
            }
        }
        else {
            for (EGemVertex v : _vertices) {
                for (GemVertex vv : v.getTriball().getVertices()) {
                    if (vv.getLabel() == 1) {
                        S.add(v);
                        break;
                    }
                }
            }
        }

        return S;
    }

    public int countIntersections(EGemVertex specialVertex) {
        int sets[] = {
        GemColor.getColorSet(GemColor.blue,GemColor.red,GemColor.green),
        GemColor.getColorSet(GemColor.yellow,GemColor.red,GemColor.green),
        GemColor.getColorSet(GemColor.yellow,GemColor.blue,GemColor.green),
        GemColor.getColorSet(GemColor.yellow,GemColor.blue,GemColor.red)
        };

        int result = 0;
        int comparisions = 0;

        ArrayList<GemVertex> list = _gem.getVertices();
        for (int i=0;i<list.size();i++){
            GemVertex vi = list.get(i);
            EGemVertex vi0 = _map.get(vi.getComponent(sets[0]));
            EGemVertex vi1 = _map.get(vi.getComponent(sets[1]));
            EGemVertex vi2 = _map.get(vi.getComponent(sets[2]));
            EGemVertex vi3 = _map.get(vi.getComponent(sets[3]));

            if (specialVertex == vi0 || specialVertex == vi1 ||
                specialVertex == vi2 || specialVertex == vi3)
                continue;

            for (int j=i+1;j<list.size();j++){
                GemVertex vj = list.get(j);
                EGemVertex vj0 = _map.get(vj.getComponent(sets[0]));
                EGemVertex vj1 = _map.get(vj.getComponent(sets[1]));
                EGemVertex vj2 = _map.get(vj.getComponent(sets[2]));
                EGemVertex vj3 = _map.get(vj.getComponent(sets[3]));

                if (specialVertex == vj0 || specialVertex == vj1 ||
                    specialVertex == vj2 || specialVertex == vj3)
                    continue;

                boolean intersects = BlinkNativeLib.intersects(
                vi0.getX3d(),vi0.getY3d(),vi0.getZ3d(),
                vi1.getX3d(),vi1.getY3d(),vi1.getZ3d(),
                vi2.getX3d(),vi2.getY3d(),vi2.getZ3d(),
                vi3.getX3d(),vi3.getY3d(),vi3.getZ3d(),
                vj0.getX3d(),vj0.getY3d(),vj0.getZ3d(),
                vj1.getX3d(),vj1.getY3d(),vj1.getZ3d(),
                vj2.getX3d(),vj2.getY3d(),vj2.getZ3d(),
                vj3.getX3d(),vj3.getY3d(),vj3.getZ3d());

                if (intersects) {
                    if (problems1.size() == 0) {
                        problems1.add(vi0);
                        problems1.add(vi1);
                        problems1.add(vi2);
                        problems1.add(vi3);
                        problems2.add(vj0);
                        problems2.add(vj1);
                        problems2.add(vj2);
                        problems2.add(vj3);
                    }
                    result++;
                }
                comparisions++;
            }
        }
        System.out.println(String.format("Intersection Percentage %.4f%%  %d em %d",100*(double)result/(double)comparisions,result,comparisions));
        return result;
    }

    private HashSet<EGemVertex> problems1 = new HashSet<EGemVertex>();
    private HashSet<EGemVertex> problems2 = new HashSet<EGemVertex>();

    public Object[] getBoundaryPointsAndRemovedPoint() {
        // get max degree vertex
        EGemVertex maxDegreeVertex = null;
        for (EGemVertex v: _vertices) {
            // System.out.println("Triball: "+v.getTriball().getComplementColors()[0]);
            if (maxDegreeVertex == null &&
                v.getTriball().getComplementColors()[0] == GemColor.red) {
                maxDegreeVertex = v;
            }
            else if (maxDegreeVertex != null &&
                     maxDegreeVertex.degree() < v.degree() &&
                     v.getTriball().getComplementColors()[0] == GemColor.red) {
                maxDegreeVertex = v;
            }
        }

        //
        HashSet<EGemVertex> S = new HashSet<EGemVertex>();
        for (EGemEdge e: maxDegreeVertex.getEdges()) {
            S.add(e.getOpposite(maxDegreeVertex));
        }
        return new Object[] {maxDegreeVertex,S};
    }

    public EGemVertex newVertex(Component triball) {
        int newLabel = 1;
        for (EGemVertex v:_vertices) {
            if (v.getLabel() >= newLabel) {
                newLabel = v.getLabel()+1;
            }
        }
        EGemVertex v = new EGemVertex(newLabel,triball);
        _vertices.add(v);
        return v;
    }

    public Gem getGem() {
        return _gem;
    }

    public Graph getGraph() {
        Graph graph = new SparseGraph();

        // create vertices
        HashMap<EGemVertex, Vertex> map = new HashMap<EGemVertex, Vertex>();
        for (EGemVertex gv : _vertices) {
            Vertex v = graph.addVertex(new SparseVertex());
            v.setUserDatum("key", gv, UserData.SHARED);
            map.put(gv, v);
        }

        // create edges
        HashSet<EGemEdge> addedEdges = new HashSet<EGemEdge>();
        for (EGemVertex gv : _vertices) {
            Vertex v = map.get(gv);
            for (EGemEdge ee : gv.getEdges()) {

                if (addedEdges.contains(ee)) continue;
                addedEdges.add(ee);

                Vertex u = map.get(ee.getOpposite(gv));
                // add an edge from parent gem to this new simplified gem

                Edge e = graph.addEdge(new UndirectedSparseEdge(u, v));
                e.setUserDatum("key", ee, UserData.SHARED);
            }
        }
        return graph;
    }


    private EGemVertex[]  defineBoundaryPlanarPosition(
            HashSet<EGemVertex> S) {
        // -- find any triangle ----------------------------
        EGemVertex T[] = null;
        for (EGemVertex v: S) {
            for (EGemEdge ev: v.getEdges()) {
                EGemVertex u = ev.getOpposite(v);
                if (!S.contains(u)) continue;
                for (EGemEdge eu: u.getEdges()) {
                    EGemVertex w = eu.getOpposite(u);
                    if (!S.contains(w)) continue;
                    if (v.isAdjacentTo(w)) {
                        T = new EGemVertex[] {v,u,w};
                        break;
                    }
                }
                if (T != null) break;
            }
            if (T != null) break;
        }
        if (T == null)
            throw new RuntimeException();
        // -- find any triangle ----------------------------

        // vertices of the triangles
        double tx1 = 0.0, ty1 = 0;
        double tx2 = 0.5, ty2 = Math.sqrt(3)/2.0;
        double tx3 = 1.0, ty3 = 0;

        // set position
        T[0].setPosition2d(tx1,ty1);
        T[1].setPosition2d(tx2,ty2);
        T[2].setPosition2d(tx3,ty3);

        // now mount tutte-system on S\T
        int n = S.size();

        // prepare Matrix
        DoubleMatrix2D X = new DenseDoubleMatrix2D(n, n);
        DoubleMatrix2D Y = new DenseDoubleMatrix2D(n, n);
        DoubleMatrix2D Bx = new DenseDoubleMatrix2D(n, 1);
        DoubleMatrix2D By = new DenseDoubleMatrix2D(n, 1);

        // mount map vertices on S to {0,...,n-1}
        HashMap<EGemVertex,Integer> mapV2I = new HashMap<EGemVertex,Integer>();
        int k = 0;
        for (EGemVertex v : S)
            mapV2I.put(v, k++);

        // prepare fixed values on system of equations
        for (EGemVertex v : T) {
            int index = mapV2I.get(v);
            X.set(index, index, 1);
            Bx.set(index, 0, v.getX2d());

            Y.set(index, index, 1);
            By.set(index, 0, v.getY2d());
        }

        // prepare other vertices
        for (EGemVertex v : S) {
            // vertice already considered
            if (v==T[0] || v==T[1] || v==T[2])
                continue;

            int vi = mapV2I.get(v);

            //
            X.set(vi, vi, 1);
            Y.set(vi, vi, 1);
            Bx.set(vi, 0, 0);
            By.set(vi, 0, 0);

            // count neighbours on boundary
            int countNeighboursOnBoundary = 0;
            for (EGemEdge e : v.getEdges())
                if (S.contains(e.getOpposite(v)))
                    countNeighboursOnBoundary++;

            //
            for (EGemEdge e : v.getEdges()) {
                EGemVertex vn = e.getOpposite(v); // v neighbour
                // System.out.println("Neighbour: "+vn.getOriginalLabel()+" by color "+cc);
                if (S.contains(vn)) {
                    int vni = mapV2I.get(vn);
                    X.set(vi, vni, -1.0 / countNeighboursOnBoundary);
                    Y.set(vi, vni, -1.0 / countNeighboursOnBoundary);
                }
            }
        }

        // System.out.println("X\n"+X.toString());
        // System.out.println("\n\nY\n"+Y.toString());

        DoubleMatrix2D Rx = Algebra.DEFAULT.solve(X, Bx);
        DoubleMatrix2D Ry = Algebra.DEFAULT.solve(Y, By);

        for (EGemVertex v : S) {
            int vi = mapV2I.get(v);
            double x = Rx.get(vi, 0);
            double y = Ry.get(vi, 0);
            v.setPosition2d(x,y);
        }

        return T;
    }

    private void defineBoundary3dPosition(HashSet<EGemVertex> S, double r) {
        int maxDegree = 0;
        for (EGemVertex u: S) {
            int deg = u.degree(S);
            if (deg > maxDegree)
                maxDegree = deg;
        }

        EGemVertex top=null,bottom=null,middle=null;
        for (EGemVertex u: S) {
            if (u.degree(S) == maxDegree && (top == null || bottom == null)) {
                if (top == null) top = u;
                else if (bottom == null) bottom = u;
            }
            else if (u.degree(S) == 4) {
                middle = u;
            }
            else throw new RuntimeException();
        }

        // oooops
        if (middle == null || top == null || bottom == null)
            throw  new RuntimeException();

        // vertices
        ArrayList<EGemVertex> list = new ArrayList<EGemVertex>();
        EGemVertex u = middle;
        while (list.size() < S.size()-2) {
            list.add(u);
            for (EGemEdge e: u.getEdges()) {
                EGemVertex v = e.getOpposite(u);
                if (S.contains(v) && v != top && v!= bottom && !list.contains(v)) {
                    u = v;
                    break;
                }
            }
        }

        //
        top.setPosition3d(0,0,r);
        bottom.setPosition3d(0,0,-r);
        int i=0;
        for (EGemVertex v: list) {
            double theta = (i++)/(double)list.size() * Math.PI * 2;
            v.setPosition3d(r*Math.cos(theta),r*Math.sin(theta),0);
        }
    }

    private void defineInternalVertices3dPosition(HashSet<EGemVertex> S, EGemVertex removedVertice) {
        ArrayList<EGemVertex> list = new ArrayList<EGemVertex>(_vertices);
        list.remove(removedVertice);

        // now mount tutte-system on S\T
        int n = list.size();

        // prepare Matrix
        DoubleMatrix2D X = new DenseDoubleMatrix2D(n, n);
        DoubleMatrix2D Y = new DenseDoubleMatrix2D(n, n);
        DoubleMatrix2D Z = new DenseDoubleMatrix2D(n, n);
        DoubleMatrix2D Bx = new DenseDoubleMatrix2D(n, 1);
        DoubleMatrix2D By = new DenseDoubleMatrix2D(n, 1);
        DoubleMatrix2D Bz = new DenseDoubleMatrix2D(n, 1);

        // mount map vertices on S to {0,...,n-1}
        HashMap<EGemVertex, Integer> mapV2I = new HashMap<EGemVertex, Integer>();
        int k = 0;
        for (EGemVertex v : list)
            mapV2I.put(v, k++);

        // prepare fixed values on system of equations
        for (EGemVertex v : S) {
            int index = mapV2I.get(v);
            X.set(index, index, 1);
            Bx.set(index, 0, v.getX3d());

            Y.set(index, index, 1);
            By.set(index, 0, v.getY3d());

            Z.set(index, index, 1);
            Bz.set(index, 0, v.getZ3d());
        }

        // prepare other vertices
        for (EGemVertex v : list) {
            // vertice already considered
            if (S.contains(v))
                continue;

            int vi = mapV2I.get(v);

            //
            X.set(vi, vi, 1);
            Y.set(vi, vi, 1);
            Z.set(vi, vi, 1);
            Bx.set(vi, 0, 0);
            By.set(vi, 0, 0);
            Bz.set(vi, 0, 0);

            // count neighbours on boundary
            int countNeighboursOnBoundary = 0;
            for (EGemEdge e : v.getEdges())
                if (list.contains(e.getOpposite(v)))
                    countNeighboursOnBoundary++;

            //
            for (EGemEdge e : v.getEdges()) {
                EGemVertex vn = e.getOpposite(v); // v neighbour
                // System.out.println("Neighbour: "+vn.getOriginalLabel()+" by color "+cc);
                if (S.contains(vn)) {
                    int vni = mapV2I.get(vn);
                    X.set(vi, vni, -1.0 / countNeighboursOnBoundary);
                    Y.set(vi, vni, -1.0 / countNeighboursOnBoundary);
                    Z.set(vi, vni, -1.0 / countNeighboursOnBoundary);
                }
            }
        }

        // System.out.println("X\n"+X.toString());
        // System.out.println("\n\nY\n"+Y.toString());
        // System.out.println("\n\nZ\n"+Z.toString());

        DoubleMatrix2D Rx = Algebra.DEFAULT.solve(X, Bx);
        DoubleMatrix2D Ry = Algebra.DEFAULT.solve(Y, By);
        DoubleMatrix2D Rz = Algebra.DEFAULT.solve(Z, Bz);

        for (EGemVertex v : list) {
            int vi = mapV2I.get(v);
            double x = Rx.get(vi, 0);
            double y = Ry.get(vi, 0);
            double z = Rz.get(vi, 0);
            v.setPosition3d(x, y, z);
        }

    }



    private void projectBoundaryPointsOnSphere(HashSet<EGemVertex> S, double r) {
        double alpha = Math.sqrt(r*r-1.0/3.0);
        double xs = 0.5, ys = Math.sqrt(3)/6.0, zs = - alpha;
        double xc = 0.5, yc = Math.sqrt(3)/6.0, zc = r - alpha; //zc = 6.0; // r - alpha;

        double p[] = {0,0,0};
        for (EGemVertex v: S) {
            double x0 = v.getX2d();
            double y0 = v.getY2d();
            double z0 = 0.0;
            boolean ok = intersect(x0,y0,z0,xc,yc,zc,xs,ys,zs,r,p);
            if (!ok) {
                v.setPosition3d(Double.NaN,Double.NaN,Double.NaN);
            }
            else {
                v.setPosition3d(p[0],p[1],p[2]);
            }
        }
    }

    private void projectBoundaryPointsOnSphere2(HashSet<EGemVertex> S, double r) {
        HashMap<Double,ArrayList<EGemVertex>> map = new HashMap<Double,ArrayList<EGemVertex>>();

        double x0 =0, y0 =0;
        for (EGemVertex v: S) {
            x0 += v.getX2d();
            y0 += v.getY2d();
        }
        x0 = x0/S.size();
        y0 = y0/S.size();

        //double x0 = 0.5;
        //double y0 = Math.sqrt(3.0)/6.0;
        for (EGemVertex v: S) {
            double xv = v.getX2d();
            double yv = v.getY2d();
            double rv = Math.sqrt((xv-x0)*(xv-x0)+(yv-y0)*(yv-y0));
            double thetav = Math.acos((xv-x0)*1 + (yv-y0)*0);
            if (yv-y0 < 0) thetav = 2*Math.PI - thetav;

            // System.out.println(String.format("Theta: %.6f Radius: %.6f",thetav*180.0/Math.PI,rv));

            ArrayList<EGemVertex> list = map.get(rv);
            if (list ==  null) {
                list = new ArrayList<EGemVertex>();
                map.put(rv,list);
            }
            list.add(v);
        }

        //
        ArrayList<Double> keys = new ArrayList<Double>(map.keySet());
        Collections.sort(keys);
        int n = keys.size();
        double delta = 1.0/(n+1);
        int k = 1;
        for (double d: keys) {
            double h = (k++)*delta;
            double zz = (h < 0.5 ? -(0.5-h)/0.5*r : (h-0.5)/0.5*r);
            // System.out.println(String.format("zz = %.6f",zz));
            ArrayList<EGemVertex> list = map.get(d);
            for (EGemVertex v: list) {
                double xv = v.getX2d();
                double yv = v.getY2d();
                double rv = Math.sqrt((xv-x0)*(xv-x0)+(yv-y0)*(yv-y0));
                double thetav = Math.acos((xv-x0)*1 + (yv-y0)*0);
                if (yv-y0 < 0) thetav = Math.PI*2 - thetav;

                // position based on the center of the sphere
                double rr = Math.sqrt(r*r - zz*zz);
                double xx = rr * Math.cos(thetav);
                double yy = rr * Math.sin(thetav);
                v.setPosition3d(xx,yy,zz);
            }
        }
    }

    private void defineBoundaryPositions() {
        Object[] data = this.getBoundaryPointsAndRemovedPoint();
        EGemVertex specialVertex = (EGemVertex) data[0];
        HashSet<EGemVertex> S = (HashSet<EGemVertex>) data[1];


        // define planar position of boundary points using Tutte
        EGemVertex T[] = defineBoundaryPlanarPosition(S);

        // project boundary points
        double r = 5;
        defineBoundary3dPosition(S,r);
        defineInternalVertices3dPosition(S,specialVertex);

        countIntersections(specialVertex);

        //

        //projectBoundaryPointsOnSphere(S,r);
        //projectBoundaryPointsOnSphere2(S,r);

        //
        try {
            PrintWriter pw = new PrintWriter("c:/x.wrl");
            pw.println("#VRML V2.0 utf8");

            pw.println(
            "PROTO ConnectingCylinder [\n"+
            "   field SFNode  appearance NULL\n"+
            "   field SFBool  bottom     TRUE\n"+
            "   field SFVec3f vertex0    0 -1 0\n"+
            "   field SFVec3f vertex1    0 1 0\n"+
            "   field SFFloat radius     1\n"+
            "   field SFBool  side       TRUE\n"+
            "   field SFBool  top        TRUE\n"+
            "]\n"+
            "{\n"+
            "   DEF TRANSFORM Transform {\n"+
            "      children [\n"+
            "         Shape {\n"+
            "            appearance Appearance { material Material { diffuseColor 1 0 0 } }\n"+
            "            geometry Cylinder {\n"+
            "            bottom IS bottom\n"+
            "            height 1\n"+
            "            radius IS radius\n"+
            "            side IS  side\n"+
            "            top  IS top\n"+
            "            }\n"+
            "         }\n"+
            "      ]\n"+
            "   }\n"+
            "   Script {\n"+
            "      field SFVec3f vertex0   IS vertex0\n"+
            "      field SFVec3f vertex1   IS vertex1\n"+
            "      field SFNode  transform USE TRANSFORM\n"+
            "      directOutput TRUE\n"+
            "      url  \"javascript:\n"+
            "         function initialize() {\n"+
            "            // Calculate vector for cylinder\n"+
            "            var vecCylinder;\n"+
            "            vecCylinder = vertex0.subtract(vertex1);\n"+
            "            // Calculate length and store into scale factor\n"+
            "            transform.scale = new SFVec3f (1,vecCylinder.length(),1);\n"+
            "            // Calculate translation (average of vertices) and store\n"+
            "            var vecTranslation;\n"+
            "            vecTranslation = vertex0.add(vertex1).divide(2);\n"+
            "            transform.translation = vecTranslation;\n"+
            "            // Calculate rotation (rotation that takes vector 0 1 0 to vecCylinder).\n"+
            "            var rotTransform;\n"+
            "            rotTransform = new SFRotation(new SFVec3f(0,1,0),vecCylinder);\n"+
            "            transform.rotation = rotTransform;\n"+
            "            // Done\n"+
            "            return;\n"+
            "         }\n"+
            "      \"\n"+
            "   }\n"+
            "}");


            pw.println("PROTO BlueVertice [] { Shape {");
            pw.println("appearance Appearance { material Material { diffuseColor 0 0 1 } }");
            pw.println("geometry Sphere{radius 0.06}");
            pw.println("} }");

            pw.println("PROTO GreenVertice [] { Shape {");
            pw.println("appearance Appearance { material Material { diffuseColor 0 1 0 } }");
            pw.println("geometry Sphere{radius 0.06}");
            pw.println("} }");

            pw.println("PROTO CyanVertice [] { Shape {");
            pw.println("appearance Appearance { material Material { diffuseColor 0 1 1 } }");
            pw.println("geometry Sphere{radius 0.06}");
            pw.println("} }");

            pw.println("PROTO YellowVertice [] { Shape {");
            pw.println("appearance Appearance { material Material { diffuseColor 1 1 0 } }");
            pw.println("geometry Sphere{radius 0.06}");
            pw.println("} }");

            pw.println("PROTO WhiteVertice [] { Shape {");
            pw.println("appearance Appearance { material Material { diffuseColor 1 1 1 } }");
            pw.println("geometry Sphere{radius 0.06}");
            pw.println("} }");

            /*
            double xs = 0, ys = 0, zs = 0;
            //double xs = 0.5, ys = Math.sqrt(3)/6.0, zs = - Math.sqrt(r*r-1.0/3.0);;
            pw.println("Transform {");
            pw.println(String.format("translation  %.6f %.6f %.6f",xs,ys,zs));
            pw.println("children Shape {");
            pw.println("appearance Appearance { material Material { transparency 0.7 diffuseColor 1 1 1 } }");
            pw.println(String.format("geometry Sphere{radius %.6f}",r));
            pw.println("} }"); */

            for (EGemEdge e: _edges) {
                EGemVertex u = (EGemVertex)e.getVertices().getFirst();
                EGemVertex v = (EGemVertex)e.getVertices().getSecond();

                //if (!S.contains(specialVertex) || !S.contains(v))
                //    continue;

                if (u == specialVertex || v == specialVertex)
                    continue;

                /*
                if ((!problems1.contains(u) && !problems2.contains(u)) ||
                    (!problems1.contains(v) && !problems2.contains(v)))
                    continue;*/



                double x1 = u.getX3d(), y1 = u.getY3d(), z1 = u.getZ3d();
                double x2 = v.getX3d(), y2 = v.getY3d(), z2 = v.getZ3d();
                double vx = x2-x1, vy = y2-y1, vz = z2-z1;
                double length = Math.sqrt(vx*vx+vy*vy+vz*vz);
                vx = vx/length;
                vy = vy/length;
                vz = vz/length;

                double ux = 0, uy = 1, uz = 0;
                double wx = uy*vz-vz*uy, wy = uz*vx-ux*vz, wz = ux*vy-uy*vx;

                // 0,1,0 . X2-X1
                double theta = Math.acos(ux*vx+uy*vy+uz*vz);

                pw.println(String.format(
                   "ConnectingCylinder { radius 0.01 vertex0 %.8f %.8f %.8f vertex1 %.8f %.8f %.8f }",
                   x1,y1,z1,x2,y2,z2));

            }

            for (EGemVertex v: _vertices) {
                if (v == specialVertex)
                    continue;
                if (v.getX3d() == Double.NaN) {
                    System.out.println("Problema");
                    continue;
                }
                pw.println("Transform {");
                pw.println(String.format("translation  %.6f %.6f %.6f",v.getX3d(),v.getY3d(),v.getZ3d()));

                if (problems1.contains(v)&&problems2.contains(v)) {
                    pw.println("children CyanVertice {}");
                }
                else if (problems1.contains(v)) {
                    pw.println("children GreenVertice {}");
                }
                else if (problems2.contains(v)) {
                    pw.println("children BlueVertice {}");
                }
                else if (S.contains(v)) {
                    pw.println("children YellowVertice {}");
                }
                else {
                    pw.println("children WhiteVertice {}");
                }
                pw.println("}");
            }
            pw.flush();
            pw.close();
        } catch (FileNotFoundException ex) {
        }
    }

    private boolean intersect(double x0, double y0, double z0,
                              double xc, double yc, double zc,
                              double xs, double ys, double zs, double r,
                              double[] output) {
        // System.out.println(String.format("proj. %.6f %.6f %.6f",x0,y0,z0));

        double xc2 = xc*xc, x02 = x0*x0, xs2 = xs*xs;
        double yc2 = yc*yc, y02 = y0*y0, ys2 = ys*ys;
        double zc2 = zc*zc, z02 = z0*z0, zs2 = zs*zs;
        double r2 = r*r;

        double aux1 = -yc*y0+ys*y0-xc*xs-x0*xc-zc*zs+xs*x0-yc*ys-zc*z0+yc2+zs*z0+zc2+xc2;
        double aux2 = (2*x0*xc*zs2+z02*r2-z02*yc2-zc2*ys2-zc2*xs2+zc2*r2-2*yc*y0*xc*xs+2*yc*y0*x0*xc-2*yc*y0*zc*zs-2*yc*y0*xs*x0+2*yc*y0*zc*z0-2*yc*y0*zs*z0-2*ys*y0*xc*xs-2*ys*y0*x0*xc-2*ys*y0*zc*zs+2*ys*y0*xs*x0-2*ys*y0*zc*z0+2*ys*y0*zs*z0+2*xc*xs*zc*zs+2*xc*xs*yc*ys-2*xc*xs*zc*z0-2*xc*xs*zs*z0-2*x0*xc*zc*zs-2*x0*xc*yc*ys+2*x0*xc*zc*z0-2*x0*xc*zs*z0-x02*ys2-x02*zc2-x02*zs2+x02*r2-x02*yc2-xc2*ys2-xc2*zs2-2*zc*zs*xs*x0+2*zc*zs*yc*ys+xc2*r2-y02*zc2-y02*zs2-y02*xc2-y02*xs2+y02*r2-yc2*zs2-yc2*xs2+yc2*r2-z02*ys2-z02*xc2-z02*xs2+2*xs*x0*yc2+2*xs*x0*zc2+2*yc2*zs*z0+2*zs*z0*xc2-2*x0*xc*r2+2*yc*y0*zs2+2*yc*y0*xs2-2*yc*y0*r2+2*x02*zc*zs+2*x02*yc*ys+2*y02*zc*zs+2*y02*xc*xs+2*z02*xc*xs+2*z02*yc*ys+2*zc*z0*ys2+2*zc*z0*xs2-2*zc*z0*r2-2*xs*x0*yc*ys-2*xs*x0*zc*z0+2*xs*x0*zs*z0-2*yc*ys*zc*z0-2*yc*ys*zs*z0+2*ys*y0*zc2+2*ys*y0*xc2+2*x0*xc*ys2);
        double aux3 = (-2*x0*xc-2*yc*y0+x02+xc2+y02+yc2+z02+zc2-2*zc*z0);

        double t1 = (aux1+Math.sqrt(aux2))/aux3;
        double t2 = (aux1-Math.sqrt(aux2))/aux3;
        boolean t1Real = (t1 != Double.NaN &&
                          t1 != Double.NEGATIVE_INFINITY &&
                          t1 != Double.POSITIVE_INFINITY);

        boolean t2Real = (t2 != Double.NaN &&
                          t2 != Double.NEGATIVE_INFINITY &&
                          t2 != Double.POSITIVE_INFINITY);

        // System.out.println(String.format("t1, t2: %.6f %.6f",t1,t2));

        // give priority to the smallest t greater than zero
        if (!t1Real && !t2Real) {
            return false; // doesn't have real solution
        }
        else if (!t1Real) {
            double t = t2;
            output[0] = xc+(x0-xc)*t;
            output[1] = yc+(y0-yc)*t;
            output[2] = zc+(z0-zc)*t;
        }
        else if (!t2Real) {
            double t = t1;
            output[0] = xc+(x0-xc)*t;
            output[1] = yc+(y0-yc)*t;
            output[2] = zc+(z0-zc)*t;
        }
        else {
            double t = Math.max(t1,t2);
            output[0] = xc+(x0-xc)*t;
            output[1] = yc+(y0-yc)*t;
            output[2] = zc+(z0-zc)*t;
        }
        // System.out.println(String.format("result. %.6f %.6f %.6f",output[0],output[1],output[2]));
        return true;
    }



}

class EGemVertex {
    private Component _triball;
    private int _label;
    private ArrayList<EGemEdge> _edges;
    private double _position2d[] = {0,0};
    private double _position3d[] = {0,0,0};
    public EGemVertex(int label,Component triball) {
        _label = label;
        _edges = new ArrayList<EGemEdge>();
        _triball = triball;
    }
    public void setPosition2d(double x, double y) {
        _position2d[0] = x;
        _position2d[1] = y;
    }
    public void setPosition3d(double x, double y, double z) {
        _position3d[0] = x;
        _position3d[1] = y;
        _position3d[2] = z;
    }
    public double getX2d() { return _position2d[0]; }
    public double getY2d() { return _position2d[1]; }
    public double getX3d() { return _position3d[0]; }
    public double getY3d() { return _position3d[1]; }
    public double getZ3d() { return _position3d[2]; }
    public int getLabel() {
        return _label;
    }
    public int getNumberOfEdges() {
        return _edges.size();
    }
    public EGemEdge getEdge(int index) {
        return _edges.get(index);
    }
    public void addEdge(EGemEdge e) {
        _edges.add(e);
    }
    public ArrayList<EGemEdge> getEdges() {
        return _edges;
    }
    public void permuteEdges(ArrayList<EGemEdge> p) {
        if (!_edges.containsAll(p) || _edges.size() != p.size() ||
            (new HashSet(_edges).size()) != (new HashSet(p).size()))
            throw new RuntimeException("OOopppsss");
        _edges.clear();
        _edges.addAll(p);
    }
    public void removeEdgesWithVertex(EGemVertex v) {
        for (int i=_edges.size()-1;i>=0;i--) {
            if (_edges.get(i).isIncidentTo(v))
                _edges.remove(i);
        }
    }
    public void removeEdge(EGemEdge e) {
        for (int i=_edges.size()-1;i>=0;i--) {
            if (e == _edges.get(i))
                _edges.remove(i);
        }
    }
    public String getOriginLabel() {
        String stColors = GemColor.getColorSetCompactString(_triball.getColorSet());
        return _triball.getVertex().getLabel()+":"+stColors;
    }
    public Component getTriball() {
        return _triball;
    }

    public int degree() {
        return _edges.size();
    }

    public int degree(HashSet<EGemVertex> S) {
        int degree = 0;
        for (EGemEdge e: _edges) {
            EGemVertex v = e.getOpposite(this);
            if (S.contains(v))
                degree++;
        }
        return degree;
    }

    public boolean isAdjacentTo(EGemVertex v) {
        for (EGemEdge e: _edges)
            if (e.getOpposite(this) == v)
                return true;
        return false;
    }

}

class EGemEdge {
    private Component _bigon;
    private EGemVertex _u;
    private EGemVertex _v;
    public EGemEdge(EGemVertex u, EGemVertex v, Component bigon) {
        _u = u;
        _v = v;
        _bigon = bigon;
    }
    public Pair getVertices() { return new Pair(_u,_v); }
    public EGemVertex getOpposite(EGemVertex w) { return (_u == w ? _v : _u); }
    public boolean isIncidentTo(EGemVertex w) {
        return (w == _u || w == _v);
    }
    public String getOriginLabel() {
        String stColors = GemColor.getColorSetCompactString(_bigon.getColorSet());
        return _bigon.getVertex().getLabel()+":"+stColors;
    }
}
