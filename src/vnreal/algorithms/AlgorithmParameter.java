package vnreal.algorithms;

import java.util.HashMap;
import java.util.Map.Entry;


public class AlgorithmParameter {
	
	private HashMap<String, String> parameters;
	
	public AlgorithmParameter() {
		this.parameters = new HashMap<String, String>();
	}
	
	public void put(String key, String value){
		this.parameters.put(key, value);
	}
	
	/**
	 * @deprecated Do not use this anymore.
	 * @param key
	 * @return
	 */
	@Deprecated
	public String get(String key) {
		return this.parameters.get(key);
	}
	
	public String getString(String key, String defVal) {
		String val = this.parameters.get(key);
		if (val != null) {
			return val;
		} else {
			return defVal;
		}
	}
	
	public Boolean getBoolean(String key, Boolean defVal) {
		String val = this.parameters.get(key);
		if (val != null) {
			return Boolean.parseBoolean(val);
		} else {
			return defVal;
		}
	}
	
	public Integer getInteger(String key, Integer defVal) {
		String val = this.parameters.get(key);
		if (val != null) {
			return Integer.parseInt(val);
		} else {
			return defVal;
		}
	}
	
	public Double getDouble(String key, Double defVal) {
		String val = this.parameters.get(key);
		if (val != null) {
			return Double.parseDouble(val);
		} else {
			return defVal;
		}
	}
	
	public String getSuffix(String prefix) {
		String result = "";
		for (Entry<String, String> e : parameters.entrySet()) {
			if (result.length() != 0)
				result += "_";
			result += prefix + e.getKey() + ":" + e.getValue();
		}
		return result;		
	}
	
	public String toString(String prefix) {
		String result = "";
		for (Entry<String, String> e : parameters.entrySet())
			result += prefix + e.getKey() + " = " + e.getValue() + "\n";
		return result;
	}
}
