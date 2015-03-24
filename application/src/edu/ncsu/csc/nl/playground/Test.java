package edu.ncsu.csc.nl.playground;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Test {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		/*
		Permission p = Permission.valueOf(Permission.class,"CREATE");
		System.out.println(p);
		p = Permission.valueOf("UPDATE");
		System.out.println(p);
*/
		
		String patternstr = "system(\\s)+displays";
		Pattern pattern = Pattern.compile(patternstr);		
		  String text = "The system   display data.";
			
		  Matcher matcher = pattern.matcher(text);
		  boolean matchFound = matcher.find(); 
		  if (matchFound) {	
			  System.out.println("yes");
		  }
		  
		
	}

}
