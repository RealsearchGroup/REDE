package edu.ncsu.csc.nl.model.relation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

import edu.ncsu.csc.nl.model.Sentence;
import edu.ncsu.csc.nl.model.WordVertex;
import edu.ncsu.csc.nl.model.english.Ambiguity;
import edu.ncsu.csc.nl.model.type.RelationSource;
import edu.ncsu.csc.nl.model.type.WordType;
import edu.ncsu.csc.nl.util.Utility;

/**
 * AccessControl represents the elements that make up an access control policy
 * 
 * JSON Representation notes:
 * - only export/convert to JSON, the vertex ID for the WordVertex objects.
 *   Thus, only a load of this object, we need to recreate the actual word vertex items
 *
 * @author John Slankas
 *
 */
public class AccessControlRelation implements Serializable {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private List<WordVertex> _subject;   //who's doing the action (ie, the person/role)
	
	private List<WordVertex> _action;
	
	private List<WordVertex> _object;    //object / entity that's being protected
	
	/** string of c,r,u, and/or d.  used to represent create, retrieve, update, delete */
	private String _permissions = "";
	
	/** Is the permission negative?  ie, the user shouldn't have it.  If so, which wordVertex represents it? */
	private WordVertex _negative = null;
	
	/** Should the access be limited to just the subject */
	private WordVertex _limitToSubject = null;
	
	/** If this access control element was derived, was it done so correctly? We will keep all of these records, but ignore in generated the final items  */
	private boolean _correctlyDerived = true;


	/** what was the source of this access control rule?  Was it derived or did some user enter it? */
	private RelationSource _source = RelationSource.UNKNOWN;
	
	
	/** What senteneces does this ACR derived from? */
	private ArrayList<Sentence> _sourceSentences = new ArrayList<Sentence>();
	
	/** This is set by a function within NLDocument that will resolve missing or ambiguous values. If blank, will default to the current value by the subject */
	private String _resolvedSubject = "";
	
	/** This is set by a function within NLDocument that will resolve missing or ambiguous values. If blank, will default to the current value by the subject */
	private String _resolvedObject = "";
	
	
	private String _resolvedSubjectReason = "";
	private String _resolvedObjectReason = "";
	
	/**
	 * Used to re-create the AccessControl object when it has been loaded from a file and the contained WordVertex 
	 * objects only contain the IDs.
	 *  
	 * @param ac
	 * @return
	 */
	public static AccessControlRelation createFromWordVertexWithIDsOnly(AccessControlRelation ac, Sentence sentence) throws Exception {
		AccessControlRelation newAC = new AccessControlRelation();

		newAC.setSubjectVertexList(sentence.generateWordVertexListFromString(ac.getSubjectAsVertexIDs()));
		newAC.setActionVertexList(sentence.generateWordVertexListFromString(ac.getActionAsVertexIDs()));
		newAC.setObjectVertexList(sentence.generateWordVertexListFromString(ac.getObjectAsVertexIDs()));
		newAC.setPermissions(ac.getPermissions());
		if (ac.getNegativeVertex() != null) {
			newAC.setNegativeVertex(sentence.getWordVertexAt(ac.getNegativeVertex().getID()-1));
		}
		if (ac.getLimitToSubjectVertex() != null) {
			newAC.setLimitToSubjectVertex(sentence.getWordVertexAt(ac.getLimitToSubjectVertex().getID()-1));
		}
		newAC.setSource(ac.getSource());
		newAC.setCorrectlyDerived(ac.isCorrectlyDerived());
		
		return newAC;		
	}
	
	public AccessControlRelation() { 
		_subject = new ArrayList<WordVertex>();  
		_action  = new ArrayList<WordVertex>();
		_object = new ArrayList<WordVertex>(); 
	}
	
	public AccessControlRelation(AccessControlRelation originalObject) {
		this();
		_subject.addAll(originalObject.getSubjectVertexList() ); 
		_action.addAll(originalObject.getActionVertexList());
		_object.addAll(originalObject.getObjectVertexList());
		
		_permissions    = originalObject.getPermissions();
		_negative       = originalObject.getNegativeVertex();
		_limitToSubject = originalObject.getLimitToSubjectVertex();
		
		this.setSource(originalObject.getSource());
		this.setCorrectlyDerived(originalObject.isCorrectlyDerived());
	}
	
