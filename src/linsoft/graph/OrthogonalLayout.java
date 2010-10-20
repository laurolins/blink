package linsoft.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.JFrame;

import linsoft.Pair;
import linsoft.UnorderedPair;
import linsoft.netsimplex.Arc;
import linsoft.netsimplex.Network;
import linsoft.netsimplex.Node;


/**
 * Tamassia's Algorithm
 */
public class OrthogonalLayout {

    public static boolean STOP = false;

    public OrthogonalLayout(PlanarRepresentation P) {

        HashMap<Integer,ArrayList<PRVertex>> mapDegree = P.getMapDegree2Vertices();

        if (P.getNumberOfVertices() > mapDegree.get(1).size() +
            mapDegree.get(2).size() + mapDegree.get(3).size() + mapDegree.get(4).size())
            throw new RuntimeException("Ooooppsss");

        int n = mapDegree.get(1).size()+mapDegree.get(2).size()+mapDegree.get(3).size() + P.getNumberOfFaces() + 2;
        int m = 10000;

        // mount network from planar representation
        Network N = new Network(n,m);

        // create nodes on the network
        HashMap<Object, Node> mapObject2Node = new HashMap<Object, Node>();
        HashMap<Node, Object> mapNode2Object = new HashMap<Node, Object>();

        HashMap<Object, Arc> mapObject2Arc = new HashMap<Object, Arc>();
        HashMap<Arc, Object> mapArc2Object = new HashMap<Arc, Object>();

        ArrayList<PRVertex> Vhat = new ArrayList<PRVertex>();
        Vhat.addAll(mapDegree.get(1));
        Vhat.addAll(mapDegree.get(2));
        Vhat.addAll(mapDegree.get(3));

        for (PRVertex v: Vhat) {
            Node node = N.addNode(0);
            node.setObject(v);
            mapObject2Node.put(v,node);
            mapNode2Object.put(node,v);
        }
        for (PRFace f: P.getFaces()) {
            Node node = N.addNode(0);
            node.setObject(f);
            mapObject2Node.put(f,node);
            mapNode2Object.put(node,f);
        }

        Node source = N.addNode(0);
        source.setObject("source");
        Node target = N.addNode(0);
        target.setObject("target");

        // arcs Av
        for (PRFace f: P.getFaces()) {
            Node nf = mapObject2Node.get(f);
            HashSet<PRVertex> V = new HashSet<PRVertex>();
            for (PREdge e: f.getEdges()) {
                if (e.getV1().getDegree() <= 3 && !V.contains(e.getV1())) {
                    PRVertex v1 = e.getV1();
                    V.add(v1);
                    Node nv = mapObject2Node.get(v1);
                    Arc a = null;
                    if (f.isAdjustmentDegreeFace() && v1.isDegreeAdjustedVertex()) {
                        if (v1.isCornerDegreeAdjustmentVertex()) {
                            // 90 degrees on the face f
                            a = N.addArc(nv, nf, 0, 0, 0);
                        }
                        else {
                            // 180 degrees on the face f
                            a = N.addArc(nv, nf, 0, 1, 1);
                        }
                    }
                    else {
                        a = N.addArc(nv, nf, 0, Integer.MAX_VALUE);
                    }
                    // a = N.addArc(nv, nf, 0, Integer.MAX_VALUE);
                    Pair pair = new Pair(nv, nf);
                    mapObject2Arc.put(pair, a);
                    mapArc2Object.put(a, pair);
                }
                if (e.getV2().getDegree() <= 3 && !V.contains(e.getV2())) {
                    PRVertex v2 = e.getV2();
                    V.add(v2);
                    Node nv = mapObject2Node.get(e.getV2());
                    Arc a = null;
                    if (f.isAdjustmentDegreeFace() && v2.isDegreeAdjustedVertex()) {
                        if (v2.isCornerDegreeAdjustmentVertex()) {
                            // 90 degrees on the face f
                            a = N.addArc(nv, nf, 0, 0, 0);
                        }
                        else {
                            // 180 degrees on the face f
                            a = N.addArc(nv, nf, 0, 1, 1);
                        }
                    }
                    else {
                        a = N.addArc(nv, nf, 0, Integer.MAX_VALUE);
                    }
                    // a = N.addArc(nv, nf, 0, Integer.MAX_VALUE);
                    Pair pair = new Pair(nv, nf);
                    mapObject2Arc.put(pair, a);
                    mapArc2Object.put(a, pair);
                }
            }
        }

        // arcs Af
        HashSet<UnorderedPair> S = new HashSet<UnorderedPair>();
        for (PREdge e: P.getEdges()) {
            PRFace f1 = e.getFaces().get(0);
            PRFace f2 = e.getFaces().get(1);

            UnorderedPair p = new UnorderedPair(f1,f2);
            if (S.contains(p))
                continue;

            S.add(p);
            Node nf1 = mapObject2Node.get(f1);
            Node nf2 = mapObject2Node.get(f2);

            int minFlow = 0;
            int maxFlow = Integer.MAX_VALUE;

            if (e.isDegreeAdjustementEdge()) {
                maxFlow = 0;
            }

            Arc a1 = N.addArc(nf1,nf2,1,minFlow,maxFlow);
            Pair p1 = new Pair(f1, f2);
            mapObject2Arc.put(p1,a1);
            mapArc2Object.put(a1,p1);

            if (f1 != f2) {
                Arc a2 = N.addArc(nf2, nf1, 1,minFlow,maxFlow);
                Pair p2 = new Pair(f2,f1);
                mapObject2Arc.put(p2, a2);
                mapArc2Object.put(a2, p2);
            }
        }

        // arcs AS and AT
        int sumFromSource = 0;
        int sumToTarget = 0;
        for (PRFace f: P.getFaces()) {
            Node nf = mapObject2Node.get(f);
            if (f.size() <= 3 && P.getExternalFace() != f) {
                int bmax = 4-f.size();
                Arc a = N.addArc(source,nf,0,bmax);
                sumFromSource+=bmax;
            }
            else if (f == P.getExternalFace()) {
                int bmax = f.size()+4;
                Arc a = N.addArc(nf,target,0, bmax);
                sumToTarget += bmax;
            }
            else if (f.size() > 4) {
                int bmax = f.size() - 4;
                Arc a = N.addArc(nf, target, 0, bmax);
                sumToTarget += bmax;
            }
        }
        for (PRVertex v: Vhat) {
            int bmax = 4-v.getDegree();
            Node nv = mapObject2Node.get(v);
            Arc a = N.addArc(source, nv, 0, bmax);
            sumFromSource += bmax;
        }



        //
        /*
        try {
            NetworkGraphModel.execute(N);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        if (1 + 5 - 6 == 0)
            return;*/
        //System.exit(0);

        //
        /*
        System.out.println("SumFromSource = "+sumFromSource);
        System.out.println("SumToTarget   = "+sumToTarget);

        // vertices
        for (Object o: mapObject2Node.keySet()) {
            if (o instanceof PRVertex) {
                System.out.println(String.format("vertex: %4d  <->  node: %4d", ((PRVertex) o).getId(),
                                                 mapObject2Node.get(o).get_index()));
            }
            else if (o instanceof PRFace) {
                System.out.println(String.format("face:   %4d  <->  node: %4d", ((PRFace) o).getId(),
                                                 mapObject2Node.get(o).get_index()));
            }
        } */

        {
            N.addArc(target,source,0,sumFromSource,sumFromSource);
        }

        /*
        for (int i=0;i<N.getNumberOfArcs();i++) {
            Arc a = N.getArc(i);
            Object o1 = mapNode2Object.get(a.get_tail());
            Object o2 = mapNode2Object.get(a.get_head());
            String s1 =
                    (a.get_tail() == source ? "source" :
                     (a.get_tail() == target ? "target" :
                      (o1 instanceof PRVertex ? "v" + ((PRVertex) o1).getId() :
                       (o1 instanceof PRFace ? "f" + ((PRFace) o1).getId() : "?"))));
            String s2 =
                    (a.get_head() == source ? "source" :
                     (a.get_head() == target ? "target" :
                      (o2 instanceof PRVertex ? "v" + ((PRVertex) o2).getId() :
                       (o2 instanceof PRFace ? "f" + ((PRFace) o2).getId() : "?"))));
            System.out.println(String.format("arc %-6s -> %-6s  =  flow: %5d cost: %5d bmin: %5d bmax: %5s",s1,s2,a.get_flow(),
                               a.get_cost(),a.get_minFlow(),(a.get_maxFlow() == Integer.MAX_VALUE ? "inf" : ""+a.get_maxFlow())));
        } */

        // N.networkGenerator();

        N.initNetworkForAlgorithm();
        // N.networkGenerator();
        while(!N.nextIteration()) {
            // System.out.println("iter: "+N.getNetworkValue());
            continue;
        }

        // System.out.println("Network is feasible? "+N.isOriginalNetworkFeasible());
        // System.out.println("On exit");

        /*
        for (int i=0;i<N.getNumberOfArcs();i++) {
            Arc a = N.getArc(i);
            Object o1 = mapNode2Object.get(a.get_tail());
            Object o2 = mapNode2Object.get(a.get_head());
            String s1 =
                    (a.get_tail() == source ? "source" :
                     (a.get_tail() == target ? "target" :
                      (o1 instanceof PRVertex ? "v" + ((PRVertex) o1).getId() :
                       (o1 instanceof PRFace ? "f" + ((PRFace) o1).getId() : "?"))));
            String s2 =
                    (a.get_head() == source ? "source" :
                     (a.get_head() == target ? "target" :
                      (o2 instanceof PRVertex ? "v" + ((PRVertex) o2).getId() :
                       (o2 instanceof PRFace ? "f" + ((PRFace) o2).getId() : "?"))));
            System.out.println(String.format("arc %-6s -> %-6s  =  flow: %5d cost: %5d bmin: %5d bmax: %5s",s1,s2,a.get_flow(),
                               a.get_cost(),a.get_minFlow(),(a.get_maxFlow() == Integer.MAX_VALUE ? "inf" : ""+a.get_maxFlow())));
        } */

        // System.out.println("\n\nNetwork value: "+N.getNetworkValue());

        // mount orthogonal representation extension based on the
        // flow on the planar representation P.

        // init. by default every transition is 90 and every
        // edge has no bends. (bends = e).

        for (PRFace f: P.getFaces()) { // take care of transitions
            Node nf = mapObject2Node.get(f);
            PRVertex[] vs = f.getVertices();
            HashSet<PRVertex> verticesAlreadyCounted = new HashSet<PRVertex>();
            for (int i = 0; i < vs.length; i++) {
                Node nv = mapObject2Node.get(vs[i]);
                if (nv == null || verticesAlreadyCounted.contains((vs[i])))
                    continue;
                Arc a = mapObject2Arc.get(new Pair(nv,nf));
                if (a == null)
                    continue;
                f.setTransition(i,(a.get_flow()+1)*90);
                verticesAlreadyCounted.add(vs[i]);
            }
        }

        ArrayList<PRFace> faces = P.getFaces();
        for (int i=0;i<faces.size();i++) { // take care of bends
            PRFace fi = faces.get(i);

            { // same face case
                Arc aii = mapObject2Arc.get(new Pair(fi,fi));
                if (aii != null) {
                    int indexes[] = fi.getIndexesOfFirstBridge();
                    int xii = aii.get_flow();
                    int bends0[] = new int[xii];
                    int bends1[] = new int[xii];
                    for (int ii = 0;ii<xii;i++) {
                        bends0[ii] = 0;
                        bends1[ii] = 1;
                    }
                    fi.setBends(indexes[0],bends0);
                    fi.setBends(indexes[1],bends1);
                }
            }

            // different faces case
            for (int j=i+1;j<faces.size();j++) {
                PRFace fj = faces.get(j);
                Arc aij = mapObject2Arc.get(new Pair(fi,fj));
                Arc aji = mapObject2Arc.get(new Pair(fj,fi));

                if (aij == null || aji == null)
                    continue;

                int ie = 0; // index of edge e on face i that will be bended
                int je = 0; // index of edge e on face j that will be bended
                for (int ii=0;ii<fi.size();ii++) {
                    PREdge e = fi.getEdge(ii);
                    int jj = fj.indexOf(e);
                    if (jj >= 0) {
                        ie = ii;
                        je = jj;
                        break;
                    }
                }

                int xij = aij.get_flow();
                int xji = aji.get_flow();

                int bendsij[] = new int[xij + xji];
                int bendsji[] = new int[xij + xji];
                for (int ii=xij;ii<bendsij.length;ii++)
                    bendsij[ii] = 1;
                for (int ii=0;ii<xij;ii++)
                    bendsji[ii] = 1;

                fi.setBends(ie,bendsij);
                fj.setBends(je,bendsji);
            }
        }

        // log orthogonal description
        // System.out.println("\nBEFORE BENDS\n"+P.getFullDescription(false));

        P.putAVertexInEachBend();

        // System.out.println("\nAFTER BENDS\n"+P.getFullDescription(false));

        // divide all faces
        for (PRFace f: P.getFaces()) {
            if (f != P.getExternalFace())
                P.divideInternalFaceInRectangles(f);
        }

        // System.out.println("\n\nAFTER INTERNAL FACES\n"+P.getFullDescription(false));

        if (STOP) return;

        // divide all faces
        for (PRFace f: P.getFaces()) {
            if (f == P.getExternalFace())
                P.divideExternalFaceInRectangles(f);
        }

        // System.out.println("\n\nAFTER RECTANGLES\n"+P.getFullDescription(false));

        //if (1 == 1)
        //  return;

        this.setLengths(P);
        /*
        for (PREdge e : P.getEdges()) {
            e.setLength(1);
        }*/

        P.positionEachVertex();

        /*
        JFrame f = new JFrame("Tamassia's Algorithm");
        linsoft.gui.util.Library.resizeAndCenterWindow(f, 640, 480);
        f.setContentPane(new PanelPlanarRepresentation(P));
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.setVisible(true);
        */

        /*
        ArrayList<PRFace> faces = new ArrayList<PRFace>();
        for (int i=0;i<faces.size();i++) {
            for (int j=0;i<faces.size();i++) {
                Node nf = mapObject2Node.get(f);
                for (PREdge e : f.getEdges()) {
                    if (e.getV1().getDegree() > 3)
                        continue;
                    Node nv = mapObject2Node.get(e.getV1());
                    Arc a = N.addArc(nv, nf, 0, Integer.MAX_VALUE);
                    Object[] pair = {nv, nf};
                    mapObject2Arc.put(pair, a);
                    mapArc2Object.put(a, pair);
                }
            }
        }*/
    }

