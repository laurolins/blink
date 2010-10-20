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
s * @version 1.0
 */
public class FindByQI {

    public FindByQI() {
    }

    public static void main(String[] args) throws FileNotFoundException, IOException, SQLException, ClassNotFoundException {
        BlinkDB db = (BlinkDB) App.getRepositorio();

        long t0 = System.currentTimeMillis();

        QIRepository R = new QIRepository();
        HashMap<BlinkEntry, QI> _map = new HashMap<BlinkEntry, QI>();

        long minmax[] = db.getMinMaxBlinkIDs();
        int delta = 10000;
        long k = minmax[0];
        while (k <= minmax[1]) {

            System.out.println(String.format("From id %d to id %d", k,k+delta-1));


            long t = System.currentTimeMillis();
            ArrayList<QI> bs = db.getQIByIDInterval(k, k+delta-1);
            // System.out.println(String.format("Retrieved %d blinks in %.2f sec.", bs.size(), (System.currentTimeMillis() - t) / 1000.0));

            t = System.currentTimeMillis();

            for (QI qi : bs) {
                if (qi.isInNeighborhood(4, 0.5000000000, 0.0000000000) &&
                    qi.isInNeighborhood(5, 0.9732489894, 0.2700907567))
                    System.out.println("QI: "+qi.get_id());
            }

            // update index
            k = k+delta;
        }

        System.out.println(String.format("Total Time to calculate QIs %.2f sec.",(System.currentTimeMillis() - t0) / 1000.0));


        System.exit(0);
    }

    public static ArrayList<Integer> findQI(QI qiIn) throws SQLException, ClassNotFoundException, IOException {
        ArrayList<Integer> result = new ArrayList<Integer>();

        BlinkDB db = (BlinkDB) App.getRepositorio();

        long t0 = System.currentTimeMillis();

        QIRepository R = new QIRepository();
        HashMap<BlinkEntry, QI> _map = new HashMap<BlinkEntry, QI>();

        long minmax[] = db.getMinMaxBlinkIDs();
        int delta = 10000;
        long k = minmax[0];
        while (k <= minmax[1]) {

            long t = System.currentTimeMillis();
            ArrayList<QI> bs = db.getQIByIDInterval(k, k + delta - 1);
            // System.out.println(String.format("Retrieved %d blinks in %.2f sec.", bs.size(), (System.currentTimeMillis() - t) / 1000.0));

            t = System.currentTimeMillis();

            QIEntry qie4 = qiIn.getEntryByR(4);
            QIEntry qie5 = qiIn.getEntryByR(5);
            QIEntry qie6 = qiIn.getEntryByR(6);
            for (QI qi : bs) {
                if (qi.isInNeighborhood(4, qie4.get_real(), qie4.get_imaginary()) &&
                    qi.isInNeighborhood(5, qie5.get_real(), qie5.get_imaginary()) &&
                    qi.isInNeighborhood(6, qie6.get_real(), qie6.get_imaginary()))
                    result.add((int)qi.get_id());
                    // System.out.println("QI: " + qi.get_id());
            }

            // update index
            k = k+delta;
        }

        // System.out.println(String.format("Total Time to calculate QIs %.2f sec.",(System.currentTimeMillis() - t0) / 1000.0));
        // System.exit(0);

        return result;

    }

}
