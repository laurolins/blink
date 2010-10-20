package blink;

import java.awt.Toolkit;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
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
public class Temp {
    public Temp() {
    }

    public static void main(String[] args) throws ClassNotFoundException, IOException, SQLException {
        // expandSomeQuantumInvariants();
        // testIfSpaceIsPresent();
        // generatePrimeList();
        // testIfSpaceIsPresent();
        // testOfRepresentantWithFlipsAndDualFlips();
        // testMergingTheorems();
        // testIfRepresentantIsItself();
        // filterBlocks();
        // filterBlocks2();
        // filterEqualBlocks();
        // testIfRecoloringOfARepresentantIsItselfARepresentant();
        // combineBlocks();
        filterGBlinks();
    }

    /**
     * Filter a set of representant g-blinks.
     */
    public static void filterGBlinks() throws FileNotFoundException, IOException {
        long t0 = System.currentTimeMillis();

        //
        LinkedList<GBlink> input = new LinkedList<GBlink>();
        ArrayList<GBlink> output = new ArrayList<GBlink>();

        //
        System.out.println("Reading input file...");

        //
        BufferedReader br = new BufferedReader(new FileReader("res/spaces9.txt"));
        String s;
        while ((s = br.readLine()) != null) {
            GBlink G = new GBlink(s);
            input.add(G);
        }


        //
        Collections.sort(input);

        //
        while (!input.isEmpty()) {
            GBlink G = input.poll();

            System.out.println(String.format("output: %6d    input: %6d   time: %6.2f seg.",
                               output.size(),
                               input.size(),
                               (System.currentTimeMillis()-t0)/1000.0));

            //
            boolean removeAllClass = false;

            // find closure and representant
            ArrayList<GBlink> closure = G.getReidemeisterIIIClosure();
            ArrayList<GBlink> closureRep = new ArrayList<GBlink>();
            for (GBlink GG: closure) {
                GBlink rGG = GG.getNewRepresentant(true,true,true);
                if (!closureRep.contains(rGG))
                    closureRep.add(rGG);
            }

            //
            for (GBlink GG: closure) {
                if (GG.containsSimplifyingReidemeisterIIPoint())
                    removeAllClass = true;
                // check also other reducing cross structures...
            }

            // remove all class
            if (removeAllClass) {
                for (GBlink GG: closure)
                    input.remove(GG.getNewRepresentant(true,true,true));
            }
            else {
                closureRep.remove(G);
                input.removeAll(closureRep);
                output.add(G);
            }
        }

        Collections.sort(output);

        PrintWriter pr = new PrintWriter(new FileWriter("res/filteredSpaces9.txt"));
        for (GBlink G: output) {
            pr.println(G.getBlinkWord().toString());
            pr.flush();
        }
        pr.close();

        System.out.println(String.format("\nFinished: output size %6d time elapsed %6.2f seg.",
                           output.size(),
                           (System.currentTimeMillis()-t0)/1000.0));

    }





    public static void combineBlocks() throws FileNotFoundException, IOException {
        BufferedReader br = new BufferedReader(new FileReader("c:/blocks9.txt"));

        ArrayList<GBlink> blocks = new ArrayList<GBlink>();

        long t0 = System.currentTimeMillis();

        //
        String s;
        System.out.println("Reading file...");
        while ((s = br.readLine()) != null) {
            GBlink G = new GBlink(s);
            // System.out.println(""+G.getBlinkWord().toString());
            blocks.add(G);
        }

        int max = 10;
        ArrayList[] lists = new ArrayList[max+1];
        lists[1] = blocks;
        for (int ii=2;ii<=max;ii++) {
            lists[ii] = new ArrayList<GBlink>();
        }

        int count = lists[1].size();
        int maxNumGEdges=9;
        int k=2;
        boolean somethingNew = true;
        while (k<=max && somethingNew) {
            System.out.println(String.format("\nProcessing step %6d combined maps size %6d time elapsed %6.2f seg.",
                               k,
                               count,
                               (System.currentTimeMillis()-t0)/1000.0));
            somethingNew = false;
            int kk = 1;
            for (GBlink B: (ArrayList<GBlink>) blocks) {
                System.out.print((kk++)+" ");
                if (kk % 100 == 0)
                    System.out.println("");
                ArrayList<GBlinkVertex> Bz = B.getOneVertexForEachGZigzag();
                for (GBlink G : (ArrayList<GBlink>)lists[k - 1]) {
                    if (B.getNumberOfGEdges() + G.getNumberOfGEdges() <= maxNumGEdges) {
                        ArrayList<GBlinkVertex> Gz = G.getOneVertexForEachGZigzag();
                        for (int i=0;i<Bz.size();i++) {
                            for (int j=0;j<Gz.size();j++) {
                                GBlink BB = B.copy();
                                GBlink GG = G.copy();
                                GBlinkVertex vBB = BB.findVertex(Bz.get(i).getLabel());
                                GBlinkVertex vGG = GG.findVertex(Gz.get(j).getLabel());
                                if (vBB.hasEvenLabel()) vBB = vBB.getNeighbour(GBlinkEdgeType.edge);
                                if (vGG.hasEvenLabel()) vGG = vGG.getNeighbour(GBlinkEdgeType.edge);
                                GBlink.merge(BB,vBB,GG,vGG);

                                if (!lists[k].contains(BB)) {
                                    lists[k].add(BB);
                                    somethingNew = true;
                                    count++;
                                }
                            }
                        }
                    }
                }
            }
            k++;
        }

        System.out.println(String.format("\nFinished: combined maps size %6d time elapsed %6.2f seg.",
                           count,
                           (System.currentTimeMillis()-t0)/1000.0));

        //
        ArrayList<GBlink> completeList = new ArrayList<GBlink>();
        for (int kk=1;kk<=max;kk++) {
            ArrayList<GBlink> list = (ArrayList<GBlink>) lists[kk];
            completeList.addAll(list);
        }
        Collections.sort(completeList);

        System.out.println("Writing File...");
        PrintWriter pr = new PrintWriter(new FileWriter("c:/combinedBlocks.txt"));
        for (GBlink G: completeList) {
            pr.println(G.getBlinkWord().toString());
            pr.flush();
        }
        pr.close();

    }

