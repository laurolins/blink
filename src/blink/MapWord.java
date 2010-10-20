package blink;

import java.awt.Dimension;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.StringTokenizer;

import javax.swing.JFrame;


/**
 * A map (3-regular graph) with edges labeled by the same
 * convention as of a GBlink.
 */
public class MapWord {

    private int[] _expandedCode;

    public MapWord(int code[]) {

        // System.out.println(""+Library.intArrayToString(code));

        _expandedCode = new int[2 * code.length + 1];
        for (int i=0;i<code.length;i++) {
            _expandedCode[2*i+1] = 2*code[i];
            _expandedCode[2*code[i]] = 2*i+1;
        }
    }

    public MapWord(String s) {
        StringTokenizer st = new StringTokenizer(s,",");
        int code[] = new int[st.countTokens()];
        int i=0;
        while (st.hasMoreTokens())
            code[i++] = Integer.parseInt(st.nextToken());
        _expandedCode = new int[2 * code.length + 1];

        for (i=0;i<code.length;i++) {
            _expandedCode[2*i+1] = 2*code[i];
            _expandedCode[2*code[i]] = 2*i+1;
        }
    }

    public int compareTo(Object o) {
        MapWord m = (MapWord) o;

        // test if size of the maps are different
        int r = this.size()-m.size();
        if (r != 0) return r;

        // same size, so check the code
        for (int i = 1; i < _expandedCode.length; i += 2) {
            if (_expandedCode[i] > m._expandedCode[i])
                return (i + 1);
            if (_expandedCode[i] < m._expandedCode[i])
                return -(i + 1);
        }
        return 0;
    }

    public MapWord(MapPackedWord mpw) {
        this(mpw.getPackedWord());
    }

    public int size() {
        return _expandedCode.length - 1; // first position is a dummy one
    }

    public void write() {
        boolean first = true;
        for (int i: _expandedCode) {
            if (!first)
                System.out.print(",");
            System.out.print(""+i);
            first = false;
        }
        System.out.println();
    }

    public String toString() {
        String result = "";
        boolean first = true;
        for (int i=1;i<_expandedCode.length;i+=2) {
            if (!first) result+=",";
            result+=_expandedCode[i]/2;
            first = false;
        }
        return result;
    }

    public int hashCode() {
        int result = 0;
        for (int i=0;i<_expandedCode.length;i+=7) {
            result+=_expandedCode[i];
        }
        return result;
    }

    public void writeWord() {
        boolean first = true;
        for (int i=1;i<_expandedCode.length;i+=2) {
            if (!first)
                System.out.print(",");
            System.out.print(""+_expandedCode[i]/2);
            first = false;
        }
        System.out.println();
    }

    public int getNeighbour(int label,  GBlinkEdgeType t) {
        int result = -1;
        if (t == GBlinkEdgeType.edge) {
            result = _expandedCode[label];
        }
        else if (t == GBlinkEdgeType.face) {
            if (label % 4 == 1) result = label+1;
            else if (label % 4 == 2) result = label-1;
            else if (label % 4 == 3) result = label+1;
            else if (label % 4 == 0) result = label-1;
        }
        else if (t == GBlinkEdgeType.vertex) {
            if (label % 4 == 1) result = label+3;
            else if (label % 4 == 2) result = label+1;
            else if (label % 4 == 3) result = label-1;
            else if (label % 4 == 0) result = label-3;
        }
        else if (t == GBlinkEdgeType.diagonal) {
            if (label % 4 == 1) result = label+2;
            else if (label % 4 == 2) result = label+2;
            else if (label % 4 == 3) result = label-2;
            else if (label % 4 == 0) result = label-2;
        }
        if (result == -1)
            throw new RuntimeException("Oooooops!");
        else {
            return result;
        }
    }

