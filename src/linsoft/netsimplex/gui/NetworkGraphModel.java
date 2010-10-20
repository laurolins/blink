package linsoft.netsimplex.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.UIManager;

import linsoft.netsimplex.Arc;
import linsoft.netsimplex.Network;
import linsoft.netsimplex.Node;

import org.jgraph.JGraph;
import org.jgraph.graph.ConnectionSet;
import org.jgraph.graph.DefaultEdge;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.DefaultGraphModel;
import org.jgraph.graph.DefaultPort;
import org.jgraph.graph.GraphConstants;
import org.jgraph.layout.LayoutAlgorithm;
import org.jgraph.layout.LayoutController;
import org.jgraph.layout.LayoutDialog;

public class NetworkGraphModel extends DefaultGraphModel {
    Network _network;
    Map _mapNode2Cell;
    public NetworkGraphModel(Network n) {
        super();
        _network = n;

        //
        _mapNode2Cell = new Hashtable();
        Map attributes = new Hashtable();
        ConnectionSet cs = new ConnectionSet();

        // array of cells
        double W = 1000;
        double H = 800;
        double w = 120;
        double h = 30;
        double points[] = {
            0, 0,
            W-w, 0,
            (W-w)/2.0, H/4,
            (W-w)/4.0, (H - h) / 2,
            3*(W-w)/4.0, (H - h) / 2,
            (W-w)/2.0, 3*H/4,
            0,H-h,
            W-w,H-h,
            (W-w)/2,(H-h)/2
        };

        int k = 0;
        ArrayList listObjs = new ArrayList();
        for (int i=0;i<_network.getNumberOfNodes();i++) {
            Node node_i = _network.getNode(i);
            DefaultGraphCell cell = new DefaultGraphCell(node_i.getLabel());
            Map cellAttributes;

            Color cor = (node_i == _network.getRoot() ? Color.blue : Color.red);
            if (2 * i < points.length) {
                cellAttributes = createBounds( (int) points[2 * i], (int) points[2 * i + 1], (int) w, (int) h, cor);
            }
            else {
                cellAttributes = createBounds( (int)(Math.random() * 1000),(int)(Math.random() * 800), (int) w, (int) h, cor);
            }
            attributes.put(cell,cellAttributes);
            cell.add(new DefaultPort());
            listObjs.add(cell);
            _mapNode2Cell.put(_network.getNode(i),cell);
        }

        // non tree arc style
        Map nonTreeArcStyle = GraphConstants.createMap();
        GraphConstants.setLineBegin(nonTreeArcStyle,GraphConstants.ARROW_TECHNICAL);
        GraphConstants.setBeginFill(nonTreeArcStyle, true);
        GraphConstants.setBeginSize(nonTreeArcStyle, 10);
        GraphConstants.setForeground(nonTreeArcStyle,new Color(0,0,100));
        GraphConstants.setFont(nonTreeArcStyle,GraphConstants.defaultFont.deriveFont(Font.PLAIN,10));

        // tree arc style
        Map treeArcStyle = GraphConstants.createMap();
        GraphConstants.setLineBegin(treeArcStyle,GraphConstants.ARROW_TECHNICAL);
        GraphConstants.setLineWidth(treeArcStyle,2f);
        GraphConstants.setLineColor(treeArcStyle,Color.MAGENTA);
        GraphConstants.setBeginFill(treeArcStyle, true);
        GraphConstants.setBeginSize(treeArcStyle, 10);
        GraphConstants.setForeground(treeArcStyle,Color.black);
        GraphConstants.setFont(treeArcStyle,GraphConstants.defaultFont.deriveFont(Font.PLAIN,10));


        //
        for (int i=0;i<_network.getNumberOfArcs();i++) {
            Arc a = _network.getArc(i);
            // if (a.isNonTreeArc())
            //     continue;
            DefaultGraphCell tail = (DefaultGraphCell) _mapNode2Cell.get(a.get_tail());
            DefaultGraphCell head = (DefaultGraphCell) _mapNode2Cell.get(a.get_head());
            DefaultEdge edge = new DefaultEdge(a.getLabel());
            cs.connect(edge, head.getChildAt(0), tail.getChildAt(0)); // a cabeça da seta é no source (parece um BUG em jGraph)
            if (a.isNonTreeArc())
                attributes.put(edge, nonTreeArcStyle);
            else
                attributes.put(edge, treeArcStyle);
            listObjs.add(edge);
        }

        // filtrar as arestas
        this.insert(listObjs.toArray(), attributes, cs, null, null);
    }

