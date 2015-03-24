package edu.ncsu.csc.nl;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import edu.ncsu.csc.nl.model.NLDocument;
import edu.ncsu.csc.nl.model.SecurityObjectiveAnnotation;
import edu.ncsu.csc.nl.model.Sentence;
import edu.ncsu.csc.nl.model.WordVertex;
import edu.ncsu.csc.nl.model.classification.BooleanClassification;
import edu.ncsu.csc.nl.model.classification.ClassificationAttribute;
import edu.ncsu.csc.nl.model.classification.ClassificationType;
import edu.ncsu.csc.nl.model.ml.Document;
import edu.ncsu.csc.nl.model.relation.AccessControlRelation;
import edu.ncsu.csc.nl.model.relation.AccessControlRelationManager;
import edu.ncsu.csc.nl.model.relation.DatabaseRelation;
import edu.ncsu.csc.nl.model.type.DatabaseRelationType;
import edu.ncsu.csc.nl.model.type.SecurityObjective;


/**
 * 
 * 
 * @author Adminuser
 */
public class ReportController implements ActionListener {
	private static ReportController _theReportController = new ReportController();
	public static ReportController getTheReportController() {return _theReportController; }
	
	
	private ReportController() {	}


	@Override
	public void actionPerformed(ActionEvent ae) {
		GCController.getTheGCController().setStatusMessage("");
		
		switch (ae.getActionCommand()) {
			case GCConstants.ACTION_REPORT_VALIDATION: produceValidationReport(); return;
			case GCConstants.ACTION_REPORT_DOCUMENT_STATISTICS: GCController.getTheGCController().getCurrentDocument().produceReport(); return;
			case GCConstants.ACTION_REPORT_FREQUENCY: produceFrequencyReport(); return;
			case GCConstants.ACTION_REPORT_FREQUENCY_BY_CLASSIFICATION: produceFrequencyByClassReport(); return;
			case GCConstants.ACTION_REPORT_FREQUENCY_SPREADSHEET: produceFrequencySpreadsheetReport(); return;
			case GCConstants.ACTION_REPORT_CLASSIFICATION_SENTENCES: produceSentencesByClassificationReport(); return;
			case GCConstants.ACTION_REPORT_ANNOTATION_SENTENCES: produceSentencesByAnnotationReport(); return;
			case GCConstants.ACTION_REPORT_ANNOTATION_KEYWORD_FREQ_SS: produceAnnotationFrequencySpreadsheetReport(); return;
			case GCConstants.ACTION_REPORT_CUSTOM: customReport(); return;
		}
	}
	
	public void produceFrequencyReport() {
		java.util.HashMap<String,Integer> frequency = GCController.getTheGCController().getCurrentDocument().produceWordCount();
		edu.ncsu.csc.nl.util.Utility.printFrequencyTable(frequency, new PrintWriter(System.out),null);
	}
	
	public void produceFrequencyByClassReport() {
		for (ClassificationAttribute ca: GCController.getTheGCController().getClassificationAttributes().getAttributeList()) {
			java.util.HashMap<String,Integer> frequency = GCController.getTheGCController().getCurrentDocument().produceWordCountForClassification(ca);
			edu.ncsu.csc.nl.util.Utility.printFrequencyTable(frequency, new PrintWriter(System.out),ca.getName());
		}		
	}
	