    /**
     * Hypothesis: the faces are already rectangles
     */
    public void setLengths(PlanarRepresentation P) {

        P.mounSideInformationOnPlanarRepresentation();

        // System.out.println("\n\nDescription of edges: E0");
        // System.out.println(P.getFullDescription(true));

        int n = P.getNumberOfFaces();

        if (n == 1) {
            for (PREdge e: P.getEdges()) {
                e.setLength(1);
            }
            return;
        }

        // first network sides 0 -> 2
        Network N02 = new Network(n, P.getNumberOfEdges());
        Network N13 = new Network(n, P.getNumberOfEdges());

        HashMap<PRFace, Node> mapFace2NodeN02 = new HashMap<PRFace, Node>();
        HashMap<PRFace, Node> mapFace2NodeN13 = new HashMap<PRFace, Node>();
        HashMap<Node, PRFace> mapNode2Face = new HashMap<Node, PRFace>();

        HashMap<PREdge, Arc> mapEdge2Arc = new HashMap<PREdge, Arc>();

        for (PRFace f : P.getFaces()) {
            {
                Node node = N02.addNode(0);
                mapFace2NodeN02.put(f, node);
                mapNode2Face.put(node,f);
            }
            {
                Node node = N13.addNode(0);
                mapFace2NodeN13.put(f, node);
                mapNode2Face.put(node,f);
            }
        }

        for (PREdge e: P.getEdges()) {
            PRFace f = e.getFace(0);
            PRFace g = e.getFace(1);

            PRFaceEdge ef = f.getFaceEdge(f.indexOf(e));
            PRFaceEdge eg = g.getFaceEdge(g.indexOf(e));

            Arc a = null;
            if (ef.getSide() == 0 && eg.getSide() == 2) {
                a = N02.addArc(mapFace2NodeN02.get(f),mapFace2NodeN02.get(g),1,1,Integer.MAX_VALUE);
            }
            else if (ef.getSide() == 2 && eg.getSide() == 0) {
                a = N02.addArc(mapFace2NodeN02.get(g),mapFace2NodeN02.get(f),1,1,Integer.MAX_VALUE);
            }
            else if (ef.getSide() == 1 && eg.getSide() == 3) {
                a = N13.addArc(mapFace2NodeN13.get(f),mapFace2NodeN13.get(g),1,1,Integer.MAX_VALUE);
            }
            else if (ef.getSide() == 3 && eg.getSide() == 1) {
                a = N13.addArc(mapFace2NodeN13.get(g),mapFace2NodeN13.get(f),1,1,Integer.MAX_VALUE);
            }
            else {
                System.out.println(""+ef.getSide()+"   "+eg.getSide());
                throw new RuntimeException("OOOopppsss");
            }
            mapEdge2Arc.put(e,a);
        }


        N02.initNetworkForAlgorithm();
        while (!N02.nextIteration())
            continue;

        N13.initNetworkForAlgorithm();
        while (!N13.nextIteration())
            continue;

        /*
        System.out.println("Network 02");
        // -------------------------------
        for (int i=0;i<N02.getNumberOfArcs();i++) {
            Arc a = N02.getArc(i);
            PRFace f1 = mapNode2Face.get(a.get_tail());
            PRFace f2 = mapNode2Face.get(a.get_head());
            String s1 = "special";
            if (f1 != null)
                s1 = "f" + f1.getId();
            String s2 = "special";
            if (f2 != null)
                s2 = "f" + f2.getId();
            System.out.println(String.format("arc %-6s -> %-6s  =  flow: %5d cost: %5d bmin: %5d bmax: %5d",s1,s2,a.get_flow(),
                               a.get_cost(),a.get_minFlow(),(a.get_maxFlow() == Integer.MAX_VALUE ? -1 : a.get_maxFlow())));
        }
        // -------------------------------

        System.out.println("Network 13");
        // -------------------------------
        for (int i=0;i<N13.getNumberOfArcs();i++) {
            Arc a = N13.getArc(i);
            PRFace f1 = mapNode2Face.get(a.get_tail());
            PRFace f2 = mapNode2Face.get(a.get_head());
            String s1 = "special";
            if (f1 != null)
                s1 = "f" + f1.getId();
            String s2 = "special";
            if (f2 != null)
                s2 = "f" + f2.getId();
            System.out.println(String.format("arc %-6s -> %-6s  =  flow: %5d cost: %5d bmin: %5d bmax: %5d",s1,s2,a.get_flow(),
                               a.get_cost(),a.get_minFlow(),(a.get_maxFlow() == Integer.MAX_VALUE ? -1 : a.get_maxFlow())));
        }
        // -------------------------------
        */


        for (PREdge e: P.getEdges()) {
            Arc a = mapEdge2Arc.get(e);
            e.setLength(a.get_flow());
            // System.out.println(String.format("edge %d has length %d",e.getId(),a.get_flow()));
        }
    }



