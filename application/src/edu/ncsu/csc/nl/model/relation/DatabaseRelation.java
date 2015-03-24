package edu.ncsu.csc.nl.model.relation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import edu.ncsu.csc.nl.model.Sentence;
import edu.ncsu.csc.nl.model.WordVertex;
import edu.ncsu.csc.nl.model.type.RelationSource;
import edu.ncsu.csc.nl.model.type.DatabaseCardinalityType;
import edu.ncsu.csc.nl.model.type.DatabaseRelationType;
import edu.ncsu.csc.nl.model.type.DatabaseRelationshipType;
import edu.ncsu.csc.nl.model.type.WordType;
import edu.ncsu.csc.nl.util.Logger;

/**
 * DatabaseRelation represents the different types that can make up a database relation.
 * While ideally, this should be four seperate types, it has been simplified into one
 * to make it easier for JSON parsing and to use as a model for the jTable.
 * 
 * JSON Representation notes:
 * - only export/convert to JSON, the vertex ID for the WordVertex objects.
 * - we will also export the correspsonding word list.  This will allow us to do
 *   some validation on the load.
 *
 * @author John Slankas
 *
 */
public class DatabaseRelation implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/** what are the primary node(s) for this relation? */
	private List<WordVertex> _identifyingNode;
	

	/** What type of relation is this?  entity, relationship, etc. */
	private DatabaseRelationType _relationType;
	
	
	/** If this is an attribute, what is the corresponding parent entity / relationship */
	private List<WordVertex> _parenEntityNode;	

	
	/* if this is a relationship, what type of a relationship is it? */
	private DatabaseRelationshipType _relationship;
	
	/** for a relationship, what is the first part of the relationship */
	private List<WordVertex> _firstEntityNode;	


	/** What is the cardinality of the first element in the relationship */
	private DatabaseCardinalityType _firstCardinality;
	
	/** for a relationship, what is the first part of the relationship */
	private List<WordVertex> _firstCardinalityNode;	


	/** for a relationship, what is the second part of the relationship */
	private List<WordVertex> _secondEntityNode;	


	/** What is the cardinality of the second element in the relationship */
	private DatabaseCardinalityType _secondCardinality;
	
	/** for a relationship, what is the second part of the relationship */
	private List<WordVertex> _secondCardinalityNode;	

	/** What sentences does this ACR derived from? */
	private ArrayList<Sentence> _sourceSentences = new ArrayList<Sentence>();
	
	/** Tracks the actual number of entities that belong to this relationship.
	 *  We only support two, but this allows us to see how often it otherwise occurs.
	 */
	private int _actualNumberOfEntities = -1;
		
	/** If this database element was derived, was it done so correctly? We will keep all of these records, but ignore in generated the final items  */
	private boolean _correctlyDerived = true;


	/** what was the source of this access control rule?  Was it derived or did some user enter it? */
	private RelationSource _source = RelationSource.UNKNOWN;
	
	
	/**
	 * Used to re-create the AccessControl object when it has been loaded from a file and the contained WordVertex 
	 * objects only contain the IDs.
	 *  
	 * @param ac
	 * @return
	 */
	public static DatabaseRelation createFromWordVertexWithIDsOnly(DatabaseRelation dbr, Sentence sentence) throws Exception {
		DatabaseRelation newDBR = new DatabaseRelation();

		newDBR.setIdentifyingNode(sentence.generateWordVertexListFromString(dbr.getIndentifyingNodeAsVertexIDs()));
		
		newDBR.setRelationType(dbr.getRelationType());
		
		newDBR.setParentEntityNode(sentence.generateWordVertexListFromString(dbr.getParentEntityNodeAsVertexIDs() ));
		
		newDBR.setRelationshipType(dbr.getRelationshipType());
		
		newDBR.setFirstEntityNode(sentence.generateWordVertexListFromString(dbr.getFirstEntityNodeAsVertexIDs() ));

		newDBR.setFirstCardinality(dbr.getFirstCardinality());
		newDBR.setFirstCardinalityNode(sentence.generateWordVertexListFromString(dbr.getFirstCardinalityNodeAsVertexIDs() ));
        
		newDBR.setSecondEntityNode(sentence.generateWordVertexListFromString(dbr.getSecondEntityNodeAsVertexIDs() ));

		newDBR.setSecondCardinality(dbr.getSecondCardinality());        
		newDBR.setSecondCardinalityNode(sentence.generateWordVertexListFromString(dbr.getSecondCardinalityNodeAsVertexIDs() ));
		
		newDBR.setActualNumberOfEntities (dbr.getActualNumberOfEntities());
		newDBR.setCorrectlyDerived(dbr.isCorrectlyDerived());
		newDBR.setSource(dbr.getSource());
		
		if (!newDBR.getIdentifyingNodeString().equals(dbr.getIdentifyingNodeString())) {
			Logger.log(Logger.LEVEL_ERROR, "IdentityNode, possible node ID mismatch - "+ dbr+","+dbr.getIndentifyingNodeAsVertexIDs() +" for "+sentence.getOriginalSentencePosition()+": "+sentence );
		}
		if (!newDBR.getParentEntityNodeString().equals(dbr.getParentEntityNodeString())) {
			Logger.log(Logger.LEVEL_ERROR, "ParentEntityNode, possible node ID mismatch - "+ dbr+","+dbr.getParentEntityNodeAsVertexIDs() +" for "+sentence.getOriginalSentencePosition()+": "+sentence );
		}		
		if (!newDBR.getFirstEntityNodeString().equals(dbr.getFirstEntityNodeString())) {
			Logger.log(Logger.LEVEL_ERROR, "FirstEntityNode, possible node ID mismatch - "+ dbr+","+dbr.getFirstEntityNodeAsVertexIDs() +" for "+sentence.getOriginalSentencePosition()+": "+sentence );
		}		
		if (!newDBR.getFirstCardinalityNodeString().equals(dbr.getFirstCardinalityNodeString())) {
			Logger.log(Logger.LEVEL_ERROR, "FirstCardinalityNode, possible node ID mismatch - "+ dbr+","+dbr.getFirstCardinalityNodeAsVertexIDs() +" for "+sentence.getOriginalSentencePosition()+": "+sentence );
		}			
		if (!newDBR.getSecondEntityNodeString().equals(dbr.getSecondEntityNodeString())) {
			Logger.log(Logger.LEVEL_ERROR, "SecondEntityNode, possible node ID mismatch - "+ dbr+","+dbr.getSecondEntityNodeAsVertexIDs() +" for "+sentence.getOriginalSentencePosition()+": "+sentence );
		}		
		if (!newDBR.getSecondCardinalityNodeString().equals(dbr.getSecondCardinalityNodeString())) {
			Logger.log(Logger.LEVEL_ERROR, "SecondCardinalityNode, possible node ID mismatch - "+ dbr+","+dbr.getSecondCardinalityNodeAsVertexIDs() +" for "+sentence.getOriginalSentencePosition()+": "+sentence );
		}		
		
		return newDBR;		
	}
	
	public DatabaseRelation() { 
		_identifyingNode              = new ArrayList<WordVertex>();
		_relationType                 = DatabaseRelationType.UNKNOWN;
		
		_parenEntityNode               = new ArrayList<WordVertex>();	
		
		_relationship = DatabaseRelationshipType.UNKNOWN;
		
		_firstEntityNode              =  new ArrayList<WordVertex>();	
        _firstCardinality             = DatabaseCardinalityType.UNKNOWN;
        _firstCardinalityNode              = new ArrayList<WordVertex>();	
        
		_secondEntityNode              =  new ArrayList<WordVertex>();	
        _secondCardinality             = DatabaseCardinalityType.UNKNOWN;        
		_secondCardinalityNode              = new ArrayList<WordVertex>();;			
	}
	
	public DatabaseRelation(DatabaseRelation originalObject) {
		this();

		_identifyingNode.addAll(originalObject.getIndentifyingNode());
		
		_relationType                 = originalObject.getRelationType();
		
		_parenEntityNode.addAll(originalObject.getParentEntityNode());
		
		_relationship = originalObject.getRelationshipType();
		
		_firstEntityNode.addAll(originalObject.getFirstEntityNode());
        _firstCardinality             = originalObject.getFirstCardinality();
        _firstCardinalityNode.addAll(originalObject.getSecondCardinalityNode());
        
		_secondEntityNode.addAll(originalObject.getSecondEntityNode());
        _secondCardinality             = originalObject.getSecondCardinality();       
		_secondCardinalityNode.addAll(originalObject.getSecondCardinalityNode());
		
		_actualNumberOfEntities = originalObject.getActualNumberOfEntities();
		_correctlyDerived       = originalObject.isCorrectlyDerived();
		_source                 = originalObject.getSource();
	}
	
	/**
	 * Using the DatabaseRelation defined in the original object, create
	 * a new database relation object by using the matching elements found 
	 * in the graph rooted at acwv
	 * 
	 * @param originalObject
	 * @param acwv
	 */
	public DatabaseRelation(DatabaseRelation originalObject, WordVertex acwv) {
		this();
		
		ArrayList<WordVertex>  tempList = new ArrayList<WordVertex>();
		for(WordVertex wv: originalObject.getIndentifyingNode()   ) {
			tempList.add( acwv.getVertexByID(wv.getID())); 		
		}
		this.setIdentifyingNode(tempList);

		tempList = new ArrayList<WordVertex>();
		for(WordVertex wv: originalObject.getParentEntityNode() ) {
			tempList.add( acwv.getVertexByID(wv.getID())); 		
		}
		this.setParentEntityNode(new ArrayList<WordVertex>(tempList));
		
		tempList = new ArrayList<WordVertex>();
		for(WordVertex wv: originalObject.getFirstEntityNode() ) {
			tempList.add( acwv.getVertexByID(wv.getID())); 		
		}
		this.setFirstEntityNode(tempList);
		
		tempList = new ArrayList<WordVertex>();
		for(WordVertex wv: originalObject.getFirstCardinalityNode() ) {
			tempList.add( acwv.getVertexByID(wv.getID())); 		
		}		
		this.setFirstCardinalityNode(tempList);
		
		this.setFirstCardinality(originalObject.getFirstCardinality());

		tempList = new ArrayList<WordVertex>();
		for(WordVertex wv: originalObject.getSecondEntityNode() ) {
			tempList.add( acwv.getVertexByID(wv.getID())); 		
		}
		this.setSecondEntityNode(tempList);

		tempList = new ArrayList<WordVertex>();
		for(WordVertex wv: originalObject.getSecondCardinalityNode() ) {
			tempList.add( acwv.getVertexByID(wv.getID())); 		
		}
		this.setSecondCardinalityNode(tempList);
		
		this.setSecondCardinality(originalObject.getSecondCardinality());			
		
		_relationType                 = originalObject.getRelationType();
				
		this.setSource(originalObject.getSource());
		this.setCorrectlyDerived(originalObject.isCorrectlyDerived());
	}	
	
	public DatabaseRelation(List<WordVertex> identifyingNode, List<WordVertex> parentNode, 
			                List<WordVertex> firstEntity, List<WordVertex> firstEntityCardinalityNode, DatabaseCardinalityType firstEntityCardinality,
			                List<WordVertex> secondEntity, List<WordVertex> secondEntityCardinalityNode, DatabaseCardinalityType secondEntityCardinality,
			                DatabaseRelationType relationType, DatabaseRelationshipType relationshipType,
			                boolean correctlyDerived, RelationSource source) {
		

		this.setIdentifyingNode(new ArrayList<WordVertex>(identifyingNode));
		this.setParentEntityNode(new ArrayList<WordVertex>(parentNode));
		
		this.setFirstEntityNode(firstEntity);
		this.setFirstCardinalityNode(firstEntityCardinalityNode);
		this.setFirstCardinality(firstEntityCardinality);

		this.setSecondEntityNode(firstEntity);
		this.setSecondCardinalityNode(firstEntityCardinalityNode);
		this.setSecondCardinality(firstEntityCardinality);		
		
		_relationType     = relationType;
		_relationship     = relationshipType;
		_correctlyDerived = correctlyDerived;
		_source           = source;
				
	}
	
	

	public List<WordVertex> getIndentifyingNode() {
		return _identifyingNode;
	}
	
	public void setIdentifyingNode(List<WordVertex> node) {
		_identifyingNode = new ArrayList<WordVertex>(node);
	}
	
	public String getIdentifyingNodeString() {
		return WordVertex.getListAsSingleWord(_identifyingNode, WordType.LEMMA);
		//return _identifyingNodeString;
	}	

	public DatabaseRelationType getRelationType() {
		return _relationType;
	}
	
	public void setRelationType(DatabaseRelationType type) {
		_relationType = type;
	}
		
	public List<WordVertex> getParentEntityNode() {
		return _parenEntityNode;
	}
	
	public void setParentEntityNode(List<WordVertex> node) {
		_parenEntityNode = new ArrayList<WordVertex>(node);
	}
	
	public String getParentEntityNodeString() {
		return WordVertex.getListAsSingleWord(_parenEntityNode, WordType.LEMMA);
	}
		
	
	public DatabaseRelationshipType getRelationshipType() {
		return _relationship;
	}
	
	public void setRelationshipType(DatabaseRelationshipType value) {
		_relationship = value;
	}
	
	public List<WordVertex> getFirstEntityNode() {
		return _firstEntityNode;
	}
	
	public void setFirstEntityNode(List<WordVertex> node) {
		_firstEntityNode = new ArrayList<WordVertex>(node);
	}
	
	public String getFirstEntityNodeString() {
		return WordVertex.getListAsSingleWord(_firstEntityNode, WordType.LEMMA);
	}
	

	public DatabaseCardinalityType getFirstCardinality() {
		return _firstCardinality;
	}
	
	public void setFirstCardinality(DatabaseCardinalityType cardinality) {
		_firstCardinality = cardinality;
	}
	
	public List<WordVertex> getFirstCardinalityNode() {
		return _firstCardinalityNode;
	}
	
	public void setFirstCardinalityNode(List<WordVertex> node) {
		_firstCardinalityNode = new ArrayList<WordVertex>(node);	
	}
	
	public String getFirstCardinalityNodeString() {
		return WordVertex.getListAsSingleWord(_firstCardinalityNode, WordType.LEMMA);
	}

	
	public List<WordVertex> getSecondEntityNode() {
		return _secondEntityNode;
	}
	
	public void setSecondEntityNode(List<WordVertex> node) {
		_secondEntityNode = new ArrayList<WordVertex>(node);		
	}
	
	public String getSecondEntityNodeString() {
		return WordVertex.getListAsSingleWord(_secondEntityNode, WordType.LEMMA);
	}
	
	public DatabaseCardinalityType getSecondCardinality() {
		return _secondCardinality;
	}
	
	public void setSecondCardinality(DatabaseCardinalityType cardinality) {
		_secondCardinality = cardinality;
	}
	
	public List<WordVertex> getSecondCardinalityNode() {
		return _secondCardinalityNode;
	}
	
	public void setSecondCardinalityNode(List<WordVertex> node) {
		_secondCardinalityNode = new ArrayList<WordVertex>(node);
	}
	
	public String getSecondCardinalityNodeString() {
		return WordVertex.getListAsSingleWord(_secondCardinalityNode, WordType.LEMMA);
	}
	
	// Junk code to load json files ....
	public void setIdentifyingNodeString(String newvalue) {
	}	

	public void setParentEntityNodeString(String newvalue) {
	}	
	
	public void setFirstEntityNodeString(String newValue) {
	}
		
	public void setFirstCardinalityNodeString(String newValue) {
		
	}	
	public void setSecondEntityNodeString(String newValue) {
	}
	
	public void  setSecondCardinalityNodeString(String newValue) {
	}
	// end of temporary code
	
	
	public int getActualNumberOfEntities() {
		return _actualNumberOfEntities;
	}
	
	public void setActualNumberOfEntities(int value) {
		_actualNumberOfEntities = value;
	}
	

	public boolean isCorrectlyDerived() {
		return _correctlyDerived;
	}
	
	public void setCorrectlyDerived(boolean value) {
		_correctlyDerived = value;
	}
	
	
	public RelationSource getSource() {
		return _source;
	}

	public void setSource(RelationSource source) {
		this._source = source;
	}	
	
	
	@JsonIgnore
	public String getIndentifyingNodeAsVertexIDs() {
		return WordVertex.getListAsVertexIDs(_identifyingNode);
	}
	
	@JsonIgnore
	public String getParentEntityNodeAsVertexIDs() {
		return WordVertex.getListAsVertexIDs(_parenEntityNode);
	}
	
	@JsonIgnore
	public String getFirstEntityNodeAsVertexIDs() {
		return WordVertex.getListAsVertexIDs(_firstEntityNode);
	}

	@JsonIgnore
	public String getFirstCardinalityNodeAsVertexIDs() {
		return WordVertex.getListAsVertexIDs(_firstCardinalityNode);
	}

	@JsonIgnore
	public String getSecondEntityNodeAsVertexIDs() {
		return WordVertex.getListAsVertexIDs(_secondEntityNode);
	}

	@JsonIgnore
	public String getSecondCardinalityNodeAsVertexIDs() {
		return WordVertex.getListAsVertexIDs(_secondCardinalityNode);
	}

	@JsonIgnore
	public Set<WordVertex> getAllVertices() {
		Set<WordVertex> result = new HashSet<WordVertex>();
	
		for (WordVertex wv: _identifyingNode) { result.add(wv); }
		if (_parenEntityNode       != null) { for (WordVertex wv: _parenEntityNode) { result.add(wv); } }
		if (_firstCardinalityNode  != null) { for (WordVertex wv: _firstCardinalityNode) { result.add(wv); } }
		if (_firstEntityNode       != null) { for (WordVertex wv: _firstEntityNode) { result.add(wv); } }
		if (_secondCardinalityNode != null) { for (WordVertex wv: _secondCardinalityNode) { result.add(wv); } }
		if (_secondEntityNode      != null) { for (WordVertex wv: _secondEntityNode) { result.add(wv); } }
		
		return result;
	}
	
	@JsonIgnore
	public boolean isComplete() {
				
		//
		if (_identifyingNode == null || _identifyingNode.size() == 0) { return false;}
		if (_relationType == DatabaseRelationType.ENTITY_ATTR) {
			if (_parenEntityNode == null || _parenEntityNode.size() == 0) { return false;}
		}
		
		if (_relationType == DatabaseRelationType.RELATIONSHIP) {
			if (_firstEntityNode == null  || _firstEntityNode.size() == 0) { return false;}
			if (_secondEntityNode == null || _secondEntityNode.size() == 0) { return false;}
			
		}
		
		return true;
	}
	
	/**
	 * Checks whether or not two relations are the same.  For this to be true
	 * - they must have the same type
	 * - if entities, the lemma must be the same.
	 * - if entity attributes, the lemmas for both the attribute and the entity must be the same
	 * - if relationship, lemma for the the relation must be the same.
	 *   - the two children must be the same (allow for either order
	 * 
	 */
	public boolean equals(Object o) {
		if (o == null || o instanceof DatabaseRelation == false) {
			return false;
		}
		DatabaseRelation otherDR = (DatabaseRelation) o;
		
		if (otherDR.getRelationType() != this.getRelationType()) {
			return false;
		}
		
		if (this.getRelationType() == DatabaseRelationType.ENTITY) {
			return (this.getIdentifyingNodeString().equalsIgnoreCase(otherDR.getIdentifyingNodeString()));
		}
		else if (this.getRelationType() == DatabaseRelationType.ENTITY_ATTR) {
			return ((this.getIdentifyingNodeString().equalsIgnoreCase(otherDR.getIdentifyingNodeString())) && 
					(this.getParentEntityNodeString().equalsIgnoreCase(otherDR.getParentEntityNodeString())));
		}
		else if (this.getRelationType() == DatabaseRelationType.RELATIONSHIP) {
			if (this.getIdentifyingNodeString().equalsIgnoreCase(otherDR.getIdentifyingNodeString()) == false) { return false; }
			if (this.getFirstEntityNodeString().equalsIgnoreCase(otherDR.getFirstEntityNodeString()) &&
				    this.getSecondEntityNodeString().equalsIgnoreCase(otherDR.getSecondEntityNodeString())) { return true; }
			if (this.getFirstEntityNodeString().equalsIgnoreCase(otherDR.getSecondEntityNodeString()) &&
				    this.getSecondEntityNodeString().equalsIgnoreCase(otherDR.getFirstEntityNodeString())) { return true; }
		}
		
		
		return false;
	}
	
	/**
	 * Checks whether or not two relations are the sam / can be combined.
	 * This is largely the same as the equals method, except a relationship can differ by it's label.
	 * 
	 * For this to be true
	 * - they must have the same type except for
	 *   if one is an entity and the other is an attribute.  Then the entity is subsummed to a beceome an attribute node instead
	 * - if entities, the lemma must be the same.
	 * - if entity attributes, the lemmas for both the attribute and the entity must be the same
	 * - if relationship, lemma for the the relation must be the same.
	 *   - the two children must be the same
	 * 
	 */
	public boolean isCombinable (DatabaseRelation otherDR) {
		
		if (otherDR.getRelationType() == DatabaseRelationType.ENTITY &&
			this.getRelationType()    == DatabaseRelationType.ENTITY_ATTR &&
			otherDR.getIdentifyingNodeString().equalsIgnoreCase(this.getIdentifyingNodeString())) {
			return true;
		}

		if (otherDR.getRelationType() == DatabaseRelationType.ENTITY_ATTR &&
			this.getRelationType()    == DatabaseRelationType.ENTITY &&
			otherDR.getIdentifyingNodeString().equalsIgnoreCase(this.getIdentifyingNodeString())) {
			return true;
		}

		
		if (otherDR.getRelationType() != this.getRelationType()) {
			return false;
		}
		
		if (this.getRelationType() == DatabaseRelationType.ENTITY) {
			return (this.getIdentifyingNodeString().equalsIgnoreCase(otherDR.getIdentifyingNodeString()));
		}
		else if (this.getRelationType() == DatabaseRelationType.ENTITY_ATTR) {
			return ((this.getIdentifyingNodeString().equalsIgnoreCase(otherDR.getIdentifyingNodeString())) && 
					(this.getParentEntityNodeString().equalsIgnoreCase(otherDR.getParentEntityNodeString())));
		}
		else if (this.getRelationType() == DatabaseRelationType.RELATIONSHIP) {
			if (this.getFirstEntityNodeString().equalsIgnoreCase(otherDR.getFirstEntityNodeString()) &&
				    this.getSecondEntityNodeString().equalsIgnoreCase(otherDR.getSecondEntityNodeString())) { return true; }
			if (this.getFirstEntityNodeString().equalsIgnoreCase(otherDR.getSecondEntityNodeString()) &&
				    this.getSecondEntityNodeString().equalsIgnoreCase(otherDR.getFirstEntityNodeString())) { return true; }		
		
		}
		
		return false;
	}
	
	
	/**
	 * Checks whether or not two relations are the sam / can be combined.
	 * This is largely the same as the equals method, except a relationship can differ by it's label.
	 * 
	 * For this to be true
	 * - they must have the same type except for
	 *   if one is an entity and the other is an attribute.  Then the entity is subsummed to a beceome an attribute node instead
	 * - if entities, the lemma must be the same.
	 * - if entity attributes, the lemmas for both the attribute and the entity must be the same
	 * - if relationship, lemma for the the relation must be the same.
	 *   - the two children must be the same
	 * 
	 * null is return if the two items were not mergeable (this check should have been made first!)
	 */
	public DatabaseRelation merge(DatabaseRelation otherDR) {
		
		if (otherDR.getRelationType() == DatabaseRelationType.ENTITY &&
			this.getRelationType()    == DatabaseRelationType.ENTITY_ATTR &&
			otherDR.getIdentifyingNodeString().equalsIgnoreCase(this.getIdentifyingNodeString())) {
			return this;
		}

		if (otherDR.getRelationType() == DatabaseRelationType.ENTITY_ATTR &&
			this.getRelationType()    == DatabaseRelationType.ENTITY &&
			otherDR.getIdentifyingNodeString().equalsIgnoreCase(this.getIdentifyingNodeString())) {
			return otherDR;
		}

		
		if (otherDR.getRelationType() != this.getRelationType()) {
			return null;
		}
		
		if (this.getRelationType() == DatabaseRelationType.ENTITY) {
			return this;
		}
		else if (this.getRelationType() == DatabaseRelationType.ENTITY_ATTR) {
			return this;
		}
		else if (this.getRelationType() == DatabaseRelationType.RELATIONSHIP) {
			return this;	
		
		}
		
		return null;
	}	
	
	
	
	
	/**
	 * Does this Database Relation object contain the passed in object?
	 * If so, it's feasible for us to ignore the passed in objects when applying patterns
	 * 
	 * The method checks the following patterns:
	 *    blank   ACTION RESOURCE
	 *    SUBJECT ACTION blank
	 *    blank   ACTION blank
	 * 
	 * 
	 * Side Note: If subject / action / resource agree, then isCombinable would be true.
	 * 
	 * @param other
	 * @return
	 */
	@JsonIgnore
	public boolean contains(DatabaseRelation other) {
		
		if (wordVertexListASubset(this.getIndentifyingNode(), other.getIndentifyingNode()) && 
			wordVertexListASubset(this.getParentEntityNode(), other.getParentEntityNode()) && 
			(wordVertexListASubset(this.getFirstEntityNode(),  other.getFirstEntityNode())  && 	wordVertexListASubset(this.getSecondEntityNode(), other.getSecondEntityNode())) ||
			(wordVertexListASubset(this.getFirstEntityNode(),  other.getSecondEntityNode())  && 	wordVertexListASubset(this.getSecondEntityNode(), other.getFirstEntityNode())) ) {
			return true;
		}
		
		//TODO ELEMENT
		/*
		
		//  blank   ACTION RESOURCE
		if (other.getSubjectVertexList().size() == 0 && 
			wordVertexListASubset(this.getActionVertexList(), other.getActionVertexList()) &&
			wordVertexListASubset(this.getObjectVertexList(), other.getObjectVertexList()) ) {
			return true;
		}
		
		// SUBJECT ACTION blank
		if (wordVertexListASubset(this.getSubjectVertexList(), other.getSubjectVertexList()) && 
			wordVertexListASubset(this.getActionVertexList(), other.getActionVertexList()) &&
			other.getObjectVertexList().size() == 0 ) {
			return true;
		}
		
		// blank ACTION blank
		if (other.getSubjectVertexList().size() == 0 && 
			wordVertexListASubset(this.getActionVertexList(), other.getActionVertexList()) &&
			other.getObjectVertexList().size() == 0 ) {
			return true;
		}		
	
		 */
		
		return false;
	}	
	
	public String toString() {
		switch (this.getRelationType()) {
			case ENTITY: return this.getIdentifyingNodeString();
			case ENTITY_ATTR: return this.getIdentifyingNodeString() +"(" + this.getParentEntityNodeString() +")";
			case RELATIONSHIP: return this.getIdentifyingNodeString() +"(" + this.getFirstEntityNodeString()+ ","+ // [ " +  this.getFirstCardinality() + " - " + this.getSecondCardinality() + " ] " +
			                          this.getSecondEntityNodeString() + ")";
			case RELATIONSHIP_ATTR: return this.getIdentifyingNodeString() +"(" + this.getParentEntityNodeString() +")";
			case UNKNOWN: return "unknown";
			default:      return "bad relationship type";
		}
	}
	
	
	/**
	 * returns true if b is a subset of a
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	private static boolean wordVertexListASubset(List<WordVertex> a, List<WordVertex> b) {
		for (WordVertex bWV: b) {
			boolean found = false;
			for (WordVertex aWV: a) {
				if (bWV.getID() == aWV.getID() || bWV.getLemma().equals(aWV.getLemma())) {
					found = true; break;
				}
			}
			if (!found) { return false;}
		}
		return true;
	}	
	
	
	@JsonIgnore
	public void addSourceSentence(Sentence s) {
		_sourceSentences.add(s);
	}
	
	@JsonIgnore
	public boolean hasSourceSentence(Sentence s) {
		return _sourceSentences.contains(s);
	}
	
	@JsonIgnore
	public List<Sentence> getSourceSentences() {
		return new ArrayList<Sentence>(_sourceSentences);
	}
	
}
