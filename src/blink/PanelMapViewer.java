package blink;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.apache.commons.collections.Predicate;

import edu.uci.ics.jung.graph.ArchetypeVertex;
import edu.uci.ics.jung.graph.Edge;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.graph.decorators.ConstantEdgeStrokeFunction;
import edu.uci.ics.jung.graph.decorators.ConstantVertexPaintFunction;
import edu.uci.ics.jung.graph.decorators.EdgePaintFunction;
import edu.uci.ics.jung.graph.decorators.VertexStringer;
import edu.uci.ics.jung.graph.impl.SparseGraph;
import edu.uci.ics.jung.graph.impl.SparseVertex;
import edu.uci.ics.jung.graph.impl.UndirectedSparseEdge;
import edu.uci.ics.jung.utils.Pair;
import edu.uci.ics.jung.utils.UserData;
import edu.uci.ics.jung.visualization.AbstractLayout;
import edu.uci.ics.jung.visualization.Coordinates;
import edu.uci.ics.jung.visualization.PluggableRenderer;
import edu.uci.ics.jung.visualization.VisualizationViewer;

/**
 * PanelMapViewer
 */
public class PanelMapViewer extends JPanel {
    SparseGraph _G;
    GBlink _blink;
    Lens _lenses;
    VisualizationViewer _view;
    PluggableRenderer _pluggableRenderer;
    HashMap<GBlinkVertex,Vertex> _mapGV2V;
    JCheckBox[] _cbColors = {
                            new JCheckBox("Face"),
                            new JCheckBox("Vertex"),
                            new JCheckBox("Angle"),
                            new JCheckBox("Cross")
    };

    public final static String KEY = "key";
    public final static String COLOR = "color";
    public PanelMapViewer(GBlink blink) {
        // mount graph
        _blink = blink;
        _G = this.mountGraph(blink);

        // setup renderer
        PluggableRenderer pr = new PluggableRenderer();
        pr.setEdgePaintFunction(new MapEdgePaint());
        // pr.setEdgePaintFunction(new ConstantEdgePaintFunction(Color.cyan,null));
        pr.setVertexPaintFunction(new ConstantVertexPaintFunction(Color.black,Color.lightGray));
        pr.setVertexStringer(new MapVertexLabeler());
        pr.setVertexLabelCentering(true);
        pr.setEdgeStrokeFunction(new ConstantEdgeStrokeFunction(2.0f));
        pr.setEdgeIncludePredicate(new MapEdgeIncludePredicate());

        _pluggableRenderer = pr;

        // setup viewer
        //_view = new VisualizationViewer(new CircleLayout(_G), pr);
        _view = new VisualizationViewer(new MapTutteLayout(_G), pr);
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
        bottomPanel.add(new JButton(new ShowLenses()));
        bottomPanel.add(new JButton(new SavePDF(this, _view)));

        // layout panel
        this.setLayout(new BorderLayout());
        this.add(new JScrollPane(_view),BorderLayout.CENTER);
        this.add(bottomPanel,BorderLayout.SOUTH);
        this.add(new JLabel(String.format("Blink")),BorderLayout.NORTH);
    }