    /**
     * Returns an attributeMap for the specified position and color.
     */
    private static Map createBounds(int x, int y, int w, int h, Color c) {
        Map map = GraphConstants.createMap();
        GraphConstants.setBounds(map, new Rectangle(x, y, w, h));
        GraphConstants.setBorder(map, BorderFactory.createRaisedBevelBorder());
        GraphConstants.setBackground(map, c.darker());
        GraphConstants.setForeground(map, Color.white);
        GraphConstants.setFont(map, GraphConstants.defaultFont.deriveFont(Font.PLAIN, 9));
        GraphConstants.setOpaque(map, true);
        return map;
    }

    private Node rootNode;
    public void align() {

        // int width = 2500;
        int width = 1000;

        //
        Map mapDepth2Nodes = new Hashtable();
        Node root = _network.getRoot();
        rootNode = root;
        this.dfs(root,mapDepth2Nodes);

        //
        double dy = 100;
        double y0 = dy / 2.0;

        //
        int nodesAdjusted = 0;
        for (int i=0;i<_network.getNumberOfNodes() && nodesAdjusted < _network.getNumberOfNodes();i++) {

            // get the list
            ArrayList list = (ArrayList) mapDepth2Nodes.get(new Integer(i));

            //
            if (list == null) {
                System.out.println("Erro pois a profundidade "+i+" nao tem ninguem");
                continue;
            }

            // zero-sized list
            if (list.size() == 0)
                continue;

            //
            double dx = width / list.size();
            double x0 = dx / 2.0;

            double boxW = 95;
            double boxH = 25;

            for (int j=0;j<list.size();j++) {
                DefaultGraphCell cell = (DefaultGraphCell) _mapNode2Cell.get(list.get(j));
                Map map = this.getAttributes(cell);
                GraphConstants.setBounds(map, new Rectangle((int)(x0 + j * dx - boxW / 2.0), (int) (y0 + i * dy - boxH / 2.0), (int)boxW, (int)boxH));
                nodesAdjusted++;
            }
        }
    }

    private void dfs(Node n, Map mapDepth2Cells) {
        Integer index = new Integer(n.get_depth());
        ArrayList list = (ArrayList) mapDepth2Cells.get(index);
        if (list == null) {
            list = new ArrayList();
            mapDepth2Cells.put(index,list);
        }
        list.add(n);
        if (n.get_sucessor() != rootNode)
            dfs(n.get_sucessor(),mapDepth2Cells);
    }

    public static void mainOld(String[] args) throws Exception {
        // final Network network = new Network("resources/ex4.8.net");
        // final Network network = new Network("resources/ex4.9.net");
        // final Network network = new Network("resources/mysample.net");
        // final Network network = new Network("resources/gte_bad.1160");
        // final Network network = new Network("resources/gte_bad.469010");
        // final Network network = new Network("resources/stndrd1.net");
        // final Network network = Network.getDefaultNetwork();
        // final Network network = Network.getDefaultNetwork();
        final Network network = new Network("resources/big6.net");
        // final Network network = new Network("resources/xxx.net");

        // time
        long t = System.currentTimeMillis();

        network.initNetworkForAlgorithm();
        while (true) {
            try {
                // System.out.println("It:\t"+network.getIteracao()+"\tValue:\t"+network.getNetworkValue());
                System.out.println("It:\t"+network.getIteracao());
                if (network.nextIteration()) {
                    break;
                }
            }
            catch (Exception ex) {
                ex.printStackTrace();
                break;
            }
        }

        // time
        t = System.currentTimeMillis() - t;
        System.out.println("Solucao Otima");
        System.out.println("Iteracoes: "+network.getIteracao());
        System.out.println("Value: "+network.getNetworkValue());
        System.out.println("Tempo: "+t+" msec");
    }

    public static void main(String[] args) throws Exception {
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            Locale.setDefault(new Locale("pt", "BR"));
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        //
        java.util.Observable x;

        // final Network network = new Network("resources/ex4.8.net");
        // final Network network = new Network("resources/ex4.9.net");
        // final Network network = new Network("resources/mysample.net");
        // final Network network = new Network("resources/gte_bad.1160");
        // final Network network = new Network("resources/gte_bad.469010");
        // final Network network = new Network("resources/stndrd1.net");
        // final Network network = Network.getDefaultNetwork();
        // final Network network = Network.getDefaultNetwork();
        // final Network network = new Network("resources/big6.net");
        // final Network network = new Network("resources/bug.net");



        final Network network = new Network("res/bug.net");

        execute(network);
    }

