package blink;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import edu.uci.ics.jung.graph.Edge;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.graph.impl.DirectedSparseEdge;
import edu.uci.ics.jung.graph.impl.SparseGraph;
import edu.uci.ics.jung.graph.impl.SparseVertex;
import edu.uci.ics.jung.utils.UserData;

public class GemGraph {
    private HashMap<Vertex, Vertex> _mapRepresentant;
    private HashMap<Long, Vertex> _mapId2Vertex;
    private Graph _G;
    public GemGraph() throws ClassNotFoundException, IOException, SQLException {
        this.mountGraph();
        this.mountRepresentantMap();
    }

    private GemEntry getGemEntry(Vertex v) {
        return (GemEntry) v.getUserDatum("key");
    }

    public long getIdOfMinEquivalentGemByGemId(long gemId) {
        return this.getGemEntry(_mapRepresentant.get(_mapId2Vertex.get(gemId))).getId();
    }

    public GemEntry getGemEntry(long id) {
        Vertex v = _mapId2Vertex.get(id);
        if (v == null) throw new RuntimeException("Oooppsss");
        else return (GemEntry) v.getUserDatum("key");
    }

    public void addEdge(GemEntry e1, GemEntry e2, GemPathEntry gpe) {
        Vertex u = this.getVertex(e1.getId());
        Vertex v = this.getVertex(e2.getId());
        if (u == null) u = addVertex(e1);
        if (v == null) v = addVertex(e2);
        this.addEdge(gpe);

        // update representant map
        this.mountRepresentantMap();
    }

    public long getRepresentantGemId(long id) {
        return getGemEntry(_mapRepresentant.get(this.getVertex(id))).getId();
    }

    public boolean isConnected(HashSet<Long> S) {
        long[] ids = new long[S.size()];
        int i = 0;
        for (long l : S)
            ids[i++] = l;
        if (ids.length <= 1) return true;
        long repId = this.getRepresentantGemId(ids[0]);
        for (i=1;i<ids.length;i++) {
            if (repId != this.getRepresentantGemId(ids[i]))
                return false;
        }
        return true;
    }

    public HashSet<Long> getConnectedComponentGemEntriesIds(long id1) {
        HashSet<Long> S = new HashSet<Long>();
        Vertex vRep = _mapRepresentant.get(getVertex(id1));

        ArrayList<Vertex> vertices = new ArrayList<Vertex>((Set<Vertex>)_G.getVertices());
        for (Vertex v: vertices) {
            if (vRep.equals(_mapRepresentant.get(v))) {
                S.add(getGemEntry(v).getId());
            }
        }
        return S;
    }

    public Vertex getVertex(long gemId) {
        return _mapId2Vertex.get(gemId);
    }

    private Vertex addVertex(GemEntry e) {
        Vertex v = _G.addVertex(new SparseVertex());
        v.setUserDatum("key",e,UserData.SHARED);
        _mapId2Vertex.put(e.getId(),v);
        return v;
    }

    private void addEdge(GemPathEntry gpe) {
        Vertex u = _mapId2Vertex.get(gpe.getSource());
        Vertex v = _mapId2Vertex.get(gpe.getTarget());

        // add an edge from parent gem to this new simplified gem
        Edge e = _G.addEdge(new DirectedSparseEdge(u, v));
        e.setUserDatum("key", gpe, UserData.SHARED);
    }

    /**
     * get all arrows that are on the subgraph induced by gemEntries
     */
    public HashSet<GemPathEntry> getGemPaths(Collection<GemEntry> gemEntries) {
        HashSet<GemPathEntry> result = new HashSet<GemPathEntry>();

        Stack<Vertex> U = new Stack<Vertex>(); // unprocessed
        HashSet<Vertex> P = new HashSet<Vertex>(); // processed

        for (GemEntry ge: gemEntries) {
            U.add(_mapId2Vertex.get(ge.getId()));
        }

        while (!U.isEmpty()) {
            Vertex v = U.pop();
            P.add(v);
            for (Edge e : (Set<Edge>) v.getInEdges()) {
                GemPathEntry gpe = (GemPathEntry) e.getUserDatum("key");
                result.add(gpe);
                Vertex opposite = e.getOpposite(v);
                if (!U.contains(opposite) && !P.contains(opposite)) {
                    U.push(opposite);
                }
            }
            for (Edge e : (Set<Edge>) v.getOutEdges()) {
                GemPathEntry gpe = (GemPathEntry) e.getUserDatum("key");
                result.add(gpe);
                Vertex opposite = e.getOpposite(v);
                if (!U.contains(opposite) && !P.contains(opposite)) {
                    U.push(opposite);
                }
            }
        }

        // return all getGemPaths
        return result;
    }

