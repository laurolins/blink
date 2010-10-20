package blink;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.StringTokenizer;

import edu.uci.ics.jung.graph.Edge;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.graph.impl.SparseGraph;
import edu.uci.ics.jung.graph.impl.SparseVertex;
import edu.uci.ics.jung.graph.impl.UndirectedSparseEdge;
import edu.uci.ics.jung.utils.Pair;
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
public class GemString {
    private ArrayList<GemStringVertex> _vertices;

    public GemString() {
        _vertices = new ArrayList<GemStringVertex>();
    }

    public void delete(GemStringVertex v) {
        _vertices.remove(v);
        for (GemStringVertex u: _vertices) {
            u.removeEdgesWithVertex(v);
        }
    }

    public ArrayList<GemStringVertex> getVertices() {
        return _vertices;
    }

    public void delete(GemStringEdge e) {
        ((GemStringVertex)e.getVertices().getFirst()).removeEdge(e);
        ((GemStringVertex)e.getVertices().getSecond()).removeEdge(e);
    }

    public GemString(String s, char tokenSep, char vertSep) {
        _vertices = new ArrayList<GemStringVertex>();

        HashMap<Integer,GemStringVertex> map = new HashMap<Integer,GemStringVertex>();

        { // create the vertices
            StringTokenizer st = new StringTokenizer(s,""+vertSep);
            while (st.hasMoreTokens()) {
                String line = st.nextToken();
                StringTokenizer st2 = new StringTokenizer(line, "" + tokenSep);
                if (!st2.hasMoreTokens())
                    continue;
                int label = Integer.parseInt(st2.nextToken());
                GemStringVertexType type = GemStringVertexType.fromString(st2.nextToken());
                GemStringVertex v = new GemStringVertex(label, type);
                _vertices.add(v);
                map.put(label, v);
            }
        } // create the vertices

        HashMap<Pair,GemStringEdge> map2 = new HashMap<Pair,GemStringEdge>();
        { // create the edges

            StringTokenizer st = new StringTokenizer(s,""+vertSep);
            while (st.hasMoreTokens()) {
                String line = st.nextToken();
                StringTokenizer st2 = new StringTokenizer(line, "" + tokenSep);
                if (!st2.hasMoreTokens())
                    continue;
                int label = Integer.parseInt(st2.nextToken());
                GemStringVertexType type = GemStringVertexType.fromString(st2.nextToken());
                GemStringVertex v = map.get(label);
                int k = (type == GemStringVertexType.end) ? 1 :
                        ((type == GemStringVertexType.plus ||
                         type == GemStringVertexType.times ||
                         type == GemStringVertexType.minus) ? 2 : 4);

                for (int i=0;i<k;i++) {
                    GemStringEdge e = map2.get(new Pair(v,i));

                    // the edge was already added
                    if (e != null) continue;

                    // the other endpoint of this edge
                    String sn = st2.nextToken();
                    StringTokenizer st3 = new StringTokenizer(sn,"ABCDabcd");
                    StringTokenizer st4 = new StringTokenizer(sn,"0123456789");
                    GemStringVertex u = map.get(Integer.parseInt(st3.nextToken()));
                    int j = (int) (st4.nextToken().toUpperCase().charAt(0) - 'A');

                    // add edge
                    e = this.newEdge(v,u);
                    map2.put(new Pair(v,i),e);
                    map2.put(new Pair(u,j),e);
                }
            }
        } // create the edges

        // sort the crossings
        for (GemStringVertex v: _vertices) {
            if (v.getType() != GemStringVertexType.cross) continue;
            ArrayList<GemStringEdge> edges = v.getEdges();
            ArrayList<GemStringEdge> pedges = new ArrayList<GemStringEdge>();
            for (int i=0;i<edges.size();i++) {
                pedges.add(map2.get(new Pair(v,i)));
            }
            v.permuteEdges(pedges);
        }
    }

    public GemStringEdge newEdge(GemStringVertex u, GemStringVertex v) {
        GemStringEdge e = new GemStringEdge(u,v);
        u.addEdge(e);
        v.addEdge(e);
        return e;
    }

