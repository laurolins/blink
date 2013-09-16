package blink;

import java.awt.Dimension;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.Set;
import java.util.Stack;
import java.util.StringTokenizer;

import javax.swing.JFrame;
import javax.swing.JTabbedPane;

import edu.uci.ics.jung.algorithms.flows.EdmondsKarpMaxFlow;
import edu.uci.ics.jung.graph.Edge;
import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.graph.impl.DirectedSparseEdge;
import edu.uci.ics.jung.graph.impl.DirectedSparseGraph;
import edu.uci.ics.jung.graph.impl.DirectedSparseVertex;
import edu.uci.ics.jung.io.GraphMLFile;
import edu.uci.ics.jung.utils.MutableInteger;
import edu.uci.ics.jung.utils.Pair;
import edu.uci.ics.jung.utils.UserData;

public class Gem implements Cloneable, Comparable {

    private ArrayList<GemVertex> _vertices = new ArrayList<GemVertex>();

    /**
     * Create Gem from vertices
     */
    private Gem(ArrayList<GemVertex> vertices) {
        _vertices = vertices;
    }

    /**
     * Hashcode that is used on the database
     * @return long
     */
    public long getGemHashCode() {
        return this.getCurrentLabelling().getGemHashCode();
    }

    /**
     * Create a gem from the blink like this:
     * 1. each vertex on the blink corresponds to a vertex on the gem
     * 2. each blink "face" edge corresponds to a blue edge
     * 3. each blink "vertex" edge corresponds to a red edge
     * 4. each blink "angle" edge corresponds to a green edge
     * 5. each blink "face", "vertex", "angle" path corresponds to an yellow edge
     * @param b Blink
     */
    public Gem(GBlink b) {
        _vertices = new ArrayList<GemVertex>();
        HashMap<GBlinkVertex,GemVertex> map = new HashMap<GBlinkVertex,GemVertex>();
        int i=1;
        for (GBlinkVertex bv: b.getVertices()) {
            GemVertex gv = this.newVertex(bv.getLabel());
            map.put(bv,gv);
        }

        for (GBlinkVertex bv: b.getVertices()) {

            if (bv.getLabel() % 2 == 0)
                continue;

            GBlinkVertex faceNeighbour = bv.getNeighbour(GBlinkEdgeType.face);
            GBlinkVertex vertexNeighbour = bv.getNeighbour(GBlinkEdgeType.vertex);
            GBlinkVertex angleNeighbour = bv.getNeighbour(GBlinkEdgeType.edge);

            GemVertex gv = map.get(bv);
            GemVertex g0 = map.get(
                    faceNeighbour.getNeighbour(GBlinkEdgeType.vertex).
                    getNeighbour(GBlinkEdgeType.edge));
            GemVertex g1 = map.get(faceNeighbour);
            GemVertex g2 = map.get(vertexNeighbour);
            GemVertex g3 = map.get(angleNeighbour);

            Gem.setNeighbours(gv,g0,GemColor.yellow);
            Gem.setNeighbours(gv,g1,GemColor.blue);
            Gem.setNeighbours(gv,g2,GemColor.red);
            Gem.setNeighbours(gv,g3,GemColor.green);
        }
    }

    public Gem(File file) throws FileNotFoundException, IOException {
        StringBuffer s = new StringBuffer();
        FileReader fr = new FileReader(file);
        int c;
        while ((c = fr.read()) != -1) {
            s.append((char) c);
        }
        fr.close();
        this.createGemFromExpandedWord(s.toString());
    }

    public static void testDipoleCheck() throws FileNotFoundException, IOException {
        Gem g = new Gem(new File("c:/nodipole.gem"));
        Dipole d = g.findAnyDipole();
        System.out.println(""+d);
        System.out.println("Agemality: "+g.getAgemality());

        JTabbedPane t = new JTabbedPane();
        t.add("No Dipole?? See 42 65 blue",new PanelGemViewer(g));

        // desenhar o mapa
        JFrame f = new JFrame("Reduction Graph");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(new Dimension(1024,768));
        f.setContentPane(t);
        f.setVisible(true);
        // desenhar o mapa

    }

    public static void main(String[] args)
            throws
            FileNotFoundException,
            IOException,
            SQLException,
            ClassNotFoundException {

        testS3();

        //testFindMaximumLabellings();
        //testDipoleCheck();
        //testMonopoles();
        //testUMoveFindingPathBetweenTSClasses();
        //testUMove();
        //testLinsMandelGem();
        //testHG();
        //testGemHG();
        //testGemCode();
        //testGemCreationFromLabelling();
    }

    public static void testS3() throws ClassNotFoundException, IOException, SQLException {
        // GemEntry e = App.getRepositorio().getGemEntryByCatalogNumber(28,19);
        GemPackedLabelling lbl = new GemPackedLabelling("eabcdhfgmijklpnosqrvtuywxhosvyeipgxurnmqabltckwdjfvyhosxlcwtqpfkdgnjeriaumb");
        //Gem gem = e.getGem();
        Gem gem = new Gem(lbl);
        gem.applyColorPermutation(new GemColor[] {GemColor.green,
                                  GemColor.yellow,
                                  GemColor.blue,
                                  GemColor.red});

        DifferenceToS3 d = gem.getDifferenceToS3();
        System.out.println(""+d.toString());

        System.exit(0);
    }

    public Gem(String expandedWord) {
        this.createGemFromExpandedWord(expandedWord);
    }

    public ArrayList<Gist> getAllGists() {
        Gem gem = this.copy();
        gem.goToCodeLabel();

        ArrayList<Permutation> pPlusCandidates = new ArrayList<Permutation>();
        ArrayList<Permutation> pMinusCandidates = new ArrayList<Permutation>();

        /**
         * @todo the findMaximumRelabelings and findAllRelabelings are
         * giving different results!
         */
        ArrayList<GemVertexAndColorPermutation> list2 = gem.findMaximumRelabelings();
        ArrayList<GemVertexAndColorPermutation> list = gem.findAllRelabelings();
        if (list.size() != list2.size())
            System.out.println("Problema!!!");

        for (GemVertexAndColorPermutation a : list) {
            // System.out.println(a.toString());
            Permutation p = gem.getVertexPermutationFromRelabellingWith(a);
            // System.out.println(""+p.toString());
            // add p to plist if it is an involution
            if (p.isInvolution()) {
                GemColor c[] = a.getPermutation();
                if (c[0] == GemColor.blue &&
                    c[1] == GemColor.yellow &&
                    c[2] == GemColor.green &&
                    c[3] == GemColor.red)
                    pPlusCandidates.add(p);
                else if (
                        c[0] == GemColor.red &&
                        c[1] == GemColor.green &&
                        c[2] == GemColor.yellow &&
                        c[3] == GemColor.blue)
                    pMinusCandidates.add(p);
            }
        }

        //
        HashSet<Pair> strings = new HashSet<Pair>();
        for (int i = 0; i < pPlusCandidates.size(); i++) {
            Permutation p1 = pPlusCandidates.get(i);
            for (int j = 0; j < pMinusCandidates.size(); j++) {
                Permutation p2 = pMinusCandidates.get(j);

                // if (p1.equals(p2))
                //    continue;

                Permutation p1p2 = Permutation.compose(p1, p2);
                Permutation p2p1 = Permutation.compose(p2, p1);
                if (p1p2.equals(p2p1)) {
                    strings.add(new Pair(p1, p2));
                }
            }
        }

        // strings!!!!
        for (Pair p : strings) {
            Permutation p1 = (Permutation) p.getFirst();
            Permutation p2 = (Permutation) p.getSecond();
            System.out.println("string with permutations");
            System.out.println("" + p1);
            System.out.println("" + p2);
        }
        System.out.println("Strings: " + strings.size());

        //
        ArrayList<Gist> gists = new ArrayList<Gist>();

        // build a graph
        for (Pair p: strings) {

            Permutation p1 = (Permutation) p.getFirst(); // +
            Permutation p2 = (Permutation) p.getSecond(); // -
            Permutation p3 = Permutation.compose(p1, p2); // x

            Gist gist = new Gist();

            // create vertices
            HashMap<Integer, GistVertex> map = new HashMap<Integer, GistVertex>();
            for (GemVertex v : gem.getVertices()) {
                GistVertex vv = gist.newVertex();
                map.put(v.getLabel(), vv);
            }

            // create edges
            for (GemVertex u : gem.getVertices()) {
                int i = u.getLabel();
                GistVertex uu = map.get(i);

                { // first neighbour original 0 color neighbour
                    GemVertex v = u.getYellow();
                    int j = v.getLabel();
                    if (i <= j) {
                        GistVertex vv = map.get(j);
                        gist.setNeighbours(uu, vv, GistEdgeType.zero);
                    }
                }

                { // neighbour by the plus permutation
                    int j = p1.getImage(i);
                    if (i <= j) {
                        GistVertex vv = map.get(j);
                        gist.setNeighbours(uu, vv, GistEdgeType.plus);
                    }
                }

                { // neighbour by the plus permutation
                    int j = p2.getImage(i);
                    if (i <= j) {
                        GistVertex vv = map.get(j);
                        gist.setNeighbours(uu, vv, GistEdgeType.minus);
                    }
                }

                { // neighbour by the plus permutation
                    int j = p3.getImage(i);
                    if (i <= j) {
                        GistVertex vv = map.get(j);
                        gist.setNeighbours(uu, vv, GistEdgeType.times);
                    }
                }
            }
            gists.add(gist);
        }
        return gists;
    }

    /**
     * FourFoldGist. GemString of another Gem. This is a
     * bijection!
     * @return ArrayList
     */
    public Gist getFourFoldGist() {
        Gist gist = new Gist();

        HashMap<GemVertex,GistVertex[]> map = new HashMap<GemVertex,GistVertex[]>();
        for (GemVertex v: this._vertices) {
            GistVertex vvs[] = {
                               gist.newVertex(),
                               gist.newVertex(),
                               gist.newVertex(),
                               gist.newVertex()};
            if (v.getLabel() == 1) {
                for (int i=0;i<4;i++) {
                    gist.setNeighbours(vvs[i], vvs[i], GistEdgeType.plus);
                    gist.setNeighbours(vvs[i], vvs[i], GistEdgeType.minus);
                    gist.setNeighbours(vvs[i], vvs[i], GistEdgeType.times);
                }
            }
            else {
                gist.setNeighbours(vvs[0], vvs[1], GistEdgeType.plus);
                gist.setNeighbours(vvs[1], vvs[3], GistEdgeType.minus);
                gist.setNeighbours(vvs[3], vvs[2], GistEdgeType.plus);
                gist.setNeighbours(vvs[2], vvs[0], GistEdgeType.minus);
                gist.setNeighbours(vvs[0], vvs[3], GistEdgeType.times);
                gist.setNeighbours(vvs[1], vvs[2], GistEdgeType.times);
            }
            map.put(v,vvs);
        }

        for (GemVertex v: this._vertices) {
            GistVertex vvs[] = map.get(v);
            for (GemColor c: GemColor.values()) {
                GemVertex u = v.getNeighbour(c);
                if (v.getLabel() > u.getLabel())
                    continue;
                GistVertex uus[] = map.get(u);
                gist.setNeighbours(
                        vvs[c.getNumber()],
                        uus[c.getNumber()],
                        GistEdgeType.zero);
            }
        }

        return gist;
    }

    /**
     * Get four fold gem.
     */
    public Gem getFourFoldGem() {
        return this.getFourFoldGist().getGem();
    }

    /** Checks whether this gem is valid. */
    public boolean check() {

        System.out.println(""+this.getStringWithNeighbours());

        for (GemVertex v: _vertices) {
            if (v.getYellow() == v || v.getYellow() == null) {
                System.out.println(""+v.getLabel()+".0 is null or the same");
                return false;
            }
            else if (v.getBlue() == v || v.getBlue() == null) {
                System.out.println(""+v.getLabel()+".1 is null or the same");
                return false;
            }
            else if (v.getRed() == v || v.getRed() == null) {
                System.out.println(""+v.getLabel()+".2 is null or the same");
                return false;
            }
            else if (v.getGreen() == v || v.getGreen() == null) {
                System.out.println(""+v.getLabel()+".3 is null or the same");
                return false;
            }

            GemColor[][] p = {
                             {GemColor.yellow,GemColor.blue},
                             {GemColor.yellow,GemColor.red},
                             {GemColor.yellow,GemColor.green},
                             {GemColor.blue,GemColor.red},
                             {GemColor.blue,GemColor.green},
                             {GemColor.red,GemColor.green}
            };

            for (GemColor[] pi : p) {
                int k = 0;
                GemVertex u = v;
                GemColor c = pi[0];
                while (true) {
                    u = u.getNeighbour(c);
                    if (u == v)
                        break;
                    c = (c == pi[0] ? pi[1] : pi[0]);
                    k++;
                    if (k >= this.getNumVertices()) {
                        System.out.println("Do not go back: "+v.getLabel()+" "+pi[0]+" "+pi[1]);
                        return false;
                    }
                }
            }
        }

        return true;
    }


    private void createGemFromExpandedWord(String expandedWord) {
        StringTokenizer st = new StringTokenizer(expandedWord,",;\t"+((char)0x0D)+((char)0x0A));
        HashMap<Integer,GemVertex> id2v = new HashMap<Integer,GemVertex>();
        HashMap<Integer,Integer> yellowMap = new HashMap<Integer,Integer>();
        HashMap<Integer,Integer> blueMap = new HashMap<Integer,Integer>();
        HashMap<Integer,Integer> redMap = new HashMap<Integer,Integer>();
        HashMap<Integer,Integer> greenMap = new HashMap<Integer,Integer>();
        while (st.hasMoreTokens()) {
            StringTokenizer st2 = new StringTokenizer(st.nextToken(),"YRGB",true);
            int id = Integer.parseInt(st2.nextToken());
            GemVertex v = this.newVertex(id);
            id2v.put(id,v);
            for (int i=1;i<=4;i++) {
                String c = st2.nextToken();
                if (c.equals("Y"))
                    yellowMap.put(id,Integer.parseInt(st2.nextToken()));
                else if (c.equals("B"))
                    blueMap.put(id,Integer.parseInt(st2.nextToken()));
                else if (c.equals("R"))
                    redMap.put(id,Integer.parseInt(st2.nextToken()));
                else if (c.equals("G"))  {
                    greenMap.put(id, Integer.parseInt(st2.nextToken()));
                }
            }
        }

        // vertices
        for (GemVertex v: _vertices) {
            v.setNeighbour(id2v.get(yellowMap.get(v.getLabel())),GemColor.yellow);
            v.setNeighbour(id2v.get(blueMap.get(v.getLabel())),GemColor.blue);
            v.setNeighbour(id2v.get(redMap.get(v.getLabel())),GemColor.red);
            v.setNeighbour(id2v.get(greenMap.get(v.getLabel())),GemColor.green);
        }
    }


    public Gem(GemPackedLabelling gl) {

        for (int i=1;i<=gl.getNumberOfVertcices();i++)
            _vertices.add(new GemVertex(i));

        for (int i=1;i<=this.getNumVertices();i+=2) {
            GemVertex v = _vertices.get(i-1);

            GemVertex w;

            GemVertex w0 = w = _vertices.get(gl.getNeighbour(i,0) - 1);
            Gem.setNeighbours(v,w,GemColor.yellow);        // color zero
            GemVertex w1 = w = _vertices.get(gl.getNeighbour(i,1) - 1);
            Gem.setNeighbours(v,w,GemColor.blue);          // color one
            GemVertex w2 = w = _vertices.get(gl.getNeighbour(i,2) - 1);
            Gem.setNeighbours(v,w,GemColor.red);           // color two
            GemVertex w3 = w = _vertices.get(gl.getNeighbour(i,3) - 1);
            Gem.setNeighbours(v,w,GemColor.green);         // color three

            // System.out.println(String.format("Vertex %d neighbours -> %d %d %d %d", v.getLabel(),
            //                                  w0.getLabel(), w1.getLabel(),
            //                                  w2.getLabel(), w3.getLabel()));
        }

        // set handle number
        _handleNumber = gl.getHandleNumber();
    }


    /**
     * Gem constructor of Lins-Mandel Manifolds S(b,l,t,c).
     * See page 33 of Gems, Computers and Attractors.
     * @param gl GemLabelling
     */
    public Gem(int b, int l, int t, int c) {
        HashMap<Dimension,GemVertex> map = new HashMap<Dimension,GemVertex>();
        int k = 1;
        for (int i = 1; i <= b; i++) {
            for (int j = 1; j <= 2*l; j++) {
                GemVertex v = this.newVertex(k++);
                map.put(new Dimension(i,j),v);
            }
        }

        for (int i = 1; i <= b; i++) {
            for (int j = 1; j <= 2*l; j++) {
                GemVertex v = map.get(new Dimension(i,j));

                int i0 = norm(i+c*mi(j-t,l),b);
                int j0 = norm(1-j+2*t,2*l);
                GemVertex v0 = map.get(new Dimension(i0,j0));

                int i1 = i;
                int j1 = norm(j-(j%2==0?1:-1),2*l);
                GemVertex v1 = map.get(new Dimension(i1,j1));

                int i2 = i;
                int j2 = norm(j+(j%2==0?1:-1),2*l);
                GemVertex v2 = map.get(new Dimension(i2,j2));

                int i3 = norm(i+mi(j,l),b);
                int j3 = norm(1-j,2*l);
                GemVertex v3 = map.get(new Dimension(i3,j3));

                v.setNeighbour(v0,GemColor.yellow);
                v.setNeighbour(v1,GemColor.blue);
                v.setNeighbour(v2,GemColor.red);
                v.setNeighbour(v3,GemColor.green);
                /*
                System.out.println(String.format("%d %d -> %s %d %d - %s %d %d - %s %d %d - %s %d %d",
                                                 i,j,
                                                 GemColor.yellow,i0,j0,
                                                 GemColor.blue,i1,j1,
                                                 GemColor.red,i2,j2,
                                                 GemColor.green,i3,j3
                                   )); */
            }
        }
    }

    public int norm(int x, int base) {
        x = x % base;
        if (x < 0) x += base;
        if (x == 0) x = base;
        return x;
    }

    public int mi(int j, int l) {
        return (norm(j,2*l)<=l ? 1 : -1);
    }

    public GemVertex getVertex(int lbl) {
        return _vertices.get(lbl-1);
    }

    public Gem() {
    }

    public Gem copy() {
        Gem g = null;
        try {
            HashMap<GemVertex,GemVertex> map = new HashMap<GemVertex,GemVertex>();
            g = (Gem)this.clone();
            g._tagged = false;
            g._components = null;
            g._vertices = new ArrayList<GemVertex>();
            for (int i=0;i<this._vertices.size();i++) {
                GemVertex vSource = this._vertices.get(i);
                GemVertex vTarget = vSource.copy();
                g._vertices.add(vTarget);
                // System.out.println("Map: "+vSource+" -> "+vTarget);
                map.put(vSource,vTarget);
            }
            for (GemVertex v: g._vertices)
                for (GemColor c: GemColor.values()) {
                    GemVertex oldNeighbour = v.getNeighbour(c);
                    GemVertex newNeighbour = map.get(oldNeighbour);
                    // System.out.println(""+oldNeighbour+" -> "+newNeighbour);
                    v.setNeighbour(newNeighbour, c);
                }
            return g;
        } catch (CloneNotSupportedException ex) {
            throw new RuntimeException();
        }
    }

    public GemVertex newVertex(int label) {
        GemVertex v = new GemVertex(label);
        _vertices.add(v);
        return v;
    }

    public void addVertex(GemVertex v) {
        _vertices.add(v);
    }
    
    public int getNumBlobs() {
        return this.getComponentRepository().getNumberOfBlobs();
    }

    private void sortVerticesByLabel() {
        Collections.sort(_vertices);
    }

    private int[][] getMatrix() {
        this.sortVerticesByLabel();
        int[][] result = new int[_vertices.size()][4];
        int i=0;
        for (GemVertex v: _vertices) {
            result[i][0] = v.getYellow().getLabel();
            result[i][1] = v.getBlue().getLabel();
            result[i][2] = v.getRed().getLabel();
            result[i][3] = v.getGreen().getLabel();
            i++;
        }
        return result;
    }

    public int getNumVertices() {
        return _vertices.size();
    }

    public void write() {
        synchronized(System.out) {
            int n = this.getNumVertices();
            int[][] m = this.getMatrix();
            for (int i = 0; i < n; i++) {
                System.out.print(String.format("%4d", i + 1));
            }
            System.out.println();
            for (int c = 0; c < 4; c++) {
                for (int i = 0; i < n; i++) {
                    System.out.print(String.format("%4d", m[i][c]));
                }
                System.out.println();
            }

            System.out.println(String.format("agemality: %d",
                                             this.getNumberOfResidues(2)-
                                             this.getNumberOfResidues(3)-
                                             this.getNumberOfResidues(0)));
        }
    }

    public ComponentRepository getComponentRepository() {
        if (!_tagged)
            this.tag();
        return _components;
    }

    public ArrayList<Residue> findResidues(GemColor ... colors) {
        ArrayList<Residue> result = new ArrayList<Residue>();

        for (GemVertex v: _vertices) {
            v.setFlag(false);
        }

        for (GemVertex v: _vertices) {
            if (v.getFlag() == true)
                continue;

            Residue r = new Residue(colors);
            Queue<GemVertex> Q = new LinkedList<GemVertex>();
            Q.offer(v);
            v.setFlag(true);

            // main loop
            while (!Q.isEmpty()) {
                GemVertex x = Q.poll();
                r.addVertex(x);
                for (GemColor c: colors) {
                    GemVertex y = x.getNeighbour(c);
                    if (y.getFlag()==true)
                        continue;
                    else {
                        y.setFlag(true);
                        Q.offer(y);
                    }
                }
            }

            //
            result.add(r);

        }
        return result;
    }

    /**
     * Add blob. The labelling become dirty!
     */
    public void addBlob(GemVertex u, GemColor c) {
        GemVertex v = u.getNeighbour(c);
        GemVertex uu = this.newVertex(this.getNumVertices()+1);
        GemVertex vv = this.newVertex(this.getNumVertices()+1);
        for (GemColor cc: GemColor.values()) {
            if (c == cc) {
                Gem.setNeighbours(v,vv,cc);
                Gem.setNeighbours(u,uu,cc);
            }
            else Gem.setNeighbours(uu,vv,cc);
        }
        this.setGemAsModified();
    }


    public void cancelAllBlobs() {
        ArrayList<Component> blobs = this.getComponentRepository().getBlobs();
        for (Component c: blobs) {
            GemVertex uu = c.getVertex();
            GemVertex vv = uu.getNeighbour(c.getColors()[0]);
            GemColor color = c.getComplementColors()[0];
            GemVertex u = uu.getNeighbour(color);
            GemVertex v = vv.getNeighbour(color);
            Gem.setNeighbours(u,v,color);
            _vertices.remove(uu);
            _vertices.remove(vv);
        }

        this.setGemAsModified();
    }

    public void setGemAsModified() {
        _tagged = false;
    }

    public void applyTwistor(Twistor t, GemColor c1) {
        GemColor c2 = t.getColor();
        GemVertex u = t.getU();
        GemVertex v = t.getV();

        GemColor c0 = GemColor.yellow;
        GemColor c3 = GemColor.getComplementColors(GemColor.getColorSet(c0,c1,c2))[0];

        GemVertex u0 = u.getNeighbour(c0);
        GemVertex u1 = u.getNeighbour(c1);
        // GemVertex u2 = u.getNeighbour(c2);
        // GemVertex u3 = u.getNeighbour(c3);

        GemVertex v0 = v.getNeighbour(c0);
        GemVertex v1 = v.getNeighbour(c1);
        // GemVertex v2 = v.getNeighbour(c2);
        // GemVertex v3 = v.getNeighbour(c3);

        this.setNeighbours(u0,v,c0);
        this.setNeighbours(v0,u,c0);
        this.setNeighbours(u1,v,c1);
        this.setNeighbours(v1,u,c1);

        // set as modified
        this.setGemAsModified();
    }