    public ArrayList<ArrayList<Integer>> getBicoloredComponents(GBlinkEdgeType x, GBlinkEdgeType y) {
        HashSet<Integer> labelsUsed = new HashSet<Integer>();
        ArrayList<ArrayList<Integer>> result = new ArrayList<ArrayList<Integer>>();
        // System.out.println("Componentes: "+x+" "+y);
        for (int i=1;i<=this.size();i++) {
            GBlinkEdgeType et = x;
            if (!labelsUsed.contains(i)) {
                int label = i;
                ArrayList<Integer> component = new ArrayList<Integer>();

                // System.out.print(""+label+" ");

                component.add(label);
                labelsUsed.add(label);
                int neighbour;
                while (!labelsUsed.contains(neighbour = this.getNeighbour(label,et))) {
                    label = neighbour;
                    component.add(label);
                    labelsUsed.add(label);
                    et = (et == x ? y : x);

                    // System.out.print(""+label+" ");
                }
                result.add(component);
                // System.out.println();
            }
        }
        return result;
    }

    public ArrayList<Integer[]> getSameFacePairOfAngles() {
        ArrayList<Integer[]> result = new ArrayList<Integer[]>();
        ArrayList<ArrayList<Integer>> list = getBicoloredComponents(GBlinkEdgeType.edge, GBlinkEdgeType.vertex);
        for (ArrayList<Integer> faceList: list) {
            for (int i=0;i<faceList.size();i++) {
                int label_i = faceList.get(i);
                if (label_i % 2 == 0)
                    continue;
                for (int j=i+1;j<faceList.size();j++) {
                    int label_j = faceList.get(j);
                    if (label_j % 2 == 0)
                        continue;
                    result.add(new Integer[] {label_i, label_j});
                }
            }
        }
        return result;
    }

    public ArrayList<Integer[]> getSameGVertexPairOfAngles() {
        ArrayList<Integer[]> result = new ArrayList<Integer[]>();
        ArrayList<ArrayList<Integer>> list = getBicoloredComponents(GBlinkEdgeType.edge, GBlinkEdgeType.face);
        for (ArrayList<Integer> faceList: list) {
            for (int i=0;i<faceList.size();i++) {
                int label_i = faceList.get(i);
                if (label_i % 2 == 0)
                    continue;
                for (int j=i+1;j<faceList.size();j++) {
                    int label_j = faceList.get(j);
                    if (label_j % 2 == 0)
                        continue;
                    result.add(new Integer[] {label_i, label_j});
                }
            }
        }
        return result;
    }

    public ArrayList<ArrayList<Integer>> getVertices() {
        return getBicoloredComponents(GBlinkEdgeType.face,GBlinkEdgeType.edge);
    }

    public ArrayList<ArrayList<Integer>> getEdges() {
        return getBicoloredComponents(GBlinkEdgeType.face,GBlinkEdgeType.vertex);
    }

    public ArrayList<ArrayList<Integer>> getFaces() {
        return getBicoloredComponents(GBlinkEdgeType.edge,GBlinkEdgeType.vertex);
    }

