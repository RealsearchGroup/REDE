package edu.ncsu.csc.nl.model.type;

import static org.junit.Assert.*;

import org.junit.Test;

public class SecurityImpactTest {

	@Test
	public void test() {
		assertEquals("low",SecurityImpact.LOW.toString());
		assertEquals(4,SecurityImpact.getSelectableList().length);
	}

}
