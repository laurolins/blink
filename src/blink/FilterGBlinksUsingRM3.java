package blink;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;

/**
 * Colors the given g-blinks without loosing any
 * space.
 */
public class FilterGBlinksUsingRM3 {

    ArrayList<GBlink> _result;

    public FilterGBlinksUsingRM3(ArrayList<GBlink> list, String fileName) throws IOException {
        long t0 = System.currentTimeMillis();

        //
        LinkedList<GBlink> input = new LinkedList<GBlink>(list);
        HashSet<GBlink> output = new HashSet<GBlink>();

        //
        System.out.println("Reading input file...");

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
                if (GG.containsSimplifyingReidemeisterIIPoint() ||
                    GG.containsPointOfAdjacentOppositeCurls() ||
                    GG.containsAnEliminationRing() ||
                    GG.containsPointOfAlpha1Move() ||
                    GG.hasAGZigZagThatIsFreeOfTheOthers())
                    removeAllClass = true;
                // check also other reducing cross structures...
            }

            // remove all class
            input.removeAll(closureRep);
            if (!removeAllClass) {
                output.add(G);
            }
        }

        _result = new ArrayList<GBlink>(output);
        Collections.sort(_result);

        // export gblinks
        if (fileName != null) {
            PrintWriter pr = new PrintWriter(new FileWriter(fileName));
            for (GBlink G : _result) {
                pr.println(G.getBlinkWord().toString());
                pr.flush();
            }
            pr.close();
        }
        System.out.println(String.format("\nFinished: output size %6d time elapsed %6.2f seg.",
                           output.size(),
                           (System.currentTimeMillis()-t0)/1000.0));

    }

    public ArrayList<GBlink> getResult() {
        return _result;
    }
}
