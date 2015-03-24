package edu.ncsu.csc.nl.model;

import static org.junit.Assert.*;

import org.junit.Test;

import edu.ncsu.csc.nl.model.type.SecurityImpact;
import edu.ncsu.csc.nl.model.type.SecurityMitigation;
import edu.ncsu.csc.nl.model.type.SecurityObjective;

public class SecurityObjectiveAnnotationTest {

	@Test
	public void testBasicMethods() {
		SecurityObjectiveAnnotation soa = new SecurityObjectiveAnnotation(SecurityObjective.ACCESS_CONTROL, 
				                                                          SecurityImpact.HIGH,
				                                                          SecurityMitigation.PREVENTION,
				                                                          true);
		
		assertEquals(SecurityObjective.ACCESS_CONTROL, soa.getSecurityObjective());
		assertEquals(SecurityImpact.HIGH, soa.getSecurityImpact());
		assertEquals(SecurityMitigation.PREVENTION, soa.getSecurityMitigation());
		assertEquals(true, soa.isImplied());
		assertEquals("",soa.getIndicativePhrase());
		
		SecurityObjectiveAnnotation soa3 = new SecurityObjectiveAnnotation(soa);
		assertEquals(SecurityObjective.ACCESS_CONTROL, soa3.getSecurityObjective());
		assertEquals(SecurityImpact.HIGH, soa3.getSecurityImpact());
		assertEquals(SecurityMitigation.PREVENTION, soa3.getSecurityMitigation());
		assertEquals(true, soa3.isImplied());
		assertEquals("",soa3.getIndicativePhrase());
		
		
		SecurityObjectiveAnnotation soa2 = new SecurityObjectiveAnnotation();
		assertNull(soa2.getSecurityObjective());
		assertNull(soa2.getSecurityImpact());
		assertNull(soa2.getSecurityMitigation());
		assertFalse(soa2.isImplied());
		
		soa2.setSecurityObjective(SecurityObjective.CONFIDENTIALITY);
		soa2.setSecurityImpact(SecurityImpact.LOW);
		soa2.setSecurityMitigation(SecurityMitigation.REACTION);
		soa2.setImplied(true);
		soa2.setIndicativePhrase("hello world!");
		
		assertEquals(SecurityObjective.CONFIDENTIALITY, soa2.getSecurityObjective());
		assertEquals(SecurityImpact.LOW, soa2.getSecurityImpact());
		assertEquals(SecurityMitigation.REACTION, soa2.getSecurityMitigation());
		assertEquals(true, soa2.isImplied());	
		assertEquals("hello world!",soa2.getIndicativePhrase());
	}

}
