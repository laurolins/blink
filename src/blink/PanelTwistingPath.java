package blink;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.commons.collections.Predicate;

import edu.uci.ics.jung.graph.ArchetypeEdge;
import edu.uci.ics.jung.graph.ArchetypeVertex;
import edu.uci.ics.jung.graph.Edge;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.graph.decorators.ConstantEdgePaintFunction;
import edu.uci.ics.jung.graph.decorators.ConstantEdgeStrokeFunction;
import edu.uci.ics.jung.graph.decorators.ConstantVertexPaintFunction;
import edu.uci.ics.jung.graph.decorators.EdgeShape;
import edu.uci.ics.jung.graph.decorators.EdgeStringer;
import edu.uci.ics.jung.graph.decorators.VertexStringer;
import edu.uci.ics.jung.visualization.AbstractLayout;
import edu.uci.ics.jung.visualization.Coordinates;
import edu.uci.ics.jung.visualization.FRLayout;
import edu.uci.ics.jung.visualization.GraphMouseListener;
import edu.uci.ics.jung.visualization.ISOMLayout;
import edu.uci.ics.jung.visualization.Layout;
import edu.uci.ics.jung.visualization.PluggableRenderer;
import edu.uci.ics.jung.visualization.SpringLayout;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.contrib.CircleLayout;
import edu.uci.ics.jung.visualization.contrib.KKLayout;

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
public class PanelTwistingPath extends JPanel {
    Graph _G;
    Lens _lenses;
    VisualizationViewer _view;
    PluggableRenderer _pluggableRenderer;
    JComboBox _cbLayoutOffColor = new JComboBox(new Object[] {"yellow","blue","red","green"});

    public PanelTwistingPath(Gem gem) {
        // calculate reduction graph
        CalculateReductionGraph crg = new CalculateReductionGraph(gem);
        _G = crg.getGraph();
        System.out.println("Rep: "+crg.getRepresentant().getCurrentLabelling().getLettersString(","));

        this.buildUI();
    }

    public PanelTwistingPath(Graph G) {
        _G = G;
        this.buildUI();
    }

