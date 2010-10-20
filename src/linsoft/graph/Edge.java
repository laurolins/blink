package linsoft.graph;

class Edge {
   private int _label;         // edge label
   private int _labelV1;       // first incident vertice label
   private int _labelV2;       // second incident vertice label
   private int _weight;        // weight of the edge
	private boolean _marked;    // indicate if the edge is marked
   private Object _userObject; // user object
	private int _tag;           // an integer mark in an edge
   public Edge(int label, int labelV1, int labelV2, int weight, Object userObject)
   {
	  _label = label;
	  _labelV1 = labelV1;
	  _labelV2 = labelV2;
	  _weight = weight;
	  _userObject = userObject;
	  _marked = false;
   }   
   public int getAdjacentLabel(int label)
   {
	   if (label == _labelV1) return _labelV2;
	   else if (label == _labelV2) return _labelV1;
	   else return -1;
   }      
   public int getIncidentVertice1Label()
   {
	  return _labelV1; 
   }   
   public int getIncidentVertice2Label()
   {
	  return _labelV2; 
   }   
public int getLabel()
{
	return _label;
}
public int getTag()
{
	return _tag;
}
   public Object getUserObject()
   {
	  return _userObject; 
   }   
   public int getWeight()
   {
	  return _weight;
   }   
public boolean isMarked()
{
	return _marked;
}
public void mark()
{
	_marked = true;
}
public void setTag(int tag)
{
	_tag = tag;
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
