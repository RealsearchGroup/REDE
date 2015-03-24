package edu.ncsu.csc.nl.model;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;









import com.fasterxml.jackson.annotation.JsonIgnore;

import weka.core.Instances;
import edu.ncsu.csc.nl.GCController;
import edu.ncsu.csc.nl.model.classification.BooleanClassification;
import edu.ncsu.csc.nl.model.classification.ClassificationAttribute;
import edu.ncsu.csc.nl.model.relation.AccessControlRelation;
import edu.ncsu.csc.nl.model.relation.AccessControlFactor;
import edu.ncsu.csc.nl.model.relation.AccessControlPattern;
import edu.ncsu.csc.nl.model.relation.AccessControlRelationManager;
import edu.ncsu.csc.nl.model.relation.DatabaseFactor;
import edu.ncsu.csc.nl.model.relation.DatabasePattern;
import edu.ncsu.csc.nl.model.relation.DatabaseRelationManager;
import edu.ncsu.csc.nl.model.type.NamedEntity;
import edu.ncsu.csc.nl.model.type.PartOfSpeech;
import edu.ncsu.csc.nl.model.type.Relationship;
import edu.ncsu.csc.nl.model.type.SecurityObjective;
import edu.ncsu.csc.nl.model.type.WordType;

/**
 * Class is used to export sentences to a Weka format
 * 
 * @author John Slankas
 */
public class WekaCreator {

	/**
	 * builds a vector of the columns that will be exported.  (i.e., column names, types, allowable values)
	 * 
	 * @param maxNodes
	 * @param classificationAttributes
	 * @param wo
	 * @param includeNERPresence
	 * @return
	 */
	protected weka.core.FastVector buildAttributesForClassifications(int maxNodes, java.util.List<ClassificationAttribute> classificationAttributes, WekaCreatorOptions wo) {
		int numberOfNodeSelectOptions = wo.getNumberOfNodeOptionsSelected();
		
		int numAttributes; 		
		if (wo.exportSentenceAsString() ) {
			numAttributes = classificationAttributes.size() + 2;
			
			if (wo.useNERSentenceIndicators()) { numAttributes +=  NamedEntity.NAMED_ENTITY_CATEGORIES.length;}  
		}
		else {
			numAttributes = (maxNodes*(numberOfNodeSelectOptions)) + classificationAttributes.size() + 1;
		}
		
		weka.core.FastVector attributes = new weka.core.FastVector(numAttributes);
		weka.core.FastVector stringIndicator = null;  //this needs to be null per the Weka API to represent strings

		weka.core.FastVector classificationVector = new weka.core.FastVector();
		classificationVector.addElement("yes");
		classificationVector.addElement("no");
		
		if (wo.exportSentenceAsString()) {
			for (ClassificationAttribute ca: classificationAttributes) {     
				if (ca.getType().equalsIgnoreCase("boolean")) {
					attributes.addElement(new weka.core.Attribute(ca.getName(), classificationVector));
				}
				else if (ca.getType().equalsIgnoreCase("list")) {
					String[] valueList = ca.getValues().split(",");
					weka.core.FastVector listVector = new weka.core.FastVector();
					for (String s: valueList) {
						listVector.addElement(s.trim()); 
					}
					attributes.addElement(new weka.core.Attribute(ca.getName(), listVector));
				}
				else {
					attributes.addElement(new weka.core.Attribute(ca.getName(), stringIndicator));
				}
			}
						
			if (wo.useNERSentenceIndicators()) {
				for (String s: NamedEntity.NAMED_ENTITY_CATEGORIES) {  
					attributes.addElement(new weka.core.Attribute(s));
				}
			}
			if (wo.useUltraCollapsedStringRepresentation()) {
				attributes.addElement(new weka.core.Attribute("treeRelationship", stringIndicator));
			}
			attributes.addElement(new weka.core.Attribute("documentID", stringIndicator)); 			
			attributes.addElement(new weka.core.Attribute("sentence", stringIndicator));
			
			return attributes;
		}
		else {
			weka.core.FastVector relValues    = this.createRelationshipStringFastVector();
			weka.core.FastVector posValues    = this.createPartOfSpeechStringFastVector();
			
			for (int i=1;i<=maxNodes;i++) {
				if (wo.useNodeNumber())           { attributes.addElement(new weka.core.Attribute("node_"+i, stringIndicator));  }
				if (wo.useLemma())                { attributes.addElement(new weka.core.Attribute("lemma_"+i, stringIndicator)); }
				if (wo.useOriginalWord())         { attributes.addElement(new weka.core.Attribute("word_"+i, stringIndicator)); }
				if (wo.usePartOfSpeech())         { attributes.addElement(new weka.core.Attribute("PartOfSpeech_"+i, posValues)); }
				if (wo.useNamedEntity())          { attributes.addElement(new weka.core.Attribute("namedEntity_"+i, stringIndicator)); }		
				if (wo.useRelationshipToParent()) { attributes.addElement(new weka.core.Attribute("Relationship_"+i, relValues)); }
				if (wo.useParentNodeNumber())     { attributes.addElement(new weka.core.Attribute("ParentNode_"+i, stringIndicator)); }
			}
	
			for (ClassificationAttribute ca: classificationAttributes) {
				if (ca.getType().equalsIgnoreCase("boolean")) {
					attributes.addElement(new weka.core.Attribute(ca.getName(), classificationVector));
				}
				else if (ca.getType().equalsIgnoreCase("list")) {
					String[] valueList = ca.getValues().split(",");
					weka.core.FastVector listVector = new weka.core.FastVector();
					for (String s: valueList) {
						listVector.addElement(s.trim()); 
					}
					attributes.addElement(new weka.core.Attribute(ca.getName(), listVector));
				}
				else {
					attributes.addElement(new weka.core.Attribute(ca.getName(), stringIndicator));
				}
			}
		
			attributes.addElement(new weka.core.Attribute("documentID", stringIndicator)); 
		
			return attributes;
		}
	}
	
