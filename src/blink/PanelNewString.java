package blink;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.JToggleButton;

import edu.uci.ics.jung.graph.ArchetypeVertex;
import edu.uci.ics.jung.graph.Edge;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.graph.decorators.ConstantVertexAspectRatioFunction;
import edu.uci.ics.jung.graph.decorators.ConstantVertexSizeFunction;
import edu.uci.ics.jung.graph.decorators.DefaultToolTipFunction;
import edu.uci.ics.jung.graph.decorators.EdgeShape;
import edu.uci.ics.jung.graph.decorators.VertexPaintFunction;
import edu.uci.ics.jung.graph.decorators.VertexShapeFunction;
import edu.uci.ics.jung.graph.decorators.VertexStringer;
import edu.uci.ics.jung.graph.event.GraphEvent;
import edu.uci.ics.jung.graph.event.GraphEventListener;
import edu.uci.ics.jung.graph.event.GraphEventType;
import edu.uci.ics.jung.graph.impl.DirectedSparseEdge;
import edu.uci.ics.jung.graph.impl.SparseGraph;
import edu.uci.ics.jung.graph.impl.SparseVertex;
import edu.uci.ics.jung.graph.impl.UndirectedSparseEdge;
import edu.uci.ics.jung.utils.UserData;
import edu.uci.ics.jung.visualization.AbstractLayout;
import edu.uci.ics.jung.visualization.DefaultSettableVertexLocationFunction;
import edu.uci.ics.jung.visualization.GraphZoomScrollPane;
import edu.uci.ics.jung.visualization.Layout;
import edu.uci.ics.jung.visualization.PickSupport;
import edu.uci.ics.jung.visualization.PickedState;
import edu.uci.ics.jung.visualization.PluggableRenderer;
import edu.uci.ics.jung.visualization.SettableVertexLocationFunction;
import edu.uci.ics.jung.visualization.ShapePickSupport;
import edu.uci.ics.jung.visualization.StaticLayout;
import edu.uci.ics.jung.visualization.VertexShapeFactory;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.AbstractPopupGraphMousePlugin;
import edu.uci.ics.jung.visualization.control.CrossoverScalingControl;
import edu.uci.ics.jung.visualization.control.EditingModalGraphMouse;
import edu.uci.ics.jung.visualization.control.ModalGraphMouse;
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
public class PanelNewString extends JPanel {
    /**
     * the graph
     */
    Graph _graph;

    AbstractLayout _layout;

    GemString _gemString;

    /**
     * the visual component and renderer for the graph
     */
    VisualizationViewer _vv;

    DefaultSettableVertexLocationFunction _vertexLocations;

    /**
     * New Vertex Type.
     */
    GemStringVertexType _newVertexType = GemStringVertexType.end;

    public PanelNewString() {
        this(new GemString());
    }

    private HashMap<GemStringVertex,Point2D> align(GemString gemString) {
        HashMap<GemStringVertex,Point2D> map = new HashMap<GemStringVertex,Point2D>();
        for (GemStringVertex v: gemString.getVertices()) {
            map.put(v,new Point2D.Double(Math.random()*600,Math.random()*600));
        }
        return map;
    }

    public PanelNewString(GemString gemString) {
        this.buildUI();

        _gemString = gemString;

        // align map
        HashMap<GemStringVertex,Point2D> mapPos = align(gemString);

        Graph graph = _vv.getGraphLayout().getGraph();

        HashMap<GemStringVertex,Vertex> map = new HashMap<GemStringVertex,Vertex>();
        for (GemStringVertex v: gemString.getVertices()) {
            Vertex newVertex = graph.addVertex(new SparseVertex());
            newVertex.setUserDatum("key",v,UserData.SHARED);
            _vertexLocations.setLocation(newVertex,mapPos.get(v));
            map.put(v,newVertex);
        }

        HashSet<GemStringEdge> S = new HashSet<GemStringEdge>();
        for (GemStringVertex v: gemString.getVertices()) {
            for (GemStringEdge e: v.getEdges()) {
                if (S.contains(e))
                    continue;
                Vertex uu = map.get(e.getVertices().getFirst());
                Vertex vv = map.get(e.getVertices().getSecond());
                Edge newEdge = graph.addEdge(new UndirectedSparseEdge(uu,vv));
                newEdge.setUserDatum("key",e,UserData.SHARED);
                S.add(e);
            }
        }

        _vv.getModel().restart();
        _vv.repaint();

        this.installGraphListener();
    }

