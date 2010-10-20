package blink;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

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
public class GenerateRepresentantGem {

    private GemPackedLabelling _representant;
    private ArrayList<GemPackedLabelling> _unprocessed = new ArrayList<GemPackedLabelling>();

    private int _tsClassSize = -1;

    public int getTSClassSize() {
        return _tsClassSize;
    }

    public GenerateRepresentantGem(Gem gem) {
        _representant = gem.goToCodeLabel();
        _unprocessed.add(_representant);
        process();
    }

    public Gem findSimplifyingGemOnTheTSClass(Gem g) {
        // System.out.println("Processing:\n"+l.getLettersString(' '));

        { // test if given input gem has not simplifying point
            Gem g0 = new Gem(g.goToCodeLabel());
            Dipole d = g.findAnyDipole();
            if (d != null) {
                System.out.println("Found " + d);
                return g0;
            }
            RhoPair r2 = g0.findAnyRho2Pair();
            if (r2 != null) {
                System.out.println("Found " + r2);
                return g0;
            }
            RhoPair r3 = g0.findAnyRho3Pair();
            if (r3 != null) {
                System.out.println("Found " + r3);
                return g0;
            }
        } // test if given input gem has not simplifying point

        HashSet<GemPackedLabelling> _set = new HashSet<GemPackedLabelling>();
        ArrayList<GemPackedLabelling> _unprocessed = new ArrayList<GemPackedLabelling>();

        GemPackedLabelling lbl = g.goToCodeLabel();
        lbl.setNumBlobs(g.getNumBlobs());

        _unprocessed.add(lbl);
        _set.add(lbl);

        TSMoveType[] types = TSMoveType.values(); // {TSMoveType.TS5,TSMoveType.TS6};

        Random r = new Random(15L);

        while (!_unprocessed.isEmpty()) {

            int N = _unprocessed.size();
            int k = r.nextInt(N);
            GemPackedLabelling l = _unprocessed.get(k);
            _unprocessed.set(k,_unprocessed.get(N-1));
            _unprocessed.remove(N-1);

            // System.out.println("Processing:\n"+l.getLettersString(' '));
            Gem g0 = new Gem(l);

            for (GemVertex v: g0.getVertices()) {
                for (GemColor[] p: GemColor.PERMUTATIONS) {
                    for (TSMoveType type: types) {
                        if (g0.isTSMovePoint(v, p, type)) {

                            TSMovePoint m = new TSMovePoint(v.getLabel(), p, type);

                            Gem copy = g0.copy();
                            copy.applyTSMove(m);
                            copy = new Gem(copy.goToCodeLabel());

                            // if (copy.getAgemality() != 0)
                            //    throw new RuntimeException("Oooooppppssss");

                            GemPackedLabelling lCopy = copy.goToCodeLabel();
                            if (!_set.contains(lCopy)) {
                                Dipole d = (new Gem(copy.getCurrentLabelling())).findAnyDipole();
                                if (d != null) {
                                    System.out.println(""+copy.getCurrentLabelling().getLettersString(",")+" tem simplificação: ");
                                    System.out.println("Found "+d);
                                    return copy;
                                }
                                RhoPair r2 = (new Gem(copy.getCurrentLabelling())).findAnyRho2Pair();
                                if (r2 != null) {
                                    System.out.println(""+copy.getCurrentLabelling().getLettersString(",")+" tem simplificação: ");
                                    System.out.println("Found "+r2);
                                    return copy;
                                }
                                RhoPair r3 = (new Gem(copy.getCurrentLabelling())).findAnyRho3Pair();
                                if (r3 != null) {
                                    System.out.println(""+copy.getCurrentLabelling().getLettersString(",")+" tem simplificação: ");
                                    System.out.println("Found "+r3);
                                    return copy;
                                }

                                // System.out.println("NÃO");
                                _set.add(lCopy);
                                _unprocessed.add(lCopy);
                            }
                        }
                    }
                }
            }
        }
        return null;
    }


    public void process() {

        while (!_unprocessed.isEmpty()) {
            GemPackedLabelling lbl = _unprocessed.get(_unprocessed.size()-1);
            _unprocessed.remove(_unprocessed.size()-1);

            boolean simplified = false;
            Gem gem = new Gem(lbl);

            // try to simplify gem
            while (true) {

                // LOG ----------------------------------
                System.out.println("\nProcessing: "+gem.getCurrentLabelling().getLettersString(",")+" num vertices: "+gem.getNumVertices());
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

                // if (gem.getAgemality() != 0)
                //    throw new RuntimeException("oooooppppsssssss");

                // simplify dipole while exists
                Dipole d = gem.findAnyDipole();
                if (d != null) {
                    System.out.println("<SIMPLIFICATION> Found dipole cancelation: "+d.toString());

                    gem.cancelDipole(d);
                    gem = new Gem(gem.goToCodeLabel());

                    simplified = true;
                    continue;
                }

                // find any rho 3 pair
                RhoPair r3 = gem.findAnyRho3Pair();
                if (r3 != null) {
                    System.out.println("<SIMPLIFICATION> Found "+r3.toString()+". Applying it...");

                    gem.applyRhoPair(r3);
                    gem = new Gem(gem.goToCodeLabel());

                    continue;
                }

                // find any rho 2 pair
                RhoPair r2 = gem.findAnyRho2Pair();
                if (r2 != null) {
                    System.out.println("<SIMPLIFICATION> Found "+r2.toString()+". Applying it...");

                    gem.applyRhoPair(r2);
                    gem = new Gem(gem.goToCodeLabel());

                    continue;
                }

                break;
            }

            if (!simplified)
                System.out.println("No simplification found");

            // gem.goToCodeLabel();
            // System.out.println(""+gem.getStringWithNeighbours());
            // System.out.println(""+gem.goToCodeLabel().getLettersString(','));

            System.out.println("Searching TS-class of " + gem.getCurrentLabelling().getLettersString(","));
            Gem gemSimp = this.findSimplifyingGemOnTheTSClass(gem);

            // finished the search
            _unprocessed.clear();
            if (gemSimp != null) {
                System.out.println("Found " + gemSimp.getCurrentLabelling().getLettersString(","));
                _unprocessed.add(gemSimp.getCurrentLabelling());
            }
            else {
                GenerateTSClass G = new GenerateTSClass(gem);
                _tsClassSize = G.size();
                _representant = G.getTSClass().get(0);
            }
        }
    }

    public GemPackedLabelling getRepresentant() {
        return _representant;
    }

}
