package blink;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Stack;

import javax.imageio.ImageIO;
import javax.vecmath.Vector2d;

import org.jscience.mathematics.numbers.Complex;

/**
 * GBlinkEmbeddingGraph is a graph built from a GBlink. It
 * is 3-connected and can be embedded on a plane as a kissing disk
 * configuration. See theorem of Koebe.
 */
public class GBlinkEmbeddingGraph {

    /**
     * Original GBlink
     */
    GBlink _gBlink;

    /**
     * Original GBlink
     */
    private ArrayList<GBlinkEmbeddingGraphVertex> _vertices = new ArrayList<GBlinkEmbeddingGraphVertex>();
    private ArrayList<GBlinkEmbeddingGraphVertex> _crossingVertices = new ArrayList<GBlinkEmbeddingGraphVertex>();
    private ArrayList<GBlinkEmbeddingGraphVertex> _edgeVertices = new ArrayList<GBlinkEmbeddingGraphVertex>();
    private ArrayList<GBlinkEmbeddingGraphVertex> _whiteFaceVertices = new ArrayList<GBlinkEmbeddingGraphVertex>();
    private ArrayList<GBlinkEmbeddingGraphVertex> _blackFaceVertices = new ArrayList<GBlinkEmbeddingGraphVertex>();

    /**
     * Edges (i.e. 1-simplexes) of this graph
     */
    private ArrayList<GBlinkEmbeddingGraphEdge> _edges = new ArrayList<GBlinkEmbeddingGraphEdge>();

    private static Random _random = new Random(87718211L);

    /**
     * Constructor of the GBlinkEmbeddingGraph (a simplicial 2-complex).
     */
    public GBlinkEmbeddingGraph(GBlink G) {

        _gBlink = G;

        HashMap<Object,GBlinkEmbeddingGraphVertex> map2Vertex = new HashMap<Object,GBlinkEmbeddingGraphVertex>();

        HashMap<Object,GBlinkEmbeddingGraphVertex> mapGBlinkVertex2WhiteFace = new HashMap<Object,GBlinkEmbeddingGraphVertex>();
        HashMap<Object,GBlinkEmbeddingGraphVertex> mapGBlinkVertex2BlackFace = new HashMap<Object,GBlinkEmbeddingGraphVertex>();
        HashMap<Object,GBlinkEmbeddingGraphVertex> mapGBlinkVertex2CrossingFace = new HashMap<Object,GBlinkEmbeddingGraphVertex>();
        HashMap<Object,GBlinkEmbeddingGraphVertex> mapGBlinkVertex2AngleEdge = new HashMap<Object,GBlinkEmbeddingGraphVertex>();


        // mount auxiliar structure that maps GBlinkVertex to
        // its incident faces (WhiteFace, BlackFace and CrossingFace)

        // these are the edge vertices
        for (GBlinkVertex v: G.getVertices()) {
            if (v.hasOddLabel()) {
                GBlinkEmbeddingGraphVertex angleEdgeVertex = this.newVertex(v,GBlinkEmbeddingGraphVertexType.edge);
                map2Vertex.put(v,angleEdgeVertex);
                map2Vertex.put(v.getNeighbour(GBlinkEdgeType.edge),angleEdgeVertex);

                // put both vertices on the game...
                mapGBlinkVertex2AngleEdge.put(v,angleEdgeVertex);
                mapGBlinkVertex2AngleEdge.put(v.getNeighbour(GBlinkEdgeType.edge),angleEdgeVertex);
            }
        }

        // these are the crossing vertices
        for (Variable v: G.getGEdges()) {
            GBlinkEmbeddingGraphVertex crossingVertex = this.newVertex(v,GBlinkEmbeddingGraphVertexType.crossing);
            map2Vertex.put(v,crossingVertex);

            // important to edges definition
            for (GBlinkVertex vv : v.getVertices())
                mapGBlinkVertex2CrossingFace.put(vv,crossingVertex);
        }

        // these are the face vertices
        for (Variable v: G.getGVertices()) {
            GBlinkEmbeddingGraphVertex blackFaceVertex = this.newVertex(v,GBlinkEmbeddingGraphVertexType.blackFace);
            map2Vertex.put(v,blackFaceVertex);

            // important to edges definition
            for (GBlinkVertex vv : v.getVertices())
                mapGBlinkVertex2BlackFace.put(vv,blackFaceVertex);
        }

        // these are whiteFace
        for (Variable v: G.getGFaces()) {
            GBlinkEmbeddingGraphVertex whiteFaceVertex = this.newVertex(v,GBlinkEmbeddingGraphVertexType.whiteFace);
            map2Vertex.put(v,whiteFaceVertex);

            // important to edges definition
            for (GBlinkVertex vv : v.getVertices())
                mapGBlinkVertex2WhiteFace.put(vv,whiteFaceVertex);
        }

        // create the adjacency list part of Crossing and Edges
        for (GBlinkEmbeddingGraphVertex crossingVertex: _crossingVertices) {
            Variable gEdge = (Variable) crossingVertex.getObject();

            // get vertices on conterclockwise-orientation
            GBlinkVertex v0 = gEdge.getVertices().get(0);
            if (v0.hasOddLabel())
                v0 = v0.getNeighbour(GBlinkEdgeType.face);
            GBlinkVertex v1 = v0.getNeighbour(GBlinkEdgeType.face);
            GBlinkVertex v2 = v0.getNeighbour(GBlinkEdgeType.diagonal);
            GBlinkVertex v3 = v0.getNeighbour(GBlinkEdgeType.vertex);

            // follow the schema on my notebook

            GBlinkEmbeddingGraphVertex neighbourVertex;
            GBlinkEmbeddingGraphVertexType types[] = {
                                                     GBlinkEmbeddingGraphVertexType.edge,
                                                     GBlinkEmbeddingGraphVertexType.blackFace,
                                                     GBlinkEmbeddingGraphVertexType.edge,
                                                     GBlinkEmbeddingGraphVertexType.whiteFace,
                                                     GBlinkEmbeddingGraphVertexType.edge,
                                                     GBlinkEmbeddingGraphVertexType.blackFace,
                                                     GBlinkEmbeddingGraphVertexType.edge,
                                                     GBlinkEmbeddingGraphVertexType.whiteFace};
            Object linkingObject[] = {v0,v0,v1,v1,v2,v2,v3,v3};

            // create the 8 edges that are adjacent to each crossing
            for (int i=0;i<8;i++) {
                neighbourVertex = null;
                if (types[i] == GBlinkEmbeddingGraphVertexType.edge) {
                    neighbourVertex = mapGBlinkVertex2AngleEdge.get(linkingObject[i]);
                }
                else if (types[i] == GBlinkEmbeddingGraphVertexType.blackFace) {
                    neighbourVertex = mapGBlinkVertex2BlackFace.get(linkingObject[i]);
                }
                else if (types[i] == GBlinkEmbeddingGraphVertexType.whiteFace) {
                    neighbourVertex = mapGBlinkVertex2WhiteFace.get(linkingObject[i]);
                }
                else throw new RuntimeException();
                if (neighbourVertex != null) {
                    GBlinkEmbeddingGraphEdge edge = this.newEdge(crossingVertex, neighbourVertex, linkingObject[i]);
                    crossingVertex.addEdge(edge);
                }
            }
        }

        // edge crossing must be searched on the list
        for (GBlinkEmbeddingGraphVertex angleEdgeVertex: _edgeVertices) {
            // make v0 a even labeled vertex
            GBlinkVertex v0 = (GBlinkVertex) angleEdgeVertex.getObject();
            if (v0.hasOddLabel())
                v0 = v0.getNeighbour(GBlinkEdgeType.edge);
            GBlinkVertex v1 = v0.getNeighbour(GBlinkEdgeType.edge);

            GBlinkEmbeddingGraphEdge edge;
            GBlinkEmbeddingGraphVertex neighbourVertex;

            // get crossing on v0
            edge = findEdge(GBlinkEmbeddingGraphVertexType.crossing,GBlinkEmbeddingGraphVertexType.edge,v0);
            if (edge == null) throw new RuntimeException();
            angleEdgeVertex.addEdge(edge);

            // get WhiteFace on v0
            neighbourVertex = mapGBlinkVertex2WhiteFace.get(v0);
            edge = this.newEdge(angleEdgeVertex, neighbourVertex,v0);
            angleEdgeVertex.addEdge(edge);

            // get crossing on v1
            edge = findEdge(GBlinkEmbeddingGraphVertexType.crossing,GBlinkEmbeddingGraphVertexType.edge,v1);
            if (edge == null) throw new RuntimeException();
            angleEdgeVertex.addEdge(edge);

            // get VertexFace on v1
            neighbourVertex = mapGBlinkVertex2BlackFace.get(v1);
            edge = this.newEdge(angleEdgeVertex, neighbourVertex,v1);
            angleEdgeVertex.addEdge(edge);
        }

        // edge crossing must be searched on the list
        for (GBlinkEmbeddingGraphVertex whiteFaceVertex: _whiteFaceVertices) {
            Variable gWhiteFace = (Variable) whiteFaceVertex.getObject();

            // number of GBlinkVertex on this whiteFace
            int n = gWhiteFace.size();

            // make v0 a even labeled vertex
            GBlinkVertex v0 = gWhiteFace.getVertices().get(0);
            if (v0.hasOddLabel())
                v0 = v0.getNeighbour(GBlinkEdgeType.edge);

            // for each vertex search for the
            GBlinkEmbeddingGraphEdge edge;

            int i=0;
            GBlinkVertex v = v0;
            while (i < n) {
                // get angleEdge on v
                edge = this.findEdge(GBlinkEmbeddingGraphVertexType.edge,GBlinkEmbeddingGraphVertexType.whiteFace,v,v.getNeighbour(GBlinkEdgeType.edge));
                if (edge == null) throw new RuntimeException();
                whiteFaceVertex.addEdge(edge);

                // get crossing on v
                edge = this.findEdge(GBlinkEmbeddingGraphVertexType.crossing,GBlinkEmbeddingGraphVertexType.whiteFace,v,v.getNeighbour(GBlinkEdgeType.vertex));
                if (edge == null) throw new RuntimeException();
                whiteFaceVertex.addEdge(edge);

                i+=2;
                v = v.getNeighbour(GBlinkEdgeType.vertex).getNeighbour(GBlinkEdgeType.edge);
            }
        }


        // edge crossing must be searched on the list
        for (GBlinkEmbeddingGraphVertex blackFaceVertex: _blackFaceVertices) {
            Variable gBlackFace = (Variable) blackFaceVertex.getObject();

            // number of GBlinkVertex on this whiteFace
            int n = gBlackFace.size();

            // make v0 a even labeled vertex
            GBlinkVertex v0 = gBlackFace.getVertices().get(0);
            if (v0.hasOddLabel())
                v0 = v0.getNeighbour(GBlinkEdgeType.edge);

            // for each vertex search for the
            GBlinkEmbeddingGraphEdge edge;

            int i=0;
            GBlinkVertex v = v0;
            while (i < n) {
                // get angleEdge on v
                edge = this.findEdge(GBlinkEmbeddingGraphVertexType.crossing,GBlinkEmbeddingGraphVertexType.blackFace,v,v.getNeighbour(GBlinkEdgeType.face));
                if (edge == null) throw new RuntimeException();
                blackFaceVertex.addEdge(edge);

                // get crossing on v
                edge = this.findEdge(GBlinkEmbeddingGraphVertexType.edge,GBlinkEmbeddingGraphVertexType.blackFace,v,v.getNeighbour(GBlinkEdgeType.edge));
                if (edge == null) throw new RuntimeException();
                blackFaceVertex.addEdge(edge);

                i+=2;
                v = v.getNeighbour(GBlinkEdgeType.edge).getNeighbour(GBlinkEdgeType.face);
            }
        }

        //
        // this.completeDegree2Vertices();

        // this.removeAllButTheLargestWhiteFace();

        //
        // this.cleanEdges();
    }

