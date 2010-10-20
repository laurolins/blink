package blink;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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
public class GenerateBlinksMonochromaticAndHG {

    public GenerateBlinksMonochromaticAndHG() {
    }

    public static void main(String[] args) throws FileNotFoundException, IOException {
        long t = System.currentTimeMillis();
        BufferedReader br = new BufferedReader(new FileReader("c:/maps3tc.txt"));
        PrintWriter pr = new PrintWriter(new FileWriter("c:/workspace/blink/res/hg3tc16.txt"));

        String s;
        int count=0;
        int blinks=0;

        StringBuffer colors = new StringBuffer();

        while ((s = br.readLine()) != null) {
            System.out.println(String.format("mapa: %6d     %7.2f seg.",(++count),(System.currentTimeMillis()-t)/1000.0));

            StringTokenizer st = new StringTokenizer(s,",");
            int code[] = new int[st.countTokens()];
            int i=0;
            while (st.hasMoreTokens())
                code[i++] = Integer.parseInt(st.nextToken());

            GBlink m = new GBlink(new MapWord(code));
            int ne = m.getNumberOfGEdges();

            HomologyGroup hg = m.homologyGroupFromGem();

            if (hg == null)
                continue;

            //
            colors.setLength(0);
            for (int e = 1; e <= ne; e++)
                if (m.getColor(e) == BlinkColor.green)
                    colors.append("0");
                else
                    colors.append("1");

            //
            pr.println(String.format("%s\t%s\t%s", s, colors.toString(), hg.toString()));
            pr.flush();
        }

        pr.close();
        br.close();

    }
}
