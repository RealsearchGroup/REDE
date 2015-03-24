package edu.ncsu.csc.nl.model.relation;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;

import com.fasterxml.jackson.annotation.JsonIgnore;

import edu.ncsu.csc.nl.GCController;
import edu.ncsu.csc.nl.event.NLPEvent;
import edu.ncsu.csc.nl.event.NLPEventListener;
import edu.ncsu.csc.nl.event.NLPEventManager;
import edu.ncsu.csc.nl.event.NLPEventSentenceAccessControlMarkedEvent;
import edu.ncsu.csc.nl.event.NLPEventSentenceDatabaseMarkedEvent;
import edu.ncsu.csc.nl.event.NLPEventType;
import edu.ncsu.csc.nl.model.NLDocument;
import edu.ncsu.csc.nl.model.Sentence;
import edu.ncsu.csc.nl.model.WordEdge;
import edu.ncsu.csc.nl.model.WordVertex;
import edu.ncsu.csc.nl.model.english.Ambiguity;
import edu.ncsu.csc.nl.model.english.Voice;
import edu.ncsu.csc.nl.model.type.AccessControlElement;
import edu.ncsu.csc.nl.model.type.DatabaseElement;
import edu.ncsu.csc.nl.model.type.DatabaseRelationType;
import edu.ncsu.csc.nl.model.type.DatabaseRelationshipType;
import edu.ncsu.csc.nl.model.type.RelationSource;
import edu.ncsu.csc.nl.model.type.PartOfSpeech;
import edu.ncsu.csc.nl.model.type.Relationship;
import edu.ncsu.csc.nl.model.type.WordType;
import edu.ncsu.csc.nl.util.Logger;


/**
 * DatabaseRelation manager is responsible for 
 *   - detecting database relations  when a sentence is marked as having database defined
 *   - searching through the current database relations for a particular match on the current sentence
 *   - performing "bootstrap" type operations to extend the possible patterns that may exist within the system.
 *   
 * @author John Slankas
 */
public class DatabaseRelationManager implements NLPEventListener {
	
	//TODO: Need ways to seed for entities
	//      -- use objects found from relations (this will show how the two can interrelate)
	//TODO: Need ways to seed entity attributes
	//      -- use a rule or two from Chen/Hartmann's papers.  Use entities from the first rules
	//TODO: Need ways to seed relationships.
	//      -- use a rule with CHen
	//TODO: Need ways to seed relationship attributes
	//     -- do we really have any of these?  should I really consider them?
	//   Question: What relationships exist that are not access control..    Will probably be hard to find in my space
	//
	//Need to look at how these rules consistently interrelate 
	
	private HashSet<String> _uniqueEntities   = new HashSet<String>();
	private HashSet<String> _uniqueAttributes = new HashSet<String>();
	private HashSet<String> _uniqueRelations  = new HashSet<String>();
	
	private ArrayList<DatabasePattern> _patterns = new ArrayList<DatabasePattern>();
	

	private boolean _verbose = true;
	

	private static DatabaseRelationManager _theDatabaseRelationManager = new DatabaseRelationManager();
	
	private DatabaseRelationManager() {
		NLPEventManager.getTheEventManager().registerForEvent(NLPEventType.SENTENCE_DATABASE_CHANGE, this);
	}
		
	public static DatabaseRelationManager getTheDatabaseRelationManager() {
		return _theDatabaseRelationManager;
	}
	
	public void reset(boolean resetUniqueLists) {
		System.out.println("WARNING: DatabaseRelationManager: reset() called");
		_patterns = new ArrayList<DatabasePattern>();

		if (resetUniqueLists) {
			_uniqueEntities   = new HashSet<String>();
			_uniqueAttributes = new HashSet<String>();
			_uniqueRelations  = new HashSet<String>();
		}
	}

	/**
	 * Bootstrap performs the following actions:
	 *

	 */
	public void bootstrap() {
		this.printMessage("- bootstrap -> creating patterns from ACR");
		
		AccessControlRelationManager acrm = AccessControlRelationManager.getTheAccessControlRelationManager();
		
		Set<String> actions   = acrm.getActions();
		Set<String> subjects  = acrm.getSubjects();
		Set<String> resources=  acrm.getResources();
		
		Set<String> combinedSubjectResource = new HashSet<String>(resources);
		combinedSubjectResource.addAll(subjects);
		
		if (_patterns == null) { _patterns = new ArrayList<DatabasePattern>(); }
		
		//Create all of the possible entities...
		for (String entity: combinedSubjectResource) {
			DatabasePattern dp = DatabasePattern.createEntityPattern_nsubj(entity);
			//System.out.println(dp);
			_patterns.add(dp);
			dp = DatabasePattern.createEntityPattern_dobj(entity);
			//System.out.println(dp);
			_patterns.add(dp);
			dp = DatabasePattern.createEntityPattern_prep(entity);
			//System.out.println(dp);
			_patterns.add(dp);
			
		}
		for (String entity: combinedSubjectResource) {
			for (String attribute: combinedSubjectResource) {
				if (entity.equalsIgnoreCase(attribute)) {continue; }
				DatabasePattern dp = DatabasePattern.createEntityAttributePattern_poss(entity, attribute);
				//System.out.println(dp);
				_patterns.add(dp);
				dp = DatabasePattern.createEntityAttributePattern_prepOf(entity, attribute);
				//System.out.println(dp);
				_patterns.add(dp);
				
				dp = DatabasePattern.createRelationPattern_association(entity, attribute);
				//System.out.println(dp);
				_patterns.add(dp);
				
				dp = DatabasePattern.createRelationPattern_aggregation("contain", entity, attribute);
				//System.out.println(dp);
				_patterns.add(dp);
				
				dp = DatabasePattern.createRelationPattern_aggregation("compose", entity, attribute);
				//System.out.println(dp);
				_patterns.add(dp);
				
				dp = DatabasePattern.createRelationPattern_aggregation("belong", entity, attribute);
				//System.out.println(dp);
				_patterns.add(dp);
				
				dp = DatabasePattern.createRelationPattern_aggregation("have", entity, attribute);
				//System.out.println(dp);
				_patterns.add(dp);
				
				dp = DatabasePattern.createRelationPattern_inheritance("be", entity, attribute);
				//System.out.println(dp);
				_patterns.add(dp);
			}
		}
		
		this.printMessage("- bootstrap -> complete");
	}	

	
	private boolean containsPronoun(List<WordVertex> words) {
		try {
			for (WordVertex word: words) {
				if (word.getPartOfSpeech() == PartOfSpeech.PRP || word.getPartOfSpeech() == PartOfSpeech.PRP$) {
					return true;
				}
			}
			return false;
		}
		catch (NullPointerException ex) {
			System.err.println("Found null pointer in contains pronous");
		}
		return false;
	}
	
	public void addEntity(String entity) {
		entity = entity.trim();
		if (entity.length()>0 /* && Ambiguity.allowableInSOA(subject) */) {
			_uniqueEntities.add(entity);
		}
	}

