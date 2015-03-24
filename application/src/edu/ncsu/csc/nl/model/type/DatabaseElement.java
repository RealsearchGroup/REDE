package edu.ncsu.csc.nl.model.type;


/**
 * Represents the different elements of a item that represents part of the database modely;
 * 
 * 
 * @author John
 *
 */
public enum DatabaseElement {

	IDENTITY_NODE("identityNode", true),
	PARENT_NODE("parentNode", true),
	//RELATIONSHIP_NODE("relationshipNode", true),   //I'm not sure if I want this or not..  Depends if I need to operate on both at once ....
	FIRST_REL_NODE("firstRelationshipNode", true),
	SECOND_REL_NODE("secondRelationshipNode", true),
	
	PREPOSITION("preposition", false);
	
	public static final DatabaseElement[] PRIMARY_ELEMENTS = {IDENTITY_NODE, PARENT_NODE, FIRST_REL_NODE, SECOND_REL_NODE };
	
	private String _label;
	private boolean _primary;
	
	private DatabaseElement(String label, boolean primary) {
		_label = label;
	}
	
	public String toString() {
		return _label;
	}
	
	public boolean isPrimary() {
		return _primary;
	}
	

}
