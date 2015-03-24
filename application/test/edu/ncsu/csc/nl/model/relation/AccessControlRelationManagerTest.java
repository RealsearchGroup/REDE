package edu.ncsu.csc.nl.model.relation;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import edu.ncsu.csc.nl.model.NLDocument;
import edu.ncsu.csc.nl.test.TestConfiguration;

public class AccessControlRelationManagerTest {

	
	
	
	@Before
	public void setUp() throws Exception {
		TestConfiguration.setupEnvironment();
		AccessControlRelationManager.getTheAccessControlRelationManager().reset(true);
	}

	@Test
	public void test() {
		fail("Not yet implemented");
	}
	
	/*
	 * 
	 * doctors write prescriptions
	 * nurses write orders
	 * doctors and nurses write orders and prescriptions
	 * a nurse may write a lab procedure for a patient
	 * a nures may order a lab procedure for a patient
	 */
	@Test
	public void testPatterns() {
/*		NLDocument document = new NLDocument();
		document.addSentences(document.parseSentence(GCController.getTheGCController().getPipeline(), "Doctors write prescriptions."));
		Sentence s = document.getElementAt(0);
		AccessControl ac = new AccessControl();
		
		List<WordVertex> newList = null;
		try {
			newList = _currentSentence.generateWordVertexListFromString(aValue.toString());
		}
		catch(Exception e) {
			GCController.getTheGCController().setStatusMessage(e.getMessage());
			return;
		}
		switch (columnIndex) {
		case 0: ac.setSubjectVertexList(newList); break;
		case 1: ac.setActionVertexList(newList); break;
		case 2: ac.setObjectVertexList(newList); break;		
		
		ac.set

		
		validateBasicDocument(document);
		
		return document;
		*/
	}
	
	public static void validateBasicDocument(NLDocument document) {
		assertEquals("Number of sentences equals 3", 3,document.getNumberOfSentences());
		assertEquals("The doctors write prescriptions.",document.getElementAt(1).getSentence());
		assertEquals(3,document.getElementAt(1).getNumberOfNodes());	
	}		
	
	

}
