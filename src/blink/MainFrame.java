package blink;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.beans.IntrospectionException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import linsoft.gui.bean.BeanTable;
import linsoft.gui.bean.MultipleBeanTableModel;
import linsoft.gui.util.ViewEspera;
import edu.uci.ics.jung.graph.Edge;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Vertex;
import edu.uci.ics.jung.graph.impl.DirectedSparseEdge;
import edu.uci.ics.jung.graph.impl.SparseGraph;
import edu.uci.ics.jung.graph.impl.SparseVertex;
import edu.uci.ics.jung.utils.UserData;

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
public class MainFrame extends JFrame {

    private JSplitPane _leftSplitPanel = new JSplitPane();
    private JTextField _tfSearchField = new JTextField();
    private BeanTable _tableBlnks = new BeanTable();
    private BeanTable _tableHGQI = new BeanTable();
    private MultipleBeanTableModel _modelBlinks;
    private MultipleBeanTableModel _modelClassesHGQI;
    private TitledScrollPane _tspBlinks;
    private TitledScrollPane _tspClasses;
    private JTextArea _taBlinkDeails = new JTextArea();
    private JButton _btnSearch = new JButton();
    private JButton _btnUpdateGem = new JButton();
    private JSplitPane _splitPane = new JSplitPane();
    private JSplitPane _splitPaneTables = new JSplitPane();
    private JLabel _bottomLabel = new JLabel();

    private DrawPanelMultipleMaps _drawPanel;

