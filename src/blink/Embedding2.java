package blink;

import java.awt.Dimension;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

import javax.swing.JFrame;

import edu.uci.ics.jung.utils.Pair;

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
public class Embedding2 {
    Gem _originalJJGem;
    Gem _workGem;
    Stack<InverseDipole> _S;

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

    public Embedding2(Gem jjg) throws FileNotFoundException {
        _originalJJGem = jjg;
        _workGem = jjg.copy();

        //
        Stack<Integer> originalLabels = new Stack<Integer>();

        _S = new Stack<InverseDipole>();
        while (true) {

            // dipole
            Dipole d = _workGem.findAnyTwoDipoleOnColors(GemColor.blue,GemColor.green);
            if (d != null) {
                InverseDipole id = d.getInverseDipole();

                // push labels
                originalLabels.push(id.getEvenNewLabel());
                originalLabels.push(id.getOddNewLabel());

                _S.push(id);
                System.out.println("Applying dipole: "+d.toString()+" whose inverse is "+id.toString());
                _workGem.cancelDipole(d);
                continue;
            }

            d = _workGem.findAnyTwoDipoleOnColors(GemColor.blue,GemColor.red);
            if (d != null) {
                InverseDipole id = d.getInverseDipole();

                // push labels
                originalLabels.push(id.getEvenNewLabel());
                originalLabels.push(id.getOddNewLabel());

                _S.push(id);
                System.out.println("Applying dipole: "+d.toString()+" whose inverse is "+id.toString());
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
            }
            else {
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
        int i=0;
        while (!originalLabels.isEmpty()) {
            int lbl = originalLabels.pop();
            _mapOld2New.put(lbl,i);
            _mapNew2Old.put(i,lbl);
            System.out.println(""+lbl+" -> "+i);
            i++;
        }

        // now do the thing
        LZip Z = new LZip();
        int nextNewTetrahedra = 1;
        while (!_S.isEmpty()) {
            InverseDipole d = _S.pop();

            GemColor c1 = d.getEdgeColor1();
            GemColor c2 = d.getEdgeColor2();

            boolean insert13Bigon = GemColor.getColorSet(c1,c2) == GemColor.getColorSet(GemColor.yellow,GemColor.red);
            boolean insert12Bigon = GemColor.getColorSet(c1,c2) == GemColor.getColorSet(GemColor.yellow,GemColor.green);

            // not valid colors
            if (!insert13Bigon && !insert12Bigon)
                throw new RuntimeException("OOoooopss");

            // find representant bigon label
            _workGem.getComponentRepository(); // assert that the components are initialized
            Component b = _workGem.findVertex(d.getEdgeVertex1()).getComponent(GemColor.getColorSet(c1, c2));
            int representant = this.findRepresentantNewLabelOfBigon(b);

            // go the next gem
            _workGem.applyInverseDipole(d);

            // before
            _workGem.getComponentRepository(); // assert the omponents are initialized
            b = _workGem.findVertex(d.getEvenNewLabel()).getComponent(GemColor.getColorSet(c1, c2));
            int repBefore = this.findRepresentantNewLabelOfBigon(b);

            // after
            _workGem.getComponentRepository(); // assert the omponents are initialized
            b = _workGem.findVertex(d.getOddNewLabel()).getComponent(GemColor.getColorSet(c1, c2));
            int repAfter = this.findRepresentantNewLabelOfBigon(b);

            // construct
            if (insert12Bigon) {
                Z.insert12Bigon(representant/2,nextNewTetrahedra,repBefore/2,repAfter/2);
            } else {
                Z.insert13Bigon(representant/2,nextNewTetrahedra,repBefore/2,repAfter/2);
            }

            nextNewTetrahedra++;

        }

        System.out.println(""+Z);



        //--------------------------------------------------------------------
        //-- Build embedding data.
        {
            ArrayList<EVertice> V = new ArrayList<EVertice>();

            for (Component c: _workGem.getComponentRepository().getTriballs()) {
                System.out.println(GemColor.getColorSetCompactStringABCD(c.getColorSet())+" --- triball");

                if (c.getComplementColors()[0] == GemColor.yellow) {
                    EVertice v = new EVertice(
                                 EVerticeType.TrueVertice,
                                 new BigRational(-1),
                                 new BigRational(-1),
                                 new BigRational(-1));
                    V.add(v);

                    System.out.println(""+v.toString());

                    _mapComponentToZeroCell.put(c,v);
                }
                else if (c.getComplementColors()[0] == GemColor.blue) {
                    EVertice v = new EVertice(
                                 EVerticeType.TrueVertice,
                                 new BigRational(+1),
                                 new BigRational(+1),
                                 new BigRational(-1));
                    V.add(v);

                    System.out.println(""+v.toString());

                    _mapComponentToZeroCell.put(c,v);
                }
                else if (c.getComplementColors()[0] == GemColor.red) {
                    EVertice v = new EVertice(
                                 EVerticeType.TrueVertice,
                                 new BigRational(-1),
                                 new BigRational(+1),
                                 new BigRational(+1));
                    V.add(v);

                    System.out.println(""+v.toString());

                    _mapComponentToZeroCell.put(c,v);
                }
                else if (c.getComplementColors()[0] == GemColor.green) {
                    EVertice v = new EVertice(
                                 EVerticeType.TrueVertice,
                                 new BigRational(+1),
                                 new BigRational(-1),
                                 new BigRational(+1));
                    V.add(v);

                    System.out.println(""+v.toString());

                    _mapComponentToZeroCell.put(c,v);
                }
            }

            // for every bigon not 01 and not 23
            // create a zero cell at position defined
            // by the LZip structure
            for (Component b: _workGem.getComponentRepository().getBigons()) {

                // these bigons do not define a zero cell
                if (b.getColorSet() == GemColor.getColorSet(GemColor.yellow, GemColor.blue) ||
                    b.getColorSet() == GemColor.getColorSet(GemColor.red, GemColor.green)) {
                    continue;
                }

                int representant = this.findRepresentantNewLabelOfBigon(b);

                System.out.println(GemColor.getColorSetCompactStringABCD(b.getColorSet())+(representant/2)+"    ----   Bigon: "+b.toStringWithLabels());

                int index = Z.indexOf(representant/2,b.getColorSet());

                BigRational x = null, y = null, z = null;
                BigInteger coord = null;
                if (index < 0) { // zero cell only for >=0 indexes
                    GemVertex v = b.getVertex();
                    GemColor cc[] = GemColor.getComplementColors(b.getColorSet());
                    Component c1 = v.getComponent(GemColor.getColorSet(b.getColors()[0],b.getColors()[1],cc[0]));
                    Component c2 = v.getComponent(GemColor.getColorSet(b.getColors()[0],b.getColors()[1],cc[1]));

                    EVertice v1 = _mapComponentToZeroCell.get(c1);
                    EVertice v2 = _mapComponentToZeroCell.get(c2);

                    System.out.println("Media aritmetica entre "+v1.toString()+" e "+v2.toString());

                    x = v1.getX().add(v2.getX()).div(2);
                    y = v1.getY().add(v2.getY()).div(2);
                    z = v1.getZ().add(v2.getZ()).div(2);
                }
                else {
                    // coordinate
                    coord = BigInteger.valueOf(6L);
                    coord = coord.pow(index);

                    System.out.println("Index of "+(representant/2)+" is "+index);

                    if (b.getColorSet() == GemColor.getColorSet(GemColor.yellow, GemColor.red) ||
                        b.getColorSet() == GemColor.getColorSet(GemColor.blue, GemColor.green)) {
                        x = new BigRational(coord);
                        y = new BigRational(0);
                        z = new BigRational(0);
                    }
                    else {
                        x = new BigRational(0);
                        y = new BigRational(coord);
                        z = new BigRational(0);
                    }
                }

                EVertice v = new EVertice(EVerticeType.InsideArcVertice,x,y,z);
                V.add(v);
                _mapComponentToZeroCell.put(b, v);

                System.out.println("" + v.toString());
            }

            // for every face of a tetrahedra
            for (GemVertex v: _workGem.getVertices()) {
                int lblv = _mapOld2New.get(v.getLabel());
                if (lblv % 2 != 0)
                    continue;


                { // yellow case

                    // triballs
                    Component c_acd = v.getComponent(GemColor.getComplementColorSet(GemColor.blue));
                    Component c_abd = v.getComponent(GemColor.getComplementColorSet(GemColor.red));
                    Component c_abc = v.getComponent(GemColor.getComplementColorSet(GemColor.green));

                    //
                    Component c_ac = v.getComponent(GemColor.colorSetFromABCD("ac"));
                    Component c_ad = v.getComponent(GemColor.colorSetFromABCD("ad"));

                    //
                    EVertice acd = _mapComponentToZeroCell.get(c_acd);
                    EVertice abd = _mapComponentToZeroCell.get(c_abd);
                    EVertice abc = _mapComponentToZeroCell.get(c_abc);
                    EVertice ac = _mapComponentToZeroCell.get(c_ac);
                    EVertice ad = _mapComponentToZeroCell.get(c_ad);

                    // define the zero cell
                    BigRational x = new BigRational(0);
                    BigRational y = new BigRational(0);
                    BigRational z = new BigRational(0);

                    x = x.add(abd.getX());
                    x = x.add(abc.getX());
                    x = x.add(ad.getX());
                    x = x.add(ac.getX());
                    x = x.div(4);

                    y = y.add(abd.getY());
                    y = y.add(abc.getY());
                    y = y.add(ad.getY());
                    y = y.add(ac.getY());
                    y = y.div(4);

                    z = z.add(abd.getZ());
                    z = z.add(abc.getZ());
                    z = z.add(ad.getZ());
                    z = z.add(ac.getZ());
                    z = z.div(4);

                    EVertice zeroCell = new EVertice(EVerticeType.InsideFaceVertice, x, y, z);
                    V.add(zeroCell);
                    Pair p = new Pair(v, GemColor.yellow);
                    _mapComponentToZeroCell.put(p, zeroCell);
                    p = new Pair(v.getYellow(), GemColor.yellow);
                    _mapComponentToZeroCell.put(p, zeroCell);

                    System.out.println("a"+(lblv/2)+"    ---  Yellow face of thetrahedra "+lblv/2);
                    System.out.println("" + zeroCell.toString());

                } // yellow case


                { // blue case

                    // triballs
                    Component c_bcd = v.getComponent(GemColor.getComplementColorSet(GemColor.yellow));
                    Component c_abd = v.getComponent(GemColor.getComplementColorSet(GemColor.red));
                    Component c_abc = v.getComponent(GemColor.getComplementColorSet(GemColor.green));

                    //
                    Component c_bc = v.getComponent(GemColor.colorSetFromABCD("bc"));
                    Component c_bd = v.getComponent(GemColor.colorSetFromABCD("bd"));

                    //
                    EVertice bcd = _mapComponentToZeroCell.get(c_bcd);
                    EVertice abd = _mapComponentToZeroCell.get(c_abd);
                    EVertice abc = _mapComponentToZeroCell.get(c_abc);
                    EVertice bc = _mapComponentToZeroCell.get(c_bc);
                    EVertice bd = _mapComponentToZeroCell.get(c_bd);

                    // define the zero cell
                    BigRational x = new BigRational(0);
                    BigRational y = new BigRational(0);
                    BigRational z = new BigRational(0);

                    x = x.add(abd.getX());
                    x = x.add(abc.getX());
                    x = x.add(bd.getX());
                    x = x.add(bc.getX());
                    x = x.div(4);

                    y = y.add(abd.getY());
                    y = y.add(abc.getY());
                    y = y.add(bd.getY());
                    y = y.add(bc.getY());
                    y = y.div(4);

                    z = z.add(abd.getZ());
                    z = z.add(abc.getZ());
                    z = z.add(bd.getZ());
                    z = z.add(bc.getZ());
                    z = z.div(4);

                    EVertice zeroCell = new EVertice(EVerticeType.InsideFaceVertice, x, y, z);
                    V.add(zeroCell);
                    Pair p = new Pair(v, GemColor.blue);
                    _mapComponentToZeroCell.put(p, zeroCell);
                    p = new Pair(v.getBlue(), GemColor.blue);
                    _mapComponentToZeroCell.put(p, zeroCell);

                    System.out.println("b"+(lblv/2)+"    ---  Blue face of thetrahedra "+lblv/2);
                    System.out.println("" + zeroCell.toString());

                } // blue case

                { // red case

                    // triballs
                    Component c_bcd = v.getComponent(GemColor.getComplementColorSet(GemColor.yellow));
                    Component c_acd = v.getComponent(GemColor.getComplementColorSet(GemColor.blue));
                    Component c_abc = v.getComponent(GemColor.getComplementColorSet(GemColor.green));

                    //
                    Component c_ac = v.getComponent(GemColor.colorSetFromABCD("ac"));
                    Component c_bc = v.getComponent(GemColor.colorSetFromABCD("bc"));

                    //
                    EVertice bcd = _mapComponentToZeroCell.get(c_bcd);
                    EVertice acd = _mapComponentToZeroCell.get(c_acd);
                    EVertice abc = _mapComponentToZeroCell.get(c_abc);
                    EVertice ac = _mapComponentToZeroCell.get(c_ac);
                    EVertice bc = _mapComponentToZeroCell.get(c_bc);

                    // define the zero cell
                    BigRational x = new BigRational(0);
                    BigRational y = new BigRational(0);
                    BigRational z = new BigRational(0);

                    x = x.add(bcd.getX());
                    x = x.add(acd.getX());
                    x = x.add(ac.getX());
                    x = x.add(bc.getX());
                    x = x.div(4);

                    y = y.add(bcd.getY());
                    y = y.add(acd.getY());
                    y = y.add(ac.getY());
                    y = y.add(bc.getY());
                    y = y.div(4);

                    z = z.add(bcd.getZ());
                    z = z.add(acd.getZ());
                    z = z.add(ac.getZ());
                    z = z.add(bc.getZ());
                    z = z.div(4);

                    EVertice zeroCell = new EVertice(EVerticeType.InsideFaceVertice, x, y, z);
                    V.add(zeroCell);
                    Pair p = new Pair(v, GemColor.red);
                    _mapComponentToZeroCell.put(p, zeroCell);
                    p = new Pair(v.getRed(), GemColor.red);
                    _mapComponentToZeroCell.put(p, zeroCell);

                    System.out.println("c"+(lblv/2)+"    ---  Red face of thetrahedra "+lblv/2);
                    System.out.println("" + zeroCell.toString());

                } // red case

                { // green case

                    // triballs
                    Component c_bcd = v.getComponent(GemColor.getComplementColorSet(GemColor.yellow));
                    Component c_acd = v.getComponent(GemColor.getComplementColorSet(GemColor.blue));
                    Component c_abd = v.getComponent(GemColor.getComplementColorSet(GemColor.red));

                    //
                    Component c_ad = v.getComponent(GemColor.colorSetFromABCD("ad"));
                    Component c_bd = v.getComponent(GemColor.colorSetFromABCD("bd"));

                    //
                    EVertice bcd = _mapComponentToZeroCell.get(c_bcd);
                    EVertice acd = _mapComponentToZeroCell.get(c_acd);
                    EVertice abd = _mapComponentToZeroCell.get(c_abd);
                    EVertice ad = _mapComponentToZeroCell.get(c_ad);
                    EVertice bd = _mapComponentToZeroCell.get(c_bd);

                    // define the zero cell
                    BigRational x = new BigRational(0);
                    BigRational y = new BigRational(0);
                    BigRational z = new BigRational(0);

                    x = x.add(bcd.getX());
                    x = x.add(acd.getX());
                    x = x.add(ad.getX());
                    x = x.add(bd.getX());
                    x = x.div(4);

                    y = y.add(bcd.getY());
                    y = y.add(acd.getY());
                    y = y.add(ad.getY());
                    y = y.add(bd.getY());
                    y = y.div(4);

                    z = z.add(bcd.getZ());
                    z = z.add(acd.getZ());
                    z = z.add(ad.getZ());
                    z = z.add(bd.getZ());
                    z = z.div(4);

                    EVertice zeroCell = new EVertice(EVerticeType.InsideFaceVertice, x, y, z);
                    V.add(zeroCell);
                    Pair p = new Pair(v, GemColor.green);
                    _mapComponentToZeroCell.put(p, zeroCell);
                    p = new Pair(v.getGreen(), GemColor.green);
                    _mapComponentToZeroCell.put(p, zeroCell);

                    System.out.println("d"+(lblv/2)+"    ---  Green face of thetrahedra "+lblv/2);
                    System.out.println("" + zeroCell.toString());

                } // green case

            }

            // Perturb
            // for (EVertice v : V) {
            //    v.perturb();
            // }

        }

        // EPoly[] ps = twistPolys(_workGem.findVertex(1),_workGem.findVertex(5));
        EPoly[] ps = twistPolysBD(_workGem.findVertex(1),_workGem.findVertex(5));
        System.out.println("P\n"+ps[0].toString());
        System.out.println("\nQ\n"+ps[1].toString());

        testGaps();
        test(ps[0],ps[1]);
        sinal(ps[0],ps[1]);

        PrintWriter pw = new PrintWriter("c:/polys.dxf");
        printDXF(pw,ps[0],ps[1]);
        pw.close();

        pw = new PrintWriter("c:/polys.wrl");
        ps[0].transform();
        printVRML(pw,ps[0],ps[1]);
        pw.close();


        System.out.println("Verify the inverse sequence? "+(_workGem.compareTo(_originalJJGem) == 0));

    }

    public void sinal(EPoly A, EPoly B) {
        int sum = 0;
        for (int i=0;i<A.size();i++) {
            EVertice p1 = A.get(i);
            EVertice p2 = A.get((i+1)%A.size());
            for (int j=0;j<B.size();j++) {
                EVertice q1 = B.get(j);
                EVertice q2 = B.get((j+1)%B.size());
                System.out.println(String.format("-- Calculating sinal between %d and %d...",i,j));
                int sinal = EPoly.sinal(p1,p2,q1,q2);;
                sum += sinal;
                if (sinal != 0)
                    System.out.println(String.format("Sinal between %d and %d is %d", i, j, sinal));
                System.out.println(String.format("-----------------------"));
            }
        }
        System.out.println("CROSS SUM = "+sum);
    }

    private void test(EPoly A, EPoly B) {
        for (int i=0;i<A.size();i++) {
            EVertice p1 = A.get(i);
            EVertice p2 = A.get((i+1)%A.size());
            for (int j=0;j<B.size();j++) {
                EVertice q1 = B.get(j);
                EVertice q2 = B.get((j+1)%B.size());
                BigRational gap = EPoly.gap(p1,p2,q1,q2);

                if (gap == null) {
                    // System.out.println(String.format("Gap between %d and %d is NO INTERSECTION",i,j));
                }
                else {
                    System.out.println(String.format("Gap between %d and %d is %s",i,j,gap.toString()));
                }
            }
        }
    }

    private void testGaps() {
        for (GemVertex v: _workGem.getVertices()) {

            int lbl = _mapOld2New.get(v.getLabel());

            if (lbl % 2 == 0)
                continue; // only odd vertices

            System.out.println(""+(lbl/2)+"'");

            EVertice abc = _mapComponentToZeroCell.get(v.getComponent(GemColor.colorSetFromABCD("abc")));
            EVertice abd = _mapComponentToZeroCell.get(v.getComponent(GemColor.colorSetFromABCD("abd")));
            EVertice acd = _mapComponentToZeroCell.get(v.getComponent(GemColor.colorSetFromABCD("acd")));
            EVertice bcd = _mapComponentToZeroCell.get(v.getComponent(GemColor.colorSetFromABCD("bcd")));

            EVertice ac = _mapComponentToZeroCell.get(v.getComponent(GemColor.colorSetFromABCD("ac")));
            EVertice ad = _mapComponentToZeroCell.get(v.getComponent(GemColor.colorSetFromABCD("ad")));
            EVertice bc = _mapComponentToZeroCell.get(v.getComponent(GemColor.colorSetFromABCD("bc")));
            EVertice bd = _mapComponentToZeroCell.get(v.getComponent(GemColor.colorSetFromABCD("bd")));

            EVertice a = _mapComponentToZeroCell.get(new Pair(v,GemColor.yellow));
            EVertice b = _mapComponentToZeroCell.get(new Pair(v,GemColor.blue));
            EVertice c = _mapComponentToZeroCell.get(new Pair(v,GemColor.red));
            EVertice d = _mapComponentToZeroCell.get(new Pair(v,GemColor.green));

            BigRational gap1 = EPoly.gapLine(bcd,bc,bd,d);
            System.out.println("bcd,bc - bd,d = "+gap1);

            BigRational gap2 = EPoly.gapLine(ac,c,ad,acd);
            System.out.println("ac,c - ad,acd = "+gap2);

            BigRational gap3 = EPoly.gapLine(bd,b,abd,ad);
            System.out.println("bd,b - abd,ad = "+gap3);

            BigRational gap4 = EPoly.gapLine(abc,bc,a,ac);
            System.out.println("abc,bc - a,ac = "+gap4);
        }
    }


    private EPoly[] twistPolys(GemVertex h,  GemVertex k) {

        // caso 3-twistor
        EVertice adh = _mapComponentToZeroCell.get(h.getComponent(GemColor.colorSetFromABCD("ad")));
        EVertice ach = _mapComponentToZeroCell.get(h.getComponent(GemColor.colorSetFromABCD("ac")));
        EVertice ack = _mapComponentToZeroCell.get(k.getComponent(GemColor.colorSetFromABCD("ac")));
        EVertice bck = _mapComponentToZeroCell.get(k.getComponent(GemColor.colorSetFromABCD("bc")));
        EVertice bdk = _mapComponentToZeroCell.get(k.getComponent(GemColor.colorSetFromABCD("bd")));
        EVertice bdh = _mapComponentToZeroCell.get(h.getComponent(GemColor.colorSetFromABCD("bd")));
        EVertice adk = _mapComponentToZeroCell.get(k.getComponent(GemColor.colorSetFromABCD("ad")));
        EVertice abk = _mapComponentToZeroCell.get(k.getComponent(GemColor.colorSetFromABCD("ab")));
        EVertice bch = _mapComponentToZeroCell.get(h.getComponent(GemColor.colorSetFromABCD("bc")));

        EVertice abd = _mapComponentToZeroCell.get(h.getComponent(GemColor.colorSetFromABCD("abd")));
        EVertice acd = _mapComponentToZeroCell.get(h.getComponent(GemColor.colorSetFromABCD("acd")));
        EVertice bcd = _mapComponentToZeroCell.get(h.getComponent(GemColor.colorSetFromABCD("bcd")));
        EVertice abc = _mapComponentToZeroCell.get(h.getComponent(GemColor.colorSetFromABCD("abc")));

        EVertice ah = _mapComponentToZeroCell.get(new Pair(h,GemColor.yellow));
        EVertice ak = _mapComponentToZeroCell.get(new Pair(k,GemColor.yellow));
        EVertice bh = _mapComponentToZeroCell.get(new Pair(h,GemColor.blue));
        EVertice bk = _mapComponentToZeroCell.get(new Pair(k,GemColor.blue));
        EVertice ch = _mapComponentToZeroCell.get(new Pair(h,GemColor.red));
        EVertice ck = _mapComponentToZeroCell.get(new Pair(k,GemColor.red));
        EVertice dh = _mapComponentToZeroCell.get(new Pair(h,GemColor.green));
        EVertice dk = _mapComponentToZeroCell.get(new Pair(k,GemColor.green));

        BigRational r = new BigRational(1,10);
        BigRational rr = new BigRational(9,10);

        EVertice P[] = new EVertice[12];
        P[0] = EVertice.getPointOnSegment(bck,ack,r);
        P[1] = EVertice.getPointOnSegment(bck,bk,r);
        P[2] = EVertice.getPointOnSegment(bcd,bk,r);
        P[3] = EVertice.getPointOnSegment(bcd,acd,r);
        P[4] = EVertice.getPointOnSegment(bcd,dh,r);
        P[5] = EVertice.getPointOnSegment(bcd,bdh,r);
        P[6] = EVertice.getPointOnSegment(bch,bdh,r);
        P[7] = EVertice.getPointOnSegment(bch,bh,r);
        P[8] = EVertice.getPointOnSegment(abc,bh,r);
        P[9] = EVertice.getPointOnSegment(abc,abd,r);
        P[10] = EVertice.getPointOnSegment(abc,dk,r);
        P[11] = EVertice.getPointOnSegment(abc,ack,r);
        EPoly p1 = new EPoly(P);

        EVertice Q[] = new EVertice[12];
        Q[0] = EVertice.getPointOnSegment(bck,ack,rr);
        Q[1] = EVertice.getPointOnSegment(bck,bk,rr);
        Q[2] = EVertice.getPointOnSegment(bcd,bk,rr);
        Q[3] = EVertice.getPointOnSegment(bcd,acd,rr);
        Q[4] = EVertice.getPointOnSegment(bcd,dh,rr);
        Q[5] = EVertice.getPointOnSegment(bcd,bdh,rr);
        Q[6] = EVertice.getPointOnSegment(bch,bdh,rr);
        Q[7] = EVertice.getPointOnSegment(bch,bh,rr);
        Q[8] = EVertice.getPointOnSegment(abc,bh,rr);
        Q[9] = EVertice.getPointOnSegment(abc,abd,rr);
        Q[10] = EVertice.getPointOnSegment(abc,dk,rr);
        Q[11] = EVertice.getPointOnSegment(abc,ack,rr);
        EPoly p2 = new EPoly(Q);

        /* Caso Azul Vermelho
        EVertice P[] = new EVertice[12];
        P[0] = EVertice.getPointOnSegment(bdk,adk,r);
        P[1] = EVertice.getPointOnSegment(bdk,bk,r);
        P[2] = EVertice.getPointOnSegment(bcd,bk,r);
        P[3] = EVertice.getPointOnSegment(bcd,acd,r);
        P[4] = EVertice.getPointOnSegment(bcd,ch,r);
        P[5] = EVertice.getPointOnSegment(bcd,bch,r);
        P[6] = EVertice.getPointOnSegment(bdh,bch,r);
        P[7] = EVertice.getPointOnSegment(bdh,bh,r);
        P[8] = EVertice.getPointOnSegment(abd,bh,r);
        P[9] = EVertice.getPointOnSegment(abd,abc,r);
        P[10] = EVertice.getPointOnSegment(abd,ck,r);
        P[11] = EVertice.getPointOnSegment(abd,adk,r);
        EPoly p1 = new EPoly(P);

        EVertice Q[] = new EVertice[12];
        Q[0] = EVertice.getPointOnSegment(bdk,adk,rr);
        Q[1] = EVertice.getPointOnSegment(bdk,bk,rr);
        Q[2] = EVertice.getPointOnSegment(bcd,bk,rr);
        Q[3] = EVertice.getPointOnSegment(bcd,acd,rr);
        Q[4] = EVertice.getPointOnSegment(bcd,ch,rr);
        Q[5] = EVertice.getPointOnSegment(bcd,bch,rr);
        Q[6] = EVertice.getPointOnSegment(bdh,bch,rr);
        Q[7] = EVertice.getPointOnSegment(bdh,bh,rr);
        Q[8] = EVertice.getPointOnSegment(abd,bh,rr);
        Q[9] = EVertice.getPointOnSegment(abd,abc,rr);
        Q[10] = EVertice.getPointOnSegment(abd,ck,rr);
        Q[11] = EVertice.getPointOnSegment(abd,adk,rr);
        EPoly p2 = new EPoly(Q);*/

        return new EPoly[] {p1,p2};
    }

    private EPoly[] twistPolysBD(GemVertex h,  GemVertex k) {

        // caso 3-twistor
        EVertice adh = _mapComponentToZeroCell.get(h.getComponent(GemColor.colorSetFromABCD("ad")));
        EVertice ach = _mapComponentToZeroCell.get(h.getComponent(GemColor.colorSetFromABCD("ac")));
        EVertice ack = _mapComponentToZeroCell.get(k.getComponent(GemColor.colorSetFromABCD("ac")));
        EVertice bck = _mapComponentToZeroCell.get(k.getComponent(GemColor.colorSetFromABCD("bc")));
        EVertice bdk = _mapComponentToZeroCell.get(k.getComponent(GemColor.colorSetFromABCD("bd")));
        EVertice bdh = _mapComponentToZeroCell.get(h.getComponent(GemColor.colorSetFromABCD("bd")));
        EVertice adk = _mapComponentToZeroCell.get(k.getComponent(GemColor.colorSetFromABCD("ad")));
        EVertice abk = _mapComponentToZeroCell.get(k.getComponent(GemColor.colorSetFromABCD("ab")));
        EVertice bch = _mapComponentToZeroCell.get(h.getComponent(GemColor.colorSetFromABCD("bc")));

        EVertice abd = _mapComponentToZeroCell.get(h.getComponent(GemColor.colorSetFromABCD("abd")));
        EVertice acd = _mapComponentToZeroCell.get(h.getComponent(GemColor.colorSetFromABCD("acd")));
        EVertice bcd = _mapComponentToZeroCell.get(h.getComponent(GemColor.colorSetFromABCD("bcd")));
        EVertice abc = _mapComponentToZeroCell.get(h.getComponent(GemColor.colorSetFromABCD("abc")));

        EVertice ah = _mapComponentToZeroCell.get(new Pair(h,GemColor.yellow));
        EVertice ak = _mapComponentToZeroCell.get(new Pair(k,GemColor.yellow));
        EVertice bh = _mapComponentToZeroCell.get(new Pair(h,GemColor.blue));
        EVertice bk = _mapComponentToZeroCell.get(new Pair(k,GemColor.blue));
        EVertice ch = _mapComponentToZeroCell.get(new Pair(h,GemColor.red));
        EVertice ck = _mapComponentToZeroCell.get(new Pair(k,GemColor.red));
        EVertice dh = _mapComponentToZeroCell.get(new Pair(h,GemColor.green));
        EVertice dk = _mapComponentToZeroCell.get(new Pair(k,GemColor.green));

        BigRational r = new BigRational(1,10);
        BigRational rr = new BigRational(1,9);

        /*
                 EVertice P[] = new EVertice[4];
                 P[0] = EVertice.getPointOnSegment(bck,bck,r);
                 P[1] = EVertice.getPointOnSegment(bcd,bcd,r);
                 P[2] = EVertice.getPointOnSegment(bch,bch,r);
                 P[3] = EVertice.getPointOnSegment(abc,abc,r);
                 EPoly p1 = new EPoly(P); */

        /*
                 EVertice P[] = new EVertice[4];
                 P[0] = EVertice.getPointOnSegment(adk,adk,r);
                 P[1] = EVertice.getPointOnSegment(acd,acd,r);
                 P[2] = EVertice.getPointOnSegment(adh,adh,r);
                 P[3] = EVertice.getPointOnSegment(abd,abd,r);
                 EPoly p1 = new EPoly(P); */

        EVertice P[] = new EVertice[12];
        P[0] = EVertice.getPointOnSegment(adk, ack, r);
        P[1] = EVertice.getPointOnSegment(acd, ack, r);
        P[2] = EVertice.getPointOnSegment(acd, bk, r);
        P[3] = EVertice.getPointOnSegment(acd, bcd, r);
        P[4] = EVertice.getPointOnSegment(acd, dh, r);
        P[5] = EVertice.getPointOnSegment(adh, dh, r);
        P[6] = EVertice.getPointOnSegment(adh, bdh, r);
        P[7] = EVertice.getPointOnSegment(abd, bdh, r);
        P[8] = EVertice.getPointOnSegment(abd, bh, r);
        P[9] = EVertice.getPointOnSegment(abd, abc, r);
        P[10] = EVertice.getPointOnSegment(abd, dk, r);
        P[11] = EVertice.getPointOnSegment(adk, dk, r);
        EPoly p1 = new EPoly(P);

        EVertice Q[] = new EVertice[12];
        Q[0] = EVertice.getPointOnSegment(adk,ack,rr);
        Q[1] = EVertice.getPointOnSegment(acd,ack,rr);
        Q[2] = EVertice.getPointOnSegment(acd,bk,rr);
        Q[3] = EVertice.getPointOnSegment(acd,bcd,rr);
        Q[4] = EVertice.getPointOnSegment(acd,dh,rr);
        Q[5] = EVertice.getPointOnSegment(adh,dh,rr);
        Q[6] = EVertice.getPointOnSegment(adh,bdh,rr);
        Q[7] = EVertice.getPointOnSegment(abd,bdh,rr);
        Q[8] = EVertice.getPointOnSegment(abd,bh,rr);
        Q[9] = EVertice.getPointOnSegment(abd,abc,rr);
        Q[10] = EVertice.getPointOnSegment(abd,dk,rr);
        Q[11] = EVertice.getPointOnSegment(adk,dk,rr);
        EPoly p2 = new EPoly(Q);

        /* EVertice Q[] = new EVertice[12];
        Q[0] = EVertice.getPointOnSegment(bck,ack,rr);
        Q[1] = EVertice.getPointOnSegment(bck,bk,rr);
        Q[2] = EVertice.getPointOnSegment(bcd,bk,rr);
        Q[3] = EVertice.getPointOnSegment(bcd,acd,rr);
        Q[4] = EVertice.getPointOnSegment(bcd,dh,rr);
        Q[5] = EVertice.getPointOnSegment(bcd,bdh,rr);
        Q[6] = EVertice.getPointOnSegment(bch,bdh,rr);
        Q[7] = EVertice.getPointOnSegment(bch,bh,rr);
        Q[8] = EVertice.getPointOnSegment(abc,bh,rr);
        Q[9] = EVertice.getPointOnSegment(abc,abd,rr);
        Q[10] = EVertice.getPointOnSegment(abc,dk,rr);
        Q[11] = EVertice.getPointOnSegment(abc,ack,rr);
        EPoly p2 = new EPoly(Q); */

        /* Caso Azul Vermelho
        EVertice P[] = new EVertice[12];
        P[0] = EVertice.getPointOnSegment(bdk,adk,r);
        P[1] = EVertice.getPointOnSegment(bdk,bk,r);
        P[2] = EVertice.getPointOnSegment(bcd,bk,r);
        P[3] = EVertice.getPointOnSegment(bcd,acd,r);
        P[4] = EVertice.getPointOnSegment(bcd,ch,r);
        P[5] = EVertice.getPointOnSegment(bcd,bch,r);
        P[6] = EVertice.getPointOnSegment(bdh,bch,r);
        P[7] = EVertice.getPointOnSegment(bdh,bh,r);
        P[8] = EVertice.getPointOnSegment(abd,bh,r);
        P[9] = EVertice.getPointOnSegment(abd,abc,r);
        P[10] = EVertice.getPointOnSegment(abd,ck,r);
        P[11] = EVertice.getPointOnSegment(abd,adk,r);
        EPoly p1 = new EPoly(P);

        EVertice Q[] = new EVertice[12];
        Q[0] = EVertice.getPointOnSegment(bdk,adk,rr);
        Q[1] = EVertice.getPointOnSegment(bdk,bk,rr);
        Q[2] = EVertice.getPointOnSegment(bcd,bk,rr);
        Q[3] = EVertice.getPointOnSegment(bcd,acd,rr);
        Q[4] = EVertice.getPointOnSegment(bcd,ch,rr);
        Q[5] = EVertice.getPointOnSegment(bcd,bch,rr);
        Q[6] = EVertice.getPointOnSegment(bdh,bch,rr);
        Q[7] = EVertice.getPointOnSegment(bdh,bh,rr);
        Q[8] = EVertice.getPointOnSegment(abd,bh,rr);
        Q[9] = EVertice.getPointOnSegment(abd,abc,rr);
        Q[10] = EVertice.getPointOnSegment(abd,ck,rr);
        Q[11] = EVertice.getPointOnSegment(abd,adk,rr);
        EPoly p2 = new EPoly(Q);*/

        return new EPoly[] {p1,p2};
    }




    public void printDXF(PrintWriter pw, EPoly p1, EPoly p2) {
        pw.println(" 0");
        pw.println("SECTION");
        pw.println(" 2");
        pw.println("ENTITIES");

        for (int i=0;i<p1.size();i++) {
            EVertice v1 = p1.get(i);
            EVertice v2 = p1.get((i+1)%p1.size());

            pw.println(" 0");
            pw.println("LINE");
            pw.println(" 8");
            pw.println("A12");
            pw.println(" 10");
            pw.println(v1.getX().toStringDot(5));
            pw.println(" 20");
            pw.println(v1.getY().toStringDot(5));
            pw.println(" 30");
            pw.println(v1.getZ().toStringDot(5));
            pw.println(" 11");
            pw.println(v2.getX().toStringDot(5));
            pw.println(" 21");
            pw.println(v2.getY().toStringDot(5));
            pw.println(" 31");
            pw.println(v2.getZ().toStringDot(5));
        }

        for (int i=0;i<p2.size();i++) {
            EVertice v1 = p2.get(i);
            EVertice v2 = p2.get((i+1)%p1.size());

            pw.println(" 0");
            pw.println("LINE");
            pw.println(" 8");
            pw.println("A23");
            pw.println(" 10");
            pw.println(v1.getX().toStringDot(5));
            pw.println(" 20");
            pw.println(v1.getY().toStringDot(5));
            pw.println(" 30");
            pw.println(v1.getZ().toStringDot(5));
            pw.println(" 11");
            pw.println(v2.getX().toStringDot(5));
            pw.println(" 21");
            pw.println(v2.getY().toStringDot(5));
            pw.println(" 31");
            pw.println(v2.getZ().toStringDot(5));
        }


        pw.println(" 0");
        pw.println("ENDSEC");
        pw.println(" 0");
        pw.println("EOF");
    }

    public void printVRML(PrintWriter pw, EPoly p1, EPoly p2) {
        pw.println("#VRML V2.0 utf8");

        pw.println(
        "PROTO ConnectingCylinder [\n"+
        "   field SFNode  appearance NULL\n"+
        "   field SFBool  bottom     TRUE\n"+
        "   field SFVec3f vertex0    0 -1 0\n"+
        "   field SFVec3f vertex1    0 1 0\n"+
        "   field SFFloat radius     1\n"+
        "   field SFBool  side       TRUE\n"+
        "   field SFBool  top        TRUE\n"+
        "]\n"+
        "{\n"+
        "   DEF TRANSFORM Transform {\n"+
        "      children [\n"+
        "         Shape {\n"+
        "            appearance Appearance { material Material { diffuseColor 0.5 0.6 0 } }\n"+
        "            geometry Cylinder {\n"+
        "            bottom IS bottom\n"+
        "            height 1\n"+
        "            radius IS radius\n"+
        "            side IS  side\n"+
        "            top  IS top\n"+
        "            }\n"+
        "         }\n"+
        "      ]\n"+
        "   }\n"+
        "   Script {\n"+
        "      field SFVec3f vertex0   IS vertex0\n"+
        "      field SFVec3f vertex1   IS vertex1\n"+
        "      field SFNode  transform USE TRANSFORM\n"+
        "      directOutput TRUE\n"+
        "      url  \"javascript:\n"+
        "         function initialize() {\n"+
        "            // Calculate vector for cylinder\n"+
        "            var vecCylinder;\n"+
        "            vecCylinder = vertex0.subtract(vertex1);\n"+
        "            // Calculate length and store into scale factor\n"+
        "            transform.scale = new SFVec3f (1,vecCylinder.length(),1);\n"+
        "            // Calculate translation (average of vertices) and store\n"+
        "            var vecTranslation;\n"+
        "            vecTranslation = vertex0.add(vertex1).divide(2);\n"+
        "            transform.translation = vecTranslation;\n"+
        "            // Calculate rotation (rotation that takes vector 0 1 0 to vecCylinder).\n"+
        "            var rotTransform;\n"+
        "            rotTransform = new SFRotation(new SFVec3f(0,1,0),vecCylinder);\n"+
        "            transform.rotation = rotTransform;\n"+
        "            // Done\n"+
        "            return;\n"+
        "         }\n"+
        "      \"\n"+
        "   }\n"+
        "}");


        pw.println("PROTO BlueVertice [] { Shape {");
        pw.println("appearance Appearance { material Material { diffuseColor 0 0 1 } }");
        pw.println("geometry Sphere{radius 0.06}");
        pw.println("} }");

        pw.println("PROTO GreenVertice [] { Shape {");
        pw.println("appearance Appearance { material Material { diffuseColor 0 1 0 } }");
        pw.println("geometry Sphere{radius 0.06}");
        pw.println("} }");

        pw.println("PROTO CyanVertice [] { Shape {");
        pw.println("appearance Appearance { material Material { diffuseColor 0 1 1 } }");
        pw.println("geometry Sphere{radius 0.06}");
        pw.println("} }");

        pw.println("PROTO YellowVertice [] { Shape {");
        pw.println("appearance Appearance { material Material { diffuseColor 1 1 0 } }");
        pw.println("geometry Sphere{radius 0.06}");
        pw.println("} }");

        pw.println("PROTO WhiteVertice [] { Shape {");
        pw.println("appearance Appearance { material Material { diffuseColor 1 1 1 } }");
        pw.println("geometry Sphere{radius 0.06}");
        pw.println("} }");

        pw.println("PROTO RedVertice [] { Shape {");
        pw.println("appearance Appearance { material Material { diffuseColor 1 0 0 } }");
        pw.println("geometry Sphere{radius 0.06}");
        pw.println("} }");

        pw.println("PROTO SmallWhiteVertice [] { Shape {");
        pw.println("appearance Appearance { material Material { diffuseColor 1 1 1 } }");
        pw.println("geometry Sphere{radius 0.03}");
        pw.println("} }");



        /*
        pw.println("Shape {");
        pw.println(String.format("appearance Appearance { material Material { diffuseColor %.3f %.3f %.3f  transparency 0 } } ",1.0,0.0,0.0));
        pw.println("geometry IndexedLineSet { ");
        //pw.println(" solid FALSE ");
        pw.println("coord Coordinate { ");
        pw.println("point [ ");
        for (int j=0;j<p1.size();j++) {
            EVertice v0 = p1.get(j);
            double x0 = Double.parseDouble(v0.getX().toStringDot(10,10));
            double y0 = Double.parseDouble(v0.getY().toStringDot(10,10));
            double z0 = Double.parseDouble(v0.getZ().toStringDot(10,10));
            pw.print(String.format("%.8f %.8f %.8f", x0, y0, z0));
            pw.println(",");
        }
        for (int j=0;j<p2.size();j++) {
            EVertice v0 = p2.get(j);
            double x0 = Double.parseDouble(v0.getX().toStringDot(10,10));
            double y0 = Double.parseDouble(v0.getY().toStringDot(10,10));
            double z0 = Double.parseDouble(v0.getZ().toStringDot(10,10));
            pw.print(String.format("%.8f %.8f %.8f", x0, y0, z0));
            pw.println(",");
        }
        pw.println("] }");
        pw.println("coordIndex [");
        pw.println("0 1,");
        // pw.println("12 13 14 15 16 17 18 19 20 21 22 23 12,");
        pw.println("] } }"); */

        // arcs
        for (int i=0;i<p1.size();i++) {
            EVertice v0 = p1.get(i);
            EVertice v1 = p1.get((i+1)%p1.size());

            double x0 = Double.parseDouble(v0.getX().toStringDot(10,10));
            double y0 = Double.parseDouble(v0.getY().toStringDot(10,10));
            double z0 = Double.parseDouble(v0.getZ().toStringDot(10,10));

            double x1 = Double.parseDouble(v1.getX().toStringDot(10,10));
            double y1 = Double.parseDouble(v1.getY().toStringDot(10,10));
            double z1 = Double.parseDouble(v1.getZ().toStringDot(10,10));

            pw.println(String.format(
                    "ConnectingCylinder { radius 0.05 vertex0 %.8f %.8f %.8f vertex1 %.8f %.8f %.8f }",
                    x0, y0, z0, x1, y1, z1));
        }

        /*
        for (int i=0;i<p2.size();i++) {
            EVertice v0 = p2.get(i);
            EVertice v1 = p2.get((i+1)%p2.size());

            double x0 = Double.parseDouble(v0.getX().toStringDot(10,10));
            double y0 = Double.parseDouble(v0.getY().toStringDot(10,10));
            double z0 = Double.parseDouble(v0.getZ().toStringDot(10,10));

            double x1 = Double.parseDouble(v1.getX().toStringDot(10,10));
            double y1 = Double.parseDouble(v1.getY().toStringDot(10,10));
            double z1 = Double.parseDouble(v1.getZ().toStringDot(10,10));

            pw.println(String.format(
                    "ConnectingCylinder { radius 0.1 vertex0 %.8f %.8f %.8f vertex1 %.8f %.8f %.8f }",
                    x0, y0, z0, x1, y1, z1));
        }*/
        pw.flush();
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

    public static void main(String[] args) throws
            FileNotFoundException,
            ClassNotFoundException,
            IOException,
            SQLException {
        Gem G = App.getRepositorio().getGemEntryByCatalogNumber(8,1,0).getGem();

        SearchByTwistor S = new SearchByTwistor(G,GemColor.blue);
        Gem RG = S.getGemWithOneBigon();

        JFrame f1 = new JFrame("JJG Gem");
        f1.setSize(new Dimension(1024, 768));
        f1.setContentPane(new PanelGemViewer(RG.copy()));
        f1.setVisible(true);

        JFrame f2 = new JFrame("JJG Gem");
        f2.setSize(new Dimension(1024, 768));
        f2.setContentPane(new PanelTwistingPath(S.getGraph()));
        f2.setVisible(true);


        Embedding2 E = new Embedding2(RG);
    }
}

class LZip {

    ArrayList<Integer> _tetLabels02 = new ArrayList<Integer>();
    ArrayList<Integer> _bigonRepLabels02 = new ArrayList<Integer>();
    ArrayList<Integer> _tetLabels03 = new ArrayList<Integer>();
    ArrayList<Integer> _bigonRepLabels03 = new ArrayList<Integer>();

    public LZip() {
        _tetLabels02.add(0);
        _bigonRepLabels02.add(0);
        _tetLabels03.add(0);
        _bigonRepLabels03.add(0);
    }

    public void insert12Bigon(int bigon03,
                              int newTetrahedronLabel,
                              int bigon03LabelBefore,
                              int bigon03LabelAfter) {
        int index = _bigonRepLabels03.indexOf(bigon03);

        if (index == -1)
            throw new RuntimeException("OOoooppss");

        _tetLabels03.add(index+1,newTetrahedronLabel);
        _bigonRepLabels03.set(index,bigon03LabelBefore);
        _bigonRepLabels03.add(index+1,bigon03LabelAfter);
    }

    /**
     * The representant value must be 0 to
     * (n/2)-1 where they are the tetrahedra.
     */
    public boolean isXTetrahedron(int rep) {
        if (rep == 0) throw new RuntimeException();
        return _tetLabels02.contains(rep);
    }

    public boolean isYTetrahedron(int rep) {
        if (rep == 0) throw new RuntimeException();
        return _tetLabels03.contains(rep);
    }

    public int indexOf(int representant, int bigonColors) {

        // the following cases are X indexes
        if (bigonColors == GemColor.getColorSet(GemColor.yellow,GemColor.red)) {
            int index = _bigonRepLabels02.indexOf(representant);
            if (index == -1)
                throw new RuntimeException("Ooopsss");
            return 2*index;
        }
        else if (bigonColors == GemColor.getColorSet(GemColor.blue,GemColor.green)) {
            if (representant == 0) return -1;
            int index = _tetLabels02.indexOf(representant);
            if (index == -1)
                throw new RuntimeException("Ooopsss");
            return 2*index-1;
        }

        // the following cases are Y indexes
        else if (bigonColors == GemColor.getColorSet(GemColor.yellow,GemColor.green)) {
            int index = _bigonRepLabels03.indexOf(representant);
            if (index == -1)
                throw new RuntimeException("Ooopsss");
            return 2*index;
        }
        else if (bigonColors == GemColor.getColorSet(GemColor.blue,GemColor.red)) {
            if (representant == 0) return -1;
            int index = _tetLabels03.indexOf(representant);
            if (index == -1)
                throw new RuntimeException("Ooopsss");
            return 2*index-1;
        }
        throw new RuntimeException("oooppsss");
    }

    public void insert13Bigon(int bigon02,
                              int newTetrahedronLabel,
                              int bigon02LabelBefore,
                              int bigon02LabelAfter) {
        int index = _bigonRepLabels02.indexOf(bigon02);
        if (index == -1)
            throw new RuntimeException("OOoooppss");
        _tetLabels02.add(index+1,newTetrahedronLabel);
        _bigonRepLabels02.set(index,bigon02LabelBefore);
        _bigonRepLabels02.add(index+1,bigon02LabelAfter);
    }

    public String toString() {
        StringBuffer b = new StringBuffer();
        b.append("02 -> ");
        for (int i=0;i<_tetLabels02.size();i++) {
            int tetLbl = _tetLabels02.get(i);
            int big02Lbl = _bigonRepLabels02.get(i);
            b.append(String.format("T%d B%d ",tetLbl,big02Lbl));
        }
        b.append("\n");
        b.append("03 -> ");
        for (int i=0;i<_tetLabels03.size();i++) {
            int tetLbl = _tetLabels03.get(i);
            int big03Lbl = _bigonRepLabels03.get(i);
            b.append(String.format("T%d B%d ",tetLbl,big03Lbl));
        }
        return b.toString();
    }
}

enum EVerticeType {
    TrueVertice,
    InsideArcVertice,
    InsideFaceVertice,
    PathPoint
}

/**
 * Embedding Vertice using big numbers.
 */
class EVertice {
    private EVerticeType _type;
    private BigRational _x;
    private BigRational _y;
    private BigRational _z;
    public EVertice(EVerticeType type,
                    BigRational x,
                    BigRational y,
                    BigRational z) {
        _type = type;
        _x = x;
        _y = y;
        _z = z;
    }
    public EVerticeType getType() { return _type; }
    public BigRational getX() { return _x; }
    public BigRational getY() { return _y; }
    public BigRational getZ() { return _z; }
    public void set(BigRational x, BigRational y, BigRational z) {
        _x = x; _y = y; _z = z;
    }
    public String toString() {
        return ""+_type+" ("+_x.toString()+","+_y.toString()+","+_z.toString()+")";
    }
    public static EVertice getPointOnSegment(EVertice origin, EVertice target, BigRational r) {
        BigRational x = origin.getX();
        BigRational y = origin.getY();
        BigRational z = origin.getZ();
        x = x.add(target.getX().sub(origin.getX()).mul(r));
        y = y.add(target.getY().sub(origin.getY()).mul(r));
        z = z.add(target.getZ().sub(origin.getZ()).mul(r));
        return new EVertice(EVerticeType.PathPoint,x,y,z);
    }
    public void perturb() {
        double x = Math.random();
        double y = Math.random();
        double z = Math.random();
        BigRational xx = new BigRational((long)(x*1000.0),100000000L);
        BigRational yy = new BigRational((long)(y*1000.0),100000000L);
        BigRational zz = new BigRational((long)(z*1000.0),100000000L);
        _x = _x.add(xx);
        _y = _y.add(yy);
        _z = _z.add(zz);
    }
}

class EPoly {

    private ArrayList<EVertice> _points = new ArrayList<EVertice>();

    public EPoly() {
    }

    public EPoly(EVertice[] vs) {
        for (EVertice v: vs)
            _points.add(v);
    }

    public void transform() {
        for (EVertice v: _points) {
            double x0 = Double.parseDouble(v.getX().add(100).toStringDot(10,10));
            double y0 = Double.parseDouble(v.getY().add(100).toStringDot(10,10));
            x0 = Math.log(x0);
            y0 = Math.log(y0);
            v.set(new BigRational((long)(x0*10000),10000L),
                  new BigRational((long)(y0*10000),10000L),
                  v.getZ());
        }
    }

    public void addPoint(EVertice v) {
        _points.add(v);
    }

    public String toString() {
        StringBuffer s = new StringBuffer();
        for (EVertice v: _points) {
            s.append(v.toString()+"\n");
        }
        return s.toString();
    }

    public EVertice get(int index) {
        return _points.get(index);
    }

    public int size() {
        return _points.size();
    }

    public void test() {
        for (int i=0;i<this.size();i++) {
            EVertice p1 = _points.get(i);
            EVertice p2 = _points.get((i+1)%this.size());
            for (int j=i+2;j<this.size();j++) {
                EVertice q1 = _points.get(j);
                EVertice q2 = _points.get((j+1)%this.size());
                BigRational gap = this.gap(p1,p2,q1,q2);

                if (gap == null) {
                    System.out.println(String.format("Gap between %d and %d is NO INTERSECTION",i,j));
                }
                else {
                    System.out.println(String.format("Gap between %d and %d is %s",i,j,gap.toString()));
                }
            }
        }
    }


    /**
     * See the z-gap of the segments P1P2
     * and Q1Q2 at the (x,y) coordinate where
     * their z=0 projection intersects.
     */
    public static BigRational gap(EVertice P1, EVertice P2, EVertice Q1, EVertice Q2) {

        BigRational xp1 = P1.getX();
        BigRational xp2 = P2.getX();
        BigRational yp1 = P1.getY();
        BigRational yp2 = P2.getY();
        BigRational zp1 = P1.getZ();
        BigRational zp2 = P2.getZ();

        BigRational xq1 = Q1.getX();
        BigRational xq2 = Q2.getX();
        BigRational yq1 = Q1.getY();
        BigRational yq2 = Q2.getY();
        BigRational zq1 = Q1.getZ();
        BigRational zq2 = Q2.getZ();

        // (-xq2*yp2 + xq2*yp1 + xq1*yp2 - xq1*yp1 + yq2*xp2 - yq2*xp1 - yq1*xp2 + yq1*xp1)
        BigRational denom =
            xq2.neg().mul(yp2).
            add(xq2.mul(yp1)).
            add(xq1.mul(yp2)).
            sub(xq1.mul(yp1)).
            add(yq2.mul(xp2)).
            sub(yq2.mul(xp1)).
            sub(yq1.mul(xp2)).
            add(yq1.mul(xp1));


        if (denom.equals(denom.ZERO)) {
            // colinear?
            System.out.println("Denominador é zero: não tem solução");

            /*
            // a reta r(k) = P1 + k(P2-P1)
            // encontrar k0 tal que r(k0) = (xq1, yy)
            // se (yy == yq1) então ok!

            // check the case
            BigRational k0 = xq1.sub(xp1).div(xp2.sub(xp1));
            BigRational yy = yp1.add(k0.mul(yp2.sub(yp1)));

            if (yy.compareTo(yq1) == 0) {
                System.out.println("they are colinear at "+k0);
            }
            else {
                System.out.println("NOT colinear");
            }*/
            return null;
        }

        // (xq2*yp1+yq2*xq1-xq2*yq1-xq1*yp1-yq2*xp1+yq1*xp1)
        BigRational numS =
            xq2.mul(yp1).
            add(yq2.mul(xq1)).
            sub(xq2.mul(yq1)).
            sub(xq1.mul(yp1)).
            sub(yq2.mul(xp1)).
            add(yq1.mul(xp1));

        // -(-xq1*yp2+xq1*yp1+yq1*xp2+xp1*yp2-yp1*xp2-yq1*xp1)
        BigRational numT =
            xq1.neg().mul(yp2).
            add(xq1.mul(yp1)).
            add(yq1.mul(xp2)).
            add(xp1.mul(yp2)).
            sub(yp1.mul(xp2)).
            sub(yq1.mul(xp1)).
            neg();

        BigRational s = numS.div(denom);
        BigRational t = numT.div(denom);

        if (s.compareTo(BigRational.ZERO) <= 0 ||
            s.compareTo(BigRational.ONE) >= 1 ||
            t.compareTo(BigRational.ZERO) <= 0 ||
            t.compareTo(BigRational.ONE) >= 1) {
            // System.out.println("s ou t estão fora de (0,1): não tem solução");
            return null;
        }

        /*
        // test intersections on the projected thing
        BigRational xp = xp1.add(xp2.sub(xp1).mul(s));
        BigRational xq = xq1.add(xq2.sub(xq1).mul(t));
        BigRational yp = yp1.add(yp2.sub(yp1).mul(s));
        BigRational yq = yq1.add(yq2.sub(yq1).mul(t));
        if (xp.sub(xq).compareTo(BigRational.ZERO) != 0 ||
            yp.sub(yq).compareTo(BigRational.ZERO) != 0) {
            System.out.println("Problema na intersacao");
        }
        else {
            System.out.println(xp+" "+xq);
            System.out.println(yp+" "+yq);
        } */

        BigRational zp = zp1.add(zp2.sub(zp1).mul(s));
        BigRational zq = zq1.add(zq2.sub(zq1).mul(t));

        // o gap
        return zq.sub(zp);
    }

    /**
     * See the z-gap of the segments P1P2
     * and Q1Q2 at the (x,y) coordinate where
     * their z=0 projection intersects.
     */
    public static BigRational gapLine(EVertice P1, EVertice P2, EVertice Q1, EVertice Q2) {

        BigRational xp1 = P1.getX();
        BigRational xp2 = P2.getX();
        BigRational yp1 = P1.getY();
        BigRational yp2 = P2.getY();
        BigRational zp1 = P1.getZ();
        BigRational zp2 = P2.getZ();

        BigRational xq1 = Q1.getX();
        BigRational xq2 = Q2.getX();
        BigRational yq1 = Q1.getY();
        BigRational yq2 = Q2.getY();
        BigRational zq1 = Q1.getZ();
        BigRational zq2 = Q2.getZ();

        // (-xq2*yp2 + xq2*yp1 + xq1*yp2 - xq1*yp1 + yq2*xp2 - yq2*xp1 - yq1*xp2 + yq1*xp1)
        BigRational denom =
            xq2.neg().mul(yp2).
            add(xq2.mul(yp1)).
            add(xq1.mul(yp2)).
            sub(xq1.mul(yp1)).
            add(yq2.mul(xp2)).
            sub(yq2.mul(xp1)).
            sub(yq1.mul(xp2)).
            add(yq1.mul(xp1));


        if (denom.equals(denom.ZERO)) {
            // colinear?
            System.out.println("Denominador é zero: não tem solução");

            /*
            // a reta r(k) = P1 + k(P2-P1)
            // encontrar k0 tal que r(k0) = (xq1, yy)
            // se (yy == yq1) então ok!

            // check the case
            BigRational k0 = xq1.sub(xp1).div(xp2.sub(xp1));
            BigRational yy = yp1.add(k0.mul(yp2.sub(yp1)));

            if (yy.compareTo(yq1) == 0) {
                System.out.println("they are colinear at "+k0);
            }
            else {
                System.out.println("NOT colinear");
            }*/
            return null;
        }

        // (xq2*yp1+yq2*xq1-xq2*yq1-xq1*yp1-yq2*xp1+yq1*xp1)
        BigRational numS =
            xq2.mul(yp1).
            add(yq2.mul(xq1)).
            sub(xq2.mul(yq1)).
            sub(xq1.mul(yp1)).
            sub(yq2.mul(xp1)).
            add(yq1.mul(xp1));

        // -(-xq1*yp2+xq1*yp1+yq1*xp2+xp1*yp2-yp1*xp2-yq1*xp1)
        BigRational numT =
            xq1.neg().mul(yp2).
            add(xq1.mul(yp1)).
            add(yq1.mul(xp2)).
            add(xp1.mul(yp2)).
            sub(yp1.mul(xp2)).
            sub(yq1.mul(xp1)).
            neg();

        BigRational s = numS.div(denom);
        BigRational t = numT.div(denom);

        if (s.compareTo(BigRational.ZERO) <= 0 ||
            s.compareTo(BigRational.ONE) >= 1 ||
            t.compareTo(BigRational.ZERO) <= 0 ||
            t.compareTo(BigRational.ONE) >= 1) {
            System.out.println("s ou t estão fora de (0,1): não tem solução");
            // return null;
        }

        /*
        // test intersections on the projected thing
        BigRational xp = xp1.add(xp2.sub(xp1).mul(s));
        BigRational xq = xq1.add(xq2.sub(xq1).mul(t));
        BigRational yp = yp1.add(yp2.sub(yp1).mul(s));
        BigRational yq = yq1.add(yq2.sub(yq1).mul(t));
        if (xp.sub(xq).compareTo(BigRational.ZERO) != 0 ||
            yp.sub(yq).compareTo(BigRational.ZERO) != 0) {
            System.out.println("Problema na intersacao");
        }
        else {
            System.out.println(xp+" "+xq);
            System.out.println(yp+" "+yq);
        } */

        BigRational zp = zp1.add(zp2.sub(zp1).mul(s));
        BigRational zq = zq1.add(zq2.sub(zq1).mul(t));

        // o gap
        return zq.sub(zp);
    }


    /**
     * See the z-gap of the segments P1P2
     * and Q1Q2 at the (x,y) coordinate where
     * their z=0 projection intersects.
     */
    public static int sinal(EVertice P1, EVertice P2, EVertice Q1, EVertice Q2) {

        BigRational xp1 = P1.getX();
        BigRational xp2 = P2.getX();
        BigRational yp1 = P1.getY();
        BigRational yp2 = P2.getY();
        BigRational zp1 = P1.getZ();
        BigRational zp2 = P2.getZ();

        BigRational xq1 = Q1.getX();
        BigRational xq2 = Q2.getX();
        BigRational yq1 = Q1.getY();
        BigRational yq2 = Q2.getY();
        BigRational zq1 = Q1.getZ();
        BigRational zq2 = Q2.getZ();

        // (-xq2*yp2 + xq2*yp1 + xq1*yp2 - xq1*yp1 + yq2*xp2 - yq2*xp1 - yq1*xp2 + yq1*xp1)
        BigRational denom =
            xq2.neg().mul(yp2).
            add(xq2.mul(yp1)).
            add(xq1.mul(yp2)).
            sub(xq1.mul(yp1)).
            add(yq2.mul(xp2)).
            sub(yq2.mul(xp1)).
            sub(yq1.mul(xp2)).
            add(yq1.mul(xp1));


        if (denom.equals(denom.ZERO)) {
            // a reta r(k) = P1 + k(P2-P1)
            // encontrar k0 tal que r(k0) = (xq1, yy)
            // se (yy == yq1) então ok!

            // check the case
            BigRational k0 = xq1.sub(xp1).div(xp2.sub(xp1));
            BigRational yy = yp1.add(k0.mul(yp2.sub(yp1)));

            // colinear?
            System.out.println("Denominador é zero: não tem solução");
            if (yy.compareTo(yq1) == 0) {
                BigRational k1 = xq2.sub(xp1).div(xp2.sub(xp1));
                System.out.println("they are colinear at "+k0+" and "+k1);
            }
            else {
                System.out.println("NOT colinear");
            }
            return 0;
        }

        // (xq2*yp1+yq2*xq1-xq2*yq1-xq1*yp1-yq2*xp1+yq1*xp1)
        BigRational numS =
            xq2.mul(yp1).
            add(yq2.mul(xq1)).
            sub(xq2.mul(yq1)).
            sub(xq1.mul(yp1)).
            sub(yq2.mul(xp1)).
            add(yq1.mul(xp1));

        // -(-xq1*yp2+xq1*yp1+yq1*xp2+xp1*yp2-yp1*xp2-yq1*xp1)
        BigRational numT =
            xq1.neg().mul(yp2).
            add(xq1.mul(yp1)).
            add(yq1.mul(xp2)).
            add(xp1.mul(yp2)).
            sub(yp1.mul(xp2)).
            sub(yq1.mul(xp1)).
            neg();

        BigRational s = numS.div(denom);
        BigRational t = numT.div(denom);

        if (s.compareTo(BigRational.ZERO) <= 0 ||
            s.compareTo(BigRational.ONE) >= 1 ||
            t.compareTo(BigRational.ZERO) <= 0 ||
            t.compareTo(BigRational.ONE) >= 1) {
            // System.out.println("s ou t estão fora de (0,1): não tem solução");
            return 0;
        }

        // test intersections on the projected thing
        BigRational xp = xp1.add(xp2.sub(xp1).mul(s));
        BigRational xq = xq1.add(xq2.sub(xq1).mul(t));
        BigRational yp = yp1.add(yp2.sub(yp1).mul(s));
        BigRational yq = yq1.add(yq2.sub(yq1).mul(t));

        BigRational xa = xp2.sub(xp1);
        BigRational ya = yp2.sub(yp1);
        BigRational xb = xq2.sub(xq1);
        BigRational yb = yq2.sub(yq1);

        BigRational crossProd = xa.mul(yb).sub(ya.mul(xb));

        // o gap
        BigRational zp = zp1.add(zp2.sub(zp1).mul(s));
        BigRational zq = zq1.add(zq2.sub(zq1).mul(t));
        BigRational gap = zq.sub(zp);

        BigRational sinal = crossProd.mul(gap).neg();

        if (sinal.compareTo(BigRational.ZERO) < 0)
            return -1;
        else if (sinal.compareTo(BigRational.ZERO) > 0)
            return 1;
        else  {
            System.out.println("xa ya = "+xa+","+ya);
            System.out.println("xb yb = "+xb+","+yb);
            System.out.println("P1 P2 = "+xp1+","+yp1+","+zp1+"    "+xp2+","+yp2+","+zp2);
            System.out.println("Q1 Q2 = "+xq1+","+yq1+","+zq1+"    "+xq2+","+yq2+","+zq2);
            System.out.println("Inter = "+xp+","+yp+" = "+xq+","+yq+"  zp,zq = "+zp+", "+zq);
            System.out.println("Zero assim CrossProd: "+crossProd+" gap "+gap);
            return 0;
        }
    }
}
