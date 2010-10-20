package blink;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
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
public class GenerateTSClass {

    private HashSet<GemPackedLabelling> _set = new HashSet<GemPackedLabelling>();

    private ArrayList<GemPackedLabelling> _unprocessed = new ArrayList<GemPackedLabelling>();

    public GenerateTSClass(Gem g) {
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
        GemPackedLabelling lbl = g.goToCodeLabel();
        lbl.setNumBlobs(g.getNumBlobs());

        _unprocessed.add(lbl);
        _set.add(lbl);

        int count = 1;

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

            ArrayList<TSMovePoint> list = new ArrayList<TSMovePoint>();
            for (GemVertex v: g0.getVertices()) {
                for (GemColor[] p: GemColor.PERMUTATIONS) {
                    for (TSMoveType type: types) {
                        if (g0.isTSMovePoint(v, p, type)) {
                            list.add(new TSMovePoint(v.getLabel(), p, type));
                        }
                    }
                }
            }

            // System.out.println(""+this.getCurrentLabelling().getLettersString(' '));

            // now do the moves on the copies and write down the codes
            for (TSMovePoint m: list) {
                Gem copy = g0.copy();

                // System.out.println(""+m);

                copy.applyTSMove(m);
                copy.goToCodeLabel();
                GemPackedLabelling lCopy = copy.getCurrentLabelling();
                if (!_set.contains(lCopy)) {

                    // System.out.println("Predecessor: "+l.getLettersString(' '));
                    // System.out.println("Sucessor: "+lCopy.getLettersString(' '));
                    // System.out.println("Move: "+m);

                    _set.add(lCopy);
                    _unprocessed.add(lCopy);
                }
            }

            // System.out.println(String.format("Gems: %5d    Unprocessed: %5d   Vertices: %5d    UsedMem: %.2fMb",_set.size(),_unprocessed.size(),numVertices,(double)usedMemory()/(double)(1024*1024)));
            count++;
        }

        int k=1;
        for (GemPackedLabelling l: _set) {
            System.out.println(l.getLettersString(" "));

            try {
                PrintStream s = new PrintStream(new FileOutputStream("c:/program files/pigale/tgf/s" + (k++) + ".txt"));
                l.generatePIGALE(s);
                s.close();
            } catch (FileNotFoundException ex) {
            }
        }