    public void applyTwistorByLabels(Twistor t, GemColor c1) {
        GemColor c2 = t.getColor();
        int uLabel = t.getU().getLabel();
        int vLabel = t.getV().getLabel();

        GemVertex u = this.findVertex(uLabel);
        GemVertex v = this.findVertex(vLabel);

        GemColor c0 = GemColor.yellow;
        GemColor c3 = GemColor.getComplementColors(GemColor.getColorSet(c0,c1,c2))[0];

        GemVertex u0 = u.getNeighbour(c0);
        GemVertex u1 = u.getNeighbour(c1);
        // GemVertex u2 = u.getNeighbour(c2);
        // GemVertex u3 = u.getNeighbour(c3);

        GemVertex v0 = v.getNeighbour(c0);
        GemVertex v1 = v.getNeighbour(c1);
        // GemVertex v2 = v.getNeighbour(c2);
        // GemVertex v3 = v.getNeighbour(c3);

        this.setNeighbours(u0,v,c0);
        this.setNeighbours(v0,u,c0);
        this.setNeighbours(u1,v,c1);
        this.setNeighbours(v1,u,c1);
    }

    public void applyRhoPair(RhoPair p) {
        this.tag();

        GemVertex u = p.getU();
        GemVertex v = p.getV();
        GemVertex uu = u.getNeighbour(p.getColor());
        GemVertex vv = v.getNeighbour(p.getColor());

        // descobrir um bigon comum às duas arestas
        GemColor c = p.getColor();
        GemColor[] cs = new GemColor[] {c,null};
        for (GemColor cc: GemColor.getColorsOfColorSet(GemColor.getComplementColorSet(c))){
            int colorSet = GemColor.getColorSet(c,cc);
            if (u.getComponent(colorSet) == v.getComponent(colorSet)) {
                cs[1] = cc;
                break;
            }
        }

        // calcular a distância entre u e v (num de arestas entre elas)
        int i=0;
        int k = 0;
        GemVertex x = u;
        while (x != v) {
            x = x.getNeighbour(cs[i]);
            k++;
            i=(i+1)%2;
        }

        if (k % 2 == 0) { // se a distancia for par entao liga assim
            Gem.setNeighbours(u,vv,p.getColor());
            Gem.setNeighbours(v,uu,p.getColor());
        }
        else { // se a distancia for impar entao liga assim
            Gem.setNeighbours(u,v,p.getColor());
            Gem.setNeighbours(vv,uu,p.getColor());
        }

        if (p.foundAsA() == 3)
            _handleNumber++;
        this.setGemAsModified();
    }

    private int _handleNumber = 0;

    public int getHandleNumber() {
        return _handleNumber;
    }

    public void setHandleNumber(int hn) {
        _handleNumber = hn;
    }

    public int getBiggestLabel() {
        int result = Integer.MIN_VALUE;
        for (GemVertex v: _vertices) {
            if (result < v.getLabel())
                result = v.getLabel();
        }
        return result;
    }

    public void cancelDipole(Dipole d) {
        GemVertex uu = d.getU();
        GemVertex vv = d.getV();
        for (GemColor c: d.getComplementColors()) {
            GemVertex u = uu.getNeighbour(c);
            GemVertex v = vv.getNeighbour(c);
            Gem.setNeighbours(u,v,c);
        }
        _vertices.remove(uu);
        _vertices.remove(vv);

        //
        this.setGemAsModified();
    }

    public void applyInverseDipole(InverseDipole i) {
        GemVertex ev1 = this.findVertex(i.getEdgeVertex1());
        GemVertex ev2 = this.findVertex(i.getEdgeVertex2());

        GemColor ec1 = i.getEdgeColor1();
        GemColor ec2 = i.getEdgeColor2();

        GemVertex nev1 = ev1.getNeighbour(ec1);
        GemVertex nev2 = ev2.getNeighbour(ec2);

        GemVertex nv1 = this.newVertex(i.getNewLabel1());
        GemVertex nv2 = this.newVertex(i.getNewLabel2());

        Gem.setNeighbours(ev1,nv1,ec1);
        Gem.setNeighbours(ev2,nv1,ec2);

        GemColor[] cc = GemColor.getComplementColors(ec1,ec2);
        Gem.setNeighbours(nv1,nv2,cc[0]);
        Gem.setNeighbours(nv1,nv2,cc[1]);

        Gem.setNeighbours(nv2,nev1,ec1);
        Gem.setNeighbours(nv2,nev2,ec2);

        //
        this.setGemAsModified();
    }

    public int getMaxLabel() {
        int maxLabel = -1;
        for (GemVertex v: _vertices) {
            if (v.getLabel()>maxLabel) {
                maxLabel = v.getLabel();
            }
        }
        return maxLabel;
    }

    /**
     *
     * @param u GemVertex
     * @param c1 GemColor
     */
    public void doubleTwoDipoleCreation(GemVertex u, GemColor c1) {

        GemColor c0 = GemColor.yellow;
        GemColor[] cc = GemColor.getComplementColors(c1,c0);
        GemColor c2 = cc[0];
        GemColor c3 = cc[1];

        GemVertex u0 = u.getNeighbour(c0);
        GemVertex u1 = u.getNeighbour(c1);
        GemVertex u2 = u.getNeighbour(c2);
        GemVertex u3 = u.getNeighbour(c3);

        int nextLabel = this.getMaxLabel() + 1;

        GemVertex v = this.newVertex(-1);
        GemVertex w = this.newVertex(-1);
        if (u.getLabel()% 2 == nextLabel % 2) {
            v.setLabel(nextLabel+1);
            w.setLabel(nextLabel);
        }
        else {
            v.setLabel(nextLabel);
            w.setLabel(nextLabel+1);
        }

        Gem.setNeighbours(u,v,c1,c3);
        Gem.setNeighbours(v,w,c0,c2);
        Gem.setNeighbours(w,u1,c1);
        Gem.setNeighbours(w,u3,c3);

        //
        this.setGemAsModified();
    }

    /**
     * Apply U move on this gem.
     */
    public void uMove(Monopole monopole) {
        this.tag();

        GemVertex u = monopole.getVertex();
        Component b1 = monopole.getBigon1();
        Component b2 = monopole.getBigon2();

        int m = b1.size();
        int n = b2.size();

        //
        GemColor[] c1 = monopole.getColorsBigon1();
        GemColor[] c2 = monopole.getColorsBigon2();

        GemVertex[] v1s = new GemVertex[m];
        { // put vertices on array
            ArrayList<GemVertex> vs = b1.getVerticesFromBigon();
            int index = vs.indexOf(u);
            int increment = (u.getNeighbour(c1[0]) == vs.get((index+1) % m) ? 1 : -1);
            for (int i = 0; i < m; i++) {
                int ii = (index + i*increment) % m;
                if (ii < 0) ii+=m;
                v1s[i] = vs.get(ii);
                // System.out.println(String.format("v1[%d] = %d",i,v1s[i].getLabel()));
            }
        }

        GemVertex[] v2s = new GemVertex[n];
        { // put vertices on array
            ArrayList<GemVertex> vs = b2.getVerticesFromBigon();
            int index = vs.indexOf(u);
            int increment = (u.getNeighbour(c2[0]) == vs.get((index+1) % n) ? 1 : -1);
            for (int i = 0; i < n; i++) {
                int ii = (index + i*increment) % n;
                if (ii < 0) ii+=n;
                v2s[i] = vs.get(ii);
                // System.out.println(String.format("v2[%d] = %d",i,v2s[i].getLabel()));
            }
        }

        //
        HashSet<GemVertex> inVertices = new HashSet<GemVertex>();
        for (GemVertex v: v1s) inVertices.add(v);
        for (GemVertex v: v2s) inVertices.add(v);

        // fake edges
        ArrayList<FakeEdge> fakeEdges = new ArrayList<FakeEdge>();

        //
        int k = this.getBiggestLabel()+1;
        GemVertex[][] M = new GemVertex[m+1][n+1];
        for (int i=0;i<=m;i++)
            for (int j=0;j<=n;j++) {
                if (i == 0 && j > 0 && j < n) {
                    M[i][j] = getProtectedNeighbour(v2s[j],c1[0],inVertices,fakeEdges);
                }
                else if (i == m && j > 0  && j < n) {
                    M[i][j] = getProtectedNeighbour(v2s[j],c1[1],inVertices,fakeEdges);
                }
                else if (j == 0 && i > 0 && i < m) {
                    M[i][j] = getProtectedNeighbour(v1s[i],c2[0],inVertices,fakeEdges);
                }
                else if (j == n && i > 0 && i < m) {
                    M[i][j] = getProtectedNeighbour(v1s[i],c2[1],inVertices,fakeEdges);
                }
                else if (j > 0 && i > 0 && i < m && j < n) M[i][j] = this.newVertex(k++);
            }

        /*
        for (int j=n;j>=0;j--) {
            for (int i = 0; i <= m; i++) {
                GemVertex v = M[i][j];
                if (v != null)
                    System.out.print(String.format("%3d ", v.getLabel()));
                else
                    System.out.print(String.format("    "));
            }
            System.out.println();
        }*/

        //
        for (int i=1;i<=m;i++)
            for (int j=1;j<=n;j++) {
                // System.out.println(String.format("%3d %3d",i,j));
                // System.out.println(""+M[i-1][j]);
                // System.out.println(""+M[i][j-1]);
                if (j < n) {
                    GemColor cc = (i % 2) == 1 ? c1[0] : c1[1];
                    Gem.setNeighbours(M[i][j], M[i - 1][j], cc);
                    /*
                     System.out.println(String.format("Connect %3d  %3d %s",
                                                     M[i][j].getLabel(),
                                                     M[i - 1][j].getLabel(),
                                                     cc)); */
                }
                if (i < m) {
                    GemColor cc = (j % 2) == 1 ? c2[0] : c2[1];
                    Gem.setNeighbours(M[i][j], M[i][j - 1], cc);
                    /*
                     System.out.println(String.format("Connect %3d  %3d %s",
                                                     M[i][j].getLabel(),
                                                     M[i][j-1].getLabel(),
                                                     cc)); */
                }
            }

        // erase other vertices
        for (GemVertex v: v1s)
            _vertices.remove(v);
        for (GemVertex v: v2s)
            _vertices.remove(v);

        // Remove Fake Edges
        for (FakeEdge fe : fakeEdges) {
            GemVertex xx = fe.getA().getNeighbour(fe.getColor());
            GemVertex yy = fe.getB().getNeighbour(fe.getColor());
            Gem.setNeighbours(xx, yy,fe.getColor());
            /*
            System.out.println(String.format("Fake edge %6d %6d Connecting: %6d to %6d with color %10s",
                                             fe.getA().getLabel(), fe.getB().getLabel(), xx.getLabel(), yy.getLabel(),
                                             fe.getColor()));
            */
        }

        //
        this.setGemAsModified();
    }

    private GemVertex getProtectedNeighbour(
            GemVertex inVertex,
            GemColor c,
            HashSet<GemVertex> inVertices,
            ArrayList<FakeEdge> fakeEdges) {
        GemVertex boundaryVertex = inVertex.getNeighbour(c);
        if (inVertices.contains(boundaryVertex)) {
            FakeEdge e = new FakeEdge(c);
            Gem.setNeighbours(inVertex,e.getA(),c);
            Gem.setNeighbours(boundaryVertex,e.getB(),c);
            fakeEdges.add(e);
            boundaryVertex = e.getA();
        }
        return boundaryVertex;
    }

    public static void testUMove() {
        GBlink b = new GBlink(new int[][] {{1},{1}}, new int[] {});
        Gem g = b.getGem();

        Monopole m = g.findMonopoles().get(0);
        System.out.println("Apply U Move at "+m);


        g.copyCurrentLabellingToOriginalLabelling();

        JTabbedPane t = new JTabbedPane();
        t.add("Before U-Move at "+m,new PanelGemViewer(g.copy()));

        g.uMove(m);
        g.copyCurrentLabellingToOriginalLabelling();

        System.out.println("Agemality: "+g.getAgemality());

        t.add("After U-Move at "+m,new PanelGemViewer(g));

        // desenhar o mapa
        JFrame f = new JFrame("Operations");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(new Dimension(1023,768));
        f.setContentPane(t);
        f.setVisible(true);
        // desenhar o mapa
    }

    public static void testMonopoles() {
        Gem g = new Gem(new GemPackedLabelling("cabedhfgjimklhldckemjgafbidkglcmeihbjaf"));
        g.copyCurrentLabellingToOriginalLabelling();


        Gem gm = g.copy();
        ArrayList<Monopole> monopoles = gm.findMonopoles();
        // for (Monopole m: monopoles)
        //    System.out.println(""+m);
        Monopole m = monopoles.get(0);
        System.out.println("Applying uMove at: "+m);
        gm.uMove(m);
        gm.copyCurrentLabellingToOriginalLabelling();

        System.out.println("G  Agemality: " + g.getAgemality());
        System.out.println("Gu Agemality: " + gm.getAgemality());
        JTabbedPane t = new JTabbedPane();
        t.add("Before U-Move at "+m,new PanelGemViewer(g.copy()));

        try {
            System.out.println("Agemality: " + gm.getAgemality());
        } catch (Exception ex) {
            ex.printStackTrace();
        }


        try {
            t.add("After U-Move at " + m, new PanelGemViewer(gm.copy()));
        } catch (Exception ex1) {
            ex1.printStackTrace();
        }

        // desenhar o mapa
        JFrame f = new JFrame("Operations");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(new Dimension(1023,768));
        f.setContentPane(t);
        f.setVisible(true);
        // desenhar o mapa

        //Monopole m = g.findMonopoles().get(0);
        //System.out.println("Apply U Move at " + m);

    }

    public static void testUMoveFindingPathBetweenTSClasses() {
        GBlink b = new GBlink(new int[][] {{1,6},{1,2,3},{2},{3,4},{4,5},{5,6}},new int[] {});
        Gem g = b.getGem();
        CalculateReductionGraph crg = new CalculateReductionGraph(g);
        Gem gR = crg.getRepresentant();

        Random r = new Random(21092138011L);
        while (gR.getNumVertices() > 34) {
            ArrayList<Monopole> list = gR.findMonopoles();
            Monopole m = list.get(r.nextInt(list.size()));
            System.out.println("Apply u-Move at: "+m);
            gR.uMove(m);
            crg = new CalculateReductionGraph(gR);
            gR = crg.getRepresentant();
        }

        // desenhar o mapa
        JFrame f = new JFrame("Operations");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(new Dimension(1023,768));
        f.setContentPane(new PanelGemViewer(gR));
        f.setVisible(true);
        // desenhar o mapa

    }


    // static JTabbedPane _tabPane = new JTabbedPane();
    // static int _n = 0;
    public void cancelAllDipoles() {
        this.warrantyParityLabelling();
        // this.goToCodeLabel();
        // this.saveCurrentLabelsAsOriginalLabelOnVertices();
        // System.out.println(this.getComponentRepository().toStringWithOriginalLabels());

        // _tabPane.add("Original",new PanelGemViewer(this.copy()));
        Dipole d;
        while ((d = this.findAnyDipole()) != null) {
            // System.out.println("Cancel: " + d.toStringWithOriginalLabels());
            this.cancelDipole(d);
            // String st = "D"+d.size()+" "+GemColor.getColorSetCompactString(d.getColorSet())+" "+d.getU().getLabel()+" "+d.getV().getLabel();
            // _tabPane.add((_n++)+": "+st,new PanelGemViewer(this.copy()));
        }

        /* // desenhar o mapa
        JFrame f = new JFrame("Operations");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(new Dimension(1023,768));
        f.setContentPane(_tabPane);
        f.setVisible(true);
        // desenhar o mapa */
    }

    public static void testHG() {
        GBlink b = new GBlink(new int[][] {{1,6,4,3},{1,2,7},{2,3,4,5},{5,6,7}}, new int[] {1,5,7});
        HomologyGroup hgBlink = b.homologyGroupFromGBlink();
        HomologyGroup hgGem = b.homologyGroupFromGem();

        System.out.println("Blink: "+hgBlink.toString());
        System.out.println("Gem:   "+hgGem.toString());
    }

    /**
     * Changes the labelling and may need
     * to calculata components again.
     */
    public void warrantyParityLabelling() {
        this.dfsNumbering(_vertices.get(0),GemColor.PERMUTATIONS[0]);
    }

    public Residue findResidue(GemVertex u, GemColor ... colors) {
        for (GemVertex v: _vertices) {
            v.setFlag(false);
        }

        Residue r = new Residue(colors);
        Queue<GemVertex> Q = new LinkedList<GemVertex>();
        Q.offer(u);
        u.setFlag(true);

        // main loop
        while (!Q.isEmpty()) {
            GemVertex x = Q.poll();
            r.addVertex(x);
            for (GemColor c : colors) {
                GemVertex y = x.getNeighbour(c);
                if (y.getFlag() == true)
                    continue;
                else {
                    y.setFlag(true);
                    Q.offer(y);
                }
            }
        }

        return r;
    }

    public ArrayList<GemVertex> getVertices() {
        return _vertices;
    }

    public ArrayList<GemVertex> getOddVertices() {
        ArrayList<GemVertex> R = new ArrayList<GemVertex>();
        for (GemVertex v: _vertices) {
            if (v.hasOddLabel())
                R.add(v);
        }
        return R;
    }

    public int getNumberOfResidues(int numColors) {
        if (numColors == 0) {
            return findResidues().size();
        }
        else if (numColors == 1) {
            return findResidues(GemColor.yellow).size() +
                   findResidues(GemColor.red).size() +
                   findResidues(GemColor.blue).size() +
                   findResidues(GemColor.green).size();
        }
        else if (numColors == 2) {
            return findResidues(GemColor.yellow,GemColor.red).size() +
                   findResidues(GemColor.yellow,GemColor.green).size() +
                   findResidues(GemColor.yellow,GemColor.blue).size() +
                   findResidues(GemColor.red,GemColor.green).size() +
                   findResidues(GemColor.red,GemColor.blue).size() +
                   findResidues(GemColor.green,GemColor.blue).size();
        }
        else if (numColors == 3) {
            return findResidues(GemColor.yellow,GemColor.red,GemColor.green).size() +
                   findResidues(GemColor.yellow,GemColor.red,GemColor.blue).size() +
                   findResidues(GemColor.yellow,GemColor.blue,GemColor.green).size() +
                   findResidues(GemColor.blue,GemColor.red,GemColor.green).size();
        }
        else if (numColors == 4) {
            return findResidues(GemColor.yellow,GemColor.red,GemColor.green,GemColor.blue).size();
        }
        else throw new RuntimeException("");

    }

    public static GemColor _colors[] = {GemColor.yellow,GemColor.blue,GemColor.red,GemColor.green};
    public GemColor[] complement(GemColor ... colors) {
        int n = colors.length;
        boolean all[] = new boolean[4];
        GemColor[] result = new GemColor[4-n];

        for (int i=0;i<n;i++)
            all[colors[i].ordinal()] = true;

        int k=0;
        for (int i=0;i<_colors.length;i++)
            if (!all[i])
                result[k++] = _colors[i];

        return result;
    }

    public boolean isOneDipole(GemVertex u, GemColor c) {
        GemVertex v = u.getNeighbour(c);
        return !findVertex(u,complement(c),v);
    }

    public boolean findVertex(GemVertex root, GemColor[] colors, GemVertex target) {
        for (GemVertex v: _vertices)
            v.setFlag(false);
        return findDfs(root,colors,target);
    }

    public GemVertex findVertex(int label) {
        for (GemVertex v: _vertices)
            if (v.getLabel() == label)
                return v;
        return null;
    }

    public boolean findDfs(GemVertex root, GemColor[] colors, GemVertex target) {
        root.setFlag(true);
        if (root == target)
            return true;
        else {
            boolean result = false;
            root.setFlag(true);
            for (GemColor c: colors) {
                GemVertex u = root.getNeighbour(c);
                if (!u.getFlag())
                     result = findDfs(u,colors,target);
                if (result)
                    break;
            }
            return result;
        }
    }



    // ------------------------------------------------------------------------
    // -- CODE OF A GEM


    public void copyCurrentLabellingToOriginalLabelling() {
        for (GemVertex v: _vertices)
            v.copyLabelToOriginalLabel();
    }

    public void copyTempLabellingToCurrentLabelling() {
        for (GemVertex v: _vertices)
            v.copyTempLabelToLabel();
    }

    public void copyCurrentLabellingToTempLabelling() {
        for (GemVertex v: _vertices) {
            v.copyLabelToTempLabel();
        }
    }

    /**
     * Find the maximum labeling for this gem. This routine
     * may change the coloring permutation on the vertices.
     */
    public GemPackedLabelling goToCodeLabel() {
        int n = _vertices.size()/2;
        GemVertex maxRoot = null;
        GemColor[] maxPermutation = null;
        int[] maxLabeling = new int[n * 3];

        // save current labelling on temp
        this.copyCurrentLabellingToTempLabelling();

        ArrayList<GemVertexAndColorPermutation> possibleRoots = this.getComponentRepository().getPossibleRoots();

        // System.out.println("goToCodeLabel3 vertices and permutations that generate max first row: "+possibleRoots.size());

        for (GemVertexAndColorPermutation pr : possibleRoots) {
            GemVertex v = pr.getVertex();
            GemColor[] p = pr.getPermutation();
            dfsNumbering(v, p);

            // first code?
            if (maxRoot == null) {
                maxRoot = v;
                maxPermutation = p;
                System.arraycopy(__currentLabelling, 0, maxLabeling, 0, n * 3);
            }

            // else compare
            else {
                boolean change = false;
                for (int i = 0; i < 3*n; i++) {
                    if (maxLabeling[i] < __currentLabelling[i]) {
                        change = true;
                        break;
                    } else if (maxLabeling[i] > __currentLabelling[i]) {
                        break;
                    }
                }

                if (change) {
                    maxRoot = v;
                    maxPermutation = p;
                    System.arraycopy(__currentLabelling, 0, maxLabeling, 0, n * 3);
                }
            }
        }

        dfsNumbering(maxRoot,maxPermutation);

        __maxRoot = maxRoot.getTempLabel();
        __maxPermutation = maxPermutation; //(GemColor[]) maxPermutation.clone();

        // apply permutation to each vertex
        for (GemVertex v: _vertices)
            v.applyPermutation(maxPermutation); // make permutation definitive

        //System.out.println("rooting: "+maxRoot+" --- "+maxPermutation[0]+","+maxPermutation[1]+","+maxPermutation[2]+","+maxPermutation[3]);

        this.setGemAsModified();

        return new GemPackedLabelling(maxLabeling,this.getHandleNumber());
    }

    /**
     * Apply color permutation.
     * @param p GemColor[]
     */
    public void applyColorPermutation(GemColor[] p) {
        // apply permutation to each vertex
        // this.dfsNumbering(_vertices.get(0),p);
        for (GemVertex v: _vertices)
            v.applyPermutation(p); // make permutation definitive
        this.dfsNumbering(_vertices.get(0),
                          new GemColor[] {
                          GemColor.yellow,
                          GemColor.blue,
                          GemColor.red,
                          GemColor.green});
        this.setGemAsModified();
    }

