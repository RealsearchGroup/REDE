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
import edu.ncsu.csc.nl.event.NLPEventType;
import edu.ncsu.csc.nl.model.NLDocument;
import edu.ncsu.csc.nl.model.Sentence;
import edu.ncsu.csc.nl.model.WordEdge;
import edu.ncsu.csc.nl.model.WordVertex;
import edu.ncsu.csc.nl.model.english.Ambiguity;
import edu.ncsu.csc.nl.model.english.Voice;
import edu.ncsu.csc.nl.model.type.AccessControlElement;
import edu.ncsu.csc.nl.model.type.RelationSource;
import edu.ncsu.csc.nl.model.type.PartOfSpeech;
import edu.ncsu.csc.nl.model.type.Relationship;
import edu.ncsu.csc.nl.model.type.WordType;
import edu.ncsu.csc.nl.util.Logger;


/**
 * AccessControlRelation manager is responsible for 
 *   - detecting events when a sentence is marked as having access defined
 *   - searching through the current access control relations for a particular match on the current sentence
 *   - performing "bootstrap" type operations to extend the possible patterns that may exist within the system.
 *   
 * @author John Slankas
 */
public class AccessControlRelationManager implements NLPEventListener {
	
	private String _actionSeeds[] = { "create","retrieve","update","delete" ,"edit","view","modify","record", "choose","read","add","order","write","see","change","select","display","generate","indicate","submit"};// , "display","send"  };
	private String _permissionSeeds[] = { "C","R","U","D", "RU","R","C","R","RU","CU","R","R","C","C","C","R","C","R","R","C","C","C" };		
	
	
	
	private ArrayList<AccessControlPattern> _patterns = new ArrayList<AccessControlPattern>();
	
	/**
	 *  list of the unique subjects extracted from the current document.  The hashmap is index by the word
	 *  itself (composed of the lemma, not the original word) and a list of word vertexes that correspond to it
	 */
	private HashSet<String> _uniqueSubjects = new HashSet<String>(); 
	private HashSet<String> _uniqueActions  = new HashSet<String>();
	private HashSet<String> _uniqueObjects  = new HashSet<String>(); 

	private boolean _verbose = true;
	

	private static AccessControlRelationManager _theAccessControlRelationManager = new AccessControlRelationManager();
	
	private AccessControlRelationManager() {
		NLPEventManager.getTheEventManager().registerForEvent(NLPEventType.SENTENCE_ACCESS_CONTROL_CHANGE, this);
	}
	
	public void setNewListsForActionsAndPermissions(String[] actions, String[] permissions) {
		_actionSeeds = actions;
		_permissionSeeds = permissions;
	}
	
	public static AccessControlRelationManager getTheAccessControlRelationManager() {
		return _theAccessControlRelationManager;
	}
	
	public void reset(boolean resetUniqueLists) {
		System.out.println("WARNING: AccessControlRelationManager: reset() called");
		_patterns = new ArrayList<AccessControlPattern>();

		if (resetUniqueLists) {
			_uniqueSubjects = new HashSet<String>();
			_uniqueActions = new HashSet<String>();
			_uniqueObjects = new HashSet<String>();
		}
	}

