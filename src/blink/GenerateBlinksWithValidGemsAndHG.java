package blink;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

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
public class GenerateBlinksWithValidGemsAndHG {

    public GenerateBlinksWithValidGemsAndHG() {
    }


    static int _countFreeZigzags = 0;

    /*
    public static void main(String[] args) {
        GBlink G = new GBlink(new int[] {8,7,2,4,3,6,5,1}, new int[]{ 1});
        ArrayList<EdgePartitionPart> epp = G.getGEdgePartition();
        for (EdgePartitionPart p : epp) {
            System.out.println(""+p.toString());
        }
    }*/


    public static void main(String[] args) throws FileNotFoundException, IOException {
        long t = System.currentTimeMillis();
        // BufferedReader br = new BufferedReader(new FileReader("c:/combinedBlocks.txt"));
        BufferedReader br = new BufferedReader(new FileReader("c:/blocks9.txt"));
        //BufferedReader br = new BufferedReader(new FileReader("res/maps3TConnected.txt"));
        //PrintWriter pr = new PrintWriter(new FileWriter("res/mapsComCoresNovoRepresentanteFlip.txt"));
        //BufferedReader br = new BufferedReader(new FileReader("res/x.txt"));
        //PrintWriter pr = new PrintWriter(new FileWriter("res/y.txt"));

        String s;
        int count=0;
        int blinks=0;


        HashSet<GBlink> R = new HashSet<GBlink>();


        while ((s = br.readLine()) != null) {
            System.out.println(String.format("mapa: %6d  #gblink: %6d   %7.2f seg.",(++count),R.size(),(System.currentTimeMillis()-t)/1000.0));

            GBlink m = new GBlink(s);
            int ne = m.getNumberOfGEdges();

            // set of already processed blinks
            // for this map. this structure is
            // to capture the simmetrical blinks
            // and to include them only once.
            // HashSet<BlinkWord> S = new HashSet<BlinkWord>();

            //if (ne > 9)
            //    break;

            ArrayList<EdgePartitionPart> epp = m.getGEdgePartition();

            StringBuffer colors = new StringBuffer();

            int N = (int) Math.pow(2,epp.size());
            for (int k=0;k<N;k++) {
            //for (int k=0;k<=0;k++) {

                blinks++;

                // para cada coloração válida
                int countRed = 0;
                for (int e=1;e<=epp.size();e++) {
                    BlinkColor cor = (k & (0x01 << (e - 1))) == 0 ? BlinkColor.green : BlinkColor.red;
                    epp.get(e-1).assign(m,cor);
                    // System.out.println("Assign "+cor+" to "+epp.get(e-1));
                    // m.setColor(e, ((k & (0x01 << (e - 1))) == 0 ? BlinkColor.green : BlinkColor.red));
                    // System.out.println(String.format("cor %d = %s",e,""+m.getColor(e)));
                }

                // m.goToCodeLabelPreservingSpaceOrientation();

                // only if this occurs
                if (m.numberOfRedEdgesGreaterThanNumberOfGreenEdges())
                    continue;

                // test to see if it contains eliminating ring
                // GBlink b =  new GBlink(bw);
                // b.setColor(k);
                if (m.containsAnEliminationRing())
                    continue;

                // test if it has more than one zigzag ond
                // one of the zigzags is all under or is all over
                // the others (this implies that it is not
                // a prime space)
                if (m.hasAGZigZagThatIsFreeOfTheOthers()) {
                    _countFreeZigzags++;
                    // System.out.println("Free zigzag "+(_countFreeZigzags)+" "+m.getBlinkWord().toString());
                    continue;
                }

                //
                m = m.getNewRepresentant(true,true,true);

                //System.out.println(""+m.getBlinkWord().toString());

                // include blink word
                /*
                BlinkWord bw = m.goToCodeLabelConsideringTheColorsAtComparisions();
                if (S.contains(bw)) {
                    continue;
                } else {
                    S.add(bw);
                }*/

                // check if there is only one handle
                //Gem g = m.getGem();
                //if (m.getGem().getHandleNumber() > 0)
                //    continue;

                /*
                // test Reidemeister III
                ArrayList<PointOfReidemeisterIII> moves = m.findAllReidemeisterIIIPoints();
                boolean isRedundant = false;
                for (PointOfReidemeisterIII p: moves) {
                    GBlink copy = m.copy();
                    copy.applyReidemeisterIIIMove(p);
                    copy.goToCodeLabelPreservingSpaceOrientation();
                    copy = copy.getNewRepresentant();
                    if (copy.compareTo(m) < 0) {
                        // System.out.println("Found simpler GBlink");
                        isRedundant = true;
                        break;
                    }
                }
                if (isRedundant)
                    continue;
                */

                // test flip and dual
                /*
                {
                    GBlink dualRep = m.dual().getNewRepresentant();
                    if (dualRep.compareTo(m) < 0) {
                        System.out.println("Dual Filter");
                        // System.out.println("B = "+m.getBlinkWord().toString());
                        // System.out.println("Dual(B) = "+dualRep.getBlinkWord().toString());
                        continue;
                    }
                    GBlink reflectionRep = m.reflection().getNewRepresentant();
                    if (reflectionRep.compareTo(m) < 0) {
                        System.out.println("Reflection Filter");
                        continue;
                    }
                    GBlink refDualRep = m.refDual().getNewRepresentant();
                    if (refDualRep.compareTo(m) < 0) {
                        System.out.println("RefDual Filter");
                        continue;
                    }
                }*/

                //
                /*
                {
                    ArrayList<GBlink> list = m.getReidemeisterIIIClosure();
                    boolean unecessary = false;
                    for (GBlink g: list) {
                        if (g.containsSimplifyingReidemeisterIIPoint() ||
                            g.containsAnEliminationRing()) {
                            unecessary = true;
                            break;
                        }
                        //else if (m.compareTo(g) > 0) {
                        //    unecessary = true;
                        //    break;
                        //}
                    }
                    if (unecessary) {
                        System.out.println("Unecessary by RM3 closure");
                        continue;
                    }
                } */

                //
                if (!R.contains(m)) {
                    R.add(m.copy());
                }
            }
        }

        ArrayList<GBlink> list = new ArrayList<GBlink>(R);
        Collections.sort(list);
        PrintWriter pr = new PrintWriter(new FileWriter("c:/blocks9WithColor.txt"));
        for (GBlink G: list) {
            pr.println(G.getBlinkWord().toString());
        }
        pr.flush();
        br.close();

    }

    public static String reverseAndFillWithZeros(String s, int n) {
        StringBuffer sb = new StringBuffer();
        int i=0;
        for (;i<s.length();i++) {
            sb.append(s.charAt(s.length()-1-i));
        }
        for (;i<n;i++)
            sb.append("0");
        return sb.toString();
    }

}
