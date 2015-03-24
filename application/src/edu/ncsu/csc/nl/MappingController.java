package edu.ncsu.csc.nl;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;



import java.util.HashMap;
import java.util.List;

import edu.ncsu.csc.nl.model.AccessControlRuleList;
import edu.ncsu.csc.nl.model.DatabaseElementList;
import edu.ncsu.csc.nl.model.relation.AccessControlRelation;
import edu.ncsu.csc.nl.model.relation.DatabaseRelation;
import edu.ncsu.csc.nl.util.Logger;


/**
 * 
 * 
 * @author Adminuser
 */
public class MappingController implements ActionListener {
	private static MappingController _theMappingController = new MappingController();
	public static MappingController getTheMappingController() {return _theMappingController; }
	
	
	private MappingController() {	}


	@Override
	public void actionPerformed(ActionEvent ae) {
		GCController.getTheGCController().setStatusMessage("");
		
		switch (ae.getActionCommand()) {
		
			case GCConstants.ACTION_MAPPING_START: initiateMapping(); return;
			case GCConstants.ACTION_MAPPING_ATTRIBUTE: replaceAttributesWithEnities(); return;
			case GCConstants.ACTION_MAPPING_RESOLVE_SUBJECTS: resolveSubjects(); return; 
			case GCConstants.ACTION_MAPPING_RESOLVE_OBJECTS: resolveObjects(); return;
			case GCConstants.ACTION_MAPPING_MERGE_RULES: mergeRules(); return; 
			case GCConstants.ACTION_MAPPING_LOAD_DB_TABLES: GCController.getTheGCController().setStatusMessage("loadDBTables"); return;
			case GCConstants.ACTION_MAPPING_MAP_DB_TABLES: GCController.getTheGCController().setStatusMessage("mapDBTables"); return; 
			case GCConstants.ACTION_MAPPING_RPT_TRACEABILITY: printTraceabilityReport(); return;
			case GCConstants.ACTION_MAPPING_RPT_CONFLICT: printConflictReport(); return;
			case GCConstants.ACTION_MAPPING_RPT_SQL_AC: printSQLReport(); return;
			case GCConstants.ACTION_MAPPING_RPT_MONGODB_AC: printMongoDBReport(); return;  
		}
	}
	
	
	public void initiateMapping() {
		Logger.log(Logger.LEVEL_INFO, "ACR Mapping - initiate");
		
		AccessControlRuleList acrList = new AccessControlRuleList(GCController.getTheGCController().getCurrentDocument());
		GCController.getTheGCController().setACRList(acrList);
		
		DatabaseElementList del = new DatabaseElementList(GCController.getTheGCController().getCurrentDocument());
		GCController.getTheGCController().setDatabaseElementList(del);
		
		int aCount = acrList.getNumberOfAmbiguousRules();
		
		GCController.getTheGCController().setStatusMessage("Mapping initiated.  Initial number of rules: "+acrList.getRowCount()+", ambiguous count: "+ aCount);

		Logger.log(Logger.LEVEL_INFO, "Mapping initiated.  Initial number of rules: "+acrList.getRowCount());
		Logger.log(Logger.LEVEL_INFO, "Mapping initiated.  Initial number of ambiguous rules: "+aCount);		
		Logger.log(Logger.LEVEL_INFO, "Mapping initiated.  "+ acrList.getObjectResolvedCounts());
		Logger.log(Logger.LEVEL_INFO, "Mapping initiated.  Initial number of database elements: "+del.getRowCount());		
		
	}
	
	
	public void replaceAttributesWithEnities() {
		Logger.log(Logger.LEVEL_INFO, "ACR Mapping - replace Attributes with Entities");
		
		int numReplaced = GCController.getTheGCController().getACRList().replaceAttributesWithEntities();
		Logger.log(Logger.LEVEL_INFO, "Attribute Replacement: replaced # of attributes: "+numReplaced);
		
		GCController.getTheGCController().setStatusMessage("Attribute Replacement: replaced # of attributes: "+numReplaced);
	}
	
	
	public void resolveSubjects() {
		int numReplaced = GCController.getTheGCController().getACRList().resolveSubjects();
		Logger.log(Logger.LEVEL_INFO, "Subject resolution: replaced # of subjects: "+numReplaced);
		GCController.getTheGCController().setStatusMessage("Subject resolution: replaced - "+numReplaced);
	}
	
	public void resolveObjects() {
		int numReplaced = GCController.getTheGCController().getACRList().resolveObjects();
		Logger.log(Logger.LEVEL_INFO, "Subject resolution: replaced # of objects: "+numReplaced);
		GCController.getTheGCController().setStatusMessage("Object resolution: replaced - "+numReplaced);
	}
	
	public void mergeRules() {
		int numMerged = GCController.getTheGCController().getACRList().mergeACRs();
		Logger.log(Logger.LEVEL_INFO, "Number of rules merged: "+numMerged);
		GCController.getTheGCController().setStatusMessage("Merged rules - "+numMerged);
		
		GCController.getTheGCController().setStatusMessage("Merged rules - "+numMerged); return;
	}
	
	public void printTraceabilityReport() {
		GCController.getTheGCController().getACRList().printTraceabilityReport();
		GCController.getTheGCController().setStatusMessage("traceability");
	}
	
	public void printConflictReport() {
		GCController.getTheGCController().getACRList().printConflictReport();
		GCController.getTheGCController().setStatusMessage("conflict");
	}
	
	public void printSQLReport() {
		GCController.getTheGCController().getACRList().printSQLReport();
		GCController.getTheGCController().setStatusMessage("sql"); 
	}
	
	public void printMongoDBReport() {
		GCController.getTheGCController().getACRList().printMongoDBReport();
		GCController.getTheGCController().setStatusMessage("mongo");
	}
}