	/**
	 * Using the accessControl defined in the original object, create
	 * a new access control object by using the matching elements found 
	 * in the graph rooted at acwv
	 * 
	 * @param originalObject
	 * @param acwv
	 */
	public AccessControlRelation(AccessControlRelation originalObject, WordVertex acwv) {
		this();
		for(WordVertex wv: originalObject.getSubjectVertexList()) {
			_subject.add( acwv.getVertexByID(wv.getID())); 		
		}

		for(WordVertex wv: originalObject.getActionVertexList()) {
			_action.add( acwv.getVertexByID(wv.getID()));
		}		

		for(WordVertex wv: originalObject.getObjectVertexList()) {
			_object.add( acwv.getVertexByID(wv.getID()));
		}		
		
		_permissions    = originalObject.getPermissions();
		if (originalObject.getNegativeVertex() != null) {
			_negative       = acwv.getVertexByID(originalObject.getNegativeVertex().getID());
		}
		if (originalObject.getLimitToSubjectVertex() != null) {
			_limitToSubject = acwv.getVertexByID(originalObject.getLimitToSubjectVertex().getID());
		}
		
		this.setSource(originalObject.getSource());
		this.setCorrectlyDerived(originalObject.isCorrectlyDerived());
	}	
	
	public AccessControlRelation(List<WordVertex> subject, List<WordVertex> action, List<WordVertex> object, String permissions, 
			WordVertex negative, WordVertex limitToSubject, boolean correctlyDerived, RelationSource source) {
		_subject = new ArrayList<WordVertex>(subject);
		_action  = new ArrayList<WordVertex>(action);
		_object  = new ArrayList<WordVertex>(object);
		_permissions    = permissions;
		_negative       = negative;
		_limitToSubject = limitToSubject;
		_correctlyDerived = correctlyDerived;
		_source = source;
	}
	
	public List<WordVertex> getSubjectVertexList() {
		return _subject;
	}
	
	public List<WordVertex> getActionVertexList() {
		return _action;
	}
	public List<WordVertex> getObjectVertexList() {
		return _object;
	}
	
	public void setSubjectVertexList(List<WordVertex> subject) {
		_subject = new ArrayList<WordVertex>(subject);
	}
	
	public void setActionVertexList(List<WordVertex> action) {
		_action = new ArrayList<WordVertex>(action);
	}
	public void setObjectVertexList(List<WordVertex> object) {
		 _object = new ArrayList<WordVertex>(object);
	}
	
	public String getPermissions() {
		return _permissions;
	}

	public WordVertex getNegativeVertex() {
		return _negative;
	}
	
	public WordVertex getLimitToSubjectVertex() {
		return _limitToSubject;
	}

	/**
	 * 
	 * @param permissions
	 */
	public void setPermissions(String permissions) throws IllegalArgumentException {
		if (permissions == null) { permissions = ""; }
		permissions = permissions.toUpperCase();
		
		for (int i=0; i< permissions.length();i++) {
			char c = permissions.charAt(i);
			if (c != 'C' && c != 'R' && c != 'U' && c != 'D' && c != 'E') {
				throw new IllegalArgumentException("Invalid permission.  Must be C,R,U,D,E: "+permissions);
			}
		}
		_permissions = permissions;
	}

	public void setNegativeVertex(WordVertex negative) {
		_negative = negative;
	}
	
	public void setLimitToSubjectVertex(WordVertex limit) {
		_limitToSubject = limit;
	}
	
	public boolean isCorrectlyDerived() {
		return _correctlyDerived;
	}

	public void setCorrectlyDerived(boolean correctlyDerived) {
		this._correctlyDerived = correctlyDerived;
	}

	public RelationSource getSource() {
		return _source;
	}

	public void setSource(RelationSource source) {
		this._source = source;
	}	
	
	
	@JsonIgnore
	public String getResolvedSubject() {
		if (_resolvedSubject == null || _resolvedSubject.equals("")) {
			return this.getSubject();
		}
		return _resolvedSubject;
	}
	
