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
public class GenerateQI {

    public GenerateQI() {
    }

    public static void main(String[] args) throws FileNotFoundException, IOException, SQLException, ClassNotFoundException {
        BlinkDB db = (BlinkDB) App.getRepositorio();
        long t0 = System.currentTimeMillis();

        QIRepository R = new QIRepository();

        // fill in QIRepository
        ArrayList<QI> qis = App.getRepositorio().getQIs();
        for (QI q: qis)
            R.add(q);

        HashMap<BlinkEntry, QI> _map = new HashMap<BlinkEntry, QI>();

        long minmax[] = db.getMinMaxBlinkIDs(); // {15231, 15231}; //db.getMinMaxIDs();
        int delta = 100;
        int count = 1;
        int acum = 0;
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

                QI qi = b.optimizedQuantumInvariant(3, 8);

                // System.out.println("Calculated "+count++);

                // add to repository
                QI qiRep = R.add(qi);
                if (qiRep == null)
                    qiRep = qi;

                //
                _map.put(be, qiRep);
            }

            System.out.println(String.format("Time to calculate QIs %.2f sec.", (System.currentTimeMillis() - t) / 1000.0));

            //
            ArrayList<QI> list = R.getList(); // get list of not persistent QIs
            t = System.currentTimeMillis();
            db.insertQIs(list);
            acum = acum+list.size();
            System.out.println(String.format("Inserted %6d new QIs total QIs %6d in %.2f sec.", list.size(), acum, (System.currentTimeMillis() - t) / 1000.0));

            // updating biEntry
            for (BlinkEntry be : bs) {
                QI qi = _map.get(be);
                be.set_qi(qi.get_id());
            }

            // update qis
            t = System.currentTimeMillis();
            db.updateBlinksQI(bs);
            // System.out.println(String.format("Updated QIs %d blinks in %.2f sec.", bs.size(), (System.currentTimeMillis() - t) / 1000.0));

            // update index
            k = k+delta;
        }

        System.out.println(String.format("Total Time to calculate QIs %.2f sec.",(System.currentTimeMillis() - t0) / 1000.0));
        System.exit(0);
    }

}

class QIRepository {
    HashMap<Long,ArrayList<QI>> _map = new HashMap<Long,ArrayList<QI>>();
    public QIRepository() {}

    /**
     * Returns null if the qi is new to the repository
     * otherwise returns the already stored QI
     */
    public QI add(QI qi) {
        long hashCode = qi.getHashCode();
        ArrayList<QI> list = _map.get(hashCode);
        if (list == null) {
            list = new ArrayList<QI>();
            _map.put(hashCode,list);
            list.add(qi);
            return null;
        }
        else {
            for (QI aux: list) {
                if (aux.isEqual(qi))
                    return aux;
            }
            list.add(qi);
            return null;
        }
    }

    /**
     * Contains such quantum invariant?
     */
    public boolean contains(QI qi) {
        long hashCode = qi.getHashCode();
        ArrayList<QI> list = _map.get(hashCode);
        if (list == null) {
            return false;
        }
        else {
            for (QI aux: list) {
                if (aux.isEqual(qi))
                    return true;
            }
            return false;
        }
    }

    /**
     * Get list of not-persistent qi
     */
    public ArrayList<QI> getList() {
        ArrayList<QI> result = new ArrayList<QI>();
        for (ArrayList<QI> list: _map.values()) {
            for (QI qi: list) {
                if (qi.get_id() == QI.NOT_PERSISTENT)
                    result.add(qi);
            }
        }
        return result;
    }
}

