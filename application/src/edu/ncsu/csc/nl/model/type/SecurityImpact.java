package edu.ncsu.csc.nl.model.type;


/**
 * Represents a security object
 * 
 * 
 * @author John
 *
 */
public enum SecurityImpact {
	NOT_APPLICABLE("not applicable"),
	LOW("low"),
	MODERATE("moderate"),
	HIGH("high");
	
	private static final SecurityImpact[] _selectableList = { NOT_APPLICABLE,LOW,MODERATE, HIGH}; 
	
	private String _label;
	
	private SecurityImpact(String label) {
		_label = label;
	}
	
	public String toString() {
		return _label;
	}
	
	public static SecurityImpact[] getSelectableList() {
		return _selectableList;
	}

}
