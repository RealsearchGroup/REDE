package edu.ncsu.csc.nl.model.type;


/**
 * Represents the different elements of an access control policy;
 * 
 * 
 * @author John
 *
 */
public enum AccessControlElement {

	SUBJECT("subject", true),
	ACTION("action", true),
	OBJECT("object", true),
	
	PREPOSITION("preposition", false);
	
	public static final AccessControlElement[] PRIMARY_ELEMENTS = {SUBJECT, ACTION, OBJECT};
	
	private String _label;
	private boolean _primary;
	
	private AccessControlElement(String label, boolean primary) {
		_label = label;
	}
	
	public String toString() {
		return _label;
	}
	
	public boolean isPrimary() {
		return _primary;
	}
	

}
