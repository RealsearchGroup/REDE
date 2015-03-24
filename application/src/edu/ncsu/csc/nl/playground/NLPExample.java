package edu.ncsu.csc.nl.playground;

import edu.stanford.nlp.ling.CoreAnnotations.LemmaAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.NamedEntityTagAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TextAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;

public class NLPExample {
	public static void dumpSentenceValues(Annotation document) {
	    java.util.List<CoreMap> sentences = document.get(SentencesAnnotation.class);
	    
	    for(CoreMap sentence: sentences) {
	    	//temporary debugging code to list all of the annotations defined.
	    	System.out.println("Defined Annotations:");
	    	java.util.Set keys = sentence.keySet();
	    	
	    	for (Object o: keys) {
	    		System.out.println("  "+o.getClass().getName()+":"+o);
	    	}
	    
	    	
		    for (CoreLabel token: sentence.get(TokensAnnotation.class)) {
		    	// this is the text of the token
			    String word = token.get(TextAnnotation.class);
			    // this is the POS tag of the token
			    String pos = token.get(PartOfSpeechAnnotation.class);
			    // this is the NER label of the token
			    String ne = token.get(NamedEntityTagAnnotation.class);
			    String lemma = token.get(LemmaAnnotation.class);
		        
			    System.out.println(word+":"+pos+":"+ne+":"+lemma);
			        
			}
	    }
	}	
}