	public void produceFrequencySpreadsheetReport() {
		NLDocument currentDocument = GCController.getTheGCController().getCurrentDocument();
		java.util.List<ClassificationAttribute> classificationAttributeList = GCController.getTheGCController().getClassificationAttributes().getAttributeList();
		
		double numTrainedSentences = currentDocument.getNumberOfTrainedSentences();
		java.util.HashMap<String,Integer> frequency = currentDocument.produceWordCount();
		java.util.HashMap<ClassificationAttribute, HashMap<String,Integer>> wordCountByClass = new java.util.HashMap<ClassificationAttribute, HashMap<String,Integer>>();
		java.util.HashMap<String,Integer> wordCountNonApplicableClass = currentDocument.produceWordCountForClassification(null); 
				
		for (ClassificationAttribute ca: classificationAttributeList) {
			wordCountByClass.put(ca,  currentDocument.produceWordCountForClassification(ca));
		}	
		
		// Now produce the count of classifications for each category
		HashMap<String,Double> classifiedCount = new HashMap<String,Double>();
		double numberOfNotApplicableSentences = 0.0;
		for (Sentence s: currentDocument.getSentences()) {
			if (s.isTrained()) {
				String[] classifications = s.getBooleanClassificationsAsStringArray();			
				for (String key: classifications) {
					double count = classifiedCount.containsKey(key) ? classifiedCount.get(key) : 0.0;
					classifiedCount.put(key, count + 1.0);
				}
				if (classifications.length == 0) {
					numberOfNotApplicableSentences++;
				}
			}
		}
		
		//sort the words alphabetically
		ArrayList<String> words = new ArrayList<String>( frequency.keySet());
		Collections.sort(words);

		//print header
		System.out.print("Word\tTotalFrequency\tSum TFIDF");//\tNA Count\tNA Freq\tNA TF-IDF");
		for (ClassificationAttribute ca: classificationAttributeList) {
			System.out.print("\t");
			System.out.print(ca.getName()+" Count");
			System.out.print("\t");
			System.out.print(ca.getName()+" Freq");
			System.out.print("\t");
			System.out.print(ca.getName()+" TF-IDF");
			System.out.print("\t");
			System.out.print(ca.getName()+" Normalized TF-IDF");
		}
		System.out.println("");
		
		//print body
		for (String word: words) {
			// compute the number of documents containing the word.
			double numDocumentsContainingWord = 0.0;
			for (Sentence s: currentDocument.getSentences()) {
				if (s.isTrained() && s.hasLemma(word)) {
					numDocumentsContainingWord ++;					
				}
			}
			double idf = Math.log10(numTrainedSentences/numDocumentsContainingWord);
			
			System.out.print(word);
			System.out.print("\t");
			System.out.print(frequency.get(word));  // Total Frequency of word in document
			
			System.out.print("\t");
			double sumTF_IDF = 0.0;
			/*
			double naTermFrequency =  ((wordCountNonApplicableClass.containsKey(word)? wordCountNonApplicableClass.get(word):0));
			naTermFrequency  = naTermFrequency/frequency.get(word);
			
			System.out.print((wordCountNonApplicableClass.containsKey(word)? wordCountNonApplicableClass.get(word):0));  //Frequency of word in Na sentences
			System.out.print("\t");
			System.out.print(naTermFrequency);
			System.out.print("\t");
			
			double naTF_IDF = naTermFrequency*idf;
			 sumTF_IDF =naTF_IDF;
			System.out.print(naTF_IDF);
			*/
			for (ClassificationAttribute ca: classificationAttributeList) {
				java.util.HashMap<String,Integer> classCount = wordCountByClass.get(ca);
				if (classCount.containsKey(word)) {
					double tf = classCount.get(word)/classifiedCount.get(ca.getName());
					double tfIDF = tf*idf;
					sumTF_IDF += tfIDF;
				}
			}
			System.out.print("\t");
			System.out.print(sumTF_IDF);
			for (ClassificationAttribute ca: classificationAttributeList) {
				System.out.print("\t");
			
				java.util.HashMap<String,Integer> classCount = wordCountByClass.get(ca);
				if (classCount.containsKey(word)) {
					System.out.print(classCount.get(word));
					System.out.print("\t");
					double tf = classCount.get(word)/classifiedCount.get(ca.getName());
					System.out.print(tf);
					System.out.print("\t");
					
					double tfIDF = tf*idf;
					System.out.print(tfIDF);
					System.out.print("\t");
					System.out.print(tfIDF/sumTF_IDF);
				}
				else {
					System.out.print("0\t0\t0\t0");
				}
				
			}			
			
			
			
			System.out.println();
		}
	}
	