    public static void execute(Network network) throws Exception {
        //
        java.util.Observable x;

        // final Network network = new Network("res/bug.net");
        network.initNetworkForAlgorithm();

        // add step
        _stepList = new ArrayList();
        Step firstStep = new Step(new NetworkGraphModel(network),network.getIteracao(),network.getNetworkValue(),network.getYSum());

        _currentStep = firstStep;
        while (true) {
            try {
                System.out.println("It:\t"+network.getIteracao());
                if (!network.nextIteration()) {
                    Step newStep = new Step(new NetworkGraphModel(network), network.getIteracao(), network.getNetworkValue(), network.getYSum());
                    _currentStep.setNextStep(newStep);
                    newStep.setPreviousStep(_currentStep);
                    _currentStep = newStep;
                }
                else {
                    break;
                }
            }
            catch (Exception ex) {
                ex.printStackTrace();
                break;
            }
        }
        _currentStep.setNextStep(firstStep);
        firstStep.setPreviousStep(_currentStep);


        // create view on a scroll pane
        final JGraph jGraph = new JGraph(_currentStep.getModel());
        JScrollPane jGraphScrollPane = new JScrollPane(jGraph);

        // create labels and buttons
        final JLabel lblIteracao = new JLabel("Iteração: "+_currentStep.getIndex());
        final JLabel lblNetworkValue = new JLabel("Value: "+_currentStep.getValue());
        final JLabel lblYSum = new JLabel("Y Sum: "+_currentStep.getYSum());
        JButton btnLayout = new JButton("Layout");
        JButton btnPrevious = new JButton("Previous Iteration");
        JButton btnNext = new JButton("Next Iteration");

        // create bottom panel
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new FlowLayout(FlowLayout.LEADING,5,1));
        bottomPanel.add(btnLayout);
        bottomPanel.add(new JLabel("        "));
        bottomPanel.add(btnPrevious);
        bottomPanel.add(btnNext);
        bottomPanel.add(new JLabel("        "));
        bottomPanel.add(lblIteracao);
        bottomPanel.add(new JLabel("    "));
        bottomPanel.add(lblNetworkValue);
        bottomPanel.add(new JLabel("    "));
        bottomPanel.add(lblYSum);


        // main panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(jGraphScrollPane,BorderLayout.CENTER);
        mainPanel.add(bottomPanel,BorderLayout.SOUTH);

        // create main frame
        final JFrame mainFrame = new JFrame();
        mainFrame.setTitle("Network Simplex");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setContentPane(mainPanel);
        mainFrame.setBounds(0,0,500,500);

        // btn next
        btnPrevious.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                _currentStep = _currentStep.getPreviousStep();