    public void removeAllButTheLargestWhiteFace() {
        // external face (it's neighbours will be horocycle with it)
        // find the face with the most neighbours
        GBlinkEmbeddingGraphVertex externalFace = null;
        for (GBlinkEmbeddingGraphVertex v:  _whiteFaceVertices) {
            if (externalFace == null) {
                externalFace = v;
            }
            else if (v.numEdges() > externalFace.numEdges()) {
                externalFace = v;
            }
        }

        // removed vertices
        HashSet<GBlinkEmbeddingGraphVertex> removedVertices = new HashSet<GBlinkEmbeddingGraphVertex>();
        for (GBlinkEmbeddingGraphVertex v : _vertices) {
            if ((v.getType() == GBlinkEmbeddingGraphVertexType.blackFace ||
                v.getType() == GBlinkEmbeddingGraphVertexType.whiteFace)  && v != externalFace) {
                removedVertices.remove(v);
            }
        }

        // removed vertices
        HashSet<GBlinkEmbeddingGraphEdge> removedEdges = new HashSet<GBlinkEmbeddingGraphEdge>();
        for (GBlinkEmbeddingGraphEdge e : _edges) {
            if (removedVertices.contains(e.getU()) || removedVertices.contains(e.getV())) {
                removedEdges.add(e);
            }
        }

        _vertices.removeAll(removedVertices);
        _edges.removeAll(removedEdges);

        for (GBlinkEmbeddingGraphVertex v : _vertices) {
            v._edges.retainAll(removedEdges);
        }
        _blackFaceVertices.removeAll(removedEdges);
        _whiteFaceVertices.removeAll(removedEdges);

    }



    /**
     * Set the center of w based on u and angle and if it is
     * an hyperbolic case.
     */
    private void setCenter(GBlinkEmbeddingGraphVertex u, GBlinkEmbeddingGraphVertex w, double angle, boolean hyperbolic) {
        double ur = u.getRadii();
        double wr = w.getRadii();
        Vector2d vector = new Vector2d(Math.cos(angle), Math.sin(angle));
        if (hyperbolic) {
            // is w an horocycle?
            double hur = -Math.log(ur)/2.0;
            double hwr = 0.0;
            if (wr > 1e-10) { // not horocycle
                hwr = -Math.log(wr)/2.0;
            }
            else {
                System.out.println("Tagging v"+w.getLabel()+" as an horocycle");
                w.setHorocycleDirectionVertex(u);
            }
            double distance = hur + hwr;
            double ed = Math.exp(distance);
            double scale = (ed-1)/(ed+1);

            vector.scale(scale);

            w.setPosition(vector.x, vector.y);
        }
        else { // euclidean case
            vector.scale(ur+wr);
            w.setPosition(vector.x, vector.y);
        }
        w.setFlag(true);
    }

    /**
     * Add new edge to this graph
     */
    private int _nextFreeEdgeLabel = 1;
    private GBlinkEmbeddingGraphEdge newEdge(GBlinkEmbeddingGraphVertex u, GBlinkEmbeddingGraphVertex v, Object linkingObject) {
        GBlinkEmbeddingGraphEdge edge = new GBlinkEmbeddingGraphEdge(_nextFreeEdgeLabel++, u, v, linkingObject);
        _edges.add(edge);
        System.out.println("Adding: "+edge);
        return edge;
    }

    /**
     * Add new vertex to this graph
     */
    private int _nextFreeVertexLabel = 1;
    private GBlinkEmbeddingGraphVertex newVertex(Object object, GBlinkEmbeddingGraphVertexType type) {
        GBlinkEmbeddingGraphVertex vertex = new GBlinkEmbeddingGraphVertex(_nextFreeVertexLabel++, object, type);
        _vertices.add(vertex);
        if (type == GBlinkEmbeddingGraphVertexType.blackFace)
            _blackFaceVertices.add(vertex);
        else if (type == GBlinkEmbeddingGraphVertexType.whiteFace)
            _whiteFaceVertices.add(vertex);
        else if (type == GBlinkEmbeddingGraphVertexType.crossing)
            _crossingVertices.add(vertex);
        else if (type == GBlinkEmbeddingGraphVertexType.edge)
            _edgeVertices.add(vertex);

        System.out.println("Adding Vertex "+vertex);


        //System.out.println("Adding: "+edge);
        return vertex;
    }

    /**
     * Find edge matching some condition.
     */
    private GBlinkEmbeddingGraphEdge findEdge(GBlinkEmbeddingGraphVertexType t1, GBlinkEmbeddingGraphVertexType t2, Object ... linkingObject) {
        for (GBlinkEmbeddingGraphEdge e: _edges)
            if (e.test(t1,t2,linkingObject))
                return e;
        return null;
    }


    /**
     * Log Graph
     */
    public void logGraph() {
        for (GBlinkEmbeddingGraphVertex v: _vertices) {
            System.out.println(""+v.edgesString());
        }
    }

    /**
     * Find vertex by label
     */
    public GBlinkEmbeddingGraphVertex findVertexByLabel(int label) {
        for (GBlinkEmbeddingGraphVertex v: _vertices) {
            if (v.getLabel() == label)
                return v;
        }
        return null;
    }