	/**
	 * This report will produce a list of 20% of the document to be as validation for another
	 * individual to manual examine the data.
	 */
	public void produceValidationReport() { 
		
		ArrayList<Sentence> sentences = GCController.getTheGCController().getCurrentDocument().getSentences();
		
		int numSentences = sentences.size();
		
		HashSet<Integer> positionsToValidate = new HashSet<Integer>();
		SecureRandom sr = new SecureRandom();
		
		int numSentencesToValidate = 50;  //  .2 * sentences.size();  -- for doing a percentage of the document
		
		while (positionsToValidate.size() < numSentencesToValidate) {
			positionsToValidate.add( sr.nextInt(sentences.size()));
		}
		
		int numberOfACRs = 0;
		int numberOfDDEs = 0;
		int numberOfEnitities = 0;
		int numberOfEntityAttributes = 0;
		int numberOfRelationships = 0;
		for (int i = 0; i < numSentences; i++ ) {
			Sentence s = sentences.get(i);
			
 			System.out.print(s.getOriginalSentencePosition());
 			if (positionsToValidate.contains(i)) { System.out.print("(VALIDATE)"); }
 			System.out.print("-"+s.getSentenceType());
 			System.out.println(": "+ s);
			System.out.println("\tAccess Control: functional: " + s.hasBooleanClassification("access control:functional"));
			
			List<AccessControlRelation> accessRelations=  s.getAccessControlRelations();
			for (AccessControlRelation ar: accessRelations) { System.out.println("\t\t"+ar + " - " + ar.getPermissions()); numberOfACRs++; }
			
			System.out.println("\tDatabase Design: "+s.hasBooleanClassification("database design"));
			List<DatabaseRelation> dbRelations=  s.getDatabaseRelations();
			for (DatabaseRelation dr: dbRelations) { 
				System.out.print("\t\t"+dr + " - ");
			
				if (dr.getRelationType() == DatabaseRelationType.ENTITY) {
					System.out.println("entity");
					numberOfEnitities++;
				}
				else if (dr.getRelationType() == DatabaseRelationType.ENTITY_ATTR) {
					System.out.println("entity-attribute");
					numberOfEntityAttributes++;
				}
				else if (dr.getRelationType() == DatabaseRelationType.RELATIONSHIP) {
					System.out.println("relationship:"+dr.getRelationshipType());
					numberOfRelationships++;
				}			
				numberOfDDEs++;
				
				
			}
		}
		System.out.println("Total number of ACRs: "+ numberOfACRs);
		System.out.println("Total number of DDEs: "+ numberOfDDEs);
		
		System.out.println("Total number of Entities: "+ numberOfEnitities);
		System.out.println("Total number of Entity Attributes: "+ numberOfEntityAttributes);
		System.out.println("Total number of Relationships: "+ numberOfRelationships);
	}
	
	
	/**
	 * 
	 * @param al list of classifications 
	 */
	public void produceSentencesByClassificationReport() {  
			
		
		System.out.println("=====================================");
		java.util.List<ClassificationAttribute> classificationAttributeList = GCController.getTheGCController().getClassificationAttributes().getAttributeList();
		for (ClassificationAttribute ca: classificationAttributeList) {
			for (Sentence s:  GCController.getTheGCController().getCurrentDocument().getSentences()) {
				if (s.isTrained()) {
					ClassificationType ct = s.getClassifications().get(ca.getName());
					if (ct != null && ct instanceof BooleanClassification && ((BooleanClassification) ct).getValue().getBooleanValue()) {
						System.out.println(ca.getName()+"\t"+s);
					}
				}
			}
		}		
		// now display sentences with no classifications
		for (Sentence s: GCController.getTheGCController().getCurrentDocument().getSentences()) {
			if (s.isTrained() && s.hasBooleanClassifications() == false) {
				System.out.println("none\t"+s);
			}
		}
	}	
		
