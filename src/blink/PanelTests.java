
package blink;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.StringTokenizer;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

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
public class PanelTests extends JPanel {

    private JSplitPane _splitPane = new JSplitPane();
    private JTextArea _entry = new JTextArea();
    private JTextArea _output = new JTextArea();
    private JButton _run = newButton("Run","Ler, desenha, QI e HG");
    private JButton _save = newButton("Save","Salvar arquivo");
    private JButton _open = newButton("Open","Abrir blink de arquivo com representação ciclica");
    private JButton _rep = newButton("Rep.","Desenha representante");
    private JButton _all = newButton("All","Ler e gera todas as colorações de arestas e escreve todo QI válido.");
    private JButton _gem = newButton("Gem","Ler e escreve codigo da gema do blink.");
    private JButton _bcr = newButton("BCR","Ler e escreve representação ciclica do blink.");
    private JButton _dup = newButton("Dup","Duplicatas.");
    private JButton _findGem = newButton("F.Gem","Enontrar Gem. (Mapa Azul)");
    private JButton _att = newButton("Att","Attractor from Blink Cyclic Rep.");
    private JButton _btnDrawBlinkGem = newButton("Bl.Gem.","Draw Blink Gem");
    private JButton _btnReductionGraph = newButton("R.Graph.","Reduction Graph");
    private JButton _mapDrawing = newButton("MapD","Map Drawing");
    private JButton _blinkDrawing = newButton("BlinkD","Blink Drawing");
    private JButton _btnGemString = newButton("GemSt","Gem String");
    private JButton _btnThickenGem = newButton("Thick","Gem String");
    private JButton _btnResolve = newButton("Res.","Resolution");
    private JCheckBox _cbFromBlue = new JCheckBox("Blue Maps");
    private JCheckBox _cbMount3d = new JCheckBox("3D");
    private JTextField _tfR = new JTextField();

    public PanelTests() {
        _entry.setFont(new Font("Courier New",Font.PLAIN,11));
        _output.setFont(new Font("Courier New",Font.PLAIN,11));


        JSplitPane leftSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BorderLayout());
        leftPanel.add(new JScrollPane(_entry),BorderLayout.CENTER);

