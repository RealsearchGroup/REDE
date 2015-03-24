package edu.ncsu.csc.nl.model.relation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import edu.ncsu.csc.nl.model.Sentence;
import edu.ncsu.csc.nl.model.WordEdge;
import edu.ncsu.csc.nl.model.WordVertex;
import edu.ncsu.csc.nl.model.type.DatabaseCardinalityType;
import edu.ncsu.csc.nl.model.type.DatabaseElement;
import edu.ncsu.csc.nl.model.type.DatabaseRelationType;
import edu.ncsu.csc.nl.model.type.DatabaseRelationshipType;
import edu.ncsu.csc.nl.model.type.RelationSource;
import edu.ncsu.csc.nl.model.type.PartOfSpeech;
import edu.ncsu.csc.nl.model.type.Relationship;
import edu.ncsu.csc.nl.util.Utility;

/**
 * DatabasePattern represents a particular graph pattern that can represent a database entity.  
 * There is a specific type field that contains which type of pattern this is...
 * 
 * There are three types of wildcards that can be used with patterns 
 * (currently on the nodes themselves).  Primarily used within the nodeEquals()
 * Types:
 *   '*' matches anything
 *   '+' matches any existing object of the same element (subject,action,resource/object)
 *   '%' matches anything of the same part of speech.
 * The wildcards are stored in the lemma value for a WordVertex
 * 
 * @author Adminuser
 */
public class  DatabasePattern {
	
	public static final String WILDCARD_ANYTHING = "*";
	public static final String WILDCARD_SAME_ELEMENT = "+";  //ie, existing subject/action/resource
	public static final String WILDCARD_SAME_PART_OF_SPEECH = "%";
	
	private WordVertex    _rootVertex;
	private DatabaseRelation _databaseItem;
	private DatabaseRelationType _patternType;
	
	/** In what sentences, does this access control pattern appear correctly? (ie, valid) */
	private ArrayList<Sentence> _validSentenceAppearances = new ArrayList<Sentence>();   

	private ArrayList<Sentence> _invalidSentenceAppearances = new ArrayList<Sentence>();   

	private ArrayList<Sentence> _negativeSentenceAppearances = new ArrayList<Sentence>();   

	private RelationSource _source;
	
	/** "temporary" flag to to save work by not constantly checking for transformations */
	private boolean _hasBeenCheckedForTransformations = false;
	
	/** how has this pattern been classified?   */
	private boolean _classification = true;
	
	/** what is the base pattern of this string.  this is the collapsed version minus node IDs.  Lazily-created */
	private String _stringPattern = null;
	
	/**
	 * 
	 * @param root
	 * @param databaseItem
	 * @param source
	 */
	public DatabasePattern(WordVertex root, DatabaseRelation databaseItem, RelationSource source, DatabaseRelationType patternType) {
		_rootVertex = root;
		_databaseItem = databaseItem;
		_source = source;
		_patternType = patternType;
	}
	
	/**
	 * Makes a deep copy of the passed in object with the exception of
	 * the passed in sentences
	 * 
	 * @param dbPattern
	 */
	public DatabasePattern(DatabasePattern dbPattern) {
		_rootVertex = (WordVertex) Utility.copy(dbPattern.getRoot());
		_databaseItem = new DatabaseRelation(dbPattern.getDatabaseItem(),_rootVertex);
		_source = dbPattern.getDatabaseSource();
		_classification = dbPattern.getClassification();
		_patternType = dbPattern.getPatternType();
	}
	
	/**
	 * Default constructor for JSON mapping
	 * 
	 * @return
	 */
	/*
	public AccessControlPattern() {
		
	}
	*/
	
	public WordVertex getRoot() {
		return _rootVertex;
	}
	
	public DatabaseRelation getDatabaseItem() {
		return _databaseItem;
	}
	
	public String[] getFactors(ArrayList<DatabaseFactor> factors) {
		
		return DatabaseFactor.getFactorValues(factors, this);
	}
	
	
	public DatabaseRelationType getPatternType() {
		return _patternType;
	}
	
	public void setPatternType(DatabaseRelationType type) {
		_patternType = type;
	}
	
	public boolean getClassification() {
		return _classification;
	}
	