    public static void testFindMaximumLabellings() {
        //Gem G = new Gem(3,3,2,1);
        //Gem G = new Gem(3,5,2,1);
        Gem G = new Gem(5,3,2,1);
        G.goToCodeLabel();


        // desenhar o mapa
        JFrame f = new JFrame("Reduction Graph");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(new Dimension(1024,768));
        f.setContentPane(new PanelString(G));
        f.setVisible(true);
        // desenhar o mapa

    }



    public Permutation getVertexPermutationFromRelabellingWith(GemVertexAndColorPermutation i) {

        // save temp labelling
        this.copyCurrentLabellingToTempLabelling();

        dfsNumbering(i.getVertex(),i.getPermutation());

        Permutation p = new Permutation(_vertices.size());
        for (GemVertex v: _vertices) {
            p.setImage(v.getTempLabel(),v.getLabel());
        }

        // save temp labelling
        this.copyTempLabellingToCurrentLabelling();

        return p;
    }




    /**
     * Find all ReLabellings that do not change.
     * Messes up only the temp label of the vertices.
     */
    public ArrayList<GemVertexAndColorPermutation> findMaximumRelabelings() {
        // save labelling vertices on temp field
        for (GemVertex v: _vertices) {
            v.copyLabelToTempLabel();
        }

        //
        int n = _vertices.size()/2;
        GemVertex maxRoot = null;
        int[] maxLabeling = new int[n * 3];

        ArrayList<GemVertexAndColorPermutation> possibleRoots = this.getComponentRepository().getPossibleRoots();

        // System.out.println("goToCodeLabel3 vertices and permutations that generate max first row: "+possibleRoots.size());

        ArrayList<GemVertexAndColorPermutation> roots = new ArrayList<GemVertexAndColorPermutation>();

        for (GemVertexAndColorPermutation pr : possibleRoots) {
            GemVertex v = pr.getVertex();
            GemColor[] p = pr.getPermutation();
            dfsNumbering(v, p);

            // first code?
            if (maxRoot == null) {
                maxRoot = v;
                System.arraycopy(__currentLabelling, 0, maxLabeling, 0, n * 3);
            }

            // else compare
            else {
                boolean change = false;
                boolean equal = true;
                for (int i = 0; i < 3*n; i++) {
                    if (maxLabeling[i] < __currentLabelling[i]) {
                        change = true;
                        break;
                    } else if (maxLabeling[i] > __currentLabelling[i]) {
                        equal = false;
                        break;
                    }
                }

                if (change) {
                    roots.clear();
                    roots.add(pr);
                    maxRoot = v;
                    System.arraycopy(__currentLabelling, 0, maxLabeling, 0, n * 3);
                }

                if (equal) {
                    roots.add(pr);
                }
            }
        }

        // restore the labellings
        for (GemVertex v: _vertices) {
            v.copyTempLabelToLabel();
        }

        return roots;

    }

    /**
     * Find all ReLabellings that do not change.
     * Messes up only the temp label of the vertices.
     */
    public ArrayList<GemVertexAndColorPermutation> findAllRelabelings() {
        // save labelling vertices on temp field
        for (GemVertex v: _vertices) {
            v.copyLabelToTempLabel();
        }

        //
        int n = _vertices.size()/2;
        GemVertex maxRoot = null;
        int[] maxLabeling = new int[n * 3];

        //
        ArrayList<GemVertexAndColorPermutation> possibleRoots = new ArrayList<GemVertexAndColorPermutation>();
        for (GemVertex v: _vertices) {
            for (GemColor[] p: GemColor.PERMUTATIONS) {
                possibleRoots.add(new GemVertexAndColorPermutation(v,p));
            }
        }

        // System.out.println("goToCodeLabel3 vertices and permutations that generate max first row: "+possibleRoots.size());

        ArrayList<GemVertexAndColorPermutation> roots = new ArrayList<GemVertexAndColorPermutation>();

        for (GemVertexAndColorPermutation pr : possibleRoots) {
            GemVertex v = pr.getVertex();
            GemColor[] p = pr.getPermutation();
            dfsNumbering(v, p);

            // first code?
            if (maxRoot == null) {
                maxRoot = v;
                System.arraycopy(__currentLabelling, 0, maxLabeling, 0, n * 3);
            }

            // else compare
            else {
                boolean change = false;
                boolean equal = true;
                for (int i = 0; i < 3*n; i++) {
                    if (maxLabeling[i] < __currentLabelling[i]) {
                        change = true;
                        break;
                    } else if (maxLabeling[i] > __currentLabelling[i]) {
                        equal = false;
                        break;
                    }
                }

                if (change) {
                    roots.clear();
                    roots.add(pr);
                    maxRoot = v;
                    System.arraycopy(__currentLabelling, 0, maxLabeling, 0, n * 3);
                }

                if (equal) {
                    roots.add(pr);
                }
            }
        }

        // restore the labellings
        for (GemVertex v: _vertices) {
            v.copyTempLabelToLabel();
        }

        return roots;

    }



    // -------------------------------------------------------------------
    // -- Static information
    private static int __maxRoot;
    private static GemColor[] __maxPermutation;
    public static int getLastGoToCodeLabellingRootVertexLabel() {
        return __maxRoot;
    }
    public GemColor[] getLastGoToCodeLabellingColorsPermutation() {
        return __maxPermutation;
    }
    // -- Static information
    // -------------------------------------------------------------------

    /**
     * Find the maximum labeling for this gem.
     */
    public GemPackedLabelling getCurrentLabelling() {
        int n = _vertices.size()/2;
        int labelling[] = new int[n * 3];
        for (GemVertex v: _vertices) {
            if (v.getLabel() % 2 == 1) {
                int vLbl = v.getLabel()/2;
                int n1 = v.getNeighbour(GemColor.blue).getLabel()/2;
                int n2 = v.getNeighbour(GemColor.red).getLabel()/2;
                int n3 = v.getNeighbour(GemColor.green).getLabel()/2;
                labelling[0 *n + vLbl] = n1;
                labelling[1 *n + vLbl] = n2;
                labelling[2 *n + vLbl] = n3;
            }
        }
        return new GemPackedLabelling(labelling,this.getHandleNumber());
    }

    private void dfsNumbering(GemVertex root, GemColor[] p) {
        for (GemVertex v: _vertices)
            v.setLabelAsUndefined();
        __i = 0;
        __n = _vertices.size()/2;
        __dfsPermutation = p;
        __currentLabelling = new int[__n * 3];

        dfsSearch(root);

        for (GemVertex v: _vertices) {
            if (v.getLabel() % 2 == 0)
                continue;
            for (int k = 1; k < 4; k++) {
                // int x = v.getLabel();
                // int y = v.getNeighbour(__dfsPermutation[k]).getLabel();
                // System.out.println(String.format("%d %d -> %d",k,x,y));
                __currentLabelling[(k - 1) * __n + v.getLabel() / 2] = v.getNeighbour(__dfsPermutation[k]).getLabel() / 2;
            }
        }
    }

    public String getStringWithNeighbours() {
        StringBuffer sb = new StringBuffer();
        for (GemVertex v: _vertices) {
            if (!v.hasOddLabel())
                continue;
            sb.append(v.getLabel());
            sb.append(" -> ");
            sb.append(v.getYellow().getLabel()+" ");
            sb.append(v.getBlue().getLabel()+" ");
            sb.append(v.getRed().getLabel()+" ");
            sb.append(v.getGreen().getLabel()+" ");
            sb.append("\n");
        }
        return sb.toString();
    }

    static GemColor[] __dfsPermutation;
    static int[] __currentLabelling;
    static int[] __maxLabelling;
    static int __n;
    static int __i;
    private void dfsSearch(GemVertex v) {
        __i = __i+1;
        int vLabel = __i;
        v.setLabel(vLabel);

        for (int k=0;k<4;k++) {
            GemVertex w = v.getNeighbour(__dfsPermutation[k]);
            if (w.isLabelUndefined() && (__i + vLabel) % 2 == 0) {
                dfsSearch(w);
            }
        }
    }
    public Gem ArrumarGema(GemVertex v){
    	this.dfsSearch(v);
    	return this;
 //   	this.findAllRelabelings();
    }

    public boolean isGem(){
    	return this.getNumberOfResidues(3)+this.getNumVertices() ==  this.getNumberOfResidues(2);
    }
    
    public GemVertex removerVertice(int v){
    	GemVertex temp = _vertices.get(v-1);
    	_vertices.remove(v-1);
    	return temp;
    }
    public void inserirVertice(GemVertex v){
    	_vertices.add(v);
 //   	this.newVertex(label);
    }
    
    public void ArrumarNumeracao(){
    	for(int i = 1;i<this.getNumVertices();i++){
    		if(i%2 == 1)
    			this.getVertex(i).getYellow().setLabel(i+1);
    	}
    }
    
    public static void testLinsMandelGem() {
        Gem g = new Gem(5,8,3,2);
        g.goToCodeLabel();
        g.copyCurrentLabellingToOriginalLabelling();

        // desenhar o mapa
        JFrame f = new JFrame("Operations");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(new Dimension(1023,768));
        f.setContentPane(new PanelGemViewer(g));
        f.setVisible(true);
        // desenhar o mapa

    }

    // -- CODE OF A GEM
    // ------------------------------------------------------------------------

    public void cancelDipolesOld() {

        while (true) {
            boolean foundDipole = false;
            GemVertex v = null;
            GemColor c = null;
            for (GemColor cc: GemColor.values()) {
                for (GemVertex vv : _vertices) {
                    if (isOneDipole(vv, cc)) {
                        // System.out.println(String.format("Cancel dipole! %d %s",vv.getLabel(),""+cc));
                        foundDipole = true;
                        v = vv;
                        c = cc;
                        break;
                    }
                }
                if (foundDipole)
                    break;
            }

            if (!foundDipole) {
                break;
            }
            else {
                GemVertex u = v.getNeighbour(c);
                GemColor[] colors = complement(c);
                GemVertex v1 = v.getNeighbour(colors[0]);
                GemVertex v2 = v.getNeighbour(colors[1]);
                GemVertex v3 = v.getNeighbour(colors[2]);
                GemVertex u1 = u.getNeighbour(colors[0]);
                GemVertex u2 = u.getNeighbour(colors[1]);
                GemVertex u3 = u.getNeighbour(colors[2]);
                Gem.setNeighbours(u1,v1,colors[0]);
                Gem.setNeighbours(u2,v2,colors[1]);
                Gem.setNeighbours(u3,v3,colors[2]);
                _vertices.remove(u);
                _vertices.remove(v);
            }
        }

        rootLabeling(_vertices.get(0));
    }

    public static void setNeighbours(GemVertex u, GemVertex v, GemColor color) {
        v.setNeighbour(u,color);
        u.setNeighbour(v,color);
    }

    public static void setNeighbours(GemVertex u, GemVertex v, GemColor ... color) {
        for (GemColor c: color) {
            v.setNeighbour(u, c);
            u.setNeighbour(v, c);
        }
    }

    public void rootLabeling(GemVertex root) {
        for (GemVertex v: _vertices)
            v.setFlag(false);
        _k = 1;
        this.process(root);
        Collections.sort(_vertices);
    }

    static int _k;
    public void process(GemVertex v) {
        int myLabel = _k++;
        v.setFlag(true);
        v.setLabel(myLabel);
        if (myLabel % 2 == 1) {
            if (!v.getYellow().getFlag()) {
                process(v.getYellow());
            }
        }
        else {
            if (!v.getBlue().getFlag()) {
                process(v.getBlue());
            }
            if (!v.getRed().getFlag()) {
                process(v.getRed());
            }
            if (!v.getGreen().getFlag()) {
                process(v.getGreen());
            }
        }
    }

    public int getAgemality() {
        return this.getNumberOfResidues(2)-
        this.getNumberOfResidues(3)-
        this.getNumberOfResidues(0);
    }

    public HomologyGroup homologyGroup() {
        return this.copy().homologyGroupHardwork();
    }

    public static void testGemHG() {
        GBlink b = new GBlink(new int[][] {{1,6,4,3},{1,2,7},{2,3,4,5},{5,6,7}}, new int[] {1,5,7});
        Gem g = b.getGem();

        // desenhar o mapa
        JFrame f = new JFrame("Operations");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(new Dimension(1023,768));
        g.copyCurrentLabellingToOriginalLabelling();
        JTabbedPane tp = new JTabbedPane();
        tp.add("Map",new PanelMapViewer(b));
        tp.add("Gem",new PanelGemViewer(g));
        f.setContentPane(tp);
        f.setVisible(true);
        // desenhar o mapa

        /*
        HomologyGroup hgBlink = b.homologyGroupFromBlink();
        HomologyGroup hgGem = b.homologyGroupFromGem();



        System.out.println("Blink: "+hgBlink.toString());
        System.out.println("Gem:   "+hgGem.toString()); */
    }

    private HomologyGroup homologyGroupHardwork() {
        this.cancelAllDipoles();

        // desenhar o mapa
        /*
        this.saveCurrentLabelsAsOriginalLabelOnVertices();
        JFrame f = new JFrame("Operations");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(new Dimension(1023,768));
        f.setContentPane(new PanelGemViewer(this));
        f.setVisible(true);*/
        // desenhar o mapa

        ArrayList<Residue> r01 = this.findResidues(GemColor.yellow,GemColor.blue);
        ArrayList<Residue> r23 = this.findResidues(GemColor.red,GemColor.green);

        /*
        { // log
            boolean first = true;
            for (Residue r : r01) {
                if (first) r.writeHeader();
                r.write();
                first = false;
            }
            first = true;
            for (Residue r : r23) {
                if (first) r.writeHeader();
                r.write();
                first = false;
            }
        } // log */

        int n = r01.size();
        int m = r23.size();
        int[][] A = new int[m-1][n-1]; // jogar fora uma linha e uma coluna

        int i = 0;
        for (Residue r: r23) {
            for (GemVertex v: r.getVertices()) {
                int j = 0;

                // procurar em que residuo o rotulo aparece
                for (Residue s: r01) {
                    if (s.contains(v))
                        break;
                    j++;
                }

                // problema
                if (j >= r01.size())
                    throw new RuntimeException("OOOooooopppppsssss");

                if (i < m - 1 && j < n - 1)
                    A[i][j] += ((v.getLabel() % 2) == 0 ? -1 : 1);

            }
            i++;
        }

        MatrixBI B = new MatrixBI(A);
        // System.out.println("Antes de calcular Smith");
        // B.print(System.out);
        Smith S = new Smith(B);
        S.smith();
        // System.out.println("Depois de calcular Smith");
        // S.getSmithNormalForm().print(System.out);

        MatrixBI R = S.getSmithNormalForm();

        HomologyGroup hg = new HomologyGroup();
        for (int k=0;k<R.getNumRows();k++) {
            BigInteger d = R.get(k, k);
            if (d.compareTo(BigInteger.ONE) != 0)
                hg.add(d);
        }

        // add 0 "handle number" of times
        for (int k=0;k<this.getHandleNumber();k++)
            hg.add(BigInteger.ZERO);

        return hg;
    }


    // ------------------------------------------------------------------------
    // -- Tag components...
    private boolean _tagged = false;
    private ComponentRepository _components;

    /**
     * The hypothesis is that you have a
     * labelling that satisfies the parity
     * constraint, i.e. every even label
     * vertex is connected to odd label vertex.
     */
    public void tag() {
        if (_tagged)
            return;

        _components = new ComponentRepository();

        // create a stack for the processing
        Stack<GemVertex> S = new Stack<GemVertex>();

        int freeLabel = 1;
        for (int colorSet: GemColor.COLOR_SET_WITH_1_OR_2_OR_3_COLORS) {

            GemColor colors[] = GemColor.COLOR_SET[colorSet];

            // unmark all vertices
            for (GemVertex v : _vertices)
                v.setFlag(false);

            for (GemVertex v : _vertices) {

                if (v.getFlag() == true)
                    continue;

                int currentComponentSize = 0;

                S.clear(); // clear stack

                Component component = new Component(this,freeLabel,v,colors);

                // ArrayList<GemVertex> list = new ArrayList<GemVertex>();

                v.setFlag(true);
                S.push(v);
                while (!S.isEmpty()) {
                    GemVertex u = S.pop();

                    // list.add(u);

                    u.setComponent(colorSet,component);
                    currentComponentSize++;

                    for (GemColor c: colors) {
                        GemVertex w = u.getNeighbour(c);
                        if (!w.getFlag()) {
                            w.setFlag(true);
                            S.push(w);
                        }
                    }
                }

                component.setSize(currentComponentSize);
                _components.add(component);

                // System.out.println("component: "+freeLabel+" on colors "+GemColor.getColorSetCompactString(colorSet));
                // for (GemVertex vvv: list)
                //    System.out.print(vvv.getLabel()+" ");
                // System.out.println();

                freeLabel++;
            }
        }

        _tagged = true;
    }

    /**
     * Monopole is indexed by a vertex v and color c in {1,2,3} such that
     * the incidents "0c-bigon" and "complement(0c)-bigon" have only the
     * vertex "v" in common.
     */
    public ArrayList<Monopole> findMonopoles() {

        this.tag();

        ArrayList<Monopole> result = new ArrayList<Monopole>();

        for (GemColor c: new GemColor[] {GemColor.blue,GemColor.red,GemColor.green}) {

            int colorSetBigon1 = GemColor.getColorSet(GemColor.yellow,c);
            int colorSetBigon2 = GemColor.difference(GemColor.COLORSET_ALL_COLORS,colorSetBigon1);

            for (GemVertex v: _vertices) {

                Component b1 = v.getComponent(colorSetBigon1);
                Component b2 = v.getComponent(colorSetBigon2);

                boolean monopole = true;
                for (GemVertex u: b1.getVerticesFromBigon()) {

                    if (u == v)
                        continue;

                    if (u.getComponent(colorSetBigon2) == b2) {
                        monopole = false;
                        break;
                    }
                }

                if (monopole) {
                    result.add(new Monopole(v,c,b1.size(),b2.size()));
                }

            }
        }
        return result;
    }

    /**
     * Find all dipoles of the Gem. First tags then does the rest...
     */
    public ArrayList<Dipole> findOneTwoOrThressDipoles() {

        this.tag();

        ArrayList<Dipole> result = new ArrayList<Dipole>();

        for (GemVertex v: _vertices) {

            if (v.hasEvenLabel()) // only consider odd labels
                continue;

            GemVertex v0 = v.getYellow();
            GemVertex v1 = v.getBlue();
            GemVertex v2 = v.getRed();
            GemVertex v3 = v.getGreen();

            // check 1-dipole on (v,v0)
            if (v.getComponent(GemColor.COLORSET_NO_YELLOW) !=  v0.getComponent(GemColor.COLORSET_NO_YELLOW))
                result.add(new Dipole(v,GemColor.yellow));

            // check 1-dipole on (v,v1)
            if (v.getComponent(GemColor.COLORSET_NO_BLUE) !=  v1.getComponent(GemColor.COLORSET_NO_BLUE))
                result.add(new Dipole(v,GemColor.blue));

            // check 1-dipole on (v,v2)
            if (v.getComponent(GemColor.COLORSET_NO_RED) !=  v2.getComponent(GemColor.COLORSET_NO_RED))
                result.add(new Dipole(v,GemColor.red));

            // check 1-dipole on (v,v3)
            if (v.getComponent(GemColor.COLORSET_NO_GREEN) !=  v3.getComponent(GemColor.COLORSET_NO_GREEN))
                result.add(new Dipole(v,GemColor.green));

            // check 2-dipole on (v,v0=v1)
            if (v0 == v1 &&
                v.getComponent(GemColor.COLORSET_NO_YELLOW + GemColor.COLORSET_NO_BLUE) !=
                v0.getComponent(GemColor.COLORSET_NO_YELLOW + GemColor.COLORSET_NO_BLUE))
                result.add(new Dipole(v,GemColor.yellow,GemColor.blue));

            // check 2-dipole on (v,v0=v2)
            if (v0 == v2 &&
                v.getComponent(GemColor.COLORSET_NO_YELLOW + GemColor.COLORSET_NO_RED) !=
                v0.getComponent(GemColor.COLORSET_NO_YELLOW + GemColor.COLORSET_NO_RED))
                result.add(new Dipole(v,GemColor.yellow,GemColor.red));

            // check 2-dipole on (v,v0=v3)
            if (v0 == v3 &&
                v.getComponent(GemColor.COLORSET_NO_YELLOW + GemColor.COLORSET_NO_GREEN) !=
                v0.getComponent(GemColor.COLORSET_NO_YELLOW + GemColor.COLORSET_NO_GREEN))
                result.add(new Dipole(v,GemColor.yellow,GemColor.green));

            // check 2-dipole on (v,v1=v2)
            if (v1 == v2 &&
                v.getComponent(GemColor.COLORSET_NO_BLUE + GemColor.COLORSET_NO_RED) !=
                v1.getComponent(GemColor.COLORSET_NO_BLUE + GemColor.COLORSET_NO_RED))
                result.add(new Dipole(v,GemColor.blue,GemColor.red));

            // check 2-dipole on (v,v1=v2)
            if (v1 == v3 &&
                v.getComponent(GemColor.COLORSET_NO_BLUE + GemColor.COLORSET_NO_GREEN) !=
                v1.getComponent(GemColor.COLORSET_NO_BLUE + GemColor.COLORSET_NO_GREEN))
                result.add(new Dipole(v,GemColor.blue,GemColor.green));

            // check 2-dipole on (v,v1=v2)
            if (v2 == v3 &&
                v.getComponent(GemColor.COLORSET_NO_RED + GemColor.COLORSET_NO_GREEN) !=
                v2.getComponent(GemColor.COLORSET_NO_RED + GemColor.COLORSET_NO_GREEN))
                result.add(new Dipole(v,GemColor.red,GemColor.green));

            // check 3-dipole on (v,v0=v1=v2)
            if (v0 == v1 && v0 == v2 && v3 != v0)
                result.add(new Dipole(v,GemColor.yellow,GemColor.blue,GemColor.red));

            // check 3-dipole on (v,v0=v1=v3)
            if (v0 == v1 && v0 == v3 && v0 != v2)
                result.add(new Dipole(v,GemColor.yellow,GemColor.blue,GemColor.green));

            // check 3-dipole on (v,v0=v2=v3)
            if (v0 == v2 && v0 == v3 && v0 != v1)
                result.add(new Dipole(v,GemColor.yellow,GemColor.red,GemColor.green));

            // check 3-dipole on (v,v1=v2=v3)
            if (v1 == v2 && v1 == v3 && v1 != v0)
                result.add(new Dipole(v,GemColor.blue,GemColor.red,GemColor.green));
        }
        return result;
    }

    /**
     * This is working only when there is a
     * valid bipartite labelling.
     * @return ArrayList
     */
    public ArrayList<Twistor> findAllTwistors() {
        return findTwistors(new GemColor[] {GemColor.blue,GemColor.red,GemColor.green});
    }