	/**
	 * builds a vector of the columns that will be exported.  (i.e., column names, types, allowable values)
	 * 
	 * @param maxNodes
	 * @param classificationAttributes
	 * @param wo
	 * @param includeNERPresence
	 * @return
	 */
	protected weka.core.FastVector buildAttributesForSecurityObjectiveAnnotations(int maxNodes, WekaCreatorOptions wo, List<SecurityObjective> objectives) {
		int numberOfNodeSelectOptions = wo.getNumberOfNodeOptionsSelected();
		
		int numAttributes; 		
		if (wo.exportSentenceAsString() ) {
			numAttributes = objectives.size() + 2;  //2 represents the document ID and sentence columns
			
			if (wo.useNERSentenceIndicators()) { numAttributes +=  NamedEntity.NAMED_ENTITY_CATEGORIES.length;}  
		}
		else {
			numAttributes = (maxNodes*(numberOfNodeSelectOptions)) + objectives.size() + 1;   // 1 is the document ID column
		}
		
		weka.core.FastVector attributes = new weka.core.FastVector(numAttributes);
		weka.core.FastVector stringIndicator = null;  //this needs to be null per the Weka API to represent strings

		weka.core.FastVector classificationVector = new weka.core.FastVector();
		classificationVector.addElement("yes");
		classificationVector.addElement("no");
		
		if (wo.exportSentenceAsString()) {
			for (SecurityObjective obj: objectives) {   
				attributes.addElement(new weka.core.Attribute(obj.toString(), classificationVector));
			}	
						
			if (wo.useNERSentenceIndicators()) {
				for (String s: NamedEntity.NAMED_ENTITY_CATEGORIES) {  
					attributes.addElement(new weka.core.Attribute(s));
				}
			}
			if (wo.useUltraCollapsedStringRepresentation()) {
				attributes.addElement(new weka.core.Attribute("treeRelationship", stringIndicator));
			}
			attributes.addElement(new weka.core.Attribute("documentID", stringIndicator)); 			
			attributes.addElement(new weka.core.Attribute("sentence", stringIndicator));
			
			return attributes;
		}
		else {
			weka.core.FastVector relValues    = this.createRelationshipStringFastVector();
			weka.core.FastVector posValues    = this.createPartOfSpeechStringFastVector();
			
			for (int i=1;i<=maxNodes;i++) {
				if (wo.useNodeNumber())           { attributes.addElement(new weka.core.Attribute("node_"+i, stringIndicator));  }
				if (wo.useLemma())                { attributes.addElement(new weka.core.Attribute("lemma_"+i, stringIndicator)); }
				if (wo.useOriginalWord())         { attributes.addElement(new weka.core.Attribute("word_"+i, stringIndicator)); }
				if (wo.usePartOfSpeech())         { attributes.addElement(new weka.core.Attribute("PartOfSpeech_"+i, posValues)); }
				if (wo.useNamedEntity())          { attributes.addElement(new weka.core.Attribute("namedEntity_"+i, stringIndicator)); }		
				if (wo.useRelationshipToParent()) { attributes.addElement(new weka.core.Attribute("Relationship_"+i, relValues)); }
				if (wo.useParentNodeNumber())     { attributes.addElement(new weka.core.Attribute("ParentNode_"+i, stringIndicator)); }
			}
	
			for (SecurityObjective obj: objectives) {   
				attributes.addElement(new weka.core.Attribute(obj.toString(), classificationVector));
			}	
		
			attributes.addElement(new weka.core.Attribute("documentID", stringIndicator)); 
		
			return attributes;
		}
	}	
	
	
	/**
	 * This method generates all of the information necessary to run a classifier within WEKA
	 * If you compare it to the ARFF data format, it has the relation name, the defined attributes,
	 * and the data
	 * 
	 * @param relationName what is the relation name.  More of a table name.
	 * 
	 * @return the Instances class wrapping the attributes and data
	 */
	public weka.core.Instances createWekaInstancesForClassifications(String relationName, ArrayList<Sentence> sentences, java.util.List<ClassificationAttribute> classificationAttributes, WekaCreatorOptions wo) {
		int maxNodes = this.getMaxNodesInGraph(sentences);
		int numberOfSelectOptions = wo.getNumberOfNodeOptionsSelected();
		
		weka.core.FastVector attributes = this.buildAttributesForClassifications(maxNodes, classificationAttributes, wo);
		
		
		Instances instances = new Instances(relationName, attributes, sentences.size());
	    instances.setClassIndex(instances.numAttributes() - 1);
		
	    //now for the data
	    int count=0;
SentenceLoop:
		for (Sentence s: sentences) {
			count++;
			
			weka.core.Instance i = new weka.core.DenseInstance(attributes.size());
			i.setDataset(instances);
			
			if (wo.exportSentenceAsString()) {
				int index = 0;
				for (ClassificationAttribute ca: classificationAttributes) {
					String value = "";
					if (ca.getType().equalsIgnoreCase("boolean")) {
						value = "no";
						if (s.getClassifications().containsKey(ca.getName()) &&((BooleanClassification) s.getClassifications().get(ca.getName())).getValue().getBooleanValue()) {
							value = "yes";
						}		
						
					}
					else {
						if (s.getClassifications().containsKey(ca.getName())) {
							value = s.getClassifications().get(ca.getName()).getValue().toString();
						}						
					}				
					
					i.setValue(index, value);
					index++; // increment needs to be at the end of the loop.
				}			
						
				
				if (wo.useNERSentenceIndicators()) {
					for (String ner: NamedEntity.NAMED_ENTITY_CATEGORIES) {  
						double value = 0.01;
						if (this.hasNERCategory(s, ner, wo.getWordType(), wo.getStopWords())) {
							value = 1.0;
						}

						i.setValue(index, value);
						index++; // increment needs to be at the end
					}
				}
				if (wo.useUltraCollapsedStringRepresentation()) {
					i.setValue(index,s.getRoot().getStringRepresentationUltraCollapsed());
					index++;
				}
				
				String sentence;
				if (wo.useCasamayorSentenceRepresentation()) {
					sentence = WekaCreator.getCasamayorStringRepresentation(s.toString(),  wo.getStopWords());
				}
				else if (wo.useOriginalSentence()) {
					sentence = s.toString();
				}
				else {
					sentence = s.getSentence(wo.getWordType(), wo.getStopWords());
				}
				
				//Set the document ID of the sentence
				i.setValue(attributes.size()-2, s.getDocumentID());
				i.setValue(attributes.size()-1, sentence);				
			}
			else {
				int numNodes = s.getNumberOfNodes();
				for (int k=0;k < numNodes; k++) {
					WordVertex wv = s.getWordVertexBySortedPosition(k);
					int position = k * numberOfSelectOptions;
					try {
						if (wo.useNodeNumber())           { i.setValue(position, wv.getID());  position++;  }
						if (wo.useLemma())                { i.setValue(position, wv.getLemma());  position++;   }
						if (wo.useOriginalWord())         { i.setValue(position, wv.getOriginalWord());  position++;   }
						if (wo.usePartOfSpeech())         { i.setValue(position, wv.getPartOfSpeech().getCollapsedLabel());  position++;   }
						if (wo.useNamedEntity())          { i.setValue(position, wv.getNamedEntityRecognizedLabel());  position++;   }		
						if (wo.useRelationshipToParent()) { i.setValue(position, wv.getParentAt(0).getRelationship().getLabel());  position++;   }
						if (wo.useParentNodeNumber())     { i.setValue(position, wv.getParentAt(0).getParentNode().getID());  position++;   }					
					}
					catch (Exception e) {
						System.err.println(e);
						System.err.println(count+":"+s);
						continue SentenceLoop;					
					}
				}
			
				//Now, fill in the undefined positioning with missing values
				int startIndex = numNodes*numberOfSelectOptions;
				int endIndex = maxNodes*numberOfSelectOptions;
				for (int j=startIndex;j<endIndex;j++) {
					i.setMissing(j);
				}
			
			
				int index = maxNodes* numberOfSelectOptions;
				for (ClassificationAttribute ca: classificationAttributes) {
					String value = "";
					if (ca.getType().equalsIgnoreCase("boolean")) {
						value = "no";
						if (s.getClassifications().containsKey(ca.getName()) &&((BooleanClassification) s.getClassifications().get(ca.getName())).getValue().getBooleanValue()) {
							value = "yes";
						}		
						
					}
					else {
						if (s.getClassifications().containsKey(ca.getName())) {
							value = s.getClassifications().get(ca.getName()).getValue().toString();
						}						
					}
						
					i.setValue(index, value);
					index++; // increment needs to be at the end of the loop.
	
				}
				
				//Set the document ID of the sentence
				i.setValue(attributes.size()-1, s.getDocumentID());
			}
			instances.add(i);
			
		}	    
		return instances;
	}

	
	/**
	 * This method generates all of the information necessary to run a classifier within WEKA
	 * If you compare it to the ARFF data format, it has the relation name, the defined attributes,
	 * and the data
	 * 
	 * @param relationName what is the relation name.  More of a table name.
	 * 
	 * @param annotationList what set of security objectives should be used for the data
	 * 
	 * @return the Instances class wrapping the attributes and data
	 */
	public weka.core.Instances createWekaInstancesForSecurityObjectiveAnnotations(String relationName, ArrayList<Sentence> sentences, WekaCreatorOptions wo, java.util.List<SecurityObjective> objectives) {
		int maxNodes = this.getMaxNodesInGraph(sentences);
		int numberOfSelectOptions = wo.getNumberOfNodeOptionsSelected();
		
		weka.core.FastVector attributes = this.buildAttributesForSecurityObjectiveAnnotations(maxNodes, wo,objectives);
		
		Instances instances = new Instances(relationName, attributes, sentences.size());
	    instances.setClassIndex(instances.numAttributes() - 1);
		
	    //now for the data
	    int count=0;
SentenceLoop:
		for (Sentence s: sentences) {
			count++;
			
			weka.core.Instance i = new weka.core.DenseInstance(attributes.size());
			i.setDataset(instances);
			
			if (wo.exportSentenceAsString()) {
				int index = 0;
				for (SecurityObjective obj: objectives) {
					String value = "no";
					if (s.hasSecurityObjectiveAnnotation(obj)) {
						value = "yes";
					}
		
					i.setValue(index, value);
					index++; // increment needs to be at the end of the loop.
				}			
						
				if (wo.useNERSentenceIndicators()) {
					for (String ner: NamedEntity.NAMED_ENTITY_CATEGORIES) {  
						double value = 0.01;
						if (this.hasNERCategory(s, ner, wo.getWordType(), wo.getStopWords())) {
							value = 1.0;
						}

						i.setValue(index, value);
						index++; // increment needs to be at the end
					}
				}
				if (wo.useUltraCollapsedStringRepresentation()) {
					i.setValue(index,s.getRoot().getStringRepresentationUltraCollapsed());
					index++;
				}
				
				String sentence;
				if (wo.useCasamayorSentenceRepresentation()) {
					sentence = WekaCreator.getCasamayorStringRepresentation(s.toString(),  wo.getStopWords());
				}
				else if (wo.useOriginalSentence()) {
					sentence = s.toString();
				}
				else {
					sentence = s.getSentence(wo.getWordType(), wo.getStopWords());
				}
				
				//Set the document ID of the sentence
				i.setValue(attributes.size()-2, s.getDocumentID());
				i.setValue(attributes.size()-1, sentence);				
			}
			else {
				int numNodes = s.getNumberOfNodes();
				for (int k=0;k < numNodes; k++) {
					WordVertex wv = s.getWordVertexBySortedPosition(k);
					int position = k * numberOfSelectOptions;
					try {
						if (wo.useNodeNumber())           { i.setValue(position, wv.getID());  position++;  }
						if (wo.useLemma())                { i.setValue(position, wv.getLemma());  position++;   }
						if (wo.useOriginalWord())         { i.setValue(position, wv.getOriginalWord());  position++;   }
						if (wo.usePartOfSpeech())         { i.setValue(position, wv.getPartOfSpeech().getCollapsedLabel());  position++;   }
						if (wo.useNamedEntity())          { i.setValue(position, wv.getNamedEntityRecognizedLabel());  position++;   }		
						if (wo.useRelationshipToParent()) { i.setValue(position, wv.getParentAt(0).getRelationship().getLabel());  position++;   }
						if (wo.useParentNodeNumber())     { i.setValue(position, wv.getParentAt(0).getParentNode().getID());  position++;   }					
					}
					catch (Exception e) {
						System.err.println(e);
						System.err.println(count+":"+s);
						continue SentenceLoop;					
					}
				}
			
				//Now, fill in the undefined positioning with missing values
				int startIndex = numNodes*numberOfSelectOptions;
				int endIndex = maxNodes*numberOfSelectOptions;
				for (int j=startIndex;j<endIndex;j++) {
					i.setMissing(j);
				}
			
			
				int index = maxNodes* numberOfSelectOptions;
				for (SecurityObjective obj: objectives) {
					String value = "no";
					if (s.hasSecurityObjectiveAnnotation(obj)) {
						value = "yes";
					}
					
					i.setValue(index, value);
					index++; // increment needs to be at the end of the loop.
				}
				
				//Set the document ID of the sentence
				i.setValue(attributes.size()-1, s.getDocumentID());
			}
			instances.add(i);
			
		}	    
		return instances;
	}	
	
		
	@JsonIgnore
	public int getMaxNodesInGraph(ArrayList<Sentence> sentences) {
		int maxNodesInGraph = Integer.MIN_VALUE;
		for (Sentence s: sentences) {
			maxNodesInGraph = Math.max(maxNodesInGraph, s.getNumberOfNodes());
		}
		return maxNodesInGraph;
		
	}
		
