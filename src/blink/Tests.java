package blink;

import java.awt.Dimension;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.JFrame;

/**
 *
 */
public class Tests {
    public Tests() {
    }

    public static void main(String[] args) throws SQLException, FileNotFoundException, ClassNotFoundException, IOException {

        ManifoldCatalog C = new ManifoldCatalog();

        ArrayList<GemEntry> gems = App.getRepositorio().getSpaceDefiningGemsFromCatalog28();


        PrintWriter pw= new PrintWriter(new FileWriter("log/differenceFormGemCatalog28.txt"));
        int countDifference = 0;
        int countCommons = 0;
        for (GemEntry ge: gems) {
            Manifold S = C.getManifoldFromMinGem(ge.getId());
            GemPrimeStatus p = ge.getGemPrimeStatus();
            pw.println(
                    String.format(
                            "gemId: %6d  #vert: %6d  catNumb: %6d   primeStatus: %s  ->  %s",
                            ge.getId(),
                            ge.getNumVertices(),
                            ge.getCatalogNumber(),
                            p.toString(),
                            (S == null ? "no space" : "S "+S.blinkComplexity()+" "+S.getNumberOnComplexity())));
            if (S == null) countDifference++;
            else countCommons++;
        }

        pw.println(String.format("Difference %6d    Commons %6d",countDifference,countCommons));
        pw.close();
        System.exit(0);
        /*
        GemEntry ge = App.getRepositorio().getGemEntryByCatalogNumber(30,2346);
        Gem g = ge.getGem();
        Quartet q = g.findAllNonTrivialQuartets().get(0);
        ArrayList<HashSet<GemVertex>> partition = g.connectedComponentsAfterQuartetRemoval(q);
        Gem gems[] = g.breakGemOnQuartet(q,partition.get(0),partition.get(1));
        Gem g1 = gems[0];
        Gem g2 = gems[1];
        System.out.println("G1 #"+g1.getNumVertices());
        System.out.println("G2 #"+g2.getNumVertices());

        GemSimplificationPathFinder SA0 = new GemSimplificationPathFinder(gems[0],0,5000L,0);
        GemSimplificationPathFinder SA1 = new GemSimplificationPathFinder(gems[1],0,5000L,0);
        gems[0] = (SA0.getBestAttractorFound() != null ? SA0.getBestAttractorFound() : gems[0]);
        gems[1] = (SA1.getBestAttractorFound() != null ? SA1.getBestAttractorFound() : gems[1]);

        GemEntry ge0 = null;
        GemEntry ge1 = null;

        ArrayList<GemEntry> ges0 = App.getRepositorio().getGemsByHashcodeAndHandleNumber(gems[0].getGemHashCode(), gems[0].getHandleNumber());
        for (GemEntry gege : ges0) {
            if (gege.getGem().equals(gems[0])) {
                ge0 = gege;
                break;
            }
        }
        ArrayList<GemEntry> ges1 = App.getRepositorio().getGemsByHashcodeAndHandleNumber(gems[1].getGemHashCode(),gems[1].getHandleNumber());
        for (GemEntry gege: ges1) {
            if (gege.getGem().equals(gems[1])) {
                ge1 = gege;
                break;
            }
        }

        System.out.println("ge0 #"+ge0.getNumVertices()+" catalogNumber: "+ge0.getCatalogNumber());
        System.out.println("ge1 #"+ge1.getNumVertices()+" catalogNumber: "+ge1.getCatalogNumber());


        System.exit(0);

        // testPreservingParityCode();
        // testTrivialSimplifications();
        // testGemCode();
        // testSmartGemCode();
        // testRBond();
        //testTSMoves(); */
    }

    public static void testTSMoves() throws FileNotFoundException {
        GemPackedLabelling l = new GemPackedLabelling("dabcgefjhilkjkfedchgbliaihefkjlbadcg");
        System.out.println(""+l.getIntegersString('\n'));
        Gem g = new Gem(l);
        g.tsMoves();
    }

    public static void testRBond() throws FileNotFoundException {
        // GemLabelling l = new GemLabelling("dabcgefjhilknmpojmoedchgpniblafkejimpdkocbgnfhla");
        // GemLabelling l = new GemLabelling("eabcdjfghijihgfedcbahgfjicbaed");
        GemPackedLabelling l = new GemPackedLabelling("dabcgefjhilkjkfedchgbliaihefkjlbadcg");
        System.out.println(""+l.getIntegersString('\n'));
        Gem g = new Gem(l);

        l = g.getCurrentLabelling();
        PrintStream s = new PrintStream(new FileOutputStream("c:/program files/pigale/tgf/gem.txt"));
        l.generatePIGALE(s);
        s.close();


        long t = System.currentTimeMillis();
        // g.identifyRectangles();
        g.rbond();
        System.out.println(String.format("Tempo %.2f seg.",(System.currentTimeMillis()-t)/1000.0));
    }


