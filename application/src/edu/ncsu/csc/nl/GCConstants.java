package edu.ncsu.csc.nl;


public class GCConstants {

	public static final String VERSION_INFORMATION = "Access Control Relation Extractor, verion 0.5";
	
	public static final int VIEW_CLASSIFICATIONS = 1;
	public static final int VIEW_ANNOTATIONS = 2;
	
	public static final String ACTION_PARSE_NEW_SENTENCE = "ParseNewSentence";
	public static final String ACTION_DICTIONARY_VIEW = "ViewDict";
	public static final String ACTION_DICTIONARY_LOAD = "LoadDict";
	public static final String ACTION_DICTIONARY_SAVE = "SaveDict";
	public static final String ACTION_DICTIONARY_REPORT = "ReportDict";
	public static final String ACTION_LOAD_PIPELINE   = "LoadPipe";
	public static final String ACTION_LEARNER_CLEAR   = "ClearLearner";
	public static final String ACTION_LEARNER_DUMP    = "DumpLearner";
	public static final String ACTION_LEARNER_LOAD    = "LoadLearner";
	public static final String ACTION_LEARNER_SAVE    = "SaveLearner";
	public static final String ACTION_LEARNER_LOAD_SERIAL = "LoadLearnerSerial";
	public static final String ACTION_LEARNER_SAVE_SERIAL = "SaveLearnerSerial";
	public static final String ACTION_LEARNER_SETK    = "setklearner";
	public static final String ACTION_LEARNER_SELFEVAL = "selfevallearner";
	public static final String ACTION_LEARNER_DOCUMENT_EVAL = "documentEvalLearner";
	public static final String ACTION_LEARNER_INTERNAL_NB   = "internalNBEvaluation";
	
	public static final String ACTION_LEARNER_SELFEVAL_ANNOTATIONS = "selfevallearnerAnnotation";
	public static final String ACTION_LEARNER_DOCUMENT_EVAL_ANNOTATIONS = "documentEvalLearnerAnnotation";
	public static final String ACTION_LEARNER_CURRENT_EVAL_ANNOTATIONS = "currentEvalLearnerAnnotation";

	
	public static final String ACTION_LEARNER_MOVE_TO_CURRENT_DOC = "learnerMoveToCurrentDoc";

	public static final String ACTION_LEARNER_ADDTRAIN = "AddTrainSent";
	public static final String ACTION_LEARNER_SUPERON  = "LearnSupperOn";
	public static final String ACTION_LEARNER_SUPEROFF = "LearnSupperOff";
	
	public static final String ACTION_LEARNER_EXPORT_SOA_SENTENCES = "LNExportSOA";
	public static final String ACTION_LEARNER_PRODUCE_SOA_MATRIX = "LNSOA_MATRIX";
	public static final String ACTION_LEARNER_PRODUCE_SOA_FULL = "LNSOA_fullrpt";
	
	
	public static final String ACTION_WEKA_COMPUTE_INFO_GAIN_ANNOTATIONS     = "wekaInformationGainAnnotations";
	public static final String ACTION_WEKA_COMPUTE_INFO_GAIN_CLASSIFICATIONS = "wekaInformationGainClassifictions";
	public static final String ACTION_WEKA_EVAL_NAIVE_BAYES   = "WekaNaiveBayes";
	public static final String ACTION_WEKA_EVAL_SMO           = "WekaSMO";
	public static final String ACTION_WEKA_CURRENT_DOC_NAIVE_BAYES   = "WekaCurrDocNaiveBayes";
	public static final String ACTION_WEKA_CURRENT_DOC_SMO           = "WekaCurrDocSMO";
	public static final String ACTION_WEKA_SOA_EVAL_NAIVE_BAYES        = "WekaSOANaiveBayes";
	public static final String ACTION_WEKA_SOA_EVAL_SMO           	   = "WekaSOASMO";
	public static final String ACTION_WEKA_SOA_CURRENT_DOC_NAIVE_BAYES = "WekaSOACurrDocNaiveBayes";
	public static final String ACTION_WEKA_SOA_CURRENT_DOC_SMO         = "WekaSOACurrDocSMO";	
	public static final String ACTION_WEKA_CREATE_CLASSIFIERS = "WekacreateClass";
	public static final String ACTION_WEKA_CLASSIFY_CURRENT   = "WekaClassifycurrentSent";
	
