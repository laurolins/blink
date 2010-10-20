package blink;

import java.awt.Dimension;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import javax.swing.JFrame;

import org.jscience.mathematics.matrices.Vector;
import org.jscience.mathematics.numbers.Float64;

import edu.uci.ics.jung.utils.Pair;

/**
 * GemEmbedding
 */
public class GemEmbedding {

    Gem _originalJJGem;
    Gem _workGem;
    Stack<InverseDipole> _S;

    ArrayList<RTetrahedron> _tets = new ArrayList<RTetrahedron>();
    RTetrahedron _internalTetrahedron;
    RTetrahedron _externalTetrahedron;

    RPoint _a,_b,_c,_d;

    public static final int _U = 100000;
    public static final BigInteger _DEN = BigInteger.valueOf(100);
    public static final BigInteger _NUM1 = BigInteger.valueOf(10*_U);
    public static final BigInteger _NUM2 = BigInteger.valueOf(90*_U);

    /**
     * Map of old labels to new labels
     * and the inverse map.
     */
    HashMap<Integer,Integer> _mapOld2New = new HashMap<Integer,Integer>();
    HashMap<Integer,Integer> _mapNew2Old = new HashMap<Integer,Integer>();

    /**
     * Component to zero cell
     */
    HashMap<Object,EVertice> _mapComponentToZeroCell = new HashMap<Object,EVertice>();

    public GemEmbedding(Gem jjg) throws FileNotFoundException {
        _originalJJGem = jjg;
        _workGem = jjg.copy();

        //
        Stack<Integer> originalLabels = new Stack<Integer>();

        _S = new Stack<InverseDipole>();
        while (true) {

            // dipole
            Dipole d = _workGem.findAnyTwoDipoleOnColors(GemColor.blue, GemColor.green);
            if (d != null) {
                InverseDipole id = d.getInverseDipole();

                // push labels
                originalLabels.push(id.getEvenNewLabel());
                originalLabels.push(id.getOddNewLabel());

                _S.push(id);
                System.out.println("Applying dipole: " + d.toString() + " whose inverse is " + id.toString());
                _workGem.cancelDipole(d);
                continue;
            }

            d = _workGem.findAnyTwoDipoleOnColors(GemColor.blue, GemColor.red);
            if (d != null) {
                InverseDipole id = d.getInverseDipole();

                // push labels
                originalLabels.push(id.getEvenNewLabel());
                originalLabels.push(id.getOddNewLabel());

                _S.push(id);
                System.out.println("Applying dipole: " + d.toString() + " whose inverse is " + id.toString());
                _workGem.cancelDipole(d);
                continue;
            }

            break;

        }

        if (_workGem.getNumVertices() > 2) {
            throw new RuntimeException("Not an S3 gem!");
        }

        { // complete the list
            GemVertex v0 = _workGem.getVertex(1);
            GemVertex v1 = _workGem.getVertex(2);
            if (v0.hasEvenLabel()) {
                originalLabels.push(v0.getLabel());
                originalLabels.push(v1.getLabel());
            } else {
                originalLabels.push(v1.getLabel());
                originalLabels.push(v0.getLabel());
            }
        } // complete the list

        // at this point, we have destroyed by
        // dipole 12 or dipole 13 cancelations and
        // registered the way back to the original
        // gem. Here it is easy to embedd the dual
        // of this simple gem (2 vertices).

        // prepare relabeling map
        int i = 0;
        while (!originalLabels.isEmpty()) {
            int lbl = originalLabels.pop();
            _mapOld2New.put(lbl, i);
            _mapNew2Old.put(i, lbl);
            System.out.println("" + lbl + " -> " + i);
            i++;
        }

        // points
        RPoint a = new RPoint( -_U, -_U, -_U);
        a.setType(RPointType.vertice);
        a.setName("A");
        _a = a;
        RPoint b = new RPoint( +_U, +_U, -_U);
        b.setType(RPointType.vertice);
        b.setName("B");
        _b = b;
        RPoint c = new RPoint( -_U, +_U, +_U);
        c.setType(RPointType.vertice);
        c.setName("C");
        _c = c;
        RPoint d = new RPoint( +_U, -_U, +_U);
        d.setType(RPointType.vertice);
        d.setName("D");
        _d = d;

        RSegment sab = new RSegment(a, b);
        RSegment sac = new RSegment(a, c);
        RSegment sad = new RSegment(a, d);
        RSegment sbc = new RSegment(b, c);
        RSegment sbd = new RSegment(b, d);
        RSegment scd = new RSegment(c, d);

        RArc ab = new RArc(sab);
        RArc ac = new RArc(sac);
        RArc ad = new RArc(sad);
        RArc bc = new RArc(sbc);
        RArc bd = new RArc(sbd);
        RArc cd = new RArc(scd);

        // define the triangle to point out
        RTriangle ta = new RTriangle(sbc, scd, sbd);
        RTriangle tb = new RTriangle(sac, sad, scd);
        RTriangle tc = new RTriangle(sab, sbd, sad);
        RTriangle td = new RTriangle(sab, sac, sbc);

        RFace fa = new RFace(b, c, d, bc, cd, bd, GemColor.yellow);
        fa.addTriangle(ta);

        RFace fb = new RFace(a, c, d, ac, cd, ad, GemColor.blue);
        fb.addTriangle(tb);

        RFace fc = new RFace(a, b, d, ab, bd, ad, GemColor.red);
        fc.addTriangle(tc);

        RFace fd = new RFace(a, b, c, ab, bc, ac, GemColor.green);
        fd.addTriangle(td);

        // the first two tetrahedras
        _internalTetrahedron = new RTetrahedron(
                _workGem.getVertices().get(0).getLabel(),
                fa, false,
                fb, false,
                fc, false,
                fd, false);
        _externalTetrahedron = new RTetrahedron(
                _workGem.getVertices().get(1).getLabel(),
                fa, true,
                fb, true,
                fc, true,
                fd, true);
        _tets.add(_internalTetrahedron);
        _tets.add(_externalTetrahedron);

        System.out.println("Internal Tet: " + _internalTetrahedron.getLabel());
        System.out.println("External Tet: " + _externalTetrahedron.getLabel());


        // include dipoles and everything...
        // now do the thing
        int count = 0;

        // LOG
        for (RTetrahedron tet: _tets) {
            new TetrahedronToEPS(tet,count);
        }

        while (!_S.isEmpty()) {
            InverseDipole id = _S.pop();
            System.out.println("" + id);
            GemColor c1 = id.getEdgeColor1();
            GemColor c2 = id.getEdgeColor2();

            GemVertex v1 = _workGem.findVertex(id.getEdgeVertex1());
            RTetrahedron t1 = this.findTetrahedron(v1.getLabel());
            RTetrahedron t1c1 = this.findTetrahedron(v1.getNeighbour(c1).getLabel());

            GemVertex v2 = _workGem.findVertex(id.getEdgeVertex2());
            RTetrahedron t2 = this.findTetrahedron(v2.getLabel());
            RTetrahedron t2c2 = this.findTetrahedron(v2.getNeighbour(c2).getLabel());

            RFace f1 = t1.getFace(c1);
            RFace f2 = t2.getFace(c2);

            System.out.println("\n\nFace 1");
            System.out.println("" + f1.toString());
            System.out.println("\n\nFace 2");
            System.out.println("" + f2.toString());
            System.out.println("\n\n");

            // go the next gem
            _workGem.applyInverseDipole(id);

            // RFace.facesProjection(f1,f2,(c1==GemColor.red || c2==GemColor.red));
            // RFace.findSurfacePath(f1,f2);
            RFace[] faces = refine(t1,t2,f1, f2, t1.invertNormal(c1), t2.invertNormal(c2),c1,c2,
                            id.getNewLabel1(), id.getNewLabel2());

            RTetrahedron tNew1 = new RTetrahedron(id.getNewLabel1());
            tNew1.setFace(c1, f1, !t1.invertNormal(c1));
            tNew1.setFace(c2, f2, !t2.invertNormal(c2));
            tNew1.setFace(getFaceColor(faces[0]), faces[0], false);
            tNew1.setFace(getFaceColor(faces[1]), faces[1], false);

            // System.out.println(""+tNew1.toString());

            RTetrahedron tNew2 = new RTetrahedron(id.getNewLabel2());
            tNew2.setFace(getFaceColor(faces[0]), faces[0], true);
            tNew2.setFace(getFaceColor(faces[1]), faces[1], true);
            tNew2.setFace(getFaceColor(faces[2]), faces[2], t1.invertNormal(c1));
            tNew2.setFace(getFaceColor(faces[3]), faces[3], t2.invertNormal(c2));

            // update the faces...
            t1c1.setFace(c1, faces[2], t1c1.invertNormal(c1));
            t2c2.setFace(c2, faces[3], t2c2.invertNormal(c2));

            // faces
            new FacesToEPS(
            new RFace[] {f1,f2,faces[0],faces[1],faces[2],faces[3]},
            count,id.toString(),
                    _a,_b,_c,_d);

            // System.out.println(""+tNew2.toString());

            _tets.add(tNew1);
            _tets.add(tNew2);

            // normalize triangles
            for (RTetrahedron tet : _tets) {
                if (tet.getLabel() % 2 == 0)
                    continue;
                if (tet == _externalTetrahedron)
                    continue;
                this.normalizeTriangles(tet);
            }

            count++;

            // LOG
            for (RTetrahedron tet: _tets) {
                new TetrahedronToEPS(tet,count);
            }

            if (count == 3)
                break;

        }

        this.testEmbedding();

        /*RFace f[] = RFace.refine(fa,fc);
          RTetrahedron t1 = new RTetrahedron(1,fa,f[0],fc,f[1]);
          RTetrahedron t2 = new RTetrahedron(3,f[0],f[1],f[2],f[3]);
          _tets.add(t1);
          _tets.add(t2);*/

        try {
            PrintWriter pw = new PrintWriter("c:/zembedding.dxf");
            printDXF(pw);
            pw.close();
        } catch (FileNotFoundException ex) {
        }

        /*
                 {
            RTetrahedron t1 = this.findTetrahedron(1);
            RTetrahedron t5 = this.findTetrahedron(5);

            RArc arc1 = t1.getB().getArc(a,c);
            RArc arc2 = t5.getB().getArc(a,c);

            RArc arc3 = t1.getC().getArc(b,d);
            RArc arc4 = t5.getC().getArc(b,d);

            PrintWriter pw = new PrintWriter("c:/zlink.dxf");
            DXF.printDXFLayersTableBegin(pw, 2);
            DXF.printDXFTetrahedronLayerInfo(pw, t1);
            DXF.printDXFTetrahedronLayerInfo(pw, t5);
            DXF.printDXFLayersTableEnd(pw);

            DXF.printDXFHeader(pw);
            for (RSegment s: arc1.getSegments()) {
                DXF.printSegmentDXF(pw,s,"T1AP");
            }
            for (RSegment s: arc2.getSegments()) {
                DXF.printSegmentDXF(pw,s,"T1AP");
            }
            for (RSegment s: arc3.getSegments()) {
                DXF.printSegmentDXF(pw,s,"T1BP");
            }
            for (RSegment s: arc4.getSegments()) {
                DXF.printSegmentDXF(pw,s,"T1BP");
            }
            DXF.printDXFFooter(pw);

            pw.close();
                 }*/




        {

            PrintWriter pw = new PrintWriter("c:/zpaths.dxf");
            DXF.printDXFLayersTableBegin(pw, 100);
            for (RTetrahedron tet : _tets) {
                if (tet.getLabel() % 2 == 1)
                    DXF.printDXFTetrahedronLayerInfo(pw, tet);
            }
            DXF.printDXFLayersTableEnd(pw);

            DXF.printDXFHeader(pw);
            for (RTetrahedron tet : _tets) {
                if (tet.getLabel() % 2 == 1) {
                    RArc arcac = tet.getB().getArc(a, c);
                    for (RSegment s : arcac.getSegments()) {
                        DXF.printSegmentDXF(pw, s, "T" + tet.getLabel() + "ACP");
                    }
                    RArc arcad = tet.getB().getArc(a, d);
                    for (RSegment s : arcad.getSegments()) {
                        DXF.printSegmentDXF(pw, s, "T" + tet.getLabel() + "ADP");
                    }
                    RArc arcbc = tet.getA().getArc(b, c);
                    for (RSegment s : arcbc.getSegments()) {
                        DXF.printSegmentDXF(pw, s, "T" + tet.getLabel() + "BCP");
                    }
                    RArc arcbd = tet.getA().getArc(b, d);
                    for (RSegment s : arcbd.getSegments()) {
                        DXF.printSegmentDXF(pw, s, "T" + tet.getLabel() + "BDP");
                    }
                }
            }
            DXF.printDXFFooter(pw);

            pw.close();
        }

        for (RTetrahedron t: _tets) {
            Log.log(3,t.toString());
        }
        System.out.println("Finished");

    }

