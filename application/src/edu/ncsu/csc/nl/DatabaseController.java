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
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import weka.core.Instances;
import edu.ncsu.csc.nl.model.NLDocument;
import edu.ncsu.csc.nl.model.Sentence;
import edu.ncsu.csc.nl.model.WekaCreator;
import edu.ncsu.csc.nl.model.WordVertex;
import edu.ncsu.csc.nl.model.english.Readability;
import edu.ncsu.csc.nl.model.ml.ClassificationResult;
import edu.ncsu.csc.nl.model.ml.ConfusionMatrix;
import edu.ncsu.csc.nl.model.ml.WekaInterfacer;
import edu.ncsu.csc.nl.model.naivebayes.NaiveBayesClassifier;
import edu.ncsu.csc.nl.model.relation.DatabaseFactor;
import edu.ncsu.csc.nl.model.relation.DatabasePattern;
import edu.ncsu.csc.nl.model.relation.DatabaseRelation;
import edu.ncsu.csc.nl.model.relation.DatabaseRelationManager;
import edu.ncsu.csc.nl.model.type.DatabaseRelationType;
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
public class DatabaseController implements ActionListener {
	
	private static final DatabaseController _theDatabaseController = new DatabaseController(); 

	private NaiveBayesClassifier _bayes    = null;
	private WekaInterfacer _wekaClassifier = null; 
	private File _currentFileLocation = null;   //used with the save dialog for the bayes classifier

	ArrayList<DatabaseFactor> _factors_TR = new ArrayList<DatabaseFactor>();
	{
		_factors_TR.add(DatabaseFactor.FACTOR_TREE_RELATIONSHIP);
	}

	/*
	ArrayList<DatabaseFactor> _factors_TR_VOSA_RP = new ArrayList<DatabaseFactor>();
	{
		_factors_TR_VOSA_RP.add(DatabaseFactor.FACTOR_TREE_RELATIONSHIP);
		_factors_TR_VOSA_RP.add(DatabaseFactor.FACTOR_VOICE_ORDER_SUBJECT_ACTION);
		_factors_TR_VOSA_RP.add(DatabaseFactor.FACTOR_ROOT_POS);

	}	
	
	ArrayList<DatabaseFactor> _factors_TR_VOSA_OP = new ArrayList<DatabaseFactor>();
	{
		_factors_TR_VOSA_OP.add(DatabaseFactor.FACTOR_TREE_RELATIONSHIP);
		_factors_TR_VOSA_OP.add(DatabaseFactor.FACTOR_VOICE_ORDER_SUBJECT_ACTION);
		_factors_TR_VOSA_OP.add(DatabaseFactor.FACTOR_OBJECT_POS);
	}	
	
	ArrayList<DatabaseFactor> _factors_TR_VOSA_SO = new ArrayList<DatabaseFactor>();
	{
		_factors_TR_VOSA_SO.add(DatabaseFactor.FACTOR_TREE_RELATIONSHIP);
		_factors_TR_VOSA_SO.add(DatabaseFactor.FACTOR_VOICE_ORDER_SUBJECT_OBJECT);
		_factors_TR_VOSA_SO.add(DatabaseFactor.FACTOR_OBJECT_POS);
	}
*/
	
	
	ArrayList<DatabaseFactor> _currentFactors = _factors_TR;

	private boolean _verbose = true;
	
