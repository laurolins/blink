package linsoft.rbtree2;

/**
 * Insert the type's description here.
 * Creation date: (8/24/2001 3:53:39 PM)
 * @author: Administrator
 */
public class RBTree {
    /**
     * Root of the tree.
     */
    private RBNode _root;

    public static final boolean BLACK = true;
    public static final boolean RED = false;

    /**
     * RBTree constructor.
     */
    public RBTree() {
        super();
        _root = null;
    }

    /**
     * Insert in a Binary Search Tree.
     */
    private RBNode treeInsert(Comparable key, Object object) {

        // search where is the place of "key"
        RBNode y = null;
        RBNode x = _root;
        while (x != null) {
            y = x;
            if (key.compareTo(x.key()) <= 0) {
                x = x.left();
            }
            else {
                x = x.right();
            }
        }

        // create node.
        RBNode newNode = new RBNode(object, key, RBNode.RED, y, null, null);
        if (y == null) {
            _root = newNode;
        }
        else {
            if (key.compareTo(y.key()) <= 0) {
                y.setLeft(newNode);
            }
            else {
                y.setRight(newNode);
            }
        }

        // return created node.
        return newNode;
    }

    /**
     * Get node color.
     */
    private boolean color(RBNode x) {
        if (x == null) {
            return BLACK;
        }
        else {
            return x.color();
        }
    }

    /**
     * Insert in a Binary Search Tree.
     */
    public void delete(Comparable key) {
        // declare some useful variables.
        RBNode px, x, y;

        // find node
        RBNode z = this.treeSearch(key);

        // if it doesn't exists do nothing
        if (z == null) {
            return;
        }

        // reorganize tree.

        // define y. The node that will be actually removed from the tree.
        if (z.left() == null || z.right() == null) {
            y = z;
        }
        else {
            y = treeSuccessor(z);
        }

        // define x. y's unique subtree.
        if (y.left() != null) {
            x = y.left();
        }
        else {
            x = y.right();
        }

        // x will replace y on the tree
        px = y.parent();
        if (x != null) {
            x.setParent(y.parent());
        }
        if (y.parent() == null) {
            _root = x;
        }
        else {
            if (y == y.parent().left()) {
                y.parent().setLeft(x);
            }
            else {
                y.parent().setRight(x);
            }
        }

        // copy y content to the z node.
        if (y != z) {
            z.setKey(y.key());
            z.setObject(y.object());
        }

        // if color rules are broken correct them.
        if (y.color() == BLACK) {
            this.deleteFixup(x, px);
        }
    }

    /**
     * x = base node
     * px = x parent.
     */
    private void deleteFixup(RBNode x, RBNode px) {
        // declare useful variables.
        RBNode w;

        //
        /* if (x == null) {
            return;
        } */

        // reorganize tree.
        while (x != _root && this.color(x) == BLACK) {

            // x is a left child...
            if (x == px.left()) {
                w = px.right(); // w is allways not null

                //
                if (w == null) {
                    System.out.println("w não pode ser null 1");
                    throw new RuntimeException("w não pode ser null 1");
                }

                // Case 1
                if (this.color(w) == RED) {
                    w.setColor(BLACK);
                    px.setColor(RED);
                    this.leftRotate(px);
                    w = px.right();
                }

                //
                if (w == null) {
                    System.out.println("w não pode ser null 2");
                    throw new RuntimeException("w não pode ser null 2");
                }

                // Case 2
                if (this.color(w.left()) == BLACK && this.color(w.right()) == BLACK) {
                    w.setColor(RED);
                    x = px;
                    px = x.parent();
                }
                else {
                    // Case 3
                    if (this.color(w.right()) == BLACK) {
                        w.left().setColor(RED);
                        w.setColor(RED);
                        this.rightRotate(w);
                        w = px.right();
                    }

                    // Case 4
                    w.setColor(px.color());
                    px.setColor(BLACK);
                    w.right().setColor(BLACK);
                    this.leftRotate(px);
                    x = _root;
                }
            }

            // x is a right child...
            else {
                w = px.left(); // w is allways not null

                // Case 1
                if (w.color() == RED) {
                    w.setColor(BLACK);
                    px.setColor(RED);
                    this.rightRotate(px);
                    w = px.left();
                }

                // Case 2
                if (this.color(w.right()) == BLACK && this.color(w.left()) == BLACK) {
                    w.setColor(RED);
                    x = px;
                    px = x.parent();
                }
                else {
                    // Case 3
                    if (this.color(w.left()) == BLACK) {
                        w.right().setColor(RED);
                        w.setColor(RED);
                        this.leftRotate(w);
                        w = px.left();
                    }

                    // Case 4
                    w.setColor(px.color());
                    px.setColor(BLACK);
                    w.left().setColor(BLACK);
                    this.rightRotate(px);
                    x = _root;
                }
            }
        }
        if (x != null)
            x.setColor(BLACK);
    }

