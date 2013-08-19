package blink.cli;

import java.awt.BorderLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import linsoft.graph.OrthogonalLayout;
import linsoft.graph.PanelPlanarRepresentation;
import linsoft.graph.PlanarRepresentation;
import linsoft.netsimplex.Network;

import org.jscience.mathematics.numbers.Complex;

import blink.App;
import blink.BlinkDrawing;
import blink.BlinkEntry;
import blink.BlinkGemGraph;
import blink.ClassEntry;
import blink.ClassHGNormQI;
import blink.ColorGBlinks;
import blink.CombineGBlinks;
import blink.EPSCatalog;
import blink.EPSLibrary;
import blink.FilterGBlinksUsingRM3;
import blink.FindGemsThatShouldBeTheSameByHGQI;
import blink.FourCluster;
import blink.GBlink;
import blink.GBlinkDrawing;
import blink.GBlinkEdgeType;
import blink.GBlinkEmbeddingGraph;
import blink.GBlinkVertex;
import blink.Gem;
import blink.GemColor;
import blink.GemEntry;
import blink.GemGraph;
import blink.GemPackedLabelling;
import blink.GemPathEntry;
import blink.GemPathRepository;
import blink.GemPrimeStatus;
import blink.GemPrimeTest;
import blink.GemRepository;
import blink.GemSimplificationPathFinder;
import blink.GemVertex;
import blink.GenerateBlockMaps;
import blink.GenerateMaps3TConnected;
import blink.HomologyGroup;
import blink.Library;
import blink.LinkDrawing;
import blink.MapPackedWord;
import blink.PanelBlinkGemGraph;
import blink.PanelDrawBlinks;
import blink.PanelDrawGBlinks;
import blink.PanelDrawLinks;
import blink.PanelReductionGraph;
import blink.PanelString;
import blink.PanelTutteDrawBlinks;
import blink.Path;
import blink.PointOfAdjacentOppositeCurls;
import blink.PointOfAlpha1Move;
import blink.PointOfReidemeisterII;
import blink.PointOfReidemeisterIII;
import blink.QI;
import blink.Quartet;
import blink.RhoPair;
import blink.SavePDF;
import blink.SearchAttractor;
import blink.TryToConnectGemsFromTheSameHGQI;
import blink.Twistor;
import edu.uci.ics.jung.graph.Graph;

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
public class SomeFunctions {
    public SomeFunctions() {
    }
}

class FunctionGem extends Function {
    public FunctionGem() {
        super("gem","3-Gem multi constructor");
    }

    public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
        try {
            Object result = hardwork(params, localData);
            return result;
        } catch (EvaluationException ex) {
            throw ex;
        }
        catch (Exception e) {
            throw new EvaluationException(e.getMessage());
        }
    }

    private Object hardwork(ArrayList<Object> params, DataMap localData) throws EvaluationException, Exception {
        if (params.size() == 1) {
            // gblink gem
            if (params.get(0) instanceof GBlink) {


                boolean blueConstruction = false;
                try {
                    blueConstruction = ((Number) localData.getData("blue")).intValue() == 1;
                } catch (Exception e) {}


                Gem G;
                if (blueConstruction) {
                    // use the map of the gblink as a constructor
                    // do not induce the same space as the gblink
                    G = new Gem((GBlink) params.get(0));
                }
                else {
                    // gem that induces the same space as
                    // the one
                    G = ((GBlink) params.get(0)).getGem();
                }

                G.goToCodeLabel();
                return G;
            }
            else if (params.get(0) instanceof String) {
                // up to 26 * 2 * 2 vertices
                String lettersString = (String) params.get(0);
                GemPackedLabelling GPL = new GemPackedLabelling(lettersString,0);
                Gem G = new Gem(GPL);
                return G;
            }

            // gem with given id on database
            else {
                Gem gem = App.getRepositorio().getGemById(((Number) params.get(0)).longValue()).getGem();
                return gem;
            }
        }
        else if (params.size() == 2) {
            Gem gem = App.getRepositorio().getGemEntryByCatalogNumber(
                    ((Number) params.get(0)).intValue(),
                    ((Number) params.get(1)).intValue(),
                    0).getGem();
            return gem;
        }
        else if (params.size() == 4) {
            Gem gem = new Gem(
                    ((Number) params.get(0)).intValue(),
                    ((Number) params.get(1)).intValue(),
                    ((Number) params.get(2)).intValue(),
                    ((Number) params.get(3)).intValue());
            return gem;
        }
        else throw new EvaluationException("Not implemented");
    }

}

class FunctionReidemeisterIII extends Function {
    public FunctionReidemeisterIII() {
        super("rm3","Reidemeister III");
    }
    public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
        try {
            Object result = hardwork(params, localData);
            return result;
        } catch (EvaluationException ex) {
            throw ex;
        }
        catch (Exception e) {
            throw new EvaluationException(e.getMessage());
        }
    }

    private Object hardwork(ArrayList<Object> params, DataMap localData) throws EvaluationException, Exception {


        boolean closure = false;
        try {
            closure = ((Number) localData.getData("closure")).intValue() == 1;
        } catch (Exception e) {}

        if (closure) {
            GBlink G = ((GBlink) params.get(0));
            ArrayList<GBlink> list = G.getReidemeisterIIIClosure();
            return list;
            /*
            StringBuffer sb = new StringBuffer();
            sb.append("Size: "+list.size());
            for (GBlink GG: list) {
                sb.append("\n");
                sb.append(GG.getBlinkWord().toString());
            }
            return sb.toString();*/
        }



        if (params.size() == 1) {
            GBlink G = ((GBlink) params.get(0));
            ArrayList<PointOfReidemeisterIII> list = G.findAllReidemeisterIIIPoints();
            StringBuffer sb = new StringBuffer();
            boolean first = true;
            for (PointOfReidemeisterIII p: list) {
                if (!first)
                    sb.append("\n");
                sb.append(p.description(G));
                first = false;
            }
            return sb.toString();
        }

        else if (params.size() == 3) {
            GBlink G = ((GBlink) params.get(0)).copy();
            int vLabel = ((Number) params.get(1)).intValue();
            String vertexOrFace = (String) params.get(2);
            PointOfReidemeisterIII p = null;
            if ("vertex".equals(vertexOrFace.toLowerCase()) || "v".equals(vertexOrFace.toLowerCase())) {
                p = new PointOfReidemeisterIII(vLabel,true);
            }
            else if ("face".equals(vertexOrFace.toLowerCase()) || "f".equals(vertexOrFace.toLowerCase())) {
                p = new PointOfReidemeisterIII(vLabel,false);
            }
            G.applyReidemeisterIIIMove(p);
            G.goToCodeLabelPreservingSpaceOrientation();
            return G;
        }
        else throw new EvaluationException("Not implemented");
    }
}

class FunctionReidemeisterII extends Function {
    public FunctionReidemeisterII() {
        super("rm2","Reidemeister II");
    }
    public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
        try {
            Object result = hardwork(params, localData);
            return result;
        } catch (EvaluationException ex) {
            throw ex;
        }
        catch (Exception e) {
            throw new EvaluationException(e.getMessage());
        }
    }

    private Object hardwork(ArrayList<Object> params, DataMap localData) throws EvaluationException, Exception {
        if (params.size() == 1) {
            GBlink G = ((GBlink) params.get(0));
            ArrayList<PointOfReidemeisterII> list = G.findAllReidemeisterIIPoints();
            StringBuffer sb = new StringBuffer();
            boolean first = true;
            for (PointOfReidemeisterII p: list) {
                if (!first)
                    sb.append("\n");
                sb.append(p.description(G));
                first = false;
            }
            return sb.toString();
        }

        else if (params.size() == 3) {
            GBlink G = ((GBlink) params.get(0)).copy();
            int vLabel = ((Number) params.get(1)).intValue();
            String vertexOrFace = (String) params.get(2);
            PointOfReidemeisterII p = null;
            if ("vertex".equals(vertexOrFace.toLowerCase()) || "v".equals(vertexOrFace.toLowerCase())) {
                p = new PointOfReidemeisterII(vLabel,true);
            }
            else if ("face".equals(vertexOrFace.toLowerCase()) || "f".equals(vertexOrFace.toLowerCase())) {
                p = new PointOfReidemeisterII(vLabel,false);
            }
            G.applyReidemeisterIIMove(p);
            G.goToCodeLabelPreservingSpaceOrientation();
            return G;
        }
        else throw new EvaluationException("Not implemented");
    }
}

class FunctionDrawCP extends Function {
    public FunctionDrawCP() {
        super("drawcp","Draw with circle packing");
    }
    public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
        try {
            Object result = hardwork(params, localData);
            return result;
        } catch (EvaluationException ex) {
            ex.printStackTrace();
            throw ex;
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EvaluationException(e.getMessage());
        }
    }

    private Object hardwork(ArrayList<Object> params, DataMap localData) throws EvaluationException, Exception {
        GBlink G = ((GBlink) params.get(0));

        int width = 500;
        int height = 500;
        boolean drawLink = false;
        boolean drawCircles = false;
        boolean drawBlink = true;
        String eps = null;

        try {
            width = ((Number) localData.getData("w")).intValue();
        } catch (Exception e) {}
        try {
            height = ((Number) localData.getData("h")).intValue();
        } catch (Exception e) {}
        try {
            drawLink = ((Number) localData.getData("L")).intValue() == 1;
        } catch (Exception e) {}
        try {
            drawCircles = ((Number) localData.getData("C")).intValue() == 1;
        } catch (Exception e) {}
        try {
            drawBlink = ((Number) localData.getData("B")).intValue() == 1;
        } catch (Exception e) {}
        try {
            eps = ((String) localData.getData("eps")).toString();
        } catch (Exception e) {}

        // execute program
        G = G.getWithProtectedCrossings();
        GBlinkEmbeddingGraph GG = new GBlinkEmbeddingGraph(G);
        GG.findPackingLabel(true);


        if (eps == null) {
            BufferedImage img = GG.getImage(width, height, drawCircles, drawLink, drawBlink);

            JFrame f = new JFrame("Circle Packing Drawing: " +
                                  CommandLineInterface.getInstance()._currentCommand.replace('\n', ' '));
            linsoft.gui.util.Library.resizeAndCenterWindow(f, 640, 480);
            f.setContentPane(new PanelDrawing(img));
            f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            f.setVisible(true);
            return null;
        }
        else {
            GG.drawEPS(eps,6,6,0.25,drawLink,drawBlink,drawCircles);
            return "File "+eps+" saved with success!";
        }
    }

    static class PanelDrawing extends JPanel {
        private BufferedImage _img;

        public PanelDrawing(BufferedImage img) {
            _img = img;
        }

        public void paint(Graphics g) {
            Graphics2D g2d = (Graphics2D) g;
            Rectangle bounds = getBounds();
            Image scaledImg = _img.getScaledInstance(bounds.width, bounds.height, Image.SCALE_SMOOTH);
            g.drawImage(scaledImg, 0, 0, null);
        }
    }

}

class FunctionTwistor extends Function {
    public FunctionTwistor() {
        super("twistor","3-Gem twistors");
    }

    public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
        try {
            Object result = hardwork(params, localData);
            return result;
        } catch (EvaluationException ex) {
            throw ex;
        }
        catch (Exception e) {
            throw new EvaluationException(e.getMessage());
        }
    }

    private Object hardwork(ArrayList<Object> params, DataMap localData) throws EvaluationException, Exception {

        if (params.size() == 1) {

            Gem G = (Gem) params.get(0);
            ArrayList<Twistor> twistors = G.findAllTwistors();

            boolean first = false;
            StringBuffer sb = new StringBuffer();
            for (Twistor t: twistors) {
                if (!first)
                    sb.append("\n");
                sb.append(t.toString());
                first = false;
            }
            return sb.toString();

        }

        else {

            Gem G = (Gem) params.get(0);

            GemColor c1 = GemColor.getByNumber(((Number)params.get(1)).intValue());
            GemColor c2 = GemColor.getByNumber(((Number)params.get(2)).intValue());
            int v1 = ((Number)params.get(3)).intValue();
            int v2 = ((Number)params.get(4)).intValue();

            Gem copy = G.copy();
            ArrayList<Twistor> list = copy.findAllTwistors();
            Twistor choosed = null;
            for (Twistor ts: list) {
                if (
                        (ts.getU().getLabel() == v1 || ts.getU().getLabel() == v2) &&
                        (ts.getV().getLabel() == v1 || ts.getV().getLabel() == v2) &&
                        (ts.getColor() == c2)
                    ) {
                    choosed = ts;
                    break;
                }
            }
            copy.applyTwistor(choosed,c1);
            return copy;
        }
    }
}

class FunctionAttractor extends Function {

    public FunctionAttractor() {
        super("att","Find attractor for 3-gem");
    }

    public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
        try {
            Object result = hardwork(params, localData);
            return result;
        } catch (EvaluationException ex) {
            ex.printStackTrace();
            throw ex;
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EvaluationException(e.getMessage());
        }
    }

    private Object hardwork(ArrayList<Object> params, DataMap localData) throws EvaluationException, Exception {
        boolean pathFormat = false;
        try { pathFormat = (((Number)localData.getData("path")).intValue() == 1); } catch (Exception e) {}


        if (params.get(0) instanceof ArrayList) {
            if (pathFormat) {

                long t0 = System.currentTimeMillis();
                ArrayList<GBlink> list = (ArrayList<GBlink>) params.get(0);

                String export = null;
                try {
                    export = ((String) localData.getData("export")).toString();
                } catch (Exception e) {}

                long time = 60000;
                try {
                    time = ((Number) localData.getData("time")).longValue();
                } catch (Exception e) {}

                PrintWriter pw = null;
                if (export != null)
                    pw = new PrintWriter(new FileWriter(export));

                StringBuffer sb = new StringBuffer();
                int count = 0;
                int countSuccess = 0;
                for (GBlink G : list) {
                    Gem gem = G.getGem();
                    gem.goToCodeLabel();

                    double t = (System.currentTimeMillis() - t0) / 1000.0;
                    double est = count / t * (list.size() - count);

                    System.out.println(String.format(
                            "Processing gblink %4s of %4d   Success %4d (%6.2f%%)   estimateTime: %6.2f   elapsedTime: %6.2f",
                            count + 1,
                            list.size(),
                            countSuccess,
                            (double) countSuccess / count * 100.0,
                            est, t));

                    count++;
                    SearchAttractor A = new SearchAttractor(G.getGem(), time);
                    if (A.isBestAttractorTSClassRepresentant()) {
                        countSuccess++;
                        Path path = A.getBestPath();
                        String solution = String.format("%s\t%d\t%s\t%s",
                                                        G.getBlinkWord().toString(),
                                                        A.getBestAttractorTSClassSize(),
                                                        path.getSignature(),
                                                        A.getBestAttractorFound().getCurrentLabelling().
                                                        getLettersString(""));
                        System.out.println(solution);

                        // cantar
                        Library.playSound("solucao.wav", 2000);

                        if (export != null) {
                            pw.println(solution);
                            pw.flush();
                        }
                        sb.append(solution + "\n");
                    } else {
                        sb.append("attractor not found for " + G.getBlinkWord().toString());
                        System.out.println("Not found");
                    }
                }

                if (export != null)
                    pw.close();

                return sb.toString();
            }

            // just return the attractors
            else {
                ArrayList<Gem> gems = new ArrayList<Gem>();
                for (Object o: (List) params.get(0)) {
                    Gem G;
                    if (o instanceof GBlink) {
                        G = ((GBlink) o).getGem();
                    } else
                        G = ((Gem) o);
                    G.goToCodeLabel();
                    G = G.copy();

                    SearchAttractor A = new SearchAttractor(G,60000L);
                    gems.add(A.getBestAttractorFound());
                }
                return gems;
            }
        }
        else {
            Gem G;
            if (params.get(0) instanceof GBlink) {
                G = ((GBlink) params.get(0)).getGem();
            } else
                G = ((Gem) params.get(0));
            G = G.copy();
            G.goToCodeLabel();
            PanelReductionGraph prg = new PanelReductionGraph(G);

            Gem attG = prg.getAttractor();

            JFrame f = new JFrame("Attractor: " + CommandLineInterface.getInstance()._currentCommand.replace('\n', ' '));
            linsoft.gui.util.Library.resizeAndCenterWindow(f, 640, 480);
            f.setContentPane(prg);
            f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            f.setVisible(true);

            return attG;
        }
    }
}

class FunctionCompare extends Function {

    public FunctionCompare() {
        super("compare","Compare g-blink or gems");
    }

    public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
        try {
            Object result = hardwork(params, localData);
            return result;
        } catch (EvaluationException ex) {
            throw ex;
        }
        catch (Exception e) {
            throw new EvaluationException(e.getMessage());
        }
    }

    private Object hardwork(ArrayList<Object> params, DataMap localData) throws EvaluationException, Exception {

        if (params.get(0) instanceof GBlink) {
            GBlink A = ((GBlink) params.get(0));
            GBlink B = ((GBlink) params.get(1));
            int compareResult = A.compareTo(B);
            return new Integer(compareResult);
        }
        else if (params.get(0) instanceof Gem) {
            Gem A = ((Gem) params.get(0));
            Gem B = ((Gem) params.get(1));
            int compareResult = A.compareTo(B);
            return new Integer(compareResult);
        }
        else throw new EvaluationException("this function compares gblink and gems");

    }

}

class FunctionReflection extends Function {
    public FunctionReflection() {
        super("reflection","Reflection of a g-blink");
    }

    public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
        if (params.size() == 1) {
            Object a = params.get(0);
            GBlink G;
            if (a instanceof BlinkEntry) {
                G = ((BlinkEntry)a).getBlink();
            }
            else if (a instanceof GBlink) {
                G = (GBlink)a;
            }
            else throw new EvaluationException("first argument of qi must be");

            return G.reflection();
        }
        else return null;
    }
}

class FunctionRefDual extends Function {
    public FunctionRefDual() {
        super("refDual","RefDual of a g-blink");
    }

    public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
        try {
            Object result = hardwork(params, localData);
            return result;
        } catch (EvaluationException ex) {
            ex.printStackTrace();
            throw ex;
        } catch (Exception e) {
            e.printStackTrace();
            throw new EvaluationException(e.getMessage());
        }
    }

    private Object hardwork(ArrayList<Object> params, DataMap localData) throws EvaluationException {
        if (params.get(0) instanceof GBlink) {
            return ((GBlink) params.get(0)).refDual();
        }
        else {
            ArrayList<GBlink> result = new ArrayList<GBlink>();
            ArrayList list = (ArrayList) params.get(0);
            for (Object b: list) {
                result.add(((GBlink) b).refDual());
            }
            return result;
        }
    }
}

class FunctionProtect extends Function {
    public FunctionProtect() {
        super("protect","Protect a g-blink");
    }

