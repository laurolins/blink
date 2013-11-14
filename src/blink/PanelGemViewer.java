package blink;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.apache.commons.collections.Predicate;

import edu.uci.ics.jung.graph.ArchetypeVertex;
import edu.uci.ics.jung.graph.Edge;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.graph.decorators.ConstantEdgeStrokeFunction;
import edu.uci.ics.jung.graph.decorators.ConstantVertexAspectRatioFunction;
import edu.uci.ics.jung.graph.decorators.ConstantVertexPaintFunction;
import edu.uci.ics.jung.graph.decorators.ConstantVertexSizeFunction;
import edu.uci.ics.jung.graph.decorators.EdgePaintFunction;
import edu.uci.ics.jung.graph.decorators.EdgeStrokeFunction;
import edu.uci.ics.jung.graph.decorators.VertexShapeFunction;
import edu.uci.ics.jung.graph.decorators.VertexStringer;
import edu.uci.ics.jung.graph.impl.SparseGraph;
import edu.uci.ics.jung.graph.impl.SparseVertex;
import edu.uci.ics.jung.graph.impl.UndirectedSparseEdge;
import edu.uci.ics.jung.utils.UserData;
import edu.uci.ics.jung.visualization.AbstractLayout;
import edu.uci.ics.jung.visualization.Coordinates;
import edu.uci.ics.jung.visualization.DefaultVisualizationModel;
import edu.uci.ics.jung.visualization.PluggableRenderer;
import edu.uci.ics.jung.visualization.VertexShapeFactory;
import edu.uci.ics.jung.visualization.VisualizationViewer;

/**
 * PanelGemViewer
 */


public class PanelGemViewer extends JPanel {
    SparseGraph _G;
    Gem _gem;
    Lens _lenses;
    
    public static PluggableRenderer bwRender = new PluggableRenderer();
    
    /** A VisualizationViewer from JUNG library that holds the drawing of a {@link Gem}. */
    VisualizationViewer _view;
    PluggableRenderer _pluggableRenderer;
    HashMap<GemVertex,Vertex> _mapGV2V;
    JCheckBox[] _cbColors = {
                            new JCheckBox("Y"),
                            new JCheckBox("B"),
                            new JCheckBox("R"),
                            new JCheckBox("G")
    };
    JComboBox _cbLayoutOffColor = new JComboBox(new Object[] {"yellow","blue","red","green"});

    public final static String KEY = "key";
    public final static String COLOR = "color";
    public static class Simple extends PanelGemViewer {
    	  public Simple (Gem gem) {
            super(gem, true,true,null,true, false);   
        }
    }
    
    public PanelGemViewer(Gem gem) {
        this(gem,true,true,null,true, true);
    }

    public PanelGemViewer(Gem gem, boolean mount3dModel) {
        this(gem,true,true,null,mount3dModel, true);
    }

    public PanelGemViewer(Gem gem, IGemVertexLabeler labeler, boolean mount3dModel) {
        this(gem,true,true,labeler,mount3dModel, true);
    }

