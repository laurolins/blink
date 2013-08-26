package blink;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
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
public class ImportGemClassification {
    public ImportGemClassification() {
    }

    public static void main(String[] args) throws FileNotFoundException, IOException, SQLException, ClassNotFoundException {
        ClassificationGemRep R = new ClassificationGemRep();

        //ArrayList<GemPackedLabelling> set = new ArrayList<GemPackedLabelling>();
        //FileWriter fw = new FileWriter("c:/x.txt");

        int arrNumVertices[] = {16,18,20,22,24,26,28};

        //int ns[] = {8,12,14,16};
        for (int numVert: arrNumVertices) {
            int catNumber = 0;
            BufferedReader br = new BufferedReader(new FileReader("res/gem-classification/gc"+(numVert < 10 ? "0"+numVert : ""+numVert)+".txt"));
            String st;
            while ((st = br.readLine()) != null) {
                catNumber++;
                System.out.println("Processing: "+st);
                StringTokenizer tok = new StringTokenizer(st,"/.");
                int minGemNumVert = Integer.parseInt(tok.nextToken().trim());
                int minGemCatNumber = Integer.parseInt(tok.nextToken().trim());
                int minGemHandles = Integer.parseInt(tok.nextToken().trim());
                R.setMinGem(numVert,catNumber,minGemNumVert,minGemCatNumber,minGemHandles);
            }
            br.close();
        }
        R.persist();
        System.exit(0);
    }
}

class ClassificationGemRep {
    private ArrayList<GemEntry> _gems;
    private ArrayList<GemEntry> _addedGems;
    private HashMap<GemEntry,GemEntry> _mapGem2MinGem = new HashMap<GemEntry,GemEntry>();
    public ClassificationGemRep() throws ClassNotFoundException, IOException, SQLException {
        _gems = App.getRepositorio().getGemsByNumVertices(0,28);
        _addedGems = new ArrayList<GemEntry>();
    }

    public void setMinGem(
            int sourceNumberOfVertices,
            int sourceCatalogNumber,
            int targetNumberOfVertices,
            int targetCatalogNumber,
            int targetNumberOfHandles) throws ClassNotFoundException, IOException {

        GemEntry geSource = this.findGemByCatalogNumberAndHandle(sourceNumberOfVertices,sourceCatalogNumber,0);
        GemEntry geTarget = this.findGemByCatalogNumberAndHandle(targetNumberOfVertices,targetCatalogNumber,targetNumberOfHandles);

        // case where both, source and target, are already on the database
        if (geSource != null && geTarget != null) {
            _mapGem2MinGem.put(geSource,geTarget);
        }
        else if (geSource != null && geTarget == null) {
            GemEntry geTargetWithZeroHandle = this.findGemByCatalogNumberAndHandle(targetNumberOfVertices,targetCatalogNumber,0);

            if (geTargetWithZeroHandle != null) {
                geTarget = this.addNewGemEntryFromZeroHandleGemEntry(geTargetWithZeroHandle,targetNumberOfHandles);
                _mapGem2MinGem.put(geSource,geTarget);
            }
            else {
                System.out.println("What is happening!!");
            }
        }
        else System.out.println("Strange situation... no geSource!");
    }

    private GemEntry findGemByCatalogNumberAndHandle(int numberOfVertices, int catalogNumber, int handles) {
        for (GemEntry ge: _gems) {
            if (ge.getNumVertices() == numberOfVertices &&
                ge.getCatalogNumber() == catalogNumber &&
                ge.getHandleNumber() == handles) {
                return ge;
            }
        }
        return null;
    }

    private GemEntry addNewGemEntryFromZeroHandleGemEntry(GemEntry zeroHandleGemEntry, int desiredHandleNumber) throws IOException, ClassNotFoundException {
        System.out.println("Adding new entry with handle "+desiredHandleNumber);
        GemEntry ge = new GemEntry(
            GemEntry.NOT_PERSISTENT,
            new ByteArrayInputStream(zeroHandleGemEntry.getCodeAsByteArray()),
            desiredHandleNumber,
            zeroHandleGemEntry.getTSClassSize(),
            zeroHandleGemEntry.getCatalogNumber(),
            GemPrimeStatus.COMPOSITE_FROM_HANDLE.getNumber(),
            0,
            zeroHandleGemEntry.isTSRepresentant());
        _gems.add(ge);
        _addedGems.add(ge);
        return ge;
    }

    public void persist() throws IOException, SQLException {
        App.getRepositorio().insertGems(_addedGems);
        for (GemEntry source: _mapGem2MinGem.keySet()) {
            GemEntry target = _mapGem2MinGem.get(source);
            source.setMinGem(target.getId());

            System.out.println("source -> target = "+source.getId()+" -> "+target.getId());
        }
        App.getRepositorio().updateGemMinGem(new ArrayList<GemEntry>(_mapGem2MinGem.keySet()));
    }

}
