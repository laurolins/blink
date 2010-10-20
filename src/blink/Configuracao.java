package blink;

import java.util.Vector;
import java.util.prefs.Preferences;

import javax.swing.JOptionPane;

import linsoft.gui.IHaveProperties;

public class Configuracao implements IHaveProperties {
    private Preferences _root;

    /**
     * Create a configuracao setting it's root node.
     * Everything will be under this node.
     */
    public Configuracao(String rootPath) {
        if (rootPath == null)
            _root = Preferences.userRoot();
        else
            _root = Preferences.userRoot().node(rootPath);
    }

    public Preferences getRoot() {
        return _root;
    }

    public String getDefaultValue(String propertyName) {
        return "";
    }

    /**
     * set property on the application root level
     */
    public void setProperty(String propertyName, String newValue) {
        this.setProperty(null, propertyName, newValue);
    }

    /**
     * Set property based on subnode of root
     */
    public void setProperty(String nodePath, String propertyName, String newValue) {
        try {
            // get node
            Preferences node = getRoot();
            if (nodePath != null)
                node = node.node(nodePath);

            // get current value
            String value = node.get(propertyName,"__noValue");

            // if there is already a value remove it
            // (doubt: why not overwrite)
            if (!value.equals("__noValue"))
                node.remove(propertyName);

            //
            node.put(propertyName, newValue);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * set property on the application root level
     */
    public String getProperty(String propertyName) {
        return getProperty(null,propertyName);
    }

    /**
     * Get property based on a one level subnode of root
     */
    public String getProperty(String nodePath, String propertyName) {
        String value = null;
        try {
            // get node
            Preferences node = getRoot();
            if (nodePath != null)
                node = node.node(nodePath);

            // get value
            value = node.get(propertyName, getDefaultValue(propertyName));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    /**
     * Get keys from a nodePath. (null for application root keys)
     */
    public String[] getKeys(String nodePath) {
        String[] keys = null;
        try {
            // get node
            Preferences node = getRoot();
            if (nodePath != null)
                node = node.node(nodePath);
            keys = node.keys();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return keys;
    }

    /**
     * Remove node from path
     */
    public void removeNode(String nodePath) {
        if (nodePath == null)
            return;
        try {
            // get node
            Preferences node = getRoot().node(nodePath);
            node.removeNode();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Get values for a node with keys ranging from "0" to "n-1".
     */
    public Vector getValues(String nodePath) {
        Vector result = new Vector();
        try {
            // get node
            Preferences node = getRoot();
            if (nodePath != null)
                node = node.node(nodePath);

            // get keys (suppose the keys a Integer strings from 0 to n-1
            String[] keys = node.keys();

            // add non-null objects...
            for (int i = 0; i < keys.length; i++) {
                Object obj = node.get("" + i, null);
                if (obj != null)
                    result.add(obj);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Setup a node with keys ranging from "0" to "n-1".
     */
    public void setValuesOnNode(String nodePath, String values[]) {
        try {
            // get node
            Preferences node = getRoot();
            if (nodePath != null)
                node = node.node(nodePath);

            // remove current keys
            String[] keys = node.keys();
            for (int i=0;i<keys.length;i++)
                node.remove(keys[i]);

            // add non-null objects...
            for (int i = 0; i < values.length; i++)
                node.put(""+i,values[i]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public static void main(String[] args) {
        String dbName = App.getProperty(App.DB_NAME_PROPERTY);
        String newName = JOptionPane.showInputDialog(null,"DB Name",dbName);
        if (newName != null) {
            App.setProperty(App.DB_NAME_PROPERTY,newName);
        }
        System.exit(0);
    }


}
