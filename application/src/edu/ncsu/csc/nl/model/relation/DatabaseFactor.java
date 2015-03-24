package edu.ncsu.csc.nl.model.relation;

import java.io.Serializable;
import java.util.Collection;

import edu.ncsu.csc.nl.model.WordVertex;

public enum DatabaseFactor implements Serializable {

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
	FACTOR_ROOT_RELATIONSHIP("Relationship to root node","root_rel_", false),
	FACTOR_ENTITY_POS("Entity Part of Speech", "entity_pos_",false),
	FACTOR_ENITY_ATTRIBUTE("Entity Relationship", "entity_rel_",false),
	FACTOR_ENTITY_ATTRIBUTE_POS("Entity Attribute Part of Speech", "entity_attr_pos_",false),
	FACTOR_ENTITY_ATTRIBUTE_RELATIONSHIP("attribute Relationship", "attr_rel_",false),
	FACTOR_ENTITY_ATTRIBUTE_ORDERING("ordering between the entity and attribute ", "entity_rel_",false),
	
	FACTOR_TREE_POS("Treee Part of Speech", "tree_pos_", false),   // this looks at all of the parts of speech for all of the nodes
	FACTOR_TREE_RELATIONSHIP("Tree Relationship","tree_rel_", false), // string representation of the relationship into each node ..
	
	/*
	FACTOR_OBJECT_POS("Object Part of Speech", "object_pos_",false),
	FACTOR_OBJECT_RELATIONSHIP("Object Relationship", "object_rel_",false),
	FACTOR_VOICE_ORDER_SUBJECT_OBJECT("voice - active/pass, then ordering of subject and object","pos_order_subj_obj",false),
	FACTOR_VOICE_ORDER_ACTION_OBJECT("voice - active/pass, then ordering of action and object","pos_order_act_obj",false),
	FACTOR_VOICE_ORDER_SUBJECT_ACTION("voice - active/pass, then ordering of subject and action","pos_order_subj_act",false),
	*/
	FACTOR_CONTAINS_CLAUSE("has clause", "has_clause_",false),
	FACTOR_ROOT_VALUE("Root Value","rootvalue_", true),
	FACTOR_ENTITY_VALUE("Entity Value","entity_value_", true),
	FACTOR_ENTITY_ATTRIBUTE_VALUE("Entity Attribute Value","actvalue_", true);

	//TODO: Neeed to add more factors for relations and relations attributes
	
	private String _label;
	private String _prefix;
	private boolean _contextSpecific;
	
	DatabaseFactor(String label, String prefix, boolean contextSpecific) {
		_label = label;
		_prefix = prefix;
		_contextSpecific = contextSpecific;
	}
	