    public static void main(String[] args) throws Exception {
        // PlanarRepresentation P = loadFromFile("res/ex1.pr");
        //new OrthogonalLayout(loadFromFile("res/ex1.pr"));
        //new OrthogonalLayout(loadFromFile("res/ex2.pr"));
        //new OrthogonalLayout(loadFromFile("res/ex3.pr"));
        //new OrthogonalLayout(loadFromFile("res/ex4.pr"));
        //new OrthogonalLayout(loadFromFile("res/ex5.pr"));
        //new OrthogonalLayout(loadFromFile("res/ex6.pr"));
        //new OrthogonalLayout(loadFromFile("res/ex8.pr"));
        //PlanarRepresentation P = PlanarRepresentation.loadFromFile("res/ex12.pr");
        PlanarRepresentation P = PlanarRepresentation.loadFromFile("res/ex17.pr");
        int n = P.getNumberOfEdges();
        P.createFacesOnVerticesWithDegreeGreaterThan4();
        new OrthogonalLayout(P);

        JFrame f = new JFrame("Tamassia's Algorithm");
        linsoft.gui.util.Library.resizeAndCenterWindow(f, 640, 480);
        f.setContentPane(new PanelPlanarRepresentation(P));
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.setVisible(true);

        /*
        for (int i=1;i<=n;i++) {
            double path[] = P.getPathOfEdge(i);
            System.out.println("Edge "+i+"  "+blink.Library.intArrayToString(path));
        }*/
    }

}