    private void testEmbedding() {
        // batizar os segmentos e os triangulos
        HashMap<RSegment,String> mapS = new HashMap<RSegment,String>();
        HashMap<RTriangle,String> mapT = new HashMap<RTriangle,String>();

        for (RTetrahedron tet: _tets) {
            if (tet.getLabel() % 2 == 0)
                continue;

            // face
            RFace faces[] = {tet.getA(),tet.getB(),tet.getC(),tet.getD()};
            for (int i=0;i<faces.length;i++) {
                RFace f = faces[i];
                String name = "" + (char)((int) 'A' + i);
                for (RSegment s : f.getSegments()) {
                    mapS.put(s,
                             "seg(" + s.getA().getNameIfExistsOtherwiseId() + "," +
                             s.getB().getNameIfExistsOtherwiseId() + ") T" + tet.getLabel() + name);
                }
                for (RTriangle t : f.getTriangles()) {
                    mapT.put(t,t.toString()+" T"+tet.getLabel() + name);
                }
            }
        }

        Set<RSegment> S = mapS.keySet();
        Set<RTriangle> T = mapT.keySet();
        Log.log(2, "There are "+S.size()+ " segments.");
        Log.log(2, "There are "+T.size()+ " triangles.");
        int count =0;
        for (RSegment s: S) {
            for (RTriangle t: T) {
                LineAndPlanIntersection I = new LineAndPlanIntersection(t,s);
                if (I.noIntersection() || I.onThePlan())
                    continue;
                if (!t.hitTest(I.getIntersectionPoint()))
                    continue;
                if (I.getParameter().compareTo(BigRational.ZERO) > 0 &&
                    I.getParameter().compareTo(BigRational.ONE) < 0) {
                    Log.log(2, "Intersection "+(++count)+"\n" + mapS.get(s) + "\n" + mapT.get(t) + "\n"+
                            I.getParameter().toStringDot(20)+"\n");
                }
            }
        }
    }