    /**
     * Insert in a Binary Search Tree.
     */
    public void insert(Comparable key, Object object) {
        // declare some variables
        RBNode x;
        RBNode y;

        // tree insert
        x = this.treeInsert(key, object);

        // put it RED
        x.setColor(RBNode.RED);

        //
        while (x != _root) {

            // if x parent color is BLACK we are finished
            if (x.parent().color() == RBNode.BLACK) {
                break;
            }

            // x parent is x grandparent left child
            if (x.parent() == x.parent().parent().left()) {
                y = x.parent().parent().right();

                // case 1
                if (y != null && y.color() == RED) {
                    x.parent().setColor(BLACK);
                    y.setColor(BLACK);
                    x.parent().parent().setColor(RED);
                    x = x.parent().parent();
                }

                // case 2 & 3
                else {
                    if (x == x.parent().right()) {
                        x = x.parent();
                        this.leftRotate(x);
                    }
                    x.parent().setColor(BLACK);
                    x.parent().parent().setColor(RED);
                    this.rightRotate(x.parent().parent());
                }
            }

            // x parent is x grandparent right child
            else {
                y = x.parent().parent().left();

                // case 1
                if (y != null && y.color() == RED) {
                    x.parent().setColor(BLACK);
                    y.setColor(BLACK);
                    x.parent().parent().setColor(RED);
                    x = x.parent().parent();
                }

                // case 2 & 3
                else {
                    if (x == x.parent().left()) {
                        x = x.parent();
                        this.rightRotate(x);

                        //
                        // System.out.println("------- Right Rotate ------\n"+this.mountString());
                    }
                    x.parent().setColor(BLACK);
                    x.parent().parent().setColor(RED);
                    this.leftRotate(x.parent().parent());
                }
            }
        }

        // always do this!
        _root.setColor(BLACK);
    }

    /**
     * left rotation. Assume that x.right() != null
     */
    private void leftRotate(RBNode x) {
        // Debug...
        // {
        // System.out.println("left rotation...");
        // }

        RBNode y = x.right();

        // turn y's left subtree into x's right subtree
        x.setRight(y.left());
        if (y.left() != null) {
            y.left().setParent(x);

            // link x's parent to y
        }
        y.setParent(x.parent());
        if (x.parent() == null) {
            _root = y;
        }
        else {
            if (x == x.parent().left()) {
                x.parent().setLeft(y);
            }
            else {
                x.parent().setRight(y);
            }
        }

        // put x on y's left
        y.setLeft(x);
        x.setParent(y);
    }

    /**
     *
     */
    public String mountString() {
        StringBuffer buffer = new StringBuffer(1000);
        this.mountStringRecursively(_root, buffer, 0);
        return buffer.toString();
    }

    /**
     *
     */
    private void mountStringRecursively(RBNode x, StringBuffer buffer, int depth) {
        // base case
        if (x == null) {
            return;
        }

        // go to the right
        mountStringRecursively(x.right(), buffer, depth + 1);

        // print it self
        for (int i = 0; i < depth; i++) {
            buffer.append("\t");
        }
        buffer.append(x.key() + "_" + (x.color() ? "B" : "R"));
        buffer.append("\n");

        // go to the left
        mountStringRecursively(x.left(), buffer, depth + 1);
    }

    /**
     * right rotation. Assume that x.left() != null
     */
    private void rightRotate(RBNode x) {
        // Debug...
        // {
        // System.out.println("rigth rotation...");
        // }

        RBNode y = x.left();

        // turn y's right subtree into x's left subtree
        x.setLeft(y.right());
        if (y.right() != null) {
            y.right().setParent(x);

            // link x's parent to y
        }
        y.setParent(x.parent());
        if (x.parent() == null) {
            _root = y;
        }
        else {
            if (x == x.parent().right()) {
                x.parent().setRight(y);
            }
            else {
                x.parent().setLeft(y);
            }
        }

        // put x on y's right
        y.setRight(x);
        x.setParent(y);
    }

    /**
     * Root node of the tree.
     */
    public RBNode root() {
        return _root;
    }

    /**
     * Insert in a Binary Search Tree.
     */
    public Object search(Comparable key) {
        RBNode x = this.treeSearch(key);
        if (x != null) {
            return x.object();
        }
        else {
            return null;
        }
    }

    /**
     * Get node with minimum key at the tree beggining with root x.
     */
    private RBNode treeMaximum(RBNode x) {
        while (x.right() != null) {
            x = x.right();
        }
        return x;
    }

