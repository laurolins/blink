package blink;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
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
public class GemSimplificationPathFinder {
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

    public GemSimplificationPathFinder(Gem g, int k, long maxTime, int tsClassSize) {
        System.out.println("Start GemSimplificationPathFinder (new map)");

        _gem = g;
        _maxTime = maxTime;

        // create graph and mapping
        _G = new SparseGraph();
        _map = new HashMap<Gem,Vertex>();

        // insert the root node on the reduction graph
        Vertex root = _G.addVertex(new SparseVertex());
        root.setUserDatum("key",_gem,UserData.SHARED);
        putOnMap(_gem,root);

        _startingTime = System.currentTimeMillis();
        _currentGem = _gem;
        _bestGem = _gem;
        _bestGemIsTSClassRepresentant = true;
        _bestGemTSClassSize = tsClassSize;

        for (int i = 0; i < k; i++)
            U();

        // simplify and go to ts-class
        // representant
        ST();

        //
        this.tagWinningPath();

    }

    private int _updateBest = 0;
    private void ST() {
        System.out.println("Starting ST...");
        int tResult;
        while (true) {
            // try to simplify current gem
            int sResult = S();
            if (sResult == S_SAMEPATH) {
                tResult = T_SAMEPATH;
                break;
            }

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
                System.out.println("First bestGem");
                System.out.println("Current: "+_currentGem.getCurrentLabelling().getLettersString(","));
                _bestGem = _currentGem;
                _bestGemTSClassSize = _currentTSClass.size();
                _bestGemIsTSClassRepresentant = (tResult == T_NO_SIMPLIFICATION ? true : false);
            } else if (_bestGem.getNumVertices() > _currentGem.getNumVertices() ||
                       _bestGem.compareTo(_currentGem) > 0) {
                System.out.println("Change bestGem");
                System.out.println("Current: "+_currentGem.getCurrentLabelling().getLettersString(","));
                System.out.println("Best: "+_currentGem.getCurrentLabelling().getLettersString(","));
                _bestGem = _currentGem;
                _bestGemTSClassSize = _currentTSClass.size();
                _bestGemIsTSClassRepresentant = (tResult == T_NO_SIMPLIFICATION ? true : false);
                _updateBest++;
            }
        }
        else {
            System.out.println("T SAMEPATH");
            _currentGem = _bestGem;
        }
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
        System.out.println("Running T");

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

            // append node
            this.appendNewNodeToGraph(g,move,uu);

