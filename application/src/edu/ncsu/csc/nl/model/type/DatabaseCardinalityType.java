package edu.ncsu.csc.nl.model.type;

/**
 * What are the different types of database relations that are possible?
 * 
 * @author John
 *
 */
public enum DatabaseCardinalityType {
	UNKNOWN("Unknown"),         // hasn't been answered yet
	ZERO("Zero"),
	ONE("One"),
	ZERO_TO_MANY("0:M"),
	ONE_TO_MANY("1:M"),
	UNSPECIFIED("Unspecified"); // we've looked at this field, but there's not enough information present to answer
	
	private String _label;
	
	private DatabaseCardinalityType(String label){
		_label = label;
	}
	
	public String toString() {
		return _label;
	}
	
	private static final DatabaseCardinalityType[] _selectableList = { UNKNOWN,ZERO, ONE, ZERO_TO_MANY, ONE_TO_MANY, UNSPECIFIED}; 
	
	public static DatabaseCardinalityType[] getSelectableList() {
		return _selectableList;
	}	
	
	

}
