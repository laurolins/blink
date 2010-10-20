package maps;

import java.util.ArrayList;

public class Graph extends PropertySet {

	private ArrayList<Graph.Vertex> _vertices = new ArrayList<Graph.Vertex>();
	
	private ArrayList<Graph.Edge> _edges = new ArrayList<Graph.Edge>();
	
	public Graph.Vertex newVertex() {
		Graph.Vertex v = new Graph.Vertex(this,_vertices.size());
		_vertices.add(v);
		return v;
	}

	public Graph.Edge newEdge(Graph.Vertex v1, Graph.Vertex v2) {
		Graph.Edge e = new Graph.Edge(this,_edges.size(),v1,v2);
		v1.addEdge(e);
		v2.addEdge(e);
		_edges.add(e);
		return e;
	}
	
	public void deleteEdge(Edge e) {
		e.getV1().deleteEdge(e);
		if (!e.isLoop())
			e.getV2().deleteEdge(e);
		_edges.remove(e);
	}
	
	public void deleteVertex(Vertex v) {
		for (Edge e: v.getEdges())
			this.deleteEdge(e);
		_vertices.remove(v);
	}
	
	public ArrayList<Graph.Vertex> getVertices() {
		return _vertices;
	}
	
	public ArrayList<Graph.Edge> getEdges() {
		return _edges;
	}

	public static class Vertex extends PropertySet {
		private Graph _graph;
		private int _id;
		private ArrayList<Edge> _edges;
		public Vertex(Graph g, int id) {
			_graph = g;
			_id = id;
			_edges = new ArrayList<Edge>();
		}
		public Graph getGraph() {
			return _graph;
		}
		public int getId() {
			return _id;
		}
		public void addEdge(Edge e) {
			_edges.add(e);
		}
		
		public void deleteEdge(Edge e) {
			_edges.remove(e);
		}
		
		public ArrayList<Edge> getEdges() {
			return _edges;
		}
	}	

	public static class Edge extends PropertySet {
		private Graph _graph;
		private int _id;
		private Vertex _v1;
		private Vertex _v2;
		public Edge(Graph g, int id, Vertex v1, Vertex v2) {
			_graph = g;
			_id = id;
			_v1 = v1;
			_v2 = v2;
		}
		public Vertex getV1() {
			return _v1;			
		}
		public Vertex getV2() {
			return _v2;
		}
		
		public Graph getGraph() {
			return _graph;
		}
		public boolean isLoop() {
			return _v1 == _v2;
		}
		public Vertex getOpposite(Vertex v) {
			if (v == _v1) return _v2;
			else if (v == _v2) return _v1;
			else throw new RuntimeException();
		}
				
	}


}