    private void installGraphListener() {
        GraphEventListener listener = new GraphEventListener() {
            public void vertexAdded(GraphEvent event) {
                Vertex v = (Vertex) event.getGraphElement();
                GemStringVertex vv = _gemString.newVertex(_newVertexType);
                v.setUserDatum("key",vv,UserData.SHARED);
            }
            public void vertexRemoved(GraphEvent event) {
                Vertex v = (Vertex) event.getGraphElement();
                GemStringVertex vv = (GemStringVertex) v.getUserDatum("key");
                _gemString.delete(vv);
            }
            public void edgeAdded(GraphEvent event) {
                Edge e = (Edge) event.getGraphElement();
                Vertex u = (Vertex)e.getEndpoints().getFirst();
                Vertex v = (Vertex)e.getEndpoints().getSecond();
                GemStringVertex uu = (GemStringVertex) u.getUserDatum("key");
                GemStringVertex vv = (GemStringVertex) v.getUserDatum("key");
                GemStringEdge ee = _gemString.newEdge(uu,vv);
                e.setUserDatum("key",ee,UserData.SHARED);
            }
            public void edgeRemoved(GraphEvent event) {
                Edge e = (Edge) event.getGraphElement();
                GemStringEdge ee = (GemStringEdge) e.getUserDatum("key");
                _gemString.delete(ee);
            }
        };
        _graph.addListener(listener,GraphEventType.ALL_SINGLE_EVENTS);
    }

