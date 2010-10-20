package blink;

import java.util.ArrayList;
import java.util.HashMap;

import edu.uci.ics.jung.graph.Edge;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.graph.impl.SparseGraph;
import edu.uci.ics.jung.graph.impl.SparseVertex;
import edu.uci.ics.jung.graph.impl.UndirectedSparseEdge;
import edu.uci.ics.jung.utils.Pair;
import edu.uci.ics.jung.utils.UserData;

public class Gist {
    private ArrayList<GistVertex> _vertices;
    public Gist() {
        _vertices = new ArrayList<GistVertex>();
    }
    public GistVertex newVertex() {
        GistVertex v = new GistVertex(_vertices.size()+1);
        _vertices.add(v);
        return v;
    }
    public ArrayList<GistVertex> getVertices() {
        return _vertices;
    }
    public void setNeighbours(GistVertex u, GistVertex v, GistEdgeType t) {
        u.setNeighbour(v,t);
        v.setNeighbour(u,t);
    }

    public GemString getGemString() {
        GemString G = new GemString();

        HashMap<GistVertex,GemStringVertex> map = new HashMap<GistVertex,GemStringVertex>();
        HashMap<GemStringVertex,GistVertex[]> crossMap = new HashMap<GemStringVertex,GistVertex[]>();
        HashMap<GemStringEdge,Pair> mapEdges = new HashMap<GemStringEdge,Pair>();

        // create the vertices
        for (GistVertex v: _vertices) {
            if (map.get(v) != null)
                continue;

            GistVertex vPlus = v.getPlus();
            GistVertex vMinus = v.getMinus();
            GistVertex vTimes = v.getTimes();

            // End: e
            if (vPlus == v && vMinus == v && vTimes == v) {
                GemStringVertex vv = G.newVertex(GemStringVertexType.end);
                map.put(v,vv);
            }
            // Minus: +
            else if (vPlus == v && vMinus != v && vMinus == vTimes) {
                GemStringVertex vv = G.newVertex(GemStringVertexType.plus);
                map.put(v,vv);
                map.put(vMinus,vv);
            }
            // Minus: -
            else if (vMinus == v && vPlus != v && vPlus == vTimes) {
                GemStringVertex vv = G.newVertex(GemStringVertexType.minus);
                map.put(v,vv);
                map.put(vPlus,vv);
            }
            // Times: X
            else if (vTimes == v && vPlus != v && vPlus == vMinus) {
                GemStringVertex vv = G.newVertex(GemStringVertexType.times);
                map.put(v,vv);
                map.put(vPlus,vv);
            }
            // Cross: c
            else if (v != vMinus && v != vPlus && v != vTimes &&
                     vPlus != vMinus && vPlus != vTimes &
                     vMinus != vTimes) {
                GemStringVertex vv = G.newVertex(GemStringVertexType.cross);
                map.put(v,vv);
                map.put(vPlus,vv);
                map.put(vMinus,vv);
                map.put(vTimes,vv);
                crossMap.put(vv,new GistVertex[] {v,vMinus,vTimes,vPlus});
            }
            else throw new RuntimeException("OOOpppsss");
        }

        // create the edges
        for (GistVertex u: _vertices) {
            GistVertex v = u.getZero();
            if (u.getLabel() > v.getLabel())
                continue;
            GemStringVertex uu = map.get(u);
            GemStringVertex vv = map.get(v);
            GemStringEdge e = G.newEdge(uu,vv);
            mapEdges.put(e,new Pair(u,v));
            // System.out.println(String.format("Edge %3d(%3d) %3d(%3d)",u.getLabel(),uu.getLabel(),v.getLabel(),vv.getLabel()));
        }

        // permute the cross adjacency to preserve
        // the crossing pattern
        for (GemStringVertex v: G.getVertices()) {
            if (v.getType() != GemStringVertexType.cross)
                continue;
            GistVertex[] p = crossMap.get(v);

            // log
            /*
            System.out.println("\n--- Cross:");
            for (int i=0;i<p.length;i++) {
                System.out.print(String.format("%3d ",p[i].getLabel()));
            }
            System.out.println("\n--- Edges:");
            for (GemStringEdge e : v.getEdges()) {
                System.out.print(
                        String.format("(%3d %3d)",
                        ((GemStringVertex)e.getVertices().getFirst()).getLabel(),
                        ((GemStringVertex)e.getVertices().getSecond()).getLabel()));
            }
            System.out.println("");*/

            // System.out.print(String.format("%3d ",p[i].getLabel()));

            ArrayList<GemStringEdge> perm = new ArrayList<GemStringEdge>();
            for (int i = 0; i < 4; i++) {
                for (GemStringEdge e : v.getEdges()) {
                    Pair pair = mapEdges.get(e);
                    GistVertex uu = (GistVertex) pair.getFirst();
                    GistVertex vv = (GistVertex) pair.getSecond();
                    if (uu == p[i] ||  vv == p[i])
                        perm.add(e);
                }
            }
            v.permuteEdges(perm);
        }

        return G;
    }