	/**
	 * Bootstrap performs the following actions:
	 *
	 * - determines whether or not to use the median or the mean as a cutoff
	 * - determines % of the median/mean to further use as a cutoff (default: 100%)
	 * - produces the simple patterns (ie, verb with action/subject wildcard)
	 * - finds all of the subjects/resources and determines their frequencies
	 * - produces the initial list of subjects(roles)/resources (delegated behavior to accesscontrolrelationshipManager)
	 */
	public void bootstrap(String domainWords[], boolean allowPronounSubjects) {
		this.printMessage("- creating seed patterns");


		String _actionSeeds[] = { "create","retrieve","update","delete" ,"edit","view","modify","record", "choose","read","add","order","write","see","change","select","display","generate","indicate","submit","sort","assign","show","collect"};// , "display","send"  };
		String _permissionSeeds[] = { "C","R","U","D", "RU","R","C","R","RU","CU","R","R","C","C","C","R","C","R","R","C","C","C","R","C","R","R" };
		
		AccessControlPattern seedPatterns[] = new AccessControlPattern[_actionSeeds.length];
		
		for (int i=0;i<_actionSeeds.length; i++) {
			seedPatterns[i] = AccessControlPattern.createBasicPattern(_actionSeeds[i],_permissionSeeds[i]);
			if (allowPronounSubjects) {
				seedPatterns[i] = AccessControlPattern.createBasicPatternWithPronominalSubject(_actionSeeds[i], _permissionSeeds[i], AccessControlPattern.WILDCARD_SAME_PART_OF_SPEECH);
			}
		}
		
		
		// Step 2: find all subjects, resources, and their matching counts
		AccessControlRelationManager.WordFrequency seedResults = this.computeKeywordSeeds(GCController.getTheGCController().getCurrentDocument(),seedPatterns);
		
		this.printMessage("Seed Results");
		for (String key: seedResults.subjects.keySet()) {
			this.printMessage(key + "\t"+ seedResults.subjects.get(key));
		}
		for (String key: seedResults.resources.keySet()) {
			this.printMessage(key + "\t"+ seedResults.resources.get(key));
		}
		try {
			seedResults.computeDescriptiveStatistics();
		}
		catch (Exception e) {
			this.printMessage("Unable to continue bootstrap process.  returning");
		}
		
		// Step 2.5 - get any user options for thresholds.
		seedResults.askForCutOffValues();
		
		// Step 3: populate subject/resource lists with those above the threshold.
		HashSet<String> setSubjects  = new HashSet<String>();
		HashSet<String> setResources = new HashSet<String>();
		for (String key: seedResults.subjects.keySet()) {
			if (seedResults.subjects.get(key) >= seedResults.subjectCutOffValue || seedResults.subjects.get(key) == seedResults.subjectMax) { setSubjects.add(key); }
		}
		for (String key: seedResults.resources.keySet()) {
			if (seedResults.resources.get(key) >= seedResults.resourceCutOffValue || seedResults.resources.get(key) == seedResults.resourceMax) { setResources.add(key); }
		}
		this.printMessage("Initial subject count: " + setSubjects.size());
		this.printMessage("Initial resource count: " + setResources.size());
		this.printMessage("Adding domain words: " + Arrays.toString(domainWords));

		for (String key: domainWords) {
			setResources.add(key); 
		}
		
		this.printMessage("Updated resource count: " + setResources.size());
		
		this.setResources(setResources.toArray(new String[0]));
		this.setSubjects(setSubjects.toArray(new String[0]));

	}	
	
	
	
	
	public void addSubject(String subject) {
		subject = subject.trim();
		if (subject.length()>0 /* && Ambiguity.allowableInSOA(subject) */) {
			_uniqueSubjects.add(subject);
		}
	}

	public void addAction(String action) {
		action = action.trim();
		if (action.length()>0 /* && Ambiguity.allowableInSOA(action) */) {
			_uniqueActions.add(action);
		}
	}

