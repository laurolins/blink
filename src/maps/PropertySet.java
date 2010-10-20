package maps;

import java.util.HashMap;

public abstract class PropertySet {
	private HashMap<String,Object> _mapKeyValue = new HashMap<String,Object>();

	public Object getProperty(String key) {
		if (_mapKeyValue == null)
			return null;
		return _mapKeyValue.get(key);
	}
	
	public void setProperty(String key, Object value) {
		_mapKeyValue.put(key, value);
	}
	
	public String getPropertiesSaveString() {
		StringBuffer sb = new StringBuffer(); 
		for (String key: _mapKeyValue.keySet()) {
			Object value = _mapKeyValue.get(key);
			sb.append(key);
			sb.append("\t");
			if (value != null)
				sb.append(value.toString());
			else
				sb.append("");
			sb.append("\t");
		}
		return sb.toString();
	}
        
        public void clearProperties() {
            _mapKeyValue.clear();
        }
	
	public HashMap<String,Object> getProperties() {
		return _mapKeyValue;
	}
}
