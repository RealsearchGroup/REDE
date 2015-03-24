package edu.ncsu.csc.nl.view;

import static org.junit.Assert.*;

import java.awt.event.ActionEvent;

import org.junit.Before;
import org.junit.Test;

import edu.ncsu.csc.nl.model.NLDocument;
import edu.ncsu.csc.nl.model.SecurityObjectiveAnnotation;
import edu.ncsu.csc.nl.model.type.SecurityImpact;
import edu.ncsu.csc.nl.model.type.SecurityMitigation;
import edu.ncsu.csc.nl.model.type.SecurityObjective;
import edu.ncsu.csc.nl.test.TestConfiguration;

public class SecurityObjectiveAnnotationControllerTest {
	@Before
	public void setUp() throws Exception {
		TestConfiguration.setupEnvironment();
	}	
	
	@Test
	public void test() {
		SecurityObjectiveAnnotationController soac = new SecurityObjectiveAnnotationController(null);
		NLDocument document = TestConfiguration.createBasicDocumentForSOA();
		
		soac.setCurrentSentence(document.getElementAt(3));
		assertEquals(0,soac.getRowCount());
		assertEquals(5,soac.getColumnCount());
		
		assertEquals("Objective",soac.getColumnName(0));
		assertEquals("Implied",soac.getColumnName(1));
		assertEquals("Impact",soac.getColumnName(2));
		assertEquals("Mitigation",soac.getColumnName(3));
		assertEquals("Indicative Phrases",soac.getColumnName(4));
		assertEquals("UNDEFINED COLUMN NAME",soac.getColumnName(5));
		assertEquals("UNDEFINED COLUMN NAME",soac.getColumnName(-1));
		
		assertEquals(SecurityObjective.class,soac.getColumnClass(0));
		assertEquals(Boolean.class,soac.getColumnClass(1));
		assertEquals(SecurityImpact.class,soac.getColumnClass(2));
		assertEquals(SecurityMitigation.class,soac.getColumnClass(3));
		assertEquals(String.class,soac.getColumnClass(4));
		assertEquals(Object.class,soac.getColumnClass(5));	
		assertEquals(Object.class,soac.getColumnClass(-1));	

		//cells are always editable
		assertEquals(true, soac.isCellEditable(0, 0));
		assertEquals(true, soac.isCellEditable(-1, -1));
		assertEquals(true, soac.isCellEditable(4, 10));
		
		try {
			soac.getValueAt(0, 0);
			fail("Should have thrown an index out of bounds - no rows");
		}
		catch (Exception ex) {
	        assertEquals(IndexOutOfBoundsException.class, ex.getClass());
	    }

		soac.actionPerformed(new ActionEvent(this,-1,SecurityObjectiveAnnotationController.ACTION_ADD));
		assertEquals(1,soac.getRowCount());

		try {
			soac.setValueAt("", 1, 0);
			fail("Should have thrown an index out of bounds - only one row");
		}
		catch (Exception ex) {
	        assertEquals(IndexOutOfBoundsException.class, ex.getClass());
	    }		
		
		soac.setValueAt(SecurityObjective.CONFIDENTIALITY, 0, 0);
		soac.setValueAt(Boolean.FALSE, 0, 1);
		soac.setValueAt(SecurityImpact.LOW, 0, 2);
		soac.setValueAt(SecurityMitigation.PREVENTION, 0, 3);
		soac.setValueAt("logout", 0, 4);
	
		try {
			soac.setValueAt("logout", 0, 5);
			fail("Should have thrown an IllegalArgumentException - invalid column");
		}
		catch (Exception ex) {
	        assertEquals(IllegalArgumentException.class, ex.getClass());
	    }		
		
		try {
			soac.setValueAt("logout", 0, -1);
			fail("Should have thrown an IllegalArgumentException - invalid column");
		}
		catch (Exception ex) {
	        assertEquals(IllegalArgumentException.class, ex.getClass());
	    }				
		
		SecurityObjectiveAnnotation soa = document.getElementAt(3).getSecurityObjectiveAnnotations().get(0);
		
		assertEquals(SecurityObjective.CONFIDENTIALITY,soa.getSecurityObjective());
		assertEquals(Boolean.FALSE,soa.isImplied());
		assertEquals(SecurityImpact.LOW,soa.getSecurityImpact());
		assertEquals(SecurityMitigation.PREVENTION,soa.getSecurityMitigation());
		assertEquals("logout",soa.getIndicativePhrase());

		assertEquals(SecurityObjective.CONFIDENTIALITY,soac.getValueAt(0,0));
		assertEquals(Boolean.FALSE,soac.getValueAt(0,1));
		assertEquals(SecurityImpact.LOW,soac.getValueAt(0,2));
		assertEquals(SecurityMitigation.PREVENTION,soac.getValueAt(0,3));
		assertEquals("logout",soac.getValueAt(0,4));
		
		// test the bounds on the columns
		assertEquals("UNDEFINED COLUMN",soac.getValueAt(0,5));
		assertEquals("UNDEFINED COLUMN",soac.getValueAt(0,-1));
		
		assertEquals(SecurityObjective.CONFIDENTIALITY,soac.getValueAt(0,0));
		
		//add second row
		soac.actionPerformed(new ActionEvent(this,-1,SecurityObjectiveAnnotationController.ACTION_ADD));
		assertEquals(SecurityObjective.CONFIDENTIALITY,soac.getValueAt(0,0));
		
		assertEquals(2,soac.getRowCount());
		soac.setValueAt(SecurityObjective.LOGGING_SECURITY, 1, 0);
		soac.setValueAt(Boolean.TRUE, 1, 1);
		soac.setValueAt(SecurityImpact.HIGH, 1, 2);
		soac.setValueAt(SecurityMitigation.REACTION, 1, 3);
		soac.setValueAt("automatic", 1, 4);

		assertEquals(SecurityObjective.LOGGING_SECURITY,soac.getValueAt(1,0));
		assertEquals(Boolean.TRUE,soac.getValueAt(1,1));
		assertEquals(SecurityImpact.HIGH,soac.getValueAt(1,2));
		assertEquals(SecurityMitigation.REACTION,soac.getValueAt(1,3));
		assertEquals("automatic",soac.getValueAt(1,4));		
		
		assertEquals(SecurityObjective.CONFIDENTIALITY,soac.getValueAt(0,0));
		assertEquals(SecurityObjective.LOGGING_SECURITY,soac.getValueAt(1,0));
		
		soac.actionPerformed(new ActionEvent(this,-1,SecurityObjectiveAnnotationController.ACTION_ADD));
		
		assertEquals(SecurityObjective.CONFIDENTIALITY,soac.getValueAt(0,0));
		assertEquals(SecurityObjective.LOGGING_SECURITY,soac.getValueAt(1,0));
		
		assertEquals(3,soac.getRowCount());
		
		//delete blank row
		soac.actionPerformed(new ActionEvent(this,-1,SecurityObjectiveAnnotationController.ACTION_DELETE));
		assertEquals(2,soac.getRowCount());
		assertEquals(SecurityObjective.CONFIDENTIALITY,soac.getValueAt(0,0));
		assertEquals(SecurityObjective.LOGGING_SECURITY,soac.getValueAt(1,0));
		
		
		soac.actionPerformed(new ActionEvent(this,-1,SecurityObjectiveAnnotationController.ACTION_COMPLETE));
		assertEquals(true,document.getElementAt(3).isSecurityObjectiveAnnotationsDefined());

		soac.actionPerformed(new ActionEvent(this,-1,SecurityObjectiveAnnotationController.ACTION_INCOMPLETE));
		assertEquals(false,document.getElementAt(3).isSecurityObjectiveAnnotationsDefined());
		
		soac.setVisible(true);
		assertEquals(true,soac.isVisible());
		soac.actionPerformed(new ActionEvent(this,-1,SecurityObjectiveAnnotationController.ACTION_CLOSE));
		soac.setVisible(false);
		assertEquals(false,soac.isVisible());
	
		// send and invalid action
		try {
			soac.actionPerformed(new ActionEvent(this,-1,"invalid action test"));
			fail("Should have thrown an IllegalArgumentException - invalid action");
		}
		catch (Exception ex) {
	        assertEquals(IllegalArgumentException.class, ex.getClass());
	    }			
		
		
	}
}