	public void setClassification(boolean newValue) {
		_classification = newValue;
	}
	
	
	@JsonIgnore
	public boolean hasValidSentence(Sentence s) {
		return _validSentenceAppearances.contains(s);
	}

	@JsonIgnore
	public int getNumberOfValidSentences() {
		return _validSentenceAppearances.size();
	}
	
	@JsonIgnore
	public void removeValidSentence(Sentence s) {
		_validSentenceAppearances.remove(s);
	}
	
	@JsonIgnore
	public void addValidSentence(Sentence s) {
		_validSentenceAppearances.add(s);
	}	
	
	@JsonIgnore
	public Sentence[] getValidSentences() {
		return _validSentenceAppearances.toArray(new Sentence[0]);
	}
	
	@JsonIgnore
	public void setValidSentences(Sentence[] s) {
		_validSentenceAppearances = new ArrayList<Sentence>();
		Collections.addAll(_validSentenceAppearances, s);
	}
	
	
	@JsonIgnore
	public boolean hasInvalidSentenceByOriginalPosition(Sentence s) {
		return _invalidSentenceAppearances.contains(s);
	}

	@JsonIgnore
	public int getNumberOfInvalidSentences() {
		return _invalidSentenceAppearances.size();
	}
	
	@JsonIgnore
	public void removeInvalidSentence(Sentence s) {
		_invalidSentenceAppearances.remove(s);
	}
	
	@JsonIgnore
	public void addInvalidSentence(Sentence s) {
		_invalidSentenceAppearances.add(s);
	}	
	
	public Sentence[] getInvalidSentences() {
		return _invalidSentenceAppearances.toArray(new Sentence[0]);
	}
	
	public void setInvalidSentences(Sentence[] s) {
		_invalidSentenceAppearances = new ArrayList<Sentence>();
		Collections.addAll(_invalidSentenceAppearances, s);
	}
	
	@JsonIgnore
	public boolean hasWildcardElement() {
		return hasWildcardElement(this.getRoot(), new HashSet<Integer>());
	}
	
	
	@JsonIgnore
	private boolean hasWildcardElement(WordVertex node, HashSet<Integer> visitedNodes) {

		if (visitedNodes.contains(node.getID())) { 
			return false;
		}
		
		visitedNodes.add(node.getID());
		if (isWildcard(node)) { return true; }
				
		Iterator<WordEdge> children = node.getChildren();
		
		while (children.hasNext()){
			if (this.hasWildcardElement(children.next().getChildNode(), visitedNodes)) { return true; }
		}
		return false;
	}		
	
	
	
	@JsonIgnore
	public boolean hasNegativeSentence(Sentence s) {
		return _negativeSentenceAppearances.contains(s);
	}

	@JsonIgnore
	public int getNumberOfNegativeSentences() {
		return _negativeSentenceAppearances.size();
	}
	
	@JsonIgnore
	public void removeNegativeSentence(Sentence s) {
		_negativeSentenceAppearances.remove(s);
	}
	
	@JsonIgnore
	public void addNegativeSentence(Sentence s) {
		_negativeSentenceAppearances.add(s);
	}	
	
	@JsonIgnore
	public Sentence[] getNegativeSentences() {
		return _negativeSentenceAppearances.toArray(new Sentence[0]);
	}
	
	@JsonIgnore
	public void setNegativeSentences(Sentence[] s) {
		_negativeSentenceAppearances = new ArrayList<Sentence>();
		Collections.addAll(_negativeSentenceAppearances, s);
	}		
	
	public void removeAnyOccuranceOfSentence(Sentence s) {
		this.removeValidSentence(s);
		this.removeInvalidSentence(s);
		this.removeNegativeSentence(s);
	}
	
	public void resetSentenceOccurances() {
		_validSentenceAppearances    = new ArrayList<Sentence>();   
		_invalidSentenceAppearances  = new ArrayList<Sentence>();   
		_negativeSentenceAppearances = new ArrayList<Sentence>();   		
	}
	
	
	@JsonIgnore
	public int getTotalNumberOfSentences() {
		return _validSentenceAppearances.size() + _invalidSentenceAppearances.size() + _negativeSentenceAppearances.size();
	}
	
	public RelationSource getDatabaseSource() {
		return _source;
	}
	