    private void buildUI() {
        // setup renderer
        PluggableRenderer pr = new PluggableRenderer();
        // pr.setEdgePaintFunction(new ConstantEdgePaintFunction(Color.cyan,null));
        pr.setVertexPaintFunction(new ConstantVertexPaintFunction(Color.black,Color.lightGray));
        pr.setVertexStringer(new PTGVertexLabeler());
        pr.setEdgeStringer(new PTGEdgeLabeler());
        pr.setVertexLabelCentering(true);
        pr.setEdgeStrokeFunction(new ConstantEdgeStrokeFunction(2.0f));
        pr.setEdgePaintFunction(new ConstantEdgePaintFunction(Color.orange,null));
        pr.setEdgeIncludePredicate(new PTGEdgeIncludePredicate());
        pr.setVertexIncludePredicate(new PTGVertexIncludePredicate());
        pr.setEdgeShapeFunction(new EdgeShape.Line());


        _pluggableRenderer = pr;

        // setup viewer
        _view = new VisualizationViewer(new PTGLayout(_G),pr);
        _lenses = new Lens(_view);

        _view.addGraphMouseListener(new TestGraphMouseListener());


        //
        Class[] combos = getCombos();
        JComboBox jcb = new JComboBox(combos);
        jcb.addActionListener(new LayoutChooser(jcb, _view));

        // Layout bottom panel
        ActionListener al = new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _view.repaint();
            }
        };
        JPanel bottomPanel = new JPanel();
        bottomPanel.add(new JButton(new ActionZoom(1.20)));
        bottomPanel.add(new JButton(new ActionZoom(0.80)));
        bottomPanel.add(jcb);
        bottomPanel.add(new JButton(new ShowLenses()));

        // layout panel
        this.setLayout(new BorderLayout());
        this.add(new JScrollPane(_view),BorderLayout.CENTER);
        this.add(bottomPanel,BorderLayout.SOUTH);
    }

    /**
     * @return
     */
    private static Class[] getCombos()
    {
        List layouts = new ArrayList();
        layouts.add(PTGLayout.class);
        layouts.add(ISOMLayout.class);
        layouts.add(KKLayout.class);
        layouts.add(FRLayout.class);
        layouts.add(CircleLayout.class);
        layouts.add(SpringLayout.class);
        return (Class[]) layouts.toArray(new Class[0]);
    }

    private class LayoutChooser implements ActionListener {
        private final JComboBox jcb;
        private final VisualizationViewer vv;

        private LayoutChooser(JComboBox jcb, VisualizationViewer vv) {
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
                vv.stop();
                vv.setGraphLayout(l);
                vv.restart();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void myLayout() {
        // _view.setModel(
        //    new DefaultVisualizationModel(
        //        new PTGTutteLayout(_G,GemColor.values()[_cbLayoutOffColor.getSelectedIndex()])));
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
            super(String.format("Zoom %.2f",scale));
            _scale = scale;
        }
        public void actionPerformed(ActionEvent e) {
            _view.getViewTransformer().scale(_scale, _scale, _view.getCenter());
            _view.repaint();
        }
    }

    /**
     * A nested class to demo the GraphMouseListener finding the
     * right vertices after zoom/pan
     */
    static class TestGraphMouseListener implements GraphMouseListener {
                public void graphClicked(Vertex v, MouseEvent me) {
                    if (me.getClickCount() > 1) {
                        Gem l = (Gem)v.getUserDatum("key");
                        PanelGemViewer pgv = new PanelGemViewer(l);
                        JDialog d = new JDialog();
                        d.setAlwaysOnTop(true);
                        d.setContentPane(pgv);
                        d.setBounds(10,10,800,600);
                        d.setVisible(true);
                    }

//    		    System.err.println("Vertex "+v+" was clicked at ("+me.getX()+","+me.getY()+")");
                }
                public void graphPressed(Vertex v, MouseEvent me) {
//    		    System.err.println("Vertex "+v+" was pressed at ("+me.getX()+","+me.getY()+")");
                }
                public void graphReleased(Vertex v, MouseEvent me) {
//    		    System.err.println("Vertex "+v+" was released at ("+me.getX()+","+me.getY()+")");
                }
    }
    class ShowLenses extends AbstractAction {
        public ShowLenses() {
            super("Lenses");
        }
        public void actionPerformed(ActionEvent e) {
            _lenses.setVisible(true);
        }
    }

    class PTGEdgeIncludePredicate implements Predicate {
        public boolean evaluate(Object o) {
            return true;
        }
    }

    class PTGVertexIncludePredicate implements Predicate {
        public boolean evaluate(Object o) {
            Vertex v = (Vertex) o;
            Boolean b = (Boolean) v.getUserDatum("onPath");
            if (b != null && b.booleanValue())
                return true;
            return false;
        }
    }

    public static void main(String[] args) {

        // Blink b2 = new Blink(new int[]{6,14,2,9,4,8,5,12,7,13,10,1,11,3},81);
        // Gem g2 = (new GemFromBlink(b2)).getGem();
        Gem g2 = new Gem(new GemPackedLabelling("dabcgefjhimkljmledchgkaifbkfihmjalcbgde"));
        PanelTwistingPath prg = new PanelTwistingPath(g2);

        // desenhar o mapa
        JFrame f = new JFrame("Reduction Graph");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(new Dimension(1024,768));
        f.setContentPane(prg);
        f.setVisible(true);
        // desenhar o mapa
    }





    class PTGLayout extends AbstractLayout {

        public PTGLayout(Graph g) {
            super(g);
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

            // find vertex onPath with no outgoing edge
            Vertex v = null;
            Iterator it = getVertexIterator();
            while (it.hasNext()) {
                Vertex u = (Vertex) it.next();
                Boolean b = (Boolean) u.getUserDatum("onPath");

                // count on path out edges
                int countOnPathOutEdges = 0;
                for (Edge e: (Set<Edge>) u.getOutEdges()) {
                    Vertex vv = e.getOpposite(u);
                    Boolean b2 = (Boolean) vv.getUserDatum("onPath");
                    if (b2 != null && b2 == true)
                        countOnPathOutEdges++;
                }

                //
                if (b != null && b == true && countOnPathOutEdges == 0) {
                    v = u;
                    break;
                }
            }

            if (v == null)
                throw new RuntimeException("ooooopppppssss");

            ArrayList<Vertex> list = new ArrayList<Vertex>();
            list.add(v);

            Vertex u = v;
            while (true) {
                Set S = u.getInEdges();
                if (S.size() == 0)
                    break;
                else {
                    Vertex parent = null;
                    Iterator it2 = S.iterator();
                    while (it2.hasNext()) {
                        Edge e = (Edge) it2.next();
                        Vertex aux = e.getOpposite(u);
                        Boolean b = (Boolean) aux.getUserDatum("onPath");
                        if (b != null && b == true) {
                            parent = aux;
                            break;
                        }
                    }
                    if (parent == null) {
                        break;
                    } else {
                        list.add(parent);
                        u = parent;
                    }
                }
            }

            Collections.reverse(list);

            double x0 = 15;
            double y0 = 15;
            double deltaX = 300;
            double deltaY = 20;


            double x = x0;
            double y = y0;
            for (int i = 0;i<list.size();i++) {
                Vertex vv = list.get(list.size()-1-i);
                Coordinates coord = getCoordinates(vv);
                coord.setX(x);
                coord.setY(y);
                if (i % 2 == 0) {
                    x += deltaX;
                    y += deltaY;
                } else {
                    x -= deltaX;
                    y += deltaY;
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

class PTGVertexLabeler implements VertexStringer {
    public String getLabel(ArchetypeVertex v) {
        Gem g = (Gem) v.getUserDatum("key");
        return ""+g.getNumVertices()/*+" "+g.homologyGroup().toString()*/;
    }
}

class PTGEdgeLabeler implements EdgeStringer {
    public String getLabel(ArchetypeEdge e) {
        Object o = e.getUserDatum("key");
        return ""+o;
    }
}
