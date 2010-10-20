package blink;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
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
public class GenerateHG {

    public GenerateHG() {
    }

    public static void main(String[] args) throws FileNotFoundException, IOException, SQLException {
        BlinkDB db = (BlinkDB) App.getRepositorio();

        long t0 = System.currentTimeMillis();

        PrintStream pw = new PrintStream(new FileOutputStream("c:/hg.log"));

        long minmax[] = db.getMinMaxBlinkIDs();
        int delta = 500;
        long k = minmax[0];
        while (k <= minmax[1]) {

            System.out.println(String.format("From id %d to id %d", k,k+delta-1));

            long t = System.currentTimeMillis();
            ArrayList<BlinkEntry> bs = db.getBlinksByIDInterval(k, k+delta-1);
            // System.out.println(String.format("Retrieved %d blinks in %.2f sec.", bs.size(), (System.currentTimeMillis() - t) / 1000.0));

            t = System.currentTimeMillis();

            int j = 0;
            int n = bs.size();
            for (BlinkEntry be : bs) {
                // System.out.println(String.format("QI %6d/%6d", ++j, n));
                GBlink b = new GBlink(new MapWord(be.get_mapCode()));
                b.setColor((int)be.get_colors());



                HomologyGroup hg1 = b.homologyGroupFromGem();
                HomologyGroup hg2 = b.homologyGroupFromGBlink();

                if (be.get_id() == 300)
                    hg1 = hg2;

                if (!hg1.equals(hg2)) {
                    pw.println("Problema de Homology Groups Diferentes em "+be.get_mapCode()+" "+be.get_colors());
                    pw.flush();
                    be.set_hg("problema");
                }
                else be.set_hg(hg1.toString());

            }

            System.out.println(String.format("Time to calculate HGs %.2f sec.", (System.currentTimeMillis() - t) / 1000.0));

            // update qis
            t = System.currentTimeMillis();
            db.updateBlinksHG(bs);
            // System.out.println(String.format("Updated QIs %d blinks in %.2f sec.", bs.size(), (System.currentTimeMillis() - t) / 1000.0));

            // update index
            k = k+delta;
        }

        pw.close();
        System.out.println(String.format("Total Time to calculate HGs %.2f sec.",(System.currentTimeMillis() - t0) / 1000.0));

        System.exit(0);
    }

    public static void mainOld(String[] args) throws FileNotFoundException, IOException {
        long t = System.currentTimeMillis();
        BufferedReader br = new BufferedReader(new FileReader("c:/workspace/blink/res/maps8.txt"));
        PrintWriter pr = new PrintWriter(new FileWriter("c:/workspace/blink/res/current-qi.txt"));

        String s;
        int count=0;
        while ((s = br.readLine()) != null) {
            System.out.println(String.format("mapa: %6d     %7.2f seg.",(++count),(System.currentTimeMillis()-t)/1000.0));

            StringTokenizer st = new StringTokenizer(s,",");
            int code[] = new int[st.countTokens()];
            int i=0;
            while (st.hasMoreTokens())
                code[i++] = Integer.parseInt(st.nextToken());

            GBlink m = new GBlink(new MapWord(code));
            int ne = m.getNumberOfGEdges();

            if (ne > 7)
                break;

            // if (count != 3167 && count != 3166)
            //    continue;
            //if (ne >= 8)
            //    break;

            int N = (int) Math.pow(2,ne);
            for (int k=0;k<N;k++) {

                // para cada coloração válida
                for (int e=1;e<=ne;e++)
                    m.setColor(e,((k & 0x01 << (e-1)) == 0 ? BlinkColor.green : BlinkColor.red));

                pr.println(String.format("%-43s %-10s",s,reverseAndFillWithZeros(Integer.toBinaryString(k),ne)));
                QI result = m.quantumInvariant(3,6);
                result.print();

            }
            //
            pr.flush();
        }

        pr.close();
        br.close();

    }

    public static String reverseAndFillWithZeros(String s, int n) {
        StringBuffer sb = new StringBuffer();
        int i=0;
        for (;i<s.length();i++) {
            sb.append(s.charAt(s.length()-1-i));
        }
        for (;i<n;i++)
            sb.append("0");
        return sb.toString();
    }

}