	public void setDatabaseSource(RelationSource newSource) {
		_source = newSource;
	}	
	
	
	public String toString() {
		return this._databaseItem +": "+ this.getTotalNumberOfSentences()+"|"+this.getRoot().getStringRepresentation();
	}
	
	public String toStringCollapsed() {
		return this._databaseItem +": "+ this.getTotalNumberOfSentences()+"|"+this.getRoot().getStringRepresentationPOSCollapsed();
	}
	
	/**
	 * Produces a basic pattern of the representation, but with all elements wildcarded 
	 * (so just parts of speech and relationships)
	 * 
	 * @return
	 */
	public String toStringPattern() {
		//if (_stringPattern == null) {
			DatabasePattern newPattern = new DatabasePattern(this);
			
			//for a string pattern, we don't care about the exact word in a given location, so we need to wildcard it

			newPattern = DatabasePattern.createMatchingElementOnPartofSpeechPattern(newPattern, DatabaseElement.IDENTITY_NODE);
			newPattern = DatabasePattern.createMatchingElementOnPartofSpeechPattern(newPattern, DatabaseElement.PARENT_NODE);
			newPattern = DatabasePattern.createMatchingElementOnPartofSpeechPattern(newPattern, DatabaseElement.FIRST_REL_NODE);
			newPattern = DatabasePattern.createMatchingElementOnPartofSpeechPattern(newPattern, DatabaseElement.SECOND_REL_NODE);
			newPattern = DatabasePattern.createMatchingElementOnPartofSpeechPattern(newPattern, DatabaseElement.PREPOSITION);
			

			String pattern = newPattern.toStringCollapsed();   // This looks like "%;%;%: 0|(1 % VB root (2 % NNPS nsubj )(4 % NNS dobj ))".  Need to remove numbers
			_stringPattern = pattern.replaceAll("[0-9]","");
		//}
		return _stringPattern;
	}
		
	
	public boolean isIdentifyingNode(WordVertex wv) {
		return this.getDatabaseItem().getIndentifyingNode().contains(wv);
	}
	
	public boolean isParentNode(WordVertex wv) {
		return this.getDatabaseItem().getParentEntityNode().contains(wv);
	}
	
	public boolean isFirstEntityNode(WordVertex wv) {
		return this.getDatabaseItem().getFirstEntityNode().contains(wv);
	}
	
	public boolean isSecondEntityNode(WordVertex wv) {
		return this.getDatabaseItem().getSecondEntityNode().contains(wv);
	}
	
	public boolean isRelationshipNode(WordVertex wv) {
		return this.getDatabaseItem().getFirstEntityNode().contains(wv) || this.getDatabaseItem().getSecondEntityNode().contains(wv);
	}	
	
	
	/**
	 * vPattern 
	 * 
	 * 
	 * 
	 * @param v1
	 * @param vPattern  should belong to this access control object (ie, a child of the pattern's root)
	 * @param uniqueSubjects
	 * @param uniqueObjects
	 * @return
	 */
	public boolean nodeEquals(WordVertex v1, WordVertex vPattern, HashSet<String> uniqueEntities,  HashSet<String> uniqueAttributes, HashSet<String> uniqueRelations) {   
		if (vPattern.getLemma().equals(WILDCARD_ANYTHING)) {    // The '*' matches everything
			return true;
		}
		
		if (!v1.getPartOfSpeech().equalsCollapsed(vPattern.getPartOfSpeech())) { return false; }
		
		if (vPattern.getLemma().equals(WILDCARD_SAME_PART_OF_SPEECH)) {    // The '%' just checks that the part of speech is the same, which we did right above
			return true;
		}
		
		
		if (this.isIdentifyingNode(vPattern)) {
			if (vPattern.getLemma().equals(WILDCARD_SAME_ELEMENT)) {
				return uniqueEntities.contains(v1.getLemma()) || uniqueAttributes.contains(v1.getLemma());
			}
		}
		else if (this.isParentNode(vPattern)) { 
			if (vPattern.getLemma().equals(WILDCARD_SAME_ELEMENT)) {
				return uniqueEntities.contains(v1.getLemma());
			}
		}
		else if (this.isRelationshipNode(vPattern)) { 
			if (vPattern.getLemma().equals(WILDCARD_SAME_ELEMENT)) {
				return uniqueRelations.contains(v1.getLemma());
			}
		}		
		
		return v1.getLemma().equalsIgnoreCase(vPattern.getLemma()); 
	}	

	
	/**
	 * Does the passed in WordVertex have a wildcard?
	 * 
	 * @param a
	 * @return
	 */
	public static boolean isWildcard(WordVertex wv) {
		return wv.getLemma().equals(WILDCARD_ANYTHING) ||  wv.getLemma().equals(WILDCARD_SAME_ELEMENT) ||  wv.getLemma().equals(WILDCARD_SAME_PART_OF_SPEECH);
	}
	
