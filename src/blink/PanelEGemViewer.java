package blink;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.AbstractAction;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ToolTipManager;

import org.apache.commons.collections.Predicate;

import edu.uci.ics.jung.graph.ArchetypeEdge;
import edu.uci.ics.jung.graph.ArchetypeVertex;
import edu.uci.ics.jung.graph.Edge;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.graph.decorators.ConstantEdgeStrokeFunction;
import edu.uci.ics.jung.graph.decorators.ConstantVertexAspectRatioFunction;
import edu.uci.ics.jung.graph.decorators.ConstantVertexPaintFunction;
import edu.uci.ics.jung.graph.decorators.ConstantVertexSizeFunction;
import edu.uci.ics.jung.graph.decorators.DefaultToolTipFunction;
import edu.uci.ics.jung.graph.decorators.EdgePaintFunction;
import edu.uci.ics.jung.graph.decorators.EdgeStringer;
import edu.uci.ics.jung.graph.decorators.VertexShapeFunction;
import edu.uci.ics.jung.graph.decorators.VertexStringer;
import edu.uci.ics.jung.visualization.AbstractLayout;
import edu.uci.ics.jung.visualization.Coordinates;
import edu.uci.ics.jung.visualization.DefaultGraphLabelRenderer;
import edu.uci.ics.jung.visualization.DefaultVisualizationModel;
import edu.uci.ics.jung.visualization.FRLayout;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.ISOMLayout;
import edu.uci.ics.jung.visualization.Layout;
import edu.uci.ics.jung.visualization.PluggableRenderer;
import edu.uci.ics.jung.visualization.ShapePickSupport;
import edu.uci.ics.jung.visualization.SpringLayout;
import edu.uci.ics.jung.visualization.VertexShapeFactory;
import edu.uci.ics.jung.visualization.VisualizationModel;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.contrib.CircleLayout;
import edu.uci.ics.jung.visualization.contrib.KKLayout;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.control.SatelliteVisualizationViewer;
import edu.uci.ics.jung.visualization.control.ScalingControl;

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
public class PanelEGemViewer extends JPanel {
    EGem _egem;
    Graph _G;
    VisualizationModel _view;
    PluggableRenderer _pluggableRenderer;
    JComboBox _cbLayoutOffColor = new JComboBox(new Object[] {"yellow","blue","red","green"});
    JCheckBox _cbBoundary = new JCheckBox("Boundary",false);

    EGemVertex _removedPoint;
    HashSet<EGemVertex> _boundarySet;

    public PanelEGemViewer(EGem egem)  {
        _egem = egem;

        try {
            PrintStream pw = new PrintStream("c:/gem.txt");
            egem.pigale(pw);
            pw.flush();
            pw.close();
        } catch (FileNotFoundException ex) { ex.printStackTrace(); }

        Object[] x = egem.getBoundaryPointsAndRemovedPoint();
        _removedPoint = (EGemVertex)x[0];
        _boundarySet = (HashSet<EGemVertex>) x[1];

        _G = egem.getGraph();
        this.buildUI();
    }

