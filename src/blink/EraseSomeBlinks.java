package blink;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.StringTokenizer;

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
public class EraseSomeBlinks {

    public EraseSomeBlinks() {
    }

    public static void main(String[] args) throws
            FileNotFoundException,
            NumberFormatException,
            IOException,
            SQLException {

        BlinkDB db = (BlinkDB) App.getRepositorio();

        long t = System.currentTimeMillis();

        BufferedReader br = new BufferedReader(new FileReader("c:/workspace/blink/res/hg9-filtered.txt"));

        ArrayList<BlinkEntry> list = new ArrayList<BlinkEntry>();

        // Blinks that are to be mantained
        HashSet<GBlink> S = new HashSet<GBlink>();

        String s;
        int count = 0;
        while ((s = br.readLine()) != null) {

            count++;
            if (count % 1000 == 0) {
                System.out.println(String.format("Blinks processed: %d   %.2f", count,
                                                 (System.currentTimeMillis() - t) / 1000.0));
            }

            StringTokenizer st = new StringTokenizer(s, "\t");
            String mapCode = st.nextToken();

            StringTokenizer st2 = new StringTokenizer(mapCode, ",");
            int numEdges = st2.countTokens() / 2;

            String colorsSt = st.nextToken().trim();

            long colors = 0L;
            for (int i = 0; i < colorsSt.length(); i++) {
                int d = Integer.parseInt("" + colorsSt.charAt(i));
                colors = colors + (d << i);
            }
            String hg = st.nextToken().trim();
            BlinkEntry be = new BlinkEntry(BlinkEntry.NOT_PERSISTENT, mapCode, colors, numEdges, hg, -1, -1, -1, "", 0);

            S.add(be.getBlink());

        }

        ArrayList<BlinkEntry> all = App.getRepositorio().getBlinks(1,100);

        StringBuffer sb = new StringBuffer("delete from blink where id in (");
        boolean first = true;
        for (BlinkEntry be: all) {
            if (!S.contains(be.getBlink())) {
                if (!first)
                    sb.append(',');
                sb.append(be.get_id());
                first = false;
            }
        }
        sb.append(')');

        PrintStream pw = new PrintStream(new FileOutputStream("c:/delete.sql"));
        pw.append(sb.toString());
        pw.close();
    }
}