	/**
	 * returns a listing of all the possible values in a list.
	 * 
	 * @return
	 */
	protected weka.core.FastVector createRelationshipStringFastVector() {
		Relationship[] list = Relationship.values();

		weka.core.FastVector result = new weka.core.FastVector(list.length +1);
		result.addElement("0");
		for (Relationship r: list) {
			result.addElement(r.getLabel());
		}
		return result;
	}

	
	/**
	 * returns a listing of all the possible values in a list.
	 * 
	 * @return
	 */
	protected weka.core.FastVector createPartOfSpeechStringFastVector() {
		PartOfSpeech[] list = PartOfSpeech.values();

		weka.core.FastVector result = new weka.core.FastVector(list.length +1);
		result.addElement("0");
		for (PartOfSpeech r: list) {
			result.addElement(r.getActualLabel());
		}
		return result;
	}	

		
	private boolean hasNERCategory(Sentence s, String nerCategory, WordType wt,  List<String> stopWords) {
		int numberOfWords = s.getNumberOfNodes();
		for (int i=0; i< numberOfWords; i++) {
			String word = s.getWordVertexAt(i).getWord(wt);
			if (stopWords.contains(word)) { continue; } // skip stop words
			
			if (s.getWordVertexAt(i).getNamedEntityRecognizedLabel().equalsIgnoreCase(nerCategory)) {
				return true;
			}
			
		}		
		
		return false;
	}
		
	
	/**
	 * Prepocess a sentence based upon the approach presented in
	 * "Identification of non-functional requirements in textual specifications: a semi-supervised approach"
	 * Casmamayor, Godoy, Campo 2009
	 * 
	 * Several pre-processing steps are followed to transform textual requirements into 
	 * vectors according to the vector space model. When dealing with unstructured text documents, 
	 * the most com- mon pre-processing tasks are normalization (including changing cases of letters,
	 *  digits, hyphens and punctuation marks), stop-word removal and stemming. 
	 *  
	 *  In a first step, normalization is performed on the documents
	 *  describing requirements. This step includes removing numbers and 
	 *  terms that contain digits, breaking hyphens to get individual words, 
	 *  removing punctuation marks, and finally converting letters to lower case. 
	 *  The next pre-processing task is stop-word removal. (They use the Glasgow list)
	 *  finally, they use the porter stemming algorithm
	 * 
	 * @param sentence
	 * @param stopWords
	 * @return
	 */
	protected static String getCasamayorStringRepresentation(String sentence, List<String> stopWords) {
	    Pattern nonWord =  Pattern.compile("[^A-Za-z]");
	    
		String result = "";
		for (String word: sentence.toString().split("[ \t]|/|-|\\.")) {
			word = word.trim().toLowerCase();
			if (word.equals("")) {continue; }
			if (word.endsWith(".")) { word = word.substring(0,word.length()-1); }  //probably not needed since I split on periods
			if (word.endsWith(":")) { word = word.substring(0,word.length()-1); }
			if (word.endsWith(")")) { word = word.substring(0,word.length()-1); }
			if (word.endsWith("'s")) { word = word.substring(0,word.length()-2); }
			if (word.endsWith("’s")) { word = word.substring(0,word.length()-2); }
			if (word.startsWith("(")) { word = word.substring(1); }
			if (word.startsWith("‘")) { word = word.substring(1); }
			if (word.startsWith("“")) { word = word.substring(1); }
			if (word.endsWith("’")) { word = word.substring(0,word.length()-1); }
			if (word.endsWith("”")) { word = word.substring(0,word.length()-1); }
			if (word.endsWith("(s")) { word = word.substring(0,word.length()-2); }
			if (word.endsWith(";")) { word = word.substring(0,word.length()-1); }
		
			Matcher m = nonWord.matcher(word);
			if (m.find()) { continue ;} // skip digits and number
			if (stopWords.contains(word)) { continue; } // skip stop words
		
			word = PorterStemmer.getStem(word);
			word = word.trim();
			if (!word.equals("")) {
				result += (word + " ");
			}
		}
		return result.trim();
	}
	
