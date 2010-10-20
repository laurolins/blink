package linsoft.gui.bean;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.beans.IntrospectionException;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

public class PanelSelectVisibleColumns extends JPanel {
    BorderLayout borderLayout1 = new BorderLayout();
    FlowLayout flowLayout1 = new FlowLayout();
    BorderLayout borderLayout2 = new BorderLayout();
    JPanel _panelCancelarOK = new JPanel();
    JButton _botaoOK = new JButton();
    JButton _botaoCancelar = new JButton();
    JSplitPane _splitPane = new JSplitPane();
    JPanel _panelToolBarPropriedadesNaoVisiveis = new JPanel();
    JScrollPane _scrollPanePropriedadesVisiveis = new JScrollPane();
    Border border1;
    TitledBorder titledBorder1;
    BorderLayout borderLayout3 = new BorderLayout();
    JPanel _panelToolBar = new JPanel();
    JScrollPane _scrollPanePropriedadesNaoVisiveis = new JScrollPane();
    Border border2;
    TitledBorder titledBorder2;
    JButton _botaoTornarVisivel = new JButton();
    JButton _botaoTornarNaoVisivel = new JButton();
    Border border3;
    // FIM: Visual Components

    // INICIO: tabelas de propriedades visiveis e nao visiveis
    BeanTable _tablePropriedadesVisiveis = new BeanTable();
    BeanTable _tablePropriedadesNaoVisiveis = new BeanTable();
    MultipleBeanTableModel _modelPropriedadesVisiveis = new MultipleBeanTableModel(BeanProperty.class);
    MultipleBeanTableModel _modelPropriedadesNaoVisiveis = new MultipleBeanTableModel(BeanProperty.class);
    // FIM: tabelas de propriedades visiveis e nao visiveis

    // INICIO: estado
    boolean _state = false;
    // FIM: estado

