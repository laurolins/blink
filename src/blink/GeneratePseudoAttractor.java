package blink;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

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
public class GeneratePseudoAttractor {

    public GeneratePseudoAttractor() {
    }

    public static void main(String[] args) throws FileNotFoundException, IOException, SQLException,
            ClassNotFoundException {
        BlinkDB db = (BlinkDB) App.getRepositorio();
        long t0 = System.currentTimeMillis();

        GemRepository R = new GemRepository(db.getGems());
        HashMap<BlinkEntry, GemEntry> _map = new HashMap<BlinkEntry, GemEntry>();

        long minmax[] = db.getMinMaxBlinkIDs();
        // minmax[0] = 1; minmax[1] = 61;
        minmax[0] = 1001; minmax[1] = 10000;
        int delta = 10;
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
                GBlink b = new GBlink(new MapWord(be.get_mapCode()));
                b.setColor((int) be.get_colors());

                // calculate pseudo-attractor
                GemFromBlink gfb = new GemFromBlink(b);
                Gem lblFromBlink = gfb.getGem();

                GenerateRepresentantGem grg = new GenerateRepresentantGem(lblFromBlink);
                GemPackedLabelling lblRepresentantFromBlink = grg.getRepresentant();
                int tsClassSize = grg.getTSClassSize();

                System.out.println("Calculated "+count++);

                // add to repository
                GemEntry ge = R.getExistingGemEntryOrCreateNew(lblRepresentantFromBlink,tsClassSize,true);

                // save on map
                _map.put(be, ge);
            }

            System.out.println(String.format("Time to calculate QIs %.2f sec.", (System.currentTimeMillis() - t) / 1000.0));

            //
            ArrayList<GemEntry> list = R.getNewEntriesLists(); // get list of not persistent QIs
            t = System.currentTimeMillis();
            db.insertGems(list);
            R.clearNewEntriesList(); // new entries have been updated
            System.out.println(String.format("Inserted %d QIs in %.2f sec.", list.size(), (System.currentTimeMillis() - t) / 1000.0));

            // updating biEntry
            for (BlinkEntry be : bs) {
                GemEntry ge = _map.get(be);
                be.set_gem(ge.getId());
            }

            // update gems on blinks
            t = System.currentTimeMillis();
            db.updateBlinksGems(bs);
            // System.out.println(String.format("Updated QIs %d blinks in %.2f sec.", bs.size(), (System.currentTimeMillis() - t) / 1000.0));

            // update index
            k = k+delta;
        }

        System.out.println(String.format("Total Time to calculate Gems %.2f sec.",(System.currentTimeMillis() - t0) / 1000.0));

        System.exit(0);
    }
}
