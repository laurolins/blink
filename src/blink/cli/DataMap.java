//
//
//  Generated by StarUML(tm) Java Add-In
//
//  @ Project : Untitled
//  @ File Name : Data.java
//  @ Date : 10/30/2006
//  @ Author :
//
//
package blink.cli;

import java.util.HashMap;
import java.util.Set;


public class DataMap {
    private HashMap<String,Object> _map;
    public DataMap() {
        _map = new HashMap<String,Object>();
    }

    public void addData(String key, Object value) {
        _map.put(key,value);
    }

    public Object getData(String key) {
        return _map.get(key);
    }

    public Set<String> getKeySet() {
        return _map.keySet();
    }

}