	public static final String ACTION_DOCUMENT_NEW                = "actDocNew";
	public static final String ACTION_DOCUMENT_LOAD_JSON          = "actDocLoadJson";
	public static final String ACTION_DOCUMENT_LOAD_SERIAL        = "actDocLoadSer";
	public static final String ACTION_DOCUMENT_LOAD_TEXT_DOCUMENT = "actDocLoadText";
	public static final String ACTION_DOCUMENT_APPEND_JSON        = "actDocAppendJSON";
	public static final String ACTION_DOCUMENT_SAVE_JSON          = "actDocSaveJSON";
	public static final String ACTION_DOCUMENT_SAVE_SERIAL        = "actDocSaveSerial";
	public static final String ACTION_DOCUMENT_EXPORT_ARFF        = "actDocExportARFF";
	public static final String ACTION_DOCUMENT_EXIT               = "actDocExit";

	public static final String ACTION_DOCUMENT_COMPARE_JSON       = "actDocCompJSON";
	
	public static final String ACTION_DOCUMENT_GOTO_LINE          = "actiongotoLine";
	public static final String ACTION_DOCUMENT_GOTO_NEXT_UNCLASS  = "actiongotonextunclass";
	public static final String ACTION_DOCUMENT_GOTO_NEXT_ACCESS   = "actiongotonextunAccessDefined";
	public static final String ACTION_DOCUMENT_GOTO_NEXT_DATABASE = "actiongotonextunDatabaseDefined";	
	public static final String ACTION_DOCUMENT_GOTO_NEXT_SOA      = "actiongotonextunSOADefined";


	public static final String ACTION_DOCUMENT_SET_ID             = "docsetID";
	public static final String ACTION_DOCUMENT_RENUMBER           = "docRenumberMe";

	public static final String ACTION_REPORT_VALIDATION                  = "reportValidation";
	public static final String ACTION_REPORT_DOCUMENT_STATISTICS         = "DOCUMENTCurrREport";
	public static final String ACTION_REPORT_FREQUENCY                   = "docactionfreqreport";
	public static final String ACTION_REPORT_FREQUENCY_BY_CLASSIFICATION = "docactionfreqreportclass";
	public static final String ACTION_REPORT_FREQUENCY_SPREADSHEET       = "docactionfreqspreadsheet";	
	public static final String ACTION_REPORT_CLASSIFICATION_SENTENCES    = "reprotClassificationSentences";
	//public static final String ACTION_REPORT_ACCESS_CONTROL_PATTERNS     = "reportActionACPatterns";
	public static final String ACTION_REPORT_ANNOTATION_SENTENCES        = "reportActionAnotationSentences";	
	public static final String ACTION_REPORT_ANNOTATION_KEYWORD_FREQ_SS  = "rptAnnotateKeywordFreqSpreadsheet";
	public static final String ACTION_REPORT_CUSTOM                      = "reportCustomRpt";
	
	public static final String ACTION_POPUP_REMOVE_SENTENCE  = "PopupRemoveSentence";
	public static final String ACTION_POPUP_REPLACE_SENTENCE = "ActReplaceSentence";
	public static final String ACTION_POPUP_REPARSE_SENTENCE = "popActReparseSentence";
	
	public static final String ACTION_OTHER_POS_OVERRIDES      = "OTHPOSoverRides";
	public static final String ACTION_OTHER_WORDNET_BROWSER    = "OTHWordNetBrowser";
	public static final String ACTION_OTHER_VIEW_CLASS_RESULTS = "OTHViewClassResults";
	public static final String ACTION_OTHER_OPTIONS            = "OTHOptions";
	public static final String ACTION_OTHER_FIND               = "OTHFind";
	public static final String ACTION_OTHER_FIND_NEXT          = "OTHFindNext";
	public static final String ACTION_OTHER_MARK_FOR_ACDL      = "OTHMarkSOAACDL";
	public static final String ACTION_OTHER_CLUSTER            = "OTHcluster";
	public static final String ACTION_OTHER_ALL_SELF_EVALUATE  = "OTHallSelfEval";
	public static final String ACTION_OTHER_ALL_DOC_EVALUATE  = "OTHallDocEval";
	public static final String ACTION_OTHER_CURRENT_DOC_EVALUATE  = "OTHallCURRENTDocEval";
	