    public static void main(String[] args) {
        GBlink b2 = new GBlink(new int[] {6, 14, 2, 9, 4, 8, 5, 12, 7, 13, 10, 1, 11, 3}, 81);
        //Blink b2 = new Blink(new int[][] {{1},{1,3,2},{2,3,4,4}}, new int[] {2});
        // Gem g2 = (new GemFromBlink(b2)).getGem();
        // PanelReductionGraph prg = new PanelReductionGraph(g2);

        // desenhar o mapa
        JFrame f = new JFrame("Reduction Graph");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(new Dimension(1024, 768));
        f.setContentPane(new PanelMapViewer(b2));
        f.setVisible(true);
        // desenhar o mapa
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

    class ShowLenses extends AbstractAction {
        public ShowLenses() {
            super("Lenses");
        }
        public void actionPerformed(ActionEvent e) {
            _lenses.setVisible(true);
        }
    }

    private SparseGraph mountGraph(GBlink blink) {
        // create the contracted graph with offColor c
        SparseGraph G = new SparseGraph();
        _G = G;

        HashSet<Pair> processedVertices = new HashSet<Pair>();

        // add vertices from gem
        _mapGV2V = new HashMap<GBlinkVertex,Vertex>();
        for (GBlinkVertex v: blink.getVertices()) {
            Vertex vv = G.addVertex(new SparseVertex());
            vv.setUserDatum(KEY,v,UserData.SHARED);
            _mapGV2V.put(v,vv);
        }

        // add edges from gem
        for (GBlinkVertex v: blink.getVertices()) {
            Vertex vv  = _mapGV2V.get(v);
            for (GBlinkEdgeType t: new GBlinkEdgeType[] {GBlinkEdgeType.face,GBlinkEdgeType.vertex,GBlinkEdgeType.edge}) {
                if (v.getNeighbour(t).getLabel() > v.getLabel()) {
                    Vertex vvy = _mapGV2V.get(v.getNeighbour(t));
                    Edge ey = G.addEdge(new UndirectedSparseEdge(vv, vvy));

                    ey.setUserDatum(COLOR, t, UserData.SHARED);
                }
            }
        }

        // add diagonal edge for each blink edge
        HashSet<GBlinkVertex> S = new HashSet<GBlinkVertex>();
        for (GBlinkVertex a: _blink.getVertices()) {
            if (S.contains(a))
                continue;
            GBlinkVertex b = a.getNeighbour(GBlinkEdgeType.face);
            GBlinkVertex c = b.getNeighbour(GBlinkEdgeType.vertex);
            GBlinkVertex d = c.getNeighbour(GBlinkEdgeType.face);
            S.add(a); S.add(b); S.add(c); S.add(d);

            if (a.overcross()) {
                {
                    Edge ey = G.addEdge(new UndirectedSparseEdge(_mapGV2V.get(b), _mapGV2V.get(d)));
                    ey.setUserDatum(COLOR, Color.black, UserData.SHARED);
                }
                {
                    Edge ey = G.addEdge(new UndirectedSparseEdge(_mapGV2V.get(a), _mapGV2V.get(c)));
                    ey.setUserDatum(COLOR, Color.red, UserData.SHARED);
                }
            }
            else {
                {
                    Edge ey = G.addEdge(new UndirectedSparseEdge(_mapGV2V.get(b), _mapGV2V.get(d)));
                    ey.setUserDatum(COLOR, Color.red, UserData.SHARED);
                }
                {
                    Edge ey = G.addEdge(new UndirectedSparseEdge(_mapGV2V.get(a), _mapGV2V.get(c)));
                    ey.setUserDatum(COLOR, Color.black, UserData.SHARED);
                }
            }
        }
        S = null;


        /*
        for (int i=1;i<=_blink.getNumberOfGEdges();i++) {
            BlinkColor c = _blink.getColor(i);
            if (c == BlinkColor.green) {
                {
                    Vertex u = _mapGV2V.get(_blink.findVertex(4 * (i - 1) + 2));
                    Vertex v = _mapGV2V.get(_blink.findVertex(4 * (i - 1) + 4));
                    Edge ey = G.addEdge(new UndirectedSparseEdge(u, v));
                    ey.setUserDatum(COLOR, Color.black, UserData.SHARED);
                }
                {
                    Vertex u = _mapGV2V.get(_blink.findVertex(4 * (i - 1) + 1));
                    Vertex v = _mapGV2V.get(_blink.findVertex(4 * (i - 1) + 3));
                    Edge ey = G.addEdge(new UndirectedSparseEdge(u, v));
                    ey.setUserDatum(COLOR, Color.red, UserData.SHARED);
                }
            }
            else if (c == BlinkColor.red) {
                {
                    Vertex u = _mapGV2V.get(_blink.findVertex(4*(i-1)+1));
                    Vertex v = _mapGV2V.get(_blink.findVertex(4 * (i - 1) + 3));
                    Edge ey = G.addEdge(new UndirectedSparseEdge(u, v));
                    ey.setUserDatum(COLOR, Color.black, UserData.SHARED);
                }
                {
                    Vertex u = _mapGV2V.get(_blink.findVertex(4 * (i - 1) + 2));
                    Vertex v = _mapGV2V.get(_blink.findVertex(4 * (i - 1) + 4));
                    Edge ey = G.addEdge(new UndirectedSparseEdge(u, v));
                    ey.setUserDatum(COLOR, Color.red, UserData.SHARED);
                }
            }
        }*/

        return G;
    }

    class MapEdgeIncludePredicate implements Predicate {
        public boolean evaluate(Object o) {
             if (o instanceof Edge) {
                Edge e = (Edge) o;
                Object oo = e.getUserDatum("color");
                if (oo instanceof GBlinkEdgeType) {
                    GBlinkEdgeType t = (GBlinkEdgeType) oo;
                    if (t == GBlinkEdgeType.face && _cbColors[0].isSelected()) {
                        return true;
                    } else if (t == GBlinkEdgeType.vertex && _cbColors[1].isSelected()) {
                        return true;
                    } else if (t == GBlinkEdgeType.edge && _cbColors[2].isSelected()) {
                        return true;
                    }
                }
                else if (_cbColors[3].isSelected()) {
                    return true;
                }
                else return false;
            }
            return false;
        }
    }

    class MapTutteLayout extends AbstractLayout {

        public MapTutteLayout(Graph g) {
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

            //
            HashMap<GBlinkVertex, Point2D.Double> map =
                    TuttesLayout.mapLayout(_blink,20,20,640,640);

            //
            Iterator it = getVertexIterator();
            while (it.hasNext()) {
                Vertex v = (Vertex) it.next();
                GBlinkVertex gv = (GBlinkVertex) v.getUserDatum("key");
                Point2D.Double p = map.get(gv);

                Coordinates coord = getCoordinates(v);
                coord.setX(p.getX());
                coord.setY(p.getY());
            }
        }

        /**
         * Do nothing.
         */
        public void advancePositions() {
        }
    }
}

class MapEdgePaint implements EdgePaintFunction {
    public Paint getDrawPaint(Edge e) {
        Object o = e.getUserDatum("color");
        if (o instanceof Color)
            return (Color) o;
        else if (o instanceof GBlinkEdgeType) {
            GBlinkEdgeType t = (GBlinkEdgeType) o;
            Color c = Color.black;
            if (t == GBlinkEdgeType.face)
                c = Color.cyan;
            else if (t == GBlinkEdgeType.vertex)
                c = Color.magenta;
            else if (t == GBlinkEdgeType.edge)
                c = Color.ORANGE;
            return c;
        }
        return Color.black;
    }
    public Paint getFillPaint(Edge e) {
        return null;
        /*
        BlinkColor c = (BlinkColor) e.getUserDatum("key");
        if (c == BlinkColor.yellow)
            return Color.yellow;
        else if (c == BlinkColor.blue)
            return Color.blue;
        else if (c == BlinkColor.red)
            return Color.red;
        else if (c == BlinkColor.green)
            return Color.green;
        else return Color.black;*/
    }
}

class MapVertexLabeler implements VertexStringer {
    public String getLabel(ArchetypeVertex v) {
        GBlinkVertex vv = (GBlinkVertex) v.getUserDatum("key");
        return ""+vv.getLabel();
    }
}
