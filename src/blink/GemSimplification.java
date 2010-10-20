package blink;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
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
public class GemSimplification {

    private HashSet<GemPackedLabelling> _set = new HashSet<GemPackedLabelling>();

    private ArrayList<GemPackedLabelling> _unprocessed = new ArrayList<GemPackedLabelling>();

    public GemSimplification(Gem g) {
        process(g);
    }


    private static long usedMemory ()
    {
        return s_runtime.totalMemory () - s_runtime.freeMemory ();
    }
    private static final Runtime s_runtime = Runtime.getRuntime ();

    private void process(Gem g) {
        try {
            PrintStream ps = new PrintStream(new FileOutputStream("c:/pentagonAttractor.txt"));
            ps.println(g.getCurrentLabelling().getLettersString(" "));
            ps.close();
        } catch (FileNotFoundException ex1) {
        }


        _set = new HashSet<GemPackedLabelling>();
        _unprocessed = new ArrayList<GemPackedLabelling>();

        int numVertices = g.getNumVertices();

        // start by removing all dipoles
        g.cancelAllDipoles();

        // add two blobs
        g.addBlob(g.getVertex(1),GemColor.yellow);
        g.addBlob(g.getVertex(1),GemColor.yellow);

        //
        GemPackedLabelling lbl = g.goToCodeLabel();
        lbl.setNumBlobs(g.getNumBlobs());

        _unprocessed.add(lbl);
        _set.add(lbl);

        int count = 1;

        boolean moreThan2Blobs[] = {false};

        Random r = new Random();

        while (!_unprocessed.isEmpty()) {

            int N = _unprocessed.size();
            int k = r.nextInt(N);
            GemPackedLabelling l = _unprocessed.get(k);
            _unprocessed.set(k,_unprocessed.get(N-1));
            _unprocessed.remove(N-1);

            // System.out.println("Processing: "+l.getLettersString(' '));
            Gem g0 = new Gem(l);

            l.setNumBlobs(g0.getNumBlobs()); // update number of blobs on the labelling

            Object o[] = g0.findDipoleThickenningAndDipoleNarrowingPoints();
            ArrayList<DipoleThickenning> listDipoleThickenning = (ArrayList<DipoleThickenning>) o[0];
            for (DipoleThickenning dt: listDipoleThickenning) {
                GemPackedLabelling ll = g0.getGemCodeAfterDipoleThickenning(dt,moreThan2Blobs);
                if (!_set.contains(ll)) {
                    if (moreThan2Blobs[0]) { // found vertice reduction path
                        System.out.println("Found Simplification...");
                        process(new Gem(ll));
                    }
                    else {
                        _set.add(ll);
                        _unprocessed.add(ll);
                    }
                    // System.out.println("Adding: "+ll.getLettersString(' '));
                }
            }

            ArrayList<DipoleNarrowing> listDipoleNarrowing = (ArrayList<DipoleNarrowing>) o[1];
            for (DipoleNarrowing dn: listDipoleNarrowing) {
                GemPackedLabelling ll = g0.getGemCodeAfterDipoleNarrowing(dn,moreThan2Blobs);
                if (!_set.contains(ll)) {
                    if (moreThan2Blobs[0]) { // found vertice reduction path
                        System.out.println("Found Simplification...");
                        process(new Gem(ll));
                    }
                    else {
                        _set.add(ll);
                        _unprocessed.add(ll);
                    }
                    // System.out.println("Adding: "+ll.getLettersString(' '));
                }
            }

            System.out.println(String.format("Gems: %5d    Unprocessed: %5d   Vertices: %5d    UsedMem: %.2fMb",_set.size(),_unprocessed.size(),numVertices,(double)usedMemory()/(double)(1024*1024)));
            count++;
        }

        //
        int n = 0;
        ArrayList<GemPackedLabelling> lbls = new ArrayList<GemPackedLabelling>();
        for (GemPackedLabelling glbl: _set) {
            if (glbl.getNumBlobs() == n) {
                lbls.add(glbl);
            }
            else if (glbl.getNumBlobs() > n) {
                lbls.clear();
                lbls.add(glbl);
                n = glbl.getNumBlobs();
            }
        }

        HashSet<GemPackedLabelling> set = new HashSet<GemPackedLabelling>();
        for (GemPackedLabelling glbl: lbls) {
            Gem gg = new Gem(glbl);
            gg.cancelAllBlobs();
            GemPackedLabelling l =  gg.goToCodeLabel();
            set.add(l);
        }

        int k=1;
        for (GemPackedLabelling l: set) {
            System.out.println(l.getLettersString(" "));

            try {
                PrintStream s = new PrintStream(new FileOutputStream("c:/program files/pigale/tgf/s" + (k++) + ".txt"));
                l.generatePIGALE(s);
                s.close();
            } catch (FileNotFoundException ex) {
            }
        }


    }
