        _tfR.setPreferredSize(new Dimension(30,21));
        _tfR.setText("6");

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridBagLayout());
        buttonPanel.add(_open,new GridBagConstraints(0,0,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(2,2,2,2),0,0));
        buttonPanel.add(_save,new GridBagConstraints(1,0,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(2,2,2,2),0,0));
        buttonPanel.add(_run, new GridBagConstraints(2,0,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(2,2,2,2),0,0));
        buttonPanel.add(_tfR, new GridBagConstraints(3,0,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(2,2,2,2),0,0));
        buttonPanel.add(_findGem, new GridBagConstraints(0,1,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(2,2,2,2),0,0));
        buttonPanel.add(_rep, new GridBagConstraints(1,1,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(2,2,2,2),0,0));
        buttonPanel.add(_all, new GridBagConstraints(2,1,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(2,2,2,2),0,0));
        buttonPanel.add(_gem, new GridBagConstraints(3,1,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(2,2,2,2),0,0));
        buttonPanel.add(_bcr, new GridBagConstraints(0,2,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(2,2,2,2),0,0));
        buttonPanel.add(_dup, new GridBagConstraints(1,2,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(2,2,2,2),0,0));
        buttonPanel.add(_att, new GridBagConstraints(2,2,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(2,2,2,2),0,0));
        buttonPanel.add(_btnDrawBlinkGem, new GridBagConstraints(3,2,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(2,2,2,2),0,0));
        buttonPanel.add(_btnReductionGraph, new GridBagConstraints(0,3,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(2,2,2,2),0,0));
        buttonPanel.add(_mapDrawing, new GridBagConstraints(1,3,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(2,2,2,2),0,0));
        buttonPanel.add(_blinkDrawing, new GridBagConstraints(2,3,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(2,2,2,2),0,0));
        buttonPanel.add(_btnGemString, new GridBagConstraints(3,3,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(2,2,2,2),0,0));
        buttonPanel.add(_btnThickenGem, new GridBagConstraints(0,4,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(2,2,2,2),0,0));
        buttonPanel.add(_cbFromBlue, new GridBagConstraints(1,4,2,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(2,2,2,2),0,0));
        buttonPanel.add(_cbMount3d, new GridBagConstraints(3,4,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(2,2,2,2),0,0));
        buttonPanel.add(_btnResolve, new GridBagConstraints(0,5,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(2,2,2,2),0,0));
        buttonPanel.add(new JButton(new ActionListReidemeisterIII()), new GridBagConstraints(1,5,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(2,2,2,2),0,0));
        buttonPanel.add(new JButton(new ActionRandomGBlink()), new GridBagConstraints(2,5,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.HORIZONTAL,new Insets(2,2,2,2),0,0));

        leftPanel.add(buttonPanel,BorderLayout.SOUTH);

        leftSplitPane.setTopComponent(leftPanel);
        leftSplitPane.setBottomComponent(new JScrollPane(_output));
        leftSplitPane.setDividerLocation(300);

        _splitPane.setOrientation(JSplitPane.HORIZONTAL_SPLIT);
        _splitPane.setLeftComponent(leftSplitPane);

        _splitPane.setDividerLocation(200);

        this.setLayout(new BorderLayout());
        this.add(_splitPane,BorderLayout.CENTER);

        _btnGemString.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                gemString();
            }
        });


        _run.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                execute();
            }
        });

        _gem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                gem();
            }
        });

        _open.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                open();
            }
        });

        _save.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                save();
            }
        });

        _rep.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                representant();
            }
        });

        _btnResolve.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    resolve();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        _all.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                all();
            }
        });

        _findGem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    findGem();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                } catch (IOException ex) {
                    ex.printStackTrace();
                } catch (ClassNotFoundException ex) {
                    ex.printStackTrace();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        _bcr.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                bcr();
            }
        });

        _dup.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                dup();
            }
        });

        _att.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    att();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                } catch (IOException ex) {
                    ex.printStackTrace();
                } catch (ClassNotFoundException ex) {
                    ex.printStackTrace();
                }
            }
        });

        _btnDrawBlinkGem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                drawBlinkGem();
            }
        });

        _btnReductionGraph.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                reduce();
            }
        });

        _mapDrawing.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                drawMap();
            }
        });

        _blinkDrawing.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                drawBlink();
            }
        });

        _btnThickenGem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                try {
                    thicken();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });


        ////////////////////////////////////////////
        // Ações pelo teclado
        this.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke('p'), "reidemeister");
        this.getActionMap().put("reidemeister", new ActionListReidemeisterIII());
        //
        //




    }

    public static JButton newButton(String title, String description) {
        JButton btn = new JButton(title);
        btn.setToolTipText(description);
        btn.setMargin(new Insets(0,0,0,0));
        return btn;
    }

    private void representant() {

        GBlink b = readBlink();

        ArrayList<GBlink> pieces = b.copy().breakMap();

        ArrayList<MapD> list = new ArrayList<MapD>();
        list.add(new MapD(b));
        for (GBlink bb: pieces)
            list.add(new MapD(bb));

        // dual
        list.add(new MapD(b.dual()));
        for (GBlink bb: pieces)
            list.add(new MapD(bb.dual()));

        // merge
        /*
        Blink m = b.copy();
        Blink md = m.dual();
        Blink.merge(m,m.findVertex(1),md,md.findVertex(1));
        list.add(new MapD(m));*/

        //
        list.add(new MapD(b.getRepresentant()));

        // desenhar o mapa
        JFrame f = new JFrame("Map Drawing");
        f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        f.setSize(new Dimension(250,250));
        int n = (int) Math.ceil(Math.sqrt(list.size()));
        f.setContentPane(new DrawPanelMultipleMaps(list,n,n));
        f.setVisible(true);
        // desenhar o mapa

    }

    private GBlink readBlink() {
        GBlink result = null;
        try {
            result = this.readBlinkFromCyclicRepresentation();
        }
        catch (Exception ex) {
            result = this.readGBlinkFromCode();
        }
        System.out.println(""+result.getBlinkWord().toString());
        return result;
    }

    private GBlink readBlinkFromCyclicRepresentation() {
        StringTokenizer stLines = new StringTokenizer(_entry.getText(),"\n");
        ArrayList<ArrayList<Integer>> list = new ArrayList<ArrayList<Integer>>();
        ArrayList<Integer> reds = new ArrayList<Integer>();
        while (stLines.hasMoreTokens()) {
            String line = stLines.nextToken();
            StringTokenizer stEdges = new StringTokenizer(line," ,\t\n");
            if (stEdges.countTokens() == 0)
                continue;
            ArrayList<Integer> vcl = new ArrayList<Integer>(); // vertice cyclic edge list
            list.add(vcl);
            boolean isRed = false;
            while (stEdges.hasMoreTokens()) {
                String s = stEdges.nextToken();
                if (s.toUpperCase().equals("R")) {
                    isRed = true;
                    continue;
                }
                int edge = Integer.parseInt(s);
                if (isRed) {
                    reds.add(edge);
                }
                else {
                    vcl.add(edge);
                }
            }

            if (vcl.size() == 0)
                list.remove(vcl);
        }

        int[][] vertices = new int[list.size()][];
        for (int i=0;i<list.size();i++) {
            ArrayList<Integer> listi = list.get(i);
            vertices[i] = new int[listi.size()];
            for (int j=0;j<listi.size();j++) {
                vertices[i][j] = listi.get(j);
            }
        }

        int redsArray[] = new int[reds.size()];
        for (int i=0;i<reds.size();i++) {
            redsArray[i] = reds.get(i);
        }

        GBlink b = new GBlink(vertices,redsArray);

        return b;

    }

    /**
     * Apply Reidemeister III
     */
    class ActionListReidemeisterIII extends AbstractAction {
        public ActionListReidemeisterIII() {
            super("RIII");
        }
        public void actionPerformed(ActionEvent e) {
            try {
                hardwork();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        private void hardwork() {
            GBlink G = readBlink();

            GBlink G2 = G.copy();
            ArrayList<PointOfReidemeisterIII> points = G2.findAllReidemeisterIIIPoints();
            StringBuffer sb = new StringBuffer();
            sb.append("Reidemeister III Points: \n");
            for (PointOfReidemeisterIII p: points) {
                sb.append(p.description(G2)+"\n");
            }
            _output.setText(sb.toString());

            if (points.size() > 0) {
                G2.applyReidemeisterIIIMove(points.get(0));
                G2.goToCodeLabelPreservingSpaceOrientation();
            }

            JTabbedPane tp = new JTabbedPane();
            tp.add("Original GBlink",new PanelMapViewer(G));
            tp.add("Original GBlink Coin",new DrawPanel(new MapD(G)));
            tp.add("Original Blink",new PanelBlinkViewer(G));
            if (points.size() > 0) {
                tp.add("First Reidemeister III", new PanelMapViewer(G2));
                tp.add("First Reidemeister III Coin", new DrawPanel(new MapD(G2)));
                tp.add("First Reidemeister III Blink", new PanelBlinkViewer(G2));

                GBlink G3 = G2.getNewRepresentant();
                tp.add("New Representant GBLink", new PanelMapViewer(G3));
                tp.add("New Representant Coin", new DrawPanel(new MapD(G3)));
                tp.add("New Representant Blink", new PanelBlinkViewer(G3));

                System.out.println("Original:         "+G.getBlinkWord().toString());
                System.out.println("New Representant: "+G3.getBlinkWord().toString());

                // compare
                QI qi0 = G.optimizedQuantumInvariant(3,8);
                QI qi1 = G3.optimizedQuantumInvariant(3,8);

                HomologyGroup hg0 = G.homologyGroupFromGBlink();
                HomologyGroup hg1 = G3.homologyGroupFromGBlink();

                if (qi0.compareNormalizedEntries(qi1) && hg0.compareTo(hg1) == 0) {
                    System.out.println("OK they induce GBlinks with the same HG and QI");
                }
                else {
                    System.out.println("Problem");
                }
            }
            int dl = _splitPane.getDividerLocation();
            _splitPane.setRightComponent(tp);
            _splitPane.setDividerLocation(dl);
        }
    }

    /**
     * Random GBlink
     */
    class ActionRandomGBlink extends AbstractAction {
        public ActionRandomGBlink() {
            super("RndGb");
        }
        public void actionPerformed(ActionEvent e) {
            try {
                hardwork();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        private void hardwork() {
            // randomly choose GBlink
            GBlink G = GBlink.random(Integer.parseInt(_tfR.getText()));

            _output.setText(G.getBlinkWord().toString());

            JTabbedPane tp = new JTabbedPane();
            tp.add("Random GBlink Coin",new DrawPanel(new MapD(G)));
            tp.add("Random GBlink",new PanelMapViewer(G));
            tp.add("Random Blink",new PanelBlinkViewer(G));

            int dl = _splitPane.getDividerLocation();
            _splitPane.setRightComponent(tp);
            _splitPane.setDividerLocation(dl);
        }
    }



    private GBlink readGBlinkFromCode() {
        StringTokenizer st = new StringTokenizer(_entry.getText()," ");
        int[] code = Library.stringToIntArray(st.nextToken(),",");
        int[] reds = Library.stringToIntArray(st.nextToken(),",");
        GBlink G = new GBlink(code,reds);
        return G;
    }



    private Gem readLinsMandelGem() {
        StringTokenizer st = new StringTokenizer(_entry.getText(),"\n ,");
        if (!st.hasMoreTokens())
            throw new RuntimeException("Ooooppsss");

        if (!st.nextToken().toUpperCase().equals("S"))
            throw new RuntimeException("Ooooppsss");

        int b = Integer.parseInt(st.nextToken());
        int l = Integer.parseInt(st.nextToken());
        int t = Integer.parseInt(st.nextToken());
        int c = Integer.parseInt(st.nextToken());

        return new Gem(b,l,t,c);
    }

    private void execute() {
        try {
            executeHardWork();
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,ex.getMessage());
        } catch (ClassNotFoundException ex) {
            JOptionPane.showMessageDialog(this,ex.getMessage());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,ex.getMessage());
        } catch (RuntimeException ex) {
            JOptionPane.showMessageDialog(this,ex.getMessage());
        }

    }

    private void executeHardWork() throws IOException, ClassNotFoundException, SQLException {

        GBlink b = readBlink();

        MapD mapD = new MapD(b);
        int dl = _splitPane.getDividerLocation();
        _splitPane.setRightComponent(new DrawPanel(mapD));
        _splitPane.setDividerLocation(dl);

        StringBuffer buf = new StringBuffer();
        QI qi = b.optimizedQuantumInvariant(3,getRMax());
        buf.append(qi.toString());

        ArrayList<Integer> qis = FindByQI.findQI(qi);
        buf.append("\n\nQIs on database:\n");
        for (int i: qis) {
            buf.append(i+" ");
        }
        buf.append("\n");


        buf.append("\nHomology by Gem:\n");
        buf.append(b.homologyGroupFromGem().toString());
        buf.append("\n");

        buf.append("\nHomology by Blink:\n");
        buf.append(b.homologyGroupFromGBlink().toString());
        buf.append("\n");

        _output.setText(buf.toString());

    }

    public void open() {
        try {
            openHardWork();
        } catch (FileNotFoundException ex) {
            JOptionPane.showMessageDialog(this,ex.getMessage());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,ex.getMessage());
        }
    }

    public void openHardWork() throws FileNotFoundException, IOException {
        JFileChooser fc = new JFileChooser();
        fc.setMultiSelectionEnabled(true);
        fc.setSelectedFile(new File(App.getProperty("lastOpenFilename")));

        int r = fc.showOpenDialog(this);
        if (r == JFileChooser.APPROVE_OPTION) {
            File file = fc.getSelectedFile();

            // file
            App.setProperty("lastOpenFilename", fc.getSelectedFile().getAbsolutePath());

            _entry.setText("");

            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;
            while ((line = br.readLine()) != null) {
                _entry.append(line+"\n");
            }
        }
    }

    public void save() {
        try {
            saveHardWork();
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,ex.getMessage());
        }
    }


    public void saveHardWork() throws IOException {
        JFileChooser fc = new JFileChooser();
        fc.setMultiSelectionEnabled(false);

        String file1 = App.getProperty("savefilename");
        if (file1 == null)
            file1 = "c:/blink01.txt";
        fc.setSelectedFile(new File(file1));

        int r = fc.showSaveDialog(this);
        if (r == JFileChooser.APPROVE_OPTION) {
            App.setProperty("savefilename", fc.getSelectedFile().getAbsolutePath());
            FileWriter fw = new FileWriter(fc.getSelectedFile());
            fw.write(_entry.getText());
            fw.flush();
            fw.close();
        }
    }

    public static void main(String[] args) {
        // desenhar o mapa
        JFrame f = new JFrame("Tests");
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setSize(new Dimension(800, 600));
        f.setContentPane(new PanelTests());
        f.setVisible(true);
        // desenhar o mapa
    }

    public void all() {
        try {
            allHardWork();
        } catch (SQLException ex1) {
            JOptionPane.showMessageDialog(this,ex1.getMessage());
        } catch (ClassNotFoundException ex1) {
            JOptionPane.showMessageDialog(this,ex1.getMessage());
        } catch (IOException ex1) {
            JOptionPane.showMessageDialog(this,ex1.getMessage());
        }
    }

    public int getRMax() {
        int r;
        try {
            r = Integer.parseInt(_tfR.getText());
        } catch (NumberFormatException ex) {
            r = 4;
        }
        return r;
    }

    private void allHardWork() throws IOException, ClassNotFoundException, SQLException {

        GBlink b = readBlink();

        int N = (int) Math.pow(2,b.getNumberOfGEdges());

        _output.setText("");

        ArrayList<MapD> list = new ArrayList<MapD>();
        for (int k=0;k<N;k++) {
            GBlink b2 = b.copy();
            b2.setColor(k);
            list.add(new MapD(b2));

            StringBuffer buf = new StringBuffer();
            System.out.println("Color: "+k);
            b2.write();
            buf.append("Color: "+k+"\n");

            try {
                QI qi = b2.optimizedQuantumInvariant(3, getRMax());
                buf.append(qi.toString());

                ArrayList<Integer> qis = FindByQI.findQI(qi);
                buf.append("\n\nQIs on database:\n");
                for (int i : qis) {
                    buf.append(i + " ");
                    System.out.print(i+" ");
                }
                System.out.println();
                buf.append("\n");

                /*
                buf.append("\nHomology by Gem:\n");
                buf.append(b.homologyGroup().toString());
                buf.append("\n");
                */

                buf.append("\nHomology by Blink:\n");
                buf.append(b2.homologyGroupFromGBlink().toString());
                buf.append("\n");

            } catch (Exception ex) {
                // ex.printStackTrace();
            }
            _output.append(buf.toString());
        }

        int dl = _splitPane.getDividerLocation();

        int n = (int)Math.ceil(Math.sqrt(list.size()));
        _splitPane.setRightComponent(new DrawPanelMultipleMaps(list,n,n));
        _splitPane.setDividerLocation(dl);

    }

    public void gem() {
        GBlink b = readBlink();
        Gem g = (Gem) (new GemFromBlink(b)).getGem();
        g.goToCodeLabel();
        GemPackedLabelling l = g.getCurrentLabelling();

        ArrayList<Dipole> dipoles = g.findOneTwoOrThressDipoles();
        for (Dipole d: dipoles)
            System.out.println(""+d.toString());

        Object[] o = g.findDipoleThickenningAndDipoleNarrowingPoints();
        ArrayList<DipoleThickenning> dts = (ArrayList<DipoleThickenning>) o[0];
        ArrayList<DipoleNarrowing> dns = (ArrayList<DipoleNarrowing>) o[1];
        for (DipoleThickenning d: dts)
            System.out.println(""+d.toString());
        for (DipoleNarrowing d: dns)
            System.out.println(""+d.toString());


        // desenhar o mapa
        int dl = _splitPane.getDividerLocation();
        _splitPane.setRightComponent(new DrawPanel(new MapD(b)));
        _splitPane.setDividerLocation(dl);
        // desenhar o mapa

        _output.setText(l.getLettersString("\n"));
    }

    public void bcr() {
        GBlink b = readBlink();
        BlinkCyclicRepresentation bcr = b.getCyclicRepresentation();
        _output.setText(bcr.toString());

        // desenhar o mapa
        int dl = _splitPane.getDividerLocation();
        _splitPane.setRightComponent(new DrawPanel(new MapD(b)));
        _splitPane.setDividerLocation(dl);
    }

    public void dup() {
        try {
            StringBuffer buf = new StringBuffer();
            ArrayList<ClassHGQI> list = App.getRepositorio().getHGQIClasses(App.MAX_EDGES);
            Collections.sort(list,new Comparator() {
                public int compare(Object o1, Object o2) {
                    ClassHGQI c1 = (ClassHGQI) o1;
                    ClassHGQI c2 = (ClassHGQI) o2;
                    return (int) (c1.get_qi() - c2.get_qi());
                }
            });
            for (int i=0;i<list.size();i++) {
                long qi = list.get(i).get_qi();
                int k = 0;
                while (i+k+1 < list.size()) {
                    if (list.get(i+k+1).get_qi() == qi)
                        k++;
                    else break;
                }

                if (k > 0) {
                    buf.append(String.format("QI: %3d  ",qi));
                    boolean first = true;
                    for (int j=i;j<=i+k;j++) {
                        if (!first)
                            buf.append(" - ");
                        buf.append(list.get(j).get_hg());
                        first = false;
                    }
                    buf.append("\n");
                }
            }

            _output.setText(buf.toString());

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void resolve() throws Exception {
         this.findGem();
        Gem g = this.readGem();
        SearchByTwistor S = new SearchByTwistor(g,GemColor.blue);
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
            S = new SearchByTwistor(g,GemColor.red);
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
                S = new SearchByTwistor(g, GemColor.green);
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
                    JOptionPane.showMessageDialog(this,"No Resolution for this gem.");
                }
            }
        }
    }


    public void gemString() {
        GemString gs = new GemString(_entry.getText(),' ','\n');

        Gem gem = null;
        try {
            gem = gs.getGem();
        } catch (Exception ex) {
        }

        JTabbedPane tp = new JTabbedPane();
        tp.add("String",new PanelEditString(gs));
        if (gem != null) {
            tp.add("Gem: yes", new PanelGemViewer(gs.getGem(),_cbMount3d.isSelected()));
        }
        else {
            tp.add("Gem: no", new JLabel("Did not find gem"));
        }

        int dl = _splitPane.getDividerLocation();
        _splitPane.setRightComponent(tp);
        _splitPane.setDividerLocation(dl);
    }

    public void findGem() throws ClassNotFoundException, IOException, SQLException, Exception {
        // read gem from textarea
        Gem g = readGem();

        ArrayList<GemEntry> list = App.getRepositorio().getGems();

        int dl = _splitPane.getDividerLocation();
        _splitPane.setRightComponent(new PanelGemViewer(g,_cbMount3d.isSelected()));
        _splitPane.setDividerLocation(dl);

        for (GemEntry x: list) {
            if (x.getGem().equals(g)) {
                StringBuffer sb = new StringBuffer();
                sb.append("Gem: "+x.getId()+"\n");
                sb.append("NumVert: "+x.getNumVertices()+"\n");
                sb.append("Handle: "+x.getHandleNumber()+"\n");
                sb.append("TS-Class Size: "+x.getTSClassSize()+"\n\n");

                ArrayList<BlinkEntry> lBlinks = App.getRepositorio().getBlinksByGem(x.getId());
                for (BlinkEntry be: lBlinks) {
                    sb.append(String.format("\nID: %6d  QI: %6d  HG: %18s",be.get_id(),be.get_qi(),be.get_hg()));
                }
                _output.setText(sb.toString());
                return;
            }
        }
        _output.setText("Não Cadastrado!");


    }


    public void thicken() throws Exception {
        // read gem from textarea
        Gem g = readGem();
        ThickenedGem tg = new ThickenedGem(g);

        int dl = _splitPane.getDividerLocation();
        _splitPane.setRightComponent(new PanelGemViewer(tg,tg,_cbMount3d.isSelected()));
        _splitPane.setDividerLocation(dl);

    }

    public void att() throws ClassNotFoundException, IOException, SQLException {

        // read gem from textarea
        GBlink b = this.readBlink();
        Gem g = (new GemFromBlink(b)).getGem();
        GenerateRepresentantGem grg = new GenerateRepresentantGem(g);
        GemPackedLabelling a = grg.getRepresentant();

        // string buffer
        StringBuffer sb = new StringBuffer();
        sb.append(String.format("Pseudo-Attractor gem: %s [%d]\nnvert: %d\nTS-Class size: %d\n\n",
                  a.getLettersString(","),
                  a.getHandleNumber(),
                  a.getNumberOfVertcices(),
                  grg.getTSClassSize()));

        //
        ArrayList<GemEntry> list = App.getRepositorio().getGems();
        for (GemEntry x: list) {
            if (x.equals(a)) {
                sb.append("Gem: "+x.getId()+"\n");
                sb.append("NumVert: "+x.getNumVertices()+"\n");
                sb.append("Handle: "+x.getHandleNumber()+"\n");
                sb.append("TS-Class Size: "+x.getTSClassSize()+"\n\n");

                ArrayList<BlinkEntry> lBlinks = App.getRepositorio().getBlinksByGem(x.getId());
                for (BlinkEntry be: lBlinks) {
                    sb.append(String.format("\nID: %6d  QI: %6d  HG: %18s",be.get_id(),be.get_qi(),be.get_hg()));
                }
                _output.setText(sb.toString());
                return;
            }
        }
        _output.setText("Não Cadastrado!");
    }

    private void drawBlinkGem() {
        // read gem from textarea
        GBlink b = this.readBlink();
        Gem g = (new GemFromBlink(b)).getGem();
        g = new Gem(g.goToCodeLabel());

        int dl = _splitPane.getDividerLocation();
        _splitPane.setRightComponent(new PanelGemViewer(g));
        _splitPane.setDividerLocation(dl);
    }

    private Gem readGem() throws Exception {
        Gem g = null;
        try {
            GemPackedLabelling l = new GemPackedLabelling(_entry.getText());
            g = new Gem(l);
        } catch (Exception ex) {
            try {
                System.out.println("Not a gem labelling. Trying Lins Mandel...");
                g = readLinsMandelGem();
            } catch (Exception ex2) {
                try {
                    System.out.println("Not! Trying Gem from Blink...");
                    if (_cbFromBlue.isSelected()) {
                        g = new Gem(readBlink());
                        g.goToCodeLabel();
                        g = g.copy();
                    }
                    else {
                        GBlink b = readBlink();
                        g = b.getGem();
                    }
                    System.out.println(""+g.getAgemality());
                } catch (Exception ex3) {
                    System.out.println("Not! Trying Gem from Blink...");
                    StringTokenizer st = new StringTokenizer(_entry.getText()," ");
                    st.nextToken();
                    int numVert = Integer.parseInt(st.nextToken());
                    int catNumb = Integer.parseInt(st.nextToken());
                    GemEntry ge = App.getRepositorio().getGemEntryByCatalogNumber(numVert,catNumb,0);
                    if (ge != null) {
                        g = ge.getGem();
                    }
                }
            }
        }
        return g;
    }

    private void reduce() {
        Gem g = null;
        try {
            g = readGem();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,"Problema!!!");
            return;
        }

        g = new Gem(g.goToCodeLabel());
        PanelReductionGraph prg = new PanelReductionGraph(g);
        int dl = _splitPane.getDividerLocation();
        _splitPane.setRightComponent(prg);
        _splitPane.setDividerLocation(dl);

    }

    private void drawMap() {
        GBlink b = this.readBlink();
        b.goToCodeLabelAndDontCareAboutSpaceOrientation();
        int dl = _splitPane.getDividerLocation();
        _splitPane.setRightComponent(new PanelMapViewer(b));
        _splitPane.setDividerLocation(dl);
    }

    private void drawBlink() {
        GBlink b = this.readBlink();
        b.goToCodeLabelAndDontCareAboutSpaceOrientation();

        Gem g = b.getGem();
        g.copyCurrentLabellingToOriginalLabelling();

        JTabbedPane tp = new JTabbedPane();
        tp.add("Blink",new PanelBlinkViewer(b));
        tp.add("Map",new PanelMapViewer(b));
        tp.add("Gem",new PanelGemViewer(g));

        int dl = _splitPane.getDividerLocation();
        _splitPane.setRightComponent(tp);
        _splitPane.setDividerLocation(dl);


    }

}