    public JPanel getPanelDB() throws IntrospectionException, SQLException, IOException, ClassNotFoundException {
        JPanel panelDB = new JPanel();


        JPanel panelSearchAndResult = new JPanel();
        panelSearchAndResult.setLayout(new GridBagLayout());

        _tfSearchField.setPreferredSize(new Dimension(150,21));
        panelSearchAndResult.add(_tfSearchField,
                                 new GridBagConstraints(0, 0, 1, 1, 1, 0,
                                                        GridBagConstraints.CENTER,
                                                        GridBagConstraints.HORIZONTAL,
                                                        new Insets(2, 2, 2, 2), 0, 0));

        _btnSearch.setText("Search");
        panelSearchAndResult.add(_btnSearch,
                                 new GridBagConstraints(1, 0, 1, 1, 0, 0,
                                                        GridBagConstraints.CENTER,
                                                        GridBagConstraints.NONE,
                                                        new Insets(2, 2, 2, 2), 0, 0));

        _btnUpdateGem.setText("Upd.Gem");
        panelSearchAndResult.add(_btnUpdateGem,
                                 new GridBagConstraints(2, 0, 1, 1, 0, 0,
                                                        GridBagConstraints.CENTER,
                                                        GridBagConstraints.NONE,
                                                        new Insets(2, 2, 2, 2), 0, 0));

        _tfSearchField.setText("1000");

        _modelBlinks = new MultipleBeanTableModel(BlinkEntry.class);
        _modelBlinks.setVisiblePropertySequence(new String[] {"_id","_numEdges","_hg","_qi","_mingem","_gem","_mapCode"});
        //_modelBlinks.addObject(App.getRepositorio().getBlinksByIDInterval(62359,62359+128));
        //_modelBlinks.addObject(App.getRepositorio().getBlinks(1,3));

        _tableBlnks = new BeanTable();
        _tableBlnks.setModel(_modelBlinks);
        _tableBlnks.registerColumnSorter();
        _tableBlnks.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
            public void valueChanged(ListSelectionEvent e) {
                try {
                    changeSelection();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                } catch (IOException ex) {
                    ex.printStackTrace();
                } catch (ClassNotFoundException ex) {
                    ex.printStackTrace();
                }
            }
        });

        _modelClassesHGQI = new MultipleBeanTableModel(ClassHGNormQI.class);
        _modelClassesHGQI.setVisiblePropertySequence(new String[] {"_hg","_qi","_numElements","_loaded","_monochromatic"});
        //_modelBlinks.addObject(App.getRepositorio().getBlinksByIDInterval(62359,62359+128));
        _modelClassesHGQI.addObject(App.getRepositorio().getHGNormQIClasses(App.MAX_EDGES));

        _tableHGQI = new BeanTable();
        _tableHGQI.setModel(_modelClassesHGQI);
        _tableHGQI.registerColumnSorter();
        _tableHGQI.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
            public void valueChanged(ListSelectionEvent e) {
                changeSelectionClass();
            }
        });
        _tspClasses = new TitledScrollPane("Classes "+_modelClassesHGQI.getRowCount(),_tableHGQI);
        _tspClasses.setPreferredSize(new Dimension(200,100));

        _tspBlinks = new TitledScrollPane("Blinks "+_modelBlinks.getRowCount(),_tableBlnks);
        _tspBlinks.setPreferredSize(new Dimension(200,100));

        _splitPaneTables = new JSplitPane(JSplitPane.VERTICAL_SPLIT,_tspClasses,_tspBlinks);
        panelSearchAndResult.add(_splitPaneTables,
                                 new GridBagConstraints(0, 1, 4, 1, 1, 1,
                                                        GridBagConstraints.CENTER,
                                                        GridBagConstraints.BOTH,
                                                        new Insets(2, 2, 2, 2), 0, 0));

        _taBlinkDeails.setEditable(false);
        _taBlinkDeails.setFont(new Font("Courier New",Font.PLAIN,11));

        _leftSplitPanel.setOrientation(JSplitPane.VERTICAL_SPLIT);
        _leftSplitPanel.setTopComponent(panelSearchAndResult);
        _leftSplitPanel.setBottomComponent(new JScrollPane(_taBlinkDeails));

        _splitPane.setLeftComponent(_leftSplitPanel);
        _splitPane.setDividerLocation(380);

        _bottomLabel.setPreferredSize(new Dimension(100,25));
        _bottomLabel.setFont(new Font("Courier New",Font.PLAIN,14));
        _bottomLabel.setHorizontalAlignment(JLabel.CENTER);

        //
        panelDB.setLayout(new BorderLayout());
        panelDB.add(_splitPane,BorderLayout.CENTER);
        panelDB.add(_bottomLabel,BorderLayout.SOUTH);

        _btnSearch.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                homologyGroupOfSelection();
            }
        });

        _btnUpdateGem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    updateGem2();
                } catch (ClassNotFoundException ex) {
                    ex.printStackTrace();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });


        return panelDB;
    }

    public static JFrame MAIN_FRAME = null;

    public MainFrame() throws IntrospectionException, SQLException, IOException, ClassNotFoundException {
        super("Blinks v0.01 - 29/03/2006");
        JTabbedPane tabPane = new JTabbedPane();
        tabPane.add(getPanelDB(),"Catalog");
        tabPane.add(new PanelGems(),"Gems");
        tabPane.add(new PanelGemPaths(),"Gem Paths");
        tabPane.add(new PanelTests(),"Experiment");
        tabPane.add(new PanelNewString(),"Strings");
        try {
            tabPane.add(new PanelGemEquivalence(), "Gem Paths Graph");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        this.getContentPane().add(tabPane);

        MAIN_FRAME = this;
    }

    private void homologyGroupOfSelection() {
        int[] rows = _tableBlnks.getSelectedRows();
        _taBlinkDeails.setText("");
        for (int r: rows) {
            BlinkEntry b = (BlinkEntry) _modelBlinks.getObject(r);
            _taBlinkDeails.append(b.get_mapCode()+" "+b.get_colors()+"\n");
            _taBlinkDeails.append("Homology Group: "+(new GBlink(b)).homologyGroupFromGem().toString()+"\n\n");
        }
    }

    private void changeSelection() throws ClassNotFoundException, IOException, SQLException {
        Vector vs = _modelBlinks.getObjects(_tableBlnks.getSelectedRows());
        int k = (int) Math.ceil(Math.sqrt(vs.size()));

        if (k == 0)
            return;

        ArrayList<MapD> list = new ArrayList<MapD>();
        int count=0;
        for (Object o: vs) {
            BlinkEntry be = (BlinkEntry) o;
            count++;
            list.add(new MapD(new GBlink(be)));
            if (count == 121)
                break;
        }

        _drawPanel = new DrawPanelMultipleMaps(list,k,k);
        _drawPanel.addMouseMotionListener(new MouseMotionListener() {
            public void mouseDragged(MouseEvent e) {
            }
            public void mouseMoved(MouseEvent e) {
                int x = e.getX();
                int y = e.getY();
                MapD mapD = _drawPanel.getMapFromPosition(x,y);
                if (mapD != null) {
                    GBlink b = mapD.getBlink();
                    BlinkEntry be = b.getBlinkEntry();
                    if (be != null) {
                        _bottomLabel.setText(String.format("ID: %d   %s   %d   HG: %s   QI: %d",be.get_id(),be.get_mapCode(),be.get_colors(),be.get_hg(),be.get_qi()));
                    }
                }
            }
        });

        JTabbedPane tp = new JTabbedPane();
        tp.add("Coins",_drawPanel);
        for (Object o : vs) {
            BlinkEntry be = (BlinkEntry) o;
            JTabbedPane tp2 = new JTabbedPane();
            tp2.add(""+be.get_id(),new PanelBlinkViewer(be.getBlink()));
            be.loadPath();
            if (be.getPath() != null) {
                tp2.add("Path",new PanelReductionGraph((new MountGraphFromPath(be)).getGraph()));
            }
            tp.add(""+be.get_id(),tp2);
        }

        int dl = _splitPane.getDividerLocation();
        _splitPane.setRightComponent(tp);
        _splitPane.setDividerLocation(dl);

        BlinkEntry be = (BlinkEntry)_tableBlnks.getSelectedObject();
        if (be != null) {
            if (be.get_qi() >= 0) {
                QI qi = App.getRepositorio().getQI(be.get_qi());
                if (qi != null) {
                    _taBlinkDeails.setText("Quantum Invariant DB\n" + qi.toString());
                }
            } else {
                QI qi = be.getBlink().optimizedQuantumInvariant(3, 14);
                _taBlinkDeails.setText("Quantum Invariant CALCULATED\n" + qi.toString());
            }
        }

        BlinkCyclicRepresentation bcr = new GBlink((BlinkEntry)vs.get(0)).getCyclicRepresentation();
        _taBlinkDeails.setText(_taBlinkDeails.getText()+"\n"+bcr.toString());

        //
        try {
            PrintStream ps = new PrintStream(new FileOutputStream("c:/graph.txt"));
            bcr.pigale(ps);
            ps.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }


    }


    private ClassHGNormQI _classToLoad;
    private void changeSelectionClass() {
        int[] rows = _tableHGQI.getSelectedRows();
        if (rows.length == 0) {
            _modelBlinks.clear();
        }
        else {
            _modelBlinks.clear();
            for (int r: rows) {
                _classToLoad = (ClassHGNormQI) _modelClassesHGQI.getObject(r);
                ViewEspera ve = new ViewEspera(this);
                Object o = ve.doWork(new linsoft.gui.util.IWorker() {
                    public Object doWork() {
                        try {
                            _classToLoad.load();
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                            return ex;
                        }
                        return null;
                    }
                }
                , "Importando Candles Diários...", 160, 120);

                if (o != null)
                    throw new RuntimeException("Oooooooppppsss");

                _modelBlinks.addObject(_classToLoad.getBlinks());
            }

            if (rows.length > 1) {
                _tspBlinks.setTitle(String.format("Blinks. HG: *   QI: *   #: %d", _modelBlinks.getRowCount()));
            } else if (rows.length == 1) {
                _tspBlinks.setTitle(String.format("Blinks. HG: %s   QI: %s   #: %d", _classToLoad.get_hg(),
                                                  _classToLoad.getStringOfQIs(), _modelBlinks.getRowCount()));
            }

        }
    }

    public void updateGem() throws IOException, SQLException, ClassNotFoundException {
        GemRepository R = new GemRepository(App.getRepositorio().getGems());

        HashMap<BlinkEntry, GemEntry> map = new HashMap<BlinkEntry, GemEntry>();

        ArrayList<BlinkEntry> bs = new ArrayList<BlinkEntry>((Vector<BlinkEntry>)_modelBlinks.getObjects(_tableBlnks.getSelectedRows()));
        for (BlinkEntry be: bs) {
            CalculateReductionGraph crg = new CalculateReductionGraph(be.getBlink().getGem());
            Gem rep = crg.getRepresentant();
            GemEntry gEntry = R.getExistingGemEntryOrCreateNew(rep.getCurrentLabelling(),crg.getTSClassSize(),true);
            map.put(be,gEntry);
        }

        //
        ArrayList<GemEntry> list = R.getNewEntriesLists(); // get list of not persistent QIs
        App.getRepositorio().insertGems(list);
        R.clearNewEntriesList(); // new entries have been updated

        // updating biEntry
        for (BlinkEntry be : bs) {
            GemEntry ge = map.get(be);
            be.set_gem(ge.getId());
        }

        // update gems on blinks
        App.getRepositorio().updateBlinksGems(bs);
        // System.out.println(String.format("Updated QIs %d blinks in %.2f sec.", bs.size(), (System.currentTimeMillis() - t) / 1000.0));

        _modelBlinks.updateAllCells();
    }

    public void updateGem2() throws IOException, SQLException, ClassNotFoundException {
        HashMap<Long,GemEntry> mapId2Gem = App.getRepositorio().getGemsMap();
        GemRepository R = new GemRepository(App.getRepositorio().getGems());

        HashSet<BlinkEntry> changedBlinks = new HashSet<BlinkEntry>();

        HashMap<BlinkEntry, GemEntry> map = new HashMap<BlinkEntry, GemEntry>();
        ArrayList<BlinkEntry> bs = new ArrayList<BlinkEntry>((Vector<BlinkEntry>)_modelBlinks.getObjects(_tableBlnks.getSelectedRows()));
        for (BlinkEntry be: bs) {
            Gem g = be.getBlink().getGem();
            g.goToCodeLabel();
            int t = Integer.parseInt(_tfSearchField.getText());
            SearchAttractor A = new SearchAttractor(g,t);

            //
            Gem bestAttractor = A.getBestAttractorFound();

            // consultar gem anterior
            boolean changeAttractor = false;
            if (be.get_gem() != -1) {
                GemEntry ge = mapId2Gem.get(be.get_gem());
                Gem oldAttractor = new Gem(ge.getLabelling());
                if (bestAttractor.getNumVertices() < oldAttractor.getNumVertices() ||
                    (A.isBestAttractorTSClassRepresentant() &&
                     oldAttractor.compareTo(bestAttractor) > 0))  {
                    changeAttractor = true;
                }
            }
            else changeAttractor = true;

            if (changeAttractor) {
                GemEntry gEntry = R.getExistingGemEntryOrCreateNew(
                        bestAttractor.getCurrentLabelling(),
                        A.isBestAttractorTSClassRepresentant() ? A.getBestAttractorTSClassSize() : 0,true);
                map.put(be,gEntry);
                be.set_path(A.getBestPath());
                changedBlinks.add(be);
            }

        }

        //
        ArrayList<GemEntry> list = R.getNewEntriesLists(); // get list of not persistent QIs
        App.getRepositorio().insertGems(list);
        R.clearNewEntriesList(); // new entries have been updated

        // filter bs list
        for (int i=bs.size()-1;i>=0;i--) {
            if (!changedBlinks.contains(bs.get(i)))
                bs.remove(i);
        }

        // updating biEntry
        for (BlinkEntry be : bs) {
            GemEntry ge = map.get(be);
            be.set_gem(ge.getId());
        }


        // update gems on blinks
        App.getRepositorio().updateBlinksGems(bs);
        // System.out.println(String.format("Updated QIs %d blinks in %.2f sec.", bs.size(), (System.currentTimeMillis() - t) / 1000.0));

        _modelBlinks.updateAllCells();
    }

    public static void main(String[] args) throws SQLException, IntrospectionException, ClassNotFoundException,
            IOException {
        MainFrame f = new MainFrame();
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        linsoft.gui.util.Library.resizeAndCenterWindow(f,1000,800);
        f.setSize(new Dimension(1000,800));
        // f.setContentPane(new DrawPanel(md));
        f.setVisible(true);
        // desenhar o mapa
    }

}


