package blink;

import java.awt.Dimension;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

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
public class SearchAttractor {
    Gem _gem;
    long _maxTime;
    long _startingTime;

    private Graph _G;

    private HashMap<Gem,Vertex> _map;

    Gem _currentGem;

    Gem _bestGem = null;
    int _bestGemTSClassSize;
    boolean _bestGemIsTSClassRepresentant;

    Random _random = new Random();

    public SearchAttractor(Gem g, long maxTime) {
        _gem = g;
        _maxTime = maxTime;

        // create graph and mapping
        _G = new SparseGraph();
        _map = new HashMap<Gem,Vertex>();

        // insert the root node on the reduction graph
        Vertex root = _G.addVertex(new SparseVertex());
        root.setUserDatum("key",_gem,UserData.SHARED);
        _map.put(_gem,root);

        _startingTime = System.currentTimeMillis();
        _currentGem = _gem;


        while (true) {

            // System.out.println("Starting again...");

            int tResult;

            while (true) {
                // try to simplify current gem
                S();

                // try to find a simplifying gem
                // on the same ts-class
                tResult = T();

                // maybe a timeout
                if (tResult != T_FOUND_SIMPLIFICATION)
                    break;
            }

            if (tResult != T_SAMEPATH) {
                // update best gem found if it is the case
                if (_bestGem == null) {
                    // System.out.println("First bestGem");
                    // System.out.println("Current: "+_currentGem.getCurrentLabelling().getLettersString(","));
                    _bestGem = _currentGem;
                    _bestGemTSClassSize = _currentTSClass.size();
                    _bestGemIsTSClassRepresentant = (tResult == T_NO_SIMPLIFICATION ? true : false);
                } else if (_bestGem.getNumVertices() > _currentGem.getNumVertices() ||
                           _bestGem.compareTo(_currentGem) > 0) {
                    // System.out.println("Change bestGem");
                    // System.out.println("Current: "+_currentGem.getCurrentLabelling().getLettersString(","));
                    // System.out.println("Best: "+_currentGem.getCurrentLabelling().getLettersString(","));
                    _bestGem = _currentGem;
                    _bestGemTSClassSize = _currentTSClass.size();
                    _bestGemIsTSClassRepresentant = (tResult == T_NO_SIMPLIFICATION ? true : false);
                }
            }
            else {
                _currentGem = _bestGem;
            }

            // put on 02/11/06 o speed up. once we get one
            // correct gem we are done.
            // if found some tsClassRepresentant get out!!
            if (_bestGem != null && _bestGemIsTSClassRepresentant)
                break;

            // no more time
            if (tResult == T_TIMEOUT || System.currentTimeMillis() - _startingTime > _maxTime)
                break;

            // apply u-Move
            boolean uResult = U();

            if (uResult == false)
                break; // this does not happen

            // apply u-Move
            uResult = U();

            if (uResult == false)
                break; // this does not happen

        }

        //
        this.tagWinningPath();

    }

