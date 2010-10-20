package linsoft.gui.bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Table configuration cached data
 */
public class ConfigLeaf {
    private static final String CONFIG_SETTINGS_NODE_NAME = "__settings";
    private static final String TABLE_ROW_HEIGHT_PROPERTY = "__height";
    private static final String COLUMN_WIDTH_PROPERTY = "width";
    private static final String PROTOTYPE_NAME_PREFIX = "*";
    private String _name;
    private ArrayList _sequence = new ArrayList();
    private Map _map = new HashMap();
    private int _rowHeight;
    private boolean _isPrototype;
    private boolean _isPersistent;
    private MBTMConfig _configParent;

    /**
     * Constructor that queries the registry. If it doesn't exists, creates
     * a new default table configuration on registry and keep it cached.
     */
    public ConfigLeaf(MBTMConfig configParent, String configName, boolean prototipo) throws BackingStoreException {
        _configParent = configParent;
        _name = configName;
        _isPrototype = prototipo;
        this.load();
        this.setAsPersistent();
    }

    public String get_id() { return _configParent.get_id(); }
    public String get_path() { return _configParent.get_path(); }
    public boolean isPrototype() { return _isPrototype; }
    public Preferences getConfigNode() { return Preferences.userRoot().node(get_path()).node(get_id()).node(getConfigStoreName()); }
    public Preferences getTableNode() { return Preferences.userRoot().node(get_path()).node(get_id()); }
    public Preferences getSettingsNode() { return getConfigNode().node(CONFIG_SETTINGS_NODE_NAME); }
    public String getConfigStoreName() { return (_isPrototype ? PROTOTYPE_NAME_PREFIX+_name : _name); }

    /**
     * Set new currentConfig from a name and from the registry
     */
    private void load() throws BackingStoreException {
        //
        this.setAsNotPersistent();

        // clear current config cache
        _sequence.clear();
        _map.clear();

        // get current table configuration node
        Preferences tableNode = Preferences.userRoot().node(get_path()).node(get_id());

        Preferences currentConfigNode = null;
        if (!tableNode.nodeExists(getConfigStoreName())) {
            currentConfigNode = this.getConfigNode();

            // essa coluna sempre existe
            currentConfigNode.put("0",MultipleBeanTableModel.ROW_NUMBER_COLUMN_ID);
            Preferences settingsNode = currentConfigNode.node(CONFIG_SETTINGS_NODE_NAME);
            settingsNode.put(TABLE_ROW_HEIGHT_PROPERTY,"20");
        }
        else {
            currentConfigNode = tableNode.node(_name);
        }

        // get keys (suppose the keys a Integer strings from 0 to n-1
        String[] keys = currentConfigNode.keys();

        // add non-null objects...
        for (int i = 0; i < keys.length; i++) {
            String columnId = (String) currentConfigNode.get("" + i, null);
            this.add(columnId);
            if (currentConfigNode.nodeExists(columnId)) {
                Preferences p = currentConfigNode.node(columnId);

                // get keys (suppose the keys a Integer strings from 0 to n-1
                String[] properties = p.keys();
                for (int j=0;j<properties.length;j++) {
                    String value = (String) p.get(properties[j], null);
                    if (value != null)
                        this.setProperty(columnId, properties[j], value);
                }
            }
        }

        //
        try {
            _rowHeight = Integer.parseInt(currentConfigNode.node(CONFIG_SETTINGS_NODE_NAME).get(TABLE_ROW_HEIGHT_PROPERTY, "20"));
        }
        catch (NumberFormatException ex) {
            _rowHeight = 20;
        }

        //
        this.setAsPersistent();
    }

    /**
     * Copy current model state on the this configuration.
     */
    public void copyFrom(MultipleBeanTableModel model) throws BackingStoreException {
        _sequence.clear();
        _map.clear();

        //
        int rowHeight = this.getHeight();

        // warranty removal
        Preferences configNode = this.getConfigNode();
        configNode.removeNode();
        // configNode.flush();

        configNode = this.getConfigNode();

        // guardar a sequencia de identificadores e suas configuracoes
        for (int i=0;i<model.getNumberOfVisibleProperties();i++) {
            BeanProperty bp = model.getVisibleProperty(i);
            String columnId = bp.getName();

            // keys are from 0 to n-1
            configNode.put(""+i,columnId);

            // set width
            Preferences p = configNode.node(columnId);
            p.put(COLUMN_WIDTH_PROPERTY,""+bp.getWidth());
        }


        getSettingsNode().put(TABLE_ROW_HEIGHT_PROPERTY,""+rowHeight);
        // configNode.flush();

        // load again
        this.load();
    }

    /**
     * Copy current model state on the this configuration.
     */
    public void copyFrom(ConfigLeaf c) throws BackingStoreException {
        _sequence.clear();
        _map.clear();

        // warranty removal
        Preferences configNode = this.getConfigNode();
        configNode.removeNode();

        configNode = this.getConfigNode();

        //
        configNode.node(CONFIG_SETTINGS_NODE_NAME).put(TABLE_ROW_HEIGHT_PROPERTY,""+c.getHeight());

        // guardar a sequencia de identificadores e suas configuracoes
        for (int i=0;i<c.getNumColumns();i++) {
            String columnId = c.getColumnId(i);

            // keys are from 0 to n-1
            configNode.put(""+i,columnId);

            String stWidth = c.getValue(columnId,COLUMN_WIDTH_PROPERTY);
            if (stWidth != null) {
                Preferences p = configNode.node(columnId);
                p.put(COLUMN_WIDTH_PROPERTY, stWidth);
            }
        }

        //
        // configNode.flush();

        // load again
        this.load();
    }

    public void add(String columnId) {
        _sequence.add(columnId);
        if (this.isPersistent()) {
            Preferences currentTableNode = Preferences.userRoot().node(get_path()).node(get_id()).node(_name);
            currentTableNode.put(""+(_sequence.size()-1),""+columnId);
        }
    }

    public int getNumColumns(){
        return _sequence.size();
    }

    /**
     * return the name
     */
    public String getColumnId(int index){
        return (String) _sequence.get(index);
    }

    public void setProperty(String columnId, String property, String value) {
        Map map = (Map) _map.get(columnId);
        if (map == null)  {
            map = new HashMap();
            _map.put(columnId,map);
        }
        map.put(property,value);

        // make it persistent
        if (this.isPersistent()) {
            Preferences propertyNode = Preferences.userRoot().node(get_path()).node(get_id()).node(_name).node(columnId);
            propertyNode.put(property, value);
        }
    }

    public Map getProperties(String columnId) {
        return (Map) _map.get(columnId);
    }

    public String getValue(String columnId, String property) {
        String value = null;
        Map map = (Map) _map.get(columnId);
        if (map != null) {
            value = (String) map.get(property);
        }
        return value;
    }

    public void setAsNotPersistent() {
        _isPersistent = false;
    }

    public void setAsPersistent() {
        _isPersistent = true;
    }

    public boolean isPersistent() {
        return _isPersistent;
    }

    public int getHeight() {
        return _rowHeight;
    }

    public void setRowHeight(int h) {
        _rowHeight = h;
        if (isPersistent()) {
            Preferences settingsNode = Preferences.userRoot().node(get_path()).node(get_id()).node(_name).node(CONFIG_SETTINGS_NODE_NAME);
            settingsNode.put(TABLE_ROW_HEIGHT_PROPERTY,""+h);
        }
    }
}
