package edu.ncsu.csc.nl.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import edu.ncsu.csc.nl.GCController;
import edu.ncsu.csc.nl.model.relation.AccessControlRelation;
import edu.ncsu.csc.nl.model.relation.DatabaseRelation;
import edu.ncsu.csc.nl.model.type.DatabaseRelationType;
import edu.ncsu.csc.nl.model.type.DatabaseRelationshipType;
import edu.ncsu.csc.nl.util.Logger;

public class AccessControlRuleList implements TableModel {

	ArrayList<AccessControlRelation> _acrList = new ArrayList<AccessControlRelation>();

	
	
	private transient TableModelListener _listener;
	
	
	public AccessControlRuleList(NLDocument document) {

		for (Sentence s: document.getSentences()) {
			for (AccessControlRelation acr: s.getAccessControlRelations()) {
				AccessControlRelation newACR = new AccessControlRelation(acr);
				newACR.addSourceSentence(s);
				_acrList.add(newACR);
			}
			if (s.getAccessControlRelations().size() == 0) { continue; } // since no access control was defined for the sentence, we shouldn't have to worry new relations or plural entity attributes
			
			//Look to see if we also need to add ACRs for relations not covered by existing rules and for entity-attibutes where the attributes are plural
			for (DatabaseRelation dbr: s.getDatabaseRelations()) {
				if (dbr.getRelationType() == DatabaseRelationType.ENTITY_ATTR) {
					List<WordVertex> wvList = dbr.getIndentifyingNode();
					for (WordVertex wv: wvList) {
						if (wv.getPartOfSpeech().isPlural()) {						
							String objectName = dbr.getParentEntityNodeString() +"_"+dbr.getIdentifyingNodeString()+"_map";
							List<AccessControlRelation> rulesForAttribute = s.getAccessControlRulesWithObject(dbr.getIdentifyingNodeString(),dbr.getParentEntityNodeString());
							for (AccessControlRelation acr: rulesForAttribute) {
								AccessControlRelation newACR = new AccessControlRelation(acr);
								newACR.addSourceSentence(s);
								newACR.setResolvedObject(objectName);
								newACR.setResolvedObjectReason("created from multi-valued attribute");
								_acrList.add(newACR);
							}					
						}
					}
				}
				else if (dbr.getRelationType() == DatabaseRelationType.RELATIONSHIP) {
					if (dbr.getRelationshipType() != DatabaseRelationshipType.ASSOCIATION) { continue; }   // only consider adding rules for associations (is-a / Composition rules need not apply ;) )
					
					if (s.hasAccessControlBySubjectActionObject(dbr.getFirstEntityNodeString(), dbr.getIdentifyingNodeString(), dbr.getSecondEntityNodeString()) ||
						s.hasAccessControlBySubjectActionObject(dbr.getSecondEntityNodeString(), dbr.getIdentifyingNodeString(), dbr.getFirstEntityNodeString()) ) {
						
						continue;
					}
					String objectName =  dbr.getIdentifyingNodeString()+"_"+ dbr.getFirstEntityNodeString()+"_"+ dbr.getSecondEntityNodeString() +"_map";
					List<AccessControlRelation> rulesForRelation = s.getAccessControlRulesWithObject(dbr.getFirstEntityNodeString(), dbr.getSecondEntityNodeString());
					for (AccessControlRelation acr: rulesForRelation) {
						AccessControlRelation newACR = new AccessControlRelation(acr);
						newACR.addSourceSentence(s);
						newACR.setResolvedObject(objectName);
						newACR.setResolvedObjectReason("created from DB association relation");
						_acrList.add(newACR);
					}			
					
				}
			}
		}
		
	}
	
	public int getNumberOfAmbiguousRules() {
		int resultCount = 0;
		
		for (AccessControlRelation acr: _acrList) {
			if (acr.isAmbiguous()) { resultCount++; }
		}
		return resultCount;
	}
	
