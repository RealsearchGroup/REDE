package edu.ncsu.csc.nl.view;

import java.awt.BorderLayout;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import edu.ncsu.csc.nl.AccessControlController;
import edu.ncsu.csc.nl.DatabaseController;
import edu.ncsu.csc.nl.GCConstants;
import edu.ncsu.csc.nl.GCController;
import edu.ncsu.csc.nl.MappingController;
import edu.ncsu.csc.nl.ReportController;

import javax.swing.KeyStroke;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.InputEvent;

import javax.swing.JCheckBoxMenuItem;

public class MainFrame extends JFrame {

	public static final long serialVersionUID = 1;
	
	private static class MyMenu extends JMenuItem {
		public static final long serialVersionUID = 1;
		
		public MyMenu(String text, String actionCommand) {
			this(text,actionCommand, GCController.getTheGCController());
		}
		
		public MyMenu(String text, String actionCommand,ActionListener al) {
			super(text);
			
			this.setActionCommand(actionCommand);
			this.addActionListener(al);
		}		
		
	}
	
	

	MyMenu _miNew               = new MyMenu("New",GCConstants.ACTION_DOCUMENT_NEW);
	MyMenu _miLoadJSON          = new MyMenu("Load JSON File",GCConstants.ACTION_DOCUMENT_LOAD_JSON);
	MyMenu _miAppendJSON        = new MyMenu("Append JSON File",GCConstants.ACTION_DOCUMENT_APPEND_JSON);
	MyMenu _miLoadSerial        = new MyMenu("Load Serial File",GCConstants.ACTION_DOCUMENT_LOAD_SERIAL);
	MyMenu _miLoadDocument      = new MyMenu("Load Text Document",GCConstants.ACTION_DOCUMENT_LOAD_TEXT_DOCUMENT);
	MyMenu _miSaveJSON          = new MyMenu("Save JSON File",GCConstants.ACTION_DOCUMENT_SAVE_JSON);
	MyMenu _miSaveSerial        = new MyMenu("Save Serial File",GCConstants.ACTION_DOCUMENT_SAVE_SERIAL);
	MyMenu _miCompareJSON       = new MyMenu("Compare JSON File", GCConstants.ACTION_DOCUMENT_COMPARE_JSON);
	
	MyMenu _miExit               = new MyMenu("Exit",GCConstants.ACTION_DOCUMENT_EXIT);	
	MyMenu _miExportAsARFF       = new MyMenu("Export as ARFF ...",GCConstants.ACTION_DOCUMENT_EXPORT_ARFF);
	MyMenu _miLineGoto           = new MyMenu("Goto Line ...",GCConstants.ACTION_DOCUMENT_GOTO_LINE);
	MyMenu _miGotoNextUnclass    = new MyMenu("Goto First Unclassifed",GCConstants.ACTION_DOCUMENT_GOTO_NEXT_UNCLASS);
	MyMenu _miGotoNextUnAccess   = new MyMenu("Goto First AC Undefined",GCConstants.ACTION_DOCUMENT_GOTO_NEXT_ACCESS);
	MyMenu _miGotoNextUnDatabase = new MyMenu("Goto First DB Undefined",GCConstants.ACTION_DOCUMENT_GOTO_NEXT_DATABASE);
	MyMenu _miGotoNextUnSOA      = new MyMenu("Goto First SOA Undefined",GCConstants.ACTION_DOCUMENT_GOTO_NEXT_SOA);

	MyMenu _miSetCurrentDocID   = new MyMenu("Set Document ID",GCConstants.ACTION_DOCUMENT_SET_ID);
	MyMenu _miRenumber          = new MyMenu("Renumber",GCConstants.ACTION_DOCUMENT_RENUMBER);
	
	MyMenu _miMarkSOA_ACDL     = new MyMenu("Mark SOA for AU,C,DB,LG",GCConstants.ACTION_OTHER_MARK_FOR_ACDL);
	MyMenu _miMarkSOAComplete    = new MyMenu("MarkComplete",GCConstants.ACTION_SOA_MARK_COMPLETE);
	MyMenu _miConvertSOA         = new MyMenu("Convert SOAs", GCConstants.ACTION_SOA_CONVERT);
	MyMenu _miSOAConfidentiality = new MyMenu("Confidentiality",GCConstants.ACTION_SOA_CONFIDENTIALITY);
	MyMenu _miSOAIntegrity       = new MyMenu("Integrity",GCConstants.ACTION_SOA_INTEGRITY);
	MyMenu _miSOAAvailability    = new MyMenu("Availability",GCConstants.ACTION_SOA_AVAILABILITY );
	MyMenu _miSOAACIdentity      = new MyMenu("Identity & Authentication",GCConstants.ACTION_SOA_AC_IDENTITY  );
	MyMenu _miSOAAuthorization   = new MyMenu("Authorization",GCConstants.ACTION_SOA_AUTHORIZATION );
	MyMenu _miSOALogging         = new MyMenu("Logging",GCConstants.ACTION_SOA_LOGGING    );
	MyMenu _miSOANonRepudiation  = new MyMenu("Non-Repudiation",GCConstants.ACTION_SOA_NONREPUDIATION  );
	MyMenu _miSOAPrivacy         = new MyMenu("Privacy",GCConstants.ACTION_SOA_PRIVACY);
	MyMenu _miSOADatabase        = new MyMenu("Database",GCConstants.ACTION_SOA_DATABASE);
	