	public static final String ACTION_ACEXTRACT_BOOTSTRAP            = "actACE_bootstrap";
	public static final String ACTION_ACEXTRACT_SEARCH_FOR_PATTERNS  = "actACE_searchForPatterns";
	public static final String ACTION_ACEXTRACT_TRANSFORM_PATTERNS   = "actACE_transform";
	public static final String ACTION_ACEXTRACT_EXPAND_PATTERNS      = "actACE_expandPatterns";
	public static final String ACTION_ACEXTRACT_EXTRACT_AC_CONTROL   = "actACE_extract";
	public static final String ACTION_ACEXTRACT_DETECT_NEGATIVITY    = "actACE_detectNegativity";
	public static final String ACTION_ACEXTRACT_MOVE_AC_TO_HOLD      = "actACE_move_ac_to_hold";
	public static final String ACTION_ACEXTRACT_RESTORE_AC_RELATIONS = "actACE_restore_ac";
	public static final String ACTION_ACEXTRACT_EVALUATE_AC          = "actACE_evaluate";
	public static final String ACTION_ACEXTRACT_REPORT_AC_PATTERNS   = "actACE_rpt_patterns";	
	public static final String ACTION_ACEXTRACT_RESET_ACRM           = "actACE_resetACRM";
	public static final String ACTION_ACEXTRACT_ADD_HOLD_TO_ACTIVE   = "actACE_copyholdtoactive";
	public static final String ACTION_ACEXTRACT_CYCLE_PROCESS        = "actACE_cycleProcess";
	public static final String ACTION_ACEXTRACT_REPORT_ON_HOLD       = "actACE_reportOnHold";
	public static final String ACTION_ACEXTRACT_ANALYZE_PATTERNS     = "actACE_analyzePatterns";
	public static final String ACTION_ACEXTRACT_CREATE_BAYES         = "actACE_createBayesClassifier";
	public static final String ACTION_ACEXTRACT_CLASSIFY_ACP         = "actACE_classifyAccessControlPatterns";
	public static final String ACTION_ACEXTRACT_CREATE_ACP_WEKA      = "actACE_createAccessControlPatternsWEKA";
	public static final String ACTION_ACEXTRACT_LOAD_ACP_WEKA        = "actACE_loadAccessControlPatternsWEKA";
	public static final String ACTION_ACEXTRACT_SAVE_ACP_WEKA        = "actACE_saveAccessControlPatternsWEKA";
	public static final String ACTION_ACEXTRACT_CLASSIFY_ACP_WEKA    = "actACE_classifyAccessControlPatternsWEKA";
	public static final String ACTION_ACEXTRACT_EVAL_DOC_COMPLETION  = "actACE_evaluatePerformanceOnDocumentCompletion";
	public static final String ACTION_ACEXTRACT_LOAD_NB_CLASSIFIER  = "actACE_loadNBClassifier";
	public static final String ACTION_ACEXTRACT_SAVE_NB_CLASSIFIER   = "actACE_saveNBClassifier";
	public static final String ACTION_ACEXTRACT_ADD_TO_NB_CLASSIFIER = "actACE_AddToNBClassifier";
	public static final String ACTION_ACEXTRACT_ANALYZE_FACTORS      = "actACE_analyze_factos";
	public static final String ACTION_ACEXTRACT_REPORT_UNDISCOVERED_PATTERNS = "actACE_report_undisc_Fact";
	public static final String ACTION_ACEXTRACT_INJECT_PATTERN  = "actACE_inject_action_object_pattern";
	