    /**
     * Algorithm as described on the article "A Circle Packing Algorithm" by
     * Charles R. Collins and Kenneth Stephenson.
     *
     * The position and radii in the hyperbolic case are stored as
     * in a special way:
     *     s = exp(-2h)
     * where h is the hyperbolic value and s is the value stored. To
     * get from special to the hyperbolic:
     *     h = -ln(s)/2
     */
    public void findPackingLabel(boolean hyperbolic) throws IOException {

        boolean euclidean = !hyperbolic;

        /////////////////////////
        //
        // Phase 1.
        //
        // Find the radii (the packing label)
        //

        // external face (it's neighbours will be horocycle with it)
        // find the face with the most neighbours
        GBlinkEmbeddingGraphVertex externalFace = null;
        for (GBlinkEmbeddingGraphVertex v:  _whiteFaceVertices) {
            if (externalFace == null) {
                externalFace = v;
            }
            else if (v.numEdges() > externalFace.numEdges()) {
                externalFace = v;
            }
        }

        // white face
        System.out.println("External white face "+externalFace);

        // remove boundary vertices
        ArrayList<GBlinkEmbeddingGraphVertex> boundaryVertices = new ArrayList<GBlinkEmbeddingGraphVertex>();
        for (GBlinkEmbeddingGraphEdge e: externalFace.getEdges()) {
            GBlinkEmbeddingGraphVertex w = e.getOpposite(externalFace);
            System.out.println("Boundary Vertex: "+w);
            boundaryVertices.add(w);
        }

        // mount list of internal vertices
        ArrayList<GBlinkEmbeddingGraphVertex> uList = (ArrayList<GBlinkEmbeddingGraphVertex>) _vertices.clone();
        uList.removeAll(boundaryVertices);
        uList.remove(externalFace);

        // set current labeling g
        if (euclidean) {
            for (GBlinkEmbeddingGraphVertex w : boundaryVertices) {
                if (w.getType() == GBlinkEmbeddingGraphVertexType.crossing) {
                    w.setRadii(0.5);
                } else
                    w.setRadii(0.2);
            }
            // radius of boundary
            double radius=1;
            for (GBlinkEmbeddingGraphVertex w: boundaryVertices) {
                w.setRadii(radius);
                // radius = radius * 1.5;
            }
        }
        else {
            // maximal packing
            for (GBlinkEmbeddingGraphVertex w: boundaryVertices) {
                w.setRadii(0);
            }
        }

        // randomly choose an radii for each internal (or free) vertex.
        for (GBlinkEmbeddingGraphVertex u: uList) {
            u.setRadii(0.2);
        }

        //
        double epsilon = 1e-5;
        double delta = 0.05;

        //
        double c = epsilon+1;
        double lambda = -1.0;
        int flag = 0;

        // find radiis with given precision
        int iter =1;
        while (c > epsilon) {

            double c0 = c;
            double lambda0 = lambda;
            int flag0 = flag;
            c = 0.0; // esta linha não está no programa

            // R0 <- R
            for (GBlinkEmbeddingGraphVertex u: _vertices)
                u.assignRadiiToRadii0();


            for (GBlinkEmbeddingGraphVertex u: uList) {

                // calculate angle sum theta
                int k = u.numEdges();
                boolean useRadii0 = true;
                double theta = u.calculateAngleSum(useRadii0,hyperbolic);

                // calculate angle sum theta
                double beta = Math.sin(theta/(2*k));
                double localDelta = Math.sin(2*Math.PI/(2*k));

                double newU;

                // see page 11 on the article
                if (euclidean) {
                    double u1 = beta/(1-beta)*u.getRadii0();
                    double u2 = (1 - localDelta)/localDelta * u1;
                    newU = u2;
                }
                else { // hyperbolic
                    double Ru = u.getRadii0();
                    double sqrtRu = Math.sqrt(Ru);
                    double u1 = (beta - sqrtRu) / (beta*Ru - sqrtRu);
                    if (u1 < 0) u1 = 0;
                    double oneMinusU1 = 1-u1;
                    //double oneMinusU1 = beta*(Ru-1)/(sqrtU*(1-beta*sqrtU));
                    double t = 2 * localDelta / (Math.sqrt(oneMinusU1*oneMinusU1 + 4*localDelta*localDelta*u1) + oneMinusU1);
                    double u2 = t*t;
                    newU = u2;
                }

                u.setRadii(newU);
                //System.out.println(String.format("Updated radii of %s from %10.6f to %10.6f",u.toString(),u.getRadii0(),u.getRadii()));

                // accumulate error using old theta (not the one that could be calculated
                // with the just updated "u"-radii).
                // theta = u.calculateAngleSum(false);
                double localError = (theta - Math.PI*2)*(theta - Math.PI*2);
                c = c + localError;
                // System.out.println(String.format("localError %10.6f     cut = %10.6f",localError,c));
            }

            //
            c = Math.sqrt(c);
            lambda = c/c0;
            flag = 1;

            //
            if (flag0 == 1 && lambda < 1) {
                c = lambda*c;
                if (Math.abs(lambda-lambda0) < delta) {
                    lambda = lambda/(1-lambda);
                }
                double lambdaStar = 1e100;
                for (GBlinkEmbeddingGraphVertex u: uList) {
                    double u_minus_u0 = u.getRadii()-u.getRadii0();
                    if (hyperbolic) {
                        //  0 < u + lambda*(u-u0) < 1
                        if (u_minus_u0 < 0) {
                            double lambdaCandidate = -u.getRadii() / u_minus_u0;
                            if (lambdaCandidate < lambdaStar)
                                lambdaStar = lambdaCandidate;
                        }
                        else {
                            double lambdaCandidate = (1-u.getRadii()) / u_minus_u0;
                            if (lambdaCandidate < lambdaStar)
                                lambdaStar = lambdaCandidate;
                        }
                    }
                    else {
                        if (u_minus_u0 < 0) { // there is an upperbound here
                            double lambdaCandidate = -u.getRadii() / u_minus_u0;
                            if (lambdaCandidate < lambdaStar)
                                lambdaStar = lambdaCandidate;
                        }
                        // else not an upperbound here
                    }
                }
                lambda = Math.min(lambda,0.5*lambdaStar);
                // System.out.println(String.format("lambda = %10.6f",lambda));
                for (GBlinkEmbeddingGraphVertex u: uList) {
                    double u_minus_u0 = u.getRadii()-u.getRadii0();
                    u.setRadii(u.getRadii() + lambda*u_minus_u0);
                    // System.out.println(String.format("Acceleration Step updated radii of %s from %10.6f to %10.6f",u.toString(),u.getRadii0(),u.getRadii()));
                }
                flag = 0;
            }

            //
            System.out.println(String.format("Iter: %3d  cut %10.6f  eps %10.6f",iter++,c,epsilon));

        }

        // test condition independently
        for (GBlinkEmbeddingGraphVertex u: uList) {
            double theta = u.calculateAngleSum(false,hyperbolic);
            if (Math.abs(theta-Math.PI*2) > 1e-4) {
                System.out.println("<ERROR> Problem at internal vertex "+u+" theta = "+theta);
                Toolkit.getDefaultToolkit().beep();
            }
        }

        // log the radii
        for (GBlinkEmbeddingGraphVertex u: _vertices) {
            System.out.println(String.format("v%-3d has radii %10.6f angle sums to %10.6f",u.getLabel(),u.getRadii(),u.calculateAngleSum(false,hyperbolic)));
        }

        /////////////////////////
        //
        // Phase 2.
        //
        // Time to position the centers of the circles.
        //

        // unmark all vertices
        for (GBlinkEmbeddingGraphVertex v: _vertices)
            v.setFlag(false);

        GBlinkEmbeddingGraphVertex firstVertex = uList.get(0);
        // GBlinkEmbeddingGraphVertex firstVertex = w1;

        // this is true for both: the euclidean and hyperbolic cases
        firstVertex.setPosition(0,0);
        firstVertex.setFlag(true);

        Stack<GBlinkEmbeddingGraphVertex> S = new Stack<GBlinkEmbeddingGraphVertex>();
        S.push(firstVertex);

        int iterArrange = 0;

        HashSet<GBlinkEmbeddingGraphVertex> P = new HashSet<GBlinkEmbeddingGraphVertex>();
        while (!S.isEmpty()) {

            iterArrange++;

            GBlinkEmbeddingGraphVertex u = S.pop();
            P.add(u);

            // do not treat boundary vertices
            if (boundaryVertices.contains(u))
                continue;

            System.out.println("Arrange Iteration "+iterArrange+"    vertex "+u);


            // begin translating all positioned objects by the vector (0,0)-u
            // see hyperbolic isometry for hyperbolic case. The horocylces are
            // stored as their "south pole" with the direction from the circle.
            double x0,y0;
            x0 = u.getX();
            y0 = u.getY();
            Complex z0 = Complex.valueOf(u.getX(),u.getY()); // save position
            System.out.println("Apply isometry that takes "+u.getLabel()+" to origin...");
            for (GBlinkEmbeddingGraphVertex v: _vertices) {
                // if vertex has a position already defined
                if (v.getFlag()) {
                    if (hyperbolic) { // hyperbolic
                        Complex z  = Complex.valueOf(v.getX(),v.getY());
                        Complex tz = this.applyIsometry(z,z0);//   z.minus(z0).divide(Complex.ONE.minus(z0.conjugate().times(z)));
                        v.setPosition(tz.getReal(),tz.getImaginary());
                    }
                    else { // euclidean
                        v.setPosition(v.getX()-x0,v.getY()-y0);
                    }
                }
            }
            System.out.println("Finished applying isometry...");

            // log drawings
            for (GBlinkEmbeddingGraphVertex v : _vertices) {
                this.adjustHyperbolicDrawingPositionAndRadii(v);
            }
            //this.drawState(1000,1000,true,false,false,"log/images/link"+Library.fillStringWithChar(""+iterArrange,4,'0',true)+"-01.jpg");


            // number of edges
            int k = u.numEdges();

            // search for some defined position
            int countUndefined = 0;
            for (int i = 0; i < u.numEdges(); i++) {
                GBlinkEmbeddingGraphVertex v = u.getEdge(i).getOpposite(u);
                if (!v.getFlag())
                    countUndefined++;
            }

            // not one undefined nodes
            if (countUndefined == 0)
                continue;

            // find first position where u,v,v+1,v+2  where v+2 is
            // undefined and the rest is defined.
            int index = -1;
            for (int i = 0; i < u.numEdges(); i++) {
                GBlinkEmbeddingGraphVertex v = u.getEdge(i).getOpposite(u);
                GBlinkEmbeddingGraphVertex w = u.getEdge((i+1)%k).getOpposite(u);
                if (v.getFlag() && w.getFlag()) {
                    index = i;
                    break;
                }
            }

            // there is not a defined neighbour
            if (index == -1) {
                System.out.println("All undefined step");
                GBlinkEmbeddingGraphVertex v, w;
                double angle = 0;
                double ur = u.getRadii();
                v = u.getEdge(0).getOpposite(u);

                // define first circle position
                // u is always at origin
                if (hyperbolic) {
                    // point on the line that passes on u.

                    // u+v radius on the hyperbolic metric.
                    // if v is an horocycle than just
                    // u radius
                    double distance = -Math.log(u.getRadii())/2;
                    if (v.getRadii() > 1e-10) {
                        distance += -Math.log(v.getRadii())/2;
                    }
                    double ed = Math.exp(distance);
                    double scale = (ed-1)/(ed+1);
                    v.setPosition(scale, 0.0);
                }
                else {
                    v.setPosition(u.getRadii()+v.getRadii(), 0);
                }
                v.setFlag(true);

                // not processed yet
                if (!P.contains(v)) {
                    S.push(v);
                }

                for (int i = 1; i < k; i++) {
                    v = u.getEdge(i - 1).getOpposite(u);
                    w = u.getEdge(i).getOpposite(u);

                    double vr = v.getRadii();
                    double wr = w.getRadii();

                    angle += GBlinkEmbeddingGraphVertex.angleFromRadii(ur,vr,wr,hyperbolic);

                    // set center of w
                    this.setCenter(u,w,angle,hyperbolic);
                    w.setFlag(true);

                    // not processed yet?
                    if (!P.contains(w)) {
                        S.push(w);
                    }
                }

            }

            // there is a neighbour already defined
            else {
                GBlinkEmbeddingGraphVertex v, w;

                double ur = u.getRadii();

                v = u.getEdge(index).getOpposite(u);
                w = u.getEdge((index+1) % k).getOpposite(u);

                System.out.println(String.format("Indexes already defined   v%3d   v%3d",v.getLabel(),w.getLabel()));

                // calculate angle to start
                // the position is the drawing position
                // BECAREFUL: this must be a correct horocycles position (it's euclidean center)
                Vector2d vect = new Vector2d(w.getDrawingPosition());
                double angle = vect.angle(new Vector2d(1,0));
                if (vect.y < 0) angle = Math.PI*2 - angle;

                System.out.println(String.format("Starting Angle   v%10.6f",Math.toDegrees(angle)));

                int count = 0;
                for (int i = index + 2; count < k-2; i++) {
                    int j = i % k;

                    // vertex to operate
                    w = u.getEdge(j).getOpposite(u);

                    // log some message
                    if (w.getFlag())
                        System.out.println("Overdefining v"+w.getLabel());

                    // previous vertex on cyclic order of u
                    v = u.getEdge((k+j-1) % k).getOpposite(u);

                    double vr = v.getRadii();
                    double wr = w.getRadii();

                    double alphaUVW =  GBlinkEmbeddingGraphVertex.angleFromRadii(ur,vr,wr,hyperbolic);
                    angle += alphaUVW;
                    System.out.println(String.format("alphaUVW = %10.6f    v%d  at angle %10.6f ",Math.toDegrees(alphaUVW),w.getLabel(),Math.toDegrees(angle)));

                    // set center of w
                    this.setCenter(u,w,angle,hyperbolic);
                    w.setFlag(true);

                    count++;

                    // set flag
                    // not processed yet?
                    if (!P.contains(w)) {
                        S.push(w);
                    }
                }
            }

            //
            for (GBlinkEmbeddingGraphVertex v : _vertices) {
                this.adjustHyperbolicDrawingPositionAndRadii(v);
            }

            // log drawings
            //this.drawState(1000,1000,true,false,false,"log/images/link"+Library.fillStringWithChar(""+iterArrange,4,'0',true)+"-02.jpg");

        } // end of positioning loop

        // find mass center of interior vertices
        int count = 0;
        Complex massCenter = Complex.valueOf(0,0);
        for (GBlinkEmbeddingGraphVertex v : _vertices) {
            if (v.getRadii() < 1e-7 || !v.getFlag())
                continue;
            massCenter = massCenter.plus(Complex.valueOf(v.getX(),v.getY()));
            System.out.println("Mass + "+Complex.valueOf(v.getX(),v.getY()));
            count++;
        }
        massCenter = massCenter.divide(count);
        for (GBlinkEmbeddingGraphVertex v : _vertices) {
            Complex pos = Complex.valueOf(v.getX(),v.getY());
            pos = this.applyIsometry(pos,massCenter);
            v.setPosition(pos.getReal(),pos.getImaginary());
        }

        // convert the hyperbolic arrangement to euclidean
        if (hyperbolic) {

            //
            for (GBlinkEmbeddingGraphVertex v : _vertices) {
                if (v == externalFace)
                    continue;
                this.adjustHyperbolicDrawingPositionAndRadii(v);
            }
        }
        else { // euclidean drawing
            for (GBlinkEmbeddingGraphVertex v : _vertices) {
                v.savePositionAndRadiiIntoDrawingPositionAndRadii();
            }
        } // euclidean drawing

    }

    /**
     * Adjust Hyperbolic Drawing Position And Radii
     */
    private void adjustHyperbolicDrawingPositionAndRadii(GBlinkEmbeddingGraphVertex v) {

        if (!v.getFlag())
            return;

        // radii
        if (v.getRadii() > 1.0e-10) {

            // if it is on the origin then
            if (Math.abs(v.getX()) < 1.0e-10 && Math.abs(v.getY()) < 1.0e-10) {

                // calculate angle to start
                double hvr = -Math.log(v.getRadii()) / 2.0;

                double ed = Math.exp(hvr);
                double scale = (ed - 1) / (ed + 1);

                Complex za = Complex.valueOf( -scale, 0);
                Complex zb = Complex.valueOf( +scale, 0);

                Complex euclideanCenter = zb.plus(za).divide(2.0);
                double euclideanRadius = zb.minus(za).magnitude() / 2.0;

                // euclidean position and radii
                v.setDrawingPositionAndRadii(euclideanCenter.getReal(), euclideanCenter.getImaginary(), euclideanRadius);
            }

            // else do this
            else {
                // calculate angle to start
                Vector2d vect = new Vector2d(v.getX(),v.getY());
                vect.normalize();
                double angle = vect.angle(new Vector2d(1, 0));
                if (vect.y < 0)
                    angle = Math.PI * 2 - angle;

                //
                double hvr = -Math.log(v.getRadii()) / 2.0;

                Complex z = Complex.valueOf(v.getX(), v.getY());

                double ed = Math.exp(hvr);
                double scale = (ed - 1) / (ed + 1);

                Complex za = Complex.valueOf( -vect.x * scale, -vect.y * scale);
                Complex zb = Complex.valueOf(vect.x * scale, vect.y * scale);

                // apply isometry take 0 to z
                Complex z0 = Complex.ZERO.minus(z);
                Complex tza = this.applyIsometry(za, z0); //minus(z0).divide(Complex.ONE.minus(z0.conjugate().times(za)));
                Complex tzb = this.applyIsometry(zb, z0); //zb.minus(z0).divide(Complex.ONE.minus(z0.conjugate().times(zb)));

                Complex euclideanCenter = tzb.plus(tza).divide(2.0);
                double euclideanRadius = tzb.minus(tza).magnitude() / 2.0;

                // euclidean position and radii
                v.setDrawingPositionAndRadii(euclideanCenter.getReal(), euclideanCenter.getImaginary(), euclideanRadius);
            }
        }

        // horocycles
        else {

            // System.out.println("Processing horocycle v" + v.getLabel() + "    internal parent v" + v.getHorocycleDirectionVertex().getLabel());

            //
            Complex z0 = Complex.valueOf(v.getHorocycleDirectionVertex().getX(), v.getHorocycleDirectionVertex().getY());
            Complex zh = Complex.valueOf(v.getX(), v.getY());

            //
            if (z0.getReal() == 0 && z0.getImaginary() == 0) {

                // angle of (1,0) with (x0,y0)
                double beta = (new Vector2d(1, 0)).angle(new Vector2d(zh.getReal(), zh.getImaginary()));
                if (zh.getImaginary() < 0)
                    beta = Math.PI * 2 - beta;

                Complex answer = Complex.valueOf(Math.cos(beta), Math.sin(beta));

                // euclidean
                Complex euclideanCenter = zh.plus(answer).divide(2.0);
                double euclideanRadius = answer.minus(zh).magnitude() / 2.0;

                // euclidean position and radii
                v.setDrawingPositionAndRadii(euclideanCenter.getReal(), euclideanCenter.getImaginary(), euclideanRadius);

            }

            else {

                // calculate line
                double arr[] = this.findHorocycleInfinityPointLine(z0, zh);
                double x0 = arr[3];
                double y0 = arr[4];
                double r0 = arr[5];

                // set drawing position and radii
                v.setDrawingPositionAndRadii(x0, y0, r0);

            }
        }
    }




