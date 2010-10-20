package blink.graph;

import java.util.Set;

import edu.uci.ics.jung.algorithms.flows.EdmondsKarpMaxFlow;
import edu.uci.ics.jung.graph.Edge;
import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.graph.impl.DirectedSparseEdge;
import edu.uci.ics.jung.graph.impl.DirectedSparseGraph;
import edu.uci.ics.jung.graph.impl.DirectedSparseVertex;
import edu.uci.ics.jung.utils.MutableInteger;
import edu.uci.ics.jung.utils.UserDataContainer;

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
public class TestJung {
    public TestJung() {
    }


    // public static void find(Gem g, RectangleRepository rr,




    public static void main(String[] args) {
        UserDataContainer.CopyAction ca = new UserDataContainer.CopyAction.Shared();
        UserDataContainer.CopyAction cs = new UserDataContainer.CopyAction.Remove();
        DirectedSparseGraph g = new DirectedSparseGraph();

        Vertex v1 = (Vertex) g.addVertex(new DirectedSparseVertex());
        v1.setUserDatum("LABEL","1",cs);
        Vertex v2 = (Vertex) g.addVertex(new DirectedSparseVertex());
        v2.setUserDatum("LABEL","2",cs);
        Vertex v3 = (Vertex) g.addVertex(new DirectedSparseVertex());
        v3.setUserDatum("LABEL","3",cs);
        Vertex v4 = (Vertex) g.addVertex(new DirectedSparseVertex());
        v4.setUserDatum("LABEL","4",cs);

        Edge e;

        e = g.addEdge(new DirectedSparseEdge(v1, v2));
        e.setUserDatum("CAPACITY",new MutableInteger(1),ca);
        e.setUserDatum("LABEL","1a",cs);
        e = g.addEdge(new DirectedSparseEdge(v2, v1));
        e.setUserDatum("CAPACITY",new MutableInteger(1),ca);
        e.setUserDatum("LABEL","1b",cs);

        e = g.addEdge(new DirectedSparseEdge(v1, v3));
        e.setUserDatum("CAPACITY",new MutableInteger(1),ca);
        e.setUserDatum("LABEL","2a",cs);
        e = g.addEdge(new DirectedSparseEdge(v3, v1));
        e.setUserDatum("CAPACITY",new MutableInteger(1),ca);
        e.setUserDatum("LABEL","2b",cs);

        e = g.addEdge(new DirectedSparseEdge(v2, v3));
        e.setUserDatum("CAPACITY",new MutableInteger(1),ca);
        e.setUserDatum("LABEL","3a",cs);
        e = g.addEdge(new DirectedSparseEdge(v3, v2));
        e.setUserDatum("CAPACITY",new MutableInteger(1),ca);
        e.setUserDatum("LABEL","3b",cs);

        e = g.addEdge(new DirectedSparseEdge(v2, v4));
        e.setUserDatum("CAPACITY",new MutableInteger(1),ca);
        e.setUserDatum("LABEL","4a",cs);
        e = g.addEdge(new DirectedSparseEdge(v4, v2));
        e.setUserDatum("CAPACITY",new MutableInteger(1),ca);
        e.setUserDatum("LABEL","4b",cs);

        e = g.addEdge(new DirectedSparseEdge(v3, v4));
        e.setUserDatum("CAPACITY",new MutableInteger(1),ca);
        e.setUserDatum("LABEL","5a",cs);
        e = g.addEdge(new DirectedSparseEdge(v4, v3));
        e.setUserDatum("CAPACITY",new MutableInteger(1),ca);
        e.setUserDatum("LABEL","5b",cs);

        e = g.addEdge(new DirectedSparseEdge(v1, v4));
        e.setUserDatum("CAPACITY",new MutableInteger(1),ca);
        e.setUserDatum("LABEL","6a",cs);
        e = g.addEdge(new DirectedSparseEdge(v4, v1));
        e.setUserDatum("CAPACITY",new MutableInteger(1),ca);
        e.setUserDatum("LABEL","6b",cs);


        EdmondsKarpMaxFlow ek = new EdmondsKarpMaxFlow(g,v1,v4,"CAPACITY","FLOW");
        ek.evaluate(); // This actually instructs the solver to compute the max flow

        System.out.println("Min Cut (maxflow="+ek.getMaxFlow()+")");
        for (Edge ee: (Set<Edge>) ek.getMinCutEdges()) {

            System.out.print(""+ee.getUserDatum("LABEL")+" ");
        }
        System.out.println("\n\nSource");
        for (Vertex vv: (Set<Vertex>) ek.getNodesInSourcePartition()) {
            System.out.print(""+vv.getUserDatum("LABEL")+" ");
        }
        System.out.println("\n\nSink");
        for (Vertex vv: (Set<Vertex>) ek.getNodesInSinkPartition()) {
            System.out.print(""+vv.getUserDatum("LABEL")+" ");
        }



    }
}