	// Code below is for access control relations
	
	
	/**
	 * builds a vector of the columns that will be exported.  (i.e., column names, types, allowable values)
	 * 
	 * the overall classification is the first column.  The other columns are ordered by their appearances in AccessControlFactor
	 * 
	 * @return
	 */
	protected weka.core.FastVector buildAttributesForAccessControlPatterns(boolean includeContextSensitiveFactors) {
		
		AccessControlFactor[] factorList = AccessControlFactor.values();
		int numAttributes =  factorList.length + 1; // This "+ 1" is for the classification of the attribute
		if (includeContextSensitiveFactors == false) {
			numAttributes = AccessControlFactor.getNumberOfContextInsensitiveFactors() + 1;
		}
		
		weka.core.FastVector attributes = new weka.core.FastVector(numAttributes);
		weka.core.FastVector stringIndicator = null;  //this needs to be null per the Weka API to represent strings

		weka.core.FastVector classificationVector = new weka.core.FastVector();
		classificationVector.addElement("yes");
		classificationVector.addElement("no");
		
		attributes.addElement(new weka.core.Attribute("category", classificationVector));
		for (AccessControlFactor acf: factorList) {
			if (includeContextSensitiveFactors == false && acf.isContextSpecific()) { continue;}  // need to skip these
			
			attributes.addElement(new weka.core.Attribute(acf.getLabel(),stringIndicator));
		}
		
		return attributes;

	}
	
