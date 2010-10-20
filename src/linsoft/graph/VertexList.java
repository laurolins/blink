package linsoft.graph;

class VertexList {
   private Vertex _vertex;
   private VertexList _next;
   
   public VertexList(Vertex vertex, VertexList next)
   {
	  _vertex = vertex;
	  _next = next;
   }   
   public VertexList getNext()
   {
	  return _next; 
   }   
   public Vertex getVertex()
   {
	  return _vertex; 
   }   
   public void setNext(VertexList next)
   {
	  _next = next; 
   }   
   public void setVertex(Vertex vertex)
   {
	  _vertex = vertex; 
   }   
}
