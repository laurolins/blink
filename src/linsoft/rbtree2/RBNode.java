package linsoft.rbtree2;

/**
 * Insert the type's description here.
 * Creation date: (8/24/2001 3:53:53 PM)
 * @author: Administrator
 */
public class RBNode {
	public static final boolean RED = false;
	public static final boolean BLACK = true;

	/**
	 * Object of this node.
	 */
	private Object _object;

	/**
	 * key of the object.
	 */
	private Comparable _key;

	/**
	 * Object of this node.
	 */
	private boolean _color;

	/**
	 * left child of this node.
	 */
	private RBNode _left;

	/**
	 * righ child of this node.
	 */
	private RBNode _right;

	/**
	 * parent node of this node.
	 */
	private RBNode _parent;

	/**
	 * RBNode constructor.
	 */
	public RBNode(Object object, Comparable key, boolean color, RBNode parent, RBNode left, RBNode right) {
		super();
		_object = object;
		_key = key;
		_color = color;
		_parent = parent;
		_left = left;
		_right = right;
	}

	/**
	 * object of this node.
	 */
	public Object object() {
		return _object;
	}

	/**
	 * key of this node.
	 */
	public Comparable key() {
		return _key;
	}

	/**
	 * color of this node.
	 */
	public boolean color() {
		return _color;
	}

	/**
	 * left node of this node. can be null!
	 */
	public RBNode left() {
		return _left;
	}

	/**
	 * right node of this node. can be null!
	 */
	public RBNode right() {
		return _right;
	}

	/**
	 * parent node of this node. can be null!
	 */
	public RBNode parent() {
		return _parent;
	}

	/**
	 * set color of this node.
	 */
	public void setColor(boolean newColor) {
		_color = newColor;
	}

	/**
	 * set left node of this node.
	 */
	public void setLeft(RBNode newNode) {
		_left = newNode;
	}

	/**
	 * set left node of this node.
	 */
	public void setRight(RBNode newNode) {
		_right = newNode;
	}

	/**
	 * set parent node of this node.
	 */
	public void setParent(RBNode newParent) {
		_parent = newParent;
	}

	/**
	 * object of this node.
	 */
	public void setKey(Comparable newKey) {
		_key = newKey;
	}

	/**
	 * object of this node.
	 */
	public void setObject(Object newObject) {
		_object = newObject;
	}

	public void clear() {
		_object = null;
		_key = null;
		_parent = null;
		if(_left != null) {
			_left.clear();
			_left = null;
		}
		if(_right != null) {
			_right.clear();
			_right = null;
		}
	}
}
