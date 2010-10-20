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

/**
 * Janela que mostra um painel para editar alguma coisa e botões
 * de confirmação e cancelamento.
 */
public class EditDialog extends JDialog {
    JPanel _panel = new JPanel();
    JButton _botaoOk = new JButton();
    JButton _botaoCancelar = new JButton();
    GridBagLayout gridBagLayout1 = new GridBagLayout();
    BeanTablePanel _editPanel = new BeanTablePanel();

    public EditDialog() {
        setModal(true);
        try {
            jbInit();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    public EditDialog(JFrame frame) {
        super(frame, true);
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
        this.getContentPane().add(_panel, BorderLayout.CENTER);
        _panel.add(_botaoOk,              new GridBagConstraints(0, 1, 1, 1, 0.5, 0.0
            ,GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 0, 5, 5), 0, 0));
        _panel.add(_botaoCancelar,            new GridBagConstraints(1, 1, 1, 1, 0.5, 0.0
            ,GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 0), 0, 0));
        _panel.add(_editPanel,       new GridBagConstraints(0, 0, 2, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
    }

    void _botaoOk_actionPerformed(ActionEvent e) {
        this.setVisible(false);
    }

    void _botaoCancelar_actionPerformed(ActionEvent e) {
        this.setVisible(false);
    }

    /**
     * Seta o painel de edição da janela.
     */
    public void setEditPanel(BeanTablePanel panel) {
        _panel.remove(_editPanel);
        _editPanel = panel;
        _panel.add(_editPanel,       new GridBagConstraints(0, 0, 2, 1, 1.0, 1.0
            ,GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
    }
}