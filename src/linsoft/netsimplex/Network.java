package linsoft.netsimplex;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Network
 */
public class Network {

    // nodes of the network
    private Node[] _nodes;

    // nodes of the network
    private int _numNodes;

    // arcs of the network
    private Arc[] _arcs;

    // nodes of the network
    private int _numArtificialArcs;

    // nodes of the network
    private int _numArtificialNodes;

    // nodes of the network
    private int _numArcs;

    // list of non-tree arcs (first non-tree arc)
    private Arc _firstNonTreeArc;

    // root of the tree node
    private Node _root;

    // choose edge cluster size (sqrt of numedges)
    private int _clusterSize;

    public Network(int maxNodes, int maxArcs) {
        this.allocateSpace(maxNodes, maxArcs);
    }

    private void allocateSpace(int maxNodes, int maxArcs) {
        _nodes = new Node[maxNodes + 1]; // +1 is to save space for the special node
        _arcs = new Arc[maxArcs + maxNodes]; // +maxNodes is to save space for one arc from the special node to the each of the normal nodes
        _numNodes = 0;
        _numArcs = 0;
    }

    public Network(String fileName) throws Exception {
        BufferedReader fr = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));


        boolean problemDefinitionProcessed = false;

        while (true) {
            String s = fr.readLine();
            if (s == null)
                break;

            // empty line
            if (s.length() == 0)
                continue;

            // a comment line
            if (s.charAt(0) == 'c')
                continue;

            // problem definition line
            if (s.charAt(0) == 'p' && !problemDefinitionProcessed) {
                StringTokenizer st = new StringTokenizer(s," ");

                // throw away control token
                st.nextToken();

                // problem name
                String problemId = st.nextToken();

                // numNodes and numArcs
                int numNodes = Integer.parseInt(st.nextToken());
                int numArcs = Integer.parseInt(st.nextToken());

                // allocate space for all nodes and arcs
                this.allocateSpace(numNodes,numArcs);

                // create all nodes
                for (int i=0;i<numNodes;i++)
                    this.addNode(0);

                problemDefinitionProcessed = true;
            }

            // node update line
            else if (s.charAt(0) == 'n') {
                StringTokenizer st = new StringTokenizer(s," ");

                // throw away control token
                st.nextToken();

                // nodeName
                int nodeIndex = Integer.parseInt(st.nextToken()) - 1;
                int nodeFlow = Integer.parseInt(st.nextToken());

                // allocate space for all nodes and arcs
                Node n = this.getNode(nodeIndex);
                n.set_b(-nodeFlow);
            }

            // arc create line
            else if (s.charAt(0) == 'a') {
                StringTokenizer st = new StringTokenizer(s," ");

                // throw away control token
                st.nextToken();

                // properties
                int tail = Integer.parseInt(st.nextToken()) - 1;
                int head = Integer.parseInt(st.nextToken()) - 1;
                int minFlow = Integer.parseInt(st.nextToken());
                int maxFlow = Integer.parseInt(st.nextToken());
                maxFlow = (maxFlow < 0 ? Arc.INFINITE_CAPACITY : maxFlow);
                int cost = Integer.parseInt(st.nextToken());

                // create arc
                this.addArc(_nodes[tail],_nodes[head],cost,minFlow,maxFlow);
            }
        }
    }

    /**
     * Método usado para guardar uma network. Que pode ser recuperada depois por
     * Network(String fileName).
     */
    public void networkGenerator() {
        // Ainda sem comentários... c.

        // A linha que define o problema: minimizar o valor desta network...
        System.out.println("p min "+this.getNumberOfNodes()+" "+this.getNumberOfArcs());

        // Todos os nós.
        for (int i=0;i<this.getNumberOfNodes()-this._numArtificialNodes;i++){
            Node node = this.getNode(i);
            System.out.println("n "+(i+1)+" "+node.get_b());
        }

        // Todos os arcos.
        for (int i=0;i<this.getNumberOfArcs()-this._numArtificialArcs;i++){
            Arc arc = this.getArc(i);
            int maxFlow = arc.get_maxFlow() < 0 ? Arc.INFINITE_CAPACITY : arc.get_maxFlow();
            System.out.print("a "+(arc.get_tail().get_index()+1)+" "+(arc.get_head().get_index()+1)+" ");
            System.out.println(arc.get_minFlow()+" "+maxFlow+" "+arc.get_cost());
        }

        System.out.println("");
    }

    /**
     * add new node to the network giving it's net-flow or excess.
     * @param b net-flow or excess of the new node
     */
    public Node addNode(int b) {
        if (_numNodes == _nodes.length)
            throw new NetworkException();
        int index = _numNodes;
        Node newNode = _nodes[index] = new Node(index,b);
        _numNodes++;
        return newNode;
    }

    /**
     * add new node to the network giving it's net-flow or excess.
     * @param b net-flow or excess of the new node
     */
    public Arc addArc(Node tail, Node head, int cost, int minCapacity, int maxCapacity) {
        if (_numArcs == _arcs.length)
            throw new NetworkException();
        int index = _numArcs;
        _arcs[_numArcs++] = new Arc(index,tail, head, cost, minCapacity, maxCapacity);
        return _arcs[_numArcs-1];
    }

    /**
     * add new node to the network giving it's net-flow or excess.
     * @param b net-flow or excess of the new node
     */
    public Arc addArc(Node tail, Node head, int cost, int maxCapacity) {
        return this.addArc(tail, head, cost, 0, maxCapacity);
    }

    public int getNumberOfNodes() {
        return _numNodes;
    }

    public Node getRoot() {
        return _root;
    }

    public Node getNode(int index) {
        return _nodes[index];
    }

    public int getNumberOfArcs() {
        return _numArcs;
    }

    public Arc getArc(int index) {
        return _arcs[index];
    }

    /**
     * Init
     */
    public void initNetworkForAlgorithm() {
        // iteracoes
        _iteracao = 0;

        ///////////////////////////////////////////
        // 1. find an initial feasible flow

        // adjust the current net flow of each node
        // admit it is initially zero.
        int maxCostInModuleOfOriginalArcs = 0;
        Arc previousArc = null;
        for (int i=0;i<_numArcs;i++) {
            Arc a = this.getArc(i);
            if (a.get_flow() > 0) {
                a.get_tail().addConstantToNetFlow(-a.get_flow());
                a.get_head().addConstantToNetFlow(a.get_flow());
            }
            int a_cost_mod = Math.abs(a.get_cost());

            // make a list of non-tree arcs
            a.set_type(Arc.ARC_TYPE_NON_TREE);
            a.set_previousArcOfTheSameType(previousArc);

            // manter lista ligada de arcos
            // do tipo non-tree-arc.
            if (previousArc != null) {
                // se nao for o primeiro arco faça...
                previousArc.set_nextArcOfTheSameType(a);
            }
            else {
                // se for o primeiro arco em processamento guarde um link p/ ele
                _firstNonTreeArc = a;
            }
            previousArc = a;

            // keep the largest cost of an arc
            maxCostInModuleOfOriginalArcs = (maxCostInModuleOfOriginalArcs > a_cost_mod ? maxCostInModuleOfOriginalArcs : a_cost_mod);

            // if it is the last arc...
            if (i == _numArcs-1) {
                previousArc.set_nextArcOfTheSameType(_firstNonTreeArc);
                _firstNonTreeArc.set_previousArcOfTheSameType(previousArc);
            }
        }


        // add an special node that is linked to
        // all the other nodes
        Node specialNode = this.addNode(0);
        Node previousNode = specialNode;

        //
        _numArtificialNodes++;

        // previous Arc
        previousArc = null;

        // add an edge with the flow needed to balance everything
        for (int i=0;i<_numNodes-_numArtificialNodes;i++) {
            Node currentNode = this.getNode(i);
            int net_flow = currentNode.get_netFlow();
            int b = currentNode.get_b();
            // net_flow + new_edge == b => new_edge = b - net_flow
            int newArcFlow = b - net_flow;

            // the depth of the nodes
            currentNode.set_depth(1);

            // create an arc on the right direction and set it's
            // flow to balance the things
            Arc a;
            if (newArcFlow < 0) {
                a = this.addArc(currentNode, specialNode, 0, Arc.INFINITE_CAPACITY);
            }
            else {
                a = this.addArc(specialNode, currentNode, 0, Arc.INFINITE_CAPACITY);
            }

            // set a large cost

            a.set_cost(1 + _numNodes * maxCostInModuleOfOriginalArcs);
            a.set_flow(Math.abs(newArcFlow));

            // add an artificial arc
            _numArtificialArcs++;

            // set new arc as a tree arc
            a.set_type(Arc.ARC_TYPE_TREE);

            // set predecessor and predecessor arc of currentNode
            currentNode.set_predecessor(specialNode);
            currentNode.set_treeArcToThisNode(a);
            if (a.get_tail() == specialNode)
                currentNode.set_y(a.get_cost());
            else
                currentNode.set_y(-a.get_cost());

            // set the sucessor of the previous node
            previousNode.set_sucessor(currentNode);
            previousNode = currentNode;

            // set sucessor of the last node
            if (i == _numNodes-_numArtificialNodes-1) {
                currentNode.set_sucessor(specialNode);
            }

        }
        _root = specialNode;

        // 1. the end
        ///////////////////////////////////////////

        // this will optimize the search for the tree edge
        // and must be called with artificial edges at the end
        // and artificial no at the end
        this.sortArcsByTailAndSetNodeOutgoingArcsInterval();

        //
        _clusterSize = (int) Math.ceil(Math.sqrt(_numArcs));

        /////////////////
        // log
        /*
        System.out.println("INITIALIZATION: ");
        System.out.println("Network value: " + this.getNetworkValue());
        System.out.println("Network feasability test: "+(this.isFeasible() ? "TRUE" : "FALSE") );
        System.out.println("Predecessor tree test: "+(this.checkPredecessorTree() ? "TRUE" : "FALSE"));
        System.out.println("Optimality test: "+(this.checkOptimality() ? "TRUE" : "FALSE")); */
        // log
        /////////////////
    }

    /**
     * Init
     */
    public void initNetworkForAlgorithmForContinuation() {
        // iteracoes
        _iteracao = 0;
        // this will optimize the search for the tree edge
        // and must be called with artificial edges at the end
        // and artificial no at the end
        this.sortArcsByTailAndSetNodeOutgoingArcsInterval();
    }

    private int _iteracao = 0;

    public int getIteracao() {
        return _iteracao;
    }

    // cycle edges list (keep it allocated)
    ArrayList _cycleEdges = new ArrayList();

    /**
     * Return true if we at an optimal solution.
     */
    public boolean nextIteration() {

        _iteracao++;

        //////////////////////////////
        // log
        // System.out.println("Iteracao: " + (_iteracao));
        // log
        //////////////////////////////

        ///////////////////////////////////////////
        // 3. find an arc "e" to enter on the tree.
        // OBS. the efficiency of this algorithm can
        // be increased if we wait to choose
        // a better "cost" in turn of choosing the
        // first possibility as is currently implemented
        int k = 0;
        Arc e = null;
        int eReducedCostInModule = 0;
        Arc eCurrent = _firstNonTreeArc;
        int eCurrentReducedCost = eCurrent.get_reducedCost();
        while (true && k < _clusterSize) {

            // System.out.println("testing arc to enter: "+eCurrent.getHeadTailString());

            // avoid arcs without min capacity == max capacity
            if (eCurrent.get_minFlow() != eCurrent.get_maxFlow()) {
                // flow is on minimum and reduced cost is negative
                if (eCurrent.isFlowOnMinimum() && eCurrentReducedCost < 0) {
                    int eCurrentReducedCostInModule = -eCurrentReducedCost;
                    if (e == null || eReducedCostInModule < eCurrentReducedCostInModule) {
                        e = eCurrent;
                        eReducedCostInModule = eCurrentReducedCostInModule;
                    }
                    k++;
                }

                else if (eCurrent.isFlowOnMaximum() && eCurrentReducedCost > 0) {
                    if (e == null || eReducedCostInModule < eCurrentReducedCost) {
                        e = eCurrent;
                        eReducedCostInModule = eCurrentReducedCost;
                    }
                    k++;
                }
            }

            // next arc
            eCurrent = eCurrent.get_nextArcOfTheSameType();
            eCurrentReducedCost = eCurrent.get_reducedCost();

            // already seen all the arcs
            if (eCurrent == _firstNonTreeArc)
                break;
        }

        // we are finished. we are on a minimal solution
        if (e == null) {
            //////////////////////////////
            // log
            // System.out.println("No edges to enter! We are finished!");
            // log
            //////////////////////////////
            return true;
        }

        //////////////////
        // Log
        /*
        System.out.println("Entering edge \"e\" will be: " +
                           e.get_tail().get_index() + "->" +
                           e.get_head().get_index() + " " + e.getLabel());   */
        // Log
        //////////////////

        // 3. the end
        ///////////////////////////////////////////

        /////////////////////////////////////////////
        // 4. find the arcs on "T  U {e}" that forms the cycle C.
        // We define C by starting with an edge incident to the apex
        // of the cycle (the node with shallowest depth) and the folowing
        // edges on C are so that we will add flow on it

        // how to add flow to the cycle?
        // if the entering edge e has flow x = M (is on maximum flow)
        // then it's reduced cost is rc_e > 0 and we want to
        // reduce it's current flow. Otherwise, if e has flow
        // x = m (is on minimum flow) then it's reduced cost
        // is rc_e < 0 and we want to augment it's flow
        boolean eIsOnMinimumFlow = e.isFlowOnMinimum();

        Node apex = null;
        _cycleEdges.clear();

        // add e to the cycle
        _cycleEdges.add(e);

        Node a = e.get_tail();
        Node b = e.get_head();
        while (a != b) {
            if (a.get_depth() <= b.get_depth()) {
                // Node predecessor = b.get_predecessor();
                // Arc ee = this.findArcOnTree(predecessor,b);
                Arc ee = b.get_treeArcToThisNode();
                if (eIsOnMinimumFlow)
                    _cycleEdges.add(ee);
                else
                    _cycleEdges.add(0, ee);
                b = b.get_predecessor();
            }
            else {
                // Node predecessor = a.get_predecessor();
                // Arc ee = this.findArcOnTree(predecessor,a);
                Arc ee = a.get_treeArcToThisNode();
                if (eIsOnMinimumFlow)
                    _cycleEdges.add(0, ee);
                else
                    _cycleEdges.add(ee);
                a = a.get_predecessor();
            }
        }
        apex = a;

        //////////////////
        // Log
        /*System.out.println("Cycle formed with e:");
        System.out.println("Apex: " + apex.getLabel());
        for (int i = 0; i < _cycleEdges.size(); i++) {
            Arc ee = (Arc) _cycleEdges.get(i);
            System.out.println("Arc[" + i + "] = " +
                               ee.get_tail().get_index() + "->" +
                               ee.get_head().get_index() + " " +
                               ee.getLabel());
        }*/
        // Log
        //////////////////

        // 4. the end
        /////////////////////////////////////////////


        /////////////////////////////////////////////
        // 5. Check if the cycle has at leas one edge
        // with finite capacity (problem is unbounded)
        // and, at the same time find the most
        // contrained edge in cycle order (edge f to
        // exit the tree).
        boolean isUnboundedCycle = true;

        // this is a control flag to mark wheter the
        // edge e has already being seen.
        boolean eWasNotSeenYet = true;

        // most contrained variation of flow
        // that can be applied
        int teta = Integer.MAX_VALUE;

        // the exiting-from-tree arc
        Arc f = null;

        // this flag indicates if on cycle direction,
        // the exiting-from-tree arc "f" comes before
        // the entering-on-tree arc "e".
        boolean f_ComesBefore_e = true;

        for (int i = 0; i < _cycleEdges.size(); i++) {
            Arc ee = (Arc) _cycleEdges.get(i);

            // if ee
            if (ee.isReverse(e, eWasNotSeenYet) || !ee.isCapacityUnbounded()) {
                isUnboundedCycle = false;
            }

            // ee is reverse?
            if (ee.isReverse(e, eWasNotSeenYet)) {

                // check to see if it is the most constrained edge until now
                int tetaCurrent = ee.get_flow() - ee.get_minFlow();
                if (f == null || tetaCurrent < teta) {
                    f = ee;
                    teta = tetaCurrent;
                    f_ComesBefore_e = eWasNotSeenYet;
                }

                // every edge has a positive minFlow so
                // the cycle flow cannot be increased infinitely.
                isUnboundedCycle = false;
            }

            // ee is forward
            else {

                // check to see if it is the most constrained edge until now
                int tetaCurrent = ee.get_maxFlow() - ee.get_flow();
                if (f == null || tetaCurrent < teta) {
                    f = ee;
                    teta = tetaCurrent;
                    f_ComesBefore_e = eWasNotSeenYet;
                }

                // if the arc capacity is bounded than
                // the cycle flow cannot be increased infinitely.
                if (!ee.isCapacityUnbounded())
                    isUnboundedCycle = false;
            }

            // update the control flag "passedBy_e"
            if (e == ee) {
                eWasNotSeenYet = false;
            }
        }

        //////////////////
        // Log
        /*
        if (f != null) {
            System.out.println("Exiting edge \"f\" will be: " +
                               f.get_tail().get_index() + "->" +
                               f.get_head().get_index() + " " + f.getLabel());
            System.out.println("teta: " + teta);
        }
        else {
            System.out.println("Exiting edge not found!!");
        } */
        // Log
        //////////////////

        // unbounded cycle
        if (isUnboundedCycle)
            throw new NetworkException("Unbounded cycle");

        // 5. the end
        /////////////////////////////////////////////

        ///////////////////////////////////////////////////
        // 6. Augment x by teta on the cycle

        // teta may be 0
        if (teta > 0) {

            // this is a control flag to mark wheter the
            // edge e has already being seen.
            eWasNotSeenYet = true;

            // adjust cycle flow
            for (int i = 0; i < _cycleEdges.size(); i++) {
                Arc ee = (Arc) _cycleEdges.get(i);

                // the entering arc is on it's maximum flow and decreasing it's flow
                // gives a better solution so, on the direction of the cycle induced
                // by "e", we have to remove teta from the flow of each edge.
                if (ee.isReverse(e, eWasNotSeenYet)) {
                    ee.set_flow(ee.get_flow() - teta);
                }
                else {
                    ee.set_flow(ee.get_flow() + teta);
                }

                // update the control flag "passedBy_e"
                if (e == ee) {
                    eWasNotSeenYet = false;
                }
            }
        }
        // 6. the end
        //////////////////////////////////////////////////

        //////////////////////////////////////////////////
        // 7. Updating the sucessor

        // if e just changed bounds then we are done for this iteration
        if (e != f) {

            Node e1 = (f_ComesBefore_e ? e.get_head() : e.get_tail());
            Node e2 = (f_ComesBefore_e ? e.get_tail() : e.get_head());
            if (!eIsOnMinimumFlow) { // se "e" ta no maximo inverte
                Node aux = e1;
                e1 = e2;
                e2 = aux;
            }

            Node f1 = f.getShallowestNode();
            Node f2 = f.getDeepestNode();

            /*
            System.out.println("e1: " + e1.getLabel());
            System.out.println("e2: " + e2.getLabel());
            System.out.println("f1: " + f1.getLabel());
            System.out.println("f2: " + f2.getLabel()); */

            this.updateTree(e, f, e1, e2, f1, f2);
        }
        else {
           /* System.out.println(
                "e did not enter on the tree it only changed it's bounds!");*/
        }

        // 7. the end
        //////////////////////////////////////////////////

        /////////////////
        // log
        /*
        System.out.println("-- It ---------------------------");
        System.out.println("Network value: " + this.getNetworkValue());
        System.out.println("Network feasability test: "+(this.isFeasible() ? "TRUE" : "FALSE") );
        System.out.println("Predecessor tree test: "+(this.checkPredecessorTree() ? "TRUE" : "FALSE"));
        System.out.println("Optimality test: "+(this.checkOptimality() ? "TRUE" : "FALSE"));
        System.out.println("Sum Y: "+this.getYSum());   */
        // log
        /////////////////

        //
        return false;
    }

    public void colocarArcComFluxoMaximoNaArvore(Arc arcoFixo, Arc arco){
        if(!arco.isFlowOnMaximum()){
            System.out.println("Arco com capacidade != do max.: "+arco.getLabel());
            throw new NetworkException("O arco deve ter fluxo na maxima capacidade.");
        }
        Arc e = arco;
        Node apex = null;
        _cycleEdges.clear();

        // adiciona e ao ciclo
        _cycleEdges.add(e);

        // construindo o ciclo
        Node a = e.get_tail();
        Node b = e.get_head();
        while (a != b) {
            if (a.get_depth() <= b.get_depth()) {
                Arc ee = b.get_treeArcToThisNode();
                _cycleEdges.add(0, ee);
                b = b.get_predecessor();
            }
            else {
                Arc ee = a.get_treeArcToThisNode();
                _cycleEdges.add(ee);
                a = a.get_predecessor();
            }
        }
        apex = a;

        // encontrando o f que vai sair.
        boolean eWasNotSeenYet = true;
        Arc f = null;
        boolean f_ComesBefore_e = true;
        for (int i = 0; i < _cycleEdges.size(); i++) {
            Arc ee = (Arc) _cycleEdges.get(i);
            // o arco f deve ser diferente do arcoFixo que deve permanecer na arvore
            if (ee.equals(arcoFixo)){
                continue;
            }
            if ( (ee.isFlowOnMinimum() || ee.isFlowOnMaximum()) && ee != e) {
                f = ee;
                f_ComesBefore_e = eWasNotSeenYet;
                if (!eWasNotSeenYet) // depois de passarmos por e, ficamos com o primeiro candidato.
                    break;
            }
            // update the control flag "passedBy_e"
            if (e == ee) {
                eWasNotSeenYet = false;
                if (f != null)
                    break;// se encontramos um cadidato antes (o mais proximo) de e, ficamos com ele.
            }
        }

        if (f == null){
            throw new NetworkException("Nao ha arco com fluxo min ou max alem de e.");
        }

        // fazendo a troca na arvore
        Node e1 = (f_ComesBefore_e ? e.get_tail() : e.get_head());
        Node e2 = (f_ComesBefore_e ? e.get_head() : e.get_tail());
        Node f1 = f.getShallowestNode();
        Node f2 = f.getDeepestNode();
        this.updateTree(e, f, e1, e2, f1, f2);
    }

    public void colocarArcComFluxoMaximoNaArvore(Arc arco){
        this.colocarArcComFluxoMaximoNaArvore(null, arco);
    }

    public Arc findArcOnTree(Node a, Node b) {
        for (int i=0;i<_numArcs;i++) {
            Arc e = _arcs[i];
            if (e.isTreeArc() &&
                ((e.get_tail() == a && e.get_head() == b) || (e.get_tail() == b && e.get_head() == a)))
                return e;
        }
        throw new RuntimeException("No Tree Arc: "+a.get_index()+"-"+b.get_index());
        // return null;
    }

    public Arc optimizedFindArcOnTree(Node a, Node b) {
        // search first on the outgoing arcs from a
        if (a.get_numOutgoingArcs() == 0)
            throw new RuntimeException("No Tree arc from a to b");

        // a -> b?
        for (int i = a.get_firstOutgoingArcIndex();i<a.get_firstOutgoingArcIndex()+a.get_numOutgoingArcs();i++) {
            Arc e = _arcs[i];
            if (e.isTreeArc() && e.get_head() == b)
                return e;
        }

        // b -> a?
        for (int i = b.get_firstOutgoingArcIndex();i<b.get_firstOutgoingArcIndex()+b.get_numOutgoingArcs();i++) {
            Arc e = _arcs[i];
            if (e.isTreeArc() && e.get_head() == a)
                return e;
        }

        throw new RuntimeException("No Tree Arc: "+a.get_index()+"-"+b.get_index());
        // return null;
    }

    private void insertionSort() {
        for (int j=1;j<_numArcs;j++) {
            Arc key = _arcs[j];
            int i = j-1;
            while (i>=0 &&
                   (_arcs[i].get_tail().get_index() > key.get_tail().get_index()) ||
                   (_arcs[i].get_tail().get_index() == key.get_tail().get_index() &&
                    _arcs[i].get_head().get_index() > key.get_head().get_index())) {
                _arcs[i+1] = _arcs[i];
                i = i-1;
            }
            _arcs[i+1] = key;
        }
    }

    private void sortArcsByTailAndSetNodeOutgoingArcsInterval() {
        // this.quicksort(0,_numArcs-1);
        // this.insertionSort(); // this is much better!!!
        Arc.qsort(_arcs,0,_numArcs-1);
        // System.out.println("Check qsort: "+Arc.check(_arcs,0,_numArcs-1));

        // Arc.inssort(_arcs,0,_numArcs-1);

        // reset indexes and setup nodes interval
        Node currentTail = null;
        int currentNumArcs = 0;
        for (int j=0;j<_numArcs;j++) {
            // adjust the index
            _arcs[j].set_index(j);

            // adjust outgoing interval of the nodes
            Node tail = _arcs[j].get_tail();
            if (currentTail == null) {
                currentTail = tail;
                currentNumArcs = 1;
            }
            else if (currentTail != tail) {
                currentTail.setOutgoingsArcsIndexInterval(j-currentNumArcs,currentNumArcs);
                currentTail = tail;
                currentNumArcs = 1;
            }
            else {
                currentNumArcs++;
            }
        }

        // the last one
        if (currentTail != null) {
            currentTail.setOutgoingsArcsIndexInterval(_numArcs-currentNumArcs,currentNumArcs);
        }
    }

    public long getNetworkValue() {
        long networkValue = 0;
        for (int i=0;i<_numArcs;i++)
            networkValue += (long)_arcs[i].get_cost() * _arcs[i].get_flow();
        return networkValue;
    }

    /**
     * Check if the network flow is feasible
     */
    public boolean isFeasible() {

        // reset netflow on nodes
        for (int i=0;i<_numNodes;i++) {
            _nodes[i].set_netFlow(0);
        }

        // check flow on edges and update netFlow on nodes
        for (int i=0;i<_numArcs;i++) {
            Arc a = _arcs[i];
            Node t = a.get_tail();
            Node h = a.get_head();
            t.addConstantToNetFlow(-a.get_flow());
            h.addConstantToNetFlow(a.get_flow());
            if (a.get_flow() < a.get_minFlow() || (!a.isCapacityUnbounded() && a.get_flow() > a.get_maxFlow())) {
                System.out.println("Checking network feasability counterexample");
                System.out.println("Arc "+t.get_index()+"->"+h.get_index()+" has flow out of range!");
                return false;
            }
        }

        // check netFlow on nodes
        for (int i=0;i<_numNodes;i++) {
            if (_nodes[i].get_netFlow() != _nodes[i].get_b()) {
                System.out.println("Checking network feasability counterexample");
                System.out.println("Node "+_nodes[i].get_index()+" has netFlow <> b!");
                return false;
            }
        }

        // ok it is feasible
        return true;
    }

    /**
     * Check optimality
     */
    public boolean checkOptimality() {
        // check flow on edges and update netFlow on nodes
        for (int i=0;i<_numArcs;i++) {
            Arc a = _arcs[i];

            // System.out.println("reduced cost = "+a.get_reducedCost());

            if (a.isTreeArc())
                continue;
            if (a.isFlowOnMinimum() && a.get_reducedCost() < 0) {
                System.out.println("Not optimality proof: "+a.getHeadTailString()+" reduced cost = "+a.get_reducedCost());
                return false;
            }
            else if (!a.isCapacityUnbounded() && a.get_reducedCost() > 0 &&
                     a.isFlowOnMaximum()) {
                System.out.println("Not optimality proof: "+a.getHeadTailString()+" reduced cost = "+a.get_reducedCost());
                return false;
            }
        }
        return true;
    }

    /**
     * Check if any artificial arc has flow different from zero.
     */
    public boolean isOriginalNetworkFeasible() {
        for (int i=_numArcs-_numArtificialArcs;i<_numArcs;i++)
            if (_arcs[i].get_flow() != 0)
                return false;
        return true;
    }


    /**
     * Check if the network flow is feasible
     */
    public boolean checkPredecessorTree() {
        // reset netflow on nodes
        for (int i=0;i<_numNodes;i++) {

            // walk on the path from node to the root
            // checking the depths and if there is no cycle
            Node n = _nodes[i];
            int nDepth = n.get_depth();

            // check predecessor structure
            if (n.get_predecessor() == null && nDepth != 0) {
                System.out.println("Checking predecessor tree counterexample");
                System.out.println("A tree path passing by node "+n.get_index()+" does not match depths!");
                return false;
            }
            else {
                while (n.get_predecessor() != null && nDepth >= 0) {
                    n = n.get_predecessor();
                    nDepth--;
                    if (n.get_depth() != nDepth) {
                        System.out.println("Checking predecessor tree counterexample");
                        System.out.println("A tree path passing by node " + n.get_index() + " does not match depths!");
                        return false;
                    }
                }
            }
            return true;
        }

        // check flow on edges and update netFlow on nodes
        for (int i=0;i<_numArcs;i++) {
            Arc a = _arcs[i];
            Node t = a.get_tail();
            Node h = a.get_head();
            t.addConstantToNetFlow(-a.get_flow());
            h.addConstantToNetFlow(a.get_flow());
            if (a.get_flow() < a.get_minFlow() || (!a.isCapacityUnbounded() && a.get_flow() > a.get_maxFlow())) {
                System.out.println("Arc "+t.get_index()+"->"+h.get_index()+" has flow out of range!");
                return false;
            }
        }

        // check netFlow on nodes
        for (int i=0;i<_numNodes;i++) {
            if (_nodes[i].get_netFlow() != _nodes[i].get_b()) {
                System.out.println("Node "+_nodes[i].get_index()+" has netFlow <> b!");
                return false;
            }
        }

        // ok it is feasible
        return true;
    }

    public long getYSum() {
        long ysum = 0;
        for (int i=0;i<_numNodes;i++)
            ysum += _nodes[i].get_y();
        return ysum;
    }

    public long getOriginalNetworkValue() {
        long networkValue = 0;
        for (int i=0;i<_numArcs-_numArtificialArcs;i++)
            networkValue += (long)_arcs[i].get_cost() * _arcs[i].get_flow();
        return networkValue;
    }

    private void updateTree(Arc e, Arc f, Node e1, Node e2, Node f1, Node f2) {

        // iteracao
        if (this.getIteracao() == 40) {
            _iteracao = _iteracao + 1 - 1;
        }

        // reduced cost to add to all the nodes
        int signedReducedCost = (e1 == e.get_tail() ? e.get_reducedCost() : -e.get_reducedCost());

        // step 0. [initialize]
        Node a = e1;
        while (a.get_sucessor() != f2)
            a = a.get_sucessor();
        Node b = e1.get_sucessor();
        Node i = e2;

        // constante pra somar nas profundidades
        // de toda subarvore c_i
        int constantToAddToDepthsOfSubtree_i = e1.get_depth() + 1 - e2.get_depth();

        // step 1. [find last node k in S1 and initialize the value of
        // r and adjust the depths of all S1 subtree]
        Node k = i;
        while(true) {
            if (k.get_sucessor().get_depth() > i.get_depth()) {
                // update node depth and y
                k.get_sucessor().addConstantToDepth(constantToAddToDepthsOfSubtree_i);
                k.get_sucessor().addConstantToY(signedReducedCost);

                // go to sucessor on the same subtree
                k = k.get_sucessor();
            }
            else break;
        }
        Node r = k.get_sucessor();

        // proximo predecessor
        Node nextPredecessor = e1;

        // iterate while not i == f2
        int climb_count = 0;
        while (i != f2) {

            // step 3. [climb up the pivot stem and update s(k)]
            Node j = i;
            i = i.get_predecessor();

            // increment number of go ups used to
            // update constantToAddToDepthsOfSubtree_i
            climb_count++;

            k.set_sucessor(i);

            // update old i subtree root (now j subtree root)
            j.addConstantToDepth(constantToAddToDepthsOfSubtree_i);
            j.addConstantToY(signedReducedCost);
            j.set_predecessor(nextPredecessor);
            if (e1 == nextPredecessor)
                j.set_treeArcToThisNode(e);
            else
                j.set_treeArcToThisNode(optimizedFindArcOnTree(nextPredecessor, j));
                // j.set_treeArcToThisNode(findArcOnTree(nextPredecessor, j));

            // prepare to next update of predecessor
            nextPredecessor = j;

            // prepare constantToAddToDepthsOfSubtree_i
            constantToAddToDepthsOfSubtree_i = e1.get_depth() + 1 + climb_count - i.get_depth();

            // step 4. [find the last node k in the left part of S_i]
            k = i;
            while (k.get_sucessor() != j) {
                k = k.get_sucessor();

                // update subtree of k (but not the first node)
                k.addConstantToDepth(constantToAddToDepthsOfSubtree_i);
                k.addConstantToY(signedReducedCost);
            }

            // step 5. [if the right part of S_i is non-empty then update s(k), find the last node in S_i, and update r]
            if (r.get_depth() > i.get_depth()) {
                k.set_sucessor(r); // passo 5.1

                // passo 5.2
                while(true) {
                    if (k.get_sucessor().get_depth() > i.get_depth()) {
                        // go to sucessor on the same subtree
                        k = k.get_sucessor();

                        // update node depth and y
                        k.addConstantToDepth(constantToAddToDepthsOfSubtree_i);
                        k.addConstantToY(signedReducedCost);
                    }
                    else break;
                }
                r = k.get_sucessor();
            }
        }

        // step 2. [if at the end of S*, remove S and insert S*]

        // set predecessor
        i.set_predecessor(nextPredecessor);
        if (e1 == nextPredecessor)
            i.set_treeArcToThisNode(e);
        else
            i.set_treeArcToThisNode(optimizedFindArcOnTree(nextPredecessor, i));

        // set depth and y
        i.addConstantToDepth(constantToAddToDepthsOfSubtree_i);
        i.addConstantToY(signedReducedCost);

        if (e1 != a) {
            a.set_sucessor(r);
            e1.set_sucessor(e2);
            k.set_sucessor(b);
        }
        else {
            e1.set_sucessor(e2);
            k.set_sucessor(r);
        }

        //
        this.updateTreeAndNonTreeArcs(e,f);
    }

    /**
     * If the arc is on the tree replace it by the artificial
     * arc to the deepest node (arc from root to it).
     */
    public boolean removeArcFromTree(Arc f) {
        if (!f.isTreeArc())
            return false;

        if (f.get_flow() > 0)
            throw new RuntimeException("We do not allow removing an arc with flow from the tree.");

        if (f.getShallowestNode() == _root)
            throw new RuntimeException("We do not allow removing an artificial arc.");


        // get nodes of "f"
        Node f1 = f.getShallowestNode();
        Node f2 = f.getDeepestNode();

        // get nodes of "e"
        Arc e = _arcs[_numArcs - _numArtificialArcs + f2.get_index()];

        Node e1 = e.getShallowestNode(); // this is the root
        Node e2 = e.getDeepestNode();

        // check
        if (e1 != _root)
            throw new RuntimeException("e1 is not root");

        // check
        if (e2 != f2)
            throw new RuntimeException("e2 is not f2");

        //
        this.updateTree(e,f,e1,e2,f1,f2);

        //
        return true;
    }

    /**
     * Remove arc without reindexing.
     */
    public void removeArcKeepingTheFlow(Arc a) {
        if (a.isTreeArc())
            throw new RuntimeException("We cannot remove this edge");

        if (a.get_flow() > 0)
            throw new RuntimeException("We cannot remove this edge");

        if (a.getShallowestNode() == _root)
            throw new RuntimeException("We do not allow removing an artificial arc.");

        int index = a.get_index();
        if (index >= _numArcs)
            throw new RuntimeException("Incompatile index");
        int lastIndex = _numArcs-1;
        _arcs[index] = _arcs[lastIndex];
        _arcs[index].set_index(index);
        _arcs[lastIndex]=null;
        _numArcs--;

        // remove arc a from the NonTree list
        this.removeArcFromNonTreeArcsList(a);
    }

    /**
     * Add arc keeping the infra-structure of the algorithm.
     * The minCapacity must be zero. Before calling nextIteration
     * We need to sort the arcs.
     */
    public Arc addArcKeepingTheFlow(Node tail, Node head, int cost, int maxCapacity) {
        if (_numArcs == _arcs.length)
            throw new NetworkException();
        int index = _numArcs;
        Arc a = new Arc(index, tail, head, cost, 0, maxCapacity);
        _arcs[_numArcs++] = a;
        a.set_index(_numArcs-1);

        // put arc a on the list
        this.addArcToNonTreeArcsList(a);

        return a;
    }

    /**
     * @param e Arc entering on tree arc
     * @param f Arc exiting of tree arc
     */
    private void updateTreeAndNonTreeArcs(Arc e, Arc f) {

        // update
        e.set_type(Arc.ARC_TYPE_TREE);
        f.set_type(Arc.ARC_TYPE_NON_TREE);

        /*
        System.out.println("Nova Aresta de Arvore: " +
                           e.get_tail().get_index() + "->" +
                           e.get_head().get_index());
        System.out.println("Nova Aresta fora de Arvore: " +
                           f.get_tail().get_index() + "->" +
                           f.get_head().get_index());
        */

        Arc ep = e.get_previousArcOfTheSameType();
        Arc en = e.get_nextArcOfTheSameType();
        ep.set_nextArcOfTheSameType(f);
        en.set_previousArcOfTheSameType(f);
        f.set_previousArcOfTheSameType(ep);
        f.set_nextArcOfTheSameType(en);

        e.set_nextArcOfTheSameType(null);
        e.set_previousArcOfTheSameType(null);

        _firstNonTreeArc = f.get_nextArcOfTheSameType();

        if (_firstNonTreeArc.get_nextArcOfTheSameType() == null || _firstNonTreeArc.get_previousArcOfTheSameType() == null)
            _firstNonTreeArc.set_nextArcOfTheSameType(null);

    }

    private void removeArcFromNonTreeArcsList(Arc a) {
        if (a.isTreeArc())
            throw new NetworkException("Tree Arc cannot be removed from Non-Tree arcs list");
        Arc p = a.get_previousArcOfTheSameType();
        Arc n = a.get_nextArcOfTheSameType();
        p.set_nextArcOfTheSameType(n);
        n.set_previousArcOfTheSameType(p);
        a.set_nextArcOfTheSameType(null);
        a.set_previousArcOfTheSameType(null);


        // lista com um unico elemento
        if (n == a) {
            _firstNonTreeArc = null;
        }

        // se a cabeça da lista é a entao a cabeca passa a ser o next de a
        else if (_firstNonTreeArc == a) {
            _firstNonTreeArc = n;
        }
    }

    private void addArcToNonTreeArcsList(Arc a) {
        if (a.isTreeArc())
            throw new NetworkException("Tree Arc cannot be inserted into Non-Tree arcs list");

        if (_firstNonTreeArc == null) {
            _firstNonTreeArc = a;
            _firstNonTreeArc.set_nextArcOfTheSameType(_firstNonTreeArc);
            _firstNonTreeArc.set_previousArcOfTheSameType(_firstNonTreeArc);
        }
        else {
            Arc n = _firstNonTreeArc;
            Arc p = _firstNonTreeArc.get_previousArcOfTheSameType();
            a.set_nextArcOfTheSameType(n);
            n.set_previousArcOfTheSameType(a);
            a.set_previousArcOfTheSameType(p);
            p.set_nextArcOfTheSameType(a);
            _firstNonTreeArc = a;
        }
    }

    public void printNonTreeArcsList() {
        System.out.println("Non tree arcs list: ");
        Arc a = _firstNonTreeArc;
        if (a == null)
            return;
        do {
            System.out.println("" + a.getHeadTailString());
            a = a.get_nextArcOfTheSameType();
        } while (a != _firstNonTreeArc);
    }


    public static Network getDefaultNetwork() {
        Network n = new Network(8,16);

        // add the nodes on the network
        Node[] v = new Node[8];
        v[0] = n.addNode(1);
        v[1] = n.addNode(3);
        v[2] = n.addNode(-4);
        v[3] = n.addNode(8);
        v[4] = n.addNode(0);
        v[5] = n.addNode(-5);
        v[6] = n.addNode(-14);
        v[7] = n.addNode(11);

        // add the arcs on the network
        n.addArc(v[0],v[1],-2,4);
        n.addArc(v[0],v[3],-2,2);
        n.addArc(v[1],v[4],2,3);
        n.addArc(v[2],v[0],6,3);
        n.addArc(v[2],v[1],5,2);
        n.addArc(v[2],v[4],4,6);
        n.addArc(v[3],v[2],2,4);
        n.addArc(v[4],v[7],3,5);
        n.addArc(v[5],v[3],-3,2);
        n.addArc(v[5],v[4],3,2);
        n.addArc(v[5],v[6],0,3);
        n.addArc(v[5],v[7],2,3);
        n.addArc(v[6],v[0],6,2);
        n.addArc(v[6],v[3],3,8);
        n.addArc(v[6],v[7],5,4);
        n.addArc(v[7],v[1],0,4);

        //
        return n;
    }

    public boolean checkArcsList() {
        for (int i=0;i<_numArcs-1;i++) {
            Arc a = _arcs[i];
            Arc b = _arcs[i+1];
            if (a.get_tail().get_index() > b.get_tail().get_index() ||
                (a.get_tail().get_index() == b.get_tail().get_index() && a.get_head().get_index() > b.get_head().get_index()))
                return false;
        }
        return true;
    }
}
