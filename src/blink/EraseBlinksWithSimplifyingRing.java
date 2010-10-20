package blink;

import java.io.FileNotFoundException;
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
public class EraseBlinksWithSimplifyingRing {
    public EraseBlinksWithSimplifyingRing() {
    }
    public static void main(String[] args) throws FileNotFoundException, IOException, SQLException {
        BlinkDB db = (BlinkDB) App.getRepositorio();

        ArrayList<BlinkEntry> entriesToRemove = new ArrayList<BlinkEntry>();

        long minmax[] = db.getMinMaxBlinkIDs();
        int delta = 100;
        int count = 1;
        long k = minmax[0];
        while (k <= minmax[1]) {

            System.out.println(String.format("From id %d to id %d", k,k+delta-1));


            long t = System.currentTimeMillis();
            ArrayList<BlinkEntry> bs = db.getBlinksByIDInterval(k, k+delta-1);
            // System.out.println(String.format("Retrieved %d blinks in %.2f sec.", bs.size(), (System.currentTimeMillis() - t) / 1000.0));

            t = System.currentTimeMillis();

            for (BlinkEntry be : bs) {
                // System.out.println(String.format("QI %6d/%6d", ++j, n));
                GBlink b = be.getBlink();
                if (b.containsAnEliminationRing()) {
                    entriesToRemove.add(be);
                }
            }

            // update index
            k = k+delta;
        }

        StringBuffer sb = new StringBuffer();
        sb.append("delete from blink where id in (");
        boolean first = true;
        for (BlinkEntry be: entriesToRemove) {
            if (!first)
                sb.append(',');
            sb.append(be.get_id());
            first = false;
        }
        sb.append(')');
        System.out.println("Eliminate: "+entriesToRemove.size());
        System.out.println(""+sb.toString());
        System.exit(0);
    }
}