    MyMenu _miReportValidation         = new MyMenu("Validation Report", GCConstants.ACTION_REPORT_VALIDATION, ReportController.getTheReportController());
	MyMenu _miReportDocumentStatistics = new MyMenu("View Document Stats",GCConstants.ACTION_REPORT_DOCUMENT_STATISTICS, ReportController.getTheReportController());
	MyMenu _miReportFreqReport         = new MyMenu("Frequency Report - overall",GCConstants.ACTION_REPORT_FREQUENCY, ReportController.getTheReportController());
	MyMenu _miReportPrintFreqClassRpt  = new MyMenu("Frequency Report - by class",GCConstants.ACTION_REPORT_FREQUENCY_BY_CLASSIFICATION, ReportController.getTheReportController());
	MyMenu _miReportPrintFreqSS        = new MyMenu("Frequency Report - spreadsheet",GCConstants.ACTION_REPORT_FREQUENCY_SPREADSHEET, ReportController.getTheReportController());
	MyMenu _miReportClassSentences     = new MyMenu("Sentences by Classification",GCConstants.ACTION_REPORT_CLASSIFICATION_SENTENCES, ReportController.getTheReportController());
	MyMenu _miReportAnnotateSentences  = new MyMenu("Sentences by Annotation", GCConstants.ACTION_REPORT_ANNOTATION_SENTENCES, ReportController.getTheReportController());
	MyMenu _miReportAnnotateKeywordSS  = new MyMenu("Annotation Keyword Freq Spreadsheet", GCConstants.ACTION_REPORT_ANNOTATION_KEYWORD_FREQ_SS,ReportController.getTheReportController());
	MyMenu _miReportCustom             = new MyMenu("Custom", GCConstants.ACTION_REPORT_CUSTOM, ReportController.getTheReportController());