	public void addObject(String object) {
		object = object.trim();
		if (object.length()>0 /*&& Ambiguity.allowableInSOA(object) */) {
			_uniqueObjects.add(object);
		}
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
	
	private void updateUniqueLists(AccessControlPattern acp) {
		AccessControlRelation ac = acp.getAccessControl();
		if (!containsPronoun(ac.getSubjectVertexList())) {
			this.addSubject(ac.getSubject());			
		}
		if (!containsPronoun(ac.getActionVertexList())) {
			this.addAction(ac.getAction());			
		}
		if (!containsPronoun(ac.getObjectVertexList())) {
			this.addObject(ac.getObject());
		}
	
	}

	@Override
	public void eventOccured(NLPEventType eventType, NLPEvent event) {
		if (eventType == NLPEventType.SENTENCE_ACCESS_CONTROL_CHANGE) {
			NLPEventSentenceAccessControlMarkedEvent actualEvent = (NLPEventSentenceAccessControlMarkedEvent) event;
			processAccessControlElements(actualEvent.getSentence());
		}
		
	}

	/**
	 * Once a sentence has been marked as having the access control defined,
	 * this method is called via an eventListener
	 * 	
	 * @param s
	 */
	private void processAccessControlElements(Sentence s) { 
		if (s.isAccessControlDefined()) {
		
			List<AccessControlRelation> acList = s.getAccessControlRelations();
			if (acList.size() > 0) {
				Logger.log(Logger.LEVEL_TRACE, "Access control has been defined, processing....");
			}

			for (int i = acList.size()-1;i >=0; i--) {
				AccessControlRelation ac  = acList.get(i);
				try {
					
					if (ac.getAllVertices().size() == 0) {
						Logger.log(Logger.LEVEL_DEBUG, "warning: blank access control list defined, removing ...");
						acList.remove(i);
						continue;
					}
					
					WordVertex wvp = WordVertex.extractPattern(ac.getAllVertices());
					AccessControlPattern newPattern = new AccessControlPattern(wvp, new AccessControlRelation(ac,wvp), ac.getSource());
					this.updateUniqueLists(newPattern);  //we always need to updated the existing list no matter what, so do it now ...
					
					AccessControlPattern existingPattern = this.findExistingPattern(newPattern,true);
					if (existingPattern != null) {
						//existingPattern.addUnknownSentenceByOriginalPosition(s.getOriginalSentencePosition());
						Logger.log(Logger.LEVEL_DEBUG, "  - found existing pattern: "+existingPattern.getRoot().getStringRepresentation());					
					}
					else {
						Logger.log(Logger.LEVEL_DEBUG, "  - extracted new pattern: "+wvp.getStringRepresentation());
						if (ac.getSource() == RelationSource.USER) {
							newPattern.setAccessControlSource(RelationSource.PATTERN);
						}
						_patterns.add(newPattern);
							
						// Test for new patterns to create
						this.createTransformedPatterns(newPattern);
					}
				}
				catch (Throwable t) {
					System.err.println("Sentence: "+ s);
					System.err.println("Access control: "+ac);
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
		Logger.log(Logger.LEVEL_TRACE, "ACRM: processAccessControlElements exit");
	}
	
	/**
	 * Called in the code to automagically find and extract patterns...
	 * Note (3/14/2014) -> right now I think this code is only been called from the bootstrap function  (via ExtractAccessControl in AccessControlController)
	 *                  I can probably call this from the json load to restablish patterns 
	 *                  rather than saving them.  This also updates the lists themselves...
	 * 
	 * For the passed in sentence, it looks for any access control tuples that have been
	 * identified.  It creates patterns for all such tuples/rules.
	 * 
	 * @param s
	 */
	public void extractAccessControlPatternsFromDefinedAccessControlTuples(Sentence s) { 
		List<AccessControlRelation> acList = s.getAccessControlRelations();
		if (acList.size() > 0) {
			Logger.log(Logger.LEVEL_TRACE, "ACRM: extractACP from AC Tuples: - Found possible access control records, processing: "+ s.getSentence());
		}

		for (AccessControlRelation ac: acList) {
			try {
				WordVertex wvp = WordVertex.extractPattern(ac.getAllVertices());
				AccessControlPattern newPattern = new AccessControlPattern(wvp, new AccessControlRelation(ac,wvp), ac.getSource());
				//newPattern.addUnknownSentenceByOriginalPosition(s.getOriginalSentencePosition());
				this.updateUniqueLists(newPattern);  //we always need to updated the existing list no matter what, so do it now ...
				
				AccessControlPattern existingPattern = this.findExistingPattern(newPattern,true);
				if (existingPattern != null) {
					//existingPattern.addUnknownSentenceByOriginalPosition(s.getOriginalSentencePosition());
					Logger.log(Logger.LEVEL_TRACE, "  - found existing pattern: "+existingPattern.getRoot().getStringRepresentation());					
				}
				else {
					Logger.log(Logger.LEVEL_TRACE,"  - extracted new pattern: "+wvp.getStringRepresentation());
					if (ac.getSource() == RelationSource.USER) {
						newPattern.setAccessControlSource(RelationSource.PATTERN);
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
				System.err.println("Access Control: "+ac);
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
	public void findPatternsFromSubjectsAndResources(List<Sentence> sentences, int maxPatternSize) {
		// extract patterns from the cartesian product of subjects/resources
		
		HashSet<String> subjects  = new HashSet<String>(_uniqueSubjects);
		HashSet<String> resources = new HashSet<String>(_uniqueObjects);
		
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
					List<AccessControlPattern> bootstrapPatterns = AccessControlRelationManager.extractBootstrapPattern(possibleSubjectVertices, possibleResourceVertices);

					if (bootstrapPatterns.size() == 0) {
						System.out.println("No CARTESIAN pattern found");
					}              
					
					for (AccessControlPattern bootstrapPattern: bootstrapPatterns) {
						WordVertex wv = bootstrapPattern.getRoot();
						Logger.log(Logger.LEVEL_TRACE,"\t"+wv.getStringRepresentation());
						/*
						if (wv.getGraphSize() > maxPatternSize) {
							Logger.log(Logger.LEVEL_TRACE, "  - new pattern > "+maxPatternSize+" (dropping): "+bootstrapPattern.getRoot().getStringRepresentation());
							continue;
						}
						*/
						
						AccessControlPattern acp = this.findExistingPattern(bootstrapPattern,true);
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
	
	

	private AccessControlPattern findExistingPattern(AccessControlPattern patternToFind, boolean exactMatch) {
		for (AccessControlPattern existingPattern: _patterns) {
			if (AccessControlPattern.graphPatternsEquals(existingPattern, patternToFind, existingPattern.getRoot(), patternToFind.getRoot(),exactMatch )) {
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
			AccessControlPattern acp = _patterns.get(i);
			if (acp.hasBeenCheckedForTransformations()) { continue; }
			
			this.createTransformedPatterns(acp);
			acp.setCheckedForTransformations();
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
	 * @param acp
	 * @param createWildCardPOSPatterns
	 */
	private void createTransformedPatterns(AccessControlPattern acp) {

		WordVertex rootNode = acp.getRoot();
		if (rootNode.getPartOfSpeech().equalsCollapsed(PartOfSpeech.VB) == false) { return; } // can only manipulate patterns rooted with a verb
		if (Voice.inPassiveVoice(rootNode)) {
			if (rootNode.hasAllChildRelationships(REQUIRED_RELATIONSHIP_PASSIVE)) {
				AccessControlPattern activePattern = new AccessControlPattern(acp);
				activePattern.changePatternFromPassiveToActive();
				activePattern.setAccessControlSource(RelationSource.PATTERN_DERIVED_ACTIVE);
				
				if (this.findExistingPattern(activePattern,true) == null) {
					Logger.log(Logger.LEVEL_TRACE, "  - extracted new derived active pattern: "+activePattern.getRoot().getStringRepresentation());
					_patterns.add(activePattern);
				}
			}
		}
		else {   // pattern is in active voice, must have nsubj and dobj as children
			if (rootNode.hasAllChildRelationships(REQUIRED_RELATIONSHIP_ACTIVE)) {
				AccessControlPattern passivePattern = new AccessControlPattern(acp);
				passivePattern.changePatternFromActiveToPassive();
				passivePattern.setAccessControlSource(RelationSource.PATTERN_DERIVED_PASSIVE);
				
				if (this.findExistingPattern(passivePattern,true) == null) {
					Logger.log(Logger.LEVEL_TRACE, "  - extracted new derived passive pattern: "+passivePattern.getRoot().getStringRepresentation());
					_patterns.add(passivePattern);	
				}				
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
	public void expandPatternSet(boolean subjects, boolean actions, boolean resources, boolean prepositions) {
		Logger.log(Logger.LEVEL_DEBUG, "Expand Patterns START");
		Logger.log(Logger.LEVEL_DEBUG, "  Current Number of patterns "+_patterns.size());

		int maxIndex = _patterns.size();
		for( int i=0; i < maxIndex; i++) {
			AccessControlPattern acp = _patterns.get(i);
			if (acp.hasWildcardElement()) { // don't allow multiple wildcards unless we explicitly call for it 
				continue;
			}
			try {
				this.createExpandedPatterns(acp, subjects, actions, resources, prepositions);
			}
			catch (Exception e) {
				this.printMessage("  Unable to expand, removing: "+acp);
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
	private void createExpandedPatterns(AccessControlPattern acp, boolean subjects,  boolean actions, boolean resources, boolean prepositions) {
		AccessControlPattern newPattern = new AccessControlPattern(acp);
		
		if (subjects) {
			newPattern = AccessControlPattern.createMatchingElementOnPartofSpeechPattern(newPattern, AccessControlElement.SUBJECT);
		}
		
		if (resources) {
			newPattern = AccessControlPattern.createMatchingElementOnPartofSpeechPattern(newPattern, AccessControlElement.OBJECT);
		}			
		
		if (actions) {
			newPattern = AccessControlPattern.createMatchingElementOnPartofSpeechPattern(newPattern, AccessControlElement.ACTION);
		}		

		if (prepositions) {
			newPattern = AccessControlPattern.createMatchingElementOnPartofSpeechPattern(newPattern, AccessControlElement.PREPOSITION);
		}		
		
		if (this.findExistingPattern(newPattern,true) == null) {
			Logger.log(Logger.LEVEL_TRACE, "  - extracted wildcard pattern: "+newPattern.getRoot().getStringRepresentation());
			_patterns.add(newPattern);
		}		
		
	}
	
	
	public void injectBasicPatternWithPronominalSubject() {	
		
		for (int i=0; i < _actionSeeds.length; i++) {
			for (String subject: this.getSubjects()) {
				AccessControlPattern newPattern = AccessControlPattern.createBasicPatternWithPronominalSubject(_actionSeeds[i], _permissionSeeds[i], subject);
				if (this.findExistingPattern(newPattern,true) == null) {
					Logger.log(Logger.LEVEL_TRACE, "  - added injected pronominal pattern: "+newPattern.getRoot().getStringRepresentation());
					_patterns.add(newPattern);
				}
			}
		}			
	}
	
	public void injectMissingSubjectPattern() {
		for (int i=0; i < _actionSeeds.length; i++) {
			AccessControlPattern newPattern = AccessControlPattern.createDoubleNodeWithMissingSubjectPassive(_actionSeeds[i], _permissionSeeds[i]);
			if (this.findExistingPattern(newPattern,true) == null) {
				Logger.log(Logger.LEVEL_TRACE, "  - added injected missing subject passive pattern: "+newPattern.getRoot().getStringRepresentation());
				_patterns.add(newPattern);
			}
			newPattern = AccessControlPattern.createDoubleNodeWithMissingSubjectActive(_actionSeeds[i], _permissionSeeds[i]);
			if (this.findExistingPattern(newPattern,true) == null) {
				Logger.log(Logger.LEVEL_TRACE, "  - added injected missing subject active pattern: "+newPattern.getRoot().getStringRepresentation());
				_patterns.add(newPattern);
			}			
		}			
	}
	
	
	
	
	private static class SearchResult {
		public WordVertex root;
		public AccessControlPattern pattern;
		
		public SearchResult(WordVertex r, AccessControlPattern acp) {
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
	public ArrayList<AccessControlRelation> findAccessControl(Sentence s, boolean useClassification) {  // TODO.  Make it optional as to whether or not relationships should be limited..  Probably only during bootstrap processing...
		ArrayList<AccessControlRelation> result = new ArrayList<AccessControlRelation>();
		ArrayList<SearchResult> srList = this.walkGraph(s.getRoot(), new HashSet<Integer>(), useClassification);
		
		// the above could create duplicate access control items, will need to unify the results.
		// the unification of the results is done within the Sentence routine as it needs to account 
		// for any access control lists already defined there as well.
		for (SearchResult sr: srList) {
			result.addAll(extractAccessControl(sr));
		}
		
		
		return result;
	}

	public ArrayList<AccessControlRelation> extractAccessControl(SearchResult sr) {
		AccessControlRelation acPattern = sr.pattern.getAccessControl();
		ArrayList<AccessControlRelation> result = new ArrayList<AccessControlRelation>();
		
		List<ArrayList<WordVertex>> subjectLists = AccessControlRelationManager.createWordVertexListFromPatternList(sr.root,acPattern.getSubjectVertexList(), Relationship.getEmptyRelationships(), _uniqueSubjects);
		List<ArrayList<WordVertex>> actionLists  = AccessControlRelationManager.createWordVertexListFromPatternList(sr.root,acPattern.getActionVertexList(),  Relationship.getEmptyRelationships(), _uniqueActions);
		List<ArrayList<WordVertex>> objectLists  = AccessControlRelationManager.createWordVertexListFromPatternList(sr.root,acPattern.getObjectVertexList(),  Relationship.getEmptyRelationships(), _uniqueObjects);
		
		for (ArrayList<WordVertex> subjectlist: subjectLists) {
			for (ArrayList<WordVertex> actionList: actionLists) {
				for (ArrayList<WordVertex> objectList: objectLists) {
					AccessControlRelation extractedItem = new AccessControlRelation();
					
					extractedItem.setSubjectVertexList(subjectlist);
					extractedItem.setActionVertexList(actionList);
					extractedItem.setObjectVertexList(objectList);
					
					if (subjectlist.size() == 0 && objectList.size() == 0) {
						Logger.log(Logger.LEVEL_TRACE, "    access control found with no subject and no objects,  will skip");
						Logger.log(Logger.LEVEL_TRACE, "      search result root: "+sr.root.toString());
						Logger.log(Logger.LEVEL_TRACE, "      search result ac pattern: "+sr.pattern);
						continue;
					}
					if (subjectlist.equals(actionList) || subjectlist.equals(objectList) || actionList.equals(objectList)) {
						Logger.log(Logger.LEVEL_TRACE, "    access control found with subject=action, subject=object or action= object  ---  removing");
						continue;
					}
					// need to check for duplicated nodes as well.
					HashSet<WordVertex> existingNodes = new HashSet<WordVertex>();
					existingNodes.addAll(subjectlist);
					boolean found = false;
					for (WordVertex wv: actionList) {
						if (existingNodes.contains(wv)) { found = true;} else { existingNodes.add(wv); }
					}
					for (WordVertex wv: objectList) {
						if (existingNodes.contains(wv)) { found = true;} else { existingNodes.add(wv); }
					}
					if (found) {
						Logger.log(Logger.LEVEL_TRACE, "    access control found with a repeated node  ---  removing");
						continue;						
					}
					
					extractedItem.setPermissions(acPattern.getPermissions());

					if (acPattern.getNegativeVertex() != null) {
						extractedItem.setNegativeVertex(AccessControlRelationManager.createWordVertexListFromPatternVertex(sr.root,acPattern.getNegativeVertex(), Relationship.getEmptyRelationships(), new HashSet<String>()));
					}
					if (acPattern.getLimitToSubjectVertex() != null) {
						extractedItem.setLimitToSubjectVertex(AccessControlRelationManager.createWordVertexListFromPatternVertex(sr.root,acPattern.getLimitToSubjectVertex(), Relationship.getEmptyRelationships(), new HashSet<String>()));
					}
					extractedItem.setSource(sr.pattern.getAccessControlSource());
					
					result.add(extractedItem);
					
				}
			}
		}
		return result;
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
		for (AccessControlPattern acp: _patterns) {
			if (useClassification && acp.getClassification() == false) { continue; }  //don't process
			
			if (matchPattern(wv, acp, acp.getRoot())) {
				Logger.log(Logger.LEVEL_TRACE, "Found match - " +acp.getRoot().getStringRepresentation());
				results.add(new SearchResult(wv,acp));
			}
		}
		return results;
	}
	
	private boolean matchPattern(WordVertex v1, AccessControlPattern acPattern, WordVertex vPattern) {
		// need to walk the graphs together to see if there is a match.  descend to children based upon the pattern
		if (acPattern.nodeEquals(v1,vPattern,_uniqueSubjects,_uniqueActions, _uniqueObjects)) {
			for (int i=0; i<vPattern.getNumberOfChildren();i++) {
				
				// TODO: need to make relationship more generic for conjunctions.  (prepositions should now work without problems)
				
				List<WordEdge> vEdges = v1.getChildRelationshipAll(vPattern.getChildAt(i),true);
				
				// NOTE its feasible that a node has multiple edges with the same result.  Hence, it is possible for us to match the same pattern multiple times
				// in a graph, not just once.
				
				if (vEdges.size() == 0) { return false; }
				boolean found = false;
				for (WordEdge vEdge: vEdges) {
					boolean result = matchPattern(vEdge.getChildNode(),acPattern, vPattern.getChildAt(i).getChildNode());
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
	public ArrayList<AccessControlRelation> matchAccessControlPatternInSentence(Sentence s, AccessControlPattern acp) {
		ArrayList<AccessControlRelation> results = new ArrayList<AccessControlRelation>();
		
		int numNodes = s.getNumberOfNodes();
		for (int i=0; i< numNodes; i++) {
			WordVertex wv = s.getWordVertexAt(i);
		
			if (matchPattern(wv, acp, acp.getRoot())) {
				results.addAll(this.extractAccessControl(new SearchResult(wv,acp)));
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
	
	public AccessControlRelationManager.WordFrequency computeKeywordSeeds(NLDocument currentDocument, AccessControlPattern seedPatterns[]) {
		WordFrequency results = new WordFrequency();
		
		ArrayList<AccessControlPattern>   holdPattens = _patterns; 
		HashSet<String> holdUniqueSubjects = _uniqueSubjects;
		HashSet<String> holdUniqueActions  = _uniqueActions;
		HashSet<String> holdUniqueObjects  = _uniqueObjects; 	
		
		_patterns = new ArrayList<AccessControlPattern>();
		_uniqueSubjects = new HashSet<String>(); 
		_uniqueActions = new HashSet<String>(); 
		_uniqueObjects = new HashSet<String>(); 
		
		for (AccessControlPattern acp: seedPatterns) {_patterns.add(acp); }
		
		for (Sentence s :currentDocument.getSentences()) {
			// finds any access control matching our base(seed) patterns
			ArrayList<AccessControlRelation> acList = AccessControlRelationManager.getTheAccessControlRelationManager().findAccessControl(s,true);
			
			for (AccessControlRelation ac: acList) { 
				String subject = ac.getSubject();
				String resource = ac.getObject();
				
				int count = results.subjects.containsKey(subject) ? results.subjects.get(subject) : 0;
				results.subjects.put(subject, count + 1);
				
				count =  results.resources.containsKey(resource) ? results.resources.get(resource) : 0;
				results.resources.put(resource, count + 1);
			}
			
		}
		
		_patterns       = holdPattens;
		_uniqueSubjects = holdUniqueSubjects; 
		_uniqueActions  = holdUniqueActions; 
		_uniqueObjects  = holdUniqueObjects; 	
		
		return results;
	}
	
	@JsonIgnore
	public static List<AccessControlPattern> extractBootstrapPattern(List<WordVertex> subjects, List<WordVertex> resources) {
		List<AccessControlPattern> results = new ArrayList<AccessControlPattern>();
		
		for (WordVertex subjectVertex: subjects) {
			for (WordVertex resourceVertex: resources) {
				Set<WordVertex> vertices = new HashSet<WordVertex>();  
				vertices.add(subjectVertex);
				vertices.add(resourceVertex);
				
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
					if (!subjects.contains(resultRoot) && !resources.contains(resultRoot)) {
						discoveredVerbs.add(resultRoot);
					}
					//otherwise, we'll just leave the action blank.  We are better off identifying a possible situation than discarding it completely.
				}  
				
				List<WordVertex> tempSubject = new ArrayList<WordVertex>(); tempSubject.add(subjectVertex);
				List<WordVertex> tempResource = new ArrayList<WordVertex>(); tempResource.add(resourceVertex);
				
				AccessControlRelation accessControl = new AccessControlRelation();
				accessControl.setSubjectVertexList(tempSubject);
				accessControl.setActionVertexList(discoveredVerbs);
				accessControl.setObjectVertexList(tempResource);
				
				AccessControlPattern bp = new AccessControlPattern(resultRoot, accessControl, RelationSource.SEED);
				results.add(bp);
			}
		}
		
		return results;
	}		
	
	
	
	

	public void printReport(PrintStream ps) {
		ps.println("=========================================================================");
		ps.println("Access Control Pattern Report");
		ps.println("-------------------------------------------------------------------------");
		ps.println("Unique subjects: " + _uniqueSubjects);
		ps.println("Unique objects: " + _uniqueObjects);
		ps.println("Number of patterns: "+_patterns.size());
		
		ps.println("NumberValid\tNumberInvalid\tNumberNotPresent\tSource\tAccessControlPattern\tSubject\tAction\tObject\tsize");
		
		for (AccessControlPattern acp: _patterns) {
			System.out.print(acp.getNumberOfValidSentences()+ "\t" + acp.getNumberOfInvalidSentences()+ "\t" + acp.getNumberOfNegativeSentences()+ "\t"+ acp.getAccessControlSource().name() +"\t");
			System.out.print(acp.getRoot().getStringRepresentation());
			System.out.print("\t"+acp.getAccessControl().getSubjectAsVertexIDs());
			System.out.print("\t"+acp.getAccessControl().getActionAsVertexIDs());
			System.out.print("\t"+acp.getAccessControl().getObjectAsVertexIDs());
			System.out.println("\t"+acp.getRoot().getGraphSize());
		}
		ps.println("=========================================================================");
		
		
	}
	
	
	/* These attributes are added such that we can export/save off the state of the accesscontrolmanager in conjunction with a particular document */
	
	public HashSet<String> getSubjects() { return new HashSet<String>(this._uniqueSubjects);	}
	public HashSet<String> getActions() { return new HashSet<String>(this._uniqueActions);	    }
	public HashSet<String> getResources() { return new HashSet<String>(this._uniqueObjects);	}

	
	public void setSubjects(String[] subjects) { 
		_uniqueSubjects = new HashSet<String>(); 	
		Collections.addAll(_uniqueSubjects, subjects);
	}
	
	public void setActions(String[] actions) { 
		_uniqueActions = new HashSet<String>(); 	
		Collections.addAll(_uniqueActions, actions);
	}

	public void setResources(String[] resources) { 
		_uniqueObjects = new HashSet<String>(); 	
		Collections.addAll(_uniqueObjects, resources);
	}
		
	public ArrayList<AccessControlPattern> getAccessControlPatterns() {
		return _patterns;
	}
	
	public void setAccessControlPatterns(AccessControlPattern[] patterns) {
		_patterns = new ArrayList<AccessControlPattern>();
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
