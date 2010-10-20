package linsoft.graph;

/**
 * Simple Graphs only! (Weighted or Not), (Undirected)
 * No loops, no multiple edges!
 */
public class Graph
{
	private int _n; // number of vertices
	private int _time; // variable needed to register the time in DFS algorithm
	private Vertex[] _vertices; // linked list representation
	private Edge[][] _edges; // adjacent matrix representation
	private EdgeList _edgeList; // list of edges...
/**
* Creates a graph with n vertices with labels <1, 2, ..., n>
*/
public Graph(int n)
{
	// number of vertices
	_n = n;

	// initialize linked list representation...
	_vertices = new Vertex[n];
	int i, j;
	for (i = 0; i < n; i++)
		_vertices[i] = new Vertex(i + 1, null, false);

	// initialize adjacent matrix representation
	_edges = new Edge[n][n];
	for (i = 0; i < n; i++)
		for (j = 0; j < n; j++)
			_edges[i][j] = null;

		// initialize edge list
	_edgeList = new EdgeList();
}
/**
* Adds an undirected edge identified by "label" from vertex with label "labelV1"
* to a vertex with label "labelV2"
*/
public void addEdge(int label, int labelV1, int labelV2)
{
	// register the new edge (a link) in the adjacent list
	Vertex v1, v2;
	v1 = _vertices[labelV1 - 1];
	v2 = _vertices[labelV2 - 1];
	v1.addAdjacentVertex(v2);
	v2.addAdjacentVertex(v1);

	// register the new edge (a link) in the adjacent matrix
	Edge e = new Edge(label, labelV1, labelV2, 1, null);
	_edges[labelV1 - 1][labelV2 - 1] = e;
	_edges[labelV2 - 1][labelV1 - 1] = e;

	// register the new edge in the edge list
	_edgeList.addEdge(e);
}
/**
* Adds an undirected edge identified by "label" from vertex with label "labelV1"
* to a vertex with label "labelV2" with weight "weigth"
*/
public void addEdge(int label, int labelV1, int labelV2, int weigth)
{
	// register the new edge (a link) in the adjacent list
	Vertex v1, v2;
	v1 = _vertices[labelV1 - 1];
	v2 = _vertices[labelV2 - 1];
	v1.addAdjacentVertex(v2);
	v2.addAdjacentVertex(v1);

	// register the new edge (a link) in the adjacent matrix
	Edge e = new Edge(label, labelV1, labelV2, weigth, null);
	_edges[labelV1 - 1][labelV2 - 1] = e;
	_edges[labelV2 - 1][labelV1 - 1] = e;

	// register the new edge in the edge list
	_edgeList.addEdge(e);
}
/**
* Does a breadth first search printing the vertices labels...
*/
public void breadthFirstSearch(int label)
{
	this.unmarkAllVertices();
	VertexQueue queue = new VertexQueue(_n);
	Vertex v = _vertices[label - 1];

	// initialize
	queue.enqueue(v);
	v.mark();

	// iterate
	while (!queue.isEmpty())
	{
		v = queue.getFirst();
		queue.dequeue();
		System.out.println("" + v.getLabel());
		VertexList vl = v.getAdjacentVertices();
		while (vl != null)
		{
			Vertex w = vl.getVertex();
			if (!w.isMarked())
			{
				queue.enqueue(w);
				w.mark();
			}
			vl = vl.getNext();
		}
	}
}
/**
* Does a depth first search printing the vertices labels...
*/
public void depthFirstSearch(int label)
{
	this.unmarkAllVertices();
	this.depthFirstSearch(_vertices[label - 1]);
}
private void depthFirstSearch(Vertex v)
{
	Vertex w;
	System.out.println("" + v.getLabel());
	v.mark();
	VertexList vl = v.getAdjacentVertices();
	// iterate in all adjacent vertices
	while (vl != null)
	{
		w = vl.getVertex();
		if (!w.isMarked())
			depthFirstSearch(w);
		vl = vl.getNext();
	}
}
/**
 * Executa um breadth first search nas arestas de uma árvore gerada por um DFS.
 * Este algoritmo é baseado nos seguintes teoremas seja "G" um grafo e "G'"
 * um DFS subgrafo (uma árvore) de "G":
 * 1.
 *    se "v" um vértice não raiz de "G'" então:
 *    v é vértice de corte (se e somente se) existe "v'" descendente de "v" (em G'), e "u" descendente (em G')
 *    ou igual a "u" tal que existe uma aresta de retorno (u,w) em "G" e "v" seja descendente de "w" (em G').
 * 2.
 *    se "v" é raiz de "G'" então "v" é cut-vertex se e somente se seu grau for maior que 1 em "G'" (ou seja tem
 *    pelo menos dois descendentes diretos (dois filhos).
 */
public boolean[] findCutVertices()
{
	int rootLabel = 1;
	boolean cutVertices[] = new boolean[_n];

	// tag a DFS tree with root at rootLabel
	this.tagDFSTree(rootLabel);

	// mount low structure
	int low[] = this.mountLowVector(rootLabel);

	// initialize for a breadth firs search in the DFS tree (only marked edges)
	this.unmarkAllVertices();
	VertexQueue queue = new VertexQueue(_n);
	Vertex v = _vertices[rootLabel - 1];

	// initialize
	queue.enqueue(v);
	v.mark();

	// aplicar teorema 1
	while (!queue.isEmpty())
	{
		v = queue.getFirst();
		queue.dequeue();
		cutVertices[v.getLabel() - 1] = false;

		// iterate in the adjacent list
		VertexList vl = v.getAdjacentVertices();
		while (vl != null)
		{
			// get adjacent vertex
			Vertex u = vl.getVertex();

			// prepares the next adjacent vertex
			vl = vl.getNext();

			// checks to see if the edge (v,u) is in the tree (is marked)
			Edge e = _edges[v.getLabel() - 1][u.getLabel() - 1];
			if (!e.isMarked())
				continue;

			// if u wasn't verified yet (not marked) put into the queue
			if (!u.isMarked())
			{
				queue.enqueue(u);
				u.mark();
			}

			// if u needs v to get reached (u.discoverTime() == low[u]) then v is a cut vertex.
			if (v.getDiscoverTime() <= low[u.getLabel() - 1])
				cutVertices[v.getLabel() - 1] = true;
		}
	}

	// aplicar teorema 2
	v = _vertices[rootLabel - 1];
	VertexList vl = v.getAdjacentVertices();
	int count = 0;
	while (vl != null)
	{
		// get adjacent vertex
		Vertex u = vl.getVertex();

		// prepares the next adjacent vertex
		vl = vl.getNext();

		// checks to see if the edge (v,u) is in the tree (is marked)
		Edge e = _edges[v.getLabel() - 1][u.getLabel() - 1];
		if (!e.isMarked())
			count = count + 1;
	}
	if (count > 1)
		cutVertices[rootLabel - 1] = true;
	else
		cutVertices[rootLabel - 1] = false;

	// return boolean array
	return cutVertices;
}
/**
* Paton's Algorithm to find a set of fundamental cycles of a simple connected Graph
* T = set of vertices already in the partial tree.
* W = set of vertices not examined.
*/
public Walk[] findFundamentalCycles()
{
	int level[] = new int[_n]; // indicates the depth of the vertex i in the partial tree formed so far.
	int pred[] = new int[_n]; // indicates the predecessor vertex label of the path from the root to i.
	int i;
	int z, p;
	Vertex zv, pv;
	java.util.Vector walks = new java.util.Vector(); // a dynamic list of objects to store the cycles that are found.

	// desmarcar todas as arestas
	this.unmarkAllEdges();

	// initialize level and pred vectors with -1
	for (i = 0; i < _n; i++)
	{
		level[i] = -1;
		pred[i] = -1;
	}

	// creates a stack to keep the elements of (T and W)
	VertexStack TW = new VertexStack(_n);

	// initialization
	TW.push(_vertices[0]);
	pred[0] = _vertices[0].getLabel();
	level[0] = 0;

	// Iterate
	while (!TW.isEmpty())
	{
		zv = TW.pop();
		z = zv.getLabel();

		for (p = 1; p <= _n; p++)
		{

			// there is no edge (z,p)
			if (_edges[z - 1][p - 1] == null)
				continue;

			// the edge (z,p) was already used
			if (_edges[z - 1][p - 1].isMarked())
				continue;

			// otherwise...

			// p is not in T?
			if (level[p - 1] == -1)
			{
				TW.push(_vertices[p - 1]);
				level[p - 1] = level[z - 1] + 1;
				pred[p - 1] = z;
			}

			// p is in T. A cycle!
			else
			{
				Walk w = new Walk(_n);
				w.addLabel(z);
				int k = pred[z - 1];
				while (k != pred[p - 1])
				{
					w.addLabel(k);
					k = pred[k - 1];
				}
				w.addLabel(k);
				w.addLabel(p);
				w.addLabel(z);

				// add in the list of cycles
				walks.add(w);
			}

			// mark edges
			_edges[z - 1][p - 1].mark();
			_edges[p - 1][z - 1].mark();
		}

	}

	// mount solution from dynamic list
	Walk[] r = new Walk[walks.size()];
	for (i = 0;i<walks.size();i++) r[i] = (Walk) walks.get(i);

	return r;

}
/**
 * Test routine
 */
public static void main(String argv[])
{
	// create a graph
	Graph g = new Graph(9);
	g.addEdge(1, 1, 1, 1);
	g.addEdge(2, 1, 2, 1);
	g.addEdge(3, 1, 3, 1);
	g.addEdge(4, 1, 4, 1);
	g.addEdge(5, 4, 5, 1);
	g.addEdge(6, 1, 5, 1);
	g.addEdge(7, 1, 6, 1);
	g.addEdge(8, 7, 1, 1);
	g.addEdge(9, 6, 7, 1);
	g.addEdge(10, 3, 8, 1);
	g.addEdge(11, 8, 9, 1);
	g.addEdge(12, 3, 9, 1);

	// test routines in this graph
	System.out.println(g.toString());

	// cut vertices
	System.out.println("....................................................................................");
	System.out.println("Cut Vertices. (Pontos de Articulação)");
	boolean[] cutVertices = g.findCutVertices();
	for (int i = 0; i < cutVertices.length; i++)
	{
		if (cutVertices[i])
			System.out.print("" + (i + 1) + ", ");
	}
	System.out.println("");

	// blocks
	System.out.println("....................................................................................");
	System.out.println("Tag blocks");
	System.out.println("   Edges labeled with 0 are bridges");
	System.out.println("   Edges labeled with n > 0 are all participating of the same biconnected component.");
	System.out.println("Edge Labels:");
	g.tagBlocks();

        // mount solution from dynamic list
        for (int i = 0; i < g._edgeList.getSize(); i++)
                System.out.println("" + g._edgeList.getEdge(i).getLabel() + " -> " + g._edgeList.getEdge(i).getTag());

}
/**
* Given a simple connected weighted graph "G" this routine generates another graph that is a
* minimum cost spanning tree of "G"
* Kruskal's Algorithm
*/
public Graph mcst()
{
	int i, k; // the number of edges in the tree
	int component[] = new int[_n]; // component is labeled with it's smallest vertex label

	// create resultant graph
	Graph tree = new Graph(_n);

	// Initialize component. Initially each component has one vertex and it is labeled with this vertex label
	for (i = 0; i < _n; i++)
		component[i] = i + 1;

	// sort the edges of G in non-decreasing order
	_edgeList.heapSort();

	// iterate
	k = 0; // tree edges count
	i = 0; // "G" edge count
	while (k < _n - 1)
	{
		Edge e = _edgeList.getEdge(i);
		int c1 = component[e.getIncidentVertice1Label()-1];
		int c2 = component[e.getIncidentVertice2Label()-1];

		// edge i is being used
		i = i+1;

		// a cycle
		if (c1 == c2) continue;

		// set c1 to be the smallest component label
		if (c2 < c1)
		{
			int aux = c1;
			c1 = c2;
			c2 = c1;
		}

		// relabel compoenent c2 to c1. This could be more efficient if we used a depth search in the c2 component!
	   for (int j = 0; j < _n; j++)
			if (component[j] == c2)
			   component[j] = c1;

		// add edge in the graph
		tree.addEdge(e.getLabel(),e.getIncidentVertice1Label(),e.getIncidentVertice2Label(),e.getWeight());

		// number of edges in "T"
		k = k+1;

	}
	return tree;
}
/**
 * mount the low vector.
 * pre-condition:
 *    the graph G must have been tagged by tagDFSTree(rootLabel) which defines a tree G'
 *
 * definition:
 *    low[v] = min { discovery[v], discovery[w] : u is descendent or equal v and (u,w) is a backedge in G'}
 */
private int[] mountLowVector(int rootLabel)
{
	Vertex v = _vertices[rootLabel - 1];
	int[] low = new int[_n];
	this.unmarkAllVertices();
	this.mountLowVectorVisit(null, v, low);
	return low;
}
/**
 * Recursive routine to execute the task assigned to mountLowVector(rootLabel).
 */
private int mountLowVectorVisit(Vertex parent, Vertex v, int[] low)
{
	Vertex u;
	v.mark();
	int dw = v.getDiscoverTime();

	// iterate in all adjacent vertices
	VertexList vl = v.getAdjacentVertices();
	while (vl != null)
	{
		u = vl.getVertex();

		// adiantar lista.
		vl = vl.getNext();

		// não considerar a aresta que vai para o pai...
		if (parent == u)
			continue;

		// se u está marcado então (v,u) é aresta de retorno.
		if (u.isMarked())
		{
			dw = Math.min(dw, u.getDiscoverTime());
		}

		// u é descendente de v.
		if (!u.isMarked())
		{
			dw = Math.min(dw, this.mountLowVectorVisit(v, u, low));
		}
	}
	// register the low value
	low[v.getLabel() - 1] = dw;
	return dw;
}
/**
* Test routine
*/
public static void oldmain(String argv[])
{
	/*
	  Graph g = new Graph(5);
	  g.addEdge(1, 1, 2, 1);
	  g.addEdge(2, 2, 3, 1);
	  g.addEdge(3, 3, 4, 1);
	  g.addEdge(4, 4, 5, 1);
	  g.addEdge(5, 3, 5, 1);

	  Graph g = new Graph(7);
	  g.addEdge(1,1,2,1);
	  g.addEdge(2,1,3,2);
	  g.addEdge(3,2,4,4);
	  g.addEdge(4,3,5,1);
	  g.addEdge(5,4,5,1);
	  g.addEdge(6,5,6,5);
	  g.addEdge(7,5,7,2);
	  g.addEdge(8,6,7,1);

	  Graph g = new Graph(5);
	  g.addEdge(1,1,2,1);
	  g.addEdge(2,1,3,1);
	  g.addEdge(3,2,3,1);
	  g.addEdge(4,2,4,1);
	  g.addEdge(5,3,4,1);
	  g.addEdge(6,3,5,1);
	  g.addEdge(7,4,5,1);
	  */

	// create a graph
	Graph g = new Graph(23);
	g.addEdge(1, 1, 2, 1);
	g.addEdge(2, 2, 3, 1);
	g.addEdge(3, 3, 4, 1);
	g.addEdge(4, 4, 1, 1);
	g.addEdge(5, 3, 5, 1);
	g.addEdge(6, 5, 6, 1);
	g.addEdge(7, 6, 7, 1);
	g.addEdge(8, 7, 5, 1);
	g.addEdge(9, 5, 8, 1);
	g.addEdge(10, 5, 9, 1);
	g.addEdge(11, 8, 9, 1);
	g.addEdge(12, 9, 10, 1);
	g.addEdge(13, 5, 11, 1);
	g.addEdge(14, 11, 12, 1);
	g.addEdge(15, 12, 13, 1);
	g.addEdge(16, 13, 14, 1);
	g.addEdge(17, 11, 14, 1);
	g.addEdge(18, 13, 15, 1);
	g.addEdge(19, 15, 19, 1);
	g.addEdge(20, 15, 16, 1);
	g.addEdge(21, 16, 17, 1);
	g.addEdge(22, 17, 19, 1);
	g.addEdge(23, 16, 19, 1);
	g.addEdge(24, 17, 18, 1);
	g.addEdge(25, 16, 20, 1);
	g.addEdge(26, 16, 21, 1);
	g.addEdge(27, 21, 22, 1);
	g.addEdge(28, 22, 23, 1);
	g.addEdge(29, 23, 21, 1);


	// test routines in this graph
   System.out.println("......");
	System.out.println("Graph's Adjacent List");
	System.out.println(g.toString());
	System.out.println("......");
	System.out.println("Depth First Search from 1");
	g.depthFirstSearch(1);
	System.out.println("......");
	System.out.println("Breadth First Search from 1");
	g.breadthFirstSearch(1);
	System.out.println("......");
	System.out.println("Shortest path length from 1 to ");
	int[] length = g.shortestPathLength(1);
	for (int i = 0; i < length.length; i++)
		System.out.println("" + (i + 1) + "->" + length[i] + ",");
	System.out.println("......");
	System.out.println("Shortest path from 1 to ");
	Walk[] w = g.shortestPath(1);
	for (int i = 0; i < w.length; i++)
		System.out.println("" + (i + 1) + "->" + w[i].toString());
	System.out.println("......");
	System.out.println("Shortest path length from all to all");
	int[][] D = g.shortestPathLength();
	for (int i = 0; i < D.length; i++)
	{
		System.out.print("" + (i + 1) + "->");
		for (int j = 0; j < D.length; j++)
			System.out.print(D[i][j] + ",");
		System.out.println("");
	}
	System.out.println("......");
	System.out.println("Shortest paths from all to all");
	Walk[][] ws = g.shortestPath();
	for (int i = 0; i < ws.length; i++)
		for (int j = 0; j < ws[i].length; j++)
			System.out.println("(" + (i + 1) + "," + (j + 1) + ")" + "->" + ws[i][j].toString());
	System.out.println("......");
	System.out.println("Cycles in a connected graph");
	Walk[] cycles = g.findFundamentalCycles();
	for (int i = 0; i < cycles.length; i++)
		System.out.println("" + (i + 1) + "->" + cycles[i].toString());
	System.out.println("......");
	System.out.println("Minimum cost spanning tree");
	Graph tree = g.mcst();
	System.out.println(tree.toString());
	System.out.println("......");
	System.out.println("Cut Vertexes. (Pontos de Articulação)");
	boolean[] cutVertices = g.findCutVertices();
	for (int i = 0; i < cutVertices.length; i++)
		System.out.println("" + (i + 1) + " -> " + cutVertices[i]);
	System.out.println("......");
	System.out.println("Tag blocks");
	g.tagBlocks();
}
/**
* shortest path from all vertices to all vertices
* Floyd Algorithm
*/
public Walk[][] shortestPath()
{
	int i, j, k;
	int[][] D = new int[_n][_n];
	int[][] Z = new int[_n][_n];

	// Z[i][j] means the first intermediate vertex after i to
	// arrive by the shortest path in j
	// (if the path is <i,j> Z[i][j] = j)

	// inicializar a matriz de distâncias D0
	for (i = 0; i < _n; i++)
	{
		for (j = 0; j < _n; j++)
		{
			if (i == j)
			{
				D[i][j] = 0;
				Z[i][j] = -1;
			}
			else
				if (_edges[i][j] != null)
				{
					D[i][j] = _edges[i][j].getWeight();
					Z[i][j] = j + 1;
				}
				else
				{
					D[i][j] = -1;
					Z[i][j] = -1;
				}
		}
	}

	// iterar _n vezes
	for (k = 0; k < _n; k++)
	{

		// seja k o ponto intermediário de um caminho de i para j
		for (i = 0; i < _n; i++)
		{
			for (j = 0; j < _n; j++)
			{

				// qual o comprimento deste caminho? é melhor do que o que já existe?
				if (i == k || j == k || i == j)
					continue;
				if (D[i][k] != -1 && D[k][j] != -1)
				{
					int m = D[i][k] + D[k][j];
					if (D[i][j] == -1)
					{
						D[i][j] = m;
						Z[i][j] = Z[i][k];
					}
					else
						if (D[i][j] > m)
						{
							D[i][j] = m;
							Z[i][j] = Z[i][k];
						}
				}
			}
		}
	}

	// Mount all the paths
	Walk[][] r = new Walk[_n][_n];
	for (i = 0; i < _n; i++)
	{
		for (j = 0; j < _n; j++)
		{
			r[i][j] = new Walk(_n);

			// add all vertices of the walk except the last
			// or find that there is no connection between i+1 to j+1
			boolean notConnected = false;
			k = i + 1;
			while (k != j + 1)
			{
				r[i][j].addLabel(k);
				k = Z[k - 1][j];
				if (k == -1)
				{
					notConnected = true;
					break;
				}
			}

			// if there is no connection empty the partial walk
			if (notConnected)
				r[i][j].clear();

			// else add the last vertice of the walk
			else
				r[i][j].addLabel(k);
		}
	}
	return r;
}
/**
* shortest path (from vertex identified by label to all the other vertices)
* Dijkstra's Algorithm
*/
public Walk[] shortestPath(int label)
{
	int length[] = new int[_n];
	int parent[] = new int[_n];
	int i;
	int nextU, nextULength;
	int lastU;

	// initialize the length and parent vector with -1
	for (i = 0; i < _n; i++)
	{
		length[i] = -1;
		parent[i] = -1;
	}

	// creates a label set
	LabelSet S = new LabelSet(_n);

	// initialize
	S.addLabel(label);
	length[label - 1] = 0;
	nextU = label;

	// Iterate
	while (!S.isComplementEmpty())
	{
		int u = nextU;

		// initialize next element to be added to S to be unavailable
		nextULength = -1;
		nextU = -1;
		S.startComplementIteration();
		while (S.hasNextComplementLabel())
		{
			int v = S.nextComplementLabel();
			Edge e = _edges[u - 1][v - 1];
			int u0vLength;

			// calcular a distância entre u0 e v por u.
			if (e == null)
				u0vLength = -1;
			else
				if (length[u - 1] == -1)
					u0vLength = -1;
				else
					u0vLength = e.getWeight() + length[u - 1];

			// atualizar length de u0 para v caso seja menor que a distância corrente
			if (u0vLength >= 0 && (length[v - 1] > u0vLength || length[v - 1] == -1))
			{
				length[v - 1] = u0vLength;
				parent[v - 1] = u;
			}

			// manter o vértice de !S (not S, S complement) com menor distância para u0 em nextU
			if (nextU == -1 || (length[v - 1] >= 0 && (nextULength > length[v - 1] || nextULength == -1)))
			{
				nextU = v;
				nextULength = length[v - 1];
			}
		}

		// adicionar em S nextU que é o vértice de !S com distância minimal para u0.
		S.addLabel(nextU);
	}

	// Mount the paths from label to the others...
	Walk[] r = new Walk[_n];
	for (i = 0; i < _n; i++)
	{
		r[i] = new Walk(_n);

		// add all vertices from back to front except the first one
		// or find out that there is no connection from label to i+1;
		int j = i + 1;
		boolean notConnected = false;
		while (j != label)
		{
			r[i].addLabel(j);
			j = parent[j - 1];
			if (j == -1)
			{
				notConnected = true;
				break;
			}
		}

		// if there is no connection empty the partial walk
		if (notConnected)
			r[i].clear();

		// else put the first walk vertice and invert the walk
		// to become front to back
		else
		{
			r[i].addLabel(j);
			r[i].invert();
		}
	}
	return r;
}
/**
* shortest path length from all vertices to all vertices
* Floyd Algorithm
*/
public int[][] shortestPathLength()
{
	int i, j, k;
	int[][] D = new int[_n][_n];

	// inicializar a matriz de distâncias D0
	for (i = 0; i < _n; i++)
	{
		for (j = 0; j < _n; j++)
		{
			if (i == j)
				D[i][j] = 0;
			else
				if (_edges[i][j] != null)
					D[i][j] = _edges[i][j].getWeight();
				else
					D[i][j] = -1;
		}
	}

	// iterar _n vezes
	for (k = 0; k < _n; k++)
	{
		// seja k o ponto intermediário de um caminho de i para j
		for (i = 0; i < _n; i++)
		{
			for (j = 0; j < _n; j++)
			{

				// qual o comprimento deste caminho? é melhor do que o que já existe?
				if (i == k || j == k || i == j)
					continue;
				if (D[i][k] != -1 && D[k][j] != -1)
				{
					int m = D[i][k] + D[k][j];
					if (D[i][j] == -1)
						D[i][j] = m;
					else
						if (D[i][j] > m)
							D[i][j] = m;
				}
			}
		}
	}
	return D;
}
/**
* shortest path length (from vertex identified by label to all the other vertices)
* Dijkstra's Algorithm
*/
public int[] shortestPathLength(int label)
{
	int[] length = new int[_n];
	int i;
	int nextU, nextULength;
	int lastU;

	// initialize the length vector with -1
	for (i = 0; i < _n; i++)
		length[i] = -1;

	// creates a label set
	LabelSet S = new LabelSet(_n);

	// initialize
	S.addLabel(label);
	length[label - 1] = 0;
	nextU = label;

	// Iterate
	while (!S.isComplementEmpty())
	{
		int u = nextU;

		// initialize next element to be added to S to be unavailable
		nextU = -1;
		nextULength = -1;
		S.startComplementIteration();
		while (S.hasNextComplementLabel())
		{
			int v = S.nextComplementLabel();
			Edge e = _edges[u - 1][v - 1];
			int u0vLength;

			// calcular a distância entre u0 e v por u.
			if (e == null)
				u0vLength = -1;
			else
				if (length[u - 1] == -1)
					u0vLength = -1;
				else
					u0vLength = e.getWeight() + length[u - 1];

			// atualizar length de u0 para v caso seja menor que a distância corrente
			if (u0vLength >= 0 && (length[v - 1] > u0vLength || length[v - 1] == -1))
				length[v - 1] = u0vLength;

			// manter o vértice de !S (not S, S complement) com menor distância para u0 em nextU
			if (nextU == -1 || (length[v - 1] >= 0 && (nextULength > length[v - 1] || nextULength == -1)))
			{
				nextU = v;
				nextULength = length[v - 1];
			}
		}

		// adicionar em S nextU que é o vértice de !S com distância minimal para u0.
		S.addLabel(nextU);
	}
	return length;
}
/**
 * Adaptation of Paton's Algorithm to find a set of fundamental cycles for the problem of identifying
 * the blocks of G.
 * T = set of vertices already in the partial tree.
 * W = set of vertices not examined.
 */
public void tagBlocks()
{
	int level[] = new int[_n]; // indicates the depth of the vertex i in the partial tree formed so far.
	int pred[] = new int[_n]; // indicates the predecessor vertex label of the path from the root to i.
	int zs[] = new int[_n]; // indicates ordering of z's.
	Object p_zs[] = new Object[_n]; // indicates what are the p vertices for the z vertice in the zs array
	int count;
	int i;
	int k, z, p;
	int tag;
	Vertex zv, pv;
	java.util.Vector backEdgeVertices;

	// desmarcar todas as arestas
	this.unmarkAllEdges();

	// initialize level and pred vectors with -1
	count = 0;
	for (i = 0; i < _n; i++)
	{
		level[i] = -1;
		pred[i] = -1;
		zs[i] = -1;
		p_zs[i] = null;
	}

	// creates a stack to keep the elements of (T and W)
	VertexStack TW = new VertexStack(_n);

	// initialization
	TW.push(_vertices[0]);
	pred[0] = _vertices[0].getLabel();
	level[0] = 0;

	// Discover the tree (pred) and save the information to identify a set of fundamental cycles in G
	while (!TW.isEmpty())
	{
		zv = TW.pop();
		z = zv.getLabel();

		// create a dynamic list to keep the p vertices of the current z vertice.
		backEdgeVertices = new java.util.Vector();
		for (p = 1; p <= _n; p++)
		{

			// there is no edge (z,p)
			if (_edges[z - 1][p - 1] == null)
				continue;

			// the edge (z,p) was already used
			if (_edges[z - 1][p - 1].isMarked())
				continue;

			// otherwise...

			// p is not in T?
			if (level[p - 1] == -1)
			{
				TW.push(_vertices[p - 1]);
				level[p - 1] = level[z - 1] + 1;
				pred[p - 1] = z;
			}

			// p is in T. A cycle!
			else
			{
				// save vertices p such that z,pred(z),pred(pred(z)),...,pred(p),p,z is a cycle.
				backEdgeVertices.add(_vertices[p - 1]);
			}

			// mark edges
			_edges[z - 1][p - 1].mark();
			_edges[p - 1][z - 1].mark();
		}

		// finishing z, saving the information to retreive the cycles found with z.
		zs[count] = z;
		p_zs[count] = backEdgeVertices;
		count = count + 1;
	}


	// start labeling...
	tag = 0;
	while (count > 0)
	{
		count = count - 1;
		z = zs[count];
		backEdgeVertices = (java.util.Vector) p_zs[count];

		// if z doesn't participate of any block then continue.
		if (backEdgeVertices.size() == 0)
			continue;

		// label

		// calculate label of the cycles generated at z

		// if they share an edge in the main trunc...
		int zCyclesTag = _edges[z - 1][pred[z - 1] - 1].getTag();

		// if they share an edge of the type (p,pred(p))
		if (zCyclesTag == 0)
		{
			// search for a label.
			i = 0;
			while (i < backEdgeVertices.size() && zCyclesTag == 0)
			{
				// initialize p.
				p = ((Vertex) backEdgeVertices.get(i)).getLabel();

				// (p,pred(p)).
				zCyclesTag = _edges[p - 1][pred[p - 1] - 1].getTag();
				i++;
			}
		}

		// if it is not one of the above cases it is a new block.
		if (zCyclesTag == 0)
		{
			tag = tag + 1;
			zCyclesTag = tag;
		}

		// find the vertex in the main trunc with the smallest level of all participating vertexes
		// of the cycles generated at z. and label the edges (z,p) (p,pred(p)).
		int smallestPredecessorLevel = _n + 1; // the smallest level of a vertex that is in the block
		int smallestPredecessor = _n + 1; // the smallest predecessor
		for (i = 0; i < backEdgeVertices.size(); i++)
		{
			// label (z,p), (p,pred(p)).
			p = ((Vertex) backEdgeVertices.get(i)).getLabel();
			_edges[z - 1][p - 1].setTag(zCyclesTag);
			_edges[p - 1][pred[p - 1] - 1].setTag(zCyclesTag);

			// calculates smallestPredecessor
			int pPredLevel = level[pred[p - 1] - 1];
			if (pPredLevel < smallestPredecessorLevel)
			{
				smallestPredecessorLevel = pPredLevel;
				smallestPredecessor = pred[p - 1];
			}

			// tag edges that are in the main trunc of the tree
			k = z;
			while (k != smallestPredecessor)
			{
				_edges[k - 1][pred[k - 1] - 1].setTag(zCyclesTag);
				// System.out.println("tagging edge (" + k + "," + pred[k - 1] + ")");
				k = pred[k - 1];
			}
		}
	}
}
/**
 * Initial routine that registers:
 *    - the discovery time of each vertex.
 *    - the finish time of each vertex.
 *    - the tree and back edges of the searh.
 * This routine uses the tagDFSTreeVisit recursive routine.
 */
private void tagDFSTree(int rootLabel)
{
	_time = 0;
	this.unmarkAllVertices();
   this.tagDFSTreeVisit(null,_vertices[rootLabel-1]);
}
/**
 * Recursive routine to register:
 *    the discovery time of each vertex.
 *    the finish time of each vertex.
 *    the tree and back edges of the searh.
 */
private void tagDFSTreeVisit(Vertex parent, Vertex v)
{
	Vertex u;
	v.mark();
	_time = _time + 1;
	v.setDiscoverTime(_time);
	VertexList vl = v.getAdjacentVertices();

	// iterate in all adjacent vertices
	while (vl != null)
	{
		u = vl.getVertex();

		// adiantar lista.
		vl = vl.getNext();

		// não considerar a aresta que vai para o pai...
		if (parent == u)
			continue;

		// se u está marcado então é aresta de retorno.
		if (u.isMarked()) {
		   // arestas de retorno ficam desmarcadas.
			_edges[v.getLabel()-1][u.getLabel()-1].unmark();
		}

		// se u não está marcado então é aresta da árvore.
		if (!u.isMarked()) {
		   // arestas de árvores são marcadas
			_edges[v.getLabel()-1][u.getLabel()-1].mark();

			// marcar nós das sub-árvores
			tagDFSTreeVisit(v,u);
		}
	}
	_time = _time + 1;
	v.setFinishTime(_time);
}
/**
* Prints the graph vertices adjacent list.
*/
public String toString()
{
	String s = "";
	s = s + "........................................................................................\n";
	s = s + "Graph's adjacent list:\n";
	s = s + "   verticeLabel -> (adjacentVertice1, edgeLabel1), ... , (adjacentVerticeN, edgeLabelN) \n";
	s = s + "........................................................................................\n";

	for (int i = 0; i < _n; i++)
	{
		Vertex v = _vertices[i];
		s = s + v.getLabel() + " - ";
		VertexList vl = v.getAdjacentVertices();
		while (vl != null)
		{
			Vertex w = vl.getVertex();
			s = s + "(" + w.getLabel()+ "," + _edges[v.getLabel()-1][w.getLabel()-1].getLabel() +")" + ", ";
			vl = vl.getNext();
		}
		s = s + "\n";
	}
	return s;
}
/**
* unmark all vertices.
* in the adjacent list representation
*/
private void unmarkAllEdges()
{
	for (int i = 0; i < _edgeList.getSize(); i++)
		_edgeList.getEdge(i).unmark();
}
/**
* unmark all vertices.
* in the adjacent list representation
*/
private void unmarkAllVertices()
{
	for (int i = 0; i < _n; i++)
		_vertices[i].unmark();
}
}