    /**
     * Given some subset C of  {1,2,3}, find
     * all twistors on colors 0c for c in C.
     */
    public ArrayList<Twistor> findTwistors(GemColor ... colors) {
        this.tag();

        GemColor c0 = GemColor.yellow;

        ArrayList<Twistor> result = new ArrayList<Twistor>();
        for (GemColor c1: colors) {

            GemColor[] complementColors = GemColor.getComplementColors(GemColor.yellow,c1);
            GemColor c2 = complementColors[0];
            GemColor c3 = complementColors[1];

            // these must be the same
            int colorSet1 = GemColor.getColorSet(c0, c1);
            int colorSet2 = GemColor.getColorSet(c2, c3);
            // these must be different
            int colorSet3 = GemColor.getColorSet(c0,c2);
            int colorSet4 = GemColor.getColorSet(c0,c3);
            int colorSet5 = GemColor.getColorSet(c1,c2);
            int colorSet6 = GemColor.getColorSet(c1,c3);

            for (int i=0;i<_vertices.size();i++) {
                GemVertex vi = _vertices.get(i);
                for (int j=i+1;j<_vertices.size();j++) {
                    GemVertex vj = _vertices.get(j);

                    if (vi.getLabel() % 2 != vj.getLabel() % 2)
                        continue;

                    if ((vi.getComponent(colorSet1) == vj.getComponent(colorSet1)) &&
                        (vi.getComponent(colorSet2) == vj.getComponent(colorSet2)) &&
                        (vi.getComponent(colorSet3) != vj.getComponent(colorSet3)) &&
                        (vi.getComponent(colorSet4) != vj.getComponent(colorSet4)) &&
                        (vi.getComponent(colorSet5) != vj.getComponent(colorSet5)) &&
                        (vi.getComponent(colorSet6) != vj.getComponent(colorSet6))) {

                        result.add(new Twistor(vi,vj,c1));

                    }

                }
            }
        }

        return result;
    }

    /**
     * This is working only when there is a
     * valid bipartite labelling.
     * @return ArrayList
     */
    public ArrayList<Quartet> findAllNonTrivialQuartets() {
        // tag all vertices with their bigons...
        this.tag();

        // Quartets
        ArrayList<Quartet> R = new ArrayList<Quartet>();

        // Vertices
        ArrayList<GemVertex> V = this.getOddVertices();


        GemVertex v[] = {null, null, null, null};
        GemVertex n[] = {null, null, null, null};
        int x[] = {-1, -1, -1, -1};
        int i=0;
        while (i >= 0) {
            // next candidate edge for positio "i"
            x[i]++;

            // no more candidates for this position
            if (x[i] == V.size()) {
                x[i] = -1;
                i--;
                continue;
            }

            // get vertice
            v[i] = V.get(x[i]);
            n[i] = v[i].getNeighbour(i);

            // verify if edge of color "i" satisfies the
            // 2 by 2 same bigon condition
            boolean satisfies = true;
            for (int j=0;j<i;j++) {
                int colorsij = GemColor.getColorSet(GemColor.getByNumber(i),GemColor.getByNumber(j));
                Component bi = v[i].getComponent(colorsij);
                Component bj = v[j].getComponent(colorsij);
                if (bi != bj) {
                    satisfies = false;
                    break;
                }
            }

            // do not satisfy
            if (!satisfies)
                continue;

            // found a quartet. is it proper?
            if (i == 3) {
                if (v[0] == v[1] && v[1] == v[2] && v[2] == v[3] || n[0] == n[1] && n[1] == n[2] && n[2] == n[3])
                    continue;
                else
                    R.add(new Quartet(v[0],v[1],v[2],v[3]));
            }
            else i++;
        }
        return R;
    }

    // Says if the removal of the edges of the quartet
    // disconnects the graph
    public ArrayList<HashSet<GemVertex>> connectedComponentsAfterQuartetRemoval(Quartet q) {
        // unmark all vertices
        for (GemVertex v: _vertices)
            v.setFlag(false);

        ArrayList<HashSet<GemVertex>> partition = new ArrayList<HashSet<GemVertex>>();
        for (GemVertex v: _vertices) {
            if (v.getFlag() == true)
                continue;
            HashSet<GemVertex> part = new HashSet<GemVertex>();
            Stack<GemVertex> S = new Stack<GemVertex>();
            S.push(v);
            v.setFlag(true);
            while (!S.isEmpty()) {
                GemVertex u = S.pop();
                part.add(u);
                GemVertex us[] = {u.getNeighbour(0), u.getNeighbour(1), u.getNeighbour(2), u.getNeighbour(3)};
                for (int i=0;i<4;i++) {
                    if (us[i].getFlag() == false && u != q.getV(i) && us[i] != q.getV(i)) {
                        us[i].setFlag(true);
                        S.push(us[i]);
                    }
                }
            }
            partition.add(part);
        }

        return partition;
    }


    public Gem[] breakGemOnQuartet(Quartet q, HashSet<GemVertex> P1, HashSet<GemVertex> P2) {
        HashMap<GemVertex,GemVertex> mapOriginalToNew = new HashMap<GemVertex,GemVertex>();
        HashMap<GemVertex,GemVertex> mapNewToOriginal = new HashMap<GemVertex,GemVertex>();
        for (GemVertex originalVertex: this.getVertices()) {
            GemVertex newVertex = new GemVertex(originalVertex.getLabel());
            mapOriginalToNew.put(originalVertex,newVertex);
            mapNewToOriginal.put(newVertex,originalVertex);
        }

        GemVertex newV1 = new GemVertex(this.getNumVertices()+1);
        GemVertex newV2 = new GemVertex(this.getNumVertices()+2);

        for (GemVertex originalVertex: this.getOddVertices()) {
            GemVertex newVertex = mapOriginalToNew.get(originalVertex);
            for (int i=0;i<4;i++) {
                GemVertex originalVertexNeighbour_i = originalVertex.getNeighbour(i);
                GemVertex newVertexNeighbour_i = mapOriginalToNew.get(originalVertexNeighbour_i);
                // is it one of the new edges
                if (originalVertex == q.getV(i) || originalVertexNeighbour_i == q.getV(i)) {
                    if (P1.contains(originalVertex)) {
                        Gem.setNeighbours(newVertex,newV1,GemColor.getByNumber(i));
                        Gem.setNeighbours(newVertexNeighbour_i,newV2,GemColor.getByNumber(i));
                    }
                    else {
                        Gem.setNeighbours(newVertex,newV2,GemColor.getByNumber(i));
                        Gem.setNeighbours(newVertexNeighbour_i,newV1,GemColor.getByNumber(i));
                    }
                }
                // no, can connect normally
                else {
                    Gem.setNeighbours(newVertex,newVertexNeighbour_i,GemColor.getByNumber(i));
                }
            }
        }

        ArrayList<GemVertex> L1 = new ArrayList<GemVertex>();
        L1.add(newV1);
        for (GemVertex originalVertex: P1) {
            L1.add(mapOriginalToNew.get(originalVertex));
        }
        Gem G1 = new Gem(L1);
        G1.goToCodeLabel();


        ArrayList<GemVertex> L2 = new ArrayList<GemVertex>();
        L2.add(newV2);
        for (GemVertex originalVertex: P2) {
            L2.add(mapOriginalToNew.get(originalVertex));
        }
        Gem G2 = new Gem(L2);
        G2.goToCodeLabel();

        return new Gem[] {G1, G2};
    }




    public ArrayList<MinusCylinder> findAllMinusCylinder() {
        ArrayList<MinusCylinder> result = new ArrayList<MinusCylinder>();
        GemColor cs[] = { GemColor.blue, GemColor.red, GemColor.green };
        GemColor c0 = GemColor.yellow;
        for (GemColor c1: cs) {
            for (GemColor c2: cs) {

                if (c1.getNumber() >= c2.getNumber()) continue;

                GemColor c3 = GemColor.getComplementColors(c0,c1,c2)[0];

                for (GemVertex u: _vertices) {
                    for (GemVertex v: _vertices) {
                        if ((u.getLabel() % 2 == 0) ||
                            (u.getLabel() % 2 == v.getLabel() % 2))
                            continue;

                        GemVertex u0 = u.getNeighbour(c0);
                        GemVertex u1 = u.getNeighbour(c1);
                        GemVertex u2 = u.getNeighbour(c2);
                        GemVertex u3 = u.getNeighbour(c3);

                        GemVertex v0 = v.getNeighbour(c0);
                        GemVertex v1 = v.getNeighbour(c1);
                        GemVertex v2 = v.getNeighbour(c2);
                        GemVertex v3 = v.getNeighbour(c3);

                        int bigon12 = GemColor.getColorSet(c1,c2);
                        int bigon13 = GemColor.getColorSet(c1,c3);
                        int bigon02 = GemColor.getColorSet(c0,c2);
                        int bigon03 = GemColor.getColorSet(c0,c3);

                        if (u1.getComponent(bigon12) == v2.getComponent(bigon12) &&
                            v3.getComponent(bigon03) == u0.getComponent(bigon03) &&
                            u0.getComponent(bigon02) != v2.getComponent(bigon02) &&
                            u1.getComponent(bigon13) != v3.getComponent(bigon13)) {
                            result.add(new MinusCylinder(u,v,c1,c2));
                        }
                    }
                }
            }
        }
        return result;

    }



    /**
     * This is working only when there is a
     * valid bipartite labelling.
     * @return ArrayList
     */
    public ArrayList<QuadColor> findAllQuadColors() {
        this.tag();

        GemColor[][] ps = {
                         {GemColor.blue,GemColor.red,GemColor.green},
                         {GemColor.blue,GemColor.green,GemColor.red},
                         {GemColor.red,GemColor.blue,GemColor.green},
                         {GemColor.red,GemColor.green,GemColor.blue},
                         {GemColor.green,GemColor.blue,GemColor.red},
                         {GemColor.green,GemColor.red,GemColor.blue}
        };

        ArrayList<QuadColor> list = new ArrayList<QuadColor>();

        for (GemVertex v0: _vertices) {
            if (v0.getLabel() % 2 == 0)
                continue;
            for (GemColor p[] : ps) {
                GemVertex v1 = v0.getNeighbour(p[0]);
                GemVertex v2 = v1.getNeighbour(p[1]);
                GemVertex v3 = v2.getNeighbour(p[2]);
                GemVertex v4 = v3.getNeighbour(GemColor.yellow);
                if (v0 != v4)
                    continue;
                if (v0 != v1 && v0 != v2 && v0 != v3 &&
                    v1 != v2 && v1 != v3  &&
                    v2 != v3) {
                    list.add(new QuadColor(v0,p));
                }
            }
        }
        return list;
    }

    /**
     * This is working only when there is a
     * valid bipartite labelling.
     * @return ArrayList
     */
    public ArrayList<FourCluster> findAllFourCluster() {
        this.tag();

        GemColor c0 = GemColor.yellow;
        GemColor c1 = GemColor.blue;
        GemColor c2 = GemColor.red;
        GemColor c3 = GemColor.green;

        int[] colorSets = new int[] {
                          GemColor.getColorSet(c0, c1),
                          GemColor.getColorSet(c0, c2),
                          GemColor.getColorSet(c0, c3),
                          GemColor.getColorSet(c1, c2),
                          GemColor.getColorSet(c1, c3),
                          GemColor.getColorSet(c2, c3)};

        ArrayList<FourCluster> list = new ArrayList<FourCluster>();

        for (GemVertex v: _vertices) {
            int count = 0;
            for (int cs: colorSets) {
                if (v.getComponent(cs).size() == 4)
                    count++;
            }
            if (count >= 4) {
                list.add(new FourCluster(v));
            }
        }
        return list;
    }

    /**
     * This is working only when there is a
     * valid bipartite labelling.
     * @return ArrayList
     */
    public ArrayList<Antipole> findAllAntipoles() {
        this.tag();

        GemColor c0 = GemColor.yellow;

        ArrayList<Antipole> result = new ArrayList<Antipole>();
        for (GemColor c1: new GemColor[] {GemColor.blue,GemColor.red,GemColor.green}) {

            GemColor[] complementColors = GemColor.getComplementColors(GemColor.yellow,c1);
            GemColor c2 = complementColors[0];
            GemColor c3 = complementColors[1];

            // these must be the same
            int colorSet1 = GemColor.getColorSet(c0, c1);
            int colorSet2 = GemColor.getColorSet(c2, c3);
            // these must be different
            int colorSet3 = GemColor.getColorSet(c0,c2);
            int colorSet4 = GemColor.getColorSet(c0,c3);
            int colorSet5 = GemColor.getColorSet(c1,c2);
            int colorSet6 = GemColor.getColorSet(c1,c3);

            for (int i=0;i<_vertices.size();i++) {
                GemVertex vi = _vertices.get(i);
                for (int j=i+1;j<_vertices.size();j++) {
                    GemVertex vj = _vertices.get(j);

                    if (vi.getLabel() % 2 == vj.getLabel() % 2)
                        continue;

                    if ((vi.getComponent(colorSet1) == vj.getComponent(colorSet1)) &&
                        (vi.getComponent(colorSet2) == vj.getComponent(colorSet2)) &&
                        (vi.getComponent(colorSet3) != vj.getComponent(colorSet3)) &&
                        (vi.getComponent(colorSet4) != vj.getComponent(colorSet4)) &&
                        (vi.getComponent(colorSet5) != vj.getComponent(colorSet5)) &&
                        (vi.getComponent(colorSet6) != vj.getComponent(colorSet6))) {

                        result.add(new Antipole(vi,vj,c1));

                    }

                }
            }
        }
        return result;
    }

    /**
     * Tags the components of the graph with all possible combinations
     * then searches for the first dipole
     */
    public Dipole findAnyDipole() {

        this.tag();

        for (GemVertex v: _vertices) {

            GemVertex v0 = v.getYellow();
            GemVertex v1 = v.getBlue();
            GemVertex v2 = v.getRed();
            GemVertex v3 = v.getGreen();

            // check 1-dipole on (v,v0)
            if (v.getComponent(GemColor.COLORSET_NO_YELLOW) !=  v0.getComponent(GemColor.COLORSET_NO_YELLOW))
                return new Dipole(v,GemColor.yellow);

            // check 1-dipole on (v,v1)
            if (v.getComponent(GemColor.COLORSET_NO_BLUE) !=  v1.getComponent(GemColor.COLORSET_NO_BLUE))
                return new Dipole(v,GemColor.blue);

            // check 1-dipole on (v,v2)
            if (v.getComponent(GemColor.COLORSET_NO_RED) !=  v2.getComponent(GemColor.COLORSET_NO_RED))
                return new Dipole(v,GemColor.red);

            // check 1-dipole on (v,v3)
            if (v.getComponent(GemColor.COLORSET_NO_GREEN) !=  v3.getComponent(GemColor.COLORSET_NO_GREEN))
                return new Dipole(v,GemColor.green);

            // check 2-dipole on (v,v0=v1)
            if (v0 == v1 &&
                v.getComponent(GemColor.COLORSET_NO_YELLOW + GemColor.COLORSET_NO_BLUE) !=
                v0.getComponent(GemColor.COLORSET_NO_YELLOW + GemColor.COLORSET_NO_BLUE))
                return new Dipole(v,GemColor.yellow,GemColor.blue);

            // check 2-dipole on (v,v0=v2)
            if (v0 == v2 &&
                v.getComponent(GemColor.COLORSET_NO_YELLOW + GemColor.COLORSET_NO_RED) !=
                v0.getComponent(GemColor.COLORSET_NO_YELLOW + GemColor.COLORSET_NO_RED))
                return new Dipole(v,GemColor.yellow,GemColor.red);

            // check 2-dipole on (v,v0=v3)
            if (v0 == v3 &&
                v.getComponent(GemColor.COLORSET_NO_YELLOW + GemColor.COLORSET_NO_GREEN) !=
                v0.getComponent(GemColor.COLORSET_NO_YELLOW + GemColor.COLORSET_NO_GREEN))
                return new Dipole(v,GemColor.yellow,GemColor.green);

            // check 2-dipole on (v,v1=v2)
            if (v1 == v2 &&
                v.getComponent(GemColor.COLORSET_NO_BLUE + GemColor.COLORSET_NO_RED) !=
                v1.getComponent(GemColor.COLORSET_NO_BLUE + GemColor.COLORSET_NO_RED))
                return new Dipole(v,GemColor.blue,GemColor.red);

            // check 2-dipole on (v,v1=v2)
            if (v1 == v3 &&
                v.getComponent(GemColor.COLORSET_NO_BLUE + GemColor.COLORSET_NO_GREEN) !=
                v1.getComponent(GemColor.COLORSET_NO_BLUE + GemColor.COLORSET_NO_GREEN))
                return new Dipole(v,GemColor.blue,GemColor.green);

            // check 2-dipole on (v,v1=v2)
            if (v2 == v3 &&
                v.getComponent(GemColor.COLORSET_NO_RED + GemColor.COLORSET_NO_GREEN) !=
                v2.getComponent(GemColor.COLORSET_NO_RED + GemColor.COLORSET_NO_GREEN))
                return new Dipole(v,GemColor.red,GemColor.green);

            // check 3-dipole on (v,v0=v1=v2)
            if (v0 == v1 && v0 == v2 && v3 != v0)
                return new Dipole(v,GemColor.yellow,GemColor.blue,GemColor.red);

            // check 3-dipole on (v,v0=v1=v3)
            if (v0 == v1 && v0 == v3 && v0 != v2)
                return new Dipole(v,GemColor.yellow,GemColor.blue,GemColor.green);

            // check 3-dipole on (v,v0=v2=v3)
            if (v0 == v2 && v0 == v3 && v0 != v1)
                return new Dipole(v,GemColor.yellow,GemColor.red,GemColor.green);

            // check 3-dipole on (v,v1=v2=v3)
            if (v1 == v2 && v1 == v3 && v1 != v0)
                return new Dipole(v,GemColor.blue,GemColor.red,GemColor.green);
        }
        return null;
    }

    /**
     * Tags the components of the graph with all possible combinations
     * then searches for the first dipole
     */
    public Dipole findAnyTwoDipoleOnColors(GemColor c1, GemColor c2) {

        this.tag();

        for (GemVertex v: _vertices) {
            GemVertex vc1 = v.getNeighbour(c1);
            GemVertex vc2 = v.getNeighbour(c2);
            int cc = GemColor.getComplementColorSet(c1,c2);

            // check 2-dipole on (v,v0=v1)
            if (vc1 == vc2 &&
                v.getComponent(cc) != vc1.getComponent(cc))
                return new Dipole(v,c1,c2);
        }

        return null;

    }



    /**
     * Find rho 2 pair
     */
    public RhoPair findAnyRho2PairOld() {
        ArrayList<Component> bs;

        HashMap<Component,ArrayList<GemVertex>> mapComp2Vertices = new HashMap<Component,ArrayList<GemVertex>>();

        // try with yellow
        for (GemColor c: GemColor.values()) {
            bs = this.getComponentRepository().getBigons(c);
            for (int i = 0; i < bs.size(); i++) {
                Component ci = bs.get(i);
                ArrayList<GemVertex> vi = mapComp2Vertices.get(ci);
                if (vi == null) {
                    vi = ci.getVerticesFromBigon();
                    mapComp2Vertices.put(ci,vi);
                }

                // get color set of bigon i
                int colorSetI = ci.getColorSet();

                for (int j = i + 1; j < bs.size(); j++) {

                    Component cj = bs.get(j);
                    ArrayList<GemVertex> vj = mapComp2Vertices.get(cj);
                    if (vj == null) {
                        vj = cj.getVerticesFromBigon();
                        mapComp2Vertices.put(cj,vj);
                    }

                    // get color set of bigon j
                    int colorSetJ = cj.getColorSet();

                    // cannot be a rho pair (no intersection)!!!
                    if (colorSetI == colorSetJ)
                        continue;

                    // common vertices
                    ArrayList<GemVertex> common = new ArrayList<GemVertex>();
                    for (GemVertex x : vj) {
                        if (!vi.contains(x))
                            continue;

                        common.add(x);
                    }

                    // robust rho 2 pair
                    int missingColorSet = GemColor.difference(
                            GemColor.COLORSET_ALL_COLORS,
                            GemColor.union(colorSetI, colorSetJ));
                    int otherBigonColors = GemColor.union(
                            missingColorSet,
                            GemColor.getColorSet(c));

                    if (common.size() > 1) {

                        RhoPair result = null;

                        Collections.sort(common);

                        for (int ii=0;ii<common.size() && result == null;ii++) {
                            GemVertex vii = common.get(ii);
                            for (int jj=ii+1;jj<common.size() && result == null;jj++) {
                                GemVertex vjj = common.get(jj);
                                if (vii.getComponent(otherBigonColors) ==
                                    vjj.getComponent(otherBigonColors)) {
                                    continue;
                                }

                                if (vii.getNeighbour(c) != vjj) {
                                    result = new RhoPair(vii, vjj, c, 2);
                                }

                            }
                        }

                        if (result != null)
                            return result;

                    }

                    /*
                    // found rho "at least" 2 pair?
                    if (common.size() > 1) {
                        Collections.sort(common);
                        RhoPair result = new RhoPair(common.get(0), common.get(1), c, 2);


                        // System.out.println("Found: "+result.toString());
                        // System.out.print("bigon 1: ");
                        // for (GemVertex xx: vi)
                        //    System.out.print(xx.getLabel()+" ");
                        // System.out.print("\nbigon 2: ");
                        // for (GemVertex xx: vj)
                        //    System.out.print(xx.getLabel()+" ");
                        // System.out.println();

                        return result;
                    } */






                }
            }
        }
        return null;
    }

    /**
     * Isn't this code correct and simpler and more efficient?
     * @return RhoPair
     */
    public RhoPair findAnyRho3Pair() {
        for (int i=0;i<_vertices.size();i++) {
            GemVertex vi = _vertices.get(i);
            if (vi.hasEvenLabel()) continue;
            for (int j=i+1;j<_vertices.size();j++) {
                GemVertex vj = _vertices.get(j);
                if (vj.hasEvenLabel()) continue;

                int c01 = GemColor.COLORSET_YELLOW & GemColor.COLORSET_BLUE;
                int c02 = GemColor.COLORSET_YELLOW & GemColor.COLORSET_RED;
                int c03 = GemColor.COLORSET_YELLOW & GemColor.COLORSET_GREEN;
                int sum0 = (vi.getComponent(c01) == vj.getComponent(c01) ? 1 : 0) +
                      (vi.getComponent(c02) == vj.getComponent(c02) ? 1 : 0) +
                      (vi.getComponent(c03) == vj.getComponent(c03) ? 1 : 0);
                if (sum0 == 3)
                    return new RhoPair(vi, vj, GemColor.yellow, 3);

                int c10 = GemColor.COLORSET_BLUE & GemColor.COLORSET_YELLOW;
                int c12 = GemColor.COLORSET_BLUE & GemColor.COLORSET_RED;
                int c13 = GemColor.COLORSET_BLUE & GemColor.COLORSET_GREEN;
                int sum1 = (vi.getComponent(c10) == vj.getComponent(c10) ? 1 : 0) +
                      (vi.getComponent(c12) == vj.getComponent(c12) ? 1 : 0) +
                      (vi.getComponent(c13) == vj.getComponent(c13) ? 1 : 0);
                if (sum1 == 3)
                    return new RhoPair(vi, vj, GemColor.blue, 3);

                int c20 = GemColor.COLORSET_RED & GemColor.COLORSET_YELLOW;
                int c21 = GemColor.COLORSET_RED & GemColor.COLORSET_BLUE;
                int c23 = GemColor.COLORSET_RED & GemColor.COLORSET_GREEN;
                int sum2 = (vi.getComponent(c20) == vj.getComponent(c20) ? 1 : 0) +
                      (vi.getComponent(c21) == vj.getComponent(c21) ? 1 : 0) +
                      (vi.getComponent(c23) == vj.getComponent(c23) ? 1 : 0);
                if (sum2 == 3)
                    return new RhoPair(vi, vj, GemColor.red, 3);

                int c30 = GemColor.COLORSET_GREEN & GemColor.COLORSET_YELLOW;
                int c31 = GemColor.COLORSET_GREEN & GemColor.COLORSET_BLUE;
                int c32 = GemColor.COLORSET_GREEN & GemColor.COLORSET_RED;
                int sum3 = (vi.getComponent(c30) == vj.getComponent(c30) ? 1 : 0) +
                      (vi.getComponent(c31) == vj.getComponent(c31) ? 1 : 0) +
                      (vi.getComponent(c32) == vj.getComponent(c32) ? 1 : 0);
                if (sum3 == 3)
                    return new RhoPair(vi, vj, GemColor.green, 3);
            }
        }
        return null;
    }


