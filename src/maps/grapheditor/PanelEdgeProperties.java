/*
 * FrameProperties.java
 *
 * Created on January 2, 2008, 12:10 AM
 */
package maps.grapheditor;

import java.awt.Component;
import java.awt.Font;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import linsoft.Pair;
import maps.EmbeddedGraph;

/**
 *
 * @author  lauro
 */
public class PanelEdgeProperties extends javax.swing.JFrame {

    private EmbeddedGraph.Edge _edge;
    private DefaultListModel _model;

    /** Creates new form FrameProperties */
    public PanelEdgeProperties(EmbeddedGraph.Edge e) {
        // super((JDialog)null,true);
        initComponents();


        _edge = e;

        _model = new DefaultListModel();
        for (String key : e.getProperties().keySet()) {
            Object value = e.getProperties().get(key);
            if (value instanceof String) {
                _model.addElement(new Pair(key, value));
            }
        }
        
        _edtId.setText(""+_edge.getId());

        _list.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        _list.setModel(_model);
        _list.setCellRenderer(new DefaultListCellRenderer() {

            public Component getListCellRendererComponent(
                    JList list,
                    Object value,
                    int index,
                    boolean isSelected,
                    boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                Pair p = (Pair) value;
                this.setText(String.format("%-10s -> %s", (String) p.getFirst(), (String) p.getSecond()));
                return this;
            }
        });
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        _edtId = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        _list = new javax.swing.JList();
        _btnAdd = new javax.swing.JButton();
        _btnRemove = new javax.swing.JButton();
        _btnEdit = new javax.swing.JButton();
        _btnOk = new javax.swing.JButton();
        _btnCancel = new javax.swing.JButton();

        jLabel1.setText("id:");

        jLabel2.setText("properties:");

        _list.setModel(new javax.swing.AbstractListModel() {
            String[] strings = { "Item 1", "Item 2", "Item 3", "Item 4", "Item 5" };
            public int getSize() { return strings.length; }
            public Object getElementAt(int i) { return strings[i]; }
        });
        jScrollPane1.setViewportView(_list);

        _btnAdd.setText("Add");
        _btnAdd.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _btnAddActionPerformed(evt);
            }
        });

        _btnRemove.setText("Remove");
        _btnRemove.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _btnRemoveActionPerformed(evt);
            }
        });

        _btnEdit.setText("Edit");
        _btnEdit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _btnEditActionPerformed(evt);
            }
        });

        _btnOk.setText("OK");
        _btnOk.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _btnOkActionPerformed(evt);
            }
        });

        _btnCancel.setText("Cancel");
        _btnCancel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                _btnCancelActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jLabel2, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                            .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(_edtId, javax.swing.GroupLayout.PREFERRED_SIZE, 128, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGap(65, 65, 65)
                                .addComponent(_btnOk)
                                .addGap(18, 18, 18)
                                .addComponent(_btnCancel))
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 271, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(_btnEdit, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(_btnRemove, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(_btnAdd, javax.swing.GroupLayout.DEFAULT_SIZE, 70, Short.MAX_VALUE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {_btnCancel, _btnOk});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(_edtId, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel2)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(15, 15, 15)
                        .addComponent(_btnAdd)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(_btnRemove)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(_btnEdit))
                    .addGroup(layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 127, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(_btnOk)
                    .addComponent(_btnCancel))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    private void _btnRemoveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__btnRemoveActionPerformed
        int index = _list.getSelectedIndex();
        if (index == -1) {
            return;
        }

        _model.removeElementAt(index);
}//GEN-LAST:event__btnRemoveActionPerformed

    private void _btnEditActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__btnEditActionPerformed
        int index = _list.getSelectedIndex();
        Pair p = (Pair) _list.getSelectedValue();//GEN-LAST:event__btnEditActionPerformed
        if (p == null) {
            return;
        }

        String name = JOptionPane.showInputDialog("property name:", p.getFirst());
        if (name == null) {
            return;
        }
        String value = JOptionPane.showInputDialog("property value:", p.getSecond());
        if (value == null) {
            return;
        }

        _model.setElementAt(new Pair(name, value), index);

        _list.updateUI();
}

    private void _btnAddActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__btnAddActionPerformed
        String name = JOptionPane.showInputDialog("property name:");
        if (name == null) {
            return;
        }
        String value = JOptionPane.showInputDialog("property value:");
        if (value == null) {
            return;
        }
        _model.addElement(new Pair(name, value));
    }//GEN-LAST:event__btnAddActionPerformed

    private void _btnOkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__btnOkActionPerformed
        _edge.setId(Integer.parseInt(_edtId.getText()));
        _edge.clearProperties();
        for (int i=0;i<_model.size();i++) {
            Pair p = (Pair)_model.get(i);
            _edge.setProperty((String)p.getFirst(), (String)p.getSecond());
        }
        this.setVisible(false);        
    }//GEN-LAST:event__btnOkActionPerformed

    private void _btnCancelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event__btnCancelActionPerformed
        this.setVisible(false);        
    }//GEN-LAST:event__btnCancelActionPerformed

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
//        java.awt.EventQueue.invokeLater(new Runnable() {
//            public void run() {
//                new PanelEdgeProperties().setVisible(true);
//            }
//        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton _btnAdd;
    private javax.swing.JButton _btnCancel;
    private javax.swing.JButton _btnEdit;
    private javax.swing.JButton _btnOk;
    private javax.swing.JButton _btnRemove;
    private javax.swing.JTextField _edtId;
    private javax.swing.JList _list;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    // End of variables declaration//GEN-END:variables
}
