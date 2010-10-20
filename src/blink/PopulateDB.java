package blink;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
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
public class PopulateDB {
    public PopulateDB() {
    }
    public static void main(String[] args) throws FileNotFoundException, NumberFormatException, IOException,
            SQLException {
        BlinkDB db = (BlinkDB) App.getRepositorio();

        long t = System.currentTimeMillis();
        //BufferedReader br = new BufferedReader(new FileReader("res/dualRepresentantsFilteredByDualReflectionAndRefDual.txt"));
        //BufferedReader br = new BufferedReader(new FileReader("res/pipeline2/experiment-filteredGBlinks.txt"));
        //BufferedReader br = new BufferedReader(new FileReader("gblink3con.txt"));
        BufferedReader br = new BufferedReader(new FileReader("experiment-filteredGBlinks.txt"));

        ArrayList<BlinkEntry> list = new ArrayList<BlinkEntry>();

        String s;
        int count = 0;
        while ((s = br.readLine()) != null) {
            count++;
            if (count % 25000 == 0) {
                System.out.println(String.format("Blinks processed: %d   %.2f",count,(System.currentTimeMillis()-t)/1000.0));
            }

            // System.out.println(""+s);

            StringTokenizer st = new StringTokenizer(s, " ");
            String mapCode = st.nextToken().trim();

            String colors = "";
            if (st.hasMoreTokens()) {
                colors = st.nextToken().trim();
            }

            GBlink G = new GBlink(s);

            String hg = G.homologyGroupFromGBlink().toString();
            BlinkEntry be = new BlinkEntry(BlinkEntry.NOT_PERSISTENT,mapCode,G.getColorInAnInteger(),G.getNumberOfGEdges(),hg,-1,-1,-1,"",0);
            list.add(be);

            if (list.size() == 250000) {
                db.insertBlinks(list);
                list.clear();
            }

        }

        if (list.size() > 0) {
            db.insertBlinks(list);
            list.clear();
        }
        System.out.println(String.format("Blinks processed: %d   %.2f",count,(System.currentTimeMillis()-t)/1000.0));

        br.close();
        System.exit(0);
    }







    public static void main2(String[] args) throws FileNotFoundException, NumberFormatException, IOException,
            SQLException {
        BlinkDB db = (BlinkDB) App.getRepositorio();

        long t = System.currentTimeMillis();
        //BufferedReader br = new BufferedReader(new FileReader("res/dualRepresentantsFilteredByDualReflectionAndRefDual.txt"));
        //BufferedReader br = new BufferedReader(new FileReader("res/maps3con.txt"));
        BufferedReader br = new BufferedReader(new FileReader("c:/gblinkOfBlocks9.txt"));

        ArrayList<BlinkEntry> list = new ArrayList<BlinkEntry>();

        String s;
        int count = 0;
        while ((s = br.readLine()) != null) {
            count++;
            if (count % 25000 == 0) {
                System.out.println(String.format("Blinks processed: %d   %.2f",count,(System.currentTimeMillis()-t)/1000.0));
            }

            StringTokenizer st = new StringTokenizer(s, "\t");
            String mapCode = st.nextToken();

            StringTokenizer st2 = new StringTokenizer(mapCode,",");
            int numEdges = st2.countTokens()/2;

            String colorsSt = st.nextToken().trim();

            long colors = 0L;
            for (int i = 0; i < colorsSt.length(); i++) {
                int d = Integer.parseInt("" + colorsSt.charAt(i));
                colors = colors + (d << i);
            }
            String hg = st.nextToken().trim();
            BlinkEntry be = new BlinkEntry(BlinkEntry.NOT_PERSISTENT,mapCode,colors,numEdges,hg,-1,-1,-1,"",0);
            list.add(be);

            if (list.size() == 250000) {
                db.insertBlinks(list);
                list.clear();
            }

        }

        if (list.size() > 0) {
            db.insertBlinks(list);
            list.clear();
        }
        System.out.println(String.format("Blinks processed: %d   %.2f",count,(System.currentTimeMillis()-t)/1000.0));

        br.close();
        System.exit(0);
    }


}