    private void buildUI() {
        // create a simple graph for the demo
        _graph = new SparseGraph();

        // allows the precise setting of initial vertex locations
        _vertexLocations = new DefaultSettableVertexLocationFunction();

        PluggableRenderer pr = new PluggableRenderer();
        _layout = new StaticLayout(_graph);
        _layout.initialize(new Dimension(600, 600), _vertexLocations);

        _vv = new VisualizationViewer(_layout, pr);
        _vv.setBackground(Color.white);
        _vv.setPickSupport(new ShapePickSupport());


        // ----------------------------
        // set decotations
        pr.setEdgeShapeFunction(new EdgeShape.QuadCurve());
        pr.setVertexStringer(new VertexStringer() {
            public String getLabel(ArchetypeVertex v) {
                return v.toString();
            }});
        _vv.setToolTipFunction(new DefaultToolTipFunction());

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
        pr.setVertexShapeFunction(vvv);
        pr.setVertexLabelCentering(true);
        pr.setVertexPaintFunction(new PNSVertexPaint());
        pr.setVertexStringer(new PNSVertexLabeler());
        // set decotations
        // ----------------------------


        final GraphZoomScrollPane panel = new GraphZoomScrollPane(_vv);

        final ScalingControl scaler = new CrossoverScalingControl();
        JButton plus = new JButton("+");
        plus.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                scaler.scale(_vv, 1.1f, _vv.getCenter());
            }
        });
        JButton minus = new JButton("-");
        minus.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                scaler.scale(_vv, 0.9f, _vv.getCenter());
            }
        });

        // --------------------------------------------------------------------
        // GRAPH MOUSE
        final EditingModalGraphMouse graphMouse = new EditingModalGraphMouse();

        // the EditingGraphMouse will pass mouse event coordinates to the
        // vertexLocations function to set the locations of the vertices as
        // they are created
        graphMouse.setVertexLocations(_vertexLocations);
        _vv.setGraphMouse(graphMouse);
        graphMouse.add(new MyEditingPopupGraphMousePlugin(_vertexLocations));
        graphMouse.setMode(ModalGraphMouse.Mode.EDITING);
        // --------------------------------------------------------------------

        // controls
        JPanel controls = new JPanel();
        controls.add(plus);
        controls.add(minus);
        JComboBox modeBox = graphMouse.getModeComboBox();
        controls.add(modeBox);
        controls.add(new JButton(new TryGemString()));
        controls.add(new JButton(new FindGemOnCatalog()));
        controls.add(new JButton(new Clear()));

        // bottom panel
        JPanel bottomPanel = new JPanel();
        bottomPanel.add(getNewVertexTypeButtons());
        bottomPanel.add(controls);

        // big panel
        this.setLayout(new BorderLayout());
        this.add(panel,BorderLayout.CENTER);
        this.add(bottomPanel,BorderLayout.SOUTH);

    }

    /**
     * New Vertex Type Choose Panel.
     */
    public JPanel getNewVertexTypeButtons() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridLayout(1,5,4,4));
        ButtonGroup bg = new ButtonGroup();
        for (GemStringVertexType t: GemStringVertexType.values()) {
            JToggleButton btn = new JToggleButton(new SetNewVertexType(t));
            panel.add(btn);
            bg.add(btn);
            if (t == GemStringVertexType.end)
                btn.setSelected(true);
        }
        return panel;
    }

    final class SetNewVertexType extends AbstractAction {
        private GemStringVertexType _type;
        public SetNewVertexType(GemStringVertexType type) {
            super(""+type.getLabel());
            _type = type;
        }
        public void actionPerformed(ActionEvent e) {
            _newVertexType = _type;
        }
    }

    final class TryGemString extends AbstractAction {
        public TryGemString() {
            super("Try");
        }
        public void actionPerformed(ActionEvent e) {
            Gem gem = null;
            try {
                gem = _gemString.getGem();
            } catch (Exception ex) {
            }

            JTabbedPane tp = new JTabbedPane();
            tp.add("String",new PanelEditString(_gemString));
            if (gem != null) {
                tp.add("Gem: yes", new PanelGemViewer(_gemString.getGem()));
            }
            else {
                tp.add("Gem: no", new JLabel("Did not find gem"));
            }

            JFrame d = new JFrame("Try Gem String");
            d.setContentPane(tp);
            d.setBounds(100,100,800,600);
            d.setVisible(true);
        }
    }

    final class FindGemOnCatalog extends AbstractAction {
        public FindGemOnCatalog() {
            super("CatNumb");
        }
        public void actionPerformed(ActionEvent e) {
            try {
                hardwork(((Component) e.getSource()));
            } catch (SQLException ex) {
                ex.printStackTrace();
            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (ClassNotFoundException ex) {
                ex.printStackTrace();
            }
        }
        private void hardwork(Component component) throws ClassNotFoundException, IOException, SQLException {
            Gem gem = null;
            try {
                gem = _gemString.getGem();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(component,"Not a valid gem!");
                return;
            }

            CalculateReductionGraph crg = new CalculateReductionGraph(gem);
            gem = crg.getRepresentant();

            long hc = gem.getCurrentLabelling().getGemHashCode();

            GemEntry result = null;
            int numregs = 1000;

            long minId = 1;

            while (true) {
                System.out.println("Querying from " + minId + " next " + numregs + "...");
                ArrayList<GemEntry> gems = App.getRepositorio().getSomeGems(minId, gem.getNumVertices(), numregs);
                if (gems.size() == 0)
                    break;
                for (GemEntry ee : gems) {
                    if (ee.getId() >= minId) {
                        minId = ee.getId() + 1;
                    }
                    if (hc != ee.getGemHashCode() || ee.getHandleNumber() != 0)
                        continue;
                    Gem otherGem = ee.getGem();
                    if (gem.equals(otherGem)) {
                        result = ee;
                        break;
                    }
                }
            }

            if (result == null) {
                JOptionPane.showMessageDialog(component, "Gem not on the catalog");
            } else {
                JOptionPane.showMessageDialog(component,
                                              String.format(
                                                      "This gem is R %d %d   (id: %d)",
                                                      result.getNumVertices(),
                                                      result.getCatalogNumber(),
                                                      result.getId()));
            }
        }
    }


    class MyEditingPopupGraphMousePlugin extends AbstractPopupGraphMousePlugin {

        SettableVertexLocationFunction vertexLocations;

        public MyEditingPopupGraphMousePlugin(SettableVertexLocationFunction vertexLocations) {
            this.vertexLocations = vertexLocations;
        }

        protected void handlePopup(MouseEvent e) {
            final VisualizationViewer vv =
                    (VisualizationViewer) e.getSource();
            final Layout layout = vv.getGraphLayout();
            final Graph graph = layout.getGraph();
            final Point2D p = e.getPoint();
            final Point2D ivp = vv.inverseViewTransform(e.getPoint());
            PickSupport pickSupport = vv.getPickSupport();
            if (pickSupport != null) {

                final Vertex vertex = pickSupport.getVertex(ivp.getX(), ivp.getY());
                final Edge edge = pickSupport.getEdge(ivp.getX(), ivp.getY());
                final PickedState pickedState = vv.getPickedState();
                JPopupMenu popup = new JPopupMenu();

                if (vertex != null) {
                    Set picked = pickedState.getPickedVertices();
                    if (picked.size() > 0) {
                        JMenu directedMenu = new JMenu("Create Directed Edge");
                        popup.add(directedMenu);
                        for (Iterator iterator = picked.iterator(); iterator.hasNext(); ) {
                            final Vertex other = (Vertex) iterator.next();
                            directedMenu.add(new AbstractAction("[" + other + "," + vertex + "]") {
                                public void actionPerformed(ActionEvent e) {
                                    Edge newEdge = new DirectedSparseEdge(other, vertex);
                                    graph.addEdge(newEdge);
                                    vv.repaint();
                                }
                            });
                        }
                        JMenu undirectedMenu = new JMenu("Create Undirected Edge");
                        popup.add(undirectedMenu);
                        for (Iterator iterator = picked.iterator(); iterator.hasNext(); ) {
                            final Vertex other = (Vertex) iterator.next();
                            undirectedMenu.add(new AbstractAction("[" + other + "," + vertex + "]") {
                                public void actionPerformed(ActionEvent e) {
                                    Edge newEdge = new UndirectedSparseEdge(other, vertex);
                                    graph.addEdge(newEdge);
                                    vv.repaint();
                                }
                            });
                        }
                    }
                    popup.add(new AbstractAction("Delete Vertex") {
                        public void actionPerformed(ActionEvent e) {
                            pickedState.pick(vertex, false);
                            graph.removeVertex(vertex);
                            vv.repaint();
                        }
                    });

                    GemStringVertex v = (GemStringVertex) vertex.getUserDatum("key");
                    popup.add(new ChangeType(GemStringVertexType.plus, v));
                    popup.add(new ChangeType(GemStringVertexType.minus, v));
                    popup.add(new ChangeType(GemStringVertexType.times, v));
                    popup.add(new ChangeType(GemStringVertexType.end, v));
                    popup.add(new ChangeType(GemStringVertexType.cross, v));

                } else if (edge != null) {
                    popup.add(new AbstractAction("Delete Edge") {
                        public void actionPerformed(ActionEvent e) {
                            pickedState.pick(edge, false);
                            graph.removeEdge(edge);
                            vv.repaint();
                        }
                    });

                    Vertex u = (Vertex) edge.getEndpoints().getFirst();
                    Vertex v = (Vertex) edge.getEndpoints().getSecond();
                    GemStringVertex uuu = (GemStringVertex) u.getUserDatum("key");
                    GemStringVertex vvv = (GemStringVertex) v.getUserDatum("key");
                    if (uuu.getType() == GemStringVertexType.cross)
                        popup.add(new MakeEdgeAsPrimary(edge, u));
                    if (vvv.getType() == GemStringVertexType.cross)
                        popup.add(new MakeEdgeAsPrimary(edge, v));
                    if (vvv.getType() == GemStringVertexType.cross && uuu.getType() == GemStringVertexType.cross)
                        popup.add(new MakeEdgeAsPrimary(edge,u,v));

                } else {
                    popup.add(new AbstractAction("Create Vertex") {
                        public void actionPerformed(ActionEvent e) {
                            Vertex newVertex = new SparseVertex();
                            vertexLocations.setLocation(newVertex, vv.inverseTransform(p));
                            Layout layout = vv.getGraphLayout();
                            for (Iterator iterator = graph.getVertices().iterator(); iterator.hasNext(); ) {
                                layout.lockVertex((Vertex) iterator.next());
                            }
                            graph.addVertex(newVertex);
                            vv.getModel().restart();
                            for (Iterator iterator = graph.getVertices().iterator(); iterator.hasNext(); ) {
                                layout.unlockVertex((Vertex) iterator.next());
                            }
                            vv.repaint();
                        }
                    });
                }
                if (popup.getComponentCount() > 0) {
                    popup.show(vv, e.getX(), e.getY());
                }
            }
        }
    }

    class Clear extends AbstractAction {
        public Clear() {
            super("Clear");
        }

        public void actionPerformed(ActionEvent event) {
            ArrayList<Vertex> list = new ArrayList<Vertex>((Set<Vertex>)_graph.getVertices());
            for (Vertex v: list) {
                _vv.getPickedState().pick(v, false);
                _graph.removeVertex(v);
            }
            _vv.repaint();
        }
    }

    class ChangeType extends AbstractAction {
        private GemStringVertexType _type;
        private GemStringVertex _vertex;
        public ChangeType(GemStringVertexType type, GemStringVertex vertex) {
            super("Change to "+type.getLabel());
            _type = type;
            _vertex = vertex;
        }

        public void actionPerformed(ActionEvent event) {
            _vertex.setType(_type);
            _newVertexType = _type;
            _vv.repaint();
        }
    }



    class MakeEdgeAsPrimary extends AbstractAction {
        private Edge _e;
        private Vertex[] _vs;
        private GemStringEdge _ee;
        private GemStringVertex[] _vvs;
        public MakeEdgeAsPrimary(Edge e, Vertex ... vertices) {
            super("");
            _e = e;
            _ee = (GemStringEdge)_e.getUserDatum("key");
            _vs = vertices;
            _vvs = new GemStringVertex[vertices.length];
            for (int i=0;i<_vs.length;i++) {
                _vvs[i] = (GemStringVertex) _vs[i].getUserDatum("key");
            }
            String st = "Make edge primary of";
            for (GemStringVertex v: _vvs) {
                st = st+" "+v.getLabel();
            }
            super.putValue(Action.NAME,st);
        }

        public void actionPerformed(ActionEvent event) {
            for (Vertex u: _vs) {

                // define p0, the base unitary vector (primary)
                Point2D pu = _vv.getGraphLayout().getLocation(u);
                Point2D pv = _vv.getGraphLayout().getLocation(_e.getOpposite(u));
                double x = pv.getX()-pu.getX();
                double y = pv.getY()-pu.getY();
                double dist = Math.sqrt(x*x+y*y);
                Point2D.Double p0 = new Point2D.Double(x/dist,y/dist);

                // calculate p0 angle
                double cosTheta0 = p0.getX()*1 + 0*p0.getY();
                double theta0 = Math.acos(cosTheta0);
                if (p0.getY() < 0) theta0 = Math.PI*2 - theta0;

                System.out.println(String.format("Edge %d %d theta0 %.2f ",
                                   ((GemStringVertex) _ee.getVertices().getFirst()).getLabel(),
                                   ((GemStringVertex) _ee.getVertices().getSecond()).getLabel(),
                                   180.0 * theta0 / Math.PI));

                // calculate the angle for each other vertex
                GemStringVertex uu = (GemStringVertex) u.getUserDatum("key");
                HashMap<GemStringEdge,Double> mapEdgeTheta = new HashMap<GemStringEdge,Double>();
                ArrayList<GemStringEdge> list = new ArrayList<GemStringEdge>();
                for (Edge e: (Set<Edge>) u.getOutEdges()) {
                    if (e == _e) continue;
                    GemStringEdge ee = (GemStringEdge) e.getUserDatum("key");
                    list.add(ee);

                    // define the unitary vector pe
                    Point2D pOpposite = _vv.getGraphLayout().getLocation(e.getOpposite(u));
                    double xx = pOpposite.getX() - pu.getX();
                    double yy = pOpposite.getY() - pu.getY();
                    double ddist = Math.sqrt(xx*xx+yy*yy);
                    Point2D pe = new Point2D.Double(xx/ddist,yy/ddist);

                    // find pe angle, thetaE
                    double cosThetaE = pe.getX()*1 + 0*pe.getY();
                    double thetaE = Math.acos(cosThetaE);
                    if (pe.getY() < 0) thetaE = Math.PI*2 - thetaE;

                    double deltaTheta = (thetaE < theta0 ? 2*Math.PI + thetaE : thetaE) - theta0;
                    if (deltaTheta < 0) deltaTheta = Math.PI*2 - deltaTheta;

                    mapEdgeTheta.put(ee,deltaTheta);
                    System.out.println(String.format("Edge %d %d theta %.2f deltaTheta %.2f",
                                       ((GemStringVertex) ee.getVertices().getFirst()).getLabel(),
                                       ((GemStringVertex) ee.getVertices().getSecond()).getLabel(),
                                       180.0 * thetaE / Math.PI,
                                       180.0 * deltaTheta / Math.PI));
                }

                // sort these angles...
                Collections.sort(list,new GemStringEdgeAngleComparator(mapEdgeTheta));

                // insert on the correct cyclic order
                if (_ee.getVertices().getFirst() == _ee.getVertices().getSecond()) {
                    list.add(0,_ee);
                    list.add(0,_ee);
                }
                else list.add(0,_ee);
                uu.permuteEdges(list);
            }
            _vv.repaint();
        }
    }


    final class GemStringEdgeAngleComparator implements Comparator {
        HashMap<GemStringEdge,Double> _mapEdgeTheta = new HashMap<GemStringEdge,Double>();
        public GemStringEdgeAngleComparator(HashMap<GemStringEdge,Double> mapEdgeTheta) {
            _mapEdgeTheta = mapEdgeTheta;
        }
        public int compare(Object o1, Object o2) {
            GemStringEdge e1 = (GemStringEdge) o1;
            GemStringEdge e2 = (GemStringEdge) o2;
            double t1 = _mapEdgeTheta.get(e1);
            double t2 = _mapEdgeTheta.get(e2);
            if (t1 < t2) return -1;
            else if (t1 > t2) return 1;
            else return 0;
        }
    }

    public static void main(String[] args) {
        JFrame f = new JFrame("Test String Input");
        f.setContentPane(new PanelNewString());
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setBounds(10,10,500,500);
        f.setVisible(true);
    }

}