	public String getFactorValue(DatabasePattern dbPattern) {
		String result = "unknown";
		try {
		switch (this) {
			case FACTOR_PATTERN_SIZE:         result = this.getPrefix() + dbPattern.getRoot().getGraphSize(); break;
			case FACTOR_PATTERN_DISTANCE:     result = this.getPrefix() +Integer.toString(dbPattern.getRoot().getGreatestWordPosition() - dbPattern.getRoot().getSmallestWordPosition()); break;
			case FACTOR_ROOT_VALUE:           result = this.getPrefix() + dbPattern.getRoot().getLemma(); break;
			/* TODO: Need to add all of the FACTORS for databases here and remove old code
			case FACTOR_SUBJECT_VALUE:        result = this.getPrefix() + dbPattern.getAccessControl().getSubject(); break;
			case FACTOR_OBJECT_VALUE:         result = this.getPrefix() + dbPattern.getAccessControl().getObject(); break;
			case FACTOR_ACTION_VALUE:         result = this.getPrefix() + dbPattern.getAccessControl().getAction(); break;
			case FACTOR_ROOT_POS:             result = this.getPrefix() + dbPattern.getRoot().getPartOfSpeech().getCollapsedLabel(); break;
			case FACTOR_TREE_POS:             result = this.getPrefix() + dbPattern.getRoot().getStringRepresentationPOSOnly(); break;
			case FACTOR_TREE_RELATIONSHIP:    result = this.getPrefix() + dbPattern.getRoot().getStringRepresentationRelationshipOnly(); break;
			case FACTOR_SUBJECT_POS: 		  {  String temp = "";
					                             for (WordVertex wv: dbPattern.getAccessControl().getSubjectVertexList()) {
					                        	   if (temp.length()>0) { temp += "_"; }
					                        	   temp += wv.getPartOfSpeech();
					                             }
					                          result = this.getPrefix() + temp;
					                          }
					                          break;
			case FACTOR_SUBJECT_RELATIONSHIP: {  String temp = "";
									             for (WordVertex wv: dbPattern.getAccessControl().getSubjectVertexList()) {
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
								                 for (WordVertex wv: dbPattern.getAccessControl().getActionVertexList()) {
								       	           if (temp.length()>0) { temp += "_"; }
								       	           temp += wv.getPartOfSpeech();
								                 }
								                 result = this.getPrefix() + temp;
								              }
			                                  break;
			case FACTOR_ACTION_RELATIONSHIP:  {  String temp = "";
								                 for (WordVertex wv: dbPattern.getAccessControl().getActionVertexList()) {
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
			                                     for (WordVertex wv: dbPattern.getAccessControl().getObjectVertexList()) {
			                                       if (temp.length()>0) { temp += "_"; }
			                                       temp += wv.getPartOfSpeech();
			                                     }
			                                     result = this.getPrefix() + temp;
			                                  }
										      break;
			case FACTOR_OBJECT_RELATIONSHIP:  {  String temp = "";
												 for (WordVertex wv: dbPattern.getAccessControl().getObjectVertexList()) {
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
				                                       if (edu.ncsu.csc.nl.model.english.Voice.inPassiveVoice(dbPattern.getRoot())) { voice = this.getPrefix() +"passive_"; }
					                                   if (dbPattern.getAccessControl().getSubjectVertexList().size() == 0 || 
						                                   dbPattern.getAccessControl().getObjectVertexList().size()  == 0) {
													        result = voice+ "unknown";
				                                       } else if (dbPattern.getAccessControl().getSubjectVertexList().get(0).getWordIndex() < dbPattern.getAccessControl().getObjectVertexList().get(0).getWordIndex()) {
				                                    	   result = voice+"true";
				                                       }
				                                       else {
				                                    	   result = voice+"false";
				                                       }
			                                        } 
												    break;
			case FACTOR_VOICE_ORDER_ACTION_OBJECT: {   String voice = this.getPrefix() +"active_";
	                                                   if (edu.ncsu.csc.nl.model.english.Voice.inPassiveVoice(dbPattern.getRoot())) { voice = this.getPrefix() +"passive_"; }  
					                                   if (dbPattern.getAccessControl().getActionVertexList().size() == 0 || 
									                       dbPattern.getAccessControl().getObjectVertexList().size()  == 0) {
													        result = voice+"unknown";
									                   } else if (dbPattern.getAccessControl().getActionVertexList().get(0).getWordIndex() < dbPattern.getAccessControl().getObjectVertexList().get(0).getWordIndex()) {
									             	        result = voice+"true";
									                   }
									                   else {
									             	      result = voice+"false";
									                   }
			                                       }
				                                   break;
			case FACTOR_VOICE_ORDER_SUBJECT_ACTION: {   String voice = this.getPrefix() +"active_";
                                                        if (edu.ncsu.csc.nl.model.english.Voice.inPassiveVoice(dbPattern.getRoot())) { voice = this.getPrefix() +"passive_"; }   
                                                        if (dbPattern.getAccessControl().getSubjectVertexList().size() == 0 || 
								                            dbPattern.getAccessControl().getActionVertexList().size()  == 0) {
												            result = voice+"unknown";
								                        } else if (dbPattern.getAccessControl().getSubjectVertexList().get(0).getWordIndex() < dbPattern.getAccessControl().getActionVertexList().get(0).getWordIndex()) {
								             	            result = voice+"true";
								                        }
								                        else {
								             	            result = voice+"false";
								                        }
			                                        }
												    break;
			case FACTOR_CONTAINS_CLAUSE: result = this.getPrefix() + Boolean.toString(edu.ncsu.csc.nl.model.english.Clause.hasClause(dbPattern.getRoot()));
			                             break;
			*/								  
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
	
	public static String[] getFactorValues(Collection<DatabaseFactor> factors, DatabasePattern dbPattern) {
		String[] result = new String[factors.size()];
		int index = 0;
		for (DatabaseFactor factor: factors) {
			result[index] = factor.getFactorValue(dbPattern);
			index++;
		}
		return result;
	}
	
	public static int getNumberOfContextSensitiveFactors() {
		int result = 0;
		for (DatabaseFactor acf: DatabaseFactor.values()) {
			if (acf.isContextSpecific()) { result++; }
		}
		return result;
	}
	
	public static int getNumberOfContextInsensitiveFactors() {
		return DatabaseFactor.values().length - DatabaseFactor.getNumberOfContextSensitiveFactors();
	}
}
