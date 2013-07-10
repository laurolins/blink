package blink;

import java.awt.Dimension;
import java.awt.HeadlessException;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;
import java.util.Stack;
import java.util.StringTokenizer;

import javax.swing.JFrame;

import org.jscience.mathematics.numbers.Complex;




/**
 * <p>
 * Title: GBlink
 * </p>
 * 
 * <p>
 * Description: As written on my thesis chapter, this is a GBFL with a partition
 * on the square edges on vertex-edge and face-edge.
 * </p>
 * 
 * <p>
 * Copyright: Copyright (c) 2013
 * </p>
 * 
 * <p>
 * Company:
 * </p>
 * 
 * @author Lauro Didier Lins
 * @version 1.0
 */
public class GBlink implements Comparable {

    /**
     * Blink representing S1xS2. equivalent to a blink without edges.
     */
    private static final GBlink _S1xS2Blink = new GBlink(new int[] {1,3,2,4},1);

    /**
     * A GBlink is represented by its vertex list. The overcrossing and
     * orientation of the squares should be stored on these vertices.
     */
    private ArrayList<GBlinkVertex> _vertices = new ArrayList<GBlinkVertex>();


    /**
     * Constructor from the Map. All g-edges of this GBlink are green.
     */
    public GBlink(MapWord mapWord) {
        this.createVerticesAndEdgesFromMapWord(mapWord);
    }

    /**
     * Constructor from the Map. All g-edges of this GBlink are green.
     * By default all g-edges are green.
     */
    private void createVerticesAndEdgesFromMapWord(MapWord mapWord) {
        HashMap<Integer, GBlinkVertex> map = new HashMap<Integer, GBlinkVertex>();
        for (int i = 1; i <= mapWord.size(); i++) {
            GBlinkVertex v = new GBlinkVertex();
            map.put(i, v);
        }
        for (int i = 1; i <= mapWord.size(); i++) {
            GBlinkVertex v = map.get(i);
            GBlinkVertex en = map.get(mapWord.getNeighbour(i, GBlinkEdgeType.edge));
            GBlinkVertex fn = map.get(mapWord.getNeighbour(i, GBlinkEdgeType.face));
            GBlinkVertex vn = map.get(mapWord.getNeighbour(i, GBlinkEdgeType.vertex));
            v.setNeighbours(en, fn, vn);
            v.setLabel(i);

            // set the overcross status: odd vertices are overcross
            // and even vertices are undercross.
            if (i % 2 == 1) v.setOvercross(true);
            else v.setOvercross(false);

            // add to vertex list
            _vertices.add(v);
        }
    }

    /**
     * Create g-blink given adjacency code part and colors code part.
     */
    public GBlink(String codeAndColors) {
        StringTokenizer st = new StringTokenizer(codeAndColors," ");
        String code = st.nextToken().trim();
        String colors = "";
        if (st.hasMoreTokens()) {
            colors = st.nextToken().trim();
        }
        MapWord m = new MapWord(code);
        createVerticesAndEdgesFromMapWord(m);
        int[] reds = Library.stringToIntArray(colors,",");
        for (int r: reds) {
            this.setColor(r,BlinkColor.red);
        }
    }


    /**
     * Create g-blink given adjacency code part and colors code part.
     */
    public void GBlinkXX(String adjacencyCodePart, String colorsCodePart) {
        MapWord m = new MapWord(adjacencyCodePart);
        createVerticesAndEdgesFromMapWord(m);
        int[] reds = Library.stringToIntArray(colorsCodePart,",");
        for (int r: reds) {
            this.setColor(r,BlinkColor.red);
        }
    }

    /**
     * Constructor from the Map labeling and the color of the g-edges.
     */
    public GBlink(int code[], int color) {
        this(new MapWord(code));
        this.setColor(color);
    }

    /**
     * Constructor from the Map labeling and the red g-edges
     */
    public GBlink(int code[], int reds[]) {
        this(new MapWord(code));
        this.setColor(reds);
    }

    /**
     * Constructor from a MapPackedWord. A more compressed MapWord.
     */
    public GBlink(MapPackedWord mpw) {
        this(new MapWord(mpw));
    }

    /**
     * Constructor from a list of vertices created outside this method.
     * Note that it is a unsecure constructor so we defined it as private.
     */
    private GBlink(ArrayList<GBlinkVertex> vertices) {
        _vertices.addAll(vertices);
    }

    /**
     * Construct a GBlink from the cyclic representation of a map.
     */
    public GBlink(int[][] blinkCR, int reds[]) {

        // set of red edges
        HashSet<Integer> redSet = new HashSet<Integer>();
        for (int r: reds)
            redSet.add(r);

        // connect edges
        HashMap<Integer,E> map = new HashMap<Integer,E>();
        for (int i=0;i<blinkCR.length;i++) {

            int n = blinkCR[i].length;
            if (n == 0)
                continue;

            GBlinkVertex vFirst = null;
            E eFirst = null;

            for (int j = 0; j < n; j++) {

                int lbl1 = blinkCR[i][j];

                int lbl2 = (j == blinkCR[i].length-1 ? blinkCR[i][0] : blinkCR[i][j+1]);

                E e1 = map.get(lbl1);
                if (e1 == null) {
                    e1 = new E(lbl1, (redSet.contains(lbl1) ? BlinkColor.red : BlinkColor.green));
                    map.put(lbl1, e1);
                }
                E e2 = map.get(lbl2);
                if (e2 == null) {
                    e2 = new E(lbl2, (redSet.contains(lbl2) ? BlinkColor.red : BlinkColor.green));
                    map.put(lbl2,e2);
                }

                GBlinkVertex v1 = e1.nextFree();
                GBlinkVertex v2 = e2.nextFree();

                if (vFirst == null) {
                    vFirst = v1;
                    eFirst = e1;
                }

                GBlinkVertex.setNeighbours(v1,v2,GBlinkEdgeType.edge); // red edge
                // System.out.println(e1.getLabel()+","+v1.getLabel()+" "+e2.getLabel()+","+v2.getLabel()+" "+EdgeType.edge);

                if (j == n-1) {
                    GBlinkVertex.setNeighbours(v2,vFirst,GBlinkEdgeType.face); // red edge
                    eFirst.setOrientation(v2,vFirst);
                    // System.out.println(e2.getLabel()+","+v2.getLabel()+" "+eFirst.getLabel()+","+vFirst.getLabel()+" "+EdgeType.face);
                }
                else {
                    GBlinkVertex v2n = e2.next(v2);
                    GBlinkVertex.setNeighbours(v2,v2n,GBlinkEdgeType.face); // red edge
                    e2.setOrientation(v2,v2n);
                    // System.out.println(e2.getLabel()+","+v2.getLabel()+" "+e2.getLabel()+","+v2n.getLabel()+" "+EdgeType.face);
                }

            }
        }


        // check reverse connections
        for (E e: map.values()) {
            e.defineSquare();
        }

        // go to label
        this.goToCodeLabelPreservingSpaceOrientation();
        // this.goToCodeLabelAndDontCareAboutSpaceOrientation();

        // set colors
        for (E e: map.values()) {
            int lbl = e.getVertex(0).getLabel();
            int edge = (lbl % 4 == 0 ? lbl/4 : lbl/4+1);
            this.setColor(edge,e.getColor());
        }

        // go to label
        this.goToCodeLabelPreservingSpaceOrientation();

    }


    /**
     * Constructor of a GBlink based on the Gauss code.
     * crossSeparator is a string with the symbols that separate crossings.
     * componentSeparator is a string with symbos that separate compoenents.
     */
    public GBlink(String gaussCode, String crossSeparator, String compoenentSeparator) {
        // count number of crossing and do some testing
        int minCrossing = Integer.MAX_VALUE;
        int maxCrossing = Integer.MIN_VALUE;
        int crossingsAppearanceOnGaussCodeCount = 0;

        // put into memory
        StringTokenizer st = new StringTokenizer(gaussCode,compoenentSeparator);
        ArrayList<ArrayList<Integer>> listOfComponents = new ArrayList<ArrayList<Integer>>();
        while (st.hasMoreTokens()) {
            String componentSt = st.nextToken();
            ArrayList<Integer> crossings  = new ArrayList<Integer>();
            listOfComponents.add(crossings);
            StringTokenizer st2 = new StringTokenizer(componentSt,crossSeparator);
            while (st2.hasMoreTokens()) {
                crossingsAppearanceOnGaussCodeCount++;
                int crossing = Integer.parseInt(st2.nextToken());
                int absCrossing = Math.abs(crossing);
                crossings.add(crossing);
                if (absCrossing < minCrossing) {
                    minCrossing = absCrossing;
                }
                if (absCrossing > maxCrossing) {
                    maxCrossing = absCrossing;
                }
            }
        }

        // check
        if (crossingsAppearanceOnGaussCodeCount % 2 == 1)
            throw new RuntimeException("Invalid Gauss Code: Odd number of crossings appearance");
        if (minCrossing != 1 || maxCrossing != crossingsAppearanceOnGaussCodeCount/2)
            throw new RuntimeException("The Gauss Code crossings labels must be from 1 to n");

        // num crossings
        int numCrossings = crossingsAppearanceOnGaussCodeCount/2;

        // create GBlinkVertex from 1 to
        HashMap<Integer, GBlinkVertex> map = new HashMap<Integer, GBlinkVertex>();
        for (int i = 1; i <= numCrossings*4; i++) {
            GBlinkVertex v = new GBlinkVertex();
            map.put(i, v);

            v.setLabel(i);
            _vertices.add(v);
        }

        // create crossings square
        for (int i = 1; i <= numCrossings*4; i+=4) {
            GBlinkVertex v1 = map.get(i);
            GBlinkVertex v2 = map.get(i+1);
            GBlinkVertex v3 = map.get(i+2);
            GBlinkVertex v4 = map.get(i+3);
            GBlinkVertex.setNeighbours(v1,v2,GBlinkEdgeType.face);
            GBlinkVertex.setNeighbours(v2,v3,GBlinkEdgeType.vertex);
            GBlinkVertex.setNeighbours(v3,v4,GBlinkEdgeType.face);
            GBlinkVertex.setNeighbours(v1,v4,GBlinkEdgeType.vertex);
        }

        // 0 if not used.
        // 1 if used odd diagonal
        // 2 if used even diagonal
        // 3 if used both
        int crossingsUsed[] = new int[numCrossings+1];
        boolean first = true;
        while (listOfComponents.size() > 0) {
            ArrayList<Integer> component = null;

            // first time, so choose the first component
            if (first) {
                component = listOfComponents.get(0);
                listOfComponents.remove(0);
            }
            else {
                int usedCrossing = -1;

                // find some used crossing
                for (int i=1;i<=numCrossings;i++) {
                    if (crossingsUsed[i]==1 || crossingsUsed[i]==2) {
                        usedCrossing = crossingsUsed[i];
                    }
                }

                if (usedCrossing == -1) {
                    component = listOfComponents.get(0);
                    listOfComponents.remove(0);
                }
                else {
                    for (int i=0;i<listOfComponents.size();i++) {
                        ArrayList<Integer> c = listOfComponents.get(0);
                        if (c.contains(usedCrossing) || c.contains(Math.abs(usedCrossing))) {
                            component = c;
                            listOfComponents.remove(i);
                        }
                    }

                    if (component == null)
                        throw new RuntimeException("Missing crossing (used only once): "+usedCrossing);

                    // cycle component to the usedCrossing be the first one!
                    while (true) {
                        int crossing = Math.abs(component.get(0));
                        if (usedCrossing != crossing) {
                            component.remove(0);
                            component.add(crossing);
                        }
                        else break;
                    }
                }
            }

            // get the first vertex
            int usedStateOfFirstCrossing = crossingsUsed[Math.abs(component.get(0))];
            boolean nextUseIsOddDiagonal = (usedStateOfFirstCrossing == 2 || usedStateOfFirstCrossing == 0) ? true : false;
            for (int i=0;i<component.size();i++) {
                int cA = component.get(i);
                int cB = component.get((i+1) % component.size());

                int kA = nextUseIsOddDiagonal ? 1 : 2;
                GBlinkVertex vA = map.get(4 * (Math.abs(cA) - 1) + kA);
                if (vA.getFlag()) {
                    vA = map.get(4 * (Math.abs(cA) - 1) + kA + 2);
                    if (vA.getFlag()) {
                        throw new RuntimeException("Diagonal already used twice already");
                    }
                }
                vA.setFlag(true);
                crossingsUsed[Math.abs(cA)] |= kA; // update turn on bit kA

                int kB = 0;
                switch (vA.getLabel() % 4) {
                case 0: kB = 3; break;
                case 1: kB = 2; break;
                case 2: kB = 1; break;
                case 3: kB = 0; break;
                }
                GBlinkVertex vB = map.get(4 * (Math.abs(cB) - 1) + kB);
                if (vB.getFlag()) {
                    throw new RuntimeException("Diagonal already used twice already");
                }
                vB.setFlag(true);
                crossingsUsed[Math.abs(cB)] |= (kB % 2 == 0 ? 2 : 1); // update turn on bit kA

                vA.setOvercross(cA > 0);
                vB.setOvercross(cB > 0);
                GBlinkVertex.setNeighbours(vA,vB,GBlinkEdgeType.edge);

                // change
                nextUseIsOddDiagonal = !nextUseIsOddDiagonal;
            }
        }

        // check
        for (GBlinkVertex v: _vertices) {
            if (v.getNeighbour(GBlinkEdgeType.edge) == null || !v.getFlag())
                throw new RuntimeException("Vertices with problems!!!");
        }


        // update labeling to the vertex 1 be the first before the
        // first crossing on the gauss code
        this.relabel(this.findVertex(3),new int[2*numCrossings]);

    }

    /**
     * The vertices of this GBlink. The list is the same as the internal list. Be
     * careful not to destroy the list.
     */
    public ArrayList<GBlinkVertex> getVertices() {
        return _vertices;
    }

    /**
     * The number of g-edges of this GBlink
     */
    public int getNumberOfGEdges() {
        return _vertices.size() / 4;
    }

    /**
     * The number of vertices
     */
    public int getNumGBlinkVertices() {
        return _vertices.size();
    }

    /**
     * Create a copy of the GBlink.
     */
    public GBlink copy() {
        GBlink result = new GBlink(this.getMapWord());
        for (int i=1;i<=this.getNumberOfGEdges();i++) {
            result.setColor(i,this.getColor(i));
        }
        return result;
    }

    /**
     * Get color of the i-th edge (edges are numbered from 1 to n).
     * Find the vertex that is the base of the "square" (g-edge).
     * The label of vertices of g-edge "k" are labeled
     *    4(k-1)+1,4(k-1)+2,4(k-1)+3,4k
     */
    public BlinkColor getColor(int edgeLabel) {
        GBlinkVertex v = this.findVertex(4*(edgeLabel-1)+1);
        if (v.overcross()) return BlinkColor.green;
        else return BlinkColor.red;
    }

    /**
     * Get color of the i-th edge (edges are numbered from 1 to n).
     * Find the vertex that is the base of the "square" (g-edge).
     * The label of vertices of g-edge "k" are labeled
     *    4(k-1)+1,4(k-1)+2,4(k-1)+3,4k
     */
    public int getColorInAnInteger() {
        int result = 0;
        for (int i=0;i<this.getNumberOfGEdges();i++) {
            int c = (this.getColor(i+1) == BlinkColor.red ? 1 : 0);
            int k = (c << i);
            result = result + k;
        }
        return result;
    }


    /**
     * Calculates the number "g" of green edges and number of "r" of red edges.
     * Returns true r > g else returns false.
     */
    public boolean numberOfRedEdgesGreaterThanNumberOfGreenEdges() {
        int reds = 0;
        int greens = 0;
        for (int i=1;i<=this.getNumberOfGEdges();i++) {
            if (this.getColor(i) == BlinkColor.red) {
                reds++;
            }
            else greens++;
        }
        return reds > greens;
    }

    /**
     * Calculates the number "g" of green edges and number of "r" of red edges.
     * Returns true r > g else returns false.
     */
    public int getNumberOfRedGedges() {
        int result = 0;
        for (GBlinkVertex v: _vertices) {
            if (v.getLabel() % 4 == 1 && v.isRed())
                result++;
        }
        return result;
    }

    /**
     * Calculates the number "g" of green edges and number of "r" of red edges.
     * Returns true r > g else returns false.
     */
    public int getNumberOfGreenGedges() {
        int result = 0;
        for (GBlinkVertex v: _vertices) {
            if (v.getLabel() % 4 == 1 && v.isGreen())
                result++;
        }
        return result;
    }

    /**
     * All the g-edges have the same color?
     */
    public boolean isMonochromatic() {
        int n = this.getNumberOfGEdges();
        if (n == 0)
            return  true;
        BlinkColor c = this.getColor(1);
        for (int i=2;i<=n;i++)
            if (this.getColor(i) != c)
                return false;
        return true;
    }

    /**
     * Set the color of the g-edge with labeled "label" as being "c".
     */
    public void setColor(int label, BlinkColor c) {
        GBlinkVertex v1 = this.findVertex(4*(label-1)+1);
        GBlinkVertex v2 = v1.getNeighbour(GBlinkEdgeType.face);
        GBlinkVertex v3 = v1.getNeighbour(GBlinkEdgeType.diagonal);
        GBlinkVertex v4 = v1.getNeighbour(GBlinkEdgeType.vertex);
        if (c == BlinkColor.green) {
            v1.setOvercross(true);
            v3.setOvercross(true);
            v2.setOvercross(false);
            v4.setOvercross(false);
        }
        else {
            v1.setOvercross(false);
            v3.setOvercross(false);
            v2.setOvercross(true);
            v4.setOvercross(true);
        }
    }

    /**
     * Set the color of the g-edge with labeled "label" as being "c".
     */
    public void setColor(GBlinkVertex v, BlinkColor c) {
        GBlinkVertex v1 = (v.hasEvenLabel() ? v.getNeighbour(GBlinkEdgeType.face) : v);
        GBlinkVertex v2 = v1.getNeighbour(GBlinkEdgeType.face);
        GBlinkVertex v3 = v1.getNeighbour(GBlinkEdgeType.diagonal);
        GBlinkVertex v4 = v1.getNeighbour(GBlinkEdgeType.vertex);
        if (c == BlinkColor.green) {
            v1.setOvercross(true);
            v3.setOvercross(true);
            v2.setOvercross(false);
            v4.setOvercross(false);
        }
        else {
            v1.setOvercross(false);
            v3.setOvercross(false);
            v2.setOvercross(true);
            v4.setOvercross(true);
        }
    }

    /**
     * Set the color of the g-edges by the string of zeros and ones.
     * If the first char is 1 then the g-edge 1 is red else it is green,
     * and so on for 2,3,...,n.
     */
    public void setColor(String binary) {
        for (int i=0;i<binary.length();i++) {
            try {
                int k = Integer.parseInt("" + binary.charAt(i));
                if (k == 0) this.setColor(i+1,BlinkColor.green);
                else if (k == 1) this.setColor(i+1,BlinkColor.red);
            } catch (NumberFormatException ex) {
            }
        }
    }

    /**
     * Extract the color of all g-edges of the GBlink from an integer.
     * The first bit is 1 if the g-edge 1 is red otherwise it is green.
     * The secong bit is 1 if the g-edge 2 is red otherwise it is green,
     * and so on...
     */
    public void setColor(int c) {
        for (int i=0;i<this.getNumberOfGEdges();i++) {
            int k = (c >> i) & 0x01;
            setColor(i + 1, (k == 0 ? BlinkColor.green : BlinkColor.red));
            //System.out.println("cor: "+this.getColor(i+1));
        }
    }

    /**
     * Set the color from array of red edges
     */
    public void setColor(int reds[]) {
        for (int i=0;i<this.getNumberOfGEdges();i++) {
            this.setColor(i+1,BlinkColor.green);
        }
        for (int e: reds) {
            this.setColor(e,BlinkColor.red);
        }
    }

    /**
     * 1. swap "face" and "vertex" neighbour of every map-vertex.
     * 2. change the original parity.
     * 3. relabel map-vertex to become the lexicographically maximum (preserving parity).
     *
     * Note that all colours become changed automatically.
     */
    public void goToDual() {
        for (GBlinkVertex v: _vertices) {
            GBlinkVertex fn = v.getNeighbour(GBlinkEdgeType.face);
            GBlinkVertex vn = v.getNeighbour(GBlinkEdgeType.vertex);
            v.setNeighbour(vn,GBlinkEdgeType.face);
            v.setNeighbour(fn,GBlinkEdgeType.vertex);
        }

        // this is needed to keep the correct interpretation:
        // the face-edge cyclic ordering are from odd-vertex to even-vertex.
        this.changeParity();

        // recalculate the code
        this.goToCodeLabelPreservingSpaceOrientation();
    }

    /**
     * 1. swap "face" and "vertex" neighbour of every map-vertex.
     * 2. change over-under cross.
     * 3. relabel map-vertex to become the lexicographically maximum (preserving parity).
     *
     * Note that all colors become changed automatically.
     */
    public void goToRefDual() {
        for (GBlinkVertex v: _vertices) {
            v.setOvercross(!v.overcross());
            GBlinkVertex fn = v.getNeighbour(GBlinkEdgeType.face);
            GBlinkVertex vn = v.getNeighbour(GBlinkEdgeType.vertex);
            v.setNeighbour(vn,GBlinkEdgeType.face);
            v.setNeighbour(fn,GBlinkEdgeType.vertex);
        }

        // recalculate the code
        this.goToCodeLabelPreservingSpaceOrientation();
    }

    /**
     * 1. swap "face" and "vertex" neighbour of every map-vertex.
     * 2. change over-under cross.
     * 3. relabel map-vertex to become the lexicographically maximum (preserving parity).
     *
     * Note that all colors become changed automatically.
     */
    public void goToReflection() {
        for (GBlinkVertex v: _vertices) {
            v.setOvercross(!v.overcross());
        }

        // this is needed to keep the correct interpretation:
        // the face-edge cyclic ordering are from odd-vertex to even-vertex.
        this.changeParity();

        // recalculate the code
        this.goToCodeLabelPreservingSpaceOrientation();
    }


