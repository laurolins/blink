package blink;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
public class ReRunGems {
    public ReRunGems() throws SQLException, IOException, ClassNotFoundException {
        HashMap<Long,GemEntry> mapGemId2gGemEntry = App.getRepositorio().getGemsMap();

        ArrayList<ClassQI> classes = App.getRepositorio().getQIClasses(App.MAX_EDGES);
        int k = 0;
        ArrayList<BlinkEntry> entries = new ArrayList<BlinkEntry>();
        for (ClassQI c: classes) {

            System.out.print(String.format("QI Class (%d/%d): %d ",(++k),classes.size(),c.get_qi()));
            c.load();

            // get blink entries
            ArrayList<BlinkEntry> blinkEntries = c.getBlinks();

            HashMap<Long,BlinkEntry> map = new HashMap<Long,BlinkEntry>();
            for (BlinkEntry be: blinkEntries) {
                long gem = be.get_gem();
                if (map.get(gem) == null) {
                    map.put(gem,be);
                }
            }

            if (map.size()<=1) { // ok. no doubts.
                System.out.println("No Doubt");
                continue;
            }
            else {
                System.out.println("...");
            }

            // now filter only blinks whose gem is not
            // the minimum
            long minimumGemId = 0;
            Gem minimumGem = null;
            for (long gemId: map.keySet()) {
                Gem g = new Gem(mapGemId2gGemEntry.get(gemId).getLabelling());
                if (minimumGem == null) {
                    minimumGem = g;
                    minimumGemId = gemId;
                }
                else if (g.compareTo(minimumGem) < 0) {
                    minimumGem = g;
                    minimumGemId = gemId;
                }
            }

            // add only non minimum blink entries
            for (BlinkEntry be: c.getBlinks()) {
                if (be.get_gem() != minimumGemId) {
                    entries.add(be);
                }
            }
        }

        //
        System.out.println("List Size: "+entries.size());
        int n = entries.size();
        Random rand = new Random();
        while (n > 0) {
            int index = rand.nextInt(n);
            BlinkEntry aux = entries.get(index);
            entries.set(index,entries.get(n-1));
            entries.set(n-1,aux);
            n--;
        }
        runList(entries);
    }

    public void runList(ArrayList<BlinkEntry> bs) throws IOException, SQLException, ClassNotFoundException {

        PrintWriter pw = new PrintWriter(new FileWriter("c:/log.txt"));

        HashMap<Long,GemEntry> mapId2Gem = new HashMap<Long,GemEntry>();
        ArrayList<GemEntry> listGems = App.getRepositorio().getGems();
        for (GemEntry ge: listGems) {
            mapId2Gem.put(ge.getId(),ge);
        }
        GemRepository R = new GemRepository(listGems);

        HashSet<BlinkEntry> changedBlinks = new HashSet<BlinkEntry>();

        HashMap<BlinkEntry, GemEntry> map = new HashMap<BlinkEntry, GemEntry>();

        int k = 0;
        for (BlinkEntry be: bs) {

            System.out.println(String.format("%d of %d   (id: %d)",(++k),bs.size(),be.get_id()));

            Gem g = be.getBlink().getGem();
            g.goToCodeLabel();
            int t = Integer.parseInt("360000");
            SearchAttractor A = new SearchAttractor(g,t);

            //
            Gem bestAttractor = A.getBestAttractorFound();

            // consultar gem anterior
            boolean changeAttractor = false;
            if (be.get_gem() != -1) {
                GemEntry ge = mapId2Gem.get(be.get_gem());
                Gem oldAttractor = new Gem(ge.getLabelling());
                if (bestAttractor.getNumVertices() < oldAttractor.getNumVertices() ||
                    (A.isBestAttractorTSClassRepresentant() &&
                     oldAttractor.compareTo(bestAttractor) > 0))  {
                    changeAttractor = true;
                }
            }
            else changeAttractor = true;

            if (changeAttractor) {
                GemEntry gEntry = R.getExistingGemEntryOrCreateNew(
                        bestAttractor.getCurrentLabelling(),
                        A.isBestAttractorTSClassRepresentant() ? A.getBestAttractorTSClassSize() : 0,true);
                map.put(be,gEntry);
                be.set_path(A.getBestPath());
                changedBlinks.add(be);
            }
            else continue;

            ArrayList<GemEntry> list = R.getNewEntriesLists(); // get list of not persistent QIs
            App.getRepositorio().insertGems(list);
            R.clearNewEntriesList(); // new entries have been updated

            GemEntry ge = map.get(be);
            be.set_gem(ge.getId());
            System.out.println("Updating blink "+be.get_id()+" gem to be "+ge.getId());
            pw.println("Updating blink "+be.get_id()+" gem to be "+ge.getId());
            pw.flush();

            ArrayList<BlinkEntry> listOfOne = new ArrayList<BlinkEntry>();
            listOfOne.add(be);
            App.getRepositorio().updateBlinksGems(listOfOne);
        }

    }


    public static void main(String[] args) throws IOException, SQLException, ClassNotFoundException {
        new ReRunGems();
        System.exit(0);
    }
}
