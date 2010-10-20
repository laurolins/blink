package blink;

import java.awt.Dimension;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import javax.swing.JFrame;

import edu.uci.ics.jung.graph.Edge;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.graph.impl.DirectedSparseEdge;
import edu.uci.ics.jung.graph.impl.SparseGraph;
import edu.uci.ics.jung.graph.impl.SparseVertex;
import edu.uci.ics.jung.utils.UserData;

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
public class SearchByTwistor {
    private HashSet<Gem> _S = new HashSet<Gem>();
    private Gem _gemWithOneBigon = null;

    GemColor _c1;
    public GemColor getC1() { return _c1; }

    private Graph _G;
    HashMap<Gem,Vertex> _map = new HashMap<Gem,Vertex>();

    public SearchByTwistor(Gem gem, GemColor c1) {
        _c1 = c1;

        // create
        _G = new SparseGraph();


        // add root vertex
        this.storeOnGraph(null,gem.copy(),null);

        gem = gem.copy();

        GemColor c0 = GemColor.yellow;
        GemColor complementColors[] = GemColor.getComplementColors(c0,c1);
        GemColor c2 = complementColors[0];
        GemColor c3 = complementColors[1];

        int initialBigons = gem.getComponentRepository().getBigons(c2,c3).size();
        int minBigons = initialBigons;

        // the given gem has already only "one c2c3-gon".
        if (initialBigons == 1) {
            _gemWithOneBigon = gem;
            return;
        }

        LinkedList<Gem> Q = new LinkedList<Gem>();
        _S.add(gem);
        Q.offer(gem);
        int k = 1;
        while (!Q.isEmpty() && _gemWithOneBigon==null) {
            Gem g = Q.poll();

            // get vertex
            Vertex vg = _map.get(g);

            // System.out.println("Processing "+(k++)+" remaining "+Q.size());
            for (Twistor t: g.findTwistors(c3)) {
                Gem gt; // twisted gem

                // apply 1-twisting on a 3-twistor
                gt = g.copy();
                gt.applyTwistorByLabels(t,c1);
                // gt.goToCodeLabel();
                if (!_S.contains(gt)) {
                    Q.offer(gt);
                    _S.add(gt);

                    this.storeOnGraph(vg,gt.copy(),"1-twisting of "+t);
                }

                // apply 2-twisting on a 3-twistor
                gt = g.copy();
                gt.applyTwistorByLabels(t,c2);
                // gt.goToCodeLabel();
                int numBigons = gt.getComponentRepository().getBigons(c2,c3).size();
                if (numBigons == 1) {
                    _gemWithOneBigon = gt;

                    this.storeOnGraph(vg,gt.copy(),"2-twisting of "+t);

                    return;
                }
                if (!_S.contains(gt)) {
                    _S.add(gt);

                    this.storeOnGraph(vg,gt.copy(),"2-twisting of "+t);

                    if (numBigons <= minBigons) {
                        Q.add(0,gt);
                        minBigons = numBigons;
                    }
                }
            }

            for (Twistor t: g.findTwistors(c2)) {
                Gem gt; // twisted gem

                // apply 1-twisting on a 2-twistor
                gt = g.copy();
                gt.applyTwistorByLabels(t,c1);
                // gt.goToCodeLabel();
                if (!_S.contains(gt)) {
                    Q.offer(gt);
                    _S.add(gt);

                    this.storeOnGraph(vg,gt.copy(),"1-twisting of "+t);

                }

                // apply 3-twisting on a 2-twistor
                gt = g.copy();
                gt.applyTwistorByLabels(t,c3);
                // gt.goToCodeLabel();
                int numBigons = gt.getComponentRepository().getBigons(c2,c3).size();
                if (numBigons == 1) {
                    _gemWithOneBigon = gt;

                    this.storeOnGraph(vg,gt.copy(),"3-twisting of "+t);

                    return;
                }
                if (!_S.contains(gt)) {
                    _S.add(gt);

                    this.storeOnGraph(vg,gt.copy(),"3-twisting of "+t);

                    if (numBigons <= minBigons) {
                        Q.add(0,gt);
                        minBigons = numBigons;
                    }
                }
            }

            for (Twistor t: g.findTwistors(c1)) {
                Gem gt; // twisted gem

                if (g.getComponentRepository().getBigons(c2,c3).size() == initialBigons)
                    continue;

                // apply 2-twisting on a 1-twistor
                gt = g.copy();
                gt.applyTwistorByLabels(t, c2);
                // gt.goToCodeLabel();
                if (!_S.contains(gt)) {
                    Q.offer(gt);
                    _S.add(gt);

                    this.storeOnGraph(vg,gt.copy(),"2-twisting of "+t);

                }

                // apply 3-twisting on a 1-twistor
                gt = g.copy();
                gt.applyTwistorByLabels(t,c3);
                // gt.goToCodeLabel();
                if (!_S.contains(gt)) {
                    Q.offer(gt);
                    _S.add(gt);

                    this.storeOnGraph(vg,gt.copy(),"3-twisting of "+t);

                }
            }
        }
    }


