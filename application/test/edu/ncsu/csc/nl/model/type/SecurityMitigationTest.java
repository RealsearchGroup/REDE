package edu.ncsu.csc.nl.model.type;

import static org.junit.Assert.*;

import org.junit.Test;

public class SecurityMitigationTest {

	@Test
	public void test() {
		assertEquals("prevention",SecurityMitigation.PREVENTION.toString());
		assertEquals(4,SecurityMitigation.getSelectableList().length);
	}

}
