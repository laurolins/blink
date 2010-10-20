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
public class RemoveBlinkDifference {
    public RemoveBlinkDifference() {
    }
    public static void main(String[] args) throws FileNotFoundException, NumberFormatException, IOException, SQLException {
        BlinkDB db = (BlinkDB) App.getRepositorio();

        long t = System.currentTimeMillis();
        BufferedReader br = new BufferedReader(new FileReader("res/mapsCores8.txt"));

        ArrayList<GBlink> list = new ArrayList<GBlink>();

        ArrayList<BlinkEntry> allEntries = App.getRepositorio().getBlinks(0,12);

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
            list.add(be.getBlink());
        }

        /*
        PrintWriter pr = new PrintWriter(new FileWriter("res/entriesWithRMIII.txt"));
        for (BlinkEntry g: allEntries) {
            pr.println(""+g.getBlink().getBlinkWord().toString());
        }
        pr.close();

        pr = new PrintWriter(new FileWriter("res/entriesWithoutRMIII.txt"));
        Collections.sort(list);
        for (GBlink g: list) {
            pr.println(""+g.getBlinkWord().toString());
        }
        pr.close(); */

        //
        ArrayList<BlinkEntry> removeEntries = new ArrayList<BlinkEntry>();
        for (BlinkEntry b: allEntries) {
            System.out.print("Testing "+b.get_id()+"... ");
            boolean found = false;
            GBlink g = b.getBlink();
            int gHashCode = g.hashCode();
            for (GBlink valid: list) {
                if (gHashCode != valid.hashCode())
                    continue;

                g.equals(valid);

                int compareTo = g.compareTo(valid);

                if (compareTo < 0) {
                    break;
                }
                else if (compareTo == 0) {
                    list.remove(valid);
                    found = true;
                    break;
                }
            }
            if (!found) {
                System.out.println(" remove "+b.get_id());
                removeEntries.add(b);
            }
            else {
                System.out.println("");
            }
        }

        StringBuffer sb = new StringBuffer();
        sb.append("delete from blink where id in (\n");
        boolean first = true;
        for (BlinkEntry be: removeEntries) {
            if (!first)
                sb.append(",");
            sb.append(be.get_id());
            first = false;
        }
        sb.append(");");
        System.out.println(""+sb.toString());

        br.close();
        System.exit(0);
    }
}
