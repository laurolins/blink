package blink;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import edu.uci.ics.jung.graph.Edge;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.graph.impl.DirectedSparseEdge;
import edu.uci.ics.jung.graph.impl.SparseGraph;
import edu.uci.ics.jung.graph.impl.SparseVertex;
import edu.uci.ics.jung.utils.UserData;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class CalculateReductionGraph {
    private Graph _G;
    private HashMap<Gem,Vertex> _map;
    private Gem _representant;
    private ArrayList<Gem> _unprocessed = new ArrayList<Gem>();

    private int _tsClassSize = -1;

    public int getTSClassSize() {
        return _tsClassSize;
    }

    public CalculateReductionGraph(Gem gem) {
        // create a copy to protect the given gem
        _representant = gem.copy();

        // go to code label. every gem on the
        // reduction graph is in it's code labelling
        _representant.goToCodeLabel();

        // notice that all gems that will be found are
        // descendants of _representant, so every one
        // will have it's vertices tagged with the
        // current labels.
        _representant.copyCurrentLabellingToOriginalLabelling();

        _unprocessed.add(_representant);

        // create graph and mapping
        _G = new SparseGraph();
        _map = new HashMap<Gem,Vertex>();

        // insert the root node on the reduction graph
        Vertex root = _G.addVertex(new SparseVertex());
        root.setUserDatum("key",_representant,UserData.SHARED);
        _map.put(_representant,root);

        // process
        process();
    }

    public Gem findSimplifyingGemOnTheTSClass(Gem g) {
        // System.out.println("Processing:\n"+l.getLettersString(' '));


        {
            // get this gem's vertex
            Vertex uu = _map.get(g);

            // make g a copy of g labelled with the code labelling
            g = g.copy();
            g.goToCodeLabel();


            //
            Move move = new RelabelMove(
                    g.getLastGoToCodeLabellingRootVertexLabel(),
                    g.getLastGoToCodeLabellingColorsPermutation()
            );

            // add new vertex on the reduction graph
            Vertex vv = _G.addVertex(new SparseVertex());
            vv.setUserDatum("key", g, UserData.SHARED);

            // create link from g to this new vertex
            _map.put(g, vv);

            // add an edge from parent gem to this new simplified gem
            Edge e = _G.addEdge(new DirectedSparseEdge(uu, vv));
            e.setUserDatum("key", move, UserData.SHARED);
        }

        { // test if given input gem has not simplifying point
            Dipole d = g.findAnyDipole();
            if (d != null) {
                System.out.println("Found " + d);
                return g;
            }
            RhoPair r3 = g.findAnyRho3Pair();
            if (r3 != null) {
                System.out.println("Found " + r3);
                return g;
            }
            RhoPair r2 = g.findAnyRho2Pair();
            if (r2 != null) {
                System.out.println("Found " + r2);
                return g;
            }
        } // test if given input gem has not simplifying point

        HashSet<Gem> _set = new HashSet<Gem>();
        ArrayList<Gem> _unprocessed = new ArrayList<Gem>();

        _unprocessed.add(g);
        _set.add(g);

        TSMoveType[] types = TSMoveType.values(); // {TSMoveType.TS5,TSMoveType.TS6};

        Random r = new Random(15L);

        while (!_unprocessed.isEmpty()) {

            int N = _unprocessed.size();
            int k = r.nextInt(N);
            Gem g0 = _unprocessed.get(k);
            _unprocessed.set(k,_unprocessed.get(N-1));
            _unprocessed.remove(N-1);

            // get vertex of current labelling
            Vertex u = _map.get(g0);

            for (GemVertex v: g0.getVertices()) {
                for (GemColor[] p: GemColor.PERMUTATIONS) {
                    for (TSMoveType type: types) {
                        if (g0.isTSMovePoint(v, p, type)) {

                            TSMovePoint m = new TSMovePoint(v.getLabel(), p, type);

                            Gem candidate = g0.copy();
                            candidate.applyTSMove(m);
                            candidate.goToCodeLabel();

                            // if (candidate.getAgemality() != 0)
                            //    throw new RuntimeException("Oooooppppssss");

                            if (!_set.contains(candidate)) {

                                // create move to put on edge
                                Move move = new TSMove(m.getA(),m.getP(),m.getType());

                                // add new vertex and connect it
                                Vertex newV = _G.addVertex(new SparseVertex());
                                newV.setUserDatum("key",candidate,UserData.SHARED);
                                _map.put(candidate,newV);

                                Edge e = _G.addEdge(new DirectedSparseEdge(u,newV));
                                e.setUserDatum("key",move,UserData.SHARED);

                                // check if it simplifies
                                Dipole d = candidate.findAnyDipole();
                                if (d != null) {
                                    System.out.println(""+candidate.getCurrentLabelling().getLettersString(",")+" tem simplificação: ");
                                    System.out.println("Found "+d);
                                    return candidate;
                                }
                                RhoPair r2 = candidate.findAnyRho2Pair();
                                if (r2 != null) {
                                    System.out.println(""+candidate.getCurrentLabelling().getLettersString(",")+" tem simplificação: ");
                                    System.out.println("Found "+r2);
                                    return candidate;
                                }
                                RhoPair r3 = candidate.findAnyRho3Pair();
                                if (r3 != null) {
                                    System.out.println(""+candidate.getCurrentLabelling().getLettersString(",")+" tem simplificação: ");
                                    System.out.println("Found "+r3);
                                    return candidate;
                                }

                                // System.out.println("NÃO");
                                _set.add(candidate);
                                _unprocessed.add(candidate);
                            }
                        }
                    }
                }
            }
        }
        return null;
    }


    public Gem generateTheTSClass(Gem g) {
        // System.out.println("Processing:\n"+l.getLettersString(' '));
        g.goToCodeLabel();

        HashSet<Gem> _set = new HashSet<Gem>();
        ArrayList<Gem> _unprocessed = new ArrayList<Gem>();

        _unprocessed.add(g);
        _set.add(g);

        TSMoveType[] types = TSMoveType.values(); // {TSMoveType.TS5,TSMoveType.TS6};

        Random r = new Random(15L);

        while (!_unprocessed.isEmpty()) {

            int N = _unprocessed.size();
            int k = r.nextInt(N);
            Gem g0 = _unprocessed.get(k);
            _unprocessed.set(k,_unprocessed.get(N-1));
            _unprocessed.remove(N-1);

            // get vertex of current labelling
            Vertex u = _map.get(g0);

            for (GemVertex v: g0.getVertices()) {
                for (GemColor[] p: GemColor.PERMUTATIONS) {
                    for (TSMoveType type: types) {
                        if (g0.isTSMovePoint(v, p, type)) {

                            TSMovePoint m = new TSMovePoint(v.getLabel(), p, type);

                            String st = ""+m.getType()+" "+GemColor.getColorSetCompactString(m.getP())+" "+m.getA();

                            Gem candidate = g0.copy();
                            candidate.applyTSMove(m);
                            candidate.goToCodeLabel();

                            if (candidate.getAgemality() != 0)
                                throw new RuntimeException("Oooooppppssss");

                            if (!_set.contains(candidate)) {

                                // create move to put on edge
                                Move move = new TSMove(m.getA(),m.getP(),m.getType());

                                // add new vertex and connect it
                                Vertex newV = _G.addVertex(new SparseVertex());
                                newV.setUserDatum("key",candidate,UserData.SHARED);
                                _map.put(candidate,newV);

                                Edge e = _G.addEdge(new DirectedSparseEdge(u,newV));
                                e.setUserDatum("key",move,UserData.SHARED);

                                // System.out.println("NÃO");
                                _set.add(candidate);
                                _unprocessed.add(candidate);
                            }
                        }
                    }
                }
            }
        }
        ArrayList<Gem> list = new ArrayList<Gem>(_set);
        Collections.sort(list);
        _tsClassSize = _set.size();
        return list.get(0);
    }

    public void process() {

        while (!_unprocessed.isEmpty()) {

            // get the current unprocessed gem
            // take care to not mess up with
            // it's main structure and labelling
            Gem gem = _unprocessed.get(_unprocessed.size()-1);
            _unprocessed.remove(_unprocessed.size()-1);

            boolean simplified = false;

            // try to simplify gem
            while (true) {

                // System.out.println("HG: "+gem.homologyGroup().toString());

                // get reduction graph vertex for "gem"
                Vertex u = _map.get(gem);

                // LOG ----------------------------------
                System.out.println("\nProcessing: "+gem.getExpandedWord()+" num vertices: "+gem.getNumVertices());
                // System.out.println("Agemality: "+gem.getAgemality());
                // System.out.println("" + gem.getStringWithNeighbours());
                // System.out.println("" + gem.getCurrentLabelling().getIntegersString('\n'));
                // System.out.println(gem.getCurrentLabelling().getLettersString(','));
                try {
                    // PrintStream s = new PrintStream(new FileOutputStream("c:/program files/pigale/tgf/aaa.txt"));
                    // gem.getCurrentLabelling().generatePIGALE(s);
                    // s.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
                // LOG ----------------------------------

                // check to see if it is a valid gem (later, this should be commented)
                if (gem.getAgemality() != 0)
                    throw new RuntimeException("oooooppppsssssss");

                // create a copy because the dipole and rho
                // operands are structure dependent
                gem = gem.copy();

                // simplify dipole if there exists one
                Dipole d = gem.findAnyDipole();
                if (d != null) {
                    System.out.println("<SIMPLIFICATION> Found dipole cancelation: "+d.toString());

                    Move move = new DipoleMove(d.getU().getLabel(),d.getColors());

                    // String st = "D"+d.size()+" "+
                    //           GemColor.getColorSetCompactString(d.getColorSet())+" "+
                    //           d.getU().getOriginalLabel()+" "+d.getV().getOriginalLabel();

                    // alter gem (but keep the field originalLabel
                    // of the vertices intact) by removing the
                    // dipole d from it.
                    gem.cancelDipole(d);

                    // add new vertex to the reduction graph
                    Vertex v = _G.addVertex(new SparseVertex());
                    v.setUserDatum("key",gem,UserData.SHARED);

                    // save a link from current gem to this new
                    // reduction graph vertex.
                    _map.put(gem,v);

                    // add an edge from parent gem to this new
                    // simplified gem
                    Edge e = _G.addEdge(new DirectedSparseEdge(u,v));
                    e.setUserDatum("key",move,UserData.SHARED);

                    // turn on simplified flag
                    simplified = true;
                    continue;
                }

                // find any rho 3 pair
                RhoPair r3 = gem.findAnyRho3Pair();
                if (r3 != null) {
                    System.out.println("<SIMPLIFICATION> Found "+r3.toString()+". Applying it...");

                    Move move = new RhoMove(r3.getU().getLabel(),
                                            r3.getV().getLabel(),
                                            r3.getColor(),
                                            r3.foundAsA());
                    //String st = "R"+r3.foundAsA()+GemColor.getColorSetCompactString(r3.getColor())+" "+r3.getU().getOriginalLabel()+" "+r3.getV().getOriginalLabel();

                    // alter gem (but keep the field originalLabel
                    // of the vertices intact) by removing the
                    // dipole d from it.
                    gem.applyRhoPair(r3);

                    // add new vertex to the reduction graph
                    Vertex v = _G.addVertex(new SparseVertex());
                    v.setUserDatum("key",gem,UserData.SHARED);

                    // save a link from current gem to this new
                    // reduction graph vertex.
                    _map.put(gem,v);

                    // add an edge from parent gem to this new
                    // simplified gem.
                    Edge e = _G.addEdge(new DirectedSparseEdge(u,v));
                    e.setUserDatum("key",move,UserData.SHARED);

                    // the next iteration should find a dipole
                    continue;
                }

                // find any rho 2 pair
                RhoPair r2 = gem.findAnyRho2Pair();
                if (r2 != null) {
                    System.out.println("<SIMPLIFICATION> Found "+r2.toString()+". Applying it...");

                    Move move = new RhoMove(r2.getU().getLabel(),
                                            r2.getV().getLabel(),
                                            r2.getColor(),
                                            r2.foundAsA());

                    //String st = "R"+r2.foundAsA()+GemColor.getColorSetCompactString(r2.getColor())+" "+r2.getU().getOriginalLabel()+" "+r2.getV().getOriginalLabel();

                    // alter gem (but keep the field originalLabel
                    // of the vertices intact) by removing the
                    // dipole d from it.
                    gem.applyRhoPair(r2);

                    // add new vertex to the reduction graph
                    Vertex v = _G.addVertex(new SparseVertex());
                    v.setUserDatum("key",gem,UserData.SHARED);

                    // save a link from current gem to this new
                    // reduction graph vertex.
                    _map.put(gem,v);

                    // add an edge from parent gem to this new
                    // simplified gem.
                    Edge e = _G.addEdge(new DirectedSparseEdge(u,v));
                    e.setUserDatum("key",move,UserData.SHARED);

                    // the next iteration should find a dipole
                    continue;
                }

                break;
            }

            if (!simplified)
                System.out.println("No simplification found");

            // gem.goToCodeLabel();
            // System.out.println(""+gem.getStringWithNeighbours());
            // System.out.println(""+gem.goToCodeLabel().getLettersString(','));

            System.out.println("Searching TS-class of " + gem.getExpandedWord());
            Gem gemSimp = this.findSimplifyingGemOnTheTSClass(gem);

            // finished the search
            _unprocessed.clear();
            if (gemSimp != null) {
                System.out.println("Found " + gemSimp.getCurrentLabelling().getLettersString(","));
                System.out.println("Storing gem "+gemSimp);
                _unprocessed.add(gemSimp);
            }
            else {
                _representant = generateTheTSClass(gem);
            }
        }

        // tag vertices from representant backwards...
        Vertex v = _map.get(_representant);
        while (true) {
            v.setUserDatum("onPath",true,UserData.SHARED);
            Set s = v.getInEdges();
            if (s.isEmpty())
                break;
            Edge e = (Edge) s.iterator().next();
            Vertex u = e.getOpposite(v);
            v = u;
        }
    }

    public Graph getGraph() {
        return _G;
    }

    public Gem getRepresentant() {
        return _representant;
    }

}





/**
 * Move is the abstract class of all kind of
 * moves that may be a applied to a gem. The
 * idea is that given a initial gem and a
 * sequence of moves we can rebuild the last
 * gem.
 */
abstract class Move {
    public abstract String getSignature();
}

class DipoleMove extends Move {
    int _u;
    GemColor[] _colors; // sharpening color
    public DipoleMove(int u, GemColor ...colors) {
        _u = u;
        _colors = colors;
    }

    public int getComplementColorSet() {
        return GemColor.getComplementColorSet(_colors);
    }

    public int getColorSet() {
        return GemColor.getColorSet(_colors);
    }


    public GemColor[] getColors() {
        return _colors;
    }

    public GemColor[] getComplementColors() {
        return GemColor.getColorsOfColorSet(this.getComplementColorSet());
    }

    public int getU() {
        return _u;
    }

    public int size() {
        return _colors.length;
    }


    public String getSignature() {
        return String.format("D%d %s %d",
                             this.size(),
                             GemColor.getColorSetCompactString(this.getColorSet()),
                             getU());
    }

    public String toString() {
        String st = "";
        boolean first = true;
        for (GemColor c : _colors) {
            if (!first)
                st += "+";
            st += c;
            first = false;
        }
        return String.format("%d-Dipole on vertices %d with colors %s",
                             this.size(), getU(),st);
    }
}

class TSMove extends Move {
    TSMoveType _type;
    int _a;
    GemColor[] _p;
    public TSMove(int a, GemColor[] p, TSMoveType type) {
        _a = a;
        _p = p;
        _type = type;
    }
    public int getA() {
        return _a;
    }
    public GemColor[] getP() {
        return _p;
    }
    public TSMoveType getType() {
        return _type;
    }

    public String getSignature() {
        return String.format("%s %s %d",
                             _type,
                             GemColor.getColorsCompactString(_p),
                             _a);
    }

    public String toString() {
        return String.format("%5s %5d %8s%8s%8s%8s",_type,_a,_p[0],_p[1],_p[2],_p[3]);
    }
}

class RelabelMove extends Move {
    int _a;
    GemColor[] _p;
    public RelabelMove(int a, GemColor[] p) {
        _a = a;
        _p = p;
    }
    public int getA() {
        return _a;
    }
    public GemColor[] getP() {
        return _p;
    }
    public String getSignature() {
        return String.format("L %s %d",
                             GemColor.getColorsCompactString(_p),
                             _a);
    }
}

class UMove extends Move {
    private int _a;
    private GemColor _color;
    public UMove(int a, GemColor color) {
        _a = a;
        _color = color;
    }
    public int getA() {
        return _a;
    }
    public GemColor getColor() {
        return _color;
    }
    public String getSignature() {
        return String.format("U %s %d",
                             GemColor.getColorsCompactString(_color),_a);
    }
}

class RhoMove extends Move {
    int _u;
    int _v;
    GemColor _color;
    int _foundAsA;
    public RhoMove(int u, int v, GemColor c, int foundAsA) {
        _u = u;
        _v = v;
        _color = c;
        _foundAsA = foundAsA;
    }

    public int getU() {
        return _u;
    }

    public int getV() {
        return _v;
    }

    public GemColor getColor() {
        return _color;
    }

    public int foundAsA() {
        return _foundAsA;
    }

    public String getSignature() {
        return String.format("R%d %s %d %d",_foundAsA,GemColor.getColorsCompactString(_color),_u,_v);
    }

    public String toString() {
        return String.format("Rho %d Pair: %6d %6d %10s", _foundAsA, _u, _v, _color);
    }
}
