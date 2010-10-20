package linsoft.graph;

class EdgeList {
   private java.util.Vector _list; // the list
   public EdgeList()
   {
	   _list = new java.util.Vector();
   }   
   public void addEdge(Edge e)
   {
	   _list.add(e);
   }   
private void buildHeap()
{
	int n = this.getSize();
	int i = n / 2;
	int j, k;
	Edge p, q, r;
	while (i > 0)
	{
		j = i;
		k = j + j;

		// enqunto houver filho...
		while (k <= n)
		{
			p = this.getEdge(j - 1);
			q = this.getEdge(k - 1);

			// se tem filho a direita ...
			if (k < n)
			{
				r = this.getEdge(k);
				// register q with the edge and j with the index of the smallest of the sons of the edge p with index i
				if (q.getWeight() < r.getWeight())
				{
					k = k + 1;
					q = r;
				}
			}

			// se é preciso descer com o parent ...
			if (q.getWeight() > p.getWeight())
			{
				this.swap(j - 1, k - 1);
				j = k;
				k = j + j;
			}

			// senão acabou a árvore binária rooted em i está em forma de heap...
			else
				break;
		}
		i = i - 1;
	}
}
   public Edge getEdge(int index)
   {
	   return (Edge) _list.get(index);
   }   
   public int getSize()
   {
	   return _list.size();
   }   
public void heapSort()
{
	this.buildHeap();

	int n = this.getSize();

	for (int i=n;i>1;i--) {
		this.swap(0,i-1);
		this.rearrangeHeap(i-1);
	}
}
private void rearrangeHeap(int size)
{
	int n = size;
	int j, k;
	Edge p, q, r;

	// inicializar
	j = 1;
	k = j + j;

	// enqunto houver filho...
	while (k <= n)
	{
		p = this.getEdge(j - 1);
		q = this.getEdge(k - 1);

		// se tem filho a direita ...
		if (k < n)
		{
			r = this.getEdge(k);
			// register q with the edge and j with the index of the smallest of the sons of the edge p with index i
			if (q.getWeight() < r.getWeight())
			{
				k = k + 1;
				q = r;
			}
		}

		// se é preciso descer com o parent ...
		if (q.getWeight() > p.getWeight())
		{
			this.swap(j - 1, k - 1);
			j = k;
			k = j + j;
		}

		// senão acabou a árvore binária rooted em 1 com n elementos está em forma de heap...
		else
			break;
	}
}
   public void swap(int index1, int index2)
   {
	   Object aux = _list.get(index1);
	   _list.set(index1,_list.get(index2));
	   _list.set(index2,aux);
   }   
   public String toString()
   {
	   String st = "";
	   for (int i=0;i<this.getSize();i++) {
		   Edge e = this.getEdge(i);
		   st = st+"("+e.getIncidentVertice1Label()+","+e.getIncidentVertice2Label()+", w:"+e.getWeight()+"), ";
	   }
	   return st;
   }   
}