	public void addAttribute(String attribute) {
		attribute = attribute.trim();
		if (attribute.length()>0 /* && Ambiguity.allowableInSOA(action) */) {
			_uniqueAttributes.add(attribute);
		}
	}

	public void addRelation(String relation) {
		relation = relation.trim();
		if (relation.length()>0 /*&& Ambiguity.allowableInSOA(object) */) {
			_uniqueRelations.add(relation);
		}
	}	
	
	
	private void updateUniqueLists(DatabasePattern dp) {
		DatabaseRelation dr = dp.getDatabaseItem();
		if (dr.getRelationType() == DatabaseRelationType.ENTITY) {
			if (!containsPronoun(dr.getIndentifyingNode())) {
				this.addEntity(dr.getIdentifyingNodeString());			
			}			
		}
		else if (dr.getRelationType() == DatabaseRelationType.ENTITY_ATTR) {
			if (!containsPronoun(dr.getIndentifyingNode())) {
				this.addAttribute(dr.getIdentifyingNodeString());			
			}
			if (!containsPronoun(dr.getParentEntityNode())) {
				this.addEntity(dr.getParentEntityNodeString());			
			}
		}
		else if (dr.getRelationType() == DatabaseRelationType.RELATIONSHIP) {
			if (!containsPronoun(dr.getIndentifyingNode())) {
				this.addRelation(dr.getIdentifyingNodeString());			
			}			
			if (!containsPronoun(dr.getFirstEntityNode())) {
				this.addEntity(dr.getFirstEntityNodeString());			
			}			
			if (!containsPronoun(dr.getSecondEntityNode())) {
				this.addEntity(dr.getSecondEntityNodeString());			
			}			
			
		}
		else {
			System.out.println ("Update Unique Lists: not a valide database relation type "+dr.getRelationType());
		}
	
	}	
	
	
	
	@Override
	public void eventOccured(NLPEventType eventType, NLPEvent event) {
		if (eventType == NLPEventType.SENTENCE_DATABASE_CHANGE) {
			NLPEventSentenceDatabaseMarkedEvent actualEvent = (NLPEventSentenceDatabaseMarkedEvent) event;
			processDatabaseElements(actualEvent.getSentence());
		}
		
	}

	/**
	 * Once a sentence has been marked as having the access control defined,
	 * this method is called via an eventListener
	 * 	
	 * @param s
	 */
	private void processDatabaseElements(Sentence s) { 
		if (s.isDatabaseComplete()) {
		
			List<DatabaseRelation> dbList = s.getDatabaseRelations();
			if (dbList.size() > 0) {
				Logger.log(Logger.LEVEL_TRACE, "Access control has been defined, processing....");
			}

			for (int i = dbList.size()-1;i >=0; i--) {
				DatabaseRelation dr  = dbList.get(i);
				try {
					
					if (dr.getAllVertices().size() == 0) {
						Logger.log(Logger.LEVEL_DEBUG, "warning: blank databaes relation item defined, removing ...");
						dbList.remove(i);
						continue;
					}
					
					
					
					Set<WordVertex> vertices = dr.getAllVertices();
					if (dr.getRelationType() == DatabaseRelationType.ENTITY) {
						vertices.add(s.getRoot());
					}
					
					WordVertex wvp = WordVertex.extractPattern(vertices);
					//WordVertex wvp = WordVertex.extractPattern(dr.getAllVertices());
					
					DatabasePattern newPattern = new DatabasePattern(wvp, new DatabaseRelation(dr,wvp), dr.getSource(),dr.getRelationType());
					this.updateUniqueLists(newPattern);  //we always need to updated the existing list no matter what, so do it now ...
					
					/*
					if (newPattern.getRoot().getGraphSize() == 1) {
						System.out.println ("SINGLE: "+newPattern);
					}
					*/
					
					DatabasePattern existingPattern = this.findExistingPattern(newPattern,true);
					if (existingPattern != null) {
						//existingPattern.addUnknownSentenceByOriginalPosition(s.getOriginalSentencePosition());
						Logger.log(Logger.LEVEL_DEBUG, "  - found existing pattern: "+existingPattern.getRoot().getStringRepresentation());					
					}
					else {
						Logger.log(Logger.LEVEL_DEBUG, "  - extracted new pattern: "+wvp.getStringRepresentation());
						if (dr.getSource() == RelationSource.USER) {
							newPattern.setDatabaseSource(RelationSource.PATTERN);
						}
						_patterns.add(newPattern);
							
						// Test for new patterns to create
						this.createTransformedPatterns(newPattern);
					}
				}
				catch (Throwable t) {
					System.err.println("Sentence: "+ s);
					System.err.println("Database Item: "+dr);
					t.printStackTrace();
				}
			}			
			
		}
		else { // need to go through all of the patterns.  if equal to this one, then remove my sentence. 
			//for (int j = _patterns.size()-1; j>=0; j--) {
			//	AccessControlPattern acp = _patterns.get(j);
				//acp.removeAnyOccuranceOfSentence(s.getOriginalSentencePosition());
			//}
			// Commented this out as I'm going to manually compute sentences and patterns for correctness
			
		}
		Logger.log(Logger.LEVEL_TRACE, "DBRM: processDatabaseElements exit");
	}
	
	/**
	 * Called in the code to automagically find and extract patterns...
	 * Note (3/14/2014) -> right now I think this code is only been called from the bootstrap function  (via extractDatabaseObjects in AccessControlController)
	 *                  I can probably call this from the json load to restablish patterns 
	 *                  rather than saving them.  This also updates the lists themselves...
	 * 
	 * For the passed in sentence, it looks for any access control tuples that have been
	 * identified.  It creates patterns for all such tuples/rules.
	 * 
	 * @param s
	 */
	public void extractDatabasePatternsFromDefinedDatabaseTuples(Sentence s) { 
		List<DatabaseRelation> dbList = s.getDatabaseRelations();
		if (dbList.size() > 0) {
			Logger.log(Logger.LEVEL_TRACE, "ACRM: extract DB patterns from DDE Tuples: - Found possible DDE records, processing: "+ s.getSentence());
		}

		for (DatabaseRelation dr: dbList) {
			try {
				Set<WordVertex> vertices = dr.getAllVertices();
				if (dr.getRelationType() == DatabaseRelationType.ENTITY) {
					vertices.add(s.getRoot());
				}
				
				WordVertex wvp = WordVertex.extractPattern(vertices);// dr.getAllVertices());
				DatabasePattern newPattern = new DatabasePattern(wvp, new DatabaseRelation(dr,wvp), dr.getSource(),dr.getRelationType());
				//newPattern.addUnknownSentenceByOriginalPosition(s.getOriginalSentencePosition());
				this.updateUniqueLists(newPattern);  //we always need to updated the existing list no matter what, so do it now ...
				
				DatabasePattern existingPattern = this.findExistingPattern(newPattern,true);
				if (existingPattern != null) {
					//existingPattern.addUnknownSentenceByOriginalPosition(s.getOriginalSentencePosition());
					Logger.log(Logger.LEVEL_TRACE, "  - found existing pattern: "+existingPattern.getRoot().getStringRepresentation());					
				}
				else {
					Logger.log(Logger.LEVEL_TRACE,"  - extracted new pattern: "+wvp.getStringRepresentation());
					if (dr.getSource() == RelationSource.USER) {
						newPattern.setDatabaseSource(RelationSource.PATTERN);
					}
					_patterns.add(newPattern);
					
					if (newPattern.getRoot().getGraphSize() < 3) {
						Logger.log(Logger.LEVEL_TRACE, "  -    warning less than 3, from: ");
					}
						
						
					// Test for new patterns to create
					this.createTransformedPatterns(newPattern);
				}
			}
			catch (Throwable t) {
				System.err.println("Sentence: "+s);
				System.err.println("Access Control: "+dr);
				System.err.println(t);
				t.printStackTrace();
			}
		}	
		Logger.log(Logger.LEVEL_TRACE, "ACRM: extractACP from AC Tuples: - complete");
	}	
	
