package linsoft.gui.bean;

import java.util.ArrayList;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class MBTMConfig {
    private static final String CURRENT_CONFIG_PROPERY = "__current";
    private static final String CONFIG_SETTINGS_NODE_NAME = "__settings";
    private static final String TABLE_ROW_HEIGHT_PROPERTY = "__height";
    private static final String COLUMN_WIDTH_PROPERTY = "width";
    private static final String PROTOTYPE_NAME_PREFIX = "*";
    private int _rowHeight;

    private String _path;
    private String _id;
    private String _currentConfigName;

    private ConfigLeaf _currentConfig;
    private ArrayList _prototypesNames = new ArrayList();
    private ArrayList _configsNames = new ArrayList();

    public MBTMConfig(String path, String id) throws BackingStoreException {
        _path = path;
        _id = id;
        this.loadFromRegistry();
    }

    public String get_id() { return _id; }
    public String get_path() { return _path; }

    private void loadFromRegistry() throws BackingStoreException {
        // root node
        Preferences root = Preferences.userRoot().node(_path);

        // table node (many configurations are possible from here)
        Preferences tableNode = null;

        //
        if (!root.nodeExists(_id)) {
            tableNode = root.node(_id);
            tableNode.put(CURRENT_CONFIG_PROPERY, "default");
        }
        else {
            tableNode = root.node(_id);
        }

        // get current table configuration name
        String st = (String) tableNode.get(CURRENT_CONFIG_PROPERY, "default");
        this.loadConfig(st);

        // add children to options
        String[] configs = tableNode.childrenNames();
        for (int i = 0; i < configs.length; i++) {
            if (configs[i].indexOf(PROTOTYPE_NAME_PREFIX) == 0) {
                _prototypesNames.add(configs[i].substring(PROTOTYPE_NAME_PREFIX.length(),configs[i].length()));
            }
            else {
                _configsNames.add(configs[i]);
            }
        }
    }

    /**
     * Save current model state on the current configuration.
     */
    public void copyFrom(MultipleBeanTableModel model) throws BackingStoreException {
        _currentConfig.copyFrom(model);
    }

    private void sync() throws BackingStoreException {
        Preferences.userRoot().node(_path).node(_id).sync();
    }

    /**
     * Set new currentConfig from a name and from the registry
     */
    private void loadConfig(String configName) throws BackingStoreException {
        _currentConfigName = configName;
        _currentConfig = new ConfigLeaf(this,configName,false);
    }

    public int getNumConfigs() {
        return _configsNames.size();
    }

    public String getConfigName(int i) {
        return (String) _configsNames.get(i);
    }

    public String getCurrentConfigName() {
        return _currentConfigName;
    }

    public ConfigLeaf getCurrentConfig() {
        return _currentConfig;
    }

    public int getNumPrototypes() {
        return _prototypesNames.size();
    }

    public String getPrototypeName(int i) {
        return (String) _prototypesNames.get(i);
    }

    /**
     * Setar configuracao corrente a partir do nome
     * da configuracao.
     */
    public void setCurrentConfig(String configName) throws BackingStoreException {
        this.loadConfig(configName);
    }

    /**
     * Adicionar uma nova configuracao protótipo.
     */
    public void addPrototype(String prototypeName, String[] sequence, int rowHeight) throws BackingStoreException {
        // warranty removal
        Preferences pNode = Preferences.userRoot().node(_path).node(_id).node(PROTOTYPE_NAME_PREFIX+prototypeName);
        pNode.removeNode();
        // pNode.flush();

        //
        pNode = Preferences.userRoot().node(_path).node(_id).node(PROTOTYPE_NAME_PREFIX+prototypeName);
        pNode.node(CONFIG_SETTINGS_NODE_NAME).put(TABLE_ROW_HEIGHT_PROPERTY,""+rowHeight);

        // guardar a sequencia de identificadores e suas configuracoes
        for (int i=0;i<sequence.length;i++) {
            // keys are from 0 to n-1
            pNode.put(""+i,sequence[i]);

            // no width info
        }

        //
        _prototypesNames.add(prototypeName);

        // this.sync();

        //
        if (!_prototypesNames.contains(prototypeName))
            _configsNames.add(prototypeName);
    }

    /**
     * Salvar informacao de configuracao corrente numa
     * nova configuracao dado o nome desta nova configuracao.
     */
    public void saveCurrentConfig(String newConfigName, boolean prototipo) throws BackingStoreException {
        ConfigLeaf cl = new ConfigLeaf(this,newConfigName,prototipo);
        cl.copyFrom(_currentConfig);
        if (!prototipo && !_configsNames.contains(newConfigName))
            _configsNames.add(newConfigName);
        else if (prototipo && !_prototypesNames.contains(newConfigName))
            _prototypesNames.add(newConfigName);
    }

    /**
     * Copiar informacao de configuracao prototipo
     * para a configuracao corrente.
     */
    public void copyFromPrototype(String prototipoName) throws BackingStoreException {
        ConfigLeaf cPrototype = new ConfigLeaf(this,PROTOTYPE_NAME_PREFIX+prototipoName,false);
        _currentConfig.copyFrom(cPrototype);
    }

    /**
     * Remover protoripo a partir do nome
     */
    public void removerPrototipo(String name) throws BackingStoreException {
        if (_prototypesNames.contains(name)) {
            Preferences node = Preferences.userRoot().node(_path).node(_id).node(PROTOTYPE_NAME_PREFIX+name);
            node.removeNode();
            // node.flush();
            _prototypesNames.remove(name);
        }
    }

}