    public static void mainOld(String[] args) throws IOException {
        String[][] maps2 = {
                          {"4,2,1,5,3,6","2"},
                          {"4,2,1,5,3,6","5"},
                          {"4,5,2,3,1,6","3"},
                          {"4,5,2,3,1,6","4"},
                          {"4,5,2,6,3,1","0"},
                          {"4,5,2,6,3,1","7"},
                          {"4,6,2,3,1,5","0"},
                          {"4,6,2,3,1,5","7"},
                          {"5,2,1,4,3,6","0"},
                          {"5,2,1,4,3,6","3"},
                          {"5,2,1,4,3,6","7"},
                          {"5,3,2,4,1,6","0"},
                          {"5,3,2,4,1,6","7"},
                          {"6,1,2,3,4,5","0"},
                          {"6,1,2,3,4,5","3"},
                          {"6,1,2,3,4,5","4"},
                          {"6,1,2,3,4,5","7"},
                          {"6,2,1,3,4,5","1"},
                          {"6,2,1,3,4,5","4"},
                          {"6,2,1,3,4,5","6"},
                          {"6,2,1,4,3,5","3"},
                          {"6,2,1,4,3,5","4"},
                          {"6,3,2,4,1,5","3"},
                          {"6,3,2,4,1,5","4"},
                          {"6,3,2,5,4,1","0"},
                          {"6,3,2,5,4,1","7"},
                          {"6,4,2,3,1,5","1"},
                          {"6,4,2,3,1,5","3"},
                          {"6,4,2,3,1,5","4"},
                          {"6,4,2,3,1,5","6"}};

        String[][] maps3 = {
                          {"4,2,1,9,3,10,5,7,8,12,6,11","60"},
                          {"8,11,2,7,4,6,5,10,3,9,1,12","26"},
                          {"8,12,2,7,4,6,5,10,3,9,1,11","58"}

        };

        String[][] maps = { {"4,11,2,6,3,8,5,10,7,12,9,1", "0"}, {"4,11,2,6,3,8,5,10,7,12,9,1", "63"},
                           {"8,11,2,5,4,7,6,10,3,12,9,1", "0"}, {"8,11,2,5,4,7,6,10,3,12,9,1", "63"},
                           {"12,3,2,5,4,7,6,9,8,11,10,1", "0"}, {"12,3,2,5,4,7,6,9,8,11,10,1", "63"}
        };

        String[][] mapsHomologyNine = {{"6,13,2,4,3,8,5,10,7,12,9,14,11,1","0"},
{"6,13,2,4,3,8,5,10,7,12,9,14,11,1","2"},
{"6,13,2,4,3,8,5,10,7,12,9,14,11,1","125"},
{"6,13,2,4,3,8,5,10,7,12,9,14,11,1","127"},
{"6,13,2,8,4,5,3,10,7,12,9,14,11,1","0"},
{"6,13,2,8,4,5,3,10,7,12,9,14,11,1","4"},
{"6,13,2,8,4,5,3,10,7,12,9,14,11,1","123"},
{"6,13,2,8,4,5,3,10,7,12,9,14,11,1","127"},
{"6,14,2,13,4,8,5,10,7,12,9,1,11,3","60"},
{"6,14,2,13,4,8,5,10,7,12,9,1,11,3","61"},
{"6,14,2,13,4,8,5,10,7,12,9,1,11,3","66"},
{"6,14,2,13,4,8,5,10,7,12,9,1,11,3","67"},
{"8,13,2,5,4,7,6,10,3,14,9,12,11,1","0"},
{"8,13,2,5,4,7,6,10,3,14,9,12,11,1","32"},
{"8,13,2,5,4,7,6,10,3,14,9,12,11,1","95"},
{"8,13,2,5,4,7,6,10,3,14,9,12,11,1","127"},
{"8,14,2,5,4,7,6,10,3,12,9,1,11,13","0"},
{"8,14,2,5,4,7,6,10,3,12,9,1,11,13","63"},
{"8,14,2,5,4,7,6,10,3,12,9,1,11,13","64"},
{"8,14,2,5,4,7,6,10,3,12,9,1,11,13","127"},
{"8,14,2,5,4,13,6,10,7,12,9,1,11,3","6"},
{"8,14,2,5,4,13,6,10,7,12,9,1,11,3","57"},
{"8,14,2,5,4,13,6,10,7,12,9,1,11,3","70"},
{"8,14,2,5,4,13,6,10,7,12,9,1,11,3","121"},
{"10,13,2,4,3,7,6,9,8,12,5,14,11,1","0"},
{"10,13,2,4,3,7,6,9,8,12,5,14,11,1","2"},
{"10,13,2,4,3,7,6,9,8,12,5,14,11,1","125"},
{"10,13,2,4,3,7,6,9,8,12,5,14,11,1","127"},
{"10,13,2,5,4,7,6,12,8,9,3,14,11,1","0"},
{"10,13,2,5,4,7,6,12,8,9,3,14,11,1","16"},
{"10,13,2,5,4,7,6,12,8,9,3,14,11,1","111"},
{"10,13,2,5,4,7,6,12,8,9,3,14,11,1","127"},
{"10,13,2,7,4,5,6,9,8,12,3,14,11,1","0"},
{"10,13,2,7,4,5,6,9,8,12,3,14,11,1","4"},
{"10,13,2,7,4,5,6,9,8,12,3,14,11,1","123"},
{"10,13,2,7,4,5,6,9,8,12,3,14,11,1","127"},
{"10,13,2,7,4,6,5,9,8,12,3,14,11,1","0"},
{"10,13,2,7,4,6,5,9,8,12,3,14,11,1","4"},
{"10,13,2,7,4,6,5,9,8,12,3,14,11,1","123"},
{"10,13,2,7,4,6,5,9,8,12,3,14,11,1","127"},
{"10,14,2,13,4,7,6,9,8,12,5,1,11,3","60"},
{"10,14,2,13,4,7,6,9,8,12,5,1,11,3","61"},
{"10,14,2,13,4,7,6,9,8,12,5,1,11,3","66"},
{"10,14,2,13,4,7,6,9,8,12,5,1,11,3","67"},
{"12,14,2,5,4,13,6,9,8,11,10,1,7,3","6"},
{"12,14,2,5,4,13,6,9,8,11,10,1,7,3","57"},
{"12,14,2,5,4,13,6,9,8,11,10,1,7,3","70"},
{"12,14,2,5,4,13,6,9,8,11,10,1,7,3","121"},
{"14,5,2,3,4,7,6,9,8,11,10,13,12,1","0"},
{"14,5,2,3,4,7,6,9,8,11,10,13,12,1","2"},
{"14,5,2,3,4,7,6,9,8,11,10,13,12,1","125"},
{"14,5,2,3,4,7,6,9,8,11,10,13,12,1","127"},
{"14,5,2,4,3,7,6,9,8,11,10,13,12,1","0"},
{"14,5,2,4,3,7,6,9,8,11,10,13,12,1","2"},
{"14,5,2,4,3,7,6,9,8,11,10,13,12,1","125"},
{"14,5,2,4,3,7,6,9,8,11,10,13,12,1","127"}};

        String[][] alones = {{"10,11,2,7,4,6,5,3,8,1,9,12","0"}};

        ArrayList<MapD> list = new ArrayList<MapD>();
        for (String[] st: alones) {
            String code = st[0];
            int colors = Integer.parseInt(st[1]);
            GBlink m = new GBlink(new MapWord(code));
            m.setColor(colors);
            list.add(new MapD(m));
        }

        // desenhar o mapa
        JFrame f = new JFrame("Map Drawing");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(new Dimension(150,150));
        f.setContentPane(new DrawPanelMultipleMaps(list,1,1));
        f.setVisible(true);
        // desenhar o mapa

    }


