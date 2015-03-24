package edu.ncsu.csc.nl.model.type;

/**
 * What are the different types of database relations that are possible?
 * 
 * @author John
 *
 */
public enum DatabaseRelationType {
	UNKNOWN("Unknown"),
	ENTITY("Entity"),
	ENTITY_ATTR("Entity-Attr"),
	RELATIONSHIP("Relationship"),
	RELATIONSHIP_ATTR("Relationship-Attr");
	
	
	private String _label;
	
	private DatabaseRelationType(String label){
		_label = label;
	}
	
	public String toString() {
		return _label;
	}

	private static final DatabaseRelationType[] _selectableList = { UNKNOWN, ENTITY, ENTITY_ATTR, RELATIONSHIP, RELATIONSHIP_ATTR}; 
	
	public static DatabaseRelationType[] getSelectableList() {
		return _selectableList;
	}

}
