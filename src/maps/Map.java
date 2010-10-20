package maps;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashMap;

import util.Library;

public class Map {

	private ArrayList<Map.Vertex> _vertices = new ArrayList<Map.Vertex>();
	
	private ArrayList<Map.Edge> _edges = new ArrayList<Map.Edge>();
	
	private EdgeType _edgeTypesIndex[] = { EdgeType.vertex, EdgeType.face, EdgeType.diagonal, EdgeType.angle };
	
	private Map.Vertex newVertex() {
		Map.Vertex v = new Map.Vertex(this,_vertices.size());
		_vertices.add(v);
		return v;
	}
	
	public int getIndexOfEdgeType(EdgeType t) {
		if (t == _edgeTypesIndex[0]) return 0;
		else if (t == _edgeTypesIndex[1]) return 1;
		else if (t == _edgeTypesIndex[2]) return 2;
		else if (t == _edgeTypesIndex[3]) return 3;
		else throw new RuntimeException();
	}
	
	private Map.Edge newEdge(Map.Vertex v1, Map.Vertex v2, int edgeTypeIndex) {
		Map.Edge e = new Map.Edge(this,_edges.size(),v1,v2,edgeTypeIndex);
		v1.setEdge(e, edgeTypeIndex);
		v2.setEdge(e, edgeTypeIndex);
		_edges.add(e);
		return e;
	}
	
	public ArrayList<Map.Edge> getEdges() {
		return _edges;
	}

	public ArrayList<Map.Vertex> getVertices() {
		return _vertices;
	}
	
	public Map(int[][] mapcr) {
		new MapCyclicRepresentationConstructor(this,mapcr);
	}
	
	private static class MapCyclicRepresentationConstructor {
		
		private Map _map;
		
		public MapCyclicRepresentationConstructor(Map map, int[][] mapcr) {
			_map = map;
			
	        // connect edges
	        HashMap<Integer,E> id2e = new HashMap<Integer,E>();
	        for (int i=0;i<mapcr.length;i++) {

	            int n = mapcr[i].length;
	            if (n == 0)
	                continue;

	            // v1j0 da iteracao zero
	            Map.Vertex v1j0 = null;
	            
	            // v2a da iteracao anterior
	            Map.Vertex v2a = null;
	            E eFirst = null;

	            
				// criar os vértices do mapa ao redor do vértice do grafo
	            for (int j = 0; j < n; j++) {
	                int lbl1 = mapcr[i][j];
	            	
	                E e = id2e.get(lbl1);
	                if (e == null) {
	                    e = new E(lbl1);
	                    id2e.put(lbl1, e);
	                }
	                
	                Map.Vertex v1 = e.nextFree();
	                Map.Vertex v2 = e.nextFree();

	                v1.setProperty("vertex_id", i);
	                v2.setProperty("vertex_id", i);
	                
	                // angle edge
	                _map.newEdge(v1,v2, Map.EdgeType.vertex.getDefaultIndex());
	                // orientation
	                e.setOrientation(v1, v2);
	                	                
	                if (j == 0)
	                	v1j0 = v1;	                	               

	                //
	                if (v2a != null) 
		                _map.newEdge(v2a, v1, Map.EdgeType.angle.getDefaultIndex());
	                	
	                if (j == n-1)
		                _map.newEdge(v2, v1j0, Map.EdgeType.angle.getDefaultIndex());
	                
	                v2a = v2;
	            }
	        }
				

	        // check reverse connections and define face edges!
	        for (E e: id2e.values()) {
	            e.defineSquare();
	        }
	        
	        // set the diagonals
	        for (Map.Vertex v : _map.getVertices()) {
	        	Map.Vertex w = v.getNeighbor(EdgeType.vertex).getNeighbor(EdgeType.face);
	        	if (v.getId() < w.getId()) {
	        		_map.newEdge(v, w, EdgeType.diagonal.getDefaultIndex());
	        	}
	        }
	        
	        // label vertices such that each rectangle is 4k, 4k+1, 4k+2, 4k+3
	        for (E e: id2e.values())
	        	e.labelVertices();
	        
//	        // go to label
//	        this.goToCodeLabelPreservingSpaceOrientation();
//	        // this.goToCodeLabelAndDontCareAboutSpaceOrientation();
//
//	        // set colors
//	        for (E e: map.values()) {
//	            int lbl = e.getVertex(0).getLabel();
//	            int edge = (lbl % 4 == 0 ? lbl/4 : lbl/4+1);
//	            this.setColor(edge,e.getColor());
//	        }
//
//	        // go to label
//	        this.goToCodeLabelPreservingSpaceOrientation();

		}

