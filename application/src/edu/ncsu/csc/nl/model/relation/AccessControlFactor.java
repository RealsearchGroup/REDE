package edu.ncsu.csc.nl.model.relation;

import java.io.Serializable;
import java.util.Collection;

import edu.ncsu.csc.nl.model.WordVertex;

public enum AccessControlFactor implements Serializable {

	/*
	String factor_pattern_size      = "size_"+this.getRoot().getGraphSize();
	String factor_root_value        = "root_"+this.getRoot().getLemma();
	String factor_root_POS          = "root_pos_"+this.getRoot().getPartOfSpeech().getCollapsedLabel();
	String factor_TREE_POS          = "TREE_POS_"+this.getRoot().getStringRepresentationPOSOnly();
	String factor_TREE_RELATIONSHIP = "TREE_REL_"+this.getRoot().getStringRepresentationRelationshipOnly();
    */

	FACTOR_PATTERN_SIZE("Pattern size","size_", false),
	FACTOR_PATTERN_DISTANCE("Pattern Distance","distance_", false),
	FACTOR_ROOT_POS("Root Part of Speech", "rootpos_", false),
	FACTOR_TREE_POS("Treee Part of Speech", "tree_pos_", false),   // this looks at all of the parts of speech for all of the nodes
	FACTOR_TREE_RELATIONSHIP("Tree Relationship","tree_rel_", false),
	FACTOR_SUBJECT_POS("Subject Part of Speech", "subject_pos_",false),
	FACTOR_SUBJECT_RELATIONSHIP("Subject Relationship", "subject_rel_",false),
	FACTOR_ACTION_POS("Action Part of Speech", "action_pos_",false),
	FACTOR_ACTION_RELATIONSHIP("Action Relationship", "action_rel_",false),
	FACTOR_OBJECT_POS("Object Part of Speech", "object_pos_",false),
	FACTOR_OBJECT_RELATIONSHIP("Object Relationship", "object_rel_",false),
	FACTOR_VOICE_ORDER_SUBJECT_OBJECT("voice - active/pass, then ordering of subject and object","pos_order_subj_obj",false),
	FACTOR_VOICE_ORDER_ACTION_OBJECT("voice - active/pass, then ordering of action and object","pos_order_act_obj",false),
	FACTOR_VOICE_ORDER_SUBJECT_ACTION("voice - active/pass, then ordering of subject and action","pos_order_subj_act",false),
	FACTOR_CONTAINS_CLAUSE("has clause", "has_clause_",false),
	FACTOR_ROOT_VALUE("Root Value","rootvalue_", true),
	FACTOR_SUBJECT_VALUE("Subject Value","subvalue_", true),
	FACTOR_ACTION_VALUE("Action Value","actvalue_", true),
	FACTOR_OBJECT_VALUE("Object Value","objvalue_", true);
	
	private String _label;
	private String _prefix;
	private boolean _contextSpecific;
	
	AccessControlFactor(String label, String prefix, boolean contextSpecific) {
		_label = label;
		_prefix = prefix;
		_contextSpecific = contextSpecific;
	}
	