	public void produceSentencesByAnnotationReport() {
		System.out.println("Annotation Report");
		int countSOA = 0;
		int total    = 0;
		for (Sentence s: GCController.getTheGCController().getCurrentDocument().getSentences()) {
			total++;
			if (s.isSecurityObjectiveAnnotationsDefined()) {countSOA++; }
		}
		System.out.println("Annotation Progress: " + countSOA+" /" + total);
		
		System.out.println("=====================================");
		SecurityObjective[] objectives = SecurityObjective.getSelectableList();
		for (SecurityObjective so: objectives) {
			for (Sentence s:  GCController.getTheGCController().getCurrentDocument().getSentences()) {
				if (s.isSecurityObjectiveAnnotationsDefined()) {
					if (s.hasSecurityObjectiveAnnotation(so)) {
						System.out.println(so.toString()+"\t"+s);
					}
				}
			}
		}		
		// now display sentences with no Security Objective Annotations
		for (Sentence s: GCController.getTheGCController().getCurrentDocument().getSentences()) {
			if (s.isSecurityObjectiveAnnotationsDefined() && s.getNumberOfSecurityObjectiveAnnotations() == 0) {
				System.out.println("none\t"+s);
			}
		}
	}	
	
	
	public void customReport() {
		NLDocument doc =  GCController.getTheGCController().getCurrentDocument();
		System.out.println("Document size: "+doc.getNumberOfSentences());
		
		int count1=0;
		for (Sentence s: doc.getSentences()) {
			if (s.isAccessControlDefined()) {count1++;}
		}
		System.out.println("access control Trained: "+count1);
		
		JFileChooser fileChooser  = new JFileChooser();

		int returnVal = fileChooser.showSaveDialog(null);
		if (returnVal != JFileChooser.APPROVE_OPTION) { return; }
		
		File f = fileChooser.getSelectedFile();
		try (PrintWriter pw = new PrintWriter(f)) {
			String documentID = GCController.getTheGCController().getCurrentDocument().getElementAt(0).getDocumentID();
			
			pw.println("DocumentID\tSecurityObjective\tType\tWord\tCount");
			
			List<SecurityObjective> objectives = SecurityObjective.getMinimalListForClassification();
			for (SecurityObjective so: objectives) {
				HashMap<String, Integer> verbs = new HashMap<String, Integer>();
				HashMap<String, Integer> nouns = new HashMap<String, Integer>();
				
				for (Sentence s:  GCController.getTheGCController().getCurrentDocument().getSentences()) {
					if (!s.hasSecurityObjectiveAnnotation(so)) { continue; }
					
					for (String verb: s.retrieveAllVerbs()) {
						int count = verbs.containsKey(verb) ? verbs.get(verb) : 0;
						verbs.put(verb, count + 1);
					}
					
					for (String noun: s.retrieveAllNouns()) {
						int count = nouns.containsKey(noun) ? nouns.get(noun) : 0;
						nouns.put(noun, count + 1);
					}
				} // end of marching through the document for the particular objective
				for (String word: verbs.keySet()) {
					pw.print(documentID);
					pw.print("\t");
					pw.print(so.toString());
					pw.print("\tverb\t");
					pw.print(word);
					pw.print("\t");
					pw.print(verbs.get(word));
					pw.println();
				}
				for (String word: nouns.keySet()) {
					pw.print(documentID);
					pw.print("\t");
					pw.print(so.toString());
					pw.print("\tnoun\t");
					pw.print(word);
					pw.print("\t");
					pw.print(nouns.get(word));
					pw.println();
				}
			}
		}
		catch (Exception e) {
			System.err.println("Custom Report: "+e);
		}
		
		
			
	}
	