    public GemStringVertex newVertex(GemStringVertexType type) {
        int newLabel = 1;
        for (GemStringVertex v:_vertices) {
            if (v.getLabel() >= newLabel) {
                newLabel = v.getLabel()+1;
            }
        }
        GemStringVertex v = new GemStringVertex(newLabel, type);
        _vertices.add(v);
        return v;
    }

    public Graph getGraph() {
        Graph graph = new SparseGraph();

        // create vertices
        HashMap<GemStringVertex, Vertex> map = new HashMap<GemStringVertex, Vertex>();
        for (GemStringVertex gv : _vertices) {
            Vertex v = graph.addVertex(new SparseVertex());
            v.setUserDatum("key", gv, UserData.SHARED);
            map.put(gv, v);
        }

        // create edges
        HashSet<GemStringEdge> addedEdges = new HashSet<GemStringEdge>();
        for (GemStringVertex gv : _vertices) {
            Vertex v = map.get(gv);
            for (GemStringEdge ee : gv.getEdges()) {

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

    public Gist getGist() {
        Gist gist = new Gist();

        // create vertices
        HashMap<Pair,GistVertex> map = new HashMap<Pair,GistVertex>();
        HashMap<GistVertex,GemStringVertex> map2 = new HashMap<GistVertex,GemStringVertex>();

        for (GemStringVertex gv : _vertices) {

            if (gv.getType() == GemStringVertexType.end) {
                GistVertex v = gist.newVertex();
                map.put(new Pair(gv,gv.getEdge(0)),v);
                map2.put(v,gv);
                gist.setNeighbours(v,v,GistEdgeType.plus);
                gist.setNeighbours(v,v,GistEdgeType.minus);
                gist.setNeighbours(v,v,GistEdgeType.times);
            }

            else if (gv.getType() == GemStringVertexType.plus ||
                     gv.getType() == GemStringVertexType.minus ||
                     gv.getType() == GemStringVertexType.times)
            {
                GistEdgeType[] types = null;
                if (gv.getType() == GemStringVertexType.plus) {
                    types = new GistEdgeType[]{
                            GistEdgeType.plus,
                            GistEdgeType.minus,
                            GistEdgeType.times};
                }
                else if (gv.getType() == GemStringVertexType.minus) {
                    types = new GistEdgeType[]{
                            GistEdgeType.minus,
                            GistEdgeType.plus,
                            GistEdgeType.times};
                }
                else if (gv.getType() == GemStringVertexType.times) {
                    types = new GistEdgeType[]{
                            GistEdgeType.times,
                            GistEdgeType.plus,
                            GistEdgeType.minus};
                }

                //
                GistVertex v1 = gist.newVertex();
                map.put(new Pair(gv,gv.getEdge(0)),v1);
                map2.put(v1,gv);

                GistVertex v2 = gist.newVertex();
                map.put(new Pair(gv,gv.getEdge(1)),v2);
                map2.put(v2,gv);

                gist.setNeighbours(v1,v1,types[0]);
                gist.setNeighbours(v2,v2,types[0]);
                gist.setNeighbours(v1,v2,types[1]);
                gist.setNeighbours(v1,v2,types[2]);

            }

            else if (gv.getType() == GemStringVertexType.cross) {
                GistVertex v1 = gist.newVertex();
                map.put(new Pair(gv,gv.getEdge(0)),v1);
                map2.put(v1,gv);
                GistVertex v2 = gist.newVertex();
                map.put(new Pair(gv,gv.getEdge(1)),v2);
                map2.put(v2,gv);
                GistVertex v3 = gist.newVertex();
                map.put(new Pair(gv,gv.getEdge(2)),v3);
                map2.put(v3,gv);
                GistVertex v4 = gist.newVertex();
                map.put(new Pair(gv,gv.getEdge(3)),v4);
                map2.put(v4,gv);

                gist.setNeighbours(v1,v2,GistEdgeType.minus);
                gist.setNeighbours(v2,v3,GistEdgeType.plus);
                gist.setNeighbours(v3,v4,GistEdgeType.minus);
                gist.setNeighbours(v4,v1,GistEdgeType.plus);
                gist.setNeighbours(v1,v3,GistEdgeType.times);
                gist.setNeighbours(v2,v4,GistEdgeType.times);
            }
        }

        // create 0 edges
        HashSet<GemStringEdge> edgesAdded = new HashSet<GemStringEdge>();
        for (GemStringVertex uu : _vertices) {
            int k = 2;
            if (uu.getType() == GemStringVertexType.cross) k = 4;
            else if (uu.getType() == GemStringVertexType.end) k = 1;
            for (int i=0;i<k;i++) {
                GemStringEdge ee = uu.getEdge(i);

                if (edgesAdded.contains(ee)) continue;
                edgesAdded.add(ee);

                GemStringVertex vv = ee.getOpposite(uu);
                GistVertex u = map.get(new Pair(uu,ee));
                GistVertex v = map.get(new Pair(vv,ee));
                gist.setNeighbours(u, v, GistEdgeType.zero);
            }
        }

        return gist;

    }

    public Gem getGem() {
        return this.getGist().getGem();
    }

}

enum GemStringVertexType {
    plus, minus, times, end, cross;
    public static GemStringVertexType fromString(String s) {
        if (s.toUpperCase().equals("+")) return GemStringVertexType.plus;
        else if (s.toUpperCase().equals("-")) return GemStringVertexType.minus;
        else if (s.toUpperCase().equals("X")) return GemStringVertexType.times;
        else if (s.toUpperCase().equals("E")) return GemStringVertexType.end;
        else if (s.toUpperCase().equals("C")) return GemStringVertexType.cross;
        else throw new RuntimeException();
    }
    public String getLabel() {
        if (this == GemStringVertexType.plus) return "+";
        else if (this == GemStringVertexType.minus) return "-";
        else if (this == GemStringVertexType.times) return "x";
        else if (this == GemStringVertexType.end) return "e";
        else if (this == GemStringVertexType.cross) return "c";
        return "";
    }
}

class GemStringVertex {
    private int _label;
    private GemStringVertexType _type;
    private ArrayList<GemStringEdge> _edges;
    public GemStringVertex(int label, GemStringVertexType type) {
        _label = label;
        _type = type;
        _edges = new ArrayList<GemStringEdge>();
    }
    public int getLabel() {
        return _label;
    }
    public GemStringVertexType getType() {
        return _type;
    }
    public int getNumberOfEdges() {
        return _edges.size();
    }
    public void setType(GemStringVertexType type) {
        _type = type;
    }
    public GemStringEdge getEdge(int index) {
        return _edges.get(index);
    }
    public void addEdge(GemStringEdge e) {
        _edges.add(e);
    }
    public ArrayList<GemStringEdge> getEdges() {
        return _edges;
    }
    public void permuteEdges(ArrayList<GemStringEdge> p) {
        if (!_edges.containsAll(p) || _edges.size() != p.size() ||
            (new HashSet(_edges).size()) != (new HashSet(p).size()))
            throw new RuntimeException("OOopppsss");
        _edges.clear();
        _edges.addAll(p);
    }

    public void removeEdgesWithVertex(GemStringVertex v) {
        for (int i=_edges.size()-1;i>=0;i--) {
            if (_edges.get(i).isIncidentTo(v))
                _edges.remove(i);
        }
    }
    public void removeEdge(GemStringEdge e) {
        for (int i=_edges.size()-1;i>=0;i--) {
            if (e == _edges.get(i))
                _edges.remove(i);
        }
    }
}

class GemStringEdge {
    private GemStringVertex _u;
    private GemStringVertex _v;
    public GemStringEdge(GemStringVertex u, GemStringVertex v) {
        _u = u;
        _v = v;
    }
    public Pair getVertices() { return new Pair(_u,_v); }
    public GemStringVertex getOpposite(GemStringVertex w) { return (_u == w ? _v : _u); }
    public boolean isIncidentTo(GemStringVertex w) {
        return (w == _u || w == _v);
    }

}