    public static void testTrivialSimplifications() {
        GBlink b = new GBlink(new int[][] {
                            {1,5},
                            {1,2,3,4},
                            {2},
                            {3},
                            {4,5}
        }, new int[]{});
        b.write();
        ArrayList<EdgePartitionPart> epps = b.getGEdgePartition();
        for (EdgePartitionPart epp: epps) {
            System.out.println(epp.toString());
        }

        // desenhar o mapa
        JFrame f = new JFrame("Map Drawing");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(new Dimension(250,250));
        f.setContentPane(new DrawPanel(new MapD(b)));
        f.setVisible(true);
        // desenhar o mapa
    }


    public static void testRepresentant(String[] args) throws IOException {
        //MapWord mapWord = new MapWord(new int[] {10,14,2,5,4,3,6,9,8,7,1,13,12,11});
        /* // exemplo do caderno
                 Blink b = new Blink(new int[][] {
                            {1, 1, 2, 3, 4, 6, 7, 8}, {2}, {3, 10, 12},
                            {4, 5}, {5, 6}, {7, 9}, {8, 9}, {10, 11}, {11, 12}},
                            new int[] {}); */

        /*
        Blink b = new Blink(new int[][] {
                            {6,1,4},
                            {5,2,1},
                            {2,3},
                            {3,4},
                            {6},{5}},
                new int[] {});*/

    GBlink b = new GBlink(new int[][] {
                        {1,2,4,5,6},
                        {1,5},
                        {6},
                        {2,3,4},
                        {3}},
            new int[] {});

/*
    // exemplo Funcionando
    Blink b = new Blink(new int[][] {
                        {1, 2, 3, 5, 6, 6, 7, 11, 10},
                        {1},
                        {2},
                        {3, 4},
                        {4, 5},
                        {7, 8},
                        {11, 8, 9},
                        {10, 9}},
            new int[] {});
        b.goToCodeLabel();
        b.write();*/

     // em serie vira em estrela
   /* Blink b = new Blink(new int[][] {
                        {1},
                        {1,2},
                        {2,3},
                        {3,4},
                        {4,5},
                        {5,6},
                        {6,7},
                        {7}},new int[] {});
        b.goToCodeLabel();
        b.write(); */


        ArrayList<GBlink> pieces = b.copy().breakMap();


        ArrayList<MapD> list = new ArrayList<MapD>();
        list.add(new MapD(b));
        for (GBlink bb: pieces)
            list.add(new MapD(bb));

        // dual
        list.add(new MapD(b.dual()));
        for (GBlink bb: pieces)
            list.add(new MapD(bb.dual()));

        // merge
        /*
        Blink m = b.copy();
        Blink md = m.dual();
        Blink.merge(m,m.findVertex(1),md,md.findVertex(1));
        list.add(new MapD(m));*/

        //
        list.add(new MapD(b.getRepresentant()));

        // desenhar o mapa
        JFrame f = new JFrame("Map Drawing");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(new Dimension(250,250));
        int n = (int) Math.ceil(Math.sqrt(list.size()));
        f.setContentPane(new DrawPanelMultipleMaps(list,n,n));
        f.setVisible(true);
        // desenhar o mapa

    }

    public static void testPreservingParityCode() {
        ArrayList<MapD> list = new ArrayList<MapD>();


        GBlink b = new GBlink(new int[][] {{1,2},{1,3,4},{2,5,4,3},{5}},new int[]{});
        b.goToCodeLabelAndDontCareAboutSpaceOrientation();
        b.write(); list.add(new MapD(b.copy()));
        b.goToCodeLabelChangingSpaceOrientation();
        b.write(); list.add(new MapD(b.copy()));
        b.goToCodeLabelChangingSpaceOrientation();
        b.write(); list.add(new MapD(b.copy()));
        b.goToDual();
        b.write(); list.add(new MapD(b.copy()));
        b.goToCodeLabelAndDontCareAboutSpaceOrientation();
        b.write(); list.add(new MapD(b.copy()));
        b.goToCodeLabelChangingSpaceOrientation();
        b.write(); list.add(new MapD(b.copy()));

        // desenhar o mapa
        JFrame f = new JFrame("Map Drawing");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(new Dimension(250,250));
        int n = (int) Math.ceil(Math.sqrt(list.size()));
        f.setContentPane(new DrawPanelMultipleMaps(list,n,n));
        f.setVisible(true);
        // desenhar o mapa

    }
}