	@JsonIgnore 
	public String getResolvedObject() {
		if (_resolvedObject == null || _resolvedObject.equals("")) {
			return this.getObject();
		}
		return _resolvedObject;
	}
	
	public void setResolvedSubject(String newValue) {
		_resolvedSubject = newValue;
	}

	public void setResolvedObject(String newValue) {
		_resolvedObject = newValue;
	}
	
	
	@JsonIgnore
	public String getResolvedSubjectReason() {
		return _resolvedSubjectReason;
	}

	@JsonIgnore
	public String getResolvedObjectReason() {
		return _resolvedObjectReason;
	}

	@JsonIgnore
	public void setResolvedSubjectReason(String value) {
		_resolvedSubjectReason = value;
	}

	@JsonIgnore
	public void setResolvedObjectReason(String value) {
		_resolvedObjectReason = value;
	}

	
	@JsonIgnore
	public String getSubject() {
		return WordVertex.getListAsSingleWord(_subject, WordType.LEMMA);
	}
	
	@JsonIgnore
	public String getAction() {
		return WordVertex.getListAsSingleWord(_action, WordType.LEMMA);
	}

	@JsonIgnore
	public String getObject() {
		return WordVertex.getListAsSingleWord(_object, WordType.LEMMA);
	}
	
	public String toString() {
		return getSubject()+";"+getAction()+";"+getObject() + 
			   ( this.getNegativeVertex() != null ? ";NEG-"+this.getNegativeVertex().getLemma() : "") +
			   ( this.getLimitToSubjectVertex() != null ? ";ONLY-"+this.getLimitToSubjectVertex().getLemma() : "");
	}
	
	public String toResolvedString() {
		return getResolvedSubject()+";"+getAction()+";"+getResolvedObject() + 
				   ( this.getNegativeVertex() != null ? ";NEG-"+this.getNegativeVertex().getLemma() : "") +
				   ( this.getLimitToSubjectVertex() != null ? ";ONLY-"+this.getLimitToSubjectVertex().getLemma() : "");
	}
	
	@JsonIgnore
	public String getSubjectAsVertexIDs() {
		return WordVertex.getListAsVertexIDs(_subject);
	}
	
	@JsonIgnore
	public String getActionAsVertexIDs() {
		return WordVertex.getListAsVertexIDs(_action);
	}
	
	@JsonIgnore
	public String getObjectAsVertexIDs() {
		return WordVertex.getListAsVertexIDs(_object);
	}	
	
	
	@JsonIgnore
	public String getNegativeVertexAsID() {
		if (_negative == null) {
			return "";
		}
		else {
			return Integer.toString(_negative.getID());
		}
	}
	
	@JsonIgnore
	public String getLimitToSubjectVertexAsID() {
		if (_limitToSubject == null) {
			return "";
		}
		else {
			return Integer.toString(_limitToSubject.getID());
		}
	}	
	
	@JsonIgnore
	public Set<WordVertex> getAllVertices() {
		Set<WordVertex> result = new HashSet<WordVertex>();
	
		for (WordVertex wv: _subject) { result.add(wv); }
		for (WordVertex wv: _object) { result.add(wv); }
		for (WordVertex wv: _action) { result.add(wv); }
		if (_limitToSubject != null) { result.add(_limitToSubject); }
		if (_negative != null)  { result.add(_negative); } 
		
		return result;
	}
	
	@JsonIgnore
	public boolean isComplete() {
		return _subject.size() > 0 && _action.size() > 0 && _object.size() > 0;
	}
	
	/**
	 * Can this and the other access control object be combined from a permissions standpoint?
	 * Requires the same action and object.  Different permissions.  Negative and limit factors have to be the same
	 * 
	 * @param other
	 * @return
	 */
	@JsonIgnore
	public boolean isCombinable(AccessControlRelation other) {
		boolean intermediateResult = this._subject.equals(other.getSubjectVertexList()) &&
			   this._action.equals(other.getActionVertexList()) &&
			   this._object.equals(other.getObjectVertexList());
		
		if (!intermediateResult) { return false; }

		if (_negative != null) {
			if (!_negative.equals(other.getNegativeVertex())) { return false; }
		}
		else {
			if (other.getNegativeVertex() !=null) { return false; }
		}
		
		if (_limitToSubject != null) {
			if (!_limitToSubject.equals(other.getLimitToSubjectVertex())) { return false; }
		}
		else {
			if (other.getLimitToSubjectVertex() !=null) { return false; }
		}
		return true;
	}
	
