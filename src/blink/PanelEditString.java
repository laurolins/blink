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
import java.awt.geom.GeneralPath;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
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
import edu.uci.ics.jung.graph.decorators.EdgeArrowFunction;
import edu.uci.ics.jung.graph.decorators.EdgeStringer;
import edu.uci.ics.jung.graph.decorators.VertexPaintFunction;
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
public class PanelEditString extends JPanel {
    Graph _G;
    VisualizationModel _view;
    PluggableRenderer _pluggableRenderer;
    JComboBox _cbLayoutOffColor = new JComboBox(new Object[] {"yellow","blue","red","green"});

    public PanelEditString(GemString gemString) {
        _G = gemString.getGraph();
        this.buildUI();
    }

    private void buildUI() {
        // need separate renderers for each view
        PluggableRenderer pr1 = new PluggableRenderer();
        PluggableRenderer pr2 = new PluggableRenderer();

        VertexShapeFunction vvv = new VertexShapeFunction() {
            VertexShapeFactory _vsf = new VertexShapeFactory(
                new ConstantVertexSizeFunction(30),
                new ConstantVertexAspectRatioFunction(1f));
            VertexShapeFactory _vsf2 = new VertexShapeFactory(
                new ConstantVertexSizeFunction(70),
                new ConstantVertexAspectRatioFunction(0.5f));
            public Shape getShape(Vertex v) {
                GemStringVertex gv = (GemStringVertex) v.getUserDatum("key");
                if (gv.getType() == GemStringVertexType.cross)
                    return _vsf2.getRoundRectangle(v);
                else //if (gv.getType() == GemStringVertexType.cross)
                    return _vsf.getEllipse(v);
            }
        };

        //
        for (PluggableRenderer pr: new PluggableRenderer[] {pr1,pr2}) {
            //pr.setEdgePaintFunction(new MyEdgePaint());
            pr.setVertexPaintFunction(new ConstantVertexPaintFunction(Color.black,Color.yellow));
            pr.setVertexStringer(new PESVertexLabeler());
            pr.setVertexLabelCentering(true);
            // pr.setEdgeStringer(new PESEdgeLabeler());
            pr.setEdgeStrokeFunction(new ConstantEdgeStrokeFunction(2.0f));
            pr.setVertexPaintFunction(new PESVertexPaint());
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
            //pr.setEdgeArrowFunction(new PESEdgeArrowFunction(10, 8, 4));
            /*
            pr.setEdgeArrowPredicate(new Predicate() {
                public boolean evaluate(Object o) {
                    return true;
                }
            }); */

            //new DirectionDisplayPredicate(true,true));
        }



        // the preferred sizes for the two views
        Dimension preferredSize1 = new Dimension(600,600);
        Dimension preferredSize2 = new Dimension(300, 300);

        // create one layout for the graph
        //CircleLayout layout = new CircleLayout(_G);
        ISOMLayout layout = new ISOMLayout(_G);
        layout.initialize(new Dimension(800,800));

        // create one model that both views will share
        VisualizationModel vm =
            new DefaultVisualizationModel(layout, preferredSize1);
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
        controls.setLayout(new GridLayout(2,3,2,2));
        controls.add(plus);
        controls.add(minus);
        controls.add(modeBox);

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

    private class GraphChooser implements ActionListener {
        private final JComboBox _graphCombo;
        private final JComboBox _layoutCombo;
        private final VisualizationModel vv;

        private GraphChooser(JComboBox graphCombo,
                             JComboBox layoutCombo,
                             VisualizationModel vv) {
            super();
            this._graphCombo = graphCombo;
            this._layoutCombo = layoutCombo;
            this.vv = vv;
        }

        public void actionPerformed(ActionEvent arg0) {
            _G = (Graph) _graphCombo.getSelectedItem();


            Object[] constructorArgs = {_G};

            Class layoutC = (Class) _layoutCombo.getSelectedItem();
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
        layouts.add(ISOMLayout.class);
        layouts.add(KKLayout.class);
        layouts.add(FRLayout.class);
        layouts.add(CircleLayout.class);
        layouts.add(SpringLayout.class);
        return (Class[]) layouts.toArray(new Class[0]);
    }

    class PESEdgeIncludePredicate implements Predicate {
        public boolean evaluate(Object o) {
            return true;
        }
    }

    class PESVertexIncludePredicate implements Predicate {
        public boolean evaluate(Object o) {
            Vertex v = (Vertex) o;
            Boolean b = (Boolean) v.getUserDatum("onPath");
            if (b != null && b.booleanValue())
                return true;
            return false;
        }
    }
}

class PESVertexLabeler implements VertexStringer {
    public String getLabel(ArchetypeVertex v) {
        GemStringVertex gv = (GemStringVertex) v.getUserDatum("key");
        if (gv.getType() == GemStringVertexType.cross) {
            int lbl1 = gv.getEdge(0).getOpposite(gv).getLabel();
            int lbl2 = gv.getEdge(2).getOpposite(gv).getLabel();
            return String.format("%d [%d,%d]",gv.getLabel(),lbl1,lbl2);
        }
        else return String.format("%s %d",gv.getType().getLabel(),gv.getLabel());
    }
}

class PESEdgeLabeler implements EdgeStringer {
    public String getLabel(ArchetypeEdge e) {
        return ""+e.getUserDatum("key");
    }
}

class PESVertexPaint implements VertexPaintFunction {
    public Paint getFillPaint(Vertex v){
        GemStringVertex gv = (GemStringVertex) v.getUserDatum("key");
        if (gv.getType() == GemStringVertexType.plus) {
            return Color.blue;
        }
        else if (gv.getType() == GemStringVertexType.minus) {
            return Color.red;
        }
        else if (gv.getType() == GemStringVertexType.times) {
            return Color.green;
        }
        else if (gv.getType() == GemStringVertexType.cross) {
            return Color.yellow;
        }
        else if (gv.getType() == GemStringVertexType.end) {
            return Color.white;
        }
        return Color.cyan;
    }
    public Paint getDrawPaint(Vertex v){
        return Color.black;
    }
}

class PESEdgeArrowFunction implements EdgeArrowFunction
{
    protected Shape _myArrow;

    public PESEdgeArrowFunction(int length, int width, int notch_depth)
    {
        _myArrow = getMyArrow(width, length, notch_depth);
    }

    public static GeneralPath getMyArrow(float base, float height, float notch_height)
    {
        GeneralPath arrow = new GeneralPath();
        arrow.moveTo(0,0);
        arrow.lineTo(0, base/2.0f);
        arrow.lineTo(-height, base/2.0f);
        arrow.lineTo(-height, -base/2.0f);
        arrow.lineTo(0, -base/2.0f);
        arrow.lineTo(0,0);
        return arrow;
    }

    /**
     * @see edu.uci.ics.jung.graph.decorators.EdgeArrowFunction#getArrow(edu.uci.ics.jung.graph.Edge)
     */
    public Shape getArrow(Edge e)
    {
        return _myArrow;
    }

}
