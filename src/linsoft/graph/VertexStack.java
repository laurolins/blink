package linsoft.graph;

class VertexStack {
   private int _top;
   private Vertex _stack[];
public VertexStack(int maxSize)
{
	_stack = new Vertex[maxSize];
	_top = 0;
}
public boolean isEmpty()
{
	return (_top == 0);
}
public Vertex pop()
{
	if (_top == 0)
		return null;
	else
	{
		_top = _top - 1;
		return _stack[_top];
	}
}
public void push(Vertex v)
{
	_stack[_top] = v;
	_top = _top + 1;
}
}