    /**
     * Isn't this code correct and simpler and more efficient?
     * @return RhoPair
     */
    public RhoPair findAnyRho2Pair() {
        for (int i=0;i<_vertices.size();i++) {
            GemVertex vi = _vertices.get(i);
            if (vi.hasEvenLabel()) continue;
            for (int j=i+1;j<_vertices.size();j++) {
                GemVertex vj = _vertices.get(j);
                if (vj.hasEvenLabel()) continue;

                int c01 = GemColor.COLORSET_YELLOW & GemColor.COLORSET_BLUE;
                int c02 = GemColor.COLORSET_YELLOW & GemColor.COLORSET_RED;
                int c03 = GemColor.COLORSET_YELLOW & GemColor.COLORSET_GREEN;
                int sum0 = (vi.getComponent(c01) == vj.getComponent(c01) ? 1 : 0) +
                      (vi.getComponent(c02) == vj.getComponent(c02) ? 1 : 0) +
                      (vi.getComponent(c03) == vj.getComponent(c03) ? 1 : 0);
                if (sum0 == 2)
                    return new RhoPair(vi, vj, GemColor.yellow, 2);

                int c10 = GemColor.COLORSET_BLUE & GemColor.COLORSET_YELLOW;
                int c12 = GemColor.COLORSET_BLUE & GemColor.COLORSET_RED;
                int c13 = GemColor.COLORSET_BLUE & GemColor.COLORSET_GREEN;
                int sum1 = (vi.getComponent(c10) == vj.getComponent(c10) ? 1 : 0) +
                      (vi.getComponent(c12) == vj.getComponent(c12) ? 1 : 0) +
                      (vi.getComponent(c13) == vj.getComponent(c13) ? 1 : 0);
                if (sum1 == 2)
                    return new RhoPair(vi, vj, GemColor.blue, 2);

                int c20 = GemColor.COLORSET_RED & GemColor.COLORSET_YELLOW;
                int c21 = GemColor.COLORSET_RED & GemColor.COLORSET_BLUE;
                int c23 = GemColor.COLORSET_RED & GemColor.COLORSET_GREEN;
                int sum2 = (vi.getComponent(c20) == vj.getComponent(c20) ? 1 : 0) +
                      (vi.getComponent(c21) == vj.getComponent(c21) ? 1 : 0) +
                      (vi.getComponent(c23) == vj.getComponent(c23) ? 1 : 0);
                if (sum2 == 2)
                    return new RhoPair(vi, vj, GemColor.red, 2);

                int c30 = GemColor.COLORSET_GREEN & GemColor.COLORSET_YELLOW;
                int c31 = GemColor.COLORSET_GREEN & GemColor.COLORSET_BLUE;
                int c32 = GemColor.COLORSET_GREEN & GemColor.COLORSET_RED;
                int sum3 = (vi.getComponent(c30) == vj.getComponent(c30) ? 1 : 0) +
                      (vi.getComponent(c31) == vj.getComponent(c31) ? 1 : 0) +
                      (vi.getComponent(c32) == vj.getComponent(c32) ? 1 : 0);
                if (sum3 == 2)
                    return new RhoPair(vi, vj, GemColor.green, 2);
            }
        }
        return null;
    }


    /**
     * Find rho 3 pair
     */
    public RhoPair findAnyRho3PairOld() {
        ArrayList<Component> bs;

        HashMap<Component,ArrayList<GemVertex>> mapComp2Vertices = new HashMap<Component,ArrayList<GemVertex>>();

        // try with yellow
        for (GemColor c: GemColor.values()) {
            bs = this.getComponentRepository().getBigons(c);
            for (int i = 0; i < bs.size(); i++) {

                Component ci = bs.get(i);
                ArrayList<GemVertex> vi = mapComp2Vertices.get(ci);
                if (vi == null) {
                    vi = ci.getVerticesFromBigon();
                    mapComp2Vertices.put(ci,vi);
                }

                // color set of bigon i
                int colorSetI = ci.getColorSet();

                for (int j = i + 1; j < bs.size(); j++) {

                    Component cj = bs.get(j);
                    ArrayList<GemVertex> vj = mapComp2Vertices.get(cj);
                    if (vj == null) {
                        vj = cj.getVerticesFromBigon();
                        mapComp2Vertices.put(cj,vj);
                    }

                    // color set of bigon j
                    int colorSetJ = cj.getColorSet();

                    //
                    if (colorSetI == colorSetJ)
                        continue;

                    for (int k = j + 1; k < bs.size(); k++) {

                        Component ck = bs.get(k);
                        ArrayList<GemVertex> vk = mapComp2Vertices.get(ck);
                        if (vk == null) {
                            vk = ck.getVerticesFromBigon();
                            mapComp2Vertices.put(ck, vk);
                        }

                        // color set of bigon j
                        int colorSetK = ck.getColorSet();

                        // cannot be a rho pair
                        if (colorSetI == colorSetK || colorSetJ == colorSetK)
                            continue;

                        // common vertices
                        ArrayList<GemVertex> common = new ArrayList<GemVertex>();
                        for (GemVertex x : vk) {
                            if (!vi.contains(x) || !vj.contains(x))
                                continue;
                            common.add(x);
                        }

                        // test to see if found rho3pair
                        if (common.size() > 1) {
                            Collections.sort(common);

                            RhoPair result = null;
                            for (int ii=0;ii<common.size() && result==null;ii++) {
                                GemVertex uu = common.get(ii);
                                for (int jj=ii+1;jj<common.size() && result==null;jj++) {
                                    GemVertex vv = common.get(jj);
                                    // the common vertices
                                    // cannot be neighbours by color
                                    if (uu.getNeighbour(c) != vv) {
                                        result = new RhoPair(uu, vv, c, 3);
                                    }
                                }
                            }

                            if (result != null)
                                return result;



                            /*
                            System.out.println("Found: "+result.toString());
                            System.out.print("bigon 1: ");
                            for (GemVertex xx: vi)
                                System.out.print(xx.getLabel()+" ");
                            System.out.print("\nbigon 2: ");
                            for (GemVertex xx: vj)
                                System.out.print(xx.getLabel()+" ");
                            System.out.println();*/

                        }
                    }
                }
            }
        }
        return null;
    }

    /**
     * Destroys current labelling, but keeps the
     * parity of the labels and all the rest.
     */
    public GemPackedLabelling getGemCodeAfterDipoleThickenning(DipoleThickenning dt, boolean[] moreThan2blobs) {

        // System.out.println("Thickenning dipole: "+dt.toString());

        GemColor c = dt.getColor();
        GemVertex u = dt.getDipole().getU();
        GemVertex v = dt.getDipole().getV();
        GemVertex uu = u.getNeighbour(c);
        GemVertex vv = v.getNeighbour(c);

        Gem.setNeighbours(u,v,c);
        Gem.setNeighbours(uu,vv,c);

        // calculate labelling
        GemPackedLabelling lbl = this.goToCodeLabel();

        if (this.getComponentRepository().getNumberOfBlobs() >= 3) {
            moreThan2blobs[0] = true;
        }
        else moreThan2blobs[0] = false;

        Gem.setNeighbours(u,uu,c);
        Gem.setNeighbours(v,vv,c);

        // needs to tag again
        this.setGemAsModified();

        return lbl;
    }

    /**
     * Destroys current labelling, but keeps the
     * parity of the labels and all the rest.
     */
    public boolean checkIfDipoleNarrowingIsValid(DipoleNarrowing dn) {
        // System.out.println("Thickenning dipole: "+dt.toString());

        Dipole d = dn.getDipole();
        GemColor c = dn.getColor();
        GemVertex u = d.getU();
        GemVertex v = d.getV();
        GemVertex uu = dn.getUU();
        GemVertex vv = dn.getVV();

        Gem.setNeighbours(u,uu,c);
        Gem.setNeighbours(v,vv,c);

        int colorSet = GemColor.removeColor(d.getComplementColorSet(),c);
        GemColor colors[] = GemColor.getColorsOfColorSet(colorSet);

        boolean isDipole = !findVertex(u,colors,v);

        Gem.setNeighbours(u,v,c);
        Gem.setNeighbours(uu,vv,c);

        return isDipole;
    }

    /**
     * Destroys current labelling, but keeps the
     * parity of the labels and all the rest.
     */
    public GemPackedLabelling getGemCodeAfterDipoleNarrowing(DipoleNarrowing dn, boolean[] moreThan2blobs) {

        // System.out.println("Thickenning dipole: "+dt.toString());

        GemColor c = dn.getColor();
        GemVertex u = dn.getDipole().getU();
        GemVertex v = dn.getDipole().getV();
        GemVertex uu = dn.getUU();
        GemVertex vv = dn.getVV();

        Gem.setNeighbours(u,uu,c);
        Gem.setNeighbours(v,vv,c);

        // calculate labelling
        GemPackedLabelling lbl = this.goToCodeLabel();

        if (this.getComponentRepository().getNumberOfBlobs() >= 3) {
            moreThan2blobs[0] = true;
        }
        else moreThan2blobs[0] = false;


        Gem.setNeighbours(u,v,c);
        Gem.setNeighbours(uu,vv,c);

        return lbl;
    }

    public Object[] findDipoleThickenningAndDipoleNarrowingPoints() {

        ArrayList<Dipole> dipoles = this.findOneTwoOrThressDipoles();

        ArrayList<DipoleThickenning> dtList = new ArrayList<DipoleThickenning>();
        ArrayList<DipoleNarrowing>   dnList = new ArrayList<DipoleNarrowing>();

        // find the thickenning points (easier first)
        for (Dipole d : dipoles) {
            if (d.size() > 2)
                continue;
            int component = d.getComplementColorSet();
            GemColor cc[] = GemColor.getColorsOfColorSet(component);
            for (GemColor c : cc) {
                dtList.add(new DipoleThickenning(d, c));
            }
        }

        // find the narrowing points

        // prepare mapping of component colors of dipoles to the dipoles of those colors (not efficient)
        HashMap<GemColor,ArrayList<Dipole>>  mapColor2Dipoles = new HashMap<GemColor,ArrayList<Dipole>>();
        mapColor2Dipoles.put(GemColor.yellow, new ArrayList<Dipole>());
        mapColor2Dipoles.put(GemColor.blue, new ArrayList<Dipole>());
        mapColor2Dipoles.put(GemColor.red, new ArrayList<Dipole>());
        mapColor2Dipoles.put(GemColor.green, new ArrayList<Dipole>());

        // put 2 or 3 dipoles on the correct list
        for (Dipole d: dipoles) {

            if (d.size() <= 1)
                continue;

            int colorSet = d.getColorSet();
            for (GemColor c: GemColor.getColorsOfColorSet(colorSet)) {
                ArrayList<Dipole> list = mapColor2Dipoles.get(c);
                list.add(d);
            }
        }

        // for each edge
        GemColor[] colors = GemColor.values();
        for (GemVertex vv: _vertices) { // vv has odd label

            // do not consider even vertices
            if (vv.getLabel() % 2 == 0)
                continue;

            // gem color
            for (GemColor c: colors) {

                GemVertex uu = vv.getNeighbour(c); // uu has even label

                // System.out.println(String.format("Edge %d %d on color %s",vv.getLabel(),uu.getLabel(),""+c));


                ArrayList<Dipole> ds = mapColor2Dipoles.get(c);

                for (Dipole d: ds) {
                    GemVertex u = d.getU(); // u has odd label
                    GemVertex v = d.getV(); // u has even label

                    // System.out.println(String.format("Trying narrowing with %s",d.toString()));

                    // must be disjoint of the dipole
                    if (u == vv || uu == v)
                        continue;

                    // dipole complement color set
                    int dipoleComplementColorSet = d.getComplementColorSet();

                    // NECESSARY CONDITION 1:
                    // uu and vv must be in different "dipoleComplementColorSet"-components
                    if (uu.getComponent(dipoleComplementColorSet) == vv.getComponent(dipoleComplementColorSet))
                        continue;

                    // System.out.println(String.format("Passed test 1..."));

                    // NECESSARY CONDITION 2:
                    // u and uu must be on the same c,cc-bigon
                    // (where c is the edge color and cc is a complement color of the dipole d)
                    GemColor[] dipoleComplementColors = GemColor.getColorsOfColorSet(dipoleComplementColorSet);
                    boolean testCond2 = true;
                    for (GemColor cc: dipoleComplementColors) {
                        int c_cc_colorSet = GemColor.getColorSet(c,cc);
                        if (u.getComponent(c_cc_colorSet) != uu.getComponent(c_cc_colorSet)) {
                            testCond2 = false;
                            break;
                        }
                    }
                    if (!testCond2)
                        continue;

                    // System.out.println(String.format("Passed test 2..."));

                    // Now it is time to test if after the operation it
                    // really becomes a dipole
                    DipoleNarrowing dn = new DipoleNarrowing(d,uu,vv,c);
                    if (this.checkIfDipoleNarrowingIsValid(dn)) {
                        // System.out.println(String.format("Passed test 3..."));
                        dnList.add(dn);
                    }
                }
            }
        }

        return new Object[] {dtList, dnList};

    }

    // -- Tag components...
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // -- MinCut Algorithm to simplify gem to it's attractors

    public RectangleRepository identifyRectangles() {

        RectangleRepository rr = new RectangleRepository();

        // for each color
        for (GemColor cOff: GemColor.values()) {

            GemColor complementColors[] = GemColor.getComplementColors(cOff);

            // for every odd diagonal
            for (GemVertex u: _vertices) {

                if (u.hasEvenLabel())
                    continue;

                for (GemColor cIn: complementColors) {

                    // if diagonal of v is sorted then ok
                    GemVertex v = u.getNeighbour(cIn);
                    GemVertex uu = u.getNeighbour(cOff);
                    GemVertex vv = uu.getNeighbour(cIn);

                    if (vv.getLabel() < u.getLabel())
                        continue;

                    if (v.getNeighbour(cOff) != vv)
                        continue; // not a rectangle

                    // found a rectangle
                    rr.add(new Rectangle(u,uu,v,vv,cIn,cOff));
                }
            }
        }

        System.out.println(""+rr.toString());

        return rr;

    }

    public void rbond() {

        RectangleRepository rr = this.identifyRectangles();

        for (GemColor c: GemColor.values()) {

            System.out.println("Cor: "+c);

            GemColor[] inColors = GemColor.getComplementColors(c);

            ArrayList<RetangleSetWithSameTag> p = rr.getPartition(c);

            // create the contracted graph with offColor c
            DirectedSparseGraph g = new DirectedSparseGraph();

            // add vertices and build mapping
            HashMap<GemVertex,Vertex> mapV2Integer = new HashMap<GemVertex,Vertex>();
            HashMap<Vertex,RetangleSetWithSameTag> mapV2Tag = new HashMap<Vertex,RetangleSetWithSameTag>();
            HashMap<Vertex,ArrayList<GemVertex>> mapV2GemVs = new HashMap<Vertex,ArrayList<GemVertex>>();
            ArrayList<Vertex> pairs = new ArrayList<Vertex>();
            for (RetangleSetWithSameTag pair: p) {
                Object[] o = pair.getIndentificationSets();
                ArrayList<GemVertex> list1 = (ArrayList<GemVertex>) o[0];
                ArrayList<GemVertex> list2 = (ArrayList<GemVertex>) o[1];
                Vertex v1 = g.addVertex(new DirectedSparseVertex());
                Vertex v2 = g.addVertex(new DirectedSparseVertex());
                for (GemVertex v: list1)
                    mapV2Integer.put(v,v1);
                for (GemVertex v: list2)
                    mapV2Integer.put(v,v2);
                mapV2GemVs.put(v1,list1);
                mapV2GemVs.put(v2,list2);
                pairs.add(v1);
                pairs.add(v2);
                mapV2Tag.put(v1,pair);
                mapV2Tag.put(v2,pair);
            }

            for (GemVertex v: _vertices) {
                Vertex vv = mapV2Integer.get(v);
                if (vv == null) {
                    vv = g.addVertex(new DirectedSparseVertex());
                    mapV2Integer.put(v,vv);
                    ArrayList<GemVertex> list = new ArrayList<GemVertex>();
                    list.add(v);
                    mapV2GemVs.put(vv,list);
                }
            }

            // add edges
            String capacityKey = "cap";
            HashMap<String,DirectedSparseEdge> mapSt2Edge =  new HashMap<String,DirectedSparseEdge>();
            for (GemVertex v: _vertices) {
                if (v.hasEvenLabel())
                    continue;

                Vertex vv = mapV2Integer.get(v);
                for (GemColor ic: inColors) {
                    GemVertex u = v.getNeighbour(ic);
                    Vertex uu = mapV2Integer.get(u);

                    if (vv == uu)
                        continue;

                    String key1 = vv.toString()+"-"+uu.toString();
                    String key2 = uu.toString()+"-"+vv.toString();
                    DirectedSparseEdge e1 = mapSt2Edge.get(key1);
                    DirectedSparseEdge e2 = mapSt2Edge.get(key2);

                    if (e1 == null) {
                        DirectedSparseEdge e;
                        e = (DirectedSparseEdge) g.addEdge(new DirectedSparseEdge(vv,uu));
                        e.setUserDatum(capacityKey,new MutableInteger(1),UserData.SHARED);
                        mapSt2Edge.put(key1,e);
                        e = (DirectedSparseEdge) g.addEdge(new DirectedSparseEdge(uu,vv));
                        e.setUserDatum(capacityKey,new MutableInteger(1),UserData.SHARED);
                        mapSt2Edge.put(key2,e);
                    }
                    else {
                        MutableInteger mi;
                        mi = (MutableInteger) e1.getUserDatum(capacityKey);
                        mi.increment();
                        // e1.setUserDatum(capacityKey,mi,UserData.SHARED);

                        mi = (MutableInteger) e2.getUserDatum(capacityKey);
                        mi.increment();
                        // e2.setUserDatum(capacityKey,new MutableInteger(mi.intValue()+1),UserData.SHARED);
                    }
                }
            }

            for (int i=0;i<pairs.size();i+=2) {
                Vertex a = pairs.get(i);
                Vertex b = pairs.get(i+1);

                EdmondsKarpMaxFlow ek = new EdmondsKarpMaxFlow(g,a,b,capacityKey,"FLOW");
		ek.evaluate();

                RetangleSetWithSameTag tag = mapV2Tag.get(a);
                System.out.println("Caso MinCut: "+ek.getMaxFlow()+"  TagSize: "+tag.size());

                System.out.println("Source");
                System.out.println(labelsToString(mapV2GemVs.get(a)));
                System.out.println("Sink");
                System.out.println(labelsToString(mapV2GemVs.get(b)));
                System.out.println("Cut");
                Set<Edge> S = (Set<Edge>) ek.getMinCutEdges();
                for (Edge e: S) {
                    System.out.println(
                            labelsToString(mapV2GemVs.get(e.getEndpoints().getFirst()))+"  ---  "+
                            labelsToString(mapV2GemVs.get(e.getEndpoints().getSecond())));
                }

            }


            GraphMLFile graphmlFile = new GraphMLFile();
            graphmlFile.save(g,"c:/g-"+c+".ml");
        }
    }

    public String labelsToString(ArrayList<GemVertex> list) {
        boolean first = true;
        StringBuffer s = new StringBuffer();
        for (GemVertex v : list) {
            if (!first) {
                s.append(" ");
            }
            s.append(v.getLabel());
            first = false;
        }
        return s.toString();
    }

    // -- MinCut Algorithm to simplify gem to it's attractors
    // ------------------------------------------------------------------------


    // ------------------------------------------------------------------------
    // -- TS Moves
    public void tsMoves() {
        // must be in a ok-labelling
        this.goToCodeLabel();

        ArrayList<TSMovePoint> list = new ArrayList<TSMovePoint>();

        for (GemVertex v: _vertices) {
            for (GemColor[] p: GemColor.PERMUTATIONS) {
                for (TSMoveType type: TSMoveType.values()) {
                    if (isTSMovePoint(v, p, type)) {
                        list.add(new TSMovePoint(v.getLabel(), p, type));
                    }
                }
            }
        }

        System.out.println(""+this.getCurrentLabelling().getLettersString(" "));

        // now do the moves on the copies and write down the codes
        for (TSMovePoint m: list) {
            Gem copy = this.copy();
            copy.applyTSMove(m);
            System.out.println(""+copy.getCurrentLabelling().getLettersString(" "));
        }
    }