	public int replaceAttributesWithEntities() {
		int numReplaced = 0;
		
		DatabaseElementList del = GCController.getTheGCController().getADatabaseElementList();
		for (AccessControlRelation acr: _acrList) {
			if (acr.getResolvedObject() == null || acr.getResolvedObject().equals("") == false) {
				String entity = del.getEntityForAttribute(acr.getObject(),acr.getSourceSentences().get(0).getOriginalSentencePosition());
				if (entity != null) {
					Logger.log(Logger.LEVEL_INFO, "Attribute: replacing "+acr.getObject()+" with "+entity);
					
					acr.setResolvedObject(entity);
					acr.setResolvedObjectReason("Attribute to Entity");
					numReplaced++;
				}
			}
		}

		return numReplaced;		
	}
	
	public HashMap<String, Integer> getObjectResolvedCounts() {
		HashMap<String, Integer> result = new HashMap<String, Integer>();
		
		for (AccessControlRelation acr: _acrList) {
			if (acr.getResolvedObjectReason() != null && acr.getResolvedObjectReason().equals("") == false) {
				String reason = acr.getResolvedObjectReason();
				int count = result.containsKey(reason) ? result.get(reason) : 0;
				result.put(reason, count + 1);
			}
		}
		
		return result;
	}
	
	public ResolutionResult findPreviousUnambiguousSubject(int position) {
		position = position - 1;
		
		for (int i=position; i >= 0; i--) {
			AccessControlRelation acr = _acrList.get(i);
			if (!acr.isSubjectAmbiguous()) {
				return new ResolutionResult(acr.getResolvedSubject(), acr.getSourceSentences().get(0).getOriginalSentencePosition());
			}
		}
				
		return null;
	}
	
	public ResolutionResult findPreviousUnambiguousObject(int position) {
		position = position - 1;
		
		for (int i=position; i >= 0; i--) {
			AccessControlRelation acr = _acrList.get(i);
			if (!acr.isObjectAmbiguous()) {
				return new ResolutionResult(acr.getResolvedObject(), acr.getSourceSentences().get(0).getOriginalSentencePosition());
			}
		}
				
		return null;
	}
	
	
	public int resolveSubjects() {
		int numReplaced = 0;
		
		DatabaseElementList del = GCController.getTheGCController().getADatabaseElementList();
		
		for (int i=0;i < _acrList.size(); i++) {
			AccessControlRelation acr = _acrList.get(i);

			if (acr.isSubjectAmbiguous()) {
				System.out.println(acr.getSourceSentences().get(0).getOriginalSentencePosition() +": "+acr);

				ResolutionResult rrAssigned = GCController.getTheGCController().getCurrentDocument().getMostRecentlyAssignedRole(acr.getSourceSentences().get(0));
				ResolutionResult rrFoundInRules = this.findPreviousUnambiguousSubject(i);
				
				numReplaced++;
				if (rrAssigned == null && rrFoundInRules == null) {
					System.out.println("--- unable to resolve");
					numReplaced--; // saves putting the code on all of the lines....
				}
				else if (rrAssigned == null) {
					acr.setResolvedSubject(rrFoundInRules.getValue());
					acr.setResolvedSubjectReason("resolved from prior rule");
				}
				else if (rrFoundInRules == null) {
					acr.setResolvedSubject(rrAssigned.getValue());
					acr.setResolvedSubjectReason("resolved from user assigned value");				
				}
				else {
					if (rrFoundInRules.getSentencePosition() > rrAssigned.getSentencePosition()) {
						acr.setResolvedSubject(rrFoundInRules.getValue());
						acr.setResolvedSubjectReason("resolved from prior rule");
					}
					else {
						acr.setResolvedSubject(rrAssigned.getValue());
						acr.setResolvedSubjectReason("resolved from user assigned value");
					}
				}
				
				
			}
		}

		return numReplaced;		
	}	