    public static void mainSingle(String[] args) throws IOException {
        //MapWord mapWord = new MapWord(new int[] {4,3,2,1}); // cubo
        //MapWord mapWord = new MapWord(new int[] {3,2,1,4}); // cubo
        //MapWord mapWord = new MapWord(new int[] {1,2}); // cubo
        //MapWord mapWord = new MapWord(new int[] {6,10,2,5,4,7,3,8,1,9});
        //MapWord mapWord = new MapWord(new int[] {1,5,2,4,3,6}); // L3,1
        //MapWord mapWord = new MapWord(new int[] {6,12,2,7,4,10,5,11,8,1,9,3}); // torus
        //MapWord mapWord = new MapWord(new int[] {8,3,2,5,4,7,6,1}); // torus
        //MapWord mapWord = new MapWord(new int[] {6,13,2,10,4,5,3,9,8,11,7,12,1,14});
        //MapWord mapWord = new MapWord(new int[] {6,12,2,7,4,10,5,11,8,1,9,3});
        //MapWord mapWord = new MapWord(new int[] {8,11,2,7,4,6,5,10,3,9,1,12});

        MapWord mapWord = new MapWord(new int[] {4,2,1,9,3,10,5,7,8,12,6,11});
        GBlink map = new GBlink(mapWord);
        map.setColor("001111");
        map.write();

        // MapWord mapWord = new MapWord(new int[] {8,11,2,7,4,6,5,10,3,9,1,12});
        // Map map = new Map(mapWord);
        // map.setColor("010110");
        // map.write();

        // MapWord mapWord = new MapWord(new int[] {6,3,2,5,4,1});
        // Map map = new Map(mapWord);
        // map.setColor("000");
        // map.write();

        // MapWord mapWord = new MapWord(new int[] {1,6,2,5,4,3});
        // Map map = new Map(mapWord);
        // map.setColor("000");
        // map.write();

        // MapWord mapWord = new MapWord(new int[] {6,1,2,5,4,3});
        // Map map = new Map(mapWord);
        // map.setColor("100");
        // map.write();

        HomologyGroup hg = map.homologyGroupFromGem();
        System.out.println("Homol.Group: "+hg.toString());

        long t = System.currentTimeMillis();
        QI result = map.quantumInvariant(3,6);
        result.print();
        System.out.println(String.format("total time = %.2f",(System.currentTimeMillis()-t)/1000.0));



        //0111101
        //map.setColor("0111101");
        //map.setColor(63);

        // desenhar o mapa
        MapD md = new MapD(map);
        JFrame f = new JFrame("Map Drawing");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(new Dimension(150,150));
        f.setContentPane(new DrawPanel(md));
        f.setVisible(true);
        // desenhar o mapa

    }