    private void normalizeTriangles(RTetrahedron tet) {

        ArrayList<RTriangle> T = new ArrayList<RTriangle>();
        T.addAll(tet.getA().getTriangles());
        T.addAll(tet.getB().getTriangles());
        T.addAll(tet.getC().getTriangles());
        T.addAll(tet.getD().getTriangles());

        //
        for (int i=0;i<T.size();i++) {
            RTriangle t = T.get(i);
            HashSet<BigRational> parameters = new HashSet<BigRational>();
            RPoint c = t.getMiddle();
            RPoint currentNormal = t.getNormal();
            RSegment s = new RSegment(c,c.add(currentNormal));
            for (int j=0;j<T.size();j++) {
                if (i==j) continue;
                RTriangle tj = T.get(j);
                LineAndPlanIntersection I = new LineAndPlanIntersection(tj,s);
                RPoint ip = I.getIntersectionPoint();
                if (ip != null &&
                    I.getParameter().compareTo(BigRational.ZERO) > 0 &&
                    tj.hitTest(ip)) {
                    parameters.add(I.getParameter());
                }
            }
            if (parameters.size() % 2 == 1) {
                System.out.println("invert...");
                t.invertOrientation();
            }
        }

        // adjust the things
        for (GemColor c: GemColor.values()) {
            tet.setInvertNormal(c, false);
            GemVertex v = _workGem.findVertex(tet.getLabel());
            RTetrahedron tc = this.findTetrahedron(v.getNeighbour(c).getLabel());
            tc.setInvertNormal(c,true);
        }
    }

    public GemColor getColor(RPoint p) {
        if (_a.equals(p)) return GemColor.yellow;
        else if (_b.equals(p)) return GemColor.blue;
        else if (_c.equals(p)) return GemColor.red;
        else if (_d.equals(p)) return GemColor.green;
        else throw new RuntimeException();
    }