	/**
	 * 
	 * @param sentences
	 * @param setSubjects
	 * @param setResources
	 * @param maxPatternSize don't allow extract patterns larger than this size!
	 */
	public void searchForPatternsFromKnownObjects(List<Sentence> sentences, int maxPatternSize) {
		// extract patterns from the cartesian product of entities * entities
		
		HashSet<String> subjects  = new HashSet<String>(_uniqueEntities);
		HashSet<String> resources = new HashSet<String>(_uniqueEntities);
		
		Logger.log(Logger.LEVEL_DEBUG, "Current Number of patterns "+_patterns.size());
		
		for (Sentence s: sentences) {
			for (String subject: subjects) {
				List<WordVertex> possibleSubjectVertices = s.getRoot().getVertexByWord(WordType.LEMMA, subject);
				if (possibleSubjectVertices.size() == 0) { continue; } // subject doesn't exist, no reason to check resources
				for (String resource: resources) {
					if (resource.equalsIgnoreCase(subject)) { continue; } // don't allow subjects / actions to be the same
					List<WordVertex> possibleResourceVertices = s.getRoot().getVertexByWord(WordType.LEMMA, resource);
					if (possibleResourceVertices.size() == 0) { continue; } // no pair found, onward...
					Logger.log(Logger.LEVEL_TRACE, subject+"\t"+resource+"\t | "+s.getOriginalSentencePosition()+":" +s.getSentence());
					//List<AccessControlWordVertex> patternRoots = AccessControlWordVertex.extractBootstrapPattern(possibleSubjectVertices, possibleResourceVertices);
					List<DatabasePattern> bootstrapPatterns = DatabaseRelationManager.extractBootstrapPattern(possibleSubjectVertices, possibleResourceVertices);

					if (bootstrapPatterns.size() == 0) {
						System.out.println("No CARTESIAN pattern found");
					}              
					
					for (DatabasePattern bootstrapPattern: bootstrapPatterns) {
						WordVertex wv = bootstrapPattern.getRoot();
						Logger.log(Logger.LEVEL_TRACE,"\t"+wv.getStringRepresentation());
						/*
						if (wv.getGraphSize() > maxPatternSize) {
							Logger.log(Logger.LEVEL_TRACE, "  - new pattern > "+maxPatternSize+" (dropping): "+bootstrapPattern.getRoot().getStringRepresentation());
							continue;
						}
						*/
						
						DatabasePattern acp = this.findExistingPattern(bootstrapPattern,true);
						if (acp == null) {
							Logger.log(Logger.LEVEL_TRACE, "  - extracted new pattern: "+bootstrapPattern.getRoot().getStringRepresentation());
							_patterns.add(bootstrapPattern);
							this.updateUniqueLists(bootstrapPattern);
						}
					}
				}
			}
		}
		Logger.log(Logger.LEVEL_DEBUG, "Current Number of patterns "+_patterns.size());
	}
	
	

	private DatabasePattern findExistingPattern(DatabasePattern patternToFind, boolean exactMatch) {
		for (DatabasePattern existingPattern: _patterns) {
			if (DatabasePattern.graphPatternsEquals(existingPattern, patternToFind, existingPattern.getRoot(), patternToFind.getRoot(),exactMatch )) {
				return existingPattern;
			}
		}
		return null;
	}
	
	
	private static final Relationship[] REQUIRED_RELATIONSHIP_ACTIVE = { Relationship.NSUBJ, Relationship.DOBJ };
	private static final Relationship[] REQUIRED_RELATIONSHIP_PASSIVE = { Relationship.AGENT, Relationship.NSUBJPASS }; 
	
	
	public void transformPatterns() {
		Logger.log(Logger.LEVEL_DEBUG, "Transforming Patterns START");
		Logger.log(Logger.LEVEL_DEBUG, "  Current Number of patterns "+_patterns.size());
		
		int maxIndex = _patterns.size();
		for( int i=0; i < maxIndex; i++) {
			DatabasePattern dp = _patterns.get(i);
			if (dp.hasBeenCheckedForTransformations()) { continue; }
			
			this.createTransformedPatterns(dp);
			dp.setCheckedForTransformations();
		}
		Logger.log(Logger.LEVEL_DEBUG, "  Current Number of patterns "+_patterns.size());
		Logger.log(Logger.LEVEL_DEBUG, "Transforming Patterns END");
	}
	
	/**
	 * From the passed in pattern, see what derived patterns can be generated
	 * - active from passive
	 * - passive from active
	 * 
	 * createWildCardPOSPatterns is set to true, then ACP and any other derived patterns will also have
	 * a new set of patterns created to mathc on wildcards.
	 * 
	 * @param dp
	 * @param createWildCardPOSPatterns
	 */
	private void createTransformedPatterns(DatabasePattern dp) {

		WordVertex rootNode = dp.getRoot();
		if (rootNode.getPartOfSpeech().equalsCollapsed(PartOfSpeech.VB) == false) { return; } // can only manipulate patterns rooted with a verb
		if (Voice.inPassiveVoice(rootNode)) {
			if (rootNode.hasAllChildRelationships(REQUIRED_RELATIONSHIP_PASSIVE)) {
				/*
				 TODO: Allow for conversions of patterns
				DatabasePattern activePattern = new DatabasePattern(dp);
				//activePattern.changePatternFromPassiveToActive();
				activePattern.setDatabaseSource(RelationSource.PATTERN_DERIVED_ACTIVE);
				
				if (this.findExistingPattern(activePattern,true) == null) {
					Logger.log(Logger.LEVEL_TRACE, "  - extracted new derived active pattern: "+activePattern.getRoot().getStringRepresentation());
					_patterns.add(activePattern);
				}
				*/
			}
		}
		else {   // pattern is in active voice, must have nsubj and dobj as children
			if (rootNode.hasAllChildRelationships(REQUIRED_RELATIONSHIP_ACTIVE)) {
				/*
				 TODO: allow for conversion of patterns
				DatabasePattern passivePattern = new DatabasePattern(dp);
				passivePattern.changePatternFromActiveToPassive();
				passivePattern.setDatabaseSource(RelationSource.PATTERN_DERIVED_PASSIVE);
				
				if (this.findExistingPattern(passivePattern,true) == null) {
					Logger.log(Logger.LEVEL_TRACE, "  - extracted new derived passive pattern: "+passivePattern.getRoot().getStringRepresentation());
					_patterns.add(passivePattern);	
				}				
  			    */

			}
			
		}
	}