    public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
        try {
            Object result = hardwork(params, localData);
            return result;
        } catch (EvaluationException ex) {
            ex.printStackTrace();
            throw ex;
        } catch (Exception e) {
            e.printStackTrace();
            throw new EvaluationException(e.getMessage());
        }
    }

    private Object hardwork(ArrayList<Object> params, DataMap localData) throws EvaluationException {
        if (params.get(0) instanceof GBlink) {
            return ((GBlink) params.get(0)).getWithProtectedCrossings();
        }
        else {
            ArrayList<GBlink> result = new ArrayList<GBlink>();
            ArrayList list = (ArrayList) params.get(0);
            for (Object b: list) {
                result.add(((GBlink) b).getWithProtectedCrossings());
            }
            return result;
        }
    }
}

class FunctionRefDualLabel extends Function {
    public FunctionRefDualLabel() {
        super("refDualLabel","RefDual of a g-blink vertex label");
    }

    public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
        GBlink A = ((GBlink) params.get(0)).copy();
        int a = ((Number) params.get(1)).intValue();
        GBlinkVertex va = A.findVertex(a);
        A.goToRefDual();
        return new Integer(va.getLabel());
    }
}

class FunctionReflectionLabel extends Function {
    public FunctionReflectionLabel() {
        super("reflectionLabel","Reflection of a g-blink vertex label");
    }

    public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
        try {
            Object result = hardwork(params, localData);
            return result;
        } catch (EvaluationException ex) {
            ex.printStackTrace();
            throw ex;
        } catch (Exception e) {
            e.printStackTrace();
            throw new EvaluationException(e.getMessage());
        }
    }

    private Object hardwork(ArrayList<Object> params, DataMap localData) throws EvaluationException {
        if (params.get(0) instanceof GBlink) {
            return ((GBlink) params.get(0)).reflection();
        }
        else {
            ArrayList<GBlink> result = new ArrayList<GBlink>();
            ArrayList list = (ArrayList) params.get(0);
            for (Object b: list) {
                result.add(((GBlink) b).reflection());
            }
            return result;
        }
    }
}

class FunctionDBStatus extends Function {
    public FunctionDBStatus() {
        super("dbStatus","Database status");
    }
    public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
        try {
            Object result = hardwork(params, localData);
            return result;
        } catch (EvaluationException ex) {
            throw ex;
        }
        catch (Exception e) {
            throw new EvaluationException(e.getMessage());
        }
    }

    private Object hardwork(ArrayList<Object> params, DataMap localData) throws EvaluationException, Exception {

        HashMap<Integer,Integer> mapBlinksByNumEdges = new HashMap<Integer,Integer>();
        HashMap<Integer,Integer> mapNumComponents = new HashMap<Integer,Integer>();
        HashSet<Long> gem = new HashSet<Long>();
        HashSet<Long> minGem = new HashSet<Long>();
        int undefinedGem = 0;
        int undefinedMinGem = 0;
        ArrayList<BlinkEntry> list = App.getRepositorio().getBlinks(0,20);
        int numBlinks = list.size();
        int num1Connected = 0;
        for (BlinkEntry be: list) {
            int nEdges = be.get_numEdges();
            Integer num = mapBlinksByNumEdges.get(nEdges);
            if (num == null)
                num = 0;
            mapBlinksByNumEdges.put(be.get_numEdges(),num+1);
            GBlink G = be.getBlink();

            if (G.containsABreakpair())
                num1Connected++;

            int zigZags = G.getNumberOfGZigZags();
            num = mapNumComponents.get(zigZags);
            if (num == null)
                num = 0;
            mapNumComponents.put(zigZags,num+1);

            if (be.get_gem() <= 0)
                undefinedGem++;
            else
                gem.add(be.get_gem());

            if (be.getMinGem() <= 0)
                undefinedMinGem++;
            else
                minGem.add(be.getMinGem());
        }
        int numClassesHGNormQI = App.getRepositorio().getHGNormQIClasses(App.MAX_EDGES).size();

        StringBuffer sb = new StringBuffer();

        sb.append("database: "+App.getProperty(App.DB_NAME_PROPERTY)+"\n\n");
        sb.append("num gblinks: "+numBlinks+"\n");
        sb.append("num gblinks 1-connected: "+num1Connected+" "+String.format("%6.2f%%\n",100.0*(double)num1Connected/(double)numBlinks));
        sb.append("num classes hg,norm(qi): "+numClassesHGNormQI+"\n");
        sb.append("num gblink with without ts-gem: "+undefinedGem+"\n");
        sb.append("num gblink with without ts-minBem: "+undefinedMinGem+"\n");
        sb.append("distinct ts-gems: "+gem.size()+"\n");
        sb.append("distinct ts-minGems: "+minGem.size()+"\n");
        ArrayList<Integer> listKeys = new ArrayList<Integer>(mapBlinksByNumEdges.keySet());
        Collections.sort(listKeys);
        int acum = 0;
        for (Integer i: listKeys) {
            int count = mapBlinksByNumEdges.get(i);
            acum += count;
            sb.append(String.format(
                    "\nnum gblinks with %2d edges %6d (%6.2f%%)  acum. %6d (%6.2f%%)",
                    i, count, 100.0 * (double) count / (double) numBlinks,
                    acum, 100.0 * (double) acum / (double) numBlinks));
        }

        // log zigzags
        listKeys = new ArrayList<Integer>(mapNumComponents.keySet());
        Collections.sort(listKeys);
        acum = 0;
        for (Integer i: listKeys) {
            int count = mapNumComponents.get(i);
            acum += count;
            sb.append(String.format(
                    "\nnum gblinks with %2d components %6d (%6.2f%%)  acum. %6d (%6.2f%%)",
                    i, count, 100.0 * (double) count / (double) numBlinks,
                    acum, 100.0 * (double) acum / (double) numBlinks));
        }
        return sb.toString();
    }
}

class FunctionChangeDB extends Function {
    public FunctionChangeDB() {
        super("db","Change database name");
    }
    public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
        try {
            Object result = hardwork(params, localData);
            return result;
        } catch (EvaluationException ex) {
            throw ex;
        }
        catch (Exception e) {
            throw new EvaluationException(e.getMessage());
        }
    }

    private Object hardwork(ArrayList<Object> params, DataMap localData) throws EvaluationException, Exception {
        String currentDBName = App.getConfiguracao().getProperty(App.DB_NAME_PROPERTY);
        if (params.size() == 0)
            return currentDBName;

        String newName = (String) params.get(0);
        App.getConfiguracao().setProperty(App.DB_NAME_PROPERTY,newName);
        App.getRepositorio().getConnection().close();


        // App.restartDbDriver();

        return currentDBName+" -> "+newName;
    }
}

class FunctionLogHGNormQIClasses extends Function {
    public FunctionLogHGNormQIClasses() {
        super("logClassesHGNormQI","Log classed HG and NormQI");
    }
    public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
        try {
            Object result = hardwork(params, localData);
            return result;
        } catch (EvaluationException ex) {
            throw ex;
        }
        catch (Exception e) {
            throw new EvaluationException(e.getMessage());
        }
    }

    private Object hardwork(ArrayList<Object> params, DataMap localData) throws EvaluationException, Exception {

        ArrayList<ClassHGNormQI> list = App.getRepositorio().getHGNormQIClasses(App.MAX_EDGES);

        boolean full = false;
        try { if (((Number) localData.getData("full")).intValue() == 1) full = true;
        } catch (Exception e) {}

        boolean polar = true;
        try {
            if (localData.getData("polar") != null) {
                if (((Number) localData.getData("polar")).intValue() == 0)
                    polar = false;
            }
        } catch (Exception e) {}

        StringBuffer sb = new StringBuffer();
        if (!full) {
            for (ClassHGNormQI C : list) {
                long qiId = C.get_qi(0);
                QI qi = App.getRepositorio().getQI(qiId).normalize();
                C.load();
                GBlink G = C.getBlinks().get(0).getBlink();
                sb.append(String.format("hg{%s} qi{%s} gblink{%s}\n", C.get_hg(), qi.getValuesInString(polar, ','),
                                        G.getBlinkWord().toString()));
            }
        }
        else {
            for (ClassHGNormQI C : list) {
                long qiId = C.get_qi(0);
                QI qi = App.getRepositorio().getQI(qiId).normalize();
                C.load();
                GBlink G = C.getBlinks().get(0).getBlink();
                sb.append(String.format("hg{%s} qi{%s}\n", C.get_hg(), qi.getValuesInString(polar, ',')));
                for (BlinkEntry b: C.getBlinks()) {
                    sb.append(String.format("%s\n", b.getBlink().getBlinkWord().toString()));
                }
            }
        }

        if (params.size() == 1) {
            String fileName = (String) params.get(0);
            PrintWriter pw = new PrintWriter(new FileWriter(fileName));
            pw.print(sb.toString());
            pw.close();
            sb.setLength(0);
            sb.append(String.format("File %s saved with output",fileName));
        }
        return sb.toString();
    }
}


class FunctionPrint extends Function {
    public FunctionPrint() {
        super("print","Print GBlink Code or Gem Code");
    }
    public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
        try {
            Object result = hardwork(params, localData);
            return result;
        } catch (EvaluationException ex) {
            ex.printStackTrace();
            throw ex;
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EvaluationException(e.getMessage());
        }
    }

    private Object hardwork(ArrayList<Object> params, DataMap localData) throws EvaluationException, Exception {
        Object o = params.get(0);
        if (o instanceof List) {
            StringBuffer sb = new StringBuffer();
            for (Object oo: (List) o) {
                sb.append(""+printOneElement(oo,localData)+"\n");
            }
            return sb.toString();
        }
        else return printOneElement(o,localData);
    }

    private Object printOneElement(Object o, DataMap localData) throws EvaluationException, Exception {
        if (o instanceof GBlink) {
            GBlink G = (GBlink) o;
            return "\""+G.getBlinkWord().toString()+"\"";
        }
        else if (o instanceof Gem) {
            Gem G = (Gem) o;
            return "\""+G.getCurrentLabelling().getLettersString("")+"\"";
        }
        else if (o instanceof ClassEntry) {
            return ((ClassEntry) o).getDescription();
        }
        return "";
    }
}


class FunctionSwap extends Function {
    public FunctionSwap() {
        super("swap","Swap parity or squares or colors of a g-blink");
    }
    public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
        try {
            Object result = hardwork(params, localData);
            return result;
        } catch (EvaluationException ex) {
            throw ex;
        }
        catch (Exception e) {
            throw new EvaluationException(e.getMessage());
        }
    }

    private Object hardwork(ArrayList<Object> params, DataMap localData) throws EvaluationException, Exception {
        boolean parity = false;
        boolean faceEdgeVertexEdge = false;
        boolean crossings = true;
        try { parity = ((Number) localData.getData("P")).intValue() == 1; } catch (Exception e) {}
        try { faceEdgeVertexEdge = ((Number) localData.getData("FV")).intValue() == 1; } catch (Exception e) {}
        try { crossings = ((Number) localData.getData("C")).intValue() == 1; } catch (Exception e) {}

        if (params.get(0) instanceof GBlink) {
            // return ((GBlink) params.get(0)).getRepresentantNotPreservingOrientation();
            GBlink G = (GBlink) params.get(0);
            GBlink result = G.copy();
            result.swap(parity,crossings,faceEdgeVertexEdge);
            return result;
        }
        else {
            ArrayList<GBlink> result = new ArrayList<GBlink>();
            ArrayList list = (ArrayList) params.get(0);
            for (Object b: list) {
                GBlink G = (GBlink) b;
                GBlink Gc = G.copy();
                Gc.swap(parity,crossings,faceEdgeVertexEdge);
                result.add(Gc);
            }
            return result;
        }
    }
}

class FunctionExport extends Function {
    public FunctionExport() {
        super("exportGBlinks","Export g-blinks to text file");
    }
    public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
        try {
            Object result = hardwork(params, localData);
            return result;
        } catch (EvaluationException ex) {
            throw ex;
        }
        catch (Exception e) {
            throw new EvaluationException(e.getMessage());
        }
    }

    private Object hardwork(ArrayList<Object> params, DataMap localData) throws EvaluationException, Exception {
        ArrayList<GBlink> blinks = new ArrayList<GBlink>();
        ArrayList<BlinkEntry> list = App.getRepositorio().getBlinks(0, 20);
        for (BlinkEntry be: list) {
            blinks.add(be.getBlink());
        }
        Collections.sort(blinks);

        String st = (String) params.get(0);
        PrintWriter pw = new PrintWriter(new FileWriter(st));
        for (GBlink G: blinks) {
            pw.println(G.getBlinkWord().toString());
        }
        pw.close();
        return "File "+st+" saved with success!";
    }
}

class FunctionLoadGBlinks extends Function {
    public FunctionLoadGBlinks() {
        super("loadGBlinks","Load g-blinks from file");
    }
    public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
        try {
            Object result = hardwork(params, localData);
            return result;
        } catch (EvaluationException ex) {
            ex.printStackTrace();
            throw ex;
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EvaluationException(e.getMessage());
        }
    }

    private Object hardwork(ArrayList<Object> params, DataMap localData) throws EvaluationException, Exception {
        String fileName = (String) params.get(0);
        BufferedReader br = new BufferedReader(new FileReader(fileName));
        ArrayList<GBlink> result = new ArrayList<GBlink>();
        String s;
        int i = 1;
        while ((s = br.readLine()) != null) {
            result.add(new GBlink(s));
        }
        br.close();
        return result;
    }
}

class FunctionDiffGBlinks extends Function {
    public FunctionDiffGBlinks() {
        super("diffGBlinks","Diff g-blinks from file");
    }
    public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
        try {
            Object result = hardwork(params, localData);
            return result;
        } catch (EvaluationException ex) {
            throw ex;
        }
        catch (Exception e) {
            throw new EvaluationException(e.getMessage());
        }
    }

    private Object hardwork(ArrayList<Object> params, DataMap localData) throws EvaluationException, Exception {

        long t0 = System.currentTimeMillis();

        System.out.println("Creating sets 1");
        ArrayList<GBlink> set1 = (ArrayList<GBlink>) params.get(0);
        System.out.println("Creating sets 2");
        ArrayList<GBlink> set2 = (ArrayList<GBlink>) params.get(1);

        ArrayList<GBlink> diff12 = new ArrayList<GBlink>();
        ArrayList<GBlink> diff21 = new ArrayList<GBlink>();
        ArrayList<GBlink> intersection = new ArrayList<GBlink>();

        boolean maps = false;
        try { if (localData.getData("maps") != null) { if (((Number) localData.getData("maps")).intValue() == 1) maps = true; } } catch (Exception e) {}


        if (maps) {
            for (int i = 0; i < set1.size(); i++) {
                GBlink G = set1.get(i);
                GBlink GG = G.copy();
                GG.setColor(0);
                set1.set(i, GG);
            }
            for (int i = 0; i < set2.size(); i++) {
                GBlink G = set2.get(i);
                GBlink GG = G.copy();
                GG.setColor(0);
                set2.set(i, GG);
            }
        }
        Collections.sort(set1);
        Collections.sort(set2);

        for (int i=set1.size()-1;i>0;i--) {
            if (set1.get(i).equals(set1.get(i-1))) set1.remove(i);
        }
        for (int i=set2.size()-1;i>0;i--) {
            if (set2.get(i).equals(set2.get(i-1))) set2.remove(i);
        }

        System.out.println("Starting to calculate difference 1 2");
        int i=0;
        int j=0;
        while (i < set1.size() && j < set2.size()) {
            GBlink A = set1.get(i);
            GBlink B = set2.get(j);
            int comp = A.compareTo(B);
            if (comp < 0) {
                diff12.add(A);
                i++;
            }
            else if (comp > 0) {
                diff21.add(B);
                j++;
            }
            else {
                intersection.add(A);
                i++;
                j++;
            }
        }
        for (;i<set1.size();i++) {
            diff12.add(set1.get(i));
        }
        for (;j<set2.size();j++) {
            diff21.add(set2.get(j));
        }

        boolean detail = false;
        try { if (localData.getData("detail") != null) { if (((Number) localData.getData("detail")).intValue() == 1) detail = true; } } catch (Exception e) {}

        StringBuffer sb = new StringBuffer();
        sb.append(String.format("list 1:           %6d\n",set1.size()));
        sb.append(String.format("list 2:           %6d\n",set2.size()));
        sb.append(String.format("list 1 - list 2:  %6d (%6.2f%%)\n",diff12.size(),(100.0*diff12.size())/set1.size()));
        sb.append(String.format("list 2 - list 1:  %6d (%6.2f%%)\n",diff21.size(),(100.0*diff21.size())/set2.size()));
        sb.append(String.format("Intersection:     %6d (%6.2f%%) (%6.2f%%)",
                                intersection.size(),
                                (100.0*intersection.size())/set1.size(),
                                (100.0*intersection.size())/set2.size()));
        if (detail) {
            sb.append("\ndiff list 1 - list 2");
            for (GBlink b: diff12)
                sb.append("\n"+b.getBlinkWord().toString());
            sb.append("\ndiff list 2 - list 1");
            for (GBlink b: diff21)
                sb.append("\n"+b.getBlinkWord().toString());
            sb.append("\nintersection");
            Collections.sort(intersection);
            for (GBlink b: intersection)
                sb.append("\n"+b.getBlinkWord().toString());
        }
        sb.append(String.format("\nTime: %6.2f seg.",(System.currentTimeMillis()-t0)/1000.0));
        return sb.toString();
    }
}

