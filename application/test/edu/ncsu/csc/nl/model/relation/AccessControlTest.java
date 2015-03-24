package edu.ncsu.csc.nl.model.relation;

import static org.junit.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import edu.ncsu.csc.nl.model.NLDocument;
import edu.ncsu.csc.nl.model.Sentence;
import edu.ncsu.csc.nl.model.WordVertex;
import edu.ncsu.csc.nl.model.type.RelationSource;
import edu.ncsu.csc.nl.test.TestConfiguration;

public class AccessControlTest {
	@Before
	public void setUp() throws Exception {
		TestConfiguration.setupEnvironment();
	}	
	
	@Test
	public void test() {
		fail("Not yet implemented");
	}

	// test the basic pattern of  ( a  (s) (o) )
	@Test
	public void testAccessControlBasicPattern() {
		NLDocument document = TestConfiguration.createBasicDocument();
		Sentence s = document.getElementAt(1); 
		assertEquals(3, s.getNumberOfNodes());	
	
		AccessControlRelation ac = this.createAccessControlBasicPattern(s);
		
		this.checkAccessControlBasicPatternValues(ac);
		
		List<WordVertex> subjectList = s.generateWordVertexListFromString("2");
		List<WordVertex> actionList  = s.generateWordVertexListFromString("1");
		List<WordVertex> objectList  = s.generateWordVertexListFromString("3");
		ac = new AccessControlRelation(subjectList,actionList,objectList,"",null,null,true,RelationSource.USER);
		this.checkAccessControlBasicPatternValues(ac);
		
		ac = new AccessControlRelation(ac);
		this.checkAccessControlBasicPatternValues(ac);
		
	}
	
	private AccessControlRelation createAccessControlBasicPattern(Sentence s) {
		List<WordVertex> subjectList = s.generateWordVertexListFromString("2");
		List<WordVertex> actionList  = s.generateWordVertexListFromString("1");
		List<WordVertex> objectList  = s.generateWordVertexListFromString("3");
		
		AccessControlRelation ac = new AccessControlRelation();
		ac.setSubjectVertexList(subjectList);
		ac.setActionVertexList(actionList);
		ac.setObjectVertexList(objectList);
		ac.setPermissions("CRUD");
		ac.setSource(RelationSource.USER);
		
		return ac;
	}
	
	private void checkAccessControlBasicPatternValues(AccessControlRelation ac) {
		assertEquals("doctor",ac.getSubjectVertexList().get(0).getLemma());
		assertEquals("write",ac.getActionVertexList().get(0).getLemma());
		assertEquals("prescription",ac.getObjectVertexList().get(0).getLemma());
		
		assertEquals("doctor",ac.getSubject());
		assertEquals("write",ac.getAction());
		assertEquals("prescription",ac.getObject());

		assertEquals("2",ac.getSubjectAsVertexIDs());
		assertEquals("1",ac.getActionAsVertexIDs());
		assertEquals("3",ac.getObjectAsVertexIDs());
		
		assertEquals("",ac.getNegativeVertexAsID());
		assertEquals("",ac.getLimitToSubjectVertexAsID());

		assertEquals(null,ac.getNegativeVertex());
		assertEquals(null,ac.getLimitToSubjectVertex());
		
		assertEquals(RelationSource.USER,ac.getSource());
		assertEquals(true,ac.isCorrectlyDerived());
	}
	
	@Test
	public void testSerialization() {
		NLDocument document = TestConfiguration.createBasicDocument();
		Sentence s = document.getElementAt(1); 
		assertEquals(3, s.getNumberOfNodes());	
	
		AccessControlRelation ac = this.createAccessControlBasicPattern(s);
       try {
            // Write the object out to a byte array
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(ac);
            out.flush();
            out.close();

            // Make an input stream from the byte array and read
            // a copy of the object back in.
            ObjectInputStream in = new ObjectInputStream( new ByteArrayInputStream(bos.toByteArray()));
            Object o = in.readObject();
            AccessControlRelation ac2 = (AccessControlRelation) o;
            this.checkAccessControlBasicPatternValues(ac2);
        }
        catch(IOException e) {
        	fail("IO Exception thrown: "+e);
        }
        catch(ClassNotFoundException cnfe) {
        	fail("ClassNotFoundException"+cnfe);
        }
	}
}