	public void produceAnnotationFrequencySpreadsheetReport() {
		JFileChooser fileChooser  = new JFileChooser();

		int returnVal = fileChooser.showSaveDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File f = fileChooser.getSelectedFile();
			try {
				this.produceAnnotationFrequencySpreadsheetReport(f);
				GCController.getTheGCController().setStatusMessage("Annotation Frequency Report generated");
			}
			catch (Exception e) {
				GCController.getTheGCController().setStatusMessage("Unable to generate annotation frequency report to "+f.getAbsolutePath());
				System.err.println("Unable to generate annotation frequency report : "+e);
				e.printStackTrace();
			}
		}	
	}
	
	
	public void produceAnnotationFrequencySpreadsheetReport(File outputFile) throws Exception {
		PrintWriter pw = new PrintWriter( new OutputStreamWriter(new FileOutputStream(outputFile), Charset.forName("UTF-8")));   // if not specified, uses windows-1552
	
		NLDocument currentDocument = GCController.getTheGCController().getCurrentDocument();
		java.util.List<SecurityObjective> annotationList = SecurityObjective.getListForClassification();
		
		int numTrainedSentences = currentDocument.getNumberOfSentences();//currentDocument.getNumberOfSecurityObjectiveAnnotationsDefined();
		java.util.HashMap<String,Integer> frequency = currentDocument.produceWordCount();
		java.util.HashMap<SecurityObjective, HashMap<String,Integer>> wordCountByClass = new java.util.HashMap<SecurityObjective, HashMap<String,Integer>>();  // number of times each word appears for the given objective
		java.util.HashMap<SecurityObjective, HashMap<String,Integer>> sentenceCountForWordByClass = new java.util.HashMap<SecurityObjective, HashMap<String,Integer>>();  // number of sentences containing the particular word for an objective

		java.util.HashMap<String,Integer> wordCountNonApplicableClass = currentDocument.produceWordCountForAnnotation(null); 
				
		for (SecurityObjective so: annotationList) {
			wordCountByClass.put(so,  currentDocument.produceWordCountForAnnotation(so));
			sentenceCountForWordByClass.put(so,  currentDocument.produceSentenceForWordCountForAnnotation(so));
		}	
		
		// Now produce the count of annotations for each objective.  (ie, how many trained sentences have this objective?)
		HashMap<SecurityObjective,Double> annotationSentenceCount = new HashMap<SecurityObjective,Double>();
		for (SecurityObjective so: annotationList) {
			double count = 0.0;
			for (Sentence s: currentDocument.getSentences()) {
				if (s.isSecurityObjectiveAnnotationsDefined() && s.hasSecurityObjectiveAnnotation(so)) {
					count++;
				}
			}
			annotationSentenceCount.put(so, count);
		}
		
		double numberOfNotApplicableSentences = 0.0;
		for (Sentence s: currentDocument.getSentences()) {
			if (s.isSecurityObjectiveAnnotationsDefined() && s.getNumberOfAccessControlRelations() == 0) {
				numberOfNotApplicableSentences++;
			}
		}		
		
		//sort the words alphabetically
		ArrayList<String> words = new ArrayList<String>( frequency.keySet());
		Collections.sort(words);

		//print header
		pw.print("Word\tTotal Frequency\tNum Sentences\tNum Objectives\tIDF\tNA Sentence Count\tNA Freq\tNA TF-IDF");
		for (SecurityObjective so: annotationList) {
			pw.print("\t");
			pw.print(so+" Sentence Count");
			pw.print("\t");
			pw.print(so+" Term Freq");
			pw.print("\t");
			pw.print(so+" TF-IDF");
			pw.print("\t");
			pw.print(so+" TF-IDF*(1/num Objectives)");
		}
		pw.println("");
		
		//print body
		for (String word: words) {
			// compute the number of documents containing the word.
			double numDocumentsContainingWord = 0.0;
			double naDocumentsContainingWord  = 0.0;
			for (Sentence s: currentDocument.getSentences()) {
				if (s.isSecurityObjectiveAnnotationsDefined() && s.hasLemma(word)) {
					numDocumentsContainingWord ++;	
					if (s.getNumberOfSecurityObjectiveAnnotations() == 0) {naDocumentsContainingWord++;}
				}
			}
			double idf = Math.log10(numTrainedSentences/(double) numDocumentsContainingWord);
			
			// compute the number of objectives that has this word
			double numSecurityObjectivesHavingWord = 0.0;
			for (SecurityObjective so: annotationList) {
				if (wordCountByClass.get(so).get(word) != null) {
					numSecurityObjectivesHavingWord++;
				}
			}
			
			
			pw.print(word);
			pw.print("\t");

			pw.print(frequency.get(word));  // total # of times the word appears in the document
			pw.print("\t");

			pw.print(numDocumentsContainingWord);      // number of sentences 
			pw.print("\t");

			pw.print(numSecurityObjectivesHavingWord);
			pw.print("\t");
			
			pw.print(idf);
			pw.print("\t");

			pw.print(naDocumentsContainingWord);      // Number of documents with no security objective annotations that have this word
			pw.print("\t");

			pw.print((wordCountNonApplicableClass.containsKey(word)? wordCountNonApplicableClass.get(word):0));  //Frequency of word in Na sentences
			pw.print("\t");
			
			double naTF_IDF = (wordCountNonApplicableClass.containsKey(word)? wordCountNonApplicableClass.get(word):0)*idf;
			pw.print(naTF_IDF);

			for (SecurityObjective so: annotationList) {
				pw.print("\t");
			
				java.util.HashMap<String,Integer> classCount = wordCountByClass.get(so);
				if (classCount.containsKey(word)) {
					pw.print(sentenceCountForWordByClass.get(so).get(word));
					pw.print("\t");
					double tf = classCount.get(word) / annotationSentenceCount.get(so);
					pw.print(tf);
					pw.print("\t");
					double tfIDF = tf*idf;
					pw.print(tfIDF);
					pw.print("\t");
					
					double numSO = Math.max(numSecurityObjectivesHavingWord, 1.0);
					
					pw.print(tfIDF * 1.0/numSO); 
				}
				else {
					pw.print("0\t0\t0\t0");
				}
				
			}			
			
			pw.println();
		}
		pw.close();

	}
	
	public void exportSOA(java.io.File outputFile) throws java.io.IOException {
		PrintWriter pw = new PrintWriter( new OutputStreamWriter(new FileOutputStream(outputFile), Charset.forName("UTF-8")));   // if not specified, uses windows-1552
		
		//output header
		pw.print("DocumentID\tSentence");
		
		SecurityObjective[] objectives = SecurityObjective.getSelectableList();
		for (SecurityObjective so: objectives) {
			pw.print("\t"); pw.print(so.name());
			pw.print("\t"); pw.print(so.name()+"_implied");
			pw.print("\t"); pw.print(so.name()+"_impact");
			pw.print("\t"); pw.print(so.name()+"_mitigation");
			pw.print("\t"); pw.print(so.name()+"_indicative_phrase");
		}
		pw.println();
		
		//output rows
		for (Document d:GCController.getTheGCController().getInstanceLearner().getTrainedSentences()) {
			Sentence s = d.sentence;
			pw.print(s.getDocumentID()); pw.print("\t"); pw.print(s.getSentence());
			
			for (SecurityObjective so: objectives) {
				SecurityObjectiveAnnotation soa = s.getSecurityObjectiveAnnotation(so);
				if (soa == null) {
					pw.print("\t"); pw.print("FALSE");
					pw.print("\t"); pw.print(" ");
					pw.print("\t"); pw.print(" ");
					pw.print("\t"); pw.print(" ");
					pw.print("\t"); pw.print(" ");
				}
				else {
					if (soa.getSecurityImpact() == null || soa.getSecurityMitigation() == null || soa.getIndicativePhrase() == null) {
						System.err.println("Sentence Error: "+s.getDocumentID()+": "+s.getSentence());
						pw.print("\t"); pw.print("FALSE");
						pw.print("\t"); pw.print(" ");
						pw.print("\t"); pw.print(" ");
						pw.print("\t"); pw.print(" ");
						pw.print("\t"); pw.print(" ");
						continue;
					}
					pw.print("\t"); pw.print("TRUE");
					pw.print("\t"); pw.print(soa.isImplied());
					pw.print("\t"); pw.print(soa.getSecurityImpact().name());
					pw.print("\t"); pw.print(soa.getSecurityMitigation().name());
					pw.print("\t"); pw.print(soa.getIndicativePhrase());
					
				}
			}
			pw.println();
			
		}
		pw.close();
	}
	
	
	public List<List<SecurityObjective>> getCombinations(SecurityObjective[] objectives, int depth) {
		if (depth == 1) {
			List<List<SecurityObjective>> combinations = new LinkedList<List<SecurityObjective>>();
			for (SecurityObjective so: objectives) {
				ArrayList<SecurityObjective> item = new ArrayList<SecurityObjective>();
				item.add(so);
				combinations.add(item);
			}
			return combinations;
		}
		else {
			List<List<SecurityObjective>> combinations = this.getCombinations(objectives, depth-1);
			List<List<SecurityObjective>> newCombinations = new LinkedList<List<SecurityObjective>>();
			
			for (SecurityObjective so: objectives) {
				for (List<SecurityObjective> l: combinations) {
					if (l.size() < (depth-1)) {continue; } //only want to add only to previous depth,not all, otherwise dup
					if (l.contains(so)) { break; }
					
					List<SecurityObjective> newList = new ArrayList<SecurityObjective>(l);
					newList.add(so);
					newCombinations.add(newList);
					
				}
			}
			combinations.addAll(newCombinations);
			return combinations;
		}
	}


	
	String getKeyValueForSecurityObjectiveList(List<SecurityObjective> l) {
		if (l.size() == 0) { return ""; }

		String result = l.get(0).toString();
		
		for (int i=1;i<l.size(); i++) {
			result += "!";
			result += l.get(i).toString();
		}

		return result;
		
	}
	
	static class ValueComparator implements Comparator<String> {
	    Map<String, Integer> base;
	    public ValueComparator(Map<String, Integer> base) {
	        this.base = base;
	    }
	    public int compare(String a, String b) {
	    	 if (base.get(a) >= base.get(b)) {
	             return -1;
	         } else {
	             return 1;
	         }
	    }
	}
	
	public void produceSOAMatrix() {
		SecurityObjective[] objectives = SecurityObjective.getSelectableListWithoutDatabase();
		
        HashMap<String,Integer> counts = new HashMap<String,Integer>();
		
		for (int i=0; i < objectives.length - 1; i++) {
			for (int j=i+1; j < objectives.length; j++) {
				String key = objectives[i].getAbbreviation() + "!" + objectives[j].getAbbreviation();
			    int count = 0;
			    for (Document document: GCController.getTheGCController().getInstanceLearner().getTrainedSentences()) {
					if (document.sentence.getNumberOfSecurityObjectiveAnnotations() <= 1) { continue; }
					if ( !document.sentence.hasSecurityObjectiveAnnotation(objectives[i]) || 
					     !document.sentence.hasSecurityObjectiveAnnotation(objectives[j]) ) {continue;}
					count++;
				}
			    counts.put(key, count);
			}
		}
		
		// print header
		System.out.print(" \t "); // blank space for the column names and abbreviation
		for (int i=0; i < objectives.length ; i++) {
			System.out.print("\t"+objectives[i].getAbbreviation());
		}
		System.out.println();

		// Now, print each row
		for (int i=0; i < objectives.length ; i++) {
			System.out.print(objectives[i].toString()+"\t"+objectives[i].getAbbreviation());
		
			for (int j=0; j < objectives.length; j++) {		
				String key = objectives[i].getAbbreviation() + "!" + objectives[j].getAbbreviation();
				
				Integer count = counts.get(key);
				if (count == null) {
					System.out.print("\t-");
				}
				else {
					System.out.print("\t");
					System.out.print(count);
				}
			}
			System.out.println();
		}
		
	}
	
	
	
	
	public void produceSOAFullReport() {
		SecurityObjective[] objectives = SecurityObjective.getSelectableListWithoutDatabase();
		
		HashMap<Integer, Integer>  numObjectivesPerSentence = new HashMap<Integer, Integer>();
		for (int i=0;i<=objectives.length;i++) { numObjectivesPerSentence.put(i, 0); }
		for (Document document: GCController.getTheGCController().getInstanceLearner().getTrainedSentences()) {
			int annotationCountForSentence = document.sentence.getNumberOfSecurityObjectiveAnnotations();
			int currentCount = numObjectivesPerSentence.get(annotationCountForSentence) + 1;
			 numObjectivesPerSentence.put(annotationCountForSentence, currentCount);
		}
		System.out.println("NumObjectives\tNumSentencesOccurring");
		for (int i=0;i<=objectives.length;i++) {
			System.out.print(i);
			System.out.print("\t");
			System.out.println(numObjectivesPerSentence.get(i));
		}
		
		
		int combinationSize=2;
		String value = JOptionPane.showInputDialog(null,"Enter combination size: ","Enter Combination Size", JOptionPane.QUESTION_MESSAGE);
		if (value == null) {
			return;
		}
		else {	
			try {
				int temp = Integer.parseInt(value);
				if (temp >= 2 && combinationSize <= objectives.length) {
					combinationSize = temp;
				}
			}
			catch (Throwable t) {
				System.out.println("Bad value entered, defaulting to 2");
			}
		}
		
		List<List<SecurityObjective>> combinations = this.getCombinations(SecurityObjective.getSelectableListWithoutDatabase(),combinationSize);
		
		
        HashMap<String,Integer> counts = new HashMap<String,Integer>();
        ValueComparator bvc =  new ValueComparator(counts);
        TreeMap<String,Integer> sorted_map = new TreeMap<String,Integer>(bvc);
		
        for (List<SecurityObjective> combination: combinations) {
        	String key = getKeyValueForSecurityObjectiveList(combination);
            int count = 0;
		    for (Document document: GCController.getTheGCController().getInstanceLearner().getTrainedSentences()) {
		    	if (document.sentence.getNumberOfSecurityObjectiveAnnotations() <= 1) { continue; }
		    	boolean found = true;
		    	for (SecurityObjective so: combination) {
					if ( !document.sentence.hasSecurityObjectiveAnnotation(so)) { 
						found = false; 
						break;
					} 
		    	}
		    	if (found) { count++; }
		    }
		    if (count > 0) {
			    counts.put(key, count);  //only store items we've found
			}
		}
		
		sorted_map.putAll(counts);
		
		//NavigableSet<String> set = ;
		System.out.println("Occurences\tSize\tObjectives....");
		for (String key: sorted_map.keySet()) {
			String[] objectiveNames = key.split("!");
			System.out.print(counts.get(key)); System.out.print("\t");
			System.out.print(objectiveNames.length);
			for (String name: objectiveNames) {
				 System.out.print("\t");
				 System.out.print(name);
			}
			System.out.println();
		}		
	}
	
	
	
}
