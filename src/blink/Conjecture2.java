package blink;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;

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
public class Conjecture2 {

    public Conjecture2() {
    }

    public static void main(String[] args) throws ClassNotFoundException, IOException, SQLException {
        PrintWriter pw = new PrintWriter("c:/counterexamples");

        GemEntry result = null;
        int numregs = 250;

        long minId = 1;

        int counterExamples = 0;
        while (true) {
            System.out.println("Querying from " + minId + " next " + numregs + " counterExmples: "+counterExamples);
            ArrayList<GemEntry> gems = App.getRepositorio().getSomeGems(minId, -1, numregs);
            if (gems.size() == 0)
                break;
            for (GemEntry e : gems) {
                if (e.getId() >= minId) {
                    minId = e.getId() + 1;
                }
                if (e.getCatalogNumber() == 0)
                    continue;
                if (e.getNumVertices() > 30)
                    continue;

                Gem G = e.getGem();
                DifferenceToS3 d = G.getDifferenceToS3();
                int components = d.getNumberOfComponents();

                // found counter example
                if (components > 1) {
                    pw.println("R " + e.getNumVertices() + " " + e.getCatalogNumber() +" #cs: "+components);
                    pw.flush();
                    counterExamples++;
                }
            }
        }
        pw.close();
    }
}