    /**
     * Search TS-Class for some simplification
     * point (TS-Move that creates space for dipole
     * cancelation or rho moves).
     */
    private static final int T_FOUND_SIMPLIFICATION = 1;
    private static final int T_NO_SIMPLIFICATION = 2;
    private static final int T_TIMEOUT = 3;
    private static final int T_SAMEPATH = 4;
    private HashSet<Gem> _currentTSClass = new HashSet<Gem>();
    private int T() {
        // System.out.println("Running T");

        // System.out.println("Processing:\n"+l.getLettersString(' '));
        Gem g = _currentGem;

        // to search on the TS-Class the labelling
        // must be normalized, so the first step
        // here is to normalize the labelling of
        // current gem.
        {
            // get this gem's vertex
            Vertex uu = _map.get(g);

            // make g a copy of g labelled with the code labelling
            g = g.copy();
            g.goToCodeLabel();

            // got to an already existing path
            // if it is the first time this occurs
            // (bestGem == null) then we need to continue...
            if (_bestGem != null &&  _map.get(g) != null) {
                return T_SAMEPATH;
            }

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

            // change current gem
            _currentGem = g;
        }


        // for robustness, test if the given gem
        // has a simplification point.
        {
            Dipole d = g.findAnyDipole();
            if (d != null) {
                // System.out.println("Found " + d);
                return T_FOUND_SIMPLIFICATION;
            }
            RhoPair r3 = g.findAnyRho3Pair();
            if (r3 != null) {
                // System.out.println("Found " + r3);
                return T_FOUND_SIMPLIFICATION;
            }
            RhoPair r2 = g.findAnyRho2Pair();
            if (r2 != null) {
                // System.out.println("Found " + r2);
                return T_FOUND_SIMPLIFICATION;
            }
        } // test if given input gem has not simplifying point

        // place to store all different gems on the tsClass
        _currentTSClass = new HashSet<Gem>();
        ArrayList<Gem> _unprocessed = new ArrayList<Gem>();

        _unprocessed.add(g);
        _currentTSClass.add(g);

        TSMoveType[] types = TSMoveType.values(); // {TSMoveType.TS5,TSMoveType.TS6};

        boolean timeout = false;

        while (!_unprocessed.isEmpty()) {

            int N = _unprocessed.size();
            int k = _random.nextInt(N);
            Gem g0 = _unprocessed.get(k);
            _unprocessed.set(k,_unprocessed.get(N-1));
            _unprocessed.remove(N-1);

            // get vertex of current labelling
            Vertex u = _map.get(g0);

            for (GemVertex v: g0.getVertices()) {

                // check for timeout
                timeout = (System.currentTimeMillis()-_startingTime) > _maxTime;
                if (timeout)
                    break;
                // check for timeout;

                for (GemColor[] p: GemColor.PERMUTATIONS) {
                    for (TSMoveType type: types) {
                        if (g0.isTSMovePoint(v, p, type)) {

                            TSMovePoint m = new TSMovePoint(v.getLabel(), p, type);

                            // System.out.println("Trying: "+m.toString());

                            Gem candidate = g0.copy();
                            candidate.applyTSMove(m);
                            candidate.goToCodeLabel();

                            // System.out.println(""+candidate.getCurrentLabelling().getLettersString(',')+" tem simplificação: ");

                            // if (candidate.getAgemality() != 0)
                            //    throw new RuntimeException("Oooooppppssss");

                            if (!_currentTSClass.contains(candidate)) {

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
                                    // System.out.println(""+candidate.getCurrentLabelling().getLettersString(",")+" tem simplificação: ");
                                    // System.out.println("Found "+d);

                                    _currentGem = candidate;
                                    return T_FOUND_SIMPLIFICATION;
                                }
                                RhoPair r3 = candidate.findAnyRho3Pair();
                                if (r3 != null) {
                                    // System.out.println(""+candidate.getCurrentLabelling().getLettersString(",")+" tem simplificação: ");
                                    // System.out.println("Found "+r3);

                                    _currentGem = candidate;
                                    return T_FOUND_SIMPLIFICATION;
                                }
                                RhoPair r2 = candidate.findAnyRho2Pair();
                                if (r2 != null) {
                                    // System.out.println(""+candidate.getCurrentLabelling().getLettersString(",")+" tem simplificação: ");
                                    // System.out.println("Found "+r2);

                                    _currentGem = candidate;
                                    return T_FOUND_SIMPLIFICATION;
                                }

                                // System.out.println("NÃO");
                                _currentTSClass.add(candidate);
                                _unprocessed.add(candidate);
                            }
                        } // end of found a ts-move point
                    } // end of ts-move types
                } // end of permutations
            } // end of vertices
        } // ts-class closure had no simplification


        // make the maximum gem found to be the one.
        Gem min = null;
        for (Gem gg: _currentTSClass) {
            if (min == null) min = gg;
            else if (gg.compareTo(min) < 0) min = gg;
        }
        _currentGem = min;