	/**
	 * builds a vector of the columns that will be exported.  (i.e., column names, types, allowable values)
	 * 
	 * the overall classification is the first column.  The other columns are ordered by their appearances in DatabaseFactor
	 * 
	 * @return
	 */
	protected weka.core.FastVector buildAttributesForDatabasePatterns(boolean includeContextSensitiveFactors) {
		
		DatabaseFactor[] factorList = DatabaseFactor.values();
		int numAttributes =  factorList.length + 1; // This "+ 1" is for the classification of the attribute
		if (includeContextSensitiveFactors == false) {
			numAttributes = DatabaseFactor.getNumberOfContextInsensitiveFactors() + 1;
		}
		
		weka.core.FastVector attributes = new weka.core.FastVector(numAttributes);
		weka.core.FastVector stringIndicator = null;  //this needs to be null per the Weka API to represent strings

		weka.core.FastVector classificationVector = new weka.core.FastVector();
		classificationVector.addElement("yes");
		classificationVector.addElement("no");
		
		attributes.addElement(new weka.core.Attribute("category", classificationVector));
		for (DatabaseFactor acf: factorList) {
			if (includeContextSensitiveFactors == false && acf.isContextSpecific()) { continue;}  // need to skip these
			
			attributes.addElement(new weka.core.Attribute(acf.getLabel(),stringIndicator));
		}
		
		return attributes;

	}	
	
	
	
