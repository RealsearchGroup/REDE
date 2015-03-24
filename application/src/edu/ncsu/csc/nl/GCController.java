package edu.ncsu.csc.nl;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;

import weka.core.Instances;
import edu.mit.jwi.Dictionary;
import edu.ncsu.csc.nl.event.NLPEvent;
import edu.ncsu.csc.nl.event.NLPEventListener;
import edu.ncsu.csc.nl.event.NLPEventManager;
import edu.ncsu.csc.nl.event.NLPEventSentenceDataEvent;
import edu.ncsu.csc.nl.event.NLPEventType;
import edu.ncsu.csc.nl.event.NLPEventViewChanged;
import edu.ncsu.csc.nl.model.AccessControlRuleList;
import edu.ncsu.csc.nl.model.DatabaseElementList;
import edu.ncsu.csc.nl.model.NLDocument;
import edu.ncsu.csc.nl.model.SecurityObjectiveAnnotation;
import edu.ncsu.csc.nl.model.Sentence;
import edu.ncsu.csc.nl.model.WekaCreator;
import edu.ncsu.csc.nl.model.WekaCreatorOptions;
import edu.ncsu.csc.nl.model.WordVertex;
import edu.ncsu.csc.nl.model.classification.BooleanClassification;
import edu.ncsu.csc.nl.model.classification.ClassificationAttribute;
import edu.ncsu.csc.nl.model.classification.ClassificationAttributeTableModel;
import edu.ncsu.csc.nl.model.classification.ClassificationType;
import edu.ncsu.csc.nl.model.distance.CosineTermFreqIDFDistance;
import edu.ncsu.csc.nl.model.distance.DiceWordSetDistance;
import edu.ncsu.csc.nl.model.distance.GraphWalkDistance;
import edu.ncsu.csc.nl.model.distance.JacardWordSetDistance;
import edu.ncsu.csc.nl.model.distance.LevenshteinSentenceAsStringDistance;
import edu.ncsu.csc.nl.model.distance.LevenshteinSentenceAsWordsDistance;
import edu.ncsu.csc.nl.model.distance.LinearWalkDistance;
import edu.ncsu.csc.nl.model.distance.StringDistance;
import edu.ncsu.csc.nl.model.distance.TreeRelationshipAsLevenshteinDistance;
import edu.ncsu.csc.nl.model.distance.WordDistance;
import edu.ncsu.csc.nl.model.english.StopWord;
import edu.ncsu.csc.nl.model.ml.ClassificationResult;
import edu.ncsu.csc.nl.model.ml.ClusterOptions;
import edu.ncsu.csc.nl.model.ml.ConfusionMatrix;
import edu.ncsu.csc.nl.model.ml.Document;
import edu.ncsu.csc.nl.model.ml.DocumentFoldCreator;
import edu.ncsu.csc.nl.model.ml.ExpirementOptions;
import edu.ncsu.csc.nl.model.ml.InstanceLearner;
import edu.ncsu.csc.nl.model.ml.KMedoid;
import edu.ncsu.csc.nl.model.ml.MasterClassifier;
import edu.ncsu.csc.nl.model.ml.SentenceCluster;
import edu.ncsu.csc.nl.model.ml.WekaInterfacer;
import edu.ncsu.csc.nl.model.relation.AccessControlRelationManager;
import edu.ncsu.csc.nl.model.relation.DatabaseRelationManager;
import edu.ncsu.csc.nl.model.type.BooleanType;
import edu.ncsu.csc.nl.model.type.PartOfSpeech;
import edu.ncsu.csc.nl.model.type.Relationship;
import edu.ncsu.csc.nl.model.type.SecurityImpact;
import edu.ncsu.csc.nl.model.type.SecurityMitigation;
import edu.ncsu.csc.nl.model.type.SecurityObjective;
import edu.ncsu.csc.nl.model.type.Source;
import edu.ncsu.csc.nl.model.type.WordType;
import edu.ncsu.csc.nl.view.AccessControlDialog;
import edu.ncsu.csc.nl.view.ClassificationBooleanPanel;
import edu.ncsu.csc.nl.view.ClassificationPanel;
import edu.ncsu.csc.nl.view.ClassifierDialog;
import edu.ncsu.csc.nl.view.ClassifierResultsDialog;
import edu.ncsu.csc.nl.view.DatabaseDialog;
import edu.ncsu.csc.nl.view.OptionsDialog;
import edu.ncsu.csc.nl.view.SecurityObjectiveAnnotationController;
import edu.ncsu.csc.nl.view.WordNetBrowserDialog;
import edu.ncsu.csc.nl.view.MainFrame;
import edu.ncsu.csc.nl.view.POSOverrideEditor;
import edu.ncsu.csc.nl.weka.WekaUtility;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;


/**
 * This is the primary class for starting the RBAC application.
 * 
 * 
 * @author John
 *
 */
public class GCController implements ActionListener, ListSelectionListener, NLPEventListener  {
	/** */
	private edu.stanford.nlp.pipeline.StanfordCoreNLP _pipeline;
	
	/** This is the link to the Princeton WordNet Dictionary */
	private edu.mit.jwi.Dictionary _wordNetDictionary;
	
	private static GCController _theGCController;
	
	/** */
	private MainFrame  _mainFrame;
	
	private ClassifierDialog    _classifierDialog;
	private AccessControlDialog _accessControlDialog;
	private DatabaseDialog      _databaseDialog;
	private SecurityObjectiveAnnotationController _securityObjectiveAnnotationController;
	
	private WordNetBrowserDialog _wordnetBrowserDialog;
	
	private ClassifierResultsDialog _classifierResultsDialog;
	
	private ClassificationAttributeTableModel _classificationAttributes;
	
	private OptionsDialog _optionsDialog;
	
	private NLDocument _currentDocument;
	private String     _currentDocumentID = "unknown";	
	private int _currentSentenceNumber = -1;
	
	/** tracks the access control rules associated with the current document */
	private AccessControlRuleList _acrList;
	public AccessControlRuleList getACRList() {return _acrList;	}
	public void setACRList(AccessControlRuleList acrList) {this._acrList = acrList;	}

	/** tracks the database elements associated with the current document */
	private DatabaseElementList _dbElementList;
	public DatabaseElementList getADatabaseElementList() {return _dbElementList;	}
	public void setDatabaseElementList(DatabaseElementList dbElementList) {this._dbElementList = dbElementList;	}


	private InstanceLearner _theInstanceLearner = new InstanceLearner();
	
	private MasterClassifier _masterClassifier = null;
	
	private File _currentFileLocation = null;
	
	private String _lastSearchValue = "";
	
	private POSOverrideEditor _posOverrideEditor = new POSOverrideEditor();
	
	private int _currentView = GCConstants.VIEW_CLASSIFICATIONS;
	
	public static GCController getTheGCController() {
		if (_theGCController == null) {
			_theGCController = new GCController();
		}
		return _theGCController;
	}
	
	private  GCController() {
	}
	
	public void initialize(String wordNetDictionaryLocation, boolean parse) {
		try {
			java.io.File wordnetDir = new java.io.File(wordNetDictionaryLocation);
			_wordNetDictionary = new Dictionary(wordnetDir);
			_wordNetDictionary.open();
		}
		catch (java.io.IOException ex) {
			System.err.println("Unable to create dictionary from this location: "+wordNetDictionaryLocation);
			System.exit(0);
		}
		
		NLPEventManager.getTheEventManager().registerForEvent(NLPEventType.MARK_CLASSIFIED_AND_MOVE, this);
		NLPEventManager.getTheEventManager().registerForEvent(NLPEventType.MOVED_TO_SENTENCE, this);
		AccessControlRelationManager.getTheAccessControlRelationManager();
		DatabaseRelationManager.getTheDatabaseRelationManager();
		
		if (parse) {
			this.createPipeline();
		}	
	}
	
	public void initializeClassifierElements(String location) {
		try {
			_classificationAttributes = ClassificationAttributeTableModel.readFromFile(new java.io.File(location));
			//catm.dumpAttributeList(System.out);
			
			
			_masterClassifier  = new MasterClassifier(_classificationAttributes.getAttributeList(), SecurityObjective.getMinimalListForClassification());
		}
		catch (Exception e) {
			System.out.println(e);
		}
	}
	
	public void initializeGUI() {
		_mainFrame = new MainFrame();
		_wordnetBrowserDialog = new WordNetBrowserDialog();
		_wordnetBrowserDialog.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
		
		
		_accessControlDialog = new AccessControlDialog(_mainFrame);
		_accessControlDialog.setVisible(true);
		_accessControlDialog.setLocation(1250,320);
		
		_databaseDialog = new DatabaseDialog(_mainFrame);
		_databaseDialog.setVisible(true);
		_databaseDialog.setLocation(1250,500);
		
		_securityObjectiveAnnotationController = new SecurityObjectiveAnnotationController(_mainFrame);
		//_securityObjectiveAnnotationController.setVisible(true);
		
		_classifierResultsDialog = new ClassifierResultsDialog(_mainFrame);
		_classifierResultsDialog.register();		
		
		ClassificationPanel cp = new ClassificationBooleanPanel(_classificationAttributes);
		_classifierDialog = new ClassifierDialog(_mainFrame,cp);
		_classifierDialog.setVisible(true);
		_classifierDialog.setLocation(1250, 20);
		
	}
	
	/**
	 * returns a constant as to whether or not annotations/classifications should be shown
	 * @return
	 */
	public int getCurrentView() {
		return _currentView;
	}

	/**
	 * Sets whether or not annotations or classifications should be shown
	 * 
	 * @param currentView
	 */
	public void setCurrentView(int currentView) throws IllegalArgumentException {
		if (currentView != GCConstants.VIEW_CLASSIFICATIONS &&
		    currentView != GCConstants.VIEW_ANNOTATIONS) {
			throw new IllegalArgumentException("Invalid argument to setCurrentView: "+currentView);
		}
		
		_currentView = currentView;  
		NLPEventViewChanged e = new NLPEventViewChanged(currentView);
		NLPEventManager.getTheEventManager().sendEvent(NLPEventType.VIEW_CHANGED, e);
		
		//TODO need send a message that the view has changed.
	}	
	
	public NLDocument getCurrentDocument() {
		return _currentDocument;
	}
	
	/**
	 * Only used to allow dialog boxes to tie themselves too.
	 * @return
	 */
	public MainFrame getMainFrame() {
		return _mainFrame;
	}
	
	public void setStatusMessage(String message) {
		_mainFrame.setStatusMessage(message);
	}
	
	private void createPipeline() {
		if (_pipeline == null) {
		    // creates a StanfordCoreNLP object, with POS tagging, lemmatization, NER, parsing, and coreference resolution 
		    java.util.Properties props = new java.util.Properties();
		    //props.put("annotators", "tokenize, ssplit, pos, posfix, lemma, ner, parse");
		    //props.put("annotators", "tokenize, ssplit, pos, posfix, lemma, ner, parse, dcoref");
		    props.put("annotators", "tokenize, ssplit, pos, posfix, lemma, ner, parse");
		    
		    props.put("customAnnotatorClass.posfix","edu.ncsu.csc.nl.model.CorrectPOSTags");
		    _pipeline = new StanfordCoreNLP(props);				
		}
	}
	
	public StanfordCoreNLP getPipeline() {
		return _pipeline;
	}
	
	public void loadAndParseDocument(String filename){
		_currentDocument = new NLDocument();
		_currentDocument.setFileLocation(filename);
		_currentDocument.loadAndParse(this._pipeline);
		_currentFileLocation = new File(filename);
		setViewWithDocument();
	}
	
	
	private void setViewWithDocument() {
		_mainFrame.getSentenceClassifer().setCurrentDocument(_currentDocument);
		this.setCurrentSentence(0,true,true);
	}
	
	public ClassificationAttributeTableModel getClassificationAttributes() {
		return _classificationAttributes;
	}
	
	public Dictionary getWordNetDictionary() {
		return _wordNetDictionary;
	}
	
	
	public boolean isSupervisedLearning() {
		return _mainFrame.isSupervisedLearningChecked();
	}
	
	public boolean isAutocompleteChecked() {
		return _mainFrame.isAutocompleteChecked();
	}
	