    public boolean isTSMovePoint(GemVertex v, GemColor p[], TSMoveType type) {
        if (type == TSMoveType.TS1) {
            GemVertex a = v;
            GemVertex b = a.getNeighbour(p[2]);
            GemVertex c = b.getNeighbour(p[3]);
            GemVertex d = c.getNeighbour(p[2]);

            if (a.getNeighbour(p[3]) != d) // test
                return false;

            GemVertex e = c.getNeighbour(p[1]);
            GemVertex f = e.getNeighbour(p[2]);

            if (d.getNeighbour(p[1]) != f) // test
                return false;

            GemVertex g = f.getNeighbour(p[3]);

            if (a.getNeighbour(p[1]) != g) // test
                return false;

            GemVertex h = d.getNeighbour(p[0]);

            HashSet<GemVertex> set = new HashSet<GemVertex>();
            set.add(a);set.add(b);set.add(c);set.add(d);
            set.add(e);set.add(f);set.add(g);set.add(h);
            if (set.size() < 8)
                return false;

            return true;
        }
        else if (type == TSMoveType.TS2) {
            GemVertex a = v;
            GemVertex b = a.getNeighbour(p[2]);
            GemVertex c = b.getNeighbour(p[3]);
            GemVertex d = c.getNeighbour(p[2]);

            if (a.getNeighbour(p[3]) != d) // test
                return false;

            GemVertex e = c.getNeighbour(p[1]);
            GemVertex f = e.getNeighbour(p[2]);

            if (d.getNeighbour(p[1]) != f) // test
                return false;

            GemVertex g = f.getNeighbour(p[3]);

            if (a.getNeighbour(p[1]) != g) // test
                return false;

            GemVertex h = a.getNeighbour(p[0]);

            HashSet<GemVertex> set = new HashSet<GemVertex>();
            set.add(a);set.add(b);set.add(c);set.add(d);
            set.add(e);set.add(f);set.add(g);set.add(h);
            if (set.size() < 8)
                return false;

            return true;
        }
        else if (type == TSMoveType.TS3) {
            GemVertex a = v;
            GemVertex b = a.getNeighbour(p[3]);
            GemVertex c = b.getNeighbour(p[1]);
            GemVertex d = c.getNeighbour(p[3]);

            if (a.getNeighbour(p[1]) != d) // test
                return false;

            GemVertex e = c.getNeighbour(p[2]);
            GemVertex f = e.getNeighbour(p[3]);

            if (d.getNeighbour(p[2]) != f) // test
                return false;

            GemVertex g = f.getNeighbour(p[1]);

            if (a.getNeighbour(p[2]) != g) // test
                return false;

            GemVertex h = e.getNeighbour(p[0]);

            HashSet<GemVertex> set = new HashSet<GemVertex>();
            set.add(a);set.add(b);set.add(c);set.add(d);
            set.add(e);set.add(f);set.add(g);set.add(h);
            if (set.size() < 8)
                return false;

            return true;
        }



        else if (type == TSMoveType.TS4) {
            GemVertex a = v;
            GemVertex b = a.getNeighbour(p[0]);
            GemVertex c = b.getNeighbour(p[2]);
            GemVertex d = c.getNeighbour(p[0]);

            if (a.getNeighbour(p[2]) != d) // test
                return false;

            GemVertex e = d.getNeighbour(p[3]);
            GemVertex f = e.getNeighbour(p[2]);

            if (a.getNeighbour(p[3]) != f) // test
                return false;

            GemVertex g = f.getNeighbour(p[1]);
            GemVertex h = g.getNeighbour(p[3]);

            if (a.getNeighbour(p[1]) != h) // test
                return false;

            /*
            System.out.println("Is Valid?");
            System.out.println(String.format("a = %d",a.getLabel()));
            System.out.println(String.format("b = %d",b.getLabel()));
            System.out.println(String.format("c = %d",c.getLabel()));
            System.out.println(String.format("d = %d",d.getLabel()));
            System.out.println(String.format("e = %d",e.getLabel()));
            System.out.println(String.format("f = %d",f.getLabel()));
            System.out.println(String.format("g = %d",g.getLabel()));
            System.out.println(String.format("h = %d", h.getLabel()));*/

            HashSet<GemVertex> set = new HashSet<GemVertex>();
            set.add(a);set.add(b);set.add(c);set.add(d);
            set.add(e);set.add(f);set.add(g);set.add(h);
            if (set.size() < 8)
                return false;

            return true;
        }
        else if (type == TSMoveType.TS5) {
            GemVertex a = v;
            GemVertex b = a.getNeighbour(p[3]);
            GemVertex c = b.getNeighbour(p[0]);
            GemVertex d = c.getNeighbour(p[3]);

            if (a.getNeighbour(p[0]) != d) // test
                return false;

            GemVertex e = d.getNeighbour(p[1]);
            GemVertex f = e.getNeighbour(p[3]);

            if (c.getNeighbour(p[1]) != f) // test
                return false;

            GemVertex g = f.getNeighbour(p[2]);
            GemVertex h = g.getNeighbour(p[3]);

            if (e.getNeighbour(p[2]) != h) // test
                return false;

            HashSet<GemVertex> set = new HashSet<GemVertex>();
            set.add(a);set.add(b);set.add(c);set.add(d);
            set.add(e);set.add(f);set.add(g);set.add(h);
            if (set.size() < 8)
                return false;

            return true;
        }
        else if (type == TSMoveType.TS6) {
            GemVertex a = v;
            GemVertex b = a.getNeighbour(p[2]);
            GemVertex e = b.getNeighbour(p[0]);
            GemVertex h = e.getNeighbour(p[2]);

            if (a.getNeighbour(p[0]) != h) // test
                return false;

            GemVertex c = b.getNeighbour(p[1]);
            GemVertex d = c.getNeighbour(p[0]);

            if (e.getNeighbour(p[1]) != d) // test
                return false;

            GemVertex f = e.getNeighbour(p[3]);
            GemVertex g = f.getNeighbour(p[0]);

            if (b.getNeighbour(p[3]) != g) // test
                return false;

            HashSet<GemVertex> set = new HashSet<GemVertex>();
            set.add(a);set.add(b);set.add(c);set.add(d);
            set.add(e);set.add(f);set.add(g);set.add(h);
            if (set.size() < 8)
                return false;

            return true;
        }


        return false;
    }

    public void applyTSMove(TSMovePoint m) {
        GemVertex a,b,c,d,e,f,g,h;
        GemColor p[] = m.getP();

        // vs[i] = seq[i].getNeightbour(p[cs[i]) and
        // will be rewired to vt[i] on color p[cs[i]]
        GemVertex[] seq;
        int[]       cs;
        GemVertex[] vs;
        GemVertex[] vt;

        if (m.getType() == TSMoveType.TS1) {

            a = this.findVertex(m.getA());
            b = a.getNeighbour(p[2]);
            c = b.getNeighbour(p[3]);
            d = c.getNeighbour(p[2]);
            e = c.getNeighbour(p[1]);
            f = e.getNeighbour(p[2]);
            g = f.getNeighbour(p[3]);
            h = d.getNeighbour(p[0]);

            seq = new GemVertex[] {a, h, g, g, h, f, e, e, h, c, b, b}; // source
            vt = new GemVertex[] {e, e, h, c, b, b, a, h, g, g, h, f}; // target
            cs = new int[] {0, 3, 2, 0, 1, 0, 0, 3, 2, 0, 1, 0}; // color
        }
        else if (m.getType() == TSMoveType.TS2) {
            a = this.findVertex(m.getA());
            b = a.getNeighbour(p[2]);
            c = b.getNeighbour(p[3]);
            d = c.getNeighbour(p[2]);
            e = c.getNeighbour(p[1]);
            f = e.getNeighbour(p[2]);
            g = f.getNeighbour(p[3]);
            h = a.getNeighbour(p[0]);

            seq = new GemVertex[] {c, h, d, h, g, e, e, f, g, b, h, b}; // source
            cs = new int[] {0, 3, 0, 2, 2, 0, 3, 0, 0, 1, 1, 0}; // color
            vt = new GemVertex[] {f, e, e, g, h, d, h, c, b, h, b, g}; // target
        }
        else if (m.getType() == TSMoveType.TS3) {
            a = this.findVertex(m.getA());
            b = a.getNeighbour(p[3]);
            c = b.getNeighbour(p[1]);
            d = c.getNeighbour(p[3]);
            e = c.getNeighbour(p[2]);
            f = e.getNeighbour(p[3]);
            g = f.getNeighbour(p[1]);
            h = e.getNeighbour(p[0]);
            seq = new GemVertex[] {e, a, g, g, f, h, d, h, h, c, b, b}; // source
            cs = new int[] {1, 0, 3, 0, 0, 3, 0, 1, 2, 0, 0, 2}; // color
            vt = new GemVertex[] {h, d, h, f, g, g, a, e, b, b, c, h}; // target
        }
        else if (m.getType() == TSMoveType.TS4) {
            a = this.findVertex(m.getA());
            b = a.getNeighbour(p[0]);
            c = b.getNeighbour(p[2]);
            d = c.getNeighbour(p[0]);
            e = d.getNeighbour(p[3]);
            f = e.getNeighbour(p[2]);
            g = f.getNeighbour(p[1]);
            h = g.getNeighbour(p[3]);
            seq = new GemVertex[] {h, b, c, h, g, f, e, e, d, c, b, g}; // source
            cs  = new int[]       {0, 1, 1, 2, 2, 0, 0, 1, 1, 3, 3, 0}; // color
            vt  = new GemVertex[] {e, e, f, g, h, c, b, h, g, b, c, d}; // target
        }
        else if (m.getType() == TSMoveType.TS5) {
            a = this.findVertex(m.getA());
            b = a.getNeighbour(p[3]);
            c = b.getNeighbour(p[0]);
            d = c.getNeighbour(p[3]);
            e = d.getNeighbour(p[1]);
            f = e.getNeighbour(p[3]);
            g = f.getNeighbour(p[2]);
            h = g.getNeighbour(p[3]);

            seq = new GemVertex[] {e, h, h, g, c, b, g, f, b, a, a, d}; // source
            cs = new int[]       {0, 0, 1, 1, 2, 2, 0, 0, 1, 1, 2, 2}; // color
            vt = new GemVertex[] {g, f, b, a, a, d, e, h, h, g, c, b}; // target
        }
        else if (m.getType() == TSMoveType.TS6) {
            a = this.findVertex(m.getA());
            b = a.getNeighbour(p[2]);
            e = b.getNeighbour(p[0]);
            h = e.getNeighbour(p[2]);
            c = b.getNeighbour(p[1]);
            d = c.getNeighbour(p[0]);
            f = e.getNeighbour(p[3]);
            g = f.getNeighbour(p[0]);

            seq = new GemVertex[] {f, g, g, f, d, c, d, c, a, h, h, a}; // source
            cs = new int[]       {2, 2, 1, 1, 3, 3, 2, 2, 1, 1, 3, 3}; // color
            vt = new GemVertex[] {d, c, a, h, h, a, f, g, g, f, d, c}; // target
        }

        // Exception...
        else {
            throw new RuntimeException();
        }

        /*
        System.out.println(String.format("a = %d",a.getLabel()));
        System.out.println(String.format("b = %d",b.getLabel()));
        System.out.println(String.format("c = %d",c.getLabel()));
        System.out.println(String.format("d = %d",d.getLabel()));
        System.out.println(String.format("e = %d",e.getLabel()));
        System.out.println(String.format("f = %d",f.getLabel()));
        System.out.println(String.format("g = %d",g.getLabel()));
        System.out.println(String.format("h = %d",h.getLabel()));  */

        // find fake edges
        _fakeVerticesCount = 0;
        ArrayList<FakeEdge> fakeEdges = new ArrayList<FakeEdge>();
        for (int i = 0; i < seq.length; i++) {
            GemColor color = p[cs[i]];
            GemVertex x = seq[i];
            GemVertex y = seq[i].getNeighbour(color);
            if (y == a || y == b || y == c || y == d || y == e || y == f || y == g || y == h) {
                // System.out.println("Fake edge: x: "+x.getLabel()+"   y: "+y.getLabel()+"  cor: "+color);
                FakeEdge fe = new FakeEdge(color);
                Gem.setNeighbours(x, fe.getA(), color);
                Gem.setNeighbours(y, fe.getB(), color);
                fakeEdges.add(fe);
            }
        }

        // now define the neighborhood
        vs  = new GemVertex[vt.length];
        for (int i=0;i<vs.length;i++)
            vs[i] = seq[i].getNeighbour(p[cs[i]]);

        // print vertices
        /* for (int i = 0; i < vs.length; i++) {
            System.out.println(String.format("n[%d] = %6d    %6d    %10s",i+1,vs[i].getLabel(),vs[i].getNeighbour(p[cs[i]]).getLabel(),p[cs[i]]));
        } */

        // do the rewiring... what is really the dipole problem?
        if (m.getType() == TSMoveType.TS4) {
            GemVertex v0, v1;

            v0 = a.getNeighbour(p[0]);
            v1 = a.getNeighbour(p[1]);
            Gem.setNeighbours(a,v0,p[1]);
            Gem.setNeighbours(a,v1,p[0]);

            // System.out.println(String.format("Rewiring %d to %d from   %s  to  %s",a.getLabel(),v0.getLabel(),p[0],p[1]));
            // System.out.println(String.format("Rewiring %d to %d from   %s  to  %s",a.getLabel(),v1.getLabel(),p[1],p[0]));

            v0 = c.getNeighbour(p[0]);
            Gem.setNeighbours(c,v0,p[1]);

            // System.out.println(String.format("Rewiring %d to %d from   %s  to  %s",c.getLabel(),v0.getLabel(),p[0],p[1]));

            v1 = f.getNeighbour(p[1]);
            Gem.setNeighbours(f,v1,p[0]);

            // System.out.println(String.format("Rewiring %d to %d from   %s  to  %s",f.getLabel(),v1.getLabel(),p[1],p[0]));

        }

        for (int i = 0; i < vs.length; i++) {
            GemColor color = p[cs[i]];
            GemVertex xx = vs[i];
            GemVertex yy = vt[i];
            Gem.setNeighbours(xx, yy, color);
            // System.out.println(String.format("Connecting: %6d to %6d with color %10s",xx.getLabel(),yy.getLabel(),color));
        }


        // merge fake edges
        // System.out.println(String.format("Connecting fake edges..."));
        for (FakeEdge fe : fakeEdges) {
            GemVertex xx = fe.getA().getNeighbour(fe.getColor());
            GemVertex yy = fe.getB().getNeighbour(fe.getColor());
            Gem.setNeighbours(xx, yy,fe.getColor());
            /*
            System.out.println(String.format("Fake edge %6d %6d Connecting: %6d to %6d with color %10s",
                                             fe.getA().getLabel(), fe.getB().getLabel(), xx.getLabel(), yy.getLabel(),
                                             fe.getColor()));
            */
        }

        this.setGemAsModified(); // gem was modified. needs everything again.

    }

    public int hashCode() {
        this.sortVerticesByLabel();
        int result = 0;
        for (GemVertex v: _vertices) {
            int k = v.getYellow().getLabel();
            k-=v.getBlue().getLabel();
            k*=v.getRed().getLabel();
            k+=v.getGreen().getLabel();
            result += k;
        }
        return result;
    }

    static int _fakeVerticesCount = 0;

    public String getExpandedWord() {
        boolean first = true;
        StringBuffer s = new StringBuffer();
        for (GemVertex v: _vertices) {
            if (!first)
                s.append(",");
            s.append(v.getLabel());
            s.append("Y"+v.getYellow().getLabel());
            s.append("B"+v.getBlue().getLabel());
            s.append("R"+v.getRed().getLabel());
            s.append("G"+v.getGreen().getLabel());
            first = false;
        }
        return s.toString();
    }

    public int compareTo(Object o) {
        Gem g = (Gem) o;
        int r = this.getNumVertices() - g.getNumVertices();
        if (r != 0)
            return r;

        this.sortVerticesByLabel();
        g.sortVerticesByLabel();

        // if there is some hole on the
        // vertices labelling then they
        // are different.
        int n=this.getNumVertices();
        int i=0;
        while (r == 0 && i < n) {
            GemVertex u = _vertices.get(i);
            GemVertex v = g._vertices.get(i);
            r = u.getLabel() - v.getLabel();
            i++;
        }
        if (r != 0)
            return r;

        // now compare each color for a
        // difference.
        for (GemColor c: GemColor.getColorsOfColorSet(GemColor.COLORSET_ALL_COLORS)) {
            i=0;
            while (r == 0 && i < n) {
                GemVertex u = _vertices.get(i).getNeighbour(c);
                GemVertex v = g._vertices.get(i).getNeighbour(c);
                r = u.getLabel() - v.getLabel();
                i++;
            }
            if (r != 0)
                return r;
        }

        return r;
    }

    public boolean equals(Object o) {
        return this.compareTo(o) == 0;
    }

    class FakeEdge {
        private GemColor _color;
        private GemVertex _a;
        private GemVertex _b;
        public FakeEdge(GemColor c) {
            _color = c;
            _a = new GemVertex(-1*(++_fakeVerticesCount));
            _b = new GemVertex(-1*(++_fakeVerticesCount));
        }

        public GemVertex getA() {
            return _a;
        }

        public GemVertex getB() {
            return _b;
        }

        public GemColor getColor() {
            return _color;
        }
    }

    // -- TS Moves
    // ------------------------------------------------------------------------


    // ------------------------------------------------------------------------
    // Find spanning tree on contracted bigon graph
    // with twistor and antipole edges.

    public DifferenceToS3 getDifferenceToS3() {
        GemColor[][] ps = {
                         {GemColor.blue, GemColor.red},
                         {GemColor.blue, GemColor.green},
                         {GemColor.red, GemColor.green}
        };

        ArrayList<Twistor> twistors = this.findAllTwistors();
        ArrayList<Antipole> antipoles = this.findAllAntipoles();

        this.tag();

        int minComponents = 0;
        BGraph minGraph = null;
        GemColor minColors[] = null;
        ArrayList<BGraphEdge> minList = null;

        for (GemColor[] p: ps) {

            GemColor c1 = p[0];
            GemColor c2 = p[1];
            int colorSet = GemColor.getColorSet(c1, c2);

            // bigons
            ArrayList<Component> B = _components.getBigons(c1, c2);

            // create vertices
            BGraph BG = new BGraph();
            HashMap<Component, BGraphVertex> map = new HashMap<Component, BGraphVertex>();
            for (Component c : B) {
                BGraphVertex v = BG.newVertex(c);
                map.put(c, v);
            }

            // create edges
            for (Twistor t : twistors) {
                if (t.getColor() != c1 && t.getColor() != c2)
                    continue;
                BGraphVertex v1 = map.get(t.getU().getComponent(colorSet));
                BGraphVertex v2 = map.get(t.getV().getComponent(colorSet));
                BG.newEdge(v1, v2, t);
            }

            // create edges
            for (Antipole a: antipoles) {
                if (a.getColor() != c1 && a.getColor() != c2)
                    continue;
                BGraphVertex v1 = map.get(a.getU().getComponent(colorSet));
                BGraphVertex v2 = map.get(a.getV().getComponent(colorSet));
                BG.newEdge(v1, v2, a);
            }


            // incompatibile edges
            ArrayList<BGraphEdge> edges = BG.getEdges();
            for (int i = 0; i < edges.size(); i++) {
                BGraphEdge e1 = edges.get(i);
                for (int j = i + 1; j < edges.size(); j++) {
                    BGraphEdge e2 = edges.get(j);
                    if (BGraphEdge.hasCommonVertex(e1, e2)) {
                        // System.out.println("Incompat. "+e1.getKey()+" "+e2.getKey());
                        BG.addIncompatibleEdges(e1, e2);
                    }
                }
            }

            ArrayList<BGraphEdge> list = BG.getMaximumCompatibleEdgesToBuildUpATree();
            int numComponents = BG.getNumVertices() - list.size();

            if (minList == null || numComponents < minComponents) {
                minList = list;
                minColors = p;
                minGraph = BG;
                minComponents = numComponents;
            }

            if (minComponents == 1) {
                break;
            }

        }
        return new DifferenceToS3(minGraph,minColors[0],minColors[1],minList);
    }

    // BGraph
    // ------------------------------------------------------------------------


    /**
     * Simplifies
     * @param gem Gem
     * @return boolean
     */
    public boolean simplifyDipolesAndRhoPairs() {

        boolean simplified = false;

        boolean foundRhoMove = false;

        // try to simplify gem
        while (true) {

            // simplify dipole if there exists one
            Dipole d = this.findAnyDipole();
            if (d != null) {
                System.out.println("<SIMPLIFICATION> Found dipole cancelation: " + d.toString());

                // alter gem (but keep the field originalLabel
                // of the vertices intact) by removing the
                // dipole d from it.
                this.cancelDipole(d);

                // turn on simplified flag
                simplified = true;
                foundRhoMove = false;
                continue;
            }
            else if (foundRhoMove) {
                throw new RuntimeException("Oooopsss");
            }

            // find any rho 3 pair
            RhoPair r3 = this.findAnyRho3Pair();
            if (r3 != null) {
                System.out.println("<SIMPLIFICATION> Found " + r3.toString() + ". Applying it...");

                // alter gem (but keep the field originalLabel
                // of the vertices intact) by removing the
                // dipole d from it.
                this.applyRhoPair(r3);

                // found rho move
                foundRhoMove = true;

                // the next iteration should find a dipole
                continue;
            }

            // find any rho 2 pair
            RhoPair r2 = this.findAnyRho2Pair();
            if (r2 != null) {
                System.out.println("<SIMPLIFICATION> Found " + r2.toString() + ". Applying it...");

                // alter gem (but keep the field originalLabel
                // of the vertices intact) by removing the
                // dipole d from it.
                this.applyRhoPair(r2);

                // found rho move
                foundRhoMove = true;

                // the next iteration should find a dipole
                continue;
            }

            // no rho and no dipole cancelation!
            break;
        }

        return simplified;

    }


    /**
     * Simplify a copy of this Gem by removing dipoles,
     * using TS1, TS2, TS3 and TS4.
     */
    public Gem getVersionWithoutFourClusters() {
        Gem g = this.copy();


        TSMoveType types[] = {TSMoveType.TS1,TSMoveType.TS2,TSMoveType.TS3,TSMoveType.TS4};

        g.simplifyDipolesAndRhoPairs();

        // the famous go to!!!
        findSimplifyingTSMove:

        for (GemVertex v : g.getVertices()) {
            for (GemColor[] p : GemColor.PERMUTATIONS) {
                for (TSMoveType type : types) {
                    if (g.isTSMovePoint(v, p, type)) {
                        TSMovePoint m = new TSMovePoint(v.getLabel(), p, type);
                        Gem copy = g.copy();
                        copy.applyTSMove(m);
                        copy.goToCodeLabel();

                        System.out.println(""+copy.getCurrentLabelling().getLettersString(""));

                        System.out.println("Trying Move "+m);
                        if (copy.simplifyDipolesAndRhoPairs()) {

                            g = copy;
                            break findSimplifyingTSMove;
                        }
                    }
                }
            }
        }

        // return a gem without simplifications...
        return g;

    }
}

// ----------------------------------------------------------------------------
// Graph with Contracted Bigons and
// Twistor/Antipole edges and a set of
// incompatible pairs.
class BGraph {
    private ArrayList<BGraphVertex> _vertices = new ArrayList<BGraphVertex>();
    private ArrayList<BGraphEdge> _edges = new ArrayList<BGraphEdge>();

    public BGraphVertex newVertex(Object key) {
        BGraphVertex v = new BGraphVertex(key);
        _vertices.add(v);
        return v;
    }

    public ArrayList<BGraphEdge> getEdges() {
        return _edges;
    }

    public BGraphEdge newEdge(BGraphVertex u, BGraphVertex v, Object key) {
        BGraphEdge e = new BGraphEdge(key,u,v);
        u.addEdge(e);
        v.addEdge(e);
        _edges.add(e);
        return e;
    }

    public void addIncompatibleEdges(BGraphEdge e1, BGraphEdge e2) {
        e1.addIncompaibleEdge(e2);
        e2.addIncompaibleEdge(e1);
    }

    private void untagVerticesAndEdges() {
        for (BGraphVertex v: _vertices) v.untag();
        for (BGraphEdge e: _edges) e.untag();
    }

    public int getNumVertices() { return _vertices.size(); }
    public int getNumEdges() { return _edges.size(); }


    // Find a maximum tree that with compatible edges
    private ArrayList<BGraphEdge> _taggedEdges;
    private ArrayList<BGraphEdge> _maxTaggedEdges;

    /**
     * Return edges
     */
    public ArrayList<BGraphEdge>  oldMaximumCompatibleTree() {
        this.untagVerticesAndEdges();
        _taggedEdges = new ArrayList<BGraphEdge>();
        _maxTaggedEdges = new ArrayList<BGraphEdge>();
        if (this.getNumVertices() > 0) {
            maximumCompatibleTreeDFS(_vertices.get(0));
        }
        return _maxTaggedEdges;
    }

    public void maximumCompatibleTreeDFS(BGraphVertex u) {
        // tag vertex
        u.tag();

        //
        for (BGraphEdge e: u.getEdges()) {
            if (e.hasSomeTaggedEdgeOnIncompaibilityList() || e.getOpposite(u).isTagged())
                continue;

            // ok. expand the tree.
            e.tag();
            _taggedEdges.add(e);

            maximumCompatibleTreeDFS(e.getOpposite(u));

            e.untag();
            _taggedEdges.remove(_taggedEdges.size()-1);

            // already a spanning tree?
            if (_maxTaggedEdges.size() == this.getNumVertices()-1)
                break;
        }

        // change the maximum tree found
        if (_taggedEdges.size() > _maxTaggedEdges.size()) {
            _maxTaggedEdges.clear();
            _maxTaggedEdges.addAll(_taggedEdges);
        }

        // untag vertex
        u.untag();
    }