		/**
		 * Auxiliar class representing the edges of the graph for 
		 * which we want to create the map.
		 */
		public class E {
			int _label;
		    Map.Vertex[] _vs;
		    int[] _orientation = {0,0,0,0};
		    int _freeIndex = 0;
		
		    public E(int label) {
		    	_label = label;
		        _vs = new Map.Vertex[] {null,null,null,null};
//		        int k = 4*(_label-1)+1;
//		        _vs[0].setLabel(1);
//		        _vs[1].setLabel(2);
//		        _vs[2].setLabel(3);
//		        _vs[3].setLabel(4);
		    }

		    public void labelVertices() {
		    	_vs[0].setId(4*_label);
		    	_vs[0].getNeighbor(EdgeType.vertex).setId(4*_label+1);
		    	_vs[0].getNeighbor(EdgeType.diagonal).setId(4*_label+2);
		    	_vs[0].getNeighbor(EdgeType.face).setId(4*_label+3);
		    	
                //
                _vs[0].setProperty("edge_id", _label);
		    	_vs[1].setProperty("edge_id", _label);
		    	_vs[0].setProperty("edge_id", _label);
		    	_vs[0].setProperty("edge_id", _label);
		    }
		    
		    public int index(Map.Vertex v) {
		        if (v == _vs[0]) return 0;
		        else if (v == _vs[1]) return 1;
		        else if (v == _vs[2]) return 2;
		        else if (v == _vs[3]) return 3;
		        else throw new RuntimeException();
		    }
		
		    public void setOrientation(Map.Vertex v1, Map.Vertex v2) {
		        int index1 = index(v1);
		        int index2 = index(v2);
		        _orientation[index1] = 1;
		        _orientation[index2] = -1;
		        // System.out.println("Orientation "+this.getLabel()+" -> "+_orientation[0]+" "+_orientation[1]+" "+_orientation[2]+" "+_orientation[3]);
			}
			
			public Map.Vertex nextFree() {
				_vs[_freeIndex] = _map.newVertex();
			    Map.Vertex v = _vs[_freeIndex];
			    _freeIndex++;
			    return v;
			}
			
			public Map.Vertex getVertex(int index) {
			    return _vs[index];
			}
			
			public Map.Vertex next(Map.Vertex v) {
			    if (v == _vs[0]) return _vs[1];
			    else if (v == _vs[1]) return _vs[2];
			    else if (v == _vs[2]) return _vs[3];
			    else if (v == _vs[3]) return _vs[0];
			    else throw new RuntimeException();
			}
			
			public Map.Vertex previous(Map.Vertex v) {
			    if (v == _vs[0]) return _vs[3];
			    else if (v == _vs[1]) return _vs[0];
			    else if (v == _vs[2]) return _vs[1];
			    else if (v == _vs[3]) return _vs[2];
			    else throw new RuntimeException();
			}
			
			public void defineSquare() {
			    int i = 0;
			    Map.Vertex a = _vs[0]; Map.Vertex b = _vs[1];
			    Map.Vertex c = _vs[2]; Map.Vertex d = _vs[3];
			
			    if (a.getNeighbor(Map.EdgeType.vertex) == b) {  // a is connected to b by a face edge
					if (_orientation[0] == _orientation[2]) {
						_map.newEdge(b, c, Map.EdgeType.face.getDefaultIndex());
						_map.newEdge(a, d, Map.EdgeType.face.getDefaultIndex());
					}
					else {
						_map.newEdge(b, d, Map.EdgeType.face.getDefaultIndex());
						_map.newEdge(a, c, Map.EdgeType.face.getDefaultIndex());
				    }
				}
				else { // a is connected to d by a face edge
					if (_orientation[3] == _orientation[1]) {
						_map.newEdge(a, b, Map.EdgeType.face.getDefaultIndex());
						_map.newEdge(c, d, Map.EdgeType.face.getDefaultIndex());
					}
					else {
						_map.newEdge(a, d, Map.EdgeType.face.getDefaultIndex());
						_map.newEdge(c, b, Map.EdgeType.face.getDefaultIndex());
			        }
				}
		    }
			