	/**
	 * This method generates all of the information necessary to run a classifier within WEKA
	 * If you compare it to the ARFF data format, it has the relation name, the defined attributes,
	 * and the data
	 * 
	 * @param relationName what is the relation name.  More of a table name.
	 * 
	 * @return the Instances class wrapping the attributes and data
	 */
	public weka.core.Instances createWekaInstancesForAccessControlPatterns(String relationName, boolean includeContextSensitiveFactors) {
		weka.core.FastVector attributes = this.buildAttributesForAccessControlPatterns(includeContextSensitiveFactors);
		
		
		Instances instances = new Instances(relationName, attributes, AccessControlRelationManager.getTheAccessControlRelationManager().getAccessControlPatterns().size()*2);
	    instances.setClassIndex(0);
		
	    
	    int numAttributes = attributes.size();
	    //now for the data

		for (AccessControlPattern acp: AccessControlRelationManager.getTheAccessControlRelationManager().getAccessControlPatterns()) {
			int numValidSentence = acp.getValidSentences().length;
			for (int i=0; i < numValidSentence; i++) {  // I just need to print out the invalid pattern x number of times
 				instances.add(this.createAccessControlPattternInstance(instances, acp, "yes", numAttributes, includeContextSensitiveFactors));
			}
			
			int numInvalidSentence = acp.getInvalidSentences().length;
			for (int i=0; i < numInvalidSentence; i++) {   // I just need to print out the invalid pattern x number of times
 				instances.add(this.createAccessControlPattternInstance(instances, acp, "no", numAttributes, includeContextSensitiveFactors));
			}
	
		}		
	    
		return instances;
	}
	