	/**
	 * This function will go through all of the current patterns that are available, looking to 
	 * see if they can be wildcarded to locate additional patterns and access control rules.
	 * 
	 * @param subjects Should the subjects be expanded/wildcarded?
	 * @param actions
	 * @param resources
	 * @param prepositions Allow a preposition in a relationship to a resource to be a wildcard?  (allows any prepositions to match)
	 */
	public void expandPatternSet(boolean identify, boolean parent, boolean firstEntity, boolean secondEntity, boolean prepositions) {
		
		//TODO: need to alter this function to deal with the database patterns
		
		Logger.log(Logger.LEVEL_DEBUG, "Expand Patterns START");
		Logger.log(Logger.LEVEL_DEBUG, "  Current Number of patterns "+_patterns.size());

		int maxIndex = _patterns.size();
		for( int i=0; i < maxIndex; i++) {
			DatabasePattern dp = _patterns.get(i);
			if (dp.hasWildcardElement()) { // don't allow multiple wildcards unless we explicitly call for it 
				continue;
			}
			try {
				this.createExpandedPatterns(dp, identify, parent, firstEntity, secondEntity, prepositions);
			}
			catch (Exception e) {
				this.printMessage("  Unable to expand, removing: "+dp);
				_patterns.remove(i);
				i--;
				maxIndex--;
			}
		}

		Logger.log(Logger.LEVEL_DEBUG, "  Current Number of patterns "+_patterns.size());
		Logger.log(Logger.LEVEL_DEBUG, "Expand Patterns END");		
		
		
	}
	
	/**
	 *
	 *
	 * createWildCardPOSPatterns is set to true, then ACP and any other derived patterns will also have
	 * a new set of patterns created to mathc on wildcards.
	 * 
	 * @param acp
	 * @param createWildCardPOSPatterns
	 */
	private void createExpandedPatterns(DatabasePattern acp, boolean identify, boolean parent, boolean firstEntity, boolean secondEntity, boolean prepositions) {
		DatabasePattern newPattern = new DatabasePattern(acp);
		
		if (identify) {
			newPattern = DatabasePattern.createMatchingElementOnPartofSpeechPattern(newPattern, DatabaseElement.IDENTITY_NODE);
		}
		
		if (parent) {
			newPattern = DatabasePattern.createMatchingElementOnPartofSpeechPattern(newPattern, DatabaseElement.PARENT_NODE);
		}			
		
		if (firstEntity) {
			newPattern = DatabasePattern.createMatchingElementOnPartofSpeechPattern(newPattern, DatabaseElement.FIRST_REL_NODE);
		}		

		if (secondEntity) {
			newPattern = DatabasePattern.createMatchingElementOnPartofSpeechPattern(newPattern, DatabaseElement.SECOND_REL_NODE);
		}		
		
		
		if (prepositions) {
			newPattern = DatabasePattern.createMatchingElementOnPartofSpeechPattern(newPattern, DatabaseElement.PREPOSITION);
		}		
		
		if (this.findExistingPattern(newPattern,true) == null) {
			Logger.log(Logger.LEVEL_TRACE, "  - extracted wildcard pattern: "+newPattern.getRoot().getStringRepresentation());
			_patterns.add(newPattern);
		}		
		
	}
	
	//TO DO.  Need to create/ inject neew patterns as necessary.  Add calls from DatabaseController.injectPatterns() to here
	
	public void injectBasicPatternWithPronominalSubject() {	
		/*
		 * 
		TODO: Add implementation if necessary
		for (int i=0; i < _actionSeeds.length; i++) {
			for (String subject: this.getSubjects()) {
				AccessControlPattern newPattern = AccessControlPattern.createBasicPatternWithPronominalSubject(_actionSeeds[i], _permissionSeeds[i], subject);
				if (this.findExistingPattern(newPattern,true) == null) {
					Logger.log(Logger.LEVEL_TRACE, "  - added injected pronominal pattern: "+newPattern.getRoot().getStringRepresentation());
					_patterns.add(newPattern);
				}
			}
		}
		*/			
	}
	
	public void injectMissingSubjectPattern() {
		/*
        TODO: Add implementation if necessary
		for (int i=0; i < _actionSeeds.length; i++) {
			AccessControlPattern newPattern = AccessControlPattern.createDoubleNodeWithMissingSubject(_actionSeeds[i], _permissionSeeds[i]);
			if (this.findExistingPattern(newPattern,true) == null) {
				Logger.log(Logger.LEVEL_TRACE, "  - added injected pronominal pattern: "+newPattern.getRoot().getStringRepresentation());
				_patterns.add(newPattern);
			}
		}			
		*/
	}
	
	
	
	
	private static class SearchResult {
		public WordVertex root;
		public DatabasePattern pattern;
		
		public SearchResult(WordVertex r, DatabasePattern acp) {
			root = r;
			pattern = acp;
		}
	}
	
	//TODO: This moethod needs work.  2 issues.  temp has multiple paths matching the same vertex.  If pattern list has more than one item in it, then 
	//      we really need to be matching that pattern where ever it may exist under the root object ....
	//      that's really the way to go.  extract the graph for pattern list and then search for.  would need to be an exact match??? 
	//      the nodes would have to be exact.
	private static List<ArrayList<WordVertex>> createWordVertexListFromPatternList(WordVertex root, List<WordVertex> patternList, List<Relationship> limitToRelationships, HashSet<String> knownItems) {
		List<ArrayList<WordVertex>> overallResults = new ArrayList<ArrayList<WordVertex>>();
		
		overallResults.add( new ArrayList<WordVertex>());
		
		for (WordVertex wv: patternList){
			// can generate multiple locations
			List<WordVertex> temp = root.getNodeFollowingRootPathFromOtherGraph(wv,true,knownItems);
			
			/*
			 * This is a check to make sure that only certain parent relationships can be brought back.  
			 * Unfortunately, it doesn't cover all of the possibilities.
			 * 
			 * Now, I'm just putting the subjects into place.  Otherwise, I get too much crap
			 * */
			if (limitToRelationships.size() > 0) {
				boolean relationshipExists = false;
				
				for (WordVertex wvCheck: temp) {
					for (int i=0; i< wvCheck.getNumberOfParents(); i++) {
						if (limitToRelationships.contains(wvCheck.getParentAt(i).getRelationship())) {
							relationshipExists = true;
						}
					}
					
				}
				
				if (!relationshipExists) {
					continue;
				}
			}
			
			
			while (overallResults.size() < temp.size()) {
				overallResults.add( (ArrayList<WordVertex>) overallResults.get(0).clone());
			}
			for (int i=0;i < temp.size(); i++ ) {
				overallResults.get(i).add(temp.get(i));
			}			
		}
		return overallResults;
	}
	
	
		