    public static void testIfRecoloringOfARepresentantIsItselfARepresentant() throws SQLException, IOException {
        int testNo = 0;
        Random r = new Random();
        while (true) {
            testNo++;
            int numEdges1 = 7; //3+r.nextInt(4);
            GBlink G = GBlink.random(numEdges1);
            GBlink rG = G.getNewRepresentant(true,true,true);
            GBlink rGRandColors = rG.copy();
            for (int i=1;i<=rGRandColors.getNumberOfGEdges();i++) {
                rGRandColors.setColor(i,(r.nextBoolean() ? BlinkColor.green : BlinkColor.red));
            }
            rGRandColors.goToCodeLabelPreservingSpaceOrientation();
            GBlink rrGRandColors = rGRandColors.getNewRepresentant(true,true,true);


            if (rGRandColors.getMapWord().compareTo(rrGRandColors.getMapWord())==0) {
                System.out.println("test "+testNo+" OK...");
            }
            else {
                System.out.println("failure...");
                System.out.println("rG: "+rG.getBlinkWord().toString());
                System.out.println("randColors(rG): "+rGRandColors.getBlinkWord().toString());
                System.out.println("rep(randColors(rG)): "+rrGRandColors.getBlinkWord().toString());
            }
        }
    }


    public static void filterEqualBlocks() throws SQLException, IOException {
        // strings
        ArrayList<String> strings = new ArrayList<String>();

        //
        BufferedReader br = new BufferedReader(new FileReader("c:/gblinkOfBlocks9.txt"));
        PrintWriter pr = new PrintWriter(new FileWriter("c:/z.txt"));

        HashSet<GBlink> U = new HashSet<GBlink>();


        //
        String s;
        int i = 1;
        while ((s = br.readLine()) != null) {
            System.out.println("Processing line "+(i++)+" U size: "+U.size());
            GBlink G = new GBlink(s);

            if (U.contains(G))
                continue;

            U.add(G);
            pr.println(G.getBlinkWord().toString());
            pr.flush();
        }
        pr.close();
        System.exit(0);
    }


    public static void filterBlocks() throws SQLException, IOException {
        // strings
        ArrayList<String> strings = new ArrayList<String>();

        //
        BufferedReader br = new BufferedReader(new FileReader("c:/gblinkOfBlocks9.txt"));
        PrintWriter pr = new PrintWriter(new FileWriter("c:/filteredGblinkOfBlocks9.txt"));

        ArrayList<GBlink> U = new ArrayList<GBlink>();

        //
        String s;
        int i = 1;
        while ((s = br.readLine()) != null) {
            System.out.println("Processing line "+(i++)+" U size: "+U.size());
            GBlink G = new GBlink(s);

            if (U.contains(G))
                continue;

            boolean add = true;
            ArrayList<PointOfReidemeisterIII> points = G.findAllReidemeisterIIIPoints();
            for (PointOfReidemeisterIII p: points) {
                GBlink Gcopy = G.copy();
                Gcopy.applyReidemeisterIIIMove(p);
                Gcopy = Gcopy.getNewRepresentant(true,true,true);
                if (G.compareTo(Gcopy) < 0) {
                    add = false;
                    break;
                }
            }

            if (add) {
                U.add(G);
                pr.println(G.getBlinkWord().toString());
                pr.flush();
            }
        }
        pr.close();
        System.exit(0);
    }