    // ------------------------------------------------------------------------
    // -- Tests ---------------------------------------------------------------

    public static void testDetails(String[] args) throws FileNotFoundException {
        // Blink b = new Blink(new int[][] {{1,2},{1,3,4},{2,5,3},{4},{5}},new int[] {});
        // Blink b = new Blink(new int[][] {{1},{1}},new int[] {});
        // GemFromBlink gfb = new GemFromBlink(b);
        // Gem g = gfb.getGem();

        //GemLabelling l = new GemLabelling(new int[] {4,1,2,3,8,5,6,7,8,7,6,5,4,3,2,1,5,8,7,6,1,4,3,2});
        //GemLabelling l = new GemLabelling(new int[] {4,1,2,3,8,5,6,7,8,7,6,5,4,3,2,1,5,8,7,6,1,4,3,2});
        GemPackedLabelling l = new GemPackedLabelling(new int[] {4,1,2,3,8,5,6,7,8,7,6,5,4,3,2,1,7,6,5,8,3,2,1,4});
        Gem g = new Gem(l);

        int k = 1;

        l = g.getCurrentLabelling();
        PrintStream s = new PrintStream(new FileOutputStream("c:/program files/pigale/tgf/s"+(k++)+".txt"));
        l.generatePIGALE(s);
        s.close();

        g.addBlob(g.getVertex(12), GemColor.green);
        g.addBlob(g.getVertex(2), GemColor.green);

        l = g.getCurrentLabelling();
        s = new PrintStream(new FileOutputStream("c:/program files/pigale/tgf/s"+(k++)+".txt"));
        l.generatePIGALE(s);
        s.close();

        Object o[] = g.findDipoleThickenningAndDipoleNarrowingPoints();
        ArrayList<DipoleThickenning> listDipoleThickenning = (ArrayList<DipoleThickenning>) o[0];
        ArrayList<DipoleNarrowing> listDipoleNarrowing = (ArrayList<DipoleNarrowing>) o[1];
        for (DipoleNarrowing dn: listDipoleNarrowing) {
            System.out.println(""+dn.toString());
        }

        Dipole d;
        /*DipoleNarrowing dn = new DipoleNarrowing(
            new Dipole(g.getVertex(17),GemVertex.COMPONENT_NO_RED)


                             ));*/



        // new GemSimplification(g);
    }

    public static void testR362() throws FileNotFoundException {
        // GemLabelling l = new GemLabelling(new int[] {4,1,2,3,8,5,6,7,8,7,6,5,4,3,2,1,7,6,5,8,3,2,1,4}); // R16-3
        GemPackedLabelling l = new GemPackedLabelling(new int[] {3,1,2,6,4,5,9,7,8,11,10,13,12,9,12,4,3,10,7,6,5,13,8,2,11,1,4,13,7,1,3,9,5,11,12,2,8,6,10}); // R36-2
        Gem g = new Gem(l);
        long t = System.currentTimeMillis();
        new GemSimplification(g);
        System.out.println(String.format("Tempo %.2f seg.",(System.currentTimeMillis()-t)/1000.0));
    }


    public static void testPentagono() {
        GemPackedLabelling l = new GemPackedLabelling("dabcgefjhilknmpojmoedchgpniblafkejimpdkocbgnfhla");
        System.out.println(""+l.getLettersString("\n"));
        Gem g = new Gem(l);
        long t = System.currentTimeMillis();
        new GemSimplification(g);
        System.out.println(String.format("Tempo %.2f seg.",(System.currentTimeMillis()-t)/1000.0));
    }

    public static void main(String[] args) {
        testPentagono();
    }

    // -- Tests ---------------------------------------------------------------
    // ------------------------------------------------------------------------


}