            // change current gem
            _currentGem = g;
        }


        // for robustness, test if the given gem
        // has a simplification point.
        {
            Dipole d = g.findAnyDipole();
            if (d != null) {
                System.out.println("Found " + d);
                return T_FOUND_SIMPLIFICATION;
            }
            RhoPair r3 = g.findAnyRho3Pair();
            if (r3 != null) {
                System.out.println("Found " + r3);
                return T_FOUND_SIMPLIFICATION;
            }
            RhoPair r2 = g.findAnyRho2Pair();
            if (r2 != null) {
                System.out.println("Found " + r2);
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

                                // append to graph only it is not
                                // on the graph yet.
                                if (_map.get(candidate) == null) {

                                    // create move to put on edge
                                    Move move = new TSMove(m.getA(), m.getP(), m.getType());

                                    // append node
                                    this.appendNewNodeToGraph(candidate, move, u);

                                }

                                // check if it simplifies
                                Dipole d = candidate.findAnyDipole();
                                if (d != null) {
                                    System.out.println(""+candidate.getCurrentLabelling().getLettersString(",")+" tem simplificação: ");
                                    System.out.println("Found "+d);

                                    _currentGem = candidate;
                                    return T_FOUND_SIMPLIFICATION;
                                }
                                RhoPair r3 = candidate.findAnyRho3Pair();
                                if (r3 != null) {
                                    System.out.println(""+candidate.getCurrentLabelling().getLettersString(",")+" tem simplificação: ");
                                    System.out.println("Found "+r3);

                                    _currentGem = candidate;
                                    return T_FOUND_SIMPLIFICATION;
                                }
                                RhoPair r2 = candidate.findAnyRho2Pair();
                                if (r2 != null) {
                                    System.out.println(""+candidate.getCurrentLabelling().getLettersString(",")+" tem simplificação: ");
                                    System.out.println("Found "+r2);

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
     * Greedy Simplification of _currentGem by dipole cancelations.
     * if ("simplified" == true) update _currentGem to be the simplified gem.
     */
    private static final int S_SIMPLIFIED = 1;
    private static final int S_SAMEPATH = 2;
    private static final int S_NOSIMPLIFICATION = 3;
    private int S() {
        System.out.println("Running S");

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
            System.out.println("\nNum vertices: " + gem.getNumVertices()
                               /*+ "   Gem: "+  gem.getExpandedWord()*/ );
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
                System.out.println("<SIMPLIFICATION> Found dipole cancelation: " + d.toString());

                Move move = new DipoleMove(d.getU().getLabel(), d.getColors());

                // String st = "D"+d.size()+" "+
                //           GemColor.getColorSetCompactString(d.getColorSet())+" "+
                //           d.getU().getOriginalLabel()+" "+d.getV().getOriginalLabel();

                // alter gem (but keep the field originalLabel
                // of the vertices intact) by removing the
                // dipole d from it.
                gem.cancelDipole(d);

                // check result to see if it is
                // already on the graph (we already
                // have a path of simplification)
                if (_map.get(gem) != null) {
                    /* Object o;
                    Gem key = (Gem) _map.get(gem).getUserDatum("key");
                    System.out.println("gem on map : "+Integer.toHexString(((Object)key).hashCode()));
                    System.out.println("gem simplif: "+Integer.toHexString(((Object)gem).hashCode()));
                    System.out.println("key\n"+key.getStringWithNeighbours());
                    System.out.println("gem\n"+gem.getStringWithNeighbours());
                    System.out.println("key == gem? "+(key == gem)); */
                    return S_SAMEPATH;
                }

                // append node
                this.appendNewNodeToGraph(gem,move,u);

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
                System.out.println("<SIMPLIFICATION> Found " + r3.toString() + ". Applying it...");

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
                    return S_SAMEPATH;

                // append node
                this.appendNewNodeToGraph(gem,move,u);

                // found rho move
                foundRhoMove = true;

                // the next iteration should find a dipole
                continue;
            }

            // find any rho 2 pair
            RhoPair r2 = gem.findAnyRho2Pair();
            if (r2 != null) {
                System.out.println("<SIMPLIFICATION> Found " + r2.toString() + ". Applying it...");

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
                    return S_SAMEPATH;

                // append node
                this.appendNewNodeToGraph(gem,move,u);

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
            return S_SIMPLIFIED;
        }
        else {
            return S_NOSIMPLIFICATION;
        }
    }

    public Gem getBestAttractorFound() {
        String code;
        try {
            code = _bestGem.getCurrentLabelling().getLettersString(",");
        } catch (Exception ex) {
            code = "";
        }
        System.out.println("Best Attractor Found: "+code);

        return _bestGem;
    }

    public int getBestAttractorTSClassSize() {
        return _bestGemTSClassSize;
    }

    private void appendNewNodeToGraph(Gem gem, Move move, Vertex parent) {
        // System.out.println("append: "+gem.getStringWithNeighbours());

        // add new vertex to the reduction graph
        Vertex v = _G.addVertex(new SparseVertex());
        v.setUserDatum("key", gem, UserData.SHARED);

        // save a link from current gem to this new
        // reduction graph vertex.
        putOnMap(gem, v);

        // add an edge from parent gem to this new
        // simplified gem.
        Edge e = _G.addEdge(new DirectedSparseEdge(parent, v));
        e.setUserDatum("key", move, UserData.SHARED);
    }

    private void putOnMap(Gem g, Vertex v) {
        // System.out.println("put:\n"+g.getStringWithNeighbours());
        _map.put(g,v);
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

    public static void main(String[] args) throws ClassNotFoundException, IOException, SQLException {

        GemPathRepository R = new GemPathRepository();

        ArrayList<GemEntry> list = App.getRepositorio().getGems();
        HashMap<Long,Gem> map = new HashMap<Long,Gem>();
        HashMap<Long,GemEntry> mapGemEntry = new HashMap<Long,GemEntry>();
        for (GemEntry e: list) {
            map.put(e.getId(),e.getGem());
            mapGemEntry.put(e.getId(),e);
        }

        PrintWriter pw = new PrintWriter("c:/workspace/blink/log/tentativas.txt");

        /* long[] idList = {
                        23L,87L,256L,255L,
                        38L,89L,272L,
                        104L,137L,161L,
                        198L,151L,
                        149L,132L,
                        200L,259L,204L,260L,
                        128L,209L,270L,220L,
                        265L,276L,205L,
                        273L,268L,236L,269L,
                        234L,261L,235L,274L,
                        257L,225L,271L,
                        130L,107L}; */

         /* long[] idList = { 103L, 263L }; */
         ArrayList<Long> idList = new ArrayList<Long>();
         //long[] arrIdList = { 101,102,109,110,111,112,116,117,121,123,124,125,130,131,133,135,136,137,138,139,141,143,144,145,146,147,148,151,157,159,160,161,167,168,175,178,179,181,182,183,185,186,189,194,198,199,2,200,201,203,204,205,206,207,208,210,212,213,215,216,217,218,219,220,222,223,226,227,228,229,23,230,231,232,233,234,235,236,237,238,240,241,242,244,245,246,247,248,249,251,252,253,254,255,256,257,260,265,266,267,268,269,270,271,272,273,274,276,278,279,280,281,283,285,286,287,288,289,290,291,292,293,294,295,296,297,298,299,300,301,302,303,304,305,306,307,308,309,310,311,312,313,315,316,317,318,319,323,324,325,327,328,329,330,332,333,334,336,340,341,342,343,344,345,346,347,349,35,350,351,352,353,354,355,357,359,360,362,363,364,365,366,367,368,369,370,371,372,374,375,376,377,378,379,380,381,382,383,387,388,391,392,393,394,395,396,403,404,405,407,413,416,419,420,422,423,424,425,427,428,429,431,432,433,436,438,440,441,442,443,444,446,448,452,453,455,456,457,458,459,462,463,464,465,466,467,468,469,470,472,473,474,475,476,477,478,481,484,485,486,487,490,491,492,493,495,496,497,498,500,502,503,504,505,506,507,508,509,510,511,512,513,514,515,518,519,52,521,522,523,524,526,527,528,529,53,530,531,532,533,534,537,538,539,54,540,541,542,543,544,545,546,547,548,549,550,551,555,556,558,559,560,561,562,566,567,569,570,572,573,58,580,582,583,584,585,586,588,589,59,590,591,592,593,594,595,596,598,601,602,603,604,605,606,607,608,609,610,611,612,614,616,617,618,619,620,621,622,623,625,626,627,628,629,630,632,633,634,635,637,638,639,643,644,646,647,648,649,650,651,652,653,654,656,657,658,659,660,661,662,663,665,666,668,669,670,671,672,673,674,675,676,680,681,682,683,687,691,693,696,698,7,702,703,705,706,707,708,709,71,710,711,713,714,715,716,717,718,719,72,720,722,723,724,725,726,727,728,729,730,731,732,734,735,736,739,74,740,741,742,743,744,747,748,749,75,750,751,752,756,759,761,763,764,765,766,767,768,769,770,771,773,777,780,781,784,785,786,787,794,796,803,804,806,808,816,818,819,820,825,826,827,829,83,830,831,833,835,839,84,840,842,844,845,846,847,848,849,850,851,852,853,854,855,856,858,86,860,861,862,864,865,866,867,868,869,87,870,872,874,875,877,878,88,880,881,883,886,887,888,889,89,890,892,893,894,898,899,900,902,903,906,907,909,911,912,915,918,919,92,920,921,922,924,930,932,940,95 };
         //long[] arrIdList = { 865,804,888,922,846,818,729,919,819,759,614,316,416,246,703,375,237,588,334,635,637,763,742,825,714,849,730,491,889,665,330,878,466,309,551,304,392,372,394,670,708,272,864,511,633,473,558,246,751,545,537,493,594,523,492,485,299,907,902,341,503,840,732,296,305,378,561,359,257,340,269,368,297,598,723,839,886,654,349,658,653,724,848,604,730,543,486,716,484,717,649,603,842,497,770,768,851,720,858,740,852,855,705,663,278,854,731,771,860,766,764,332,669,665,862,514,735,324,485,492,296,305,378,359,559,340,269,272,511,660,870,526,362,530,374,761,883,773,672,405,673,405,673,2,682,570,915,920,7,425,124,446,121,438,125,194,680,681,541,528,183,781,23,529,621,683,573,796,346,23,621,924,7,892,432,58,452,691,427,35,124,121,438,125,872,194,680,347,691,35,547,382,424,419,131,167,59,161,159,519,145,240,219,313,161,159,315,404,138,145,505,626,240,219,313,147,199,146,216,274,147,199,657,596,508,522,203,477,248,71,524,270,280,206,295,251,498,869,583,903,463,912,930,713,496,605,294,290,306,542,820,756,747,893,146,216,274,459,661,509,830,702,222,388,548,360,464,268,539,292,270,206,280,289,251,639,662,235,650,652,710,630,845,544,715,736,861,487,827,287,475,504,540,711,725,593,638,533,508,596,522,534,749,617,477,247,413,248,71,752,833,899,325,367,595,853,659,856,718,710,652,845,544,675,719,715,874,844,909,728,285,611,765,287,507,353,475,288,500,540,711,222,674,623,548,86,281,496,306,668,247,413,315,403,676,671,433,567,442,178,186,175,182,443,189,185,178,422,186,182,130,168,423,569,441,687,343,344,345,803,249,806,609,260,336,301,298,223,260,336,301,365,481,532,961,794,743,311,586,515,510,231,602,634,585,448,303,227,83,217,227,83,217,592,816,212,456,241,906,940,881,932,880,607,696,490,352,644,921,918,228,632,226,777,465,236,474,328,549,470,327,333,590,829,518,739,619,826,300,317,229,354,918,606,706,709,456,241,622,470,327,616,465,318,236,546,512,226,393,515,750,867,877,890,381,562,391,875,381,562,453,608,666,101,428,887,52,436,440,102,181,453,101,428,887,52,436,440,136,137,133,137,133,123,135,527,179,342,201,88,283,366,89,307,866,383,201,387,88,350,234,279,478,351,252,538,245,286,291,279,478,252,351,245,538,291,286,350,234,767,610,495,656,612,734,656,612,643,769,395,556,785,379,141,116,429,431,143,144,627,693,808,707,469,580,244,267,230,457,521,215,255,620,462,591,589,741,513,476,744,618,467,244,267,230,364,266,472,911,215,255,380,646,900,582,847,220,727,555,369,984,989,304,312,850,894,705,868,651,898,628,200,329,468,455,629,254,530,204,375,242,334,502,550,759,614,584,200,273,207,329,370,468 };
         //long[] arrIdList = { 201, 88  };
         long[] arrIdList = {1566,573,796,1550}; //long[] arrIdList = { 865,804,888,922,846,818,729,919,819,759,614,316,416,246,703,375,237,588,334,635,637,763,742,825,714,849,730,491,889,665,330,878,466,309,551,304,392,372,394,670,708,272,864,511,633,473,558,246,751,545,537,493,594,523,492,485,299,907,902,341,503,840,732,296,305,378,561,359,257,340,269,368,297,598,723,839,886,654,349,658,653,724,848,604,730,543,486,716,484,717,649,603,842,497,770,768,851,720,858,740,852,855,705,663,278,854,731,771,860,766,764,332,669,665,862,514,735,324,485,492,296,305,378,359,559,340,269,272,511,660,870,526,362,530,374,761,883,773,672,405,673,405,673,2,682,570,915,920,7,425,124,446,121,438,125,194,680,681,541,528,183,781,23,529,621,683,573,796,346,23,621,924,7,892,432,58,452,691,427,35,124,121,438,125,872,194,680,347,691,35,547,382,424,419,131,167,59,161,159,519,145,240,219,313,161,159,315,404,138,145,505,626,240,219,313,147,199,146,216,274,147,199,657,596,508,522,203,477,248,71,524,270,280,206,295,251,498,869,583,903,463,912,930,713,496,605,294,290,306,542,820,756,747,893,146,216,274,459,661,509,830,702,222,388,548,360,464,268,539,292,270,206,280,289,251,639,662,235,650,652,710,630,845,544,715,736,861,487,827,287,475,504,540,711,725,593,638,533,508,596,522,534,749,617,477,247,413,248,71,752,833,899,325,367,595,853,659,856,718,710,652,845,544,675,719,715,874,844,909,728,285,611,765,287,507,353,475,288,500,540,711,222,674,623,548,86,281,496,306,668,247,413,315,403,676,671,433,567,442,178,186,175,182,443,189,185,178,422,186,182,130,168,423,569,441,687,343,344,345,803,249,806,609,260,336,301,298,223,260,336,301,365,481,532,961,794,743,311,586,515,510,231,602,634,585,448,303,227,83,217,227,83,217,592,816,212,456,241,906,940,881,932,880,607,696,490,352,644,921,918,228,632,226,777,465,236,474,328,549,470,327,333,590,829,518,739,619,826,300,317,229,354,918,606,706,709,456,241,622,470,327,616,465,318,236,546,512,226,393,515,750,867,877,890,381,562,391,875,381,562,453,608,666,101,428,887,52,436,440,102,181,453,101,428,887,52,436,440,136,137,133,137,133,123,135,527,179,342,201,88,283,366,89,307,866,383,201,387,88,350,234,279,478,351,252,538,245,286,291,279,478,252,351,245,538,291,286,350,234,767,610,495,656,612,734,656,612,643,769,395,556,785,379,141,116,429,431,143,144,627,693,808,707,469,580,244,267,230,457,521,215,255,620,462,591,589,741,513,476,744,618,467,244,267,230,364,266,472,911,215,255,380,646,900,582,847,220,727,555,369,984,989,304,312,850,894,705,868,651,898,628,200,329,468,455,629,254,530,204,375,242,334,502,550,759,614,584,200,273,207,329,370,468 };, long[] arrIdList = { 201, 88  };, long[] arrIdList = { 865,804,888,922,846,818,729,919,819,759,614,316,416,246,703,375,237,588,334,635,637,763,742,825,714,849,730,491,889,665,330,878,466,309,551,304,392,372,394,670,708,272,864,511,633,473,558,246,751,545,537,493,594,523,492,485,299,907,902,341,503,840,732,296,305,378,561,359,257,340,269,368,297,598,723,839,886,654,349,658,653,724,848,604,730,543,486,716,484,717,649,603,842,497,770,768,851,720,858,740,852,855,705,663,278,854,731,771,860,766,764,332,669,665,862,514,735,324,485,492,296,305,378,359,559,340,269,272,511,660,870,526,362,530,374,761,883,773,672,405,673,405,673,2,682,570,915,920,7,425,124,446,121,438,125,194,680,681,541,528,183,781,23,529,621,683,573,796,346,23,621,924,7,892,432,58,452,691,427,35,124,121,438,125,872,194,680,347,691,35,547,382,424,419,131,167,59,161,159,519,145,240,219,313,161,159,315,404,138,145,505,626,240,219,313,147,199,146,216,274,147,199,657,596,508,522,203,477,248,71,524,270,280,206,295,251,498,869,583,903,463,912,930,713,496,605,294,290,306,542,820,756,747,893,146,216,274,459,661,509,830,702,222,388,548,360,464,268,539,292,270,206,280,289,251,639,662,235,650,652,710,630,845,544,715,736,861,487,827,287,475,504,540,711,725,593,638,533,508,596,522,534,749,617,477,247,413,248,71,752,833,899,325,367,595,853,659,856,718,710,652,845,544,675,719,715,874,844,909,728,285,611,765,287,507,353,475,288,500,540,711,222,674,623,548,86,281,496,306,668,247,413,315,403,676,671,433,567,442,178,186,175,182,443,189,185,178,422,186,182,130,168,423,569,441,687,343,344,345,803,249,806,609,260,336,301,298,223,260,336,301,365,481,532,961,794,743,311,586,515,510,231,602,634,585,448,303,227,83,217,227,83,217,592,816,212,456,241,906,940,881,932,880,607,696,490,352,644,921,918,228,632,226,777,465,236,474,328,549,470,327,333,590,829,518,739,619,826,300,317,229,354,918,606,706,709,456,241,622,470,327,616,465,318,236,546,512,226,393,515,750,867,877,890,381,562,391,875,381,562,453,608,666,101,428,887,52,436,440,102,181,453,101,428,887,52,436,440,136,137,133,137,133,123,135,527,179,342,201,88,283,366,89,307,866,383,201,387,88,350,234,279,478,351,252,538,245,286,291,279,478,252,351,245,538,291,286,350,234,767,610,495,656,612,734,656,612,643,769,395,556,785,379,141,116,429,431,143,144,627,693,808,707,469,580,244,267,230,457,521,215,255,620,462,591,589,741,513,476,744,618,467,244,267,230,364,266,472,911,215,255,380,646,900,582,847,220,727,555,369,984,989,304,312,850,894,705,868,651,898,628,200,329,468,455,629,254,530,204,375,242,334,502,550,759,614,584,200,273,207,329,370,468 };, long[] arrIdList = { 201, 88  };, , 266, 472, 1051  };
         for (Long id: arrIdList) {
             Gem source = map.get(id);
             // if (source.getNumVertices() <= 26)
                 idList.add(id);
         }

         //
        Random r = new Random(System.currentTimeMillis());
        // int kk = r.nextInt(idList.size());
        for (int i=0;i<1000000;i++) {
            long id = idList.get(i % idList.size());
            System.out.println("Processing: "+id);

            for (int j=0;j<2;j++) {

                // kk =(kk+1) % idList.size();
                Gem source = map.get(id);

                // if (source.getNumVertices() >= 50)
                //    continue;

                GemSimplificationPathFinder A = new GemSimplificationPathFinder(source, 5,  300 * 1000L,mapGemEntry.get(id).getTSClassSize());
                Gem target = A.getBestAttractorFound();
                int tsClassSize = A.getBestAttractorTSClassSize();
                Path path = A.getBestPath();

                boolean add = false;
                if (source.compareTo(target) != 0) {
                    add = R.addPathIfItDoesNotExist(source, target, tsClassSize, true, path);
                    if (add)
                        Toolkit.getDefaultToolkit().beep();
                }

                System.out.println("" + target.getCurrentLabelling().getLettersString(","));
                for (GemEntry e : list) {
                    if ((new Gem(e.getLabelling())).compareTo(target) == 0) {
                        System.out.println("gem = " + e.getId() + " " + (target.compareTo(source) < 0 ? " simpl." : ""));
                        pw.println("gem = " + e.getId() + (target.compareTo(source) < 0 ? " simpl." : ""));
                        pw.flush();
                    }
                }

                if (add)
                    break;

                // desenhar o mapa
                //PanelReductionGraph prg = new PanelReductionGraph(A.getGraph());
                //JFrame f = new JFrame("Reduction Graph");
                //f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                //f.setSize(new Dimension(1024,768));
                //f.setContentPane(prg);
                //f.setVisible(true);
                // desenhar o mapa
            }
        }
        pw.close();
        // System.exit(0);

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
        System.out.println("Path: "+countPath);
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

    private boolean U() {
        System.out.println("Running U");

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
        putOnMap(g, vv);

        // add an edge from parent gem to this new simplified gem
        Edge e = _G.addEdge(new DirectedSparseEdge(uu, vv));
        e.setUserDatum("key", move, UserData.SHARED);

        // change current gem
        _currentGem = g;

        return true;
    }

}
