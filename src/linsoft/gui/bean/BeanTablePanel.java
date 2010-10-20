package linsoft.gui.bean;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableModel;

/**
 * Painel em que há uma BeanTable.
 */
public class BeanTablePanel extends JPanel implements Serializable {

    // gui objects
    JScrollPane jScrollPane1 = new JScrollPane();
    GridBagLayout gridBagLayout1 = new GridBagLayout();
    protected BeanTable _table = new BeanTable();

    public BeanTablePanel() {
        try {
            jbInit();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
    }

    protected void jbInit() throws Exception {
        this.setLayout(gridBagLayout1);
        this.setPreferredSize(new Dimension(402, 300));
        jScrollPane1.getViewport().setBackground(Color.white);
        this.add(jScrollPane1, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
            GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
        jScrollPane1.getViewport().add(_table, null);
    }

    protected void setModel(TableModel model) {
        _table.setModel(model);
    }

    /**
	 * set editor
	 */
    public void setEditor(Class c, TableCellEditor edt) {
        _table.setDefaultEditor(c, edt);
    }

    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
    }

    private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException {
        ois.defaultReadObject();
    }
}