    private BGraphEdge[][] getEdgesOptimizedArray() {
        HashSet<BGraphEdge> S = new HashSet<BGraphEdge>();
        ArrayList<ArrayList<BGraphEdge>> L = new ArrayList<ArrayList<BGraphEdge>>();

        HashMap<GemVertex,ArrayList<BGraphEdge>> map = new HashMap<GemVertex,ArrayList<BGraphEdge>>();
        for (BGraphEdge e: _edges) {
            GemVertex uu,vv;
            if (e.getKey() instanceof Twistor) {
                Twistor t = (Twistor) e.getKey();
                uu = t.getU();
                vv = t.getV();
            }
            else {
                Antipole a = (Antipole) e.getKey();
                uu = a.getU();
                vv = a.getV();
            }

            //
            ArrayList<BGraphEdge> listU = map.get(uu);
            if (listU == null) {
                listU = new ArrayList<BGraphEdge>();
                map.put(uu,listU);
            }
            listU.add(e);

            //
            ArrayList<BGraphEdge> listV = map.get(vv);
            if (listV == null) {
                listV = new ArrayList<BGraphEdge>();
                map.put(vv,listV);
            }
            listV.add(e);
        }

        for (GemVertex v: map.keySet()) {
            ArrayList<BGraphEdge> list = map.get(v);
            for (int i = list.size()-1;i>=0;i--)
                if (S.contains(list.get(i)))
                    list.remove(i);

            if (list.size() > 0) {
                S.addAll(list);
                L.add(list);
            }
        }

        BGraphEdge[][] result = new BGraphEdge[L.size()][];
        for (int i = 0; i < L.size(); i++) {
            ArrayList<BGraphEdge> Li = L.get(i);
            BGraphEdge[] ri = new BGraphEdge[Li.size()];
            for (int j=0;j<Li.size();j++)
                ri[j] = Li.get(j);
            result[i]=ri;
        }

        /*
        for (int i=0;i<result.length;i++) {
            System.out.println("-------------");
            for (int j=0;j<result[i].length;j++) {
                System.out.println(""+result[i][j].getKey().toString());
            }
        }*/

        return result;
    }

    public ArrayList<BGraphEdge> getMaximumCompatibleEdgesToBuildUpATree() {

        if (this.getNumVertices() == 1)
            return new ArrayList<BGraphEdge>();

        BGraphEdgeIterator eIterators[] = new BGraphEdgeIterator[this.getNumVertices()-1];
        BGraphEdge[][] edges = this.getEdgesOptimizedArray();
        BGraphComponentControl componentControl = new BGraphComponentControl();

        ArrayList<BGraphEdge> currentList = new ArrayList<BGraphEdge>();
        ArrayList<BGraphEdge> bestList = new ArrayList<BGraphEdge>();

        HashMap<Integer,BGraphEdge> mapAddedEdges = new HashMap<Integer,BGraphEdge>();

        int i=0;
        while (i >= 0) {

            // get this position's iterator
            BGraphEdgeIterator it = eIterators[i];
            if (it == null) {
                it = new BGraphEdgeIterator(edges);
                eIterators[i] = it;
                BGraphEdgeIterator itPrevious = null;
                if (i > 0)
                    itPrevious = eIterators[i-1];
                it.reset(itPrevious);
            }

            if (mapAddedEdges.get(i)!=null) {
                // System.out.println("Remove "+mapAddedEdges.get(i).getKey().toString());
                currentList.remove(mapAddedEdges.get(i));
                componentControl.removeEdge(mapAddedEdges.get(i));
                mapAddedEdges.put(i,null);
            }


            // bgraph edge
            BGraphEdge e = it.next();
            // if (e != null)
            //     System.out.println("Trying: " + e.getKey().toString());
            while (e != null && (componentControl.createsCycle(e) ||
                                 componentControl.containsEdgeIncidentToVertexOf(e))) {
                e = it.next();
                // if (e != null)
                //    System.out.println("Trying: " + e.getKey().toString());
            }

            // nothing more can be done
            if (e == null) {
                i--;
                continue;
            }

            // add to current list
            // System.out.println("Adding "+e);
            componentControl.addEdge(e);
            currentList.add(e);
            mapAddedEdges.put(i,e);

            // check if it is a better solution
            if (bestList.size() <  currentList.size()) {
                bestList.clear();
                bestList.addAll(currentList);
            }

            // completed a spanning tree
            if (i == this.getNumVertices()-2) {
                break; // found a tree!
            }

            // not a spanning tree
            else {
                // prepare next position
                BGraphEdgeIterator itNext = eIterators[i+1];
                if (itNext == null) {
                    itNext = new BGraphEdgeIterator(edges);
                    eIterators[i+1] = itNext;
                }
                itNext.reset(it);
                i++;
            }
        }

        return bestList;

    }
}

class BGraphEdgeIterator {
    BGraphEdge[][] _edges;
    int _i;
    int _j;
    public BGraphEdgeIterator(BGraphEdge[][] edges) {
        _edges = edges;
    }
    public void reset(BGraphEdgeIterator previous) {
        if (previous == null) {
            _i = -1;
            _j = -1;
        }
        else {
            _i = previous._i+1;
            _j = -1;
        }
    }
    private void goToNextPosition() {
        boolean found = false;
        if (_i == -1) {
            _i=0; _j=0;
        }
        else _j++;
        for (;_i<_edges.length;_i++) {
            for (;_j<_edges[_i].length;_j++) {
                if (!_edges[_i][_j].hasSomeTaggedEdgeOnIncompaibilityList()) {
                    found = true;
                    break;
                }
            }
            if (found) break;
            _j=0;
        }
    }

    public BGraphEdge next() {
        goToNextPosition();
        BGraphEdge result = current();
        return result;
    }

    public BGraphEdge current() {
        BGraphEdge result = null;
        if (_i != -1 && _j != -1 && _i < _edges.length && _j < _edges[_i].length)
            result = _edges[_i][_j];
        return result;
    }

}

class BGraphComponentControl {
    private int _nextFreeLabel = 1;
    HashMap<BGraphVertex,Integer> _map = new HashMap<BGraphVertex,Integer>();
    HashSet<GemVertex> _usedVertices = new HashSet<GemVertex>();
    public BGraphComponentControl() {
    }
    public boolean createsCycle(BGraphEdge e) {
        Integer c1 = _map.get(e.getU());
        Integer c2 = _map.get(e.getV());
        if (c1 != null && c2 != null && c1.equals(c2))
            return true;
        else
            return false;
    }
    public int nextFreeLabel() {
        int result =  _nextFreeLabel;
        _nextFreeLabel++;
        return result;
    }
    public void addEdge(BGraphEdge e) {
        BGraphVertex v1 = e.getU();
        BGraphVertex v2 = e.getV();
        Integer c1 = _map.get(e.getU());
        Integer c2 = _map.get(e.getV());

        int nextFreeLabel = this.nextFreeLabel();


        if (c1 == null && c2 == null) {
            _map.put(v1,nextFreeLabel);
            _map.put(v2,nextFreeLabel);
        }
        else if (c1 == null) {
            _map.put(v1,c2);
        }
        else if (c2 == null) {
            _map.put(v2,c1);
        }
        else { // both are in
            if (c1.equals(c2)) throw new RuntimeException("");
            HashSet<BGraphVertex> set = new HashSet<BGraphVertex>(_map.keySet());
            for (BGraphVertex v: set) {
                if (c2.equals(_map.get(v))) {
                    _map.put(v,c1);
                }
            }
        }

        // add vertices
        Pair pair = e.getGemVerticesFromKey();
        _usedVertices.add((GemVertex)pair.getFirst());
        _usedVertices.add((GemVertex)pair.getSecond());

    }

    public boolean containsEdgeIncidentToVertexOf(BGraphEdge e) {
        Pair pair = e.getGemVerticesFromKey();
        return _usedVertices.contains(pair.getFirst()) ||
                _usedVertices.contains(pair.getSecond());
    }

    public void removeEdge(BGraphEdge e) {
        BGraphVertex v = e.getU();
        HashSet<BGraphVertex> verticesProcessed = new HashSet<BGraphVertex>();
        this.dfs(v,nextFreeLabel(),e,verticesProcessed);


        // add vertices
        Pair pair = e.getGemVerticesFromKey();
        _usedVertices.remove((GemVertex)pair.getFirst());
        _usedVertices.remove((GemVertex)pair.getSecond());

    }

    private void dfs(BGraphVertex u, int label, BGraphEdge e, HashSet<BGraphVertex> verticesProcessed) {
        _map.put(u,label);
        verticesProcessed.add(u);
        for (BGraphEdge ee: u.getEdges()) {
            if (e == ee) continue;
            else if (!ee.isTagged()) continue;
            BGraphVertex v = ee.getOpposite(u);
            if (verticesProcessed.contains(v))
                continue;
            dfs(v,label,e,verticesProcessed);
        }
    }
}



class BGraphElement {
    public BGraphElement(Object key) { _key = key; }

    // key infrastructure
    private Object _key;
    public Object getKey() { return _key; }

    // tag infrastructure
    private boolean _tag;
    public boolean isTagged() { return _tag; }
    public void tag() { _tag = true; }
    public void untag() { _tag = false; }
}

class BGraphVertex extends BGraphElement {
    ArrayList<BGraphEdge> _edges = new ArrayList<BGraphEdge>();
    public BGraphVertex(Object key) {
        super(key);
    }
    public ArrayList<BGraphEdge> getEdges() {
        return _edges;
    }
    public void addEdge(BGraphEdge e) {
        _edges.add(e);
    }
    public String toString() {
        if (this.getKey() != null)
            return this.getKey().toString();
        else return super.toString();
    }
}

class BGraphEdge extends BGraphElement {

    private BGraphVertex _u;
    private BGraphVertex _v;

    public BGraphEdge(Object key, BGraphVertex u, BGraphVertex v) {
        super(key);
        _u = u;
        _v = v;
    }

    public BGraphVertex getU() {
        return _u;
    }

    public BGraphVertex getV() {
        return _v;
    }

    //
    public BGraphVertex getOpposite(BGraphVertex w) {
        if (w != _u && w != _v)
            throw new RuntimeException("Oooopspss");
        return (w == _v ? _u : _v);
    }

    // incompatible edges
    private ArrayList<BGraphEdge> _incompatibleEdges = new ArrayList<BGraphEdge>();
    public void addIncompaibleEdge(BGraphEdge e) {
        _incompatibleEdges.add(e);
    }
    public boolean hasSomeTaggedEdgeOnIncompaibilityList() {
        for (BGraphEdge e: _incompatibleEdges) {
            if (e.isTagged())
                return true;
        }
        return false;
    }

    public ArrayList<BGraphEdge> getIncompatibleEdges() {
        return (ArrayList<BGraphEdge>) _incompatibleEdges.clone();
    }

    public static boolean hasCommonVertex(BGraphEdge e1, BGraphEdge e2) {
        Object o1 = e1.getKey();
        Object o2 = e2.getKey();

        GemVertex u1,u2,v1,v2;

        if (o1 instanceof Twistor) {
            u1 = ((Twistor) o1).getU();
            v1 = ((Twistor) o1).getV();
        }
        else if (o1 instanceof Antipole) {
            u1 = ((Antipole) o1).getU();
            v1 = ((Antipole) o1).getV();
        }
        else throw new RuntimeException();

        if (o2 instanceof Twistor) {
            u2 = ((Twistor) o2).getU();
            v2 = ((Twistor) o2).getV();
        }
        else if (o2 instanceof Antipole) {
            u2 = ((Antipole) o2).getU();
            v2 = ((Antipole) o2).getV();
        }
        else throw new RuntimeException();

        return (u1 == u2 ||
                u1 == v2 ||
                v1 == u2 ||
                v2 == u2);
    }

    public Pair getGemVerticesFromKey() {
        GemVertex uu,vv;
        if (this.getKey() instanceof Twistor) {
            Twistor t = (Twistor) this.getKey();
            uu = t.getU();
            vv = t.getV();
        }
        else {
            Antipole a = (Antipole) this.getKey();
            uu = a.getU();
            vv = a.getV();
        }
        return new Pair(uu,vv);
    }

    public String toString() {
        if (this.getKey() != null)
            return this.getKey().toString();
        else return super.toString();
    }

}
// --
// ----------------------------------------------------------------------------






enum TSMoveType {
    TS1, TS2, TS3, TS4, TS5, TS6
}

class TSMovePoint {
    TSMoveType _type;
    int _a;
    GemColor[] _p;
    public TSMovePoint(int a, GemColor[] p, TSMoveType type) {
        _a = a;
        _p = p;
        _type = type;
    }
    public int getA() {
        return _a;
    }
    public GemColor[] getP() {
        return _p;
    }
    public TSMoveType getType() {
        return _type;
    }
    public String toString() {
        return String.format("%5s %5d %8s%8s%8s%8s",_type,_a,_p[0],_p[1],_p[2],_p[3]);
    }
}

class Rectangle {
    private GemVertex _u;
    private GemVertex _uu;
    private GemVertex _v;
    private GemVertex _vv;
    private GemColor _inColor;
    private GemColor _offColor;

    /**
     * u  - uu  (offColor)
     * v  - vv  (offColor)
     * u  - v   (inColor)
     * uu - vv  (inColor)
     */
    public Rectangle(
            GemVertex u,
            GemVertex uu,
            GemVertex v,
            GemVertex vv,
            GemColor inColor,
            GemColor offColor) {
        _u  = u;
        _uu = uu;
        _v  = v;
        _vv = vv;
        _inColor = inColor;
        _offColor = offColor;
    }
    public GemVertex getU()  { return _u; }
    public GemVertex getUU() { return _uu; }
    public GemVertex getV()  { return _v; }
    public GemVertex getVV() { return _vv; }
    public GemColor getInColor()  { return _inColor; }
    public GemColor getOffColor() { return _offColor; }
    public String toString() {
        return String.format("%d - %s - %d - %s - %d - %s - %d - %s - %d",
                             getU().getLabel(),
                             _offColor,
                             getUU().getLabel(),
                             _inColor,
                             getVV().getLabel(),
                             _offColor,
                             getV().getLabel(),
                             _inColor,
                             getU().getLabel());
    }
}

class RetangleSetWithSameTag {
    private int _label;
    private HashSet<GemVertex> _vertices = new HashSet<GemVertex>();
    private ArrayList<Rectangle> _rectangles = new ArrayList<Rectangle>();
    public RetangleSetWithSameTag(int label, Rectangle first) {
        _label = label;
        this.add(first);
    }

    public int size() {
        return _vertices.size()/2;
    }

    public GemColor getOffColor() {
        return _rectangles.get(0).getOffColor();
    }

    public HashSet<GemColor> getInColors() {
        HashSet<GemColor> S = new HashSet<GemColor>();
        for (Rectangle r: _rectangles)
            S.add(r.getInColor());
        return S;
    }

    public void merge(RetangleSetWithSameTag r) {
        _vertices.addAll(r._vertices);
        _rectangles.addAll(r._rectangles);
    }

    public ArrayList[] getIndentificationSets() {
        GemColor offColor = this.getOffColor();
        HashSet<GemColor> inColors = this.getInColors();

        // unmark all vertices
        for (GemVertex v: _vertices)
            v.setFlag(false);

        //
        ArrayList[] result = new ArrayList[2];
        int k = 0;
        for (GemVertex v : _vertices) {
            if (v.getFlag())
                continue;

            ArrayList<GemVertex> component = new ArrayList<GemVertex>();

            Stack<GemVertex> S = new Stack<GemVertex>();
            S.push(v);
            while (!S.isEmpty()) {
                GemVertex u = S.pop();
                component.add(u);
                u.setFlag(true);
                for (GemColor c : inColors) {
                    GemVertex uu = u.getNeighbour(c);
                    if (!uu.getFlag() && _vertices.contains(uu))
                        S.push(uu);
                }
            }
            result[k++] = component;
        }

        if (k == 1) {
            System.out.println("One Component Only");
            for (Rectangle r: _rectangles)
                System.out.println(""+r);
            throw new RuntimeException("Should not happen");
        }
        return result;
    }

    public int getLabel() {
        return _label;
    }

    private void add(Rectangle r) {
        _rectangles.add(r);
        _vertices.add(r.getU());
        _vertices.add(r.getV());
        _vertices.add(r.getUU());
        _vertices.add(r.getVV());
    }

    public boolean intersects(Rectangle r) {
        if ((_vertices.contains(r.getU()) && _vertices.contains(r.getUU())) ||
            (_vertices.contains(r.getV()) && _vertices.contains(r.getVV()))) {
            return true;
        }
        else return false;
    }

    public boolean addIfIntersects(Rectangle r) {
        boolean result = intersects(r);
        if (result) {
            this.add(r);
        }
        return result;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(String.format("Rectangles with tag: %d\n",this.getLabel()));
        for (Rectangle r : _rectangles) {
            sb.append("   "+r.toString()+"\n");
        }
        return sb.toString();
    }
}

class RectangleRepository {

    HashMap<GemColor,ArrayList<RetangleSetWithSameTag>> _map2partition;

    public RectangleRepository() {
        _map2partition = new HashMap<GemColor,ArrayList<RetangleSetWithSameTag>>();
        for (GemColor c: GemColor.values())
            _map2partition.put(c,new ArrayList<RetangleSetWithSameTag>());
    }

    public ArrayList<RetangleSetWithSameTag> getPartition(GemColor offColor) {
        return _map2partition.get(offColor);
    }

    public void add(Rectangle r) {
        ArrayList<RetangleSetWithSameTag> partition = this.getPartition(r.getOffColor());
        ArrayList<RetangleSetWithSameTag> intersect = new ArrayList<RetangleSetWithSameTag>();
        for (RetangleSetWithSameTag p: partition) {
            if (p.intersects(r)) {
                intersect.add(p);
            }
        }
        if (intersect.isEmpty()) {
            int lbl = partition.size()+1;
            partition.add(new RetangleSetWithSameTag(lbl, r));
        }
        else {
            RetangleSetWithSameTag r0 = intersect.get(0);
            for (int i=1;i<intersect.size();i++) {
                RetangleSetWithSameTag ri = intersect.get(i);
                r0.merge(ri);
                partition.remove(ri);
            }
            r0.addIfIntersects(r);
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (GemColor c: GemColor.values()) {
            sb.append(String.format("OffColor: %s\n", c));
            ArrayList<RetangleSetWithSameTag> partition = this.getPartition(c);
            for (RetangleSetWithSameTag p: partition) {
                sb.append(p.toString());
            }
        }
        return sb.toString();
    }

}

class ComponentRepository {
    private int _bigonsMaxSize;
    private ArrayList<Component> _maxBigons;
    private ArrayList<Component> _blobs;
    private ArrayList<Component> _restOfComponents;
    private ArrayList[] _color2bigons;

    public ComponentRepository() {
        _bigonsMaxSize = 0;
        _maxBigons = new ArrayList<Component>();
        _restOfComponents = new ArrayList<Component>();
        _blobs = new ArrayList<Component>();
        _color2bigons = new ArrayList[] {new ArrayList<Component>(), new ArrayList<Component>(),
                        new ArrayList<Component>(), new ArrayList<Component>()};
    }

    public ArrayList<Component> getBigons(GemColor c1, GemColor c2) {
        ArrayList<Component> list = new ArrayList<Component>();

        int colorSet = GemColor.getColorSet(c1,c2);

        for (Component c: _maxBigons) {
            if (c.getColorSet() == colorSet)
                list.add(c);
        }
        for (Component c: _restOfComponents) {
            if (c.getColorSet() == colorSet)
                list.add(c);
        }

        return list;
    }

    public ArrayList<Component> getBigons() {
        ArrayList<Component> list = new ArrayList<Component>();
        for (Component c: _maxBigons) {
            list.add(c);
        }
        for (Component c: _restOfComponents) {
            if (c.isBigon())
                list.add(c);
        }
        return list;
    }



    public Component getAnyLargestBigonWithoutColor(GemColor c) {
        Component result = null;
        HashSet<Component> all = new HashSet<Component>(_maxBigons);
        all.addAll(_restOfComponents);
        for (Component b: all) {
            if (!b.isBigon() || b.hasColor(c))
                continue;
            if (result == null) {
                result = b;
            }
            else if (result.size() < b.size()) {
                result = b;
            }
        }
        return result;
    }

    public Component getAnyLargestBigonWithoutColorAndWithVerticeIn(GemColor c, HashSet set) {
        Component result = null;
        HashSet<Component> all = new HashSet<Component>(_maxBigons);
        all.addAll(_restOfComponents);
        for (Component b: all) {
            if (!b.isBigon() || b.hasColor(c) || !set.contains(b.getVertex()))
                continue;
            if (result == null) {
                result = b;
            }
            else if (result.size() < b.size()) {
                result = b;
            }
        }
        return result;
    }

    public ArrayList<Component> getTriballsOn(GemColor c1, GemColor c2, GemColor c3) {
        ArrayList<Component> result = new ArrayList<Component>();
        int colorSet = GemColor.getColorSet(c1,c2,c3);
        for (Component t: _restOfComponents) {
            if (!t.isTriball() || t.getColorSet() != colorSet)
                continue;
            result.add(t);
        }
        for (Component t: _blobs) {
            if (!t.isTriball() || t.getColorSet() != colorSet)
                continue;
            result.add(t);
        }
        return result;
    }

    public ArrayList<Component> getBigons(GemColor color) {
        ArrayList<Component> result;
        if (color == GemColor.yellow) {
            result = _color2bigons[0];
        } else if (color == GemColor.blue) {
            result = _color2bigons[1];
        } else if (color == GemColor.red) {
            result = _color2bigons[2];
        } else if (color == GemColor.green) {
            result = _color2bigons[3];
        }
        else throw new RuntimeException();
        /*
        System.out.println("Bigons of color: "+color);
        for (Component c: result) {
            System.out.print("Bigon "+c.getLabel()+" ("+c.getColors()[0]+" "+c.getColors()[1]+") -> ");
            for (GemVertex v: c.getVerticesFromBigon()) {
                System.out.print(""+v.getLabel()+" ");
            }
            System.out.println();
        }*/
        return result;
    }

    public void add(Component c) {
        if (c.isBigon()) {
            int size = c.size();
            if (size == _bigonsMaxSize)
                _maxBigons.add(c);
            else if (size > _bigonsMaxSize) {
                _restOfComponents.addAll(_maxBigons);
                _maxBigons.clear();
                _maxBigons.add(c);
                _bigonsMaxSize = size;
            }
            else _restOfComponents.add(c);

            for (GemColor color: c.getColors()) {
                if (color == GemColor.yellow) { _color2bigons[0].add(c); }
                else if (color == GemColor.blue) { _color2bigons[1].add(c); }
                else if (color == GemColor.red) { _color2bigons[2].add(c); }
                else if (color == GemColor.green) { _color2bigons[3].add(c); }
            }
        }
        else if (c.isBlob())
            _blobs.add(c);
        else _restOfComponents.add(c);
    }
    public int getNumberOfBlobs() {
        return _blobs.size();
    }
    public ArrayList<Component> getBlobs() {
        return _blobs;
    }
    public HashSet<GemVertex> getVerticesOnMaximalBigons() {
        HashSet<GemVertex> result = new HashSet<GemVertex>();
        for (Component c: _maxBigons)
            result.addAll(c.getVertices());
        return result;
    }