class FunctionPrime extends Function {
    public FunctionPrime() {
        super("prime","G-Blink is prime?");
    }
    public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
        try {
            Object result = hardwork(params, localData);
            return result;
        } catch (EvaluationException ex) {
            ex.printStackTrace();
            throw ex;
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EvaluationException(e.getMessage());
        }
    }

    private Object hardwork(ArrayList<Object> params, DataMap localData) throws EvaluationException, Exception {
        Object o = params.get(0);
        if (o instanceof ArrayList) {

            String export = null;
            try { export = ((String)localData.getData("export")).toString(); } catch (Exception e) {}

            StringBuffer sbExport = new StringBuffer();

            long t0 = System.currentTimeMillis();
            ArrayList<GBlink> list = (ArrayList<GBlink>) o;
            HashMap<String,Integer> map = new HashMap<String,Integer>();
            GemPrimeTest test = new GemPrimeTest();
            int k=1;
            for (GBlink G: list) {
                System.out.println("isPrime "+(k++)+" of "+list.size());
                Gem gem = G.getGem();
                gem.goToCodeLabel();
                GemPrimeStatus status = test.test(gem);
                System.out.println(""+status.getSmallDescription());
                Integer i = map.get(status.getSmallDescription());
                if (i == null)
                    i=0;
                map.put(status.getSmallDescription(),i+1);
                if (export != null) {
                    sbExport.append(status.getSmallDescription()+" "+G.getBlinkWord().toString()+"\n");
                }
            }

            if (export != null) {
                PrintWriter pw = new PrintWriter(new FileWriter(export));
                pw.print(sbExport.toString());
                pw.close();
            }

            StringBuffer sb = new StringBuffer();
            ArrayList<String> listKeys = new ArrayList<String>(map.keySet());
            Collections.sort(listKeys);
            int acum = 0;
            sb.append("Prime Status for List");
            for (String i: listKeys) {
                int count = map.get(i);
                acum += count;
                sb.append(String.format(
                        "\n%30s -> %6d (%6.2f%%)  acum. %6d (%6.2f%%)",
                        i, count, 100.0 * (double) count / (double) list.size(),
                        acum, 100.0 * (double) acum / (double) list.size()));
            }
            sb.append(String.format("\nTime: %6.2f seg.",(System.currentTimeMillis()-t0)/1000.0));

            return sb.toString();
        }
        else if (params.get(0) instanceof GBlink) {
            GBlink G = (GBlink) params.get(0);
            GemPrimeTest test = new GemPrimeTest();
            Gem gem = G.getGem();
            gem.goToCodeLabel();
            GemPrimeStatus status = test.test(gem);
            return status.getSmallDescription();
        }
        else if (params.get(0) instanceof Gem) {
            Gem G = ((Gem) params.get(0)).copy();
            GemPrimeTest test = new GemPrimeTest();
            GemPrimeStatus status = test.test(G);
            return status.getSmallDescription();
        }
        else return null;
    }
}


class FunctionClassHGNormQI extends Function {
    public FunctionClassHGNormQI() {
        super("classHGNormQI","Log the classes HG Norm QI");
    }
    public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
        try {
            Object result = hardwork(params, localData);
            return result;
        } catch (EvaluationException ex) {
            throw ex;
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EvaluationException(e.getMessage());
        }
    }

    /**
     * @todo Fazer isso
     * @param params ArrayList
     * @param localData DataMap
     * @return Object
     * @throws EvaluationException
     * @throws Exception
     */
    private Object hardwork(ArrayList<Object> params, DataMap localData) throws EvaluationException, Exception {
        ArrayList<ClassHGNormQI> classes = App.getRepositorio().getHGNormQIClasses(App.MAX_EDGES);

        int r = 6;
        try { r = (int)Math.max(((Number)localData.getData("r")).intValue(),3); } catch(Exception e) {}

        Object o = params.get(0);
        if (o instanceof GBlink) {
            ClassHGNormQI result = null;
            boolean contains = false;
            GBlink G = (GBlink) o;
            String hg = G.homologyGroupFromGBlink().toString();
            QI qi = G.optimizedQuantumInvariant(3,r);
            for (ClassHGNormQI C : classes) {
                QI qiC = App.getRepositorio().getQI(C.get_qi(0));

                if (hg.equals(C.get_hg()) && qi.compareNormalizedEntriesUntilMaxR(qiC)) {
                    result = C;
                    C.load();
                    ArrayList<GBlink> blinks = new ArrayList<GBlink>();
                    for (BlinkEntry be: C.getBlinks()) {
                        blinks.add(be.getBlink());
                    }
                    if (blinks.contains(G)) {
                        contains = true;
                    }
                    break;
                }
            }

            if (result != null) {
                StringBuffer sb = new StringBuffer();
                sb.append(String.format("Class HG: %s  QI: %s  Size: %d  Present: %s  MinGBlink: %d\n",
                          result.get_hg(),
                          result.getStringOfQIs(),
                          result.getBlinks().size(),
                          contains + "",
                          result.getBlinks().get(0).get_numEdges())
                        );
                QI qiC = App.getRepositorio().getQI(result.get_qi(0));
                sb.append(qiC.toString()+"\n");
                for (BlinkEntry be: result.getBlinks()) {
                    sb.append(be.getBlink().getBlinkWord().toString()+"\n");
                }
                return sb.toString();
            }
            else return "Class HG x NormQI not found";
        }
        return null;
    }
}

class FunctionCancelDipoles extends Function {
    public FunctionCancelDipoles() {
        super("cancelDipoles","Cancel Dipoles of a gem");
    }
    public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
        try {
            Object result = hardwork(params, localData);
            return result;
        } catch (EvaluationException ex) {
            throw ex;
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EvaluationException(e.getMessage());
        }
    }

    /**
     * @todo Fazer isso
     * @param params ArrayList
     * @param localData DataMap
     * @return Object
     * @throws EvaluationException
     * @throws Exception
     */
    private Object hardwork(ArrayList<Object> params, DataMap localData) throws EvaluationException, Exception {
        Object o = params.get(0);
        Gem G;
        if (o instanceof GBlink) {
            G = ((GBlink) o).getGem();
            G.cancelAllDipoles();
            G.goToCodeLabel();
        }
        else {
            G = ((Gem) o).copy();
            G.cancelAllDipoles();
            G.goToCodeLabel();
        }
        return G;
    }
}


/**
 * Count the combination
 */
class FunctionCountSpaces extends Function {
    public FunctionCountSpaces() {
        super("countSpaces","Count combination");
    }
    public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
        try {
            Object result = hardwork(params, localData);
            return result;
        } catch (EvaluationException ex) {
            throw ex;
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EvaluationException(e.getMessage());
        }
    }

    /**
     * @todo Fazer isso
     * @param params ArrayList
     * @param localData DataMap
     * @return Object
     * @throws EvaluationException
     * @throws Exception
     */
    private Object hardwork(ArrayList<Object> params, DataMap localData) throws EvaluationException, Exception {

        long t0 = System.currentTimeMillis();

        ArrayList<ClassHGNormQI> classes = App.getRepositorio().getHGNormQIClasses(App.MAX_EDGES);

        ArrayList<CombinedSpace> primes = new ArrayList<CombinedSpace>();

        int i=0;
        for (ClassHGNormQI C: classes) {
            C.load();
            QI qiC = App.getRepositorio().getQI(C.get_qi(0));
            int size = C.getBlinks().get(0).get_numEdges();
            boolean symmetric = qiC.allEntriesAreReal();
            primes.add(new CombinedSpace(size,1,symmetric ? 1 : 2));
        }

        int max = 10;
        ArrayList[] lists = new ArrayList[max+1];
        lists[1] = primes;
        for (int ii=2;ii<=max;ii++) {
            lists[ii] = new ArrayList<CombinedSpace>();
        }

        int count = lists[1].size();
        int maxNumGEdges=9;
        int k=2;
        boolean somethingNew = true;
        while (k<=max && somethingNew) {
            System.out.println(String.format("\nProcessing step %6d combined maps size %6d time elapsed %6.2f seg.",
                               k,
                               count,
                               (System.currentTimeMillis()-t0)/1000.0));
            somethingNew = false;
            for (CombinedSpace A: (ArrayList<CombinedSpace>) primes) {
                for (CombinedSpace B: (ArrayList<CombinedSpace>)lists[k - 1]) {
                    if (A.getNumEdges() + B.getNumEdges() > maxNumGEdges)
                        continue;
                    else lists[k].add(new CombinedSpace(A.getNumEdges()+B.getNumEdges(),
                    A.isolatedComponents()+B.isolatedComponents(),
                    A.orientations()*B.orientations()));
                }
            }
            k++;
        }

        int totalPrimes = 0;
        int totalSpaces = 0;
        int totalPrimesNoOrientation = 0;
        int totalSpacesNoOrientation = 0;
        HashMap<Integer,Integer> countPrimesNoOrientation = new HashMap<Integer,Integer>();
        HashMap<Integer,Integer> countTotalNoOrientation = new HashMap<Integer,Integer>();
        HashMap<Integer,Integer> countPrimes = new HashMap<Integer,Integer>();
        HashMap<Integer,Integer> countTotal = new HashMap<Integer,Integer>();
        for (k = 1; k <=max;k++) {
            ArrayList<CombinedSpace> listk = lists[k];
            for (CombinedSpace s: listk) {
                {
                    Integer num = countTotal.get(s.getNumEdges());
                    if (num == null)
                        num = 0;
                    countTotal.put(s.getNumEdges(), num + s.orientations());
                    totalSpaces += s.orientations();
                }
                {
                    Integer num = countTotalNoOrientation.get(s.getNumEdges());
                    if (num == null)
                        num = 0;
                    countTotalNoOrientation.put(s.getNumEdges(), num + 1);
                    totalSpacesNoOrientation += 1;
                }
                if (k == 1) {
                    Integer num = countPrimes.get(s.getNumEdges());
                    if (num == null)
                        num = 0;
                    countPrimes.put(s.getNumEdges(),num + s.orientations());
                    totalPrimes += s.orientations();
                }
                if (k == 1) {
                    Integer num = countPrimesNoOrientation.get(s.getNumEdges());
                    if (num == null)
                        num = 0;
                    countPrimesNoOrientation.put(s.getNumEdges(),num + 1);
                    totalPrimesNoOrientation += 1;
                }
            }
        }

        StringBuffer sb = new StringBuffer();
        ArrayList<Integer> listKeys = new ArrayList<Integer>(countTotal.keySet());
        Collections.sort(listKeys);
        int acumPrimes = 0, acumTotal = 0, acumPrimesNoOrientation = 0, acumTotalNoOrientation = 0;
        sb.append("Prime Status for List");
        sb.append(String.format("\n%-5s %6s %6s %6s %6s %6s %6s %6s %6s %6s %6s %6s %6s %6s %6s %6s %6s",
                                "#",
                                "Pri=",
                                "%",
                                "Pri<=",
                                "%",
                                "NOri=",
                                "%",
                                "NOri<=",
                                "%",
                                "Tot=",
                                "%",
                                "Tot<=",
                                "%",
                                "NOri=",
                                "%",
                                "NOri<=",
                                "%"));
        for (Integer key: listKeys) {
            Integer numPrimes = countPrimes.get(key);
            Integer numTotal = countTotal.get(key);
            Integer numPrimesNoOrientation = countPrimesNoOrientation.get(key);
            Integer numTotalNoOrientation = countTotalNoOrientation.get(key);
            acumPrimes += numPrimes;
            acumTotal += numTotal;
            acumPrimesNoOrientation += numPrimesNoOrientation;
            acumTotalNoOrientation += numTotalNoOrientation;


            sb.append(String.format("\n%-5d %6d %6.2f %6d %6.2f %6d %6.2f %6d %6.2f %6d %6.2f %6d %6.2f %6d %6.2f %6d %6.2f",
                    0+key,
                    numPrimes, 100.0 * (double) numPrimes / (double) totalPrimes,
                    acumPrimes, 100.0 * (double) acumPrimes / (double) totalPrimes,
                    numPrimesNoOrientation, 100.0 * (double) numPrimesNoOrientation / (double) totalPrimesNoOrientation,
                    acumPrimesNoOrientation, 100.0 * (double) acumPrimesNoOrientation / (double) totalPrimesNoOrientation,
                    numTotal, 100.0 * (double) numTotal / (double) totalSpaces,
                    acumTotal, 100.0 * (double) acumTotal / (double) totalSpaces,
                    numTotalNoOrientation, 100.0 * (double) numTotalNoOrientation / (double) totalSpacesNoOrientation,
                    acumTotalNoOrientation, 100.0 * (double) acumTotalNoOrientation / (double) totalSpacesNoOrientation
                      ));
        }
        sb.append(String.format("\nTime: %6.2f seg.",(System.currentTimeMillis()-t0)/1000.0));

        return sb.toString();

    }

    static class CombinedSpace {
        private int _numEdges;
        private int _orientations;
        private int _isolatedComponents;
        public CombinedSpace(int numEdges, int isolatedComponents, int orientations) {
            _numEdges = numEdges;
            _isolatedComponents = isolatedComponents;
            _orientations = orientations;
        }
        public int getNumEdges() {
            return _numEdges;
        }
        public int orientations() {
            return _orientations;
        }
        public int isolatedComponents() {
            return _isolatedComponents;
        }
    }
}







class FunctionImportAttractors extends Function {
    public FunctionImportAttractors() {
        super("importAttractors","Import Attractor from File");
    }
    public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
        try {
            Object result = hardwork(params, localData);
            return result;
        } catch (EvaluationException ex) {
            ex.printStackTrace();
            throw ex;
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EvaluationException(e.getMessage());
        }
    }

    /**
     * @todo Fazer isso
     * @param params ArrayList
     * @param localData DataMap
     * @return Object
     * @throws EvaluationException
     * @throws Exception
     */
    private Object hardwork(ArrayList<Object> params, DataMap localData) throws EvaluationException, Exception {
        StringBuffer sb = new StringBuffer();

        ArrayList<BlinkEntry> list = App.getRepositorio().getBlinks(0,100);

        GemRepository gemRep = new GemRepository(App.getRepositorio().getGems());

        BufferedReader br = new BufferedReader(new FileReader((String)params.get(0)));
        String s;
        int line = 1;
        while ((s = br.readLine()) != null) {
            System.out.println("Processing line "+(line));
            StringTokenizer st = new StringTokenizer(s,"\t");

            if (!st.hasMoreTokens())
                continue;

            GBlink G = new GBlink(st.nextToken());
            boolean isTsRepresentant = Integer.parseInt(st.nextToken()) == 1;
            int tsClassSize = Integer.parseInt(st.nextToken());
            Path path = new Path(st.nextToken());
            int handleNumber = 0;
            Gem gem = new Gem(new GemPackedLabelling(st.nextToken(),handleNumber));

            System.out.println("preparing to test");

            Gem gemToTest = G.getGem();
            gemToTest.goToCodeLabel();
            Gem gemResult = path.getResultWhenAppliedTo(gemToTest);

            boolean check = gemResult.getCurrentLabelling().getLettersString("").equals(gem.getCurrentLabelling().getLettersString(""));

            if (check) {

                System.out.println("Check");

                for (BlinkEntry be : list) {

                    // is the correct blink
                    if (!G.equals(be.getBlink()))
                        continue;

                    // already has a gem
                    if (be.get_gem() != 0) {
                        String message = String.format("gblink %6d already has gem %6d\n",be.get_id(),be.get_gem());
                        sb.append(message);
                        System.out.println(""+message);
                        break;
                    }

                    // get entry
                    GemEntry gEntry = gemRep.getExistingGemEntryOrCreateNew(
                            gemResult.getCurrentLabelling(), tsClassSize, isTsRepresentant);

                    // get list of not persistent Gems
                    ArrayList<GemEntry> listNewGems = gemRep.getNewEntriesLists();
                    App.getRepositorio().insertGems(listNewGems);
                    gemRep.clearNewEntriesList(); // new entries have been updated

                    // update be
                    be.set_path(path);
                    be.set_gem(gEntry.getId());

                    // update gems on blinks
                    ArrayList<BlinkEntry> listOfOne = new ArrayList<BlinkEntry>();
                    listOfOne.add(be);
                    App.getRepositorio().updateBlinksGems(listOfOne);

                    String message = String.format("gblink %6d gem was updated to %6d (tsClassSize: %5d) with path %s",be.get_id(),gEntry.getId(),tsClassSize,path.getSignature());
                    System.out.println(message);
                    sb.append(message+"\n");

                    break;
                }
            }
            else {
                String message = String.format("gblink simplification parh on line %6d not verified",line);
                sb.append(message+"\n");
                System.out.println(""+message);
            }

            // next line
            line++;
        }
        System.out.println(""+sb.toString());
        return sb.toString();
    }
}

class FunctionTestPaths extends Function {
    public FunctionTestPaths() {
        super("testPaths","Test paths from g-blink to its attractor gem");
    }
    public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
        try {
            Object result = hardwork(params, localData);
            return result;
        } catch (EvaluationException ex) {
            ex.printStackTrace();
            throw ex;
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EvaluationException(e.getMessage());
        }
    }

    /**
     * @todo Fazer isso
     * @param params ArrayList
     * @param localData DataMap
     * @return Object
     * @throws EvaluationException
     * @throws Exception
     */
    private Object hardwork(ArrayList<Object> params, DataMap localData) throws EvaluationException, Exception {
        StringBuffer sb = new StringBuffer();

        int countBlinksTested = 0;
        int countOk = 0;

        ArrayList<BlinkEntry> list = App.getRepositorio().getBlinks(0,100);
        for (BlinkEntry be : list) {

            if (be.get_gem() == 0)
                continue;

            be.loadPath();

            Path path = be.getPath();
            Gem gem = be.getBlink().getGem();
            gem.goToCodeLabel();

            Gem gemResult = path.getResultWhenAppliedTo(gem);
            Gem gemDb = App.getRepositorio().getGemById(be.get_gem()).getGem();

            if (gemResult.equals(gemDb)) {
                countOk++;
                String message = String.format("Test on gblink %6d ok",be.get_id());
                System.out.println(message);
                sb.append(message+"\n");
            }
            else {
                String message = String.format("Test on gblink %6d failed", be.get_id());
                System.out.println(message);
                sb.append(message + "\n");
            }
            countBlinksTested++;
        }
        String message = String.format("Test success %6.2f %%  %6d of %6d",100.0*(double)countOk/countBlinksTested,countOk,countBlinksTested);
        sb.append(message+"\n");
        return sb.toString();
    }
}


class FunctionTestPrimality extends Function {
    public FunctionTestPrimality() {
        super("testPrimality","Prime Spaces");
    }
    public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
        try {
            Object result = hardwork(params, localData);
            return result;
        } catch (EvaluationException ex) {
            ex.printStackTrace();
            throw ex;
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EvaluationException(e.getMessage());
        }
    }

    /**
     * @todo Fazer isso
     * @param params ArrayList
     * @param localData DataMap
     * @return Object
     * @throws EvaluationException
     * @throws Exception
     */
    private Object hardwork(ArrayList<Object> params, DataMap localData) throws EvaluationException, Exception {
        long t0 = System.currentTimeMillis();
        StringBuffer sb = new StringBuffer();

        ArrayList<ClassHGNormQI> list = App.getRepositorio().getHGNormQIClasses(App.MAX_EDGES);

        int spacesWithGem = 0;
        int spacesPrimeBiased = 0;
        for (ClassHGNormQI C: list) {
            C.load();
            BlinkEntry be = C.getBlinks().get(0);
            if (be.get_gem() == 0)
                continue;

            spacesWithGem++;
            Gem gem = App.getRepositorio().getGemById(be.get_gem()).getGem();

            GemPrimeTest test = new GemPrimeTest();
            GemPrimeStatus status = test.test(gem);

            if (status.isPrimeBiased()) {
                spacesPrimeBiased++;
            }
            else {
                String s =
                        String.format("Composite Class HG: %20s   QI: %8d  with gblink %s",
                                      C.get_hg(),
                                      C.get_qi(0),
                                      be.getBlink().getBlinkWord().toString());
                sb.append(s+"\n");
                System.out.println(""+s);
            }
        }

        String message = String.format("Spaces with gem %6d of %6d (%6.2f%%)  positive prime test %6d (%6.2f%%)      time: %6.2f seg.\n",
                                       spacesWithGem, list.size(), 100.0* (double)spacesWithGem/list.size(),
                                       spacesPrimeBiased, 100.0 *(double)spacesPrimeBiased/spacesWithGem,
                                       (System.currentTimeMillis() - t0)/1000.0);
        sb.append(message+"\n");
        System.out.println(message);
        return sb.toString();
    }
}