    public RFace[] refine(
            RTetrahedron t1,
            RTetrahedron t2,
            RFace f1,
            RFace f2,
            boolean invertNormal1,
            boolean invertNormal2,
            GemColor c1,
            GemColor c2,
            int newLabel1,
            int newLabel2) {

        System.out.println("\n\n----------\n\nREFINE FACES\n\n----------\n\n");

        // for every segment and every point
        // calculate a normal vector
        NormalRepository R = new NormalRepository();
        for (RTriangle t: f1.getTriangles()) {
            RPoint normal = t.getNormal();
            if (invertNormal1)
                normal.invert();

            R.addNormal(t.getA(),normal);
            R.addNormal(t.getB(),normal);
            R.addNormal(t.getC(),normal);
            R.addNormal(t.getS1(),normal);
            R.addNormal(t.getS2(),normal);
            R.addNormal(t.getS3(),normal);
        }
        for (RTriangle t: f2.getTriangles()) {
            RPoint normal = t.getNormal();
            if (invertNormal2)
                normal.invert();
            R.addNormal(t.getA(),normal);
            R.addNormal(t.getB(),normal);
            R.addNormal(t.getC(),normal);
            R.addNormal(t.getS1(),normal);
            R.addNormal(t.getS2(),normal);
            R.addNormal(t.getS3(),normal);
        }

        // find out common arc
        RArc commonArc = RFace.getCommonArc(f1,f2);
        if (commonArc == null) throw new RuntimeException("ooooooooopssss");

        RPoint x = commonArc.getFirstPoint();
        RPoint y = commonArc.getLastPoint();
        RPoint u = f1.getOppositeVertice(commonArc);
        RPoint v = f2.getOppositeVertice(commonArc);

        System.out.println("\n\n\n f1 is ");
        System.out.println(f1.toString());
        System.out.println("\n\n f2 is ");
        System.out.println(f2.toString());

        try {
            PrintWriter pw = new PrintWriter("c:/z2faces.dxf");
            DXF.printDXF(pw,f1,f2);
            pw.close();
        } catch (FileNotFoundException ex) {
        }

        System.out.println("u = "+u.getName());
        System.out.println("v = "+v.getName());
        System.out.println("x = "+x.getName());
        System.out.println("y = "+y.getName());

        // find path on surface of f1 and f2
        ArrayList L = findSurfacePath(f1,f2,invertNormal1,invertNormal2,R);

        // update some normals
        for (Object o: L) {
            if (!(o instanceof RSegment))
                continue;
            RSegment s = (RSegment) o;
            RPoint middle = s.getMiddlePoint();
            R.addNormal(middle,R.getNormal(s));
        }

        // test the normal
        Log.log(0, "Step " + LAYERS);
        Log.log(0, ""+t1.getLabel()+GemColor.getColorsCompactStringABCD(f1.getColor())+" "+t2.getLabel()+GemColor.getColorsCompactStringABCD(f2.getColor()));
        for (RTriangle t: f1.getTriangles()) {
            RPoint normal = t.getNormal();
            if (invertNormal1)
                normal.invert();

            Log.log(0, ""+t+" "+normal.toString());


            RPoint[] list = {t.getA(),t.getB(),t.getC()};
            for (RPoint p: list) {
                BigRational dotProduct = RPoint.dotProduct(R.getNormal(p).approxNormalize(), normal.approxNormalize());
                if (dotProduct.compareTo(BigRational.ZERO) < 0) {
                    Log.log(0, "Normal Problem at " + p.getNameIfExistsOtherwiseId() +" with normal of "+t+" "+dotProduct.toStringDot(20));
                }
            }
        }
        for (RTriangle t: f2.getTriangles()) {
            RPoint normal = t.getNormal();
            if (invertNormal2)
                normal.invert();
            Log.log(0, ""+t+" "+normal.toString());
            RPoint[] list = {t.getA(),t.getB(),t.getC()};
            for (RPoint p: list) {
                BigRational dotProduct = RPoint.dotProduct(R.getNormal(p).approxNormalize(), normal.approxNormalize());
                if (dotProduct.compareTo(BigRational.ZERO) < 0) {
                    Log.log(0, "Normal Problem at " + p.getNameIfExistsOtherwiseId() +" with normal of "+t+" "+dotProduct.toStringDot(20));
                }
            }
        }

        // log things...
        Log.log(1,R.toString());

        // create segment set: S
        HashSet<RSegment> S = new HashSet<RSegment>();

        // build new arcs
        RArc[] arcs1 = f1.getArcsIncidentToVertice(u);
        RArc[] arcs2 = f2.getArcsIncidentToVertice(v);

        // these arcs will be exactelly
        // the same lets put them on the set
        S.addAll(arcs1[0].getSegments());
        S.addAll(arcs1[1].getSegments());
        S.addAll(arcs2[0].getSegments());
        S.addAll(arcs2[1].getSegments());

        // points that are fixed
        HashSet<RPoint> E = new HashSet<RPoint>();
        for (RSegment s: S) {
            E.add(s.getA());
            E.add(s.getB());
        }

        // break segments
        HashSet<RSegment> brokenSegments = new HashSet<RSegment>();
        HashMap<RSegment,Pair> piecesMap = new HashMap<RSegment,Pair>();
        for (Object o: L) {
            if (!(o instanceof RSegment))
                continue;
            RSegment s = (RSegment) o;
            brokenSegments.add(s);
            RPoint mp = s.getMiddlePoint();
            // mp.setName(s.getA().getName()+s.getB().getName()+"/2");
            ArrayList<RSegment> pieces = s.breakSegment(mp);
            piecesMap.put(s,new Pair(pieces.get(0),pieces.get(1)));
            S.addAll(pieces);
        }

        // add uv arc
        RArc uvArc = new RArc();
        for (int i=0;i<L.size()-1;i++) {
            RPoint p[] = {null,null};
            Object pair[] = {L.get(i),L.get(i+1)};
            for (int j=0;j<pair.length;j++) {
                Object o = pair[j];
                if (o instanceof RSegment) {
                    RSegment ss = (RSegment) o;
                    p[j] = ss.getB().add(ss.getA()).scale(new BigRational(1, 2));
                } else if (o instanceof RPoint) {
                    p[j] = (RPoint) o;
                } else
                    throw new RuntimeException();
            }
            RSegment s = new RSegment(p[0],p[1]);
            S.add(s);
            uvArc.addSegment(s);
        }

        // add internal segments not already in...
        for (RSegment s : f1.getInternalSegments()) {
            if (!brokenSegments.contains(s)) {
                S.add(s);
            }
        }
        for (RSegment s : f2.getInternalSegments()) {
            if (!brokenSegments.contains(s)) {
                S.add(s);
            }
        }

        // the segments of arc xy not intersected!
        for (RSegment s: commonArc.getSegments()) {
            if (!brokenSegments.contains(s))
                S.add(s);
        }

        // find triangles that are
        // divided in the middle point
        for (int i=1;i<L.size()-2;i++) {
            RSegment s1 = (RSegment) L.get(i);
            RSegment s2 = (RSegment) L.get(i+1);

            // triangle t
            RTriangle t = f1.getTriangleThatContainsSegments(s1,s2);
            if (t == null) {
                t = f2.getTriangleThatContainsSegments(s1,s2);
            }
            if (t == null) throw new RuntimeException();

            //
            RPoint commonPoint = RSegment.getCommonPoint(s1,s2);
            RPoint p1 = s1.getOpposite(commonPoint);

            RSegment s = new RSegment(p1,s2.getMiddlePoint());
            S.add(s);
        }


        /**
         * From the set of segments S build up
         * de RefineGraph data structure that
         * will help rest of the refine process.
         *
         * The set S has as it's elements
         * 1. segments of the arcs incident to "u" and to "v"
         * 2. segments of the new arc "uv"
         * 3. the internal segments that were intersected (broken segments).
         * 4. internal segments of faces f1 and f2
         * 5. the xy arc segments not intersected (broken) by "uv".
         */
        RefineGraph G = new RefineGraph();
        for (RSegment s: S) {
            RPoint a = s.getA();
            RPoint b = s.getB();

            boolean newSegment = false;
            boolean newA = false;
            boolean newB = false;
            if (!E.contains(a)) {
                a = a.copy();
                newSegment = true;
                newA = true;
            }
            if (!E.contains(b)) {
                b = b.copy();
                newSegment = true;
                newB = true;
            }

            // get RefineVertex
            RefineVertex va = G.getVertex(a);
            va.setFixed(!newA);
            RefineVertex vb = G.getVertex(b);
            vb.setFixed(!newB);

            // new segment must use the
            // refined vertice's points that
            // were found above. Not a and b.
            if (newSegment) s = new RSegment(va.getPoint(),vb.getPoint());

            // new edge
            RefineEdge re = G.newEdge(s, va, vb);
            if (E.contains(re.getA().getPoint()) &&
                E.contains(re.getB().getPoint()))
                re.setArcEdge(true);
        }

        /**
         * G must be triangulated before
         * tagging the segments. The extraction of
         * the triangles will depend on these tags
         * so it will be the arcs for the new faces.
         */
        System.out.println("\n\nGRAPH\n");
        G.report();

        // define cyclic ordering of
        // vertice's edges (planar map)
        G.defineCyclicOrderingBasedOnPlanarMap(f1,invertNormal1,f2,invertNormal2);


        {
            RefineMap M = G.createMap();
            System.out.println("\n\n MAP\n");
            System.out.println(""+M.toString());

            { // desenhar o mapa
                JFrame frame = new JFrame("Reduction Graph");
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setSize(new Dimension(1024, 768));
                frame.setContentPane(new PanelRefineMapViewer(M));
                frame.setVisible(true);
            } // desenhar o mapa
        }

        /**
         * tag phase: tag elements of G to posterior
         * extraction of useful data.
         */
        // commonArc is the XY arc
        for (RPoint point: commonArc.getPoints()) {
            G.tagVerticesAtSamePositionAsXY(point);
        }
        // the intersected segments of arc xy!
        for (RSegment s : commonArc.getSegments()) {
            if (brokenSegments.contains(s)) {
                G.tagVerticesAtSamePositionAsXY(s.getMiddlePoint());
            }
        }
        // commonArc is the UV arc
        for (RPoint point: uvArc.getPoints()) {
            G.tagVerticesAtSamePositionAsUV(point);
        }
        // tag edges
        G.tagEdges(G.getVertex(u),G.getVertex(v),G.getVertex(x),G.getVertex(y));


        /*
        try {
            PrintWriter pw = new PrintWriter("c:/zfaces.dxf");
            G.printDXF(pw);
            pw.close();
        } catch (FileNotFoundException ex) {
        }

        try {
            PrintWriter pw = new PrintWriter("c:/zfacesproj.dxf");
            G.printProjectionDXF(pw);
            pw.close();
        } catch (FileNotFoundException ex) {
        }*/

        // report
        // G.report();

        RArc arcUX = f1.getArc(u,x);
        RArc arcYU = f1.getArc(u,y);
        RArc arcXV = f2.getArc(v,x);
        RArc arcVY = f2.getArc(v,y);

        GemColor xColor = GemColor.getComplementColors(this.getColor(u),this.getColor(x),this.getColor(v))[0];
        GemColor yColor = GemColor.getComplementColors(this.getColor(u),this.getColor(y),this.getColor(v))[0];

        // build two refined faces XY
        RArc arcUV = new RArc(G.getSegmentsUV(u,v));
        ArrayList<RTriangle>[] txy = G.getTriangles(R,true);
        RFace fX = new RFace(u,x,v,arcUX,arcXV,arcUV,xColor);
        fX.addTriangles(txy[0]);
        RFace fY = new RFace(u,v,y,arcUV,arcVY,arcYU,yColor);
        fY.addTriangles(txy[1]);

        // build two refined faces UV
        RefineGraph GUV = G.copy();
        GUV.getTriangles(R,false);
        RArc arcXY = new RArc(GUV.getSegmentsXY(x,y));
        ArrayList<RTriangle>[] tuv = GUV.getTriangles(R,false);
        RFace fU = new RFace(u,x,y,arcYU,arcUX,arcXY,f1.getColor());
        fU.addTriangles(tuv[0]);
        RFace fV = new RFace(v,x,y,arcVY,arcXV,arcXY,f2.getColor());
        fV.addTriangles(tuv[1]);


        /**
         * Before lifting the two new arcs,
         * we must update some tets. The ones
         * that involves the arc xy and becomes
         * bigon c1,c2 disjoint after the lifting.
         */

        // replace map
        HashMap<RSegment,Object> map = new HashMap<RSegment,Object>();
        for (RSegment s: commonArc.getSegments()) {
            Pair p = piecesMap.get(s);
            if (p == null) {
                RSegment ss = GUV.findSegmentInstance(s);
                map.put(s,ss);
            }
            else {
                p = new Pair(GUV.findSegmentInstance((RSegment)p.getFirst()),
                             GUV.findSegmentInstance((RSegment)p.getSecond()));
                map.put(s,p);
            }
        }

        _workGem.getComponentRepository();
        GemVertex newv = _workGem.findVertex(newLabel2);
        int old1 = newv.getNeighbour(c1).getLabel();
        int old2 = newv.getNeighbour(c2).getLabel();
        Component bigon = newv.getComponent(GemColor.getColorSet(c1,c2));
        for (GemVertex bv: bigon.getVertices()) {
            if (bv.getLabel() == newLabel1 || bv.getLabel() == newLabel2 ||
                bv.getLabel() == old1 || bv.getLabel() == old2 ||
                (bv.getLabel() % 2) == 0)
                continue;
            RTetrahedron tet = this.findTetrahedron(bv.getLabel());
            tet.replace(arcXY,map);
        }

        //
        if (LAYERS < 5) {
            BigInteger scale = _DEN.pow(LAYERS);
            G.lift(R, new BigRational(_NUM1, scale));
            GUV.lift(R, new BigRational(_NUM2, scale));
        }
        else {
            System.out.println("Fiz alguma coisa");
            BigInteger scale = _DEN.pow(LAYERS).multiply(BigInteger.valueOf(10000000));
            G.lift(R, new BigRational(_NUM1, scale));
            GUV.lift(R, new BigRational(_NUM2, scale));
        }
        LAYERS++;

        // ---------------------------------------------
        // LOG NEW ARCS
        if (((x.equals(_b) && y.equals(_c)) || (x.equals(_c) && y.equals(_b)))) {
            System.out.println(
            "\n\nNew XY lifted arc BC over tetrahedras "+
            t1.getLabel()+","+t2.getLabel()+"  "+
            arcXY+"\n\n");
        }
        else if (((x.equals(_b) && y.equals(_d)) || (x.equals(_d) && y.equals(_b)))) {
            System.out.println(
            "\n\nNew XY lifted arc BD over tetrahedras "+
            t1.getLabel()+","+t2.getLabel()+"  "+
            arcXY+"\n\n");
        }
        else if (((x.equals(_a) && y.equals(_c)) || (x.equals(_c) && y.equals(_a)))) {
            System.out.println(
            "\n\nNew XY lifted arc AC over tetrahedras "+
            t1.getLabel()+","+t2.getLabel()+"  "+
            arcXY+"\n\n");
        }
        else if (((x.equals(_a) && y.equals(_d)) || (x.equals(_d) && y.equals(_a)))) {
            System.out.println(
            "\n\nNew XY lifted arc AD over tetrahedras "+
            t1.getLabel()+","+t2.getLabel()+"  "+
            arcXY+"\n\n");
        }


        if (((u.equals(_b) && v.equals(_c)) || (u.equals(_c) && v.equals(_b)))) {
            System.out.println(
            "\n\nNew UV lifted arc BC over tetrahedras "+
            t1.getLabel()+","+t2.getLabel()+"  "+
            arcUV+"\n\n");
        }
        else if (((u.equals(_b) && v.equals(_d)) || (u.equals(_d) && v.equals(_b)))) {
            System.out.println(
            "\n\nNew UV lifted arc BD over tetrahedras "+
            t1.getLabel()+","+t2.getLabel()+"  "+
            arcUV+"\n\n");
        }
        else if (((u.equals(_a) && v.equals(_c)) || (u.equals(_c) && v.equals(_a)))) {
            System.out.println(
            "\n\nNew UV lifted arc AC over tetrahedras "+
            t1.getLabel()+","+t2.getLabel()+"  "+
            arcUV+"\n\n");
        }
        else if (((u.equals(_a) && v.equals(_d)) || (u.equals(_d) && v.equals(_a)))) {
            System.out.println(
            "\n\nNew UV lifted arc AD over tetrahedras "+
            t1.getLabel()+","+t2.getLabel()+"  "+
            arcUV+"\n\n");
        }


        System.out.println("u = "+u.getName());
        System.out.println("v = "+v.getName());
        System.out.println("x = "+x.getName());
        System.out.println("y = "+y.getName());
        System.out.println("\n\nArc YU =  "+arcYU);
        System.out.println("\n\nArc UX =  "+arcUX);
        System.out.println("\n\nArc XV =  "+arcXV);
        System.out.println("\n\nArc VY =  "+arcVY);
        // ---------------------------------------------


        // ---------------------------------------------
        // Log the segments lifts that occuried
        try {
            HashSet<RSegment> X = new HashSet<RSegment>();
            X.addAll(f1.getSegments());
            X.addAll(f2.getSegments());
            X.addAll(G.getSegments());
            X.addAll(GUV.getSegments());

            HashSet<RPoint> P = new HashSet<RPoint>();
            P.addAll(f1.getPoints());
            P.addAll(f2.getPoints());

            PrintWriter pw = new PrintWriter("c:/zlift" + (LAYERS) + ".dxf");
            DXF.printDXFHeader(pw);
            for (RSegment s : X) {
                DXF.printSegmentDXF(pw, s, "" + s.getA().getNameIfExistsOtherwiseId()+"-"+s.getB().getNameIfExistsOtherwiseId());
            }

            for (RPoint p : P) {
                RPoint normal = R.getNormal(p);
                if (normal == null) continue;
                normal = normal.approxNormalize().scale(new BigRational(_U,4));
                RSegment s = new RSegment(p,normal.add(p));
                DXF.printSegmentDXF(pw, s, "" + p.getNameIfExistsOtherwiseId()+"N");
            }

            DXF.printDXFFooter(pw);
            pw.close();
        } catch (FileNotFoundException ex1) {
            ex1.printStackTrace();
        }
        // ---------------------------------------------

        // Arquivo em Maple ----------------------------
        try {
            HashSet<RPoint> P = new HashSet<RPoint>();
            P.addAll(f1.getPoints());
            P.addAll(f2.getPoints());
            P.addAll(G.getPoints());
            P.addAll(GUV.getPoints());

            PrintWriter pw = new PrintWriter("c:/zmaple" + (LAYERS) + ".txt");
            for (RPoint p : P) {
                pw.println(String.format("P%s := <%s,%s,%s>:",
                           p.getNameIfExistsOtherwiseId(),
                           p.getX().toString(),
                           p.getY().toString(),
                           p.getZ().toString()));
                try {
                    RPoint normal = R.getNormal(p);
                    normal = normal.approxNormalize().scale(new BigRational(_U, 4));
                    pw.println(String.format("N%s := <%s,%s,%s>:",
                                             p.getNameIfExistsOtherwiseId(),
                                             normal.getX().toString(),
                                             normal.getY().toString(),
                                             normal.getZ().toString()));
                } catch (Exception ex2) {
                }


            }

            for (RTriangle t: f1.getTriangles()) {
                RPoint normal = t.getNormal().approxNormalize();
                if (invertNormal1)
                    normal.invert();
                pw.println(String.format("T%s := <%s,%s,%s>:",
                                         t.getA().getNameIfExistsOtherwiseId()+"_"+
                                         t.getB().getNameIfExistsOtherwiseId()+"_"+
                                         t.getC().getNameIfExistsOtherwiseId(),
                                         normal.getX().toString(),
                                         normal.getY().toString(),
                                         normal.getZ().toString()));
            }
            for (RTriangle t: f2.getTriangles()) {
                RPoint normal = t.getNormal().approxNormalize();
                if (invertNormal2)
                    normal.invert();
                pw.println(String.format("T%s := <%s,%s,%s>:",
                                         t.getA().getNameIfExistsOtherwiseId()+"_"+
                                         t.getB().getNameIfExistsOtherwiseId()+"_"+
                                         t.getC().getNameIfExistsOtherwiseId(),
                                         normal.getX().toString(),
                                         normal.getY().toString(),
                                         normal.getZ().toString()));
            }

            pw.close();
        } catch (FileNotFoundException ex1) {
            ex1.printStackTrace();
        }
        // Arquivo em Maple ----------------------------


        //System.out.println("\n\nFace X\n"+fX+"\n\n");
        //System.out.println("\n\nFace Y\n"+fY+"\n\n");
        //System.out.println("\n\nFace U\n"+fU+"\n\n");
        //System.out.println("\n\nFace V\n"+fV+"\n\n");


        return new RFace[] {fX,fY,fU,fV};

    }
    static int LAYERS=1;

