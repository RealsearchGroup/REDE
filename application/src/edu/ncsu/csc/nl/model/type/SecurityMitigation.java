package edu.ncsu.csc.nl.model.type;


/**
 * Represents a security object
 * 
 * 
 * @author John
 *
 */
public enum SecurityMitigation {
	PREVENTION("prevention"),
	DETECTION("detection"),
	REACTION("reaction"),
	ADAPTATION("adaptation");
	
	private static final SecurityMitigation[] _selectableList = { PREVENTION,DETECTION,REACTION, ADAPTATION}; 
	
	private String _label;
	
	private SecurityMitigation(String label) {
		_label = label;
	}
	
	public String toString() {
		return _label;
	}
	
	public static SecurityMitigation[] getSelectableList() {
		return _selectableList;
	}

}