    /**
     * Constructor
     */
    public PanelSelectVisibleColumns() throws IntrospectionException {
        try {
            jbInit();
            userInit();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Init visual components
     */
    private void jbInit() throws Exception {
        border1 = BorderFactory.createEtchedBorder(Color.white,new Color(148, 145, 140));
        titledBorder1 = new TitledBorder(BorderFactory.createEtchedBorder(Color.white,new Color(148, 145, 140)),"Seqüência de Colunas Visíveis");
        border2 = BorderFactory.createEtchedBorder(Color.white,new Color(148, 145, 140));
        titledBorder2 = new TitledBorder(border2,"Colunas Nao Visíveis");
        border3 = new TitledBorder(BorderFactory.createEtchedBorder(Color.white,new Color(148, 145, 140)),"Seqüência de Colunas Visíveis");
        this.setLayout(borderLayout2);
        _botaoOK.setText("OK");
        _botaoOK.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _botaoOK_actionPerformed(e);
            }
        });
        _botaoCancelar.setText("Cancelar");
        _botaoCancelar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _botaoCancelar_actionPerformed(e);
            }
        });
        _scrollPanePropriedadesVisiveis.setBorder(border3);
        _scrollPanePropriedadesVisiveis.setToolTipText("");
        _panelToolBarPropriedadesNaoVisiveis.setLayout(borderLayout3);
        _scrollPanePropriedadesNaoVisiveis.setBorder(titledBorder2);
        _botaoTornarVisivel.setText("Subir");
        _botaoTornarVisivel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _botaoTornarVisivel_actionPerformed(e);
            }
        });
        _botaoTornarNaoVisivel.setText("Descer");
        _botaoTornarNaoVisivel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _botaoTornarNaoVisivel_actionPerformed(e);
            }
        });
        _panelCancelarOK.add(_botaoOK, null);
        _panelCancelarOK.add(_botaoCancelar, null);
        this.add(_panelCancelarOK, BorderLayout.SOUTH);
        this.add(_splitPane, BorderLayout.CENTER);
        _panelToolBarPropriedadesNaoVisiveis.add(_panelToolBar, BorderLayout.NORTH);
        _panelToolBar.add(_botaoTornarVisivel, null);
        _panelToolBar.add(_botaoTornarNaoVisivel, null);
        _panelToolBarPropriedadesNaoVisiveis.add(_scrollPanePropriedadesNaoVisiveis, BorderLayout.CENTER);
        // splitPane
        _splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        _splitPane.add(_scrollPanePropriedadesVisiveis, JSplitPane.TOP);
        _splitPane.add(_panelToolBarPropriedadesNaoVisiveis, JSplitPane.BOTTOM);
        _splitPane.setDividerLocation(120);
    }

    /**
     * userInit
     */
    private void userInit() {
        _tablePropriedadesVisiveis.registerColumnSorter();
        _tablePropriedadesVisiveis.setRowHeight(20);
        _tablePropriedadesVisiveis.setDragEnabled(true);
        _tablePropriedadesNaoVisiveis.registerColumnSorter();
        _tablePropriedadesNaoVisiveis.setRowHeight(20);
        _modelPropriedadesVisiveis.setVisiblePropertySequence(new String[] {"title","descricao","name"});
        _modelPropriedadesNaoVisiveis.setVisiblePropertySequence(new String[] {"title","descricao","name"});
        _scrollPanePropriedadesNaoVisiveis.setViewportView(_tablePropriedadesNaoVisiveis);
        _scrollPanePropriedadesVisiveis.setViewportView(_tablePropriedadesVisiveis);
        _tablePropriedadesVisiveis.setToolTipText("Ctrl+Cima ou Ctrl+Baixo para mudar a ordem");
        _tablePropriedadesVisiveis.registerSortCommands();

        //
        this.connectModel();
    }

    public void setup(ArrayList visibleProperties, ArrayList nonVisibleProperties) {
        this.disconnectModel();
        _modelPropriedadesVisiveis.clear();
        _modelPropriedadesNaoVisiveis.clear();
        _modelPropriedadesVisiveis.addObject(visibleProperties);
        _modelPropriedadesNaoVisiveis.addObject(nonVisibleProperties);
        this.connectModel();
    }

    /**
     * set property as visible
     */
    private void setPropertyAsVisible(BeanProperty p) {
        int index = _modelPropriedadesNaoVisiveis.indexOf(p);
        if (index >= 0) {
            _modelPropriedadesNaoVisiveis.removeRow(index);
            _modelPropriedadesVisiveis.addObject(p);
        }
    }

    /**
     * set property as not visible
     */
    private void setPropertyAsNotVisible(BeanProperty p) {
        int index = _modelPropriedadesVisiveis.indexOf(p);
        if (index >= 0) {
            _modelPropriedadesVisiveis.removeRow(index);
            _modelPropriedadesNaoVisiveis.addObject(p);
        }
    }

    /**
     * connectModel
     */
    private void connectModel() {
        _tablePropriedadesVisiveis.setModel(_modelPropriedadesVisiveis);
        _tablePropriedadesNaoVisiveis.setModel(_modelPropriedadesNaoVisiveis);
    }

    /**
     * disconnectModel
     */
    private void disconnectModel() {
        _tablePropriedadesVisiveis.setModel(new DefaultTableModel());
        _tablePropriedadesNaoVisiveis.setModel(new DefaultTableModel());
    }

    /**
     * Set selected Properties as not visible...
     */
    void _botaoTornarNaoVisivel_actionPerformed(ActionEvent e) {
        // get...
        int[] rows = _tablePropriedadesVisiveis.getSelectedRows();
        Vector v = new Vector();
        for (int i=0;i<rows.length;i++)
            v.add(_modelPropriedadesVisiveis.getObject(rows[i]));

        // start expensive update
        disconnectModel();

        // do the hard work
        for (int i=0;i<v.size();i++)
            this.setPropertyAsNotVisible((BeanProperty) v.get(i));

        // finish expensive update
        connectModel();

        // select
        if (v.size() > 0) {
            int i0 = _modelPropriedadesNaoVisiveis.getRowCount()-v.size();
            int i1 = _modelPropriedadesNaoVisiveis.getRowCount()-1;
            _tablePropriedadesNaoVisiveis.getSelectionModel().setSelectionInterval(i0,i1);
            _tablePropriedadesNaoVisiveis.scrollRectToVisible(_tablePropriedadesNaoVisiveis.getCellRect(i1,0,true));
        }

        // request focus to the non visible table
        _tablePropriedadesNaoVisiveis.requestFocus();
    }

    /**
     * Set selected Properties as visible...
     */
    void _botaoTornarVisivel_actionPerformed(ActionEvent e) {
        // get selected rows
        int[] rows = _tablePropriedadesNaoVisiveis.getSelectedRows();
        Vector v = new Vector();
        for (int i=0;i<rows.length;i++)
            v.add(_modelPropriedadesNaoVisiveis.getObject(rows[i]));

        // start expensive update
        disconnectModel();

        // do the hard work
        for (int i=0;i<v.size();i++)
            this.setPropertyAsVisible((BeanProperty) v.get(i));

        // finish expensive update
        connectModel();

        // select
        if (v.size() > 0) {
            int i0 = _modelPropriedadesVisiveis.getRowCount()-v.size();
            int i1 = _modelPropriedadesVisiveis.getRowCount()-1;
            _tablePropriedadesVisiveis.getSelectionModel().setSelectionInterval(i0,i1);
            _tablePropriedadesVisiveis.scrollRectToVisible(_tablePropriedadesVisiveis.getCellRect(i1,0,true));
        }

        // request focus to the visible table
        _tablePropriedadesVisiveis.requestFocus();
    }


    /**
     * Id sequence of properties...
     */
    public BeanProperty[] getSequenceOfVisibleProperties() {
        BeanProperty result[] = new BeanProperty[_modelPropriedadesVisiveis.getRowCount()];
        for (int i=0;i<result.length;i++)
            result[i] = (BeanProperty) _modelPropriedadesVisiveis.getObject(i);
        return result;
    }

    /**
     * true if it was ok
     */
    public boolean getState() {
        return _state;
    }

    /**
     * OK
     */
    void _botaoOK_actionPerformed(ActionEvent e) {
        _state = true;
        finish();
    }

    /**
     * Cancelar
     */
    void _botaoCancelar_actionPerformed(ActionEvent e) {
        _state = false;
        finish();
    }

    ////////////////////////////////////////////////////////
    // Listener
    private Listener _listener;
    public interface Listener {
        public void finish();
    }
    public void finish() {
        if (_listener != null)
            _listener.finish();
    }
    public void setListener(Listener listener) {
        _listener = listener;
    }
    // Listener
    ////////////////////////////////////////////////////////
}
