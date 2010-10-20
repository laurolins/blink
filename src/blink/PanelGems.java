package blink;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.IntrospectionException;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import linsoft.gui.bean.BeanTable;
import linsoft.gui.bean.MultipleBeanTableModel;
import edu.uci.ics.jung.utils.Pair;

/**
 * Panel Gems
 */
public class PanelGems extends JPanel {

    private JSplitPane _leftSplitPanel = new JSplitPane();
    private JTextField _tfSearchField = new JTextField();
    private BeanTable _tableGems = new BeanTable();
    private MultipleBeanTableModel _modelGems;
    private TitledScrollPane _tspGems;
    private JTextArea _taBlinkDeails = new JTextArea();
    private JButton _btnSearch = new JButton();
    private JButton _btnUpdateGem = new JButton();
    private JSplitPane _splitPane = new JSplitPane();
    private JSplitPane _splitPaneTables = new JSplitPane();
    private JLabel _bottomLabel = new JLabel();

    private DrawPanelMultipleMaps _drawPanel;

    public PanelGems() throws IntrospectionException, SQLException, ClassNotFoundException, IOException {
        JPanel panelSearchAndResult = new JPanel();
        panelSearchAndResult.setLayout(new GridBagLayout());

        _tfSearchField.setPreferredSize(new Dimension(150,21));
        panelSearchAndResult.add(_tfSearchField,
                                 new GridBagConstraints(0, 0, 1, 1, 1, 0,
                                                        GridBagConstraints.CENTER,
                                                        GridBagConstraints.HORIZONTAL,
                                                        new Insets(2, 2, 2, 2), 0, 0));

        _btnSearch.setText("Atualizar");
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

        _modelGems = new MultipleBeanTableModel(GemEntry.class);
        _modelGems.setVisiblePropertySequence(new String[] {"_id","_numVertices","_handleNumber","_tsClassSize","_catalogNumber"});
        _modelGems.addObject(App.getRepositorio().getGems());
        //_modelGems.addObject(App.getRepositorio().getBlinksByIDInterval(62359,62359+128));
        //_modelGems.addObject(App.getRepositorio().getBlinks(1,3));

        _tableGems = new BeanTable();
        _tableGems.setModel(_modelGems);
        _tableGems.registerColumnSorter();
        _tableGems.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
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

        _tspGems = new TitledScrollPane("Blinks "+_modelGems.getRowCount(),_tableGems);
        _tspGems.setPreferredSize(new Dimension(200,100));

        panelSearchAndResult.add(_tspGems,
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
        this.setLayout(new BorderLayout());
        this.add(_splitPane,BorderLayout.CENTER);
        this.add(_bottomLabel,BorderLayout.SOUTH);

        _btnSearch.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                atualizar();
            }
        });

        _btnUpdateGem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            }
        });
    }

    private void atualizar() {
        try {
            _modelGems.clear();
            _modelGems.addObject(App.getRepositorio().getGems());
        } catch (ClassNotFoundException ex) {
        } catch (IOException ex) {
        } catch (SQLException ex) {
        }
    }

    private void changeSelection() throws ClassNotFoundException, IOException, SQLException {
        Vector vs = _modelGems.getObjects(_tableGems.getSelectedRows());

        JTabbedPane tp = new JTabbedPane();
        for (GemEntry ge: (Vector<GemEntry>) vs) {
            Gem g = new Gem(ge.getLabelling());
            tp.add(""+ge.getId(),new PanelGemViewer(g));
        }
        int dl = _splitPane.getDividerLocation();
        _splitPane.setRightComponent(tp);
        _splitPane.setDividerLocation(dl);

        ArrayList<Pair> pairs = App.getRepositorio().getHGQIClassesOfGems(new ArrayList((Vector<GemEntry>) vs));
        StringBuffer sb = new StringBuffer();
        sb.append(String.format("%-20s %-6s\n","HG","QI"));
        for (Pair p: pairs) {
            sb.append(String.format("%-20s %-6d\n",p.getFirst(),(Long)p.getSecond()));
        }
        _taBlinkDeails.setText(sb.toString());
    }
}
