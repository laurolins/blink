package blink.cli;

import java.util.HashMap;
import java.util.Set;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class FunctionMap {
    private HashMap<String,Function> _map;
    public FunctionMap() {
        _map = new HashMap<String,Function>();
    }

    public void addFunction(String key, Function value) {
        _map.put(key,value);
    }

    public Function getFunction(String key) {
        return _map.get(key);
    }

    public Set<String> getKeySet() {
        return _map.keySet();
    }

}