    static int COUNT=0;
    public static ArrayList findSurfacePath(
            RFace f1,
            RFace f2,
            boolean invertNormal1,
            boolean invertNormal2,
            NormalRepository N ) {

        // find out common arc
        RArc commonArc = RFace.getCommonArc(f1,f2);
        if (commonArc == null) throw new RuntimeException("ooooooooopssss");

        // get reference points
        RPoint x = commonArc.getFirstPoint();
        RPoint y = commonArc.getLastPoint();
        RPoint u = f1.getOppositeVertice(commonArc);
        RPoint v = f2.getOppositeVertice(commonArc);

        System.out.println("u = "+u.getName());
        System.out.println("v = "+v.getName());
        System.out.println("x = "+x.getName());
        System.out.println("y = "+y.getName());


        HashSet<RSegment> S = new HashSet<RSegment>();
        S.addAll(f1.getSegments());
        S.addAll(f2.getSegments());

        RefineGraph G = new RefineGraph();
        for (RSegment s : S) {
            RPoint a = s.getA();
            RPoint b = s.getB();

            // get RefineVertex
            RefineVertex va = G.getVertex(a);
            RefineVertex vb = G.getVertex(b);

            // new edge
            G.newEdge(s, va, vb);
        }


        // ----------------------

        try {
            PrintWriter pw = new PrintWriter("c:/znopath" + (COUNT) + ".dxf");
            DXF.printDXFTablesSection(pw);
            DXF.printDXFHeader(pw);
            for (RefineEdge e : G.getEdges()) {
                String layer = "INTERNAL";
                if (f1.getArcSegments().contains(e.getSegment()) ||
                    f2.getArcSegments().contains(e.getSegment())) {
                    layer = "ARC";
                }
                DXF.printSegmentDXF(pw, e.getSegment(), layer);

                RPoint pa = e.getA().getPoint();
                RPoint na = N.getNormal(pa);
                DXF.printSegmentDXF(pw, new RSegment(pa,pa.add(na)), "NORMAL");
            }
            for (RTriangle t: f1.getTriangles()) {
                String layer = "NORMAL";
                RPoint normal = t.getNormal();
                if (invertNormal1)
                    normal.invert();
                normal = normal.approxNormalize();
                RSegment s = new RSegment(t.getMiddle(),t.getMiddle().add(normal));
                DXF.printSegmentDXF(pw, s, layer);
            }


            DXF.printDXFFooter(pw);
            pw.close();
        } catch (FileNotFoundException ex) {
        }

        // --------------------------

        // ----------------------

        try {
            PrintWriter pw = new PrintWriter("c:/zface" + (2*COUNT-1) + ".dxf");
            DXF.printDXFTablesSection(pw);
            DXF.printDXFHeader(pw);
            for (RefineEdge e : G.getEdges()) {
                if (!f1.getSegments().contains(e.getSegment()))
                    continue;
                String layer = "INTERNAL";
                if (f1.getArcSegments().contains(e.getSegment()) ||
                    f2.getArcSegments().contains(e.getSegment())) {
                    layer = "ARC";
                }
                DXF.printSegmentDXF(pw, e.getSegment(), layer);
            }
            for (RTriangle t: f1.getTriangles()) {
                String layer = "NORMAL";
                RPoint normal = t.getNormal();
                if (invertNormal1)
                    normal.invert();
                normal = normal.approxNormalize();
                RSegment s = new RSegment(t.getMiddle(),t.getMiddle().add(normal));
                DXF.printSegmentDXF(pw, s, layer);
            }
            DXF.printDXFFooter(pw);
            pw.close();

            pw = new PrintWriter("c:/zface" + (2*COUNT) + ".dxf");
            DXF.printDXFTablesSection(pw);
            DXF.printDXFHeader(pw);
            for (RefineEdge e : G.getEdges()) {
                if (!f2.getSegments().contains(e.getSegment()))
                    continue;
                String layer = "INTERNAL";
                if (f1.getArcSegments().contains(e.getSegment()) ||
                    f2.getArcSegments().contains(e.getSegment())) {
                    layer = "ARC";
                }
                DXF.printSegmentDXF(pw, e.getSegment(), layer);
            }
            for (RTriangle t: f2.getTriangles()) {
                String layer = "NORMAL";
                RPoint normal = t.getNormal();
                if (invertNormal2)
                    normal.invert();
                normal = normal.approxNormalize();
                RSegment s = new RSegment(t.getMiddle(),t.getMiddle().add(normal));
                DXF.printSegmentDXF(pw, s, layer);
            }
            DXF.printDXFFooter(pw);
            pw.close();

        } catch (FileNotFoundException ex) {
        }

        // --------------------------






        RArc ux = f1.getArc(u,x);
        System.out.println(""+ux);
        RArc xv = f2.getArc(x,v);
        System.out.println(""+xv);
        RArc uv = new RArc(ux,xv);
        System.out.println(""+uv);
        ArrayList L = new ArrayList();

        L.add(u);
        System.out.println("Concatenate " + u.getNameIfExistsOtherwiseId());

        RSegment s = uv.getSegmentIncidentToNotEqual(u,null);
        System.out.println("s <- "+s.getStringWithPointNamesIfExists());
        RPoint alpha = s.getOpposite(u);
        System.out.println("alpha <- "+alpha.getNameIfExistsOtherwiseId());

        RTriangle t = null;
        System.out.println("Find triangle containing "+s.getStringWithPointNamesIfExists()+" and not equal "+t);
        t = f1.findTriangleWithGivenSegmentAndDifferentOf(s,t);
        boolean onFace1 = true;

        while (alpha != v) {

            System.out.println("t <- "+t);

            // not a valid triangle
            if (t == null)
                throw new RuntimeException();

            RSegment beta = t.getSegmentIncidentToButNot(alpha,s);
            System.out.println("beta <- "+beta.getStringWithPointNamesIfExists());

            L.add(beta);
            System.out.println("Concatenate "+beta.getStringWithPointNamesIfExists());

            s = uv.getSegmentIncidentToNotEqual(alpha,s);
            System.out.println("s <- "+s.getStringWithPointNamesIfExists());

            // pass through triangles incident to alpha
            // input:
            //    alpha = point on path whose incident
            //            triangles we are passing through.
            //    s =     segment of the border path
            //            from alpha to next point on
            //            border path.
            //    beta =  segment indcident to alpha but not
            //            on the border path
            //    t =     triangle with segments beta and
            //            the segment previous to s on the
            //            border path segment
            // output:
            //    t =     triangle incident to alpha
            //            that contains s.
            //    beta =  segment on t incident to alpha
            //            but not s.
            //    L =     is added the sequence of segments
            //            incident to alpha of the
            //            the triangles incident to alpha.
            //
            while (!t.containsSegment(s)) {

                System.out.println("Find triangle containing "+beta.getStringWithPointNamesIfExists()+" and not equal "+t);

                // System.out.println("triangle before "+t.hashCode());

                if (onFace1) {
                    t = f1.findTriangleWithGivenSegmentAndDifferentOf(beta, t);
                }
                if (!onFace1 || t == null) {
                    t = f2.findTriangleWithGivenSegmentAndDifferentOf(beta, t);
                    onFace1 = false;
                }
                System.out.println("t <- "+t);
                // System.out.println("triangle after "+t.hashCode());

                // not a valid triangle
                if (t == null)
                    throw new RuntimeException();

                RSegment nextBetaCandidate = t.getSegmentIncidentToButNot(alpha,beta);
                System.out.println("nextBetaCandidate <- "+nextBetaCandidate.getStringWithPointNamesIfExists());

                if (nextBetaCandidate != s) {
                    beta = nextBetaCandidate;
                    System.out.println("beta <- "+beta.getStringWithPointNamesIfExists());
                    L.add(beta);
                    System.out.println("Concatenate " + beta.getStringWithPointNamesIfExists());
                }
            }

            alpha = s.getOpposite(alpha);
            System.out.println("alpha <- "+alpha.getNameIfExistsOtherwiseId());

            if (alpha == v) {
                L.add(alpha);
                System.out.println("Concatenate " + alpha.getNameIfExistsOtherwiseId());
            }

        }

        try {
            PrintWriter pw = new PrintWriter("c:/zpath" + (COUNT++) + ".dxf");
            DXF.printDXFTablesSection(pw);
            DXF.printDXFHeader(pw);
            for (RefineEdge e : G.getEdges()) {
                String layer = "INTERNAL";
                if (f1.getArcSegments().contains(e.getSegment()) ||
                    f2.getArcSegments().contains(e.getSegment())) {
                    layer = "ARC";
                }
                DXF.printSegmentDXF(pw, e.getSegment(), layer);

                RPoint pa = e.getA().getPoint();
                RPoint na = N.getNormal(pa);
                DXF.printSegmentDXF(pw, new RSegment(pa,pa.add(na)), "NORMAL");
            }

            for (int i=0;i<L.size()-1;i++) {
                RPoint p[] = {null,null};
                Object pair[] = {L.get(i),L.get(i+1)};
                for (int j=0;j<pair.length;j++) {
                    Object o = pair[j];
                    if (o instanceof RSegment) {
                        RSegment ss = (RSegment) o;
                        p[j] = ss.getB().add(ss.getA()).scale(new BigRational(1, 2));
                    } else if (o instanceof RPoint) {
                        p[j] = (RPoint) o;
                    } else
                        throw new RuntimeException();
                }
                System.out.println(""+p[0]+" "+p[1]);
                DXF.printSegmentDXF(pw, new RSegment(p[0],p[1]),"PATH");
            }

            DXF.printDXFFooter(pw);
            pw.close();
        } catch (FileNotFoundException ex) {
        }

        return L;

    }

