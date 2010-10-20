package linsoft.graph;

class Vertex {
   private int _label;
   private Object _userObject;
   private boolean _marked;
   private VertexList _adjacentVertices;
   private int _discoverTime;  // field for marking a traversal discover time
   private int _finishTime;    // field for marking a traversal finish time
   public Vertex(int label, Object userObject, boolean marked)
   {
	  _label = label;
	  _userObject = userObject;
	  _marked = marked;
	  _adjacentVertices = null;
   }   
   public void addAdjacentVertex(Vertex vertex)
   {
	  if (_adjacentVertices == null) 
	  {
		 _adjacentVertices = new VertexList(vertex,null);
		 return;
	  }
	  
	  VertexList v = _adjacentVertices;
	  while (v.getNext() != null)
		 v = v.getNext();
		 
	  v.setNext(new VertexList(vertex,null));      
   }   
   public VertexList getAdjacentVertices()
   {
	  return _adjacentVertices;
   }   
   public int getDiscoverTime()
   {
	  return _discoverTime; 
   }                  
   public int getFinishTime()
   {
	  return _finishTime; 
   }               
   public int getLabel()
   {
	  return _label; 
   }   
   public Object getUserObject()
   {
	  return _userObject; 
   }   
   public boolean isMarked()
   {
	  return _marked; 
   }   
   public void mark()
   {
	  _marked = true; 
   }   
   public void setDiscoverTime(int time)
   {
	  _discoverTime = time; 
   }      
   public void setFinishTime(int time)
   {
	  _finishTime = time; 
   }            
   public void setUserObject(Object userObject)
   {
	  _userObject = userObject; 
   }   
   public void unmark()
   {
	  _marked = false; 
   }   
}
