package edu.ncsu.csc.nl;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import weka.core.Instances;
import edu.ncsu.csc.nl.model.NLDocument;
import edu.ncsu.csc.nl.model.Sentence;
import edu.ncsu.csc.nl.model.WekaCreator;
import edu.ncsu.csc.nl.model.WordEdge;
import edu.ncsu.csc.nl.model.WordVertex;
import edu.ncsu.csc.nl.model.english.Ambiguity;
import edu.ncsu.csc.nl.model.english.Negation;
import edu.ncsu.csc.nl.model.english.Readability;
import edu.ncsu.csc.nl.model.english.Voice;
import edu.ncsu.csc.nl.model.ml.ClassificationResult;
import edu.ncsu.csc.nl.model.ml.ConfusionMatrix;
import edu.ncsu.csc.nl.model.ml.WekaInterfacer;
import edu.ncsu.csc.nl.model.naivebayes.NaiveBayesClassifier;
import edu.ncsu.csc.nl.model.relation.AccessControlRelation;
import edu.ncsu.csc.nl.model.relation.AccessControlFactor;
import edu.ncsu.csc.nl.model.relation.AccessControlPattern;
import edu.ncsu.csc.nl.model.relation.AccessControlRelationManager;
import edu.ncsu.csc.nl.model.type.PartOfSpeech;
import edu.ncsu.csc.nl.model.type.Relationship;
import edu.ncsu.csc.nl.util.Logger;
import edu.ncsu.csc.nl.weka.WekaUtility;

/**
 * Singleton that performs the necessary behavior for extracting access control from the current system.
 * 
 * @author Adminuser
 *
 */
public class AccessControlController implements ActionListener {
	
	private static final AccessControlController _theAccessControlController = new AccessControlController(); 

	private NaiveBayesClassifier _bayes    = null;
	private WekaInterfacer _wekaClassifier = null; 
	private File _currentFileLocation = null;   //used with the save dialog for the bayes classifier

	ArrayList<AccessControlFactor> _factors_TR_OR_SR_SP = new ArrayList<AccessControlFactor>();
	{
		_factors_TR_OR_SR_SP.add(AccessControlFactor.FACTOR_TREE_RELATIONSHIP);
		_factors_TR_OR_SR_SP.add(AccessControlFactor.FACTOR_OBJECT_RELATIONSHIP);
		_factors_TR_OR_SR_SP.add(AccessControlFactor.FACTOR_SUBJECT_RELATIONSHIP);
		_factors_TR_OR_SR_SP.add(AccessControlFactor.FACTOR_SUBJECT_POS);
	}

	ArrayList<AccessControlFactor> _factors_TR_VOSA_RP = new ArrayList<AccessControlFactor>();
	{
		_factors_TR_VOSA_RP.add(AccessControlFactor.FACTOR_TREE_RELATIONSHIP);
		_factors_TR_VOSA_RP.add(AccessControlFactor.FACTOR_VOICE_ORDER_SUBJECT_ACTION);
		_factors_TR_VOSA_RP.add(AccessControlFactor.FACTOR_ROOT_POS);

	}	
	
	ArrayList<AccessControlFactor> _factors_TR_VOSA_OP = new ArrayList<AccessControlFactor>();
	{
		_factors_TR_VOSA_OP.add(AccessControlFactor.FACTOR_TREE_RELATIONSHIP);
		_factors_TR_VOSA_OP.add(AccessControlFactor.FACTOR_VOICE_ORDER_SUBJECT_ACTION);
		_factors_TR_VOSA_OP.add(AccessControlFactor.FACTOR_OBJECT_POS);
	}	
	
	ArrayList<AccessControlFactor> _factors_TR_VOSA_SO = new ArrayList<AccessControlFactor>();
	{
		_factors_TR_VOSA_SO.add(AccessControlFactor.FACTOR_TREE_RELATIONSHIP);
		_factors_TR_VOSA_SO.add(AccessControlFactor.FACTOR_VOICE_ORDER_SUBJECT_OBJECT);
		_factors_TR_VOSA_SO.add(AccessControlFactor.FACTOR_OBJECT_POS);
	}

	
	
	ArrayList<AccessControlFactor> _currentFactors = _factors_TR_OR_SR_SP;

	private AccessControlController() {   }

	private boolean _verbose = true;
	
	public static AccessControlController getTheAccessControlController()  {
		return _theAccessControlController;
	}


	@Override
	public void actionPerformed(ActionEvent ae) {
		GCController.getTheGCController().setStatusMessage("");
		
		_currentFactors.clear();
		{
			/*
			_currentFactors.add(AccessControlFactor.FACTOR_TREE_RELATIONSHIP);
			_currentFactors.add(AccessControlFactor.FACTOR_VOICE_ORDER_ACTION_OBJECT);
			_currentFactors.add(AccessControlFactor.FACTOR_PATTERN_SIZE);
			_currentFactors.add(AccessControlFactor.FACTOR_SUBJECT_VALUE);
			_currentFactors.add(AccessControlFactor.FACTOR_OBJECT_VALUE);
			*/
			_currentFactors.add(AccessControlFactor.FACTOR_TREE_RELATIONSHIP);
			_currentFactors.add(AccessControlFactor.FACTOR_OBJECT_RELATIONSHIP);
			_currentFactors.add(AccessControlFactor.FACTOR_SUBJECT_RELATIONSHIP);
			_currentFactors.add(AccessControlFactor.FACTOR_SUBJECT_POS);
			
			//FACTOR_TREE_RELATIONSHIP, FACTOR_VOICE_ORDER_ACTION_OBJECT, FACTOR_PATTERN_SIZE, FACTOR_SUBJECT_VALUE, FACTOR_OBJECT_VALUE			
			
		}		 
		//this.examinePerformanceByWorkCompletion(false);
		
		switch (ae.getActionCommand()) {	    
		    case GCConstants.ACTION_ACEXTRACT_RESET_ACRM:           resetACRM(true);           return;
			case GCConstants.ACTION_ACEXTRACT_BOOTSTRAP:		    bootstrap();               return;
			case GCConstants.ACTION_ACEXTRACT_SEARCH_FOR_PATTERNS:  searchForPatterns();       return;
			case GCConstants.ACTION_ACEXTRACT_TRANSFORM_PATTERNS:   transformPatterns();       return;
			case GCConstants.ACTION_ACEXTRACT_EXPAND_PATTERNS:      expandPatterns();          return;
			case GCConstants.ACTION_ACEXTRACT_EXTRACT_AC_CONTROL:   extractAccessControl();    return;
			case GCConstants.ACTION_ACEXTRACT_CYCLE_PROCESS:        cycleProcess(); cycleProcess();cycleProcess();           return;
			case GCConstants.ACTION_ACEXTRACT_DETECT_NEGATIVITY:    detectNegativity();        return;
			case GCConstants.ACTION_ACEXTRACT_MOVE_AC_TO_HOLD:      moveAccessControlToHold(); return;
			case GCConstants.ACTION_ACEXTRACT_ADD_HOLD_TO_ACTIVE:   addHoldToAccessControlRelations();  return;
			case GCConstants.ACTION_ACEXTRACT_RESTORE_AC_RELATIONS: restoreAccessControlRelations();    return;
			case GCConstants.ACTION_ACEXTRACT_ANALYZE_PATTERNS:     analyzePatterns(1.0);               return;
			case GCConstants.ACTION_ACEXTRACT_CREATE_BAYES:         createBayesClassifier(_currentFactors,true,1.0); return;
			case GCConstants.ACTION_ACEXTRACT_CLASSIFY_ACP:         classifyAccessControlPatterns(_currentFactors);  return;
			case GCConstants.ACTION_ACEXTRACT_CREATE_ACP_WEKA:      analyzePatterns(1.0);createWekaClassifier(false);	 return;
			case GCConstants.ACTION_ACEXTRACT_LOAD_ACP_WEKA:        loadWekaClassifier();  	 return;
			case GCConstants.ACTION_ACEXTRACT_SAVE_ACP_WEKA:        saveWekaClassifier();	 return;
			case GCConstants.ACTION_ACEXTRACT_CLASSIFY_ACP_WEKA:    classifyAccessControlPatternWithWeka(false); 	 return;
			case GCConstants.ACTION_ACEXTRACT_EVALUATE_AC:          evaluateAccessControl(false,0.0);            	 return;
			case GCConstants.ACTION_ACEXTRACT_EVAL_DOC_COMPLETION:  examinePerformanceByWorkCompletion(false);		 return;
			case GCConstants.ACTION_ACEXTRACT_REPORT_AC_PATTERNS:   report();                                		 return;
			case GCConstants.ACTION_ACEXTRACT_REPORT_ON_HOLD:       reportHold();                               	 return;
	
			case GCConstants.ACTION_ACEXTRACT_LOAD_NB_CLASSIFIER:   loadClassifier();              return;
			case GCConstants.ACTION_ACEXTRACT_SAVE_NB_CLASSIFIER:   saveClassifier();              return;
			case GCConstants.ACTION_ACEXTRACT_ADD_TO_NB_CLASSIFIER: addToClassifier(_currentFactors); return;
			case GCConstants.ACTION_ACEXTRACT_ANALYZE_FACTORS:      analyzeFactors(false);         return;
			case GCConstants.ACTION_ACEXTRACT_REPORT_UNDISCOVERED_PATTERNS: reportPatternsWithIssues(System.out,true,true,true,false); return;
			case GCConstants.ACTION_ACEXTRACT_INJECT_PATTERN:       injectPatterns(); return;
		}
		
	}
	
