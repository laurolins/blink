package blink;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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
public class ExportGemPaths {
    public ExportGemPaths() {
    }

    public static void main(String[] args) throws FileNotFoundException, IOException, SQLException,
            ClassNotFoundException {

        long t = System.currentTimeMillis();
        PrintWriter pr = new PrintWriter(new FileWriter("res/gemPaths.txt"));
        //BufferedReader br = new BufferedReader(new FileReader("res/x.txt"));
        //PrintWriter pr = new PrintWriter(new FileWriter("res/y.txt"));

        HashMap<Long,GemEntry> map = App.getRepositorio().getGemsMap();
        ArrayList<GemPathEntry> paths = App.getRepositorio().getGemPaths();

        int i=1;
        for (GemPathEntry gpe: paths) {
            GemEntry a = map.get(gpe.getSource());
            GemEntry b = map.get(gpe.getTarget());
            String sa = a.getGem().getCurrentLabelling().getLettersString("");
            String sb = b.getGem().getCurrentLabelling().getLettersString("");
            String spath = gpe.getPath().getSignature();
            pr.println(String.format("%s\t%s\t%s",sa,sb,spath));
            pr.flush();
            System.out.println("Add Path "+(i++));
        }
        pr.close();

        System.out.println(String.format("Time: %.2f seg",(System.currentTimeMillis()-t)/1000.00));
        System.exit(0);
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