class FunctionDeleteSpaces extends Function {
    public FunctionDeleteSpaces() {
        super("deleteSpaces","Delete spaces");
    }
    public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
        try {
            Object result = hardwork(params, localData);
            return result;
        } catch (EvaluationException ex) {
            ex.printStackTrace();
            throw ex;
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EvaluationException(e.getMessage());
        }
    }

    /**
     * @todo Fazer isso
     * @param params ArrayList
     * @param localData DataMap
     * @return Object
     * @throws EvaluationException
     * @throws Exception
     */
    private Object hardwork(ArrayList<Object> params, DataMap localData) throws EvaluationException, Exception {
        long t0 = System.currentTimeMillis();
        StringBuffer sb = new StringBuffer();

        int prime = 1;
        int composite = 2;
        try { prime = ((Number) localData.getData("prime")).intValue(); } catch (Exception e) {}
        try { composite = ((Number) localData.getData("composite")).intValue(); } catch (Exception e) {}

        App.getRepositorio().deleteClass(prime);
        App.getRepositorio().deleteClass(composite);
        return "Prime class "+prime+" and composite class "+composite+" deleted";
    }

}

class FunctionSaveSpaces extends Function {
    public FunctionSaveSpaces() {
        super("saveSpaces","Save Prime and Composite Spaces");
    }
    public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
        try {
            Object result = hardwork(params, localData);
            return result;
        } catch (EvaluationException ex) {
            ex.printStackTrace();
            throw ex;
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EvaluationException(e.getMessage());
        }
    }

    /**
     * @todo Fazer isso
     * @param params ArrayList
     * @param localData DataMap
     * @return Object
     * @throws EvaluationException
     * @throws Exception
     */
    private Object hardwork(ArrayList<Object> params, DataMap localData) throws EvaluationException, Exception {
        long t0 = System.currentTimeMillis();
        StringBuffer sb = new StringBuffer();

        boolean useGem = true;
        try { useGem = ((Number) localData.getData("gem")).intValue() == 1 ; } catch (Exception e) {}


        int prime = 1;
        int composite = 2;
        try { prime = ((Number) localData.getData("prime")).intValue(); } catch (Exception e) {}
        try { composite = ((Number) localData.getData("composite")).intValue(); } catch (Exception e) {}

        ArrayList<ClassHGNormQI> list = App.getRepositorio().getHGNormQIClasses(App.MAX_EDGES);

        HashMap<GBlink,ClassHGNormQI> mapPrime = new HashMap<GBlink,ClassHGNormQI>();
        HashMap<GBlink,ClassHGNormQI> mapComposite = new HashMap<GBlink,ClassHGNormQI>();
        HashMap<GBlink,String> mapStatus = new HashMap<GBlink,String>();

        int spacesWithGem = 0;
        int spacesPrimeBiased = 0;
        int i=0;
        for (ClassHGNormQI C: list) {
            System.out.println(
                    String.format("processing class HG x NormQI   -   %6d of %6d  %6.2f%%",
                                  i+1,list.size(),100.0*(double)i/list.size())); i++;
            C.load();
            BlinkEntry be = C.getBlinks().get(0);

            if (useGem) {
                if (be.get_gem() == 0)
                    continue;

                spacesWithGem++;
                Gem gem = App.getRepositorio().getGemById(be.get_gem()).getGem();

                GemPrimeTest test = new GemPrimeTest();
                GemPrimeStatus status = test.test(gem);
                mapStatus.put(be.getBlink(), status.getSmallDescription());

                if (status.isPrimeBiased()) {
                    mapPrime.put(be.getBlink(), C);
                    spacesPrimeBiased++;
                } else {
                    mapComposite.put(be.getBlink(), C);
                }
            }
            else {
                mapPrime.put(be.getBlink(), C);
            }
        }

        //
        ArrayList<ClassEntry> classesToAdd = new ArrayList<ClassEntry> ();

        { // add prime classes
            ArrayList<GBlink> primeGBlinks = new ArrayList<GBlink>(mapPrime.keySet());
            Collections.sort(primeGBlinks);
            int numEdges = -1;
            int order = -1;
            for (GBlink G : primeGBlinks) {
                ClassHGNormQI C = mapPrime.get(G);
                if (G.getNumberOfGEdges() != numEdges) {
                    numEdges = G.getNumberOfGEdges();
                    order = 1;
                } else {
                    order++;
                }

                long qiId = C.get_qi(0);
                int maxqi = 0;
                String qiStatus = "";
                if (qiId != 0) {
                    QI qi = App.getRepositorio().getQI(qiId);
                    maxqi = qi.get_rmax();
                    qiStatus = (qi.allEntriesAreInteger() ? "integer" :
                                (qi.allEntriesAreReal() ? "real" : "complex"));
                }

                ClassEntry ce = new ClassEntry(
                        prime,
                        numEdges,
                        order,
                        C.get_numElements(),
                        C.get_hg(),
                        C.getStringOfQIs(),
                        C.getBlinks().get(0).get_gem(),
                        ""+mapStatus.get(G),
                        ""+qiStatus,
                        maxqi);
                classesToAdd.add(ce);
                App.getRepositorio().addBlinksToClass(ce,C.getBlinks());

                String s =
                        String.format("Added Prime Class %10s with %6d blinks",
                                      "" + numEdges + "." + order,
                                      C.get_numElements());
                sb.append(s + "\n");
                System.out.println("" + s);
            }
        }

        { // add composite classes
            ArrayList<GBlink> compositeGBlinks = new ArrayList<GBlink>(mapComposite.keySet());
            Collections.sort(compositeGBlinks);
            int numEdges = -1;
            int order = -1;
            for (GBlink G : compositeGBlinks) {
                ClassHGNormQI C = mapComposite.get(G);
                if (G.getNumberOfGEdges() != numEdges) {
                    numEdges = G.getNumberOfGEdges();
                    order = 1;
                } else {
                    order++;
                }


                long qiId = C.get_qi(0);
                int maxqi = 0;
                String qiStatus = "";
                if (qiId != 0) {
                    QI qi = App.getRepositorio().getQI(qiId);
                    maxqi = qi.get_rmax();
                    qiStatus = (qi.allEntriesAreInteger() ? "integer" :
                                (qi.allEntriesAreReal() ? "real" : "complex"));
                }

                ClassEntry ce = new ClassEntry(
                        composite,
                        numEdges,
                        order,
                        C.get_numElements(),
                        C.get_hg(),
                        C.getStringOfQIs(),
                        C.getBlinks().get(0).get_gem(),
                        ""+mapStatus.get(G),
                        ""+qiStatus,
                        maxqi);

                classesToAdd.add(ce);
                App.getRepositorio().addBlinksToClass(ce,C.getBlinks());

                String s =
                        String.format("Added Composite Class %10s with %6d blinks",
                                      "" + numEdges + "." + order,
                                      C.get_numElements());
                sb.append(s + "\n");
                System.out.println("" + s);
            }
        }

        System.out.println("Adding to database...");
        App.getRepositorio().addClasses(classesToAdd);

        String message = String.format("Created %6d prime classes and %6d composite classes.    time: %6.2f seg.\n",
                                       spacesPrimeBiased,
                                       spacesWithGem - spacesPrimeBiased,
                                       (System.currentTimeMillis() - t0)/1000.0);
        sb.append(message+"\n");
        System.out.println(message);

        return sb.toString();
    }
}

class FunctionLength extends Function {
    public FunctionLength() {
        super("len","Lenght of a list");
    }
    public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
        try {
            Object result = hardwork(params, localData);
            return result;
        } catch (EvaluationException ex) {
            ex.printStackTrace();
            throw ex;
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EvaluationException(e.getMessage());
        }
    }

    private Object hardwork(ArrayList<Object> params, DataMap localData) throws EvaluationException, Exception {
        return ((List)params.get(0)).size();
    }
}

class FunctionConcatenate extends Function {
    public FunctionConcatenate() {
        super("c","Concatenate lists");
    }
    public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
        try {
            Object result = hardwork(params, localData);
            return result;
        } catch (EvaluationException ex) {
            ex.printStackTrace();
            throw ex;
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EvaluationException(e.getMessage());
        }
    }

    private Object hardwork(ArrayList<Object> params, DataMap localData) throws EvaluationException, Exception {
        ArrayList result = new ArrayList();
        for (Object o: params) {
            if (o instanceof List) {
                result.addAll((List) o);
            }
            else result.add(o);
        }
        return result;
    }
}

class FunctionSpace extends Function {
    public FunctionSpace() {
        super("space","Get space");
    }
    public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
        try {
            Object result = hardwork(params, localData);
            return result;
        } catch (EvaluationException ex) {
            ex.printStackTrace();
            throw ex;
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EvaluationException(e.getMessage());
        }
    }

    private Object hardwork(ArrayList<Object> params, DataMap localData) throws EvaluationException, Exception {
        int id = 1;
        try { id = ((Number)localData.getData("id")).intValue(); } catch(Exception e) {}
        int e = -1;
        try { e = ((Number)localData.getData("e")).intValue(); } catch(Exception x) {}

        String withQI = null;
        try { withQI = localData.getData("withQI").toString(); } catch(Exception x) {}

        if (withQI != null) {
            ArrayList<ClassEntry> classes = App.getRepositorio().getClasses(id);
            for (int i = classes.size() - 1; i >= 0; i--) {
                String qiClass = classes.get(i).get_qi();
                //System.out.println("Compare "+qiClass+" with "+withQI);
                if (qiClass.indexOf(withQI) == -1)
                    classes.remove(i);
            }
            return classes;
        }


        if (params.size() == 0) {

            int qi = 7; // 1 is integer, 2 is real, 4 is complex
            try { qi = ((Number)localData.getData("qi")).intValue(); } catch (Exception x) {}

            ArrayList<ClassEntry> classes = App.getRepositorio().getClasses(id);

            for (int i = classes.size() - 1; i >= 0; i--) {
                String qist = classes.get(i).get_qiStatus();
                int x = ("integer".equals(qist) ? 1 :
                         "real".equals(qist) ? 2 : 4);
                if ((x & qi) == 0 || (e != -1 && classes.get(i).getNumEdges() != e))
                    classes.remove(i);
            }
            return classes;
        }
        else {
            int numEdges = ((Number)params.get(0)).intValue();
            int numOrder = ((Number)params.get(1)).intValue();
            ClassEntry c = App.getRepositorio().getClass(id,numEdges,numOrder);
            return c;
        }
    }
}

class FunctionDrawGBlinks extends Function {
    public FunctionDrawGBlinks() {
        super("d","Draw one or various gblinks");
    }

    public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
       try {
           Object result = hardwork(params, localData);
           return result;
       } catch (EvaluationException ex) {
           ex.printStackTrace();
           throw ex;
       }
       catch (Exception e) {
           e.printStackTrace();
           throw new EvaluationException(e.getMessage());
       }
   }

   private Object hardwork(ArrayList<Object> params, DataMap localData) throws EvaluationException, Exception {

       String eps = null;
       try { eps = ((String)localData.getData("eps")).toString(); } catch (Exception e) {}

       String title = null;
       try { title = ((String)localData.getData("title")).toString(); } catch (Exception e) {}

       // App.getProperty("")
       ArrayList<GBlink> list = new ArrayList<GBlink>();
       if (params.get(0) instanceof List) {
           for (Object o : (List) params.get(0))
               list.add((GBlink) o);
       }
       else if (params.get(0) instanceof ClassEntry) {
           ClassEntry C = (ClassEntry) params.get(0);
           for (BlinkEntry be : App.getRepositorio().getBlinksByClass(C))
               list.add(be.getBlink());
       }
       else {
           list.add((GBlink) params.get(0));
       }

       int n = list.size();
       int k = (int) Math.ceil(Math.sqrt((double) list.size()));
       int missing = k*k-n;
       int cols = k;
       int rows = k - (int)Math.floor((double)missing/k);
       try { rows = ((Number)localData.getData("rows")).intValue(); } catch (Exception e) {}
       try { cols = ((Number)localData.getData("cols")).intValue(); } catch (Exception e) {}
       if (eps == null) {
           if (title == null)
               title = "G-Blink Drawing: " + CommandLineInterface.getInstance()._currentCommand.replace('\n', ' ');
           JFrame f = new JFrame(title);
           linsoft.gui.util.Library.resizeAndCenterWindow(f, 500, (int)(500*((double)rows/cols)));
           f.setContentPane(new PanelTutteDrawBlinks(list, rows, cols));
           f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
           f.setVisible(true);
       }
       else {
           double CONVERSION = 72.0/25.4;

           double width = 100 * CONVERSION; // 2.5 cm per cell
           double height = 100 * (double)rows/cols * CONVERSION; // 2.5 cm per cell

           PrintWriter pw = new PrintWriter(new FileWriter(eps));
           EPSLibrary.printHeader(pw,width,height);

           double cellWidth = (double) width / cols;
           double cellHeight = (double) height / rows;

           double margin = 2 * CONVERSION; //2mm

           int r = 0; int c = 0;
           for (GBlink G: list) {
               EPSLibrary.printBlink(pw,G,c*cellWidth,r*cellHeight,cellWidth,cellHeight,margin);
               c++;
               if (c == cols) {
                   r++;
                   c=0;
               }
           }
           EPSLibrary.printFooter(pw);
           pw.close();
           return "File "+eps+" saved!";
       }
       return null;
   }
}

class FunctionDetail extends Function {
    public FunctionDetail() {
        super("detail","Detail of object");
    }

    public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
       try {
           Object result = hardwork(params, localData);
           return result;
       } catch (EvaluationException ex) {
           ex.printStackTrace();
           throw ex;
       }
       catch (Exception e) {
           e.printStackTrace();
           throw new EvaluationException(e.getMessage());
       }
   }

   private Object hardwork(ArrayList<Object> params, DataMap localData) throws EvaluationException, Exception {
        if (params.get(0) instanceof GBlink) {
            GBlink G = (GBlink) params.get(0);




            String st = String.format("g-blink:   %s\n"+
                                      "edges:     %-6d\n"+
                                      "zigzags:   %-6d\n"+
                                      "vertices:  %-6d\n"+
                                      "faces:     %-6d\n"+
                                      "blocks:    %-6d\n"+
                                      "free link: %s\n"+
                                      "circ.comp: %s\n"+
                                      "adj.curls: %s\n"+
                                      "rm2:       %s\n"+
                                      "rm3:       %s\n"+
                                      "alpha1:    %s\n"+
                                      "edge part: %s\n"+
                                      "red edg.:  %-6d\n"+
                                      "green edg: %-6d\n"+
                                      "cyclic:\n%s",
                                      G.getBlinkWord(),
                                      G.getNumberOfGEdges(),
                                      G.getNumberOfGZigZags(),
                                      G.getNumberOfGVertices(),
                                      G.getNumberOfGFaces(),
                                      G.copy().breakMap().size(),
                                      ""+G.hasAGZigZagThatIsFreeOfTheOthers(),
                                      ""+G.containsAnEliminationRing(),
                                      ""+G.containsPointOfAdjacentOppositeCurls(),
                                      ""+G.containsSimplifyingReidemeisterIIPoint(),
                                      ""+G.containsReidemeisterIIIPoint(),
                                      ""+G.containsPointOfAlpha1Move(),
                                      G.getGEdgePartition().toString(),
                                      G.getNumberOfRedGedges(),
                                      G.getNumberOfGreenGedges(),
                                      G.getCyclicRepresentation().toString());

            return st;
        }
        else {
            return "";
        }
    }
}

class FunctionBlocks extends Function {
    public FunctionBlocks() {
        super("blocks","Blocks of a g-blink");
    }

    public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
       try {
           Object result = hardwork(params, localData);
           return result;
       } catch (EvaluationException ex) {
           ex.printStackTrace();
           throw ex;
       }
       catch (Exception e) {
           e.printStackTrace();
           throw new EvaluationException(e.getMessage());
       }
   }

   private Object hardwork(ArrayList<Object> params, DataMap localData) throws EvaluationException, Exception {
       return ((GBlink) params.get(0)).copy().breakMap();
    }
}

class FunctionIdentifySwaps extends Function {
    public FunctionIdentifySwaps() {
        super("identifySwaps","Identify g-blinks that 2 versions in the database");
    }

    public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
       try {
           Object result = hardwork(params, localData);
           return result;
       } catch (EvaluationException ex) {
           ex.printStackTrace();
           throw ex;
       }
       catch (Exception e) {
           e.printStackTrace();
           throw new EvaluationException(e.getMessage());
       }
   }

   private Object hardwork(ArrayList<Object> params, DataMap localData) throws EvaluationException, Exception {

       ArrayList<BlinkEntry> blinks = App.getRepositorio().getBlinks(0,100);

       ArrayList<BlinkEntry> result = new ArrayList<BlinkEntry>();

       for (int i=0;i<blinks.size();i++) {
           System.out.println("Processing "+i+"...");
           BlinkEntry bi = blinks.get(i);
           GBlink gbi = bi.getBlink();
           int n = bi.get_numEdges();
           for (int j=i+1;j<blinks.size();j++) {
               BlinkEntry bj = blinks.get(j);
               if (n != bj.get_numEdges())
                   continue;

               GBlink gbj = bj.getBlink();

               boolean sameColor = false;
               for (int k=1;k<=n;k++) {
                   if (gbi.getColor(k) == gbj.getColor(k)) {
                       sameColor = true;
                       break;
                   }
               }

               if (sameColor)
                   continue;

               if (!bi.get_mapCode().equals(bj.get_mapCode()))
                   continue;

               if (gbi.compareTo(gbj) < 0) {
                   result.add(bj);
                   System.out.println("Remove "+bj.get_id()+" because of "+bi.get_id());
               }
               else {
                   result.add(bi);
                   System.out.println("Remove "+bi.get_id()+" because of "+bj.get_id());
               }
           }
       }

       StringBuffer sb = new StringBuffer();
       boolean first = true;
       for (BlinkEntry be: result) {
           if (!first)
               sb.append(",");
           sb.append(be.get_id());
           first = false;
       }

       return sb.toString();
    }
}





class FunctionPackGBlinkIds extends Function {
    public FunctionPackGBlinkIds() {
        super("packGBlinkIds","Pack ids of gblinks on database");
    }