    private void mountGraph() throws ClassNotFoundException, IOException, SQLException {
        _mapId2Vertex = new HashMap<Long, Vertex>();

        ArrayList<GemEntry> gems = App.getRepositorio().getGems();
        _G = new SparseGraph();
        for (GemEntry ge: gems) {
            this.addVertex(ge);
        }

        ArrayList<GemPathEntry> arcs = App.getRepositorio().getGemPaths();
        for (GemPathEntry gpe: arcs) {
            this.addEdge(gpe);
        }
    }

    private void mountRepresentantMap() {

        _mapRepresentant = new HashMap<Vertex,Vertex>();


        ArrayList<Vertex> vertices = new ArrayList<Vertex>((Set<Vertex>)_G.getVertices());

        // untag everything
        for (Vertex v: vertices) {
            v.setUserDatum("tag",false,UserData.SHARED);
        }

        // build up components and save representant
        for (Vertex v: vertices) {

            // vertex already processed
            if (v.getUserDatum("tag") != null && v.getUserDatum("tag").equals(true))
                continue;

            // else start construction of new connected component
            HashSet<Vertex> C = new HashSet<Vertex>(); // connected component
            Stack<Vertex> S = new Stack<Vertex>();
            S.push(v);
            while (!S.isEmpty()) {
                Vertex u = S.pop();
                u.setUserDatum("tag",true,UserData.SHARED);
                C.add(u);
                for (Edge e : (Set<Edge>) u.getInEdges()) {
                    Vertex uu = e.getOpposite(u);
                    if (!C.contains(uu))
                        S.push(uu);
                }
                for (Edge e : (Set<Edge>) u.getOutEdges()) {
                    Vertex uu = e.getOpposite(u);
                    if (!C.contains(uu))
                        S.push(uu);
                }
            }

            Vertex min = null;
            for (Vertex u : C) {
                if (min == null) {
                    min = u;
                }
                else if (getGemEntry(u).getGem().compareTo(getGemEntry(min).getGem()) < 0) {
                    min = u;
                }
            }

            for (Vertex u: C) {
                _mapRepresentant.put(u,min);
            }

        }

    }

    public Graph getGraph() {
        return _G;
    }

    public static void updateMinGem() throws SQLException, IOException, ClassNotFoundException {
        GemGraph GG = new GemGraph();
        Graph G = GG.getGraph();

        ArrayList<BlinkEntry> updateList = new ArrayList<BlinkEntry>();

        PrintWriter pw = new PrintWriter(new FileWriter("c:/workspace/blink/log/mingem.log",true));
        pw.println("Update at "+GregorianCalendar.getInstance().getTime());

        ArrayList<BlinkEntry> list = App.getRepositorio().getBlinks(0,10);
        for (BlinkEntry be: list) {
            if (be.get_gem() == -1L || be.get_gem() == 0L)
                continue;
            long newMinGem = GG.getIdOfMinEquivalentGemByGemId(be.get_gem());
            long oldMinGem = be.getMinGem();
            if (newMinGem != oldMinGem) {
                be.setMinGem(newMinGem);
                updateList.add(be);
                pw.println(String.format("update %6d from %6d -> %6d",be.get_id(),oldMinGem,newMinGem));
                pw.flush();
            }
        }
        pw.close();
        App.getRepositorio().updateBlinksMinGem(updateList);

    }

    public static void main(String[] args) throws SQLException, IOException, ClassNotFoundException {
        updateMinGem();
        System.exit(0);
    }
}
