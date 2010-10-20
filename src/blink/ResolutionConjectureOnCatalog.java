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
public class ResolutionConjectureOnCatalog {
    public ResolutionConjectureOnCatalog() {
    }

    public static void main(String[] args) throws ClassNotFoundException, IOException, SQLException {
        PrintWriter pw = new PrintWriter("d:/workspace/blink/log/resolutionConjectureOnCatalog.log");

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

                boolean simplifiedToOnlyOne23gon = false;

                //
                while (true) {

                    // apply twists 23 and 32
                    while (true) {
                        Twistor t = null;
                        GemColor c1 = null;
                        for (Twistor tt: G.findAllTwistors()) {
                            if (tt.getColor() == GemColor.getByNumber(2)) {
                                c1 = GemColor.getByNumber(3);
                                t = tt;
                                break;
                            }
                            else if (tt.getColor() == GemColor.getByNumber(3)) {
                                c1 = GemColor.getByNumber(2);
                                t = tt;
                                break;
                            }
                        }

                        if (t == null) {
                            break;
                        }
                        else {
                            // System.out.println("Apply twistor "+t+" target color "+c1);
                            G.applyTwistor(t,c1);
                        }
                    }

                    // is there any
                    if (G.findResidues(GemColor.red,GemColor.green).size() == 1) {
                        simplifiedToOnlyOne23gon = true;
                        break;
                    }

                    // find the antipoles
                    boolean appliedAntipole = false;
                    for (Antipole a: G.findAllAntipoles()) {
                        if (a.getColor() != GemColor.blue) {
                            G.doubleTwoDipoleCreation(a.getU(),GemColor.red);
                            appliedAntipole = true;
                            break;
                        }
                    }

                    if (!appliedAntipole) {
                        break;
                    }
                }

                // simplified to only one 23-gon?
                if (!simplifiedToOnlyOne23gon) {
                    counterExamples++;
                    pw.println("conjecture failed on R " + e.getNumVertices() + " " + e.getCatalogNumber());
                    pw.flush();
                }
            }
        }
        pw.close();
        System.exit(0);
    }
}