    /**
     * Calculate line circle
     */
    public double[] findHorocycleInfinityPointLine(Complex z0, Complex zh) {

        //
        Complex tzh = this.applyIsometry(zh,z0);
        Complex tzm = tzh.divide(2.0);
        Complex zm = this.applyIsometry(tzm,z0.opposite());

        /*
        System.out.println("z0 = "+z0);
        System.out.println("zm = "+zm);
        System.out.println("zh = "+zh); */

        //
        Complex z0m = zm.plus(z0).divide(2.0);
        Complex zmh = zh.plus(zm).divide(2.0);

        // u and v
        Complex uPerp = zm.minus(z0);
        Complex vPerp = zh.minus(zm);
        Complex u = Complex.valueOf(uPerp.getImaginary(),-uPerp.getReal());
        Complex v = Complex.valueOf(vPerp.getImaginary(),-vPerp.getReal());

        // intersection
        Complex intersection = this.intersectLines(z0m,u,zmh,v);
        double x0 = intersection.getReal();
        double y0 = intersection.getImaginary();
        double r0 = intersection.minus(z0).magnitude();

        // test
        /*
        if ( Math.abs((z0.getReal()-x0)*(z0.getReal()-x0)+(z0.getImaginary()-y0)*(z0.getImaginary()-y0) - r0*r0) > 1e-5) {
            System.out.println("<PROBLEM> z0 do not pass on the circle!");
        }
        if ( Math.abs((zh.getReal()-x0)*(zh.getReal()-x0)+(zh.getImaginary()-y0)*(zh.getImaginary()-y0) - r0*r0) > 1e-5) {
            System.out.println("<PROBLEM> zh do not pass on the circle!");
        }*/

        /*
        System.out.println("x0 = "+x0);
        System.out.println("y0 = "+y0);
        System.out.println("r0 = "+r0); */

        // center
        Complex center = Complex.valueOf(x0,y0);
        Complex centerToZh = zh.minus(center);
        double angle = (new Vector2d(1,0)).angle(new Vector2d(centerToZh.getReal(),centerToZh.getImaginary()));
        if (centerToZh.getImaginary() < 0)
            angle = Math.PI*2 - angle;
        Complex zhToHorocycleEuclideanCenter = Complex.valueOf(-r0*Math.sin(angle),r0*Math.cos(angle));

        //
        /*
        double x0e2 = x0 * x0;
        double y0e2 = y0 * y0;
        double y0e3 = y0 * y0 * y0;
        double r0e2 = r0 * r0;
        double y0e4 = y0e2*y0e2;
        double x0e4 = x0e2*x0e2;
        double r0e4 = r0e2*r0e2;

        double A = (4*x0e2+4*y0e2);
        double B = (4*y0*r0e2-4*y0e3-4*y0-4*y0*x0e2);
        double C = x0e4+2*x0e2*y0e2-2*x0e2*r0e2+1-2*r0e2+r0e4-2*x0e2+2*y0e2-2*y0e2*r0e2+y0e4;

        double yIntersect1 = (-B + Math.sqrt(B*B-4*A*C))/2*A;
        double yIntersect2 = (-B - Math.sqrt(B*B-4*A*C))/2*A;
        double xIntersect1 = 1/2.0*(-2*y0*yIntersect1+1+x0e2+y0e2-r0e2)/x0;
        double xIntersect2 = 1/2.0*(-2*y0*yIntersect2+1+x0e2+y0e2-r0e2)/x0;

        //
        //Complex horocycleCenter1 = Complex.valueOf(xIntersect1,yIntersect1);
        //Complex horocycleCenter2 = Complex.valueOf(xIntersect2,yIntersect2);
*/

        // angle of (1,0) with (x0,y0)
        double beta = (new Vector2d(x0, y0)).angle(new Vector2d(1, 0));
        if (y0 < 0)
            beta = Math.PI * 2 - beta;
        double theta = Math.acos(1.0 / Math.sqrt(1.0 + r0 * r0));
        Complex horocycleCenter1 = Complex.valueOf(Math.cos(beta + theta), Math.sin(beta + theta));
        Complex horocycleCenter2 = Complex.valueOf(Math.cos(beta - theta), Math.sin(beta - theta));

        Complex horocycleCenter, horocycleCenterNotUsed;
        if (horocycleCenter1.minus(zh).magnitude() <  horocycleCenter1.minus(z0).magnitude()) {
            horocycleCenter = horocycleCenter1;
            horocycleCenterNotUsed = horocycleCenter2;
        }
        else {
            horocycleCenter = horocycleCenter2;
            horocycleCenterNotUsed = horocycleCenter1;
        }

        // intersection
        Complex horocycleEuclideanCenter = intersectLines(horocycleCenter,horocycleCenter,zh,zhToHorocycleEuclideanCenter);
        // System.out.println("Horocycle Euclidean Center = "+horocycleEuclideanCenter+" magnitude = "+horocycleEuclideanCenter.magnitude());
        // horocycleEuclideanCenter = Complex.valueOf(-0.1,-0.3);


        return new double[] {x0,y0,r0,
                horocycleEuclideanCenter.getReal(),horocycleEuclideanCenter.getImaginary(),horocycleEuclideanCenter.minus(zh).magnitude(),
                horocycleCenter.getReal(),horocycleCenter.getImaginary(),
                horocycleCenterNotUsed.getReal(),horocycleCenterNotUsed.getImaginary(),
                zhToHorocycleEuclideanCenter.getReal(),zhToHorocycleEuclideanCenter.getImaginary()
        };
    }

    /**
     * Calculate line circle
     */
    public Complex intersectLines(Complex u0, Complex du, Complex v0, Complex dv) {

        double ux0 = u0.getReal();
        double uy0 = u0.getImaginary();

        double udx = du.getReal();
        double udy = du.getImaginary();

        double vx0 = v0.getReal();
        double vy0 = v0.getImaginary();

        double vdx = dv.getReal();
        double vdy = dv.getImaginary();

        double denom = (udx * vdy - udy * vdx);
        double s = -(ux0 * vdy - vx0 * vdy - vdx * uy0 + vdx * vy0) / denom;
        double t = -( -udx * uy0 + udx * vy0 + udy * ux0 - udy * vx0) / denom;

        double x0 = ux0 + udx * s;
        double y0 = uy0 + udy * s;

        return Complex.valueOf(x0,y0);
    }



    /**
     * Calculate line circle
     */
    public double[] getLineCircle(double xA, double yA, double xB, double yB) {
        // calculate center position and radii of geodesic line
        // that passes through z0 and zh
        // Maple solved this...
        // solve({
        // x0^2+y0^2=1+r0^2,
        // (xA-x0)^2+(yA-y0)^2=r0^2,
        // (xB-x0)^2+(yB-y0)^2=r0^2},
        // {x0,y0,r0});

        double xAe2 = xA * xA;
        double xAe3 = xAe2 * xA;
        double xAe4 = xAe3 * xA;

        double yAe2 = yA * yA;
        double yAe3 = yAe2 * yA;
        double yAe4 = yAe3 * yA;

        double xBe2 = xB * xB;
        double xBe3 = xBe2 * xB;
        double xBe4 = xBe3 * xB;

        double yBe2 = yB * yB;
        double yBe3 = yBe2 * yB;
        double yBe4 = yBe3 * yB;




        double denom = 2.0 * (xA * yB - yA * xB);
        double x0 = +( -xBe2 * yA - yBe2 * yA + yB * xAe2 + yB + yB * yAe2 - yA) / denom;
        double y0 = -(  xAe2 * xB - xA * xBe2 - xA * yBe2 + xB - xA + yAe2 * xB) / denom;
        double r0Num = Math.sqrt( -1 *
                                 (2 * xA * yBe2 * yAe2 * xB + 2 * xBe2 * yA * yB * xAe2 + 2 * yB * xAe2 * yA +
                                  2 * xBe2 * yA * yB + 2 * yB * yA - yBe2 * yAe4 + 2 * yB * yAe3 - xAe4 * xBe2 +
                                  2 * xAe3 * xBe3 - 4 * xAe2 * xBe2 + 2 * xAe3 * xB - xAe2 * xBe4 + 2 * xA * xBe3 -
                                  xAe2 * yBe4 + 2 * xB * xA - yAe4 * xBe2 - xAe2 - yAe2 - xBe2 - yBe2 -
                                  8 * xA * yB * yA * xB - 2 * xBe2 * yAe2 * yBe2 + 2 * xBe2 * yAe3 * yB +
                                  2 * yBe3 * yA * xAe2 - 2 * yBe2 * xAe2 * yAe2 + 2 * xAe3 * xB * yBe2 -
                                  2 * xAe2 * xBe2 * yAe2 - 2 * xAe2 * xBe2 * yBe2 + 2 * xA * xBe3 * yAe2 +
                                  2 * xA * yBe2 * xB + 2 * xA * yAe2 * xB - xBe4 * yAe2 - yBe4 * yAe2 + 2 * yBe3 * yA +
                                  2 * yBe3 * yAe3 - 4 * yBe2 * yAe2 - yBe2 * xAe4));
        double r0 = Math.abs(r0Num / denom);
        return new double[] {x0,y0,r0};
    }

    // ------------------------------------------------------------
    // drawing part