	/**
	 * is SOA the same?  Are the subjects, actions, and objects the same?
	 * 
	 * @param other
	 * @return
	 */
	@JsonIgnore
	public boolean isSubjectActionObjectEqual(AccessControlRelation other) {
		
		// lets test equality by lemmas, not by item #s
		return this.getSubject().equals(other.getSubject()) && this.getAction().equals(other.getAction()) && this.getObject().equals(other.getObject());
		
		//much more "intensive" test
		//return (this._subject.equals(other.getSubjectVertexList()) && this._action.equals(other.getActionVertexList()) && this._object.equals(other.getObjectVertexList()));		
	}	
	
	
	/**
	 * is SOA the same?  Are the subjects, actions, and objects the same?
	 * 
	 * @param other
	 * @return
	 */
	@JsonIgnore
	public boolean isSubjectActionObjectEqualWithResolution(AccessControlRelation other) {
		return this.getResolvedSubject().equals(other.getResolvedSubject()) && this.getAction().equals(other.getAction()) && this.getResolvedObject().equals(other.getResolvedObject());	
	}		
	
	
	/**
	 * Is this access control rule in conflict with the other?
	 * Conflicts occur when -
	 *   two objects both have the same subject, object, and permissions (or just overlappying) and negative differs
	 *   One object has the limit set, the other doesn't.  both accessing the same object
	 *   
	 *   
	 * @param other
	 * @return
	 */
	@JsonIgnore
	public boolean inConflict(AccessControlRelation other) {
		throw new Error("not implemented");
		//TODO: implement
	}
	
	public boolean equals(Object o) {
		if (o == null || o instanceof AccessControlRelation == false) {
			return false;
		}
		AccessControlRelation other = (AccessControlRelation) o;
		
		boolean intermediateResult = this._subject.equals(other.getSubjectVertexList()) &&
			   this._action.equals(other.getActionVertexList()) &&
			   this._object.equals(other.getObjectVertexList());
		
		if (!intermediateResult) { return false; }

		if (_negative != null) {
			if (!_negative.equals(other.getNegativeVertex())) { return false; }
		}
		else {
			if (other.getNegativeVertex() !=null) { return false; }
		}
		
		if (_limitToSubject != null) {
			if (!_limitToSubject.equals(other.getLimitToSubjectVertex())) { return false; }
		}
		else {
			if (other.getLimitToSubjectVertex() !=null) { return false; }
		}
		
		HashSet<Character> permSetA = Utility.createCharacterSetFromString(_permissions);
		HashSet<Character> permSetB = Utility.createCharacterSetFromString(other.getPermissions());
		
		return permSetA.equals(permSetB);
	}
	
	/**
	 * Merges this access control with another.  requires "isCombinable" to be true
	 * (will throw anIllegalArgumentException if thats not the case
	 * 
	 * @param other
	 */
	public void merge(AccessControlRelation other) {
		if (!this.isCombinable(other)) {
			throw new IllegalArgumentException("Access control objects are not combinable");
		}
		
		HashSet<Character> permSetA = Utility.createCharacterSetFromString(_permissions);
		HashSet<Character> permSetB = Utility.createCharacterSetFromString(other.getPermissions());
		
		permSetA.addAll(permSetB);
		
		StringBuilder sb = new StringBuilder(permSetA.size());
		for (Character c: permSetA) {
			sb.append(c);
		}
		_permissions = sb.toString();
	}

	
	/**
	 * Merges this access control with another.  requires "isCombinable" to be true
	 * (will throw anIllegalArgumentException if thats not the case
	 * 
	 * @param other
	 */
	public void mergeForMapping(AccessControlRelation other) {
		if (!this.isCombinableForMapping(other)) {
			throw new IllegalArgumentException("Access control objects are not combinable");
		}
		
		HashSet<Character> permSetA = Utility.createCharacterSetFromString(_permissions);
		HashSet<Character> permSetB = Utility.createCharacterSetFromString(other.getPermissions());
		
		permSetA.addAll(permSetB);
		
		StringBuilder sb = new StringBuilder(permSetA.size());
		for (Character c: permSetA) {
			sb.append(c);
		}
		_permissions = sb.toString();
		
		for (Sentence s: other.getSourceSentences()) {
			this.addSourceSentence(s);
		}
	}	
	