    private boolean _onPath = false;
    public Graph getGraph() {

        if (!_onPath && _gemWithOneBigon != null) { // tag vertices from representant backwards...
            Vertex v = _map.get(_gemWithOneBigon);
            while (true) {
                v.setUserDatum("onPath", true, UserData.SHARED);
                Set s = v.getInEdges();
                if (s.isEmpty())
                    break;
                Edge e = (Edge) s.iterator().next();
                Vertex u = e.getOpposite(v);
                v = u;
            }
            _onPath = true;
        }

        return _G;
    }

    private void storeOnGraph(Vertex parentVertex, Gem child, String st) {
        Vertex childVertex = _G.addVertex(new SparseVertex());
        childVertex.setUserDatum("key", child, UserData.SHARED);
        _map.put(child, childVertex);

        // add edge
        if (parentVertex != null) {
            Edge e = _G.addEdge(new DirectedSparseEdge(parentVertex, childVertex));
            e.setUserDatum("key", st, UserData.SHARED);
        }
    }

    public Gem getGemWithOneBigon() {
        return _gemWithOneBigon;
    }

    public static void main1(String[] args) throws ClassNotFoundException, IOException, SQLException {
        Gem gem = App.getRepositorio().getGemEntryByCatalogNumber(26,511,0).getGem();
        SearchByTwistor S = new SearchByTwistor(gem, GemColor.blue);
        Gem gemSimplified = S.getGemWithOneBigon();

        if (gemSimplified != null) {
            // desenhar o mapa
            JFrame f = new JFrame("Reduction Graph");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setSize(new Dimension(1024,768));
            f.setContentPane(new PanelGemViewer(gemSimplified,false));
            f.setVisible(true);
            // desenhar o mapa
        }
        else {
            System.out.println("Does not exist!!!");
            System.exit(0);
        }
    }

    public static void main(String[] args) throws ClassNotFoundException, IOException, SQLException {

        ArrayList<GemEntry> list = App.getRepositorio().getGemsByNumVertices(28,28);

        PrintWriter pw = new PrintWriter("c:/workspace/blink/log/twist.log");
        pw.println("List of gems that did not reach one 23-bigon by twistings");

        int noSolution = 0;
        int processed = 0;
        for (GemEntry e: list) {
            System.out.println(
                    String.format(
                    "Processing R %2d %5d    Processed: %5d  NoSolution: %5d",
                    e.getNumVertices(),e.getCatalogNumber(),
                    (processed++),noSolution));
            Gem gem = e.getGem();
            SearchByTwistor S = new SearchByTwistor(gem, GemColor.blue);
            Gem gemSimplified = S.getGemWithOneBigon();
            if (gemSimplified == null) {
                pw.println("R"+e.getNumVertices()+"-"+e.getCatalogNumber());
                pw.flush();
                noSolution++;
            }
        }
        pw.close();
        System.exit(0);
    }

}