	MyMenu _miACExtractBootstrap         =  new MyMenu("Bootstrap", GCConstants.ACTION_ACEXTRACT_BOOTSTRAP ,  AccessControlController.getTheAccessControlController());
	MyMenu _miACExtractSearchForPatterns =  new MyMenu("Search for Patterns", GCConstants.ACTION_ACEXTRACT_SEARCH_FOR_PATTERNS ,  AccessControlController.getTheAccessControlController());
	MyMenu _miACExtractTransformPatterns =  new MyMenu("Transform Patterns", GCConstants.ACTION_ACEXTRACT_TRANSFORM_PATTERNS ,  AccessControlController.getTheAccessControlController());
	MyMenu _miACExtractExpandPatterns    =  new MyMenu("Expand Patterns", GCConstants.ACTION_ACEXTRACT_EXPAND_PATTERNS ,  AccessControlController.getTheAccessControlController());
	MyMenu _miACExtractExtractAC         =  new MyMenu("Extract Access Control", GCConstants.ACTION_ACEXTRACT_EXTRACT_AC_CONTROL ,  AccessControlController.getTheAccessControlController());
	MyMenu _miACExtractDetectNegativity  =  new MyMenu("Detect Negativity", GCConstants.ACTION_ACEXTRACT_DETECT_NEGATIVITY,  AccessControlController.getTheAccessControlController());
	MyMenu _miACExtractMoveACToHold      =  new MyMenu("Move AC to Hold", GCConstants.ACTION_ACEXTRACT_MOVE_AC_TO_HOLD ,  AccessControlController.getTheAccessControlController());
	MyMenu _miACExtractRestoreAC         =  new MyMenu("Restore AC", GCConstants.ACTION_ACEXTRACT_RESTORE_AC_RELATIONS ,  AccessControlController.getTheAccessControlController());	
	MyMenu _miACExtractAnalyzePatterns   =  new MyMenu("Analyze Patterns", GCConstants.ACTION_ACEXTRACT_ANALYZE_PATTERNS,  AccessControlController.getTheAccessControlController());
	MyMenu _miACExtractCreateBayes       =  new MyMenu("Create Naive Bayes Classifier", GCConstants.ACTION_ACEXTRACT_CREATE_BAYES,  AccessControlController.getTheAccessControlController());	
	MyMenu _miACExtractEvaluateAC        =  new MyMenu("Evaluate AC", GCConstants.ACTION_ACEXTRACT_EVALUATE_AC ,  AccessControlController.getTheAccessControlController());
	MyMenu _miACExtractReportACPatterns  =  new MyMenu("Select Report ...", GCConstants.ACTION_ACEXTRACT_REPORT_AC_PATTERNS ,  AccessControlController.getTheAccessControlController());	
	MyMenu _miACExtractResetACRM         =  new MyMenu("Reset ACRM", GCConstants.ACTION_ACEXTRACT_RESET_ACRM,  AccessControlController.getTheAccessControlController());
	MyMenu _miACExtractCopyHoldToActive  =  new MyMenu("Add Hold to Active", GCConstants.ACTION_ACEXTRACT_ADD_HOLD_TO_ACTIVE,  AccessControlController.getTheAccessControlController());
	MyMenu _miACExtractCycleProcess      =  new MyMenu("Cycle Process", GCConstants.ACTION_ACEXTRACT_CYCLE_PROCESS,  AccessControlController.getTheAccessControlController());
	MyMenu _miACExtractReportOnHold      =  new MyMenu("Report AC in Hold", GCConstants.ACTION_ACEXTRACT_REPORT_ON_HOLD,  AccessControlController.getTheAccessControlController());
	MyMenu _miACExtractClassifyACP       =  new MyMenu("Classify ACP", GCConstants.ACTION_ACEXTRACT_CLASSIFY_ACP,  AccessControlController.getTheAccessControlController());
	MyMenu _miACExtractCreateACPWeka     =  new MyMenu("Create ACP with Weka", GCConstants.ACTION_ACEXTRACT_CREATE_ACP_WEKA,  AccessControlController.getTheAccessControlController());
	MyMenu _miACExtractLoadACPWeka       =  new MyMenu("Load ACP with Weka", GCConstants.ACTION_ACEXTRACT_LOAD_ACP_WEKA,  AccessControlController.getTheAccessControlController());
	MyMenu _miACExtractSaveACPWeka       =  new MyMenu("Save ACP with Weka", GCConstants.ACTION_ACEXTRACT_SAVE_ACP_WEKA,  AccessControlController.getTheAccessControlController());
	MyMenu _miACExtractClassifyACPWeka   =  new MyMenu("Classify ACP with Weka", GCConstants.ACTION_ACEXTRACT_CLASSIFY_ACP_WEKA,  AccessControlController.getTheAccessControlController());
	MyMenu _miACExtractEvalDocComplete   =  new MyMenu("Evaluate Doc Completion %", GCConstants.ACTION_ACEXTRACT_EVAL_DOC_COMPLETION,  AccessControlController.getTheAccessControlController());
	MyMenu _miACExtractLoadClassifier    =  new MyMenu("Load NB Classifier", GCConstants.ACTION_ACEXTRACT_LOAD_NB_CLASSIFIER,  AccessControlController.getTheAccessControlController());
	MyMenu _miACExtractSaveClassifier    =  new MyMenu("Save NB Classifier", GCConstants.ACTION_ACEXTRACT_SAVE_NB_CLASSIFIER,  AccessControlController.getTheAccessControlController());
	MyMenu _miACExtractAddToClassifier   =  new MyMenu("Add to NB Classifer", GCConstants.ACTION_ACEXTRACT_ADD_TO_NB_CLASSIFIER,  AccessControlController.getTheAccessControlController());
	MyMenu _miACExtractAnalyzeFactors    =  new MyMenu("Analyze Factors", GCConstants.ACTION_ACEXTRACT_ANALYZE_FACTORS, AccessControlController.getTheAccessControlController());
	MyMenu _miACExtractRptUndiscAC       =  new MyMenu("Report FP/FN Patterns", GCConstants.ACTION_ACEXTRACT_REPORT_UNDISCOVERED_PATTERNS, AccessControlController.getTheAccessControlController());
	MyMenu _miACExtractInjectACP         =  new MyMenu("Inject Patterns", GCConstants.ACTION_ACEXTRACT_INJECT_PATTERN, AccessControlController.getTheAccessControlController());
	
	
	MyMenu _miDBExtractBootstrap         =  new MyMenu("Bootstrap", GCConstants.ACTION_DBEXTRACT_BOOTSTRAP ,  DatabaseController.getTheDatabaseController());
	MyMenu _miDBExtractSearchForPatterns =  new MyMenu("Search for Patterns", GCConstants.ACTION_DBEXTRACT_SEARCH_FOR_PATTERNS ,  DatabaseController.getTheDatabaseController());
	MyMenu _miDBExtractTransformPatterns =  new MyMenu("Transform Patterns", GCConstants.ACTION_DBEXTRACT_TRANSFORM_PATTERNS ,  DatabaseController.getTheDatabaseController());
	MyMenu _miDBExtractExpandPatterns    =  new MyMenu("Expand Patterns", GCConstants.ACTION_DBEXTRACT_EXPAND_PATTERNS ,  DatabaseController.getTheDatabaseController());
	MyMenu _miDBExtractExtractDB         =  new MyMenu("Extract Database Objects", GCConstants.ACTION_DBEXTRACT_EXTRACT_DATABASE_OBJ ,  DatabaseController.getTheDatabaseController());
	MyMenu _miDBExtractMoveDBToHold      =  new MyMenu("Move DB to Hold", GCConstants.ACTION_DBEXTRACT_MOVE_DB_TO_HOLD ,  DatabaseController.getTheDatabaseController());
	MyMenu _miDBExtractRestoreDB         =  new MyMenu("Restore AC", GCConstants.ACTION_DBEXTRACT_RESTORE_DB_RELATIONS ,  DatabaseController.getTheDatabaseController());	
	MyMenu _miDBExtractAnalyzePatterns   =  new MyMenu("Analyze Patterns", GCConstants.ACTION_DBEXTRACT_ANALYZE_PATTERNS,  DatabaseController.getTheDatabaseController());
	MyMenu _miDBExtractCreateBayes       =  new MyMenu("Create Naive Bayes Classifier", GCConstants.ACTION_DBEXTRACT_CREATE_BAYES,  DatabaseController.getTheDatabaseController());	
	MyMenu _miDBExtractEvaluateDB        =  new MyMenu("Evaluate Database", GCConstants.ACTION_DBEXTRACT_EVALUATE_DB ,  DatabaseController.getTheDatabaseController());
	MyMenu _miDBExtractReportDBPatterns  =  new MyMenu("Select Report ...", GCConstants.ACTION_DBEXTRACT_REPORT_DB_PATTERNS ,  DatabaseController.getTheDatabaseController());	
	MyMenu _miDBExtractResetDBRM         =  new MyMenu("Reset Database RM", GCConstants.ACTION_DBEXTRACT_RESET_DBRM,  DatabaseController.getTheDatabaseController());
	MyMenu _miDBExtractCopyHoldToActive  =  new MyMenu("Add Hold to Active", GCConstants.ACTION_DBEXTRACT_ADD_HOLD_TO_ACTIVE,  DatabaseController.getTheDatabaseController());
	MyMenu _miDBExtractCycleProcess      =  new MyMenu("Cycle Process", GCConstants.ACTION_DBEXTRACT_CYCLE_PROCESS,  DatabaseController.getTheDatabaseController());
	MyMenu _miDBExtractReportOnHold      =  new MyMenu("Report DB in Hold", GCConstants.ACTION_DBEXTRACT_REPORT_ON_HOLD,  DatabaseController.getTheDatabaseController());
	MyMenu _miDBExtractClassifyDBP       =  new MyMenu("Classify Database Patterns", GCConstants.ACTION_DBEXTRACT_CLASSIFY_DBP,  DatabaseController.getTheDatabaseController());
	MyMenu _miDBExtractCreateDBPWeka     =  new MyMenu("Create DBP with Weka", GCConstants.ACTION_DBEXTRACT_CREATE_DBP_WEKA,  DatabaseController.getTheDatabaseController());
	MyMenu _miDBExtractLoadDBPWeka       =  new MyMenu("Load DBP with Weka", GCConstants.ACTION_DBEXTRACT_LOAD_DBP_WEKA,  DatabaseController.getTheDatabaseController());
	MyMenu _miDBExtractSaveDBPWeka       =  new MyMenu("Save DBP with Weka", GCConstants.ACTION_DBEXTRACT_SAVE_DBP_WEKA,  DatabaseController.getTheDatabaseController());
	MyMenu _miDBExtractClassifyDBPWeka   =  new MyMenu("Classify DBP with Weka", GCConstants.ACTION_DBEXTRACT_CLASSIFY_DBP_WEKA,  DatabaseController.getTheDatabaseController());
	MyMenu _miDBExtractEvalDocComplete   =  new MyMenu("Evaluate Doc Completion %", GCConstants.ACTION_DBEXTRACT_EVAL_DOC_COMPLETION,  DatabaseController.getTheDatabaseController());
	MyMenu _miDBExtractLoadClassifier    =  new MyMenu("Load NB Classifier", GCConstants.ACTION_DBEXTRACT_LOAD_NB_CLASSIFIER,  DatabaseController.getTheDatabaseController());
	MyMenu _miDBExtractSaveClassifier    =  new MyMenu("Save NB Classifier", GCConstants.ACTION_DBEXTRACT_SAVE_NB_CLASSIFIER,  DatabaseController.getTheDatabaseController());
	MyMenu _miDBExtractAddToClassifier   =  new MyMenu("Add to NB Classifer", GCConstants.ACTION_DBEXTRACT_ADD_TO_NB_CLASSIFIER,  DatabaseController.getTheDatabaseController());
	MyMenu _miDBExtractAnalyzeFactors    =  new MyMenu("Analyze Factors", GCConstants.ACTION_DBEXTRACT_ANALYZE_FACTORS, DatabaseController.getTheDatabaseController());
	MyMenu _miDBExtractRptUndiscDB       =  new MyMenu("Report FP/FN Patterns", GCConstants.ACTION_DBEXTRACT_REPORT_UNDISCOVERED_PATTERNS, DatabaseController.getTheDatabaseController());
	MyMenu _miDBExtractInjectDB          =  new MyMenu("Inject Patterns", GCConstants.ACTION_DBEXTRACT_INJECT_PATTERN, DatabaseController.getTheDatabaseController());	
	
	
	MyMenu _miMappingStart          =  new MyMenu("Start Mapping Process", GCConstants.ACTION_MAPPING_START, MappingController.getTheMappingController());
	MyMenu _miMappingAttribute      =  new MyMenu("Resolve Attributes to Entities", GCConstants.ACTION_MAPPING_ATTRIBUTE, MappingController.getTheMappingController());  
	MyMenu _miMappingResolveSubj    =  new MyMenu("Resolve Subjects", GCConstants.ACTION_MAPPING_RESOLVE_SUBJECTS, MappingController.getTheMappingController()); 
	MyMenu _miMappingResolveObj     =  new MyMenu("Resolve Objects", GCConstants.ACTION_MAPPING_RESOLVE_OBJECTS, MappingController.getTheMappingController());  
	MyMenu _miMappingMergeRules     =  new MyMenu("Merge Rules", GCConstants.ACTION_MAPPING_MERGE_RULES, MappingController.getTheMappingController());  
	MyMenu _miMappingLoadDBTables   =  new MyMenu("Load DB Tables", GCConstants.ACTION_MAPPING_LOAD_DB_TABLES, MappingController.getTheMappingController()); 
	MyMenu _miMappingMapDBTables    =  new MyMenu("Map DB Tables", GCConstants.ACTION_MAPPING_MAP_DB_TABLES, MappingController.getTheMappingController());  
	MyMenu _miMappingRptTracability =  new MyMenu("Report - Traceability", GCConstants.ACTION_MAPPING_RPT_TRACEABILITY, MappingController.getTheMappingController()); 
	MyMenu _miMappingRptConflict    =  new MyMenu("Report - Conflict", GCConstants.ACTION_MAPPING_RPT_CONFLICT, MappingController.getTheMappingController());     
	MyMenu _miMappingRptSQLAC       =  new MyMenu("Report - SQL AC", GCConstants.ACTION_MAPPING_RPT_SQL_AC, MappingController.getTheMappingController());   
	MyMenu _miMappingRptMongoDBAC   =  new MyMenu("Report - Mongo AC", GCConstants.ACTION_MAPPING_RPT_MONGODB_AC, MappingController.getTheMappingController()); 
	