    public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
       try {
           Object result = hardwork(params, localData);
           return result;
       } catch (EvaluationException ex) {
           ex.printStackTrace();
           throw ex;
       }
       catch (Exception e) {
           e.printStackTrace();
           throw new EvaluationException(e.getMessage());
       }
   }

   private Object hardwork(ArrayList<Object> params, DataMap localData) throws EvaluationException, Exception {
       App.getRepositorio().insertOffsetOnBlinkIds(100000);
       ArrayList<BlinkEntry> list = App.getRepositorio().getBlinks(0,100);
       Collections.sort(list);
       App.getRepositorio().updateBlinksIDs(list);
       return "Ids of gblink on database were packed";
    }
}


class FunctionExportAttractor extends Function {

    public FunctionExportAttractor() {
        super("expatt","Export attractors");
    }

    public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
        try {
            Object result = hardwork(params, localData);
            return result;
        } catch (EvaluationException ex) {
            ex.printStackTrace();
            throw ex;
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EvaluationException(e.getMessage());
        }
    }

    private Object hardwork(ArrayList<Object> params, DataMap localData) throws EvaluationException, Exception {
        ArrayList<GBlink> list = new ArrayList<GBlink>();
        if (params.size() > 0) {
            if (params.get(0) instanceof GBlink) {
                list = new ArrayList<GBlink>();
                list.add((GBlink) params.get(0));
            } else { //if (params.get(0) instanceof ArrayList) {
                list = (ArrayList<GBlink>) params.get(0);
            }
        }
        ArrayList<BlinkEntry> listBes = App.getRepositorio().getBlinks(0,20);

        String file = null;
        try {
            file = ((String) localData.getData("file")).toString();
        } catch (Exception e) {}

        // export all attractors
        if (list.size() == 0) {
            int count = 0;
            StringBuffer sb = new StringBuffer();
            for (BlinkEntry be : listBes) {
                System.out.println("Processing "+be.get_id());
                if (be.get_gem() == 0)
                    continue;
                be.loadPath();
                Path path = be.getPath();
                GemEntry gem = App.getRepositorio().getGemById(be.get_gem());

                String solution = String.format("%s\t%d\t%d\t%s\t%s",
                                                be.getBlink().getBlinkWord().toString(),
                                                gem.isTSRepresentant() ? 1 : 0,
                                                gem.getTSClassSize(),
                                                path.getSignature(),
                                                gem.getGem().getCurrentLabelling().getLettersString(""));

                sb.append(solution + "\n");

                System.out.println("" + solution);
                count++;
            }
            if (file != null) {
                PrintWriter pw = new PrintWriter(new FileWriter(file));
                pw.println(sb.toString());
                pw.flush();
                pw.close();
                return "File "+file+" saved with "+count+" attractors";
            }
            else return sb.toString();
        }
        else {
            StringBuffer sb = new StringBuffer();
            int count = 0;
            int countSuccess = 0;
            for (GBlink G : list) {
                System.out.println(String.format("Processing %4d of %4d", ++count, list.size()));

                BlinkEntry GG = null;
                for (BlinkEntry be : listBes) {
                    if (G.equals(be.getBlink())) {
                        if (be.get_gem() != 0)
                            GG = be;
                        break;
                    }
                }

                if (GG == null) {
                    sb.append("Did not find for: " + G.getBlinkWord().toString() + "\n");
                    continue;
                }

                GG.loadPath();
                Path path = GG.getPath();
                GemEntry gem = App.getRepositorio().getGemById(GG.get_gem());

                String solution = String.format("%s\t%d\t%d\t%s\t%s",
                                                G.getBlinkWord().toString(),
                                                gem.isTSRepresentant() ? 1 : 0,
                                                gem.getTSClassSize(),
                                                path.getSignature(),
                                                gem.getGem().getCurrentLabelling().getLettersString(""));

                sb.append(solution + "\n");
                System.out.println("" + solution);
            }
            return sb.toString();
        }
    }

}



class FunctionUpdateMinGem extends Function {

    public FunctionUpdateMinGem() {
        super("updateMinGem", "Update min gem.");
    }

    public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
        try {
            Object result = hardwork(params, localData);
            return result;
        } catch (EvaluationException ex) {
            ex.printStackTrace();
            throw ex;
        } catch (Exception e) {
            e.printStackTrace();
            throw new EvaluationException(e.getMessage());
        }
    }

    private Object hardwork(ArrayList<Object> params, DataMap localData) throws EvaluationException, Exception {

        GemGraph GG = new GemGraph();
        Graph G = GG.getGraph();

        ArrayList<BlinkEntry> updateList = new ArrayList<BlinkEntry>();

        PrintWriter pw = new PrintWriter(new FileWriter("c:/workspace/blink/log/mingem.log", true));
        pw.println("Update at " + GregorianCalendar.getInstance().getTime());

        int count = 0;

        ArrayList<BlinkEntry> list = App.getRepositorio().getBlinks(0, 10);
        for (BlinkEntry be : list) {
            if (be.get_gem() == -1L || be.get_gem() == 0L)
                continue;
            long newMinGem = GG.getIdOfMinEquivalentGemByGemId(be.get_gem());
            long oldMinGem = be.getMinGem();
            if (newMinGem != oldMinGem) {
                count++;
                be.setMinGem(newMinGem);
                updateList.add(be);
                pw.println(String.format("update %6d from %6d -> %6d", be.get_id(), oldMinGem, newMinGem));
                pw.flush();
            }
        }
        pw.close();

        App.getRepositorio().updateBlinksMinGem(updateList);

        return "updated "+count+" blinks";
    }
}

class FunctionProduceEPSCatalog extends Function {

    public FunctionProduceEPSCatalog() {
        super("produceEPSCatalog", "produce EPS catalog");
    }

    public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
        try {
            Object result = hardwork(params, localData);
            return result;
        } catch (EvaluationException ex) {
            ex.printStackTrace();
            throw ex;
        } catch (Exception e) {
            e.printStackTrace();
            throw new EvaluationException(e.getMessage());
        }
    }

    private Object hardwork(ArrayList<Object> params, DataMap localData) throws EvaluationException, Exception {

        ArrayList<ClassEntry> list = new ArrayList<ClassEntry>();
        for (Object o: (List) params.get(0)) {
            list.add((ClassEntry) o);
        }

        EPSCatalog C = new EPSCatalog(list);

        return "catalog production started";
    }
}

class FunctionFourCluster extends Function {

    public FunctionFourCluster() {

        super("fourCluster", "Four cluster");

    }

    public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
        try {
            Object result = hardwork(params, localData);
            return result;
        } catch (EvaluationException ex) {
            ex.printStackTrace();
            throw ex;
        } catch (Exception e) {
            e.printStackTrace();
            throw new EvaluationException(e.getMessage());
        }
    }

    private Object hardwork(ArrayList<Object> params, DataMap localData) throws EvaluationException, Exception {
        Gem gem = (Gem) params.get(0);

        ArrayList<FourCluster> result = gem.findAllFourCluster();

        StringBuffer sb = new StringBuffer();
        boolean first = true;
        for (FourCluster f: result) {
            if (!first)
                sb.append("\n" + f.toString());
            else
                sb.append(f.toString());
            first = false;
        }
        return sb.toString();
    }
}


class FunctionGemWithoutFourCluster extends Function {

    public FunctionGemWithoutFourCluster() {

        super("gemNo4", "Gem Without 4 cluster");

    }

    public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
        try {
            Object result = hardwork(params, localData);
            return result;
        } catch (EvaluationException ex) {
            ex.printStackTrace();
            throw ex;
        } catch (Exception e) {
            e.printStackTrace();
            throw new EvaluationException(e.getMessage());
        }
    }

    private Object hardwork(ArrayList<Object> params, DataMap localData) throws EvaluationException, Exception {
        Gem gem;
        if (params.get(0) instanceof GBlink) {
            gem = ((GBlink) params.get(0)).getGem();
            gem.goToCodeLabel();
        }
        else
            gem = (Gem) params.get(0);
        return gem.getVersionWithoutFourClusters();
    }
}


class FunctionRhoPair extends Function {

    public FunctionRhoPair() {

        super("rhoPair", "Find any rho pair");

    }

    public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
        try {
            Object result = hardwork(params, localData);
            return result;
        } catch (EvaluationException ex) {
            ex.printStackTrace();
            throw ex;
        } catch (Exception e) {
            e.printStackTrace();
            throw new EvaluationException(e.getMessage());
        }
    }

    private Object hardwork(ArrayList<Object> params, DataMap localData) throws EvaluationException, Exception {
        Gem gem = (Gem) params.get(0);

        RhoPair p = gem.findAnyRho3Pair();
        if (p == null) {
            p = gem.findAnyRho2Pair();
        }

        return (p != null ? p.toString() : null);
    }
}

class FunctionAdjacentOppositeCurls extends Function {
    public FunctionAdjacentOppositeCurls() {
        super("adjacentOppositeCurls","Find adjacent opposite curls");
    }
    public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
        try {
            Object result = hardwork(params, localData);
            return result;
        } catch (EvaluationException ex) {
            throw ex;
        }
        catch (Exception e) {
            throw new EvaluationException(e.getMessage());
        }
    }

    private Object hardwork(ArrayList<Object> params, DataMap localData) throws EvaluationException, Exception {
        if (params.size() == 1) {
            GBlink G = ((GBlink) params.get(0));
            ArrayList<PointOfAdjacentOppositeCurls> list = G.findAllPointOfAdjacentOppositeCurls();
            StringBuffer sb = new StringBuffer();
            boolean first = true;
            for (PointOfAdjacentOppositeCurls p: list) {
                if (!first)
                    sb.append("\n");
                sb.append(p.basePointDescription(G));
                first = false;
            }
            return sb.toString();
        }

        else if (params.size() == 3) {
            GBlink G = ((GBlink) params.get(0)).copy();
            int vLabel = ((Number) params.get(1)).intValue();
            String vertexOrFace = (String) params.get(2);
            PointOfAdjacentOppositeCurls p = null;
            if ("vertex".equals(vertexOrFace.toLowerCase()) || "v".equals(vertexOrFace.toLowerCase())) {
                p = new PointOfAdjacentOppositeCurls(vLabel,true);
            }
            else if ("face".equals(vertexOrFace.toLowerCase()) || "f".equals(vertexOrFace.toLowerCase())) {
                p = new PointOfAdjacentOppositeCurls(vLabel,false);
            }
            G.applyEliminationOfAdjacentOppositeCurls(p);
            G.goToCodeLabelPreservingSpaceOrientation();
            return G;
        }
        else throw new EvaluationException("Not implemented");
    }
}

class FunctionAlpha1 extends Function {
    public FunctionAlpha1() {
        super("alpha1","Find alpha 1");
    }
    public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
        try {
            Object result = hardwork(params, localData);
            return result;
        } catch (EvaluationException ex) {
            throw ex;
        }
        catch (Exception e) {
            throw new EvaluationException(e.getMessage());
        }
    }

    private Object hardwork(ArrayList<Object> params, DataMap localData) throws EvaluationException, Exception {
        if (params.size() == 1) {
            GBlink G = ((GBlink) params.get(0));
            ArrayList<PointOfAlpha1Move> list = G.findAllPointOfAlpha1Move();
            StringBuffer sb = new StringBuffer();
            boolean first = true;
            for (PointOfAlpha1Move p: list) {
                if (!first)
                    sb.append("\n");
                sb.append(p.basePointDescription(G));
                first = false;
            }
            return sb.toString();
        }

        else if (params.size() == 4) {
            GBlink G = ((GBlink) params.get(0)).copy();
            int vLabel = ((Number) params.get(1)).intValue();
            String vertexOrFace = (String) params.get(2);
            int caseOfMove = ((Number) params.get(3)).intValue();
            PointOfAlpha1Move p = null;
            if ("vertex".equals(vertexOrFace.toLowerCase()) || "v".equals(vertexOrFace.toLowerCase())) {
                p = new PointOfAlpha1Move(vLabel,true,caseOfMove);
            }
            else if ("face".equals(vertexOrFace.toLowerCase()) || "f".equals(vertexOrFace.toLowerCase())) {
                p = new PointOfAlpha1Move(vLabel,false,caseOfMove);
            }

            G.applyAlpha1Move(p);
            G.goToCodeLabelPreservingSpaceOrientation();
            return G;
        }
        else throw new EvaluationException("Not implemented");
    }
}

class FunctionIdentifyAdjacentOppositeCurls extends Function {
    public FunctionIdentifyAdjacentOppositeCurls() {
        super("identifyAdjacentOppositeCurls","Identify g-blinks with adjacent opposite curls");
    }

    public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
       try {
           Object result = hardwork(params, localData);
           return result;
       } catch (EvaluationException ex) {
           ex.printStackTrace();
           throw ex;
       }
       catch (Exception e) {
           e.printStackTrace();
           throw new EvaluationException(e.getMessage());
       }
   }

   private Object hardwork(ArrayList<Object> params, DataMap localData) throws EvaluationException, Exception {

       ArrayList<BlinkEntry> blinks = App.getRepositorio().getBlinks(0,100);

       ArrayList<BlinkEntry> result = new ArrayList<BlinkEntry>();

       for (int i=0;i<blinks.size();i++) {
           System.out.println("Processing "+i+"...");
           BlinkEntry bi = blinks.get(i);
           GBlink gbi = bi.getBlink();
           if (gbi.containsPointOfAdjacentOppositeCurls()) {
               result.add(bi);
               System.out.println("Remove "+bi.get_id()+" because of adjacent opposite curl.");
           }
       }

       StringBuffer sb = new StringBuffer();
       boolean first = true;
       for (BlinkEntry be: result) {
           if (!first)
               sb.append(",");
           sb.append(be.get_id());
           first = false;
       }

       return sb.toString();
    }
}

class FunctionIdentifyBlinksThatAreNotRepresentant extends Function {
    public FunctionIdentifyBlinksThatAreNotRepresentant() {
        super("identifyBlinksThatAreNotRepresentant","Identify g-blinks with adjacent opposite curls");
    }

    public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
       try {
           Object result = hardwork(params, localData);
           return result;
       } catch (EvaluationException ex) {
           ex.printStackTrace();
           throw ex;
       }
       catch (Exception e) {
           e.printStackTrace();
           throw new EvaluationException(e.getMessage());
       }
   }

   private Object hardwork(ArrayList<Object> params, DataMap localData) throws EvaluationException, Exception {

       ArrayList<BlinkEntry> blinks = App.getRepositorio().getBlinks(0,100);

       ArrayList<BlinkEntry> result = new ArrayList<BlinkEntry>();

       int countReplacements = 0;
       int countEqualGem = 0;

       HashSet<GBlink> set = new HashSet<GBlink>();

       for (int i=0;i<blinks.size();i++) {
           System.out.println("Processing "+i+"...");
           BlinkEntry bi = blinks.get(i);
           GBlink gbi = bi.getBlink();
           GBlink gbiRep = gbi.getNewRepresentant();
           set.add(gbiRep);
           if (!gbi.equals(gbiRep)) {
               result.add(bi);
               countReplacements++;


               Gem g1 = gbi.getGem(); g1.goToCodeLabel();
               Gem g2 = gbiRep.getGem(); g2.goToCodeLabel();
               boolean c = g1.equals(g2);
               if (c)
                   countEqualGem++;

               System.out.println(""+bi.get_id()+" gems are the same: "+c);
               System.out.println(""+gbi.getBlinkWord().toString());
               System.out.println(""+gbiRep.getBlinkWord().toString());
           }
       }
       System.out.println(String.format("Equal gems %d of %d %.2f%%",countEqualGem,countReplacements,100.0 * (double)countEqualGem/countReplacements));

       StringBuffer sb = new StringBuffer();
       boolean first = true;
       for (BlinkEntry be: result) {
           if (!first)
               sb.append(",");
           sb.append(be.get_id());
           first = false;
       }

       System.out.println(String.format("New set has %d and the old had %d    %.2f%%",set.size(),blinks.size(),100.0 * (double)set.size()/blinks.size()));

       return sb.toString();
    }
}


class FunctionGenerateBlocks extends Function {
    public FunctionGenerateBlocks() {
        super("generateBlocks","Generate all 2 connected blocks");
    }

    public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
       try {
           Object result = hardwork(params, localData);
           return result;
       } catch (EvaluationException ex) {
           ex.printStackTrace();
           throw ex;
       }
       catch (Exception e) {
           e.printStackTrace();
           throw new EvaluationException(e.getMessage());
       }
   }

   private Object hardwork(ArrayList<Object> params, DataMap localData) throws EvaluationException, Exception {
       long t0 = System.currentTimeMillis();

       // file
       String file = null;
       try { file = ((String) localData.getData("file")).toString(); } catch (Exception e) {}

       int max = ((Number)params.get(0)).intValue();

       GenerateBlockMaps GBM = new GenerateBlockMaps(max,file);
       GBM.process();

       if (file == null) {
           ArrayList<MapPackedWord> maps = GBM.getBlocks();
           ArrayList<GBlink> blinks = new ArrayList<GBlink>();
           for (MapPackedWord mpw: maps) {
               blinks.add(new GBlink(mpw.toString()));
           }
           return blinks;
       }
       else {
           String message = String.format("File %s of blocks up to %d saved with success. (time: %.2f seg.)",
                                            file,
                                            max,
                                            (System.currentTimeMillis() - t0) / 1000.0);
           System.out.println(""+message);
           return message;
       }
    }
}

class FunctionCombineGBlinks extends Function {
    public FunctionCombineGBlinks() {
        super("combineGBlinks","Combine g-blinks");
    }

    public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
       try {
           Object result = hardwork(params, localData);
           return result;
       } catch (EvaluationException ex) {
           ex.printStackTrace();
           throw ex;
       }
       catch (Exception e) {
           e.printStackTrace();
           throw new EvaluationException(e.getMessage());
       }
   }

   private Object hardwork(ArrayList<Object> params, DataMap localData) throws EvaluationException, Exception {
       long t0 = System.currentTimeMillis();

       // file
       String file = null;
       try { file = ((String) localData.getData("file")).toString(); } catch (Exception e) {}

       ArrayList<GBlink> base = (ArrayList<GBlink>) params.get(0);
       int max = ((Number)params.get(1)).intValue();

       //
       CombineGBlinks C = new CombineGBlinks(base,max,file);

       if (file == null) {
           return C.getResult();
       }
       else {
           String message = String.format("File %s of up to %d combinations saved with success. (time: %.2f seg.)",
                                            file,
                                            max,
                                            (System.currentTimeMillis() - t0) / 1000.0);
           System.out.println(""+message);
           return message;
       }
    }
}

class FunctionColorGBlinks extends Function {
    public FunctionColorGBlinks() {
        super("colorGBlinks","Color g-blinks");
    }