    /**
     * Get node with minimum key at the tree beggining with root x.
     */
    private RBNode treeMinimum(RBNode x) {
        while (x.left() != null) {
            x = x.left();
        }
        return x;
    }

    /**
     * Search node with specified key. Null if node doesn't exists.
     */
    private RBNode treeSearch(Comparable key) {
        RBNode x = _root;
        while (x != null && !key.equals(x.key())) {
            if (key.compareTo(x.key()) < 0) {
                x = x.left();
            }
            else {
                x = x.right();
            }
        }
        return x;
    }

    /**
     * Get node with minimum key at the tree beggining with root x.
     */
    private RBNode treeSuccessor(RBNode x) {
        // there is a successor under this node?
        if (x.right() != null) {
            return treeMinimum(x.right());
        }

        // the successor if exists is above this node.
        RBNode y = x.parent();
        while (y != null && x == y.right()) {
            x = y;
            y = x.parent();
        }
        return y;
    }

    public void clear() {
        if (_root != null)
            _root.clear();
        _root = null;
    }


    /**
     *
     */
    public static void main(String[] args) {
        test();
    }

    /**
     *
     */
    public static void testSpeed() {
        RBTree t = new RBTree();

        //
        int inserts = 0;
        int deletes = 0;
        int searches = 0;
        long t1, t2;

        //
        java.util.Random r = new java.util.Random(123112);

        //
        t1 = System.currentTimeMillis();
        for (int i = 0; i < 200000; i++) {
            // add
            if (r.nextInt(100) <= 99) {
                int n = r.nextInt(50000000);
                searches++;
                if (t.search("" + n) == null) {
                    //System.out.println("Insert: " + n);
                    t.insert("" + n, new Integer(n));
                    inserts++;
                }
            }
// delete
            else {
                int n = r.nextInt(50000);
                searches++;
                if (t.search("" + n) != null) {
                    System.out.println("Delete: " + n);
                    //t.delete("" + n);
                    deletes++;
                }
            }
        }
        t2 = System.currentTimeMillis();

        //
        System.out.println("Searches: " + searches);
        System.out.println("Inserts: " + inserts);
        System.out.println("Deletes: " + deletes);
        System.out.println("Time: " + (t2 - t1));
    }

    /**
     *
     */
    public static void test() {
        RBTree t = new RBTree();

        int n = 10000;
        int a = 1;
        int b = 10000;

        //
        String previousTree = null;

        //
        for (int i = 0; i < n; i++) {
            int rndNum = a + (int) Math.round( ( (double) Math.random() * (b - a)));
            if (t.search("" + rndNum) == null) {

                //
                previousTree = t.mountString();

                //
                t.insert("" + rndNum, new Integer(rndNum));

                //
                if (!t.checkRBProperty()) {
                    System.out.println("Problema ao inserir: "+rndNum);
                    System.out.println("Árvore Antiga: ");
                    System.out.println(previousTree);
                    System.out.println("Árvore Nova: ");
                    System.out.println(t.mountString());
                }
            }
        }
        //System.out.println("Tree before problem: \n" + t.mountString());
        //
        System.out.println("Tree: \n" + t.mountString());

        for (int i = 0; i < n; i++) {
            int rndNum = a + (int) Math.round( ( (double) Math.random() * (b - a)));
            try {
                //
                previousTree = t.mountString();

                //

                t.delete("" + rndNum);

                //
                if (!t.checkRBProperty()) {
                    System.out.println("Problema ao remover: "+rndNum);
                    System.out.println("Árvore Antiga: ");
                    System.out.println(previousTree);
                    System.out.println("Árvore Nova: ");
                    System.out.println(t.mountString());
                }
            }
            catch (Exception ex) {
                System.out.println("Problem!");
                System.out.println("Deleting: "+rndNum);
                System.out.println("Tree before problem: \n" + previousTree);
            }
        }
        System.out.println("Finished...");
        System.exit(0);
    }










    ////////////
    //
    int _bh = -1;
    public boolean checkRBProperty() {
        _bh = -1;

        // root
        if (_root == null)
            return true;
        else
            return dfs(_root,0);
    }

    private boolean dfs(RBNode n, int currentBH) {
        //
        boolean ok = true;

        //
        if (n.color() == BLACK)
            currentBH++;

        // test if this is node has a null child
        if (n.left() == null || n.right() == null) {
            if (_bh == -1) {
                _bh = currentBH+1;
            }
            else if (_bh != currentBH+1) {
                ok = false;
            }
        }

        // test left subtree
        if (n.left() != null) {
            ok = dfs(n.left(), currentBH);
        }

        // if still ok test right subtree
        if (ok && n.right() != null) {
            ok = dfs(n.right(), currentBH);
        }

        //
        return ok;
    }
    //
    ///////////////




}
