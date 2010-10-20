package linsoft.gui.bean;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.HashMap;

/**
 * Keep a cache of class properties
 */
public class CacheClassBeanInfo {

    ////////////////////////////////////////////////
    // Singleton Pattern
    private static CacheClassBeanInfo _singleton;
    public static CacheClassBeanInfo getInstance() {
        if (_singleton == null) {
            _singleton = new CacheClassBeanInfo();
        }
        return _singleton;
    }
    // Singleton Pattern
    ////////////////////////////////////////////////

    /**
     * Map class to array of property descriptor.
     */
    private HashMap _map = new HashMap();

    public CacheClassBeanInfo() {
    }

    /**
     * Get bean info for the class
     */
    public BeanInfo getBeanInfo(Class c) throws IntrospectionException {
        // query
        BeanInfo bi = (BeanInfo) _map.get(c);

        // not cached
        if (bi == null) {
            bi = Introspector.getBeanInfo(c);
            _map.put(c,bi);
        }

        // return
        return bi;
    }

    /**
     * Get bean info for the class
     */
    public PropertyDescriptor[] getPropertiesDescriptors(Class c) throws IntrospectionException {
        return getBeanInfo(c).getPropertyDescriptors();
    }

    /**
     * Reset
     */
    public void clearCache() {
        _map.clear();
    }
}