	public InstanceLearner getInstanceLearner() {
		return _theInstanceLearner;
	}
	
	
	public static void printUsage() {
		
		System.out.println("Arguments:");
		System.out.println("\t[-l]           Load the stanford parser");
		System.out.println("\t -c filename   Location of the classifications to use");		
		System.out.println("\t[-i filename]  Load document from a text file and apply the parser to it.");
		System.out.println("\t[-p filename]  Load the parsed document from a serialized object file specified by the filename");
		System.out.println("\t[-m filename]  Load the specified instance learner");
		System.out.println("\t -w filename   Location of the wordnet dictionary(directory only)");
		System.out.println("May not specify both -i and -p. If -i is specified, -l is automatically assumed by the systems");
		System.out.println("-w is a required parameter, the others are optional");
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		try {
		    for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
		        if ("Nimbus".equals(info.getName())) {
		        	javax.swing.UIManager.setLookAndFeel(info.getClassName());
		            break;
		        }
		    }
		} catch (Exception e) {
		    // If Nimbus is not available, you can set the GUI to another look and feel.
		}		
		
		boolean loadParser = false;
		String initialParsedDocument    = "";
		String intialDocumentToParse    = "";
		String initialLearner           = "";
		String wordNetLocation          = "";
		String classificationsLocation  = "";
		
		for (int i=0;i < args.length; i++) {
			if (args[i].equals("-l")) {
				loadParser = true;
			}
			else if(args[i].startsWith("-p") && (i+1)<args.length ){
				initialParsedDocument = args[++i]; // document name is the next argument, we can skip processing it
			}	
			else if(args[i].startsWith("-m") && (i+1)<args.length ){
				initialLearner = args[++i]; // document name is the next argument, we can skip processing it
			}		
			else if(args[i].startsWith("-i") && (i+1)<args.length ){
				intialDocumentToParse = args[++i]; // document name is the next argument, we can skip processing it
				loadParser = true;  //have to have to parse to read a text file
			}
			else if(args[i].startsWith("-w") && (i+1)<args.length ){
				wordNetLocation = args[++i]; // document name is the next argument, we can skip processing it
			}	
			else if(args[i].startsWith("-c") && (i+1)<args.length ){
				classificationsLocation = args[++i]; // document name is the next argument, we can skip processing it
			}				
		}
		
		boolean argumentError = false;
		if (wordNetLocation.equals("")) {
			System.out.println("Error: must specify the location of the WordNet dictionary");
			argumentError = true;
		}
		if (classificationsLocation.equals("")) {
			System.out.println("Error: must specify the location of the classifications file");
			argumentError = true;
		}
		
		if (intialDocumentToParse.equals("") == false && initialParsedDocument.equals("") == false){
			System.out.println("Error: cannot specify both a parsed document and a document to parse");
			argumentError = true;
		}
		if (argumentError) {
			printUsage();
			System.exit(0);
		}
		
		GCController controller = GCController.getTheGCController();
		controller.initialize(wordNetLocation,loadParser);
		controller.initializeClassifierElements(classificationsLocation);
		controller.initializeGUI();
		
	    try {
	    	if (!initialLearner.equals("")) {
	    		controller._theInstanceLearner.loadFromFile(new File(initialLearner));
	    	}
	    	
	    	if (initialParsedDocument.equals("") == false) {
	    		controller.loadJSONDocumentFromFile(new File(initialParsedDocument));
	    	}
	    	else if (intialDocumentToParse.equals("") == false) {
	    		controller.loadAndParseDocument(intialDocumentToParse);
	    	}
	    	else { //startup system with a blank document
	    		controller.newDocument();
	    	}
	    }
	    catch (Exception e) {
	    	System.out.println(e);
	    	System.exit(0);
	    }
	    		