        if (timeout) // this means that we did not search all the class
            return T_TIMEOUT;
        else // this means we finished searchin all the class
            return T_NO_SIMPLIFICATION;
    }

    /**
     * Greedy Simplification of _currentGem by
     * dipole cancelations.
     */
    private boolean S() {
        // System.out.println("Running S");

        // make gem to be current gem
        Gem gem = _currentGem;

        boolean simplified = false;

        boolean foundRhoMove = false;

        // try to simplify gem
        while (true) {

            // System.out.println("HG: "+gem.homologyGroup().toString());

            // get reduction graph vertex for "gem"
            Vertex u = _map.get(gem);

            // LOG ----------------------------------
            // System.out.println("\nNum vertices: " + gem.getNumVertices()
            //+ "   Gem: "+  gem.getExpandedWord()*/ );
            // System.out.println("Agemality: " + gem.getAgemality());
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
            // if (gem.getAgemality() != 0) {
            //    debug(u);
            //    throw new RuntimeException("oooooppppsssssss");
            // }

            // create a copy because the dipole and rho
            // operands are structure dependent
            gem = gem.copy();

            // simplify dipole if there exists one
            Dipole d = gem.findAnyDipole();
            if (d != null) {
                // System.out.println("<SIMPLIFICATION> Found dipole cancelation: " + d.toString());

                Move move = new DipoleMove(d.getU().getLabel(), d.getColors());

                // String st = "D"+d.size()+" "+
                //           GemColor.getColorSetCompactString(d.getColorSet())+" "+
                //           d.getU().getOriginalLabel()+" "+d.getV().getOriginalLabel();

                // alter gem (but keep the field originalLabel
                // of the vertices intact) by removing the
                // dipole d from it.
                gem.cancelDipole(d);

                // check result to see if it is
                // already on the graph
                if (_map.get(gem) != null)
                    return false;

                // add new vertex to the reduction graph
                Vertex v = _G.addVertex(new SparseVertex());
                v.setUserDatum("key", gem, UserData.SHARED);

                // save a link from current gem to this new
                // reduction graph vertex.
                _map.put(gem, v);

                // add an edge from parent gem to this new
                // simplified gem
                Edge e = _G.addEdge(new DirectedSparseEdge(u, v));
                e.setUserDatum("key", move, UserData.SHARED);

                // turn on simplified flag
                simplified = true;
                foundRhoMove = false;
                continue;
            }
            else if (foundRhoMove) {
                debug(u);
                throw new RuntimeException("Ooooopppsssss");
            }

            // find any rho 3 pair
            RhoPair r3 = gem.findAnyRho3Pair();
            if (r3 != null) {
                // System.out.println("<SIMPLIFICATION> Found " + r3.toString() + ". Applying it...");

                Move move = new RhoMove(r3.getU().getLabel(),
                                        r3.getV().getLabel(),
                                        r3.getColor(),
                                        r3.foundAsA());
                //String st = "R"+r3.foundAsA()+GemColor.getColorSetCompactString(r3.getColor())+" "+r3.getU().getOriginalLabel()+" "+r3.getV().getOriginalLabel();

                // alter gem (but keep the field originalLabel
                // of the vertices intact) by removing the
                // dipole d from it.
                gem.applyRhoPair(r3);

                // check result to see if it is
                // already on the graph
                if (_map.get(gem) != null)
                    return false;

                // add new vertex to the reduction graph
                Vertex v = _G.addVertex(new SparseVertex());
                v.setUserDatum("key", gem, UserData.SHARED);

                // save a link from current gem to this new
                // reduction graph vertex.
                _map.put(gem, v);

                // add an edge from parent gem to this new
                // simplified gem.
                Edge e = _G.addEdge(new DirectedSparseEdge(u, v));
                e.setUserDatum("key", move, UserData.SHARED);

                // found rho move
                foundRhoMove = true;

                // the next iteration should find a dipole
                continue;
            }

            // find any rho 2 pair
            RhoPair r2 = gem.findAnyRho2Pair();
            if (r2 != null) {
                // System.out.println("<SIMPLIFICATION> Found " + r2.toString() + ". Applying it...");

                Move move = new RhoMove(r2.getU().getLabel(),
                                        r2.getV().getLabel(),
                                        r2.getColor(),
                                        r2.foundAsA());

                //String st = "R"+r2.foundAsA()+GemColor.getColorSetCompactString(r2.getColor())+" "+r2.getU().getOriginalLabel()+" "+r2.getV().getOriginalLabel();

                // alter gem (but keep the field originalLabel
                // of the vertices intact) by removing the
                // dipole d from it.
                gem.applyRhoPair(r2);

                // check result to see if it is
                // already on the graph
                if (_map.get(gem) != null)
                    return false;

                // add new vertex to the reduction graph
                Vertex v = _G.addVertex(new SparseVertex());
                v.setUserDatum("key", gem, UserData.SHARED);

                // save a link from current gem to this new
                // reduction graph vertex.
                _map.put(gem, v);

                // add an edge from parent gem to this new
                // simplified gem.
                Edge e = _G.addEdge(new DirectedSparseEdge(u, v));
                e.setUserDatum("key", move, UserData.SHARED);

                // found rho move
                foundRhoMove = true;

                // the next iteration should find a dipole
                continue;
            }

            // no rho and no dipole cancelation!
            break;
        }

        if (simplified) {
            _currentGem = gem;
        }

        return simplified;
    }


    private boolean U() {
        // System.out.println("Running U");

        // System.out.println("Processing:\n"+l.getLettersString(' '));
        Gem g = _currentGem;

        // get this gem's vertex
        Vertex uu = _map.get(g);

        // make g a copy of g labelled with the code labelling
        g = g.copy();
        ArrayList<Monopole> monopoles = g.findMonopoles();

        if (monopoles.size() == 0)
            return false;

        Monopole monopole = monopoles.get(_random.nextInt(monopoles.size()));

        //m.get
        Move move = new UMove(
                monopole.getVertex().getLabel(),
                monopole.getColor());

        g.uMove(monopole);

        // add new vertex on the reduction graph
        Vertex vv = _G.addVertex(new SparseVertex());
        vv.setUserDatum("key", g, UserData.SHARED);

        // create link from g to this new vertex
        _map.put(g, vv);

        // add an edge from parent gem to this new simplified gem
        Edge e = _G.addEdge(new DirectedSparseEdge(uu, vv));
        e.setUserDatum("key", move, UserData.SHARED);

        // change current gem
        _currentGem = g;

        return true;
    }

    public Gem getBestAttractorFound() {
        String code;
        try {
            code = _bestGem.getCurrentLabelling().getLettersString(",");
        } catch (Exception ex) {
            code = "";
        }
        // System.out.println("Best Attractor Found: "+code);



        return _bestGem;
    }

    public int getBestAttractorTSClassSize() {
        return _bestGemTSClassSize;
    }

    /**
     * if isBestAttractorTSClassRepresentant() == true then
     * this number is the number trully the TS-Class size.
     * Else this number is a partial number of the
     * TSClassSize.
     */
    public boolean isBestAttractorTSClassRepresentant() {
        return _bestGemIsTSClassRepresentant;
    }

    /**
     * Return search graph with the winning path tagged
     */
    public Graph getGraph() {
        return _G;
    }

    public static void main(String[] args) {
        // Blink b2 = new Blink(new int[]{6,14,2,9,4,8,5,12,7,13,10,1,11,3},81);
        // Gem g2 = (new GemFromBlink(b2)).getGem();
        //Gem g2 = new Gem(3,5,2,1);
        // Gem g2 = new Gem(new GemPackedLabelling("dabcgefjhimkljmledchgkaifbkfihmjalcbgde"));
        // Gem g2 = new Gem(new GemPackedLabelling("dabcgefjhimklpnosqrutwvjpredmhgwacqviluoktsbfnlqonuiaprtjgdmcvwfhkesb"));
        Gem g2 = new Gem(new GemPackedLabelling("fabcdeighkjnlmoppinomgfjbhlkecdanhpglokcaimfdjeb"));

        SearchAttractor A = new SearchAttractor(g2,1000L);

        PanelReductionGraph prg = new PanelReductionGraph(A.getGraph());

        // desenhar o mapa
        JFrame f = new JFrame("Reduction Graph");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(new Dimension(1024,768));
        f.setContentPane(prg);
        f.setVisible(true);
        // desenhar o mapa
    }


    private void debug(Vertex u) {
        Edge e = (Edge) u.getInEdges().iterator().next();
        Vertex uPred = e.getOpposite(u);
        Gem g1 = (Gem) u.getUserDatum("key");
        try {
            PrintWriter ps = new PrintWriter(new FileWriter("c:/nodipole.gem"));
            ps.println(g1.getExpandedWord());
            ps.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }


        Gem g0 = (Gem) uPred.getUserDatum("key");

        Move m = (Move) e.getUserDatum("key");

        JTabbedPane t = new JTabbedPane();
        t.add("Before "+m.getSignature(),new PanelGemViewer(g0));
        t.add("After "+m.getSignature(),new PanelGemViewer(g1));

        // desenhar o mapa
        JFrame f = new JFrame("Reduction Graph");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(new Dimension(1024,768));
        f.setContentPane(t);
        f.setVisible(true);
        // desenhar o mapa
    }

    private void tagWinningPath() {
        // tag vertices from representant backwards...
        Vertex v = _map.get(_bestGem);
        int countPath = 0;
        while (true) {
            countPath++;
            v.setUserDatum("onPath",true,UserData.SHARED);
            Set s = v.getInEdges();
            if (s.isEmpty())
                break;
            Edge e = (Edge) s.iterator().next();
            Vertex u = e.getOpposite(v);
            v = u;
        }
        // System.out.println("Path: "+countPath);
    }

    public Path getBestPath() {
        // tag vertices from representant backwards...
        Path result = new Path();
        Vertex v = _map.get(_bestGem);
        int countPath = 0;
        while (true) {
            countPath++;
            v.setUserDatum("onPath",true,UserData.SHARED);
            Set s = v.getInEdges();
            if (s.isEmpty())
                break;
            Edge e = (Edge) s.iterator().next();
            result.addMove((Move)e.getUserDatum("key"));

            Vertex u = e.getOpposite(v);
            v = u;
        }
        result.reverse();
        return result;
    }

}