    private void buildUI() {
        // need separate renderers for each view
        PluggableRenderer pr1 = new PluggableRenderer();
        PluggableRenderer pr2 = new PluggableRenderer();

        VertexShapeFunction vvv = new VertexShapeFunction() {
            VertexShapeFactory _vsf = new VertexShapeFactory(
                new ConstantVertexSizeFunction(50),
                new ConstantVertexAspectRatioFunction(0.5f));
            public Shape getShape(Vertex v) {
                return _vsf.getRoundRectangle(v);
            }
        };

        //
        for (PluggableRenderer pr: new PluggableRenderer[] {pr1,pr2}) {
            //pr.setEdgePaintFunction(new MyEdgePaint());
            pr.setVertexPaintFunction(new ConstantVertexPaintFunction(Color.black,Color.yellow));
            pr.setVertexStringer(new PEGVVertexLabeler());
            pr.setVertexLabelCentering(true);
            pr.setEdgeStringer(new PEGVEdgeLabeler());
            pr.setEdgeStrokeFunction(new ConstantEdgeStrokeFunction(2.0f));
            pr.setEdgePaintFunction(new PEGVEdgePaint());
            // pr.setEdgeIncludePredicate(new MyEdgeIncludePredicate());
            pr.setVertexIncludePredicate(new PEGVVertexIncludePredicate());
            // pr.setEdgeStringer(new MyEdgeLabeler());
            DefaultGraphLabelRenderer r = new DefaultGraphLabelRenderer(Color.blue, Color.cyan) {
                public Component getGraphLabelRendererComponent(JComponent vv, Object value,
                        Font font, boolean isSelected, Edge edge) {
                    super.getGraphLabelRendererComponent(vv, value,
                        font, isSelected, edge);
                    setForeground(Color.black);
                    return this;
                }
            };
            pr.setGraphLabelRenderer(r);
            pr.setVertexShapeFunction(vvv);
        }

        // the preferred sizes for the two views
        Dimension preferredSize1 = new Dimension(600,600);
        Dimension preferredSize2 = new Dimension(300, 300);

        // create one layout for the graph
        //CircleLayout layout = new CircleLayout(_G);
        // ISOMLayout layout = new ISOMLayout(_G);
        // System.out.println("Number of vertices: "+_G.getVertices().size());
        // layout.initialize(new Dimension(5000,5000));
        PEGVTutteLayout layout = new PEGVTutteLayout(_G,_boundarySet);

        // create one model that both views will share
        VisualizationModel vm = new DefaultVisualizationModel(layout, preferredSize1);
        _view = vm;

        // create 2 views that share the same model
        final VisualizationViewer vv1 = new VisualizationViewer(vm, pr1, preferredSize1);
        final SatelliteVisualizationViewer vv2 = new SatelliteVisualizationViewer(vv1, vm, pr2, preferredSize2);

        vv1.setBackground(Color.white);
        vv1.setPickSupport(new ShapePickSupport());

        // add default listener for ToolTips
        vv1.setToolTipFunction(new DefaultToolTipFunction());
        vv2.setToolTipFunction(new DefaultToolTipFunction());

        ToolTipManager.sharedInstance().setDismissDelay(10000);

        Container content = this;
        Container panel = new JPanel(new BorderLayout());
        Container rightPanel = new JPanel(new BorderLayout());

        //
        final Runnable _updateViews = new Runnable() {
            public void run() {
                repaint();
            }
        };

        // create a GraphMouse for the main view
        //
        final DefaultModalGraphMouse graphMouse = new DefaultModalGraphMouse();
        graphMouse.setMode(DefaultModalGraphMouse.Mode.PICKING);
        vv1.setGraphMouse(graphMouse);

        final ScalingControl scaler = new CrossoverScalingControl();

        JButton plus = new JButton("+");
        plus.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                scaler.scale(vv1, 1.1f, vv1.getCenter());
            }
        });
        JButton minus = new JButton("-");
        minus.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                scaler.scale(vv1, 0.9f, vv1.getCenter());
            }
        });

        JComboBox modeBox = graphMouse.getModeComboBox();
        modeBox.addItemListener(((DefaultModalGraphMouse)vv2.getGraphMouse()).getModeListener());

        // controls
        JPanel controls = new JPanel();
        controls.setLayout(new GridLayout(3,3,2,2));
        controls.add(plus);
        controls.add(minus);
        controls.add(modeBox);
        _cbBoundary.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                repaint();
            }
        });

        // add layout combo box
        JComboBox layoutCombo = new JComboBox(getCombos());
        layoutCombo.setRenderer(new DefaultListCellRenderer() {
            public Component getListCellRendererComponent(
                    JList list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus) {
                super.getListCellRendererComponent(list,
                                                   value,
                                                   index,
                                                   isSelected,
                                                   cellHasFocus);
                Class v = (Class) value;
                String s = "";
                StringTokenizer st = new StringTokenizer(v.getCanonicalName(), ".");
                while (st.hasMoreTokens())
                    s = st.nextToken();
                this.setText(s);
                return this;
            }

        });
        layoutCombo.addActionListener(new LayoutChooser(layoutCombo, _view));
        controls.add(layoutCombo);
        controls.add(_cbBoundary);
        controls.add(new JLabel(String.format("V%d E%d B%d R%s",
                                              _egem.getNumVertices(),
                                              _egem.getNumEdges(),
                                              _boundarySet.size(),
                                              _removedPoint.getOriginLabel()
                                )));


        // right panel
        rightPanel.add(vv2,BorderLayout.CENTER);
        rightPanel.add(controls,BorderLayout.SOUTH);

        // center panel
        panel.add(new GraphZoomScrollPane(vv1));
        panel.add(rightPanel, BorderLayout.EAST);

        // content
        content.setLayout(new BorderLayout());
        content.add(panel, BorderLayout.CENTER);
    }

    class PEGVVertexIncludePredicate implements Predicate {
        public boolean evaluate(Object o) {
            if (_cbBoundary.isSelected()) {
                if (o instanceof Vertex) {
                    Object oo = ((Vertex) o).getUserDatum("key");
                    if (_boundarySet.contains(oo))
                        return true;
                    else return false;
                }
                else return true;
            }
            else return true;
        }
    }

    private class ActionEditCurrentString extends AbstractAction {
        JComboBox _graphCombo;
        private ActionEditCurrentString(JComboBox graphCombo) {
            super("Edit");
            _graphCombo = graphCombo;
        }

        public void actionPerformed(ActionEvent arg0) {
            Gist g = (Gist) _graphCombo.getSelectedItem();
            JDialog frame = new JDialog((JFrame)((JComponent)arg0.getSource()).getTopLevelAncestor(),"Red.Graph",false);
            frame.setContentPane(new PanelNewString(g.getGemString()));
            frame.setBounds(0,0,800,600);
            frame.setVisible(true);
        }
    }

    private class LayoutChooser implements ActionListener {
        private final JComboBox jcb;
        private final VisualizationModel vv;

        private LayoutChooser(JComboBox jcb, VisualizationModel vv) {
            super();
            this.jcb = jcb;
            this.vv = vv;
        }

        public void actionPerformed(ActionEvent arg0) {
            Object[] constructorArgs = {_G};

            Class layoutC = (Class) jcb.getSelectedItem();
            Class lay = layoutC;
            try {
                Constructor constructor = lay.getConstructor(new Class[] {Graph.class});
                Object o = constructor.newInstance(constructorArgs);
                Layout l = (Layout) o;
                l.initialize(new Dimension(800,800));

                vv.stop();
                vv.setGraphLayout(l);
                vv.restart();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * @return
     */
    private static Class[] getCombos()
    {
        List layouts = new ArrayList();
        layouts.add(PEGVTutteLayout.class);
        layouts.add(ISOMLayout.class);
        layouts.add(KKLayout.class);
        layouts.add(FRLayout.class);
        layouts.add(CircleLayout.class);
        layouts.add(SpringLayout.class);
        return (Class[]) layouts.toArray(new Class[0]);
    }

    class PEGVEdgeIncludePredicate implements Predicate {
        public boolean evaluate(Object o) {
            return true;
        }
    }











    class PEGVTutteLayout extends AbstractLayout {
        HashSet<EGemVertex> _boundary;
        public PEGVTutteLayout(Graph g, HashSet<EGemVertex> boundary) {
            super(g);
            _boundary = boundary;
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
         * The default implemention shuffles elements randomly.
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
            Iterator it = getVertexIterator();
            while (it.hasNext()) {
                Vertex v = (Vertex) it.next();
                EGemVertex gv = (EGemVertex) v.getUserDatum("key");

                if (_boundary.contains(gv)) {
                    Coordinates coord = getCoordinates(v);
                    coord.setX(20+600*gv.getX2d());
                    coord.setY(20+600*gv.getY2d());
                }
                else {
                    Coordinates coord = getCoordinates(v);
                    coord.setX(20+Math.random()*600);
                    coord.setY(20+Math.random()*600);
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

class PEGVVertexLabeler implements VertexStringer {
    public String getLabel(ArchetypeVertex v) {
        EGemVertex vv = (EGemVertex) v.getUserDatum("key");
        return vv.getOriginLabel();
    }
}

class PEGVEdgeLabeler implements EdgeStringer {
    public String getLabel(ArchetypeEdge e) {
        EGemEdge ee = (EGemEdge) e.getUserDatum("key");
        return ee.getOriginLabel();
    }
}

class PEGVEdgePaint implements EdgePaintFunction {
    public Paint getDrawPaint(Edge e) {
        return Color.gray;
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