    public PanelGemViewer(Gem gem, boolean fourFold, boolean showStrings, IGemVertexLabeler labeler, boolean mount3dModel, boolean showInfo) {
        // creating black and white renderer
    	bwRender.setVertexPaintFunction(new ConstantVertexPaintFunction(Color.black,Color.gray));
    	bwRender.setVertexStringer(new MyVertexLabeler(labeler));
    	bwRender.setVertexLabelCentering(true);
    	bwRender.setEdgePaintFunction(new GrayEdgePaint());
    	bwRender.setEdgeStrokeFunction(new GrayEdgeStroke());
    	bwRender.setEdgeIncludePredicate(new MyEdgeIncludePredicate());
        
    	// mount graph
        _G = this.mountGraph(gem);
        _gem = gem;

        // setup renderer
        PluggableRenderer pr = new PluggableRenderer();
        pr.setEdgePaintFunction(new MyEdgePaint());
        // pr.setEdgePaintFunction(new ConstantEdgePaintFunction(Color.cyan,null));
        pr.setVertexPaintFunction(new ConstantVertexPaintFunction(Color.black,Color.yellow));
        pr.setVertexStringer(new MyVertexLabeler(labeler));
        pr.setVertexLabelCentering(true);
        pr.setEdgeStrokeFunction(new ConstantEdgeStrokeFunction(2.0f));
        pr.setEdgeIncludePredicate(new MyEdgeIncludePredicate());

        if (labeler != null) {
            VertexShapeFunction vvv = new VertexShapeFunction() {
                VertexShapeFactory _vsf = new VertexShapeFactory(
                        new ConstantVertexSizeFunction(60),
                        new ConstantVertexAspectRatioFunction(0.5f));
                public Shape getShape(Vertex v) {
                    return _vsf.getRoundRectangle(v);
                }
            };
            pr.setVertexShapeFunction(vvv);
        }

        _pluggableRenderer = pr;

        // setup viewer
        //_view = new VisualizationViewer(new CircleLayout(_G), pr);
        _view = new VisualizationViewer(new MyTutteLayout(_G,GemColor.yellow), pr);
        _lenses = new Lens(_view);

        // Layout bottom panel
        ActionListener al = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _view.repaint();
            }
        };
        JPanel bottomPanel = new JPanel();
        for (JCheckBox cb: _cbColors) {
            cb.setSelected(true);
            cb.addActionListener(al);
            bottomPanel.add(cb);
        }
        bottomPanel.add(new JButton(new ActionZoom(1.20)));
        bottomPanel.add(new JButton(new ActionZoom(0.80)));
        bottomPanel.add(_cbLayoutOffColor);
        bottomPanel.add(new JButton(new ChangeTutteOffColor()));
        bottomPanel.add(new JButton(new ShowLenses()));
        bottomPanel.add(new JButton(new SearchGem(this,_gem)));
        bottomPanel.add(new JButton(new SimplifyGem(this,_gem)));
        bottomPanel.add(new JButton(new ResolveGem(this,_gem)));
        bottomPanel.add(new JButton(new SaveEPS(this,_gem)));
        bottomPanel.add(new JButton(new SavePDFContext(this, _view)));


        String code;
        try {
            code = _gem.getCurrentLabelling().getLettersString(",");
        } catch (Exception ex) {
            code = "";
        }

        // layout panel

        JPanel mainView = new JPanel();
        mainView.setLayout(new BorderLayout());
        mainView.add(new JScrollPane(_view),BorderLayout.CENTER);
        mainView.add(bottomPanel,BorderLayout.SOUTH);

        JTabbedPane tp = new JTabbedPane();
        tp.add(mainView,"Gem");
        if (showInfo) {
	        tp.add(this.getInfoPanel(),"Info");
   	     tp.add(this.getInfoPanel2(),"Diff To S3");
   	  }
   	  tp.add(this.getBigons(), "Bigons");
        /*if (showStrings) {
            PanelString ps = new PanelString(_gem); // this won't update
            tp.add(ps, "Strings " + ps.getNumberOfStrings());
        }*/
        if ("1".equals(App.getConfiguracao().getProperty("4fold")) && fourFold) {
            Gem g4 = _gem.getFourFoldGem();
            System.out.println("Agemality: "+g4.getAgemality());
            tp.add(new PanelGemViewer(_gem.getFourFoldGem(), false, true, null, false, true), "4-Fold Gem");
        }
        //if (mount3dModel) {
        //    tp.add(new PanelEGemViewer(new EGem(_gem)), "EGem");
        //}

        this.setLayout(new BorderLayout());
        this.add(new JTextField(String.format("NVert: %d  HG: %s Handle: %d Code: %s",
                                          _gem.getNumVertices(),
                                          _gem.homologyGroup().toString(),
                                          _gem.getHandleNumber(),code
                            )),BorderLayout.NORTH);
        this.add(tp,BorderLayout.CENTER);
    }

    private void appendBigons(GemColor c1, GemColor c2, StringBuffer sb) {
        ComponentRepository componentRepository = _gem.getComponentRepository();
        ArrayList<Component> bigons = componentRepository.getBigons(c1, c2);
        for (Component bigon : bigons) {
            ArrayList<GemVertex> vertexes = bigon.getVerticesFromBigon();
            for (GemVertex v : vertexes) {
                sb.append(v.getLabel() + ", ");
            }
            sb.append("\n");
        }
    }
    
    private JPanel getBigons() {
        GemColor color4 = GemColor.yellow;
        GemColor[] colors = GemColor.PERMUTATIONS[0];
        StringBuffer sb = new StringBuffer();
        
        for (GemColor c1 : colors) {
            if (c1 == color4) {
                continue;
            }
            for (GemColor c2 : colors) {
                if (c2.getNumber() <= c1.getNumber()) {
                    continue;
                }
                sb.append(c1.toString() + " " + c2.toString() + "\n");
                appendBigons(c1, c2, sb);
                sb.append("\n");
            }
        }
        
        JTextArea ta = new JTextArea();
        ta.setFont(new Font("Courier New",Font.PLAIN,14));
        ta.setText(sb.toString());
        
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(new JScrollPane(ta),BorderLayout.CENTER);
        return panel;
    }

    private JPanel getInfoPanel() {
        ArrayList<Twistor> twistors = _gem.findAllTwistors();
        ArrayList<Antipole> antipoles = _gem.findAllAntipoles();
        ArrayList<FourCluster> fourClusters = _gem.findAllFourCluster();
        ArrayList<QuadColor> quadColors = _gem.findAllQuadColors();
        ArrayList<MinusCylinder> minuscyl = _gem.findAllMinusCylinder();
        ArrayList<Quartet> quartets = _gem.findAllNonTrivialQuartets();
        RhoPair rhoPair = _gem.findAnyRho2Pair();


        StringBuffer sb = new StringBuffer();
        sb.append("Prime Status = "+(new GemPrimeTest()).test(_gem)+"\n");
        sb.append("#Twistors  = "+twistors.size()+"\n");
        sb.append("#Antipoles = "+antipoles.size()+"\n");
        sb.append("#4-Cluster = "+fourClusters.size()+"\n");
        sb.append("#QuadColors = "+quadColors.size()+"\n");
        sb.append("#MinusCylinder = "+minuscyl.size()+"\n");
        if (rhoPair != null) {
            sb.append("#rhoPair > 0 - "+rhoPair.toString()+"\n");
        }
        else {
            sb.append("#rhoPair = 0\n");
        }
        sb.append("#Quartets (non-trivial) = "+quartets.size()+"\n");
        sb.append("------\n");
        for (Twistor t: twistors) {
            sb.append(t.toString()+"\n");
        }
        sb.append("------\n");
        for (Antipole a: antipoles) {
            sb.append(a.toString()+"\n");
        }
        sb.append("------\n");
        for (FourCluster c: fourClusters) {
            sb.append(c.toString()+"\n");
        }
        sb.append("------\n");
        for (QuadColor q: quadColors) {
            sb.append(q.toString()+"\n");
        }
        sb.append("------\n");
        for (MinusCylinder m: minuscyl) {
            sb.append(m.toString()+"\n");
        }
        sb.append("------\n");
        for (Quartet q: quartets) {
            sb.append(q.toString()+"\n");
        }

        JTextArea ta = new JTextArea();
        ta.setFont(new Font("Courier New",Font.PLAIN,14));
        ta.setText(sb.toString());

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(new JScrollPane(ta),BorderLayout.CENTER);
        JPanel p = new JPanel();
        p.add(new JButton(new ApplyTwistor()));
        p.add(new JButton(new ApplyCreateDoubleDipole()));
        p.add(new JButton(new ApplyTwistorWhileExistsOne()));
        panel.add(p,BorderLayout.SOUTH);
        return panel;
    }

    class ApplyTwistor extends AbstractAction {
        public ApplyTwistor() {
            super("Apply Twistor");
        }
        public void actionPerformed(ActionEvent e) {
            String s = JOptionPane.showInputDialog("Twistor");
            StringTokenizer st = new StringTokenizer(s," ,");

            GemColor c2 = GemColor.getByNumber(Integer.parseInt(st.nextToken()));
            int v1 = Integer.parseInt(st.nextToken());
            int v2 = Integer.parseInt(st.nextToken());
            GemColor c1 = GemColor.getByNumber(Integer.parseInt(st.nextToken()));

            Gem copy = _gem.copy();
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

            if (choosed == null) {
                JOptionPane.showMessageDialog((java.awt.Component)e.getSource(),"Not a valid twistor");
                return;
            }

            copy.applyTwistor(choosed,c1);

            JFrame f = new JFrame("Application of "+choosed.toString());
            f.setSize(new Dimension(1024, 768));
            f.setContentPane(new PanelGemViewer(copy));
            f.setVisible(true);

        }
    }

    class ApplyCreateDoubleDipole extends AbstractAction {
        public ApplyCreateDoubleDipole() {
            super("Create Double Dipole");
        }
        public void actionPerformed(ActionEvent e) {
            String s = JOptionPane.showInputDialog("Double Dipole");
            StringTokenizer st = new StringTokenizer(s," ,");

            int vNumber = Integer.parseInt(st.nextToken());
            GemColor c1 = GemColor.getByNumber(Integer.parseInt(st.nextToken()));

            Gem copy = _gem.copy();

            // find vertex
            GemVertex v = copy.findVertex(vNumber);

            /*
            ArrayList<Antipole> list = copy.findAllAntipoles();
            Antipole choosed = null;
            for (Antipole a: list) {
                if ((a.getU().getLabel() == v || a.getV().getLabel() == v) && (a.getColor() == c1)) {
                    choosed = a;
                    break;
                }
            }

            if (choosed == null) {
                JOptionPane.showMessageDialog((java.awt.Component)e.getSource(),"Not a valid");
                return;
            }*/

            copy.doubleTwoDipoleCreation(v,c1);

            JFrame f = new JFrame("Application of Dipole Creation"+v.getLabel()+" "+c1);
            f.setSize(new Dimension(1024, 768));
            f.setContentPane(new PanelGemViewer(copy));
            f.setVisible(true);

        }
    }

    /**
     * Apply 3-twistor until no more 3-twistor
     */
    class ApplyTwistorWhileExistsOne extends AbstractAction {
        public ApplyTwistorWhileExistsOne() {
            super("Apply Twistor while exists 2 or 3 twistor");
        }
        public void actionPerformed(ActionEvent e) {

            Gem g = _gem.copy();
            while (true) {
                Twistor t = null;
                GemColor c1 = null;
                for (Twistor tt: g.findAllTwistors()) {
                    if (tt.getColor() == GemColor.getByNumber(2)) {
                        c1 = GemColor.getByNumber(3);
                        t = tt;
                        break;
                    }
                    else if (tt.getColor() == GemColor.getByNumber(3)) {
                        c1 = GemColor.getByNumber(2);
                        t = tt;
                        break;
                    }
                }

                if (t == null) {
                    break;
                }
                else {
                    System.out.println("Apply twistor "+t+" target color "+c1);
                    g.applyTwistor(t,c1);
                }
            }

            JFrame f = new JFrame("No 2,3-twistor");
            f.setSize(new Dimension(1024, 768));
            f.setContentPane(new PanelGemViewer(g));
            f.setVisible(true);

        }
    }



    private JPanel getInfoPanel2() {
        DifferenceToS3 diff = _gem.getDifferenceToS3();

        JTextArea ta = new JTextArea();
        ta.setFont(new Font("Courier New",Font.PLAIN,14));
        ta.setText(diff.toString());

        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(new JScrollPane(ta),BorderLayout.CENTER);
        return panel;
    }

    private void myLayout() {
        _view.setModel(
            new DefaultVisualizationModel(
                new MyTutteLayout(_G,GemColor.values()[_cbLayoutOffColor.getSelectedIndex()])));
        if (_lenses.isVisible()) {
            _lenses.setVisible(false);
            _lenses = new Lens(_view);
            _lenses.setVisible(true);
        }
        else {
            _lenses = new Lens(_view);
        }
        _view.repaint();
    }

    class ActionZoom extends AbstractAction {
        double _scale;
        public ActionZoom(double scale) {
            super(String.format("Z%.2f",scale));
            _scale = scale;
        }
        public void actionPerformed(ActionEvent e) {
            _view.getViewTransformer().scale(_scale, _scale, _view.getCenter());
            _view.repaint();
        }
    }

    class ChangeTutteOffColor extends AbstractAction {
        public ChangeTutteOffColor() {
            super("Apply");
        }
        public void actionPerformed(ActionEvent e) {
            myLayout();
        }
    }

    class ShowLenses extends AbstractAction {
        public ShowLenses() {
            super("Len.");
        }
        public void actionPerformed(ActionEvent e) {
            _lenses.setVisible(true);
        }
    }

    class SearchGem extends AbstractAction {
        private JPanel _panel;
        private Gem _gem;
        public SearchGem(JPanel p, Gem g) {
            super("Cat");
            _panel = p;
            _gem = g;
        }
        public void actionPerformed(ActionEvent e) {
            try {
                hardwork();
            } catch (SQLException ex) {
            } catch (IOException ex) {
            } catch (ClassNotFoundException ex) {
            }
        }
        private void hardwork() throws ClassNotFoundException, IOException, SQLException {

            long hc = _gem.getCurrentLabelling().getGemHashCode();

            GemEntry result = null;
            int numregs = 1000;

            long minId = 1;

            while (true) {
                System.out.println("Querying from "+minId+" next "+numregs+"...");
                ArrayList<GemEntry> gems = App.getRepositorio().getSomeGems(minId, _gem.getNumVertices(), numregs);
                if (gems.size() == 0)
                    break;
                for (GemEntry e: gems) {
                    if (e.getId() >= minId) {
                        minId = e.getId() + 1;
                    }
                    if (hc != e.getGemHashCode() || e.getHandleNumber() != 0)
                        continue;
                    Gem otherGem = e.getGem();
                    if (_gem.equals(otherGem)) {
                        result = e;
                        break;
                    }
                }
            }

            Container c = _panel.getTopLevelAncestor();

            if (result == null) {
                JOptionPane.showMessageDialog(c,"Gem not on the catalog");
            }
            else {
                JOptionPane.showMessageDialog(c,
                                              String.format(
                                                      "This gem is R %d %d   (id: %d)",
                                                      result.getNumVertices(),
                                                      result.getCatalogNumber(),
                                                      result.getId()));
            }
        }
    }

    class SimplifyGem extends AbstractAction {
        private JPanel _panel;
        private Gem _gem;
        public SimplifyGem(JPanel p, Gem g) {
            super("RG");
            _panel = p;
            _gem = g;
        }
        public void actionPerformed(ActionEvent e) {
            JDialog frame = new JDialog((JFrame)_panel.getTopLevelAncestor(),"Red.Graph",true);
            frame.setContentPane(new PanelReductionGraph(_gem));
            frame.setBounds(0,0,800,600);
            frame.setVisible(true);
        }
        private void hardwork() throws ClassNotFoundException, IOException, SQLException {

            long hc = _gem.getCurrentLabelling().getGemHashCode();

            GemEntry result = null;
            int numregs = 1000;

            long minId = 1;

            while (true) {
                System.out.println("Querying from "+minId+" next "+numregs+"...");
                ArrayList<GemEntry> gems = App.getRepositorio().getSomeGems(minId, _gem.getNumVertices(), numregs);
                if (gems.size() == 0)
                    break;
                for (GemEntry e: gems) {
                    if (e.getId() >= minId) {
                        minId = e.getId() + 1;
                    }
                    if (hc != e.getGemHashCode() || e.getHandleNumber() != 0)
                        continue;
                    Gem otherGem = e.getGem();
                    if (_gem.equals(otherGem)) {
                        result = e;
                        break;
                    }
                }
            }

            if (result == null) {
                JOptionPane.showMessageDialog((JFrame)_panel.getTopLevelAncestor(),"Gem not on the catalog");
            }
            else {
                JOptionPane.showMessageDialog((JFrame)_panel.getTopLevelAncestor(),
                                              String.format(
                                                      "This gem is R %d %d   (id: %d)",
                                                      result.getNumVertices(),
                                                      result.getCatalogNumber(),
                                                      result.getId()));
            }
        }
    }

    class ResolveGem extends AbstractAction {
        private JPanel _panel;
        private Gem _gem;
        public ResolveGem(JPanel p, Gem g) {
            super("Res");
            _panel = p;
            _gem = g;
        }
        public void actionPerformed(ActionEvent e) {
            hardwork();
        }
        private void hardwork() {
            SearchByTwistor S = new SearchByTwistor(_gem,GemColor.blue);
            Gem g1 = S.getGemWithOneBigon();
            if (g1 != null) {
                // desenhar o mapa
                JFrame f = new JFrame("JJG");
                f.setSize(new Dimension(1024,768));
                f.setContentPane(new PanelGemViewer(g1));
                f.setVisible(true);
                // desenhar o mapa
            }
            else {
                S = new SearchByTwistor(_gem,GemColor.red);
                g1 = S.getGemWithOneBigon();
                if (g1 != null) {
                    // desenhar o mapa
                    JFrame f = new JFrame("JJG");
                    f.setSize(new Dimension(1024,768));
                    f.setContentPane(new PanelGemViewer(g1));
                    f.setVisible(true);
                    // desenhar o mapa
                }
                else {
                    S = new SearchByTwistor(_gem, GemColor.green);
                    g1 = S.getGemWithOneBigon();
                    if (g1 != null) {
                        // desenhar o mapa
                        JFrame f = new JFrame("JJG");
                        f.setSize(new Dimension(1024, 768));
                        f.setContentPane(new PanelGemViewer(g1));
                        f.setVisible(true);
                        // desenhar o mapa
                    }
                    else {
                        JOptionPane.showMessageDialog(_panel,"No Resolution for this gem.");
                    }
                }
            }
        }
    }

    class SaveEPS extends AbstractAction {
        private JPanel _panel;
        private Gem _gem;
        public SaveEPS(JPanel panel, Gem g) {
            super("EPS");
            _gem = g;
            _panel = panel;
        }
        public void actionPerformed(ActionEvent e) {
            try {
                hardwork();
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            }
        }
        private void hardwork() throws FileNotFoundException {
            JFileChooser fc = new JFileChooser();
            fc.setMultiSelectionEnabled(true);
            fc.setSelectedFile(new File(App.getProperty("lastSaveGemEPS")));
            int r = fc.showOpenDialog(_panel);
            if (r == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                App.setProperty("lastSaveGemEPS", fc.getSelectedFile().getAbsolutePath());
                GemToEPS g2eps = new GemToEPS(_gem,file);
            }
        }
    }

	/**
	 * Creates a {@link SparseGraph} from a given {@link Gem}.
	 * 
	 * @param gem
	 *            - a {@link Gem} to create its {@link SparseGraph}
	 *            representation.
	 * @return A {@link SparseGraph} that describes the given {@link Gem}.
	 */
    private SparseGraph mountGraph(Gem gem) {
        // create the contracted graph with offColor c
        SparseGraph G = new SparseGraph();
        _G = G;

        // add vertices from gem
        _mapGV2V = new HashMap<GemVertex,Vertex>();
        for (GemVertex v: gem.getVertices()) {
            Vertex vv = G.addVertex(new SparseVertex());
            vv.setUserDatum(KEY,v,UserData.SHARED);
            _mapGV2V.put(v,vv);
        }

        // add edges from gem
        for (GemVertex v: gem.getVertices()) {
            Vertex vv  = _mapGV2V.get(v);

            if (v.getLabel() < v.getYellow().getLabel()) {
                Vertex vvy = _mapGV2V.get(v.getYellow());
                Edge ey = G.addEdge(new UndirectedSparseEdge(vv, vvy));
                ey.setUserDatum(COLOR, GemColor.yellow, UserData.SHARED);
                //System.out.println(String.format("%d %d %s",v.getLabel(),v.getYellow().getLabel(),GemColor.yellow));
            }

            if (v.getLabel() < v.getBlue().getLabel()) {
                Vertex vvb = _mapGV2V.get(v.getBlue());
                Edge eb = G.addEdge(new UndirectedSparseEdge(vv, vvb));
                eb.setUserDatum(COLOR, GemColor.blue, UserData.SHARED);
                //System.out.println(String.format("%d %d %s",v.getLabel(),v.getBlue().getLabel(),GemColor.blue));
            }

            if (v.getLabel() < v.getRed().getLabel()) {
                Vertex vvr = _mapGV2V.get(v.getRed());
                Edge er = G.addEdge(new UndirectedSparseEdge(vv, vvr));
                er.setUserDatum(COLOR, GemColor.red, UserData.SHARED);
                //System.out.println(String.format("%d %d %s",v.getLabel(),v.getRed().getLabel(),GemColor.red));
            }

            if (v.getLabel() < v.getGreen().getLabel()) {
                Vertex vvg = _mapGV2V.get(v.getGreen());
                Edge eg = G.addEdge(new UndirectedSparseEdge(vv, vvg));
                eg.setUserDatum(COLOR, GemColor.green, UserData.SHARED);
                //System.out.println(String.format("%d %d %s",v.getLabel(),v.getGreen().getLabel(),GemColor.green));
            }
        }

        return G;
    }

    class MyEdgeIncludePredicate implements Predicate {
        public boolean evaluate(Object o) {
            if (o instanceof Edge) {
                Edge e = (Edge) o;
                GemColor c = (GemColor) e.getUserDatum("color");
                if (c == GemColor.yellow && _cbColors[0].isSelected()) {
                    return true;
                }
                else if (c == GemColor.blue && _cbColors[1].isSelected()) {
                    return true;
                }
                else if (c == GemColor.red && _cbColors[2].isSelected()) {
                    return true;
                }
                else if (c == GemColor.green && _cbColors[3].isSelected()) {
                    return true;
                }
                else return false;
            }
            return false;
        }
    }

    class MyTutteLayout extends AbstractLayout {

        GemColor _offColor;

        public MyTutteLayout(Graph g, GemColor offColor) {
            super(g);
            _offColor = offColor;
        }

        /**
         * This one is not incremental.
         */
        public boolean isIncremental() {
            return false;
        }

        /**
         * Returns true;
         */
        public boolean incrementsAreDone() {
            return true;
        }

        /**
         * Specifies the order of vertices.  The first element of the
         * specified array will be positioned with angle 0 (on the X
         * axis), and the second one will be positioned with angle 1/n,
         * and the third one will be positioned with angle 2/n, and so on.
         * <p>
         * The default implementation shuffles elements randomly.
         */
        public void orderVertices(Vertex[] vertices) {
            List list = Arrays.asList(vertices);
            Collections.shuffle(list);
        }

        /**
         * Returns a visualization-specific key (that is, specific both
         * to this instance and <tt>AbstractLayout</tt>) that can be used
         * to access UserData related to the <tt>AbstractLayout</tt>.
         */
        public Object getKey() {
            return null;
        }

        protected void initialize_local_vertex(Vertex v) {}

        protected void initialize_local() {}

        protected void initializeLocations() {
            super.initializeLocations();

            //
            HashMap<GemVertex, Point2D.Double> map =
                    TuttesLayout.tutteLayout(_gem,
                                             _offColor,
                                             20,20,640,640);

            //
            Iterator it = getVertexIterator();
            while (it.hasNext()) {
                Vertex v = (Vertex) it.next();
                GemVertex gv = (GemVertex) v.getUserDatum("key");
                Point2D.Double p = map.get(gv);

                if (p != null) {
                    Coordinates coord = getCoordinates(v);
                    coord.setX(p.getX());
                    coord.setY(p.getY());
                }
            }
        }

        /**
         * Do nothing.
         */
        public void advancePositions() {
        }
    }
}

