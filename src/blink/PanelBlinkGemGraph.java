package blink;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.apache.commons.collections.Predicate;

import edu.uci.ics.jung.graph.ArchetypeEdge;
import edu.uci.ics.jung.graph.ArchetypeVertex;
import edu.uci.ics.jung.graph.Edge;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.graph.decorators.ConstantEdgePaintFunction;
import edu.uci.ics.jung.graph.decorators.ConstantEdgeStrokeFunction;
import edu.uci.ics.jung.graph.decorators.ConstantVertexAspectRatioFunction;
import edu.uci.ics.jung.graph.decorators.ConstantVertexSizeFunction;
import edu.uci.ics.jung.graph.decorators.EdgeShape;
import edu.uci.ics.jung.graph.decorators.EdgeStringer;
import edu.uci.ics.jung.graph.decorators.VertexPaintFunction;
import edu.uci.ics.jung.graph.decorators.VertexShapeFunction;
import edu.uci.ics.jung.graph.decorators.VertexStringer;
import edu.uci.ics.jung.visualization.FRLayout;
import edu.uci.ics.jung.visualization.GraphMouseListener;
import edu.uci.ics.jung.visualization.ISOMLayout;
import edu.uci.ics.jung.visualization.Layout;
import edu.uci.ics.jung.visualization.PluggableRenderer;
import edu.uci.ics.jung.visualization.SpringLayout;
import edu.uci.ics.jung.visualization.VertexShapeFactory;
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
public class PanelBlinkGemGraph extends JPanel {
    private static int WIDTH = 5000;
    private static int HEIGHT = 5000;

    Graph _G;
    Lens _lenses;
    VisualizationViewer _view;
    PluggableRenderer _pluggableRenderer;
    JComboBox _cbLayoutOffColor = new JComboBox(new Object[] {"yellow","blue","red","green"});
    JTextField _display = new JTextField("");

    Gem _attractor;


    public PanelBlinkGemGraph(BlinkGemGraph bge) {
        _G = bge.getGraph();



        this.buildUI();
    }

    public Gem getAttractor() {
        return _attractor.copy();
    }

    private void buildUI() {
        // setup renderer
        PluggableRenderer pr = new PluggableRenderer();

        // pr.setEdgePaintFunction(new ConstantEdgePaintFunction(Color.cyan,null));
        pr.setVertexPaintFunction(new VertexPaint());
        pr.setVertexStringer(new VertexLabeler());
        pr.setEdgeStringer(new EdgeLabeler());
        pr.setVertexLabelCentering(true);
        pr.setEdgeStrokeFunction(new ConstantEdgeStrokeFunction(3.0f));
        pr.setEdgePaintFunction(new ConstantEdgePaintFunction(Color.darkGray,null));
        pr.setEdgeIncludePredicate(new EdgeIncludePredicate());
        pr.setVertexIncludePredicate(new VertexIncludePredicate());
        pr.setEdgeShapeFunction(new EdgeShape.Line());

        VertexShapeFunction vvv = new VertexShapeFunction() {
            VertexShapeFactory _vsf = new VertexShapeFactory(
                new ConstantVertexSizeFunction(50),
                new ConstantVertexAspectRatioFunction(1));
            public Shape getShape(Vertex v) {
                return _vsf.getEllipse(v);
            }
        };
        pr.setVertexShapeFunction(vvv);



        _pluggableRenderer = pr;

        // setup viewer
        Layout layout = new KKLayout(_G);
        layout.initialize(new Dimension(WIDTH,HEIGHT));
        _view = new VisualizationViewer(layout,pr);
        _lenses = new Lens(_view);
        _view.addGraphMouseListener(new TestGraphMouseListener());
        _view.addMouseMotionListener(new MouseMotionListener() {
            public void mouseDragged(MouseEvent e) {
            }
            public void mouseMoved(MouseEvent e) {
            }
        });

        //_vv = new VisualizationViewer(_layout, pr);
        //_view.setBackground(Color.black);
        _view.setBackground(Color.white);


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
        bottomPanel.add(new JButton(new WriteEPS()));
        bottomPanel.add(new JButton(new SavePDF(this, _view)));

        // layout panel
        this.setLayout(new BorderLayout());
        _display.setBackground(Color.black);
        _display.setForeground(Color.yellow);
        this.add(_display,BorderLayout.NORTH);
        this.add(new JScrollPane(_view),BorderLayout.CENTER);
        this.add(bottomPanel,BorderLayout.SOUTH);
    }