			public int getLabel() {
			    return _label;
			}	
		}		
	}
	
	private EdgeType getEdgeType(int index) {
		return _edgeTypesIndex[index];
	}
	
	private int getEdgeTypeIndex(EdgeType type) {
		if (_edgeTypesIndex[0] == type) return 0;
		else if (_edgeTypesIndex[1] == type) return 1;
		else if (_edgeTypesIndex[2] == type) return 2;
		else return 3;
	}
	
	public static enum EdgeType {
		vertex(0), face(1), diagonal(2), angle(3);
		private int _defaultIndex;
		private EdgeType(int defaultIndex) {
			_defaultIndex = defaultIndex;
		}
		public int getDefaultIndex() {
			return _defaultIndex;
		}
		public Color getColor() {
			if (_defaultIndex == 0) return Color.green;
			else if (_defaultIndex == 1) return Color.cyan;
			else if (_defaultIndex == 2) return Color.red;
			else if (_defaultIndex == 3) return Color.blue;
			else throw new RuntimeException();
		}
		public static EdgeType getEdgeTypeFromDefaultIndex(int index) {
			if (index == 0) return EdgeType.vertex;
			else if (index == 1) return EdgeType.face;
			else if (index == 2) return EdgeType.diagonal;
			else if (index == 3) return EdgeType.angle;
			else throw new RuntimeException();
		}
	}
	
	public static class Vertex extends PropertySet {
		private Map _map;
		private int _id;
		private Edge _edges[] = {null, null, null, null};

		public Vertex(Map map, int id) {
			_map = map;
			_id = id;
		}
		
		public void setId(int id) {
			_id = id;
		}
		
		public void setEdge(Map.Edge e, int index) {
			_edges[index] = e;
		}

		public Vertex getNeighbor(int index) {
			return _edges[index].getOpposite(this);
		}
		
		public Vertex getNeighbor(EdgeType type) {
			return _edges[_map.getEdgeTypeIndex(type)].getOpposite(this);
		}		

		public Edge getEdge(EdgeType type) {
			return _edges[_map.getEdgeTypeIndex(type)];
		}		

		public int getId() {
			return _id;
		}
	}
	
	public Map() {
	}
	
	public Map copy() {
		Map copy = new Map();
		HashMap<Vertex, Vertex> v2vv = new HashMap<Vertex,Vertex>();
		for (Vertex v: _vertices) {
			Vertex vv = copy.newVertex();
			vv.setId(v.getId());
			v2vv.put(v,vv);
		}
		for (Edge e: _edges) {
			Edge ee = copy.newEdge(v2vv.get(e.getV1()), v2vv.get(e.getV2()), e._edgeTypeIndex);
			ee.setId(e.getId());
		}
		for (int i=0;i<4;i++)
			copy._edgeTypesIndex[i] = _edgeTypesIndex[i]; 
		return copy;
	}
	
	public Map dual() {
		Map result = this.copy();
		// 2 1 3
		EdgeType t1 = _edgeTypesIndex[0]; 
		EdgeType t2 = _edgeTypesIndex[1];
		EdgeType t3 = _edgeTypesIndex[2]; 
		result._edgeTypesIndex[0] = t2;
		result._edgeTypesIndex[1] = t1;
		result._edgeTypesIndex[2] = t3;
		return result;
	}
	
	public Map antidual() {
		Map result = this.copy();
		// 2 3 1
		EdgeType t1 = _edgeTypesIndex[0]; 
		EdgeType t2 = _edgeTypesIndex[1];
		EdgeType t3 = _edgeTypesIndex[2]; 
		result._edgeTypesIndex[0] = t2;
		result._edgeTypesIndex[1] = t3;
		result._edgeTypesIndex[2] = t1;
		return result;
	}

	public Map phial() {
		Map result = this.copy();
		// 2 3 1
		EdgeType t1 = _edgeTypesIndex[0]; 
		EdgeType t2 = _edgeTypesIndex[1];
		EdgeType t3 = _edgeTypesIndex[2]; 
		result._edgeTypesIndex[0] = t3;
		result._edgeTypesIndex[1] = t2;
		result._edgeTypesIndex[2] = t1;
		return result;
	}
	
