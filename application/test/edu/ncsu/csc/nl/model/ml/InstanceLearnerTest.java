package edu.ncsu.csc.nl.model.ml;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.junit.Before;
import org.junit.Test;

import edu.ncsu.csc.nl.GCController;
import edu.ncsu.csc.nl.ReportController;
import edu.ncsu.csc.nl.model.NLDocument;
import edu.ncsu.csc.nl.model.Sentence;
import edu.ncsu.csc.nl.model.type.SecurityObjective;
import edu.ncsu.csc.nl.test.TestConfiguration;

public class InstanceLearnerTest {

	public static int binom (int n, int m) {
		int[] b = new int[n+1];
		b[0] = 1;
		for (int i=1; i<=n; i++) {
			b[i] = 1;
			for (int j= i-1; j>0; --j) {
				b[j] += b[j-1];
			}
		}
		return b[m];
	}

	@Before
	public void setUp() throws Exception {
		//TestConfiguration.setupEnvironment();
	}	
	
	@Test
	public void testCombinations() {
		InstanceLearner il = new InstanceLearner();
		
		List<List<SecurityObjective>> combinations = ReportController.getTheReportController().getCombinations(SecurityObjective.getSelectableList(),6);
		
		int expectedSize = binom (21,1) + binom (21,2) + binom (21,3) +binom(21,4) + binom(21,5) + binom(21,6);		
		System.out.println("Expected size: "+ expectedSize);
		System.out.println("Actual size:"+combinations.size());
	}
		

	
}