    /**
     * 1. swap "face" and "vertex" neighbour of every map-vertex.
     * 2. change over-under cross.
     * 3. relabel map-vertex to become the lexicographically maximum (preserving parity).
     *
     * Note that all colors become changed automatically.
     */
    public void swap(boolean parity, boolean overcross, boolean faceEdgeVertexEdge) {

        if (parity)
            this.changeParity();

        if (overcross)
            for (GBlinkVertex v : _vertices) {
                v.setOvercross(!v.overcross());
            }

        if (faceEdgeVertexEdge)
            for (GBlinkVertex v : _vertices) {
                GBlinkVertex fn = v.getNeighbour(GBlinkEdgeType.face);
                GBlinkVertex vn = v.getNeighbour(GBlinkEdgeType.vertex);
                v.setNeighbour(vn, GBlinkEdgeType.face);
                v.setNeighbour(fn, GBlinkEdgeType.vertex);
            }

        // recalculate the code
        this.goToCodeLabelPreservingSpaceOrientation();
    }


    /**
     * Get a new GBlink that it is the dual of this GBlink.
     */
    public GBlink dual() {
        GBlink dual = this.copy();
        dual.goToDual();
        return dual;
    }

    /**
     * Get a new GBlink that it is the dual of this GBlink.
     */
    public GBlink refDual() {
        GBlink refDual = this.copy();
        refDual.goToRefDual();
        return refDual;
    }

    /**
     * Get a new GBlink that it is the dual of this GBlink.
     */
    public GBlink reflection() {
        GBlink reflection = this.copy();
        reflection.goToReflection();
        return reflection;
    }

    /**
     * The blink must be in it's code labelling state, otherwise
     * the comparation may fail. Then replace this by its dual
     * if code labeling is smaller.
     */
    public void goToDualIfSmallerCode() {
        GBlink d = this.dual();
        if (this.compareTo(d) > 0)
            this.goToDual();
    }

    /**
     * The blink must be in it's code labelling state, otherwise
     * the comparation may fail. Then replace this by its dual
     * if code labeling is smaller.
     */
    public void goToRefDualIfSmallerCode() {
        GBlink d = this.refDual();
        if (this.compareTo(d) > 0)
            this.goToRefDual();
    }

    /**
     * The blink must be in it's code labelling state, otherwise
     * the comparation may fail. Then replace this by its dual
     * if code labeling is smaller.
     */
    public void goToReflectionIfSmallerCode() {
        GBlink d = this.reflection();
        if (this.compareTo(d) > 0)
            this.goToReflection();
    }

    /**
     * Constructor of the GBlink through the BlinkEntry.
     */
    public GBlink(BlinkEntry be) {
        this(new MapWord(be.get_mapCode()));
        this.setColor((int)be.get_colors());
        _blinkEntry = be;
    }

    /**
     * Get the cyclic representation of a GBlink.
     */
    public BlinkCyclicRepresentation getCyclicRepresentation() {
        this.loadVariables();
        int n = _varVertices.size();
        int[][] result = new int[n][];
        int k=0;
        for (Variable var: _varVertices) {
            ArrayList<GBlinkVertex> list = var.getVertices(); // oriented colckwise
            int e = list.size() / 2;
            result[k] = new int[e];
            for (int kk=0;kk<list.size();kk+=2)
                result[k][kk/2] = list.get(kk).getEdgeLabel();
            k++;
        }
        int reds[] = this.getRedEdgesLabels();


        return new BlinkCyclicRepresentation(result,reds);
    }

    /**
     * Get array with the g-edge labels of the red edges.
     */
    public int[] getRedEdgesLabels() {
        ArrayList<Integer> redEdges = new ArrayList<Integer>();
        for (int i=1;i<=this.getNumberOfGEdges();i++) {
            if (this.getColor(i) == BlinkColor.red) {
                redEdges.add(i);
            }
        }
        int[] result = new int[redEdges.size()];
        for (int i=0;i<redEdges.size();i++)
            result[i] = redEdges.get(i);

        return result;
    }

    /**
     * Auxiliar class representing g-edges to the
     * construction of a GBlink from a cyclic representation
     * of a map.
     */
    class E {
        BlinkColor _color;
        int _label;
        GBlinkVertex[] _vs = {new GBlinkVertex(),new GBlinkVertex(),new GBlinkVertex(),new GBlinkVertex()};
        int[] _orientation = {0,0,0,0};
        int _freeIndex = 0;

        public E(int label, BlinkColor c) {
            _color = c;
            _label = label;
            int k = 4*(_label-1)+1;
            _vs[0].setLabel(1);
            _vs[1].setLabel(2);
            _vs[2].setLabel(3);
            _vs[3].setLabel(4);
            _vertices.add(_vs[0]);
            _vertices.add(_vs[1]);
            _vertices.add(_vs[2]);
            _vertices.add(_vs[3]);
        }

        public int index(GBlinkVertex v) {
            if (v == _vs[0]) return 0;
            else if (v == _vs[1]) return 1;
            else if (v == _vs[2]) return 2;
            else if (v == _vs[3]) return 3;
            else throw new RuntimeException();
        }

        public void setOrientation(GBlinkVertex v1, GBlinkVertex v2) {
            int index1 = index(v1);
            int index2 = index(v2);
            _orientation[index1] = 1;
            _orientation[index2] = -1;
            // System.out.println("Orientation "+this.getLabel()+" -> "+_orientation[0]+" "+_orientation[1]+" "+_orientation[2]+" "+_orientation[3]);
        }

        public BlinkColor getColor() {
            return _color;
        }

        public GBlinkVertex nextFree() {
            GBlinkVertex v = _vs[_freeIndex];
            _freeIndex++;
            return v;
        }

        public GBlinkVertex getVertex(int index) {
            return _vs[index];
        }

        public GBlinkVertex next(GBlinkVertex v) {
            if (v == _vs[0]) return _vs[1];
            else if (v == _vs[1]) return _vs[2];
            else if (v == _vs[2]) return _vs[3];
            else if (v == _vs[3]) return _vs[0];
            else throw new RuntimeException();
        }

        public GBlinkVertex previous(GBlinkVertex v) {
            if (v == _vs[0]) return _vs[3];
            else if (v == _vs[1]) return _vs[0];
            else if (v == _vs[2]) return _vs[1];
            else if (v == _vs[3]) return _vs[2];
            else throw new RuntimeException();
        }

        public void defineSquare() {
            int i = 0;
            GBlinkVertex a = _vs[0]; GBlinkVertex b = _vs[1];
            GBlinkVertex c = _vs[2]; GBlinkVertex d = _vs[3];

            if (a.getNeighbour(GBlinkEdgeType.face) == b) {  // a is connected to b by a face edge
                if (_orientation[0] == _orientation[2]) {
                    GBlinkVertex.setNeighbours(b, c, GBlinkEdgeType.vertex);
                    GBlinkVertex.setNeighbours(a, d, GBlinkEdgeType.vertex);
                    // System.out.println("Connect "+this.getLabel()+" -> "+b.getLabel()+" <-> "+c.getLabel());
                    // System.out.println("Connect "+this.getLabel()+" -> "+a.getLabel()+" <-> "+d.getLabel());
                }
                else {
                    GBlinkVertex.setNeighbours(b, d, GBlinkEdgeType.vertex);
                    GBlinkVertex.setNeighbours(a, c, GBlinkEdgeType.vertex);
                    // System.out.println("Connect "+this.getLabel()+" -> "+b.getLabel()+" <-> "+d.getLabel());
                    // System.out.println("Connect "+this.getLabel()+" -> "+a.getLabel()+" <-> "+c.getLabel());
                }
            }
            else { // a is connected to d by a face edge
                if (_orientation[3] == _orientation[1]) {
                    GBlinkVertex.setNeighbours(a, b, GBlinkEdgeType.vertex);
                    GBlinkVertex.setNeighbours(c, d, GBlinkEdgeType.vertex);
                    // System.out.println("Connect "+this.getLabel()+" "+a.getLabel()+" <-> "+b.getLabel());
                    // System.out.println("Connect "+this.getLabel()+" "+c.getLabel()+" <-> "+d.getLabel());
                }
                else {
                    GBlinkVertex.setNeighbours(a, d, GBlinkEdgeType.vertex);
                    GBlinkVertex.setNeighbours(c, b, GBlinkEdgeType.vertex);
                    // System.out.println("Connect "+this.getLabel()+" "+a.getLabel()+" <-> "+d.getLabel());
                    // System.out.println("Connect "+this.getLabel()+" "+c.getLabel()+" <-> "+b.getLabel());
                }
            }
        }

        public int getLabel() {
            return _label;
        }

    }

    /**
     * Find the GBlinkVertex on the same zigzag of "v" with smallest label.
     */
    public GBlinkVertex getMinOddVertexOnTheSameZigzag(GBlinkVertex v) {
        GBlinkVertex result = null;

        GBlinkVertex u = v;
        GBlinkEdgeType t = GBlinkEdgeType.diagonal;
        while (true) {
            if (u.hasOddLabel()) {
                if (result == null) result = u;
                else if (u.getLabel() < result.getLabel()) result = u;
            }
            u = u.getNeighbour(t);

            if (u == v)
                break;

            t = (GBlinkEdgeType.diagonal == t ? GBlinkEdgeType.edge : GBlinkEdgeType.diagonal);
        }

        if (result == null)
            throw new RuntimeException("This should not happen!!");

        return result;
    }


    /**
     * Find GBlinkVertex with its incident angle edge tagged.
     */
    public ArrayList<GBlinkVertex> getVerticesWithTaggedAngles() {
        ArrayList<GBlinkVertex> result = new ArrayList<GBlinkVertex>();
        for (GBlinkVertex v: _vertices)
            if (v.hasAngleLabelDefined())
                result.add(v);
        return result;
    }

    /**
     * Sort a list of blinks
     */
    public static void sort(ArrayList<GBlink> list) {
        Collections.sort(list,new Comparator() {
            public int compare(Object o1, Object o2) {
                GBlink b1 = (GBlink) o1;
                GBlink b2 = (GBlink) o2;
                return b1.compareTo(b2);
            }
        });
    }

    // ----------------------------------------
    // first position
    private BlinkEntry _blinkEntry;
    public BlinkEntry getBlinkEntry() {
        return _blinkEntry;
    }
    // first position
    // ----------------------------------------

    // ----------------------------------------
    // A value associated to a GBlink

    /**
     * A value associated to a GBlink. It is used on the
     * Decomposition tree algorithm.
     */
    private int _value = -1;
    public int getValue() {
        return _value;
    }
    public boolean valueIsUndefined() {
        return _value == -1;
    }
    public void setValue(int value) {
        _value = value;
    }

    // first position
    // ----------------------------------------


    // ----------------------------------------
    // GBlink associated to this GBLink
    private GBlink _associatedBlink;
    public GBlink getAssociatedGBlink() {
        return _associatedBlink;
    }
    public void setAssociatedGBlink(GBlink g) {
        _associatedBlink = g;
    }
    // GBlink associated to this GBLink
    // ----------------------------------------

    /**
     * Get new GBlink with protected crossings
     */
    public GBlink getWithProtectedCrossings() {
        GBlink G = this.copy();
        G.protectCrossings();
        return G;
    }

    /**
     * Protect crossings 25/09/2006. Every crossing that separates
     * the same whiteface or the same blackface will be ptrotected.
     */
    public void protectCrossings() {
        ArrayList<GBlinkVertex> vertices = (ArrayList<GBlinkVertex>) _vertices.clone();
        for (GBlinkVertex v: vertices) {

            // is this vertex the base of a crossing?
            if (v.getLabel() % 4 != 1)
                continue;

            // vertices = v[1],v[2],v[3],v[4]
            GBlinkVertex originalVertices[] = {
                                null,
                                v,
                                v.getNeighbour(GBlinkEdgeType.face),
                                v.getNeighbour(GBlinkEdgeType.diagonal),
                                v.getNeighbour(GBlinkEdgeType.vertex)};

            // check if this crossing needs to be protected
            boolean protectThisCrossing = false;
            GBlinkBigon whiteFace = new GBlinkBigon(originalVertices[1],Variable.G_FACE);
            if (whiteFace.contains(originalVertices[2]) &&
                whiteFace.contains(originalVertices[3]) &&
                whiteFace.contains(originalVertices[4])) {
                protectThisCrossing = true;
            }

            // check other
            if (!protectThisCrossing) {
                GBlinkBigon blackFace = new GBlinkBigon(originalVertices[1],Variable.G_VERTICE);
                if (blackFace.contains(originalVertices[2]) &&
                    blackFace.contains(originalVertices[3]) &&
                    blackFace.contains(originalVertices[4])) {
                    protectThisCrossing = true;
                }
            }

            if (!protectThisCrossing)
                continue;

            // new vertices
            GBlinkVertex[][] newVertices = new GBlinkVertex[5][5];
            for (int i=1;i<=4;i++) {
                for (int j=1;j<=4;j++) {
                    newVertices[i][j] = new GBlinkVertex();
                    newVertices[i][j].setLabel( (j % 2) == 1 ? ((i+1) % 2) : (i % 2));
                    _vertices.add(newVertices[i][j]);
                    if (j % 2 == 1)
                        newVertices[i][j].setOvercross(true);
                    else
                        newVertices[i][j].setOvercross(false);
                }
                GBlinkVertex.setNeighbours(newVertices[i][1],newVertices[i][2],GBlinkEdgeType.face);
                GBlinkVertex.setNeighbours(newVertices[i][2],newVertices[i][3],GBlinkEdgeType.vertex);
                GBlinkVertex.setNeighbours(newVertices[i][3],newVertices[i][4],GBlinkEdgeType.face);
                GBlinkVertex.setNeighbours(newVertices[i][4],newVertices[i][1],GBlinkEdgeType.vertex);
            }
            GBlinkVertex.setNeighbours(newVertices[1][2], newVertices[2][2], GBlinkEdgeType.edge);
            GBlinkVertex.setNeighbours(newVertices[2][4], newVertices[3][4], GBlinkEdgeType.edge);
            GBlinkVertex.setNeighbours(newVertices[3][2], newVertices[4][2], GBlinkEdgeType.edge);
            GBlinkVertex.setNeighbours(newVertices[4][4], newVertices[1][4], GBlinkEdgeType.edge);

            // save angle edge neighbours
            GBlinkVertex aux[] = {
                                 null,
                                 originalVertices[1].getNeighbour(GBlinkEdgeType.edge),
                                 originalVertices[2].getNeighbour(GBlinkEdgeType.edge),
                                 originalVertices[3].getNeighbour(GBlinkEdgeType.edge),
                                 originalVertices[4].getNeighbour(GBlinkEdgeType.edge)
            };

            // set newVertices angle edge neighbours
            GBlinkVertex.setNeighbours(newVertices[1][1], originalVertices[1], GBlinkEdgeType.edge);
            GBlinkVertex.setNeighbours(newVertices[2][1], originalVertices[2], GBlinkEdgeType.edge);
            GBlinkVertex.setNeighbours(newVertices[3][1], originalVertices[3], GBlinkEdgeType.edge);
            GBlinkVertex.setNeighbours(newVertices[4][1], originalVertices[4], GBlinkEdgeType.edge);

            // angle edge from 1
            if (aux[1] == originalVertices[2])
                GBlinkVertex.setNeighbours(newVertices[1][3], newVertices[2][3], GBlinkEdgeType.edge);
            else if (aux[1] == originalVertices[4])
                GBlinkVertex.setNeighbours(newVertices[1][3], newVertices[4][3], GBlinkEdgeType.edge);
            else
                GBlinkVertex.setNeighbours(newVertices[1][3], aux[1], GBlinkEdgeType.edge);

            // angle edge from 2
            if (aux[2] == originalVertices[3])
                GBlinkVertex.setNeighbours(newVertices[2][3], newVertices[3][3], GBlinkEdgeType.edge);
            else if (aux[2] == originalVertices[1])
                ; // do nothing
            else
                GBlinkVertex.setNeighbours(newVertices[2][3], aux[2], GBlinkEdgeType.edge);

            if (aux[3] == originalVertices[2])
                ; // do nothing
            else if (aux[3] == originalVertices[4])
                GBlinkVertex.setNeighbours(newVertices[3][3], newVertices[4][3], GBlinkEdgeType.edge);
            else
                GBlinkVertex.setNeighbours(newVertices[3][3], aux[3], GBlinkEdgeType.edge);

            if (aux[4] == originalVertices[1])
                ; // do nothing
            else if (aux[4] == originalVertices[3])
                ; // do nothing
            else
                GBlinkVertex.setNeighbours(newVertices[4][3], aux[4], GBlinkEdgeType.edge);
        }

        //
        this.goToCodeLabelPreservingSpaceOrientation();

    }

    /**
     * Get MapWord array from the current labelling of the vertices.
     */
    public int[] getMapWordArray() {
        int[] c = new int[_vertices.size()/2];
        int i=0;
        for (GBlinkVertex v: _vertices) {
            if (v.getLabel() % 2 == 1)
                c[i++] = v.getNeighbour(GBlinkEdgeType.edge).getLabel()/2;
        }
        return c;
    }

    /**
     * Get MapWord object from the current labelling of the vertices.
     */
    public MapWord getMapWord() {
        return new MapWord(this.getMapWordArray());
    }

    /**
     * Log this GBlink state.
     */
    public void write() {
        for (GBlinkVertex v: _vertices) {
            if (v.getLabel() % 2 == 1)
                System.out.print(""+(v.getNeighbour(GBlinkEdgeType.edge).getLabel()/2)+",");
        }
        System.out.print("   ");
        for (int i=1;i<=getNumberOfGEdges();i++) {
            System.out.print(""+this.getColor(i)+",");
        }
        System.out.println();
    }

    /**
     * By hypothesis v1 and v2 are vertices with
     * odd labels on their original blink.
     * b2 must not be used any more
     */
    private static int _count = 1;
    public static void merge(GBlink b1, GBlinkVertex v1, GBlink b2, GBlinkVertex v2) {

        if (b1 == b2)
            return;

        /* // LOG
        ArrayList<MapD> list = new ArrayList<MapD>();
        list.add(new MapD(b1.copy()));
        list.add(new MapD(b2.copy()));

        // desenhar o mapa
        JFrame f = new JFrame("Merge..."+(_count));
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.setLocation(_count*10,_count*10);
        f.setSize(new Dimension(500,250));
        f.setContentPane(new DrawPanelMultipleMaps(list,1,3));
        f.setVisible(true);
        */

        if (b1 != b2)
            b1._vertices.addAll(b2._vertices);
        GBlinkVertex v1n = v1.getNeighbour(GBlinkEdgeType.edge);
        GBlinkVertex v2n = v2.getNeighbour(GBlinkEdgeType.edge);
        if (v1.labelParity() == v2.labelParity()) {
            GBlinkVertex.setNeighbours(v1,v2n,GBlinkEdgeType.edge);
            GBlinkVertex.setNeighbours(v2,v1n,GBlinkEdgeType.edge);
        }
        else {
            GBlinkVertex.setNeighbours(v1,v2,GBlinkEdgeType.edge);
            GBlinkVertex.setNeighbours(v1n,v2n,GBlinkEdgeType.edge);
        }

        b1.goToCodeLabelPreservingSpaceOrientation();

        /* // LOG
        list.clear();
        list.add(new MapD(b1.copy()));

        // desenhar o mapa
        f = new JFrame("Result of Merge..."+(_count));
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.setLocation(_count*10+500,_count*10);
        f.setSize(new Dimension(250,250));
        f.setContentPane(new DrawPanelMultipleMaps(list,1,1));
        f.setVisible(true);
        */

        _count++;

    }

    /**
     * MapWord is an integer array that defines a Map.
     * Once this method returns, all vertices have their
     * label updated to reflect the mapWord rooted at root.
     */
    public void relabel(GBlinkVertex root, int[] mapWord) {
        HashSet<GBlinkVertex> P = new HashSet<GBlinkVertex>(); // vertices ja processados
        Stack<GBlinkVertex> S = new Stack<GBlinkVertex>(); // vertices ja processados

        S.push(root);

        int k = 1;
        while (!S.empty()) {
            GBlinkVertex r1 = S.pop();

            // ja processado?
            if (P.contains(r1))
                continue;

            GBlinkVertex r2 = r1.getNeighbour(GBlinkEdgeType.face);
            GBlinkVertex r3 = r2.getNeighbour(GBlinkEdgeType.vertex);
            GBlinkVertex r4 = r3.getNeighbour(GBlinkEdgeType.face);

            r1.setLabel(k);
            r2.setLabel(k + 1);
            r3.setLabel(k + 2);
            r4.setLabel(k + 3);
            k = k + 4;

            // adicionar vertices ja processados
            P.add(r1);
            P.add(r2);
            P.add(r3);
            P.add(r4);

            // novos vertices para processar
            S.push(r2.getNeighbour(GBlinkEdgeType.edge));
            S.push(r4.getNeighbour(GBlinkEdgeType.edge));
        }

        for (GBlinkVertex v : _vertices) {
            int label = v.getLabel();
            if (label % 2 == 1) {
                mapWord[label / 2] = v.getNeighbour(GBlinkEdgeType.edge).getLabel() / 2;
            }
        }
    }

    /**
     * MapWord is an integer array that defines a Map.
     * Once this method returns, all vertices have their
     * label updated to reflect the mapWord rooted at root.
     */
    public void relabelWithColors(GBlinkVertex root, int[] mapWord, int colors[]) {
        HashSet<GBlinkVertex> P = new HashSet<GBlinkVertex>(); // vertices ja processados
        Stack<GBlinkVertex> S = new Stack<GBlinkVertex>(); // vertices ja processados

        S.push(root);

        int k = 1;
        while (!S.empty()) {
            GBlinkVertex r1 = S.pop();

            // ja processado?
            if (P.contains(r1))
                continue;

            GBlinkVertex r2 = r1.getNeighbour(GBlinkEdgeType.face);
            GBlinkVertex r3 = r2.getNeighbour(GBlinkEdgeType.vertex);
            GBlinkVertex r4 = r3.getNeighbour(GBlinkEdgeType.face);

            r1.setLabel(k);
            r2.setLabel(k + 1);
            r3.setLabel(k + 2);
            r4.setLabel(k + 3);
            k = k + 4;

            // adicionar vertices ja processados
            P.add(r1);
            P.add(r2);
            P.add(r3);
            P.add(r4);

            // novos vertices para processar
            S.push(r2.getNeighbour(GBlinkEdgeType.edge));
            S.push(r4.getNeighbour(GBlinkEdgeType.edge));
        }

        // number of red edges
        int numReds = 0;
        for (GBlinkVertex v : _vertices) {
            int label = v.getLabel();
            if (label % 2 == 1) {
                mapWord[label / 2] = v.getNeighbour(GBlinkEdgeType.edge).getLabel() / 2;
            }
            // to untie the game first it is the
            // number of reds then the label of
            // the first red edge.
            if (label % 4 == 1) {
                colors[(label - 1) / 4 +1] = (v.isGreen() ? 1 : 0);
                if (v.isRed())
                    numReds++;
            }
        }
        colors[0] = numReds;

    }