    public GemColor getFaceColor(RFace f) {
        if (f.getA() != _a && f.getB() != _a && f.getC() != _a) return GemColor.yellow;
        else if (f.getA() != _b && f.getB() != _b && f.getC() != _b) return GemColor.blue;
        else if (f.getA() != _c && f.getB() != _c && f.getC() != _c) return GemColor.red;
        else if (f.getA() != _d && f.getB() != _d && f.getC() != _d) return GemColor.green;
        throw new RuntimeException();
    }

    public RTetrahedron findTetrahedron(int label) {
        for (RTetrahedron t: _tets)
            if (t.getLabel() == label)
                return t;
        throw new RuntimeException("Oooopppsss");
    }

    public void printDXF(PrintWriter pw) {
        pw.println(" 0");
        pw.println("SECTION");
        pw.println(" 2");
        pw.println("ENTITIES");

        HashSet<RSegment> S = new HashSet<RSegment>();
        int parity = _internalTetrahedron.getLabel() % 2;
        for (RTetrahedron e: _tets) {
            // only the faces of the tetrahedras with "parity" index
            if (e.getLabel() % 2 != parity)
                continue;

            S.addAll(e.getSegments());

        }

        for (RSegment s: S) {

            RPoint a = s.getA();
            RPoint b = s.getB();

            pw.println(" 0");
            pw.println("LINE");
            pw.println(" 8");
            pw.println("A12");
            pw.println(" 10");
            pw.println(a.getX().toStringDot(5));
            pw.println(" 20");
            pw.println(a.getY().toStringDot(5));
            pw.println(" 30");
            pw.println(a.getZ().toStringDot(5));
            pw.println(" 11");
            pw.println(b.getX().toStringDot(5));
            pw.println(" 21");
            pw.println(b.getY().toStringDot(5));
            pw.println(" 31");
            pw.println(b.getZ().toStringDot(5));
        }

        /*
        // triangle faces
        for (RTetrahedron e: _tets) {
            // only the faces of the tetrahedras with "parity" index
            if (e.getLabel() != 2)
                continue;
            for (RTriangle t: e.getA().getTriangles()) {
                pw.println(" 0");
                pw.println("3DFACE");
                pw.println(" 8");
                pw.println("YELLOW");
                pw.println(" 10");
                pw.println(t.getA().getX().toStringDot(5));
                pw.println(" 20");
                pw.println(t.getA().getY().toStringDot(5));
                pw.println(" 30");
                pw.println(t.getA().getZ().toStringDot(5));
                pw.println(" 11");
                pw.println(t.getB().getX().toStringDot(5));
                pw.println(" 21");
                pw.println(t.getB().getY().toStringDot(5));
                pw.println(" 31");
                pw.println(t.getB().getZ().toStringDot(5));
                pw.println(" 12");
                pw.println(t.getC().getX().toStringDot(5));
                pw.println(" 22");
                pw.println(t.getC().getY().toStringDot(5));
                pw.println(" 32");
                pw.println(t.getC().getZ().toStringDot(5));
                pw.println(" 13");
                pw.println(t.getC().getX().toStringDot(5));
                pw.println(" 23");
                pw.println(t.getC().getY().toStringDot(5));
                pw.println(" 33");
                pw.println(t.getC().getZ().toStringDot(5));
            }

            for (RTriangle t: e.getB().getTriangles()) {
                pw.println(" 0");
                pw.println("3DFACE");
                pw.println(" 8");
                pw.println("BLUE");
                pw.println(" 10");
                pw.println(t.getA().getX().toStringDot(5));
                pw.println(" 20");
                pw.println(t.getA().getY().toStringDot(5));
                pw.println(" 30");
                pw.println(t.getA().getZ().toStringDot(5));
                pw.println(" 11");
                pw.println(t.getB().getX().toStringDot(5));
                pw.println(" 21");
                pw.println(t.getB().getY().toStringDot(5));
                pw.println(" 31");
                pw.println(t.getB().getZ().toStringDot(5));
                pw.println(" 12");
                pw.println(t.getC().getX().toStringDot(5));
                pw.println(" 22");
                pw.println(t.getC().getY().toStringDot(5));
                pw.println(" 32");
                pw.println(t.getC().getZ().toStringDot(5));
                pw.println(" 13");
                pw.println(t.getC().getX().toStringDot(5));
                pw.println(" 23");
                pw.println(t.getC().getY().toStringDot(5));
                pw.println(" 33");
                pw.println(t.getC().getZ().toStringDot(5));
            }


            for (RTriangle t: e.getB().getTriangles()) {
                pw.println(" 0");
                pw.println("3DFACE");
                pw.println(" 8");
                pw.println("RED");
                pw.println(" 10");
                pw.println(t.getA().getX().toStringDot(5));
                pw.println(" 20");
                pw.println(t.getA().getY().toStringDot(5));
                pw.println(" 30");
                pw.println(t.getA().getZ().toStringDot(5));
                pw.println(" 11");
                pw.println(t.getB().getX().toStringDot(5));
                pw.println(" 21");
                pw.println(t.getB().getY().toStringDot(5));
                pw.println(" 31");
                pw.println(t.getB().getZ().toStringDot(5));
                pw.println(" 12");
                pw.println(t.getC().getX().toStringDot(5));
                pw.println(" 22");
                pw.println(t.getC().getY().toStringDot(5));
                pw.println(" 32");
                pw.println(t.getC().getZ().toStringDot(5));
                pw.println(" 13");
                pw.println(t.getC().getX().toStringDot(5));
                pw.println(" 23");
                pw.println(t.getC().getY().toStringDot(5));
                pw.println(" 33");
                pw.println(t.getC().getZ().toStringDot(5));
            }

            for (RTriangle t: e.getB().getTriangles()) {
                pw.println(" 0");
                pw.println("3DFACE");
                pw.println(" 8");
                pw.println("GREEN");
                pw.println(" 10");
                pw.println(t.getA().getX().toStringDot(5));
                pw.println(" 20");
                pw.println(t.getA().getY().toStringDot(5));
                pw.println(" 30");
                pw.println(t.getA().getZ().toStringDot(5));
                pw.println(" 11");
                pw.println(t.getB().getX().toStringDot(5));
                pw.println(" 21");
                pw.println(t.getB().getY().toStringDot(5));
                pw.println(" 31");
                pw.println(t.getB().getZ().toStringDot(5));
                pw.println(" 12");
                pw.println(t.getC().getX().toStringDot(5));
                pw.println(" 22");
                pw.println(t.getC().getY().toStringDot(5));
                pw.println(" 32");
                pw.println(t.getC().getZ().toStringDot(5));
                pw.println(" 13");
                pw.println(t.getC().getX().toStringDot(5));
                pw.println(" 23");
                pw.println(t.getC().getY().toStringDot(5));
                pw.println(" 33");
                pw.println(t.getC().getZ().toStringDot(5));
            }

        }*/

        pw.println(" 0");
        pw.println("ENDSEC");
        pw.println(" 0");
        pw.println("EOF");

    }