    private double _scale;
    private double _minX, _minY;
    private double scale(double v) {
        return _scale * v;
    }
    private double convertX(double x) {
        return _scale * (x - _minX);
    }
    private double convertY(double y) {
        return _scale*(y - _minY);
    }
    int _count = 1;
    public void drawState(int width, int height, boolean drawCircles, boolean drawLink, boolean drawBlink, String fileName) throws IOException {

        boolean first = true;
        double minX = 0, maxX = 0, minY = 0, maxY = 0;
        for (GBlinkEmbeddingGraphVertex v : _vertices) {
            if (first || v.getDrawingX() - v.getDrawingRadii() < minX)
                minX = v.getDrawingX() - v.getDrawingRadii();
            if (first || v.getDrawingX() + v.getDrawingRadii() > maxX)
                maxX = v.getDrawingX() + v.getDrawingRadii();
            if (first || v.getDrawingY() - v.getDrawingRadii() < minY)
                minY = v.getDrawingY() - v.getDrawingRadii();
            if (first || v.getDrawingY() + v.getDrawingRadii() > maxY)
                maxY = v.getDrawingY() + v.getDrawingRadii();
            first = false;
        }

        _minX = minX;
        _minY = minY;
        _scale = Math.min((double)width / (maxX - minX), (double)height / (maxY - minY));


        // hyperbolic
        _minX = -1.02;
        _minY = -1.02;
        _scale = Math.min((double)width / 2.04, (double)height / 2.04);

        // Create a buffered image in which to draw
        BufferedImage bufferedImage = new BufferedImage((int)width, (int)height, BufferedImage.TYPE_INT_RGB);

        // Create a graphics contents on the buffered image
        Graphics2D g2d = bufferedImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        Rectangle2D.Double rect = new Rectangle2D.Double(0,0,width,height);
        g2d.setBackground(Color.black);
        g2d.setColor(Color.black);
        g2d.fill(rect);
        g2d.draw(rect);


        // Draw graphics
        if (drawCircles)
            drawCircles(g2d);

        // draw link
        if (drawLink)
            drawLink(g2d);

        // draw blink
        if (drawBlink)
            drawBlink(g2d);


        // Graphics context no longer needed so dispose it
        g2d.dispose();

        // Save as JPEG
        File file = new File(fileName);
        ImageIO.write(bufferedImage, "jpg", file);
    }

    public BufferedImage getImage(int width, int height, boolean drawCircles, boolean drawLink, boolean drawBlink) throws IOException {

        boolean first = true;
        double minX = 0, maxX = 0, minY = 0, maxY = 0;
        for (GBlinkEmbeddingGraphVertex v : _vertices) {
            if (first || v.getDrawingX() - v.getDrawingRadii() < minX)
                minX = v.getDrawingX() - v.getDrawingRadii();
            if (first || v.getDrawingX() + v.getDrawingRadii() > maxX)
                maxX = v.getDrawingX() + v.getDrawingRadii();
            if (first || v.getDrawingY() - v.getDrawingRadii() < minY)
                minY = v.getDrawingY() - v.getDrawingRadii();
            if (first || v.getDrawingY() + v.getDrawingRadii() > maxY)
                maxY = v.getDrawingY() + v.getDrawingRadii();
            first = false;
        }

        _minX = minX;
        _minY = minY;
        _scale = Math.min((double)width / (maxX - minX), (double)height / (maxY - minY));


        // hyperbolic
        _minX = -1.02;
        _minY = -1.02;
        _scale = Math.min((double)width / 2.04, (double)height / 2.04);

        // Create a buffered image in which to draw
        BufferedImage bufferedImage = new BufferedImage((int)width, (int)height, BufferedImage.TYPE_INT_RGB);

        // Create a graphics contents on the buffered image
        Graphics2D g2d = bufferedImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        Rectangle2D.Double rect = new Rectangle2D.Double(0,0,width,height);
        g2d.setBackground(Color.black);
        g2d.setColor(Color.black);
        g2d.fill(rect);
        g2d.draw(rect);


        // Draw graphics
        if (drawCircles)
            drawCircles(g2d);

        // draw link
        if (drawLink)
            drawLink(g2d);

        // draw blink
        if (drawBlink)
            drawBlink(g2d);


        // Graphics context no longer needed so dispose it
        g2d.dispose();

        return bufferedImage;
    }

    public void drawEPS(String fileName, double width, double height, double margin, boolean link, boolean blink, boolean circles) throws FileNotFoundException,
            SQLException {
        new DrawBlinkEPS(fileName,width,height,margin, link, blink, circles);
    }

    class DrawBlinkEPS {
        private static final double CONVERSION = 72.0/2.54;

        // all of these are in centimeters
        private double _width = 6;
        private double _height = 6;
        private double _margin = 0.25;
        private double _blinkEdgeWidth = 0.05;

        // translation parameters
        private double _scale;
        private double _xTranslate;
        private double _yTranslate;

        public DrawBlinkEPS(String fileName, double width, double height, double margin, boolean link, boolean blink, boolean circles) throws SQLException, FileNotFoundException {
            PrintWriter pw = new PrintWriter(fileName);

            _width = width;
            _height = height;
            _margin = margin;

            double minX = 0, minY = 0, maxX = 0, maxY = 0;

            boolean first = true;
            for (GBlinkEmbeddingGraphVertex v : _vertices) {
                if (first || v.getDrawingX() - v.getDrawingRadii() < minX)
                    minX = v.getDrawingX() - v.getDrawingRadii();
                if (first || v.getDrawingX() + v.getDrawingRadii() > maxX)
                    maxX = v.getDrawingX() + v.getDrawingRadii();
                if (first || v.getDrawingY() - v.getDrawingRadii() < minY)
                    minY = v.getDrawingY() - v.getDrawingRadii();
                if (first || v.getDrawingY() + v.getDrawingRadii() > maxY)
                    maxY = v.getDrawingY() + v.getDrawingRadii();
                first = false;
            }

            _xTranslate = minX;
            _yTranslate = minY;
            _scale = Math.min((double)(_width-2*_margin) / (maxX - minX), (double)(_height-2*_margin) / (maxY - minY));

            this.printHeader(pw);

            if (circles)
                this.printCircles(pw);

            if (link)
                this.printLink(pw);

            if (blink)
                this.printBlink(pw);

            this.printFooter(pw);
            pw.close();
        }

        private double tLength(double l) {
            return (l * _scale) * CONVERSION;
        }

        private double tx(double x) {
            return (_margin + (x - _xTranslate) * _scale) * CONVERSION;
        }

        private double ty(double y) {
            return (_margin + (y - _yTranslate) * _scale) * CONVERSION;
        }

        private void printBlink(PrintWriter pw) {

            GBlink G = _gBlink;

            // translate
            pw.println("gsave");

            pw.println(String.format("%.3f setlinewidth",_blinkEdgeWidth * CONVERSION));

            // draw edges on EPS
            for (GBlinkEmbeddingGraphVertex v: _vertices) {
                if (v.getType() == GBlinkEmbeddingGraphVertexType.crossing) {

                    ArrayList<GBlinkEmbeddingGraphVertex> adjList = v.getAdjacentVerticesByType(GBlinkEmbeddingGraphVertexType.blackFace);

                    // adjList
                    if (adjList.size() != 2)
                        throw new RuntimeException("ooopps");

                    GBlinkEmbeddingGraphVertex a = adjList.get(0);
                    GBlinkEmbeddingGraphVertex b = adjList.get(1);

                    pw.println("newpath");
                    pw.println(String.format("%.4f %.4f moveto", (float)tx(a.getDrawingX()), (float)ty(a.getDrawingY())));
                    pw.println(String.format("%.4f %.4f %.4f %.4f %.4f %.4f curveto",
                                             (float)tx(v.getDrawingX()),
                                             (float)ty(v.getDrawingY()),
                                             (float)tx(v.getDrawingX()),
                                             (float)ty(v.getDrawingY()),
                                             (float)tx(b.getDrawingX()),
                                             (float)ty(b.getDrawingY())));

                    // System.out.println(""+v.getObject());
                    GBlinkVertex vertex = ((Variable) v.getObject()).getVertex(0);

                    if (vertex.isGreen())
                        pw.println("0 180 0 setrgbcolor");
                    else
                        pw.println("180 0 0 setrgbcolor");
                    pw.println("stroke");
                }
            }

            for (GBlinkEmbeddingGraphVertex v: _vertices) {
                if (v.getType() == GBlinkEmbeddingGraphVertexType.blackFace) {

                    double x0 = tx(v.getDrawingX());
                    double y0 = ty(v.getDrawingY());

                    // draw vertices as black dots
                    pw.println("newpath");
                    // pw.println(String.format("%.4f %.4f moveto", x0, y0));
                    pw.println(String.format("%.4f %.4f %.4f 0 360 arc", x0, y0, 0.05 * CONVERSION));
                    pw.println("closepath");
                    pw.println("0 0 0 setrgbcolor");
                    pw.println("fill");
                }
            }

            // restore state
            pw.println("grestore");

        }

        private double[] arc(GBlinkEmbeddingGraphVertex v, GBlinkEmbeddingGraphVertex a, GBlinkEmbeddingGraphVertex c) {
            // a and c gives origin to an arc
            Vector2d vectorAV = new Vector2d(v.getDrawingX(),v.getDrawingY());
            vectorAV.sub(a.getDrawingPosition());
            vectorAV.normalize();

            double param = Math.min(Math.min(a.getDrawingRadii(),v.getDrawingRadii()),c.getDrawingRadii());
            // double param = 0.01;


            Vector2d p0 = new Vector2d(vectorAV);
            p0.scale(a.getDrawingRadii());
            p0.add(a.getDrawingPosition());

            Vector2d p1 = new Vector2d(vectorAV);
            //p1.scale(Math.min(a.getDrawingRadii(),v.getDrawingRadii()));
            //p1.scale(v.getDrawingRadii());
            p1.scale(param);
            p1.add(p0);

            Vector2d vectorCV = new Vector2d(v.getDrawingX(),v.getDrawingY());
            vectorCV.sub(c.getDrawingPosition());
            vectorCV.normalize();

            Vector2d p3 = new Vector2d(vectorCV);
            p3.scale(c.getDrawingRadii());
            p3.add(c.getDrawingPosition());

            Vector2d p2 = new Vector2d(vectorCV);
            //p2.scale(Math.min(c.getDrawingRadii(),v.getDrawingRadii()));
            //p2.scale(v.getDrawingRadii());
            p2.scale(param);
            p2.add(p3);

            return new double[] {
                    tx(p0.x), ty(p0.y),
                    tx(p1.x), ty(p1.y),
                    tx(p2.x), ty(p2.y),
                    tx(p3.x), ty(p3.y)};
        }

        private void printArc(PrintWriter pw, double arc[], double thickness, Color color) {
            pw.println("gsave");
            pw.println(String.format("%.3f %.3f %.3f setrgbcolor",color.getRed()/255.0,color.getGreen()/255.0,color.getBlue()/255.0));
            pw.println(String.format("0 setlinecap")); // butt = 0
            pw.println(String.format("1 setlinejoin")); // round
            pw.println(String.format("%.3f setlinewidth",thickness));
            pw.println(String.format("newpath",arc[0],arc[1]));
            pw.println(String.format("%.4f %.4f moveto",arc[0],arc[1]));
            pw.println(String.format("%.4f %.4f %.4f %.4f %.4f %.4f curveto",
                                     arc[2],arc[3],
                                     arc[4],arc[5],
                                     arc[6],arc[7]));
            pw.println("stroke");
            pw.println("grestore");
        }

