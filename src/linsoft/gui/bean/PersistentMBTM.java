package linsoft.gui.bean;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.Window;
import java.beans.IntrospectionException;
import java.util.ArrayList;
import java.util.prefs.BackingStoreException;

import javax.swing.JDialog;

public class PersistentMBTM extends MultipleBeanTableModel {
    private String _path;
    private String _modelId;
    private MBTMConfig _config;

    public PersistentMBTM(Class c, String path, String modelId) throws IntrospectionException, BackingStoreException {
        super(c);
        _modelId = modelId;
        _path = path;
        _config = new MBTMConfig(_path,_modelId);
        this.loadFromConfig();
    }

    private void loadFromConfig() {
        // config
        MBTMConfig c = _config;

        // contar colunas válidas
        ArrayList ids = new ArrayList();
        for (int i = 0; i < c.getCurrentConfig().getNumColumns(); i++) {
            String id = c.getCurrentConfig().getColumnId(i);
            int index = this.findPropertyIndex(id);

            // column does not exist
            if (index == -1)
                continue;

            // add id
            ids.add(new Integer(index));

            //
            BeanProperty bp = this.getProperty(index);

            String widthSt = c.getCurrentConfig().getValue(id, "width");
            if (widthSt != null) {
                int width = 0;
                try {
                    width = Integer.parseInt(widthSt);
                    bp.setWidth(width);
                }
                catch (NumberFormatException ex) {
                    ex.printStackTrace();
                }
            }
            else {
                // setar para largura original
                bp.setWidth(bp.get_originalWidth());
            }
        }

        //
        int visiblePropertiesIndexes[] = new int[ids.size()];
        for (int i = 0; i < ids.size(); i++)
            visiblePropertiesIndexes[i] = ( (Integer) ids.get(i)).intValue();

            //
        this.setVisiblePropertySequence(visiblePropertiesIndexes);
    }

    // public void
    private JDialog _dialog;
    public void setupVisibleColumns(JDialog dialog) throws IntrospectionException, BackingStoreException {
        //
        _dialog = dialog;

        PanelSelectVisibleColumns p = new PanelSelectVisibleColumns();

        ArrayList visibleProperties = new ArrayList();
        for (int i=0;i<this.getNumberOfVisibleProperties();i++) {
            visibleProperties.add(this.getVisibleProperty(i));
        }
        ArrayList nonVisibleProperties = new ArrayList();
        for (int i=0;i<this.getNumberOfProperties();i++) {
            if (!visibleProperties.contains(this.getProperty(i)))
                nonVisibleProperties.add(this.getProperty(i));
        }
        p.setup(visibleProperties,nonVisibleProperties);
        p.setListener(new PanelSelectVisibleColumns.Listener() {
            public void finish() {
                _dialog.setVisible(false);
            }
        });

        dialog.setModal(true);
        dialog.getContentPane().add(p,null);
        dialog.setTitle("Escolher Colunas Visíveis");

        //
        resizeAndCenterWindow(dialog,640,480);
        dialog.setVisible(true);
        _dialog = null;

        // eh pra reordenar as colunas sim!
        if (p.getState()) {
            BeanProperty v[] = p.getSequenceOfVisibleProperties();
            super.setVisiblePropertySequence(v);
            _config.copyFrom(this);
        }
    }

    public static void resizeAndCenterWindow(Window w, int width, int height) {
        w.pack();
        w.setSize(
            (int) (w.getInsets().left + w.getInsets().right + width),
            (int) (w.getInsets().top + w.getInsets().bottom + height)
        );
        Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
        w.setLocation((int) (d.getWidth() - w.getWidth()) / 2, (int) (d.getHeight() - w.getHeight()) / 2);
    }

    /**
     * Set width on BeanProperty and save it
     */
    public boolean setVisibleColumnWidth(int index, int width) {
        if (super.setVisibleColumnWidth(index, width)) {
            _config.getCurrentConfig().setProperty(this.getVisibleProperty(index).getName(), "width", "" + width);
            return true;
        }
        else return false;
    }

    public int getNumConfigs() {
        return _config.getNumConfigs();
    }

    public String getConfig(int i) {
        return _config.getConfigName(i);
    }

    public String getCurrentConfigName() {
        return _config.getCurrentConfigName();
    }

    public void setCurrentConfig(String configName) throws BackingStoreException {
        _config.setCurrentConfig(configName);
        this.loadFromConfig();
    }

    public void copyFromPrototype(String prototypeName) throws BackingStoreException {
        _config.copyFromPrototype(prototypeName);
        this.loadFromConfig();
    }

    public void saveCurrentConfig(String newConfigName) throws BackingStoreException {
        _config.saveCurrentConfig(newConfigName,false);
    }

    public void saveCurrentConfigAsPrototype(String newConfigName) throws BackingStoreException {
        _config.saveCurrentConfig(newConfigName,true);
    }

    public int getNumPrototypes() {
        return _config.getNumPrototypes();
    }

    public String getPrototypeName(int i) {
        return _config.getPrototypeName(i);
    }

    public void addPrototype(String prototypeName, String[] sequence, int rowHeight) throws BackingStoreException {
        _config.addPrototype(prototypeName, sequence, rowHeight);
    }

    public int getHeight() {
        return _config.getCurrentConfig().getHeight();
    }

    public void setHeight(int height) {
        _config.getCurrentConfig().setRowHeight(height);
    }

    public void removerPrototype(String prototypeName) throws BackingStoreException {
        _config.removerPrototipo(prototypeName);
    }
}