    public static void main(String[] args) throws IOException {
        //MapWord mapWord = new MapWord(new int[] {10,14,2,5,4,3,6,9,8,7,1,13,12,11});
        //MapWord mapWord = new MapWord(new int[] {6,3,2,5,4,1});
        MapWord mapWord = new MapWord(new int[] {14,18,2,7,4,6,5,17,8,13,10,12,11,16,9,1,15,3});
        GBlink map = new GBlink(mapWord);
        //map.setColor(4,BlinkColor.red);
        //map.setColor(5,BlinkColor.red);
        // map.goToCodeLabelTakingCareOfColors();

        map.write();

        // desenhar o mapa
        MapD md = new MapD(map);
        JFrame f = new JFrame("Map Drawing");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(new Dimension(250,250));
        f.setContentPane(new DrawPanel(md));
        f.setVisible(true);
        // desenhar o mapa
    }


    public static void main4(String[] args) throws IOException {
        MapWord mapWord = new MapWord(new int[] {9,3,2,5,4,1,6,8,7,10}); // cubo
        GBlink map = new GBlink(mapWord);
        map.setColor(1,BlinkColor.green);
        map.setColor(2,BlinkColor.green);
        map.setColor(3,BlinkColor.red);
        map.setColor(4,BlinkColor.green);
        map.setColor(5,BlinkColor.red);

        // desenhar o mapa
        MapD md = new MapD(map);
        JFrame f = new JFrame("Map Drawing");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(new Dimension(250,250));
        f.setContentPane(new DrawPanel(md));
        f.setVisible(true);
        // desenhar o mapa


        GemFromBlink gfb = new GemFromBlink(map);
        Gem gem = gfb.getGem();

        gem.write();
        ArrayList<Residue> residues = gem.findResidues(GemColor.yellow,GemColor.red,GemColor.green);
        for (Residue r: residues)
            r.write();

    }

}


enum BlinkColor {
    green, red;
}