        public void printLink(PrintWriter pw) {

            double thick = 0.2*CONVERSION;
            double thin  = 0.1*CONVERSION;

            // edges
            for (GBlinkEmbeddingGraphVertex v: _vertices) {
                if (v.getType() == GBlinkEmbeddingGraphVertexType.crossing) {

                    // adjacent list
                    ArrayList<GBlinkEmbeddingGraphVertex> adjList = v.getAdjacentVerticesByType(GBlinkEmbeddingGraphVertexType.edge);

                    //
                    if (adjList.size() != 4)
                        throw new RuntimeException("ooopps");

                    GBlinkEmbeddingGraphVertex a = adjList.get(0);
                    GBlinkEmbeddingGraphVertex b = adjList.get(1);
                    GBlinkEmbeddingGraphVertex c = adjList.get(2);
                    GBlinkEmbeddingGraphVertex d = adjList.get(3);

                    // arcs
                    double arc1[] = arc(v,a,c);
                    double arc2[] = arc(v,b,d);

                    GBlinkVertex gBlinkVertex = ((Variable) v.getObject()).getVertex(0);
                    GBlinkVertex a1 = ((GBlinkVertex)a.getObject());
                    GBlinkVertex a2 = a1.getNeighbour(GBlinkEdgeType.edge);
                    GBlinkVertex c1 = ((GBlinkVertex)c.getObject());
                    GBlinkVertex c2 = c1.getNeighbour(GBlinkEdgeType.edge);

                    boolean acOver;
                    if (gBlinkVertex == a1 || gBlinkVertex == a2 || gBlinkVertex == c1 || gBlinkVertex == c2) {
                        acOver = gBlinkVertex.overcross();
                    }
                    else acOver = !gBlinkVertex.overcross();


                    //
                    if (acOver) {
                        double aux[] = arc1;
                        arc1 = arc2;
                        arc2 = aux;
                    }

                    this.printArc(pw,arc1,thick,Color.white);
                    this.printArc(pw,arc1,thin,Color.black);
                    this.printArc(pw,arc2,thick,Color.white);
                    this.printArc(pw,arc2,thin,Color.black);

                }
                else if (v.getType() == GBlinkEmbeddingGraphVertexType.edge) {

                    // adjacent list
                    ArrayList<GBlinkEmbeddingGraphVertex>
                            adjList = v.getAdjacentVerticesByType(GBlinkEmbeddingGraphVertexType.crossing);

                    //
                    if (adjList.size() != 2)
                        throw new RuntimeException("ooopps");

                    GBlinkEmbeddingGraphVertex a = adjList.get(0);
                    GBlinkEmbeddingGraphVertex b = adjList.get(1);

                    // arcs
                    double arc1[] = arc(v, a, b);

                    //
                    this.printArc(pw,arc1,thick,Color.white);
                    this.printArc(pw,arc1,thin,Color.black);
                }
            }
        }


        private void printLine(PrintWriter pw, double arc[], double thickness, Color color) {
            pw.println("gsave");
            pw.println(String.format("%.3f %.3f %.3f setrgbcolor",color.getRed()/255.0,color.getGreen()/255.0,color.getBlue()/255.0));
            pw.println(String.format("0 setlinecap")); // butt = 0
            pw.println(String.format("1 setlinejoin")); // round
            pw.println(String.format("%.3f setlinewidth",thickness));
            pw.println(String.format("newpath",arc[0],arc[1]));
            pw.println(String.format("%.4f %.4f moveto",arc[0],arc[1]));
            pw.println(String.format("%.4f %.4f lineto",arc[2],arc[3]));
            pw.println("stroke");
            pw.println("grestore");
        }

        private void printCircle(PrintWriter pw, double x0, double y0, double radius, double thickness, Color color) {
            pw.println("gsave");
            pw.println(String.format("%.3f %.3f %.3f setrgbcolor",color.getRed()/255.0,color.getGreen()/255.0,color.getBlue()/255.0));
            pw.println(String.format("0 setlinecap")); // butt = 0
            pw.println(String.format("1 setlinejoin")); // round
            pw.println(String.format("%.3f setlinewidth",thickness));
            pw.println(String.format("newpath", x0, y0));
            pw.println(String.format("%.4f %.4f %.4f 0 360 arc", x0, y0, radius));
            pw.println("closepath");
            pw.println("stroke");
            pw.println("grestore");
        }


        private void printCircles(PrintWriter pw) {
            for (GBlinkEmbeddingGraphEdge e : _edges) {
                GBlinkEmbeddingGraphVertex u = e.getU();
                GBlinkEmbeddingGraphVertex v = e.getV();

                if (u.getFlag() == false)
                    continue;
                if (v.getFlag() == false)
                    continue;

                Vector2d vv = new Vector2d(v.getDrawingPosition());

                printLine(pw, new double[] {tx(u.getDrawingX()), ty(u.getDrawingY()), tx(v.getDrawingX()), ty(v.getDrawingY())},
                          0.01 * CONVERSION, Color.lightGray);
            }

            // circle
            this.printCircle(pw, tx(0), ty(0), tLength(1), 0.02 * CONVERSION, Color.red);

            for (GBlinkEmbeddingGraphVertex v : _vertices) {
                if (v.getFlag() == false)
                    continue;

                // do not draw horocycles


                Color color = Color.red;
                if (v.getType() == GBlinkEmbeddingGraphVertexType.crossing) {
                    color = Color.green;
                } else if (v.getType() == GBlinkEmbeddingGraphVertexType.edge) {
                    color = Color.blue;
                } else if (v.getType() == GBlinkEmbeddingGraphVertexType.whiteFace) {
                    color = Color.red;
                } else if (v.getType() == GBlinkEmbeddingGraphVertexType.blackFace) {
                    color = Color.magenta;
                }
                this.printCircle(pw, tx(v.getDrawingX()), ty(v.getDrawingY()), tLength(v.getDrawingRadii()), 0.02 * CONVERSION, color);

            }
        }