	MyMenu _miClassMark_DB_FT    = new MyMenu("Mark DB, Fnct",GCConstants.ACTION_CLASS_MARK_DB_FT);
	MyMenu _miClassMark_AC_DB_FT = new MyMenu("Mark Access, DB, Fnct",GCConstants.ACTION_CLASS_MARK_ACF_DB_FT);
	MyMenu _miClassMark          = new MyMenu("Mark Complete", GCConstants.ACTION_CLASS_MARK_COMPLETE);
		
	JPanel _jpCurrentPanel = new JPanel();
	JTextField _jtfStatus  = new JTextField();
	SentenceDisplayPanel _sentenceClassifier;
	
	BorderLayout _layout = new BorderLayout();
	
	 MyMenu _miWordNetBrowser = new MyMenu("WordNet Browser",GCConstants.ACTION_OTHER_WORDNET_BROWSER);
	
	 MyMenu _miLoadPipeLine = new MyMenu("Load Pipeline",GCConstants.ACTION_LOAD_PIPELINE);

	 MyMenu _miAbout = new MyMenu("About","");

	
	 MyMenu _miClearLearner = new MyMenu("Clear Learner",GCConstants.ACTION_LEARNER_CLEAR);
	 MyMenu _miDumpLearner  = new MyMenu("Dump Learner",GCConstants.ACTION_LEARNER_DUMP);
	 MyMenu _miLoadLearner  = new MyMenu("Load Learner",GCConstants.ACTION_LEARNER_LOAD);
	 MyMenu _miSaveLearner  = new MyMenu("Save Learner",GCConstants.ACTION_LEARNER_SAVE);
	 MyMenu _miLoadLearnerSer  = new MyMenu("Load Learner - serial",GCConstants.ACTION_LEARNER_LOAD_SERIAL);
	 MyMenu _miSaveLearnerSer  = new MyMenu("Save Learner - serial",GCConstants.ACTION_LEARNER_SAVE_SERIAL);	
	 MyMenu _miSetK              = new MyMenu("Set K",GCConstants.ACTION_LEARNER_SETK);
	 MyMenu _miSelfEvaluate      = new MyMenu("Evaluate by n-Folds",GCConstants.ACTION_LEARNER_SELFEVAL);
	 MyMenu _miDocumentEvaluate  = new MyMenu("Evaluate by Documents",GCConstants.ACTION_LEARNER_DOCUMENT_EVAL);	
	 MyMenu _miEvaluateWithIntNB = new MyMenu("Evaluate with internal NB",GCConstants.ACTION_LEARNER_INTERNAL_NB);
	 MyMenu _miSelfEvaluateAnnotations    = new MyMenu("Self Evaluate for Annotations",GCConstants.ACTION_LEARNER_SELFEVAL_ANNOTATIONS);
	 MyMenu _miDocumentLearnerAnnotations = new MyMenu("Self Document for Annotations",GCConstants.ACTION_LEARNER_DOCUMENT_EVAL_ANNOTATIONS);
	 MyMenu _miCurrDocEvalAnnotations     = new MyMenu("Current Document Evaluate for Annotations",GCConstants.ACTION_LEARNER_CURRENT_EVAL_ANNOTATIONS);
	 