	private DatabaseController() { }

	
	public static DatabaseController getTheDatabaseController()  {
		return _theDatabaseController;
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
		GCController.getTheGCController().setStatusMessage("");
		
		_currentFactors.clear();
		{
			/*
			_currentFactors.add(DatabaseFactor.FACTOR_TREE_RELATIONSHIP);
			_currentFactors.add(DatabaseFactor.FACTOR_VOICE_ORDER_ACTION_OBJECT);
			_currentFactors.add(DatabaseFactor.FACTOR_PATTERN_SIZE);
			_currentFactors.add(DatabaseFactor.FACTOR_SUBJECT_VALUE);
			_currentFactors.add(DatabaseFactor.FACTOR_OBJECT_VALUE);
			*/
			_currentFactors.add(DatabaseFactor.FACTOR_TREE_RELATIONSHIP);
			/*
			_currentFactors.add(DatabaseFactor.FACTOR_OBJECT_RELATIONSHIP);
			_currentFactors.add(DatabaseFactor.FACTOR_SUBJECT_RELATIONSHIP);
			_currentFactors.add(DatabaseFactor.FACTOR_SUBJECT_POS);
			*/
			//FACTOR_TREE_RELATIONSHIP, FACTOR_VOICE_ORDER_ACTION_OBJECT, FACTOR_PATTERN_SIZE, FACTOR_SUBJECT_VALUE, FACTOR_OBJECT_VALUE			
			
		}		 
		//this.examinePerformanceByWorkCompletion(false);
		
		switch (ae.getActionCommand()) {	    
		    case GCConstants.ACTION_DBEXTRACT_RESET_DBRM:           resetDBRM(true);           return;
			case GCConstants.ACTION_DBEXTRACT_BOOTSTRAP:		    bootstrap();               return;
			case GCConstants.ACTION_DBEXTRACT_SEARCH_FOR_PATTERNS:  searchForPatterns();       return;
			case GCConstants.ACTION_DBEXTRACT_TRANSFORM_PATTERNS:   transformPatterns();       return;
			case GCConstants.ACTION_DBEXTRACT_EXPAND_PATTERNS:      expandPatterns();          return;
			case GCConstants.ACTION_DBEXTRACT_EXTRACT_DATABASE_OBJ: extractDatabaseObjects();    return;
			case GCConstants.ACTION_DBEXTRACT_CYCLE_PROCESS:        cycleProcess(); cycleProcess();cycleProcess();           return;
			case GCConstants.ACTION_DBEXTRACT_MOVE_DB_TO_HOLD:      moveDatabaseToHold(); return;
			case GCConstants.ACTION_DBEXTRACT_ADD_HOLD_TO_ACTIVE:   addHoldToDatabaseRelations();  return;
			case GCConstants.ACTION_DBEXTRACT_RESTORE_DB_RELATIONS: restoreDatabaseRelations();    return;
			case GCConstants.ACTION_DBEXTRACT_ANALYZE_PATTERNS:     analyzePatterns(1.0);               return;
			case GCConstants.ACTION_DBEXTRACT_CREATE_BAYES:         createBayesClassifier(_currentFactors,true,1.0); return;
			case GCConstants.ACTION_DBEXTRACT_CLASSIFY_DBP:         classifyDatabasePatterns(_currentFactors);  return;
			case GCConstants.ACTION_DBEXTRACT_CREATE_DBP_WEKA:      analyzePatterns(1.0);createWekaClassifier(false);	 return;
			case GCConstants.ACTION_DBEXTRACT_LOAD_DBP_WEKA:        loadWekaClassifier();  	 return;
			case GCConstants.ACTION_DBEXTRACT_SAVE_DBP_WEKA:        saveWekaClassifier();	 return;
			case GCConstants.ACTION_DBEXTRACT_CLASSIFY_DBP_WEKA:    classifyDatabasePatternWithWeka(false); 	 return;
			case GCConstants.ACTION_DBEXTRACT_EVALUATE_DB:          evaluateDatabase(false,0.0);            	 return;
			case GCConstants.ACTION_DBEXTRACT_EVAL_DOC_COMPLETION:  examinePerformanceByWorkCompletion(false);		 return;
			case GCConstants.ACTION_DBEXTRACT_REPORT_DB_PATTERNS:   report();                                		 return;
			case GCConstants.ACTION_DBEXTRACT_REPORT_ON_HOLD:       reportHold();                               	 return;
	
			case GCConstants.ACTION_DBEXTRACT_LOAD_NB_CLASSIFIER:   loadClassifier();              return;
			case GCConstants.ACTION_DBEXTRACT_SAVE_NB_CLASSIFIER:   saveClassifier();              return;
			case GCConstants.ACTION_DBEXTRACT_ADD_TO_NB_CLASSIFIER: addToClassifier(_currentFactors); return;
			case GCConstants.ACTION_DBEXTRACT_ANALYZE_FACTORS:      analyzeFactors(false);         return;
			case GCConstants.ACTION_DBEXTRACT_REPORT_UNDISCOVERED_PATTERNS: reportPatternsWithIssues(System.out,true,true,true,false); return;
			case GCConstants.ACTION_DBEXTRACT_INJECT_PATTERN:       injectPatterns(); return;
		}
		
	}
	
	/**
	 * Resets the Databaes Manager.  This "wipes opt" the patterns, and discovered entities, relationships and their associated .
	 */
	public void resetDBRM(boolean resetUniqueLists) {
		//resetUniqueLists = false;

		DatabaseRelationManager.getTheDatabaseRelationManager().reset(resetUniqueLists);
		
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
		
		DatabaseRelationManager.getTheDatabaseRelationManager().bootstrap();
		
		GCController.getTheGCController().setStatusMessage("Bootstrap complete");
	}
	
	public void searchForPatterns() {
		DatabaseRelationManager.getTheDatabaseRelationManager().searchForPatternsFromKnownObjects(GCController.getTheGCController().getCurrentDocument().getSentences(),6);

		GCController.getTheGCController().setStatusMessage("Search for patterns called");
	}
	
	public void transformPatterns() {
		DatabaseRelationManager.getTheDatabaseRelationManager().transformPatterns();
		
		GCController.getTheGCController().setStatusMessage("Transform patterns called");
	}	
	
	public void expandPatterns() {
		// TODO: this needs be altered to support the database relations ....
		
		DatabaseRelationManager.getTheDatabaseRelationManager().expandPatternSet(true, false, false, false,false);
		DatabaseRelationManager.getTheDatabaseRelationManager().expandPatternSet(false, true, false, false,false);
		DatabaseRelationManager.getTheDatabaseRelationManager().expandPatternSet(false, false, true, false,false);
		DatabaseRelationManager.getTheDatabaseRelationManager().expandPatternSet(false, false, false, true,false);
		//DatabaseRelationManager.getTheDatabaseRelationManager().expandPatternSet(false, false, true, true,false);

		//DatabaseRelationManager.getTheDatabaseRelationManager().expandPatternSet(false, true, true, true);
		//DatabaseRelationManager.getTheDatabaseRelationManager().expandPatternSet(true, true, true, true);

		
		GCController.getTheGCController().setStatusMessage("Expand patterns called");
	}		
	
	public void extractDatabaseObjects() {
		GCController.getTheGCController().setStatusMessage("Extract database objects called");
		
		for (Sentence s: GCController.getTheGCController().getCurrentDocument().getSentences()) {
			s.setDatabaseRelations(new ArrayList<DatabaseRelation>());
			s.setDatabaseComplete(false,false);
			s.processSentenceForDatabaseElements();
			DatabaseRelationManager.getTheDatabaseRelationManager().extractDatabasePatternsFromDefinedDatabaseTuples(s);
		}
	}	
	
	/**
	 * Calls search, expand, and transform,  followed extract access control
	 */
	public void cycleProcess() {
		Logger.log(Logger.LEVEL_INFO, "CycleProcess - search");
		this.searchForPatterns();
		
		Logger.log(Logger.LEVEL_INFO, "CycleProcess - expand");
		//this.expandPatterns();
		
		Logger.log(Logger.LEVEL_INFO, "CycleProcess - transform");
		//this.transformPatterns();
		
		Logger.log(Logger.LEVEL_INFO, "CycleProcess - update classifier");
		//this.addToClassifier(_currentFactors);
		
		Logger.log(Logger.LEVEL_INFO, "CycleProcess - classify patterns");
		//this.classifyDatabasePatterns(_currentFactors);
		
		Logger.log(Logger.LEVEL_INFO, "CycleProcess - extract database objects");		
		this.extractDatabaseObjects();
		
		Logger.log(Logger.LEVEL_INFO, "CycleProcess - evaluate database");		
		this.evaluateDatabase(false,0.0);
	}
	