		controller._mainFrame.setVisible(true);   //GUI now has control of the application.
	}
	
	public void  valueChanged(ListSelectionEvent lse) {
		//System.out.println(lse);
		//System.out.println(lse.getValueIsAdjusting());   //there are two events for the list.  the first event lets us know that the user is starting to  change things, which we don't care about.
		
		if (lse.getValueIsAdjusting() == false && lse.getSource() == _mainFrame.getSentenceClassifer().getSentenceTable().getSelectionModel()) {
			int index  =  _mainFrame.getSentenceClassifer().getSentenceTable().getSelectedRow();
			if (index < 0) {
				_mainFrame.getSentenceClassifer().setCurrentSentence(-1,false);
				_classifierDialog.setCurrentClassificationItem(null,null);
				return; 
			}			
			// this has been moved to setCurrent index = _mainFrame.getSentenceClassifer().getSentenceTable().convertRowIndexToModel(index);
			this.setCurrentSentence(index,false,false);
		}
		
	}	
	
	public void actionPerformed(ActionEvent ae) {
		_mainFrame.setStatusMessage("");
		
		switch (ae.getActionCommand()) {
		    case GCConstants.ACTION_DOCUMENT_NEW: newDocument(); return;
			case GCConstants.ACTION_DOCUMENT_EXIT: System.exit(1);
			case GCConstants.ACTION_DOCUMENT_LOAD_JSON: loadJSONDocument(); return;
			case GCConstants.ACTION_DOCUMENT_LOAD_SERIAL: loadSerialDocument(); return;
			case GCConstants.ACTION_DOCUMENT_LOAD_TEXT_DOCUMENT: loadTextDocument(); return;
			case GCConstants.ACTION_DOCUMENT_SAVE_JSON: saveDocumentAsJSON(); return;
			case GCConstants.ACTION_DOCUMENT_APPEND_JSON: appendJSONDocument(); return;
			case GCConstants.ACTION_DOCUMENT_SAVE_SERIAL: saveDocumentAsSerial(); return;			
			case GCConstants.ACTION_DOCUMENT_EXPORT_ARFF: exportARFF(); return;
			case GCConstants.ACTION_DOCUMENT_COMPARE_JSON: compareJSON(); //generatAnnotationsReport(); /* computeKappaForAnnotations();*/ /*compareJSON();*/ return; 
			case GCConstants.ACTION_DOCUMENT_SET_ID: establishCurrentDocumentID(); return; 
			case GCConstants.ACTION_DOCUMENT_RENUMBER: renumberCurrentDocumentID(); return;
			case GCConstants.ACTION_DOCUMENT_GOTO_LINE: gotoLineInDocument(); return;
			case GCConstants.ACTION_DOCUMENT_GOTO_NEXT_UNCLASS: gotoNextUnclassifiedInDocument(); return;
			case GCConstants.ACTION_DOCUMENT_GOTO_NEXT_ACCESS: gotoNextAccessUndefinedInDocument(); return;
			case GCConstants.ACTION_DOCUMENT_GOTO_NEXT_SOA: gotoNextSOAUndefinedInDocument(); return;
			case GCConstants.ACTION_DOCUMENT_GOTO_NEXT_DATABASE: gotoNextDatabaseIncompleteInDocument(); return;
			case GCConstants.ACTION_OTHER_WORDNET_BROWSER: _wordnetBrowserDialog.setVisible(true); return;
			case GCConstants.ACTION_OTHER_VIEW_CLASS_RESULTS: _classifierDialog.setVisible(true); 
			                                                  _classifierResultsDialog.setVisible(true);
			                                                  _accessControlDialog.setVisible(true);
			                                                  _databaseDialog.setVisible(true);
			                                                  _securityObjectiveAnnotationController.setVisible(true);
			                                                  return;
			case GCConstants.ACTION_OTHER_OPTIONS: this.openOptionsDialog(); return;
			case GCConstants.ACTION_OTHER_FIND: findWord(false); return;
			case GCConstants.ACTION_OTHER_FIND_NEXT: findWord(true); return;
			case GCConstants.ACTION_OTHER_MARK_FOR_ACDL: markSOAForACDL(); return;
			case GCConstants.ACTION_OTHER_CLUSTER: performCluster(); return;
			
			case GCConstants.ACTION_OTHER_ALL_SELF_EVALUATE: performAllSelfEvaluate(); return;
			case GCConstants.ACTION_OTHER_ALL_DOC_EVALUATE: performAllDocEvaluate(); return;
			case GCConstants.ACTION_OTHER_CURRENT_DOC_EVALUATE: performAllCurrentDocEvaluate(); return;
			case GCConstants.ACTION_OTHER_VERB_FREQUENCIES: produceVerbFrequencyCounts(); return;
			case GCConstants.ACTION_OTHER_RESTORE_AC_RELATIONS: _currentDocument.moveHoldToAccessControlRelations();; _mainFrame.setStatusMessage("Held access control relations returned."); return;
			
			case GCConstants.ACTION_LOAD_PIPELINE:   loadParser(); return;

			case GCConstants.ACTION_LEARNER_CLEAR: _theInstanceLearner.clearTrainedSentence(); _mainFrame.setStatusMessage("Instance learner cleared"); return;
			case GCConstants.ACTION_LEARNER_DUMP:  _theInstanceLearner.printTrainedSentences(System.out); return;
			case GCConstants.ACTION_LEARNER_SAVE:  saveLearner(false); return;
			case GCConstants.ACTION_LEARNER_LOAD:  loadLearner(false); return;
			case GCConstants.ACTION_LEARNER_SAVE_SERIAL:  saveLearner(true); return;
			case GCConstants.ACTION_LEARNER_LOAD_SERIAL:  loadLearner(true); return;
			
			case GCConstants.ACTION_LEARNER_SETK:  setKforInstanceLearner(); return;
			case GCConstants.ACTION_LEARNER_ADDTRAIN:  _currentDocument.addAllTrainedSentencesToInstanceLearner(); _mainFrame.setStatusMessage("Trained sentences added to instance learner"); return;
			case GCConstants.ACTION_LEARNER_SELFEVAL: this.performSelfEvaluationForClassifications(); return;
			case GCConstants.ACTION_LEARNER_SELFEVAL_ANNOTATIONS: this.performSelfEvaluationForAnnotations(); return;
			case GCConstants.ACTION_LEARNER_DOCUMENT_EVAL: this.performDocumentEvaluationForClassifications(); _mainFrame.setStatusMessage("Document-evaluation Generated"); return;			
			case GCConstants.ACTION_LEARNER_DOCUMENT_EVAL_ANNOTATIONS: this.performDocumentEvaluationForAnnotations(); _mainFrame.setStatusMessage("Document-evaluation generated for annotations"); return;						
			case GCConstants.ACTION_LEARNER_INTERNAL_NB:  this.performInternalNBEvaluation(); _mainFrame.setStatusMessage("Internal NB Evaluation complete"); return;	
			case GCConstants.ACTION_LEARNER_MOVE_TO_CURRENT_DOC: this.moveInstanceLearnerSentencesToCurrentDoc(); return;
			
			case GCConstants.ACTION_LEARNER_EXPORT_SOA_SENTENCES: exportSOASentences(); return;
			case GCConstants.ACTION_LEARNER_PRODUCE_SOA_MATRIX:   ReportController.getTheReportController().produceSOAMatrix(); return;
			case GCConstants.ACTION_LEARNER_PRODUCE_SOA_FULL:     ReportController.getTheReportController().produceSOAFullReport(); return;
			
			
			case GCConstants.ACTION_OTHER_POS_OVERRIDES: handleOverridePOS(); return;
			
			case GCConstants.ACTION_WEKA_COMPUTE_INFO_GAIN_ANNOTATIONS:     wekaIGAnnotations();  return;
			case GCConstants.ACTION_WEKA_COMPUTE_INFO_GAIN_CLASSIFICATIONS: wekaIGClassifications(); return; 
			
			case GCConstants.ACTION_WEKA_EVAL_NAIVE_BAYES:   wekaNBExecute();               return;
			case GCConstants.ACTION_WEKA_EVAL_SMO:           wekaCrossValidateByDocumentSMO(); return; //wekaSMOExecute();              return;
			case GCConstants.ACTION_WEKA_SOA_EVAL_NAIVE_BAYES: wekaAnnotationNBExecute();   return;
			case GCConstants.ACTION_WEKA_SOA_EVAL_SMO:         wekaAnnotationSMOExecute();   return;
			case GCConstants.ACTION_WEKA_CREATE_CLASSIFIERS: wekaBuildClassifier();         return;
			case GCConstants.ACTION_WEKA_CLASSIFY_CURRENT:   wekaClassifyCurrentSentence(); return;
			case GCConstants.ACTION_WEKA_CURRENT_DOC_NAIVE_BAYES: wekaCurrentDocNB();       return;
			case GCConstants.ACTION_WEKA_CURRENT_DOC_SMO:         wekaCurrentDocSMO(_theInstanceLearner.getTrainedSentencesActual(), _currentDocument.getSentences());      return;	
			
			case GCConstants.ACTION_SOA_MARK_COMPLETE   : markSOAComplete(); return;
			case GCConstants.ACTION_SOA_CONVERT         : convertSecurityObjectiveAnnotations(); return;
			case GCConstants.ACTION_SOA_CONFIDENTIALITY : performSOAMark(ae.getActionCommand()); return;
			case GCConstants.ACTION_SOA_INTEGRITY       : performSOAMark(ae.getActionCommand()); return;
			case GCConstants.ACTION_SOA_AVAILABILITY    : performSOAMark(ae.getActionCommand()); return;
			case GCConstants.ACTION_SOA_AC_IDENTITY     : performSOAMark(ae.getActionCommand()); return;
			case GCConstants.ACTION_SOA_AUTHORIZATION   : performSOAMark(ae.getActionCommand()); return;
			case GCConstants.ACTION_SOA_LOGGING         : performSOAMark(ae.getActionCommand()); return;
			case GCConstants.ACTION_SOA_NONREPUDIATION  : performSOAMark(ae.getActionCommand()); return;
			case GCConstants.ACTION_SOA_PRIVACY         : performSOAMark(ae.getActionCommand()); return;
			case GCConstants.ACTION_SOA_DATABASE        : performSOAMark(ae.getActionCommand()); return;
			
			
			case GCConstants.ACTION_CLASS_MARK_COMPLETE  : markClassificationsComplete(); return;
			case GCConstants.ACTION_CLASS_MARK_DB_FT     : markClassificationsDBFTComplete(); return;
			case GCConstants.ACTION_CLASS_MARK_ACF_DB_FT : markClassificationsACFDBFTComplete(); return;
			
			
		}
			
				
		//All of the commands below cannot operate unless their is data loaded
		if (_currentDocument.getNumberOfSentences() < 1) {
			_mainFrame.setStatusMessage("Operation ("+ae.getActionCommand()+") invalid. No data.");
			return;
		}
		
		int newSentenceNumber = _currentSentenceNumber;
		newSentenceNumber = _mainFrame.getSentenceClassifer().getSentenceTable().convertRowIndexToView(newSentenceNumber);
		if (ae.getActionCommand().equals("First")) {
			if (newSentenceNumber == 0) { return; } // already at the start
			newSentenceNumber = 0;	
		}
		else if (ae.getActionCommand().equals("Previous")) {
			if (newSentenceNumber == 0) { return; } // already at the start
			newSentenceNumber--;
		}
		else if (ae.getActionCommand().equals("Next")) {
			int max = _currentDocument.getSentences().size() -1;
			if (newSentenceNumber == max) { return; } // already at the end
			
			newSentenceNumber++;
		}
		else if (ae.getActionCommand().equals("Last")) {
				int max = _currentDocument.getSentences().size() -1;
				if (newSentenceNumber == max) { return; } // already at the end
				
				newSentenceNumber=max;
		}
		this.setCurrentSentence(newSentenceNumber, true,true);
	}
	
	private void loadParser() {
		_mainFrame.setStatusMessage("Starting to load Stanford Parser...  Wait until complete.");
        Runnable r = new  Runnable() {
             public void run() {
                  createPipeline();
                 _mainFrame.setStatusMessage("Stanford Parser loaded.");
             }   };
        (new Thread(r)).start();
	}

	private void loadTextDocument() {
		JFileChooser fileChooser = new JFileChooser();
		int returnVal = fileChooser.showOpenDialog(_mainFrame);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File f = fileChooser.getSelectedFile();
			try {
				loadAndParseDocument(f.getAbsolutePath());
				_currentDocumentID = "unknown";
				_mainFrame.setStatusMessage("Document loaded: "+f.getAbsolutePath());
			}
			catch (Exception e) {
				_mainFrame.setStatusMessage("Unable to load document from "+f.getAbsolutePath());
				System.err.println("Unable to load document: "+e);
				e.printStackTrace();
			}
		}
	}
	
	private void loadJSONDocumentFromFile(File file) throws Exception {
		_currentDocument = NLDocument.readFromJSONFile(file);	
		_currentFileLocation = file;
		
		if (_currentDocument.getNumberOfSentences() > 0) {
			_currentDocumentID = _currentDocument.getElementAt(0).getDocumentID();
		}
		
		setViewWithDocument();
	}	
	
	private void loadJSONDocument() {
		JFileChooser fileChooser = new JFileChooser();
		int returnVal = fileChooser.showOpenDialog(_mainFrame);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File f = fileChooser.getSelectedFile();
			try {
				loadJSONDocumentFromFile(f);
				_mainFrame.setStatusMessage("Document loaded: "+f.getAbsolutePath());
			}
			catch (Exception e) {
				_mainFrame.setStatusMessage("Unable to load document from "+f.getAbsolutePath());
				System.err.println("Unable to load document: "+e);
				e.printStackTrace();
			}
		}
	}
	
	private void appendJSONDocument() {
		JFileChooser fileChooser = new JFileChooser();
		int returnVal = fileChooser.showOpenDialog(_mainFrame);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File f = fileChooser.getSelectedFile();
			try {
				NLDocument newDoc = NLDocument.readFromJSONFile(f);	
				_currentDocument.getSentences().addAll(newDoc.getSentences());
				_mainFrame.setStatusMessage("Document appended: "+f.getAbsolutePath());
			}
			catch (Exception e) {
				_mainFrame.setStatusMessage("Unable to append document from "+f.getAbsolutePath());
				System.err.println("Unable to append document: "+e);
				e.printStackTrace();
			}
		}		
		
		
	}
	
	
	
	public void loadSerialDocumentFromFile(File file) throws Exception {
		_currentDocument = NLDocument.readFromSerialFile(file);	
		_currentFileLocation = file;
		
		if (_currentDocument.getNumberOfSentences() > 0) {
			_currentDocumentID = _currentDocument.getElementAt(0).getDocumentID();
		}
		
		setViewWithDocument();
	}		

	private void loadSerialDocument() {
		JFileChooser fileChooser = new JFileChooser();
		int returnVal = fileChooser.showOpenDialog(_mainFrame);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File f = fileChooser.getSelectedFile();
			try {
				loadSerialDocumentFromFile(f);
				_mainFrame.setStatusMessage("Document loaded: "+f.getAbsolutePath());
			}
			catch (Exception e) {
				_mainFrame.setStatusMessage("Unable to load document from "+f.getAbsolutePath());
				System.err.println("Unable to load document: "+e);
				e.printStackTrace();
			}
		}
	}	
	
	private void saveDocumentAsJSON() {
		JFileChooser fileChooser;
		if (_currentFileLocation != null) {
			fileChooser = new JFileChooser(_currentFileLocation);
		}
		else {
			fileChooser = new JFileChooser();
		}
		int returnVal = fileChooser.showSaveDialog(_mainFrame);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File f = fileChooser.getSelectedFile();
			try {
				_currentDocument.writeToJSONFile(f);
				_currentFileLocation = f;
				_mainFrame.setStatusMessage("Document saved as JSON: "+f.getAbsolutePath());
			}
			catch (Exception e) {
				_mainFrame.setStatusMessage("Unable to save JSON document to "+f.getAbsolutePath());
				System.err.println("Unable to save JSON document: "+e);
			}
		}						
	}	
	
	private void saveDocumentAsSerial() {
		JFileChooser fileChooser;
		if (_currentFileLocation != null) {
			fileChooser = new JFileChooser(_currentFileLocation);
		}
		else {
			fileChooser = new JFileChooser();
		}
		int returnVal = fileChooser.showSaveDialog(_mainFrame);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File f = fileChooser.getSelectedFile();
			try {
				_currentDocument.writeToSerialFile(f);
				_currentFileLocation = f;
				_mainFrame.setStatusMessage("Document saved as serial: "+f.getAbsolutePath());
			}
			catch (Exception e) {
				_mainFrame.setStatusMessage("Unable to save serial document to "+f.getAbsolutePath());
				System.err.println("Unable to save serial document: "+e);
			}
		}						
	}
	
	private void exportSOASentences() {
		JFileChooser fileChooser;
		if (_currentFileLocation != null) {
			fileChooser = new JFileChooser(_currentFileLocation);
		}
		else {
			fileChooser = new JFileChooser();
		}
		int returnVal = fileChooser.showSaveDialog(_mainFrame);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File f = fileChooser.getSelectedFile();
			try {
				ReportController.getTheReportController().exportSOA(f);
				_mainFrame.setStatusMessage("SOAs exported: "+f.getAbsolutePath());
			}
			catch (Exception e) {
				_mainFrame.setStatusMessage("Unable to export SOAs to "+f.getAbsolutePath());
				System.err.println("Unable to export SOAs: "+e);
				e.printStackTrace();
			}
		}	
		//todo implement
	}
	
	
	private void compareJSON() {
		JFileChooser fileChooser = new JFileChooser();
		int returnVal = fileChooser.showOpenDialog(_mainFrame);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File f = fileChooser.getSelectedFile();
			
			//String outputFileName = f.getParent() +"/differences"+ edu.ncsu.csc.nl.util.Utility.getCurrentTimeStamp()+".txt";
						
			try {
				//java.io.PrintStream out = new java.io.PrintStream(outputFileName);
				
				NLDocument compareDocument = NLDocument.readFromJSONFileWithoutParsing(f);	
				_mainFrame.setStatusMessage("Compare document loaded: "+f.getAbsolutePath());
				
				int exactMatches = 0;
				for (Sentence s1: _currentDocument.getSentences()) {
					for (Sentence s2: compareDocument.getSentences()) {
						String text1 = s1.getSentence().toLowerCase().replaceAll("\\s+","");
						String text2 = s2.getSentence().toLowerCase().replaceAll("\\s+","");
						if (text1.equals(text2)) { exactMatches++; }
					}
				}
				System.out.println(exactMatches);
				
				
				/*
				int diffCount = _currentDocument.produceSecurityObjectAnnotationReport(compareDocument, out, false);
				diffCount += compareDocument.produceSecurityObjectAnnotationReport(_currentDocument, out, true);
				out.close();
				_mainFrame.setStatusMessage(diffCount+": differences found.  Compare complete with "+f.getAbsolutePath());
				*/
			}
			catch (Exception e) {
				_mainFrame.setStatusMessage("Unable to compare document from "+f.getAbsolutePath());
				System.err.println("Unable to compare document: "+e);
				e.printStackTrace();
			}
		}		
	}
	
	private void computeKappaForAnnotations() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setDialogTitle("First File to Compare");
		int returnVal = fileChooser.showOpenDialog(_mainFrame);
		File firstFile;
		File secondFile;
		
		if (returnVal != JFileChooser.APPROVE_OPTION) {
			return;
		}
		firstFile = fileChooser.getSelectedFile();
		
		fileChooser.setDialogTitle("Second File to Compare");
		returnVal = fileChooser.showOpenDialog(_mainFrame);
		if (returnVal != JFileChooser.APPROVE_OPTION) {
			return;
		}
		secondFile = fileChooser.getSelectedFile();
				
		try {
			NLDocument firstDocument = NLDocument.readFromJSONFileWithoutParsing(firstFile);	
			_mainFrame.setStatusMessage("Compare document loaded: "+firstFile.getAbsolutePath());
			
			NLDocument secondDocument = NLDocument.readFromJSONFileWithoutParsing(secondFile);	
			_mainFrame.setStatusMessage("Compare document loaded: "+secondFile.getAbsolutePath());
			
			//firstDocument.produceSecurityObjectAnnotationKappaReport(secondDocument, false);
			firstDocument.produceSecurityRelatedKappaReport(secondDocument, false);

			
			//secondDocument.produceSecurityObjectAnnotationReport(firstDocument, out, true);
			_mainFrame.setStatusMessage("Kappa Score Generated");
		}
		catch (Exception e) {
			_mainFrame.setStatusMessage("Unable to compute Kappa Scores");
			System.err.println("Unable to compare document: "+e);
			e.printStackTrace();
		}		
	}

	private void generatAnnotationsReport() {
		JFileChooser fileChooser = new JFileChooser();
		
		File[] file = new File[6];
		NLDocument[] document = new NLDocument[6];
		
		int numDocuments = 6;
		
		for (int i=1;i<=numDocuments;i++) {
			fileChooser.setDialogTitle("Select File #" + (i+1));
						
			if (fileChooser.showOpenDialog(_mainFrame) != JFileChooser.APPROVE_OPTION) {
				return;
			}
			file[i-1] = fileChooser.getSelectedFile();
		}
	
				
		try {
			String outputFileName = "C:\\Users\\Adminuser\\Dropbox\\Identifying_Security_Requirements_RE2014\\sentence_"+ edu.ncsu.csc.nl.util.Utility.getCurrentTimeStamp()+".txt";
			java.io.PrintStream out = new java.io.PrintStream(outputFileName);
			
			for (int i = 0;i< numDocuments; i++) {
				document[i] = NLDocument.readFromJSONFileWithoutParsing(file[i]);
			}
			_mainFrame.setStatusMessage("All Documents loaded");

			for (SecurityObjective so: SecurityObjective.getMinimalListForClassification()) {
				out.print(so);
				out.print("\t");
			}
			System.out.println("Security Related\tDocument\tLine\tSentence");
			for (int i = 0;i< numDocuments; i++) {
				int count = 0;
				for (Sentence s: document[i].getSentences()) {
					count++;
					for (SecurityObjective so: SecurityObjective.getMinimalListForClassification()) {
						out.print(s.hasSecurityObjectiveAnnotation(so));
						out.print("\t");
					}
					out.print( s.getNumberOfSecurityObjectiveAnnotations() >0 );
					out.print("\t");
					out.print(s.getDocumentID());
					out.print("\t");
					out.print(count);
					out.print("\t");
					out.print(s.getSentence());
					
					out.println();
				}
			}
			out.close();
			_mainFrame.setStatusMessage("output file produced");
			
		}
		catch (Exception e) {
			_mainFrame.setStatusMessage("Unable to compute Kappa Scores");
			System.err.println("Unable to compare document: "+e);
			e.printStackTrace();
		}		
	}	
	
	
	
	private void exportARFF() {
		JFileChooser fileChooser = new JFileChooser();
		int returnVal = fileChooser.showSaveDialog(_mainFrame);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File f = fileChooser.getSelectedFile();
			
			WekaCreatorOptions wo = new WekaCreatorOptions();
			wo.setExportSentenceAsString(true);
			wo.setNERSentenceIndicators(false);
			wo.setWordType(WordType.LEMMA);
			wo.setUseOriginalSentence(false);
			
			ArrayList<Sentence> sentences = _currentDocument.getSentences();

			Object[] options = {"Classifications", "Annotations" };
			int n = JOptionPane.showOptionDialog(null,"Which set of attributes to include?", "Select Classifications or Annotations", JOptionPane.YES_NO_OPTION,
					                             JOptionPane.QUESTION_MESSAGE,null,options,  options[1]);
			Instances dataSet;
			if (n == JOptionPane.NO_OPTION) {
				dataSet = (new WekaCreator()).createWekaInstancesForSecurityObjectiveAnnotations(this.getCurrentDocumentID(),sentences,wo,SecurityObjective.getListForClassification());
			}
			else {
				dataSet = (new WekaCreator()).createWekaInstancesForClassifications(this.getCurrentDocumentID(),sentences,_classificationAttributes.getAttributeList(),wo);
			}
			
			try {
				WekaUtility.exportInstancesAsARFF(dataSet, f);
				_mainFrame.setStatusMessage("Data exported "+f.getAbsolutePath());
			}
			catch (Exception e) {
				_mainFrame.setStatusMessage("Unable to export data to "+f.getAbsolutePath());
				System.err.println("Unable export ARFF File: "+e);
			}
		}		
	}
	
	private void newDocument() {
		_currentFileLocation = null;
	    _currentDocument = new NLDocument();
	    AccessControlRelationManager.getTheAccessControlRelationManager().reset(true);
	    DatabaseRelationManager.getTheDatabaseRelationManager().reset(true);
	    _mainFrame.getSentenceClassifer().setCurrentDocument(_currentDocument);
	    _mainFrame.setStatusMessage("New document established.");
	}
		
	public void parseLine(String text) {
		if (_pipeline == null) {
			_mainFrame.setStatusMessage("Parser must be loaded first.");
		}
		else {
			_currentDocument.addSentenceAndParse(_pipeline, text);
			int max = _currentDocument.getSentences().size() -1;
			//_currentSentenceNumber=max;
			this.setCurrentSentence(max,true,false);
		}		
	}
	
	// this is currently called by some hacked up code in SentenceDisplayPanel to replace a sentence.  TODO: Refactor design
	public void setCurrentSentenceNoAction(int modelIndex, boolean viewPositionPassed) {
		_currentSentenceNumber = modelIndex;
	}
	
	/**
	 * 
	 * @param newNumber
	 * @param setSelection
	 * @param viewPositionPassed
	 */
	public void setCurrentSentence(int newNumber, boolean setSelection, boolean viewPositionPassed) {
		if (!viewPositionPassed) {
			newNumber = _mainFrame.getSentenceClassifer().getSentenceTable().convertRowIndexToModel(newNumber);
			_currentSentenceNumber = newNumber;
		}
		else {
			_currentSentenceNumber = _mainFrame.getSentenceClassifer().getSentenceTable().convertRowIndexToModel(newNumber);
		}
		//_currentSentenceNumber = newNumber;
		_currentDocument.getSentences().get(_currentSentenceNumber).processSentence(_currentDocument);
		_mainFrame.getSentenceClassifer().setCurrentSentence(_currentSentenceNumber,setSelection);  // this code throw an array out of bounds if not loaded/no data						
		_classifierDialog.setCurrentClassificationItem(_currentDocument.getSentences().get(_currentSentenceNumber),_currentDocument.getSentences().get(_currentSentenceNumber));
		_accessControlDialog.setCurrentSentence(_currentDocument.getSentences().get(_currentSentenceNumber));
		_databaseDialog.setCurrentSentence(_currentDocument.getSentences().get(_currentSentenceNumber));
		_securityObjectiveAnnotationController.setCurrentSentence(_currentDocument.getSentences().get(_currentSentenceNumber));
	}
	
	//TODO: next version of the application needs to deal with the fact we have multiple groups of classifications....
	public void markCurrentSentenceAsTrained() {
		_currentDocument.getSentences().get(_currentSentenceNumber).processTrainedSentence();
	}
	
	public void markCurrentSentenceAsAccessDefined() {
		_currentDocument.getSentences().get(_currentSentenceNumber).processSentenceForAccessDefined();
	}	
	
	public void markCurrentSentenceAsSecurityObjectiveAnnotationDefined() {
		_currentDocument.getSentences().get(_currentSentenceNumber).processSentenceForSecurityObjectAnnotationsDefined();
	}	
		
	public void markCurrentSentenceAsDatabaseComplete() {
		_currentDocument.getSentences().get(_currentSentenceNumber).processSentenceForDatabaseComplete();
	}		
	
	
	public void gotoNextSOAUndefinedInDocument() {
		ArrayList<Sentence> sentences = _currentDocument.getSentences();
		
		int startingPosition = _mainFrame.getSentenceClassifer().getSentenceTable().convertRowIndexToView(_currentSentenceNumber) + 1;
		int endingPosition   = sentences.size();
			
		boolean reachedEnd = false;
		boolean found      = false;
			
		for (int viewIndex = startingPosition; viewIndex<= endingPosition; viewIndex++) {
			if (viewIndex == endingPosition) {
				if (reachedEnd == true) { // we've already reached the end once, stop searching
					break;
				}
				// we've reached the end for the first time, wrap the search to the start, but stop on the current sentence
				reachedEnd = true;
				viewIndex = -1;
				endingPosition = _mainFrame.getSentenceClassifer().getSentenceTable().convertRowIndexToView(_currentSentenceNumber);
				//continue;
			}
			else {
				int modelIndex = _mainFrame.getSentenceClassifer().getSentenceTable().convertRowIndexToModel(viewIndex);
				if (sentences.get(modelIndex).isSecurityObjectiveAnnotationsDefined() == false) {
					this.setCurrentSentence(viewIndex, true,true);
					found = true;
					break;
				}
			}
		}
		if (!found) {
			_mainFrame.setStatusMessage("All sentences have security objective annotations defined");
		}	
	}
	
	public void gotoNextAccessUndefinedInDocument() {
		ArrayList<Sentence> sentences = _currentDocument.getSentences();
				
		int startingPosition = _mainFrame.getSentenceClassifer().getSentenceTable().convertRowIndexToView(_currentSentenceNumber) + 1;
		int endingPosition   = sentences.size();
			
		boolean reachedEnd = false;
		boolean found      = false;
			
		for (int viewIndex = startingPosition; viewIndex<= endingPosition; viewIndex++) {
			if (viewIndex == endingPosition) {
				if (reachedEnd == true) { // we've already reached the end once, stop searching
					break;
				}
				// we've reached the end for the first time, wrap the search to the start, but stop on the current sentence
				reachedEnd = true;
				viewIndex = -1;
				endingPosition = _mainFrame.getSentenceClassifer().getSentenceTable().convertRowIndexToView(_currentSentenceNumber);
				//continue;
			}
			else {
				int modelIndex = _mainFrame.getSentenceClassifer().getSentenceTable().convertRowIndexToModel(viewIndex);
				if (sentences.get(modelIndex).isAccessControlDefined() == false) { // && sentences.get(modelIndex).hasBooleanClassification(GCConstants.CLASSIFICATION_ACCESS_CONTROL_FUNCTIONAL)) {
					this.setCurrentSentence(viewIndex, true,true);
					found = true;
					break;
				}
			}
		}
		if (!found) {
			_mainFrame.setStatusMessage("No sentences located for acces control control that are not yet defined");
		}
	}
	
	public void gotoNextUnclassifiedInDocument() {
		ArrayList<Sentence> sentences = _currentDocument.getSentences();
				
		int startingPosition = _mainFrame.getSentenceClassifer().getSentenceTable().convertRowIndexToView(_currentSentenceNumber) + 1;
		int endingPosition   = sentences.size();
			
		boolean reachedEnd = false;
		boolean found      = false;
			
		for (int viewIndex = startingPosition; viewIndex<= endingPosition; viewIndex++) {
			if (viewIndex == endingPosition) {
				if (reachedEnd == true) { // we've already reached the end once, stop searching
					break;
				}
				// we've reached the end for the first time, wrap the search to the start, but stop on the current sentence
				reachedEnd = true;
				viewIndex = -1;
				endingPosition = _mainFrame.getSentenceClassifer().getSentenceTable().convertRowIndexToView(_currentSentenceNumber);
				//continue;
			}
			else {
				int modelIndex = _mainFrame.getSentenceClassifer().getSentenceTable().convertRowIndexToModel(viewIndex);
				if (sentences.get(modelIndex).isTrained() == false) {
					this.setCurrentSentence(viewIndex, true,true);
					found = true;
					break;
				}
			}
		}
		if (!found) {
			_mainFrame.setStatusMessage("No untrained sentences located");
		}
	}
	
	public void gotoNextDatabaseIncompleteInDocument() {
		ArrayList<Sentence> sentences = _currentDocument.getSentences();
				
		int startingPosition = _mainFrame.getSentenceClassifer().getSentenceTable().convertRowIndexToView(_currentSentenceNumber) + 1;
		int endingPosition   = sentences.size();
			
		boolean reachedEnd = false;
		boolean found      = false;
			
		for (int viewIndex = startingPosition; viewIndex<= endingPosition; viewIndex++) {
			if (viewIndex == endingPosition) {
				if (reachedEnd == true) { // we've already reached the end once, stop searching
					break;
				}
				// we've reached the end for the first time, wrap the search to the start, but stop on the current sentence
				reachedEnd = true;
				viewIndex = -1;
				endingPosition = _mainFrame.getSentenceClassifer().getSentenceTable().convertRowIndexToView(_currentSentenceNumber);
				//continue;
			}
			else {
				int modelIndex = _mainFrame.getSentenceClassifer().getSentenceTable().convertRowIndexToModel(viewIndex);
				if (sentences.get(modelIndex).isDatabaseComplete() == false) {
					this.setCurrentSentence(viewIndex, true,true);
					found = true;
					break;
				}
			}
		}
		if (!found) {
			_mainFrame.setStatusMessage("No sentences located with database relations incomplete");
		}
	}	
	

	public void findWord(boolean useLastSearch) {
		String value = _lastSearchValue;
		
		if (useLastSearch == false) {
			value = JOptionPane.showInputDialog(null,"Find:",_lastSearchValue);
		}
		
		if (value == null || value.length() == 0) {
			_mainFrame.setStatusMessage("Search cancelled");
			return;
		}
		else {	
			_lastSearchValue = value;
			
			ArrayList<Sentence> sentences = _currentDocument.getSentences();
			
			int endingPosition   = sentences.size();			
			int startingPosition = _mainFrame.getSentenceClassifer().getSentenceTable().convertRowIndexToView(_currentSentenceNumber) +1;
			
			boolean reachedEnd = false;
			boolean found      = false;
			
			for (int viewIndex = startingPosition; viewIndex<= endingPosition; viewIndex++) {
				if (viewIndex == endingPosition) {
					if (reachedEnd == true) { // we've already reached the end once, stop searching
						break;
					}
					// we've reached the end for the first time, wrap the search to the start, but stop on the current sentence
					reachedEnd = true;
					viewIndex = -1;
					endingPosition = _mainFrame.getSentenceClassifer().getSentenceTable().convertRowIndexToView(_currentSentenceNumber);
					//continue;
				}
				else {
					int modelIndex =  _mainFrame.getSentenceClassifer().getSentenceTable().convertRowIndexToModel(viewIndex);
					if (sentences.get(modelIndex).getSentence().contains(value)) {
						this.setCurrentSentence(viewIndex, true, true);
						found = true;
						break;
					}
				}
			}
			if (!found) {
				_mainFrame.setStatusMessage("Not found: "+value);
			}
		}
		
	}
	
	//yes, there's control coupling, but I'd rather have that than two separate methods
	private void saveLearner(boolean serialized) {
		JFileChooser fileChooser = new JFileChooser();
		int returnVal = fileChooser.showSaveDialog(_mainFrame);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File f = fileChooser.getSelectedFile();
			try {
				if (serialized) {
					_theInstanceLearner.saveToSerializedObjectFile(f);
				}
				else {
					_theInstanceLearner.saveToFile(f);
				}
				_mainFrame.setStatusMessage("Instance learner saved to "+f.getAbsolutePath());
			}
			catch (Exception e) {
				_mainFrame.setStatusMessage("Unable to save learner to "+f.getAbsolutePath());
				System.err.println("Unable to save learner: "+e);
			}
		}
	}
	
	
	private void loadLearner(boolean serialized) {
		JFileChooser fileChooser = new JFileChooser();
		int returnVal = fileChooser.showOpenDialog(_mainFrame);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File f = fileChooser.getSelectedFile();
			try {
				if (serialized) {
					_theInstanceLearner.loadFromSerializedFile(f);
				}
				else {
					_theInstanceLearner.loadFromFile(f);
				}

				_mainFrame.setStatusMessage("Instance Learner loaded from "+f.getAbsolutePath());
			}
			catch (Exception e) {
				_mainFrame.setStatusMessage("Unable to load instance learner from "+f.getAbsolutePath());
				System.err.println("Unable to load instance learner: "+e);
			}		
		}		
	}	
	
	private void setKforInstanceLearner() {
		String value = JOptionPane.showInputDialog(null,"<html>Current <i>k</i> for the instance learner: "+_masterClassifier.getCurrentKValueForInstanceLearner()+"<p>Enter  new <i>k</i>:","Change K", JOptionPane.QUESTION_MESSAGE);
		if (value == null) {
			_mainFrame.setStatusMessage("k change cancelled");
		}
		else {	
			try {
				int temp = Integer.parseInt(value);
				if (temp <1) {
					_mainFrame.setStatusMessage("k must be a positive integer");
				}
				else {
					_masterClassifier.setCurrentKValueForInstanceLearner(temp);
					_mainFrame.setStatusMessage("k changed to "+value);
				}
			}
			catch (Throwable t) {
				_mainFrame.setStatusMessage("Unable to change k - "+t);
			}
		}
	}
	
	public int getKForInstanceLearner() { //TODO: move
		return _masterClassifier.getCurrentKValueForInstanceLearner();
	}
	
	private void gotoLineInDocument() {
		String value = JOptionPane.showInputDialog(null,"Enter viewable line #","Select Sentence", JOptionPane.QUESTION_MESSAGE);
		try {
			int viewIndex = Integer.parseInt(value);
			if (viewIndex < 0 || viewIndex >  (_currentDocument.getNumberOfSentences()-1)) {
				_mainFrame.setStatusMessage("Bad sentence number (not in range): "+viewIndex);
				return;
			}
			this.setCurrentSentence(viewIndex, true,true);
		}
		catch (Throwable t) {
			_mainFrame.setStatusMessage("Enter an integer for the selected selected.");
			return;			
		}				
	}
	
	private void handleOverridePOS() {
		_posOverrideEditor.open();
	}
	
	
	private void markClassifiedAndMove() {
		_currentDocument.getSentences().get(_currentSentenceNumber).processTrainedSentence();
		
		
		int max = _currentDocument.getSentences().size() -1;
		
		int newSentenceNumber = _mainFrame.getSentenceClassifer().getSentenceTable().convertRowIndexToView(_currentSentenceNumber);

		
		if (newSentenceNumber == max) { return; } // already at the end		
		this.setCurrentSentence(newSentenceNumber+1, true,true);
	}
	

	@Override
	public void eventOccured(NLPEventType eventType, NLPEvent event) {
		// TODO Auto-generated method stub
		switch (eventType) {
			case MARK_CLASSIFIED_AND_MOVE: markClassifiedAndMove();  return;
			case MOVED_TO_SENTENCE: return;
			default: System.out.println("Event not specified");
		
		
		}
		
	}

	public void trainCurrentSentence() {
		if (_currentSentenceNumber >=0 ) {
			_currentDocument.getElementAt(_currentSentenceNumber).processTrainedSentence();
		}
	}
	
	public void unTrainCurrentSentence() {
		if (_currentSentenceNumber >=0 ) {
			_currentDocument.getElementAt(_currentSentenceNumber).unTrainSentence();
		}
	}
	
	public String getCurrentDocumentID() {
		return _currentDocumentID;
	}
	
	public void establishCurrentDocumentID() {
		
		String value = JOptionPane.showInputDialog(null,"<html>Current document ID: <i>"+_currentDocumentID+"</i><p>Enter document ID:","Document ID", JOptionPane.QUESTION_MESSAGE);
		if (value == null || value.trim().length()==0) {
			_mainFrame.setStatusMessage("Document ID change cancelled");
		}
		else {	
			_currentDocumentID = value;
			
			for (Sentence s: _currentDocument.getSentences()) {
				s.setDocumentID(_currentDocumentID);
			}
			_mainFrame.setStatusMessage("Document ID changed to "+_currentDocumentID);
		}
	}
	
	public void markClassificationsComplete() {
		if (_currentSentenceNumber <0) {
			this.setStatusMessage("No current sentence");
			return;
		}
		Sentence currentSentence = this._currentDocument.getElementAt(_currentSentenceNumber);
		
		currentSentence.processTrainedSentence();
		
		this.setStatusMessage("Sentence marked complete for classifications.");
        this.gotoNextUnclassifiedInDocument();
	}
	
	public void markClassificationsDBFTComplete() {
		if (_currentSentenceNumber <0) {
			this.setStatusMessage("No current sentence");
			return;
		}
		Sentence currentSentence = this._currentDocument.getElementAt(_currentSentenceNumber);
		
		HashMap<String, ClassificationType> classifications = currentSentence.getClassifications();
		
		classifications.put("database design",new BooleanClassification(BooleanType.getBooleanType(true),Source.USER));
		classifications.put("functional",new BooleanClassification(BooleanType.getBooleanType(true),Source.USER));
		
		
		//_classificationAttributes.
		
		markClassificationsComplete();
	}
	
	public void markClassificationsACFDBFTComplete() {
		if (_currentSentenceNumber <0) {
			this.setStatusMessage("No current sentence");
			return;
		}
		Sentence currentSentence = this._currentDocument.getElementAt(_currentSentenceNumber);
		HashMap<String, ClassificationType> classifications = currentSentence.getClassifications();
		
		classifications.put("access control:functional",new BooleanClassification(BooleanType.getBooleanType(true),Source.USER));
		classifications.put("database design",new BooleanClassification(BooleanType.getBooleanType(true),Source.USER));
		classifications.put("functional",new BooleanClassification(BooleanType.getBooleanType(true),Source.USER));		
		
		markClassificationsComplete();
	}
	
	public void markSOAComplete() {
		if (_currentSentenceNumber <0) {
			this.setStatusMessage("No current sentence");
			return;
		}
		Sentence currentSentence = this._currentDocument.getElementAt(_currentSentenceNumber);
		
		currentSentence.processSentenceForSecurityObjectAnnotationsDefined();
		//_securityObjectiveAnnotationController.enableButtonsFromSOADefined(true);  //TODO need to set these buttons.
       this.setStatusMessage("Sentence marked complete for SOA.");
       this.gotoNextSOAUndefinedInDocument();
	}
	
	public void markSOAForACDL() {
		//TODO: What if there is no current sentence
		if (_currentSentenceNumber <0) { return;}
		Sentence currentSentence = this._currentDocument.getElementAt(_currentSentenceNumber);
		ArrayList<SecurityObjectiveAnnotation> newList = new ArrayList<SecurityObjectiveAnnotation>();
		newList.add(new SecurityObjectiveAnnotation(SecurityObjective.ACCESS_CONTROL_AUTHORIZATION, SecurityImpact.LOW, SecurityMitigation.PREVENTION, false));
		newList.add(new SecurityObjectiveAnnotation(SecurityObjective.DATABASE, SecurityImpact.LOW, SecurityMitigation.PREVENTION, false));
		newList.add(new SecurityObjectiveAnnotation(SecurityObjective.LOGGING, SecurityImpact.LOW, SecurityMitigation.DETECTION, false));
		newList.add(new SecurityObjectiveAnnotation(SecurityObjective.CONFIDENTIALITY, SecurityImpact.LOW, SecurityMitigation.PREVENTION, false));
		
		currentSentence.setSecurityObjectiveAnnotations(newList);
		currentSentence.processSentenceForSecurityObjectAnnotationsDefined();
		//_securityObjectiveAnnotationController.enableButtonsFromSOADefined(true);  //TODO need to set these buttons.
        this.setStatusMessage("Sentence marked complete for SOA.");
        this.gotoNextSOAUndefinedInDocument();
	}
	
	public void performSOAMark(String actionCommand) {
		SecurityObjectiveAnnotation newSOA = null;
		
		switch (actionCommand) {
		case GCConstants.ACTION_SOA_CONFIDENTIALITY : newSOA = new SecurityObjectiveAnnotation(SecurityObjective.CONFIDENTIALITY, SecurityImpact.LOW, SecurityMitigation.PREVENTION, false); break;
		case GCConstants.ACTION_SOA_INTEGRITY       : newSOA = new SecurityObjectiveAnnotation(SecurityObjective.INTEGRITY, SecurityImpact.LOW, SecurityMitigation.PREVENTION, false); break;
		case GCConstants.ACTION_SOA_AVAILABILITY    : newSOA = new SecurityObjectiveAnnotation(SecurityObjective.AVAILABILITY, SecurityImpact.LOW, SecurityMitigation.PREVENTION, false); break;
		case GCConstants.ACTION_SOA_AC_IDENTITY     : newSOA = new SecurityObjectiveAnnotation(SecurityObjective.ACCESS_CONTROL_IDENTITY, SecurityImpact.LOW, SecurityMitigation.PREVENTION, false); break;
		case GCConstants.ACTION_SOA_AUTHORIZATION   : newSOA = new SecurityObjectiveAnnotation(SecurityObjective.ACCESS_CONTROL_AUTHORIZATION, SecurityImpact.LOW, SecurityMitigation.PREVENTION, false); break;
		case GCConstants.ACTION_SOA_LOGGING         : newSOA = new SecurityObjectiveAnnotation(SecurityObjective.LOGGING, SecurityImpact.LOW, SecurityMitigation.DETECTION, false); break;
		case GCConstants.ACTION_SOA_NONREPUDIATION  : newSOA = new SecurityObjectiveAnnotation(SecurityObjective.NON_REPUDITION, SecurityImpact.LOW, SecurityMitigation.PREVENTION, false); break;
		case GCConstants.ACTION_SOA_PRIVACY         : newSOA = new SecurityObjectiveAnnotation(SecurityObjective.PRIVACY, SecurityImpact.LOW, SecurityMitigation.PREVENTION, false); break;
		case GCConstants.ACTION_SOA_DATABASE        : newSOA = new SecurityObjectiveAnnotation(SecurityObjective.DATABASE, SecurityImpact.LOW, SecurityMitigation.PREVENTION, false); break;
		}		
		if (newSOA == null) {
			GCController.getTheGCController().setStatusMessage("Invalid SOAD mark command passed: "+actionCommand);
			return;
		}
		
		if (_currentSentenceNumber <0) {
			GCController.getTheGCController().setStatusMessage("No current sentence");
			return;
		}
		Sentence currentSentence = this._currentDocument.getElementAt(_currentSentenceNumber);
		
		currentSentence.replaceSecurityObjectiveAnnotation(newSOA);
		//_securityObjectiveAnnotationController.enableButtonsFromSOADefined(true);  //TODO need to set these buttons.
		NLPEventManager.getTheEventManager().sendEvent( NLPEventType.SENTENCE_DATA_CHANGE, new NLPEventSentenceDataEvent(currentSentence, "SOATable"));
		this._securityObjectiveAnnotationController.setCurrentSentence(currentSentence); // this "forces" the table to be redrawn for dialog box
        GCController.getTheGCController().setStatusMessage("Security obective annotation added: "+newSOA.getSecurityObjective());		
	}
	
	
	
	public void performCluster() {
		ClusterOptions co = ClusterOptions.establishOptionsFromUserFeedback();
		if (co.wasCancelled()) {
			this.setStatusMessage(co.getLastOperationMessage());
			return;
		}
		
		KMedoid kmCluster = new KMedoid(co);
		
		ArrayList<SentenceCluster> clusters = kmCluster.cluster(_currentDocument.getSentences());
		_currentDocument.setClusterIDs(clusters);
		
		int response = JOptionPane.showConfirmDialog(null, "Produce cluser report?", "Cluster Report", JOptionPane.QUESTION_MESSAGE, JOptionPane.YES_NO_OPTION);
		if (response == JOptionPane.YES_OPTION) {
			for (SentenceCluster sc: clusters) {
				System.out.println("====================================================");
				System.out.println("Center: "+ sc.getCenter());
				double totalDistance = sc.getTotalDistance(co.getSentenceDistance());
				System.out.println("Size: "+sc.getSize()+"    Distance: "+totalDistance+"    Avg Distance: "+(totalDistance/(sc.getSize()*sc.getSize())));
	
				Collections.sort( sc.getMembers());
				for (Sentence s:  sc.getMembers()) {
					//System.out.println(s.getAllBooleanClassificationsAsString()+"\t"+s);
					if (s.isTrained()) {
						System.out.println(s.getAllBooleanClassificationsAsString()+"\t"+s);			
					}
					else {
						System.out.println("UNTRAINED\t"+s);
					}
				}
			}
		}
	}
	
	public void performDocumentEvaluationForClassifications() {
		ConfusionMatrix totalMatrix = new ConfusionMatrix();
		for (ClassificationAttribute ca: _classificationAttributes.getAttributeList()) {
			//if (ca.getName().equals("other nonfunctional") || ca.getName().equals("database design")) { continue; }
			if (ca.getIncludeInEvaluation() == false) { continue; }	
		    // k, # of folds, threshold
			
			//ExpirementOptions options = ExpirementOptions.establishOptionsFromUserFeedback(numberOfTrainedSentences, GCController.getTheRBACController().getClassificationAttributes());
			//ExpirementOptions options = new ExpirementOptions( ca,ExpirementOptions.FOLD_RANDOM,  1, 10, 0.8, false, new LevenshteinSentenceAsWordsDistance(),StopWord.getListByName(StopWord.EMPTY));
			ExpirementOptions options = new ExpirementOptions( ca,ExpirementOptions.FOLD_RANDOM, 1, 10, 10.0, false, new LevenshteinSentenceAsWordsDistance(),StopWord.getListByName(StopWord.EMPTY));

			//ExpirementOptions options = new ExpirementOptions( ca,ExpirementOptions.FOLD_RANDOM, 1, 10, 10.0, false, new TreeRelationshipAsLevenshteinDistance(),StopWord.getListByName(StopWord.EMPTY));
			
			//CosineTermFreqIDFDistance ctfidf = new CosineTermFreqIDFDistance();
			//ctfidf.setInverseDocumentFrequncy(InstanceLearner.generateInverseDocumentFrequency(InstanceLearner.generateTermSentenceFrequency(false), false));
			//ExpirementOptions options = new ExpirementOptions(primaryClassification,ExpirementOptions.FOLD_RANDOM,  1, numberOfTrainedSentences, 1, false,ctfidf,StopWord.getListByName(StopWord.EMPTY));
			ConfusionMatrix attributeMatrix = _theInstanceLearner.performEvaluationForClassificationsByDocument(options); 
			_mainFrame.setStatusMessage("Self-evaluation generated for " + ca.getName());
			totalMatrix.add(attributeMatrix);
		}
		System.out.println("************************************************************");
		System.out.println("TOTAL:  "+ totalMatrix);

		System.out.println("Precision: "+ totalMatrix.getPrecision());
		System.out.println("Recall: "+ totalMatrix.getRecall());
		System.out.println("True negative rate: "+totalMatrix.getTrueNegativeRate());
		System.out.println("Accuracy: "+totalMatrix.getAccuracy());
		System.out.println("F-Measure:" +totalMatrix.getF1Measure());	
	}

	public void performDocumentEvaluationForAnnotations() {
		ConfusionMatrix totalMatrix = new ConfusionMatrix();
		for (SecurityObjective so: SecurityObjective.getMinimalListForClassification()) {
			
			ExpirementOptions options = new ExpirementOptions( so,ExpirementOptions.FOLD_RANDOM,  1, 10, 10.0, false, new LevenshteinSentenceAsWordsDistance(),StopWord.getListByName(StopWord.EMPTY));
			//CosineTermFreqIDFDistance ctfidf = new CosineTermFreqIDFDistance();
			//ctfidf.setInverseDocumentFrequncy(InstanceLearner.generateInverseDocumentFrequency(InstanceLearner.generateTermSentenceFrequency(false), false));
			//ExpirementOptions options = new ExpirementOptions(primaryClassification,ExpirementOptions.FOLD_RANDOM,  1, numberOfTrainedSentences, 1, false,ctfidf,StopWord.getListByName(StopWord.EMPTY));
			ConfusionMatrix attributeMatrix = _theInstanceLearner.performEvaluationForAnnotationsByDocuments(options); 
			_mainFrame.setStatusMessage("Self-evaluation generated for " + so);
			totalMatrix.add(attributeMatrix);
		}
		System.out.println("************************************************************");
		System.out.println("TOTAL:  "+ totalMatrix);

		System.out.println("Precision: "+ totalMatrix.getPrecision());
		System.out.println("Recall: "+ totalMatrix.getRecall());
		System.out.println("True negative rate: "+totalMatrix.getTrueNegativeRate());
		System.out.println("Accuracy: "+totalMatrix.getAccuracy());
		System.out.println("F-Measure:" +totalMatrix.getF1Measure());	
	}	
	
	public void performSelfEvaluationForAnnotations() { 
		ConfusionMatrix totalMatrix = new ConfusionMatrix();
		
		double precision = 0.0;
		double recall    = 0.0;
		double f1        = 0.0;
		double numberOfAttributesEvaluted = 0.0;
		
		//
		
		
		for (SecurityObjective so: SecurityObjective.getMinimalListForClassification()) {
			numberOfAttributesEvaluted++;
			//ExpirementOptions options = ExpirementOptions.establishOptionsFromUserFeedback(numberOfTrainedSentences, GCController.getTheRBACController().getClassificationAttributes());
			ExpirementOptions options = new ExpirementOptions(so,ExpirementOptions.FOLD_RANDOM,  3, 10, 10.0, false, new LevenshteinSentenceAsWordsDistance(),StopWord.getListByName(StopWord.EMPTY));
			//CosineTermFreqIDFDistance ctfidf = new CosineTermFreqIDFDistance();
			//ctfidf.setInverseDocumentFrequncy(InstanceLearner.generateInverseDocumentFrequency(InstanceLearner.generateTermSentenceFrequency(false), false));
			//ExpirementOptions options = new ExpirementOptions(primaryClassification,ExpirementOptions.FOLD_RANDOM,  1, numberOfTrainedSentences, 1, false,ctfidf,StopWord.getListByName(StopWord.EMPTY));
			ConfusionMatrix attributeMatrix = _theInstanceLearner.performEvaluationForAnnotationsByFolds(options); 
			
			//ConfusionMatrix attributeMatrix = _theInstanceLearner.performRandomEvaluation(options,true); 
			//ConfusionMatrix attributeMatrix = _theInstanceLearner.performRandomEvaluation(options,false); 
			
			_mainFrame.setStatusMessage("Self-evaluation generated for " + so.toString());
			totalMatrix.add(attributeMatrix);
			if (Double.isNaN(attributeMatrix.getPrecision())) {  // there aren't any results for this attribute so skip.  allows us to compute macro average
				numberOfAttributesEvaluted--;
			}
			else {
				precision += attributeMatrix.getPrecision();
				recall    += attributeMatrix.getRecall();
				f1        += attributeMatrix.getF1Measure();
			}
		}
		System.out.println("************************************************************");
		System.out.println("TOTAL:  "+ totalMatrix);

		System.out.println("Precision: "+ totalMatrix.getPrecision()+"\t"+ precision/numberOfAttributesEvaluted);
		System.out.println("Recall: "+ totalMatrix.getRecall() +"\t"+ recall/numberOfAttributesEvaluted);
		System.out.println("True negative rate: "+totalMatrix.getTrueNegativeRate());
		System.out.println("Accuracy: "+totalMatrix.getAccuracy());
		System.out.println("F-Measure:" +totalMatrix.getF1Measure() +"\t"+ f1/numberOfAttributesEvaluted);	
			
		_mainFrame.setStatusMessage("Self-evaluation completed");
	}	
	
	
	public void performSelfEvaluationForClassifications() { 
		ConfusionMatrix totalMatrix = new ConfusionMatrix();
		
		double precision = 0.0;
		double recall    = 0.0;
		double f1        = 0.0;
		double numberOfAttributesEvaluted = 0.0;
		
		//for (double d=0.5;d<=1.0;d=d+0.1) {
		//System.out.println ("START OF THRESHOLD: "+d);
			
		System.out.println("CLASSIFYING WITH " + LevenshteinSentenceAsWordsDistance.class.getName());
		for (ClassificationAttribute ca: _classificationAttributes.getAttributeList()) {
			//if (ca.getIncludeInEvaluation() == false && ca.getAbbreviation().equals("DB") == false) { continue; }		
			if (ca.getIncludeInEvaluation() == false) { continue;  }
					
			numberOfAttributesEvaluted++;
			//if (ca.getName().equals("functional") || ca.getName().equals("other nonfunctional") || ca.getName().equals("database design")) { continue; }
		    // k, # of folds, threshold
			
			//ExpirementOptions options = ExpirementOptions.establishOptionsFromUserFeedback(numberOfTrainedSentences, GCController.getTheRBACController().getClassificationAttributes());
			ExpirementOptions options = new ExpirementOptions( ca,ExpirementOptions.FOLD_RANDOM, 1, 10, 10.0, false, new LevenshteinSentenceAsWordsDistance(),StopWord.getListByName(StopWord.EMPTY));
			
			//ExpirementOptions options = new ExpirementOptions( ca,ExpirementOptions.FOLD_RANDOM, 1, 10, 10.0, false, new TreeRelationshipAsLevenshteinDistance(),StopWord.getListByName(StopWord.EMPTY));
			//ExpirementOptions options = new ExpirementOptions( ca,ExpirementOptions.FOLD_RANDOM, 1, 10, 10.0, false, new GraphWalkDistance(),StopWord.getListByName(StopWord.EMPTY));
			//ExpirementOptions options = new ExpirementOptions( ca,ExpirementOptions.FOLD_RANDOM, 1, 10, 10.0, false, new LevenshteinSentenceAsWordsDistance(),StopWord.getListByName(StopWord.EMPTY));
			
			//CosineTermFreqIDFDistance ctfidf = new CosineTermFreqIDFDistance();
			//ctfidf.setInverseDocumentFrequncy(_theInstanceLearner.generateInverseDocumentFrequency(_theInstanceLearner.generateTermSentenceFrequency(false), false));
			//ExpirementOptions options = new ExpirementOptions(ca,ExpirementOptions.FOLD_RANDOM,  1, 10, 10.0, false,ctfidf,StopWord.getListByName(StopWord.EMPTY));
			ConfusionMatrix attributeMatrix = _theInstanceLearner.performEvaluationForClassificationsByFolds(options); 
			
			//ConfusionMatrix attributeMatrix = _theInstanceLearner.performRandomEvaluation(options,true); 
			//ConfusionMatrix attributeMatrix = _theInstanceLearner.performRandomEvaluation(options,false); 
			
			_mainFrame.setStatusMessage("n-fold evaluation generated for " + ca.getName());
			totalMatrix.add(attributeMatrix);
			precision += attributeMatrix.getPrecision();
			recall    += attributeMatrix.getRecall();
			f1        += attributeMatrix.getF1Measure();
		}
		System.out.println("************************************************************");
		System.out.println("TOTAL:  "+ totalMatrix);

		System.out.println("Precision: "+ totalMatrix.getPrecision()+"\t"+ precision/numberOfAttributesEvaluted);
		System.out.println("Recall: "+ totalMatrix.getRecall() +"\t"+ recall/numberOfAttributesEvaluted);
		System.out.println("True negative rate: "+totalMatrix.getTrueNegativeRate());
		System.out.println("Accuracy: "+totalMatrix.getAccuracy());
		System.out.println("F-Measure:" +totalMatrix.getF1Measure() +"\t"+ f1/numberOfAttributesEvaluted);
		
		//System.out.println ("END OF THRESHOLD: "+d);
		//}

		_mainFrame.setStatusMessage("Self-evaluation completed");
	}
	
	public void performInternalNBEvaluation() {
		ConfusionMatrix totalMatrix = new ConfusionMatrix();
		
		double precision = 0.0;
		double recall    = 0.0;
		double f1        = 0.0;
		double numberOfAttributesEvaluted = 0.0;
		
		for (ClassificationAttribute ca: _classificationAttributes.getAttributeList()) {
			if (ca.getIncludeInEvaluation() == false) { continue; }		
			
			numberOfAttributesEvaluted++;
			
			ExpirementOptions options = new ExpirementOptions( ca,ExpirementOptions.FOLD_RANDOM, 1, 10, 10.0, false, new TreeRelationshipAsLevenshteinDistance(),StopWord.getListByName(StopWord.EMPTY));
			
			ConfusionMatrix attributeMatrix = _theInstanceLearner.evaluateWithInternalNaiveBayesByFolds(options); 
						
			_mainFrame.setStatusMessage("n-fold evaluation generated for " + ca.getName());
			totalMatrix.add(attributeMatrix);
			precision += attributeMatrix.getPrecision();
			recall    += attributeMatrix.getRecall();
			f1        += attributeMatrix.getF1Measure();
		}
		
		System.out.println("************************************************************");
		System.out.println("TOTAL:  "+ totalMatrix);

		System.out.println("Precision: "+ totalMatrix.getPrecision()+"\t"+ precision/numberOfAttributesEvaluted);
		System.out.println("Recall: "+ totalMatrix.getRecall() +"\t"+ recall/numberOfAttributesEvaluted);
		System.out.println("True negative rate: "+totalMatrix.getTrueNegativeRate());
		System.out.println("Accuracy: "+totalMatrix.getAccuracy());
		System.out.println("F-Measure:" +totalMatrix.getF1Measure() +"\t"+ f1/numberOfAttributesEvaluted);			
		
		
	}
	
	public void moveInstanceLearnerSentencesToCurrentDoc() {
		List<Document> documents = _theInstanceLearner.getTrainedSentences();
		for (Document d: documents) {
			_currentDocument.addSentence(d.sentence);
		}
	}
	
	public void openOptionsDialog() {
		if (_optionsDialog == null) {
			OptionsDialog od = new OptionsDialog();
			
			_optionsDialog = od;
		}
		_optionsDialog.setVisible(true);
	}
	
	public void renumberCurrentDocumentID() {
		//TODO: Add test case.  put new sentence in middle.  renumber and test.  Then check for if a column should be sorted.  then check referred to position
		
		HashMap<Double, Double> mapFromOldToNewPositions = new HashMap<Double,Double>(); 
		
		int[] newModelIndexes = new int[this._currentDocument.getNumberOfSentences()];
		int size = newModelIndexes.length;
		
		for (int viewIndex = 0; viewIndex < size; viewIndex++) {
			newModelIndexes[viewIndex] = _mainFrame.getSentenceClassifer().getSentenceTable().convertRowIndexToModel(viewIndex);
		}
		for (int i=0; i< size; i++) {
			mapFromOldToNewPositions.put(_currentDocument.getElementAt(newModelIndexes[i]).getOriginalSentencePosition(), (double) (i+1));
			_currentDocument.getElementAt(newModelIndexes[i]).setOriginalSentencePosition(i+1);
		}
		_currentDocument.sendTableChangedEvent(new TableModelEvent(_currentDocument));

		//Now, need to renumber the sentences
		for (int i=0; i< size; i++) {
			Sentence s = _currentDocument.getElementAt(i);
			if (s.getReferredToSentence() != Sentence.UNASSIGNED_SENTENCE_POSITION) {
				Double newPosition = mapFromOldToNewPositions.get(s.getReferredToSentence());
				if (newPosition != null) {
					s.setReferredToSentence(newPosition);
				}
			}
			
		}
	}
		
	public void wekaIGAnnotations() {
		ArrayList<Sentence> sentences = this.getInstanceLearner().getTrainedSentencesActual();
		
		WekaCreatorOptions wo = new WekaCreatorOptions();
		wo.setExportSentenceAsString(true);
		wo.setNERSentenceIndicators(false);
		wo.setWordType(WordType.LEMMA);
		wo.setStopWords(StopWord.getListByName(StopWord.DETERMINER));
		wo.setUseOriginalSentence(false);
		
		Instances dataSet = (new WekaCreator()).createWekaInstancesForSecurityObjectiveAnnotations("testFold",sentences,wo, SecurityObjective.getMinimalListForClassification());
		
		WekaInterfacer wi = new WekaInterfacer();
		try {
			wi.generateInformationGainMatrixAnnotations(dataSet,SecurityObjective.getMinimalListForClassification(),30);
			this.setStatusMessage("Annotation information gain matrix produced");
		}
		catch (Exception e) {
			System.out.println(e);
		}		
	}
	
	public void wekaIGClassifications() {
		ArrayList<Sentence> sentences = this.getInstanceLearner().getTrainedSentencesActual();
		
		WekaCreatorOptions wo = new WekaCreatorOptions();
		wo.setExportSentenceAsString(true);
		wo.setNERSentenceIndicators(false);
		wo.setWordType(WordType.LEMMA);
		wo.setStopWords(StopWord.getListByName(StopWord.DETERMINER));
		wo.setUseOriginalSentence(false);
		
		Instances dataSet = (new WekaCreator()).createWekaInstancesForClassifications("testFold",sentences,_classificationAttributes.getAttributeList(),wo);
		
		WekaInterfacer wi = new WekaInterfacer();
		try {
			wi.generateInformationGainMatrixClassifications(dataSet,_classificationAttributes.getAttributeList(),30);
			this.setStatusMessage("Annotation information gain matrix produced");
		}
		catch (Exception e) {
			System.out.println(e);
		}		
	}	
	
	public void wekaNBExecute() {
				
		//ArrayList<Sentence> sentences = InstanceLearner.getTrainedSentences();
		ArrayList<Sentence> sentences = _currentDocument.getSentences();
		//Instances dataSet = (new WekaCreator()).createWekaInstances(this.getCurrentDocumentID(),sentences,_classificationAttributes.getAttributeList(),wo);
		//Instances dataSet = (new WekaCreator()).createWekaInstancesSingleClass(this.getCurrentDocumentID(),sentences);
		
		//Use this for bag of word style
		//Instances dataSet = (new WekaCreator()).createWekaInstancesAsString(this.getCurrentDocumentID(),sentences,_classificationAttributes.getAttributeList(),true,true);
		
		WekaCreatorOptions wo = new WekaCreatorOptions();
		wo.setExportSentenceAsString(true);
		wo.setNERSentenceIndicators(false);
		wo.setWordType(WordType.LEMMA);
		wo.setStopWords(StopWord.getListByName(StopWord.DETERMINER));
		wo.setUseOriginalSentence(false);
		
		
		Instances dataSet = (new WekaCreator()).createWekaInstancesForClassifications("testFold",sentences,_classificationAttributes.getAttributeList(),wo);
		
		
		WekaInterfacer wi = new WekaInterfacer();
		try {
			wi.evaluateNFoldWithNaiveBayesMultinomial(dataSet,_classificationAttributes.getAttributeList(),10);
		}
		catch (Exception e) {
			System.out.println(e);
		}
	}
	
	public void wekaAnnotationNBExecute() {
		ArrayList<Sentence> sentences = this.getInstanceLearner().getTrainedSentencesActual();
		
		WekaCreatorOptions wo = new WekaCreatorOptions();
		wo.setExportSentenceAsString(true);
		wo.setNERSentenceIndicators(false);
		wo.setWordType(WordType.LEMMA);
		wo.setStopWords(StopWord.getListByName(StopWord.DETERMINER));
		wo.setUseOriginalSentence(false);
		
		Instances dataSet = (new WekaCreator()).createWekaInstancesForSecurityObjectiveAnnotations("testFold",sentences,wo, SecurityObjective.getListForClassification());
		
		
		WekaInterfacer wi = new WekaInterfacer();
		try {
			wi.evaluateNFoldWithNaiveBayesMultinomialForAnnotations(dataSet,SecurityObjective.getListForClassification(),10);
		}
		catch (Exception e) {
			System.out.println(e);
		}
	}
	
	public void wekaSMOExecute() {
				
		//ArrayList<Sentence> sentences = InstanceLearner.getTrainedSentences();
		ArrayList<Sentence> sentences = _currentDocument.getSentences();
		//Instances dataSet = (new WekaCreator()).createWekaInstancesAsString(this.getCurrentDocumentID(),sentences,_classificationAttributes.getAttributeList(),false,false);	
		
		WekaCreatorOptions wo = new WekaCreatorOptions();
		wo.setExportSentenceAsString(true);
		wo.setNERSentenceIndicators(false);
		wo.setWordType(WordType.LEMMA);
		wo.setStopWords(StopWord.getListByName(StopWord.DETERMINER));
		wo.setUseOriginalSentence(false);
		
		Instances dataSet = (new WekaCreator()).createWekaInstancesForClassifications("testFold",sentences,_classificationAttributes.getAttributeList(),wo);

		WekaInterfacer wi = new WekaInterfacer();
		try {
			wi.evaluateNFoldWithSupportVectorMachine(dataSet,_classificationAttributes.getAttributeList(),10);
		}
		catch (Exception e) {
			System.out.println(e);
		}
	}	
	
	
	public void wekaAnnotationSMOExecute() {
		ArrayList<Sentence> sentences = this.getInstanceLearner().getTrainedSentencesActual();
		
		WekaCreatorOptions wo = new WekaCreatorOptions();
		wo.setExportSentenceAsString(true);
		wo.setNERSentenceIndicators(false);
		wo.setWordType(WordType.LEMMA);
		wo.setStopWords(StopWord.getListByName(StopWord.DETERMINER));
		wo.setUseOriginalSentence(false);
		
		Instances dataSet = (new WekaCreator()).createWekaInstancesForSecurityObjectiveAnnotations("testFold",sentences,wo, SecurityObjective.getListForClassification());

		WekaInterfacer wi = new WekaInterfacer();
		try {
			wi.evaluateNFoldWithSupportVectorMachineForAnnotations(dataSet, SecurityObjective.getListForClassification(), 10);
		}
		catch (Exception e) {
			System.out.println(e);
		}
	}		
	
	public void wekaCurrentDocNB() {
		ArrayList<Sentence> testSentences = _currentDocument.getSentences();
		//Instances testDataSet = (new WekaCreator()).createWekaInstancesAsString(this.getCurrentDocumentID(),testSentences,_classificationAttributes.getAttributeList(),false,false);	
		
		WekaCreatorOptions wo = new WekaCreatorOptions();
		wo.setExportSentenceAsString(true);
		wo.setNERSentenceIndicators(false);
		wo.setWordType(WordType.LEMMA);
		wo.setStopWords(StopWord.getListByName(StopWord.FRAKES));
		wo.setUseOriginalSentence(false);
		
		
		Instances testDataSet = (new WekaCreator()).createWekaInstancesForClassifications("testFold",testSentences,_classificationAttributes.getAttributeList(),wo);

		ArrayList<Document> trainDocuments = _theInstanceLearner.getTrainedSentences();
		ArrayList<Sentence> trainSentences = new ArrayList<Sentence>();
		for (Document d: trainDocuments) {
			trainSentences.add(d.sentence);
		}
		//Instances trainDataSet = (new WekaCreator()).createWekaInstancesAsString("trainingset",trainSentences,_classificationAttributes.getAttributeList(),false,false);		
		Instances trainDataSet = (new WekaCreator()).createWekaInstancesForClassifications("testFold",trainSentences,_classificationAttributes.getAttributeList(),wo);

		
		WekaInterfacer wi = new WekaInterfacer();
		try {
			wi.evaluateTestSetWithNaiveBayesMultinomial(trainDataSet, testDataSet, _classificationAttributes.getAttributeList());
		}
		catch (Exception e) {
			System.out.println(e);
		}
			
		
		this.setStatusMessage("Current document with weka-NB complete");
	}
	
	public ConfusionMatrix wekaCurrentDocSMO(ArrayList<Sentence> trainSentences, ArrayList<Sentence> testSentences) {
		ConfusionMatrix result = null;
		
		
		WekaCreatorOptions wo = new WekaCreatorOptions();
		wo.setExportSentenceAsString(true);
		wo.setNERSentenceIndicators(false);
		wo.setWordType(WordType.LEMMA);
		wo.setStopWords(StopWord.getListByName(StopWord.FRAKES));
		wo.setUseOriginalSentence(false);
		
		
		Instances testDataSet = (new WekaCreator()).createWekaInstancesForClassifications("testFold",testSentences,_classificationAttributes.getAttributeList(),wo);

		//Instances trainDataSet = (new WekaCreator()).createWekaInstancesAsString("trainingset",trainSentences,_classificationAttributes.getAttributeList(),false,false);		
		Instances trainDataSet = (new WekaCreator()).createWekaInstancesForClassifications("testFold",trainSentences,_classificationAttributes.getAttributeList(), wo);

		
		WekaInterfacer wi = new WekaInterfacer();
		try {
			result = wi.evaluateTestSetWithSupportVectorMachine(trainDataSet, testDataSet, _classificationAttributes.getAttributeList());
		}
		catch (Exception e) {
			System.out.println(e);
		}
					 
		 
		 this.setStatusMessage("Current document with weka-SMO complete");
		 return result;
	}
	
	public void wekaCrossValidateByDocumentSMO() {
		ConfusionMatrix overAllResult = new ConfusionMatrix();
		
		java.util.Set<String> documentIDs = DocumentFoldCreator.computeUniqueDocumentIDs(_theInstanceLearner.getTrainedSentences());
		
		for (String documentId: documentIDs) {
			ArrayList<Sentence> testSentences  = new ArrayList<Sentence>();
			ArrayList<Sentence> trainSentences = new ArrayList<Sentence>();
			
			for (Document d: _theInstanceLearner.getTrainedSentences()) {
				Sentence s = d.sentence;
				if (s.getDocumentID().equals(documentId)) {
					testSentences.add(s);
				}
				else {
					trainSentences.add(s);
				}
			}
			overAllResult.add(this.wekaCurrentDocSMO(trainSentences,testSentences) );
		}
		System.out.println("************************************************************");
		System.out.println("TOTAL:  "+ overAllResult);

		System.out.println("Precision: "+ overAllResult.getPrecision());
		System.out.println("Recall: "+ overAllResult.getRecall());
		//System.out.println("True negative rate: "+totalMatrix.getTrueNegativeRate());
		//System.out.println("Accuracy: "+totalMatrix.getAccuracy());
		System.out.println("F-Measure:" +overAllResult.getF1Measure());	
					 
		 
		this.setStatusMessage("Document cross-validate with weka-SMO complete");
	}	
	
	
	public void wekaBuildClassifier() {
		_masterClassifier.createWekaInterfacer(_currentDocumentID);
		this.setStatusMessage("Weka classifiers trained.");
	}
	
	
	public void wekaClassifyCurrentSentence() {
		WekaCreatorOptions wo = new WekaCreatorOptions();
		wo.setExportSentenceAsString(true);
		wo.setNERSentenceIndicators(false);
		wo.setWordType(WordType.LEMMA);
		wo.setStopWords(StopWord.getListByName(StopWord.DETERMINER));
		wo.setUseOriginalSentence(false);		
		
		ArrayList<Sentence> sentences = new ArrayList<Sentence>();
		sentences.add(_currentDocument.getElementAt(_currentSentenceNumber));
		//Instances dataSet = (new WekaCreator()).createWekaInstancesAsString(this.getCurrentDocumentID(),sentences,_classificationAttributes.getAttributeList(),false,false);
		Instances dataSet = (new WekaCreator()).createWekaInstancesForClassifications("testFold",sentences,_classificationAttributes.getAttributeList(),wo);
		
		ClassificationResult resultWNB = _masterClassifier.getWekaInterfacer().classifyWithNaiveBayesMultinomial(dataSet);
		System.out.println("NB: "+ resultWNB);
		ClassificationResult resultSMO = _masterClassifier.getWekaInterfacer().classifyWithSMO(dataSet);
		System.out.println("SMO: "+ resultSMO);
	}	
	
	
	public void performAllSelfEvaluate() {
		
		MasterClassifier mc = new MasterClassifier(_classificationAttributes.getAttributeList(), SecurityObjective.getMinimalListForClassification());
	    mc.performOverallSelfEvaluate();
		
	    //System.out.println("Starting master classifier self-evaluation");
		//mc.performOverallSelfEvaluateForAnnotations();
		
		/*
		MasterClassifier mc = new MasterClassifier(_classificationAttributes.getAttributeList(), SecurityObjective.getMinimalListForClassification());
		for (ClassificationAttribute ca: _classificationAttributes.getAttributeList()) {
			if (ca.getIncludeInEvaluation() == false) { continue; }	
			System.out.println("Evaluating work % complete test: "+ca);
			
			mc.performSelfEvaluateByWorkComplete(ca);
		}
		*/
	}
	
	public void  performAllDocEvaluate() { 
		MasterClassifier mc = new MasterClassifier(_classificationAttributes.getAttributeList(), SecurityObjective.getListForClassification());
		mc.	performOverallDocumentEvaluate();
	}
	
	public void  performAllCurrentDocEvaluate() {
		MasterClassifier mc = new MasterClassifier(_classificationAttributes.getAttributeList(), SecurityObjective.getListForClassification());
		mc.performOverallCurrentDocumentEvaluate(_currentDocument.getSentences());
	}
	
	/**
	 * Method updates the security objective annotations to the new format where "mechanism" have been
	 * separated into different set of "markings".  
	 * 
	 * 
	 */
	public void convertSecurityObjectiveAnnotations() {
		if (_currentDocument.getNumberOfSentences() == 0) { 
			GCController.getTheGCController().setStatusMessage("Convert SOAs: No sentences in current document");
			return;
		}
		
		for (Sentence s: _currentDocument.getSentences()) {
			System.out.println("Analyzing: "+s);
			s.removeEmptySecurityObjectiveRows();    // this shouldn't be needed....
			
			if (s.getNumberOfSecurityObjectiveAnnotations() == 0) {
				System.out.println("\tNo security objective annotations");
				continue;
			}
			
			for (int i=s.getNumberOfSecurityObjectiveAnnotations()-1;i >=0 ;i--)  {
				SecurityObjectiveAnnotation soaOrginal = s.getSecurityObjectiveAnnotations().get(i);
				SecurityObjectiveAnnotation soa = new SecurityObjectiveAnnotation(soaOrginal);
				
				switch (soa.getSecurityObjective() ) {
				case AVAILABILITY_RES_UTILIZATION: System.out.println("\tConverting Availability - Resource Utilization to just Availability");
				                                   soa.setSecurityObjective(SecurityObjective.AVAILABILITY);
				                                   break;
				case PRIVACY_ANONYMITY:            System.out.println("\tConverting Privacy - Anonymity to just privacy");
                                                   soa.setSecurityObjective(SecurityObjective.PRIVACY);
                                                   break;
				case AUDIT: 					   System.out.println("\tConverting Audit to accountability");
				                                   soa.setSecurityObjective(SecurityObjective.ACCOUNTABILITY);
				                                   break;
				case AUDIT_INTRUSION: 			   System.out.println("\tConverting Audit - intrusion to accountability");
				                                   soa.setSecurityObjective(SecurityObjective.ACCOUNTABILITY);
				                                   break;
				case LOGGING:					   System.out.println("\tConverting logging to accountability");
				                                   soa.setSecurityObjective(SecurityObjective.ACCOUNTABILITY);
				                                   break;
				case LOGGING_SECURITY:             System.out.println("\tConverting logging: security to accountability");
				                                   soa.setSecurityObjective(SecurityObjective.ACCOUNTABILITY);
				                                   break;
				                                   
				case NON_REPUDITION:               System.out.println("\tConverting non-repudiation to accountability");
									               soa.setSecurityObjective(SecurityObjective.ACCOUNTABILITY);
									               break;
				case ACCESS_CONTROL_AUTHORIZATION: if (s.containsReadVerb()) {
												       System.out.print("\tConverting authorization to confidentiality");
                                                       soa.setSecurityObjective(SecurityObjective.CONFIDENTIALITY);
                                                       if (s.containsWriteVerb()) {
                                                    	   System.out.print(" and creating integrity");
                                                    	   s.replaceSecurityObjectiveAnnotation(new SecurityObjectiveAnnotation(SecurityObjective.INTEGRITY, SecurityImpact.LOW, SecurityMitigation.PREVENTION, false));
                                                       }
													   System.out.println();
				                                   }
				                                   else {
												       System.out.println("\tConverting authorization to integrity (read verb not found)");
												       soa.setSecurityObjective(SecurityObjective.INTEGRITY);
				                                   }
												   break;
				case LOGGING_INSFRASTRUCTURE:      String sentence = s.getSentence().toLowerCase();
				                                   if (sentence.contains("secur") || sentence.contains("access") || sentence.contains("restric") ) {
				                                	   System.out.println("\tConverting logging: infratructure to confidentiality");
					                                   soa.setSecurityObjective(SecurityObjective.CONFIDENTIALITY);
				                                   }
				                                   else if (sentence.contains("backup") || sentence.contains("copy") || sentence.contains("replica") || sentence.contains("cost") ) {
				                                	   System.out.println("\tConverting logging: infratructure to availability");
					                                   soa.setSecurityObjective(SecurityObjective.INTEGRITY);
				                                   }
				                                   else {
				                                	   System.out.println("\tConverting logging: infratructure to integrity");
					                                   soa.setSecurityObjective(SecurityObjective.INTEGRITY);
				                                   }
				                                   break;
				case ACCESS_CONTROL:            String sentText = s.getSentence().toLowerCase();
								                if (sentText.contains("consent") || sentText.contains("privacy") || sentText.contains("purpose")|| sentText.contains("disclosure") ) {
								             	   System.out.println("\tConverting access control to privacy");
								                    soa.setSecurityObjective(SecurityObjective.PRIVACY);
								                }
								                else {
								             	   System.out.println("\tConverting access control to confidentiality");
								                    soa.setSecurityObjective(SecurityObjective.CONFIDENTIALITY);
								                }
								                break;                  
				default: System.out.println("\tno action for "+soa.getSecurityObjective());
						break;                                                    
				}
				if (soa.getSecurityObjective() != soaOrginal.getSecurityObjective()) { // we had a change
					s.getSecurityObjectiveAnnotations().remove(i);
					if (s.hasSecurityObjectiveAnnotation(soa.getSecurityObjective())) {
						System.out.println("\tEXISTING ENTRY, dropping");
					}
					else {
						s.getSecurityObjectiveAnnotations().add(soa);
					}
					
				}
				
			}
			
			//s.replaceSecurityObjectiveAnnotation(newSOA);
			
			
	
	        
		}
		NLPEventManager.getTheEventManager().sendEvent( NLPEventType.SENTENCE_DATA_CHANGE, new NLPEventSentenceDataEvent(_currentDocument.getElementAt(0), "SOATable"));  
		//this._securityObjectiveAnnotationController.setCurrentSentence(currentSentence); // this "forces" the table to be redrawn for dialog box
        GCController.getTheGCController().setStatusMessage("SOAs converted");			
	}
	
	
	
	private void produceVerbFrequencyCounts() {
		HashMap<String, Integer> verbCounts = new HashMap<String,Integer>();
		for (Sentence s: _currentDocument.getSentences()) {
			List<WordVertex> verbs = s.getRoot().getVertexByPartOfSpeech(PartOfSpeech.VB,true,false);
			
			for (WordVertex wv: verbs) {
				if (wv.getNumberOfParents() > 0) { 
					Relationship r= wv.getParentAt(0).getRelationship();
					if (r == Relationship.XCOMP || r == Relationship.ADVMOD || r == Relationship.PARTMOD || r == Relationship.CCOMP) {
						continue;
					}
				}
				
				String key = wv.getLemma();
				
				int count =  verbCounts.containsKey(key) ?verbCounts.get(key) : 0;
				verbCounts.put(key, count + 1);
			}
			
		}
		System.out.println("==================================");
		for (String key: verbCounts.keySet()) {
			System.out.println(key + "\t"+ verbCounts.get(key));
		}		
		System.out.println("==================================");
		
		verbCounts = new HashMap<String,Integer>();
		for (Sentence s: _currentDocument.getSentences()) {
			if (s.hasBooleanClassification("access control:functional") == false) { continue; } 
			List<WordVertex> verbs = s.getRoot().getVertexByPartOfSpeech(PartOfSpeech.VB,true,false);
			
			for (WordVertex wv: verbs) {
				String key = wv.getLemma();
				
				int count =  verbCounts.containsKey(key) ?verbCounts.get(key) : 0;
				verbCounts.put(key, count + 1);
			}
			
		}
		System.out.println("==================================");
		System.out.println("Only ACF sentences");
		for (String key: verbCounts.keySet()) {
			System.out.println(key + "\t"+ verbCounts.get(key));
		}		
		System.out.println("==================================");		
	}
	
}
