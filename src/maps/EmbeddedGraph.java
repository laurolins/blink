package maps;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import linsoft.Pair;
import util.Library;
import util.Vetor;


/*
 * Graph embedded in the plane. Each vertex have a position
 * and each edge is a ployline.
 */
public class EmbeddedGraph extends PropertySet {
	private ArrayList<EmbeddedGraph.Vertex> _vertices = new ArrayList<EmbeddedGraph.Vertex>();
	
	private ArrayList<EmbeddedGraph.Edge> _edges = new ArrayList<EmbeddedGraph.Edge>();
	
	public EmbeddedGraph.Vertex newVertex() {
		EmbeddedGraph.Vertex v = new EmbeddedGraph.Vertex(this,getMaxVertexId()+1);
		_vertices.add(v);
		return v;
	}
	
	public int getMaxVertexId() {
		int maxId = 0;
		for (Vertex v: _vertices)
			maxId = Math.max(maxId,v.getId());
		return maxId;
	}

	public int getMaxEdgeId() {
		int maxId = 0;
		for (Edge e: _edges)
			maxId = Math.max(maxId,e.getId());
		return maxId;
	}

	public EmbeddedGraph.Edge newEdge(EmbeddedGraph.Vertex v1, EmbeddedGraph.Vertex v2) {
		EmbeddedGraph.Edge e = new EmbeddedGraph.Edge(this,getMaxEdgeId()+1,v1,v2);
		v1.addEdge(e);
		if (!e.isLoop())
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
	
	public EmbeddedGraph() {
	}
	
	public ArrayList<EmbeddedGraph.Vertex> getVertices() {
		return _vertices;
	}
	
	public ArrayList<EmbeddedGraph.Edge> getEdges() {
		return _edges;
	}
	
	public EmbeddedGraph copy() {
		// ineficient way, but correct!
		return new EmbeddedGraph(this.getSaveString());
	}
	
	public String getSaveString() {
		StringBuffer buf = new StringBuffer();
		buf.append(_vertices.size());
		buf.append("\t");
		buf.append(_edges.size());
		buf.append("\t");
		for (Vertex v: _vertices) {
			buf.append(v.getId());
			buf.append("\t");
			buf.append(v.getX());
			buf.append("\t");
			buf.append(v.getY());
			buf.append("\t");
			buf.append(v.getProperties().size());
			buf.append("\t");
			buf.append(v.getPropertiesSaveString());
		}
		for (Edge e: _edges) {
			buf.append(e.getId());
			buf.append("\t");
			buf.append(e.getV1().getId());
			buf.append("\t");
			buf.append(e.getV2().getId());
			buf.append("\t");
			buf.append(e.getPathSize()-2);
			buf.append("\t");
			for (int i=0;i<e.getPathSize()-2;i++) {
				buf.append(e.getX(i+1));
				buf.append("\t");
				buf.append(e.getY(i+1));
				buf.append("\t");
			}
			buf.append(e.getProperties().size());
			buf.append("\t");
			buf.append(e.getPropertiesSaveString());
		}
		buf.append(this.getProperties().size());
		buf.append("\t");
		buf.append(this.getPropertiesSaveString());
		return buf.toString();
	}
	
	public EmbeddedGraph(String saveString) {
		super();
		String[] tokens = saveString.split("\t");
		int i=0;
		int nv = Integer.parseInt(tokens[i++]);
		int ne = Integer.parseInt(tokens[i++]);
		HashMap<Integer,Vertex> map = new HashMap<Integer,Vertex>();
		for (int j=0;j<nv;j++) {
			int id = Integer.parseInt(tokens[i++]);
			double x = Double.parseDouble(tokens[i++]);
			double y = Double.parseDouble(tokens[i++]);
			Vertex v = this.newVertex();
			v.setId(id);
			v.setPosition(x, y);
			map.put(id,v);
			int nump = Integer.parseInt(tokens[i++]);
			for (int k=0;k<nump;k++) {
				v.setProperty(tokens[i++], tokens[i++]);
			}
		}
		for (int j=0;j<ne;j++) {
			int id = Integer.parseInt(tokens[i++]);
			Vertex v1 = map.get(Integer.parseInt(tokens[i++]));
			Vertex v2 = map.get(Integer.parseInt(tokens[i++]));
			Edge e = this.newEdge(v1, v2);
			e.setId(id);
			int intermediatePoints = Integer.parseInt(tokens[i++]);
			for (int k=0;k<intermediatePoints;k++) {
				double x = Double.parseDouble(tokens[i++]);
				double y = Double.parseDouble(tokens[i++]);
				e.addIntermediatePoint(x, y);
			}
			int nump = Integer.parseInt(tokens[i++]);
			for (int k=0;k<nump;k++) {
				e.setProperty(tokens[i++], tokens[i++]);
			}
		}
		int nump = Integer.parseInt(tokens[i++]);
		for (int k=0;k<nump;k++) {
			this.setProperty(tokens[i++], tokens[i++]);
		}
	}

	public static class Vertex extends PropertySet {
		private EmbeddedGraph _graph;
		private int _id;
		private ArrayList<Edge> _edges;
		
		private double _x;
		private double _y;
				
		public void setPosition(double x, double y) {
			_x = x;
			_y = y;
		}

		public void setId(int id) {
			_id = id;
		}
		
		public double getX() {
			return _x;
		}
		
		public double getY() {
			return _y;
		}
		
		public Vertex(EmbeddedGraph g, int id) {
			_graph = g;
			_id = id;
			_edges = new ArrayList<Edge>();
		}
		public EmbeddedGraph getGraph() {
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
		private EmbeddedGraph _graph;
		private int _id;
		private Vertex _v1;
		private Vertex _v2;

		private ArrayList<Double> _intermediatePoints = new ArrayList<Double>();
				
		public Edge(EmbeddedGraph g, int id, Vertex v1, Vertex v2) {
			_graph = g;
			_id = id;
			_v1 = v1;
			_v2 = v2;
		}
		
		public void setId(int id) {
			_id = id;
		}
		
		public int getId() {
			return _id;
		}
		
		public void addIntermediatePoint(double x, double y) {
			_intermediatePoints.add(x);
			_intermediatePoints.add(y);
		}
		
		public int getPathSize() {
			return 2 + _intermediatePoints.size()/2;
		}
		
		public double getX(int index) {
			if (index == 0)
				return _v1.getX() + 0.5;
			else if (index == getPathSize()-1)
				return _v2.getX() + 0.5;
			else
				return _intermediatePoints.get(2*(index-1));
		}

		public double getY(int index) {
			if (index == 0)
				return _v1.getY() + 0.5;
			else if (index == getPathSize()-1)
				return _v2.getY() + 0.5;
			else
				return _intermediatePoints.get(2*(index-1) + 1);
		}
		
		public Vertex getV1() {
			return _v1;			
		}

		public Vertex getV2() {
			return _v2;
		}
		
		public EmbeddedGraph getGraph() {
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
	
	public Edge getEdgeById(int id) {
		Edge e = null;
		for (Edge ee: _edges)
			if (ee.getId() == id) {
				e = ee;
				break;
			}
		return e;		
	}
	
	public Map getMap() {
		int[][] mapcr = new int[_vertices.size()][];
		for (int i=0;i<_vertices.size();i++) {
			Vertex v = _vertices.get(i);
			ArrayList<Edge> edgesOriginal = v.getEdges();
			ArrayList<Pair> pairs = new ArrayList<Pair>();
			Vetor ref = new Vetor(1,0);
			for (int j=0;j<edgesOriginal.size();j++) {
				Edge e = edgesOriginal.get(j);
				if (e.isLoop()) {
					Vetor ve1 = new Vetor(e.getX(1)-e.getX(0),e.getY(1)-e.getY(0));
					Vetor ve2 = new Vetor(e.getX(e.getPathSize()-2)-e.getX(e.getPathSize()-1),
								e.getY(e.getPathSize()-2)-e.getY(e.getPathSize()-1));
					ve1.normalize();
					ve2.normalize();
					double theta1 = Math.acos(ref.produtoInterno(ve1));
					if (ve1.getY() < 0)
						theta1 = 2 * Math.PI - theta1;
					double theta2 = Math.acos(ref.produtoInterno(ve2));
					if (ve2.getY() < 0)
						theta2 = 2 * Math.PI - theta2;
					pairs.add(new Pair(e,theta1));
					pairs.add(new Pair(e,theta2));
				}
				else {
					Vetor ve;
					if (v == e.getV1()) {
						ve = new Vetor(e.getX(1)-e.getX(0),e.getY(1)-e.getY(0));
					}
					else {
						ve = new Vetor(e.getX(e.getPathSize()-2)-e.getX(e.getPathSize()-1),
								e.getY(e.getPathSize()-2)-e.getY(e.getPathSize()-1));
					}
					ve.normalize();
					double theta = Math.acos(ref.produtoInterno(ve));
					if (ve.getY() < 0)
						theta = 2 * Math.PI - theta;
					pairs.add(new Pair(e,theta));
				}
			}

			Collections.sort(pairs,new Comparator<Pair>() {
				public int compare(Pair o1, Pair o2) {
					// TODO Auto-generated method stub
					double theta1 = (Double) o1.getSecond();
					double theta2 = (Double) o2.getSecond();
					if (theta1 <= theta2) return -1;
					else return 1;
				}
			});

			int vertexcr[] = new int [pairs.size()];
			for (int k=0;k<pairs.size();k++)
				vertexcr[k] = ((Edge)pairs.get(k).getFirst()).getId();
			mapcr[i] = vertexcr;
		}
		
		for (int i=0;i<mapcr.length;i++) {
			for (int j=0;j<mapcr[i].length;j++)
				System.out.print(mapcr[i][j] + " ");
			System.out.println("");
		}
		
		
		// this relies 
		Map map = new Map(mapcr);
		
		// this part relies that the bigons-vertex-angle are
		// in the same order given by mapcr
		
		ArrayList<Map.Bigon> bigonsva = map.getBigons(Map.EdgeType.vertex , Map.EdgeType.angle);
		for (int i=0;i<bigonsva.size();i++) {
			Map.Bigon b = bigonsva.get(i);
			Vertex v = _vertices.get(i);
			for (int j=0;j<b.size();j+=2) {
				Map.Vertex uu = b.getVertex(j);
				Map.Vertex vv = b.getVertex(j+1);
				// System.out.println(String.format("bigon[%d] = %s   bigon size: %d  mapcr[i].length: %d",i,Library.toString(mapcr[i]),b.size(),mapcr[i].length));
				
				// b.size()
				
				Edge e = this.getEdgeById(mapcr[i][j/2]);
				
				int vid = v.getId();
				int eid = e.getId();
				String vlabel = (v.getProperty("label") != null ? (String)v.getProperty("label") : ""+vid);
				String elabel = (e.getProperty("label") != null ? (String)e.getProperty("label") : ""+eid);
				
				uu.setProperty("vertex_id", ""+vid);
				vv.setProperty("vertex_id", ""+vid);
				
				uu.setProperty("edge_id", ""+eid);
				vv.setProperty("edge_id", ""+eid);

				uu.setProperty("vertex_label", vlabel);
				vv.setProperty("vertex_label", vlabel);
				
				uu.setProperty("edge_label", elabel);
				vv.setProperty("edge_label", elabel);
			}
		}
		
		return map;
	}
	
	public double[] getBounds() {
		Double minx = null;
		Double maxx = null;
		Double miny = null;
		Double maxy = null;
		for (EmbeddedGraph.Vertex v: _vertices) {			
			double x = v.getX();
			double y = v.getY();
			if (minx == null || x < minx)
				minx = x;
			if (miny == null || y < miny)
				miny = y;
			if (maxx == null || x > maxx)
				maxx = x;
			if (maxy == null || y > maxy)
				maxy = y;
		}
		
		
		return new double[] {
				minx == null ? 0 : minx, 
				miny == null ? 0 : miny, 
				maxx == null ? 1 : maxx, 
				maxy == null ? 1 : maxy };
	}
	
}