	public Map antiphial() {
		Map result = this.copy();
		// 2 3 1
		EdgeType t1 = _edgeTypesIndex[0]; 
		EdgeType t2 = _edgeTypesIndex[1];
		EdgeType t3 = _edgeTypesIndex[2]; 
		result._edgeTypesIndex[0] = t3;
		result._edgeTypesIndex[1] = t1;
		result._edgeTypesIndex[2] = t2;
		return result;
	}
	
	public Map antimap() {
		Map result = this.copy();
		// 2 3 1
		EdgeType t1 = _edgeTypesIndex[0]; 
		EdgeType t2 = _edgeTypesIndex[1];
		EdgeType t3 = _edgeTypesIndex[2]; 
		result._edgeTypesIndex[0] = t1;
		result._edgeTypesIndex[1] = t3;
		result._edgeTypesIndex[2] = t2;
		return result;
	}

	public static class Edge extends PropertySet {
		private Map _map;
		private int _id;
		private Vertex _v1;
		private Vertex _v2;
		private int _edgeTypeIndex;
		
		public Edge(Map m, int id, Vertex v1, Vertex v2, int edgeTypeIndex) {
			_map = m;
			_id = id;
			_v1 = v1;
			_v2 = v2;
			_edgeTypeIndex = edgeTypeIndex;
		}
		
		public Vertex getV1() {
			return _v1;
		}
		
		public Vertex getV2() {
			return _v2;
		}
		
		public void setId(int id) {
			_id = id;
		}
		
		public EdgeType getEdgeType() {
			return _map.getEdgeType(_edgeTypeIndex);
		}
		
		public Vertex getOpposite(Vertex v) {
			if (v == _v1) return _v2;
			else if (v == _v2) return _v1;
			else throw new RuntimeException();
		}

		public int getId() {
			return _id;
		}
		
		public int getEdgeTypeIndex() {
			return _edgeTypeIndex;
		}
		
	}
	
	public String getDescription() {
		StringBuffer sb = new StringBuffer();
		sb.append(String.format("%4s: %4s %4s %4s %4s\n", "#","vert","face","diag","angl"));
		for (Map.Vertex v: _vertices) {
			sb.append(String.format("%4d: %4d %4d %4d %4d\n", 
					v.getId(), 
					v.getNeighbor(EdgeType.vertex).getId(), 
					v.getNeighbor(EdgeType.face).getId(), 
					v.getNeighbor(EdgeType.diagonal).getId(),
					v.getNeighbor(EdgeType.angle).getId()));
		}
		return sb.toString();
	}
	
	public static void main(String[] args) {
		int[][] mapcr = { {1,3}, {1,2}, {2,3} };
		Map m = new Map(mapcr);
		System.out.println(m.getDescription());
	}
	
	public ArrayList<Bigon> getBigons(int edgeTypeIndex1, int edgeTypeIndex2) {
		ArrayList<Bigon> result = new ArrayList<Bigon>();
		String tag = Bigon.tagName(edgeTypeIndex1, edgeTypeIndex2);
		for (Vertex v: _vertices)
			v.setProperty(tag, false);
		for (Vertex v: _vertices) {
			if ((Boolean) v.getProperty(tag) != true) {
				result.add(new Bigon(this,edgeTypeIndex1,edgeTypeIndex2,v));
			}
		}
		return result;
	}
	
	public ArrayList<Bigon> getBigons(EdgeType t1, EdgeType t2) {
		return getBigons(this.getEdgeTypeIndex(t1), this.getEdgeTypeIndex(t2));
	}

	public int getNumVertices() {
		return _vertices.size();
	}
	
	public int getNumEdges() {
		return _edges.size();
	}

	public String getReport() {
		StringBuffer sb = new StringBuffer();
		sb.append(String.format("Num Vertices:           %5d\n", getNumVertices()));
		sb.append(String.format("Num Edges:              %5d\n", getNumEdges()));
		sb.append(String.format("Num bigons faces:       %5d\n", getBigons(EdgeType.face,EdgeType.angle).size()));
		sb.append(String.format("Num bigons vertices:    %5d\n", getBigons(EdgeType.vertex,EdgeType.angle).size()));
		sb.append(String.format("Num bigons zigzags:     %5d\n", getBigons(EdgeType.diagonal,EdgeType.angle).size()));
		return sb.toString();
	}
	