    private int findRepresentantNewLabelOfBigon(Component bigon) {
        int representant = Integer.MAX_VALUE;
        ArrayList<GemVertex> vertices = bigon.getVerticesFromBigon();
        for (GemVertex v : vertices) {
            int newLabelRep = _mapOld2New.get(v.getLabel());
            if (newLabelRep % 2 == 0 && newLabelRep < representant)
                representant = newLabelRep;
        }
        if (representant == Integer.MAX_VALUE)
            throw new RuntimeException("OOOoooopppsssss");
        return representant;
    }


    public static void main(String[] args) throws ClassNotFoundException, IOException, SQLException {
        Gem G = App.getRepositorio().getGemEntryByCatalogNumber(8,1,0).getGem();
        SearchByTwistor S = new SearchByTwistor(G,GemColor.blue);
        Gem RG = S.getGemWithOneBigon();

        /*
        JFrame f1 = new JFrame("JJG Gem");
        f1.setSize(new Dimension(1024, 768));
        f1.setContentPane(new PanelGemViewer(RG.copy()));
        f1.setVisible(true);

        JFrame f2 = new JFrame("JJG Gem");
        f2.setSize(new Dimension(1024, 768));
        f2.setContentPane(new PanelTwistingPath(S.getGraph()));
        f2.setVisible(true); */

        GemEmbedding E = new GemEmbedding(RG);
        E.print();

        // System.exit(0);
    }

