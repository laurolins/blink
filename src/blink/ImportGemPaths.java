package blink;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
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
public class ImportGemPaths {
    public ImportGemPaths() {}

    public static void main(String[] args) throws FileNotFoundException, IOException, ClassNotFoundException,
            SQLException {

        long t = System.currentTimeMillis();
        BufferedReader br = new BufferedReader(new FileReader("res/gemPaths.txt"));

        GemPathRepository R = new GemPathRepository();

        String s;

        int i=1;

        while ((s = br.readLine()) != null) {
            StringTokenizer st = new StringTokenizer(s, "\t");

            // System.out.println("read A... line"+i);
            Gem a = new Gem(new GemPackedLabelling(st.nextToken(), 0));
            // System.out.println("read B...");
            Gem b = new Gem(new GemPackedLabelling(st.nextToken(), 0));
            // System.out.println("read path...");
            Path path = new Path(st.nextToken());
            if (R.addPathIfBothGemsExists(a,b,path)) {
                System.out.println("Added Gem Path "+(i++));
            }
        }
        br.close();

        System.out.println(String.format("Time: %.2f seg",(System.currentTimeMillis()-t)/1000.00));
        System.exit(0);


    }


}