	private static WordVertex createWordVertexListFromPatternVertex(WordVertex root, WordVertex patternNode, List<Relationship> limitToRelationships, HashSet<String> knownItems) {
		List<ArrayList<WordVertex>> overallResults = new ArrayList<ArrayList<WordVertex>>();
		
		overallResults.add( new ArrayList<WordVertex>());
		
		List<WordVertex> temp = root.getNodeFollowingRootPathFromOtherGraph(patternNode,true, knownItems);
			
		return temp.get(0);
	}
	
	
	/**
	 * This method examines a specific sentence, s, to see if it contains any of the discovered patterns
	 * 
	 * @param s
	 * @return
	 */
	public ArrayList<DatabaseRelation> findDatabaseDesign(Sentence s, boolean useClassification) {  // TODO.  Make it optional as to whether or not relationships should be limited..  Probably only during bootstrap processing...
		ArrayList<DatabaseRelation> result = new ArrayList<DatabaseRelation>();
		ArrayList<SearchResult> srList = this.walkGraph(s.getRoot(), new HashSet<Integer>(), useClassification);
		
		// the above could create duplicate access control items, will need to unify the results.
		// the unification of the results is done within the Sentence routine as it needs to account 
		// for any access control lists already defined there as well.
		for (SearchResult sr: srList) {
			result.addAll(extractDatabaseObjects(sr));
		}
		
		
		return result;
	}

	
	private  ArrayList<DatabaseRelation> extractDatabaseObjectsForEntities(DatabaseRelation dbPattern, SearchResult sr) {
		ArrayList<DatabaseRelation> result = new ArrayList<DatabaseRelation>();

		List<ArrayList<WordVertex>> entityLists = DatabaseRelationManager.createWordVertexListFromPatternList(sr.root,dbPattern.getIndentifyingNode(), Relationship.getEmptyRelationships(), _uniqueEntities);
		
		for (ArrayList<WordVertex> entityList: entityLists) {
			DatabaseRelation extractedItem = new DatabaseRelation();
			extractedItem.setRelationType(DatabaseRelationType.ENTITY);
			extractedItem.setIdentifyingNode(entityList);
					
			if (entityList.size() == 0 ) {
				Logger.log(Logger.LEVEL_TRACE, "    database entity found with no entity defined,  will skip");
				Logger.log(Logger.LEVEL_TRACE, "      search result root: "+sr.root.toString());
				Logger.log(Logger.LEVEL_TRACE, "      search result ac pattern: "+sr.pattern);
				continue;
			}
			
			extractedItem.setSource(sr.pattern.getDatabaseSource());
					
			result.add(extractedItem);	
		}		
		
		return result;
	}
	
	private  ArrayList<DatabaseRelation> extractDatabaseObjectsForEntityAttributes(DatabaseRelation dbPattern, SearchResult sr) {
		ArrayList<DatabaseRelation> result = new ArrayList<DatabaseRelation>();

		List<ArrayList<WordVertex>> entityLists = DatabaseRelationManager.createWordVertexListFromPatternList(sr.root,dbPattern.getParentEntityNode(), Relationship.getEmptyRelationships(), _uniqueEntities);
		List<ArrayList<WordVertex>> attributeLists = DatabaseRelationManager.createWordVertexListFromPatternList(sr.root,dbPattern.getIndentifyingNode(), Relationship.getEmptyRelationships(), _uniqueAttributes);
		
		for (ArrayList<WordVertex> attributelist: attributeLists) {
			for (ArrayList<WordVertex> entityList: entityLists) {
				
				DatabaseRelation extractedItem = new DatabaseRelation();
				extractedItem.setRelationType(DatabaseRelationType.ENTITY_ATTR);
				extractedItem.setIdentifyingNode(attributelist);
				extractedItem.setParentEntityNode(entityList);
				
						
				if (entityList.size() == 0 ) {
					Logger.log(Logger.LEVEL_TRACE, "    database entity_attribute found with no entity defined,  will skip");
					Logger.log(Logger.LEVEL_TRACE, "      search result root: "+sr.root.toString());
					Logger.log(Logger.LEVEL_TRACE, "      search result ac pattern: "+sr.pattern);
					continue;
				}
				if (attributelist.size() == 0 ) {
					Logger.log(Logger.LEVEL_TRACE, "    database entity_attribute found with no attribute defined,  will skip");
					Logger.log(Logger.LEVEL_TRACE, "      search result root: "+sr.root.toString());
					Logger.log(Logger.LEVEL_TRACE, "      search result ac pattern: "+sr.pattern);
					continue;
				}				
				if (entityList.equals(attributelist)) {
					Logger.log(Logger.LEVEL_TRACE, "    database entity_attribute found with entity=attribute  ---  removing");
					continue;
				}
				
				extractedItem.setSource(sr.pattern.getDatabaseSource());
						
				result.add(extractedItem);					
					
			}
		}
		
		
		
		
		
		return result;
	}	