                // iterate
                jGraph.setModel(_currentStep.getModel());
                lblIteracao.setText("Iteração: "+_currentStep.getIndex());
                lblNetworkValue.setText("Value: "+_currentStep.getValue());
                lblYSum.setText("Y Sum: "+_currentStep.getYSum());
            }
        });

        // btn next
        btnNext.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                _currentStep = _currentStep.getNextStep();

                // iterate
                jGraph.setModel(_currentStep.getModel());
                lblIteracao.setText("Iteração: "+_currentStep.getIndex());
                lblNetworkValue.setText("Value: "+_currentStep.getValue());
                lblYSum.setText("Y Sum: "+_currentStep.getYSum());
            }
        });

        // btn layout action listener
        btnLayout.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                Frame f = JOptionPane.getFrameForComponent(mainFrame);
                final LayoutDialog dlg = new LayoutDialog(f);
                dlg.show();

                if (dlg.isCanceled())
                    return;

                final LayoutController controller = dlg.getSelectedLayoutController();
                if (controller == null)
                    return;

                Thread t = new Thread("Layout Algorithm " + controller.toString()) {
                    public void run() {
                        LayoutAlgorithm algorithm = controller.getLayoutAlgorithm();
                        algorithm.perform(
                            jGraph,
                            dlg.isApplyLayoutToAll(),
                            controller.getConfiguration());
                    }
                };
                t.start();
            }
        });

        // set main frame visible
        mainFrame.setVisible(true);

    }









    private static Step _currentStep;
    private static ArrayList _stepList = new ArrayList();
    public static void solveAndShowSteps(Network network) {

        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
            Locale.setDefault(new Locale("pt", "BR"));
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        //
        java.util.Observable x;

        // time
        long t = System.currentTimeMillis();

        // init
        network.initNetworkForAlgorithm();

        // add step
        _stepList = new ArrayList();
        Step firstStep = new Step(new NetworkGraphModel(network),network.getIteracao(),network.getNetworkValue(),network.getYSum());

        _currentStep = firstStep;
        while (true) {
            try {
                // System.out.println("It:\t"+network.getIteracao()+"\tValue:\t"+network.getNetworkValue());
                System.out.println("It:\t"+network.getIteracao());

                //
                if (!network.nextIteration()) {
                    Step newStep = new Step(new NetworkGraphModel(network), network.getIteracao(), network.getNetworkValue(), network.getYSum());
                    _currentStep.setNextStep(newStep);
                    newStep.setPreviousStep(_currentStep);
                    _currentStep = newStep;
                }
                else {
                    break;
                }
            }
            catch (Exception ex) {
                ex.printStackTrace();
                break;
            }
        }
        _currentStep.setNextStep(firstStep);
        firstStep.setPreviousStep(_currentStep);

        // time
        t = System.currentTimeMillis() - t;
        System.out.println("Solucao Otima");
        System.out.println("Iteracoes: "+network.getIteracao());
        System.out.println("Value: "+network.getNetworkValue());
        System.out.println("Tempo: "+t+" msec");

        // create view on a scroll pane
        final JGraph jGraph = new JGraph(_currentStep.getModel());
        JScrollPane jGraphScrollPane = new JScrollPane(jGraph);

        // create labels and buttons
        final JLabel lblIteracao = new JLabel("Iteração: "+_currentStep.getIndex());
        final JLabel lblNetworkValue = new JLabel("Value: "+_currentStep.getValue());
        final JLabel lblYSum = new JLabel("Y Sum: "+_currentStep.getYSum());
        JButton btnLayout = new JButton("Layout");
        JButton btnPrevious = new JButton("Previous Iteration");
        JButton btnNext = new JButton("Next Iteration");

        // create bottom panel
        JPanel bottomPanel = new JPanel();
        bottomPanel.setLayout(new FlowLayout(FlowLayout.LEADING,5,1));
        bottomPanel.add(btnLayout);
        bottomPanel.add(new JLabel("        "));
        bottomPanel.add(btnPrevious);
        bottomPanel.add(btnNext);
        bottomPanel.add(new JLabel("        "));
        bottomPanel.add(lblIteracao);
        bottomPanel.add(new JLabel("    "));
        bottomPanel.add(lblNetworkValue);
        bottomPanel.add(new JLabel("    "));
        bottomPanel.add(lblYSum);


        // main panel
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(jGraphScrollPane,BorderLayout.CENTER);
        mainPanel.add(bottomPanel,BorderLayout.SOUTH);

        // create main frame
        final JFrame mainFrame = new JFrame();
        mainFrame.setTitle("Network Simplex");
        mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        mainFrame.setContentPane(mainPanel);
        mainFrame.setBounds(0,0,500,500);

        // btn next
        btnPrevious.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                _currentStep = _currentStep.getPreviousStep();

                // iterate
                jGraph.setModel(_currentStep.getModel());
                lblIteracao.setText("Iteração: "+_currentStep.getIndex());
                lblNetworkValue.setText("Value: "+_currentStep.getValue());
                lblYSum.setText("Y Sum: "+_currentStep.getYSum());
            }
        });

        // btn next
        btnNext.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                _currentStep = _currentStep.getNextStep();

                // iterate
                jGraph.setModel(_currentStep.getModel());
                lblIteracao.setText("Iteração: "+_currentStep.getIndex());
                lblNetworkValue.setText("Value: "+_currentStep.getValue());
                lblYSum.setText("Y Sum: "+_currentStep.getYSum());
            }
        });

        // btn layout action listener
        btnLayout.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent actionEvent) {
                Frame f = JOptionPane.getFrameForComponent(mainFrame);
                final LayoutDialog dlg = new LayoutDialog(f);
                dlg.show();

                if (dlg.isCanceled())
                    return;

                final LayoutController controller = dlg.getSelectedLayoutController();
                if (controller == null)
                    return;

                Thread t = new Thread("Layout Algorithm " + controller.toString()) {
                    public void run() {
                        LayoutAlgorithm algorithm = controller.getLayoutAlgorithm();
                        algorithm.perform(
                            jGraph,
                            dlg.isApplyLayoutToAll(),
                            controller.getConfiguration());
                    }
                };
                t.start();
            }
        });

        // set main frame visible
        mainFrame.setVisible(true);
    }





    public static void showFrame(Network network, String title) {
        NetworkGraphModel model = new NetworkGraphModel(network);
        JFrame f = new JFrame(title);
        JGraph j = new JGraph();
        model.align();
        j.setModel(model);
        f.setContentPane(j);
        f.setBounds(0,0,300,300);
        f.setVisible(true);
    }
}

/**
 * Keep the record of the algorithm.
 */
class Step {
    private Step _next;
    private Step _previous;
    private NetworkGraphModel _gModel;
    private int _index;
    private long _value;
    private long _ysum;
    public Step(NetworkGraphModel gModel, int index, long value, long ysum) {
        _gModel = gModel;
        _gModel.align();
        _index = index;
        _value = value;
        _ysum = ysum;
    }
    public NetworkGraphModel getModel() { return _gModel; }
    public int getIndex() { return _index; }
    public long getValue() { return _value; }
    public long getYSum() { return _ysum; }
    public Step getPreviousStep() { return _previous; }
    public Step getNextStep() { return _next; }
    public void setPreviousStep(Step previous) { _previous = previous; }
    public void setNextStep(Step next) { _next = next; }
}