    public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
       try {
           Object result = hardwork(params, localData);
           return result;
       } catch (EvaluationException ex) {
           ex.printStackTrace();
           throw ex;
       }
       catch (Exception e) {
           e.printStackTrace();
           throw new EvaluationException(e.getMessage());
       }
   }

   private Object hardwork(ArrayList<Object> params, DataMap localData) throws EvaluationException, Exception {
       long t0 = System.currentTimeMillis();

       // file
       String file = null;
       try { file = ((String) localData.getData("file")).toString(); } catch (Exception e) {}

       ArrayList<GBlink> base = (ArrayList<GBlink>) params.get(0);

       //
       ColorGBlinks C = new ColorGBlinks(base,file);

       if (file == null) {
           return C.getResult();
       }
       else {
           String message = String.format("File %s of colored gblinks (%d) saved with success. (time: %.2f seg.)",
                                            file,
                                            C.getResult().size(),
                                            (System.currentTimeMillis() - t0) / 1000.0);
           System.out.println(""+message);
           return message;
       }
    }
}

class FunctionFilterGBlinksUsingRM3 extends Function {
    public FunctionFilterGBlinksUsingRM3() {
        super("filterGBlinksUsingRM3","Filter g-blinks using RM3");
    }

    public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
       try {
           Object result = hardwork(params, localData);
           return result;
       } catch (EvaluationException ex) {
           ex.printStackTrace();
           throw ex;
       }
       catch (Exception e) {
           e.printStackTrace();
           throw new EvaluationException(e.getMessage());
       }
   }

   private Object hardwork(ArrayList<Object> params, DataMap localData) throws EvaluationException, Exception {
       long t0 = System.currentTimeMillis();

       // file
       String file = null;
       try { file = ((String) localData.getData("file")).toString(); } catch (Exception e) {}

       ArrayList<GBlink> base = (ArrayList<GBlink>) params.get(0);

       //
       FilterGBlinksUsingRM3 C = new FilterGBlinksUsingRM3(base,file);

       if (file == null) {
           return C.getResult();
       }
       else {
           String message = String.format("File %s of filtered gblinks (%d) saved with success. (time: %.2f seg.)",
                                            file,
                                            C.getResult().size(),
                                            (System.currentTimeMillis() - t0) / 1000.0);
           System.out.println(""+message);
           return message;
       }
    }
}


class FunctionExportGBlinks extends Function {
    public FunctionExportGBlinks() {
        super("exportGBlinks","Export array of gblinks to a file");
    }

    public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
       try {
           Object result = hardwork(params, localData);
           return result;
       } catch (EvaluationException ex) {
           ex.printStackTrace();
           throw ex;
       }
       catch (Exception e) {
           e.printStackTrace();
           throw new EvaluationException(e.getMessage());
       }
   }

   private Object hardwork(ArrayList<Object> params, DataMap localData) throws EvaluationException, Exception {
       ArrayList<GBlink> list = (ArrayList<GBlink>) params.get(0);
       String fileName = (String) params.get(1);

       // export gblinks
       PrintWriter pr = new PrintWriter(new FileWriter(fileName));
       for (GBlink G : list) {
           pr.println(G.getBlinkWord().toString());
       }
       pr.flush();
       pr.close();

       String message = String.format("File %s saved with %d gblinks",
                                      fileName,
                                      list.size());
       System.out.println("" + message);
       return message;
   }
}

class FunctionCombine2 extends Function {
    public FunctionCombine2() {
        super("combine2","Combine 2 g-blinks");
    }

    public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
       try {
           Object result = hardwork(params, localData);
           return result;
       } catch (EvaluationException ex) {
           ex.printStackTrace();
           throw ex;
       }
       catch (Exception e) {
           e.printStackTrace();
           throw new EvaluationException(e.getMessage());
       }
   }

   private Object hardwork(ArrayList<Object> params, DataMap localData) throws EvaluationException, Exception {
       long t0 = System.currentTimeMillis();


       GBlink A = (GBlink) params.get(0);
       GBlink B = (GBlink) params.get(1);

       ArrayList<GBlink> result = new ArrayList<GBlink>();

       ArrayList<GBlinkVertex> Az = A.getOneVertexForEachGZigzag();
       ArrayList<GBlinkVertex> Bz = B.getOneVertexForEachGZigzag();
       for (int i = 0; i < Az.size(); i++) {
           for (int j = 0; j < Bz.size(); j++) {
               GBlink AA = A.copy();
               GBlink BB = B.copy();
               GBlinkVertex vAA = AA.findVertex(Az.get(i).getLabel());
               GBlinkVertex vBB = BB.findVertex(Bz.get(j).getLabel());
               if (vAA.hasEvenLabel())
                   vAA = vAA.getNeighbour(GBlinkEdgeType.edge);
               if (vBB.hasEvenLabel())
                   vBB = vBB.getNeighbour(GBlinkEdgeType.edge);
               GBlink.merge(AA, vAA, BB, vBB);

               if (!result.contains(AA)) {
                   result.add(AA);
               }
           }
       }
       return result;
    }
}

class FunctionGemRCatalogNumber extends Function {
    public FunctionGemRCatalogNumber() {
        super("rcat","Ridgid Gem Catalog Number");
    }

    public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
       try {
           Object result = hardwork(params, localData);
           return result;
       } catch (EvaluationException ex) {
           ex.printStackTrace();
           throw ex;
       }
       catch (Exception e) {
           e.printStackTrace();
           throw new EvaluationException(e.getMessage());
       }
   }

   private Object hardwork(ArrayList<Object> params, DataMap localData) throws EvaluationException, Exception {
       ArrayList<Gem> gems = new ArrayList<Gem>();
       if (params.get(0) instanceof Gem) {
           gems.add((Gem) params.get(0));
       }
       else {
           for (Object o: (List) params.get(0))
               gems.add((Gem)o);
       }

       ArrayList<GemEntry> gemEntries = new ArrayList<GemEntry>();
       for (Gem g: gems) {
           long hc = g.getCurrentLabelling().getGemHashCode();
           GemEntry result = null;
           int numregs = 1000;
           long minId = 1;
           while (true) {
               System.out.println("Querying from "+minId+" next "+numregs+"...");
               ArrayList<GemEntry> gemsToCheck = App.getRepositorio().getSomeGems(minId, g.getNumVertices(), numregs);
               if (gemsToCheck.size() == 0)
                   break;
               for (GemEntry e: gemsToCheck) {
                   if (e.getId() >= minId) {
                       minId = e.getId() + 1;
                   }
                   if (hc != e.getGemHashCode() || e.getHandleNumber() != 0)
                       continue;
                   Gem otherGem = e.getGem();
                   if (g.equals(otherGem)) {
                       result = e;
                       break;
                   }
               }
           }
           gemEntries.add(result);
       }

       ArrayList<String> result = new ArrayList<String>();
       int i=0;
       for (GemEntry ge: gemEntries) {
           int handle = gems.get(i++).getHandleNumber();
           if (ge != null)
               result.add("r"+ge.getNumVertices()+"-"+ge.getCatalogNumber()+(handle > 0 ? " h"+handle : ""));
           else
               result.add("null");
       }

       if (result.size() == 0)
           return result.get(0);
       else return result;
   }
}





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
class FunctionSet extends Function {
    public FunctionSet() {
        super("set","Creates a list without duplicates from a given list");
    }

    public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
     try {
         Object result = hardwork(params, localData);
         return result;
     } catch (EvaluationException ex) {
         ex.printStackTrace();
         throw ex;
     }
     catch (Exception e) {
         e.printStackTrace();
         throw new EvaluationException(e.getMessage());
     }
 }

    private Object hardwork(ArrayList<Object> params, DataMap localData) throws EvaluationException {
        List l = (List)params.get(0);
        ArrayList result = new ArrayList();
        for (Object o: l) {
            if (!result.contains(o))
                result.add(o);
        }
        return result;
    }

}

/**
 * FunctionGist
 */
class FunctionGist extends Function {
    public FunctionGist() {
        super("gist","Gist or string presentation for a gem");
    }

    public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
        try {
            Object result = hardwork(params, localData);
            return result;
        } catch (EvaluationException ex) {
            ex.printStackTrace();
            throw ex;
        } catch (Exception e) {
            e.printStackTrace();
            throw new EvaluationException(e.getMessage());
        }
    }

    private Object hardwork(ArrayList<Object> params, DataMap localData) throws EvaluationException {
        Gem gem = (Gem) params.get(0);
        PanelString p = new PanelString(gem);
        JFrame f = new JFrame("Circle Packing Drawing: " +
                              CommandLineInterface.getInstance()._currentCommand.replace('\n', ' '));
        linsoft.gui.util.Library.resizeAndCenterWindow(f, 640, 480);
        f.setContentPane(p);
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.setVisible(true);

        // p.get

        return null;
    }

}

/**
 * FunctionGist
 */
class FunctionTryConnectingGems extends Function {
    public FunctionTryConnectingGems() {
        super("tryConnectingGems","Try connecting gems that aren't connected");
    }

    public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
        try {
            Object result = hardwork(params, localData);
            return result;
        } catch (EvaluationException ex) {
            ex.printStackTrace();
            throw ex;
        } catch (Exception e) {
            e.printStackTrace();
            throw new EvaluationException(e.getMessage());
        }
    }

    long _time;
    int _rep;
    int _u;
    private Object hardwork(ArrayList<Object> params, DataMap localData) throws EvaluationException {
        _time = ((Number)params.get(0)).longValue();
        _rep = ((Number)params.get(1)).intValue();
        _u = ((Number)params.get(2)).intValue();
        Runnable r = new Runnable() {
            public void run() {
                try {
                    while (true) {
                        new TryToConnectGemsFromTheSameHGQI(_time, _rep, _u);
                        GemGraph.updateMinGem();
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

        };

        Thread t = new Thread(r);
        t.start();

        return t;
    }
}

/**
 * FunctionGist
 */
class FunctionStopThread extends Function {
    public FunctionStopThread() {
        super("stop","Stop a thread");
    }

    public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
        try {
            Object result = hardwork(params, localData);
            return result;
        } catch (EvaluationException ex) {
            ex.printStackTrace();
            throw ex;
        } catch (Exception e) {
            e.printStackTrace();
            throw new EvaluationException(e.getMessage());
        }
    }

    private Object hardwork(ArrayList<Object> params, DataMap localData) throws EvaluationException {
        ((Thread) params.get(0)).stop();
        return null;
    }
}







class FunctionLinkGraphInGMLFormat extends Function {

    public FunctionLinkGraphInGMLFormat() {
        super("linkGraphInGMLFormat","Link graph in GML format");
    }

    public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
        try {
            Object result = hardwork(params, localData);
            return result;
        } catch (EvaluationException ex) {
            ex.printStackTrace();
            throw ex;
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EvaluationException(e.getMessage());
        }
    }

    private Object hardwork(ArrayList<Object> params, DataMap localData) throws EvaluationException, Exception {

        boolean drawLink = false;
        boolean drawGBlink = true;
        boolean drawBlink = false;

        try {
            drawLink = ((Number) localData.getData("L")).intValue() == 1;
        } catch (Exception e) {}
        try {
            drawBlink = ((Number) localData.getData("B")).intValue() == 1;
        } catch (Exception e) {}
        try {
            drawGBlink = ((Number) localData.getData("GB")).intValue() == 1;
        } catch (Exception e) {}


        GBlink G = (GBlink) params.get(0);
        StringBuffer sb = new StringBuffer();
        if (drawLink) {
            sb.append(Library.getLinkInGMLFormat(G));
        }
        if (drawBlink) {
            sb.append(Library.getBlinkInGMLFormat(G));
        }
        if (drawGBlink) {
            sb.append(Library.getGBlinkInGMLFormat(G));
        }
        return sb.toString();
    }
}


class FunctionNetSimplex extends Function {

    public FunctionNetSimplex() {
        super("netSimplex","Network Simplex");
    }

    public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
        try {
            Object result = hardwork(params, localData);
            return result;
        } catch (EvaluationException ex) {
            ex.printStackTrace();
            throw ex;
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EvaluationException(e.getMessage());
        }
    }

    private Object hardwork(ArrayList<Object> params, DataMap localData) throws EvaluationException, Exception {
        Network network = new Network((String) params.get(0));
        // final Network network = new Network("resources/xxx.net");

        // time
        long t = System.currentTimeMillis();
        network.initNetworkForAlgorithm();
        while (true) {
            try {
                // System.out.println("It:\t"+network.getIteracao()+"\tValue:\t"+network.getNetworkValue());
                System.out.println("It:\t" + network.getIteracao());
                if (network.nextIteration()) {
                    break;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                break;
            }
        }

        // time
        t = System.currentTimeMillis() - t;

        StringBuffer sb = new StringBuffer();
        sb.append(String.format("Solution:  Iter. %d  Net.Value: %d  Time: %.2f",
                                network.getIteracao(),
                                network.getNetworkValue(),
                                t / 1000.0));
        System.out.println(sb.toString());
        return sb.toString();
    }
}

class FunctionTamassia extends Function {

    public FunctionTamassia() {
        super("t","Tamassia's Algorithm");
    }

    public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
        try {
            Object result = hardwork(params, localData);
            return result;
        } catch (EvaluationException ex) {
            ex.printStackTrace();
            throw ex;
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EvaluationException(e.getMessage());
        }
    }

    private Object hardwork(ArrayList<Object> params, DataMap localData) throws EvaluationException, Exception {
        GBlink G = (GBlink) params.get(0);

        boolean link = false;
        try {
            link = ((Number) localData.getData("L")).intValue() == 1;
        } catch (Exception e) {}

        boolean blink = true;
        try {
            blink = ((Number) localData.getData("B")).intValue() == 1;
        } catch (Exception e) {}

        String st = "";

        if (link) {
            PlanarRepresentation P = Library.getLinkPlanarRepresentation(G,-1);
            st += P.getDesciption();
            new OrthogonalLayout(P);

            JFrame f = new JFrame("Tamassia's Algorithm (Link)");
            linsoft.gui.util.Library.resizeAndCenterWindow(f, 640, 480);
            f.setContentPane(new PanelPlanarRepresentation(P));
            f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            f.setVisible(true);
        }

        if (blink) {
            PlanarRepresentation P = Library.getBlinkPlanarRepresentation(G,-1);
            st += "Description Before Degree Correction\n"+P.getDesciption();
            System.out.println("Description Before Degree Correction\n"+P.getDesciption());
            P.createFacesOnVerticesWithDegreeGreaterThan4();
            st += "Description After Degree Correction\n"+P.getDesciption();
            System.out.println("Description After Degree Correction\n"+P.getDesciption());

            new OrthogonalLayout(P);

            JFrame f = new JFrame("Tamassia's Algorithm (Blink)");
            linsoft.gui.util.Library.resizeAndCenterWindow(f, 640, 480);
            f.setContentPane(new PanelPlanarRepresentation(P));
            f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            f.setVisible(true);
        }

        return st;
    }
}

class FunctionDrawLink extends Function {
    public FunctionDrawLink() {
        super("dl","Draw one or various links");
    }

    public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
       try {
           Object result = hardwork(params, localData);
           return result;
       } catch (EvaluationException ex) {
           ex.printStackTrace();
           throw ex;
       }
       catch (Exception e) {
           e.printStackTrace();
           throw new EvaluationException(e.getMessage());
       }
   }

   private Object hardwork(ArrayList<Object> params, DataMap localData) throws EvaluationException, Exception {

       String eps = null;
       try { eps = ((String)localData.getData("eps")).toString(); } catch (Exception e) {}

       String title = null;
       try { title = ((String)localData.getData("title")).toString(); } catch (Exception e) {}

       int smooth = 2;
       try {
           smooth = ((Number) localData.getData("smooth")).intValue();
       } catch (Exception e) {}

       int crossingSpace = 3;
       try {
           crossingSpace = ((Number) localData.getData("cspace")).intValue();
       } catch (Exception e) {}

       double epsWidth = 0.2;
       try { epsWidth = ((Number) localData.getData("epsw")).doubleValue(); } catch (Exception e) {}


       boolean all = false;
       try {
           all = (((Number) localData.getData("all")).intValue()) == 1;
       } catch (Exception e) {}


       // App.getProperty("")
       ArrayList<GBlink> list = new ArrayList<GBlink>();
       if (params.get(0) instanceof List) {
           for (Object o : (List) params.get(0))
               list.add((GBlink) o);
       }
       else if (params.get(0) instanceof ClassEntry) {
           ClassEntry C = (ClassEntry) params.get(0);
           for (BlinkEntry be : App.getRepositorio().getBlinksByClass(C))
               list.add(be.getBlink());
       }
       else {
           list.add((GBlink) params.get(0));
       }

       ArrayList<LinkDrawing> listDrawings = new ArrayList<LinkDrawing>();
       for (GBlink G: list) {
           if (all) {
               for (int i=0;i<G.getNumberOfGFaces();i++) {
                   LinkDrawing ld = new LinkDrawing(G, smooth, crossingSpace, i);
                   listDrawings.add(ld);
                   ld.setEPSLineWidthInMM(epsWidth);
               }
           }
           else {
               LinkDrawing ld = new LinkDrawing(G, smooth, crossingSpace, -1);
               listDrawings.add(ld);
               ld.setEPSLineWidthInMM(epsWidth);
           }
       }

       int n = listDrawings.size();
       int k = (int) Math.ceil(Math.sqrt((double) listDrawings.size()));
       int missing = k*k-n;
       int cols = k;
       int rows = k - (int)Math.floor((double)missing/k);
       try { rows = ((Number)localData.getData("rows")).intValue(); } catch (Exception e) {}
       try { cols = ((Number)localData.getData("cols")).intValue(); } catch (Exception e) {}
       if (eps == null) {
           if (title == null)
               title = "Link Drawing: " + CommandLineInterface.getInstance()._currentCommand.replace('\n', ' ');
           JFrame f = new JFrame(title);
           linsoft.gui.util.Library.resizeAndCenterWindow(f, 500, (int)(500*((double)rows/cols)));
           f.setContentPane(new PanelDrawLinks(listDrawings, rows,cols));
           f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
           f.setVisible(true);
       }
       else {
           double CONVERSION = 72.0/25.4;

           double width = 100 * CONVERSION;
           double height = 100 * (double)rows/cols * CONVERSION;

           PrintWriter pw = new PrintWriter(new FileWriter(eps));
           EPSLibrary.printHeader(pw,width,height);

           double cellWidth = (double) width / cols;
           double cellHeight = (double) height / rows;

           double margin = 0.12; //

           int r = 0; int c = 0;
           for (LinkDrawing dl: listDrawings) {
               dl.drawEPS(pw,c*cellWidth,r*cellHeight,cellWidth,cellHeight,margin);
               c++;
               if (c == cols) {
                   r++;
                   c=0;
               }
           }
           EPSLibrary.printFooter(pw);
           pw.close();
           return "File "+eps+" saved!";
       }
       return null;
   }
}

