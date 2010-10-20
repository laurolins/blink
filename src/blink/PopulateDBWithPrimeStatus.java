package blink;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

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
public class PopulateDBWithPrimeStatus {

    public PopulateDBWithPrimeStatus() throws SQLException, ClassNotFoundException, IOException {
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
                System.out.println("Processing gem "+ge.getId()+"...");

                // System.out.println(String.format("QI %6d/%6d", ++j, n));
                GemPrimeStatus status = gpt.test(ge.getGem());
                ge.setStatus(status);
            }

            // update qis
            t = System.currentTimeMillis();
            db.updateGemStatus(bs);
            // System.out.println(String.format("Updated QIs %d blinks in %.2f sec.", bs.size(), (System.currentTimeMillis() - t) / 1000.0));

            // update index
            k = k+delta;
        }

        System.out.println(String.format("Total Time to calculate status %.2f sec.",(System.currentTimeMillis() - t0) / 1000.0));


        System.exit(0);
    }

    public static void main(String[] args)  throws SQLException, ClassNotFoundException, IOException {
        new PopulateDBWithPrimeStatus();
    }
}