	/**
	 * Can this and the other access control object be combined from a permissions standpoint?
	 * Requires the same action and object.  Different permissions.  Negative and limit factors have to be the same
	 * 
	 * @param other
	 * @return
	 */
	@JsonIgnore
	public boolean isCombinableForMapping(AccessControlRelation other) {
		boolean intermediateResult = this.getResolvedSubject().equals(other.getResolvedSubject()) &&
				                     this.getResolvedObject().equals(other.getResolvedObject());
		
		if (!intermediateResult) { return false; }

		if (_negative != null) {
			if (!_negative.equals(other.getNegativeVertex())) { return false; }
		}
		else {
			if (other.getNegativeVertex() !=null) { return false; }
		}
		
		if (_limitToSubject != null) {
			if (!_limitToSubject.equals(other.getLimitToSubjectVertex())) { return false; }
		}
		else {
			if (other.getLimitToSubjectVertex() !=null) { return false; }
		}
		return true;
	}	
	
	
	
	/**
	 * Does this AccessControl object contain the passed in object?
	 * If so, it's feasible for us to ignore the passed in objects when applying patterns
	 * 
	 * The method checks the following patterns:
	 *    blank   ACTION RESOURCE
	 *    SUBJECT ACTION blank
	 *    blank   ACTION blank
	 * 
	 * The negativeVertex and LimitToSubject must gree prior to the pattern check..
	 * 
	 * Side Note: If subject / action / resource agree, then isCombinable would be true.
	 * 
	 * @param other
	 * @return
	 */
	@JsonIgnore
	public boolean contains(AccessControlRelation other, boolean checkNegative, boolean checkLimitToSubject) {
		
		if (checkNegative && _negative == null && other.getNegativeVertex() !=null) { return false; }
		
		if (checkNegative &&  _negative != null) {
			if (other.getNegativeVertex() !=null && !_negative.equals(other.getNegativeVertex())) { return false; }
		}
		
		if (checkLimitToSubject && _limitToSubject != null) {
			if (!_limitToSubject.equals(other.getLimitToSubjectVertex())) { return false; }
		}
		else {
			if (checkLimitToSubject && other.getLimitToSubjectVertex() !=null) { return false; }
		}
		
		if (wordVertexListASubset(this.getSubjectVertexList(), other.getSubjectVertexList()) && 
			wordVertexListASubset(this.getActionVertexList(), other.getActionVertexList()) &&
			wordVertexListASubset(this.getObjectVertexList(), other.getObjectVertexList()) ) {
			return true;
		}
		
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
		
		return false;
	}	
	
	
	/**
	 * checks whether or not two access control elements are equal, but this object is negative and the passed in object is null
	 * @param other
	 * @return
	 */
	@JsonIgnore
	public boolean equalsExceptThisNegative(AccessControlRelation other) {
		
		if (_negative == null) { return false; }
				
		if (wordVertexListASubset(this.getSubjectVertexList(), other.getSubjectVertexList())  && 
			wordVertexListASubset(this.getActionVertexList(), other.getActionVertexList()) &&
			wordVertexListASubset(this.getObjectVertexList(), other.getObjectVertexList()) &&
			other.getNegativeVertex() == null) {
			return true;
		}
		
		return false;
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
	
	@JsonIgnore
	public boolean isSubjectAmbiguous() {
		if (_resolvedSubject == null || _resolvedSubject.equals("")) {
			return Ambiguity.isAmbiguous(this.getSubjectVertexList());
		}
		return false;		
	}

	@JsonIgnore
	public boolean isObjectAmbiguous() {
		if (_resolvedObject == null || _resolvedObject.equals("")) {
			return Ambiguity.isAmbiguous(this.getObjectVertexList());
		}
		return false;
	}
	
	
	@JsonIgnore
	public boolean isAmbiguous() {
		return this.isSubjectAmbiguous() || this.isObjectAmbiguous();
	}
	
}