	public static final String ACTION_DBEXTRACT_BOOTSTRAP            = "actDBE_bootstrap";
	public static final String ACTION_DBEXTRACT_SEARCH_FOR_PATTERNS  = "actDBE_searchForPatterns";
	public static final String ACTION_DBEXTRACT_TRANSFORM_PATTERNS   = "actDBE_transform";
	public static final String ACTION_DBEXTRACT_EXPAND_PATTERNS      = "actDBE_expandPatterns";
	public static final String ACTION_DBEXTRACT_EXTRACT_DATABASE_OBJ = "actDBE_extract";
	public static final String ACTION_DBEXTRACT_MOVE_DB_TO_HOLD      = "actDBE_move_db_to_hold";
	public static final String ACTION_DBEXTRACT_RESTORE_DB_RELATIONS = "actDBE_restore_ac";
	public static final String ACTION_DBEXTRACT_EVALUATE_DB          = "actDBE_evaluate";
	public static final String ACTION_DBEXTRACT_REPORT_DB_PATTERNS   = "actDBE_rpt_patterns";	
	public static final String ACTION_DBEXTRACT_RESET_DBRM           = "actDBE_resetDBRM";
	public static final String ACTION_DBEXTRACT_ADD_HOLD_TO_ACTIVE   = "actDBE_copyholdtoactive";
	public static final String ACTION_DBEXTRACT_CYCLE_PROCESS        = "actDBE_cycleProcess";
	public static final String ACTION_DBEXTRACT_REPORT_ON_HOLD       = "actDBE_reportOnHold";
	public static final String ACTION_DBEXTRACT_ANALYZE_PATTERNS     = "actDBE_analyzePatterns";
	public static final String ACTION_DBEXTRACT_CREATE_BAYES         = "actDBE_createBayesClassifier";
	public static final String ACTION_DBEXTRACT_CLASSIFY_DBP         = "actDBE_classifyDatabasePatterns";
	public static final String ACTION_DBEXTRACT_CREATE_DBP_WEKA      = "actDBE_createdDatabasePatternsWEKA";
	public static final String ACTION_DBEXTRACT_LOAD_DBP_WEKA        = "actDBE_loadDatabasePatternsWEKA";
	public static final String ACTION_DBEXTRACT_SAVE_DBP_WEKA        = "actDBE_saveDatabasePatternsWEKA";
	public static final String ACTION_DBEXTRACT_CLASSIFY_DBP_WEKA    = "actDBE_classifyDatabasePatternsWEKA";
	public static final String ACTION_DBEXTRACT_EVAL_DOC_COMPLETION  = "actDBE_evaluatePerformanceOnDocumentCompletion";
	public static final String ACTION_DBEXTRACT_LOAD_NB_CLASSIFIER   = "actDBE_loadNBClassifier";
	public static final String ACTION_DBEXTRACT_SAVE_NB_CLASSIFIER   = "actDBE_saveNBClassifier";
	public static final String ACTION_DBEXTRACT_ADD_TO_NB_CLASSIFIER = "actDBE_AddToNBClassifier";
	public static final String ACTION_DBEXTRACT_ANALYZE_FACTORS      = "actDBE_analyze_factos";
	public static final String ACTION_DBEXTRACT_REPORT_UNDISCOVERED_PATTERNS = "actDBE_report_undisc_Fact";
	public static final String ACTION_DBEXTRACT_INJECT_PATTERN        = "actDBE_inject_action_object_pattern";	
	
	public static final String ACTION_MAPPING_START            = "actMapStart";
	public static final String ACTION_MAPPING_ATTRIBUTE        = "actMapAttribute";
	public static final String ACTION_MAPPING_RESOLVE_SUBJECTS = "actMapResolveSubject";
	public static final String ACTION_MAPPING_RESOLVE_OBJECTS  = "actMapResolveObject";
	public static final String ACTION_MAPPING_MERGE_RULES      = "actMapMergeRules";
	public static final String ACTION_MAPPING_LOAD_DB_TABLES   = "actMapLoadDBList";
	public static final String ACTION_MAPPING_MAP_DB_TABLES    = "actMapDB";
	public static final String ACTION_MAPPING_RPT_TRACEABILITY = "actMapRptTrace";
	public static final String ACTION_MAPPING_RPT_CONFLICT     = "actMapRptConflic";
	public static final String ACTION_MAPPING_RPT_SQL_AC       = "actMapRptSQL";
	public static final String ACTION_MAPPING_RPT_MONGODB_AC   = "actMapRptMongo";

	
	
	
	public static final String ACTION_OTHER_VERB_FREQUENCIES             = "OTHbootitbaby";
	public static final String ACTION_OTHER_RESTORE_AC_RELATIONS  = "OTHrestoreACrelations";
	
	public static final String ACTION_SOA_CONFIDENTIALITY = "SOAaction_C";
	public static final String ACTION_SOA_INTEGRITY       = "SOAaction_I";
	public static final String ACTION_SOA_AVAILABILITY    = "SOAaction_A";
	public static final String ACTION_SOA_AC_IDENTITY     = "SOAaction_IA";
	public static final String ACTION_SOA_AUTHORIZATION   = "SOAaction_AU";
	public static final String ACTION_SOA_LOGGING         = "SOAaction_LG";
	public static final String ACTION_SOA_NONREPUDIATION  = "SOAaction_NR";
	public static final String ACTION_SOA_PRIVACY         = "SOAaction_PR";
	public static final String ACTION_SOA_DATABASE        = "SOAaction_db";
	public static final String ACTION_SOA_MARK_COMPLETE   = "SOAMarkComplete";
	public static final String ACTION_SOA_CONVERT         = "SOAConvertToNewFormat";
	
	public static final String ACTION_CLASS_MARK_COMPLETE  = "CLASSaction_mark_complete";
	public static final String ACTION_CLASS_MARK_DB_FT     = "CLASSaction_mark_DB_FT_complete";
	public static final String ACTION_CLASS_MARK_ACF_DB_FT = "CLASSaction_mark_ACF_DB_FT_complete";
	
	
	public static final int UNDEFINED = -2;
	
	public static final String CLASSIFICATION_ACCESS_CONTROL_FUNCTIONAL = "access control:functional";
}