    public static void filterBlocks2() throws SQLException, IOException {
        // strings
        ArrayList<String> strings = new ArrayList<String>();

        //
        BufferedReader br = new BufferedReader(new FileReader("c:/filteredGblinkOfBlocks9.txt"));
        PrintWriter pr = new PrintWriter(new FileWriter("c:/filteredGblinkOfBlocks9WithoutRM2.txt"));

        ArrayList<GBlink> U = new ArrayList<GBlink>();

        //
        String s;
        int i = 1;
        while ((s = br.readLine()) != null) {
            System.out.println("Processing line "+(i++)+" U size: "+U.size());
            GBlink G = new GBlink(s);

            if (U.contains(G))
                continue;

            boolean add = true;
            ArrayList<PointOfReidemeisterIII> points = G.findAllReidemeisterIIIPoints();
            for (PointOfReidemeisterIII p: points) {
                GBlink Gcopy = G.copy();
                Gcopy.applyReidemeisterIIIMove(p);
                if (Gcopy.containsSimplifyingReidemeisterIIPoint()) {
                    add = false;
                    break;
                }
            }

            if (add) {
                U.add(G);
                pr.println(G.getBlinkWord().toString());
                pr.flush();
            }
        }
        pr.close();
        System.exit(0);
    }



    public static void testOfRepresentantWithFlipsAndDualFlips() throws SQLException, IOException {
        // strings
        ArrayList<String> strings = new ArrayList<String>();

        //
        BufferedReader br = new BufferedReader(new FileReader("res/y.txt"));
        PrintWriter pr = new PrintWriter(new FileWriter("res/filteredDoneDualReflectionRefDual.txt"));

        //
        String s;
        int i = 1;
        while ((s = br.readLine()) != null) {
            System.out.print("Processing line "+(i++));
            GBlink G = new GBlink(s);
            GBlink rG = G.getNewRepresentant(true,true,true);
            String sRep = rG.getBlinkWord().toString();
            if (!strings.contains(sRep)) {
                strings.add(sRep);
                System.out.println(" Remains: "+strings.size());
                pr.println(sRep+"\t"+G.getBlinkWord().toString());
            }
            else {
                System.out.println(" removing...");
                System.out.println("G  = "+G.getBlinkWord().toString());
                System.out.println("rG = "+rG.getBlinkWord().toString());
                System.out.println("");
            }
            pr.flush();
        }
        pr.close();
        System.exit(0);
    }


    public static void testIfRepresentantIsItself() throws SQLException, IOException {
        // strings
        ArrayList<String> strings = new ArrayList<String>();

        //
        BufferedReader br = new BufferedReader(new FileReader("res/mapsDualERefDual.txt"));
        // PrintWriter pr = new PrintWriter(new FileWriter("res/isRepresentant"));

        //
        String s;
        int i = 1;
        while ((s = br.readLine()) != null) {
            System.out.println("Processing line "+(i++));
            GBlink G = new GBlink(s);
            GBlink Grep = G.getNewRepresentant(true,false,true);
            //GBlink Grep.goToCodeLabelPreservingSpaceOrientation();
            if (Grep.compareTo(G) != 0) {
                System.out.println("Found difference");
                System.out.println(""+G.getBlinkWord().toString());
                System.out.println(""+Grep.getBlinkWord().toString());
            }
        }
        // pr.close();
        System.exit(0);
    }