	/**
	 * Resets the Access Control Manager.  This "wipes opt" the patterns, and discovered subjects/resources/actions.
	 */
	public void resetACRM(boolean resetUniqueLists) {
		resetUniqueLists = false;

		AccessControlRelationManager.getTheAccessControlRelationManager().reset(resetUniqueLists);
		
		Logger.setCurrentLoggingLevel(Logger.LEVEL_INFO);
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
	public void bootstrap() {
		
		this.printMessage("Bootstrap called");



		//If the default lists need to be overriden, can do here
		//String actionSeeds[] = { "create","retrieve","update","delete" ,"edit","view","modify","enter", "choose","select"};// , "display","send"  };
		//String permissionSeeds[] = { "C","R","U","D", "RU","R","C","R","RU","CU","R","R"};

		//THESE WERE USED FOR THE SECOND STUDY.
		//String actionSeeds[] = { "create","retrieve","update","delete" ,"edit","view","modify","record", "choose","read","add","order","write","see","change","select","display"};// , "display","send"  };
		//String permissionSeeds[] = { "C","R","U","D", "RU","R","C","R","RU","CU","R","R","C","C","C","R","C","R","R" };

		String actionSeeds[] = { "create","retrieve","update","delete" ,"edit","view","modify","record", "choose","read","add","order","write","see","change","select","display"};// , "display","send"  };
		String permissionSeeds[] = { "C","R","U","D", "RU","R","C","R","RU","CU","R","R","C","C","C","R","C","R","R" };
		
		
		//AccessControlRelationManager.getTheAccessControlRelationManager().setNewListsForActionsAndPermissions(actionSeeds, permissionSeeds);
		

		String domainWords[] = {};  // place domain specific keyword here to "help" prime the bootstrapping...
		
		AccessControlRelationManager.getTheAccessControlRelationManager().bootstrap(domainWords,true);
		
		GCController.getTheGCController().setStatusMessage("Bootstrap complete");
	}
	
	public void searchForPatterns() {
		AccessControlRelationManager.getTheAccessControlRelationManager().findPatternsFromSubjectsAndResources(GCController.getTheGCController().getCurrentDocument().getSentences(),6);

		GCController.getTheGCController().setStatusMessage("Search for patterns called");
	}
	
	public void transformPatterns() {
		AccessControlRelationManager.getTheAccessControlRelationManager().transformPatterns();
		
		GCController.getTheGCController().setStatusMessage("Transform patterns called");
	}	
	
	public void expandPatterns() {
		AccessControlRelationManager.getTheAccessControlRelationManager().expandPatternSet(true, false, false, false);
		AccessControlRelationManager.getTheAccessControlRelationManager().expandPatternSet(false, false, true, false);
		AccessControlRelationManager.getTheAccessControlRelationManager().expandPatternSet(false, false, true, true);
		AccessControlRelationManager.getTheAccessControlRelationManager().expandPatternSet(false, true, false, false);

		//These wildcards bring in additional patterns, but at the high cost (especially the latter) of false positives.
		//AccessControlRelationManager.getTheAccessControlRelationManager().expandPatternSet(false, true, true, true);
		//AccessControlRelationManager.getTheAccessControlRelationManager().expandPatternSet(true, true, true, true);

		
		GCController.getTheGCController().setStatusMessage("Expand patterns called");
	}		
	
	public void extractAccessControl() {
		GCController.getTheGCController().setStatusMessage("Extract access control called");
		
		for (Sentence s: GCController.getTheGCController().getCurrentDocument().getSentences()) {
			s.setAccessControlRelations(new ArrayList<AccessControlRelation>());
			s.setAccessControlDefined(false,false);
			s.processSentenceForAccessControlElements();
			AccessControlRelationManager.getTheAccessControlRelationManager().extractAccessControlPatternsFromDefinedAccessControlTuples(s);
		}
	}	
	
	/**
	 * Calls search, expand, and transform,  followed extract access control
	 */
	public void cycleProcess() {
		Logger.log(Logger.LEVEL_INFO, "CycleProcess - search");
		this.searchForPatterns();
		
		Logger.log(Logger.LEVEL_INFO, "CycleProcess - expand");
		this.expandPatterns();
		
		Logger.log(Logger.LEVEL_INFO, "CycleProcess - transform");
		this.transformPatterns();
		
		Logger.log(Logger.LEVEL_INFO, "CycleProcess - update classifier");
		this.addToClassifier(_currentFactors);
		
		Logger.log(Logger.LEVEL_INFO, "CycleProcess - classify patterns");
		this.classifyAccessControlPatterns(_currentFactors);
		
		Logger.log(Logger.LEVEL_INFO, "CycleProcess - extract access control");		
		this.extractAccessControl();
		
		Logger.log(Logger.LEVEL_INFO, "CycleProcess - evaluate access control");		
		this.evaluateAccessControl(false,0.0);
	}
	
	public void injectPatterns() {
		//Logger.setCurrentLoggingLevel(Logger.LEVEL_TRACE);
		Logger.log(Logger.LEVEL_TRACE,"ACC: Starting Inject Action Object Patterns");

		AccessControlRelationManager.getTheAccessControlRelationManager().injectBasicPatternWithPronominalSubject();
		AccessControlRelationManager.getTheAccessControlRelationManager().injectMissingSubjectPattern();
		
		//Logger.setCurrentLoggingLevel(Logger.LEVEL_INFO);
	}
	
	
	
	public void detectNegativity() {
		Logger.log(Logger.LEVEL_INFO,"ACC: Starting NegativityCheck");

		for (Sentence s: GCController.getTheGCController().getCurrentDocument().getSentences()) {
			for (AccessControlRelation ac: s.getAccessControlRelations()) {	
				if (ac.getNegativeVertex() != null) {  continue;  }    // already negativity, no reason to process
				
				WordVertex wvp = WordVertex.extractPattern(ac.getAllVertices());
				//AccessControlPattern newPattern = new AccessControlPattern(wvp, new AccessControl(ac,wvp), ac.getSource());
				int result = this.walkTreeForNegativity(wvp, s, new HashSet<Integer>());
				if (result != -1) {
					WordVertex negVertex = s.getRoot().getVertexByID(result);
					ac.setNegativeVertex(negVertex);
					Logger.log(Logger.LEVEL_DEBUG,"ACC: "+s.getOriginalSentencePosition()+" - found negative vertex("+negVertex+"): "+s.getSentence());
				}
			}
		}
		Logger.log(Logger.LEVEL_INFO,"ACC: NegativityCheck complete");		
	}
	
	/**
	 * From the accesscontrol pattern starting a wv, is there a negative element contained within it?
	 * As we also need to check the frontier, we need the sentence to get the corresponding nodes to 
	 * check children that are determiners, negativity, adverbs, or negative conjunctions.
	 * 
	 * @param wv
	 * @param s
	 * @param visitedNodes
	 * @return
	 */
	private int walkTreeForNegativity(WordVertex wv, Sentence s, HashSet<Integer> visitedNodes) {
		if (visitedNodes.contains(wv.getID())) {
			return -1;
		}
		else {
			visitedNodes.add(wv.getID());
		}		
		
		// is the node itself negative?
		if (Negation.isWordVertexNegative(GCController.getTheGCController().getWordNetDictionary(), wv)) {
			return wv.getID();
		}
		
		// now check any of this node's (part of the actual pattern) children
		for (int i=0; i< wv.getNumberOfChildren(); i++) {
			int tempResult = this.walkTreeForNegativity(wv.getChildAt(i).getChildNode(), s, visitedNodes);
			if (tempResult != -1) { return tempResult; }
		}
		
		// now see if we need to check the frontier of the pattern (from the actual sentence for negativity
		WordVertex correspondingVertex = s.getRoot().getVertexByID(wv.getID());
		for (int i=0; i< correspondingVertex.getNumberOfChildren(); i++) {
			WordEdge we = correspondingVertex.getChildAt(i);
			if (we.getRelationship() == Relationship.DET || 
				we.getRelationship() == Relationship.DET_NEG || 
				we.getRelationship() == Relationship.NEG || 
				we.getRelationship() == Relationship.ADVMOD ||
				(we.getRelationship() == Relationship.NSUBJ && we.getChildNode().getPartOfSpeech() == PartOfSpeech.DT) ||
				we.getRelationship() == Relationship.CONJ_NEGCC) {
				if (Negation.isWordVertexNegative(GCController.getTheGCController().getWordNetDictionary(), we.getChildNode())) {
					return we.getChildNode().getID();
				}
			}
		}
	
		return -1;
	}
	
	public void moveAccessControlToHold() {
		GCController.getTheGCController().getCurrentDocument().moveAccessControlRelationsToHold();
		GCController.getTheGCController().setStatusMessage("Access controlled moved to hold for all elements");
	}
	
	public void addHoldToAccessControlRelations() {
		GCController.getTheGCController().getCurrentDocument().addHoldToAccessControlRelations();
		GCController.getTheGCController().setStatusMessage("Added access control in the hold to the active.");
	}
	
	public void restoreAccessControlRelations() {
		//AccessControlRelationManager.getTheAccessControlRelationManager().reset();
		GCController.getTheGCController().getCurrentDocument().moveHoldToAccessControlRelations();
		GCController.getTheGCController().setStatusMessage("Access control re-established from hold elements");
	}	
	
	/**
	 * Checks the patterns to see if they will produce the right results or not for the sentence.
	 * The "gold standard" is maintained in the hold list.  the current access control is temporarily over-written to analyze and then restored
	 * 
	 * 
	 * @param percentOfDocumentToUse this allows us to only consider a certain percentage of the document when creating a classifer
	 *        which allows us to evaluate the tool's performance as a user works through the tool.  should be between 0.0 and 1.0;
	 */
	private void analyzePatterns(double percentOfDocumentToUse) {
		NLDocument currentDocument = GCController.getTheGCController().getCurrentDocument(); 
		
		AccessControlRelationManager acrm = AccessControlRelationManager.getTheAccessControlRelationManager();
		
		List<Sentence> sentences = currentDocument.getSentences();
		int maxSentenceNumberToUse = (int)  (sentences.size() * percentOfDocumentToUse);
		for (AccessControlPattern acp: AccessControlRelationManager.getTheAccessControlRelationManager().getAccessControlPatterns()) {
			acp.resetSentenceOccurances();
			
			for (int i=0; i < maxSentenceNumberToUse; i++) {
				Sentence s = sentences.get(i);
				if (s.hasBooleanClassification("access control:functional") == false) { continue; } // only analyze sentences that have been classified for access control function
				if (s.isAccessControlDefined() == false ) { continue; } // only analyze sentences that have been marked complete

				List<AccessControlRelation> currentList = s.getAccessControlRelations();
				List<AccessControlRelation> holdList = s.getHoldAccessControlRelations();
				s.setAccessControlRelations(holdList);
				
				ArrayList<AccessControlRelation> tempResults = acrm.matchAccessControlPatternInSentence(s, acp);
				
				if (tempResults.size() == 0) {
					acp.addNegativeSentence(s);
				}
				else {
					for (AccessControlRelation tempAC: tempResults) {
						/*
						 * Temporary code to find why something was not showing...
						if (tempAC.toString().equals(";sort;list") && s.toString().startsWith("The patient views the list sorted by the role of the accessor relative to the patient")) {
							System.out.println("Heere");
						}
						*/
						if (s.hasAccessControlBySubjectActionObject(tempAC) || s.containsAccessControl(tempAC,false,false)) {
							acp.addValidSentence(s);
						}
						else {
							acp.addInvalidSentence(s);
							Logger.log(Logger.LEVEL_TRACE, "Invalid AC: "+tempAC+" ---- "+s);
						}
					}
				}
				s.setAccessControlRelations(currentList);
			}
		}
		GCController.getTheGCController().setStatusMessage("AC Patterns analyzed");
	}
	
	private void analyzeFactors(boolean includeContextSensitiveFactors) {
		//System.out.println(AccessControlFactor.values().length);
		
		// Use this call to force all parameters to be evaluated.  Otherwise the process stops on the first
		// attribute that decreases performance.
		boolean useCompleteSelection = true;
		
		
		this.analyzePatterns(1.0);
		
		NaiveBayesClassifier _holdClassifier = _bayes;
		
		ArrayList<AccessControlFactor> factors = new ArrayList<AccessControlFactor>();
		
		int numCycles = AccessControlFactor.values().length;
		if (includeContextSensitiveFactors == false) {
			numCycles = AccessControlFactor.getNumberOfContextInsensitiveFactors();
		}
		
		double topOverallF1Value             = Double.MIN_VALUE;
		ArrayList<AccessControlFactor> topFactors = new ArrayList<AccessControlFactor>();
		
		for (int cycleNum = 0; cycleNum < numCycles; cycleNum++) {			
			AccessControlFactor topFactor = null;
			double topF1Value             = Double.MIN_VALUE;
			
			for (AccessControlFactor acf: AccessControlFactor.values()) {
				if (includeContextSensitiveFactors == false && acf.isContextSpecific()) { continue;}  // need to skip these
				if (factors.contains(acf)) { continue; }
				factors.add(acf);
			
				System.out.println(acf);
				this.createBayesClassifier(factors,false,1.0);
				this.classifyAccessControlPatterns(factors);
				this.extractAccessControl();
				ConfusionMatrix results = this.evaluateAccessControl(false,0.0);
				
				if (results.getF1Measure() > topF1Value) {
					topFactor = acf;
					topF1Value = results.getF1Measure();
				}
				
				factors.remove(acf);
			}
			
			if (topF1Value > topOverallF1Value) {
				factors.add(topFactor);
				topFactors = (ArrayList<AccessControlFactor>) factors.clone();
				topOverallF1Value = topF1Value;
			}
			else {
				if (useCompleteSelection) {
					factors.add(topFactor);
				}
				else {
					Logger.log(Logger.LEVEL_INFO, "stopped process, performance decreased");
					break;
				}
			}
			/* cycle through all of the factors not already in the factor list
			   - if present, continue
			   - if not present
			     -- add to factor list
			     -- compute the NB
			     -- classify access control patterns
			     -- extract access control
			     -- evaluate ACP
			     -- store F! result in list
			     -- remove from factor list
			   Once complete, get the highest F1 value from the hashmap and the corresponding factor.
			   if greater than the best performance, add factor to the list and continue the cycle process.  update the best performance
			   if not greater, stop the process, we've found the factors to use.  Store those in the list.  print out the result.
			    
			*/
		}

		System.out.println(topFactors);
		
		_currentFactors = topFactors;
		
		_bayes = _holdClassifier;		
	}
	
	private void createBayesClassifier(ArrayList<AccessControlFactor> factorsForClassification, boolean analyzePatterns, double percentOfDocumentToUse) {
		if (analyzePatterns) {this.analyzePatterns(percentOfDocumentToUse); }
		
		_bayes = new NaiveBayesClassifier();

		_bayes.addClass("valid");
		_bayes.addClass("invalid");
		_bayes.addLikelyhood("text", "words", new String[0]);
		
		
		List<AccessControlPattern> patternList = AccessControlRelationManager.getTheAccessControlRelationManager().getAccessControlPatterns();
		
		for (AccessControlPattern acp: patternList) {
			String[] factors = acp.getFactors(factorsForClassification);
						
			for (int i=0;i<acp.getNumberOfValidSentences(); i++) {
				_bayes.incrementClass("valid");
				for (String factor: factors) {
					_bayes.incrementClassLikelyhood("valid","text", factor);
				}
				
			}
			
			
			for (int i=0;i<acp.getNumberOfInvalidSentences(); i++) {
				_bayes.incrementClass("invalid");
				for (String factor: factors) {
					_bayes.incrementClassLikelyhood("invalid","text", factor);
				}				

			}
		}		
		
		GCController.getTheGCController().setStatusMessage("Bayes Classifier Created");
		
	}
	
	public void addToClassifier(ArrayList<AccessControlFactor> factorsToUse) {

		// check created first
		if (_bayes == null) {
			GCController.getTheGCController().setStatusMessage("Bayes Classifier not created yet - not updated");
			return;
		}
		this.analyzePatterns(1.0);
	
		List<AccessControlPattern> patternList = AccessControlRelationManager.getTheAccessControlRelationManager().getAccessControlPatterns();
		
		for (AccessControlPattern acp: patternList) {
			String[] factors = acp.getFactors(factorsToUse);
						
			for (int i=0;i<acp.getNumberOfValidSentences(); i++) {
				_bayes.incrementClass("valid");
				for (String factor: factors) {
					_bayes.incrementClassLikelyhood("valid","text", factor);
				}
				
			}
			
			
			for (int i=0;i<acp.getNumberOfInvalidSentences(); i++) {
				_bayes.incrementClass("invalid");
				for (String factor: factors) {
					_bayes.incrementClassLikelyhood("invalid","text", factor);
				}				

			}
		}		
		
		GCController.getTheGCController().setStatusMessage("Bayes Classifier updated");
				
	}
	
	
	
	public void loadClassifier() {
		JFileChooser fileChooser = new JFileChooser();
		int returnVal = fileChooser.showOpenDialog(null); //GCController.getTheGCController().getMainFrame());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File f = fileChooser.getSelectedFile();
			try (ObjectInputStream input = new ObjectInputStream(new FileInputStream( f ) )){
				_bayes = ( NaiveBayesClassifier ) input.readObject();
				_currentFactors = (ArrayList<AccessControlFactor>) input.readObject();
				_currentFileLocation = f;
		    	
				GCController.getTheGCController().setStatusMessage("NB Classifier loaded: "+f.getAbsolutePath());
			}
			catch (Exception e) {
				GCController.getTheGCController().setStatusMessage("Unable to ACP NB Classifer from "+f.getAbsolutePath());
				System.err.println("Unable to load ACP NB Classifer: "+e);
				e.printStackTrace();
			}
		}
	}
	
	
	public void saveClassifier() {
		if (_bayes == null) {
			GCController.getTheGCController().setStatusMessage("Bayes Classifier not created yet - save not possible");
			return;
		}		
		
		JFileChooser fileChooser;
		if (_currentFileLocation != null) {
			fileChooser = new JFileChooser(_currentFileLocation);
		}
		else {
			fileChooser = new JFileChooser();
		}
		int returnVal = fileChooser.showSaveDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File f = fileChooser.getSelectedFile();
			try (ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(f) )) {
				output.writeObject(_bayes);
				output.writeObject(_currentFactors);
				_currentFileLocation = f;
				GCController.getTheGCController().setStatusMessage("NB classifier saved as serial: "+f.getAbsolutePath());
			}
			catch (Exception e) {
				GCController.getTheGCController().setStatusMessage("Unable to save serial NB classifier to "+f.getAbsolutePath());
				System.err.println("Unable to save serial NB Classifier: "+e);
			}
		}	
	}
	
	protected void exportARFFForPatterns(boolean includeContextSensitiveFactors) {

		
		WekaCreator wc  = new WekaCreator();		
		Instances dataSet = wc.createWekaInstancesForAccessControlPatterns("acp", includeContextSensitiveFactors);
		JFileChooser fileChooser = new JFileChooser();
		int returnVal = fileChooser.showSaveDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File f = fileChooser.getSelectedFile();
			try {
				WekaUtility.exportInstancesAsARFF(dataSet, f);
				GCController.getTheGCController().setStatusMessage("Data exported "+f.getAbsolutePath());
			}
			catch (Exception e) {
				GCController.getTheGCController().setStatusMessage("Unable to export data to "+f.getAbsolutePath());
				System.err.println("Unable export ARFF File: "+e);
			}
		}		
	}
	
	
	/**
	 * Classifies all of the access control patterns in the ACRM
	 */
	private void classifyAccessControlPatterns(ArrayList<AccessControlFactor> factorsForClassification) {
		
		AccessControlRelationManager acrm = AccessControlRelationManager.getTheAccessControlRelationManager();
		
		if (_bayes == null) {
			Logger.log(Logger.LEVEL_DEBUG, "ACC/classifyAccessControlPatterns: skipping classification, bayes classifier not defined.");
			return;
		}
		
		for (AccessControlPattern acp: acrm.getAccessControlPatterns()) {
			Object[] arguments = new Object[1];
			arguments[0] = acp.getFactors(factorsForClassification);
			
			Map<String, Double> result = _bayes.computeClassProbabilitiesByLogs(arguments);		//computeClassProbabilitiesByLogs //computeClassProbabilities
			
			acp.setClassification(result.get("valid") > result.get("invalid"));
			
			Logger.log(Logger.LEVEL_TRACE, acp.getClassification()+": "+acp);
		}
		
		GCController.getTheGCController().setStatusMessage("Access control patterns classified.");
		//this.createWekaClassifier(false);
		//this.classifyAccessControlPatternWithWeka(false);
		
	}
	
	protected void loadWekaClassifier() {
		JFileChooser fileChooser = new JFileChooser();
		int returnVal = fileChooser.showOpenDialog(null); //GCController.getTheGCController().getMainFrame());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File f = fileChooser.getSelectedFile();
			try (ObjectInputStream input = new ObjectInputStream(new FileInputStream( f ) )){
				_wekaClassifier = ( WekaInterfacer ) input.readObject();
				_currentFileLocation = f;
		    	
				GCController.getTheGCController().setStatusMessage("Weka ACP Classifier loaded: "+f.getAbsolutePath());
			}
			catch (Exception e) {
				GCController.getTheGCController().setStatusMessage("Unable to load Weka ACP Classifer from "+f.getAbsolutePath());
				System.err.println("Unable to load document: "+e);
				e.printStackTrace();
			}
		}		
		
	}
	
	
	protected void saveWekaClassifier() {
		if (_wekaClassifier == null) {
			GCController.getTheGCController().setStatusMessage("Weka ACP Classifier not created yet - save not possible");
			return;
		}		
		
		JFileChooser fileChooser;
		if (_currentFileLocation != null) {
			fileChooser = new JFileChooser(_currentFileLocation);
		}
		else {
			fileChooser = new JFileChooser();
		}
		int returnVal = fileChooser.showSaveDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File f = fileChooser.getSelectedFile();
			try (ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(f) )) {
				output.writeObject(_wekaClassifier);
				_currentFileLocation = f;
				GCController.getTheGCController().setStatusMessage("Weka classifier saved as serial: "+f.getAbsolutePath());
			}
			catch (Exception e) {
				GCController.getTheGCController().setStatusMessage("Unable to save Weka classifier to "+f.getAbsolutePath());
				System.err.println("Unable to save Weka Classifier: "+e);
			}
		}					
		
	}
	
	/** 
	 * Creates the specified Weka classifier.  Analyze patterns must be called prior to this.) 
	 * 
	 * @param includeContextSensitiveFactors
	 */
	protected void createWekaClassifier(boolean includeContextSensitiveFactors) {
		Logger.log(Logger.LEVEL_DEBUG, "Creating Weka classifier");	
		
		_wekaClassifier = new WekaInterfacer();
		WekaCreator wc  = new WekaCreator();
		Instances dataSet = wc.createWekaInstancesForAccessControlPatterns("acp", includeContextSensitiveFactors );
		_wekaClassifier.trainAccessControlPatternClassifier(dataSet );
		
		Logger.log(Logger.LEVEL_DEBUG, "Finished creating Weka classifier");
	}
	
	
	/**
	 * Classifies all of the access control patterns in the ACRM
	 */
	protected void classifyAccessControlPatternWithWeka(boolean includeContextSensitiveFactors) {
		Logger.log(Logger.LEVEL_DEBUG, "Starting to classify with Weka");

		WekaCreator wc  = new WekaCreator();
		
		AccessControlRelationManager acrm = AccessControlRelationManager.getTheAccessControlRelationManager();

		for (AccessControlPattern acp: acrm.getAccessControlPatterns()) {
			Instances dataSet = wc.createWekaInstancesForAccessControlPatterns("test Fold", acp, includeContextSensitiveFactors);
			ClassificationResult cr = _wekaClassifier.classifyAccessControlPattern(dataSet);
			acp.setClassification(cr.averageDistance == 0.0);
		}
		Logger.log(Logger.LEVEL_DEBUG, "Finished classifying with Weka");
		
		GCController.getTheGCController().setStatusMessage("Access control patterns classified.");
	}	
	
	protected void examinePerformanceByWorkCompletion(boolean includeContextSensitiveFactors) {
		Logger.setCurrentLoggingLevel(Logger.LEVEL_DEBUG);
		Logger.log(Logger.LEVEL_DEBUG, "Examining performance by work completion");
		
		double stepPercent = 0.02;
		 
		ConfusionMatrix[] results = new ConfusionMatrix[ (int) (1.0/stepPercent)];
		
		int stepIndex = -1;
		for (double testPercent=stepPercent; testPercent<= 1.00; testPercent += stepPercent) {
			stepIndex++;
			this.analyzePatterns(testPercent);
			this.createWekaClassifier(includeContextSensitiveFactors);
			this.classifyAccessControlPatternWithWeka(includeContextSensitiveFactors);
			Logger.setCurrentLoggingLevel(Logger.LEVEL_INFO);
			this.extractAccessControl();
			//results[stepIndex] = this.evaluateAccessControl(false,testPercent);
			results[stepIndex] = this.evaluateAccessControl(false,0.0);
			Logger.setCurrentLoggingLevel(Logger.LEVEL_DEBUG);
		}
		
		System.out.println("Step\tTP\tTN\tFP\tFN\tPrecision\tRecall\tF1");
		stepIndex = -1;
		for (double testPercent=stepPercent; testPercent<= 1.00; testPercent += stepPercent) {
			stepIndex++;
			System.out.print(testPercent);System.out.print("\t");
			System.out.print(results[stepIndex].getTruePositive());System.out.print("\t");
			System.out.print(results[stepIndex].getTrueNegative());System.out.print("\t");
			System.out.print(results[stepIndex].getFalsePositive());System.out.print("\t");
			System.out.print(results[stepIndex].getFalseNegative());System.out.print("\t");
			System.out.print(results[stepIndex].getPrecision());System.out.print("\t");
			System.out.print(results[stepIndex].getRecall());System.out.print("\t");
			System.out.println(results[stepIndex].getF1Measure());
		}
		
		Logger.log(Logger.LEVEL_DEBUG, "performance by work completion complete ");
		Logger.setCurrentLoggingLevel(Logger.LEVEL_INFO);
	}
	
	
	public ConfusionMatrix evaluateAccessControl(boolean searchDocumentForFalsePositives, double percentileToStart) {
		GCController.getTheGCController().setStatusMessage("evaluate access control called");
		Logger.switchToLevel(Logger.LEVEL_DEBUG);
		
		NLDocument currentDoc = GCController.getTheGCController().getCurrentDocument();
		
		ConfusionMatrix overallResults = new ConfusionMatrix();
		
		List<Sentence> sentences =  currentDoc.getSentences();
		int startPosition = (int)  (sentences.size() * percentileToStart);
		startPosition = Math.min(startPosition, sentences.size());
		
		for (int i= startPosition; i< sentences.size(); i++) {
			Sentence s = sentences.get(i);
			overallResults.add(s.accessControlCompare(searchDocumentForFalsePositives, currentDoc));
		}
		

		Logger.log(Logger.LEVEL_INFO, "Access Control Evaluation:");
		Logger.log(Logger.LEVEL_INFO, "Active pattern count: "+ AccessControlRelationManager.getTheAccessControlRelationManager().getAccessControlPatterns().size());
		
		Logger.log(Logger.LEVEL_INFO, overallResults.toString());		
		
		Logger.log(Logger.LEVEL_INFO, "Precision: "+ overallResults.getPrecision());
		Logger.log(Logger.LEVEL_INFO, "Recall: "+ overallResults.getRecall());
		Logger.log(Logger.LEVEL_INFO, "True negative rate: "+overallResults.getTrueNegativeRate());
		Logger.log(Logger.LEVEL_INFO, "Accuracy: "+overallResults.getAccuracy());
		Logger.log(Logger.LEVEL_INFO, "F-Measure:" +overallResults.getF1Measure());
		
		Logger.restoreLoggingLevel();
		
		GCController.getTheGCController().setStatusMessage("Access control evaluation complete");
		
		return overallResults;
	}
	
	private void reportModalVerbAppearance() {
		NLDocument currentDocument = GCController.getTheGCController().getCurrentDocument();
		
		HashMap<String,Integer> modalVerbs = new HashMap<String,Integer>();

		for (Sentence s: currentDocument.getSentences()) {
			boolean hasAccessControl = s.hasBooleanClassification("access control:functional");
			
			for (int i=0; i< s.getNumberOfNodes(); i++) {
				WordVertex wv = s.getWordVertexAt(i);
				if (wv.getPartOfSpeech() == PartOfSpeech.MD) {
					String key = hasAccessControl +"\t" + wv.getLemma();
					int count = modalVerbs.get(key) == null ? 1 : modalVerbs.get(key)  +1;
					modalVerbs.put(key, count);
					break;
				}
			}			

		}

		for (String key:modalVerbs.keySet()) { System.out.println("modal_verb\t"+ modalVerbs.get(key)+"\t"+key);		}		
	}
	
	private void reportHold() {
		this.reportAccessControlPatternAndMetrics(false);		
	}
	
	public void report() {
		Object[] options =  {"Pattern Frequency", "Access Control Report",  "Ambiguous Terms",  "Xiao Pattern", "Sentence AC Report",
				             "AC Pattern Report","False Negative Report","False Positive Report","All Issues Report", "ACRM Report", "Frequencies","Multiple forms of AC","Modal Verb Appearances", "Readability" };
		String selectedReport = (String) JOptionPane.showInputDialog(null,	"Select Report:", "Input",	JOptionPane.INFORMATION_MESSAGE, null,	options, options[1]);

		if (selectedReport == null) { GCController.getTheGCController().setStatusMessage("reporting cancelled"); return; }
		
		switch (selectedReport) {
		case "Pattern Frequency":        this.reportPatternFrequency();						break;
		case "Access Control Report":    this.reportAccessControlPatternAndMetrics(true);	break;
		case "Ambiguous Terms":          this.reportAmbiguousTerms();						break;
		case "Xiao Pattern":             this.reportXiaoPatternMatch(true); 				break;
		case "Modal Verb Appearances":   this.reportModalVerbAppearance();					break;
		case "Frequencies":			     this.reportFrequencies();  						break;
		case "AC Pattern Report":        this.reportAccessControlPatterns(); 				break;
		case "Multiple forms of AC":	 this.reportMultipleAccess(); 						break;
		case "Sentence AC Report":		 this.reportSentenceAccessControlReport();			break;
		case "False Negative Report":    this.reportPatternsWithIssues(System.out, true, false, true, true); break;
		case "False Positive Report":    this.reportPatternsWithIssues(System.out, true, true, false, true); break;
		case "All Issues Report":		 this.reportPatternsWithIssues(System.out, true, true, true, true);  break;
		case "ACRM Report":              AccessControlRelationManager.getTheAccessControlRelationManager().printReport(System.out); break;
		case "Readability":				 this.reportSentenceReadabilityActual(); break;
		}

	}
	
	private void reportSentenceAccessControlReport() {
		List<Sentence> sentences = GCController.getTheGCController().getCurrentDocument().getSentences();
		for (Sentence s: sentences) {
			System.out.println(s.getOriginalSentencePosition()+":"+s);
			if (s.hasBooleanClassification("access control:functional")) {
				List<AccessControlRelation> acList = s.getAccessControlRelations();
				for (AccessControlRelation ac: acList) {	
					String permissions = ac.getPermissions();
					if (permissions == null) { permissions = "undefined"; }
					System.out.println("          "+ac.toString()+" - " + permissions);
				}
				/*
				System.out.println("Defined:");
				List<AccessControl> acList = s.getHoldAccessControlRelations();
				for (AccessControl ac: acList) {	
					System.out.println("          "+ac.toString());
				}
				System.out.println("Mistakes:");
				List<AccessControlPattern> falsePositives = this.findFalsePositives(s);
				for (AccessControlPattern patternFP: falsePositives) {	
					System.out.println("      FP  "+patternFP.getAccessControl().toString()+" --- "  + patternFP.toStringPattern());
				}
				*/
			}
		}	
	}

	
	private void reportSentenceReadabilityActual() {
		List<Sentence> sentences = GCController.getTheGCController().getCurrentDocument().getSentences();
		for (Sentence s: sentences) {
			if (s.hasBooleanClassification("access control:functional")) {
			System.out.println (Readability.getFleshKincaidGradeLevelBySentence(s)+" - "+s);
			}
		}
		System.out.println(Readability.getFleschReadingEaseTestScore(sentences,"access control:functional"));
		System.out.println(Readability.getFleshKincaidGradeLevel(sentences,"access control:functional"));		
	}	

	private void reportAccessControlPatterns() {
		AccessControlRelationManager acrm = AccessControlRelationManager.getTheAccessControlRelationManager();
		
		PrintStream ps = System.out;
		
		ps.println("=========================================================================");
		ps.println("Access Control Pattern Report");
		ps.println("-------------------------------------------------------------------------");
		ps.println("Unique subjects: " + acrm.getSubjects());
		ps.println("Unique actions: " + acrm.getActions());
		ps.println("Unique objects: " + acrm.getResources());
		ps.println("Number of patterns: "+acrm.getAccessControlPatterns().size());
		
		ps.println("NumberValid\tNumberInvalid\tNumberNotPresent\tSource\tClassification\tValid\tInvalid\tAccessControlPattern\tSubject\tAction\tObject\tsize");
		
		for (AccessControlPattern acp: acrm.getAccessControlPatterns()) {
			System.out.print(acp.getNumberOfValidSentences()+ "\t" + acp.getNumberOfInvalidSentences()+ "\t" + acp.getNumberOfNegativeSentences()+ "\t"+ acp.getAccessControlSource().name());
						
			//Classify the current pattern
			Object[] arguments = new Object[1];
			arguments[0] = acp.getFactors(_currentFactors);;
			Map<String, Double> result = _bayes.computeClassProbabilities(arguments);		//computeClassProbabilitiesByLogs //computeClassProbabilities
			
			double total = result.get("valid") + result.get("invalid");
			
			System.out.print("\t" + (result.get("valid") > result.get("invalid")));
			System.out.print("\t" + (result.get("valid")/total));
			System.out.print("\t" + (result.get("invalid")/total));	
			
			
			System.out.print("\t"+ acp.getRoot().getStringRepresentation());
			System.out.print("\t"+acp.getAccessControl().getSubjectAsVertexIDs());
			System.out.print("\t"+acp.getAccessControl().getActionAsVertexIDs());
			System.out.print("\t"+acp.getAccessControl().getObjectAsVertexIDs());
			System.out.println("\t"+acp.getRoot().getGraphSize());
		}
		//ps.println("=========================================================================");		
	}	
	
	
	private void reportXiaoPatternMatch(boolean includeAccessControl) {

		PrintStream ps = System.out;
		
		NLDocument currentDocument = GCController.getTheGCController().getCurrentDocument();
		
		//ps.println("ID\tSentence\tAccessControl\tXiaoACP\tModelVerb\tPassiveTo\tAccessExpression\tAbilityExpression\tAccessControl");
		ps.println("type\tnumNodessize\tNumberOfWords\tID\tSentence\tAccessControl\tXiaoACP\tModelVerb\tPassiveTo\tAccessExpression\tAbilityExpression\tAccessControl");

		for (Sentence s: currentDocument.getSentences()) {
			ps.print(s.getSentenceType()); ps.print('\t');
			ps.print(s.getNumberOfNodes()); ps.print('\t');
			ps.print(s.getSentence().split("[\\p{Punct}\\s]+").length);ps.print('\t');
			ps.print(s.getOriginalSentencePosition()); ps.print('\t');
			ps.print(s.getSentence());   ps.print('\t');
			ps.print(s.hasBooleanClassification("access control:functional"));
			if (s.hasBooleanClassification("access control:functional") == false) {
				ps.println();
				continue;
			}
			ps.print('\t');
			ps.print(hasXiaoPattern_ModalVerb(s)||hasXiaoPattern_PassiveInfinitive(s)||hasXiaoPattern_AccessExpression(s)||hasXiaoPattern_AbilityExpression(s)); ps.print('\t');   //yes, I'm calling this stuff twice..
			ps.print(hasXiaoPattern_ModalVerb(s));         ps.print('\t');
			ps.print(hasXiaoPattern_PassiveInfinitive(s)); ps.print('\t');
			ps.print(hasXiaoPattern_AccessExpression(s));  ps.print('\t');
			ps.print(hasXiaoPattern_AbilityExpression(s)); ps.print('\t'); 

			if (includeAccessControl) {
				List<AccessControlRelation> acList = s.getAccessControlRelations();
				for (AccessControlRelation ac: acList) {
					ps.print(ac.toString()); ps.print('\t');
				}
			}
			ps.println();
		}


	}
	

	
	private void reportAmbiguousTerms() {
		NLDocument currentDocument = GCController.getTheGCController().getCurrentDocument();
		
		HashMap<String,Integer> subjects = new HashMap<String,Integer>();
		HashMap<String,Integer> objects  = new HashMap<String,Integer>();
		
		//int noSubjectCount = 0;
		//int noObjectCount  = 0;

		for (Sentence s: currentDocument.getSentences()) {
			List<AccessControlRelation> acList = s.getAccessControlRelations();
			for (AccessControlRelation ac: acList) {
				String subject = ac.getSubject();
				boolean ambiguous = subject.length() == 0 || Ambiguity.isAmbiguous(subject) || 
						           (ac.getSubjectVertexList().size() == 1 && Ambiguity.isAmbiguous(ac.getSubjectVertexList().get(0).getPartOfSpeech()));
				if (ambiguous) {
					int count = subjects.get(subject) == null ? 1 : subjects.get(subject)  +1;
					subjects.put(subject, count);
					if (subject.length() == 0) {
						System.out.println(s.getSentence());
						System.out.println("\t"+ac);			
					}					
				}
				ambiguous = false;
				String object = ac.getObject();
				ambiguous = object.length() == 0 ||Ambiguity.isAmbiguous(object) || 
						           (ac.getObjectVertexList().size() == 1 && Ambiguity.isAmbiguous(ac.getObjectVertexList().get(0).getPartOfSpeech()));
				if (ambiguous) {
					//System.out.println(object+"\t"+object.length());
					int count = objects.get(object) == null ? 1 : objects.get(object)  +1;
					objects.put(object, count);
					//System.out.println(s.getSentence());
					//System.out.println("\t"+ac);
					if (object.length() == 0) {
						System.out.println(s.getSentence());
						System.out.println("\t"+ac);			
					}
				}
			
				
			}
		}

		for (String s:subjects.keySet()) { System.out.println("subject_ambiguous\t"+ subjects.get(s)+"\t"+s);		}
		for (String s:objects.keySet()) { System.out.println("object_ambiguous\t"+ objects.get(s)+"\t"+s);		}
		
	}
	
	/**
	 * reports if a sentence has multiple forms are access control to the object.
	 * This is defined by multiple relationships present, not counting conjunctions
	 */
	private void reportMultipleAccess() {
		NLDocument currentDocument = GCController.getTheGCController().getCurrentDocument();
		
		int countSentenceFound = 0;
		for (Sentence s: currentDocument.getSentences()) {
			if (s.hasBooleanClassification("access control:functional") == false || s.getAccessControlRelations().size() < 2) { // don't care if there's none or just one
				continue;
			}
			
			HashSet<Relationship> subjectRelations = new HashSet<Relationship>();
			for (AccessControlRelation ac: s.getAccessControlRelations()) {
				for (WordVertex wv: ac.getSubjectVertexList()) {
					for (int parentIndex = 0; parentIndex < wv.getNumberOfParents(); parentIndex++ ) {
						Relationship parent = wv.getParentAt(parentIndex).getRelationship();
						if (!parent.isConjunction()) { subjectRelations.add(parent); }
					}
				}
			}
			
			HashSet<Relationship> objectRelations = new HashSet<Relationship>();
			for (AccessControlRelation ac: s.getAccessControlRelations()) {
				for (WordVertex wv: ac.getObjectVertexList()) {
					for (int parentIndex = 0; parentIndex < wv.getNumberOfParents(); parentIndex++ ) {
						Relationship parent = wv.getParentAt(parentIndex).getRelationship();
						if (!parent.isConjunction()) { objectRelations.add(parent); }
					}
				}
			}
			if (subjectRelations.size() > 1 || objectRelations.size() > 1) { // multiple subject/object types present
				countSentenceFound++;
				System.out.println(s.getOriginalSentencePosition()+": " + s.getSentence());
				for (AccessControlRelation ac: s.getAccessControlRelations()) {
					System.out.println("    "+ac);
				}
			}
		}
		System.out.println("Number of sentences with multiple access control types: "+ countSentenceFound );
		
	}
	
	
	private void reportAccessControlPatternAndMetrics(boolean useActive) {
		System.out.print("***** Access Control Pattern Report - ");
		if (useActive) { System.out.print("Active"); }
		else           { System.out.print("Hold");   }
		System.out.println(" *****");
		 
		NLDocument currentDocument = GCController.getTheGCController().getCurrentDocument();
		
		//currentDocument.printUniqueNouns(); 
		
		HashMap<String,Integer> subjects = new HashMap<String,Integer>();
		HashMap<String,Integer> actions  = new HashMap<String,Integer>();
		HashMap<String,Integer> objects  = new HashMap<String,Integer>();

		HashMap<Relationship,Integer> subjectRelations = new HashMap<Relationship,Integer>();
		HashMap<Relationship,Integer> actionRelations  = new HashMap<Relationship,Integer>();
		HashMap<Relationship,Integer> objectRelations  = new HashMap<Relationship,Integer>();
		
		HashMap<PartOfSpeech,Integer> subjectPOS = new HashMap<PartOfSpeech,Integer>();
		HashMap<PartOfSpeech,Integer> actionPOS  = new HashMap<PartOfSpeech,Integer>();
		HashMap<PartOfSpeech,Integer> objectPOS  = new HashMap<PartOfSpeech,Integer>();		
		
		
		boolean ignoreConjunctionsInRelationships = true; 
		
		for (Sentence s: currentDocument.getSentences()) {
			List<AccessControlRelation> acList;
			
			if (useActive) { acList = s.getAccessControlRelations(); }
			else {           acList = s.getHoldAccessControlRelations(); }
			if (acList == null) {continue;}
			
			for (AccessControlRelation ac: acList) {
				int count = subjects.get(ac.getSubject()) == null ? 1 : subjects.get(ac.getSubject())  +1;
				subjects.put(ac.getSubject(), count);
				
				count = actions.get(ac.getAction()) == null ? 1 : actions.get(ac.getAction())  +1;
				actions.put(ac.getAction(), count);

				count = objects.get(ac.getObject()) == null ? 1 : objects.get(ac.getObject())  +1;
				objects.put(ac.getObject(), count);
				
				for (WordVertex wv: ac.getSubjectVertexList()) {
					for (int parentIndex = 0; parentIndex < wv.getNumberOfParents(); parentIndex++ ) {
						Relationship parent = wv.getParentAt(parentIndex).getRelationship();
						if (ignoreConjunctionsInRelationships && parent.isConjunction()) { continue; }
						count = subjectRelations.get(parent)  == null ? 1 : subjectRelations.get(parent)  +1;
						subjectRelations.put(parent, count);
					}
					count = subjectPOS.get(wv.getPartOfSpeech())  == null ? 1 : subjectPOS.get(wv.getPartOfSpeech())  +1;
					subjectPOS.put(wv.getPartOfSpeech(), count);
				}

				for (WordVertex wv: ac.getActionVertexList()) {
					for (int parentIndex = 0; parentIndex < wv.getNumberOfParents(); parentIndex++ ) {
						Relationship parent = wv.getParentAt(parentIndex).getRelationship();
						if (ignoreConjunctionsInRelationships && parent.isConjunction()) { continue; }
						count = actionRelations.get(parent)  == null ? 1 : actionRelations.get(parent)  +1;
						actionRelations.put(parent, count);
					}
					count = actionPOS.get(wv.getPartOfSpeech())  == null ? 1 : actionPOS.get(wv.getPartOfSpeech())  +1;
					actionPOS.put(wv.getPartOfSpeech(), count);
				}

				for (WordVertex wv: ac.getObjectVertexList()) {
					for (int parentIndex = 0; parentIndex < wv.getNumberOfParents(); parentIndex++ ) {
						Relationship parent = wv.getParentAt(parentIndex).getRelationship();
						if (ignoreConjunctionsInRelationships && parent.isConjunction()) { continue; }
						count = objectRelations.get(parent)  == null ? 1 : objectRelations.get(parent)  +1;
						objectRelations.put(parent, count);
					}
					count = objectPOS.get(wv.getPartOfSpeech())  == null ? 1 : objectPOS.get(wv.getPartOfSpeech())  +1;
					objectPOS.put(wv.getPartOfSpeech(), count);
				}				
				
			}
		}

		for (String s:subjects.keySet()) { System.out.println("subject\t"+ subjects.get(s)+"\t"+s);		}
		for (String s:actions.keySet()) { System.out.println("action\t"+ actions.get(s)+"\t"+s);		}
		for (String s:objects.keySet()) { System.out.println("object\t"+ objects.get(s)+"\t"+s);		}
		
		for (Relationship r:subjectRelations.keySet()) { System.out.println("subjectRelationship\t"+ subjectRelations.get(r)+"\t"+r);		}
		for (Relationship r:actionRelations.keySet()) { System.out.println("actionRelationship\t"+ actionRelations.get(r)+"\t"+r);		}
		for (Relationship r:objectRelations.keySet()) { System.out.println("objectRelationship\t"+ objectRelations.get(r)+"\t"+r);		}

		for (PartOfSpeech p:subjectPOS.keySet()) { System.out.println("subjectPartOfSpeech\t"+ subjectPOS.get(p)+"\t"+p);		}
		for (PartOfSpeech p:actionPOS.keySet()) { System.out.println("actionPartOfSpeech\t"+ actionPOS.get(p)+"\t"+p);		}
		for (PartOfSpeech p:objectPOS.keySet()) { System.out.println("objectPartOfSpeech\t"+ objectPOS.get(p)+"\t"+p);		}		
		
		
		int sentenceCountForAccessControl = 0;
		int accessControlCountFound       = 0;
		for (Sentence s: currentDocument.getSentences()) {
			List<AccessControlRelation> acList;
			
			if (useActive) { acList = s.getAccessControlRelations(); }
			else {           acList = s.getHoldAccessControlRelations(); }
			if (acList.size() >0 ) {  sentenceCountForAccessControl++; }
			for (AccessControlRelation ac: acList) {	
				accessControlCountFound++;
				System.out.println(ac.toString());
			}
		}
		System.out.println("Number of sentences with access control: "+sentenceCountForAccessControl);
		System.out.println("Total number of access control statements: "+accessControlCountFound);		
		
	}
	
	/**
	 * prints out how often specific patterns appear for access control
	 * also generates a frequency based upon the size
	 * 
	 */
	private void reportPatternFrequency() {
		HashMap<Integer,Integer> sizeCounts      = new HashMap<Integer, Integer>();
		HashMap<Integer,Integer> patternDistance = new HashMap<Integer, Integer>();
		HashMap<String,Integer>  patternCounts   = new HashMap<String, Integer>(); 
		
		for (Sentence s: GCController.getTheGCController().getCurrentDocument().getSentences()) {
			for (AccessControlRelation ac: s.getAccessControlRelations()) {	
				WordVertex wvp = WordVertex.extractPattern(ac.getAllVertices());
				AccessControlPattern newPattern = new AccessControlPattern(wvp, new AccessControlRelation(ac,wvp), ac.getSource());

				Integer patternSize = newPattern.getRoot().getGraphSize();
				String pattern = newPattern.toStringPattern();
			
				int sizeCount = sizeCounts.get(patternSize) == null ? 1 : sizeCounts.get(patternSize)  +1;
				sizeCounts.put(patternSize, sizeCount);
				
				int patternCount = patternCounts.get(pattern) == null ? 1 : patternCounts.get(pattern)  +1;
				patternCounts.put(pattern, patternCount);
				
				int distance = newPattern.getRoot().getGreatestWordPosition() - newPattern.getRoot().getSmallestWordPosition();
				int distanceCount = patternDistance.get(distance) == null ? 1 : patternDistance.get(distance)  +1;
				patternDistance.put(distance, distanceCount);
				System.out.println(s.getRoot().getGreatestWordPosition() +"\t"+ patternSize);
			}
		}		
	
		for (Integer size:sizeCounts.keySet()) { System.out.println("pattern_size\t"+ sizeCounts.get(size)+"\t"+size);		}
		for (Integer distance:patternDistance.keySet()) { System.out.println("pattern_distance\t"+ patternDistance.get(distance)+"\t"+distance);		}
		for (String pattern:patternCounts.keySet()) { System.out.println("object\t"+ patternCounts.get(pattern)+"\t"+pattern);		}
	}
	
	/**
	 * prints out how often specific patterns appear for access control
	 * also generates a frequency based upon the size
	 * 
	 */
	private void reportFrequencies() {		
		System.out.println("attribute\tsize");
		for (Sentence s: GCController.getTheGCController().getCurrentDocument().getSentences()) {
			for (AccessControlRelation ac: s.getAccessControlRelations()) {	
				WordVertex wvp = WordVertex.extractPattern(ac.getAllVertices());
				AccessControlPattern newPattern = new AccessControlPattern(wvp, new AccessControlRelation(ac,wvp), ac.getSource());
				
				System.out.println("Pattern Size\t"+newPattern.getRoot().getGraphSize());
				
				int distance = newPattern.getRoot().getGreatestWordPosition() - newPattern.getRoot().getSmallestWordPosition();
				System.out.println("Pattern Distance\t"+distance);
				
			}
			if (s.getAccessControlRelations().size() >0 ) {
				System.out.println("Parse Tree Size\t"+s.getNumberOfNodes());
			}
		}		
	}	
	public void printMessage(String s) {
		if (_verbose) {
			System.out.println(s);
			
		}
	}
	
	/**
	 * Does the sentence contain a modal verb?
	 * @param s
	 * @return
	 */
	public boolean hasXiaoPattern_ModalVerb(Sentence s) {
		for (int i=0; i< s.getNumberOfNodes(); i++) {
			WordVertex wv = s.getWordVertexAt(i);
			if (wv.getPartOfSpeech() == PartOfSpeech.MD) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Is the sentence in passive voice, with a "TO" infinitive
	 * @param s
	 * @return
	 */
	public boolean hasXiaoPattern_PassiveInfinitive(Sentence s) {
		boolean passiveFlag = Voice.inPassiveVoice(s);
		if (passiveFlag) {
			// Now, check for "to"
			for (int i=0; i< s.getNumberOfNodes(); i++) {
				WordVertex wv = s.getWordVertexAt(i);
				if (wv.getPartOfSpeech() == PartOfSpeech.TO) {
					return true;
				}
			}			
		}
		return false;
	}	
	
	/**
	 * Does the sentence contain the word "access"  (can be part of a word)
	 * 
	 * @param s
	 * @return
	 */
	public boolean hasXiaoPattern_AccessExpression(Sentence s) {
		for (int i=0; i< s.getNumberOfNodes(); i++) {
			WordVertex wv = s.getWordVertexAt(i);
			if (wv.getLemma().contains("access")) {
				return true;
			}
		}		
		return false;
	}
	
	/**
	 * Does is contain the word "able" or the full word "ability"?
	 * 
	 * @param s
	 * @return
	 */
	public boolean hasXiaoPattern_AbilityExpression(Sentence s) {
		for (int i=0; i< s.getNumberOfNodes(); i++) {
			WordVertex wv = s.getWordVertexAt(i);
			if (wv.getLemma().equals("able") || wv.getLemma().endsWith("able") || wv.getLemma().equals("ability")) {
				return true;
			}
		}				
		return false;
	}
	
	/**
	 * For the past in sentence,
	 *   If access control elements have been extracted, are any of those false positives given the "truth" contained in the
	 *   "hold" elements
	 *   
	 * @param s
	 * @return list of all access control patterns that are false positives
	 * 
	 */
	public List<AccessControlPattern> findFalsePositives(Sentence s) {
		ArrayList<AccessControlPattern> result = new ArrayList<AccessControlPattern>();
		for (AccessControlRelation ac: s.getAccessControlRelations()) {	
			boolean foundAC = false;
			for (AccessControlRelation existingAC: s.getHoldAccessControlRelations()) {
				if (existingAC.contains(ac, false,false) || ac.contains(existingAC, false,false)) { foundAC= true; }
			}
			if (foundAC) { continue; }
	
			
			WordVertex wvp = WordVertex.extractPattern(ac.getAllVertices());
			result.add(new AccessControlPattern(wvp, new AccessControlRelation(ac,wvp), ac.getSource()));
		}
		return result;
	}
	
	public List<AccessControlPattern> findFalseNegatives(Sentence s) {
		ArrayList<AccessControlPattern> result = new ArrayList<AccessControlPattern>();

		for (AccessControlRelation existingAC:  s.getHoldAccessControlRelations()) {
			boolean foundCombinable = false;
			for (AccessControlRelation ac: s.getAccessControlRelations()) {
				if (existingAC.contains(ac, false, false) || ac.contains(existingAC, false,false) ) { foundCombinable= true; }
			}
			if (foundCombinable) { continue; }

			WordVertex wvp = WordVertex.extractPattern(existingAC.getAllVertices());
			result.add(new AccessControlPattern(wvp, new AccessControlRelation(existingAC,wvp), existingAC.getSource()));
		}
		
		return result;
	}	
	
	/**
	 * Used to list the patterns that are process can't currently find - the false negatives.
	 * Logic is pretty much the same as that needed for Evaluate Access Control
	 * 
	 * Generates two reports:
	 * 1. For each sentence, the access control tuple and associated pattern not found.
	 * 2. A frequency report (similar to that in reportPatternFrequency), but for the undiscovered patterns
	 * 
	 */
	private void reportPatternsWithIssues(PrintStream ps, boolean reportSentences, boolean reportFalsePositives, boolean reportFalseNegatives, boolean reportSizes) {
				
		HashMap<String,Integer> sizeCounts   = new HashMap<String, Integer>();
		HashMap<String,Integer> patternCounts = new HashMap<String, Integer>(); 
		
		ps.println("Type\tAC Pattern\tAC Tuple\tSentenceID\tSentence");
		for (Sentence s: GCController.getTheGCController().getCurrentDocument().getSentences()) {
			List<AccessControlPattern> falsePositives = this.findFalsePositives(s);
			for (AccessControlPattern patternFP: falsePositives) {
				String pattern = patternFP.toStringPattern();
				pattern = "FP\t"+pattern;

				String patternSize = "FP\t"+patternFP.getRoot().getGraphSize();
				
				int sizeCount = sizeCounts.get(patternSize) == null ? 1 : sizeCounts.get(patternSize)  +1;
				sizeCounts.put(patternSize, sizeCount);
				
				int patternCount = patternCounts.get(pattern) == null ? 1 : patternCounts.get(pattern)  +1;
				patternCounts.put(pattern, patternCount);
				
				if (reportSentences && reportFalsePositives) {
					ps.print(pattern); // this has the type and pattern
					ps.print("\t");
					ps.print(patternFP.getAccessControl());
					ps.print("\t");
					ps.print(s.getOriginalSentencePosition()); ps.print('\t');
					ps.println(s.getSentence());
				}
			}
			
			List<AccessControlPattern> falseNegatives= this.findFalseNegatives(s);
			for (AccessControlPattern patternFN: falseNegatives) {
				String pattern = patternFN.toStringPattern();
				pattern = "FN\t"+pattern;

				String patternSize = "FN\t"+patternFN.getRoot().getGraphSize();
				
				int sizeCount = sizeCounts.get(patternSize) == null ? 1 : sizeCounts.get(patternSize)  +1;
				sizeCounts.put(patternSize, sizeCount);
				
				int patternCount = patternCounts.get(pattern) == null ? 1 : patternCounts.get(pattern)  +1;
				patternCounts.put(pattern, patternCount);
				
				if (reportSentences && reportFalseNegatives) {
					ps.print(pattern); // this has the type and pattern
					ps.print("\t");
					ps.print(patternFN.getAccessControl());
					ps.print("\t");
					ps.print(s.getOriginalSentencePosition()); ps.print('\t');
					ps.println(s.getSentence());
				}				
			}			
		}		
	
		if (reportSizes) {
			for (String size:sizeCounts.keySet()) { 
				if (reportFalseNegatives && size.startsWith("FN") ||
					reportFalsePositives && size.startsWith("FP")) {
					System.out.println("problem_pattern_size\t"+ sizeCounts.get(size)+"\t"+size);
				}
			}
		}
		for (String pattern:patternCounts.keySet()) {
			if (reportFalseNegatives && pattern.startsWith("FN") ||
				reportFalsePositives && pattern.startsWith("FP")) {
				ps.println("pattern\t"+ patternCounts.get(pattern)+"\t"+pattern);		
			}
		}
		
		
	}

	
}