    public Gem getGem() {
        Gem G = new Gem();
        HashMap<GistVertex,GemVertex> map = new HashMap<GistVertex,GemVertex>();
        for (GistVertex v: _vertices) {
            GemVertex vv = G.newVertex(v.getLabel());
            map.put(v,vv);
        }
        for (GistVertex v : _vertices) {
            GemVertex vv = map.get(v);

            { // zero
                GistVertex u = v.getZero();
                GemVertex uu = map.get(u);
                Gem.setNeighbours(vv,uu,GemColor.yellow);
            } // zero

            { // one
                GistVertex u = v.getPlus().getZero().getPlus();
                GemVertex uu = map.get(u);
                Gem.setNeighbours(vv,uu,GemColor.blue);
            } // one

            { // two
                GistVertex u = v.getMinus().getZero().getMinus();
                GemVertex uu = map.get(u);
                Gem.setNeighbours(vv,uu,GemColor.red);
            } // two

            { // three
                GistVertex u = v.getTimes().getZero().getTimes();
                GemVertex uu = map.get(u);
                Gem.setNeighbours(vv,uu,GemColor.green);
            } // three
        }

        if (!G.check())
            throw new RuntimeException("OOoopppsss");
        if (G.getAgemality() != 0)
            throw new RuntimeException("OOoopppsss");

        G.goToCodeLabel();

        return G;
    }

    public Graph getGraph() {
        Graph graph = new SparseGraph();

        // create vertices
        HashMap<GistVertex, Vertex> map = new HashMap<GistVertex, Vertex>();
        for (GistVertex v : _vertices) {
            Vertex vv = graph.addVertex(new SparseVertex());
            vv.setUserDatum("key", v, UserData.SHARED);
            map.put(v, vv);
        }

        // create edges
        for (GistVertex v : _vertices) {
            Vertex vv = map.get(v);
            GistVertex vs[] = {v.getPlus(), v.getMinus(), v.getTimes(), v.getZero()};
            GistEdgeType ts[] = {GistEdgeType.plus, GistEdgeType.minus, GistEdgeType.times, GistEdgeType.zero};
            for (int i=0;i<4;i++) {
                if (v.getLabel() < vs[i].getLabel()) {
                    Vertex vvi = map.get(vs[i]);
                    Edge e = graph.addEdge(new UndirectedSparseEdge(vv, vvi));
                    e.setUserDatum("key", ts[i], UserData.SHARED);
                }
            }
        }

        return graph;
    }




}

class GistVertex {
    private int _label;
    private GistVertex _plus;
    private GistVertex _minus;
    private GistVertex _zero;
    private GistVertex _times;
    public GistVertex(int label) {
        _label = label;
    }
    public void setNeighbour(GistVertex v, GistEdgeType type) {
        if (type == GistEdgeType.minus) _minus = v;
        else if (type == GistEdgeType.plus) _plus = v;
        else if (type == GistEdgeType.times) _times = v;
        else if (type == GistEdgeType.zero) _zero = v;
    }
    public GistVertex getNeighbour(GistEdgeType type) {
        if (type == GistEdgeType.minus) return _minus;
        else if (type == GistEdgeType.plus) return _plus;
        else if (type == GistEdgeType.times) return _times;
        else if (type == GistEdgeType.zero) return _zero;
        else throw new RuntimeException();
    }
    public int getLabel() { return _label; }
    public GistVertex getPlus() { return _plus; }
    public GistVertex getMinus() { return _minus; }
    public GistVertex getTimes() { return _times; }
    public GistVertex getZero() { return _zero; }
}

enum GistEdgeType {
    plus, minus, zero, times;
    public String getLabel() {
        if (this == plus) return "+";
        else if (this == minus) return "-";
        else if (this == times) return "x";
        else if (this == zero) return "0";
        else return "";
    }
}