    class WriteEPS extends AbstractAction {
        public WriteEPS() {
            super("EPS");
        }
        public void actionPerformed(ActionEvent e) {
            try {
                writeFile();
            } catch (IOException ex) {
            }
        }
    };

    public void writeFile() throws IOException {

        Layout layout = _view.getModel().getGraphLayout();
        //layout.getLocation();

        double bounds[] = null;
        for (Vertex v: (Set<Vertex>) _G.getVertices()) {
            Point2D location = layout.getLocation(v);
            if (bounds == null) {
                bounds = new double[] {location.getX(),location.getY(),location.getX(),location.getY() };
            }
            else {
                if (location.getX() < bounds[0]) bounds[0] = location.getX();
                if (location.getY() < bounds[1]) bounds[1] = location.getY();
                if (location.getX() > bounds[2]) bounds[2] = location.getX();
                if (location.getY() > bounds[3]) bounds[3] = location.getY();
            }
        }

        double conversion = 72.0/25.4;
        double w = 100 * conversion;
        double h = 100 * conversion;
        double m = 5 * conversion;

        AffineTransform T = new AffineTransform();
        T.translate(m,m);
        T.scale((w-2*m)/(bounds[2]-bounds[0]),(h-2*m)/(bounds[3]-bounds[1]));
        T.translate(-bounds[0],-bounds[1]);

        PrintWriter pw = new PrintWriter(new FileWriter("teste.eps"));
        EPSLibrary.printHeader(pw,w,h);

        pw.println("1 setlinewidth");

        for (Edge e: (Set<Edge>) _G.getEdges()) {
            Point2D p1 = (Point2D) layout.getLocation((Vertex)e.getEndpoints().getFirst()).clone();
            Point2D p2 = (Point2D) layout.getLocation((Vertex)e.getEndpoints().getSecond()).clone();
            T.transform(p1,p1);
            T.transform(p2,p2);
            pw.println("gsave");
            pw.println("newpath");
            pw.println(String.format("%.4f %.4f moveto", p1.getX(), p1.getY()));
            pw.println(String.format("%.4f %.4f lineto", p2.getX() , p2.getY()));
            pw.println("0.8 setgray");
            pw.println("stroke");
            pw.println("grestore");
        }

        for (Vertex v: (Set<Vertex>) _G.getVertices()) {
            Point2D p1 = (Point2D) layout.getLocation(v).clone();
            T.transform(p1,p1);
            double r = 1 * conversion;
            pw.println("gsave");
            pw.println("newpath");
            pw.println(String.format("%.4f %.4f %.4f 0 360 arc", p1.getX(), p1.getY(), r));
            pw.println("closepath");
            pw.println("gsave");
            if (v.getUserDatum("key") instanceof BlinkEntry) {
                pw.println("1 0 0 setrgbcolor");
                pw.println("fill");
            }
            else {
                pw.println("1 1 0 setrgbcolor");
                pw.println("fill");
            }
            pw.println("grestore");
            pw.println("0.5 setlinewidth");
            pw.println("0 0 0 setrgbcolor");
            pw.println("stroke");
            pw.println("grestore");
        }
        EPSLibrary.printFooter(pw);
        pw.close();
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
                l.initialize(new Dimension(WIDTH,HEIGHT));
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
        //        new PRGTutteLayout(_G,GemColor.values()[_cbLayoutOffColor.getSelectedIndex()])));
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
    class TestGraphMouseListener implements GraphMouseListener {
        public void graphClicked(Vertex v, MouseEvent me) {
            if (me.getClickCount() == 1) {
                Object o = v.getUserDatum("key");
                if (o instanceof BlinkEntry) {
                    BlinkEntry be = (BlinkEntry) o;
                    String message = "id: "+be.get_id()+" code: "+be.getBlink().getBlinkWord().toString()+" edge: "+be.get_numEdges();
                    System.out.println(message);
                    _display.setText(message);
                }
                else if (o instanceof GemEntry) {
                    GemEntry ge = (GemEntry) o;
                    String message = "id: "+ge.getId()+" code: "+ge.getGem().getCurrentLabelling().getLettersString("")+" v: "+ge.getNumVertices();
                    System.out.println(message);
                    _display.setText(message);
                }
            }
            if (me.getClickCount() > 1) {
                Object o = v.getUserDatum("key");
                if (o instanceof BlinkEntry) {
                    BlinkEntry be = (BlinkEntry) o;
                    ArrayList<GBlink> list = new ArrayList<GBlink>();
                    list.add(be.getBlink());
                    JFrame f = new JFrame("Blink "+be.get_id());
                    linsoft.gui.util.Library.resizeAndCenterWindow(f, 500, 500);
                    if (me.getButton() == MouseEvent.BUTTON1) {
                        f.setContentPane(new PanelDrawBlinks(list, 1, 1, 2));
                    }
                    else {
                        f.setContentPane(new PanelDrawLinks(list, 1, 1, 2, 2));
                    }
                    f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    f.setVisible(true);
                }
                else if (o instanceof GemEntry) {
                    GemEntry ge = (GemEntry) o;
                    System.out.println("id: "+ge.getId()+" code: "+ge.getGem().getCurrentLabelling().getLettersString(""));
                    JFrame f = new JFrame("Gem "+ge.getId());
                    linsoft.gui.util.Library.resizeAndCenterWindow(f, 640, 480);
                    f.setContentPane(new PanelGemViewer(ge.getGem()));
                    f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
                    f.setVisible(true);
                }
            }
            // System.err.println("Vertex "+v+" was clicked at ("+me.getX()+","+me.getY()+")");
        }

        public void graphPressed(Vertex v, MouseEvent me) {
            // System.err.println("Vertex "+v+" was pressed at ("+me.getX()+","+me.getY()+")");
        }

        public void graphReleased(Vertex v, MouseEvent me) {
            // System.err.println("Vertex "+v+" was released at ("+me.getX()+","+me.getY()+")");
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

    class EdgeIncludePredicate implements Predicate {
        public boolean evaluate(Object o) {
            return true;
        }
    }

    class VertexIncludePredicate implements Predicate {
        public boolean evaluate(Object o) {
            return true;
            /*Vertex v = (Vertex) o;
            Boolean b = (Boolean) v.getUserDatum("onPath");
            if (b != null && b.booleanValue())
                return true;
            return false;*/
        }
    }

    static class VertexLabeler implements VertexStringer {
        public String getLabel(ArchetypeVertex v) {
            Object o = v.getUserDatum("key");
            if (o instanceof GemEntry) {
                return "g"+((GemEntry) o).getId();
            }
            else if (o instanceof BlinkEntry) {
                return "U["+((BlinkEntry) o).get_id()+"]";
            }
            return "";
        }
    }

    static class EdgeLabeler implements EdgeStringer {
        public String getLabel(ArchetypeEdge e) {
            //Object o = e.getUserDatum("key");
            return "";
        }
    }

    static class VertexPaint implements VertexPaintFunction {
        public Paint getFillPaint(Vertex v) {
            Object o = v.getUserDatum("key");
            if (o instanceof GemEntry) {
                return Color.yellow;
            } else if (o instanceof BlinkEntry) {
                return Color.red;
            }
            else return Color.gray;
        }
        public Paint getDrawPaint(Vertex v) {
            return Color.black;
        }
    }

}