	private weka.core.Instance createAccessControlPattternInstance(Instances instances, AccessControlPattern acp, String category, int length, boolean includeContextSensitiveFactors) {
		AccessControlFactor[] factorList = AccessControlFactor.values();
		
		weka.core.Instance row = new weka.core.DenseInstance(length);
		row.setDataset(instances);
		row.setValue(0, category);
		
		int factorIndex = 0;
		for (AccessControlFactor f: factorList) {
			if (includeContextSensitiveFactors == false && f.isContextSpecific()) { continue;}  // need to skip these
			factorIndex++;

			row.setValue(factorIndex, f.getFactorValue(acp));
		}
		return row;	
	}
	
	/**
	 * Produces a test set consisting of just one access control pattern
	 * 
	 * @param relationName what is the relation name.  More of a table name.
	 * 
	 * @return the Instances class wrapping the attributes and data
	 */
	public weka.core.Instances createWekaInstancesForAccessControlPatterns(String relationName, AccessControlPattern acp, boolean includeContextSensitiveFactors) {
		weka.core.FastVector attributes = this.buildAttributesForAccessControlPatterns(includeContextSensitiveFactors);
		AccessControlFactor[] factorList = AccessControlFactor.values();
		
		Instances instances = new Instances(relationName, attributes, 1);
	    instances.setClassIndex(0);
		
	    //now for the data
		weka.core.Instance i = new weka.core.DenseInstance(attributes.size());
		i.setDataset(instances);
		//i.setValue(0, "");
				
		int factorIndex = 0;
		for (AccessControlFactor f: factorList) {
			if (includeContextSensitiveFactors == false && f.isContextSpecific()) { continue;}  // need to skip these
			factorIndex++;

			i.setValue(factorIndex, f.getFactorValue(acp));
		}
		instances.add(i);
	    
		return instances;
	}	
	