class FunctionDrawGBlink extends Function {
    public FunctionDrawGBlink() {
        super("dg","Draw one or various glinks");
    }

    public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
       try {
           Object result = hardwork(params, localData);
           return result;
       } catch (EvaluationException ex) {
           ex.printStackTrace();
           throw ex;
       }
       catch (Exception e) {
           e.printStackTrace();
           throw new EvaluationException(e.getMessage());
       }
   }

   private Object hardwork(ArrayList<Object> params, DataMap localData) throws EvaluationException, Exception {

       String eps = null;
       try { eps = ((String)localData.getData("eps")).toString(); } catch (Exception e) {}

       String title = null;
       try { title = ((String)localData.getData("title")).toString(); } catch (Exception e) {}

       int smooth = 2;
       try {
           smooth = ((Number) localData.getData("smooth")).intValue();
       } catch (Exception e) {}

       boolean all = false;
       try {
           all = (((Number) localData.getData("all")).intValue()) == 1;
       } catch (Exception e) {}

       // App.getProperty("")
       ArrayList<GBlink> list = new ArrayList<GBlink>();
       if (params.get(0) instanceof List) {
           for (Object o : (List) params.get(0))
               list.add((GBlink) o);
       }
       else if (params.get(0) instanceof ClassEntry) {
           ClassEntry C = (ClassEntry) params.get(0);
           for (BlinkEntry be : App.getRepositorio().getBlinksByClass(C))
               list.add(be.getBlink());
       }
       else {
           list.add((GBlink) params.get(0));
       }

       ArrayList<GBlinkDrawing> listDrawings = new ArrayList<GBlinkDrawing>();
       for (GBlink G: list) {
           if (all) {
               for (int i=0;i<G.getNumberOfGFaces();i++)
                   listDrawings.add(new GBlinkDrawing(G,smooth,i));
           }
           else {
               listDrawings.add(new GBlinkDrawing(G,smooth,-1));
           }
       }

       int n = listDrawings.size();
       int k = (int) Math.ceil(Math.sqrt((double) listDrawings.size()));
       int missing = k*k-n;
       int cols = k;
       int rows = k - (int)Math.floor((double)missing/k);
       try { rows = ((Number)localData.getData("rows")).intValue(); } catch (Exception e) {}
       try { cols = ((Number)localData.getData("cols")).intValue(); } catch (Exception e) {}
       if (eps == null) {
           if (title == null)
               title = "Link Drawing: " + CommandLineInterface.getInstance()._currentCommand.replace('\n', ' ');
           JFrame f = new JFrame(title);
           linsoft.gui.util.Library.resizeAndCenterWindow(f, 500, (int)(500*((double)rows/cols)));
           f.setContentPane(new PanelDrawGBlinks(listDrawings, rows, cols));
           f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
           f.setVisible(true);
       }
       else {
           double CONVERSION = 72.0/25.4;

           double width = 100 * CONVERSION;
           double height = 100 * (double)rows/cols * CONVERSION;

           PrintWriter pw = new PrintWriter(new FileWriter(eps));
           EPSLibrary.printHeader(pw,width,height);

           double cellWidth = (double) width / cols;
           double cellHeight = (double) height / rows;

           double margin = 0.12; //

           int r = 0; int c = 0;
           for (GBlinkDrawing db: listDrawings) {
               // GBlinkDrawing  = new GBlinkDrawing(G,smooth,-1);
               db.drawEPS(pw,c*cellWidth,r*cellHeight,cellWidth,cellHeight,margin);
               c++;
               if (c == cols) {
                   r++;
                   c=0;
               }
           }
           EPSLibrary.printFooter(pw);
           pw.close();
           return "File "+eps+" saved!";
       }
       return null;
   }
}

class FunctionDrawBlink extends Function {
    public FunctionDrawBlink() {
        super("db","Draw one or various blinks using Tamassia's algorithm");
    }

    public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
       try {
           Object result = hardwork(params, localData);
           return result;
       } catch (EvaluationException ex) {
           ex.printStackTrace();
           throw ex;
       }
       catch (Exception e) {
           e.printStackTrace();
           throw new EvaluationException(e.getMessage());
       }
   }

   private Object hardwork(ArrayList<Object> params, DataMap localData) throws EvaluationException, Exception {

       String eps = null;
       try { eps = ((String)localData.getData("eps")).toString(); } catch (Exception e) {}

       String title = null;
       try { title = ((String)localData.getData("title")).toString(); } catch (Exception e) {}

       int smooth = 2;
       try {
           smooth = ((Number) localData.getData("smooth")).intValue();
       } catch (Exception e) {}

       boolean all = false;
       try {
           all = (((Number) localData.getData("all")).intValue()) == 1;
       } catch (Exception e) {}

       double epsRadius = 1;
       try { epsRadius = ((Number) localData.getData("epsrad")).doubleValue(); } catch (Exception e) {}

       double epsWidth = 1;
       try { epsWidth = ((Number) localData.getData("epsw")).doubleValue(); } catch (Exception e) {}

       double pixelRadius = 3;
       try { pixelRadius = ((Number) localData.getData("pixrad")).doubleValue(); } catch (Exception e) {}

       // App.getProperty("")
       ArrayList<GBlink> list = new ArrayList<GBlink>();
       if (params.get(0) instanceof List) {
           for (Object o : (List) params.get(0))
               list.add((GBlink) o);
       }
       else if (params.get(0) instanceof ClassEntry) {
           ClassEntry C = (ClassEntry) params.get(0);
           for (BlinkEntry be : App.getRepositorio().getBlinksByClass(C))
               list.add(be.getBlink());
       }
       else {
           list.add((GBlink) params.get(0));
       }

       ArrayList<BlinkDrawing> listDrawings = new ArrayList<BlinkDrawing>();
       for (GBlink G: list) {
           if (all) {
               for (int i=0;i<G.getNumberOfGFaces();i++) {
                   BlinkDrawing bd = new BlinkDrawing(G, smooth, i);
                   bd.setPixelRadius(pixelRadius);
                   bd.setEPSRadiusInMM(epsRadius);
                   bd.setEPSLineWidthInMM(epsWidth);
                   listDrawings.add(bd);
               }
           }
           else {
               BlinkDrawing bd = new BlinkDrawing(G, smooth, -1);
               bd.setPixelRadius(pixelRadius);
               bd.setEPSRadiusInMM(epsRadius);
               bd.setEPSLineWidthInMM(epsWidth);
               listDrawings.add(bd);
           }
       }
       int n = listDrawings.size();
       int k = (int) Math.ceil(Math.sqrt((double) listDrawings.size()));
       int missing = k*k-n;
       int cols = k;
       int rows = k - (int)Math.floor((double)missing/k);
       try { rows = ((Number)localData.getData("rows")).intValue(); } catch (Exception e) {}
       try { cols = ((Number)localData.getData("cols")).intValue(); } catch (Exception e) {}
       if (eps == null) {
           if (title == null)
               title = "Blink Drawing: " + CommandLineInterface.getInstance()._currentCommand.replace('\n', ' ');
           JFrame f = new JFrame(title);
           linsoft.gui.util.Library.resizeAndCenterWindow(f, 500,(int)(500*((double)rows/cols)));
           f.setContentPane(new PanelDrawBlinks(listDrawings, rows, cols));
           f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
           f.setVisible(true);
       }
       else {
           double CONVERSION = 72.0/25.4;

           double width = 100 * CONVERSION;
           double height = 100 * (double)rows/cols * CONVERSION;

           PrintWriter pw = new PrintWriter(new FileWriter(eps));
           EPSLibrary.printHeader(pw,width,height);

           double cellWidth = (double) width / cols;
           double cellHeight = (double) height / rows;

           double margin = 0.12; // 5%

           int r = 0; int c = 0;
           for (BlinkDrawing db: listDrawings) {
               //BlinkDrawing db = new BlinkDrawing(G,smooth,-1);
               db.drawEPS(pw,c*cellWidth,r*cellHeight,cellWidth,cellHeight,margin);
               c++;
               if (c == cols) {
                   r++;
                   c=0;
               }
           }
           EPSLibrary.printFooter(pw);
           pw.close();
           return "File "+eps+" saved!";
       }
       return null;
   }
}

class FunctionTestTamassia extends Function {
    public FunctionTestTamassia() {
        super("testTamassia","Draw one or various blinks using Tamassia's algorithm");
    }

    public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
       try {
           Object result = hardwork(params, localData);
           return result;
       } catch (EvaluationException ex) {
           ex.printStackTrace();
           throw ex;
       }
       catch (Exception e) {
           e.printStackTrace();
           throw new EvaluationException(e.getMessage());
       }
   }

   private Object hardwork(ArrayList<Object> params, DataMap localData) throws EvaluationException, Exception {
       boolean link = false;
       try {
           link = ((Number) localData.getData("L")).intValue() == 1;
       } catch (Exception e) {}

       boolean blink = true;
       try {
           blink = ((Number) localData.getData("B")).intValue() == 1;
       } catch (Exception e) {}

       // App.getProperty("")
       ArrayList<GBlink> list = new ArrayList<GBlink>();
       if (params.get(0) instanceof List) {
           for (Object o : (List) params.get(0))
               list.add((GBlink) o);
       }
       else if (params.get(0) instanceof ClassEntry) {
           ClassEntry C = (ClassEntry) params.get(0);
           for (BlinkEntry be : App.getRepositorio().getBlinksByClass(C))
               list.add(be.getBlink());
       }
       else {
           list.add((GBlink) params.get(0));
       }

       StringBuffer sb = new StringBuffer();
       int index = 0;
       for (GBlink G : list) {
           System.out.println("Testing: "+index);

           if (link) {
               PlanarRepresentation P = Library.getLinkPlanarRepresentation(G,-1);
               new OrthogonalLayout(P);
               if (!P.testTransitionsOnExternalFaceAfterRectangles()) {
                   String message = String.format("Problem at link %d gblink %s", index, G.getBlinkWord().toString());
                   System.out.println(message);
                   sb.append(message + "\n");
               }
           }
           if (blink) {
               PlanarRepresentation P = Library.getBlinkPlanarRepresentation(G,-1);
               P.createFacesOnVerticesWithDegreeGreaterThan4();
               new OrthogonalLayout(P);
               if (!P.testTransitionsOnExternalFaceAfterRectangles()) {
                   String message = String.format("Problem at blink %d gblink %s", index, G.getBlinkWord().toString());
                   System.out.println(message);
                   sb.append(message + "\n");
               }
           }


           index++;
       }
       return sb.toString();
   }
}


class FunctionInducedGraph extends Function {
    public FunctionInducedGraph() {
        super("ig","Induced blink and gem graph on database by the given gblinks");
    }

    public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
       try {
           Object result = hardwork(params, localData);
           return result;
       } catch (EvaluationException ex) {
           ex.printStackTrace();
           throw ex;
       }
       catch (Exception e) {
           e.printStackTrace();
           throw new EvaluationException(e.getMessage());
       }
   }

   private Object hardwork(ArrayList<Object> params, DataMap localData) throws EvaluationException, Exception {
       boolean reset = false;
       try { reset = ((Number) localData.getData("reset")).intValue() == 1; } catch (Exception e) {}

       ArrayList list = new ArrayList();
       if (params.get(0) instanceof List) {
           List l = (List) params.get(0);
           if (l.size() > 0) {
               for (Object o : (List) params.get(0)) {
                   if (o instanceof GBlink) {
                       list.add((GBlink) o);
                   } else if (o instanceof Gem) {
                       list.add((Gem) o);
                   }
               }
           }
       }
       else if (params.get(0) instanceof ClassEntry) {
           ClassEntry C = (ClassEntry) params.get(0);
           for (BlinkEntry be : App.getRepositorio().getBlinksByClass(C))
               list.add(be.getBlink());
       }
       else if (params.get(0) instanceof GBlink) {
           list.add((GBlink) params.get(0));
       }
       else if (params.get(0) instanceof Gem) {
           list.add((Gem) params.get(0));
       }

       if (reset)
           BlinkGemGraph.reset();

       // blink and gem graph
       BlinkGemGraph G = new BlinkGemGraph(list);
       JFrame f = new JFrame("Blink and Gem Graph #Components = "+G.getNumberOfComponents());
       linsoft.gui.util.Library.resizeAndCenterWindow(f, 640, 480);
       f.setContentPane(new PanelBlinkGemGraph(G));
       f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
       f.setVisible(true);
       return null;
   }
}

class FunctionSearchPath extends Function {
    public FunctionSearchPath() {
        super("searchPath","Search for a path from the given gem to another gem and save it in the database");
    }

    public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
       try {
           Object result = hardwork(params, localData);
           return result;
       } catch (EvaluationException ex) {
           ex.printStackTrace();
           throw ex;
       }
       catch (Exception e) {
           e.printStackTrace();
           throw new EvaluationException(e.getMessage());
       }
   }

   ArrayList<Object> _params;
   DataMap _localData;
   private Object hardwork(ArrayList<Object> params, DataMap localData) throws EvaluationException {
       _params = params;
       _localData = localData;
       Runnable r = new Runnable() {
           public void run() {
               try {
                   threadHardwork(_params,_localData);
               } catch (Exception ex) {
                   ex.printStackTrace();
               }
           }

       };
       Thread t = new Thread(r);
       t.start();
       return t;
   }


   private Object threadHardwork(ArrayList<Object> params, DataMap localData) throws EvaluationException, Exception {
       System.out.println("Preparing heavy structures...");
       GemPathRepository R = new GemPathRepository();
       GemGraph gemGraph = new GemGraph();
       System.out.println("starting the search...");

       int r = 1;
       try {
           r = ((Number) localData.getData("r")).intValue();
       } catch (Exception e) {}

       int u = 1;
       try {
           u = ((Number) localData.getData("u")).intValue();
       } catch (Exception e) {}

       long t = 10000L;
       try {
           t = ((Number) localData.getData("t")).longValue();
       } catch (Exception e) {}

       long connect = -1;
       try {
           connect = ((Number) localData.getData("connect")).longValue();
       } catch (Exception e) {}

       HashSet<Long> S = new HashSet<Long>();
       for (Object o : params) {
           Number n = (Number) o;
           S.add(n.longValue());
       }

       //
       // searchPath(75610, 75611, r=3, u=1, t=5, connect=75623)
       // S = {75610, 75611}
       // testSet = S \cup {75623}
       //
       
       HashSet<Long> testSet = new HashSet<Long>();
       testSet.addAll(S);
       if (connect != -1)
           testSet.add(connect);

       

       // try 3 times to find an arrow from each starting point
       // or exit if it becomes connected
       for (int i = 0; i < r; i++) {
           System.out.println("Repetition "+(i+1));
           for (long id : S) {
               System.out.println("Trying path from "+id);
               GemEntry source = App.getRepositorio().getGemById(id);
               
               //if (source.getNumVertices() > 50)
               //    continue;

               //if (!_SGem8.contains(source.getId()))
               //    continue;


               Gem sourceGem = source.getGem();

               GemSimplificationPathFinder A = new GemSimplificationPathFinder(sourceGem, u, t, source.getTSClassSize());
               Gem targetGem = A.getBestAttractorFound();
               int tsClassSize = A.getBestAttractorTSClassSize();
               boolean tsRepresentant = A.isBestAttractorTSClassRepresentant();
               Path path = A.getBestPath();

               boolean add = false;
               if (sourceGem.compareTo(targetGem) != 0) {
                   add = R.addPathIfItDoesNotExist(sourceGem, targetGem, tsClassSize, tsRepresentant, path);
                   if (add) {
                       Toolkit.getDefaultToolkit().beep();

                       GemEntry target = R.getLastGemEntryAdded();
                       GemPathEntry gemPathAdded = R.getLastGemPathEntryAdded();
                       gemGraph.addEdge(source, target, gemPathAdded);

                       System.out.println("Added arc " + source.getId() + " -> " + target.getId());

                       // check connection
                       if (gemGraph.isConnected(testSet)) {
                           System.out.println("Solved");

                           //AudioStream as = new AudioStream(new FileInputStream("tada.wav"));
                           //AudioPlayer.player.start(as);
                           //AudioPlayer.player.stop(as);

                           Library.playSound("solucao.wav", 3000);
                           /*
                                                       Toolkit.getDefaultToolkit().beep();
                                                       Toolkit.getDefaultToolkit().beep();
                                                       Toolkit.getDefaultToolkit().beep();
                                                       Toolkit.getDefaultToolkit().beep();
                                                       Toolkit.getDefaultToolkit().beep();*/

                           return true;
                       }

                       // use on next iteration
                       //S.add(target.getId());
                   }
               }
           }
       }
       return null;
   }
}


class FunctionGemsThatShouldBeConnected extends Function {
    public FunctionGemsThatShouldBeConnected() {
        super("gemsThatShouldBeConnected","List the ids of the gems that should be connected");
    }

    public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
       try {
           Object result = hardwork(params, localData);
           return result;
       } catch (EvaluationException ex) {
           ex.printStackTrace();
           throw ex;
       }
       catch (Exception e) {
           e.printStackTrace();
           throw new EvaluationException(e.getMessage());
       }
   }

   private Object hardwork(ArrayList<Object> params, DataMap localData) throws EvaluationException, Exception {
       FindGemsThatShouldBeTheSameByHGQI F = new FindGemsThatShouldBeTheSameByHGQI();
       return F.getResult();
   }
}

class FunctionClear extends Function {
    public FunctionClear() {
        super("clear","Clear screen");
    }

    public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
       try {
           Object result = hardwork(params, localData);
           return result;
       } catch (EvaluationException ex) {
           ex.printStackTrace();
           throw ex;
       }
       catch (Exception e) {
           e.printStackTrace();
           throw new EvaluationException(e.getMessage());
       }
   }

   private Object hardwork(ArrayList<Object> params, DataMap localData) throws EvaluationException, Exception {
       CommandLineInterface.getInstance().clear();
       return "";
   }
}



class FunctionWhatSpace extends Function {
    public FunctionWhatSpace() {
        super("whatSpace","What space is this? (by hg,qi)");
    }

