package linsoft.graph;

/**
 * Class that represents sequence of labels (identifying vertices),
 * this represents Walks on simple graphs.
 */
public class Walk {
   private int _vertices; 
   private int[] _walk;
   
   /**
	* Creates an empty walk with initial capacity of n labels (length == n)
	* This initial capacity automatically grows over demand.
	*/
   public Walk(int n)
   {
	  _vertices = 0;
	  _walk = new int[n];
   }   
   public void addLabel(int label)
   {
	  if (_vertices == _walk.length) this.growCapacity(1);
	  _walk[_vertices] = label;
	  _vertices++;
   }   
   public void clear()
   {
	  _vertices = 0;
   }   
   /**
	* The length of a walk is the number of edges
	* which is the number of vertices minus one
	*/
   public int getLength()
   {
	  return _vertices-1;
   }   
   public int getNumberOfVertices()
   {
	  return _vertices;
   }   
   public int getVerticeLabel(int index)
   {
	  return _walk[index];
   }   
   private void growCapacity(int delta)
   {
	  int aux[] = new int[_walk.length+delta];
	  for (int i=0;i<_vertices;i++) aux[i] = _walk[i];
	  _walk = aux;
   }   
   public void invert()
   {
	  int m = _vertices / 2;
	  for (int i=0;i<m;i++)
	  {
		 int aux = _walk[i];
		 _walk[i] = _walk[_vertices-1-i];
		 _walk[_vertices-1-i]=aux;
	  }
   }   
   public String toString()
   {
	  String s;
	  s = "";
	  for (int i=0;i<_vertices;i++) {
		 s = s + _walk[i];
		 if (i<_vertices-1) s=s + ", ";
	  }
	  return s;
   }   
}
