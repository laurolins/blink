package maps.grapheditor;

import java.awt.Graphics;
import java.util.ArrayList;

import maps.EmbeddedGraph;
import maps.EmbeddedGraph.Edge;
import maps.EmbeddedGraph.Vertex;
import drawing.Rectangle;
import drawing.SimpleGroup;
import drawing.View;

public class EditorGraph {
	
	// Graph
	private EmbeddedGraph _graph;

	// list of editor nodes (sync with cambio nodes)
	private ArrayList<EditorVertex> _editorVertexs = new ArrayList<EditorVertex>();

	// list of editor arcos (sync with cambio arcos)
	private ArrayList<EditorEdge> _editorEdges = new ArrayList<EditorEdge>();

	// create an empty cambio
	public EditorGraph(EmbeddedGraph cambio) {
		_graph = cambio;
		for (Vertex n: _graph.getVertices())
			this.addVertex(n);
		for (Edge c: _graph.getEdges())
			this.addEdge(c);		
	}

	public EditorVertex addVertex(Vertex node) {
		EditorVertex en = new EditorVertex(node);
		_editorVertexs.add(en);
		return en;
	}
	
	public EmbeddedGraph getEmbeddedGraph() {
		return _graph;
	}
	
	public EditorVertex addVertex(double x, double y) {
		Vertex a = _graph.newVertex();
		EditorVertex ev = this.addVertex(a);
		ev.setPosition(x, y);
		return ev;
	}	
	
	public EditorEdge addEdge(EditorVertex a, EditorVertex b) {
		Edge c = _graph.newEdge(a.getVertex(), b.getVertex());
		return this.addEdge(c,a,b);
	}
	
	public EditorEdge addEdge(Edge arco) {
		EditorVertex a = getEditorVertexFromVertex(arco.getV1());
		EditorVertex b = getEditorVertexFromVertex(arco.getV2());
		return addEdge(arco, a, b);
	}
	
	public void deleteEdge(EditorEdge ea) {
		this.getEmbeddedGraph().deleteEdge(ea.getEdge());
		this.getEditorEdges().remove(ea);
	}

	public void deleteVertex(EditorVertex ea) {
		if (ea.getVertex().getEdges().size() > 0)
			throw new RuntimeException("OOoooopsss");
		this.getEmbeddedGraph().deleteVertex(ea.getVertex());
	}
	
	public EditorEdge addEdge(Edge arco, EditorVertex a, EditorVertex b) {
		EditorEdge ec = new EditorEdge(arco, a, b);
		_editorEdges.add(ec);
		return ec;
	}	
	
	public EditorVertex getEditorVertexFromVertex(Vertex node) {
		for (EditorVertex en: this.getEditorVertexs())
			if (en.getVertex() == node)
				return en;
		throw new RuntimeException();
	}	
	
	public ArrayList<EditorVertex> getEditorVertexs() {
		return _editorVertexs;
	}

	public ArrayList<EditorEdge> getEditorEdges() {
		return _editorEdges;
	}
	
	public void draw(Graphics g, Rectangle worldRectangle, Rectangle screenRectangle) {
		SimpleGroup group = new SimpleGroup(null);
		worldRectangle.setVisible(false);
		group.addChild(worldRectangle);
		for (EditorEdge ec: this.getEditorEdges())
			group.addChild(ec);
		for (EditorVertex en: this.getEditorVertexs())
			group.addChild(en);
		
		screenRectangle.setVisible(false);
		View view = new View(worldRectangle, screenRectangle);
		
		view.paint(g);		
	}
	
}
