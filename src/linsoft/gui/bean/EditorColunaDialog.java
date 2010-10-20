package linsoft.gui.bean;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.TableModel;

/**
 * Diálogo para editar a posição e a visibilidade das colunas
 * de um table model.
 */
public class EditorColunaDialog extends JDialog {
    JPanel _panel = new JPanel();
    JButton _botaoOk = new JButton();
    JButton _botaoCancelar = new JButton();
    GridBagLayout gridBagLayout1 = new GridBagLayout();
    JButton _botaoSubir = new JButton();
    JButton _botaoDescer = new JButton();
    JScrollPane _scrollPane = new JScrollPane();
    JTable _table = new JTable();

    public EditorColunaDialog() {
        setModal(true);
        try {
            jbInit();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public EditorColunaDialog(JFrame frame, String titulo) {
        super(frame, titulo, true);
        try {
            jbInit();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    protected void jbInit() throws Exception {
        _panel.setLayout(gridBagLayout1);
        _botaoOk.setMaximumSize(new Dimension(85, 27));
        _botaoOk.setMinimumSize(new Dimension(85, 27));
        _botaoOk.setPreferredSize(new Dimension(85, 27));
        _botaoOk.setText("Ok");
        _botaoOk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _botaoOk_actionPerformed(e);
            }
        });
        _botaoCancelar.setText("Cancelar");
        _botaoCancelar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _botaoCancelar_actionPerformed(e);
            }
        });
        _botaoSubir.setMaximumSize(new Dimension(71, 27));
        _botaoSubir.setMinimumSize(new Dimension(71, 27));
        _botaoSubir.setText("Sobe");
        _botaoSubir.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _botaoSubir_actionPerformed(e);
            }
        });
        _botaoDescer.setText("Desce");
        _botaoDescer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(ActionEvent e) {
                _botaoDescer_actionPerformed(e);
            }
        });
        this.getContentPane().add(_panel, BorderLayout.CENTER);
        _panel.add(_botaoOk,                     new GridBagConstraints(0, 2, 1, 1, 1.0, 0.0
            ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 0, 5, 5), 0, 0));
        _panel.add(_botaoCancelar,                             new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 0), 0, 0));
        _panel.add(_botaoSubir,        new GridBagConstraints(2, 0, 1, 1, 0.0, 1.0
            ,GridBagConstraints.SOUTH, GridBagConstraints.NONE, new Insets(0, 5, 5, 5), 0, 0));
        _panel.add(_botaoDescer,       new GridBagConstraints(2, 1, 1, 1, 0.0, 1.0
            ,GridBagConstraints.NORTH, GridBagConstraints.NONE, new Insets(5, 5, 0, 5), 0, 0));
        _panel.add(_scrollPane,        new GridBagConstraints(0, 0, 2, 2, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
        _scrollPane.getViewport().add(_table, null);

        _table.setModel(new ColunaTableModel());
    }

    void _botaoOk_actionPerformed(ActionEvent e) {
        ColunaTableModel model = (ColunaTableModel) _table.getModel();
        model.atualizarModelo();
        this.setVisible(false);
    }

    void _botaoCancelar_actionPerformed(ActionEvent e) {
        this.setVisible(false);
    }

    void _botaoSubir_actionPerformed(ActionEvent e) {
        int row = _table.getSelectedRow();
        if(row != -1) {
            ColunaTableModel model = (ColunaTableModel) _table.getModel();
            model.diminuirPosicao(row);
            _table.getSelectionModel().setSelectionInterval(row - 1, row - 1);
            model.fireTableRowsUpdated(row - 1, row);
        }
    }

    void _botaoDescer_actionPerformed(ActionEvent e) {
        int row = _table.getSelectedRow();
        if(row != -1) {
            ColunaTableModel model = (ColunaTableModel) _table.getModel();
            model.aumentarPosicao(row);
            _table.getSelectionModel().setSelectionInterval(row + 1, row + 1);
            model.fireTableRowsUpdated(row, row + 1);
        }
    }

    public void setTableModel(TableModel model) {
        ColunaTableModel colunaTableModel = (ColunaTableModel) _table.getModel();
        colunaTableModel.setTableModel(model);
    }
}