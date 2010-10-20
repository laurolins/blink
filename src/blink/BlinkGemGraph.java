package blink;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import edu.uci.ics.jung.algorithms.cluster.ClusterSet;
import edu.uci.ics.jung.algorithms.cluster.WeakComponentClusterer;
import edu.uci.ics.jung.algorithms.connectivity.BFSDistanceLabeler;
import edu.uci.ics.jung.graph.Edge;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.graph.impl.SparseGraph;
import edu.uci.ics.jung.graph.impl.SparseVertex;
import edu.uci.ics.jung.graph.impl.UndirectedSparseEdge;
import edu.uci.ics.jung.utils.UserData;

/**
 * BlinkGemGraph
 */
public class BlinkGemGraph  {
    private static GemGraph _gemGraph;
    private static ArrayList<BlinkEntry> _blinksOnDatabase;
    public static void reset() {
        _gemGraph = null;
        _blinksOnDatabase = null;
    }
    private static ArrayList<BlinkEntry> getBlinks() throws SQLException {
        if (_blinksOnDatabase == null)  {
            System.out.println("Searching for blinks on database...");
            _blinksOnDatabase = App.getRepositorio().getBlinks(0, 20);
        }
        return (ArrayList<BlinkEntry>)_blinksOnDatabase.clone();
    }
    private static GemGraph getGemGraph() throws SQLException, IOException, ClassNotFoundException {
        if (_gemGraph == null) {
            System.out.println("Loading GemGraph");
            _gemGraph = new GemGraph();
        }
        return _gemGraph;
    }


    private Graph _graph;
    private HashMap<BlinkEntry,Vertex> _mapBlinkEntries;
    private HashMap<GemEntry,Vertex> _mapGemEntries;

    /**
     * Obtain on database the subgraph induced by gBlinks
     */
    public BlinkGemGraph(ArrayList gemsOrGBlinks) throws SQLException, IOException, ClassNotFoundException {
        ArrayList<GBlink> gBlinks = new ArrayList<GBlink>();
        ArrayList<Gem> gems = new ArrayList<Gem>();
        for (Object o: gemsOrGBlinks) {
            if (o instanceof GBlink) {
                gBlinks.add((GBlink) o);
            }
            else if (o instanceof Gem) {
                gems.add((Gem) o);
            }
        }

        ArrayList<BlinkEntry> entries = new ArrayList<BlinkEntry>();
        if (gems.size() > 0) {
            // find
            ArrayList<GemEntry> gemsOnDatabase = new ArrayList<GemEntry>();

            for (Gem gem: gems) {
                long hashCode = gem.getGemHashCode();
                ArrayList<GemEntry> list = App.getRepositorio().getGemsByHashcodeAndHandleNumber(hashCode,gem.getHandleNumber());
                boolean found = false;
                for (GemEntry ge: list) {
                    if (gem.equals(ge.getGem()) && ge.getHandleNumber() == gem.getHandleNumber()) {
                        gemsOnDatabase.add(ge);
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    System.out.println("WARNING: gem not found "+gem.getCurrentLabelling().getLettersString(""));
                }
            }

            // GemGraph
            GemGraph gemGraph = getGemGraph();

            HashSet<Long> representantIds = new HashSet<Long>();
            for (GemEntry ge: gemsOnDatabase) {
                representantIds.add(gemGraph.getRepresentantGemId(ge.getId()));
            }

            for (BlinkEntry be: this.getBlinks()) {
                if (representantIds.contains(be.getMinGem())) {
                    entries.add(be);
                }
            }
        }
        if (gBlinks.size() > 0) {
            ArrayList<BlinkEntry> blinksOnDatabase = getBlinks();
            HashMap<GBlink, BlinkEntry> map = new HashMap<GBlink, BlinkEntry>();
            for (BlinkEntry be : blinksOnDatabase) {
                map.put(be.getBlink(), be);
            }
            for (GBlink gb : gBlinks) {
                entries.add(map.get(gb));
            }
        }
        this.buildFromBlinkEntries(entries);
    }

    private void buildFromBlinkEntries(ArrayList<BlinkEntry> listOfBlinkEntries) throws ClassNotFoundException,
            IOException, SQLException {
        // new graph
        _graph = new SparseGraph();

        //
        GemGraph gemGraph = this.getGemGraph();

        // add blink entry vertices
        _mapBlinkEntries = new HashMap<BlinkEntry,Vertex>();
        _mapGemEntries = new HashMap<GemEntry,Vertex>();
        for (BlinkEntry be: listOfBlinkEntries) {
            // add vertex
            Vertex vBlink = _graph.addVertex(new SparseVertex());
            vBlink.setUserDatum("key",be,UserData.SHARED);
            _mapBlinkEntries.put(be,vBlink);

            // no gem?
            if (be.get_gem() == 0)
                continue; // isolated vertex

            // yes gem
            GemEntry ge = gemGraph.getGemEntry(be.get_gem());
            Vertex vGem = _mapGemEntries.get(ge);
            if (vGem == null) {
                vGem = _graph.addVertex(new SparseVertex());
                vGem.setUserDatum("key",ge,UserData.SHARED);
                _mapGemEntries.put(ge,vGem);
            }

            // add edge with blinkEntry on key field

            Edge eBlinkGem = _graph.addEdge(new UndirectedSparseEdge(vBlink, vGem));
            eBlinkGem.setUserDatum("key", be, UserData.SHARED);

        }

        // calculate the closure of the gems that
        // were touched
        HashSet<GemPathEntry> arcs = gemGraph.getGemPaths(new HashSet<GemEntry>(_mapGemEntries.keySet()));
        for (GemPathEntry gpe: arcs) {
            GemEntry ge1 = gemGraph.getGemEntry(gpe.getSource());
            GemEntry ge2 = gemGraph.getGemEntry(gpe.getTarget());
            Vertex v1 = _mapGemEntries.get(ge1);
            if (v1 == null) {
                v1 = _graph.addVertex(new SparseVertex());
                v1.setUserDatum("key",ge1,UserData.SHARED);
                _mapGemEntries.put(ge1,v1);
            }
            Vertex v2 = _mapGemEntries.get(ge2);
            if (v2 == null) {
                v2 = _graph.addVertex(new SparseVertex());
                v2.setUserDatum("key",ge2,UserData.SHARED);
                _mapGemEntries.put(ge2,v2);
            }

            // put only a tree in the game
            BFSDistanceLabeler labeler = new BFSDistanceLabeler();
            labeler.labelDistances(_graph,v1);
            if (labeler.getUnivistedVertices().contains(v2)) {
                // add edge with blinkEntry on key field
                Edge eGemGem = _graph.addEdge(new UndirectedSparseEdge(v1, v2));
                eGemGem.setUserDatum("key", gpe, UserData.SHARED);
            }
            labeler.removeDecorations(_graph);
        }
    }


    public int getNumberOfComponents() {
        WeakComponentClusterer wc = new WeakComponentClusterer();
        ClusterSet cs = wc.extract(_graph);
        return cs.size();
    }

    public Graph getGraph() {
        return _graph;
    }
}