    public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
       try {
           Object result = hardwork(params, localData);
           return result;
       } catch (EvaluationException ex) {
           ex.printStackTrace();
           throw ex;
       }
       catch (Exception e) {
           e.printStackTrace();
           throw new EvaluationException(e.getMessage());
       }
   }

   private Object hardwork(ArrayList<Object> params, DataMap localData) throws EvaluationException, Exception {
       // App.getProperty("")
       ArrayList list = new ArrayList();
       if (params.get(0) instanceof List) {
           for (Object o : (List) params.get(0))
               list.add(o);
       }
       else if (params.get(0) instanceof ClassEntry) {
           ClassEntry C = (ClassEntry) params.get(0);
           for (BlinkEntry be : App.getRepositorio().getBlinksByClass(C))
               list.add(be.getBlink());
       }
       else {
           list.add(params.get(0));
       }

       // r
       int r = ((Integer) params.get(1)).intValue();

       // id
       int id = 1;
       try { id = ((Number)localData.getData("id")).intValue(); } catch(Exception e) {}

       ArrayList<ClassEntry> classes = App.getRepositorio().getClasses(id);

       GemGraph gemGraph = null;

       ArrayList<ClassEntry> result = new ArrayList<ClassEntry>();
       for (int i=0;i<list.size();i++) {
           Object o = list.get(i);
           if (o instanceof GBlink) {
               GBlink G = (GBlink) o;
               QI qi = G.quantumInvariant(3, r);
               HomologyGroup hg = G.homologyGroupFromGBlink();
               boolean add = false;

               for (ClassEntry ce : classes) {
                   StringTokenizer st = new StringTokenizer(ce.get_qi(), " ");
                   if (hg.toString().equals(ce.get_hg())) {
                       QI qi2 = App.getRepositorio().getQI(Long.parseLong(st.nextToken()));
                       if (qi.compareNormalizedEntriesUntilMaxR(qi2)) {
                           add = true;
                           result.add(ce);
                           break;
                       }
                   }
               }

               if (!add)
                   result.add(null);
           }
           else if (o instanceof Gem) {
               if (gemGraph == null)
                   gemGraph = new GemGraph();

               // find
               GemEntry gemsOnDatabase = null;

               Gem gem = (Gem) o;
               long hashCode = gem.getGemHashCode();
               ArrayList<GemEntry> listGems =
                       App.getRepositorio().getGemsByHashcodeAndHandleNumber(hashCode, gem.getHandleNumber());
               boolean found = false;
               for (GemEntry ge : listGems) {
                   if (gem.equals(ge.getGem()) && ge.getHandleNumber() == gem.getHandleNumber()) {
                       gemsOnDatabase = ge;
                       found = true;
                       break;
                   }
               }

               if (!found) {
                   System.out.println("WARNING: gem not found " + gem.getCurrentLabelling().getLettersString(""));
                   result.add(null);
               }
               else {
                   long representantId = gemGraph.getRepresentantGemId(gemsOnDatabase.getId());
                   System.out.println("Rep Id: "+representantId);
                   BlinkEntry be = App.getRepositorio().getAnyBlinkWithMinGem(representantId);
                   list.set(i,be.getBlink());
                   i--;
               }
           }
       }

       return result;
   }
}



class FunctionRepresentantNOGBlink extends Function {
    public FunctionRepresentantNOGBlink () {
        super("repno","Representant not preserving orientation");
    }

    public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
     try {
         Object result = hardwork(params, localData);
         return result;
     } catch (EvaluationException ex) {
         ex.printStackTrace();
         throw ex;
     }
     catch (Exception e) {
         e.printStackTrace();
         throw new EvaluationException(e.getMessage());
     }
 }

    private Object hardwork(ArrayList<Object> params, DataMap localData) throws EvaluationException {
        if (params.get(0) instanceof GBlink) {
            return ((GBlink) params.get(0)).getRepresentantNotPreservingOrientation();
        }
        else {
            ArrayList<GBlink> result = new ArrayList<GBlink>();
            ArrayList list = (ArrayList) params.get(0);
            for (Object b: list) {
                result.add(((GBlink) b).getRepresentantNotPreservingOrientation());
            }
            return result;
        }
    }

}

class FunctionID extends Function {
    public FunctionID() {
        super("id","What is the ID of these g-blinks");
    }

    public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
       try {
           Object result = hardwork(params, localData);
           return result;
       } catch (EvaluationException ex) {
           ex.printStackTrace();
           throw ex;
       }
       catch (Exception e) {
           e.printStackTrace();
           throw new EvaluationException(e.getMessage());
       }
   }

   private Object hardwork(ArrayList<Object> params, DataMap localData) throws EvaluationException, Exception {

       // App.getProperty("")
       ArrayList<GBlink> list = new ArrayList<GBlink>();
       if (params.get(0) instanceof List) {
           for (Object o : (List) params.get(0))
               list.add((GBlink) o);
       }
       else if (params.get(0) instanceof ClassEntry) {
           ClassEntry C = (ClassEntry) params.get(0);
           for (BlinkEntry be : App.getRepositorio().getBlinksByClass(C))
               list.add(be.getBlink());
       }
       else {
           list.add((GBlink) params.get(0));
       }


       ArrayList<BlinkEntry> blinks = App.getRepositorio().getBlinks(1,20);

       ArrayList<Long> result = new ArrayList<Long>();
       for (GBlink G: list) {
           boolean found = false;
           for (BlinkEntry be: blinks) {
               if (G.equals(be.getBlink())) {
                   result.add(be.get_id());
                   found = true;
                   break;
               }
           }
           if (!found)
               result.add(null);
       }

       return result;
   }
}

class FunctionBreakGemOnAnyDisconnectingQuartet extends Function {
    public FunctionBreakGemOnAnyDisconnectingQuartet() {
        super("breakGemOnAnyDisconnectingQuartet","Break gem on quartet");
    }

    public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
       try {
           Object result = hardwork(params, localData);
           return result;
       } catch (EvaluationException ex) {
           ex.printStackTrace();
           throw ex;
       }
       catch (Exception e) {
           e.printStackTrace();
           throw new EvaluationException(e.getMessage());
       }
   }

   private Object hardwork(ArrayList<Object> params, DataMap localData) throws EvaluationException, Exception {
       Gem gem = (Gem) params.get(0);
       ArrayList<Quartet> quartets = gem.findAllNonTrivialQuartets();

       if (quartets.size() == 0) {
           return "No quartets";
       }
       for (Quartet q: quartets) {
           ArrayList<HashSet<GemVertex>> result = gem.connectedComponentsAfterQuartetRemoval(q);
           if (result.size() == 1)
               continue;
           Gem[] gems = gem.breakGemOnQuartet(q,result.get(0),result.get(1));
           ArrayList<Gem> resultGems = new ArrayList<Gem>();
           for (Gem g: gems)
               resultGems.add(g);
           return resultGems;
       }
       return "No disconnecting quartets";
   }
}



class FunctionRSpace extends Function {
    public FunctionRSpace() {
        super("rspace","Representant of space");
    }

    public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
       try {
           Object result = hardwork(params, localData);
           return result;
       } catch (EvaluationException ex) {
           ex.printStackTrace();
           throw ex;
       }
       catch (Exception e) {
           e.printStackTrace();
           throw new EvaluationException(e.getMessage());
       }
   }

   private Object hardwork(ArrayList<Object> params, DataMap localData) throws EvaluationException, Exception {
       if (params.get(0) instanceof ClassEntry) {
           ArrayList<BlinkEntry> entries = App.getRepositorio().getBlinksByClass((ClassEntry) params.get(0));
           Collections.sort(entries);
           return entries.get(0).getBlink();
       }
       else if (params.get(0) instanceof List) {
           ArrayList<GBlink> result = new ArrayList<GBlink>();
           for (ClassEntry c: (List<ClassEntry>) params.get(0)) {
               ArrayList<BlinkEntry> entries = App.getRepositorio().getBlinksByClass(c);
               Collections.sort(entries);
               result.add(entries.get(0).getBlink());
           }
           return result;
       }
       throw new RuntimeException();
   }
}



class FunctionComposeQI extends Function {
    public FunctionComposeQI() {
        super("composeqi","Composte Quantum Invariant");
    }

    public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
       try {
           Object result = hardwork(params, localData);
           return result;
       } catch (EvaluationException ex) {
           ex.printStackTrace();
           throw ex;
       }
       catch (Exception e) {
           e.printStackTrace();
           throw new EvaluationException(e.getMessage());
       }
   }

   private Object hardwork(ArrayList<Object> params, DataMap localData) throws EvaluationException, Exception {
       ArrayList<GBlink> list = new ArrayList<GBlink>();
       for (GBlink g : (List<GBlink>) params.get(0)) {
           list.add(g);
       }
       int rmax = ((Integer) params.get(1));

       int n = list.size();
       if (n == 0)
           return "empty list";

       QI qis[] = new QI[n];
       for (int i = 0; i < n; i++) {
           qis[i] = list.get(i).copy().optimizedQuantumInvariant(3, rmax);
       }

       Complex values[] = new Complex[rmax - 2];
       QI qiS3 = (new GBlink(new int[] {1,2},0)).optimizedQuantumInvariant(3,rmax);

       for (int r = 3; r <= rmax; r++) {
           Complex product = Complex.valueOf(qis[0].getReal(r), qis[0].getImaginary(r));
           for (int i = 1; i < n; i++) {
               product = product.times(Complex.valueOf(qis[i].getReal(r), qis[i].getImaginary(r)));
           }
           values[r - 3] = product.divide(Complex.valueOf(qiS3.getReal(r), qiS3.getImaginary(r)).pow(n-1));
       }
       QI qiResult = new QI(QI.NOT_PERSISTENT);
       for (int r = 3; r <= rmax; r++) {
           qiResult.addEntry(r,values[r-3].getReal(),values[r-3].getImaginary());
       }
       return qiResult;
   }
}



class FunctionGenerate3Con extends Function {
    public FunctionGenerate3Con() {
        super("get3Con","Get all 3-connected blinks, giving a number of edges or a interval of edges.");
    }

    public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
       try {
           Object result = hardwork(params, localData);
           return result;
       } catch (EvaluationException ex) {
           ex.printStackTrace();
           throw ex;
       }
       catch (Exception e) {
           e.printStackTrace();
           throw new EvaluationException(e.getMessage());
       }
   }

   private Object hardwork(ArrayList<Object> params, DataMap localData) throws EvaluationException, Exception {

       int min = ((Number)params.get(0)).intValue(), max;
       if(params.get(1) != null)
    	   max = ((Number)params.get(1)).intValue();
       else
    	   max = min;
       
       if(min < 1 || max < 1 || max > min)
    	   return "Error.";

       GenerateMaps3TConnected GBM = new GenerateMaps3TConnected(min, max);
       GBM.process();

       return GBM.getResult();
    }
}



class FunctionCode extends Function {
    public FunctionCode() {
        super("code","Go to code label");
    }

    public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
       try {
           Object result = hardwork(params, localData);
           return result;
       } catch (EvaluationException ex) {
           ex.printStackTrace();
           throw ex;
       }
       catch (Exception e) {
           e.printStackTrace();
           throw new EvaluationException(e.getMessage());
       }
   }

   private Object hardwork(ArrayList<Object> params, DataMap localData) throws EvaluationException, Exception {
       // App.getProperty("")
       ArrayList<GBlink> list = new ArrayList<GBlink>();
       if (params.get(0) instanceof List) {
           for (Object o : (List) params.get(0))
               list.add((GBlink) o);
       }
       else if (params.get(0) instanceof ClassEntry) {
           ClassEntry C = (ClassEntry) params.get(0);
           for (BlinkEntry be : App.getRepositorio().getBlinksByClass(C))
               list.add(be.getBlink());
       }
       else {
           list.add((GBlink) params.get(0));
       }

       // preserve parity
       boolean pp = true;
       try { pp = ((Number) localData.getData("pp")).intValue() == 1; } catch (Exception e) {}

       ArrayList<GBlink> result = new ArrayList<GBlink>();
       for (GBlink G: list) {
           G = G.copy();
           if (!pp)
               G.goToCodeLabelAndDontCareAboutSpaceOrientation();
           else
               G.goToCodeLabelPreservingSpaceOrientation();
           result.add(G);
       }

       if (list.size() == 1)
           return result.get(0);
       else return result;
    }
}


class FunctionSpacesRepNOCardinality extends Function {
    public FunctionSpacesRepNOCardinality() {
        super("spacesRepNoCardinality","Go to code label");
    }

    public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
       try {
           Object result = hardwork(params, localData);
           return result;
       } catch (EvaluationException ex) {
           ex.printStackTrace();
           throw ex;
       }
       catch (Exception e) {
           e.printStackTrace();
           throw new EvaluationException(e.getMessage());
       }
   }

   private Object hardwork(ArrayList<Object> params, DataMap localData) throws EvaluationException, Exception {

       int id = 1;
       try { id = ((Number)localData.getData("id")).intValue(); } catch(Exception e) {}
       int e = -1;
       try { e = ((Number)localData.getData("e")).intValue(); } catch(Exception x) {}

       String withQI = null;
       try { withQI = localData.getData("withQI").toString(); } catch(Exception x) {}

       int qi = 7; // 1 is integer, 2 is real, 4 is complex
       try { qi = ((Number)localData.getData("qi")).intValue(); } catch (Exception x) {}

       ArrayList<ClassEntry> classes = App.getRepositorio().getClasses(id);

       for (int i = classes.size() - 1; i >= 0; i--) {
           String qist = classes.get(i).get_qiStatus();
           int x = ("integer".equals(qist) ? 1 :
                    "real".equals(qist) ? 2 : 4);
           if ((x & qi) == 0 || (e != -1 && classes.get(i).getNumEdges() != e))
               classes.remove(i);
       }

       HashMap<Integer,Integer> histogram = new HashMap<Integer,Integer>();
       for (ClassEntry c: classes) {
           HashSet<GBlink> S = new HashSet<GBlink>();
           ArrayList<BlinkEntry> blinks = App.getRepositorio().getBlinksByClass(c);
           for (BlinkEntry be: blinks) {
               S.add(be.getBlink().getRepresentantNotPreservingOrientation());
           }
           Integer i = histogram.get(S.size());
           if (i == null) {
               histogram.put(S.size(),1);
           }
           else {
               histogram.put(S.size(),i+1);
           }
           System.out.println(c.getNumEdges()+"."+c.getOrder()+" has "+S.size()+" distinct repNO elements");
       }

       StringBuffer sb = new StringBuffer();
       for (Integer i: histogram.keySet()) {
           int count = histogram.get(i);
           sb.append(String.format(
                   "\nSpaces with %5d elements %6d",i, count));
       }

       return sb.toString();
    }
}

class FunctionMatveevCode extends Function {

    public FunctionMatveevCode () {
        super("mc","Matveev code for gblinks");
    }

    public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
        if (params.get(0) instanceof GBlink) {
            return ((GBlink) params.get(0)).getMatveevCode();
        }
        else {
            ArrayList<String> result = new ArrayList<String>();
            ArrayList list = (ArrayList) params.get(0);
            for (Object b: list) {
                result.add(((GBlink) b).getMatveevCode());
            }
            return result;
        }
    }
}

class FunctionMatveevReport extends Function {

    public FunctionMatveevReport() {
        super("mreport","Matveev report");
    }

    public Object evaluate(ArrayList<Object> params, DataMap localData) throws EvaluationException {
        try {
            Object result = hardwork(params, localData);
            return result;
        } catch (EvaluationException ex) {
            ex.printStackTrace();
            throw ex;
        }
        catch (Exception e) {
            e.printStackTrace();
            throw new EvaluationException(e.getMessage());
        }
    }

    private Object hardwork(ArrayList<Object> params, DataMap localData) throws EvaluationException, Exception {
        ArrayList<ClassEntry> classes = App.getRepositorio().getClasses(1);
        PrintWriter pw = new PrintWriter("/tmp/x.tex");
        pw.println("\\documentclass[a4,10pt]{article}");
        pw.println("\\usepackage[width=16cm,height=20cm]{geometry}");
        pw.println("\\usepackage[dvips]{graphicx}");

        pw.println("\\begin{document}");
        
        for (ClassEntry c: classes) {
        	        	
            ArrayList<BlinkEntry> entries = new ArrayList<BlinkEntry>();
            for (BlinkEntry be: App.getRepositorio().getBlinksByClass(c)) {
                entries.add(be);
            }
            if (entries.size() > 0) {
            	BlinkEntry be = entries.get(0);
            	GBlink b = be.getBlink();
            	
            	long id = be.get_id();
            	pw.println(String.format("\\section*{%d.%d (representative: $U_{%d}$)}",c.getNumEdges(),c.getOrder(),be.get_id()));

            	pw.println("\\begin{tabular}{cc}");
            	pw.println("\\begin{minipage}{9cm}");
            	pw.println("\\begin{small}");
            	pw.println("\\begin{verbatim}");
            	pw.print(String.format("%s",b.getMatveevCode()));
            	pw.println("\\end{verbatim}");
            	pw.println("\\end{small}");
            	pw.println("\\end{minipage}");
            	pw.println("&");
            	String fileName = "/tmp/u"+id+".eps";  
            	this.drawEPS(fileName,b);            	
            	pw.println(String.format("\\raisebox{-1.25cm}[0cm][0cm]{\\includegraphics[height=2.5cm]{%s}}",fileName));
            	pw.println("\\end{tabular}");
            	
            	if (c.getNumEdges() == 9 && (c.getOrder() == 126 || c.getOrder() == 199)) {
            		for (int i=1; i<entries.size();i++) {
                    	be = entries.get(i);
                    	b = be.getBlink();
                    	
                    	id = be.get_id();
                    	pw.println("");
                    	pw.println("\\vspace{0.35cm}");
                    	pw.println(String.format("$U_{%d}$\\\\[0.2cm]",be.get_id()));

                    	pw.println("\\begin{tabular}{cc}");
                    	pw.println("\\begin{minipage}{9cm}");
                    	pw.println("\\begin{small}");
                    	pw.println("\\begin{verbatim}");
                    	pw.print(String.format("%s",b.getMatveevCode()));
                    	pw.println("\\end{verbatim}");
                    	pw.println("\\end{small}");
                    	pw.println("\\end{minipage}");
                    	pw.println("&");
                    	fileName = "/tmp/u"+id+".eps";  
                    	this.drawEPS(fileName,b);            	
                    	pw.println(String.format("\\raisebox{-1.25cm}[0cm][0cm]{\\includegraphics[height=2.5cm]{%s}}",fileName));
                    	pw.println("\\end{tabular}");            			
            		}
            		
            	}
            	
            	
            }
        }
        pw.println("\\end{document}");
        pw.close();
		return "File saved";
    }

    private void drawEPS(String eps, GBlink g) throws FileNotFoundException {
        double CONVERSION = 72.0/25.4;

        double width = 100 * CONVERSION;
        double height = 100 * CONVERSION;
        PrintWriter pw = new PrintWriter(eps);

        int	smooth = 2;
        int crossingSpace = 6;
        int epsWidth = 2;
        
        LinkDrawing ld = new LinkDrawing(g, smooth, crossingSpace, 0);
        ld.setEPSLineWidthInMM(epsWidth);

        EPSLibrary.printHeader(pw,width,height);
        ld.drawEPS(pw,0,0,width,height,0.01);
        EPSLibrary.printFooter(pw);
        
        pw.close();
    }
}