	public void injectPatterns() {
		//Logger.setCurrentLoggingLevel(Logger.LEVEL_TRACE);
		Logger.log(Logger.LEVEL_TRACE,"ACC: Starting Inject Action Object Patterns");

		//TODO: need to call inject patterns over in the database relationship manager.
		
		//Logger.setCurrentLoggingLevel(Logger.LEVEL_INFO);
	}
	
	
	
	public void moveDatabaseToHold() {
		GCController.getTheGCController().getCurrentDocument().moveDatabaseRelationsToHold();
		GCController.getTheGCController().setStatusMessage("Database moved to hold for all elements");
	}
	
	public void addHoldToDatabaseRelations() {
		GCController.getTheGCController().getCurrentDocument().addHoldToDatabaseRelations();
		GCController.getTheGCController().setStatusMessage("Added Database in the hold to the active.");
	}
	
	public void restoreDatabaseRelations() {
		//DatabaseRelationManager.getTheDatabaseRelationManager().reset();
		GCController.getTheGCController().getCurrentDocument().moveHoldToDatabaseRelations();
		GCController.getTheGCController().setStatusMessage("Database re-established from hold elements");
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
		
		DatabaseRelationManager dbrm = DatabaseRelationManager.getTheDatabaseRelationManager();
		
		List<Sentence> sentences = currentDocument.getSentences();
		int maxSentenceNumberToUse = (int)  (sentences.size() * percentOfDocumentToUse);
		for (DatabasePattern dp: DatabaseRelationManager.getTheDatabaseRelationManager().getDatabasePatterns()) {
			dp.resetSentenceOccurances();
			
			for (int i=0; i < maxSentenceNumberToUse; i++) {
				Sentence s = sentences.get(i);
				if (s.hasBooleanClassification("database design") == false) { continue; } // only analyze sentences that have been classified for database
				if (s.isDatabaseComplete() == false ) { continue; } // only analyze sentences that have been marked complete
				
				List<DatabaseRelation> currentList = s.getDatabaseRelations();
				List<DatabaseRelation> holdList = s.getHoldDatabaseRelations();
				s.setDatabaseRelations(holdList);
				
				ArrayList<DatabaseRelation> tempResults = dbrm.matchDatabasePatternInSentence(s, dp);
				
				if (tempResults.size() == 0) {
					dp.addNegativeSentence(s);
				}
				else {
					for (DatabaseRelation tempDB: tempResults) {
						/*
						 * Temporary code to find why something was not showing...
						if (tempAC.toString().equals(";sort;list") && s.toString().startsWith("The patient views the list sorted by the role of the accessor relative to the patient")) {
							System.out.println("Heere");
						}
						*/
						if ( s.containsDatabase(tempDB)) {
							dp.addValidSentence(s);
						}
						else {
							dp.addInvalidSentence(s);
							Logger.log(Logger.LEVEL_TRACE, "Invalid database: "+tempDB+" ---- "+s);
						}
					}
				}
				s.setDatabaseRelations(currentList);
			}
		}
		GCController.getTheGCController().setStatusMessage("DB Patterns analyzed");
	}
	
