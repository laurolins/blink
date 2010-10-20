package blink;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * For each class that has more than one "minGem",
 * rerun the Quantum Invariant until r=8 and update
 * it
 */
public class ExpandQuantumInvariantOnDoubts {

    public ExpandQuantumInvariantOnDoubts() throws SQLException, IOException {

        // find all QI classes for blinks with less than MAX_EDGES
        ArrayList<ClassQI> classes = App.getRepositorio().getQIClasses(App.MAX_EDGES);

        int k = 0;
        for (ClassQI c: classes) {
            System.out.print(String.format("QI Class (%d/%d): %d ",(++k),classes.size(),c.get_qi()));
            c.load();

            // get blink entries
            ArrayList<BlinkEntry> blinkEntries = c.getBlinks();

            // map each different "minGem" to one of it's blink entries.
            HashMap<Long,BlinkEntry> map = new HashMap<Long,BlinkEntry>();
            for (BlinkEntry be: blinkEntries) {
                long gem = be.getMinGem();
                if (gem != 0L && map.get(gem) == null) {
                    map.put(gem,be);
                }
            }

            // if there is only one minGem then no doubts on this class
            if (map.size()<=1) { // ok. no doubts.
                System.out.println("No Doubt");
                continue;
            }
            else {
                System.out.println("...");
            }

            // create a QIRepository
            QIRepository R = new QIRepository();

            // create a new map
            HashMap<Long,QI> map2 = new HashMap<Long,QI>();
            for (Long gemId: map.keySet()) {

                // get the BlinkEntry (saved on "map") for each different "minGem" id
                BlinkEntry be = map.get(gemId);

                // recalculate the QI
                GBlink b = be.getBlink();
                QI qi = b.optimizedQuantumInvariant(3,8);

                if (qi == null)
                    throw new RuntimeException("OOoooopsss");

                // store on QIRepository (null if it is a new
                // QI, not on the repository. Otherwise the
                // return the QI already on repository).
                QI storedQI = R.add(qi);
                if (storedQI == null) { map2.put(gemId, qi); }
                else map2.put(gemId, storedQI);
            }

            // insert qis
            App.getRepositorio().insertQIs(R.getList());

            // --- LOG ------------------------------
            for (Long gemId: map.keySet()) {
                BlinkEntry be = map.get(gemId);
                QI qi = map2.get(be.getMinGem());
                System.out.println("Update gem "+be.getMinGem()+" (old QI: "+be.get_qi()+") to "+qi.get_id());
            }
            // --- LOG ------------------------------

            // update QIs
            for (BlinkEntry be: blinkEntries) {
                if (be.getMinGem() == 0L)
                    continue;
                QI qi = map2.get(be.getMinGem());
                be.set_qi(qi.get_id());
            }

            // update blink entries
            App.getRepositorio().updateBlinksQI(blinkEntries);

        }
    }
    public static void main(String[] args) throws IOException, SQLException {
        new ExpandQuantumInvariantOnDoubts();
        System.exit(0);
    }
}