	public int resolveObjects() {
		int numReplaced = 0;
		
		for (int i=0;i < _acrList.size(); i++) {
			AccessControlRelation acr = _acrList.get(i);

			if (acr.isObjectAmbiguous()) {
				System.out.println(acr.getSourceSentences().get(0).getOriginalSentencePosition() +": "+acr);
				System.out.println(acr.getSourceSentences().get(0));
				
				
				if (acr.getObjectVertexList().size() ==0) { continue; } // can't resolve missing subjects
				if (acr.getObjectVertexList().get(0).getPartOfSpeech().isPronomial()) {
					ResolutionResult rrFoundInRules = this.findPreviousUnambiguousObject(i);
					if (rrFoundInRules != null) {
						acr.setResolvedObject(rrFoundInRules.getValue());
						acr.setResolvedObjectReason("resolved from prior rule");
						
						numReplaced++;
					}
					continue;
				}
				
				// Test if the wordvertex has a prepositional child.  If there is one, may have multiple children that need to be handled....
				List<String> childObjects = this.getObjectOfPrepositionalChildren(acr.getObjectVertexList().get(0));
				if (childObjects.size() > 0 ) {
					_acrList.remove(i);
					for (String newObj: childObjects) {
						AccessControlRelation newAcr = new AccessControlRelation(acr);
						newAcr.addSourceSentence(acr.getSourceSentences().get(0));
						newAcr.setResolvedSubject(acr.getResolvedSubject());
						newAcr.setResolvedSubjectReason(acr.getResolvedSubjectReason());
						newAcr.setResolvedObject(newObj);
						newAcr.setResolvedObjectReason("resolved from prepositional phrase");
						_acrList.add(i, newAcr);
						
						numReplaced++;
					}
					
					continue;
				}	
			}
		}
		

		return numReplaced;		
	}	
	
	
	private List<String> getObjectOfPrepositionalChildren(WordVertex wv) {
		ArrayList<String> result = new ArrayList<String>();
		
		for (int i=0; i< wv.getNumberOfChildren(); i++) {
			WordEdge we = wv.getChildAt(i);
			
			if (we.getRelationship().isPreposition()) {
				result.add(we.getChildNode().getLemma());
			}
		}
		
		return result;
	}
	
	
	public int mergeACRs() {
		int numMerged = 0;
		
		for (int i =_acrList.size()-1; i>=1; i--) {
			for (int j = i-1; j>=0; j--) {
				AccessControlRelation acr_i = _acrList.get(i);
				AccessControlRelation acr_j = _acrList.get(j);
				
				if (acr_i.isCombinableForMapping(acr_j)) {
					System.out.println("Merging from sentences: "+ acr_i.getSourceSentences().get(0).getOriginalSentencePosition()+", "+acr_j.getSourceSentences().get(0).getOriginalSentencePosition());
					acr_j.mergeForMapping(acr_i);
					_acrList.remove(i);
					numMerged++;
					break;
				}
			}
		}
		
		
		
		return numMerged;
	}
	
	
	
	
	public void printTraceabilityReport() {
		HashSet<String> uniqueSubjects = new HashSet<String>();
		HashSet<String> uniqueObjects = new HashSet<String>();
		
		for (AccessControlRelation acr: _acrList) {
			String acrString = acr.toString();
			String acrResolved = acr.toResolvedString();
			
			System.out.println(acr);
			if (!acrString.equals(acrResolved)) {
				System.out.println(acr.toResolvedString());
			}
			
			uniqueSubjects.add(acr.getResolvedSubject());
			uniqueObjects.add(acr.getResolvedObject());
			
			for (Sentence s: acr.getSourceSentences()) {
				System.out.println("\t"+s.getOriginalSentencePosition()+"\t"+s);
			}
			
			
			
		}		 
		
		System.out.println("Subjects: " + uniqueSubjects.size() );
		ArrayList<String> sortedSubjects = new ArrayList<String>(uniqueSubjects);
		Collections.sort(sortedSubjects);
		for (String s: sortedSubjects) {
			System.out.println("\t"+s);
		}
		
		System.out.println("Objects: " + uniqueObjects.size() );
		ArrayList<String> sortedObjects = new ArrayList<String>(uniqueObjects);
		Collections.sort(sortedObjects);
		for (String o: sortedObjects) {
			System.out.println("\t"+o);
		}		
		
	}	

	public void printConflictReport() {
		
	}	
	
	public void printSQLReport() {

	}
	
	public void printMongoDBReport() {
	}
	
	
	@Override
	public int getRowCount() {
		return _acrList.size();
	}

	@Override
	public int getColumnCount() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getColumnName(int columnIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addTableModelListener(TableModelListener l) {
		_listener = l;
		
	}

	@Override
	public void removeTableModelListener(TableModelListener l) {
		_listener = null;
	}	

}