    public static void testMergingTheorems() throws SQLException, IOException {
        //long minmax[] = App.getRepositorio().getMinMaxBlinkIDs();
        long minmax[] = {1L, 1000L};

        long t = System.currentTimeMillis();
        PrintWriter pw = new PrintWriter(new FileWriter("res/theoremProblems"));
        Random r = new Random();

        long testNo=0;

        while (true) {

            testNo++;

            int numEdges1 = 3+r.nextInt(4);
            int numEdges2 = 3+r.nextInt(4);

            GBlink G1 = GBlink.random(numEdges1);
            GBlink G2 = GBlink.random(numEdges2);

            int labelV1 = r.nextInt(G1.getNumGBlinkVertices())+1;
            int labelV2 = r.nextInt(G2.getNumGBlinkVertices())+1;

            GBlink sum,sumDual,sumReflection,sumRefDual;

            { // sum
                GBlink cG1 = G1.copy();
                GBlink cG2 = G2.copy();
                GBlinkVertex v1 = cG1.findVertex(labelV1);
                GBlinkVertex v2 = cG2.findVertex(labelV2);
                GBlink.merge(cG1,v1,cG2,v2);
                sum = cG1;
            }

            { // sum dual
                GBlink cG1 = G1.copy();
                GBlink cG2 = G2.copy();
                GBlinkVertex v1 = cG1.findVertex(labelV1);
                GBlinkVertex v2 = cG2.findVertex(labelV2);
                cG2.goToDual();
                GBlink.merge(cG1,v1,cG2,v2);
                sumDual = cG1;
            }

            { // sum reflection
                GBlink cG1 = G1.copy();
                GBlink cG2 = G2.copy();
                GBlinkVertex v1 = cG1.findVertex(labelV1);
                GBlinkVertex v2 = cG2.findVertex(labelV2);
                cG2.goToReflection();
                GBlink.merge(cG1,v1,cG2,v2);
                sumReflection = cG1;
            }

            { // sum refdual
                GBlink cG1 = G1.copy();
                GBlink cG2 = G2.copy();
                GBlinkVertex v1 = cG1.findVertex(labelV1);
                GBlinkVertex v2 = cG2.findVertex(labelV2);
                cG2.goToRefDual();
                GBlink.merge(cG1,v1,cG2,v2);
                sumRefDual = cG1;
            }



            // compare
            QI qiSum = sum.optimizedQuantumInvariant(3,8);
            QI qiDualSum = sumDual.optimizedQuantumInvariant(3,8);
            QI qiReflectionSum = sumReflection.optimizedQuantumInvariant(3,8);
            QI qiRefDualSum = sumRefDual.optimizedQuantumInvariant(3,8);

            HomologyGroup hgSum = sum.homologyGroupFromGBlink();
            HomologyGroup hgDualSum = sumDual.homologyGroupFromGBlink();
            HomologyGroup hgReflectionSum = sumReflection.homologyGroupFromGBlink();
            HomologyGroup hgRefDualSum = sumRefDual.homologyGroupFromGBlink();

            boolean okDual = qiSum.compareNormalizedEntries(qiDualSum) && hgSum.compareTo(hgDualSum) == 0;
            boolean okReflection = qiSum.compareNormalizedEntries(qiReflectionSum) && hgSum.compareTo(hgReflectionSum) == 0;
            boolean okRefDual = qiSum.compareNormalizedEntries(qiRefDualSum) && hgSum.compareTo(hgRefDualSum) == 0;

            StringBuffer sb = new StringBuffer();


            if (!okDual) {
                sb.append("problem with DUAL sum on test "+testNo+"\n");
            }
            if (!okReflection) {
                sb.append("problem with REFLECTION sum on test "+testNo+"\n");
            }
            if (!okRefDual) {
                System.out.println("problem with REFDUAL sum on test "+testNo+"\n");
            }

            if (okDual && okReflection && okRefDual) {
                System.out.println("Test "+testNo+" "+G1.getNumberOfGEdges()+"+"+G2.getNumberOfGEdges()+" ... OK "+String.format("%7.2fseg.",(System.currentTimeMillis()-t)/1000.0));
            }
            else {
                Toolkit.getDefaultToolkit().beep();
                sb.append(G1.getBlinkWord().toString()+"\n");
                sb.append(G2.getBlinkWord().toString()+"\n");
                sb.append("Vertices: " + labelV1 + " " + labelV2+"\n");
                System.out.println(""+sb.toString());
                pw.println(sb.toString());
                pw.flush();
            }
        }

    }



    public static void mainOld(String[] args) throws SQLException, IOException {
        ArrayList<BlinkEntry> bes = App.getRepositorio().getBlinks(0,12);
        PrintWriter pr = new PrintWriter(new FileWriter("res/blinksHGQI-467.txt"));
        for (BlinkEntry be: bes) {
            pr.println(String.format("%s\t%s\t%d\t%s",
                          ""+be.get_qi(),
                          be.get_hg(),
                          be.get_id(),
                          be.getBlink().getBlinkWord().toString()));
        }
        pr.close();
        System.out.println("Finished");
        System.exit(0);
    }