    /**
     * @todo Esta rotina parece que não está gerando
     * todas as possibilidades de enraizamento, pois
     * ao tentar gerar todas as permutações que dão
     * o código, para montar as strings, algumas
     * ficaram faltando.
     *
     * @return ArrayList
     */
    public ArrayList<GemVertexAndColorPermutation> getPossibleRoots() {
        int _maxSecondBigonSize = 0;

        // System.out.println("Max Bigon Size: "+_bigonsMaxSize);

        ArrayList<GemVertexAndColorPermutation> result = new ArrayList<GemVertexAndColorPermutation>();
        for (Component c: _maxBigons) {
            ArrayList<GemVertex> vs = c.getVerticesFromBigon();
            GemColor[] colors = c.getColors();
            int colorSet = GemColor.getColorSet(colors);
            int complementColorSet = GemColor.getComplementColorSet(colors);
            GemColor[] complementColors = GemColor.getColorsOfColorSet(complementColorSet);
            int n = vs.size();

            // test colors to match left to right direction
            if (vs.get(0).getNeighbour(colors[0]) != vs.get(1)) {
                GemColor aux = colors[0];
                colors[0] = colors[1];
                colors[1] = aux;
            }

            // System.out.println("Colors            "+colors[0]+","+colors[1]);
            // System.out.println("Complement Colors "+complementColors[0]+","+complementColors[1]);

            //
            for (int i=0;i<n;i++) {
                GemVertex vi = vs.get(i);

                // ATENÇÃO: prestar atenção que a cor 0 e 1 da permutação
                // troca a medida que i anda muda de paridade. Note o
                // +i na hora de definir a permutação de new PossibleRoot
                // nas posições 0 e 1 no código abaixo.

                // System.out.println("Trying vertex "+vi+" "+Tests._ORIGINAL_LABEL.get(vi));

                // vertex i colorSet component
                Component ci = vi.getComponent(colorSet);

                int kList[] = {0, 1};
                for (int k: kList) {

                    int delta = 2*k - 1; //    -1 if k==0     1 if k==1

                    // try vertex i as number 1, and consider
                    // left to right permutation (color 0 connects
                    // vs[i] to vs[i+1], and color 1 connects vs[i]
                    // to vs[i-1]).
                    int last = mod(i + delta, n);
                    while (true) {
                        GemVertex u = vs.get(last);

                        // get components of outgoing vertices
                        GemVertex u0 = u.getNeighbour(complementColors[0]);
                        GemVertex u1 = u.getNeighbour(complementColors[1]);
                        Component comp0 = u0.getComponent(colorSet);
                        Component comp1 = u1.getComponent(colorSet);
                        int size0 = comp0.size();
                        int size1 = comp1.size();

                        // System.out.println(String.format("Last vertex: %d    %s -> %d  %s -> %d",
                        //                                 Tests._ORIGINAL_LABEL.get(u), complementColors[0],
                        //                                 Tests._ORIGINAL_LABEL.get(u0), complementColors[1],
                        //                                 Tests._ORIGINAL_LABEL.get(u1)));

                        int size = -1;
                        int[] ccList = null;
                        if (comp0 != ci && comp1 != ci) { // found the way out through complementColor[0] and complementColor[1]
                            if (size1 > size0) {
                                size = size1;
                                ccList = new int[] {1};
                            } else if (size0 > size1) {
                                size = size0;
                                ccList = new int[] {0};
                            } else { // if (size0 == size1) {
                                size = size0;
                                ccList = new int[] {0, 1};
                            }
                        } else if (comp0 != ci) {
                            size = size0;
                            ccList = new int[] {0,1};
                        } else if (comp1 != ci) {
                            size = size1;
                            ccList = new int[] {0,1};
                        }

                        // store the way out
                        if (size == _maxSecondBigonSize) {
                            for (int cc : ccList) {
                                GemVertexAndColorPermutation pr = new GemVertexAndColorPermutation(vi, new GemColor[] {
                                                                   colors[(k + i) % 2],
                                                                   colors[(k + 1 + i) % 2],
                                                                   complementColors[cc],
                                                                   complementColors[(cc + 1) % 2]});
                                // System.out.println(size+"  -> "+pr.toString());
                                result.add(pr);
                            }
                            break;
                        } else if (size > _maxSecondBigonSize) {
                            result.clear();
                            for (int cc : ccList) {
                                GemVertexAndColorPermutation pr = new GemVertexAndColorPermutation(vi, new GemColor[] {
                                                                   colors[(k + i) % 2],
                                                                   colors[(k + i + 1) % 2],
                                                                   complementColors[cc],
                                                                   complementColors[(cc + 1) % 2]});
                                // System.out.println(size+"  -> "+pr.toString());
                                result.add(pr);
                            }
                            _maxSecondBigonSize = size;
                            break;
                        }

                        // if did not find a way out then all vertices are on the same
                        // bigon. then the first row of the code will be fixed. We must
                        // test everybody because we do not analyse this case...
                        if (last == mod(i - delta, n)) {
                            result.add(new GemVertexAndColorPermutation(vi, new GemColor[] {colors[0], colors[1], complementColors[0],complementColors[1]}));
                            result.add(new GemVertexAndColorPermutation(vi, new GemColor[] {colors[1], colors[0], complementColors[0],complementColors[1]}));
                            result.add(new GemVertexAndColorPermutation(vi, new GemColor[] {colors[0], colors[1], complementColors[1],complementColors[0]}));
                            result.add(new GemVertexAndColorPermutation(vi, new GemColor[] {colors[1], colors[0], complementColors[1],complementColors[0]}));
                            break;
                        }

                        last = mod(last + 2*delta, n);
                    }
                }
            }
        }
        return result;
    }

    private int mod(int value, int n) {
        value = value % n;
        if (value < 0)
            value = n + value;
        return value;
    }

    public ArrayList<Component> getTriballs() {
        ArrayList<Component> result = new ArrayList<Component>();
        for (Component t: _restOfComponents) {
            if (!t.isTriball())
                continue;
            result.add(t);
        }
        for (Component t: _blobs) {
            if (!t.isTriball())
                continue;
            result.add(t);
        }


        return result;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        ArrayList<Component> list = this.getTriballs();
        for (Component c: list) {
            sb.append(c.toString()+"\n");
        }
        return sb.toString();
    }

    public String toStringWithOriginalLabels() {
        StringBuffer sb = new StringBuffer();
        ArrayList<Component> list = this.getTriballs();
        for (Component c: list) {
            sb.append(c.toStringWithOriginalLabels()+"\n");
        }
        return sb.toString();
    }
}



class Component {
    Gem _gem;
    int _label;
    GemColor[] _colors;
    int _size;
    GemVertex _v;

    public Component(Gem gem, int label, GemVertex v, GemColor[] colors) {
        _gem = gem;
        _v = v;
        _label = label;
        _colors = colors;
    }

    public int getLabel() {
        return _label;
    }

    public GemVertex getVertex() {
        return _v;
    }

    public GemColor[] getColors() {
        return _colors;
    }

    public GemColor[] getComplementColors() {
        return GemColor.getColorsOfColorSet(GemColor.getComplementColorSet(_colors));
    }

    public boolean hasColor(GemColor c) {
        for (GemColor cc: _colors)
            if (cc == c)
                return true;
        return false;
    }
    public int getColorSet() {
        return GemColor.getColorSet(_colors);
    }
    public boolean isBigon() {
        return _colors.length == 2;
    }
    public boolean isTriball() {
        return _colors.length == 3;
    }
    public boolean isBlob() {
        return _colors.length == 3 && _size == 2;
    }
    public void setSize(int size) {
        _size = size;
    }
    public int size() {
        return _size;
    }
    public void findPossibleRoots() {
    }
    public ArrayList<GemVertex>  getVertices() {
        int cs = this.getColorSet();
        Component component = _v.getComponent(cs);
        ArrayList<GemVertex> result = new ArrayList<GemVertex>();
        for (GemVertex v: _gem.getVertices()) {
            if (v.getComponent(cs) == component)
                result.add(v);
        }
        return result;
    }
    public ArrayList<GemVertex> getVerticesFromBigon() {
        if (!isBigon())
            throw new RuntimeException("Not Implemented");

        ArrayList<GemVertex> result = new ArrayList<GemVertex>();
        GemVertex u = _v;
        GemColor c = _colors[0];
        while (true) {
            result.add(u);
            u = u.getNeighbour(c);
            if (u == _v)
                break;
            c = (c == _colors[0] ? _colors[1] : _colors[0]);
        }
        return result;
    }

    public String getBigonVerticesString() {
        if (!this.isBigon())
            throw new RuntimeException("OOoopppsss");
        StringBuffer sb = new StringBuffer();
        sb.append(String.format("bigon %d on colors %s %s - vertices: ",this.getLabel(),_colors[0],_colors[1]));
        for (GemVertex v: this.getVerticesFromBigon())
            sb.append(" "+v.getLabel());
        return sb.toString();
    }

    public String toStringWithOriginalLabels() {
        StringBuffer sb = new StringBuffer();
        sb.append(String.format("[%2d s%2d %3s] ",_label,_size,GemColor.getColorSetCompactString(_colors)));
        for (GemVertex v: this.getVertices()) {
            sb.append(" "+v.getOriginalLabel());
        }
        return sb.toString();
    }

    public String toStringWithLabels() {
        StringBuffer sb = new StringBuffer();
        sb.append(String.format("[%2d s%2d %3s] ",_label,_size,GemColor.getColorSetCompactString(_colors)));
        for (GemVertex v: this.getVertices()) {
            sb.append(" "+v.getLabel());
        }
        return sb.toString();
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(String.format("[%2d s%2d %3s] ",_label,_size,GemColor.getColorSetCompactString(_colors)));
        for (GemVertex v: this.getVertices()) {
            sb.append(" "+v);
        }
        return sb.toString();
    }}

class GemVertexAndColorPermutation {
    GemVertex _v;
    GemColor[] _permutation;
    public GemVertexAndColorPermutation(GemVertex v, GemColor[] permutation) {
        _v = v;
        _permutation = permutation;
    }
    public GemVertex getVertex() {
        return _v;
    }
    public GemColor[] getPermutation() {
        return _permutation;
    }
    public String toString() {
        return String.format("%s %s",_v,_permutation[0]+" "+_permutation[1]+" "+_permutation[2]+" "+_permutation[3]);
    }
}

class Bigon {
    int _label;
    int _colorSet;
    int _size;
    GemVertex _v;
    public Bigon(int label, GemVertex v, int size, int colorSet) {
        _v = v;
        _label = label;
        _colorSet = colorSet;
        _size = size;
    }
    public int size() {
        return _size;
    }
    public void findPossibleRoots() {
    }
}

class DipoleThickenning {
    private Dipole _dipole;
    private GemColor _color;
    public DipoleThickenning(Dipole d, GemColor c) {
        _dipole = d;
        _color = c;
    }
    public Dipole getDipole() {
        return _dipole;
    }
    public GemColor getColor() {
        return _color;
    }
    public String toString() {
        return String.format("Dipole Thickening at dipole {%s} on color %s", _dipole.toString(),""+_color);
    }
}

/**
 * There existed a dipole like this:
 *
 * ev1 <-- ec1 --> nl1 <-- comlement(ec1,ec2) --> nl2 <-- ec1 --> ?
 * ev2 <-- ec2 --> nl1 <-- comlement(ec1,ec2) --> nl2 <-- ec2 --> ?
 */
class InverseDipole {
    private int _newLabel1; // l
    private int _newLabel2;
    private int _edgeVertex1;
    private GemColor _edgeColor1;
    private int _edgeVertex2;
    private GemColor _edgeColor2;
    public InverseDipole(
            int edgeVertex1,
            GemColor edgeColor1,
            int edgeVertex2,
            GemColor edgeColor2,
            int newLabel1,
            int newLabel2) {
        _edgeVertex1 = edgeVertex1;
        _edgeColor1 = edgeColor1;
        _edgeVertex2 = edgeVertex2;
        _edgeColor2 = edgeColor2;
        _newLabel1 = newLabel1;
        _newLabel2 = newLabel2;
    }
    public int getEdgeVertex1() { return _edgeVertex1; }
    public int getEdgeVertex2() { return _edgeVertex2; }
    public GemColor getEdgeColor1() { return _edgeColor1; }
    public GemColor getEdgeColor2() { return _edgeColor2; }
    public int getNewLabel1() { return _newLabel1; }
    public int getNewLabel2() { return _newLabel2; }
    public int getEvenNewLabel() {
        if (_newLabel1 % 2 == 0) return _newLabel1;
        else if (_newLabel2 % 2 == 0) return _newLabel2;
        else throw new RuntimeException();
    }
    public int getOddNewLabel() {
        if (_newLabel1 % 2 == 1) return _newLabel1;
        else if (_newLabel2 % 2 == 1) return _newLabel2;
        else throw new RuntimeException();
    }


    public String toString() {
        return String.format("Inverse Dipole edges %d %s and %d %s new vertices %d %d",
                             _edgeVertex1,_edgeColor1,
                             _edgeVertex2,_edgeColor2,
                             _newLabel1, _newLabel2);
    }
}

class DipoleNarrowing {
    Dipole _dipole;
    GemVertex _uu;
    GemVertex _vv;
    GemColor _color; // sharpening color
    public DipoleNarrowing(Dipole d, GemVertex uu, GemVertex vv, GemColor c) {
        _dipole = d;
        _uu = uu;
        _vv = vv;
        _color = c;
    }
    public String toString() {
        return String.format("Dipole Sharpenning at dipole {%s} on color %s with vertices %d %d", _dipole.toString(),""+_color,_uu.getLabel(),_vv.getLabel());
    }
    public GemColor getColor() { return _color; }
    public Dipole getDipole() { return _dipole; }
    public GemVertex getUU() { return _uu; }
    public GemVertex getVV() { return _vv; }
}

class MinusCylinder {
    private GemVertex _u;
    private GemVertex _v;
    GemColor _c1;
    GemColor _c2;
    public MinusCylinder(GemVertex u, GemVertex v, GemColor c1, GemColor c2) {
        _u = u;
        _v = v;
        _c1 = c1;
        _c2 = c2;
    }
    public GemVertex getU() { return _u; }
    public GemVertex getV() { return _v; }
    public GemColor getC1() { return _c1; }
    public GemColor getC2() { return _c2; }
    public String toString() {
        return String.format(
                        "Minus Cylinder %d%d at vertices %3d %3d ",
                        _c1.getNumber(),
                        _c2.getNumber(),
                        _u.getLabel(),
                        _v.getLabel());
    }
}



/**
 * u - _colors[0] - u1 - _colors[1] - u2 - _colors[2] - u3 - Color.yellow - u
 */
class QuadColor {
    GemVertex _u;
    GemColor[] _colors; // three colors
    public QuadColor(GemVertex u, GemColor[] colors) {
        _u = u;
        _colors = colors;
    }
    public GemVertex getU() {
        return _u;
    }
    public String toString() {
        GemVertex u1 = _u.getNeighbour(_colors[0]);
        GemVertex u2 = u1.getNeighbour(_colors[1]);
        GemVertex u3 = u2.getNeighbour(_colors[2]);
        GemVertex u4 = u3.getNeighbour(GemColor.yellow);
        return String.format("QuadColor %3d %s %3d %s %3d %s %3d %s %3d",
                             _u.getLabel(),
                             GemColor.getColorsCompactString(_colors[0]),
                             u1.getLabel(),
                             GemColor.getColorsCompactString(_colors[1]),
                             u2.getLabel(),
                             GemColor.getColorsCompactString(_colors[2]),
                             u3.getLabel(),
                             GemColor.getColorsCompactString(GemColor.yellow),
                             u4.getLabel() );
    }
}


class Antipole {
    GemVertex _u;
    GemVertex _v;
    GemColor _color;
    public Antipole(GemVertex u, GemVertex v, GemColor color) {
        _u = u;
        _v = v;
        _color = color;
    }
    public GemVertex getU() {
        return _u;
    }
    public GemVertex getV() {
        return _v;
    }
    public GemColor getColor() {
        return _color;
    }
    public String toString() {
        return String.format("%s-antipole on %3d %3d",
                             (_color == GemColor.blue ? "1" :
                              _color == GemColor.red ? "2" : "3"),
                             _u.getLabel(),
                             _v.getLabel());
    }
}



class Dipole {
    GemVertex _u;
    GemColor[] _colors; // sharpening color
    public Dipole(GemVertex u, GemColor ... colors) {
        _u = u;
        _colors = colors;
    }
    public int getComplementColorSet() {
        return GemColor.getComplementColorSet(_colors);
    }
    public int getColorSet() {
        return GemColor.getColorSet(_colors);
    }

    public InverseDipole getInverseDipole() {
        if (this.size() != 2)
            throw new RuntimeException("Not implemented");

        GemColor[] cc = this.getComplementColors();

        GemVertex evenV = (this.getU().getLabel() % 2 == 0 ? this.getU() : this.getV());

        int ev1 = evenV.getNeighbour(cc[0]).getLabel();
        GemColor ec1 = cc[0];
        int ev2 = evenV.getNeighbour(cc[1]).getLabel();
        GemColor ec2 = cc[1];
        int nv1 = evenV.getLabel();
        int nv2 = evenV.getNeighbour(this.getColors()[0]).getLabel();
        return new InverseDipole(ev1,ec1,ev2,ec2,nv1,nv2);
    }

    public GemColor[] getColors() { return _colors; }
    public GemColor[] getComplementColors() { return GemColor.getColorsOfColorSet(this.getComplementColorSet()); }
    public GemVertex getU() { return _u; }
    public GemVertex getV() { return _u.getNeighbour(_colors[0]); }
    public int size() {
        return _colors.length;
    }
    public String toString() {
        String st = "";
        boolean first = true;
        for (GemColor c: _colors) {
            if (!first)
                st +="+";
            st+=c;
            first = false;
        }
        return String.format("%d-Dipole on vertices %d %d with colors %s",
                             this.size(),getU().getLabel(),getV().getLabel(),st);
    }

    public String toStringWithOriginalLabels() {
        String st = "";
        boolean first = true;
        for (GemColor c: _colors) {
            if (!first)
                st +="+";
            st+=c;
            first = false;
        }
        return String.format("%d-Dipole on vertices %d %d with colors %s",
                             this.size(),getU().getOriginalLabel(),getV().getOriginalLabel(),st);
    }

}

class Monopole {
    GemVertex _u;
    GemColor _color; // sharpening color
    int _sizeB1;
    int _sizeB2;
    public Monopole(GemVertex u, GemColor color, int sizeB1, int sizeB2) {
        _u = u;
        _color = color;
        _sizeB1 = sizeB1;
        _sizeB2 = sizeB2;
    }
    public GemColor getColor() { return _color; }
    public GemVertex getVertex() { return _u; }
    public int getColorSetBigon1() { return GemColor.getColorSet(GemColor.yellow,_color); }
    public int getColorSetBigon2() { return GemColor.difference(GemColor.COLORSET_ALL_COLORS,getColorSetBigon1()); }
    public GemColor[] getColorsBigon1() { return new GemColor[]{GemColor.yellow,_color}; }
    public GemColor[] getColorsBigon2() { return GemColor.getComplementColors(GemColor.yellow,_color); }
    public Component getBigon1() { return _u.getComponent(getColorSetBigon1()); }
    public Component getBigon2() { return _u.getComponent(getColorSetBigon2()); }
    public int getSizeBigon1() { return _sizeB1; }
    public int getSizeBigon2() { return _sizeB2; }
    public String toString() {
        return String.format("Monopole on vertice %d and color %s (sizes: %d and %d)" ,_u.getLabel(),_color,_sizeB1,_sizeB2);
    }

    public String toStringWithOriginalLabels() {
        return String.format("Monopole on vertice %d and color %s (sizes: %d and %d)",_u.getOriginalLabel(),_color,_sizeB1,_sizeB2);
    }

}

class Residue {
    ArrayList<GemColor> _colors = new ArrayList<GemColor>();
    ArrayList<GemVertex> _vertices =  new ArrayList<GemVertex>();

    public Residue(GemColor ... colors) {
        for (GemColor c: colors)
            _colors.add(c);
    }

    public boolean contains(GemVertex v) {
        return _vertices.contains(v);
    }

    public void addVertex(GemVertex v) {
        _vertices.add(v);
    }

    public ArrayList<GemVertex> getVertices() {
        return _vertices;
    }

    public void write() {
        synchronized(System.out) {
            System.out.print("Vertices: ");
            for (GemVertex v : _vertices)
                System.out.print(v.getLabel() + " ");
            System.out.println();
        }
    }

    public void writeAll() {
        synchronized(System.out) {
            System.out.println("-------");
            System.out.print("Residue Colors: ");
            for (GemColor c : _colors)
                System.out.print(c + " ");
            System.out.println();
            System.out.print("Vertices: ");
            for (GemVertex v : _vertices)
                System.out.print(v.getLabel() + " ");
            System.out.println();
        }
    }

    public void writeHeader() {
        synchronized(System.out) {
            System.out.println("-------");
            System.out.print("Residue Colors: ");
            for (GemColor c : _colors)
                System.out.print(c + " ");
            System.out.println();
        }
    }
}



class Permutation implements Comparable {
    private int[] _p;

    public Permutation(int n) {
        _p = new int[n];
        for (int i=0;i<_p.length;i++) {
            _p[i] = i+1;
        }
    }

    public Permutation(int[] p) {
        _p = new int[p.length];
        for (int i=0;i<_p.length;i++) {
            _p[i] = p[i];
        }
    }

    public int size() {
        return _p.length;
    }

    public void setImage(int x, int y) {
        _p[x-1] = y;
    }

    public int getImage(int x) {
        return _p[x-1];
    }

    public int getPreImage(int y) {
        for (int i=0;i<this.size();i++) {
            if (y ==_p[i]) {
                return i+1;
            }
        }
        throw new RuntimeException();
    }

    public boolean isInvolution() {
        for (int x=1;x<=this.size();x++) {
            int y = getImage(x);
            if (getImage(y) != x)
                return false;
        }
        return true;
    }

    public void apply(Permutation p) {
        int[] newp = new int[this.size()];
        for (int i=1;i<=_p.length;i++) {
            newp[i-1] = p.getImage(this.getImage(i));
        }
        _p = newp;
    }

    public Permutation copy() {
        Permutation copy = new Permutation(_p);
        return copy;
    }

    public static Permutation compose(Permutation p1, Permutation p2) {
        Permutation result = p1.copy();
        result.apply(p2);
        return result;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        boolean first = true;
        for (int i=0;i<this.size();i++) {
            if (!first) {
                sb.append(",");
            }
            sb.append(_p[i]);
            first = false;
        }
        return sb.toString();
    }

    public int hashCode() {
        int sum = 0;
        for (int i=1;i<=this.size();i+=3)
            sum+=this.getImage(i);
        return sum;
    }

    public boolean equals(Object o) {
        return this.compareTo(o) == 0;
    }

    public int compareTo(Object o) {
        Permutation p = (Permutation) o;
        int r = this.size()-p.size();
        int n = this.size();
        int i=1;
        while (r == 0 && i <= n) {
            r = this.getImage(i)-p.getImage(i);
            i++;
        }
        return r;
    }

}

class DifferenceToS3 {
    BGraph _graph;
    GemColor _c1;
    GemColor _c2;
    ArrayList<BGraphEdge> _edges;
    public DifferenceToS3(BGraph graph, GemColor c1, GemColor c2, ArrayList<BGraphEdge> edges) {
        _graph = graph;
        _c1 = c1;
        _c2 = c2;
        _edges = edges;
    }

    public int getNumberOfComponents() { return _graph.getNumVertices()-_edges.size(); }
    public GemColor getColor1() { return _c1; }
    public GemColor getColor2() { return _c2; }
    public String toString() {
        StringBuffer b = new StringBuffer();
        b.append(String.format("Set of %3d independent twistors/antipoles to get S3\n",_edges.size()));
        for (BGraphEdge e: _edges) {
            b.append(e.getKey().toString()+"\n");
        }
        b.append(String.format("#V = %3d    #E = %3d    Colors: %-6s %-6s\n",
                               _graph.getNumVertices(),
                               _graph.getNumEdges(),
                               _c1.toString(),
                               _c2.toString()));
        b.append(String.format("Reduced to %3d bigons %s\n",
                               _graph.getNumVertices() - _edges.size(),
                               (_graph.getNumVertices() - _graph.getNumEdges()) == 1 ? "(S3)" : ""));
        return b.toString();
    }
}
