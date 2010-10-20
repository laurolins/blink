package linsoft.graph;

class LabelSet {
   private int _n;                       // possible labels in the set are {1 ... _n}
   private int _size;                    // size of the current set
   private int _complementSize;          // _n - _size
   private boolean[] _presenceArray;      // represents the presence in the set: (_presenceArray[label-1] == true) if and only if label is in the set.
   private int _iteratorIndex;      
   private int _complementIteratorIndex;
   
   /**
	* Creates an empty label set for the labels {1, 2, ... "n"}.
	*/
   public LabelSet(int n)
   {
	  _n = n;
	  _size = 0;
	  _complementSize = _n;
	  _presenceArray = new boolean[_n];
	  _iteratorIndex = -1;
	  _complementIteratorIndex = 0;
   }   
   /**
	* Adds a label in the set. If the label is already in the set return false
	* else returns true and restarts iteration and complement iteration.
	*/
   public boolean addLabel(int label)
   {
	  if (_presenceArray[label-1] == true) return false;

	  _presenceArray[label-1]=true;
	  _size=_size+1;
	  _complementSize=_complementSize-1;

	  // reinitializes the current iterations
	  startIteration();
	  startComplementIteration();
	  
	  return true;
   }   
   /**
	* Sets _complementIteratorIndex to k
	* where (k > _complementIteratorIndex) and (_presenceArray[k]==false) and (_presenceArray[j]==false, j>_complementIteratorIndex => j >= k) 
	* sets _complementIteratorIndex -1 if there isn't any k.
	*/
   private void findNextComplementIteratorIndex()
   {
	  _complementIteratorIndex = _complementIteratorIndex+1;
	  
	  while (_complementIteratorIndex < _n)
	  {
		 if (_presenceArray[_complementIteratorIndex] == false) break;
		 else _complementIteratorIndex = _complementIteratorIndex+1;
	  }
	  
	  if (_complementIteratorIndex >= _n) _complementIteratorIndex=-1;
				  
   }   
   /**
	* Sets _iteratorIndex to k
	* where (k > _iteratorIndex) and (_presenceArray[k]==true) and (_presenceArray[j]==true, j>_iteratorIndex => j >= k) 
	* sets _iteratorIndex -1 if there isn't any k.
	*/
   private void findNextIteratorIndex()
   {
	  _iteratorIndex = _iteratorIndex+1;
	  
	  while (_iteratorIndex < _n)
	  {
		 if (_presenceArray[_iteratorIndex] == true) break;
		 else _iteratorIndex = _iteratorIndex+1;
	  }
	  
	  if (_iteratorIndex >= _n) _iteratorIndex=-1;
				  
   }   
   /**
	* Indicates whether there is more complement labels for the current
	* complement iteration
	*/
   public boolean hasNextComplementLabel()
   {
	  return (_complementIteratorIndex != -1);
   }   
   /**
	* Indicates whether there is more labels for the current iteration
	*/
   public boolean hasNextLabel()
   {
	  return (_iteratorIndex != -1);
   }   
   public boolean isComplementEmpty()
   {
	  return (_complementSize == 0);
   }   
   public boolean isEmpty()
   {
	  return (_size == 0);
   }   
   /**
	* Gets next label not in the set (current complement iteration)
	* returns -1 if there isn't any more lables in the complement set.
	*/
   public int nextComplementLabel()
   {
	  if (_complementIteratorIndex == -1) return -1;
	  else {
		 int r = _complementIteratorIndex+1;
		 
		 this.findNextComplementIteratorIndex();
		 
		 return r;
	  }
   }   
   /**
	* Gets next label in the set (current iteration)
	* returns -1 if there isn't any more lables.
	*/
   public int nextLabel()
   {
	  if (_iteratorIndex == -1) return -1;
	  else {
		 int r = _iteratorIndex+1;
		 
		 this.findNextIteratorIndex();
		 
		 return r;
	  }
   }   
   /**
	* Removes a label in the set. If the label is not in the set return false
	* else returns true and restarts iteration and complement iteration.
	*/
   public boolean removeLabel(int label)
   {
	  if (_presenceArray[label-1] == false) return false;

	  _presenceArray[label-1]=false;
	  _size=_size-1;
	  _complementSize=_complementSize+1;

	  // reinitializes the current iterations
	  startIteration();
	  startComplementIteration();
	  
	  return true;
   }   
   /**
	* Restarts the iteration of the elements of the set.
	*/
   public void startComplementIteration()
   {
	  _complementIteratorIndex = -1;
	  this.findNextComplementIteratorIndex();
   }   
   /**
	* Restarts the iteration of the elements of the set.
	*/
   public void startIteration()
   {
	  _iteratorIndex = -1;
	  this.findNextIteratorIndex();
   }   
}