    public static void generatePrimeList() throws SQLException, IOException, ClassNotFoundException {
        ArrayList<ClassHGNormQI> classes = App.getRepositorio().getHGNormQIClasses(App.MAX_EDGES);
        PrintWriter pr = new PrintWriter(new FileWriter("res/oldRepresentantesThatMayBePrime.txt"));
        for (ClassHGNormQI c: classes) {
            System.out.print("Processing class QI:"+c.getStringOfQIs()+" HG:"+c.get_hg());
            c.load();
            BlinkEntry be = c.getBlinks().get(0);
            if (be.get_numEdges() == 0) {
                be = c.getBlinks().get(1);
            }

            boolean mayBePrime = true;
            if (be.get_gem() > 0) {
                GemEntry ge = App.getRepositorio().getGemById(be.get_gem());
                if (ge.getGemPrimeStatus() != GemPrimeStatus.PRIME &&
                    ge.getGemPrimeStatus() != GemPrimeStatus.POTENTIALLY_PRIME) {
                    mayBePrime = false;
                }
            }

            if (mayBePrime) {
                GBlink G = be.getBlink();
                pr.println(String.format("%s\t%s",
                           G.getMapWord().toString(),
                           Library.intArrayToString(G.getReds())));
            }

            System.out.println(mayBePrime ? " MAY BE PRIME" : "");

        }
        pr.close();
        System.exit(0);
    }

    public static void testIfSpaceIsPresent() throws SQLException, IOException, ClassNotFoundException {

        // load classes
        ArrayList<ClassHGNormQI> classes = App.getRepositorio().getHGNormQIClasses(App.MAX_EDGES);

        //
        BufferedReader br = new BufferedReader(new FileReader("res/oldRepresentantesThatMayBePrime.txt"));
        PrintWriter pr = new PrintWriter(new FileWriter("res/missing2.txt"));

        //
        String s;
        int i = 1;
        while ((s = br.readLine()) != null) {

            System.out.println("Processing line "+(i++));

            GBlink G = new GBlink(s);

            // qi and homology group
            QI qi = G.optimizedQuantumInvariant(3,8);
            String hg = G.homologyGroupFromGBlink().toString();

            // check if it is the HG and QI of class
            ClassHGNormQI probableClass = null;
            for (ClassHGNormQI c: classes) {
                if (!c.get_hg().equals(hg))
                    continue;

                // check QI
                QI qiClass = App.getRepositorio().getQI(c.get_qi(0));
                if (qiClass.compareNormalizedEntriesUntilMaxR(qi)) {
                    probableClass = c;
                    break;
                }
            }

            if (probableClass != null) {
                pr.println(String.format("%s\t%s\t%s",
                           "QI:"+probableClass.get_qi(0)+" HG:"+probableClass.get_hg(),
                           G.getMapWord().toString(),
                           Library.intArrayToString(G.getReds())));
            }
            else {
                pr.println(String.format("%s\t%s\t%s",
                           "NOTFOUND",
                           G.getMapWord().toString(),
                           Library.intArrayToString(G.getReds())));

            }
            pr.flush();
        }
        pr.close();
        System.exit(0);
    }



    public static void expandSomeQuantumInvariants() throws SQLException, IOException {
        long qis[] = { 584 };//1,120,15,177,21,27,33,331,4,413,415,43,447,492,500,53,73 };

        HashMap<Long,QI> map = new HashMap<Long,QI>();
        HashMap<BlinkEntry,QI> mapBlink2NewQI = new HashMap<BlinkEntry,QI>();

        QIRepository R = new QIRepository();
        int qiCount = 0;
        for (long qiID: qis) {

            // long minGem = be.getMinGem();

            for (BlinkEntry be: App.getRepositorio().getBlinksByQI(App.MAX_EDGES, qiID)) {

                System.out.println("Upgrading QI " + (++qiCount));

                QI newQI = be.getBlink().optimizedQuantumInvariant(3, 8);

                QI qiUpgraded = R.add(newQI);
                if (qiUpgraded == null)
                    qiUpgraded = newQI;

                map.put(qiID, qiUpgraded);
                mapBlink2NewQI.put(be, qiUpgraded);
            }
            // minGemOfTheUpgradedInstance.put(qiID,minGem);
        }

        // insert qis
        App.getRepositorio().insertQIs(R.getList());

        //
        PrintWriter pw = new PrintWriter(new FileWriter("log/expandSomeQIs.txt"));
        ArrayList<BlinkEntry> entries = new ArrayList<BlinkEntry>();
        for (BlinkEntry be: mapBlink2NewQI.keySet()) {
            QI qi = mapBlink2NewQI.get(be);
            pw.println("Blink "+be.get_id()+" and qi="+be.get_qi()+" will have it's qi updated to "+qi.get_id());
            be.set_qi(qi.get_id());
            entries.add(be);
        }
        pw.close();
        App.getRepositorio().updateBlinksQI(entries);

        System.exit(0);

    }

}