        private void printHeader(PrintWriter pw) {

            int W = (int) (CONVERSION * _width);
            int H = (int) (CONVERSION * _height);

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

    /**
     * Draw blink
     */
    private void drawBlink(Graphics2D g) {

        Ellipse2D.Double circle = new Ellipse2D.Double(0,0,1,1);

        // edges
        for (GBlinkEmbeddingGraphVertex v: _vertices) {
            if (v.getType() == GBlinkEmbeddingGraphVertexType.crossing) {

                ArrayList<GBlinkEmbeddingGraphVertex> adjList = v.getAdjacentVerticesByType(GBlinkEmbeddingGraphVertexType.blackFace);

                //
                if (adjList.size() != 2)
                    throw new RuntimeException("ooopps");

                GBlinkEmbeddingGraphVertex a = adjList.get(0);
                GBlinkEmbeddingGraphVertex b = adjList.get(1);

                GeneralPath path = new GeneralPath();
                path.moveTo(
                        (float)convertX(a.getDrawingX()),
                        (float)convertY(a.getDrawingY()));
                path.quadTo(
                        (float)convertX(v.getDrawingX()),
                        (float)convertY(v.getDrawingY()),
                        (float)convertX(b.getDrawingX()),
                        (float)convertY(b.getDrawingY()));

                GBlinkVertex gBlinkVertex = ((Variable) v.getObject()).getVertex(0);
                if (gBlinkVertex.getCrossingEdgeColor() == BlinkColor.green) {
                    g.setColor(Color.green);
                }
                else g.setColor(Color.red);
                g.setStroke(new BasicStroke(5));
                g.draw(path);
            }
        }

        // vertex
        double blinkVertexRadius = 6;
        for (GBlinkEmbeddingGraphVertex v: _vertices) {
            if (v.getType() == GBlinkEmbeddingGraphVertexType.blackFace) {
                circle.setFrame(
                        convertX(v.getDrawingX()) - blinkVertexRadius,
                        convertY(v.getDrawingY()) - blinkVertexRadius,
                        2 * blinkVertexRadius,
                        2 * blinkVertexRadius);
                g.setColor(Color.white);
                g.fill(circle);
            }
        }
    }

    /**
     * Draw circles
     */
    private void drawCircles(Graphics2D g) {

        g.setStroke(new BasicStroke(3));

        Ellipse2D.Double circle = new Ellipse2D.Double(0,0,1,1);
        Line2D.Double line = new Line2D.Double(0,0,0,0);

        for (GBlinkEmbeddingGraphEdge e: _edges) {
            GBlinkEmbeddingGraphVertex u = e.getU();
            GBlinkEmbeddingGraphVertex v = e.getV();

            if (u.getFlag() == false) continue;
            if (v.getFlag() == false) continue;

            Vector2d vv = new Vector2d(v.getDrawingPosition());
            //vv.sub(u.getPosition());
            line.setLine(
            convertX(u.getDrawingX()),
            convertY(u.getDrawingY()),
            convertX(v.getDrawingX()),
            convertY(v.getDrawingY()));

            g.setStroke(new BasicStroke(1));
            g.setColor(Color.white);
            g.draw(line);
        }

        g.setStroke(new BasicStroke(3));
        for (GBlinkEmbeddingGraphVertex v: _vertices) {
            if (v.getFlag() == false) continue;

            // do not draw horocycles

            circle.setFrame(
            convertX(v.getDrawingX()-v.getDrawingRadii()),
            convertY(v.getDrawingY()-v.getDrawingRadii()),
            scale(2*v.getDrawingRadii()),
            scale(2*v.getDrawingRadii()));
            if (v.getType() == GBlinkEmbeddingGraphVertexType.crossing) {
                g.setColor(Color.green);
            }
            else if (v.getType() == GBlinkEmbeddingGraphVertexType.edge) {
                g.setColor(Color.blue);
            }
            else if (v.getType() == GBlinkEmbeddingGraphVertexType.whiteFace) {
                g.setColor(Color.red);
            }
            else if (v.getType() == GBlinkEmbeddingGraphVertexType.blackFace) {
                g.setColor(Color.magenta);
            }
            g.draw(circle);

            g.setColor(Color.yellow);
            g.setFont(new Font("Arial",Font.PLAIN,24));
            g.drawString(""+v.getLabel(),(float) convertX(v.getDrawingX()),(float) convertY(v.getDrawingY()));
        }


        // circle
        g.setStroke(new BasicStroke(3));
        circle.setFrame(
                convertX(-1),
                convertY(-1),
                scale(2),
                scale(2));
        g.setColor(Color.white);
        g.draw(circle);






        /*
        // --------------------------------------------------------
        // debug
        {
            GBlinkEmbeddingGraphVertex v = this.findVertexByLabel(9);
            GBlinkEmbeddingGraphVertex u = v.getHorocycleDirectionVertex();
            System.out.println("Horocycle v"+v.getLabel()+"        Internal Neighbor v"+u.getLabel());


            Complex z0 = Complex.valueOf(u.getOriginalPosition().x,u.getOriginalPosition().y);
            Complex zh = Complex.valueOf(v.getOriginalPosition().x,v.getOriginalPosition().y);

            Complex tz0 = this.applyIsometry(z0,z0);
            Complex tzh = this.applyIsometry(zh,z0);
            Complex tzm = tzh.divide(2.0);
            Complex zm = this.applyIsometry(tzm,z0.opposite());


            double arr[] = findHorocycleInfinityPointLine(z0,zh);
            double hx0 = arr[3];
            double hy0 = arr[4];
            double hr0 = arr[5];

            Complex intersectionUsed = Complex.valueOf(arr[6],arr[7]);
            Complex intersectionNotUsed = Complex.valueOf(arr[8],arr[9]);
            Complex zhToHorocycleEuclideanCenter = Complex.valueOf(arr[10],arr[11]);

            Complex points[] = {z0,zh,zm,tz0,tzh,tzm,intersectionUsed,intersectionNotUsed};

            double x0 = arr[0];
            double y0 = arr[1];
            double r0 = arr[2];
            g.setStroke(new BasicStroke(3));
            circle.setFrame(
                    convertX(x0-r0),
                    convertY(y0-r0),
                    scale(2*r0),
                    scale(2*r0));
            g.setColor(Color.cyan);
            g.draw(circle);


            //
            g.setStroke(new BasicStroke(3));
            circle.setFrame(
                    convertX(hx0-hr0),
                    convertY(hy0-hr0),
                    scale(2*hr0),
                    scale(2*hr0));
            g.setColor(Color.PINK);
            g.draw(circle);

            // points
            int count = 0;
            Color arrColors[] = {Color.red, Color.cyan, Color.yellow};
            for (Complex c: points) {
                g.setColor(arrColors[count++ % 3]);
                circle.setFrame(
                        convertX(c.getReal())-5,
                        convertY(c.getImaginary())-5,
                        10,
                        10);
                // g.fill(circle);
                g.fill(circle);
            }
            // points
            g.setColor(Color.orange);
            double scale = 1000;
            line.setLine(
                    convertX(zh.getReal())- scale,
                    convertY(zh.getImaginary()),
                    convertX(zh.getReal()) + scale,
                    convertY(zh.getImaginary()) + 0);
            g.draw(line);

            Complex zhToHorocycleEuclideanCenterUnit = zhToHorocycleEuclideanCenter.divide(zhToHorocycleEuclideanCenter.magnitude());
            line.setLine(
                    convertX(zh.getReal())+zhToHorocycleEuclideanCenterUnit.getReal()*scale,
                    convertY(zh.getImaginary())+zhToHorocycleEuclideanCenterUnit.getImaginary()*scale,
                    convertX(zh.getReal())+zhToHorocycleEuclideanCenterUnit.getReal()*(-scale),
                    convertY(zh.getImaginary())+zhToHorocycleEuclideanCenterUnit.getImaginary()*(-scale));
            g.draw(line);


            Complex intersectionUsedUnitaryVector = intersectionUsed.divide(intersectionUsed.magnitude());
            line.setLine(
                    convertX(intersectionUsed.getReal())+intersectionUsedUnitaryVector.getReal()*scale,
                    convertY(intersectionUsed.getImaginary())+intersectionUsedUnitaryVector.getImaginary()*scale,
                    convertX(intersectionUsed.getReal())+intersectionUsedUnitaryVector.getReal()*(-scale),
                    convertY(intersectionUsed.getImaginary())+intersectionUsedUnitaryVector.getImaginary()*(-scale));
            g.draw(line);

        }        */
    }

    /**
     * Apply isometry
     */
    public Complex applyIsometry(Complex z, Complex z0) {
        return z.minus(z0).divide(Complex.ONE.minus(z0.conjugate().times(z)));
    }


    /**
     * an arc based on a central circle and two adjacnt ones
     */
    private Shape arc(GBlinkEmbeddingGraphVertex v, GBlinkEmbeddingGraphVertex a, GBlinkEmbeddingGraphVertex c) {
        // a and c gives origin to an arc
        Vector2d vectorAV = new Vector2d(v.getDrawingX(),v.getDrawingY());
        vectorAV.sub(a.getDrawingPosition());
        vectorAV.normalize();

        double param = Math.min(Math.min(a.getDrawingRadii(),v.getDrawingRadii()),c.getDrawingRadii());
        // double param = 0.01;


        Vector2d p0 = new Vector2d(vectorAV);
        p0.scale(a.getDrawingRadii());
        p0.add(a.getDrawingPosition());

        Vector2d p1 = new Vector2d(vectorAV);
        //p1.scale(Math.min(a.getDrawingRadii(),v.getDrawingRadii()));
        //p1.scale(v.getDrawingRadii());
        p1.scale(param);
        p1.add(p0);

        Vector2d vectorCV = new Vector2d(v.getDrawingX(),v.getDrawingY());
        vectorCV.sub(c.getDrawingPosition());
        vectorCV.normalize();

        Vector2d p3 = new Vector2d(vectorCV);
        p3.scale(c.getDrawingRadii());
        p3.add(c.getDrawingPosition());

        Vector2d p2 = new Vector2d(vectorCV);
        //p2.scale(Math.min(c.getDrawingRadii(),v.getDrawingRadii()));
        //p2.scale(v.getDrawingRadii());
        p2.scale(param);
        p2.add(p3);

        GeneralPath path = new GeneralPath();
        path.moveTo((float)convertX(p0.x),(float)convertY(p0.y));
        path.curveTo(
            (float)convertX(p1.x),(float)convertY(p1.y),
            (float)convertX(p2.x),(float)convertY(p2.y),
            (float)convertX(p3.x),(float)convertY(p3.y));

        return path;
    }


    /**
     * Draw blink
     */
    private void drawLink(Graphics2D g) {

        int thick = 9;
        int thin = 6;

        // edges
        for (GBlinkEmbeddingGraphVertex v: _vertices) {
            if (v.getType() == GBlinkEmbeddingGraphVertexType.crossing) {

                // adjacent list
                ArrayList<GBlinkEmbeddingGraphVertex> adjList = v.getAdjacentVerticesByType(GBlinkEmbeddingGraphVertexType.edge);

                //
                if (adjList.size() != 4)
                    throw new RuntimeException("ooopps");

                GBlinkEmbeddingGraphVertex a = adjList.get(0);
                GBlinkEmbeddingGraphVertex b = adjList.get(1);
                GBlinkEmbeddingGraphVertex c = adjList.get(2);
                GBlinkEmbeddingGraphVertex d = adjList.get(3);

                // arcs
                Shape arc1 = arc(v,a,c);
                Shape arc2 = arc(v,b,d);

                GBlinkVertex gBlinkVertex = ((Variable) v.getObject()).getVertex(0);
                GBlinkVertex a1 = ((GBlinkVertex)a.getObject());
                GBlinkVertex a2 = a1.getNeighbour(GBlinkEdgeType.edge);
                GBlinkVertex c1 = ((GBlinkVertex)c.getObject());
                GBlinkVertex c2 = c1.getNeighbour(GBlinkEdgeType.edge);

                boolean acOver;
                if (gBlinkVertex == a1 || gBlinkVertex == a2 || gBlinkVertex == c1 || gBlinkVertex == c2) {
                    acOver = gBlinkVertex.overcross();
                }
                else acOver = !gBlinkVertex.overcross();


                //
                if (acOver) {
                    Shape aux = arc1;
                    arc1 = arc2;
                    arc2 = aux;
                }
                g.setColor(Color.yellow);
                g.setStroke(new BasicStroke(thick, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
                g.draw(arc1);

                g.setColor(Color.black);
                g.setStroke(new BasicStroke(thin, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
                g.draw(arc1);

                g.setColor(Color.yellow);
                g.setStroke(new BasicStroke(thick, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
                g.draw(arc2);

                g.setColor(Color.black);
                g.setStroke(new BasicStroke(thin, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
                g.draw(arc2);


            }
            else if (v.getType() == GBlinkEmbeddingGraphVertexType.edge) {

                // adjacent list
                ArrayList<GBlinkEmbeddingGraphVertex>
                        adjList = v.getAdjacentVerticesByType(GBlinkEmbeddingGraphVertexType.crossing);

                //
                if (adjList.size() != 2)
                    throw new RuntimeException("ooopps");

                GBlinkEmbeddingGraphVertex a = adjList.get(0);
                GBlinkEmbeddingGraphVertex b = adjList.get(1);

                // arcs
                Shape arc1 = arc(v, a, b);

                //
                g.setColor(Color.yellow);
                g.setStroke(new BasicStroke(thick,BasicStroke.CAP_BUTT,BasicStroke.JOIN_ROUND));
                g.draw(arc1);

                g.setColor(Color.black);
                g.setStroke(new BasicStroke(thin,BasicStroke.CAP_BUTT,BasicStroke.JOIN_ROUND));
                g.draw(arc1);
            }
        }
    }

    // drawing part
    // ------------------------------------------------------------
    /**
     * Test
     */
    public static void main(String[] args) throws IOException, SQLException {
        // GBlink G = new GBlink(new int[][] {{1},{1}}, new int[]{});
        // GBlink G = new GBlink(new int[][] {{1,2,3},{1,4},{2,4,5},{5,3}}, new int[]{});
        // GBlink G = App.getRepositorio().getBlinksByIDs(4887).get(0).getBlink();
        // GBlink G = App.getRepositorio().getBlinksByIDs(5985).get(0).getBlink();
        // GBlink G = App.getRepositorio().getBlinksByIDs(7674).get(0).getBlink();
        // GBlink G = App.getRepositorio().getBlinksByIDs(12453).get(0).getBlink(); // non protected
        // GBlink G = App.getRepositorio().getBlinksByIDs(159).get(0).getBlink();
        // GBlink G = new GBlink(new int[][] {{1,2},{1,2,3,4},{3,4}}, new int[]{});
        // GBlink G = new GBlink(new int[][] {{1},{1}}, new int[]{});
        // GBlink G = new GBlink(new int[][] { {1, 2, 3, 4}, {1, 5}, {2, 5}, {3, 6}, {4, 6} }, new int[] {});
        // GBlink G = new GBlink(new int[][] {{1},{1}}, new int[]{});

        // Exemplo do caderno!
        // GBlink G = new GBlink(new int[][] {{6}, {1,2}, {1,2,3,4,12,5,12}, {5}, {3,6,7,8,9,4}, {9,10,10}, {7,11}, {8,11}, }, new int[]{3,4,5,6,7,8,10,11});

        GBlink G = new GBlink(new int[] {14,23,2,24,4,6,5,8,7,10,9,13,12,21,11,17,16,15,18,20,19,22,3,1},1648); //new int[] {5,6,7,10,11}

        /*
        JFrame f = new JFrame("Map View");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(new Dimension(1024, 768));
        f.setContentPane(new PanelBlinkViewer(G.getWithProtectedCrossings()));
        f.setVisible(true);
        System.out.println("Agemality: "+G.getWithProtectedCrossings().getGem().getAgemality());  */

        GBlinkEmbeddingGraph GE = new GBlinkEmbeddingGraph(G.getWithProtectedCrossings());
        GE.logGraph();
        GE.findPackingLabel(true);

        GE.drawState(1000, 1000, true, false, false, "log/images/circles.jpg");
        GE.drawState(1000, 1000, false, true, false, "log/images/link.jpg");
        GE.drawState(1000, 1000, false, false, true, "log/images/blink.jpg");
        GE.drawState(1000, 1000, true, true, false, "log/images/circlesLink.jpg");
        GE.drawState(1000, 1000, true, false, true, "log/images/circlesBlink.jpg");
        GE.drawState(1000, 1000, false, true, true, "log/images/linkBlink.jpg");
        GE.drawState(1000, 1000, true, true, true, "log/images/circlesLinkBlink.jpg"); /**/

         /*
        // GBlink
        GBlinkEmbeddingGraph GE = new GBlinkEmbeddingGraph(G.getWithProtectedCrossings());
        GE.logGraph();
        GE.findPackingLabel(true);

        GE.drawState(3000,3000,true,false,false,"log/images/circles.jpg");
        GE.drawState(1000,1000,false,true,false,"log/images/link.jpg");
        GE.drawState(1000,1000,false,false,true,"log/images/blink.jpg");
        GE.drawState(1000,1000,true,true,false,"log/images/circlesLink.jpg");
        GE.drawState(1000,1000,true,false,true,"log/images/circlesBlink.jpg");
        GE.drawState(1000,1000,false,true,true,"log/images/linkBlink.jpg");
        GE.drawState(1000,1000,true,true,true,"log/images/circlesLinkBlink.jpg");


        //
        System.out.println(""+G.getNewRepresentant().getBlinkWord().toString());

        // representant
        GE = new GBlinkEmbeddingGraph(G.getNewRepresentant().getWithProtectedCrossings());
        GE.logGraph();
        GE.findPackingLabel(true);

        GE.drawState(3000,3000,true,false,false,"log/images/tcircles.jpg");
        GE.drawState(1000,1000,false,true,false,"log/images/tlink.jpg");
        GE.drawState(1000,1000,false,false,true,"log/images/tblink.jpg");
        GE.drawState(1000,1000,true,true,false,"log/images/tcirclesLink.jpg");
        GE.drawState(1000,1000,true,false,true,"log/images/tcirclesBlink.jpg");
        GE.drawState(1000,1000,false,true,true,"log/images/tlinkBlink.jpg");
        GE.drawState(1000,1000,true,true,true,"log/images/tcirclesLinkBlink.jpg"); */

        // exit
        // System.exit(0);
        //GE.drawState();
    }

}


/**
 * Types of vertices
 *
 * the "fill" vertices are vertices that just fill some space
 * to complete the hypothesis.
 */
enum GBlinkEmbeddingGraphVertexType {
    crossing, edge, blackFace, whiteFace, fill;
}

/**
 * Class of a vertex of the GBlinkEmbeddingGraph.
 */
class GBlinkEmbeddingGraphVertex {

    /**
     * Point in 3-D space. The radius is the z coordinate
     */
    Vector2d _position = new Vector2d();

    /**
     * Point in 3-D space. The radius is the z coordinate
     */
    Vector2d _drawingPosition = new Vector2d();

    /**
     * Point in 3-D space. The radius is the z coordinate
     */
    double _drawingRadii;

    /**
     * In the hyperbolic case, if this is an horocycle (infinte radii,
     * or special radii = 0)
     */
    GBlinkEmbeddingGraphVertex _horocycleDirectionVertex;

    /**
     * Label
     */
    private int _label;

    /**
     * Radii
     */
    private double _radii;

    /**
     * Initial Radii (on the iteration)
     */
    private double _radii0;

    /**
     * Type of vertex
     */
    private GBlinkEmbeddingGraphVertexType _type;

    /**
     * This object can be a angle-edge (edge) or a GFace/GVertex (face) or a crossing (GEdge)
     */
    private Object _object;

    /**
     * The edges incident to this vertex in counterclockwise orientation.
     */
    public ArrayList<GBlinkEmbeddingGraphEdge> _edges = new ArrayList<GBlinkEmbeddingGraphEdge>();

    /**
     * Flag
     */
    private boolean _flag;

    /**
     * Is Horocycle
     */
    private boolean _isHorocycle;

    /**
     * Constructor of the GBlinkEmbeddingGraph.
     */
    public GBlinkEmbeddingGraphVertex(int label, Object object, GBlinkEmbeddingGraphVertexType type) {
        _label = label;
        _type = type;
        _object = object;
    }

    /**
     * Get label
     */
    public int getLabel() {
        return _label;
    }

    /**
     * Add edge (make sure you add them in counterclockwise convention).
     */
    public void addEdge(GBlinkEmbeddingGraphEdge e) {
        //if (_edges.contains(e))
        //    throw new RuntimeException("oooops");

        _edges.add(e);
    }

    /**
     * Insert newEdge befor(after) the referenceEdge
     * if (beforeOrAfter = true) it is before else it is after.
     */
    public void insertEdge(GBlinkEmbeddingGraphEdge newEdge, GBlinkEmbeddingGraphEdge referenceEdge, boolean beforeOrAfter) {
        for (int i=0;i<_edges.size();i++) {
            if (this.getEdge(i) == referenceEdge) {
                if (beforeOrAfter) {
                    _edges.add(i, newEdge);
                    return;
                }
                else {
                    _edges.add(i + 1, newEdge);
                    return;
                }
            }
        }
        throw new RuntimeException("ooooopspsss");
    }


    /**
     * Return object
     */
    public Object getObject() {
        return _object;
    }

    /**
     * Return type of vertex
     */
    public GBlinkEmbeddingGraphVertexType getType() {
        return _type;
    }

    public boolean getFlag() {
        return _flag;
    }

    public void setFlag(boolean flag) {
        _flag = flag;
    }

    /**
     * to String
     */
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("v"+this.getLabel()+" "+this.getType());
        if (this.getType() == GBlinkEmbeddingGraphVertexType.edge) {
            GBlinkVertex v1 = (GBlinkVertex) this.getObject();
            GBlinkVertex v2 = v1.getNeighbour(GBlinkEdgeType.edge);
            sb.append(" "+v1.getLabel()+" <-> "+v2.getLabel());
        }
        else {
            Variable var = (Variable) this.getObject();
            for (GBlinkVertex v: var.getVertices()) {
                sb.append(" "+v.getLabel());
            }
        }
        sb.append(" deg(v)="+_edges.size());
        return sb.toString();
    }

    /**
     * get edges
     */
    public ArrayList<GBlinkEmbeddingGraphEdge> getEdges(){
        return _edges;
    }

    /**
     * get edge
     */
    public GBlinkEmbeddingGraphEdge getEdge(int index){
        return _edges.get(index);
    }

    /**
     * num edges
     */
    public int numEdges() {
        return _edges.size();
    }

    /**
     * to String
     */
    public String edgesString() {
        StringBuffer sb = new StringBuffer();
        sb.append(this.toString()+"\n");
        for (GBlinkEmbeddingGraphEdge v: _edges) {
            sb.append("   "+v.toString()+"\n");
        }
        return sb.toString();
    }

    /**
     * Set embedding position of this vertex
     */
    public void setPosition(double x, double y) {
        _position.set(x,y);
        // System.out.println("Set position of v"+this.getLabel()+" to "+this._position);
    }

    /**
     * Set X position of this vertex
     */
    public void setX(double x) {
        _position.set(x,_position.y);
    }

    /**
     * Set Y position of this vertex
     */
    public void setY(double y) {
        _position.set(_position.x,y);
    }

    /**
     * Set Radius of this vertex
     */
    public void setRadii(double z) {
        if (z > 1)
            z = z+1-1;
        _radii = z;
    }

    /**
     * Set Radius of this vertex
     */
    public void assignRadiiToRadii0() {
        _radii0 = _radii;
    }

    /**
     * Set Radius of this vertex
     */
    public double getRadii() {
        return _radii;
    }

    /**
     * Set Radius of this vertex
     */
    public double getRadii0() {
        return _radii0;
    }

    /**
     * Get X
     */
    public double getX() {
        return _position.x;
    }

    /**
     * Get Y
     */
    public double getY() {
        return _position.y;
    }

    /**
     * Get X
     */
    public double getDrawingX() {
        return _drawingPosition.x;
    }

    /**
     * Get Y
     */
    public double getDrawingY() {
        return _drawingPosition.y;
    }

    /**
     * Return position
     */
    public Vector2d getDrawingPosition() {
        return _drawingPosition;
    }

    /**
     * Return position
     */
    public double getDrawingRadii() {
        return _drawingRadii;
    }

    /**
     * Set drawing position and radii
     */
    public void setDrawingPositionAndRadii(double x, double y, double r) {
        _drawingPosition.set(x,y);
        _drawingRadii = r;
    }

    /**
     * Return position
     */
    public void savePositionAndRadiiIntoDrawingPositionAndRadii() {
        _drawingPosition.set(_position.x,_position.y);
        _drawingRadii = _radii;
    }

    /**
     * Remove edge from list
     */
    public void removeEdge(GBlinkEmbeddingGraphEdge e) {
        _edges.remove(e);
    }

    /**
     * Angle on "u" based on the radii
     */
    public static double angleFromRadii(double u, double v, double w, boolean hyperbolic) {
        if (hyperbolic) {
            double aa = 2 * Math.asin(Math.sqrt(u*(1-v)/(1-u*v)*(1-w)/(1-u*w)));
//            System.out.println(String.format("Hyperbolic Angle between u=%10.6f   v=%10.6f   w=%10.6f    =    %10.6f",u,v,w,aa));
            return aa;
        }
        else {
            return 2 * Math.asin(Math.sqrt(v / (u + v) * w / (u + w)));
        }
    }

    /**
     * Calculate angle sum
     */
    public double calculateAngleSum(boolean useRadii0, boolean hyperbolic) {
        double theta = 0.0;
        int k = this.numEdges();

        double u = (useRadii0 ? this.getRadii0() : this.getRadii());

        for (int i=0;i<k;i++) {
            GBlinkEmbeddingGraphVertex vv = this.getEdge(i).getOpposite(this);
            GBlinkEmbeddingGraphVertex ww = this.getEdge((i+1) % k).getOpposite(this);

            double v = (useRadii0 ? vv.getRadii0() : vv.getRadii());
            double w = (useRadii0 ? ww.getRadii0() : ww.getRadii());

            if (v > 1 || w > 1)
                v = v+1-2+1;


            theta += angleFromRadii(u,v,w,hyperbolic);
        }
        return theta;
    }

    /**
     * Get adjacent vertices by type
     */
    public ArrayList<GBlinkEmbeddingGraphVertex> getAdjacentVerticesByType(GBlinkEmbeddingGraphVertexType type) {
        ArrayList<GBlinkEmbeddingGraphVertex> result = new ArrayList<GBlinkEmbeddingGraphVertex>();
        for (GBlinkEmbeddingGraphEdge e: _edges) {
            GBlinkEmbeddingGraphVertex opposite = e.getOpposite(this);
            if (opposite.getType() == type)
                result.add(opposite);
        }
        return result;
    }

    /**
     * Set horocycle direction vertex
     */
    public void setHorocycleDirectionVertex(GBlinkEmbeddingGraphVertex v) {
        _isHorocycle = (v != null);
        _horocycleDirectionVertex = v;
    }

    /**
     * Get horocycle direction vertex
     */
    public GBlinkEmbeddingGraphVertex getHorocycleDirectionVertex() {
        return _horocycleDirectionVertex;
    }

    /**
     * Is Horocycle
     */
    public boolean isHorocycle() {
        return _isHorocycle;
    }


}

/**
 * Class that represents the edges (one
 * complexes or K(1)) of the GBlinkEmbeddingGraph.
 */
class GBlinkEmbeddingGraphEdge {
    /**
     * One vertex
     */
    private GBlinkEmbeddingGraphVertex _u;

    /**
     * The other vertex
     */
    private GBlinkEmbeddingGraphVertex _v;

    /**
     * A Label for this edge
     */
    private int _label;

    /**
     * When this edge was created, it was by processing
     * _u on the "linkingObject".
     */
    private Object _linkingObject;

    /**
     * Constructor of GBlinkEmbeddingGraphEdge
     */
    public GBlinkEmbeddingGraphEdge(int label, GBlinkEmbeddingGraphVertex u, GBlinkEmbeddingGraphVertex v, Object linkingObject) {
        _label = label;
        _u = u;
        _v = v;
        _linkingObject = linkingObject;
    }

    /**
     * Get other vertex of this edge.
     */
    public GBlinkEmbeddingGraphVertex getOpposite(GBlinkEmbeddingGraphVertex w) {
        if (w == _u) return _v;
        else if (w == _v) return _u;
        else throw new RuntimeException("");
    }

    /**
     * Return the label.
     */
    public int getLabel() {
        return _label;
    }

    /**
     * One of the vertices.
     */
    public GBlinkEmbeddingGraphVertex getU() {
        return _u;
    }

    /**
     * The other one.
     */
    public GBlinkEmbeddingGraphVertex getV() {
        return _v;
    }

    /**
     * Test if edge matches the conditions.
     */
    public boolean test(GBlinkEmbeddingGraphVertexType t1, GBlinkEmbeddingGraphVertexType t2, Object ... linkingObject) {
        boolean found = false;
        for (Object lo: linkingObject)
            if (lo ==_linkingObject) {
                found=true;
                break;
            }
        if (!found)
            return false;
        if ((t1 == _u.getType() && t2 == _v.getType()) || (t1 == _v.getType() && t2 == _u.getType()))
            return true;
        else
            return false;
    }

    /**
     * toString
     */
    public String toString() {
        return "e"+_label + "   " + _u.toString() + " <-> " + _v.toString() +
                (_linkingObject != null ? " [lo. "+((GBlinkVertex)_linkingObject).getLabel()+"]" : "[lo. null]");
    }

    /**
     * Set label of edge.
     */
    public void setLabel(int label) {
        _label = label;
    }
}
