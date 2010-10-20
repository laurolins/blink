package linsoft.rbtree2;

import java.util.Stack;
/**
 * Insert the type's description here.
 * Creation date: (8/24/2001 3:55:02 PM)
 * @author: Administrator
 */
public class RBTreeIterator {
	private static final Byte LEFT = new Byte((byte) 0);
	private static final Byte ROOT = new Byte((byte) 1);
	private static final Byte RIGHT = new Byte((byte) 2);
	private static final Byte FINISHED = new Byte((byte) 3);
	private RBNode _currentNode;
	private Stack _stack;
/**
 * RBTreeIterator constructor comment.
 */
public RBTreeIterator(RBTree tree) {
	super();
	_stack = new Stack();
	_currentNode = null;

	// update
	if (tree.root() != null) {
		_stack.push(tree.root());
		_stack.push(LEFT);

		// go to next node.
		this.goToNext();
	}
	else _currentNode = null;
}
/**
 * Sets the current object to be the next object available in order.
 */
private void goToNext() {
	//
	while (!_stack.isEmpty()) {

		//
		Byte state = (Byte) _stack.pop();
		RBNode node = (RBNode) _stack.pop();

		//
		if (state == LEFT) {
			_stack.push(node); // push node.
			_stack.push(ROOT); // push node state = ROOT.
			while (node.left() != null) {
				node = node.left();
				_stack.push(node); // push node.
				_stack.push(ROOT); // push node state = ROOT.
			}
		}

		//
		if (state == ROOT) {
			_stack.push(node); // push node.
			_stack.push(RIGHT); // push node state = RIGHT.

			// we've found the next element.
			_currentNode = node;
			return;
		}

		//
		if (state == RIGHT) {
			_stack.push(node); // push node.
			_stack.push(FINISHED); // push node state = FINISHED.
			if (node.right() != null) {
				_stack.push(node.right()); // push node.
				_stack.push(LEFT); // push node state = LEFT.
			}
		}
	}

	// if reach this point next node is null!
	_currentNode = null;
}
/**
 * Is there more nodes to iterate?
 */
public boolean hasNext() {
	return (_currentNode != null);
}
/**
 * Get next node.
 */
public Object next() {
	// copy next node.
	RBNode node = _currentNode;

	// go to next node.
	this.goToNext();

	// return;
	if (node != null)
		return node.object();
	else
		throw new RuntimeException("There is no next node");
}
}