	 MyMenu _miLoadAllTrained = new MyMenu("Load all Trained Sentences",GCConstants.ACTION_LEARNER_ADDTRAIN);
	 MyMenu _miMoveLearnerToDoc = new MyMenu("Mover Learner to Current Doc",GCConstants.ACTION_LEARNER_MOVE_TO_CURRENT_DOC);
	 
	 MyMenu _miLearnerExportSOA = new MyMenu("Export Sentences with SOA ...",GCConstants.ACTION_LEARNER_EXPORT_SOA_SENTENCES);
	 MyMenu _miLearnerSOAMatrix = new MyMenu("SOA Occurences Matrix",GCConstants.ACTION_LEARNER_PRODUCE_SOA_MATRIX);
	 MyMenu _miLearnerSOAReport = new MyMenu("SOA Occurences Full Report",GCConstants.ACTION_LEARNER_PRODUCE_SOA_FULL);
	
	 

	
	 JCheckBoxMenuItem _jcbmiSupervisedLearning = new JCheckBoxMenuItem("Supervised Learning");
	 JCheckBoxMenuItem _jcbmAutoCompleteMatch   = new JCheckBoxMenuItem("Auto Complete Exact Match",true);

	 MyMenu _miPOSOverrides = new MyMenu("POS Overrides",GCConstants.ACTION_OTHER_POS_OVERRIDES);
	 MyMenu _miViewClassResults = new MyMenu("Classifications ...",GCConstants.ACTION_OTHER_VIEW_CLASS_RESULTS);
	 MyMenu _miOptions          = new MyMenu("Options",GCConstants.ACTION_OTHER_OPTIONS);
	 MyMenu _miFind             = new MyMenu("Find",GCConstants.ACTION_OTHER_FIND);
	 MyMenu _miFindNext         = new MyMenu("Find Next",GCConstants.ACTION_OTHER_FIND_NEXT);
	 
	 MyMenu _miCluster          = new MyMenu("Cluster",GCConstants.ACTION_OTHER_CLUSTER);
	 MyMenu _miOtherAllSelfEval = new MyMenu("All Self-Evaluate",GCConstants.ACTION_OTHER_ALL_SELF_EVALUATE);
	 MyMenu _miOtherAllDocEval  = new MyMenu("All Document-Evaluate",GCConstants.ACTION_OTHER_ALL_DOC_EVALUATE);
	 MyMenu _miOtherCurrentDoc  = new MyMenu("Current Document-Evaluate",GCConstants.ACTION_OTHER_CURRENT_DOC_EVALUATE);
	 MyMenu _miBootstrap        = new MyMenu("Verb Frequencies",GCConstants.ACTION_OTHER_VERB_FREQUENCIES);
	 MyMenu _miRestoreACRelation= new MyMenu("Restore AC Relations", GCConstants.ACTION_OTHER_RESTORE_AC_RELATIONS);
	