	private  ArrayList<DatabaseRelation> extractDatabaseObjectsForRelations(DatabaseRelation dbPattern, SearchResult sr) {
		ArrayList<DatabaseRelation> result = new ArrayList<DatabaseRelation>();

		List<ArrayList<WordVertex>> relationLists     = DatabaseRelationManager.createWordVertexListFromPatternList(sr.root,dbPattern.getIndentifyingNode(), Relationship.getEmptyRelationships(), _uniqueRelations);
		List<ArrayList<WordVertex>> firstEntityLists  = DatabaseRelationManager.createWordVertexListFromPatternList(sr.root,dbPattern.getFirstEntityNode(),  Relationship.getEmptyRelationships(), _uniqueEntities);
		List<ArrayList<WordVertex>> secondEntityLists = DatabaseRelationManager.createWordVertexListFromPatternList(sr.root,dbPattern.getSecondEntityNode(), Relationship.getEmptyRelationships(), _uniqueEntities);
		
		for (ArrayList<WordVertex> relationList: relationLists) {
			for (ArrayList<WordVertex> firstEntityList: firstEntityLists) {
				for (ArrayList<WordVertex> secondEntityList: secondEntityLists) {
					DatabaseRelation extractedItem = new DatabaseRelation();
					extractedItem.setRelationType(DatabaseRelationType.RELATIONSHIP);
					
					extractedItem.setIdentifyingNode(relationList);
					extractedItem.setFirstEntityNode(firstEntityList);
					extractedItem.setSecondEntityNode(secondEntityList);
					
					if (relationList.size() == 0 || firstEntityList.size() == 0 || secondEntityList.size() == 0) {
						Logger.log(Logger.LEVEL_TRACE, "    database relation found with no relation or no first entity or no second entity,  will skip");
						Logger.log(Logger.LEVEL_TRACE, "      search result root: "+sr.root.toString());
						Logger.log(Logger.LEVEL_TRACE, "      search result ac pattern: "+sr.pattern);
						continue;
					}
					if (relationList.equals(firstEntityList) || relationList.equals(secondEntityList) || firstEntityList.equals(secondEntityList)) {
						Logger.log(Logger.LEVEL_TRACE, "    database relation found with relation=first, relation=second or first= second  ---  removing");
						continue;
					}
					// need to check for duplicated nodes as well.
					HashSet<WordVertex> existingNodes = new HashSet<WordVertex>();
					existingNodes.addAll(relationList);
					boolean found = false;
					for (WordVertex wv: firstEntityList) {
						if (existingNodes.contains(wv)) { found = true;} else { existingNodes.add(wv); }
					}
					for (WordVertex wv: secondEntityList) {
						if (existingNodes.contains(wv)) { found = true;} else { existingNodes.add(wv); }
					}
					if (found) {
						Logger.log(Logger.LEVEL_TRACE, "   database relation found with a repeated node  ---  removing");
						continue;						
					}
					
					extractedItem.setSource(sr.pattern.getDatabaseSource());
					
					result.add(extractedItem);
					
				}
			}
		}
		
		
		return result;
	}	
	
	
	public ArrayList<DatabaseRelation> extractDatabaseObjects(SearchResult sr) {
		DatabaseRelation dbPattern = sr.pattern.getDatabaseItem();
		switch (dbPattern.getRelationType()) {
		    case ENTITY:       return this.extractDatabaseObjectsForEntities(dbPattern, sr);
		    case ENTITY_ATTR:  return this.extractDatabaseObjectsForEntityAttributes(dbPattern, sr);
		    case RELATIONSHIP: return this.extractDatabaseObjectsForRelations(dbPattern, sr);
		    default: return  new ArrayList<DatabaseRelation>();
		}
	}
	
	private ArrayList<SearchResult> walkGraph(WordVertex wv, HashSet<Integer> visitedWords, boolean useClassification) {
		if (visitedWords.contains(wv.getID())) { return  new ArrayList<SearchResult>(); }
		visitedWords.add(wv.getID());
		
		ArrayList<SearchResult> results = new ArrayList<SearchResult>();
		results.addAll(findPattern(wv,useClassification));
		for (int i=0;i< wv.getNumberOfChildren();i++) {
			WordEdge childEdge = wv.getChildAt(i);
			results.addAll( walkGraph(childEdge.getChildNode(),visitedWords,useClassification));
		}
		return results;
	}
	
	/** from the passed in node, return all patterns matched */
	private ArrayList<SearchResult> findPattern(WordVertex wv, boolean useClassification) {
		ArrayList<SearchResult> results = new ArrayList<SearchResult>();
		for (DatabasePattern dp: _patterns) {
			if (useClassification && dp.getClassification() == false) { continue; }  //don't process
			
			if (matchPattern(wv, dp, dp.getRoot())) {
				Logger.log(Logger.LEVEL_TRACE, "Found match - " +dp.getRoot().getStringRepresentation());
				results.add(new SearchResult(wv,dp));
			}
		}
		return results;
	}
	
	private boolean matchPattern(WordVertex v1, DatabasePattern dbPatten, WordVertex vPattern) {
		// need to walk the graphs together to see if there is a match.  descend to children based upon the pattern
		if (dbPatten.nodeEquals(v1,vPattern,_uniqueEntities,_uniqueAttributes, _uniqueRelations)) {
			for (int i=0; i<vPattern.getNumberOfChildren();i++) {
				
				// TODO: need to make relationship more generic for conjunctions.  (prepositions should now work without problems)
				
				List<WordEdge> vEdges = v1.getChildRelationshipAll(vPattern.getChildAt(i),true);
				
				// NOTE its feasible that a node has multiple edges with the same result.  Hence, it is possible for us to match the same pattern multiple times
				// in a graph, not just once.
				
				if (vEdges.size() == 0) { return false; }
				boolean found = false;
				for (WordEdge vEdge: vEdges) {
					boolean result = matchPattern(vEdge.getChildNode(),dbPatten, vPattern.getChildAt(i).getChildNode());
					if (result) {  found = true; break; }
				}
				if (found == false) { return false; }
			}
			return true;
		}
		else {
			return false;
		}
	}

	/**
	 * For the given sentence, return any occurances in which the access control pattern exists within it
	 * 
	 * @param s
	 * @param acp
	 * @return
	 */
	public ArrayList<DatabaseRelation> matchDatabasePatternInSentence(Sentence s, DatabasePattern acp) {
		ArrayList<DatabaseRelation> results = new ArrayList<DatabaseRelation>();
		
		int numNodes = s.getNumberOfNodes();
		for (int i=0; i< numNodes; i++) {
			WordVertex wv = s.getWordVertexAt(i);
		
			if (matchPattern(wv, acp, acp.getRoot())) {
				results.addAll(this.extractDatabaseObjects(new SearchResult(wv,acp)));
			}
		}
		
		return results;
	}

		
	public class WordFrequency {
		public static final String MEAN         = "Mean";
		public static final String MEDIAN       = "Median";
		public static final String FIXED_NUMBER = "Fixed Number";
		
		public final Object[] OPTIONS = { MEAN, MEDIAN, FIXED_NUMBER };
		
		public HashMap<String,Integer> subjects = new HashMap<String,Integer>();     // list of subjects (the keys) with their frequencies - the values
		public HashMap<String,Integer> resources = new HashMap<String,Integer>();
		
		public double subjectMean;
		public double subjectVariance;
		public double subjectStandardDeviation;
		public double subjectMedian;
		public double[] subjectValues;
		public int    subjectMax   = Integer.MIN_VALUE;
		
		public double resourceMean;
		public double resourceVariance;
		public double resourceStandardDeviation;
		public double resourceMedian;
		public double[] resourceValues;
		public int    resourceMax    = Integer.MIN_VALUE;
		
		public double subjectCutOffValue = 3.0;
		public double resourceCutOffValue = 3.0;
		
