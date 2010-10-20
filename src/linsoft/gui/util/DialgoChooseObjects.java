package linsoft.gui.util;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

/**
 *
 */
public class DialgoChooseObjects extends JDialog {
    PanelChooseObjects _panelChooseObjects;
    public DialgoChooseObjects(
        JFrame owner,
        String title,
        boolean modal,
        Object[] selectedData,
        Object[] unselectedData,
        ListCellRenderer renderer) {
        super(owner,title,modal);
        _panelChooseObjects = new PanelChooseObjects(selectedData,unselectedData,renderer);

        // create buttons and their actions
        JButton btnOk = new JButton("OK");
        btnOk.setMargin(new Insets(2,2,2,2));
        btnOk.setPreferredSize(new Dimension(60,25));
        btnOk.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ok();
            }
        });


        JButton btnCancel = new JButton("Cancel");
        btnCancel.setMargin(new Insets(2,2,2,2));
        btnCancel.setPreferredSize(new Dimension(60,25));
        btnCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                cancel();
            }
        });

        this.setLayout(new BorderLayout());

        this.add(_panelChooseObjects,BorderLayout.CENTER);

        JPanel panelButtons = new JPanel();
        panelButtons.setLayout(new GridBagLayout());
        panelButtons.add(btnOk,new GridBagConstraints(0,0,1,1,1,0,GridBagConstraints.EAST,GridBagConstraints.NONE,new Insets(15,4,15,4),0,0));
        panelButtons.add(btnCancel,new GridBagConstraints(1,0,1,1,0,0,GridBagConstraints.EAST,GridBagConstraints.NONE,new Insets(15,4,15,78),0,0));
        this.add(panelButtons,BorderLayout.SOUTH);
    }

    public DialgoChooseObjects(
        JFrame owner,
        String title,
        boolean modal,
        java.util.List selectedData,
        java.util.List unselectedData,
        ListCellRenderer renderer) {
        this(owner,title,modal,selectedData.toArray(),unselectedData.toArray(),renderer);
    }

    private boolean _ok;
    private void ok() {
        _ok = true;
        this.setVisible(false);
    }
    private void cancel() {
        _ok = false;
        this.setVisible(false);
    }

    public boolean isOk() {
        return _ok;
    }

    public java.util.List getSelectedObjects() {
        return _panelChooseObjects.getSelectedObjects();
    }

    public static void main(String[] args) {
        DialgoChooseObjects d = new DialgoChooseObjects(null,"Dialog",false,new Object[] {"A Casa", "A Corrida"},new Object[] {"O Bolo"},null);
        d.setBounds(0,0,500,300);
        d.setVisible(true);
    }

}
