package edu.ncsu.csc.nl.model.type;

import static org.junit.Assert.*;

import org.junit.Test;

public class SecurityObjectiveTest {

	@Test
	public void test() {
		assertEquals("confidentiality",SecurityObjective.CONFIDENTIALITY.toString());
		assertEquals(SecurityObjective.TECHNICAL,SecurityObjective.CONFIDENTIALITY.getParent());
		assertEquals("C",SecurityObjective.CONFIDENTIALITY.getAbbreviation());
		assertEquals(null,SecurityObjective.NONE.getParent());
		assertEquals(19,SecurityObjective.getSelectableList().length);
	}

}