		public void computeDescriptiveStatistics() throws Exception {
			double subjectTotal = 0.0;
			subjectValues = new double[subjects.size()];
			double resourceTotal = 0.0;
			resourceValues = new double[resources.size()];
			
			if (subjectValues.length == 0) {
				printMessage("No subjects found, returning with an exception");
				throw new Exception("No subjects defined");
			}
			if (subjectValues.length == 0) {
				printMessage("No resources found, returning with an exception");
				throw new Exception("No resources defined");
			}			
			
			int index = 0;
			for (int count: subjects.values()) {
				subjectTotal += count;
				subjectValues[index] = count;
				index++;
				if (count > subjectMax) {
					subjectMax = count;
				}
			}
			subjectMean = subjectTotal/subjects.size();
			
			double subjectVarianceSum = 0.0;
			for (int count: subjects.values()) {
				subjectVarianceSum += ((subjectMean-count) * (subjectMean-count));
			}
			subjectVariance = subjectVarianceSum / (subjects.size() - 1);
			subjectStandardDeviation = Math.sqrt(subjectVariance);
			
			//compute the median for subjects
			Arrays.sort(subjectValues);
			if (subjectValues.length % 2 == 0) {
				subjectMedian = ((double)subjectValues[subjectValues.length/2] + (double)subjectValues[subjectValues.length/2 - 1])/2;
			} else {
				subjectMedian = (double) subjectValues[subjectValues.length/2];			
			}
			
			
			index=0;
			for (int count: resources.values()) {
				resourceTotal += count;
				resourceValues[index] = count;
				index++;
				if (count > resourceMax) {
					resourceMax = count;
				}
			}
			resourceMean = resourceTotal/resources.size();
			
			double resourceVarianceSum = 0.0;
			for (int count: resources.values()) {
				resourceVarianceSum += ((resourceMean-count) * (resourceMean-count));
			}
			resourceVariance = resourceVarianceSum / (resources.size() - 1);	
			resourceStandardDeviation = Math.sqrt(resourceVariance);
			
			//compute the median for resources
			Arrays.sort(resourceValues);
			if (resourceValues.length % 2 == 0) {
				resourceMedian = ((double)resourceValues[resourceValues.length/2] + (double)resourceValues[resourceValues.length/2 - 1])/2;
			} else {
				resourceMedian = (double) resourceValues[resourceValues.length/2];			
			}
			
			subjectCutOffValue  = subjectMedian;
			resourceCutOffValue = resourceMedian;
		}
		
		public void askForCutOffValues() {
			String choice = (String) JOptionPane.showInputDialog(null, "How would you like to select the threshold:\n", "Threshold Selection",
                    JOptionPane.PLAIN_MESSAGE, null, this.OPTIONS, WordFrequency.MEAN);
			
			if (choice == null) {
				printMessage("Selection Cancelled - using defaults of median");
				return;
			}
			
			if (choice.equals(WordFrequency.FIXED_NUMBER)) {
				String paramString = (String) JOptionPane.showInputDialog(null, "Enter the subject cut-off value: ", "Threshold Value", JOptionPane.PLAIN_MESSAGE, null, null, "5");
				if (paramString != null) {
					try {
						subjectCutOffValue = Double.parseDouble(paramString);
					}
					catch (NumberFormatException nfe) {
						printMessage("Invalid parameter: "+paramString);
						printMessage("Defaulting to "+subjectCutOffValue);
					}
				}
				
				paramString = (String) JOptionPane.showInputDialog(null, "Enter the resource cut-off value: ", "Threshold Value", JOptionPane.PLAIN_MESSAGE, null, null, "5");
				if (paramString != null) {
					try {
						resourceCutOffValue = Double.parseDouble(paramString);
					}
					catch (NumberFormatException nfe) {
						printMessage("Invalid parameter: "+paramString);
						printMessage("Defaulting to "+resourceCutOffValue);
					}
				}			
				
			}
			else if (choice.equals(WordFrequency.MEAN)) {
				String paramString = (String) JOptionPane.showInputDialog(null, "Enter percentile of the mean:\n", "Threshold Selection", JOptionPane.PLAIN_MESSAGE, null, null, "1.0");
				if (paramString != null) {
					double value = 1.0;
					try {
						value = Double.parseDouble(paramString);
					}
					catch (NumberFormatException nfe) {
						printMessage("Invalid parameter: "+paramString);
						printMessage("Defaulting to 1.0");
					}
					subjectCutOffValue  = this.subjectMean  * value;
					resourceCutOffValue = this.resourceMean * value;
				}
			}
			else if (choice.equals(WordFrequency.MEDIAN)) {
				String paramString = (String) JOptionPane.showInputDialog(null, "Enter percenticle (.5 = median):\n", "Threshold Selection", JOptionPane.PLAIN_MESSAGE, null, null,"0.5");	
				if (paramString != null) {
					double value = 0.5;
					try {
						value = Double.parseDouble(paramString);
					}
					catch (NumberFormatException nfe) {
						printMessage("Invalid parameter: "+paramString);
						printMessage("Defaulting to 0.5");
					}
					if (value < 0.0 || value > 1.0) {
						printMessage("Invalid parameter: "+paramString);
						printMessage("Defaulting to 0.5");	
						value = 0.5;
					}
					subjectCutOffValue  = this.subjectValues[(int) (value * this.subjectValues.length)]; 
					resourceCutOffValue = this.resourceValues[(int) (value * this.resourceValues.length)]; 
				}			
			}		
		}
		
	}
	
	public DatabaseRelationManager.WordFrequency computeKeywordSeeds(NLDocument currentDocument, DatabasePattern seedPatterns[]) {
		WordFrequency results = new WordFrequency();
		
		ArrayList<DatabasePattern>   holdPattens = _patterns; 
		HashSet<String> holdUniqueEntities    = _uniqueEntities;
		HashSet<String> holdUniqueAttributes  = _uniqueAttributes;
		HashSet<String> holdUniqueRelations   = _uniqueRelations;
		
		_patterns = new ArrayList<DatabasePattern>();
		_uniqueEntities   = new HashSet<String>(); 
		_uniqueAttributes = new HashSet<String>(); 
		_uniqueRelations  = new HashSet<String>(); 
		
		for (DatabasePattern dp: seedPatterns) {_patterns.add(dp); }
		
		for (Sentence s :currentDocument.getSentences()) {
			// finds any access control matching our base(seed) patterns
			ArrayList<DatabaseRelation> dbList = DatabaseRelationManager.getTheDatabaseRelationManager().findDatabaseDesign(s,true);
			
			for (DatabaseRelation dr: dbList) { 
				String firstEntyNode    = dr.getFirstEntityNodeString();
				String secondEntityNode = dr.getSecondEntityNodeString();
				
				int count = results.resources.containsKey(firstEntyNode) ? results.resources.get(firstEntyNode) : 0;
				results.resources.put(firstEntyNode, count + 1);
				
				count =  results.resources.containsKey(secondEntityNode) ? results.resources.get(secondEntityNode) : 0;
				results.resources.put(secondEntityNode, count + 1);
			}
			
		}
		
		_patterns         = holdPattens;
	    _uniqueEntities   = holdUniqueEntities;
	    _uniqueAttributes = holdUniqueAttributes;
	    _uniqueRelations  = holdUniqueRelations;
		
	    return results;
	}
	
