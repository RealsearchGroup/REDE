package edu.ncsu.csc.nl.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import edu.ncsu.csc.nl.GCController;
import edu.ncsu.csc.nl.model.NLDocument;
import edu.ncsu.csc.nl.model.SecurityObjectiveAnnotation;
import edu.ncsu.csc.nl.model.type.SecurityImpact;
import edu.ncsu.csc.nl.model.type.SecurityMitigation;
import edu.ncsu.csc.nl.model.type.SecurityObjective;

public class TestConfiguration {
	public static final String testSerializationFileLocation = "C:\\temp\\test.ser";
	public static final String testJSONFileLocation = "C:\\temp\\test.json";
	public static final String wordNetDictionaryLocation = "C:/NLP/WordNetDictionary";
	public static final String classificationAttributeConfiguration = "C:/Users/Adminuser/Dropbox/Research Work/IdentificationStudy/attributes.json";
	
	private static boolean initialized = false;
	
	public static synchronized void setupEnvironment() {
		if (!initialized) {
			GCController controller = GCController.getTheGCController();
			controller.initialize(TestConfiguration.wordNetDictionaryLocation,true);
			controller.initializeClassifierElements(TestConfiguration.classificationAttributeConfiguration);
			initialized = true;
		}
	}
	
	public static NLDocument createBasicDocument() {
		NLDocument document = new NLDocument();
		document.addSentences(document.parseSentence(GCController.getTheGCController().getPipeline(), "iTrust Medical Records System"));
		document.addSentences(document.parseSentence(GCController.getTheGCController().getPipeline(), "The doctors write prescriptions."));
		document.addSentences(document.parseSentence(GCController.getTheGCController().getPipeline(), "nurses can order lab procedures for patients."));
		
		validateBasicDocument(document);
		
		return document;
	}
	
	public static void validateBasicDocument(NLDocument document) {
		assertEquals("Number of sentences equals 3", 3,document.getNumberOfSentences());
		assertEquals("The doctors write prescriptions.",document.getElementAt(1).getSentence());
		assertEquals(3,document.getElementAt(1).getNumberOfNodes());	
	}	

	public static NLDocument createBasicDocumentForSOA() {
		NLDocument document = new NLDocument();
		document.addSentences(document.parseSentence(GCController.getTheGCController().getPipeline(), "iTrust Medical Records System"));
		document.addSentences(document.parseSentence(GCController.getTheGCController().getPipeline(), "The doctors write prescriptions."));
		document.addSentences(document.parseSentence(GCController.getTheGCController().getPipeline(), "nurses can order lab procedures for patients."));
		
		validateBasicDocument(document);
		document.addSentences(document.parseSentence(GCController.getTheGCController().getPipeline(), "The system shall automatically log out users after 10 minutes of inactivity."));
		document.addSentences(document.parseSentence(GCController.getTheGCController().getPipeline(), "The system shall ensure messages are not altered in transit."));
		
		validateBasicDocumentForSOA(document);
		
		return document;
	}
	
		
	public static void validateBasicDocumentForSOA(NLDocument document) {
		assertEquals("Number of sentences equals 5", 5,document.getNumberOfSentences());
	}
	
	public static SecurityObjectiveAnnotation createSOA_ACCESS_CONTROL() {
		return new SecurityObjectiveAnnotation(SecurityObjective.ACCESS_CONTROL, SecurityImpact.HIGH, SecurityMitigation.PREVENTION, false);
	}
	
	public static void validateSOA_ACCESS_CONTROL(SecurityObjectiveAnnotation soa) {
		assertEquals(SecurityObjective.ACCESS_CONTROL, soa.getSecurityObjective());
		assertEquals(SecurityImpact.HIGH, soa.getSecurityImpact());
		assertEquals(SecurityMitigation.PREVENTION, soa.getSecurityMitigation());
		assertEquals(false, soa.isImplied());
		assertEquals("",soa.getIndicativePhrase());
	}

	public static SecurityObjectiveAnnotation createSOA_CONFIDENTIALITY() {
		SecurityObjectiveAnnotation soa2 = new SecurityObjectiveAnnotation();
		soa2.setSecurityObjective(SecurityObjective.CONFIDENTIALITY);
		soa2.setSecurityImpact(SecurityImpact.LOW);
		soa2.setSecurityMitigation(SecurityMitigation.REACTION);
		soa2.setImplied(true);
		soa2.setIndicativePhrase("hello world!");
		return soa2;
	}
	
	public static void validateSOA_CONFIDENTIALITY(SecurityObjectiveAnnotation soa) {
		assertEquals(SecurityObjective.CONFIDENTIALITY, soa.getSecurityObjective());
		assertEquals(SecurityImpact.LOW, soa.getSecurityImpact());
		assertEquals(SecurityMitigation.REACTION, soa.getSecurityMitigation());
		assertEquals(true, soa.isImplied());	
		assertEquals("hello world!",soa.getIndicativePhrase());
	}
	
}
