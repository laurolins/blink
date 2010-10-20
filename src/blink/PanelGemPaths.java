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
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import linsoft.gui.bean.BeanTable;
import linsoft.gui.bean.MultipleBeanTableModel;

/**
 * Panel Gems
 */
public class PanelGemPaths extends JPanel {

    private JSplitPane _leftSplitPanel = new JSplitPane();
    private JTextField _tfSearchField = new JTextField();
    private BeanTable _tableGemPaths = new BeanTable();
    private MultipleBeanTableModel _modelGemPaths;
    private TitledScrollPane _tspGems;
    private JTextArea _taBlinkDeails = new JTextArea();
    private JButton _btnSearch = new JButton();
    private JButton _btnUpdateGem = new JButton();
    private JSplitPane _splitPane = new JSplitPane();
    private JSplitPane _splitPaneTables = new JSplitPane();
    private JLabel _bottomLabel = new JLabel();

    public PanelGemPaths() throws IntrospectionException, SQLException, ClassNotFoundException, IOException {
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

        _modelGemPaths = new MultipleBeanTableModel(GemPathEntry.class);
        _modelGemPaths.setVisiblePropertySequence(new String[] {"id","source","target","pathLength"});
        _modelGemPaths.addObject(App.getRepositorio().getGemPaths());
        //_modelGemPaths.addObject(App.getRepositorio().getBlinksByIDInterval(62359,62359+128));
        //_modelGemPaths.addObject(App.getRepositorio().getBlinks(1,3));

        _tableGemPaths = new BeanTable();
        _tableGemPaths.setModel(_modelGemPaths);
        _tableGemPaths.registerColumnSorter();
        _tableGemPaths.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
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

        _tspGems = new TitledScrollPane("Blinks "+_modelGemPaths.getRowCount(),_tableGemPaths);
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
        /*
        try {
            _modelGemPaths.clear();
            _modelGemPaths.addObject(App.getRepositorio().getGems());
        } catch (ClassNotFoundException ex) {
        } catch (IOException ex) {
        } catch (SQLException ex) {
        }*/
    }

    HashMap<Long,GemEntry> _map = App.getRepositorio().getGemsMap();
    private void changeSelection() throws ClassNotFoundException, IOException, SQLException {

        GemPathEntry sel = (GemPathEntry) _tableGemPaths.getSelectedObject();

        if (sel == null)
            return;

        GemEntry sourceEntry = _map.get(sel.getSource());
        MountGraphFromPath M = new MountGraphFromPath(sourceEntry.getGem(),sel.getPath());
        //GemEntry targetEntry = _map.get(sel.getTarget());

        PanelReductionGraph prg = new PanelReductionGraph(M.getGraph());
        int dl = _splitPane.getDividerLocation();
        _splitPane.setRightComponent(prg);
        _splitPane.setDividerLocation(dl);
    }
}