	 MyMenu _miWekaComputeIGAnnotations     = new MyMenu("Information Gain - Annotations (LNR)",GCConstants.ACTION_WEKA_COMPUTE_INFO_GAIN_ANNOTATIONS);
	 MyMenu _miWekaComputeIGClassifications = new MyMenu("Information Gain - Classifications (LNR)",GCConstants.ACTION_WEKA_COMPUTE_INFO_GAIN_CLASSIFICATIONS);
	 
	 MyMenu _miWekaNaiveBayes = new MyMenu("Evaluate with Naive Bayes",GCConstants.ACTION_WEKA_EVAL_NAIVE_BAYES);
	 MyMenu _miWekaSMO        = new MyMenu("Evaluate with SVM(SMO)",GCConstants.ACTION_WEKA_EVAL_SMO);
	 MyMenu _miWekaCreate     = new MyMenu("Create from Learner",GCConstants.ACTION_WEKA_CREATE_CLASSIFIERS);
	 MyMenu _miWekaClassify   = new MyMenu("Classify Current Sentence",GCConstants.ACTION_WEKA_CLASSIFY_CURRENT);
	 MyMenu _miWekaCurrentDocNB  = new MyMenu("Current Doc - Naive Bayes",GCConstants.ACTION_WEKA_CURRENT_DOC_NAIVE_BAYES);
	 MyMenu _miWekaCurrentDocSMO = new MyMenu("Current Doc - SVM(SMO)",GCConstants.ACTION_WEKA_CURRENT_DOC_SMO);

	 MyMenu _miWekaSOANaiveBayes = new MyMenu("Evaluate SOA with Naive Bayes",GCConstants. ACTION_WEKA_SOA_EVAL_NAIVE_BAYES);
	 MyMenu _miWekaSOASMO        = new MyMenu("Evaluate SOA with SVM(SMO)",GCConstants.ACTION_WEKA_SOA_EVAL_SMO);
	 MyMenu _miWekaSOACurrentDocNB  = new MyMenu("Current Doc - SOA - Naive Bayes",GCConstants.ACTION_WEKA_SOA_CURRENT_DOC_NAIVE_BAYES);
	 MyMenu _miWekaSOACurrentDocSMO = new MyMenu("Current Doc - SOA - SVM(SMO)",GCConstants.ACTION_WEKA_SOA_CURRENT_DOC_SMO);
	 

