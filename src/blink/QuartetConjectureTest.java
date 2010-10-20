package blink;

import java.awt.Toolkit;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Every composite (non-prime) 3-manifold presentation by 3-gem has a quartet.
 */
public class QuartetConjectureTest {

    public QuartetConjectureTest() throws SQLException, ClassNotFoundException, IOException {






        BlinkDB db = (BlinkDB) App.getRepositorio();
        long t0 = System.currentTimeMillis();

        GemPrimeTest gpt = new GemPrimeTest();

        // fill in QIRepository
        long minmax[] = db.getMinMaxGemIDs();
        int delta = 2500;
        int count = 1;
        long k = minmax[0];
        while (k <= minmax[1]) {
            System.out.println(String.format("From id %d to id %d", k,k+delta-1));

            long t = System.currentTimeMillis();
            ArrayList<GemEntry> bs = db.getGemsByIDInterval(k, k+delta-1);
            // System.out.println(String.format("Retrieved %d blinks in %.2f sec.", bs.size(), (System.currentTimeMillis() - t) / 1000.0));

            t = System.currentTimeMillis();

            for (GemEntry ge : bs) {
                if (ge.getGemPrimeStatus() == GemPrimeStatus.COMPOSITE_DISCONNECTING_QUARTET_WITH_DIFFERENT_HOMOLOGY_GROUP ||
                    ge.getGemPrimeStatus() == GemPrimeStatus.COMPOSITE_FROM_NON_DISCONNECTING_QUARTET) {

                    System.out.println(String.format("Processing gem "+ge.getId()+"..."));

                    GenerateTSClass GTS = new GenerateTSClass(ge.getGem());
                    for (GemPackedLabelling glbl: GTS.getTSClass()) {
                        Gem g = new Gem(glbl);
                        if (g.findAllNonTrivialQuartets().size() == 0) {
                            System.out.println("Counterexample found on gem "+ge.getId()+" TS-class");
                            Toolkit.getDefaultToolkit().beep();
                        }
                    }
                }
            }

            // update qis
            t = System.currentTimeMillis();
            // System.out.println(String.format("Updated QIs %d blinks in %.2f sec.", bs.size(), (System.currentTimeMillis() - t) / 1000.0));

            // update index
            k = k+delta;
        }

        System.out.println(String.format("Total Time to calculate status %.2f sec.",(System.currentTimeMillis() - t0) / 1000.0));
        System.exit(0);
    }

    public static void main(String[] args)  throws SQLException, ClassNotFoundException, IOException {

        Library.playSound("tada.wav",1000L);
        //new PopulateDBWithPrimeStatus();
        System.exit(0);
    }

}