	@JsonIgnore
	public static List<DatabasePattern> extractBootstrapPattern(List<WordVertex> firstEntityNodes, List<WordVertex> secondEntityNodes) {
		List<DatabasePattern> results = new ArrayList<DatabasePattern>();
		
		for (WordVertex firstEntityVertex: firstEntityNodes) {
			for (WordVertex secondEntityVertex: secondEntityNodes) {
				Set<WordVertex> vertices = new HashSet<WordVertex>();  
				vertices.add(firstEntityVertex);
				vertices.add(secondEntityVertex);
				
				// 1) For each node, get the primary path to the root node
				HashMap<WordVertex, List<Object>> paths = new HashMap<WordVertex, List<Object>>();
				for (WordVertex wv: vertices) {
					paths.put(wv, wv.getPathToRoot());
				}
				WordVertex commonRoot = WordVertex.getLowestCommonAncestor(paths);  // 2. find the lowest root
				// 3. Generate a new graph consisting of just the vertices passed in and the common root (which may be one of those vertices)
				//    This is going to be done by creating a copy of the root, then copy over the path from each vertex to each root
				WordVertex resultRoot = new WordVertex(commonRoot);
				
				for (WordVertex wv: vertices) {
					List<Object> path = paths.get(wv);
					
					int rootIndex = WordVertex.getPositionOfVertexInPath(commonRoot, path);
					if (rootIndex == -1) { throw new IllegalStateException("ExtractPattern: didn't find common ancestor"); }
					
					WordVertex currentNode = resultRoot;
					for (int i=rootIndex-1;i>0;i=i-2) {  //we'll process both a relation and the child node in the same loop.
						WordEdge edge = (WordEdge) path.get(i);
						WordEdge existingEdge = currentNode.getChildRelationship(edge.getRelationship());
						if (existingEdge == null || !existingEdge.getChildNode().getOriginalWord().equals( ((WordVertex)path.get(i-1)).getOriginalWord() )) {
							WordVertex childNode = new WordVertex((WordVertex) path.get(i-1));
							WordEdge we = new WordEdge(edge.getRelationship(), currentNode, childNode);
							currentNode.addChild(we);
							childNode.addParent(we);
							currentNode = childNode;
						}
						else {
							currentNode = (WordVertex) existingEdge.getChildNode();
						}
					}
				}
				
				//TODO -> look at discovered verbs.  If there is more than one, then completely wildcard root
				
				List<WordVertex> discoveredVerbs = resultRoot.getVertexByPartOfSpeech(PartOfSpeech.VB, true,true);
				if (discoveredVerbs.size() == 0) { 
					// sometimes, the primary verb, especially in the case of "is/be" verb is not present directly. 
					// Examaple:  "a patient's account is access to an HCP.
					// In this case, let's just default to the root, if it's not in the subjects or resources already
					if (!firstEntityNodes.contains(resultRoot) && !secondEntityNodes.contains(resultRoot)) {
						discoveredVerbs.add(resultRoot);
					}
					//otherwise, we'll just leave the action blank.  We are better off identifying a possible situation than discarding it completely.
				}  
				
				List<WordVertex> tempFirst = new ArrayList<WordVertex>(); tempFirst.add(firstEntityVertex);
				List<WordVertex> tempSecond = new ArrayList<WordVertex>(); tempSecond.add(secondEntityVertex);
				
				DatabaseRelation databaseControl = new DatabaseRelation();
				databaseControl.setFirstEntityNode(tempFirst);
				databaseControl.setIdentifyingNode(discoveredVerbs);
				databaseControl.setSecondEntityNode(tempSecond);
				databaseControl.setRelationType(DatabaseRelationType.RELATIONSHIP);
				databaseControl.setRelationshipType(DatabaseRelationshipType.ASSOCIATION);
				
				DatabasePattern bp = new DatabasePattern(resultRoot, databaseControl, RelationSource.SEED,DatabaseRelationType.RELATIONSHIP);
				results.add(bp);
			}
		}
		
		return results;
	}		

	
	
	
	

	public void printReport(PrintStream ps) {
		ps.println("=========================================================================");
		ps.println("Database Pattern Report");
		ps.println("-------------------------------------------------------------------------");
		ps.println("Unique entitities: " + _uniqueEntities);
		ps.println("Unique attributes: " + _uniqueAttributes);
		ps.println("Unique relations: " + _uniqueRelations);
		ps.println("Number of patterns: "+_patterns.size());
		
		ps.println("NumberValid\tNumberInvalid\tNumberNotPresent\tSource\tType\tDatabasePattern\tEntity/Relationship\tAttribute\tFirstEntity\tSecondEntity\tSize");
		
		for (DatabasePattern dp: _patterns) {
			System.out.print(dp.getNumberOfValidSentences()+ "\t" + dp.getNumberOfInvalidSentences()+ "\t" + dp.getNumberOfNegativeSentences()+ "\t"+ dp.getDatabaseSource().name() +"\t");
			System.out.print(dp.getDatabaseItem().getRelationType() +"\t");
			System.out.print(dp.getRoot().getStringRepresentation()+"\t");
			if (dp.getDatabaseItem().getRelationType() == DatabaseRelationType.ENTITY) {
				System.out.print(dp.getDatabaseItem().getIdentifyingNodeString()+"\t\t\t\t");
				
			}
			else if (dp.getDatabaseItem().getRelationType() == DatabaseRelationType.ENTITY_ATTR) {
				System.out.print(dp.getDatabaseItem().getParentEntityNodeString()+"\t"+ dp.getDatabaseItem().getIdentifyingNodeString()+"\t\t\t");
			}
			else if (dp.getDatabaseItem().getRelationType() == DatabaseRelationType.RELATIONSHIP) {
				System.out.print(dp.getDatabaseItem().getIdentifyingNodeString()+"\t\t"+ dp.getDatabaseItem().getFirstEntityNodeString()+"\t"+ dp.getDatabaseItem().getSecondEntityNodeString()+"\t");				
			}
			else {
				System.out.print("\t\t\t\t");
			}
			
			System.out.println(dp.getRoot().getGraphSize());
		}
		ps.println("=========================================================================");
				
	}
	
	
	/* These attributes are added such that we can export/save off the state of the accesscontrolmanager in conjunction with a particular document */
	public HashSet<String> getEntities()   { return new HashSet<String>(this._uniqueEntities);	 }
	public HashSet<String> getAttributes() { return new HashSet<String>(this._uniqueAttributes); }
	public HashSet<String> getRelations()  { return new HashSet<String>(this._uniqueRelations);	 }

	
	public void setEntities(String[] entities) { 
		_uniqueEntities = new HashSet<String>(); 	
		Collections.addAll(_uniqueEntities, entities);
	}
	
	public void setAttributes(String[] attributes) { 
		_uniqueAttributes = new HashSet<String>(); 	
		Collections.addAll(_uniqueAttributes, attributes);
	}

	public void setRelations(String[] relations) { 
		_uniqueRelations = new HashSet<String>(); 	
		Collections.addAll(_uniqueRelations, relations);
	}
		
	public ArrayList<DatabasePattern> getDatabasePatterns() {
		return _patterns;
	}
	
	public void setDatabasePatterns(DatabasePattern[] patterns) {
		_patterns = new ArrayList<DatabasePattern>();
		Collections.addAll(_patterns, patterns);
	}

	@JsonIgnore
	public void setVerbose(boolean value) {
		_verbose = value;
	}
	
	
	public void printMessage(String s) {
		if (_verbose) {
			System.out.println(s);
			
		}
	}	
}