    /**
     * If the vertices of this map are originally labeled,
     * then relabel it to become the lexicographically maximum
     * labeling scheme preserving the original parity.
     */
    public int[] goToCodeLabelPreservingSpaceOrientationWithoutColors() {
        int n = (_vertices.size() / 2);
        int[] code = new int[n];
        int[] currentWord = new int[n];

        GBlinkVertex rootOfCode = null;

        boolean first = true;
        for (GBlinkVertex v : _vertices) {

            // only root at vertices which are originally odd.
            if (!v.hasOddLabel())
                continue;

            // after the following operation all labels might have changed
            // but it's parity were preserved.
            this.relabel(v, currentWord);

            if (first) {
                System.arraycopy(currentWord, 0, code, 0, n);
                first = false;
                rootOfCode = v;
            } else {

                // compare map
                boolean change = false;
                for (int i = 0; i < n; i++) {
                    if (code[i] < currentWord[i]) {
                        rootOfCode = v;
                        change = true;
                    } else if (code[i] > currentWord[i]) {
                        break;
                    }
                }

                if (change) {
                    System.arraycopy(currentWord, 0, code, 0, n);
                }
            }
        }

        // make it be on the code
        this.relabel(rootOfCode,currentWord);
        Collections.sort(_vertices);

        return code;
    }


    /**
     * Go to code label on the other orientation.
     */
    public int[] goToCodeLabelChangingSpaceOrientation() {
        this.changeParity();
        return goToCodeLabelPreservingSpaceOrientation();
    }

    /**
     * MapCode is a mapword that is the lexicographically maximum for this map.
     * The map changes to be at code...
     * The space induced must be the same as the
     * original or the space with the orientation changed
     */
    public int[] goToCodeLabelPreservingSpaceOrientation() {
        // preserve orientation
        return goToCodeLabel(true);
    }

    /**
     * MapCode is a mapword that is the lexicographically maximum for this map.
     * The map changes to be at code...
     * The space induced must be the same as the
     * original or the space with the orientation changed
     */
    public int[] goToCodeLabelAndDontCareAboutSpaceOrientation() {
        // dont care about orientation
        return goToCodeLabel(false);
    }

    /**
     * If the vertices of this map are originally labeled,
     * then relabel it to become the lexicographically maximum
     * labeling scheme preserving the original parity.
     * Take care of colors also that are lexicographically minimum.
     */
    public int[] goToCodeLabel(boolean preserveOrientation) {
        int n = (_vertices.size() / 2);
        int e = (_vertices.size() / 4);

        int[] code = new int[n];
        int[] colors = new int[e+1];
        int[] currentWord = new int[n];
        int[] currentColors = new int[e+1];

        GBlinkVertex rootOfCode = null;


        /*
        int numReds = 0;
        for (GBlinkVertex v : _vertices) {
            int label = v.getLabel();
            if (label % 4 == 1) {
                if (v.isRed())
                    numReds++;
            }
        }
        System.out.println("Starting with "+numReds);
        this.relabelWithColors(this.findVertex(1),code,colors);
        System.out.println("starting code:   "+Library.intArrayToString(code)+" "+Library.intArrayToString(colors)); */

        boolean first = true;
        for (GBlinkVertex v : _vertices) {

            // only root at vertices which are originally odd.
            // v may change label but only from odd to odd when
            // preserveOrientation == true. otherwise this test
            // is false
            if (preserveOrientation && !v.hasOddLabel())
                continue;

            // after the following operation all labels might have changed
            // but it's parity were preserved.
            this.relabelWithColors(v, currentWord, currentColors);

            if (first) {
                /*
                System.out.println("---------------------");
                System.out.println("First from");
                System.out.println("from: start");
                System.out.println("to:   "+Library.intArrayToString(currentWord)+" "+Library.intArrayToString(currentColors));
                System.out.println("---------------------"); */


                System.arraycopy(currentWord, 0, code, 0, n);
                System.arraycopy(currentColors, 0, colors, 0, e+1);
                first = false;
                rootOfCode = v;
            } else {

                // compare map
                Boolean change = null;
                for (int i = 0; i < n; i++) {
                    if (code[i] < currentWord[i]) {
                        rootOfCode = v;
                        change = true;
                        break;
                    } else if (code[i] > currentWord[i]) {
                        change = false;
                        break;
                    }
                }

                // check colors
                if (change == null) {
                    for (int i = 0; i < e+1; i++) {
                        if (currentColors[i] < colors[i]) {
                            rootOfCode = v;
                            change = true;
                            break;
                        } else if (currentColors[i] > colors[i]) {
                            change = false;
                            break;
                        }
                    }
                }

                if (change != null && change == true) {
                    /*
                    System.out.println("---------------------");
                    System.out.println("Change");
                    System.out.println("from: "+Library.intArrayToString(code)+" "+Library.intArrayToString(colors));
                    System.out.println("to:   "+Library.intArrayToString(currentWord)+" "+Library.intArrayToString(currentColors));
                    System.out.println("---------------------");
                    */
                    System.arraycopy(currentWord, 0, code, 0, n);
                    System.arraycopy(currentColors, 0, colors, 0, e+1);
                }
            }
        }

        // make it be on the code
        this.relabelWithColors(rootOfCode,currentWord,currentColors);
        Collections.sort(_vertices);

        return code;
    }




    /**
     * Find vertex by its label.
     */
    private int _lastFindIndex = -1;
    public GBlinkVertex findVertex(int label) {
        int n = _vertices.size();
        for (int i=0;i<n;i++) {
            _lastFindIndex = (_lastFindIndex + 1)% n;
            GBlinkVertex v = _vertices.get(_lastFindIndex);
            if (v.getLabel() == label)
                return v;
        }
        return null;
    }

    /**
     * Add a loop at vertex u.
     * @param u Vertex
     */
    public void addLoop(GBlinkVertex u) {
        GBlinkVertex v = u.getNeighbour(GBlinkEdgeType.edge);

        //    a     f     a     f     a
        // u --- a --- b --- c --- d --- v
        //       |     |     |     |
        //      v|     | ___ |     |
        //       |        v        |
        //       |_________________|

        GBlinkVertex a = new GBlinkVertex();
        GBlinkVertex b = new GBlinkVertex();
        GBlinkVertex c = new GBlinkVertex();
        GBlinkVertex d = new GBlinkVertex();

        // edge, face, vertex
        u.setNeighbour(a, GBlinkEdgeType.edge);
        a.setNeighbours(u, b, d);
        b.setNeighbours(c, a, c);
        c.setNeighbours(b, d, b);
        d.setNeighbours(v, c, a);
        v.setNeighbour(d, GBlinkEdgeType.edge);

        //
        _vertices.add(a);
        _vertices.add(b);
        _vertices.add(c);
        _vertices.add(d);
    }

    /**
     * Add isolated vertex from u.
     * @param u Vertex
     */
    public void addIsolatexVertex(GBlinkVertex u) {
        GBlinkVertex v = u.getNeighbour(GBlinkEdgeType.edge);

        //    a     f     a
        // u --- a --- b --- v
        //       |     |
        //     v |     | v
        //       |     |
        //       d --- c
        //       |  f  |
        //       | ___ |
        //          a

        GBlinkVertex a = new GBlinkVertex();
        GBlinkVertex b = new GBlinkVertex();
        GBlinkVertex c = new GBlinkVertex();
        GBlinkVertex d = new GBlinkVertex();

        // edge, face, vertex
        u.setNeighbour(a, GBlinkEdgeType.edge);
        a.setNeighbours(u, b, d);
        b.setNeighbours(v, a, c);
        c.setNeighbours(d, d, b);
        d.setNeighbours(c, c, a);
        v.setNeighbour(b, GBlinkEdgeType.edge);

        //
        _vertices.add(a);
        _vertices.add(b);
        _vertices.add(c);
        _vertices.add(d);
    }

    /**
     * Calculate HomologyGroup from Gem.
     */
    public HomologyGroup homologyGroupFromGem() {
        Gem gem = getGem();
        return gem.homologyGroup();
    }

    /**
     * Calculate the Gem for this GBlink.
     */
    public Gem getGem() {
        if (this.getNumberOfGEdges() == 0) {
            return _S1xS2Blink.getGem();
        }
        else {
            GemFromBlink gfb = new GemFromBlink(this);
            return gfb.getGem();
        }
    }

    /**
     * Calculate HomologyGroup purely from the GBlink.
     */
    public HomologyGroup homologyGroupFromGBlink() {
        this.loadVariables();
        int linkingMatrix[][] = this.linkingMatrix();
        Smith S = new Smith(new MatrixBI(linkingMatrix));
        MatrixBI R = S.getSmithNormalForm();
        HomologyGroup hg = new HomologyGroup();
        for (int k=0;k<R.getNumRows();k++) {
            BigInteger d = R.get(k, k);
            if (d.compareTo(BigInteger.ONE) != 0)
                hg.add(d);
        }
        return hg;
    }

    /**
     * Add isolated vertex from u.
     */
    public void addFaceDivision(GBlinkVertex u, GBlinkVertex x) {
        // follow "av"-path from u until get to x.
        // if the path is just (u,x) the we want a loop. throw exception
        // else if the path ends with an "a" edge then make x <- a(x)

        int size = 1;
        GBlinkEdgeType type = GBlinkEdgeType.edge;
        GBlinkVertex k = u.getNeighbour(type);
        while (k != u && k != x) {
            type = (type == GBlinkEdgeType.edge ? GBlinkEdgeType.vertex : GBlinkEdgeType.edge);
            k = k.getNeighbour(type);
            size++;
        }

        if (k == u)
            throw new RuntimeException("Not on the same face");
        else if (size == 1)
            throw new RuntimeException("Loop!");
        else if (size % 2 == 1)
            x = k.getNeighbour(GBlinkEdgeType.edge);

        GBlinkVertex v = u.getNeighbour(GBlinkEdgeType.edge);
        GBlinkVertex y = x.getNeighbour(GBlinkEdgeType.edge);

        //    a     f     a
        // u --- a --- b --- v
        //       |     |
        //     v |     | v
        //       |     |
        //       d --- c
        //       |  f  |
        //       | ___ |
        //          a

        GBlinkVertex a = new GBlinkVertex();
        GBlinkVertex b = new GBlinkVertex();
        GBlinkVertex c = new GBlinkVertex();
        GBlinkVertex d = new GBlinkVertex();

        // edge, face, vertex
        u.setNeighbour(a, GBlinkEdgeType.edge);
        v.setNeighbour(b, GBlinkEdgeType.edge);
        x.setNeighbour(c, GBlinkEdgeType.edge);
        y.setNeighbour(d, GBlinkEdgeType.edge);
        a.setNeighbours(u, b, d);
        b.setNeighbours(v, a, c);
        c.setNeighbours(x, d, b);
        d.setNeighbours(y, c, a);

        //
        _vertices.add(a);
        _vertices.add(b);
        _vertices.add(c);
        _vertices.add(d);
    }


    /**
     * Add isolated vertex from u.
     */
    public void addGFaceOrGVertexDivision(GBlinkVertex u, GBlinkVertex x, boolean gface) {
        // follow "av"-path from u until get to x.
        // if the path is just (u,x) the we want a loop. throw exception
        // else if the path ends with an "a" edge then make x <- a(x)

        int size = 1;
        GBlinkEdgeType type1 = GBlinkEdgeType.edge;
        GBlinkEdgeType type2 = gface ? GBlinkEdgeType.face : GBlinkEdgeType.vertex;
        GBlinkEdgeType type3 = gface ? GBlinkEdgeType.vertex : GBlinkEdgeType.face;

        GBlinkEdgeType type = type1;
        GBlinkVertex k = u.getNeighbour(type);
        while (k != u && k != x) {
            type = (type == type1 ? type2 : type1);
            k = k.getNeighbour(type);
            size++;
        }

        if (k == u)
            throw new RuntimeException("Not on the same face");
        else if (size == 1)
            throw new RuntimeException("Loop!");
        else if (size % 2 == 1)
            x = k.getNeighbour(GBlinkEdgeType.edge);

        GBlinkVertex v = u.getNeighbour(GBlinkEdgeType.edge);
        GBlinkVertex y = x.getNeighbour(GBlinkEdgeType.edge);

        GBlinkVertex a = new GBlinkVertex();
        GBlinkVertex b = new GBlinkVertex();
        GBlinkVertex c = new GBlinkVertex();
        GBlinkVertex d = new GBlinkVertex();

        // edge, face, vertex
        GBlinkVertex.setNeighbours(u,a,GBlinkEdgeType.edge);
        GBlinkVertex.setNeighbours(v,b,GBlinkEdgeType.edge);
        GBlinkVertex.setNeighbours(x,c,GBlinkEdgeType.edge);
        GBlinkVertex.setNeighbours(y,d,GBlinkEdgeType.edge);

        GBlinkVertex.setNeighbours(a,b,type3);
        GBlinkVertex.setNeighbours(a,d,type2);
        GBlinkVertex.setNeighbours(b,c,type2);
        GBlinkVertex.setNeighbours(c,d,type3);

        //
        _vertices.add(a);
        _vertices.add(b);
        _vertices.add(c);
        _vertices.add(d);
    }






    // ------------------------------------------------------------------------
    // -- Quantum Calculation data and routines
    private int _r = 0;
    ArrayList<Variable> _vars;
    ArrayList<Variable> _varFaces;
    ArrayList<Variable> _varVertices;
    ArrayList<Variable> _varZigzags;
    double _mi;
    Complex _alpha;
    Complex _norm;
    Complex _A;
    double _delta[];
    double _qdeform[];
    double _qdeformfact[];

    //
    HashMap<GBlinkVertex,Variable> _v2face = new HashMap<GBlinkVertex,Variable>();
    HashMap<GBlinkVertex,Variable> _v2zigzag = new HashMap<GBlinkVertex,Variable>();
    HashMap<GBlinkVertex,Variable> _v2vertex = new HashMap<GBlinkVertex,Variable>();

    public Variable findVariable(GBlinkVertex v, int type) {
        if (type == Variable.G_FACE) {
            return _v2face.get(v);
        }
        else if (type == Variable.G_ZIGZAG) {
            return _v2zigzag.get(v);
        }
        else if (type == Variable.G_VERTICE) {
            return _v2vertex.get(v);
        }
        throw new RuntimeException("OOooooopppsss");

        /*
        if (type == Variable.FACE) {
            for (Variable var: _varFaces) {
                if (var.contains(v))
                    return var;
            }
        }
        else if (type == Variable.ZIGZAG) {
            for (Variable var: _varZigzags) {
                if (var.contains(v))
                    return var;
            }
        }
        else if (type == Variable.VERTICE) {
            for (Variable var: _varVertices) {
                if (var.contains(v))
                    return var;
            }
        }
        throw new RuntimeException("OOooooopppsss");*/
    }

    /**
     * Change odd vertices to be even vertices and vice-versa. If nothing
     * more is done, this has the effect of changing all crossings or all
     * g-edge colors or the space orientation.
     */
    public void changeParity() {
        for (GBlinkVertex v: _vertices) {
            if (v.hasOddLabel())
                v.setLabel(2);
            else
                v.setLabel(1);
        }
    }


    public int[] getCrossingSigns() {

    	int n = this.getNumberOfGEdges();
        
        int signs[] = new int[n];

        for (GBlinkVertex v: _vertices) {

            if (v.getLabel() % 4 == 1) {
                GBlinkVertex a = v;
                GBlinkVertex b = a.getNeighbour(GBlinkEdgeType.face);
                GBlinkVertex c = b.getNeighbour(GBlinkEdgeType.vertex);
                GBlinkVertex d = c.getNeighbour(GBlinkEdgeType.face);

                Variable z1 = findVariable(a, Variable.G_ZIGZAG);
                Variable z2 = findVariable(b, Variable.G_ZIGZAG);

                BlinkColor color = this.getColor(v.getEdgeLabel());

                boolean z1_over_z2 = (color == BlinkColor.green ? true : false);

                boolean z1Increase = (z1.next(a) == c);
                boolean z2Increase = (z2.next(b) == d);

                int index1 = _varZigzags.indexOf(z1);
                int index2 = _varZigzags.indexOf(z2);

                int s = 0;
                if      ( z1_over_z2 &&  z1Increase &&  z2Increase) s = -1;
                else if ( z1_over_z2 &&  z1Increase && !z2Increase) s = +1;
                else if ( z1_over_z2 && !z1Increase &&  z2Increase) s = +1;
                else if ( z1_over_z2 && !z1Increase && !z2Increase) s = -1;
                else if (!z1_over_z2 &&  z1Increase &&  z2Increase) s = +1;
                else if (!z1_over_z2 &&  z1Increase && !z2Increase) s = -1;
                else if (!z1_over_z2 && !z1Increase &&  z2Increase) s = -1;
                else if (!z1_over_z2 && !z1Increase && !z2Increase) s = +1;
                
                signs[v.getEdgeLabel()-1] = s;	
    	
            }
        }
        return signs;
    }
    
    
    /**
     * Calculate the linking matrix from this GBlink.
     */
    public int[][] linkingMatrix() {
        int n = _varZigzags.size();
        int M[][] = new int[n][n];
        for (GBlinkVertex v: _vertices) {

            if (v.getLabel() % 4 == 1) {
                GBlinkVertex a = v;
                GBlinkVertex b = a.getNeighbour(GBlinkEdgeType.face);
                GBlinkVertex c = b.getNeighbour(GBlinkEdgeType.vertex);
                GBlinkVertex d = c.getNeighbour(GBlinkEdgeType.face);

                Variable z1 = findVariable(a, Variable.G_ZIGZAG);
                Variable z2 = findVariable(b, Variable.G_ZIGZAG);

                BlinkColor color = this.getColor(v.getLabel()/4+1);

                boolean z1_over_z2 = (color == BlinkColor.green ? true : false);

                boolean z1Increase = (z1.next(a) == c);
                boolean z2Increase = (z2.next(b) == d);

                int index1 = _varZigzags.indexOf(z1);
                int index2 = _varZigzags.indexOf(z2);

                int s = 0;
                if      ( z1_over_z2 &&  z1Increase &&  z2Increase) s = -1;
                else if ( z1_over_z2 &&  z1Increase && !z2Increase) s = +1;
                else if ( z1_over_z2 && !z1Increase &&  z2Increase) s = +1;
                else if ( z1_over_z2 && !z1Increase && !z2Increase) s = -1;
                else if (!z1_over_z2 &&  z1Increase &&  z2Increase) s = +1;
                else if (!z1_over_z2 &&  z1Increase && !z2Increase) s = -1;
                else if (!z1_over_z2 && !z1Increase &&  z2Increase) s = -1;
                else if (!z1_over_z2 && !z1Increase && !z2Increase) s = +1;

                M[index1][index2] += s;
                if (index1 != index2) {
                    M[index2][index1] += s;
                }
            }
        }

        //
        for (int i = 0; i < n; i++)
            for (int j = 0; j < n; j++)
                if (i != j)
                    M[i][j] = M[i][j] / 2;

        return M;
    }

    /**
     * Print an integer matrix.
     */
    public void printMatrix(int M[][]) {
        int m = M.length;
        if (m == 0)
            return;
        int n = M[0].length;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++)
                System.out.print(String.format("%4d ", M[i][j]));
            System.out.println();
        }
    }

    /**
     * Load the g-structure of this GBlink: g-vertices, g-faces, g-components.
     */
    private boolean _variablesLoaded = false;
    public void loadVariables() {

        if (_variablesLoaded)
            return;

        _vars = new ArrayList<Variable>();

        // there will be a variable for each
        //   face    (va-gon)
        //   vertice (fa-gon)
        //   zigzag  (fva-gon)
        _varFaces = new ArrayList<Variable>();
        _varVertices = new ArrayList<Variable>();
        _varZigzags = new ArrayList<Variable>();
        if (this.getNumberOfGEdges() == 0) { // empty case
            _varFaces.add(new Variable(Variable.G_FACE));
            _varVertices.add(new Variable(Variable.G_VERTICE));
            _varZigzags.add(new Variable(Variable.G_ZIGZAG));
        }
        else {
            for (GBlinkVertex v : _vertices) v.setFlag(false);
            for (GBlinkVertex v : _vertices) {
                if (v.hasOddLabel() && v.getFlag() == false)
                    _varFaces.add(new Variable(v, Variable.G_FACE));
            }

            for (GBlinkVertex v: _vertices) v.setFlag(false);
            for (GBlinkVertex v: _vertices) {
                if (v.hasOddLabel() && v.getFlag() == false)
                    _varVertices.add(new Variable(v,Variable.G_VERTICE));
            }

            _varZigzags = new ArrayList<Variable>();
            for (GBlinkVertex v: _vertices) v.setFlag(false);
            for (GBlinkVertex v: _vertices) {
                if (v.getFlag() == false)
                    _varZigzags.add(new Variable(v,Variable.G_ZIGZAG));
            }
        }

        _vars.addAll(_varFaces);
        _vars.addAll(_varVertices);
        _vars.addAll(_varZigzags);

        for (Variable var: _varFaces)
            for (GBlinkVertex v: var.getVertices())
                _v2face.put(v,var);
        for (Variable var: _varZigzags)
            for (GBlinkVertex v: var.getVertices())
                _v2zigzag.put(v,var);
        for (Variable var: _varVertices)
            for (GBlinkVertex v: var.getVertices())
                _v2vertex.put(v,var);

        _variablesLoaded = true;
    }

