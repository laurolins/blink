package linsoft.gui.util;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;

/**
 *
 */
public class PanelChooseObjects extends JPanel {

    JList _listSelected;
    JList _listUnselected;

    DefaultListModel _listModelSelected = new DefaultListModel();
    DefaultListModel _listModelUnselected = new DefaultListModel();

    public PanelChooseObjects(Object[] selectedData,
                              Object[] unselectedData,
                              ListCellRenderer renderer) {

        // adding elements to listModels
        for (Object o: selectedData) _listModelSelected.addElement(o);
        for (Object o: unselectedData) _listModelUnselected.addElement(o);

        // creating JLists
        _listSelected = new JList(_listModelSelected);
        _listUnselected = new JList(_listModelUnselected);

        // setting renderers
        if (renderer != null) {
            _listSelected.setCellRenderer(renderer);
            _listUnselected.setCellRenderer(renderer);
        }

        // setting list selecion type
        //_listSelected.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        //_listUnselected.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // create buttons and their actions
        JButton btnSelecionar = new JButton(">>");
        JButton btnRemover = new JButton("<<");
        btnSelecionar.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                selecionar();
            }
        });
        btnRemover.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                remover();
            }
        });


        // create buttons and their actions
        JButton btnUP = new JButton("cima");
        btnUP.setMargin(new Insets(2,2,2,2));
        btnUP.setPreferredSize(new Dimension(60,25));
        btnUP.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               up(_listModelSelected,_listSelected);
            }
        });

        JButton btnDOWN = new JButton("baixo");
        btnDOWN.setMargin(new Insets(2,2,2,2));
        btnDOWN.setPreferredSize(new Dimension(60,25));
        btnDOWN.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
              down(_listModelSelected,_listSelected);
            }
        });

        // layout components on the panel
        this.setLayout(new GridBagLayout());

        // Selecionados List
        this.add(new MySrollPane(_listUnselected,"Não-Selecionados"),
                 new GridBagConstraints(0,0,1,1,0.5,1,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(10,10,10,2),0,0));

        // Middle Buttons Panel
        JPanel panelMiddle = new JPanel();
        panelMiddle.setLayout(new GridBagLayout());
        panelMiddle.add(btnSelecionar,new GridBagConstraints(0,0,1,1,1,0.5,GridBagConstraints.CENTER,GridBagConstraints.NONE,new Insets(2,2,2,2),0,0));
        panelMiddle.add(btnRemover,new GridBagConstraints(0,1,1,1,1,0.5,GridBagConstraints.CENTER,GridBagConstraints.NONE,new Insets(2,2,2,2),0,0));
        this.add(panelMiddle,new GridBagConstraints(1,0,1,1,0,0,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(10,10,10,10),0,0));

        // Selecionados List
        this.add(new MySrollPane(_listSelected,"Selecionados"),
                 new GridBagConstraints(2,0,1,1,0.5,1,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(10,2,10,2),0,0));

        //
        JPanel panelUpDown = new JPanel();
        panelUpDown.setLayout(new GridBagLayout());
        panelUpDown.add(btnUP,new GridBagConstraints(0,0,1,1,1,0,GridBagConstraints.SOUTHWEST,GridBagConstraints.NONE,new Insets(2,2,6,2),0,0));
        panelUpDown.add(btnDOWN,new GridBagConstraints(0,1,1,1,1,0.5,GridBagConstraints.NORTHWEST,GridBagConstraints.NONE,new Insets(6,2,2,2),0,0));
        this.add(panelUpDown, new GridBagConstraints(3, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(16,0,10,10), 0, 0));
        // this.add(new JScrollPane(_listSelected),new GridBagConstraints(2,0,1,1,0.5,1,GridBagConstraints.CENTER,GridBagConstraints.BOTH,new Insets(10,10,10,10),0,0));
    }

    public PanelChooseObjects(java.util.List selectedData,
                              java.util.List unselectedData,
                              ListCellRenderer renderer) {
        this(selectedData.toArray(),unselectedData.toArray(),renderer);
    }

    public void selecionar() {
        move(_listModelUnselected,_listModelSelected,_listUnselected,_listSelected);
    }

    public void remover() {
        move(_listModelSelected,_listModelUnselected,_listSelected,_listUnselected);
    }

    public java.util.List getSelectedObjects() {
        ArrayList result = new ArrayList();
        for (int i=0;i<_listModelSelected.size();i++)
            result.add(_listModelSelected.getElementAt(i));
        return result;
    }

    private void move(DefaultListModel fromModel, DefaultListModel toModel, JList fromList, JList toList) {
        int[] indices = fromList.getSelectedIndices();
        int n = indices.length;
        for (int i=n-1;i>=0;i--) {
            int index = indices[i];
            Object o = fromModel.getElementAt(index);
            fromModel.removeElementAt(index);
            toModel.addElement(o);
        }
        int nn = toModel.size();
        toList.clearSelection();
        toList.addSelectionInterval(nn-n,nn-1);
        this.repaint();
    }

    public void up(DefaultListModel model, JList list) {
        if (list.getMinSelectionIndex() == 0)
            return;
        int[] indices = list.getSelectedIndices();
        int n = indices.length;
        int[] novosIndices = new int[indices.length];
        for (int i=n-1;i>=0;i--) {
            int index = indices[i];
            Object o = model.getElementAt(index);
            model.removeElementAt(index);
            model.add(index-1,o);
            novosIndices[i] = index-1;
        }
        list.setSelectedIndices(novosIndices);
        this.repaint();
    }

    public void down(DefaultListModel model, JList list) {
        int[] indices = list.getSelectedIndices();
        if (list.getMaxSelectionIndex() == model.getSize()-1)
            return;
        int n = indices.length;
        int[] novosIndices = new int[indices.length];
        for (int i=n-1;i>=0;i--) {
            int index = indices[i];
            Object o = model.getElementAt(index);
            model.removeElementAt(index);
            model.add(index+1,o);
            novosIndices[i] = index+1;
        }
        list.setSelectedIndices(novosIndices);
        this.repaint();
    }

    public static void main(String[] args) {
        JFrame f = new JFrame();
        f.setContentPane(
        new PanelChooseObjects(
            new Object[] {"Casa","Carro","Camelo"},
            new Object[] {"Viu","Nao","Sei"},
            null));

        f.setBounds(0,0,500,350);
        f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        f.setVisible(true);
    }


    class MySrollPane extends JScrollPane {
        public MySrollPane(Component c, String title) {
            super(c);
            this.setBorder(new javax.swing.border.TitledBorder(title));
            //this.setBorder(new javax.swing.border.TitledBorder(new javax.swing.border.LineBorder(Color.black,1),title));
        }
    }


}