	private void analyzeFactors(boolean includeContextSensitiveFactors) {
		//System.out.println(DatabaseFactor.values().length);
		
		// Use this call to force all parameters to be evaluated.  Otherwise the process stops on the first
		// attribute that decreases performance.
		boolean useCompleteSelection = true;
		
		
		this.analyzePatterns(1.0);
		
		NaiveBayesClassifier _holdClassifier = _bayes;
		
		ArrayList<DatabaseFactor> factors = new ArrayList<DatabaseFactor>();
		
		int numCycles = DatabaseFactor.values().length;
		if (includeContextSensitiveFactors == false) {
			numCycles = DatabaseFactor.getNumberOfContextInsensitiveFactors();
		}
		
		double topOverallF1Value             = Double.MIN_VALUE;
		ArrayList<DatabaseFactor> topFactors = new ArrayList<DatabaseFactor>();
		
		for (int cycleNum = 0; cycleNum < numCycles; cycleNum++) {			
			DatabaseFactor topFactor = null;
			double topF1Value             = Double.MIN_VALUE;
			
			for (DatabaseFactor acf: DatabaseFactor.values()) {
				if (includeContextSensitiveFactors == false && acf.isContextSpecific()) { continue;}  // need to skip these
				if (factors.contains(acf)) { continue; }
				factors.add(acf);
			
				System.out.println(acf);
				this.createBayesClassifier(factors,false,1.0);
				this.classifyDatabasePatterns(factors);
				this.extractDatabaseObjects();
				ConfusionMatrix results = this.evaluateDatabase(false,0.0);
				
				if (results.getF1Measure() > topF1Value) {
					topFactor = acf;
					topF1Value = results.getF1Measure();
				}
				
				factors.remove(acf);
			}
			
			if (topF1Value > topOverallF1Value) {
				factors.add(topFactor);
				topFactors = (ArrayList<DatabaseFactor>) factors.clone();
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
	
	private void createBayesClassifier(ArrayList<DatabaseFactor> factorsForClassification, boolean analyzePatterns, double percentOfDocumentToUse) {
		if (analyzePatterns) {this.analyzePatterns(percentOfDocumentToUse); }
		
		_bayes = new NaiveBayesClassifier();

		_bayes.addClass("valid");
		_bayes.addClass("invalid");
		_bayes.addLikelyhood("text", "words", new String[0]);
		
		
		List<DatabasePattern> patternList = DatabaseRelationManager.getTheDatabaseRelationManager().getDatabasePatterns();
		
		for (DatabasePattern db: patternList) {
			String[] factors = db.getFactors(factorsForClassification);
						
			for (int i=0;i<db.getNumberOfValidSentences(); i++) {
				_bayes.incrementClass("valid");
				for (String factor: factors) {
					_bayes.incrementClassLikelyhood("valid","text", factor);
				}
				
			}
			
			
			for (int i=0;i<db.getNumberOfInvalidSentences(); i++) {
				_bayes.incrementClass("invalid");
				for (String factor: factors) {
					_bayes.incrementClassLikelyhood("invalid","text", factor);
				}				

			}
		}		
		
		GCController.getTheGCController().setStatusMessage("Bayes Classifier Created for database objects");
		
	}
	
	public void addToClassifier(ArrayList<DatabaseFactor> factorsToUse) {

		// check created first
		if (_bayes == null) {
			GCController.getTheGCController().setStatusMessage("Bayes Classifier not created yet - not updated");
			return;
		}
		this.analyzePatterns(1.0);
	
		List<DatabasePattern> patternList = DatabaseRelationManager.getTheDatabaseRelationManager().getDatabasePatterns();
		
		for (DatabasePattern dp: patternList) {
			String[] factors = dp.getFactors(factorsToUse);
						
			for (int i=0;i<dp.getNumberOfValidSentences(); i++) {
				_bayes.incrementClass("valid");
				for (String factor: factors) {
					_bayes.incrementClassLikelyhood("valid","text", factor);
				}
				
			}
			
			
			for (int i=0;i<dp.getNumberOfInvalidSentences(); i++) {
				_bayes.incrementClass("invalid");
				for (String factor: factors) {
					_bayes.incrementClassLikelyhood("invalid","text", factor);
				}				

			}
		}		
		
		GCController.getTheGCController().setStatusMessage("Bayes Classifier updated for database");
				
	}
	
	
	
	public void loadClassifier() {
		JFileChooser fileChooser = new JFileChooser();
		int returnVal = fileChooser.showOpenDialog(null); //GCController.getTheGCController().getMainFrame());
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File f = fileChooser.getSelectedFile();
			try (ObjectInputStream input = new ObjectInputStream(new FileInputStream( f ) )){
				_bayes = ( NaiveBayesClassifier ) input.readObject();
				_currentFactors = (ArrayList<DatabaseFactor>) input.readObject();
				_currentFileLocation = f;
		    	
				GCController.getTheGCController().setStatusMessage("NB Classifier loaded for database objects: "+f.getAbsolutePath());
			}
			catch (Exception e) {
				GCController.getTheGCController().setStatusMessage("Unable to load database NB Classifer from "+f.getAbsolutePath());
				System.err.println("Unable to load database NB Classifer: "+e);
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
		Instances dataSet = wc.createWekaInstancesForDatabasePatterns("acp", includeContextSensitiveFactors);
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
	private void classifyDatabasePatterns(ArrayList<DatabaseFactor> factorsForClassification) {
		
		DatabaseRelationManager dbrm = DatabaseRelationManager.getTheDatabaseRelationManager();
		
		if (_bayes == null) {
			Logger.log(Logger.LEVEL_DEBUG, "DBC/classifyDatabasePatterns: skipping classification, bayes classifier not defined.");
			return;
		}
		
		for (DatabasePattern dp: dbrm.getDatabasePatterns()) {
			Object[] arguments = new Object[1];
			arguments[0] = dp.getFactors(factorsForClassification);
			
			Map<String, Double> result = _bayes.computeClassProbabilitiesByLogs(arguments);		//computeClassProbabilitiesByLogs //computeClassProbabilities
			
			dp.setClassification(result.get("valid") > result.get("invalid"));
			
			Logger.log(Logger.LEVEL_TRACE, dp.getClassification()+": "+dp);
		}
		
		GCController.getTheGCController().setStatusMessage("Database patterns classified.");
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
		    	
				GCController.getTheGCController().setStatusMessage("Weka DB Classifier loaded: "+f.getAbsolutePath());
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
			GCController.getTheGCController().setStatusMessage("Weka DB Classifier not created yet - save not possible");
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
		Instances dataSet = wc.createWekaInstancesForDatabasePatterns("dp", includeContextSensitiveFactors );
		_wekaClassifier.trainDatabasePatternClassifier(dataSet );
		
		Logger.log(Logger.LEVEL_DEBUG, "Finished creating Weka classifier for database patterns");
	}
	
	
	/**
	 * Classifies all of the access control patterns in the ACRM
	 */
	protected void classifyDatabasePatternWithWeka(boolean includeContextSensitiveFactors) {
		Logger.log(Logger.LEVEL_DEBUG, "Starting to classify with Weka");

		WekaCreator wc  = new WekaCreator();
		
		DatabaseRelationManager dbrm = DatabaseRelationManager.getTheDatabaseRelationManager();

		for (DatabasePattern db: dbrm.getDatabasePatterns()) {
			Instances dataSet = wc.createWekaInstancesForDatabasePatterns("test Fold", db, includeContextSensitiveFactors);
			ClassificationResult cr = _wekaClassifier.classifyDatabasePattern(dataSet);
			db.setClassification(cr.averageDistance == 0.0);
		}
		Logger.log(Logger.LEVEL_DEBUG, "Finished classifying with Weka");
		
		GCController.getTheGCController().setStatusMessage("Database patterns classified.");
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
			this.classifyDatabasePatternWithWeka(includeContextSensitiveFactors);
			Logger.setCurrentLoggingLevel(Logger.LEVEL_INFO);
			this.extractDatabaseObjects();
			//results[stepIndex] = this.evaluateAccessControl(false,testPercent);
			results[stepIndex] = this.evaluateDatabase(false,0.0);
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
	
	
	public ConfusionMatrix evaluateDatabase(boolean searchDocumentForFalsePositives, double percentileToStart) {
		GCController.getTheGCController().setStatusMessage("evaluate access control called");
		Logger.switchToLevel(Logger.LEVEL_DEBUG);
		
		NLDocument currentDoc = GCController.getTheGCController().getCurrentDocument();
		
		ConfusionMatrix overallResults = new ConfusionMatrix();
		
		List<Sentence> sentences =  currentDoc.getSentences();
		int startPosition = (int)  (sentences.size() * percentileToStart);
		startPosition = Math.min(startPosition, sentences.size());
		
		for (int i= startPosition; i< sentences.size(); i++) {
			Sentence s = sentences.get(i);
			overallResults.add(s.databaseCompare(searchDocumentForFalsePositives, currentDoc));
		}
		

		Logger.log(Logger.LEVEL_INFO, "Database Evaluation:");
		Logger.log(Logger.LEVEL_INFO, "Active pattern count: "+ DatabaseRelationManager.getTheDatabaseRelationManager().getDatabasePatterns().size());
		
		Logger.log(Logger.LEVEL_INFO, overallResults.toString());		
		
		Logger.log(Logger.LEVEL_INFO, "Precision: "+ overallResults.getPrecision());
		Logger.log(Logger.LEVEL_INFO, "Recall: "+ overallResults.getRecall());
		Logger.log(Logger.LEVEL_INFO, "True negative rate: "+overallResults.getTrueNegativeRate());
		Logger.log(Logger.LEVEL_INFO, "Accuracy: "+overallResults.getAccuracy());
		Logger.log(Logger.LEVEL_INFO, "F-Measure:" +overallResults.getF1Measure());
		
		Logger.restoreLoggingLevel();
		
		GCController.getTheGCController().setStatusMessage("Database evaluation complete");
		
		return overallResults;
	}
	
	
	private void reportHold() {
		this.reportDatabasePatternAndMetrics(false);		
	}
	
	public void report() {
		Object[] options =  { "Database Report",   "Sentence DB Report", "Pattern Frequency",
				             "DB Pattern Report","False Negative Report","False Positive Report","All Issues Report", "DBRM Report", "Frequencies", "readability" };
		String selectedReport = (String) JOptionPane.showInputDialog(null,	"Select Report:", "Input",	JOptionPane.INFORMATION_MESSAGE, null,	options, options[0]);

		if (selectedReport == null) { GCController.getTheGCController().setStatusMessage("reporting cancelled"); return; }
		
		switch (selectedReport) {
		case "Pattern Frequency":        this.reportPatternFrequency();						break;
		case "Database Report":         this.reportDatabasePatternAndMetrics(true);	break;

		case "Frequencies":			     this.reportFrequencies();  						break;
		case "DB Pattern Report":        this.reportDatabasePatterns(); 				break;
		case "Sentence DB Report":		 this.reportSentenceDatabaseReport();			break;
		case "False Negative Report":    this.reportPatternsWithIssues(System.out, true, false, true, true); break;
		case "False Positive Report":    this.reportPatternsWithIssues(System.out, true, true, false, true); break;
		case "All Issues Report":		 this.reportPatternsWithIssues(System.out, true, true, true, true);  break;
		case "DBRM Report":              DatabaseRelationManager.getTheDatabaseRelationManager().printReport(System.out); break;
		case "readability":				 this.reportSentenceReadabilityActual();
		}

	}
	
	private void reportSentenceDatabaseReport() {
		List<Sentence> sentences = GCController.getTheGCController().getCurrentDocument().getSentences();
		for (Sentence s: sentences) {
			System.out.println(s.getOriginalSentencePosition()+":"+s);
			if (s.hasBooleanClassification("database design")) {
				List<DatabaseRelation> dbList = s.getDatabaseRelations();
				for (DatabaseRelation db: dbList) {	
					System.out.println("          "+db.toString());
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
			if (s.hasBooleanClassification("database design")) {
			System.out.println (Readability.getFleshKincaidGradeLevelBySentence(s)+" - "+s);
			}
		}
		System.out.println("FleschReadingEaseTestScore: "+ Readability.getFleschReadingEaseTestScore(sentences,"database design"));
		System.out.println("FleshKincaidGradeLevel: "+ Readability.getFleshKincaidGradeLevel(sentences,"database design"));		
	}	

	private void reportDatabasePatterns() {
		DatabaseRelationManager dbrm = DatabaseRelationManager.getTheDatabaseRelationManager();
		
		PrintStream ps = System.out;
		
		ps.println("=========================================================================");
		ps.println("Database Pattern Report");
		ps.println("-------------------------------------------------------------------------");

		
		ps.println("Unique enitities: " + dbrm.getEntities());
		ps.println("Unique attributes: " + dbrm.getAttributes());
		ps.println("Unique relations: " + dbrm.getRelations());
		
		ps.println("Number of patterns: "+dbrm.getDatabasePatterns().size());
		
		ps.println("NumberValid\tNumberInvalid\tNumberNotPresent\tSource\tClassification\tValid\tInvalid\tDatabasePattern\tsize\tEntity/Relationship\tAttribute\tFirstEntity\tSecondEntity");
		
		for (DatabasePattern dp: dbrm.getDatabasePatterns()) {
			System.out.print(dp.getNumberOfValidSentences()+ "\t" + dp.getNumberOfInvalidSentences()+ "\t" + dp.getNumberOfNegativeSentences()+ "\t"+ dp.getDatabaseSource().name());
						
			//Classify the current pattern
			Object[] arguments = new Object[1];
			arguments[0] = dp.getFactors(_currentFactors);;
			Map<String, Double> result = _bayes.computeClassProbabilities(arguments);		//computeClassProbabilitiesByLogs //computeClassProbabilities
			
			double total = result.get("valid") + result.get("invalid");
			
			System.out.print("\t" + (result.get("valid") > result.get("invalid")));
			System.out.print("\t" + (result.get("valid")/total));
			System.out.print("\t" + (result.get("invalid")/total));	
			
			System.out.print("\t"+ dp.getRoot().getStringRepresentation()+"\t"+dp.getRoot().getGraphSize());
			

			if (dp.getDatabaseItem().getRelationType() == DatabaseRelationType.ENTITY) {
				System.out.print(dp.getDatabaseItem().getIdentifyingNodeString()+"\t\t\t\t");
				
			}
			else if (dp.getDatabaseItem().getRelationType() == DatabaseRelationType.ENTITY_ATTR) {
				System.out.print(dp.getDatabaseItem().getParentEntityNodeString()+"\t"+ dp.getDatabaseItem().getIdentifyingNodeString()+"\t\t\t");
			}
			else if (dp.getDatabaseItem().getRelationType() == DatabaseRelationType.RELATIONSHIP) {
				
			}
			else {
				System.out.print("\t\t\t\t");
			}
			System.out.println();
		}
		ps.println("=========================================================================");		
	}	
	
		
	private void reportDatabasePatternAndMetrics(boolean useActive) {  //TODO: Fixme
		System.out.print("***** Database Pattern Report - ");
		if (useActive) { System.out.print("Active"); }
		else           { System.out.print("Hold");   }
		System.out.println(" *****");
		 
		NLDocument currentDocument = GCController.getTheGCController().getCurrentDocument();
		
		//currentDocument.printUniqueNouns(); 	
		HashMap<String,Integer> entities = new HashMap<String,Integer>();
		HashMap<String,Integer> attributes  = new HashMap<String,Integer>();
		HashMap<String,Integer> relations  = new HashMap<String,Integer>();

		HashMap<Relationship,Integer> entityRelations = new HashMap<Relationship,Integer>();
		HashMap<Relationship,Integer> attributeRelations  = new HashMap<Relationship,Integer>();
		HashMap<Relationship,Integer> relationRelations  = new HashMap<Relationship,Integer>();
		
		HashMap<PartOfSpeech,Integer> entityPOS = new HashMap<PartOfSpeech,Integer>();
		HashMap<PartOfSpeech,Integer> attributePOS  = new HashMap<PartOfSpeech,Integer>();
		HashMap<PartOfSpeech,Integer> relationPOS  = new HashMap<PartOfSpeech,Integer>();				
		boolean ignoreConjunctionsInRelationships = true; 
		
		for (Sentence s: currentDocument.getSentences()) {
			List<DatabaseRelation> dbList;
			
			if (useActive) { dbList = s.getDatabaseRelations(); }
			else {           dbList = s.getHoldDatabaseRelations(); }
			if (dbList == null) {continue;}
			
			for (DatabaseRelation db: dbList) {
				if (db.getRelationType() == DatabaseRelationType.ENTITY) {
					int count = entities.get(db.getIdentifyingNodeString()) == null ? 1 : entities.get(db.getIdentifyingNodeString())  +1;
					entities.put(db.getIdentifyingNodeString(), count);					
				}
				else if  (db.getRelationType() == DatabaseRelationType.ENTITY_ATTR) {
					int count = attributes.get(db.getIdentifyingNodeString()) == null ? 1 : attributes.get(db.getIdentifyingNodeString())  +1;
					attributes.put(db.getIdentifyingNodeString(), count);	
					
					count = entities.get(db.getParentEntityNodeString()) == null ? 1 : entities.get(db.getParentEntityNodeString())  +1;
					entities.put(db.getParentEntityNodeString(), count);						
					
				}
				else if  (db.getRelationType() == DatabaseRelationType.RELATIONSHIP) {
					int count = relations.get(db.getIdentifyingNodeString()) == null ? 1 : relations.get(db.getIdentifyingNodeString())  +1;
					relations.put(db.getIdentifyingNodeString(), count);	
					
					count = entities.get(db.getFirstEntityNodeString()) == null ? 1 : entities.get(db.getFirstEntityNodeString())  +1;
					entities.put(db.getFirstEntityNodeString(), count);							

					count = entities.get(db.getSecondEntityNodeString()) == null ? 1 : entities.get(db.getSecondEntityNodeString())  +1;
					entities.put(db.getSecondEntityNodeString(), count);		
				}
				
				
				for (WordVertex wv: db.getIndentifyingNode()) {
					for (int parentIndex = 0; parentIndex < wv.getNumberOfParents(); parentIndex++ ) {
						Relationship parent = wv.getParentAt(parentIndex).getRelationship();
						if (ignoreConjunctionsInRelationships && parent.isConjunction()) { continue; }
						
						if (db.getRelationType() == DatabaseRelationType.ENTITY) {
							int count = entityRelations.get(parent) == null ? 1 : entityRelations.get(parent)  +1;
							entityRelations.put(parent, count);	
						}
						else if  (db.getRelationType() == DatabaseRelationType.ENTITY_ATTR) {
							int count = attributeRelations.get(parent) == null ? 1 : attributeRelations.get(parent)  +1;
							attributeRelations.put(parent, count);	
						}
						else if  (db.getRelationType() == DatabaseRelationType.RELATIONSHIP) {
							int count = relationRelations.get(parent) == null ? 1 : relationRelations.get(parent)  +1;
							relationRelations.put(parent, count);	
						}
						
					}
					
					if (db.getRelationType() == DatabaseRelationType.ENTITY) {
						int count = entityPOS.get(wv.getPartOfSpeech())  == null ? 1 : entityPOS.get(wv.getPartOfSpeech())  +1;
						entityPOS.put(wv.getPartOfSpeech(), count);
					}
					else if  (db.getRelationType() == DatabaseRelationType.ENTITY_ATTR) {
						int count = attributePOS.get(wv.getPartOfSpeech())  == null ? 1 : attributePOS.get(wv.getPartOfSpeech())  +1;
						attributePOS.put(wv.getPartOfSpeech(), count);
					}
					else if  (db.getRelationType() == DatabaseRelationType.RELATIONSHIP) {
						int count = relationPOS.get(wv.getPartOfSpeech())  == null ? 1 : relationPOS.get(wv.getPartOfSpeech())  +1;
						relationPOS.put(wv.getPartOfSpeech(), count);
					}
					
				}

				for (WordVertex wv: db.getParentEntityNode()) {
					for (int parentIndex = 0; parentIndex < wv.getNumberOfParents(); parentIndex++ ) {
						Relationship parent = wv.getParentAt(parentIndex).getRelationship();
						if (ignoreConjunctionsInRelationships && parent.isConjunction()) { continue; }
						
						if (db.getRelationType() == DatabaseRelationType.ENTITY) { } // Do nothing, this is stored as an attribute and handled above
						else if  (db.getRelationType() == DatabaseRelationType.ENTITY_ATTR) {
							int count = entityRelations.get(parent) == null ? 1 : entityRelations.get(parent)  +1;
							entityRelations.put(parent, count);	
						}
						else if  (db.getRelationType() == DatabaseRelationType.RELATIONSHIP) { // Do nothing, not applicable for this type
						}
						
					}
										
					if (db.getRelationType() == DatabaseRelationType.ENTITY) { } // Do nothing, this is stored as an attribute and handled above
					else if  (db.getRelationType() == DatabaseRelationType.ENTITY_ATTR) {
						int count = entityPOS.get(wv.getPartOfSpeech())  == null ? 1 : entityPOS.get(wv.getPartOfSpeech())  +1;
						entityPOS.put(wv.getPartOfSpeech(), count);
					}
					else if  (db.getRelationType() == DatabaseRelationType.RELATIONSHIP) { // Do nothing, not applicable for this type
					}					
				}

				for (WordVertex wv: db.getFirstEntityNode()) {
					for (int parentIndex = 0; parentIndex < wv.getNumberOfParents(); parentIndex++ ) {
						Relationship parent = wv.getParentAt(parentIndex).getRelationship();
						if (ignoreConjunctionsInRelationships && parent.isConjunction()) { continue; }
						
						if (db.getRelationType() == DatabaseRelationType.ENTITY) { } // do nothnig, just grabbing the child of relationships here
						else if  (db.getRelationType() == DatabaseRelationType.ENTITY_ATTR) { }// do nothnig, just grabbing the child of relationships here
						else if  (db.getRelationType() == DatabaseRelationType.RELATIONSHIP) {
							int count = entityRelations.get(parent) == null ? 1 : entityRelations.get(parent)  +1;
							entityRelations.put(parent, count);	
						}
					}
					if (db.getRelationType() == DatabaseRelationType.ENTITY) { } // Do nothing, this is stored as an attribute and handled above
					else if  (db.getRelationType() == DatabaseRelationType.ENTITY_ATTR) { } // Do nothing, not applicable for this type
					else if  (db.getRelationType() == DatabaseRelationType.RELATIONSHIP) { 
						int count = entityPOS.get(wv.getPartOfSpeech())  == null ? 1 : entityPOS.get(wv.getPartOfSpeech())  +1;
						entityPOS.put(wv.getPartOfSpeech(), count);
					}						
				}				

				for (WordVertex wv: db.getSecondEntityNode()) {
					for (int parentIndex = 0; parentIndex < wv.getNumberOfParents(); parentIndex++ ) {
						Relationship parent = wv.getParentAt(parentIndex).getRelationship();
						if (ignoreConjunctionsInRelationships && parent.isConjunction()) { continue; }
						
						if (db.getRelationType() == DatabaseRelationType.ENTITY) { } // do nothnig, just grabbing the child of relationships here
						else if  (db.getRelationType() == DatabaseRelationType.ENTITY_ATTR) { }// do nothnig, just grabbing the child of relationships here
						else if  (db.getRelationType() == DatabaseRelationType.RELATIONSHIP) {
							int count = entityRelations.get(parent) == null ? 1 : entityRelations.get(parent)  +1;
							entityRelations.put(parent, count);	
						}
					}
					if (db.getRelationType() == DatabaseRelationType.ENTITY) { } // Do nothing, this is stored as an attribute and handled above
					else if  (db.getRelationType() == DatabaseRelationType.ENTITY_ATTR) { } // Do nothing, not applicable for this type
					else if  (db.getRelationType() == DatabaseRelationType.RELATIONSHIP) { 
						int count = entityPOS.get(wv.getPartOfSpeech())  == null ? 1 : entityPOS.get(wv.getPartOfSpeech())  +1;
						entityPOS.put(wv.getPartOfSpeech(), count);
					}						
				}						
				
			}
		}

		for (String s:entities.keySet()) { System.out.println("entity\t"+ entities.get(s)+"\t"+s);		}
		for (String s:attributes.keySet()) { System.out.println("attribute\t"+ attributes.get(s)+"\t"+s);		}
		for (String s:relations.keySet()) { System.out.println("relation\t"+ relations.get(s)+"\t"+s);		}
		
		for (Relationship r:entityRelations.keySet()) { System.out.println("subjectRelationship\t"+ entityRelations.get(r)+"\t"+r);		}
		for (Relationship r:attributeRelations.keySet()) { System.out.println("actionRelationship\t"+ attributeRelations.get(r)+"\t"+r);		}
		for (Relationship r:relationRelations.keySet()) { System.out.println("relationRelationship\t"+ relationRelations.get(r)+"\t"+r);		}

		for (PartOfSpeech p:entityPOS.keySet()) { System.out.println("entityPartOfSpeech\t"+ entityPOS.get(p)+"\t"+p);		}
		for (PartOfSpeech p:attributePOS.keySet()) { System.out.println("attributePartOfSpeech\t"+ attributePOS.get(p)+"\t"+p);		}
		for (PartOfSpeech p:relationPOS.keySet()) { System.out.println("relationPartOfSpeech\t"+ relationPOS.get(p)+"\t"+p);		}		

		
		int sentenceCountForDatabase = 0;
		int databaseCountFound       = 0;
		for (Sentence s: currentDocument.getSentences()) {
			List<DatabaseRelation> dbList;
			
			if (useActive) { dbList = s.getDatabaseRelations(); }
			else {           dbList = s.getHoldDatabaseRelations(); }
			if (dbList.size() >0 ) {  sentenceCountForDatabase++; }
			for (DatabaseRelation dr: dbList) {	
				databaseCountFound++;
				System.out.println(dr.toString());
			}
		}
		System.out.println("Number of sentences with database: "+sentenceCountForDatabase);
		System.out.println("Total number of database identifications: "+databaseCountFound);		
		
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
			for (DatabaseRelation dr: s.getDatabaseRelations()) {	
				Set<WordVertex> vertices = dr.getAllVertices();
				if (dr.getRelationType() == DatabaseRelationType.ENTITY) {
					vertices.add(s.getRoot());
				}
				
				WordVertex wvp = WordVertex.extractPattern(vertices);
				
				DatabasePattern newPattern = new DatabasePattern(wvp, new DatabaseRelation(dr), dr.getSource(), dr.getRelationType());

				Integer patternSize = newPattern.getRoot().getGraphSize();
				String pattern = newPattern.getDatabaseItem().getRelationType() +"\t" + newPattern.toStringPattern();
			
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
			for (DatabaseRelation ac: s.getDatabaseRelations()) {	
				Set<WordVertex> vertices = ac.getAllVertices();
				if (ac.getRelationType() == DatabaseRelationType.ENTITY) {
					vertices.add(s.getRoot());
				}
				
				WordVertex wvp = WordVertex.extractPattern(vertices);
				//WordVertex wvp = WordVertex.extractPattern(dr.getAllVertices());
				DatabasePattern newPattern = new DatabasePattern(wvp, new DatabaseRelation(ac), ac.getSource(),ac.getRelationType());
				
				System.out.println(newPattern.getPatternType()+"\tPattern Size\t"+newPattern.getRoot().getGraphSize());
				
				int distance = newPattern.getRoot().getGreatestWordPosition() - newPattern.getRoot().getSmallestWordPosition();
				System.out.println(newPattern.getPatternType()+"\tPattern Distance\t"+distance);
				
			}
			if (s.getDatabaseRelations().size() >0 ) {
				System.out.println("\tParse Tree Size\t"+s.getNumberOfNodes());
			}
		}		
	}	
	public void printMessage(String s) {
		if (_verbose) {
			System.out.println(s);
			
		}
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
	public List<DatabasePattern> findFalsePositives(Sentence s) {
		ArrayList<DatabasePattern> result = new ArrayList<DatabasePattern>();
		for (DatabaseRelation ac: s.getDatabaseRelations()) {	
			boolean foundAC = false;
			for (DatabaseRelation existingAC: s.getHoldDatabaseRelations()) {
				if (existingAC.contains(ac) || ac.contains(existingAC)) { foundAC= true; }
			}
			if (foundAC) { continue; }
	
			
			Set<WordVertex> vertices = ac.getAllVertices();
			if (ac.getRelationType() == DatabaseRelationType.ENTITY) {
				vertices.add(s.getRoot());
			}
			
			WordVertex wvp = WordVertex.extractPattern(vertices);
			//WordVertex wvp = WordVertex.extractPattern(dr.getAllVertices());
			result.add(new DatabasePattern(wvp, new DatabaseRelation(ac), ac.getSource(),ac.getRelationType()));
		}
		return result;
	}
	
	public List<DatabasePattern> findFalseNegatives(Sentence s) {
		ArrayList<DatabasePattern> result = new ArrayList<DatabasePattern>();

		for (DatabaseRelation existingAC:  s.getHoldDatabaseRelations()) {
			boolean foundCombinable = false;
			for (DatabaseRelation ac: s.getDatabaseRelations()) {
				if (existingAC.contains(ac) || ac.contains(existingAC) ) { foundCombinable= true; }
			}
			if (foundCombinable) { continue; }

			Set<WordVertex> vertices = existingAC.getAllVertices();
			if (existingAC.getRelationType() == DatabaseRelationType.ENTITY) {
				vertices.add(s.getRoot());
			}
			
			WordVertex wvp = WordVertex.extractPattern(vertices);
			//WordVertex wvp = WordVertex.extractPattern(dr.getAllVertices());
			result.add(new DatabasePattern(wvp, new DatabaseRelation(existingAC), existingAC.getSource(),existingAC.getRelationType()));
		}
		
		return result;
	}	
	
	/**
	 * Used to list the patterns that are process can't currently find - the false negatives.
	 * Logic is pretty much the same as that needed for Evaluate database
	 * 
	 * Generates two reports:
	 * 1. For each sentence, the access control tuple and associated pattern not found.
	 * 2. A frequency report (similar to that in reportPatternFrequency), but for the undiscovered patterns
	 * 
	 */
	private void reportPatternsWithIssues(PrintStream ps, boolean reportSentences, boolean reportFalsePositives, boolean reportFalseNegatives, boolean reportSizes) {
				
		HashMap<String,Integer> sizeCounts   = new HashMap<String, Integer>();
		HashMap<String,Integer> patternCounts = new HashMap<String, Integer>(); 
		
		ps.println("Type\tDB Pattern\tAC Tuple\tSentenceID\tSentence");
		for (Sentence s: GCController.getTheGCController().getCurrentDocument().getSentences()) {
			List<DatabasePattern> falsePositives = this.findFalsePositives(s);
			for (DatabasePattern patternFP: falsePositives) {
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
					ps.print(patternFP);
					ps.print("\t");
					ps.print(s.getOriginalSentencePosition()); ps.print('\t');
					ps.println(s.getSentence());
				}
			}
			
			List<DatabasePattern> falseNegatives= this.findFalseNegatives(s);
			for (DatabasePattern patternFN: falseNegatives) {
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
					ps.print(patternFN);
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
