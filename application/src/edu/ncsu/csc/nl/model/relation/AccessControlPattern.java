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
import edu.ncsu.csc.nl.model.type.AccessControlElement;
import edu.ncsu.csc.nl.model.type.RelationSource;
import edu.ncsu.csc.nl.model.type.PartOfSpeech;
import edu.ncsu.csc.nl.model.type.Relationship;
import edu.ncsu.csc.nl.util.Utility;

/**
 * AccessControlPattern represents a particular graph pattern that
 * an access control marking generates.
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
public class AccessControlPattern {
	
	public static final String WILDCARD_ANYTHING = "*";
	public static final String WILDCARD_SAME_ELEMENT = "+";  //ie, existing subject/action/resource
	public static final String WILDCARD_SAME_PART_OF_SPEECH = "%";
	
	private WordVertex    _rootVertex;
	private AccessControlRelation _accessControl;
	
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
	
	public AccessControlPattern(WordVertex root, AccessControlRelation ac, RelationSource source) {
		_rootVertex = root;
		_accessControl = ac;
		_source = source;
	}
	
	/**
	 * Makes a deep copy of the passed in object with the exception of
	 * the passed in sentences
	 * 
	 * @param acp
	 */
	public AccessControlPattern(AccessControlPattern acp) {
		_rootVertex = (WordVertex) Utility.copy(acp.getRoot());
		_accessControl = new AccessControlRelation(acp.getAccessControl(),_rootVertex);
		_source = acp.getAccessControlSource();
		_classification = acp.getClassification();
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
	
	public AccessControlRelation getAccessControl() {
		return _accessControl;
	}
	
	public String[] getFactors(ArrayList<AccessControlFactor> factors) {
		
		return AccessControlFactor.getFactorValues(factors, this);
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
	
	public RelationSource getAccessControlSource() {
		return _source;
	}
	
	public void setAccessControlSource(RelationSource newSource) {
		_source = newSource;
	}	
	
	
	public String toString() {
		return this._accessControl +": "+ this.getTotalNumberOfSentences()+"|"+this.getRoot().getStringRepresentation();
	}
	
	public String toStringCollapsed() {
		return this._accessControl +": "+ this.getTotalNumberOfSentences()+"|"+this.getRoot().getStringRepresentationPOSCollapsed();
	}
	
	/**
	 * Produces a basic pattern of the representation, but with all elements
	 * (subjects/actions/objects/prepositions wildcarded 
	 * (so just parts of speech and relationships)
	 * 
	 * @return
	 */
	public String toStringPattern() {
		if (_stringPattern == null) {
			AccessControlPattern newPattern = new AccessControlPattern(this);
			newPattern = AccessControlPattern.createMatchingElementOnPartofSpeechPattern(newPattern, AccessControlElement.SUBJECT);
			newPattern = AccessControlPattern.createMatchingElementOnPartofSpeechPattern(newPattern, AccessControlElement.ACTION);
			newPattern = AccessControlPattern.createMatchingElementOnPartofSpeechPattern(newPattern, AccessControlElement.OBJECT);
			newPattern = AccessControlPattern.createMatchingElementOnPartofSpeechPattern(newPattern, AccessControlElement.PREPOSITION);

			String pattern = newPattern.toStringCollapsed();   // This looks like "%;%;%: 0|(1 % VB root (2 % NNPS nsubj )(4 % NNS dobj ))".  Need to remove numbers
			_stringPattern = pattern.replaceAll("[0-9]","");
		}
		return _stringPattern;
	}
		
	
	public boolean isSubjectNode(WordVertex wv) {
		return this.getAccessControl().getSubjectVertexList().contains(wv);
	}
	
	public boolean isActionNode(WordVertex wv) {
		return this.getAccessControl().getActionVertexList().contains(wv);
	}
	
	public boolean isObjectNode(WordVertex wv) {
		return this.getAccessControl().getObjectVertexList().contains(wv);
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
	public boolean nodeEquals(WordVertex v1, WordVertex vPattern, HashSet<String> uniqueSubjects,  HashSet<String> uniqueActions, HashSet<String> uniqueObjects) {   
		if (vPattern.getLemma().equals(WILDCARD_ANYTHING)) {    // The '*' matches everything
			return true;
		}
		
		if (!v1.getPartOfSpeech().equalsCollapsed(vPattern.getPartOfSpeech())) { return false; }
		
		if (vPattern.getLemma().equals(WILDCARD_SAME_PART_OF_SPEECH)) {    // The '%' just checks that the part of speech is the same, which we did right above
			return true;
		}
		
		
		if (this.isSubjectNode(vPattern)) { // just need to have the lemma in WordVertex as one of the keys in uniqueSubjects
			if (vPattern.getLemma().equals(WILDCARD_SAME_ELEMENT)) {
				return uniqueSubjects.contains(v1.getLemma());
			}
		}
		else if (this.isObjectNode(vPattern)) { // just need to have the lemma in WordVertex as one of the keys in uniqueSubjects
			if (vPattern.getLemma().equals(WILDCARD_SAME_ELEMENT)) {
				return uniqueObjects.contains(v1.getLemma());
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
	public static boolean graphPatternsEquals(AccessControlPattern acpA, AccessControlPattern acpB, WordVertex a, WordVertex b, boolean exactMatch) {
		//System.out.println("A: "+a.getStringRepresentation());
		//System.out.println("B: "+b.getStringRepresentation());
		
		if (!a.getPartOfSpeech().equalsCollapsed(b.getPartOfSpeech())) { return false; }
		if (acpA.isSubjectNode(a) != acpB.isSubjectNode(b)) { return false; }
		if (acpA.isObjectNode(a)  != acpB.isObjectNode(b)) { return false; }
		if (acpA.isActionNode(a)  != acpB.isActionNode(b)) { return false; }

		
		if ( acpA.isSubjectNode(a) == false && acpA.isObjectNode(a) == false &&  !a.getLemma().equalsIgnoreCase(b.getLemma())) { return false; } 
		
		if ( acpA.isSubjectNode(a) || acpA.isObjectNode(a) || acpA.isActionNode(a)) { 
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
			if (!AccessControlPattern.graphPatternsEquals(acpA, acpB, edgeA.getChildNode(),  b.getChildAt(childPositionB).getChildNode(),exactMatch)) {
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
	public static AccessControlPattern createMatchingElementOnPartofSpeechPattern(AccessControlPattern existingPattern, AccessControlElement elementToWildcard) {
		AccessControlPattern newPattern = new AccessControlPattern(existingPattern);
		
		replaceNodeWithWildcardPOS(newPattern,newPattern.getRoot(),elementToWildcard,new HashSet<Integer>());
		
		return newPattern;
	}
	
	private static void replaceNodeWithWildcardPOS(AccessControlPattern pattern, WordVertex node, AccessControlElement elementToWildcard, HashSet<Integer> visitedNodes) {
		if (visitedNodes.contains(node.getID())) { return; }
		visitedNodes.add(node.getID());
		
		switch(elementToWildcard) {
			case SUBJECT: if (pattern.isSubjectNode(node)) { node.setLemma(WILDCARD_SAME_PART_OF_SPEECH); } break;
			case ACTION:  if (pattern.isActionNode(node))  { node.setLemma(WILDCARD_SAME_PART_OF_SPEECH); } break;
			case OBJECT:  if (pattern.isObjectNode(node))  { node.setLemma(WILDCARD_SAME_PART_OF_SPEECH); } break;
			case PREPOSITION:  	Iterator<WordEdge> iChild = node.getChildren();
								while (iChild.hasNext()) {
									WordEdge we = iChild.next();
									if (we.getRelationship().isPreposition() && pattern.isObjectNode(we.getChildNode())) {
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

	/**
	 * Creates the basic pattern used to search for the initial set of resources and subjects.
	 * @param accessWord
	 * @param permissions
	 * @return
	 */
	public static AccessControlPattern createBasicPattern(String accessWord, String permissions) {
		//String sentence = accessWord;
		
		WordVertex actionWV   = new WordVertex(1, accessWord, accessWord, PartOfSpeech.VB, "", 1, 1, 2);
		WordVertex subjectWV  = new WordVertex(2, WILDCARD_SAME_PART_OF_SPEECH, WILDCARD_SAME_PART_OF_SPEECH, PartOfSpeech.NN, "", 1, 1, 1);
		WordVertex resourceWV = new WordVertex(3, WILDCARD_SAME_PART_OF_SPEECH, WILDCARD_SAME_PART_OF_SPEECH, PartOfSpeech.NN, "", 1, 1, 3);
		
		WordEdge weActionToSubject = new WordEdge(Relationship.NSUBJ, actionWV, subjectWV);
		WordEdge weActionToResource = new WordEdge(Relationship.DOBJ, actionWV, resourceWV);
		
		actionWV.addChild(weActionToSubject);
		actionWV.addChild(weActionToResource);
		subjectWV.addParent(weActionToSubject);
		resourceWV.addParent(weActionToResource);
		
		ArrayList<WordVertex> actionList   = new ArrayList<WordVertex>(); actionList.add(actionWV);
		ArrayList<WordVertex> subjectList  = new ArrayList<WordVertex>(); subjectList.add(subjectWV);
		ArrayList<WordVertex> resourceList = new ArrayList<WordVertex>(); resourceList.add(resourceWV);
		
		AccessControlRelation ac = new AccessControlRelation(subjectList, actionList, resourceList, permissions, null, null, true, RelationSource.SEED);
		AccessControlPattern acp = new AccessControlPattern(actionWV, ac, RelationSource.SEED);
		
		//System.out.println(acp.getRoot().getStringRepresentation());
		
		return acp;
	}
	
	/**
	 * Creates the basic pattern, but using a pronoun for the subject.
	 * All three nodes are wild
	 * @param actionWord
	 * @param permissions
	 * @param object
	 * 
	 * @return
	 */
	public static AccessControlPattern createBasicPatternWithPronominalSubject(String actionWord, String permissions, String object) {
		//String sentence = accessWord;
		
		WordVertex actionWV   = new WordVertex(1, actionWord, actionWord, PartOfSpeech.VB, "", 1, 1, 2);
		WordVertex subjectWV  = new WordVertex(2, WILDCARD_SAME_PART_OF_SPEECH, WILDCARD_SAME_PART_OF_SPEECH, PartOfSpeech.PRP, "", 1, 1, 1);
		WordVertex resourceWV = new WordVertex(3, object, object, PartOfSpeech.NN, "", 1, 1, 3);
		
		WordEdge weActionToSubject = new WordEdge(Relationship.NSUBJ, actionWV, subjectWV);
		WordEdge weActionToResource = new WordEdge(Relationship.DOBJ, actionWV, resourceWV);
		
		actionWV.addChild(weActionToSubject);
		actionWV.addChild(weActionToResource);
		subjectWV.addParent(weActionToSubject);
		resourceWV.addParent(weActionToResource);
		
		ArrayList<WordVertex> actionList   = new ArrayList<WordVertex>(); actionList.add(actionWV);
		ArrayList<WordVertex> subjectList  = new ArrayList<WordVertex>(); subjectList.add(subjectWV);
		ArrayList<WordVertex> resourceList = new ArrayList<WordVertex>(); resourceList.add(resourceWV);
		
		AccessControlRelation ac = new AccessControlRelation(subjectList, actionList, resourceList, permissions, null, null, true, RelationSource.SEED);
		AccessControlPattern acp = new AccessControlPattern(actionWV, ac, RelationSource.SEED);
		
		//System.out.println(acp.getRoot().getStringRepresentation());
		
		return acp;
	}	
	
	/**
	 * Creates the basic pattern with no subject
	 * All three nodes are wild
	 * @param actionWord
	 * @param permissions
	 * @param object
	 * 
	 * @return
	 */
	public static AccessControlPattern createDoubleNodeWithMissingSubjectPassive(String actionWord, String permissions) {
		//String sentence = accessWord;
		
		WordVertex actionWV   = new WordVertex(1, actionWord, actionWord, PartOfSpeech.VB, "", 1, 1, 2);
		WordVertex resourceWV = new WordVertex(2, WILDCARD_SAME_PART_OF_SPEECH, WILDCARD_SAME_PART_OF_SPEECH, PartOfSpeech.NN, "", 1, 1, 3);
		
		WordEdge weActionToResource = new WordEdge(Relationship.NSUBJPASS, actionWV, resourceWV);
		

		actionWV.addChild(weActionToResource);
		resourceWV.addParent(weActionToResource);
		
		ArrayList<WordVertex> actionList   = new ArrayList<WordVertex>(); actionList.add(actionWV);
		ArrayList<WordVertex> subjectList  = new ArrayList<WordVertex>(); 
		ArrayList<WordVertex> resourceList = new ArrayList<WordVertex>(); resourceList.add(resourceWV);
		
		AccessControlRelation ac = new AccessControlRelation(subjectList, actionList, resourceList, permissions, null, null, true, RelationSource.SEED);
		AccessControlPattern acp = new AccessControlPattern(actionWV, ac, RelationSource.SEED);
		
		//System.out.println(acp.getRoot().getStringRepresentation());
		
		return acp;
	}	
		
	/**
	 * Creates the basic pattern with no subject
	 * All three nodes are wild
	 * @param actionWord
	 * @param permissions
	 * @param object
	 * 
	 * @return
	 */
	public static AccessControlPattern createDoubleNodeWithMissingSubjectActive(String actionWord, String permissions) {
		//String sentence = accessWord;
		
		WordVertex actionWV   = new WordVertex(1, actionWord, actionWord, PartOfSpeech.VB, "", 1, 1, 2);
		WordVertex resourceWV = new WordVertex(2, WILDCARD_SAME_PART_OF_SPEECH, WILDCARD_SAME_PART_OF_SPEECH, PartOfSpeech.NN, "", 1, 1, 3);
		
		WordEdge weActionToResource = new WordEdge(Relationship.DOBJ, actionWV, resourceWV);
		

		actionWV.addChild(weActionToResource);
		resourceWV.addParent(weActionToResource);
		
		ArrayList<WordVertex> actionList   = new ArrayList<WordVertex>(); actionList.add(actionWV);
		ArrayList<WordVertex> subjectList  = new ArrayList<WordVertex>(); 
		ArrayList<WordVertex> resourceList = new ArrayList<WordVertex>(); resourceList.add(resourceWV);
		
		AccessControlRelation ac = new AccessControlRelation(subjectList, actionList, resourceList, permissions, null, null, true, RelationSource.SEED);
		AccessControlPattern acp = new AccessControlPattern(actionWV, ac, RelationSource.SEED);
		
		//System.out.println(acp.getRoot().getStringRepresentation());
		
		return acp;
	}	
	
	public void changePatternFromActiveToPassive() {
		this.changeActiveElementsToPassive(this.getAccessControl().getSubjectVertexList());
		this.changeActiveElementsToPassive(this.getAccessControl().getObjectVertexList());
	}

	/**
	 * 
	 * @param list
	 * @return
	 */
	private void changeActiveElementsToPassive(List<WordVertex> list) {
		for (WordVertex wv:list) {
			for (int i=0;i< wv.getNumberOfParents();i++) {
				WordEdge we = wv.getParentAt(i);
				if (we.getRelationship() == Relationship.NSUBJ) { we.changeRelationship(Relationship.AGENT); }
				if (we.getRelationship() == Relationship.DOBJ) { we.changeRelationship(Relationship.NSUBJPASS); }
			}
		}
	}	
	
	public void changePatternFromPassiveToActive() {
		this.changePassiveElementsToActive(this.getAccessControl().getSubjectVertexList());
		this.changePassiveElementsToActive(this.getAccessControl().getObjectVertexList());
	}
	/**
	 * 
	 * @param list
	 * @return
	 */
	private void changePassiveElementsToActive(List<WordVertex> list) {
		for (WordVertex wv:list) {
			for (int i=0;i< wv.getNumberOfParents();i++) {
				WordEdge we = wv.getParentAt(i);
				if (we.getRelationship() == Relationship.AGENT) { we.changeRelationship(Relationship.NSUBJ); }
				if (we.getRelationship() == Relationship.NSUBJPASS) { we.changeRelationship(Relationship.DOBJ); }
			}
		}
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
