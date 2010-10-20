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
public class GenerateGemsForSomeBlinks {

    public GenerateGemsForSomeBlinks() {
    }

    public static void main(String[] args) throws
            FileNotFoundException,
            IOException,
            SQLException,
            ClassNotFoundException {

        BlinkDB db = (BlinkDB) App.getRepositorio();

        GemRepository R = new GemRepository(db.getGems());

        HashMap<BlinkEntry, GemEntry> map = new HashMap<BlinkEntry, GemEntry>();

        ArrayList<BlinkEntry> bs = db.getBlinksWithoutGemAndWithNumEdges(9);

        // search attractor
        SimpleSearchAttractor s = new SimpleSearchAttractor();

        // randomize list
        Random rand = new Random();
        int k = bs.size();
        while (k > 0) {
            int index = rand.nextInt(k);
            BlinkEntry currentlast = bs.get(bs.size()-1);
            BlinkEntry newLast = bs.get(index);
            bs.set(bs.size()-1,newLast);
            bs.set(index,currentlast);
            k--;
        }

        int count = 1;
        for (BlinkEntry be: bs) {
            System.out.println(String.format("Processing blink %d  -  %4d / %4d",be.get_id(),count++,bs.size()));

            s.setBlink(be.getBlink());
            Thread t = new Thread(s);
            t.start();

            long time = System.currentTimeMillis();
            while (t.isAlive() && System.currentTimeMillis()-time < 360000) {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ex) {
                    ex.printStackTrace();
                }
            }

            if (t.getState() != Thread.State.TERMINATED) {
                System.out.println("Stop!");
                t.stop();
                continue;
            }

            // CalculateReductionGraph crg = new CalculateReductionGraph(be.getBlink().getGem());
            // Gem rep = crg.getRepresentant();
            GemEntry gEntry = R.getExistingGemEntryOrCreateNew(s.getTSClassRepresentant().getCurrentLabelling(), s.getTSClassSize(),true);
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
    }
}

class  SimpleSearchAttractor implements Runnable {
    GBlink _blink;
    Gem _tsClassRepresentant;
    int _tsClassSize;

    public SimpleSearchAttractor() {
    }

    public void setBlink(GBlink b) {
        _blink = b;
    }

    public void run() {
        if(_blink == null)
            return;
        CalculateReductionGraph crg = new CalculateReductionGraph(_blink.getGem());
        _tsClassRepresentant = crg.getRepresentant();
        _tsClassSize = crg.getTSClassSize();
    }

    public Gem getTSClassRepresentant() {
        return _tsClassRepresentant;
    }

    public int getTSClassSize() {
        return _tsClassSize;
    }
}