class TitledScrollPane extends JScrollPane {
    TitledBorder _tb;
    public TitledScrollPane(String title, Component c) {
        super(c);
        _tb = new TitledBorder(title);
        this.setBorder(_tb);
    }
    public void setTitle(String title) {
        _tb.setTitle(title);
        this.repaint();
    }
}

class MountGraphFromPath {
    Graph _G;

    public MountGraphFromPath(BlinkEntry be) throws SQLException, IOException, ClassNotFoundException {
        be.loadPath();
        Path p = be.getPath();
        this.mount(be.getBlink().getGem(),p);
    }

    public MountGraphFromPath(Gem root, Path p) throws SQLException, IOException, ClassNotFoundException {
        this.mount(root,p);
    }

    private void mount(Gem root, Path p) throws SQLException, IOException, ClassNotFoundException {
        Graph G = new SparseGraph();

        Gem g = root.copy();
        g.goToCodeLabel();

        System.out.println(""+g.getCurrentLabelling().getLettersString(","));

        // insert the root node on the reduction graph
        Vertex currentVertex = G.addVertex(new SparseVertex());
        currentVertex.setUserDatum("key",g,UserData.SHARED);
        currentVertex.setUserDatum("onPath",Boolean.TRUE,UserData.SHARED);

        for (int i=0;i<p.size();i++) {
            Move m = p.getMove(i);

            System.out.println("applying "+m.getSignature());

            if (m instanceof DipoleMove) {
                DipoleMove dm = (DipoleMove) m;

                g = g.copy();
                GemVertex u = g.findVertex(dm.getU());
                Dipole d = new Dipole(u,dm.getColors());
                g.cancelDipole(d);

            }

            else if (m instanceof RhoMove) {
                RhoMove rm = (RhoMove) m;

                g = g.copy();
                RhoPair rp = new RhoPair(
                        g.findVertex(rm.getU()),
                        g.findVertex(rm.getV()),
                        rm.getColor(),
                        rm.foundAsA());
                g.applyRhoPair(rp);

            }

            else if (m instanceof TSMove) {
                TSMove tm = (TSMove) m;

                g = g.copy();
                TSMovePoint tsmp = new TSMovePoint(tm.getA(),tm.getP(),tm.getType());
                g.applyTSMove(tsmp);
                g.goToCodeLabel();

            }

            else if (m instanceof RelabelMove) {
                // RelabelMove rm = (RelabelMove) m;

                g = g.copy();
                g.goToCodeLabel();

            }

            else if (m instanceof UMove) {
                UMove um = (UMove) m;

                g = g.copy();

                Monopole monopole = new Monopole(g.findVertex(um.getA()),
                                                 um.getColor(),
                                                 0,0);

                g.uMove(monopole);
            }

            // insert the root node on the reduction graph
            Vertex newVertex = G.addVertex(new SparseVertex());
            newVertex.setUserDatum("key",g,UserData.SHARED);
            newVertex.setUserDatum("onPath",Boolean.TRUE,UserData.SHARED);

            // add an edge from parent gem to this new simplified gem
            Edge e = G.addEdge(new DirectedSparseEdge(currentVertex, newVertex));
            e.setUserDatum("key", m, UserData.SHARED);

            currentVertex = newVertex;
        }

        _G = G;
    }


    public Graph getGraph() {
        return _G;
    }
}
