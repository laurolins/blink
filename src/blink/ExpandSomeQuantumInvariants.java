package blink;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.HashMap;

/**
 * For each class that has more than one "minGem",
 * rerun the Quantum Invariant until r=8 and update
 * it
 */
public class ExpandSomeQuantumInvariants {

    public ExpandSomeQuantumInvariants() throws SQLException, IOException {

        long qis[] = { 539 };

        HashMap<Long,QI> map = new HashMap<Long,QI>();
        HashMap<Long,Long> minGemOfTheUpgradedInstance = new HashMap<Long,Long>();

        QIRepository R = new QIRepository();
        for (long qiID: qis) {
            BlinkEntry be = App.getRepositorio().getBlinkByQI(App.MAX_EDGES, qiID);
            long minGem = be.getMinGem();
            QI newQI = be.getBlink().optimizedQuantumInvariant(3,8);

            QI qiUpgraded = R.add(newQI);
            if (qiUpgraded == null)
                qiUpgraded = newQI;

            map.put(qiID,qiUpgraded);
            minGemOfTheUpgradedInstance.put(qiID,minGem);
        }

        // insert qis
        App.getRepositorio().insertQIs(R.getList());

        PrintWriter pw = new PrintWriter(new FileWriter("log/expandSomeQIsWithMinGem.txt"));
        for (long qiID: qis) {
            QI newQI = map.get(qiID);
            long minGem = minGemOfTheUpgradedInstance.get(qiID);
            pw.println("Blinks with mingem="+minGem+" and qi="+qiID+" will have it's qi updated to "+newQI.get_id());
            App.getRepositorio().updateBlinksWithMinGemToNewQI(qiID,newQI.get_id(),minGem);
        }
        pw.close();
    }

    public static void main(String[] args) throws IOException, SQLException {
        new ExpandSomeQuantumInvariants();
        System.exit(0);
    }
}
