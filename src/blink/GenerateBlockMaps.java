package blink;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

public class GenerateBlockMaps {
    private Queue<MapPackedWord> _unprocessedMaps = new LinkedList<MapPackedWord>();
    private HashSet<MapPackedWord> _processedMaps = new HashSet<MapPackedWord>();
    private HashSet<MapPackedWord> _maps = new HashSet<MapPackedWord>();
    private int _maximum;

    private String _outputFileName;

    public GenerateBlockMaps(int maximum, String outputFileName) {
        _maximum = maximum;
        _outputFileName = outputFileName;

        if (maximum > 1) {

            // add seeds (calculate it's code before)
            this.store(new GBlink(new MapWord(new int[] {4, 3, 2, 1})));
        }
    }

    /**
     * The mapPackedWord must be of a "code".
     */
    public boolean store(GBlink b) {
        // transformar em code word
        MapPackedWord codeWord = new MapPackedWord(b.goToCodeLabelPreservingSpaceOrientationWithoutColors(),b.containsSimpleLoop());

        // this word was already processed or is on the unprocessed list?
        if (_processedMaps.contains(codeWord) || _unprocessedMaps.contains(codeWord)) {
            return false;
        }

        // System.out.println("store: "+codeWord);

        // this word is alreay on the unprocessed list?
        // if (!_unprocessedMaps.contains(codeWord)) {
        _unprocessedMaps.offer(codeWord);
        // System.out.println("Add to unprocessed list: "+codeWord);
        // }

        // obter code word do representante

        // this was changed on 02/10/2006
        // from b.getRepresentant() to b.getNewRepresentant()
        // System.out.println(""+b.getBlinkWord().toString());
        // GBlink rep = b.getNewRepresentant();
        // GBlink rep = b.getRepresentant();
        // MapPackedWord repCodeWord = new MapPackedWord(rep.goToCodeLabelAndDontCareAboutSpaceOrientation(),rep.containsSimpleLoop());

        // System.out.println("representant: "+repCodeWord);

        // contains
        if (!_maps.contains(codeWord)) {
            _maps.add(codeWord);
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * process
     * @throws IOException
     */
    public void process() throws IOException {

        long t0 = System.currentTimeMillis();

        while (!_unprocessedMaps.isEmpty()) {

            MapPackedWord x = _unprocessedMaps.poll();

            // test maximum of edges
            if (2*x.size() + 4 > _maximum * 4) {

                // out
                System.out.println(String.format("Maps: %5d     Unprocessed: %5d    Tempo: %10.2f",
                                                 _maps.size(),
                                                 _unprocessedMaps.size(),
                                                 (System.currentTimeMillis()-t0)/1000.0));

                continue;
            }

            // same face angles
            ArrayList<Integer[]> sameFaceAngles = (new MapWord(x)).getSameFacePairOfAngles();
            for (Integer[] arr: sameFaceAngles) {
                int i1 = arr[0];
                int i2 = arr[1];
                GBlink copy = new GBlink(x);
                GBlinkVertex v1 = copy.findVertex(i1);
                GBlinkVertex v2 = copy.findVertex(i2);
                if (v1 == null || v2 == null)
                    throw new RuntimeException("oooooopppssss");
                copy.addGFaceOrGVertexDivision(v1,v2,false);
                this.store(copy);
            }

            // same face angles
            ArrayList<Integer[]> sameVertexAngles = (new MapWord(x)).getSameGVertexPairOfAngles();
            for (Integer[] arr: sameVertexAngles) {
                int i1 = arr[0];
                int i2 = arr[1];
                GBlink copy = new GBlink(x);
                GBlinkVertex v1 = copy.findVertex(i1);
                GBlinkVertex v2 = copy.findVertex(i2);
                if (v1 == null || v2 == null)
                    throw new RuntimeException("oooooopppssss");
                copy.addGFaceOrGVertexDivision(v1,v2,true);
                this.store(copy);
            }

            // add to processed maps
            _processedMaps.add(x);

            // out
            System.out.println(String.format("Maps: %5d     Unprocessed: %5d    Tempo: %10.2f",
                                             _maps.size(),
                                             _unprocessedMaps.size(),
                                             (System.currentTimeMillis()-t0)/1000.0));
        }

        // finished calculating maps
        ArrayList<MapPackedWord> list = new ArrayList<MapPackedWord>();

        // artificially put one edge graph as a valid block
        _maps.add(new MapPackedWord(new int[] {1,2}, false));

        list.addAll(_maps);
        Collections.sort(list);

        // write to file if there is one
        if (_outputFileName != null) {
            FileWriter fw = new FileWriter(_outputFileName);
            for (MapPackedWord m : list) {
                if (!m.containsSimpleLoop()) {
                    fw.write(m.toString());
                    fw.write("\n");
                }
            }
            fw.flush();
            fw.close();
        }
    }

    /**
     * All in memory.
     */
    public ArrayList<MapPackedWord> getBlocks() {
        ArrayList<MapPackedWord> result = new ArrayList<MapPackedWord>();
        result.addAll(_maps);
        Collections.sort(result);
        return result;
    }

    public static void main(String[] args) throws IOException {
        System.out.println("main");
        /*
        GenerateBlockMaps mg = new GenerateBlockMaps(
            new GBlink[] {

            }, 9);
        mg.process();*/
    }
}