	/**
	 * This method generates all of the information necessary to run a classifier within WEKA
	 * If you compare it to the ARFF data format, it has the relation name, the defined attributes,
	 * and the data
	 * 
	 * @param relationName what is the relation name.  More of a table name.
	 * 
	 * @return the Instances class wrapping the attributes and data
	 */
	public weka.core.Instances createWekaInstancesForDatabasePatterns(String relationName, boolean includeContextSensitiveFactors) {
		weka.core.FastVector attributes = this.buildAttributesForDatabasePatterns(includeContextSensitiveFactors);
		
		
		Instances instances = new Instances(relationName, attributes, DatabaseRelationManager.getTheDatabaseRelationManager().getDatabasePatterns().size()*2);
	    instances.setClassIndex(0);
		
	    
	    int numAttributes = attributes.size();
	    //now for the data

		for (DatabasePattern acp: DatabaseRelationManager.getTheDatabaseRelationManager().getDatabasePatterns()) {
			int numValidSentence = acp.getValidSentences().length;
			for (int i=0; i < numValidSentence; i++) {  // I just need to print out the invalid pattern x number of times
 				instances.add(this.createDatabasePattternInstance(instances, acp, "yes", numAttributes, includeContextSensitiveFactors));
			}
			
			int numInvalidSentence = acp.getInvalidSentences().length;
			for (int i=0; i < numInvalidSentence; i++) {   // I just need to print out the invalid pattern x number of times
 				instances.add(this.createDatabasePattternInstance(instances, acp, "no", numAttributes, includeContextSensitiveFactors));
			}
	
		}		
	    
		return instances;
	}
	
	private weka.core.Instance createDatabasePattternInstance(Instances instances, DatabasePattern acp, String category, int length, boolean includeContextSensitiveFactors) {
		DatabaseFactor[] factorList = DatabaseFactor.values();
		
		weka.core.Instance row = new weka.core.DenseInstance(length);
		row.setDataset(instances);
		row.setValue(0, category);
		
		int factorIndex = 0;
		for (DatabaseFactor f: factorList) {
			if (includeContextSensitiveFactors == false && f.isContextSpecific()) { continue;}  // need to skip these
			factorIndex++;

			row.setValue(factorIndex, f.getFactorValue(acp));
		}
		return row;	
	}
	
	/**
	 * Produces a test set consisting of just one access control pattern
	 * 
	 * @param relationName what is the relation name.  More of a table name.
	 * 
	 * @return the Instances class wrapping the attributes and data
	 */
	public weka.core.Instances createWekaInstancesForDatabasePatterns(String relationName, DatabasePattern acp, boolean includeContextSensitiveFactors) {
		weka.core.FastVector attributes = this.buildAttributesForDatabasePatterns(includeContextSensitiveFactors);
		DatabaseFactor[] factorList = DatabaseFactor.values();
		
		Instances instances = new Instances(relationName, attributes, 1);
	    instances.setClassIndex(0);
		
	    //now for the data
		weka.core.Instance i = new weka.core.DenseInstance(attributes.size());
		i.setDataset(instances);
		//i.setValue(0, "");
				
		int factorIndex = 0;
		for (DatabaseFactor f: factorList) {
			if (includeContextSensitiveFactors == false && f.isContextSpecific()) { continue;}  // need to skip these
			factorIndex++;

			i.setValue(factorIndex, f.getFactorValue(acp));
		}
		instances.add(i);
	    
		return instances;
	}	
	
	
	
}
