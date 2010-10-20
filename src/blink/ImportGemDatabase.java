package blink;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;

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
public class ImportGemDatabase {
    public ImportGemDatabase() {
    }

    public static void main(String[] args) throws FileNotFoundException, IOException, SQLException, ClassNotFoundException {
        GemRep R = new GemRep();

        //ArrayList<GemPackedLabelling> set = new ArrayList<GemPackedLabelling>();
        //FileWriter fw = new FileWriter("c:/x.txt");

        int ns[] = {8,12,14,16,18,20,22,24,26,28,30};
        //int ns[] = {8,12,14,16};
        for (int n: ns) {
            FileInputStream fr = new FileInputStream("res/gems/GEMA4."+(n < 10 ? "0"+n : ""+n));
            int numVertices = fr.read();
            int numColors = fr.read();
            int numGems = (10000*fr.read())+(100*fr.read())+(1*fr.read());
            System.out.println("NV:"+numVertices+" #:"+numGems);

            byte buffer[] = new byte[numVertices/2];
            int code[] = new int[3*numVertices/2];
            for (int i=1;i<=numGems;i++) {
                int k=0;
                for (int c=1;c<=3;c++) {
                    fr.read(buffer);
                    for (int j=0;j<numVertices/2;j++) {
                        code[k++] = buffer[j];
                    }
                }
                GemPackedLabelling lbl = new GemPackedLabelling(code.clone());
                //set.add(lbl);
                // System.out.println(""+lbl.getLettersString(','));

                if (i % 100 == 0)
                    System.out.print(".");
                if (i % 10000 == 0)
                    System.out.println("");

                //fw.append(lbl.getLettersString(',')+'\n');
                R.update(lbl,i);
            }
            fr.close();
        }
        //fw.close();

        App.getRepositorio().updateGemCatalogNumber(R.getGemsToUpdateCatalogNumber());
        ArrayList<GemEntry> toAddList = R.getNewGems();
        System.out.println("Add new gems: "+toAddList.size());
        App.getRepositorio().insertGems(toAddList);

        System.exit(0);
    }
}

class GemRep {
    private ArrayList<GemEntry> _existingGems;
    private ArrayList<GemEntry> _gemsToUpdateCatalogNumber;
    private ArrayList<GemEntry> _newGems;
    private HashSet<GemPackedLabelling> _addedGems;
    public GemRep() throws ClassNotFoundException, IOException, SQLException {
        _existingGems = App.getRepositorio().getGems();
        _addedGems = new HashSet<GemPackedLabelling>();
        _gemsToUpdateCatalogNumber = new ArrayList<GemEntry>();
        _newGems = new ArrayList<GemEntry>();
    }
    public void update(GemPackedLabelling lbl, int catalogNumber) {
        // System.out.println(""+lbl.getLettersString(','));
        boolean found = false;
        for (GemEntry e: _existingGems) {
            if (lbl.equals(e.getLabelling())) {
                if (e.getCatalogNumber() != catalogNumber) {
                    e.setCatalogNumber(catalogNumber);
                    _gemsToUpdateCatalogNumber.add(e);
                }
                found = true;
                break;
            }
        }

        if (found)
            return;

        if (_addedGems.add(lbl)) {
            //System.out.println("Add: "+lbl.getLettersString(','));
            _newGems.add(new GemEntry(catalogNumber, lbl));
        }
    }

    public ArrayList<GemEntry> getGemsToUpdateCatalogNumber() {
        return _gemsToUpdateCatalogNumber;
    }

    public ArrayList<GemEntry> getNewGems() {
        return _newGems;
    }
}