        System.out.println("TS-Class of given gem has "+_set.size()+" elements");

    }

    public int size() {
        return _set.size();
    }

    public ArrayList<GemPackedLabelling>  getTSClass() {
        ArrayList<GemPackedLabelling> result = new ArrayList<GemPackedLabelling>(_set);
        Collections.sort(result);
        return result;
    }

    // ------------------------------------------------------------------------
    // -- Tests ---------------------------------------------------------------

    public static void testPentagono() {
        GemPackedLabelling l = new GemPackedLabelling("dabcgefjhilknmpojmoedchgpniblafkejimpdkocbgnfhla");
        System.out.println(""+l.getLettersString("\n"));
        Gem g = new Gem(l);
        long t = System.currentTimeMillis();
        new GenerateTSClass(g);
        System.out.println(String.format("Tempo %.2f seg.",(System.currentTimeMillis()-t)/1000.0));
    }

    public static void testL41() {
        GemPackedLabelling l = new GemPackedLabelling("cabfdehghedcbgfagfeahbdc");
        System.out.println(""+l.getLettersString("\n"));
        Gem g = new Gem(l);
        long t = System.currentTimeMillis();
        new GenerateTSClass(g);
        System.out.println(String.format("Tempo %.2f seg.",(System.currentTimeMillis()-t)/1000.0));
    }

    public static void testL43() {
        GemPackedLabelling l = new GemPackedLabelling("dabchefghgfedcbagfehcbad");
        System.out.println(""+l.getLettersString("\n"));
        Gem g = new Gem(l);
        long t = System.currentTimeMillis();
        new GenerateTSClass(g);
        System.out.println(String.format("Tempo %.2f seg.",(System.currentTimeMillis()-t)/1000.0));
    }

    public static void testL71() {
        GemPackedLabelling l = new GemPackedLabelling("dabcgefjhilknmjmfedckgnahblimhlgckdbanjeif");
        System.out.println(""+l.getLettersString("\n"));
        Gem g = new Gem(l);
        long t = System.currentTimeMillis();
        new GenerateTSClass(g);
        System.out.println(String.format("Tempo %.2f seg.",(System.currentTimeMillis()-t)/1000.0));
    }

    public static void testR2871() {
        GemPackedLabelling l = new GemPackedLabelling("dabcgefjhilknmjkfndmhgbliacefligcndmekjbha");
        System.out.println(""+l.getLettersString("\n"));
        Gem g = new Gem(l);
        long t = System.currentTimeMillis();
        new GenerateTSClass(g);
        System.out.println(String.format("Tempo %.2f seg.",(System.currentTimeMillis()-t)/1000.0));
    }

    public static void testR268() throws FileNotFoundException {
        GemPackedLabelling l = new GemPackedLabelling("cabfdeighkjmlimdcbjfkaglhemgkjlcbadifeh");
        System.out.println(""+l.getLettersString("\n"));
        Gem g = new Gem(l);
        System.out.println(g.getStringWithNeighbours());

        PrintStream s = new PrintStream(new FileOutputStream("c:/program files/pigale/tgf/bug3.txt"));
        l.generatePIGALE(s);
        s.close();

        long t = System.currentTimeMillis();
        new GenerateTSClass(g);
        System.out.println(String.format("Tempo %.2f seg.",(System.currentTimeMillis()-t)/1000.0));
    }

    public static void testBug1() throws FileNotFoundException {
        GemPackedLabelling l = new GemPackedLabelling("dabcgefjhilknmpojmoedchgpniblafkejimpdkocbgnfhla");
        Gem g = new Gem(l);
        g.goToCodeLabel();
        l = g.getCurrentLabelling();
        System.out.println(""+l.getLettersString("\n"));
        System.out.println(""+l.getIntegersString('\n'));

        PrintStream s = new PrintStream(new FileOutputStream("c:/program files/pigale/tgf/bug.txt"));
        l.generatePIGALE(s);
        s.close();

        TSMovePoint m = new TSMovePoint(6,new GemColor[] {GemColor.yellow,GemColor.blue,GemColor.red,GemColor.green},TSMoveType.TS5);

        System.out.println("Move: "+m);

        Gem g2 = g.copy();
        g2.applyTSMove(m);

        s = new PrintStream(new FileOutputStream("c:/program files/pigale/tgf/bug2.txt"));
        g2.getCurrentLabelling().generatePIGALE(s);
        s.close();


        g2.goToCodeLabel();
        System.out.println("" + g2.getCurrentLabelling().getLettersString("\n"));
    }

    public static void testBug2() throws FileNotFoundException {
        GemPackedLabelling l = new GemPackedLabelling("cabfdehghedcbgfagdfbahec");
        Gem g = new Gem(l);
        g.goToCodeLabel();
        l = g.getCurrentLabelling();
        System.out.println(""+l.getLettersString("\n"));
        System.out.println(""+l.getIntegersString('\n'));
        PrintStream s = new PrintStream(new FileOutputStream("c:/program files/pigale/tgf/bug.txt"));
        l.generatePIGALE(s);
        s.close();

        if (!g.isTSMovePoint(g.findVertex(4), new GemColor[] {GemColor.yellow,GemColor.green,GemColor.red,GemColor.blue}, TSMoveType.TS4)) {
            System.out.println("Nao é Válido!");
            System.exit(0);
        }

        TSMovePoint m = new TSMovePoint(4,new GemColor[] {GemColor.yellow,GemColor.green,GemColor.red,GemColor.blue},TSMoveType.TS4);
        System.out.println("Move: "+m);
        Gem g2 = g.copy();
        g2.applyTSMove(m);

        System.out.println(g2.getStringWithNeighbours());

        s = new PrintStream(new FileOutputStream("c:/program files/pigale/tgf/bug2.txt"));
        g2.getCurrentLabelling().generatePIGALE(s);
        s.close();
        System.out.println("" + g2.getCurrentLabelling().getLettersString("\n"));


        g2.goToCodeLabel();
        System.out.println("" + g2.getCurrentLabelling().getLettersString("\n"));
    }



    public static void testBug3() throws FileNotFoundException {
        GemPackedLabelling l = new GemPackedLabelling("dabcgefihkjmlilfedcjgmhbkalfigkhmbjcead");
        Gem g = new Gem(l);
        g.goToCodeLabel();
        l = g.getCurrentLabelling();
        System.out.println("" + l.getLettersString("\n"));
        System.out.println("" + l.getIntegersString('\n'));
        PrintStream s = new PrintStream(new FileOutputStream("c:/program files/pigale/tgf/bug.txt"));
        l.generatePIGALE(s);
        s.close();

        if (!g.isTSMovePoint(g.findVertex(1), new GemColor[] {GemColor.red, GemColor.blue, GemColor.yellow,
                             GemColor.green}, TSMoveType.TS3)) {
            System.out.println("Nao é Válido!");
            System.exit(0);
        }

        TSMovePoint m = new TSMovePoint(1, new GemColor[] {GemColor.red, GemColor.blue, GemColor.yellow, GemColor.green},
                                        TSMoveType.TS3);
        System.out.println("Move: " + m);
        Gem g2 = g.copy();
        g2.applyTSMove(m);

        System.out.println(g2.getStringWithNeighbours());

        s = new PrintStream(new FileOutputStream("c:/program files/pigale/tgf/bug2.txt"));
        g2.getCurrentLabelling().generatePIGALE(s);
        s.close();
        System.out.println("" + g2.getCurrentLabelling().getLettersString("\n"));

        g2.goToCodeLabel();
        System.out.println("" + g2.getCurrentLabelling().getLettersString("\n"));
    }

    public static void testLabelling() throws FileNotFoundException {
        GemPackedLabelling l = new GemPackedLabelling("cabfdehghedcbgfagdfbahec");
        System.out.println("Before...");
        System.out.println(""+l.getLettersString("\n"));
        System.out.println(""+l.getIntegersString('\n'));
        Gem g = new Gem(l);
        System.out.println(""+g.getStringWithNeighbours());
        GemPackedLabelling gl = g.goToCodeLabel();
        l = g.getCurrentLabelling();

        System.out.println("After...");
        System.out.println(""+l.getLettersString("\n"));
        System.out.println(""+l.getIntegersString('\n'));
        System.out.println(""+g.getStringWithNeighbours());

        System.out.println("After After...");
        System.out.println(""+gl.getLettersString("\n"));
        System.out.println(""+gl.getIntegersString('\n'));

        PrintStream s = new PrintStream(new FileOutputStream("c:/program files/pigale/tgf/codeOfL41.txt"));
        l.generatePIGALE(s);
        s.close();
    }

    public static void main(String[] args) throws FileNotFoundException {
        //testPentagono();
        //testBug1();
        //testL41();
        //testLabelling();
        //testBug2();
        //testL71();
        //testL43();
        testR2871();
        //testR268();
        //testBug3();
    }

    // -- Tests ---------------------------------------------------------------
    // ------------------------------------------------------------------------

}