	/**
	 * Create the frame.
	 */
	public MainFrame() {
		super();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		this.setSize(1250,650);
		
		JMenuBar mb = new JMenuBar();
		this.setJMenuBar(mb);
		
		JMenu projectMenu    = new JMenu("Project");
		JMenu reportMenu     = new JMenu("Reports");
		
		//JMenu dictionaryMenu = new JMenu("Dictionary");

		JMenu learnerMenu    = new JMenu("Learner");
		JMenu wekaMenu       = new JMenu("Weka");
		JMenu otherMenu      = new JMenu("Other");
		JMenu soaMenu        = new JMenu("SOA");
		JMenu classMenu      = new JMenu("Classfications");
		JMenu extractACMenu  = new JMenu("AC Extraction");
		JMenu extractDBMenu  = new JMenu("DB Extraction");
		JMenu mappingMenu    = new JMenu("Mapping");
		JMenu helpMenu       = new JMenu("Help");
		
		mb.add(projectMenu);
		mb.add(reportMenu);

		mb.add(learnerMenu);
		learnerMenu.add(_miClearLearner);
		learnerMenu.add(_miDumpLearner);
		learnerMenu.add(_miLoadLearner);
		learnerMenu.add(_miSaveLearner);
		learnerMenu.addSeparator();
		learnerMenu.add(_miLoadLearnerSer);
		learnerMenu.add(_miSaveLearnerSer);
		learnerMenu.addSeparator();
		learnerMenu.add(_jcbmiSupervisedLearning);
		learnerMenu.add(_jcbmAutoCompleteMatch);
		learnerMenu.addSeparator();
		learnerMenu.add(_miLoadAllTrained);
		learnerMenu.add(_miSetK);
		learnerMenu.addSeparator();		
		learnerMenu.add(_miSelfEvaluate);	
		learnerMenu.add(_miDocumentEvaluate);
		learnerMenu.add(_miEvaluateWithIntNB);
		learnerMenu.addSeparator();
		learnerMenu.add(_miSelfEvaluateAnnotations);
		learnerMenu.add(_miDocumentLearnerAnnotations);
		learnerMenu.add(_miCurrDocEvalAnnotations);		
		learnerMenu.addSeparator();
		learnerMenu.add(_miMoveLearnerToDoc);
		
		//learnerMenu.addSeparator();
		//learnerMenu.add(_miLearnerExportSOA);
		//learnerMenu.add(_miLearnerSOAMatrix);
		//learnerMenu.add(_miLearnerSOAReport);
		
		
		_miSetK.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K, InputEvent.CTRL_MASK));
				
		
		mb.add(wekaMenu);
		wekaMenu.add(_miWekaComputeIGAnnotations);
		wekaMenu.add(_miWekaComputeIGClassifications);
		wekaMenu.addSeparator();
		wekaMenu.add(_miWekaCreate);
		wekaMenu.addSeparator();	
		wekaMenu.add(_miWekaClassify);
		wekaMenu.addSeparator();	
		wekaMenu.add(_miWekaNaiveBayes);
		wekaMenu.add(_miWekaSMO);
		wekaMenu.add(_miWekaCurrentDocNB);
		wekaMenu.add(_miWekaCurrentDocSMO);
		
		//wekaMenu.addSeparator();
		//wekaMenu.add(_miWekaSOANaiveBayes);
		//wekaMenu.add(_miWekaSOASMO);
		//wekaMenu.add(_miWekaSOACurrentDocNB);
		//wekaMenu.add(_miWekaSOACurrentDocSMO);
		
		//mb.add(new JMenu("Requirement Extraction"));
		
		mb.add(otherMenu);
		otherMenu.add(_miWordNetBrowser);
		otherMenu.add(_miPOSOverrides);
		otherMenu.add(_miViewClassResults);
		otherMenu.addSeparator();
		otherMenu.add(_miOptions);
		otherMenu.addSeparator();
		otherMenu.add(_miFind);
		otherMenu.add(_miFindNext);
		otherMenu.addSeparator();

		otherMenu.add(_miCluster);
		otherMenu.addSeparator();
		otherMenu.add(_miOtherAllSelfEval);
		otherMenu.add(_miOtherAllDocEval);
		otherMenu.add(_miOtherCurrentDoc);
		otherMenu.addSeparator();
		otherMenu.add(_miBootstrap);
		otherMenu.add(_miRestoreACRelation);

		_miFind.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F, InputEvent.CTRL_MASK));
		_miFindNext.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F3,0));
		
		// Hide SOA menu
		//mb.add(soaMenu);
		soaMenu.add(_miMarkSOA_ACDL);
		soaMenu.add(_miMarkSOAComplete);
		soaMenu.add(_miConvertSOA);
		soaMenu.addSeparator();
		soaMenu.add(_miSOAConfidentiality);
		soaMenu.add(_miSOAIntegrity);
		soaMenu.add(_miSOAAvailability);
		soaMenu.add(_miSOAACIdentity);
		soaMenu.add(_miSOAAuthorization);
		soaMenu.add(_miSOALogging);
		soaMenu.add(_miSOANonRepudiation);
		soaMenu.add(_miSOAPrivacy);
		soaMenu.add(_miSOADatabase);
		
		//_miMarkSOA_ACDL.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F12,0));
		//_miMarkSOAComplete.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F11,0));
		_miSOAConfidentiality.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_1, InputEvent.CTRL_MASK));
		_miSOAIntegrity.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_2, InputEvent.CTRL_MASK));
		_miSOAAvailability.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_3, InputEvent.CTRL_MASK));
		_miSOAACIdentity.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_4, InputEvent.CTRL_MASK));
		_miSOAAuthorization.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_5, InputEvent.CTRL_MASK));
		_miSOALogging.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_6, InputEvent.CTRL_MASK));
		_miSOANonRepudiation.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_7, InputEvent.CTRL_MASK));
		_miSOAPrivacy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_8, InputEvent.CTRL_MASK));
		_miSOADatabase.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_0, InputEvent.CTRL_MASK));		

		mb.add(classMenu);
		classMenu.add(_miClassMark_DB_FT);
		classMenu.add(_miClassMark_AC_DB_FT);
		classMenu.add(_miClassMark);
		
		_miClassMark_DB_FT.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F9,0));
		_miClassMark_AC_DB_FT.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F11,0));
		_miClassMark.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F12,0));
			
		mb.add(extractACMenu);
		mb.add(extractDBMenu);
		mb.add(mappingMenu);
		
		mb.add(helpMenu);
		_miLoadPipeLine.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, InputEvent.CTRL_MASK));
		
		helpMenu.add(_miLoadPipeLine);
		helpMenu.add(_miAbout);
		_miNew.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK));

		_miLoadJSON.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
		_miSaveJSON.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
		_miLineGoto.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_G, InputEvent.CTRL_MASK));
		_miExit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F4, InputEvent.ALT_MASK));
		
		_miGotoNextUnDatabase.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1,0));
		_miGotoNextUnAccess.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2,0));
		_miGotoNextUnclass.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F5,0));

		projectMenu.add(_miNew);
		projectMenu.add(_miLoadJSON);
		projectMenu.add(_miLoadSerial);
		projectMenu.add(_miLoadDocument);
		projectMenu.add(_miAppendJSON);
		projectMenu.add(_miSaveJSON);
		projectMenu.add(_miSaveSerial);
		projectMenu.addSeparator();
		projectMenu.add(_miExportAsARFF);
		projectMenu.addSeparator();
		projectMenu.add(_miCompareJSON);
		projectMenu.addSeparator();
		projectMenu.add(_miLineGoto);
		projectMenu.add(_miGotoNextUnclass);
		projectMenu.add(_miGotoNextUnAccess);
		projectMenu.add(_miGotoNextUnDatabase);
		projectMenu.add(_miGotoNextUnSOA);
		projectMenu.add(_miSetCurrentDocID);
		projectMenu.add(_miRenumber);
		projectMenu.addSeparator();

		projectMenu.add(_miExit);
		
		reportMenu.add(_miReportValidation);
		reportMenu.addSeparator();
		reportMenu.add(_miReportDocumentStatistics);
		reportMenu.add(_miReportFreqReport);
		reportMenu.add(_miReportPrintFreqClassRpt);
		reportMenu.add(_miReportPrintFreqSS);
		reportMenu.add(_miReportClassSentences);
		reportMenu.addSeparator();	
		reportMenu.add(_miReportAnnotateSentences);
		reportMenu.add(_miReportAnnotateKeywordSS);
		reportMenu.addSeparator();
		reportMenu.add(_miReportCustom);
		
		extractACMenu.add(_miACExtractResetACRM);
		extractACMenu.add(_miACExtractBootstrap);
		extractACMenu.add(_miACExtractSearchForPatterns);
		extractACMenu.add(_miACExtractExpandPatterns);
		extractACMenu.add(_miACExtractTransformPatterns);
		extractACMenu.add(_miACExtractExtractAC);
		extractACMenu.add(_miACExtractCycleProcess);
		extractACMenu.add(_miACExtractInjectACP);
		extractACMenu.add(_miACExtractDetectNegativity);
		extractACMenu.addSeparator();
		extractACMenu.add(_miACExtractMoveACToHold);
		extractACMenu.add(_miACExtractCopyHoldToActive);
		extractACMenu.add(_miACExtractRestoreAC);
		extractACMenu.addSeparator();
		extractACMenu.add(_miACExtractAnalyzePatterns);
		extractACMenu.add(_miACExtractAnalyzeFactors);
		extractACMenu.add(_miACExtractCreateBayes);	
		extractACMenu.add(_miACExtractAddToClassifier);
		extractACMenu.add(_miACExtractLoadClassifier);
		extractACMenu.add(_miACExtractSaveClassifier);
		extractACMenu.add(_miACExtractClassifyACP);
		extractACMenu.addSeparator();
		extractACMenu.add(_miACExtractCreateACPWeka);
		extractACMenu.add(_miACExtractLoadACPWeka);
		extractACMenu.add(_miACExtractSaveACPWeka);
		extractACMenu.add(_miACExtractClassifyACPWeka);
		extractACMenu.addSeparator();
		extractACMenu.add(_miACExtractEvaluateAC);
		extractACMenu.add(_miACExtractEvalDocComplete);
		extractACMenu.addSeparator();
		extractACMenu.add(_miACExtractReportACPatterns);
		extractACMenu.add(_miACExtractRptUndiscAC);
		extractACMenu.add(_miACExtractReportOnHold);
		
		
		extractDBMenu.add(_miDBExtractResetDBRM);
		extractDBMenu.add(_miDBExtractBootstrap);
		extractDBMenu.add(_miDBExtractSearchForPatterns);
		extractDBMenu.add(_miDBExtractExpandPatterns);
		extractDBMenu.add(_miDBExtractTransformPatterns);
		extractDBMenu.add(_miDBExtractExtractDB);
		extractDBMenu.add(_miDBExtractCycleProcess);
		extractDBMenu.add(_miDBExtractInjectDB);
		extractDBMenu.addSeparator();
		extractDBMenu.add(_miDBExtractMoveDBToHold);
		extractDBMenu.add(_miDBExtractCopyHoldToActive);
		extractDBMenu.add(_miDBExtractRestoreDB);
		extractDBMenu.addSeparator();
		extractDBMenu.add(_miDBExtractAnalyzePatterns);
		extractDBMenu.add(_miDBExtractAnalyzeFactors);
		extractDBMenu.add(_miDBExtractCreateBayes);	
		extractDBMenu.add(_miDBExtractAddToClassifier);
		extractDBMenu.add(_miDBExtractLoadClassifier);
		extractDBMenu.add(_miDBExtractSaveClassifier);
		extractDBMenu.add(_miDBExtractClassifyDBP);
		extractDBMenu.addSeparator();
		extractDBMenu.add(_miDBExtractCreateDBPWeka);
		extractDBMenu.add(_miDBExtractLoadDBPWeka);
		extractDBMenu.add(_miDBExtractSaveDBPWeka);
		extractDBMenu.add(_miDBExtractClassifyDBPWeka);
		extractDBMenu.addSeparator();
		extractDBMenu.add(_miDBExtractEvaluateDB);
		extractDBMenu.add(_miDBExtractEvalDocComplete);
		extractDBMenu.addSeparator();
		extractDBMenu.add(_miDBExtractReportDBPatterns);
		extractDBMenu.add(_miDBExtractRptUndiscDB);
		extractDBMenu.add(_miDBExtractReportOnHold);		
		
		mappingMenu.add(_miMappingStart);
		mappingMenu.add(_miMappingResolveSubj); 
		mappingMenu.add(_miMappingResolveObj);  
		mappingMenu.add(_miMappingAttribute);
		mappingMenu.add(_miMappingMergeRules);  
		mappingMenu.add(_miMappingLoadDBTables); 
		mappingMenu.add(_miMappingMapDBTables);  
		mappingMenu.add(_miMappingRptTracability); 
		mappingMenu.add(_miMappingRptConflict);     
		mappingMenu.add(_miMappingRptSQLAC);   
		mappingMenu.add(_miMappingRptMongoDBAC); 
		
		_miAbout.addActionListener(  new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(null, GCConstants.VERSION_INFORMATION, "Access Control Relation Extractor", JOptionPane.PLAIN_MESSAGE); 

            }
        });
				
		_jtfStatus.setEditable(false);
		_jtfStatus.setText("");
		
		this.getContentPane().setLayout(_layout);
		
		_sentenceClassifier = new SentenceDisplayPanel();
		_jpCurrentPanel = _sentenceClassifier;
		
		this.getContentPane().add(_jpCurrentPanel,BorderLayout.CENTER);
		this.getContentPane().add(_jtfStatus, BorderLayout.SOUTH);
	}
	
	public SentenceDisplayPanel getSentenceClassifer() {
		return _sentenceClassifier;
	}
	
	public void setStatusMessage(String s) {
		_jtfStatus.setText(s);
	}
	
	public boolean isSupervisedLearningChecked() {
		return _jcbmiSupervisedLearning.isSelected();
	}

	public boolean isAutocompleteChecked() {
		return _jcbmiSupervisedLearning.isSelected();
	}	
}
