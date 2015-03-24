package edu.ncsu.csc.nl.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import edu.ncsu.csc.nl.GCController;
import edu.ncsu.csc.nl.model.relation.AccessControlRelation;
import edu.ncsu.csc.nl.model.relation.DatabaseRelation;
import edu.ncsu.csc.nl.model.type.DatabaseRelationType;

public class DatabaseElementList implements TableModel {

	ArrayList<DatabaseRelation> _dbList = new ArrayList<DatabaseRelation>();
	
	
	HashSet<String> _uniqueEntities = new HashSet<String>();
	
	private transient TableModelListener _listener;
	
	
	public DatabaseElementList(NLDocument document) {

		for (Sentence s: document.getSentences()) {
			for (DatabaseRelation db: s.getDatabaseRelations()) {
				DatabaseRelation newDB = new DatabaseRelation(db);
				newDB.addSourceSentence(s);
				_dbList.add(newDB);
				
				if (newDB.getRelationType() == DatabaseRelationType.ENTITY) {
					_uniqueEntities.add(newDB.getIdentifyingNodeString());
				}
			}
			
		}
		
	}
	
	/**
	 * Checks whether or not the passed in value is an attribute of an entity.
	 * If so, it changes returns the name of the entity.
	 * 
	 * if the attribute is a unique entity, don't make a replacement.
	 * 
	 * Our process will set permissions only at the table level.
	 * 
	 * @param attribute
	 * @return
	 */
	public String getEntityForAttribute(String attribute, double sentencePosition) {
		String result = null;
		
		if (_uniqueEntities.contains(attribute)) {
			return result;
		}
		
		HashSet<DatabaseRelation> possibleEntities = new HashSet<DatabaseRelation>(); 
		for (DatabaseRelation dbr: _dbList) {
			if (dbr.getRelationType() != DatabaseRelationType.ENTITY_ATTR) { continue; }
			
			if (dbr.getIdentifyingNodeString().equalsIgnoreCase(attribute)) {
				possibleEntities.add(dbr);
				//result = dbr.getParentEntityNodeString();
				//break;
			}
		}
		
		if (possibleEntities.size() == 0) { return null; }
		if (possibleEntities.size() == 1) { for (DatabaseRelation dbr: possibleEntities) { return dbr.getParentEntityNodeString(); } }
		
		//System.out.println("possible values: "+possibleEntities);
		
		int count = 0;
		while (result == null && count < 1000) {
			HashMap<String, Integer> words = new HashMap<String, Integer>();
			for (DatabaseRelation dbr: possibleEntities)  {
				words.put(dbr.getParentEntityNodeString(), 0);
			}
			GCController.getTheGCController().getCurrentDocument().countWords(words, sentencePosition - count, sentencePosition + count);
			
			result = edu.ncsu.csc.nl.util.Utility.getMostFrequentlyOccuringWord(words);
			
			count++;
		}
		if (result != null) {
			//System.out.println("  choosing "+result);
		}
		
		return result;
	}
	
	
	

	@Override
	public int getRowCount() {
		// TODO Auto-generated method stub
		return _dbList.size();
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