	public String getFactorValue(AccessControlPattern acp) {
		String result = "unknown";
		try {
		switch (this) {
			case FACTOR_PATTERN_SIZE:         result = this.getPrefix() + acp.getRoot().getGraphSize(); break;
			case FACTOR_PATTERN_DISTANCE:     result = this.getPrefix() +Integer.toString(acp.getRoot().getGreatestWordPosition() - acp.getRoot().getSmallestWordPosition()); break;
			case FACTOR_ROOT_VALUE:           result = this.getPrefix() + acp.getRoot().getLemma(); break;
			case FACTOR_SUBJECT_VALUE:        result = this.getPrefix() + acp.getAccessControl().getSubject(); break;
			case FACTOR_OBJECT_VALUE:         result = this.getPrefix() + acp.getAccessControl().getObject(); break;
			case FACTOR_ACTION_VALUE:         result = this.getPrefix() + acp.getAccessControl().getAction(); break;
			case FACTOR_ROOT_POS:             result = this.getPrefix() + acp.getRoot().getPartOfSpeech().getCollapsedLabel(); break;
			case FACTOR_TREE_POS:             result = this.getPrefix() + acp.getRoot().getStringRepresentationPOSOnly(); break;
			case FACTOR_TREE_RELATIONSHIP:    result = this.getPrefix() + acp.getRoot().getStringRepresentationRelationshipOnly(); break;
			case FACTOR_SUBJECT_POS: 		  {  String temp = "";
					                             for (WordVertex wv: acp.getAccessControl().getSubjectVertexList()) {
					                        	   if (temp.length()>0) { temp += "_"; }
					                        	   temp += wv.getPartOfSpeech();
					                             }
					                          result = this.getPrefix() + temp;
					                          }
					                          break;
			case FACTOR_SUBJECT_RELATIONSHIP: {  String temp = "";
									             for (WordVertex wv: acp.getAccessControl().getSubjectVertexList()) {
									          	   if (temp.length()>0) { temp += "_"; }
									          	   if (wv.getNumberOfParents() > 0) {
									          		   temp += wv.getParentAt(0).getRelationship();
									          	   }
									          	   else {
									          		   temp += "ROOT";
									          	   }
									             }
									             result = this.getPrefix() + temp;
											  } 
											  break;
			case FACTOR_ACTION_POS:           {  String temp = "";
								                 for (WordVertex wv: acp.getAccessControl().getActionVertexList()) {
								       	           if (temp.length()>0) { temp += "_"; }
								       	           temp += wv.getPartOfSpeech();
								                 }
								                 result = this.getPrefix() + temp;
								              }
			                                  break;
			case FACTOR_ACTION_RELATIONSHIP:  {  String temp = "";
								                 for (WordVertex wv: acp.getAccessControl().getActionVertexList()) {
									          	   if (temp.length()>0) { temp += "_"; }
									          	   if (wv.getNumberOfParents() > 0) {
									          		   temp += wv.getParentAt(0).getRelationship();
									          	   }
									          	   else {
									          		   temp += "ROOT";
									          	   }
									             }
									             result = this.getPrefix() + temp;
											  } 
											  break;
			case FACTOR_OBJECT_POS: 		  {  String temp = "";
			                                     for (WordVertex wv: acp.getAccessControl().getObjectVertexList()) {
			                                       if (temp.length()>0) { temp += "_"; }
			                                       temp += wv.getPartOfSpeech();
			                                     }
			                                     result = this.getPrefix() + temp;
			                                  }
										      break;
			case FACTOR_OBJECT_RELATIONSHIP:  {  String temp = "";
												 for (WordVertex wv: acp.getAccessControl().getObjectVertexList()) {
													 if (temp.length()>0) { temp += "_"; }
													 if (wv.getNumberOfParents() > 0) {
										          	   temp += wv.getParentAt(0).getRelationship();
										          	 }
										          	 else {
										          	   temp += "ROOT";
										          	 }
												 }
												 result = this.getPrefix() + temp;
											  } 
											  break;
		   case FACTOR_VOICE_ORDER_SUBJECT_OBJECT: {   String voice = this.getPrefix() +"active_";
				                                       if (edu.ncsu.csc.nl.model.english.Voice.inPassiveVoice(acp.getRoot())) { voice = this.getPrefix() +"passive_"; }
					                                   if (acp.getAccessControl().getSubjectVertexList().size() == 0 || 
						                                   acp.getAccessControl().getObjectVertexList().size()  == 0) {
													        result = voice+ "unknown";
				                                       } else if (acp.getAccessControl().getSubjectVertexList().get(0).getWordIndex() < acp.getAccessControl().getObjectVertexList().get(0).getWordIndex()) {
				                                    	   result = voice+"true";
				                                       }
				                                       else {
				                                    	   result = voice+"false";
				                                       }
			                                        } 
												    break;
			case FACTOR_VOICE_ORDER_ACTION_OBJECT: {   String voice = this.getPrefix() +"active_";
	                                                   if (edu.ncsu.csc.nl.model.english.Voice.inPassiveVoice(acp.getRoot())) { voice = this.getPrefix() +"passive_"; }  
					                                   if (acp.getAccessControl().getActionVertexList().size() == 0 || 
									                       acp.getAccessControl().getObjectVertexList().size()  == 0) {
													        result = voice+"unknown";
									                   } else if (acp.getAccessControl().getActionVertexList().get(0).getWordIndex() < acp.getAccessControl().getObjectVertexList().get(0).getWordIndex()) {
									             	        result = voice+"true";
									                   }
									                   else {
									             	      result = voice+"false";
									                   }
			                                       }
				                                   break;
			case FACTOR_VOICE_ORDER_SUBJECT_ACTION: {   String voice = this.getPrefix() +"active_";
                                                        if (edu.ncsu.csc.nl.model.english.Voice.inPassiveVoice(acp.getRoot())) { voice = this.getPrefix() +"passive_"; }   
                                                        if (acp.getAccessControl().getSubjectVertexList().size() == 0 || 
								                            acp.getAccessControl().getActionVertexList().size()  == 0) {
												            result = voice+"unknown";
								                        } else if (acp.getAccessControl().getSubjectVertexList().get(0).getWordIndex() < acp.getAccessControl().getActionVertexList().get(0).getWordIndex()) {
								             	            result = voice+"true";
								                        }
								                        else {
								             	            result = voice+"false";
								                        }
			                                        }
												    break;
			case FACTOR_CONTAINS_CLAUSE: result = this.getPrefix() + Boolean.toString(edu.ncsu.csc.nl.model.english.Clause.hasClause(acp.getRoot()));
			                             break;
											  
		}
		}
		catch (Exception e) {
			System.err.println("ACF("+this+"): "+e);
			result="unknown";
		}
		return result;
	}
	
	public String getPrefix() {
		return _prefix;
	}
	
	public String getLabel() {
		return _label;
	}
	
	public boolean isContextSpecific() {
		return _contextSpecific;
	}
	
	public static String[] getFactorValues(Collection<AccessControlFactor> factors, AccessControlPattern acp) {
		String[] result = new String[factors.size()];
		int index = 0;
		for (AccessControlFactor factor: factors) {
			result[index] = factor.getFactorValue(acp);
			index++;
		}
		return result;
	}
	
	public static int getNumberOfContextSensitiveFactors() {
		int result = 0;
		for (AccessControlFactor acf: AccessControlFactor.values()) {
			if (acf.isContextSpecific()) { result++; }
		}
		return result;
	}
	
	public static int getNumberOfContextInsensitiveFactors() {
		return AccessControlFactor.values().length - AccessControlFactor.getNumberOfContextSensitiveFactors();
	}
}