//    /**
//     * Returns true if the diagonal of vertex "v" crosses above
//     * the other diagonal on the square (vextex, face) that 
//     * contains "v".
//     * @param v the vertex
//     * @return
//     */
//    public boolean crossesAbove(GBlinkVertex v) {
//    	int result = 0;
//    	int edgeLabel = v.getEdgeLabel();		
//		int parity = v.getLabel() % 2;
//		BlinkColor color = this.getColor(edgeLabel);
//		
//		// compute as if label == 0 or label == 3 
//		if (parity == 0 && color == BlinkColor.green)
//			return true;
//		else if (parity == 0 && color == BlinkColor.red)
//			return false;
//		else if (parity == 1 && color == BlinkColor.red)
//			return true;
//		else if (parity == 1 && color == BlinkColor.green)
//			return false;
//		else 
//			throw new RuntimeException();
//		}
//    }
    
    
    /**
     * Code of the gblink in a language that is 
     * compatible with Matveev's programs.
     * 
     * link
     * crossings 9
     * signs -1 -1 -1 -1 1 1 1 -1 -1
     * code -1 2 -3 4 -2 8 -5 6 -7 5
     * framing 0
     * code  1 -9 3 -4 9 -6 7 -8
     * framing -1
     * end
     * 
     * @return
     */
    public String getMatveevCode() {
    	StringBuffer sb = new StringBuffer();
    	
    	ArrayList<Variable> zigZags = this.getGZigZags();

		sb.append("link\n");
		sb.append(String.format("crossings %d\n",this.getNumberOfGEdges()));

		int[] signs = this.getCrossingSigns();
		sb.append("signs ");
		for (int s: signs) {
			sb.append(String.format(" %d",s));
		}
		sb.append("\n");

		// find framings
		HashMap<Variable,Integer> framings = new HashMap<Variable,Integer>();
    	for (Variable z: zigZags) {
    		framings.put(z,0);
    	}
		for (Variable gEdge: this.getGEdges()) {
			GBlinkVertex v = gEdge.getVertex(0);
			GBlinkVertex u = v.getNeighbour(GBlinkEdgeType.face);
			int edgeLabel = v.getEdgeLabel();
			Variable zigZag1 = this.findVariable(v,GBlinkBigon.G_ZIGZAG);
			Variable zigZag2 = this.findVariable(u,GBlinkBigon.G_ZIGZAG);
			if (zigZag1 == zigZag2) {
				int f = framings.get(zigZag1);
				framings.put(zigZag1, f + signs[edgeLabel-1]);
			}
		}
		
		
    	for (Variable z: zigZags) {
    		sb.append("code");
    		for (int i=0;i<z.size();i+=2) {
    			GBlinkVertex vi = z.getVertex(i);
    			int edgeLabel = vi.getEdgeLabel();
    			if (vi.overcross())
    				sb.append(" "+(edgeLabel));
    			else
    				sb.append(" "+(-edgeLabel));
    		}
    		sb.append("\n");
    		sb.append("framing "+framings.get(z)+"\n");
    	}
		sb.append("end\n");
    	return sb.toString();
    }
        
    /**
     * Reset variables loaded.
     */
    public void resetVariablesLoaded() {
        _variablesLoaded = false;
    }

    /**
     * Find largest face (with the most vertices).
     */
    public Variable getLargestFace() {
        this.loadVariables();
        Variable result = null;
        for (Variable v : _varFaces) {
            if (result == null)
                result=v;
            else if (result.size() < v.size()) {
                result = v;
            }
        }
        return result;
    }

    /**
     * Constraint (Restriction) Triples. Used to define a valid state on the
     * Quantum Invariant calculation from a GBlink.
     */
    public ArrayList<RTriple> getRTriples() {
        // for each "edge"
        HashSet<RTriple> triplesSet = new HashSet<RTriple>();
        for (GBlinkVertex v : _vertices) {
            //
            /*
                         Variable vertex = findVariable(v,Variable.VERTICE);
                         Variable face = findVariable(v,Variable.FACE);
                         Variable zigzag = findVariable(v,Variable.ZIGZAG);
                         triplesSet.add(new RTriple(vertex,face,zigzag));
             */

            //
            if (v.getLabel() % 4 == 1) {
                Variable v1 = findVariable(v, Variable.G_VERTICE);
                Variable v2 = findVariable(v.getNeighbour(GBlinkEdgeType.vertex), Variable.G_VERTICE);
                Variable f1 = findVariable(v, Variable.G_FACE);
                Variable f2 = findVariable(v.getNeighbour(GBlinkEdgeType.face), Variable.G_FACE);
                Variable z1 = findVariable(v, Variable.G_ZIGZAG);
                Variable z2 = findVariable(v.getNeighbour(GBlinkEdgeType.face), Variable.G_ZIGZAG);

                triplesSet.add(new RTriple(v1, f1, z1));
                triplesSet.add(new RTriple(v1, f2, z2));
                triplesSet.add(new RTriple(v2, f2, z1));
                triplesSet.add(new RTriple(v2, f1, z2));
            }
        }

        ArrayList<RTriple> triples = new ArrayList<RTriple>(triplesSet);
        for (int i = 0; i < triples.size(); i++) {
            RTriple t = triples.get(i);
            t.getA().assignFirstPositionIfNotDefined(i);
            t.getB().assignFirstPositionIfNotDefined(i);
            t.getC().assignFirstPositionIfNotDefined(i);

            // log
            // System.out.println("Triple: "+t.getA().getLabel()+" "+t.getB().getLabel()+" "+t.getC().getLabel());
        }

        return triples;
    }

    /**
     * Calculate Quantum Invariant from this GBlink for r0 to r1.
     */
    public QI quantumInvariant(int r0, int r1) {

        // use an equivalent blink with more than zero edges
        if (this.getNumberOfGEdges() == 0)
            return _S1xS2Blink.optimizedQuantumInvariant(r0,r1);

        // load variables
        this.loadVariables();

        // calculate triples
        ArrayList<RTriple> triples = this.getRTriples();

        QI result = new QI(QI.NOT_PERSISTENT);
        for (_r = r0;_r<=r1;_r++) {

            long t0 = System.currentTimeMillis();

            { // primitive (4r)-th root of unity
                double aux = Math.PI / (2.0 * _r);
                _A = Complex.valueOf(Math.cos(aux), Math.sin(aux));
            } // primitive (4r)-th root of unity

            { // Mi, Alpha and Norm for S1xS2 Normalization
                _mi = Math.sin(Math.PI / _r) * Math.sqrt(2.0 / _r);
                double aux = 3 * Math.PI * (_r - 2.0) / (4 * _r);
                _alpha = Complex.valueOf(0, -1).pow(_r - 2).times(Complex.valueOf(Math.cos(aux), Math.sin(aux)));

                int linkingMatrix[][] = this.linkingMatrix();
                InertialMatrix im = new InertialMatrix(linkingMatrix);
                int nL = im.getDiagonalSum();
                int nZigZags = _varZigzags.size();
                _norm = _alpha.pow(nL).times(Math.pow(_mi, nZigZags + 1));
            } // Mi, Alpha and Norm for S1xS2 Normalization

            /*
                     System.out.println(String.format("r:     %15d",_r));
                     System.out.println(String.format("mi:    %15.10f",_mi));
             System.out.println(String.format("alpha: %15.10f + %15.10fi",_alpha.getReal(),_alpha.getImaginary()));
                     System.out.println(String.format("A:     %15.10f + %15.10fi",_A.getReal(),_A.getImaginary()));
             */

            int N = 2 * _r;
            _delta = new double[N];
            _qdeform = new double[N];
            _qdeformfact = new double[N];
            for (int i = 0; i < _delta.length; i++) {
                double di = _A.pow(2 * i + 2).minus(_A.pow( -2 * i - 2)).divide(_A.pow(2).minus(_A.pow( -2))).getReal();
                if (i % 2 == 1)
                    di = -di;
                if (i == _r - 1)
                    di = 0.0;
                _delta[i] = di;

                double qi = (i == 0 ? 0 : _delta[i - 1]);
                if (i % 2 == 0)
                    qi = -qi;
                _qdeform[i] = qi;

                _qdeformfact[i] = (i == 0 ? 1 : _qdeform[i] * _qdeformfact[i - 1]);
            }

            /*
            for (int i=0;i<_delta.length;i++) {
                System.out.println(String.format("i,d,qd,qdf = %3d %28.20f %28.20f %28.20f",i,_delta[i],_qdeform[i],_qdeformfact[i]));
            }*/

            //
            int states = 0;
            Complex sum = Complex.valueOf(0.0, 0.0);

            // fix this value
            Variable zeroVar = _varFaces.get(0);
            for (Variable v: _varFaces) {
                if (v.size() > zeroVar.size())
                    zeroVar = v;
            }
            zeroVar.setValue(0);
            zeroVar.setFirstPosition( -1);
            //_varZigzags.get(0).setValue(0);
            //_varZigzags.get(0).setFirstPosition( -1);

            ArrayList<RAdmissibleAssignment> aa = this.calculateAdmissibleTriples();

            /*
                     // log vars
                     for (Variable v: vars) {
                System.out.print(""+v.getLabel()+" ");
                     }
                     System.out.println();
                     // log vars
             */

            int na = aa.size();
            int n = triples.size();
            int x[] = new int[n];
            for (int i = 0; i < n; i++)
                x[i] = -1;
            int i = 0;
            while (i >= 0) {
                x[i]++;
                RTriple t = triples.get(i);

                /*
                             for (int k=0;k<=i;k++)
                    System.out.print(x[k]+" ");
                             System.out.println();
                 */

                // overflow this position
                if (x[i] >= na) {
                    x[i] = -1;
                    i--;
                    continue;
                }

                // assign currents
                RAdmissibleAssignment as = aa.get(x[i]);
                if (
                        (t.getA().firstPosition() == i || t.getA().getValue() == as.getA()) &&
                        (t.getB().firstPosition() == i || t.getB().getValue() == as.getB()) &&
                        (t.getC().firstPosition() == i || t.getC().getValue() == as.getC())
                        ) {
                    t.getA().setValue(as.getA());
                    t.getB().setValue(as.getB());
                    t.getC().setValue(as.getC());
                } else {
                    continue;
                }

                //
                if (i < n - 1) {
                    i++;
                    continue;
                } else if (i == n - 1) { // found a valid state
                    states++;

                    double realproduct = 1.0;
                    double lambdaArgumentSum = 0;

                    // System.out.println("State Value: "+states);
                    /*
                                     System.out.print("Found state S"+states+" ");
                                     for (Variable v: vars)
                        System.out.print(v.getLabel()+" -> "+v.getValue()+"    ");
                                     System.out.println();
                     */

                    // variable part
                    for (Variable v : _varFaces) {
                        double y = _delta[v.getValue()];
                        realproduct *= y;
                        // System.out.println(String.format("%30s = %30.10f","delta("+v.getLabel()+")",y));
                    }
                    for (Variable v : _varVertices) {
                        double y = _delta[v.getValue()];
                        realproduct *= y;
                        // System.out.println(String.format("%30s = %30.10f","delta("+v.getLabel()+")",y));
                    }
                    for (Variable v : _varZigzags) {
                        double y = _delta[v.getValue()];
                        realproduct *= y;
                        // System.out.println(String.format("%30s = %30.10f","delta("+v.getLabel()+")",y));
                    }
                    // angles part
                    for (GBlinkVertex v : _vertices) {
                        if (v.getLabel() % 2 == 1) {
                            Variable vertex = findVariable(v, Variable.G_VERTICE);
                            Variable face = findVariable(v, Variable.G_FACE);
                            Variable zigzag = findVariable(v, Variable.G_ZIGZAG);
                            double y = 1 / theta(face.getValue(), vertex.getValue(), zigzag.getValue());
                            realproduct *= y;
                            // System.out.println(String.format("%30s = %30.10f","theta("+vertex.getLabel()+","+face.getLabel()+","+zigzag.getLabel()+")",y));
                        }
                    }

                    // tet part
                    for (GBlinkVertex v : _vertices) {
                        if (v.getLabel() % 4 == 1) {
                            Variable v1 = findVariable(v, Variable.G_VERTICE);
                            Variable v2 = findVariable(v.getNeighbour(GBlinkEdgeType.vertex), Variable.G_VERTICE);
                            Variable f1 = findVariable(v, Variable.G_FACE);
                            Variable f2 = findVariable(v.getNeighbour(GBlinkEdgeType.face), Variable.G_FACE);
                            Variable z1 = findVariable(v, Variable.G_ZIGZAG);
                            Variable z2 = findVariable(v.getNeighbour(GBlinkEdgeType.face), Variable.G_ZIGZAG);

                            //
                            // System.out.println(String.format("Calculating... %30s","tet("+f1.getLabel()+","+v1.getLabel()+","+f2.getLabel()+","+v2.getLabel()+","+z2.getLabel()+","+z1.getLabel()+")"));

                            //
                            double y = tet(f1.getValue(), v1.getValue(), f2.getValue(), v2.getValue(), z2.getValue(),
                                           z1.getValue());
                            realproduct *= y;
                            // System.out.println(String.format("%30s = %30.10f","tet("+f1.getLabel()+","+v1.getLabel()+","+f2.getLabel()+","+v2.getLabel()+","+z2.getLabel()+","+z1.getLabel()+")",y));

                            if (this.getColor((v.getLabel() / 4) + 1) == BlinkColor.green) {
                                y = lambdaArgument(f1.getValue(), z1.getValue(), v1.getValue());
                                lambdaArgumentSum += y;
                                // System.out.println(String.format("%30s = %30.10f","+lambdaArgument("+f1.getLabel()+","+z1.getLabel()+","+v1.getLabel()+")",y));

                                y = -lambdaArgument(v2.getValue(), z1.getValue(), f2.getValue());
                                lambdaArgumentSum += y;
                                // System.out.println(String.format("%30s = %30.10f","-lambdaArgument("+v2.getLabel()+","+z1.getLabel()+","+f2.getLabel()+")",y));
                            } else {
                                y = lambdaArgument(v2.getValue(), z1.getValue(), f2.getValue());
                                lambdaArgumentSum += y;
                                // System.out.println(String.format("%30s = %30.10f","-lambdaArgument("+v2.getLabel()+","+z1.getLabel()+","+f2.getLabel()+")",y));

                                y = -lambdaArgument(f1.getValue(), z1.getValue(), v1.getValue());
                                lambdaArgumentSum += y;
                                // System.out.println(String.format("%30s = %30.10f","+lambdaArgument("+f1.getLabel()+","+z1.getLabel()+","+v1.getLabel()+")",y));
                            }
                        }
                    }

                    Complex stateValue =
                            Complex.valueOf(
                                    Math.cos(lambdaArgumentSum),
                                    Math.sin(lambdaArgumentSum)).times(realproduct);

                    /*
                    System.out.print("S"+states+" ");
                    for (Variable v: vars)
                         System.out.print(v.getLabel()+" -> "+v.getValue()+"    ");
                    System.out.println(String.format("Value: %30.10f %30.10fi",stateValue.getReal(),stateValue.getImaginary()));
                    */

                    sum = sum.plus(stateValue);
                }
            }
            // System.out.println(String.format("Result Before Norm: %30.10f %30.10fi", sum.getReal(), sum.getImaginary()));
            // System.out.println(String.format("Norm:               %30.10f %30.10fi", _norm.getReal(),_norm.getImaginary()));
            sum = sum.times(_norm);
            // System.out.println(String.format("Result After Norm:  %30.10f %30.10fi", sum.getReal(), sum.getImaginary()));

            result.addEntry(new QIEntry(_r,sum.getReal(),sum.getImaginary(),states,System.currentTimeMillis()-t0));

        }

        return result;

    }

    /**
     * Auxiliar method on the QuantumInvariant calculation.
     */
    public double theta(int a, int b, int c) {
        // System.out.println(String.format("theta(%d,%d,%d)",a,b,c));
        int abc = a + b + c;
        int ab_c = a + b - c;
        int bc_a = b + c - a;
        int ac_b = a + c - b;
        if ((abc > 2*_r - 4) || (abc % 2 != 0) || (ab_c < 0) || (bc_a < 0) || (ac_b < 0))
            return 0.0;

        int m = ab_c/2;
        int n = bc_a/2;
        int p = ac_b/2;

        double result = _qdeformfact[m+n+p+1]*_qdeformfact[n]*_qdeformfact[m]*_qdeformfact[p]/
                (_qdeformfact[m+n]*_qdeformfact[n+p]*_qdeformfact[p+m]);
        if ((n + p + m) % 2 == 1)
            result = -result;
        return result;
    }

    /**
     * Auxiliar method on the QuantumInvariant calculation.
     */
    public Complex lambda(int a, int b, int c) {
        int abc = a + b + c;
        int ab_c = a + b - c;
        int bc_a = b + c - a;
        int ac_b = a + c - b;
        if (abc > 2*_r - 4 || (ab_c % 2 != 0) || (bc_a % 2 != 0) || (ac_b % 2 != 0))
            return Complex.valueOf(0,0);

        Complex result = _A.pow((a*(a+2)+b*(b+2)-c*(c+2))/2);
        if (abc/2 % 2 == 1)
            result = Complex.valueOf(-result.getReal(),-result.getImaginary());

        return result;
    }

    /**
     * Auxiliar method on the QuantumInvariant calculation.
     */
    public double lambdaArgument(int a, int b, int c) {
        return 1.0/4.0*Math.PI*(2*_r*a+2*_r*b-2*_r*c+a*a+2*a+b*b+2*b-c*c-2*c)/_r;
    }

    /**
     * Auxiliar method on the QuantumInvariant calculation.
     */
    public boolean isRAdmissible(int a, int b, int c) {
        int abc = a + b + c;
        int ab_c = a + b - c;
        int bc_a = b + c - a;
        int ac_b = a + c - b;
        if ((abc > 2*_r - 4) || (abc % 2 != 0) || (ab_c < 0) || (bc_a < 0) || (ac_b < 0))
            return false;
        else
            return true;
    }

    /**
     * Auxiliar class on the QuantumInvariant calculation.
     */
    class RAdmissibleAssignment {
        int _a; int _b; int _c;
        public RAdmissibleAssignment(int a, int b, int c) {
            _a = a; _b = b; _c = c;
        }
        public int getA() { return _a; }
        public int getB() { return _b; }
        public int getC() { return _c; }
    }

    /**
     * Auxiliar class on the QuantumInvariant calculation.
     */
    class RTriple {
        Variable _a; Variable _b; Variable _c;
        public RTriple(Variable a, Variable b, Variable c) {
            _a = a; _b = b; _c = c;
        }
        public Variable getA() { return _a; }
        public Variable getB() { return _b; }
        public Variable getC() { return _c; }
        public boolean equals(Object o) {
            RTriple x = (RTriple) o;
            if (x._a == _a && x._b == _b  && x._c == _c)
                return true;
            else return false;
        }
        public int hashCode() {
            return _a.hashCode()+_b.hashCode()+_c.hashCode();
        }
        private int _degree = 0;
        public void incDegree() {
            _degree++;
        }
        public boolean contains(Variable v) {
            return (v == _a || v == _b || v == _c);
        }
        public void resetDegree() {
            _degree = 0;
        }
        public int getDegree() {
            return _degree;
        }
        public int getNumberOfCommonVariables(RTriple t) {
            return (t.getA() == _a ? 1 : 0) + (t.getB() == _b ? 1 : 0) + (t.getC() == _c ? 1 : 0);
        }
        public boolean check() {
            return isRAdmissible(_a.getValue(),_b.getValue(),_c.getValue());
        }

        /**
         * Returns true if this triple contains v and if the variable in
         * this triple are "index"-defined but v is not "index"-defined
         */
        public boolean isDefinedBy(Variable v) {
            if ((v != _a && v != _b && v != _c) || v.isIndexDefined())
                return false;
            if (v == _a)
                return (_b.isIndexDefined() && _c.isIndexDefined());
            else if (v == _b)
                return (_a.isIndexDefined() && _c.isIndexDefined());
            else // if (v == _c)
                return (_a.isIndexDefined() && _b.isIndexDefined());
        }

    }

    /**
     * Auxiliar method on the QuantumInvariant calculation.
     */
    public ArrayList<RAdmissibleAssignment> calculateAdmissibleTriples() {
        ArrayList<RAdmissibleAssignment> result = new ArrayList<RAdmissibleAssignment>();
        int i=0;
        int[] x = {-1,-1,-1};
        while (i >= 0) {
            x[i]++;

            if (x[i] > _r-2) {
                x[i] = -1;
                i--;
                continue;
            }

            if (i < 2) {
                i++;
                continue;
            }

            if (i == 2) {
                if (isRAdmissible(x[0],x[1],x[2])) {
                    // System.out.println(String.format("Admissible: %d %d %d",x[0],x[1],x[2]));
                    result.add(new RAdmissibleAssignment(x[0],x[1],x[2]));
                }
            }
        }
        return result;
    }

    /**
     * [ a d e ]
     * [ c d f ]
     */
    public double tet(int a, int b, int c, int d, int e, int f) {
        int ai[] = {0, (a + b + f)/2,  (b + c + e)/2, (c + d + f)/2, (a + d + e)/2 };
        int bj[] = {0, (b + d + e + f)/2, (a + c + e + f)/2, (a + b + c + d)/2};

        int m = Math.max(Math.max(ai[1],ai[2]),Math.max(ai[3],ai[4]));
        int M = Math.min(Math.min(bj[1],bj[2]),bj[3]);

        // System.out.println(String.format("a -> %d   b -> %d   c -> %d   d -> %d   e -> %d   f -> %d   m -> %d   M -> %d",a,b,c,d,e,f,m,M));

        double intf = 1.0;
        for (int i=1;i<=4;i++)
            for (int j=1;j<=3;j++)
                intf *= _qdeformfact[bj[j]-ai[i]];

        double extf = _qdeformfact[a] * _qdeformfact[b]  * _qdeformfact[c] *
                      _qdeformfact[d] * _qdeformfact[e]  * _qdeformfact[f];

        double sum = 0;
        for (int s=m;s<=M;s++) {

            double pi = 1.0;
            for (int i=1;i<=4;i++)
                pi *= _qdeformfact[s-ai[i]];

            double pj = 1.0;
            for (int j=1;j<=3;j++)
                pj *= _qdeformfact[bj[j]-s];

            double term = _qdeformfact[s+1]/(pi * pj);
            if (s % 2 == 1)
                term = -term;

            sum += term;
        }

        return (intf/extf)*sum;
    }

    // -- Quantum Calculation data and routines
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // -- optimized quantum invariant

    public QI optimizedQuantumInvariant(int r0, int r1) {

        // use an equivalent blink with more than zero edges
        if (this.getNumberOfGEdges() == 0)
            return _S1xS2Blink.optimizedQuantumInvariant(r0,r1);

        // load variables
        this.loadVariables();

        // calculate RTriples and their degrees (triples with intersection)
        ArrayList<RTriple> triples = getRTriples();

        // fix this value
        Variable zeroVar = _varFaces.get(0);
        for (Variable v: _varFaces) {
            if (v.size() > zeroVar.size())
                zeroVar = v;
        }
        zeroVar.setValue(0);
        zeroVar.setFirstPosition( -1);

        RTriple first = null;
        for (int i=0;i<triples.size();i++) {
            RTriple ti = triples.get(i);
            if (ti.contains(zeroVar)) {
                first = ti;
                break;
            }
        }

        //
        int n = _vars.size();

        // LOG
        /*
        System.out.println("All triples!");
        for (RTriple t: triples)
            System.out.println("triple: "+t.getA().getLabel()+" "+t.getB().getLabel()+" "+t.getC().getLabel()); */

        // define pi startup
        Variable pi[] = new Variable[n];
        ConstraintCheck cc[] = new ConstraintCheck[n];
        pi[0] = first.getB(); first.getB().setIndex(0); cc[0] = new ConstraintCheck(pi[0]); // face as first
        pi[1] = first.getA(); first.getA().setIndex(1); cc[1] = new ConstraintCheck(pi[1]);
        pi[2] = first.getC(); first.getC().setIndex(2); cc[2] = new ConstraintCheck(pi[2]);
        cc[2].addRTriple(first);
        triples.remove(first);

        { // initialize other variables in pi
            int i = 3;
            for (Variable v : _vars)
                if (v != pi[0] && v != pi[1] && v != pi[2])
                    pi[i++] = v;
        }

        // find the best variable for each remaining position of pi
        for (int ii = 3; ii < n; ii++) {
            Variable choosen = null;
            int defCount = 0;
            int index = -1;
            for (int i = ii; i < n; i++) {
                Variable v = pi[i];
                int vDefCount = 0;
                for (RTriple t : triples) {
                    if (t.isDefinedBy(v))
                        vDefCount++;
                }
                if (choosen == null || vDefCount > defCount) {
                    choosen = v;
                    defCount = vDefCount;
                    index = i;
                }
            }

            if (choosen == null)
                throw new RuntimeException("ooooooooooooooooppppppppppppppsssssssssssssssss");

            swap(pi, index, ii);
            cc[ii] = new ConstraintCheck(choosen);
            for (int i = triples.size() - 1; i >= 0; i--) {
                RTriple t = triples.get(i);
                if (t.isDefinedBy(choosen)) {
                    cc[ii].addRTriple(t);
                    triples.remove(i);
                }
            }

            pi[ii].setIndex(index);
        }


        // LOG
        /*
        System.out.println("Remaining triples!");
        for (RTriple t: triples)
            System.out.println("triple: "+t.getA().getLabel()+" "+t.getB().getLabel()+" "+t.getC().getLabel());
        System.out.println("Variables and defining triples!");
        for (int i=0;i<n;i++)
            System.out.println(cc[i].toString()); */

        if (triples.size() > 0)
            throw new RuntimeException("Oooooppppssss");

        QI result = new QI(QI.NOT_PERSISTENT);

        // result
        for (int i=r0;i<=r1;i++) {
            _r = i;
            long t0 = System.currentTimeMillis();

            this.preQICalculation();

            int states = 0;
            Complex sum = Complex.valueOf(0.0, 0.0);

            // first variable has a fixed value
            cc[0].mountValidEntries(_r);  cc[0].next();

            // go...
            int k = 1;
            cc[1].mountValidEntries(_r);
            while (k >= 1) {
                if (!cc[k].next()) {
                    k--;
                    continue;
                }

                //for (int j=0;j<k;j++)
                //    System.out.print(""+pi[j].getLabel()+" -> "+pi[j].getValue()+" ");

                if (k < n - 1) {
                    cc[k + 1].mountValidEntries(_r);
                    k++;
                    continue;
                } else {
                    states++;
                    sum = sum.plus(evaluateCurrentState());
                    continue;
                }
            }
            sum = sum.times(_norm);
            result.addEntry(new QIEntry(_r, sum.getReal(), sum.getImaginary(), states, System.currentTimeMillis() - t0));
        }

        // return result
        return result;
    }

    public void preQICalculation() {
        { // primitive (4r)-th root of unity
            double aux = Math.PI / (2.0 * _r);
            _A = Complex.valueOf(Math.cos(aux), Math.sin(aux));
        } // primitive (4r)-th root of unity

         // Mi, Alpha and Norm for S1xS2 Normalization
            _mi = Math.sin(Math.PI / _r) * Math.sqrt(2.0 / _r);
            double aux = 3 * Math.PI * (_r - 2.0) / (4 * _r);
            _alpha = Complex.valueOf(0, -1).pow(_r - 2).times(Complex.valueOf(Math.cos(aux), Math.sin(aux)));
            int linkingMatrix[][] = this.linkingMatrix();
            // printMatrix(linkingMatrix);
            InertialMatrix im = new InertialMatrix(linkingMatrix);
            int nL = im.getDiagonalSum();
            int nZigZags = _varZigzags.size();
            _norm = _alpha.pow(nL).times(Math.pow(_mi, nZigZags + 1));
         // Mi, Alpha and Norm for S1xS2 Normalization

        // LOG
        /*
        System.out.println(String.format("r:      %15d",_r));
        System.out.println(String.format("mi:     %15.10f",_mi));
        System.out.println(String.format("alpha:  %15.10f + %15.10fi",_alpha.getReal(),_alpha.getImaginary()));
        System.out.println(String.format("A:      %15.10f + %15.10fi",_A.getReal(),_A.getImaginary()));
        System.out.println(String.format("nL:     %15d",nL));
        System.out.println(String.format("nZigZag:%15d",nZigZags));
        System.out.println(String.format("norm:   %15.10f + %15.10fi",_norm.getReal(),_norm.getImaginary())); */

        int N = 2 * _r;
        _delta = new double[N];
        _qdeform = new double[N];
        _qdeformfact = new double[N];
        for (int i = 0; i < _delta.length; i++) {
            double di = _A.pow(2 * i + 2).minus(_A.pow( -2 * i - 2)).divide(_A.pow(2).minus(_A.pow( -2))).getReal();
            if (i % 2 == 1)
                di = -di;
            if (i == _r - 1)
                di = 0.0;
            _delta[i] = di;

            double qi = (i == 0 ? 0 : _delta[i - 1]);
            if (i % 2 == 0)
                qi = -qi;
            _qdeform[i] = qi;

            _qdeformfact[i] = (i == 0 ? 1 : _qdeform[i] * _qdeformfact[i - 1]);
        }
    }

    private Complex evaluateCurrentState() {
        double realproduct = 1.0;
        double lambdaArgumentSum = 0;

        // variable part
        for (Variable v : _vars) {
            double y = _delta[v.getValue()];
            realproduct *= y;
            // System.out.println(String.format("%30s = %30.10f","delta("+v.getLabel()+")",y));
        }

        // angles part
        for (GBlinkVertex v : _vertices) {
            if (v.getLabel() % 2 == 1) {
                Variable vertex = findVariable(v, Variable.G_VERTICE);
                Variable face = findVariable(v, Variable.G_FACE);
                Variable zigzag = findVariable(v, Variable.G_ZIGZAG);
                double y = 1 / theta(face.getValue(), vertex.getValue(), zigzag.getValue());
                realproduct *= y;
                // System.out.println(String.format("%30s = %30.10f","theta("+vertex.getLabel()+","+face.getLabel()+","+zigzag.getLabel()+")",y));
            }
        }

        // tet part
        for (GBlinkVertex v : _vertices) {
            if (v.getLabel() % 4 == 1) {
                Variable v1 = findVariable(v, Variable.G_VERTICE);
                Variable v2 = findVariable(v.getNeighbour(GBlinkEdgeType.vertex), Variable.G_VERTICE);
                Variable f1 = findVariable(v, Variable.G_FACE);
                Variable f2 = findVariable(v.getNeighbour(GBlinkEdgeType.face), Variable.G_FACE);
                Variable z1 = findVariable(v, Variable.G_ZIGZAG);
                Variable z2 = findVariable(v.getNeighbour(GBlinkEdgeType.face), Variable.G_ZIGZAG);

                //
                // System.out.println(String.format("Calculating... %30s","tet("+f1.getLabel()+","+v1.getLabel()+","+f2.getLabel()+","+v2.getLabel()+","+z2.getLabel()+","+z1.getLabel()+")"));

                //
                double y = tet(f1.getValue(), v1.getValue(), f2.getValue(), v2.getValue(), z2.getValue(),
                               z1.getValue());
                realproduct *= y;
                // System.out.println(String.format("%30s = %30.10f","tet("+f1.getLabel()+","+v1.getLabel()+","+f2.getLabel()+","+v2.getLabel()+","+z2.getLabel()+","+z1.getLabel()+")",y));

                if (this.getColor((v.getLabel() / 4) + 1) == BlinkColor.green) {
                    y = lambdaArgument(f1.getValue(), z1.getValue(), v1.getValue());
                    lambdaArgumentSum += y;
                    // System.out.println(String.format("%30s = %30.10f","+lambdaArgument("+f1.getLabel()+","+z1.getLabel()+","+v1.getLabel()+")",y));

                    y = -lambdaArgument(v2.getValue(), z1.getValue(), f2.getValue());
                    lambdaArgumentSum += y;
                    // System.out.println(String.format("%30s = %30.10f","-lambdaArgument("+v2.getLabel()+","+z1.getLabel()+","+f2.getLabel()+")",y));
                } else {
                    y = lambdaArgument(v2.getValue(), z1.getValue(), f2.getValue());
                    lambdaArgumentSum += y;
                    // System.out.println(String.format("%30s = %30.10f","-lambdaArgument("+v2.getLabel()+","+z1.getLabel()+","+f2.getLabel()+")",y));

                    y = -lambdaArgument(f1.getValue(), z1.getValue(), v1.getValue());
                    lambdaArgumentSum += y;
                    // System.out.println(String.format("%30s = %30.10f","+lambdaArgument("+f1.getLabel()+","+z1.getLabel()+","+v1.getLabel()+")",y));
                }
            }
        }

        // return state value
        return Complex.valueOf(Math.cos(lambdaArgumentSum),Math.sin(lambdaArgumentSum)).times(realproduct);
    }

    private void swap(Variable[] vs, int i, int j) {
        Variable v = vs[i];
        vs[i] = vs[j];
        vs[j] = v;
    }

    class ConstraintCheck {
        Variable _var;
        ArrayList<RTriple> _triples = new ArrayList<RTriple>();
        ArrayList<Integer> _values = new ArrayList<Integer>();
        private int _valueIndex;
        public ConstraintCheck(Variable v) {
            _var = v;
        }
        public void addRTriple(RTriple t) {
            _triples.add(t);
        }
        public void mountValidEntries(int r) {
            _valueIndex = 0;
            _values.clear();
            for (int i=0;i<=r-2;i++) {
                _var.setValue(i);
                boolean check = true;
                for (RTriple t: _triples) {
                    if (!t.check()) {
                        check = false;
                        break;
                    }
                }
                if (check)
                    _values.add(i);
            }
        }
        public boolean next() {
            if (_valueIndex == _values.size())
                return false;
            else {
                int result = _values.get(_valueIndex);
                _var.setValue(result);
                _valueIndex++;
                return true;
            }
        }

        public String toString() {
            String s = _var.getLabel()+" ";
            for (RTriple t: _triples) {
                s+="("+t.getA().getLabel()+","+t.getB().getLabel()+","+t.getC().getLabel()+") ";
            }
            return s;
        }

    }


    // -- Optimized quantum invariant
    // ------------------------------------------------------------------------

    /**
     * Check if there is a simple loop on this GBlink.
     */
    public boolean containsSimpleLoop() {
        for (GBlinkVertex v: _vertices) {
            GBlinkVertex a = v;
            GBlinkVertex b = a.getNeighbour(GBlinkEdgeType.face);
            GBlinkVertex c = b.getNeighbour(GBlinkEdgeType.vertex);
            GBlinkVertex d = c.getNeighbour(GBlinkEdgeType.face);

            // see if it is of kind A
            if (b.getNeighbour(GBlinkEdgeType.edge) == c)
                return true;
        }
        return false;
    }

    /**
     * if the vertex is of kind A then returns the label
     * of the edges that forms the pair. Returns null
     * otherwise.
     */
    public int[] isTheBaseVertexOfAConstrainedPair(GBlinkVertex v, boolean testA, boolean testB, boolean testC, boolean testD) {
        //return null;
        if (!testA && !testB && !testC && !testD) {
            return null;
        }
        else {
            GBlinkVertex a = v;
            GBlinkVertex b = a.getNeighbour(GBlinkEdgeType.face);
            GBlinkVertex c = b.getNeighbour(GBlinkEdgeType.vertex);
            GBlinkVertex d = c.getNeighbour(GBlinkEdgeType.face);
            GBlinkVertex e = d.getNeighbour(GBlinkEdgeType.edge);
            GBlinkVertex f = e.getNeighbour(GBlinkEdgeType.face);
            GBlinkVertex g = f.getNeighbour(GBlinkEdgeType.vertex);
            GBlinkVertex h = g.getNeighbour(GBlinkEdgeType.face);

            this.loadVariables();

            int e1 = ((a.getLabel() % 4) == 0 ? a.getLabel() / 4 : a.getLabel() / 4 + 1);
            int e2 = ((e.getLabel() % 4) == 0 ? e.getLabel() / 4 : e.getLabel() / 4 + 1);

            // see if it is of kind A
            if (testA && (h.getNeighbour(GBlinkEdgeType.edge) == a && !findVariable(a, Variable.G_VERTICE).contains(c)))
                return new int[] {e1, e2};

            // see if it is of kind B
            else if (testB && (c.getNeighbour(GBlinkEdgeType.edge) == f && !findVariable(a, Variable.G_VERTICE).contains(h)))
                return new int[] {e1, e2};

            // see if it is of kind C
            else if (testC && (b.getNeighbour(GBlinkEdgeType.edge) == c && f.getNeighbour(GBlinkEdgeType.edge) == g))
                return new int[] {e1, e2};

            // see if it is of kind D
            else if (testD && (a.getNeighbour(GBlinkEdgeType.edge) == b && g.getNeighbour(GBlinkEdgeType.edge) == h))
                return new int[] {e1, e2};

            else
                return null;
        }
    }

    /**
     * Contains an elimination ring?
     */
    public boolean containsAnEliminationRing() {
        for (GBlinkVertex v: _vertices) {
            GBlinkVertex a = v;
            GBlinkVertex b = a.getNeighbour(GBlinkEdgeType.face);
            GBlinkVertex c = b.getNeighbour(GBlinkEdgeType.vertex);
            GBlinkVertex d = c.getNeighbour(GBlinkEdgeType.face);
            GBlinkVertex e = d.getNeighbour(GBlinkEdgeType.edge);
            GBlinkVertex f = e.getNeighbour(GBlinkEdgeType.face);
            GBlinkVertex g = f.getNeighbour(GBlinkEdgeType.vertex);
            GBlinkVertex h = g.getNeighbour(GBlinkEdgeType.face);

            if (c == e || a == e)
                continue;

            if (c.getNeighbour(GBlinkEdgeType.edge) != f)
                continue;

            if (a.getNeighbour(GBlinkEdgeType.edge) != h)
                continue;

            if (this.getColor(a.getEdgeLabel()) ==
                this.getColor(e.getEdgeLabel()))
                return true;
        }
        return false;
    }


    /**
     * Edges on each part of this partition must have the
     * same color.
     */
    public ArrayList<EdgePartitionPart> getGEdgePartition() {
        int n = this.getNumberOfGEdges();
        int p[] = new int[n+1];
        for (int i=1;i<=n;i++)
            p[i] = i;

        //
        for (GBlinkVertex v: _vertices) {
            int e[] = isTheBaseVertexOfAConstrainedPair(v,true,true,true,true);
            if (e != null) {
                int p1 = p[e[0]];
                int p2 = p[e[1]];
                if (p1 < p2) p[e[1]] = p1;
                else if (p1 > p2) p[e[0]] = p2;
            }
        }

        ArrayList<EdgePartitionPart> result = new ArrayList<EdgePartitionPart>();
        HashMap<Integer,EdgePartitionPart> map = new HashMap<Integer,EdgePartitionPart>();
        for (int i=1;i<=n;i++) {
            EdgePartitionPart epp = map.get(p[i]);
            if (epp == null) {
                epp = new EdgePartitionPart();
                map.put(p[i],epp);
                result.add(epp);
            }
            epp.add(this.findVertex(4*(i-1)+1));
        }

        return result;
    }

    public static int getGEdgeLabelFromMapVertexLabel(int mvl) {
        if (mvl % 4 == 0) return mvl/4;
        else return mvl/4 + 1;
    }

    /**
     * test
     */
    public static void testNumberOfAlternatingZigZags() {
        GBlink b = new GBlink(new int[][] { {1, 6, 4, 3}, {1, 2, 7}, {2, 3, 4, 5}, {5, 6, 7}
        }, new int[] {1, 5, 7});
        System.out.println(String.format("Number of Alternating ZigZags: %d of %d",
                                         b.getNumberOfAlternatingZigZags(),
                                         b.getGZigZags().size()));

        // desenhar o mapa
        JFrame f = new JFrame("Map View");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(new Dimension(1024, 768));
        f.setContentPane(new PanelMapViewer(b));
        f.setVisible(true);
        // desenhar o mapa
    }

    public static void main(String[] args) throws HeadlessException, SQLException {
        //testNumberOfAlternatingZigZags();

        GBlink G = new GBlink(new int[]{8,2,1,3,4,5,6,7},8);
        GBlink rG = G.getNewRepresentant();
        System.exit(0);

        /*
        try {
            //GBlink G = new GBlink(new int[][] {{1,2},{2,3},{3,1}},new int[] {});
            GBlink G = new GBlink(new int[][] {{8,2,1,3,4,5,6,7}},new int[] {4});
            GBlink rG = G.getNewRepresentant();

            new DrawBlinkEPS(rG, "log/knotAblink.eps");
            new DrawBlinkEPS(rG, "log/knotArblink.eps");

            QI qi = G.optimizedQuantumInvariant(3,7);
            QI qiR = rG.optimizedQuantumInvariant(3,7);
            HomologyGroup hg = G.homologyGroupFromGBlink();
            HomologyGroup hgR = rG.homologyGroupFromGBlink();
            if (qi.isEqual(qiR) && hg.equals(hgR)) {
                System.out.println("Same QI and same HG");
            }
            else {
                System.out.println("PROBLEM! NOT the same QI or HG");
            }

            //G.
            JFrame f = new JFrame("G");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setSize(new Dimension(1024, 768));
            f.setContentPane(new PanelBlinkViewer(G));
            f.setVisible(true);

            f = new JFrame("rG");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setSize(new Dimension(1024, 768));
            f.setContentPane(new PanelBlinkViewer(rG));
            f.setVisible(true);


        } catch (IOException ex) {
            ex.printStackTrace();
        }  */

        /*
        try {
            //GBlink G = new GBlink(new int[][] {{1,2},{2,3},{3,1}},new int[] {});
            GBlink G = new GBlink(new int[][] {{1,1,2,2,3,3}},new int[] {});
            GBlink rG = G.getNewRepresentant();

            new DrawBlinkEPS(rG, "log/knotAblink.eps");
            new DrawBlinkEPS(rG, "log/knotArblink.eps");

            QI qi = G.optimizedQuantumInvariant(3,7);
            QI qiR = rG.optimizedQuantumInvariant(3,7);
            HomologyGroup hg = G.homologyGroupFromGBlink();
            HomologyGroup hgR = rG.homologyGroupFromGBlink();
            if (qi.isEqual(qiR) && hg.equals(hgR)) {
                System.out.println("Same QI and same HG");
            }
            else {
                System.out.println("PROBLEM! NOT the same QI or HG");
            }

            //G.
            JFrame f = new JFrame("G");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setSize(new Dimension(1024, 768));
            f.setContentPane(new PanelBlinkViewer(G));
            f.setVisible(true);

            f = new JFrame("rG");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setSize(new Dimension(1024, 768));
            f.setContentPane(new PanelBlinkViewer(rG));
            f.setVisible(true);


        } catch (IOException ex) {
            ex.printStackTrace();
        }
        */

        /*

        try {
            // GBlink G = new GBlink("-1 2 -3 1 -2 3", " ", "\n");
            // GBlink G = new GBlink("1,2,-3,-4,5,6,-7,-1,8,3,-6,7,-2,-8,4,-5", ",", "\n");

            GBlink G = new GBlink(new int[][] {{1,2},{1,2,3,4,12,5,12},{5},{6},{3,6,7,8,9,4},{9,10,10},{7,11},{8,11}},new int[] {3,4,5,6,7,8,10,11,12});

            G.write();
            DrawBlackboardFramedLink D1 = new DrawBlackboardFramedLink(G, "log/knotA.eps");
            DrawBlinkEPS D2 = new DrawBlinkEPS(G, "log/knotAblink.eps");

            DecompositionTree T = G.getDecompositionTree();
            GBlink rG = T.merge();
            rG.write();
            new DrawBlackboardFramedLink(rG, "log/knotAr.eps");
            new DrawBlinkEPS(rG, "log/knotArblink.eps");


            QI qi = G.optimizedQuantumInvariant(3,7);
            QI qiR = rG.optimizedQuantumInvariant(3,7);
            HomologyGroup hg = G.homologyGroupFromGBlink();
            HomologyGroup hgR = rG.homologyGroupFromGBlink();
            if (qi.isEqual(qiR) && hg.equals(hgR)) {
                System.out.println("Same QI and same HG");
            }
            else {
                System.out.println("PROBLEM! NOT the same QI or HG");
            }

            //G.
            JFrame f = new JFrame("Map View");
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            f.setSize(new Dimension(1024, 768));
            f.setContentPane(new PanelMapViewer(G));
            f.setVisible(true);

        } catch (IOException ex) {
            ex.printStackTrace();
        }*/
    }

    /**
     * Calculate the number of alternating zigzags
     */
    public int getNumberOfAlternatingZigZags() {
        // zig zags
        this.loadVariables();

        //
        int result = 0;
        for (Variable z : _varZigzags) {

            BlinkColor c = null;
            ArrayList<GBlinkVertex> list = z.getVertices();

            boolean alternating = true;
            for (int i = 0; i < list.size(); i += 2) {
                GBlinkVertex v = list.get(i);
                BlinkColor edgeColor = this.getColor(v.getEdgeLabel());
                if (edgeColor == c) {
                    alternating = false;
                    break;
                } else
                    c = edgeColor;
            }

            if (alternating) {
                // System.out.println("Is Alternating: " + z.toString());
                result++;
            } else {
                // System.out.println("Not Alternating: " + z.toString());
            }
        }

        return result;
    }

    // -----------------------------------------------------------------------
    // Break the blink map in a series of biconnected with "angle"-labels
    // maps for future reconstruction in a normalized form.
    //

    /**
     * Break a blink in blocks. All blocks preserve
     * the original parity.
     */
    public ArrayList<GBlink> breakMap() {

        // Save the name of the original zigzag (index on
        // "this.getZigZags()" list) for each GBlinkVertex.
        // Only this number is sufficient.
        HashMap<GBlinkVertex,Integer> v2zigZag = new HashMap<GBlinkVertex,Integer>();
        ArrayList<ArrayList<GBlinkVertex>> zigZags = this.getZigZags();
        for (int i=0;i<zigZags.size();i++) {
            for (GBlinkVertex v: zigZags.get(i)) {
                // System.out.println(""+v+" -> "+(i+1));
                v2zigZag.put(v,i+1);
            }
        }

        ArrayList<GBlinkVertex> blinkV = new ArrayList<GBlinkVertex>();

        for (GBlinkVertex v : _vertices)
            v.setFlag(false);

        // keep breaking the GBlink while there are breakpoints
        while (true) {

            boolean brokeMap = false;

            // for each g-vertex do
            for (GBlinkVertex v : _vertices) {
                if (v.getFlag() == true)
                    continue;

                // create list of GBlinkVertex of v's g-vertice
                blinkV.clear();
                GBlinkVertex u = v;
                GBlinkEdgeType t = GBlinkEdgeType.face;
                while (u.getFlag() == false) {
                    blinkV.add(u);
                    u.setFlag(true);
                    u = u.getNeighbour(t);
                    t = (t == GBlinkEdgeType.face ? GBlinkEdgeType.edge : GBlinkEdgeType.face);
                }

                //
                for (int i = 0; i < blinkV.size() && !brokeMap; i++) {
                    GBlinkVertex a = blinkV.get(i);

                    /**
                     * @todo desconfio (14/06/2006) que j podia
                     * ser de mesma paridade que i. Aqui estão
                     * diferentes. Mas dá no mesmo pela
                     * implementação de disconnect. Quando (b == na),
                     * coisa que acontece sempre na primeira iteração
                     * do laço j, disconnect é false.
                     */

                    for (int j = i + 1; j < blinkV.size() && !brokeMap; j+=2) {
                        GBlinkVertex b = blinkV.get(j);

                        // didn't find break point!
                        if (!disconnects(a, b))
                            continue;

                        // found a break point!!
                        GBlinkVertex na = a.getNeighbour(GBlinkEdgeType.edge);
                        GBlinkVertex nb = b.getNeighbour(GBlinkEdgeType.edge);

                        int lbl = v2zigZag.get(a);
                        a.setAngleLabel(lbl);
                        na.setAngleLabel(lbl);

                        GBlinkVertex.setNeighbours(a, b, GBlinkEdgeType.edge);
                        GBlinkVertex.setNeighbours(na, nb, GBlinkEdgeType.edge);

                        brokeMap = true;

                        // log
                        // System.out.println(String.format("Break map at  %d -- %d   and   %d -- %d   angle labels are now   %d",a.getLabel(),na.getLabel(),b.getLabel(),nb.getLabel(),lbl));

                    }
                }

                // unmark vertices from this blink vertex because of the break
                if (brokeMap)
                    for (GBlinkVertex x: blinkV)
                        x.setFlag(false);

            } // find break on blink vertex from map vertex "v"

            // there were no more breaks
            if (!brokeMap)
                break;

        } // keep in this until no more breaks are found


        // collect resulting connected components
        // to create new blinks
        ArrayList<GBlink> pieces = new ArrayList<GBlink>();

        for (GBlinkVertex v : _vertices)
            v.setFlag(false);

        ArrayList<GBlinkVertex> component = new ArrayList<GBlinkVertex>();
        for (GBlinkVertex v : _vertices) {
            if (v.getFlag() == true)
                continue;
            component.clear();
            Stack<GBlinkVertex> S = new Stack<GBlinkVertex>();
            v.setFlag(true);
            S.push(v);
            while (!S.isEmpty()) {
                GBlinkVertex u = S.pop();
                component.add(u);
                GBlinkVertex ue = u.getNeighbour(GBlinkEdgeType.edge);
                GBlinkVertex uf = u.getNeighbour(GBlinkEdgeType.face);
                GBlinkVertex uv = u.getNeighbour(GBlinkEdgeType.vertex);
                if (!ue.getFlag()) { ue.setFlag(true); S.push(ue); }
                if (!uf.getFlag()) { uf.setFlag(true); S.push(uf); }
                if (!uv.getFlag()) { uv.setFlag(true); S.push(uv); }
            }

            // create Blink with these vertices
            pieces.add(new GBlink(component));
        }

        // go to code label
        for (GBlink b: pieces)
            b.goToCodeLabelPreservingSpaceOrientation();

        return pieces;
    }

    /**
     * If angle from "a" and angle from "b" are removed,
     * "a" becomes disconnected from it's original
     * angle neighbour. By hypothesis a and b are on
     * the same fa-gon (same blink vertex).
     *
     * A pergunta é equivalente a:
     * a e b, que estão, por hipótese, no mesmo g-vértice,
     * estão na mesma g-face?
     */
    private boolean disconnects(GBlinkVertex a, GBlinkVertex b) {

        GBlinkVertex na = a.getNeighbour(GBlinkEdgeType.edge); // angle neighbour

        // atravessa toda a face va-gon a partir de "na"
        // (vizinho por angulo de "a"). "a" e "na" estão
        // na mesma face, porém, será que "b" está na
        // mesma face de "a" e "na"?
        GBlinkEdgeType t = GBlinkEdgeType.vertex;
        GBlinkVertex v = a;
        while (v != na && v != b) {
            v = v.getNeighbour(t);
            t = (t == GBlinkEdgeType.vertex ? GBlinkEdgeType.edge : GBlinkEdgeType.vertex);
        }

        if (v == na)
            return false;
        else
            return true;
    }

    /**
     * Get GZigZags of this GBlink. This is calculated on loadVariables.
     */
    public ArrayList<Variable> getGZigZags() {
        this.loadVariables();
        return (ArrayList<Variable>) _varZigzags.clone();
    }

    /**
     * Count the number of GZigZags (i.e. number of components on the framed link)
     */
    public int getNumberOfGZigZags() {
        this.loadVariables();
        return _varZigzags.size();
    }

    /**
     * Count the number of GZigZags (i.e. number of components on the framed link)
     */
    public int getNumberOfGFaces() {
        this.loadVariables();
        return _varFaces.size();
    }

    /**
     * Count the number of GZigZags (i.e. number of components on the framed link)
     */
    public int getNumberOfGVertices() {
        this.loadVariables();
        return _varVertices.size();
    }

    /**
     * Get GVertices of this GBlink. This is calculated on loadVariables.
     */
    public ArrayList<Variable> getGVertices() {
        this.loadVariables();
        return (ArrayList<Variable>) _varVertices.clone();
    }

    /**
     * Get GVertices of this GBlink. This is calculated on loadVariables.
     */
    public ArrayList<Variable> getGFaces() {
        this.loadVariables();
        return (ArrayList<Variable>) _varFaces.clone();
    }

    /**
     * Get GEdges (squares or GBlinkVertex) of this GBlink. This is not
     * calculated on the load variables.
     */
    public ArrayList<Variable> getGEdges() {
        ArrayList<Variable> gEdges = new ArrayList<Variable>();
        if (this.getNumberOfGEdges() > 0) { // empty case
            for (GBlinkVertex v : _vertices) v.setFlag(false);
            for (GBlinkVertex v : _vertices) {
                if (v.hasOddLabel() && v.getFlag() == false)
                    gEdges.add(new Variable(v, Variable.G_EDGE));
            }
        }
        return gEdges;
    }

    /**
     * Return the zigzags as a list of list of GBlinkVertex.
     */
    public ArrayList<ArrayList<GBlinkVertex>> getZigZags() {
        for (GBlinkVertex v: _vertices)
            v.setFlag(false);
        
        /**
         * @todo ok, there will be a list of vertices in the same
         * zigzag but it does not seem to be normalized in orientation
         * terms.
         */
        ArrayList<ArrayList<GBlinkVertex>> zigZags = new ArrayList<ArrayList<GBlinkVertex>>();
        for (GBlinkVertex v : _vertices) {
            if (v.getFlag() == true)
                continue;
            ArrayList<GBlinkVertex> zigZag = new ArrayList<GBlinkVertex>();
            Stack<GBlinkVertex> S = new Stack<GBlinkVertex>();
            v.setFlag(true);
            S.push(v);
            while (!S.isEmpty()) {
                GBlinkVertex u = S.pop();
                zigZag.add(u);
                GBlinkVertex ue = u.getNeighbour(GBlinkEdgeType.edge);
                GBlinkVertex ud = u.getNeighbour(GBlinkEdgeType.diagonal);
                if (!ue.getFlag()) { ue.setFlag(true); S.push(ue); }
                if (!ud.getFlag()) { ud.setFlag(true); S.push(ud); }
            }

            // add zigzags
            zigZags.add(zigZag);
        }

        return zigZags;

    }

    class BlinkInstance {
        GBlink _blink;
        public BlinkInstance(GBlink b) {
            _blink = b;
        }
        public GBlink getBlink() {
            return _blink;
        }
    }

    /**
     * Move tag breakpoint cut-angle-edge to angle-edge incident to the
     * vertex with the smallest label on the same zigzag. Return the
     * list of tags.
     */
    public HashSet<Integer> normalizeTaggedAngles() {
        HashSet<Integer> tagsFound = new HashSet<Integer>();
        ArrayList<ArrayList<GBlinkVertex>> zigZagList = this.getZigZags();
        for (ArrayList<GBlinkVertex> zigZag: zigZagList) {
            int tag = -1;
            GBlinkVertex smallestLabelVertex = null;
            for (GBlinkVertex v: zigZag) {
                if (smallestLabelVertex == null || v.getLabel() < smallestLabelVertex.getLabel()) {
                    smallestLabelVertex = v;
                }

                // has tag (once was a breakpoint angle edge)?
                if (v.hasAngleLabelDefined()) {
                    tag = v.getAngleLabel();
                    v.setAngleLabelAsUndefined();
                }
            }

            // if there was some tag then put it on the smallestLabelVertex
            if (tag != -1) {
                smallestLabelVertex.setAngleLabel(tag);
                tagsFound.add(tag);
            }
        }
        return tagsFound;
    }


    class DecompositionTreeNodeIterator {
        private int _depth;
        private boolean _finished;
        private DecompositionTreeNode _root;
        private DecompositionTreeNode _current;
        public DecompositionTreeNodeIterator(DecompositionTreeNode root) {
            _root = root;
            _finished = false;
            _depth = -1;
        }

        public boolean isFinished() {
            return _finished;
        }

        public int getDepth() {
            return _depth;
        }

        public DecompositionTreeNode getCurrent() {
            return _current;
        }

        /**
         * Get root.
         */
        public DecompositionTreeNode getRoot() {
            return _root;
        }

        /**
         * Get value of current node.
         */
        public int getValue() {
            return _current.nodeValue();
        }

        /**
         * go to next node. turns on the finished flag if it is finished.
         */
        public void next() {
            // first operation
            if (_current == null) {
                _current = _root;
                _depth = 0;
                return;
            }

            int k = (_current == _current.getParent() ? 1 : 2);

            // do _current have child different from its parent?
            if (_current.numNeighbours() > k) {
                _depth++;
                _current = _current.getNeighbour(k-1);
            }

            else {
                // find the next or finish
                while (true) {
                    // am I the root? then finished!
                    if (_current == _root) {
                        _depth = -1; // this encodes also that the procedure finished
                        _finished = true;
                        break;
                    }
                    else {
                        // is there another child of my parent?
                        int index = _current.getParent().indexOf(_current);
                        if (index < _current.getParent().numNeighbours()-1) {
                            _current = _current.getParent().getNeighbour(index+1);
                            break;
                        }
                        // go up once
                        else {
                            _current = _current.getParent();
                            _depth--;
                        }
                    }
                }
            }
        }
    }

    /**
     * DecompositionTreeNode is the base node class for the DecompositionTree.
     */
    abstract class DecompositionTreeNode {
        /**
         * Once the decomposition tree is rooted, then each node has one of its
         * neighbors as its parent. The only exception is the root, whose parent
         * is itself.
         */
        private DecompositionTreeNode _parent;

        /**
         * flag for some operations, for example: calculate
         * the center of the tree
         */
        private boolean _flag;

        /**
         * Neighbours of this node.
         */
        private ArrayList<DecompositionTreeNode> _neighbours = new ArrayList<DecompositionTreeNode>(2);
        public void addNeighbour(DecompositionTreeNode node) {
            _neighbours.add(node);
        }
        public int numNeighbours() {
            return _neighbours.size();
        }
        public ArrayList<DecompositionTreeNode> getNeighbours() {
            return _neighbours;
        }
        public void setParent(DecompositionTreeNode node) {
            _parent = node;
        }
        public DecompositionTreeNode getParent() {
            return _parent;
        }
        public boolean isFlagged() {
            return _flag == true;
        }
        public void flag() {
            _flag = true;
        }
        public void unflag() {
            _flag = false;
        }

        /**
         * Get neighbour by index. By convention, an already
         * organized tree has its index zero neighbor equal its parent.
         */
        public DecompositionTreeNode getNeighbour(int index) {
            return _neighbours.get(index);
        }

        /**
         * Define the parent recursevly. The parent must be one of the
         * neighbors or itself (in case of the root).
         */
        public void defineParentRecursevly(DecompositionTreeNode parent) {
            _parent = parent;
            for (DecompositionTreeNode n: _neighbours) {
                if (n != parent)
                    n.defineParentRecursevly(this);
            }
        }

        /**
         * Is the root node?
         */
        public boolean isRoot() {
            return this==_parent;
        }

        /**
         * Count number of neighbors of this node that is not flagged
         */
        public int countUnflaggedNeighbours() {
            int count = 0;
            for (DecompositionTreeNode n: _neighbours)
                if (!n.isFlagged())
                    count++;
            return count;
        }

        /**
         * Index of a neighbor on its list.
         */
        public int indexOf(DecompositionTreeNode n) {
            return _neighbours.indexOf(n);
        }

        /**
         * These trees have the parent as their
         * @param A DecompositionTreeNode
         * @param B DecompositionTreeNode
         * @return int
         */
        private int compareTrees(DecompositionTreeNode A, DecompositionTreeNode B) {

            // optimized
            if (A instanceof DecompositionTreeNodeBlock && B instanceof DecompositionTreeNodeBlock) {
                DecompositionTreeNodeBlock bA = (DecompositionTreeNodeBlock) A;
                DecompositionTreeNodeBlock bB = (DecompositionTreeNodeBlock) B;
                if (bA.nodeValue() < bB.nodeValue())
                    return -1;
                else if (bA.nodeValue() > bB.nodeValue())
                    return +1;
            }


            DecompositionTreeNodeIterator iA = new DecompositionTreeNodeIterator(A);
            DecompositionTreeNodeIterator iB = new DecompositionTreeNodeIterator(B);
            while (true) {
                iA.next();
                iB.next();

                if (iA.isFinished() && iB.isFinished())
                    return 0;
                else if (iA.isFinished())
                    return -1;
                else if (iB.isFinished())
                    return +1;
                else if (iA.getDepth() < iB.getDepth())
                    return -1;
                else if (iA.getDepth() > iB.getDepth())
                    return +1;
                else if (iA.getValue() < iB.getValue())
                    return -1;
                else if (iA.getValue() > iB.getValue())
                    return +1;
            }
        }


        /**
         * Organize the tree.
         */
        public void organize() {

            // first organize recursively
            for (DecompositionTreeNode n: _neighbours)
                if (n != this.getParent())
                    n.organize();

            // bring parent to first position
            if (this != _parent) {
                int index = _neighbours.indexOf(_parent);
                DecompositionTreeNode aux = _neighbours.get(0);
                _neighbours.set(0,_parent);
                _neighbours.set(index,aux);
            }

            // special integer
            int k = (this != _parent ? 1 : 0);

            // insertion sort now sort neighbours
            // (the neighbour that is also the parent comes first).
            for (int i = k+1; i < _neighbours.size(); i++) {
                DecompositionTreeNode pivot = this.getNeighbour(i);
                boolean swap = false;
                int j = i - 1;
                while (j > k-1 && compareTrees(pivot, this.getNeighbour(j)) < 0) {
                    _neighbours.set(j + 1, this.getNeighbour(j));
                    j--;
                    swap = true;
                }
                if (swap) {
                    j++;
                    _neighbours.set(j, pivot);
                }
            }
        }

        /**
         * Node value is important on the organization phase.
         */
        public abstract int nodeValue();

        /**
         * log tree
         */
        public void logTree(int k) {
            StringBuffer sb = new StringBuffer();
            for (int i=0;i<k;i++)
                sb.append("  ");
            if (this instanceof DecompositionTreeNodeBlock) {
                DecompositionTreeNodeBlock nn = (DecompositionTreeNodeBlock) this;
                sb.append("value: " + this.nodeValue() + " mapWord: " + nn.getBlock().getMapWord().toString() + " neighbours: "+this.numNeighbours());
            }
            else {
                DecompositionTreeNodeComponent nn = (DecompositionTreeNodeComponent) this;
                sb.append("value: " + this.nodeValue() + " component: " + nn.getComponent() + " neighbours: "+this.numNeighbours() );
            }
            System.out.println(""+sb.toString());
            for (DecompositionTreeNode n: _neighbours) {
                if (n != this.getParent())
                    n.logTree(k + 1);
            }
        }


    }

    /**
     * DecompositionTreeNodeBlock that keeps a block.
     */
    class DecompositionTreeNodeBlock extends DecompositionTreeNode {
        private GBlink _block;
        public DecompositionTreeNodeBlock(GBlink block) {
            _block = block;
        }
        public GBlink getBlock() {
            return _block;
        }
        public void addNeighbour(DecompositionTreeNode node) {
            if (node instanceof DecompositionTreeNodeComponent) {
                if (super._neighbours.contains(node))
                    System.out.println("Ooppss");
                super.addNeighbour(node);
            }
            else throw new RuntimeException("Not compatible neighbor");
        }

        public int nodeValue() {
            return _block.getValue();
        }
    }

    /**
     * DecompositionTreeNodeBlock that keeps a component.
     */
    class DecompositionTreeNodeComponent extends DecompositionTreeNode {
        private int _component;
        public DecompositionTreeNodeComponent(int component) {
            _component = component;
        }
        public int getComponent() {
            return _component;
        }
        public void addNeighbour(DecompositionTreeNode node) {
            if (node instanceof DecompositionTreeNodeBlock) {
                if (super._neighbours.contains(node))
                    System.out.println("Ooppss");
                super.addNeighbour(node);
            }
            else throw new RuntimeException("Not compatible neighbor");
        }

        public int nodeValue() {
            return 0;
        }
    }

    /**
     * Decomposition Tree structure.
     */
    class DecompositionTree {
        private HashMap<Integer,DecompositionTreeNodeComponent> _mapComponents = new HashMap<Integer,DecompositionTreeNodeComponent>();

        /**
         * All nodes on this tree
         */
        private ArrayList<DecompositionTreeNode> _nodes = new ArrayList<DecompositionTreeNode>();

        /**
         * Root of this tree (an invariant of the class of threes with same planar embedding).
         */
        private DecompositionTreeNode _root;


        public DecompositionTree(GBlink G, boolean dual, boolean reflection, boolean refDual) {
            GBlink GCopy = G.copy();

            // get blocks of this gblink with tagged angles
            ArrayList<GBlink> blockList = GCopy.breakMap();

            // dualize the blocks if the dual has a smaller code
            // loops becomes pendant edges.
            for (GBlink p : blockList) {
                if (dual)
                    p.goToDualIfSmallerCode();
                if (reflection)
                    p.goToReflectionIfSmallerCode();
                if (refDual)
                    p.goToRefDualIfSmallerCode();
            }

            // calculate a value (a number) for each block
            GBlink.sort(blockList);
            int nextFreeNumber = 1;
            for (int i = 0; i < blockList.size(); i++) {
                GBlink b = blockList.get(i);
                if (i == 0) {
                    b.setValue(nextFreeNumber++);
                }
                else {
                    GBlink bLast = blockList.get(i-1);
                    if (b.compareTo(bLast) == 0) {
                        b.setValue(bLast.getValue());
                    }
                    else {
                        b.setValue(nextFreeNumber++);
                    }
                }
            }

            // ------- LOG -------
            /*
            for (GBlink block : blockList) {
                System.out.println(block.getValue()+"  ->  "+block.getMapWord().toString());
            }*/

            // Create a block node for each block
            for (GBlink block : blockList) {

                // this does 2 things: normalizes the place where
                // the tagged angle is on each block and returns a
                // census of the components of the block
                HashSet<Integer> tags = block.normalizeTaggedAngles();

                // create decomposition block
                DecompositionTreeNodeBlock nBlock = new DecompositionTreeNodeBlock(block);

                // define the associated block as being itself. this will
                // be important on the merge phase of this algorithm.
                block.setAssociatedGBlink(block);

                // add neighbors
                for (int tag: tags) {
                    DecompositionTreeNodeComponent nComponent = getDecompositionTreeNodeComponent(tag);
                    nBlock.addNeighbour(nComponent);
                    nComponent.addNeighbour(nBlock);
                }

                // add to complete list of nodes
                _nodes.add(nBlock);
            }

            // now it is time to root the tree: find the
            // center of the graph.
            ArrayList<DecompositionTreeNode> listOfNodes = (ArrayList<DecompositionTreeNode>) _nodes.clone();
            ArrayList<DecompositionTreeNode> removalList = new ArrayList<DecompositionTreeNode>();
            while (true) {

                // found the center
                if (listOfNodes.size() == 1) {
                    break;
                }

                if (listOfNodes.size() == 2) {
                    throw new RuntimeException("Tree has 2 centers! This should not be possible!");
                }

                // flag all vertices with only one neighbour not flagged.
                removalList.clear();
                for (DecompositionTreeNode node: listOfNodes) {
                    if (node.countUnflaggedNeighbours() == 1) {
                        removalList.add(node);
                    }
                }

                // flag removed nodes
                for (DecompositionTreeNode node: removalList) {
                    node.flag();
                }

                // remove all nodes
                listOfNodes.removeAll(removalList);

            }

            // define center of the tree as the root and
            // define all parents recursevly.
            _root = listOfNodes.get(0);
            _root.defineParentRecursevly(_root);

            // now it is time to organize the tree
            _root.organize();

            //
            // _root.logTree(0);
        }

        public void mergeGBlinksOnComponent(GBlink A, GBlink B, int component) {
            // find merge vertex of A
            GBlinkVertex vA = null;
            for (GBlinkVertex auxA: A.getVertices()) {
                if (auxA.hasAngleLabelDefined() && auxA.getAngleLabel() == component) {
                    vA = auxA;
                    break;
                }
            }

            // find merge vertex of B
            GBlinkVertex vB = null;
            for (GBlinkVertex auxB: B.getVertices()) {
                if (auxB.hasAngleLabelDefined() && auxB.getAngleLabel() == component) {
                    vB = auxB;
                    break;
                }
            }

            // test to see if everything is all right!
            if (vA == null || vB == null) {
                throw new RuntimeException("OOoooppssss");
            }

            // put the vertices on GBlink A
            if (A != B)
                A._vertices.addAll(B._vertices);

            // get the neighbors of vA and vB
            GBlinkVertex vAn = vA.getNeighbour(GBlinkEdgeType.edge);
            GBlinkVertex vBn = vB.getNeighbour(GBlinkEdgeType.edge);
            if (vA.labelParity() == vB.labelParity()) {
                GBlinkVertex.setNeighbours(vA,vBn,GBlinkEdgeType.edge);
                GBlinkVertex.setNeighbours(vB,vAn,GBlinkEdgeType.edge);
            }
            else {
                GBlinkVertex.setNeighbours(vA,vB,GBlinkEdgeType.edge);
                GBlinkVertex.setNeighbours(vAn,vBn,GBlinkEdgeType.edge);
            }

            // update connecting-edge
            if (vA.hasOddLabel() && vB.hasOddLabel()) {
                vA.setAngleLabelAsUndefined();
            }
            else if (vA.hasOddLabel() && vB.hasEvenLabel()) {
                vA.setAngleLabelAsUndefined();
                vB.setAngleLabelAsUndefined();
                vBn.setAngleLabel(component);
            }
            else if (vA.hasEvenLabel() && vB.hasOddLabel()) {
                vA.setAngleLabelAsUndefined();
            }
            else if (vA.hasEvenLabel() && vB.hasEvenLabel()) {
                vA.setAngleLabelAsUndefined();
                vB.setAngleLabelAsUndefined();
                vBn.setAngleLabel(component);
            }

            // update associated GBlink.
            B.setAssociatedGBlink(A);

        }

        public GBlink merge() {
            // merge all components on the defined order
            for (DecompositionTreeNodeComponent c: _mapComponents.values()) {

                // Get GBlink
                GBlink A = ((DecompositionTreeNodeBlock)c.getNeighbour(0)).getBlock();
                while (A != A.getAssociatedGBlink())
                    A = A.getAssociatedGBlink();

                // neighbours
                for (int i=1;i<c.numNeighbours();i++) {
                    GBlink B = ((DecompositionTreeNodeBlock)c.getNeighbour(i)).getBlock();
                    while (B != B.getAssociatedGBlink())
                        B = B.getAssociatedGBlink();

                    // merge A and B (B connects to A)
                    this.mergeGBlinksOnComponent(A,B,c.getComponent());
                }
            }

            // everything became connected so
            // find the final GBlink!
            DecompositionTreeNodeBlock aBlock = (_root instanceof DecompositionTreeNodeComponent ?
                    (DecompositionTreeNodeBlock) (_root.getNeighbour(0)) :
                    (DecompositionTreeNodeBlock) _root);
            GBlink result = aBlock.getBlock();
            while (result.getAssociatedGBlink() != result)
                result = result.getAssociatedGBlink();

            result.goToCodeLabelPreservingSpaceOrientation();

            return result;
        }


        /**
         * Gets the DecompositionTreeNodeComponent for component named "component". If
         * there isn't one yet, it a new one is created and returned.
         */
        public DecompositionTreeNodeComponent getDecompositionTreeNodeComponent(int component) {
            DecompositionTreeNodeComponent result = _mapComponents.get(component);
            if (result == null) {
                result = new DecompositionTreeNodeComponent(component);
                _mapComponents.put(component,result);
                _nodes.add(result);
            }
            return result;
        }

    }

    /**
     * GBlink decomposition tree
     */
    public DecompositionTree getDecompositionTree(boolean dual, boolean reflection, boolean refDual) {
        return new DecompositionTree(this,dual, reflection, refDual);
    }

    /**
     * GBlink decomposition tree
     */
    public GBlink getNewRepresentant(boolean dual, boolean reflection, boolean refDual) {
        return this.getDecompositionTree(dual, reflection, refDual).merge();
    }

    /**
     * GBlink decomposition tree
     */
    public GBlink getRepresentantNotPreservingOrientation() {
        GBlink G1 = this.getNewRepresentant(true,true,true);
        GBlink G2 = this.copy();
        G2.swap(false,true,false);
        G2 = G2.getNewRepresentant(true, true, true);
        if (G1.compareTo(G2) < 0)
            return G1;
        else return G2;
    }

    /**
     * GBlink decomposition tree
     */
    public GBlink getNewRepresentant() {
        return this.getDecompositionTree(true,true,true).merge();
    }

    /**
     * GBlink representant
     * @return GBlink
     */
    public GBlink getRepresentant() {
        GBlink b = this.copy();

        // get blocks of this gblink with tagged angles
        ArrayList<GBlink> bpieces = b.breakMap();

        // dualize the blocks if the dual has a smaller code
        // loops becomes pendant edges.
        for (GBlink p: bpieces)
            p.goToDualIfSmallerCode();

        // if there is only one block, then
        // it is the result.
        if (bpieces.size() == 1)
            return bpieces.get(0);

        GBlink.sort(bpieces);

        ArrayList<BlinkInstance> pieces = new ArrayList<BlinkInstance>(bpieces.size());
        for (GBlink bp: bpieces) {
            pieces.add(new BlinkInstance(bp));
        }

        HashMap<BlinkInstance,Integer> blinkLabel = new HashMap<BlinkInstance,Integer>();
        HashMap<BlinkInstance,BlinkInstance> parentBlink = new HashMap<BlinkInstance,BlinkInstance>();

        // initialize the parent of each blink
        // as itself. Give a unique label to each
        // blink 1...k.
        int lbl = 1;
        for (BlinkInstance p: pieces) {
            parentBlink.put(p,p);
            blinkLabel.put(p, lbl++);
        }

        HashMap<Integer,ArrayList<GBlinkVertex>> mapAV = new HashMap<Integer,ArrayList<GBlinkVertex>>();
        HashMap<GBlinkVertex,BlinkInstance> mapVB = new HashMap<GBlinkVertex,BlinkInstance>();

        // for every labeled "angle edge" on every
        // "blink piece", traversed in order, find the
        // "angle edge" incident on the same g-zigzag
        // that is incident to the smallest vertex "u"
        // (smallest in the sense of the code label of
        // the "blink piece"). Save the blink of "u" on
        // mapVB concatenate "u" to a "angleLabel"'s list.
        for (BlinkInstance pi: pieces) {
            GBlink p = pi.getBlink();
            ArrayList<GBlinkVertex> vtas = p.getVerticesWithTaggedAngles();
            for (GBlinkVertex v: vtas) {
                int angleLabel = v.getAngleLabel();
                GBlinkVertex u = p.getMinOddVertexOnTheSameZigzag(v);
                mapVB.put(u,pi);
                ArrayList<GBlinkVertex> list = mapAV.get(angleLabel);
                if (list == null) {
                    list = new ArrayList<GBlinkVertex>();
                    mapAV.put(angleLabel,list);
                }
                list.add(u);
                // System.out.println("add b"+blinkLabel.get(pi)+"v"+v.getLabel()+" to angle "+angleLabel);
            }
        }

        // The pasting phase:
        // for every angle label let's do the pasting.
        BlinkInstance result = null;
        for (int angleLabel: mapAV.keySet()) {
            // System.out.println("Angle Label: "+angleLabel);

            ArrayList<GBlinkVertex> vs = mapAV.get(angleLabel);

            // fix the first

            GBlinkVertex v0 = vs.get(0);
            BlinkInstance  bi0 = mapVB.get(v0);
            GBlink  b0 = bi0.getBlink();

            // if b0 has already been pasted to other
            // piece as a child, find the top most
            // parent and say it is b0.

            //int lbl_0 = blinkLabel.get(b0);
            while (parentBlink.get(bi0) != bi0)
                bi0 = parentBlink.get(bi0);

            // System.out.println("Blink "+lbl_0+" -> "+blinkLabel.get(b0));
            // System.out.println("Blink "+blinkLabel.get(b0)+" -> "+blinkLabel.get(parentBlink.get(b0)));
            // b0 = parentBlink.get(b0);
            result = bi0;

            // for every other angle edge with the same angleLabel
            // let's do the pasting. And update it's parent.

            for (int i=1;i<vs.size();i++) {
                GBlinkVertex vi = vs.get(i);

                BlinkInstance bii = mapVB.get(vi);
                //int lbl_i = blinkLabel.get(bi);
                while (parentBlink.get(bii) != bii)
                    bii = parentBlink.get(bii);
                // System.out.println("Blink "+lbl_i+" -> "+blinkLabel.get(bi));

                // System.out.println(String.format( "Merging blinks %d and %d on vertices %d and %d",blinkLabel.get(b0),blinkLabel.get(bi),v0.getLabel(),vi.getLabel()));
                // b0.write();
                // bi.write();
                GBlink.merge(bi0.getBlink(),v0,bii.getBlink(),vi);
                // System.out.println("Resulted in...");
                // b0.write();

                parentBlink.put(bii,bi0);
            }
        }

        return result.getBlink();
    }

    //
    // Break the blink map in a series of biconnected with "angle"-labels
    // maps for future reconstruction in a normalized form.
    // -----------------------------------------------------------------------



    // ------------------------------------------------------------------------
    // Routines find points of vertice splitting and face subdivision that
    // do not destroy the property of 3-T-Connected.

    public ArrayList<SplittingPoint> findVertexSplittingPoints() {
        this.loadVariables();
        ArrayList<SplittingPoint> result = new ArrayList<SplittingPoint>();
        for (Variable var: _varVertices) {
            int n = var.size();
            if (n < 8) // no splitting point
                continue;

            // the vertices come in a face-angle order (list[1] to list[2] is face, 1 to 2 is angle...)
            // list[0] is an odd vertex
            // list[1] is an even vertex
            // list[0] to list[1] is a angle edge
            ArrayList<GBlinkVertex> list = var.getVertices();
            for (int i=1;i<n;i+=2) {
                GBlinkVertex u = list.get(i);
                int k = 3;
                while (k < n-3) {
                    GBlinkVertex v = list.get( (i+k) % n );
                    result.add(new SplittingPoint(u,v));
                    k+=2;
                }
            }
        }
        return result;
    }

    public ArrayList<SplittingPoint> findFaceSplittingPoints() {
        this.loadVariables();
        ArrayList<SplittingPoint> result = new ArrayList<SplittingPoint>();
        for (Variable var: _varFaces) {
            int n = var.size();
            if (n < 8) // no splitting point
                continue;

            // the vertices come in a face-vertex order (0 to 1 is face, 1 to 2 is angle...)
            // the vertices come in a face-angle order (list[0] to list[1] is face, 1 to 2 is angle...)
            ArrayList<GBlinkVertex> list = var.getVertices();
            for (int i=0;i<n;i+=2) {
                GBlinkVertex u = list.get(i);
                int k = 3;
                while (k < n-3) {
                    GBlinkVertex v = list.get((i+k) % n);
                    result.add(new SplittingPoint(u,v));
                    k+=2;
                }
            }
        }
        return result;
    }

    public MapPackedWord simulateVertexSplitting(SplittingPoint p) {

        GBlinkVertex u = p.getU();
        GBlinkVertex uuuu = u.getNeighbour(GBlinkEdgeType.edge);

        GBlinkVertex v = p.getV();
        GBlinkVertex vvvv = v.getNeighbour(GBlinkEdgeType.edge);

        GBlinkVertex uu = new GBlinkVertex();
        GBlinkVertex uuu = new GBlinkVertex();
        GBlinkVertex vv = new GBlinkVertex();
        GBlinkVertex vvv = new GBlinkVertex();

        GBlinkVertex.setNeighbours(u,uu,GBlinkEdgeType.edge);
        GBlinkVertex.setNeighbours(uu,uuu,GBlinkEdgeType.vertex);
        GBlinkVertex.setNeighbours(uuu,uuuu,GBlinkEdgeType.edge);

        GBlinkVertex.setNeighbours(v,vv,GBlinkEdgeType.edge);
        GBlinkVertex.setNeighbours(vv,vvv,GBlinkEdgeType.vertex);
        GBlinkVertex.setNeighbours(vvv,vvvv,GBlinkEdgeType.edge);

        GBlinkVertex.setNeighbours(uu,vv,GBlinkEdgeType.face);
        GBlinkVertex.setNeighbours(uuu,vvv,GBlinkEdgeType.face);

        uu.setOvercross(!u.overcross());
        uuu.setOvercross(!uu.overcross());
        vv.setOvercross(!v.overcross());
        vvv.setOvercross(!vv.overcross());

        // vertices...
        _vertices.add(uu); _vertices.add(vv);
        _vertices.add(uuu); _vertices.add(vvv);

        int[] lbl = this.goToCodeLabelAndDontCareAboutSpaceOrientation();
        MapPackedWord result = new MapPackedWord(lbl,false);

        _vertices.remove(uu); _vertices.remove(vv);
        _vertices.remove(uuu); _vertices.remove(vvv);

        GBlinkVertex.setNeighbours(u,uuuu,GBlinkEdgeType.edge);
        GBlinkVertex.setNeighbours(v,vvvv,GBlinkEdgeType.edge);

        return result;

    }

    public MapPackedWord simulateFaceSplitting(SplittingPoint p) {

        GBlinkVertex u = p.getU();
        GBlinkVertex uuuu = u.getNeighbour(GBlinkEdgeType.edge);

        GBlinkVertex v = p.getV();
        GBlinkVertex vvvv = v.getNeighbour(GBlinkEdgeType.edge);

        GBlinkVertex uu = new GBlinkVertex();
        GBlinkVertex uuu = new GBlinkVertex();
        GBlinkVertex vv = new GBlinkVertex();
        GBlinkVertex vvv = new GBlinkVertex();

        GBlinkVertex.setNeighbours(u,uu,GBlinkEdgeType.edge);
        GBlinkVertex.setNeighbours(uu,uuu,GBlinkEdgeType.face);
        GBlinkVertex.setNeighbours(uuu,uuuu,GBlinkEdgeType.edge);

        GBlinkVertex.setNeighbours(v,vv,GBlinkEdgeType.edge);
        GBlinkVertex.setNeighbours(vv,vvv,GBlinkEdgeType.face);
        GBlinkVertex.setNeighbours(vvv,vvvv,GBlinkEdgeType.edge);

        GBlinkVertex.setNeighbours(uu,vv,GBlinkEdgeType.vertex);
        GBlinkVertex.setNeighbours(uuu,vvv,GBlinkEdgeType.vertex);

        uu.setOvercross(!u.overcross());
        uuu.setOvercross(!uu.overcross());
        vv.setOvercross(!v.overcross());
        vvv.setOvercross(!vv.overcross());

        // vertices...
        _vertices.add(uu); _vertices.add(vv);
        _vertices.add(uuu); _vertices.add(vvv);

        int[] lbl = this.goToCodeLabelAndDontCareAboutSpaceOrientation();
        MapPackedWord result = new MapPackedWord(lbl,false);

        /*
        GBlink g = new GBlink(result);
        GBlink g2 = new GBlink(result);
        g.goToCodeLabelAndDontCareAboutSpaceOrientation();
        g.setColor(0);
        if (!g.equals(g2)) {
            System.out.println("this: "+this.getBlinkWord().toString());
            System.out.println("array: "+Library.intArrayToString(lbl));
            System.out.println("g:  "+g.getBlinkWord().toString());
            System.out.println("g2: "+g2.getBlinkWord().toString());
            System.out.println("oooppss");
        } */

        _vertices.remove(uu); _vertices.remove(vv);
        _vertices.remove(uuu); _vertices.remove(vvv);

        GBlinkVertex.setNeighbours(u,uuuu,GBlinkEdgeType.edge);
        GBlinkVertex.setNeighbours(v,vvvv,GBlinkEdgeType.edge);

        return result;

    }

    // Routines find points of vertice splitting and face subdivision that
    // do not destroy the property of 3-T-Connected.
    // ------------------------------------------------------------------------

    /**
     * Red Edges
     * @return int[]
     */
    public int[] getReds() {

        ArrayList<Integer> redEdges = new ArrayList<Integer>();
        for (GBlinkVertex v: _vertices) {
            if (v.getLabel() % 4 == 1 && v.undercross())
                redEdges.add((v.getLabel() - 1) / 4 + 1);
        }

        /*
                 for (int c=1;c<=this.getNumberOfGEdges();c++)
            if (this.getColor(c) == BlinkColor.red)
                redEdges.add(c);
        */

        int[] reds = new int[redEdges.size()];
        int k = 0;
        for (int i : redEdges)
            reds[k++] = i;

        Arrays.sort(reds);

        return reds;
    }

    public BlinkWord getBlinkWord() {
        return new BlinkWord(this.getMapWordArray(),getReds());
    }

    /**
     * Compare the number of edges first, then the map labeling.
     * @todo WHAT ABOUT THE COLOR OF THE EDGES?
     */
    public int compareTo(GBlink b) {
        return this.compareTo((Object) b);
        /*
        if (this.getNumberOfGEdges() < b.getNumberOfGEdges())
            return -1;
        else if (this.getNumberOfGEdges() > b.getNumberOfGEdges())
            return 1;
        return this.getMapWord().compareTo(b.getMapWord()); */
    }

    public int compareToXXX(Object o) {
        GBlink A = this;
        GBlink B = (GBlink) o;

        int r1 = this.compareTo(o);
        int r2 = this.getBlinkWord().compareTo(B.getBlinkWord());

        if (r1 < 0) r1 = -1;
        if (r1 > 0) r1 = 1;
        if (r2 < 0) r2 = -1;
        if (r2 > 0) r2 = 1;

        if (r1 != r2){
            System.out.println("Copare Problem");
            System.out.println(""+A.getBlinkWord().toString());
            System.out.println(""+B.getBlinkWord().toString());
        }
        return r1;
    }
    public int compareTo(Object o) {
        GBlink A = this;
        GBlink B = (GBlink) o;

        //System.out.println(""+A.getBlinkWord().toString());
        //System.out.println(""+B.getBlinkWord().toString());

        if (A.getNumberOfGEdges() < B.getNumberOfGEdges())
            return -1;
        else if (A.getNumberOfGEdges() > B.getNumberOfGEdges())
            return 1;

        int countRedA = 0;
        int countRedB = 0;
        int untieUndercross = 0;
        for (int i=1;i<=_vertices.size();i+=2) {
            GBlinkVertex a = A.findVertex(i);
            GBlinkVertex b = B.findVertex(i);
            int lbla = a.getNeighbour(GBlinkEdgeType.edge).getLabel();
            int lblb = b.getNeighbour(GBlinkEdgeType.edge).getLabel();
            if (lbla < lblb)
                return -1;
            else if (lbla > lblb)
                return 1;

            if (i % 4 == 1) {
                if (a.undercross())
                    countRedA++;
                if (b.undercross())
                    countRedB++;

                if (untieUndercross == 0) {
                    if (a.undercross() && b.overcross())
                        untieUndercross = -1;
                    else if (a.overcross() && b.undercross())
                        untieUndercross = 1;
                }
            }
        }

        if (countRedA < countRedB)
            return -1;
        else if (countRedA > countRedB)
            return +1;
        else if (untieUndercross != 0)
            return untieUndercross;
        else return 0;

        // return this.getBlinkWord().compareTo(other.getBlinkWord());
    }

    public int hashCode() {
        return this.getBlinkWord().hashCode();
    }

    public boolean equals(Object o) {
        return this.compareTo(o) == 0;
    }

    /**
     * This method returns true if there more than one zigzags
     * and one zigzag is all under or all over the others. Otherwise
     * it returns false.
     */
    public boolean hasAGZigZagThatIsFreeOfTheOthers() {
        ArrayList<Variable> zigzags = this.getGZigZags();

        // only one zigzag?
        if (zigzags.size() == 1)
            return false;

        // becareful with self crossings.
        // they do not count
        for (Variable z: zigzags) {
            int n = z.size();

            // get list of vertices one for each sequential pair [v1] v2 [v3] ... [v(2n-1)] v(2n)
            ArrayList<GBlinkVertex> zigZagVertices = new ArrayList<GBlinkVertex>();
            for (int i=0;i<n;i+=2) {
                GBlinkVertex v = z.getVertex(i);
                zigZagVertices.add(v);
            }

            int o = 0;
            int u = 0;

            for (GBlinkVertex v: zigZagVertices) {
                // is is a self crossing?
                if (zigZagVertices.contains(v.getNeighbour(GBlinkEdgeType.face))
                    || zigZagVertices.contains(v.getNeighbour(GBlinkEdgeType.vertex)))
                    continue;

                if (v.overcross())
                    o++;
                else
                    u++;
            }

            if (u == 0 || o == 0) {
                // log zigzag
                return true;
            }
        }

        // found no free zigzag so the result is false
        return false;

    }

    /**
     * find all ReidemeisterIII Points of application
     */
    public ArrayList<PointOfReidemeisterIII> findAllReidemeisterIIIPoints() {
        ArrayList<PointOfReidemeisterIII> result = new ArrayList<PointOfReidemeisterIII>();
        for (GBlinkVertex v: _vertices) {
            if (!v.hasOddLabel()) continue;
            if (PointOfReidemeisterIII.test(v,GBlinkEdgeType.face)) {
                result.add(new PointOfReidemeisterIII(v.getLabel(),GBlinkEdgeType.face));
            }
            else if (PointOfReidemeisterIII.test(v,GBlinkEdgeType.vertex)) {
                result.add(new PointOfReidemeisterIII(v.getLabel(),GBlinkEdgeType.vertex));
            }
        }
        return result;
    }

    /**
     * find all PointOfAdjacentOppositeCurls of application
     */
    public ArrayList<PointOfAdjacentOppositeCurls> findAllPointOfAdjacentOppositeCurls() {
        ArrayList<PointOfAdjacentOppositeCurls> result = new ArrayList<PointOfAdjacentOppositeCurls>();
        for (GBlinkVertex v: _vertices) {
            if (!v.hasOddLabel()) continue;
            if (PointOfAdjacentOppositeCurls.test(v,GBlinkEdgeType.face)) {
                result.add(new PointOfAdjacentOppositeCurls(v.getLabel(),GBlinkEdgeType.face));
            }
            else if (PointOfAdjacentOppositeCurls.test(v,GBlinkEdgeType.vertex)) {
                result.add(new PointOfAdjacentOppositeCurls(v.getLabel(),GBlinkEdgeType.vertex));
            }
        }
        return result;
    }

    /**
     * find all PointOfAdjacentOppositeCurls of application
     */
    public ArrayList<PointOfAlpha1Move> findAllPointOfAlpha1Move() {
        ArrayList<PointOfAlpha1Move> result = new ArrayList<PointOfAlpha1Move>();
        for (GBlinkVertex v: _vertices) {
            int k;
            if ( (k = PointOfAlpha1Move.test(v,GBlinkEdgeType.face)) != 0) {
                result.add(new PointOfAlpha1Move(v.getLabel(),GBlinkEdgeType.face,k));
            }
            else if ( (k = PointOfAlpha1Move.test(v,GBlinkEdgeType.vertex)) != 0) {
                result.add(new PointOfAlpha1Move(v.getLabel(),GBlinkEdgeType.vertex,k));
            }
        }
        return result;
    }

    /**
     * find all ReidemeisterIII Points of application
     */
    public ArrayList<PointOfReidemeisterII> findAllReidemeisterIIPoints() {
        ArrayList<PointOfReidemeisterII> result = new ArrayList<PointOfReidemeisterII>();
        for (GBlinkVertex v: _vertices) {
            if (!v.hasOddLabel()) continue;
            if (PointOfReidemeisterII.test(v,GBlinkEdgeType.face)) {
                result.add(new PointOfReidemeisterII(v.getLabel(),GBlinkEdgeType.face));
            }
            else if (PointOfReidemeisterII.test(v,GBlinkEdgeType.vertex)) {
                result.add(new PointOfReidemeisterII(v.getLabel(),GBlinkEdgeType.vertex));
            }
        }
        return result;
    }

    /**
     * contains at least one spot to simplify
     */
    public boolean containsSimplifyingReidemeisterIIPoint() {
        for (GBlinkVertex v: _vertices) {
            if (!v.hasOddLabel()) continue;
            if (PointOfReidemeisterII.test(v,GBlinkEdgeType.face)) {
                return true;
            }
            else if (PointOfReidemeisterII.test(v,GBlinkEdgeType.vertex)) {
                return true;
            }
        }
        return false;
    }

    /**
     * contains at least one spot to simplify
     */
    public boolean containsReidemeisterIIIPoint() {
        for (GBlinkVertex v: _vertices) {
            if (!v.hasOddLabel()) continue;
            if (PointOfReidemeisterIII.test(v,GBlinkEdgeType.face)) {
                return true;
            }
            else if (PointOfReidemeisterIII.test(v,GBlinkEdgeType.vertex)) {
                return true;
            }
        }
        return false;
    }

    /**
     * contains point of opposite adjacent curls
     */
    public boolean containsPointOfAdjacentOppositeCurls() {
        for (GBlinkVertex v: _vertices) {
            if (!v.hasOddLabel()) continue;
            if (PointOfAdjacentOppositeCurls.test(v,GBlinkEdgeType.face)) {
                return true;
            }
            else if (PointOfAdjacentOppositeCurls.test(v,GBlinkEdgeType.vertex)) {
                return true;
            }
        }
        return false;
    }

    /**
     * contains point of opposite adjacent curls
     */
    public boolean containsPointOfAlpha1Move() {
        for (GBlinkVertex v: _vertices) {
            if (PointOfAlpha1Move.test(v,GBlinkEdgeType.face) != 0) {
                return true;
            }
            else if (PointOfAlpha1Move.test(v,GBlinkEdgeType.vertex) != 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * applyEliminationOfAdjacentOppositeCurls
     */
     public void applyEliminationOfAdjacentOppositeCurls(PointOfAdjacentOppositeCurls p) {
        GBlinkEdgeType t1 = p.getType();
        GBlinkEdgeType t2 = (t1 == GBlinkEdgeType.face ? GBlinkEdgeType.vertex : GBlinkEdgeType.face);

        GBlinkVertex a = this.findVertex(p.getBaseVertexLabel());
        GBlinkVertex b = a.getNeighbour(t1);
        GBlinkVertex c = b.getNeighbour(t2);
        GBlinkVertex d = c.getNeighbour(t1);
        GBlinkVertex e = d.getNeighbour(GBlinkEdgeType.edge);
        GBlinkVertex f = e.getNeighbour(t1);
        GBlinkVertex g = f.getNeighbour(t2);
        GBlinkVertex h = g.getNeighbour(t1);

        GBlinkVertex na = a.getNeighbour(GBlinkEdgeType.edge);
        GBlinkVertex nh = h.getNeighbour(GBlinkEdgeType.edge);
        if (na == h) {
            throw new RuntimeException("Oooopsss disconnects!!");
        }

        _vertices.remove(a);
        _vertices.remove(b);
        _vertices.remove(c);
        _vertices.remove(d);
        _vertices.remove(e);
        _vertices.remove(f);
        _vertices.remove(g);
        _vertices.remove(h);

        GBlinkVertex.setNeighbours(na,nh,GBlinkEdgeType.edge);
    }

    /**
     * applyEliminationOfAdjacentOppositeCurls
     */
     public void applyAlpha1Move(PointOfAlpha1Move p) {
        GBlinkEdgeType t1 = p.getType();
        GBlinkEdgeType t2 = (t1 == GBlinkEdgeType.face ? GBlinkEdgeType.vertex : GBlinkEdgeType.face);

        int caseOfMove = p.getCaseOfMove();

        GBlinkVertex a = this.findVertex(p.getBaseVertexLabel());
        GBlinkVertex b = a.getNeighbour(t1);
        GBlinkVertex c = b.getNeighbour(t2);
        GBlinkVertex d = c.getNeighbour(t1);
        GBlinkVertex e = d.getNeighbour(GBlinkEdgeType.edge);

        boolean eIsOvercross = e.overcross();

        GBlinkVertex f,g,h,i,j,k,l;
        if (caseOfMove == 1) {
            f = e.getNeighbour(t2);
            g = f.getNeighbour(t1);
            h = g.getNeighbour(t2);
            i = h.getNeighbour(GBlinkEdgeType.edge);
            j = i.getNeighbour(t1);
            k = j.getNeighbour(t2);
            l = k.getNeighbour(t1);
        }
        else {
            f = e.getNeighbour(t1);
            g = f.getNeighbour(t2);
            h = g.getNeighbour(t1);
            i = h.getNeighbour(GBlinkEdgeType.edge);
            j = i.getNeighbour(t1);
            k = j.getNeighbour(t2);
            l = k.getNeighbour(t1);
        }

        GBlinkVertex.setNeighbours(b,c,GBlinkEdgeType.edge);
        GBlinkVertex.setNeighbours(d,l.getNeighbour(GBlinkEdgeType.edge),GBlinkEdgeType.edge);

        if ((caseOfMove==1 && eIsOvercross) || (caseOfMove==2 && !eIsOvercross)) {
            a.setOvercross(true);
            b.setOvercross(false);
            c.setOvercross(true);
            d.setOvercross(false);
        }
        else if ((caseOfMove==1 && !eIsOvercross) || (caseOfMove==2 && eIsOvercross)) {
            a.setOvercross(false);
            b.setOvercross(true);
            c.setOvercross(false);
            d.setOvercross(true);
        }

        _vertices.remove(e);
        _vertices.remove(f);
        _vertices.remove(g);
        _vertices.remove(h);
        _vertices.remove(i);
        _vertices.remove(j);
        _vertices.remove(k);
        _vertices.remove(l);
    }



    /**
     * apply simplifying reidemeister move II
     */
     public void applyReidemeisterIIMove(PointOfReidemeisterII p) {
        GBlinkEdgeType t1 = p.getType();
        GBlinkEdgeType t2 = (t1 == GBlinkEdgeType.face ? GBlinkEdgeType.vertex : GBlinkEdgeType.face);

        GBlinkVertex a = this.findVertex(p.getBaseVertexLabel());
        GBlinkVertex b = a.getNeighbour(t1);
        GBlinkVertex c = b.getNeighbour(GBlinkEdgeType.edge);
        GBlinkVertex d = c.getNeighbour(t1);

        // aa = a prime on the notebook
        GBlinkVertex aa = a.getNeighbour(t2);
        GBlinkVertex bb = b.getNeighbour(t2);
        GBlinkVertex cc = c.getNeighbour(t2);
        GBlinkVertex dd = d.getNeighbour(t2);

        GBlinkVertex naa = aa.getNeighbour(GBlinkEdgeType.edge);
        GBlinkVertex nbb = bb.getNeighbour(GBlinkEdgeType.edge);
        GBlinkVertex ncc = cc.getNeighbour(GBlinkEdgeType.edge);
        GBlinkVertex ndd = dd.getNeighbour(GBlinkEdgeType.edge);

        if (naa != dd && nbb != cc) {
            GBlinkVertex.setNeighbours(ndd, naa, GBlinkEdgeType.edge);
            GBlinkVertex.setNeighbours(nbb, ncc, GBlinkEdgeType.edge);
        }
        else throw new RuntimeException("Would create a g-blink without vertices");

        _vertices.remove(a);
        _vertices.remove(b);
        _vertices.remove(c);
        _vertices.remove(d);
        _vertices.remove(aa);
        _vertices.remove(bb);
        _vertices.remove(cc);
        _vertices.remove(dd);

        this.goToCodeLabelPreservingSpaceOrientation();
    }

    public ArrayList<GBlink> getReidemeisterIIIClosure() {
        // unprocessed
        Queue<GBlink> U = new LinkedList<GBlink>();

        //
        ArrayList<GBlink> list = new ArrayList<GBlink>();
        list.add(this);

        U.offer(this);
        while (!U.isEmpty()) {
           GBlink G = U.poll();
           ArrayList<PointOfReidemeisterIII> points = G.findAllReidemeisterIIIPoints();
           for (PointOfReidemeisterIII p: points) {
               GBlink Gt = G.copy();
               //System.out.println("Blink: "+Gt.getBlinkWord().toString());
               //System.out.println("Point: "+p.basePointDescription(Gt));
               Gt.applyReidemeisterIIIMove(p);
               Gt.goToCodeLabelPreservingSpaceOrientation();
               if (!list.contains(Gt)) {
                   list.add(Gt);
                   U.offer(Gt);
               }
           }
        }

        return list;
    }


    /**
     * apply ReidemeisterIII
     */
    public void applyReidemeisterIIIMove(PointOfReidemeisterIII p) {
        p.apply(this);
    }

    /**
     * Random GBlink with numGEdges edges
     */
    public static GBlink random(int numGEdges) {
        Random r = new Random();

        GBlink G;

        // start with a single loop or a single edge g-blink
        if (r.nextBoolean()) {
            G = new GBlink(new MapWord(new int[] {1,2}));
        }
        else {
            G = new GBlink(new MapWord(new int[] {2,1}));
        }


        // random
        for (int i=2;i<=numGEdges;i++) {

            double prob = r.nextDouble();



            // add a pendand edge in a random angle edge
            if (prob < 0.40) {
                ArrayList<Variable> gvertices = G.getGVertices();
                Variable gvertex = gvertices.get(r.nextInt(gvertices.size()));
                GBlinkVertex v = gvertex.getVertex(r.nextInt(gvertex.size()));
                G.addIsolatexVertex(v);
            }

            // add a loop
            else if (prob < 0.48) {
                ArrayList<Variable> gvertices = G.getGVertices();
                Variable gvertex = gvertices.get(r.nextInt(gvertices.size()));
                GBlinkVertex v = gvertex.getVertex(r.nextInt(gvertex.size()));
                G.addLoop(v);
            }

            // add a subdivision on a g-face
            else { // if (operation == 2) {
                ArrayList<Variable> faces = G.getGFaces();
                for (int ii=faces.size()-1;ii>=0;ii--)
                    if (faces.get(ii).size()<=2)
                        faces.remove(ii);

                if (faces.size() == 0) {
                    i--;
                    continue;
                }

                Variable gface = faces.get(r.nextInt(faces.size()));
                int index1 = r.nextInt(gface.size()/2);
                // int index2 = r.nextInt(gface.size()/2);
                //while (index2 == index1) {
                //    index2 = r.nextInt(gface.size()/2);
                //}
                int index2 = (index1 + gface.size()/4) % (gface.size()/2);

                // divide face
                G.addFaceDivision(gface.getVertex(2*index1),gface.getVertex(2*index2));
            }


            // normalize labeling
            G.goToCodeLabelPreservingSpaceOrientation();
            G.resetVariablesLoaded();
        }

        // randomly choose edge colors
        for (int i=1;i<=G.getNumberOfGEdges();i++) {
            G.setColor(i,(r.nextBoolean() ? BlinkColor.green : BlinkColor.red));
        }

        return G;
    }

    /**
     * Check if gblink contains a break pair
     * vertices in the same g-face and g-vertex
     * @return boolean
     */
    public boolean containsABreakpair() {

        // for each g-vertex do
        ArrayList<Variable> gvertices = this.getGVertices();
        for (Variable gv : gvertices) {
            //
            for (int i = 0; i < gv.size(); i++) {
                GBlinkVertex a = gv.getVertex(i);

                /**
                 * @todo desconfio (14/06/2006) que j podia
                 * ser de mesma paridade que i. Aqui estão
                 * diferentes. Mas dá no mesmo pela
                 * implementação de disconnect. Quando (b == na),
                 * coisa que acontece sempre na primeira iteração
                 * do laço j, disconnect é false.
                 */

                for (int j = i + 1; j < gv.size(); j += 2) {
                    GBlinkVertex b = gv.getVertex(j);

                    // didn't find break point!
                    if (disconnects(a, b))
                        return true;
                }
            }
        }
        return false;
    }

    public ArrayList<GBlinkVertex> getOneVertexForEachGZigzag() {
        ArrayList<GBlinkVertex> result = new ArrayList<GBlinkVertex>();
        for (Variable v: this.getGZigZags()) {
            result.add(v.getVertex(0));
        }
        return result;
    }
}







class SplittingPoint {
    GBlinkVertex _u;
    GBlinkVertex _v;
    public SplittingPoint(GBlinkVertex u, GBlinkVertex v) {
        _u = u;
        _v = v;
    }
    public GBlinkVertex getU() {
        return _u;
    }
    public GBlinkVertex getV() {
        return _v;
    }
}

class EdgePartitionPart {
    ArrayList<GBlinkVertex> _gEdgeBaseVertices = new ArrayList<GBlinkVertex>();
    public EdgePartitionPart() {
    }
    public void add(GBlinkVertex v) {
        _gEdgeBaseVertices.add(v);
    }
    public void assign(GBlink b, BlinkColor c) {
        for (GBlinkVertex i: _gEdgeBaseVertices) {
            b.setColor(i,c);
        }
    }
    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (GBlinkVertex i: _gEdgeBaseVertices) {
            sb.append(i.getEdgeLabel());
            sb.append(" ");
        }
        return sb.toString();
    }
}

/**
 * Class GBlinkBigon: a polygon alternating 2 types of edges
 * A and B in a GBlink, where (A,B) in {
 *    (EdgeType.face, EdgeType.edge),      // g-vertex or black face
 *    (EdgeType.vertex, EdgeType.edge)     // g-face or white face
 *    (EdgeType.edge, EdgeType.diagonal)   // g-zigzag or link component
 *    (EdgeType.face, EdgeType.vertex)     // g-crossing a crossing on a gblink
 */
class GBlinkBigon {

    // aligned to starting with an odd vertex
    public static final GBlinkEdgeType[] G_VERTICE_EDGE_TYPES = {GBlinkEdgeType.edge, GBlinkEdgeType.face};
    public static final GBlinkEdgeType[] G_FACE_EDGE_TYPES    = {GBlinkEdgeType.vertex, GBlinkEdgeType.edge};
    public static final GBlinkEdgeType[] G_ZIGZAG_EDGE_TYPES  = {GBlinkEdgeType.edge, GBlinkEdgeType.diagonal};
    public static final GBlinkEdgeType[] G_EDGE_EDGE_TYPES    = {GBlinkEdgeType.face, GBlinkEdgeType.vertex};
    public static final int G_VERTICE = 0;
    public static final int G_FACE = 1;
    public static final int G_ZIGZAG = 2;
    public static final int G_EDGE = 3;
    private int _type;
    private ArrayList<GBlinkVertex> _vertices;

    /**
     * Empty variable. Base case.
     * @param type int
     */
    public GBlinkBigon(int type) {
        _type = type;
        _vertices = new ArrayList<GBlinkVertex>();
    }

    public GBlinkBigon(GBlinkVertex root, int type) {
        _type = type;
        _vertices = new ArrayList<GBlinkVertex>();

        GBlinkEdgeType[] ets = null;
        switch (_type) {
        case G_VERTICE:
            ets = G_VERTICE_EDGE_TYPES;
            break;
        case G_FACE:
            ets = G_FACE_EDGE_TYPES;
            break;
        case G_ZIGZAG:
            ets = G_ZIGZAG_EDGE_TYPES;
            break;
        case G_EDGE:
            ets = G_EDGE_EDGE_TYPES;
            break;
        default:
            throw new RuntimeException("OOOoooooppsss");
        }

        _vertices.add(root);

        int k = 0;
        GBlinkVertex v = root;
        while ( (v = v.getNeighbour(ets[k])) != root) {
            _vertices.add(v);
            k = (k + 1) % ets.length;
        }
    }

    /**
     * Number of vertices
     */
    public int size() {
        return _vertices.size();
    }

    public boolean contains(GBlinkVertex v) {
        return _vertices.contains(v);
    }

    public ArrayList<GBlinkVertex> getVertices() {
        return _vertices;
    }

    /**
     * Return vertex at index "index"
     */
    public GBlinkVertex getVertex(int index) {
        return _vertices.get(index);
    }

    /**
     * Return the nex vertice on the orientation
     */
    public GBlinkVertex next(GBlinkVertex v) {
        int index =_vertices.indexOf(v);
        if (index < 0)
            return null;
        else return _vertices.get((index + 1)%_vertices.size());
    }

    public String getTypeString() {
        return (_type == G_VERTICE ? "G_VERTEX" : (_type == G_FACE ? "G_FACE" : (_type == G_EDGE ? "G_EDGE" : "G_ZIGZAG")));
    }

    public String toString() {
        StringBuffer st = new StringBuffer();
        for (GBlinkVertex v: _vertices)
            st.append(v.getLabel()+" ");
        return String.format("%-10s: %s",getTypeString(),st.toString());
    }

    public String getLabel() {
        if (this.size() == 0)
            return (_type == G_VERTICE ? "V" : _type == G_FACE ? "F" : "Z");
        else
            return (_type == G_VERTICE ? "V" : _type == G_FACE ? "F" : "Z") + _vertices.get(0).getLabel();
    }
}


/**
 * Class variable
 */
class Variable extends GBlinkBigon {

    public Variable(int type) {
        super(type);
    }

    public Variable(GBlinkVertex root, int type) {
        super(root,type);
        for (GBlinkVertex v: this.getVertices()) {
            if (v.getFlag() == true)
                System.out.println("Problema!!!");
            v.setFlag(true);
        }
    }

    // ----------------------------------------
    // first position
    int _firstPosition = -1;
    public int firstPosition() {
        return _firstPosition;
    }
    public void setFirstPosition(int p) {
        _firstPosition = p;
    }
    public void assignFirstPositionIfNotDefined(int p) {
        if (_firstPosition == -1)
            _firstPosition = p;
    }
    // first position
    // ----------------------------------------

    // ----------------------------------------
    // first position
    int _value = -1;
    public int getValue() {
        return _value;
    }
    public void setValue(int v) {
        _value = v;
    }
    // first position
    // ----------------------------------------

    // ----------------------------------------
    // index
    int _index = -1;
    public int index() {
        return _index;
    }
    public void setIndex(int i) {
        _index = i;
    }
    public boolean isIndexDefined() {
        return _index != -1;
    }
    // first position
    // ----------------------------------------

    public static final int UNASSIGNED = -1;

}
