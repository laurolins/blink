package maps;

import java.util.ArrayList;
import java.util.HashMap;



public class Uncrossing {

	// map of an eulerian graph
	private Map _map;
	
	// underlying graph vertex bigons
	private ArrayList<Map.Bigon> _vbigons;
	
	// underlying graph edge bigons
	private ArrayList<Map.Bigon> _ebigons;

	//
	private ArrayList<Vertex> _vertices;
	
	// current resolution information in...
	//private HashMap<Pair,>
		
	// Find vertex resolution for each vertex such as to minimize 
	// the number of v-crossings and preserve the essence.
	public Uncrossing(Map map) {

		// @todo preserve the same orientation on the bigons.
		// this may need a odd-even good rule for the vertices on the maps.
		
		// vertices
		_vbigons = map.getBigons(Map.EdgeType.vertex, Map.EdgeType.angle);
		for (Map.Bigon v: _vbigons)
			for (Map.Vertex vv: v.getVertices()) {
				vv.setProperty("v", v);
			}
		
		_ebigons = map.getBigons(Map.EdgeType.vertex, Map.EdgeType.face);
		for (Map.Bigon e: _ebigons)
			for (Map.Vertex vv: e.getVertices()) {
				vv.setProperty("e", e);
			}
		
		// create vertex structure (it already initializes 
		// with the pairing with the most crossings).
		_vertices = new ArrayList<Vertex>();
		for (Map.Bigon vbigon: _vbigons) {
			_vertices.add(new Vertex(vbigon));
		}
		
		// try to reduce the number of crossings preserving
		// the essence
		for (Vertex v: _vertices) {
			
			for (int i=0;i<v.getDegree();i++) {
				Map.Edge angle = v.getAngle(i);

				//
				Vertex currentVertex1 = v;
				Vertex currentVertex2 = v;
				
				// index of the next edge1 in current vertex 1 (analogous for 2)
				int nextEdgeIndex1 = i;
				int nextEdgeIndex2 = (i+1) % v.getDegree();
				
				// create the lambda_e1(2) path = path1(2)
				ArrayList path1 = new ArrayList();
				ArrayList path2 = new ArrayList();

				//
				while (true) {

					path1.add(currentVertex1);
					path2.add(currentVertex2);
					
					Map.Bigon nextEdge1 = currentVertex1.getEdge(nextEdgeIndex1);
					Map.Bigon nextEdge2 = currentVertex2.getEdge(nextEdgeIndex2);
					
					// case
					
					

					if (nextEdge1 == nextEdge2) {
				    }

				    if (path2.contains(nextEdge1)) {
					}
					
					if (path1.contains(nextEdge2)) {
					}
					
					else {
						/*
						path1.add(nextEdge1);
						path2.add(nextEdge2);
						
						Vertex nextVertex1 = currentVertex1.getOppositeVertexByEdgeIndex(nextEdgeIndex1);
						Vertex nextVertex2 = currentVertex2.getOppositeVertexByEdgeIndex(nextEdgeIndex2);

						
						int arrivingNextVertexEdgeIndex1 = currentVertex1.getOppositeVertexEdgeIndexByEdgeIndex(nextEdgeIndex1);
						int arrivingNextVertexEdgeIndex2 = currentVertex1.getOppositeVertexEdgeIndexByEdgeIndex(nextEdgeIndex2);
												
						int exitingNextVertexEdgeIndex1 = nextVertex1.getLambda(arrivingNextVertexEdgeIndex1);
						int exitingNextVertexEdgeIndex2 = nextVertex1.getLambda(arrivingNextVertexEdgeIndex2);
						
						currentVertex1 = nextVertex1;
						currentVertex2 = nextVertex2;
						
						nextEdgeIndex1 = exitingNextVertexEdgeIndex1;
						nextEdgeIndex2 = exitingNextVertexEdgeIndex2;
						*/
						// needs a vertex to tie it up
					}
				}
			}
		}
	}
	
	public class Vertex {
		// edges incident to the vertex in cyclic order
		private Map.Bigon _vbigon;
		private ArrayList<Map.Bigon> _edges =  new ArrayList<Map.Bigon>();
		private ArrayList<Map.Edge> _angles =  new ArrayList<Map.Edge>();
		private ArrayList<Map.Vertex> _vertices = new ArrayList<Map.Vertex>();
		private ArrayList<Map.Vertex> _vrepresentants =  new ArrayList<Map.Vertex>();
		private HashMap<Map.Bigon,Integer> _e2index = new HashMap<Map.Bigon,Integer>();
		private int _lambda[];		
		public Vertex(Map.Bigon vbigon) {
			_vbigon = vbigon;

			// add all vertices
			_vertices.addAll(vbigon.getVertices());

			// skip vertices on the same edge "rectangle"
			for (int i=0;i<_vertices.size();i+=2) {
				Map.Bigon ebigon = (Map.Bigon) _vertices.get(i).getProperty("e");
				_e2index.put(ebigon, _edges.size());
				_edges.add(ebigon);
				_angles.add((Map.Edge) _vertices.get(i+1).getEdge(Map.EdgeType.angle));

				//
				_vrepresentants.add(_vertices.get(i));
				
				// set the uvertex of the vertex
				_vertices.get(i).setProperty("uvertex", this);
				_vertices.get(i+1).setProperty("uvertex", this);
				_vertices.get(i).setProperty("edgeindex", i/2);
				_vertices.get(i+1).setProperty("edgeindex", i/2);
			}
			
			// start with the opposite pairing schema
			int k = _edges.size();
			_lambda = new int[k];
			for (int i=0;i<k;i++)
				_lambda[i] = (i + k/2) % k;
		}
		
		public int getDegree() {
			return _edges.size();
		}
		
		public Map.Edge getAngle(int index) {
			return _angles.get(index);
		}
		
		public Map.Vertex getMapVertex1OfAngle(int index) {
			return _vertices.get((2*index+1) % (2*this.getDegree()));
		}
		
		public Map.Vertex getMapVertex2OfAngle(int index) {
			return _vertices.get(2*(index+1) % (2*this.getDegree()));
		}
		
		public Map.Bigon getFirstEdgeOfAngle(int index) {
			return _edges.get(index);
		}

		public Map.Bigon getSecongEdgeOfAngle(int index) {
			return _edges.get((index + 1) % this.getDegree());
		}
		
		public Vertex getOppositeVertexByEdgeIndex(int index) {
			return (Vertex)_vrepresentants.get(index).getNeighbor(Map.EdgeType.face).getProperty("uvertex");
		}

		public Vertex getOppositeVertexEdgeIndexByEdgeIndex(int index) {
			return (Vertex)_vrepresentants.get(index).getNeighbor(Map.EdgeType.face).getProperty("edgeindex");
		}

		public Map.Vertex getMapVertexOnOppositeEdgeByEdgeIndex(int index) {
			return (Map.Vertex)_vrepresentants.get(index).getNeighbor(Map.EdgeType.face);
		}
	
		public int getLambda(int index) {
			return _lambda[index];
		}
		
		public Map.Bigon getLambdaEdge(int index) {
			return _edges.get(getLambda(index));
		}

		public Map.Bigon getEdge(int index) {
			return _edges.get(index);
		}
		
	}
	
}
