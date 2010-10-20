package blink;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

public class GenerateMaps {
    private Queue<MapPackedWord> _unprocessedMaps = new LinkedList<MapPackedWord>();
    private HashSet<MapPackedWord> _processedMaps = new HashSet<MapPackedWord>();
    private HashSet<MapPackedWord> _maps = new HashSet<MapPackedWord>();
    private int _maximum;
    private int _noLoops;

    public GenerateMaps(GBlink seeds[], int maximum) {
        _maximum = maximum;

        // add seeds (calculate it's code before)
        for (GBlink x: seeds) {
            this.store(x);
        }
    }

    /**
     * The mapPackedWord must be of a "code".
     */
    public boolean store(GBlink b) {
        // transformar em code word
        MapPackedWord codeWord = new MapPackedWord(b.goToCodeLabelAndDontCareAboutSpaceOrientation(),b.containsSimpleLoop());

        // this word was already processed or is on the unprocessed list?
        if (_processedMaps.contains(codeWord) || _unprocessedMaps.contains(codeWord)) {
            return false;
        }

        // System.out.println("store: "+codeWord);

        // this word is alreay on the unprocessed list?
        // if (!_unprocessedMaps.contains(codeWord)) {
        if (!codeWord.containsSimpleLoop())
            _noLoops++;
        _unprocessedMaps.offer(codeWord);
        // System.out.println("Add to unprocessed list: "+codeWord);
        // }

        // obter code word do representante

        // this was changed on 02/10/2006
        // from b.getRepresentant() to b.getNewRepresentant()
        // System.out.println(""+b.getBlinkWord().toString());
        GBlink rep = b.getNewRepresentant();
        // GBlink rep = b.getRepresentant();
        MapPackedWord repCodeWord = new MapPackedWord(rep.goToCodeLabelAndDontCareAboutSpaceOrientation(),rep.containsSimpleLoop());

        // System.out.println("representant: "+repCodeWord);

        // contains
        if (!_maps.contains(repCodeWord)) {
            _maps.add(repCodeWord);
            return true;
        }
        else {
            return false;
        }
    }

    public void process() throws IOException {
        long t0 = System.currentTimeMillis();
        while (!_unprocessedMaps.isEmpty()) {
            MapPackedWord x = _unprocessedMaps.poll();

            // test maximum of edges
            if (2*x.size() + 4 > _maximum * 4) {

                // out
                System.out.println(String.format("Maps: %5d     Unprocessed: %5d    NoSimpleLoops: %5d    Tempo: %10.2f",
                                                 _maps.size(),
                                                 _unprocessedMaps.size(),
                                                 _noLoops,
                                                 (System.currentTimeMillis()-t0)/1000.0));

                continue;
            }

            // map of vertices
            GBlink m = new GBlink(x);
            ArrayList<GBlinkVertex> vertices = m.getVertices();
            for (GBlinkVertex v : vertices) {
                int i = v.getLabel();
                if (i % 2 == 0)
                    continue;

                { // loop
                    GBlink copy = new GBlink(x);
                    GBlinkVertex vv = copy.findVertex(i);
                    if (vv == null)
                        throw new RuntimeException("oooooopppssss");
                    copy.addLoop(vv);
                    this.store(copy);
                } // loop

                { // pendant
                    GBlink copy = new GBlink(x);
                    GBlinkVertex vv = copy.findVertex(i);
                    if (vv == null)
                        throw new RuntimeException("oooooopppssss");
                    copy.addIsolatexVertex(vv);
                    this.store(copy);
                } // pendant
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
                copy.addFaceDivision(v1,v2);
                this.store(copy);
            }

            // add to processed maps
            _processedMaps.add(x);

            // out
            System.out.println(String.format("Maps: %5d     Unprocessed: %5d    NoSimpleLoops: %5d    Tempo: %10.2f",
                                             _maps.size(),
                                             _unprocessedMaps.size(),
                                             _noLoops,
                                             (System.currentTimeMillis()-t0)/1000.0));
        }

        ArrayList<MapPackedWord> list = new ArrayList<MapPackedWord>();
        list.addAll(_maps);
        Collections.sort(list);
        FileWriter fw = new FileWriter("c:/maps.txt");
        for (MapPackedWord m: list) {
            if (!m.containsSimpleLoop()) {
                fw.write(m.toString());
                fw.write("\n");
            }
        }
        fw.flush();
        fw.close();
    }

    public static void main(String[] args) throws IOException {
        GenerateMaps mg = new GenerateMaps(
            new GBlink[] {
                new GBlink(new MapWord(new int[] {1,2})),
                new GBlink(new MapWord(new int[] {2,1}))
            }, 9);
        mg.process();
    }

}