class PNSVertexPaint implements VertexPaintFunction {
    public Paint getFillPaint(Vertex v) {
        GemStringVertex gv = (GemStringVertex) v.getUserDatum("key");
        if (gv.getType() == GemStringVertexType.plus) {
            return Color.blue;
        } else if (gv.getType() == GemStringVertexType.minus) {
            return Color.red;
        } else if (gv.getType() == GemStringVertexType.times) {
            return Color.green;
        } else if (gv.getType() == GemStringVertexType.cross) {
            return Color.yellow;
        } else if (gv.getType() == GemStringVertexType.end) {
            return Color.white;
        }
        return Color.cyan;
    }

    public Paint getDrawPaint(Vertex v) {
        return Color.black;
    }
}

class PNSVertexLabeler implements VertexStringer {
    public String getLabel(ArchetypeVertex v) {
        GemStringVertex vv = (GemStringVertex) v.getUserDatum("key");
        if (vv.getType() == GemStringVertexType.cross) {
            if (vv.getNumberOfEdges() >= 3) {
                int lbl1 = vv.getEdge(0).getOpposite(vv).getLabel();
                int lbl2 = vv.getEdge(2).getOpposite(vv).getLabel();
                return String.format("%d [%d,%d]",vv.getLabel(),lbl1,lbl2);
            }
            else if (vv.getNumberOfEdges() >= 1) {
                int lbl1 = vv.getEdge(0).getOpposite(vv).getLabel();
                return String.format("%d [%d,?]",vv.getLabel(),lbl1);
            }
            else return String.format("%s %d",vv.getType().getLabel(),vv.getLabel());
        }
        else return String.format("%s %d",vv.getType().getLabel(),vv.getLabel());
    }
}