    public void print() throws FileNotFoundException {
        for (RTetrahedron t: _tets) {

            PrintWriter pw = new PrintWriter("c:/tet"+t.getLabel()+".dxf");
            DXF.printDXFLayersTableBegin(pw, 4);
            DXF.printDXFTetrahedronLayerInfo(pw, t);
            DXF.printDXFLayersTableEnd(pw);

            DXF.printDXFHeader(pw);
            DXF.printDXFTetrahedronFace(pw, t);
            DXF.printDXFFooter(pw);

            pw.close();
        }

        PrintWriter pw = new PrintWriter("c:/zalltet.dxf");
        DXF.printDXFLayersTableBegin(pw, 4*_tets.size());
        for (RTetrahedron t: _tets) {
            DXF.printDXFTetrahedronLayerInfo(pw, t);
        }
        DXF.printDXFLayersTableEnd(pw);
        DXF.printDXFHeader(pw);
        for (RTetrahedron t : _tets) {
            DXF.printDXFTetrahedronFace(pw, t);
        }
        DXF.printDXFFooter(pw);
        pw.close();
    }




    public void writeEPS() throws FileNotFoundException {
        for (RTetrahedron t: _tets) {

            PrintWriter pw = new PrintWriter("c:/tet"+t.getLabel()+".dxf");
            DXF.printDXFLayersTableBegin(pw, 4);
            DXF.printDXFTetrahedronLayerInfo(pw, t);
            DXF.printDXFLayersTableEnd(pw);

            DXF.printDXFHeader(pw);
            DXF.printDXFTetrahedronFace(pw, t);
            DXF.printDXFFooter(pw);

            pw.close();
        }

        PrintWriter pw = new PrintWriter("c:/zalltet.dxf");
        DXF.printDXFLayersTableBegin(pw, 4*_tets.size());
        for (RTetrahedron t: _tets) {
            DXF.printDXFTetrahedronLayerInfo(pw, t);
        }
        DXF.printDXFLayersTableEnd(pw);
        DXF.printDXFHeader(pw);
        for (RTetrahedron t : _tets) {
            DXF.printDXFTetrahedronFace(pw, t);
        }
        DXF.printDXFFooter(pw);
        pw.close();
    }



}

class PointNeighbourhood {
    private HashMap<RPoint,Vector> _map;
    private RPoint _p;
    private ArrayList<RPoint> _neighbours = new ArrayList<RPoint>();
    public PointNeighbourhood(RPoint p, HashMap<RPoint,Vector> map) {
        _p = p;
        _map = map;
    }
    public void addNeighbour(RPoint p) {
        _neighbours.add(p);
    }
    public int getNumberOfNeighboursNotDefined() {
        int c = 0;
        for (RPoint p: _neighbours)
            if (_map.get(p) == null)
                c++;
        return c;
    }
    public Vector getNeighboursBaricenter() {
        int c = 0;
        Vector v = Vector.valueOf(new double[] {0,0});
        for (RPoint p: _neighbours)
            v = v.plus(_map.get(p));
        v = v.times(Float64.valueOf(1.0/_neighbours.size()));
        return v;
    }
    public ArrayList<RPoint> getNeighbours() {
        return _neighbours;
    }
}
