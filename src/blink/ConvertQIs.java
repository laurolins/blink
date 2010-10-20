package blink;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Convert QIs
 */
public class ConvertQIs {
    public ConvertQIs() {
    }

    Connection _connection;
    public Connection getConnection() throws SQLException {
        if (_connection == null) {
            _connection = DriverManager.getConnection("jdbc:jdc:jdcpool");
        }
        return _connection;
    }

    public static void main(String[] args) throws ClassNotFoundException, IOException, SQLException {
        BlinkDB db = (BlinkDB) App.getRepositorio();
        long t0 = System.currentTimeMillis();
        QIRepository R = new QIRepository();
        long minmax[] = db.getMinMaxBlinkIDs();
        int delta = 10000;
        long k = minmax[0];
        while (k <= minmax[1]) {
            System.out.println(String.format("From id %d to id %d", k,k+delta-1));
            long t = System.currentTimeMillis();
            ArrayList<QI> bs = db.getQIByIDInterval(k, k+delta-1);
            // System.out.println(String.format("Retrieved %d blinks in %.2f sec.", bs.size(), (System.currentTimeMillis() - t) / 1000.0));

            t = System.currentTimeMillis();

            /*
            for (QI qi : bs) {
                QIEntry qie5 = qi.getEntryByR(5);
                QIEntry qie6 = qi.getEntryByR(6);
                if (qie5.isInNeighborhood(-0.58778525,-0.80901699) &&
                    qie6.isInNeighborhood(-0.5,0.86602540))
                    System.out.println("QI: "+qi.get_id());
            }*/

            // update index
            k = k+delta;
        }

        System.out.println(String.format("Total Time to calculate QIs %.2f sec.",(System.currentTimeMillis() - t0) / 1000.0));

/*
        if (blinks.size() == 0)
            return;

        Connection con = getConnection();
        con.setAutoCommit(false);
        Statement stmt = con.createStatement();
        for (BlinkEntry b : blinks) {
            stmt.addBatch("update blink set hg=" + b.get_qi() + " where hg='" + b.get_id() + "'");
        }
        stmt.executeBatch();
        con.commit();
        con.setAutoCommit(true);
        stmt.close();        */
    }


    private static InputStream encodeQI(QI qi) throws IOException {
        return null;
    }

    private static InputStream decodeQI(QI qi) throws IOException {
        return null;
    }

    private static Object decode(InputStream is) throws IOException, ClassNotFoundException {
        GZIPInputStream gzis = new GZIPInputStream(is);
        ObjectInputStream ois = new ObjectInputStream(gzis);
        Object result = ois.readObject();
        ois.close();
        return result;
    }

    private static InputStream encode(Serializable s) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        GZIPOutputStream gzos = new GZIPOutputStream(bos);
        ObjectOutputStream oos = new ObjectOutputStream(gzos);
        oos.writeObject(s);
        oos.flush();
        oos.close();
        byte[] data = bos.toByteArray();
        return new ByteArrayInputStream(data);
    }

}
