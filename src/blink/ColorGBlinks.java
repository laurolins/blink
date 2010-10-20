package blink;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

/**
 * Colors the given g-blinks without loosing any
 * space.
 */
public class ColorGBlinks {

    ArrayList<GBlink> _result;

    public ColorGBlinks(ArrayList<GBlink> list, String fileName) throws IOException {
        long t0 = System.currentTimeMillis();

        String s;
        int count=0;
        int blinks=0;


        HashSet<GBlink> R = new HashSet<GBlink>();

        for (GBlink m: list) {
            m = m.copy();
            System.out.println(String.format("mapa: %6d  #gblink: %6d   %7.2f seg.",(++count),R.size(),(System.currentTimeMillis()-t0)/1000.0));

            int ne = m.getNumberOfGEdges();

            // set of already processed blinks
            // for this map. this structure is
            // to capture the simmetrical blinks
            // and to include them only once.
            // HashSet<BlinkWord> S = new HashSet<BlinkWord>();

            //if (ne > 9)
            //    break;

            ArrayList<EdgePartitionPart> epp = m.getGEdgePartition();

            int N = (int) Math.pow(2,epp.size());
            for (int k=0;k<N;k++) {
            //for (int k=0;k<=0;k++) {

                blinks++;

                // para cada coloração válida
                for (int e=1;e<=epp.size();e++) {
                    BlinkColor cor = (k & (0x01 << (e - 1))) == 0 ? BlinkColor.green : BlinkColor.red;
                    epp.get(e-1).assign(m,cor);
                    // System.out.println("Assign "+cor+" to "+epp.get(e-1));
                    // System.out.println(""+m.getBlinkWord().toString());
                    // m.setColor(e, ((k & (0x01 << (e - 1))) == 0 ? BlinkColor.green : BlinkColor.red));
                    // System.out.println(String.format("cor %d = %s",e,""+m.getColor(e)));
                }

                m.goToCodeLabelPreservingSpaceOrientation();

                // System.out.println(""+m.getBlinkWord().toString());
                //if (m.getBlinkWord().toString().equals("8,17,2,5,4,11,6,10,7,14,9,15,12,18,13,3,16,1 2,3,6,8"))
                //    m = m;

                // only if this occurs
                if (m.numberOfRedEdgesGreaterThanNumberOfGreenEdges())
                    continue;

                // test to see if it contains eliminating ring
                // GBlink b =  new GBlink(bw);
                // b.setColor(k);
                if (m.containsAnEliminationRing() ||
                    m.containsSimplifyingReidemeisterIIPoint() ||
                    m.containsPointOfAlpha1Move() ||
                    m.hasAGZigZagThatIsFreeOfTheOthers())
                    continue;

                //
                GBlink mResult = m.getNewRepresentant(true,true,true);
                for (int i=1;i<8;i++) {
                    boolean parity = (i & 0x01) != 0;
                    boolean crossing = (i & 0x02) != 0;
                    boolean squares = (i & 0x04) != 0;
                    GBlink mSwap = m.copy();
                    mSwap.swap(parity,crossing,squares);
                    mSwap = mSwap.getNewRepresentant(true,true,true);
                    if (mSwap.compareTo(mResult) < 0)
                        mResult = mSwap;
                }

                // add mResult: note that it is a new object just created
                if (!R.contains(mResult)) {
                    R.add(mResult);
                }
            }
        }

        _result = new ArrayList<GBlink>(R);
        Collections.sort(_result);

        // Save file
        if (fileName != null) {
            PrintWriter pr = new PrintWriter(new FileWriter(fileName));
            for (GBlink G : list) {
                pr.println(G.getBlinkWord().toString());
            }
            pr.flush();
            pr.close();
        }
    }

    public ArrayList<GBlink> getResult() {
        return _result;
    }
}
