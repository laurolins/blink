package linsoft.graph;

class VertexQueue {

   private int _first;
   private int _last;
   private Vertex _queue[];
   
   public VertexQueue(int maxSize)
   {
	  _queue = new Vertex[maxSize];
	  _first = 0;
	  _last = 0;
   }   
   public void dequeue()
   {
	  if (this.isEmpty()) throw new RuntimeException("Empty Queue!");

	  _queue[_first] = null;
	  _first = (_first + 1) % _queue.length;
   }   
   public void enqueue(Vertex v)
   {
	  _queue[_last] = v;
	  _last = (_last + 1) % _queue.length;
   }   
   public Vertex getFirst()
   {
	  if (this.isEmpty()) throw new RuntimeException("Empty Queue!");
	  
	  return _queue[_first];
   }   
   public boolean isEmpty()
   {
	  return _first == _last; 
   }   
}
