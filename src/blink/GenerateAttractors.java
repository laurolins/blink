package blink;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
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
public class GenerateAttractors {

    public GenerateAttractors() {
    }

    public static void main(String[] args) throws
            FileNotFoundException,
            IOException,
            SQLException,
            ClassNotFoundException {

        long t0 = System.currentTimeMillis();

        BlinkDB db = (BlinkDB) App.getRepositorio();

        GemRepository R = new GemRepository(db.getGems());

        HashMap<BlinkEntry, GemEntry> map = new HashMap<BlinkEntry, GemEntry>();

        ArrayList<BlinkEntry> bs = db.getBlinksWithoutGemAndWithNumEdges(9);

        Random r = new Random();
        for (int i=bs.size();i>=1;i--) {
            int index = r.nextInt(i);
            BlinkEntry be = bs.get(index);
            bs.set(index,bs.get(bs.size()-1));
            bs.set(bs.size()-1,be);
        }

        int count = 1;
        for (BlinkEntry be: bs) {
            double t = (System.currentTimeMillis()-t0)/1000.0;
            double est = t/(count-1) * (bs.size()-count)/60.0;
            System.out.print(String.format("Processing blink %6d  -  %6d / %6d     time: %6.2f seg.  est: %10.2f min.",be.get_id(),count++,bs.size(),t,est));

            Gem g = be.getBlink().getGem();
            g.goToCodeLabel();
            SearchAttractor A = new SearchAttractor(g,20000L);
            be.set_path(A.getBestPath());

            if (!A.isBestAttractorTSClassRepresentant()) {
                System.out.println(" not representant. tsClassSize: "+A.getBestAttractorTSClassSize());
                // continue;
            }
            else {
                System.out.println(" ok. tsClassSize: "+A.getBestAttractorTSClassSize());
            }

            // CalculateReductionGraph crg = new CalculateReductionGraph(be.getBlink().getGem());
            // Gem rep = crg.getRepresentant();
            GemEntry gEntry = R.getExistingGemEntryOrCreateNew(
                    A.getBestAttractorFound().getCurrentLabelling(),
                    A.getBestAttractorTSClassSize(),
                    A.isBestAttractorTSClassRepresentant());
            map.put(be, gEntry);

            // get list of not persistent Gems
            ArrayList<GemEntry> list = R.getNewEntriesLists();
            App.getRepositorio().insertGems(list);
            R.clearNewEntriesList(); // new entries have been updated

            // update be
            GemEntry ge = map.get(be);
            be.set_gem(ge.getId());

            // update gems on blinks
            ArrayList<BlinkEntry> listOfOne = new ArrayList<BlinkEntry>();
            listOfOne.add(be);
            App.getRepositorio().updateBlinksGems(listOfOne);
        }

        // exit
        System.exit(0);

    }
}
