package edu.ncsu.csc.nl.model.type;

/**
 * What are the different types of database relationships that are possible?
 * association, aggregation, composition, specialization (is-a / inheritance``) 
 * 
 * @author John
 *
 */
public enum DatabaseRelationshipType {

	UNKNOWN("Unknown"),
	ASSOCIATION("Association"),
	AGGREGATION("Aggregation"),
	COMPOSITION("Composition"),
	SPECIALIZATION("Inheritance");
	
	private String _label;
	
	private DatabaseRelationshipType(String label){
		_label = label;
	}
	
	public String toString() {
		return _label;
	}
	
	private static final DatabaseRelationshipType[] _selectableList = { UNKNOWN,ASSOCIATION, AGGREGATION, COMPOSITION, SPECIALIZATION}; 
	
	public static DatabaseRelationshipType[] getSelectableList() {
		return _selectableList;
	}	
}