	public static class Bigon {
		private Map _map;
		private int _edgeTypeIndexes[] = {0,0};
		private ArrayList<Map.Vertex> _vertices = new ArrayList<Map.Vertex>();
		
		public static String tagName(int edgeTypeIndex1, int edgeTypeIndex2) {
			return "b" + (1 << edgeTypeIndex1 + 1 << edgeTypeIndex2);
		}
		
		public ArrayList<Map.Vertex> getVertices() {
			return _vertices;
		}
		
		public Map.Vertex getVertex(int index) {
			return _vertices.get(index);
		}
		
		public EdgeType getEdgeType1() {
			return _map.getEdgeType(_edgeTypeIndexes[0]);
		}
		
		public EdgeType getEdgeType2() {
			return _map.getEdgeType(_edgeTypeIndexes[1]);
		}

		public Bigon(Map map, int edgeTypeIndex1, int edgeTypeIndex2, Map.Vertex root) {
			_map = map;
			if (edgeTypeIndex1 < edgeTypeIndex2) {
				_edgeTypeIndexes[0] = edgeTypeIndex1;
				_edgeTypeIndexes[1] = edgeTypeIndex2;
			}
			else if (edgeTypeIndex1 > edgeTypeIndex2) {
				_edgeTypeIndexes[0] = edgeTypeIndex2;
				_edgeTypeIndexes[1] = edgeTypeIndex1;
			}
			else throw new RuntimeException();
			// set repersentation for tags.
			String tagName = Bigon.tagName(edgeTypeIndex1, edgeTypeIndex2);
			
			Map.Vertex v = root;
			v.setProperty(tagName, true);
			int parity = 0; 
			while (true) {
				_vertices.add(v);
				v = v.getNeighbor(_edgeTypeIndexes[parity]);				
				v.setProperty(tagName,true);
				parity = (parity + 1) % 2;
				if (v == root)
					break;
			}			
		}
		
		public int size() {
			return _vertices.size();
		}
		
		public EmbeddedGraph asGraph() {
			
			Map.Bigon b = (Map.Bigon) this;
			int n = b.size();
			EmbeddedGraph g = new EmbeddedGraph();
			
			double r = 0.5 * n;
			double thetaInc = (2 * Math.PI)/ n;
			double theta0 = Math.PI / 2;

			HashMap<Map.Vertex,EmbeddedGraph.Vertex> mv2gv = new HashMap<Map.Vertex,EmbeddedGraph.Vertex>();

			ArrayList<Map.Vertex> vertices = b.getVertices(); 
			for (int i=0;i<n;i++) {
				Map.Vertex v = vertices.get(i);
				EmbeddedGraph.Vertex vv = g.newVertex();
				vv.setId(v.getId());
				// vv.setProperty("o", );
				double theta = theta0 + i * thetaInc;
				double x = r * Math.cos(theta);
				double y = r * Math.sin(theta);
				vv.setPosition(x, y);
				
				String vlabel = (String)v.getProperty("vertex_label");
				String elabel = (String)v.getProperty("edge_label");

				// map origin info
				vv.setProperty("label_vertex_original", vlabel);
				vv.setProperty("x_vertex_original", "0.7");
				vv.setProperty("y_vertex_original", "-0.5");
				vv.setProperty("color_vertex_original", "0,200,0");
				vv.setProperty("label_edge_original", elabel);
				vv.setProperty("x_edge_original", "0.7");
				vv.setProperty("y_edge_original", "0.5");
				vv.setProperty("color_edge_original", "200,0,200");
				
				mv2gv.put(v, vv);
				
			}
			
			for (int i=0;i<n;i++) {
				Map.Vertex v1 = vertices.get(i);
				Map.Vertex v2 = vertices.get((i + 1) % n);
				EmbeddedGraph.Edge e = g.newEdge(mv2gv.get(v1), mv2gv.get(v2));
				e.setProperty("type", (i%2) == 0 ? b.getEdgeType1().toString() : b.getEdgeType2().toString());

				Color color = ((i % 2) == 0 ?b.getEdgeType1().getColor() : b.getEdgeType2().getColor());
				String fgcolor = Library.encodeColorInString(color); 
				
				// map origin info
				e.setProperty("fgcolor", fgcolor);

			}
			
			return g;
			
		}
		
	}
}
