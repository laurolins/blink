package blink;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.AbstractAction;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
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
import edu.uci.ics.jung.graph.decorators.EdgeStringer;
import edu.uci.ics.jung.graph.decorators.VertexShapeFunction;
import edu.uci.ics.jung.graph.decorators.VertexStringer;
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
public class PanelGemEquivalence extends JPanel {
    Graph _G;
    VisualizationModel _view;
    PluggableRenderer _pluggableRenderer;
    JComboBox _cbLayoutOffColor = new JComboBox(new Object[] {"yellow","blue","red","green"});

    JTextArea _output = new JTextArea();

    private GemGraph _gemGraph;
    public PanelGemEquivalence() throws ClassNotFoundException, IOException, SQLException {
        _gemGraph = new GemGraph();
        _this = this;
        _G = _gemGraph.getGraph();
        this.buildUI();
    }

    private PanelGemEquivalence _this;
    private void buildUI() {
        // need separate renderers for each view
        PluggableRenderer pr1 = new PluggableRenderer();
        PluggableRenderer pr2 = new PluggableRenderer();

        VertexShapeFunction vvv = new VertexShapeFunction() {
            VertexShapeFactory _vsf = new VertexShapeFactory(
                new ConstantVertexSizeFunction(30),
                new ConstantVertexAspectRatioFunction(1f));
            public Shape getShape(Vertex v) {
                return _vsf.getEllipse(v);
            }
        };

        //
        for (PluggableRenderer pr: new PluggableRenderer[] {pr1,pr2}) {
            //pr.setEdgePaintFunction(new MyEdgePaint());
            pr.setVertexPaintFunction(new ConstantVertexPaintFunction(Color.black,Color.yellow));
            pr.setVertexStringer(new PGEVertexLabeler());
            pr.setVertexLabelCentering(true);
            pr.setEdgeStrokeFunction(new ConstantEdgeStrokeFunction(1.0f));
            // pr.setEdgeIncludePredicate(new MyEdgeIncludePredicate());
            // pr.setVertexIncludePredicate(new MyVertexIncludePredicate());
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
        CircleLayout layout = new CircleLayout(_G);
        //ISOMLayout layout = new ISOMLayout(_G);
        //SpringLayout layout = new SpringLayout(_G);
        //FRLayout layout = new FRLayout(_G);
        //layouts.add(SpringLayout.class);
        layout.initialize(new Dimension(5000,5000));
        //layout.setMaxIterations(0);

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

        JButton btnConnectionClosure = new JButton(new ConnectionClosureAction());





        JComboBox modeBox = graphMouse.getModeComboBox();
        modeBox.addItemListener(((DefaultModalGraphMouse)vv2.getGraphMouse()).getModeListener());


        // controls
        JPanel controls = new JPanel();
        controls.add(plus);
        controls.add(minus);
        controls.add(modeBox);
        { // combo with layout chooser
            Class[] combos = getCombos();
            JComboBox jcb = new JComboBox(combos);
            jcb.setRenderer(new DefaultListCellRenderer() {
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
                    String s="";
                    StringTokenizer st = new StringTokenizer(v.getCanonicalName(),".");
                    while (st.hasMoreTokens())
                        s = st.nextToken();
                    this.setText(s);
                    return this;
                }

            });
            jcb.addActionListener(new LayoutChooser(jcb, _view));
            controls.add(jcb);
        } // combo with layout chooser

        JPanel bottomRightPanel = new JPanel();
        bottomRightPanel.setLayout(new BorderLayout());
        bottomRightPanel.add(btnConnectionClosure,BorderLayout.NORTH);
        JScrollPane sp = new JScrollPane(_output);
        sp.setPreferredSize(new Dimension(100,120));
        bottomRightPanel.add(sp,BorderLayout.CENTER);
        bottomRightPanel.add(controls,BorderLayout.SOUTH);

        // right panel
        rightPanel.add(vv2,BorderLayout.CENTER);
        rightPanel.add(bottomRightPanel,BorderLayout.SOUTH);

        // center panel
        panel.add(new GraphZoomScrollPane(vv1));
        panel.add(rightPanel, BorderLayout.EAST);

        // content
        content.setLayout(new BorderLayout());
        content.add(panel, BorderLayout.CENTER);
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
                l.initialize(new Dimension(5000,5000));

                vv.stop();
                vv.setGraphLayout(l);
                vv.restart();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class ConnectionClosureAction extends AbstractAction {
        public ConnectionClosureAction() {
            super("Con.");
        }
        public void actionPerformed(ActionEvent e) {
            this.hardwork();
        }
        private void hardwork() {
            String st = (String) JOptionPane.showInputDialog(_this,"Gem IDs");
            if (st == null) return;
            StringTokenizer stok = new StringTokenizer(st," ,-;./");
            HashSet<Long> S = new HashSet<Long>();
            while (stok.hasMoreTokens()) {
                long id = Long.parseLong(stok.nextToken());
                S.addAll(_gemGraph.getConnectedComponentGemEntriesIds(id));
            }
            ArrayList<Long> list = new ArrayList<Long>(S);
            Collections.sort(list);
            StringBuffer sb = new StringBuffer();
            for (Long id: list) {
                sb.append(id+" ");
            }
            _output.setText(sb.toString());
        }
    }


    /**
     * @return
     */
    private static Class[] getCombos()
    {
        List layouts = new ArrayList();
        layouts.add(ISOMLayout.class);
        layouts.add(KKLayout.class);
        layouts.add(FRLayout.class);
        layouts.add(CircleLayout.class);
        layouts.add(SpringLayout.class);
        return (Class[]) layouts.toArray(new Class[0]);
    }

    class PGEEdgeIncludePredicate implements Predicate {
        public boolean evaluate(Object o) {
            return true;
        }
    }

    class PGEVertexIncludePredicate implements Predicate {
        public boolean evaluate(Object o) {
            Vertex v = (Vertex) o;
            Boolean b = (Boolean) v.getUserDatum("onPath");
            if (b != null && b.booleanValue())
                return true;
            return false;
        }
    }

    public static void main(String[] args) throws SQLException, IOException, ClassNotFoundException {
        // Blink b2 = new Blink(new int[]{6,14,2,9,4,8,5,12,7,13,10,1,11,3},81);
        // Gem g2 = (new GemFromBlink(b2)).getGem();
        PanelGemEquivalence pge = new PanelGemEquivalence();

        // desenhar o mapa
        JFrame f = new JFrame("Reduction Graph");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(new Dimension(1024,768));
        f.setContentPane(pge);
        f.setVisible(true);
        // desenhar o mapa
    }

}

class PGEVertexLabeler implements VertexStringer {
    public String getLabel(ArchetypeVertex v) {
        GemEntry ge = (GemEntry) v.getUserDatum("key");
        return ""+ge.getId();
    }
}

class PGEEdgeLabeler implements EdgeStringer {
    public String getLabel(ArchetypeEdge e) {
        return "";
    }
}