class MyEdgePaint implements EdgePaintFunction {
    public Paint getDrawPaint(Edge e) {
        GemColor c = (GemColor) e.getUserDatum("color");
        if (c == GemColor.yellow)
            return Color.yellow;
        else if (c == GemColor.blue)
            return Color.blue;
        else if (c == GemColor.red)
            return Color.red;
        else if (c == GemColor.green)
            return Color.green;
        else return Color.black;
    }
    public Paint getFillPaint(Edge e) {
        return null;
        /*
        GemColor c = (GemColor) e.getUserDatum("key");
        if (c == GemColor.yellow)
            return Color.yellow;
        else if (c == GemColor.blue)
            return Color.blue;
        else if (c == GemColor.red)
            return Color.red;
        else if (c == GemColor.green)
            return Color.green;
        else return Color.black;*/
    }
}

class GrayEdgePaint implements EdgePaintFunction {

	@Override
	public Paint getDrawPaint(Edge e) {
		GemColor c = (GemColor) e.getUserDatum("color");
        if (c == GemColor.yellow)
            return Color.gray;
        else if (c == GemColor.blue)
            return Color.black;
        else if (c == GemColor.red)
            return Color.gray;
        else if (c == GemColor.green)
            return Color.black;
        else return Color.blue;
	}

	@Override
	public Paint getFillPaint(Edge e) {
		return null;
	}
	
}

class GrayEdgeStroke implements EdgeStrokeFunction {
	private BasicStroke thickStroke = new BasicStroke(2.0f);
	private BasicStroke thinStroke = new BasicStroke(5.0f);
	
	@Override
	public Stroke getStroke(Edge e) {
		GemColor c = (GemColor) e.getUserDatum("color");
        if (c == GemColor.yellow)
            return thinStroke;
        else if (c == GemColor.blue)
            return thinStroke;
        else if (c == GemColor.red)
            return thickStroke;
        else if (c == GemColor.green)
            return thickStroke;
        else return thickStroke;
	}
	
}


class MyVertexLabeler implements VertexStringer {
    IGemVertexLabeler _labeler;
    public MyVertexLabeler(IGemVertexLabeler labeler) {
        _labeler = labeler;
    }
    public String getLabel(ArchetypeVertex v) {
        GemVertex vv = (GemVertex) v.getUserDatum("key");
        // return ""+vv.getOriginalLabel();
        if (_labeler == null)
            return "" + vv.getLabel();
        else
            return _labeler.getLabel(vv);
    }
}