	/**
	 * This tests whether or not two graph patterns are equivalent to each other....
	 * 
	 * First it tests the node, then the children.  The children do not need to come in the same order
	 * 
	 * @param a root (or subgraph) of a the first graph
	 * @param b root (or subgraph) of the second graph to examine
	 * @return
	 */
	public static boolean graphPatternsEquals(DatabasePattern dbA, DatabasePattern dbB, WordVertex a, WordVertex b, boolean exactMatch) {
		//System.out.println("A: "+a.getStringRepresentation());
		//System.out.println("B: "+b.getStringRepresentation());
		
		if (!a.getPartOfSpeech().equalsCollapsed(b.getPartOfSpeech())) { return false; }
		if (dbA.isIdentifyingNode(a) != dbB.isIdentifyingNode(b)) { return false; }
		if (dbA.isParentNode(a)  != dbB.isParentNode(b)) { return false; }
		if (dbA.isRelationshipNode(a)  != dbB.isRelationshipNode(b)) { return false; }

		if ( dbA.isIdentifyingNode(a) == false && dbA.isParentNode(a) == false && dbA.isRelationshipNode(a) == false &&  !a.getLemma().equalsIgnoreCase(b.getLemma())) { return false; } 
	
	
		if ( dbA.isIdentifyingNode(a) || dbA.isParentNode(a) || dbA.isRelationshipNode(a)) { 
			//if one of the nodes is defined as a wildcard, and the other not, then the patterns differ.
			if (isWildcard(a) != isWildcard(b)) {return false;}
			if (exactMatch &&  !a.getLemma().equalsIgnoreCase(b.getLemma())) { return false; }
		}
		
		if (a.getNumberOfChildren() != b.getNumberOfChildren()) { return false; }
		
		ArrayList<Integer> visitedNodesInB = new ArrayList<Integer>();
		
		for (int i=0; i< a.getNumberOfChildren(); i++) {
			WordEdge edgeA = a.getChildAt(i);
			int childPositionB = b.getChildRelationship(edgeA.getRelationship(), visitedNodesInB);
			if (childPositionB == -1) { return false; }
			if (edgeA.isWildcardRelationship() != b.getChildAt(childPositionB).isWildcardRelationship()) { return false; }
			
			visitedNodesInB.add(childPositionB);
			if (!DatabasePattern.graphPatternsEquals(dbA, dbB, edgeA.getChildNode(),  b.getChildAt(childPositionB).getChildNode(),exactMatch)) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Creates a new pattern from an existing pattern, but replaces any node of the specified element
	 * with a wildcard such that nodes would just match by part of speech for those nodes.
	 * 
	 * The existing pattern is not modified during this process
	 * 
	 * @param existingPattern
	 * @param elementToWildcard
	 * @return
	 */
	public static DatabasePattern createMatchingElementOnPartofSpeechPattern(DatabasePattern existingPattern, DatabaseElement elementToWildcard) {
		DatabasePattern newPattern = new DatabasePattern(existingPattern);
		
		replaceNodeWithWildcardPOS(newPattern,newPattern.getRoot(),elementToWildcard,new HashSet<Integer>());
		
		return newPattern;
	}
	
	private static void replaceNodeWithWildcardPOS(DatabasePattern pattern, WordVertex node, DatabaseElement elementToWildcard, HashSet<Integer> visitedNodes) {
		if (visitedNodes.contains(node.getID())) { return; }
		visitedNodes.add(node.getID());
		
		switch(elementToWildcard) {
			case IDENTITY_NODE:   if (pattern.isIdentifyingNode(node))   { node.setLemma(WILDCARD_SAME_PART_OF_SPEECH);  }  break;
			case PARENT_NODE:     if (pattern.isParentNode(node))        { node.setLemma(WILDCARD_SAME_PART_OF_SPEECH);  }  break;
			case FIRST_REL_NODE:  if (pattern.isFirstEntityNode(node))   { node.setLemma(WILDCARD_SAME_PART_OF_SPEECH);  }  break;
			case SECOND_REL_NODE: if (pattern.isSecondEntityNode(node))  { node.setLemma(WILDCARD_SAME_PART_OF_SPEECH);  }  break;
			case PREPOSITION:  	Iterator<WordEdge> iChild = node.getChildren();
								while (iChild.hasNext()) {
									WordEdge we = iChild.next();
									
									if (we.getRelationship().isPreposition() && (pattern.isIdentifyingNode(we.getChildNode()) ||
											                                     pattern.isParentNode(we.getChildNode()) ||
											                                     pattern.isFirstEntityNode(we.getChildNode()) ||
											                                     pattern.isSecondEntityNode(we.getChildNode()) )) {
										we.setWildcardRelationship(true);
									}
								}
								break;

		}
		
		Iterator<WordEdge> iChild = node.getChildren();
		while (iChild.hasNext()) {
			replaceNodeWithWildcardPOS(pattern, iChild.next().getChildNode(), elementToWildcard, visitedNodes);
		}
	}
	
	private static void setListToWildcard(List<WordVertex> list, String wildcard) {
		for (WordVertex wv: list) {
			wv.setLemma(wildcard);
		}
	}

	
	

	//TODO: create basic/initial patterns for Entity, Entity-Attribute, Relationship, and Relationship Attribute
	/**
	 * Creates the basic pattern used to match an Entity
	 * 
	 * @param accessWord
	 * @param permissions
	 * @return
	 */
	public static DatabasePattern createEntityPattern_nsubj(String entityWord) {
		//String sentence = accessWord;
		
		WordVertex rootWV   = new WordVertex(1, WILDCARD_SAME_PART_OF_SPEECH, WILDCARD_SAME_PART_OF_SPEECH, PartOfSpeech.VB, "", 1, 1, 2);
		WordVertex identifyWV  = new WordVertex(2, entityWord, entityWord, PartOfSpeech.NN, "", 1, 1, 1);

		WordEdge weRootToIdentity = new WordEdge(Relationship.NSUBJ, rootWV, identifyWV);
		
		identifyWV.addParent(weRootToIdentity);
		rootWV.addChild(weRootToIdentity);
		
		ArrayList<WordVertex> identList   = new ArrayList<WordVertex>();   identList.add(identifyWV);
		
		DatabaseRelation dr = new DatabaseRelation(identList, new ArrayList<WordVertex>(), new ArrayList<WordVertex>(), new ArrayList<WordVertex>(), DatabaseCardinalityType.UNKNOWN, new ArrayList<WordVertex>(), new ArrayList<WordVertex>(), DatabaseCardinalityType.UNKNOWN, DatabaseRelationType.ENTITY, DatabaseRelationshipType.UNKNOWN, true, RelationSource.SEED);
		DatabasePattern dbp = new DatabasePattern(rootWV, dr, RelationSource.SEED, DatabaseRelationType.ENTITY);
		
		//System.out.println(acp.getRoot().getStringRepresentation());
		
		return dbp;
	}	
	
	/**
	 * Creates the basic pattern used to match an Entity
	 * 
	 * @param accessWord
	 * @param permissions
	 * @return
	 */
	public static DatabasePattern createEntityPattern_dobj(String entityWord) {
		//String sentence = accessWord;
		
		WordVertex rootWV   = new WordVertex(1, WILDCARD_SAME_PART_OF_SPEECH, WILDCARD_SAME_PART_OF_SPEECH, PartOfSpeech.VB, "", 1, 1, 2);
		WordVertex identifyWV  = new WordVertex(2, entityWord, entityWord, PartOfSpeech.NN, "", 1, 1, 1);

		WordEdge weRootToIdentity = new WordEdge(Relationship.DOBJ, rootWV, identifyWV);
		
		identifyWV.addParent(weRootToIdentity);
		rootWV.addChild(weRootToIdentity);
		
		ArrayList<WordVertex> identList   = new ArrayList<WordVertex>();   identList.add(identifyWV);
		
		DatabaseRelation dr = new DatabaseRelation(identList, new ArrayList<WordVertex>(), new ArrayList<WordVertex>(), new ArrayList<WordVertex>(), DatabaseCardinalityType.UNKNOWN, new ArrayList<WordVertex>(), new ArrayList<WordVertex>(), DatabaseCardinalityType.UNKNOWN, DatabaseRelationType.ENTITY, DatabaseRelationshipType.UNKNOWN, true, RelationSource.SEED);
		DatabasePattern dbp = new DatabasePattern(rootWV, dr, RelationSource.SEED, DatabaseRelationType.ENTITY);
		
		return dbp;
	}		
	
	/**
	 * Creates the basic pattern used to match an Entity
	 * 
	 * @param accessWord
	 * @param permissions
	 * @return
	 */
	public static DatabasePattern createEntityPattern_prep(String entityWord) {
		//String sentence = accessWord;
		
		WordVertex rootWV   = new WordVertex(1, WILDCARD_SAME_PART_OF_SPEECH, WILDCARD_SAME_PART_OF_SPEECH, PartOfSpeech.VB, "", 1, 1, 2);
		WordVertex identifyWV  = new WordVertex(2, entityWord, entityWord, PartOfSpeech.NN, "", 1, 1, 1);

		WordEdge weRootToIdentity = new WordEdge(Relationship.PREP_FOR, rootWV, identifyWV);
		weRootToIdentity.setWildcardRelationship(true);
		
		identifyWV.addParent(weRootToIdentity);
		rootWV.addChild(weRootToIdentity);
		
		ArrayList<WordVertex> identList   = new ArrayList<WordVertex>();   identList.add(identifyWV);
		
		DatabaseRelation dr = new DatabaseRelation(identList, new ArrayList<WordVertex>(), new ArrayList<WordVertex>(), new ArrayList<WordVertex>(), DatabaseCardinalityType.UNKNOWN, new ArrayList<WordVertex>(), new ArrayList<WordVertex>(), DatabaseCardinalityType.UNKNOWN, DatabaseRelationType.ENTITY, DatabaseRelationshipType.UNKNOWN, true, RelationSource.SEED);
		DatabasePattern dbp = new DatabasePattern(rootWV, dr, RelationSource.SEED, DatabaseRelationType.ENTITY);
		
		//System.out.println(acp.getRoot().getStringRepresentation());
		
		return dbp;
	}	
	
	/**
	 * Creates the basic pattern used to match an Entity-Attribute
	 * 
	 * @param accessWord
	 * @param permissions
	 * @return
	 */
	public static DatabasePattern createEntityAttributePattern_poss(String entityWord, String attribute) {
		//String sentence = accessWord;
		
		WordVertex rootWV   = new WordVertex(1, attribute, attribute, PartOfSpeech.NN, "", 1, 1, 2);
		WordVertex identifyWV  = new WordVertex(2, entityWord, entityWord, PartOfSpeech.NN, "", 1, 1, 1);

		WordEdge weRootToIdentity = new WordEdge(Relationship.POSS, rootWV, identifyWV);
		
		identifyWV.addParent(weRootToIdentity);
		rootWV.addChild(weRootToIdentity);
		
		ArrayList<WordVertex> identList   = new ArrayList<WordVertex>();   identList.add(rootWV);
		ArrayList<WordVertex> parentList  = new ArrayList<WordVertex>();   identList.add(identifyWV);
		
		DatabaseRelation dr = new DatabaseRelation(identList, parentList, new ArrayList<WordVertex>(), new ArrayList<WordVertex>(), DatabaseCardinalityType.UNKNOWN, new ArrayList<WordVertex>(), new ArrayList<WordVertex>(), DatabaseCardinalityType.UNKNOWN, DatabaseRelationType.ENTITY_ATTR, DatabaseRelationshipType.UNKNOWN, true, RelationSource.SEED);
		DatabasePattern dbp = new DatabasePattern(rootWV, dr, RelationSource.SEED, DatabaseRelationType.ENTITY_ATTR);
		
		return dbp;
	}		
	
	/**
	 * Creates the basic pattern used to match an Entity-Attribute
	 * 
	 * @param accessWord
	 * @param permissions
	 * @return
	 */
	public static DatabasePattern createEntityAttributePattern_prepOf(String entityWord, String attribute) {
		//String sentence = accessWord;
		
		WordVertex rootWV   = new WordVertex(1, attribute, attribute, PartOfSpeech.NN, "", 1, 1, 2);
		WordVertex identifyWV  = new WordVertex(2, entityWord, entityWord, PartOfSpeech.NN, "", 1, 1, 1);

		WordEdge weRootToIdentity = new WordEdge(Relationship.PREP_OF, rootWV, identifyWV);
		
		identifyWV.addParent(weRootToIdentity);
		rootWV.addChild(weRootToIdentity);
		
		ArrayList<WordVertex> identList   = new ArrayList<WordVertex>();   identList.add(rootWV);
		ArrayList<WordVertex> parentList  = new ArrayList<WordVertex>();   identList.add(identifyWV);
		
		DatabaseRelation dr = new DatabaseRelation(identList, parentList, new ArrayList<WordVertex>(), new ArrayList<WordVertex>(), DatabaseCardinalityType.UNKNOWN, new ArrayList<WordVertex>(), new ArrayList<WordVertex>(), DatabaseCardinalityType.UNKNOWN, DatabaseRelationType.ENTITY_ATTR, DatabaseRelationshipType.UNKNOWN, true, RelationSource.SEED);
		DatabasePattern dbp = new DatabasePattern(rootWV, dr, RelationSource.SEED, DatabaseRelationType.ENTITY_ATTR);
		
		return dbp;
	}		

	
	/**
	 * Creates the basic pattern used to search for the initial set of resources and subjects.
	 * @param accessWord
	 * @param permissions
	 * @return
	 */
	public static DatabasePattern createRelationPattern_association(String entity1, String entity2) {
		//String sentence = accessWord;
		
		WordVertex identifyWV   = new WordVertex(1, WILDCARD_SAME_PART_OF_SPEECH, WILDCARD_SAME_PART_OF_SPEECH, PartOfSpeech.VB, "", 1, 1, 2);
		WordVertex firstRelationWV  = new WordVertex(2, entity1, entity1, PartOfSpeech.NN, "", 1, 1, 1);
		WordVertex secondRelationWV = new WordVertex(3, entity2, entity2, PartOfSpeech.NN, "", 1, 1, 3);
		
		WordEdge weidentifToFirstWV = new WordEdge(Relationship.NSUBJ, identifyWV, firstRelationWV);
		WordEdge weidentifToSecondWV = new WordEdge(Relationship.DOBJ, identifyWV, secondRelationWV);
		
		identifyWV.addChild(weidentifToFirstWV);
		identifyWV.addChild(weidentifToSecondWV);
		firstRelationWV.addParent(weidentifToFirstWV);
		secondRelationWV.addParent(weidentifToSecondWV);
		
		ArrayList<WordVertex> identList   = new ArrayList<WordVertex>();   identList.add(identifyWV);
		ArrayList<WordVertex> firstRelList  = new ArrayList<WordVertex>(); firstRelList.add(firstRelationWV);
		ArrayList<WordVertex> secondRelList = new ArrayList<WordVertex>(); secondRelList.add(secondRelationWV);
		
		DatabaseRelation dr = new DatabaseRelation(identList, new ArrayList<WordVertex>(), firstRelList, new ArrayList<WordVertex>(), DatabaseCardinalityType.UNKNOWN, secondRelList, new ArrayList<WordVertex>(), DatabaseCardinalityType.UNKNOWN, DatabaseRelationType.RELATIONSHIP, DatabaseRelationshipType.ASSOCIATION, true, RelationSource.SEED);
		DatabasePattern dbp = new DatabasePattern(identifyWV, dr, RelationSource.SEED, DatabaseRelationType.RELATIONSHIP);
		
		//System.out.println(acp.getRoot().getStringRepresentation());
		
		return dbp;
	}	
	
	/**
	 * Creates the basic pattern used to search for the initial set of resources and subjects.
	 * @param accessWord
	 * @param permissions
	 * @return
	 */
	public static DatabasePattern createRelationPattern_aggregation(String relationWord, String entity1, String entity2) {
		//String sentence = accessWord;
		
		WordVertex identifyWV   = new WordVertex(1, relationWord, relationWord, PartOfSpeech.VB, "", 1, 1, 2);
		WordVertex wholeWV  = new WordVertex(2, entity1, entity1, PartOfSpeech.NN, "", 1, 1, 1);
		WordVertex partWV = new WordVertex(3, entity2, entity2, PartOfSpeech.NN, "", 1, 1, 3);
		
		WordEdge weidentifToFirstWV = new WordEdge(Relationship.NSUBJ, identifyWV, wholeWV);
		WordEdge weidentifToSecondWV = new WordEdge(Relationship.DOBJ, identifyWV, partWV);
		
		identifyWV.addChild(weidentifToFirstWV);
		identifyWV.addChild(weidentifToSecondWV);
		wholeWV.addParent(weidentifToFirstWV);
		partWV.addParent(weidentifToSecondWV);
		
		ArrayList<WordVertex> identList   = new ArrayList<WordVertex>();   identList.add(wholeWV);
		ArrayList<WordVertex> wholeEntityList  = new ArrayList<WordVertex>(); wholeEntityList.add(wholeWV);
		ArrayList<WordVertex> partEntityList = new ArrayList<WordVertex>(); partEntityList.add(partWV);
		
		DatabaseRelation dr = new DatabaseRelation(identList, new ArrayList<WordVertex>(), wholeEntityList, new ArrayList<WordVertex>(), DatabaseCardinalityType.UNKNOWN, partEntityList, new ArrayList<WordVertex>(), DatabaseCardinalityType.UNKNOWN, DatabaseRelationType.RELATIONSHIP, DatabaseRelationshipType.AGGREGATION, true, RelationSource.SEED);
		DatabasePattern dbp = new DatabasePattern(identifyWV, dr, RelationSource.SEED, DatabaseRelationType.RELATIONSHIP);
		
		//System.out.println(acp.getRoot().getStringRepresentation());
		
		return dbp;
	}	
	
	/**
	 * Creates the basic pattern used to search for the initial set of resources and subjects.
	 * @param accessWord
	 * @param permissions
	 * @return
	 */
	public static DatabasePattern createRelationPattern_inheritance(String relationWord, String entity1, String entity2) {
		//String sentence = accessWord;
		
		WordVertex identifyWV   = new WordVertex(1, relationWord, relationWord, PartOfSpeech.VB, "", 1, 1, 2);
		WordVertex specificWV  = new WordVertex(2, entity1, entity1, PartOfSpeech.NN, "", 1, 1, 1);
		WordVertex generalWV = new WordVertex(3, entity2, entity2, PartOfSpeech.NN, "", 1, 1, 3);
		
		WordEdge weidentifToFirstWV = new WordEdge(Relationship.NSUBJ, identifyWV, specificWV);
		WordEdge weidentifToSecondWV = new WordEdge(Relationship.PREP_OF, identifyWV, generalWV);
		
		identifyWV.addChild(weidentifToFirstWV);
		identifyWV.addChild(weidentifToSecondWV);
		specificWV.addParent(weidentifToFirstWV);
		generalWV.addParent(weidentifToSecondWV);
		
		ArrayList<WordVertex> identList   = new ArrayList<WordVertex>();      identList.add(specificWV);
		ArrayList<WordVertex> wholeEntityList  = new ArrayList<WordVertex>(); wholeEntityList.add(specificWV);
		ArrayList<WordVertex> partEntityList = new ArrayList<WordVertex>();   partEntityList.add(generalWV);
		
		DatabaseRelation dr = new DatabaseRelation(identList, new ArrayList<WordVertex>(), wholeEntityList, new ArrayList<WordVertex>(), DatabaseCardinalityType.UNKNOWN, partEntityList, new ArrayList<WordVertex>(), DatabaseCardinalityType.UNKNOWN, DatabaseRelationType.RELATIONSHIP, DatabaseRelationshipType.AGGREGATION, true, RelationSource.SEED);
		DatabasePattern dbp = new DatabasePattern(identifyWV, dr, RelationSource.SEED, DatabaseRelationType.RELATIONSHIP);
		
		//System.out.println(acp.getRoot().getStringRepresentation());
		
		return dbp;
	}			
	
	

	@JsonIgnore
	public void setCheckedForTransformations() {
		_hasBeenCheckedForTransformations = true;
	}
	
	@JsonIgnore 
	public boolean hasBeenCheckedForTransformations() {
		return _hasBeenCheckedForTransformations;		
	}
}
