package linsoft.gui.bean;

import java.awt.Color;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.prefs.BackingStoreException;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

/**
 * This class is made to work with PersitentMBTM models
 */
public class PersistentBeanTable extends BeanTable {
    public PersistentBeanTable() {
        super();
    }

    public void setModel(TableModel model) {
        //
        super.setModel(model);

        if (!(model instanceof PersistentMBTM))
            return;

        // System.out.println("setModel() with PersistentMBTM");

        // prepare to listen mouse right clicks on table header
        MouseListener ml = new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                headerClick(e);
            }
        };
        this.getTableHeader().addMouseListener(ml);
        // this.getParent().addMouseListener(ml);

        //
        this.setHeight(this.getPersistentModel().getHeight());
    }

    /**
     * header click
     */
    private void headerClick(MouseEvent e) {
        if (e.getButton() == MouseEvent.BUTTON3) {
            JPopupMenu p = null;
            if (e.isControlDown() && e.isAltDown())
                p = prepareExpertMenu();
            else
                p = prepareUserMenu();
            p.show(this.getTableHeader() /*(Component) e.getSource()*/, e.getX(), e.getY());
            // System.out.println("Click on "+e.getSource());
        }
    }

    public void setHeight(int h) {
        this.setRowHeight(Math.max(h,1));
    }

    /**
     * On Creation of Columns from model...
     * @param model
     */
    private TableColumnModel _lastRegisteredColumnModel;
    private PropertyChangeListener _listener;
    public void createDefaultColumnsFromModel() {
        // System.out.println("createDefaultColumnsFromModel()");
        super.createDefaultColumnsFromModel();

        if (!(getModel() instanceof PersistentMBTM))
            return;

        // get table column model
        TableColumnModel cm = this.getColumnModel();

        //System.out.println("createDefaultColumnsFromModel() with PersistentMBTM");


        // listener
        if (_listener == null) {
            _listener = new PropertyChangeListener() {
                public void propertyChange(PropertyChangeEvent evt) {
                    // System.out.println("P Change");
                    if (/*evt.getPropertyName().equals("preferredWidth") || */evt.getPropertyName().equals("width")) {
                        TableColumn tc = (TableColumn) evt.getSource();
                        //System.out.println("Source " + evt.getSource());
                        //System.out.println("EvtName " + evt.getPropertyName());
                        //System.out.println("OldValue " + evt.getOldValue());
                        //System.out.println("NewValue " + evt.getNewValue());
                        updateMBTMColumnWidths(tc.getModelIndex(),((Integer)evt.getNewValue()).intValue());
                    }
                }
            };
        }

        // unregister on oldies...
        if (_lastRegisteredColumnModel != null) {
            for (int i=0;i<_lastRegisteredColumnModel.getColumnCount();i++) {
                TableColumn tc = _lastRegisteredColumnModel.getColumn(i);
                // System.out.println(""+tc+" UNregistering the listener");
                tc.removePropertyChangeListener(_listener);
            }
        }
        //
        this.registerListenerOnColumns();
    }

    public void registerListenerOnColumns() {
        // get table column model
        TableColumnModel cm = this.getColumnModel();

        // register on new one
        for (int i=0;i<cm.getColumnCount();i++) {
            TableColumn tc = cm.getColumn(i);

            // avoid double registering
            // System.out.println(""+tc+" unregistering then registering the listener");
            tc.removePropertyChangeListener(_listener);
            tc.addPropertyChangeListener(_listener);
        }
        _lastRegisteredColumnModel = cm;
    }


    /**
     * This method updates all the widths on A MultipleBeanTableModel
     * by the current width on TableColumnModel
     */
    private void updateMBTMColumnWidths(int col, int newValue) {
        // System.out.println("updateMBTMColumnWidths()");
        MultipleBeanTableModel m = (MultipleBeanTableModel)this.getModel();
        m.setVisibleColumnWidth(col, newValue);
    }

    public PersistentMBTM getPersistentModel() {
        return (PersistentMBTM) getModel();
    }

    private JPopupMenu prepareExpertMenu() {
        JPopupMenu p = new JPopupMenu();
        p.setBorder(BorderFactory.createLineBorder(Color.black, 1));
        PersistentMBTM m = (PersistentMBTM) this.getModel();
        /* p.add(new JLabel("Configuração"));
        for (int i=0;i<m.getNumConfigs();i++) {
            JMenuItem item = createActionMenuItem(new ActionChangeConfig(m.getConfig(i)));
            if (m.getConfig(i).equals(m.getCurrentConfigName()))
                item.setFont(new Font("Tahoma",Font.BOLD,10));
            p.add(item);
        }
        p.addSeparator(); */
        p.add(new JLabel("Configurações"));
        for (int i=0;i<m.getNumPrototypes();i++) {
            JMenuItem item = createActionMenuItem(new ActionCopyFromPrototype(m.getPrototypeName(i)));
            p.add(item);
        }
        p.addSeparator();
        p.add(new JLabel("Opções"));
        p.add(createActionMenuItem(new ActionSaveCurrentConfigToNewConfig("Salvar Nova Configuração")));
        p.add(createActionMenuItem(new ActionSetRowHeight("Altura das Linhas")));
        p.add(createActionMenuItem(new ActionRemoverPrototipo("Remover Configuração")));
        return p;
    }

    private JPopupMenu prepareUserMenu() {
        JPopupMenu p = new JPopupMenu();
        p.setBorder(BorderFactory.createLineBorder(Color.black, 1));
        PersistentMBTM m = (PersistentMBTM) this.getModel();
        for (int i=0;i<m.getNumPrototypes();i++) {
            JMenuItem item = createActionMenuItem(new ActionCopyFromPrototype(m.getPrototypeName(i)));
            p.add(item);
        }
        return p;
    }

    private JMenuItem createActionMenuItem(AbstractAction a) {
        JMenuItem mi = new JMenuItem(a);
        mi.setFont(new Font("Tahoma", Font.PLAIN, 11));
        mi.setToolTipText("");
        return mi;
    }

    class ActionChangeConfig extends AbstractAction {
        private String _configName;
        public ActionChangeConfig(String configName) {
            super(configName);
            _configName = configName;
        }
        public void actionPerformed(ActionEvent e) {
            try {
                getPersistentModel().setCurrentConfig(_configName);
                setHeight(getPersistentModel().getHeight());
            }
            catch (BackingStoreException ex) {
                ex.printStackTrace();
            }
        }
    }

    class ActionSaveCurrentConfigToNewConfig extends AbstractAction {
        public ActionSaveCurrentConfigToNewConfig(String name) {
            super(name);
        }
        public void actionPerformed(ActionEvent e) {
            saveNewPrototype();
        }
    }

    class ActionCopyFromPrototype extends AbstractAction {
        private String _prototypeName;
        public ActionCopyFromPrototype(String prototypeName) {
            super(prototypeName);
            _prototypeName = prototypeName;
        }
        public void actionPerformed(ActionEvent e) {
            copyFromPrototype(_prototypeName);
        }
    }

    class ActionSetRowHeight extends AbstractAction {
        public ActionSetRowHeight(String title) {
            super(title);
        }
        public void actionPerformed(ActionEvent e) {
            setHeightOfCurrentConfig();
        }
    }

    class ActionRemoverPrototipo extends AbstractAction {
        public ActionRemoverPrototipo(String name) {
            super(name);
        }
        public void actionPerformed(ActionEvent e) {
            removerPrototype();
        }
    }

    ////////////////////////////////////
    // Implementation of actions

    /**
     * Salvar a configuracao atual como protótipo.
     */
    public void saveNewPrototype() {
        try {
            Object obj = JOptionPane.showInputDialog(this.getTopLevelAncestor(),"Nome do Protótipo","modelo "+(getPersistentModel().getNumPrototypes()+1));
            if (obj != null) {
                getPersistentModel().saveCurrentConfigAsPrototype(obj.toString());
            }
        }
        catch (BackingStoreException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Alterar altura da linha na tabela.
     */
    public void setHeightOfCurrentConfig() {
        int h = getPersistentModel().getHeight();
        Object obj = JOptionPane.showInputDialog(this.getTopLevelAncestor(), "Nome do Protótipo", ""+h);
        if (obj != null) {
            String st = (String) obj;
            try {
                h = Integer.parseInt(st);
                getPersistentModel().setHeight(h);
                this.setHeight(getPersistentModel().getHeight());
            }
            catch (NumberFormatException ex1) {
                JOptionPane.showMessageDialog(this.getTopLevelAncestor(), "Entrada Inválida");
            }
        }
    }

    /**
     * Copiar Prototype para cima da configuracao atual.
     */
    private void copyFromPrototype(String name) {
        try {
            getPersistentModel().copyFromPrototype(name);
            setHeight(getPersistentModel().getHeight());
        }
        catch (BackingStoreException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Copiar Prototype para cima da configuracao atual.
     */
    private void removerPrototype() {
        PersistentMBTM m = getPersistentModel();
        Object obj[] = new Object[m.getNumPrototypes()];
        for (int i = 0; i < m.getNumPrototypes(); i++) {
            String st = m.getPrototypeName(i);
            obj[i] = st;
        }
        Object result = JOptionPane.showInputDialog(this.getTopLevelAncestor(), "Nome do Protótipo", "Protótipos", JOptionPane.PLAIN_MESSAGE, null, obj, null);
        if (result != null) {
            try {
                getPersistentModel().removerPrototype( (String) result);
            }
            catch (BackingStoreException ex) {
                ex.printStackTrace();
            }
        }
    }

}
