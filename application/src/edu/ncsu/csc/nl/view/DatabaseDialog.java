package edu.ncsu.csc.nl.view;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import edu.ncsu.csc.nl.GCController;
import edu.ncsu.csc.nl.model.Sentence;
import edu.ncsu.csc.nl.model.WordVertex;
import edu.ncsu.csc.nl.model.relation.AccessControlRelation;
import edu.ncsu.csc.nl.model.relation.DatabaseRelation;
import edu.ncsu.csc.nl.model.type.DatabaseCardinalityType;
import edu.ncsu.csc.nl.model.type.DatabaseRelationType;
import edu.ncsu.csc.nl.model.type.DatabaseRelationshipType;
import edu.ncsu.csc.nl.model.type.RelationSource;
import edu.ncsu.csc.nl.model.type.SecurityImpact;
import edu.ncsu.csc.nl.model.type.SecurityMitigation;
import edu.ncsu.csc.nl.model.type.SecurityObjective;

public class DatabaseDialog extends JDialog implements TableModel, ActionListener {

	/** */
	private static final long serialVersionUID = 1L;
	
	JButton _btnAdd      = new JButton("Add");		
	JButton _btnDelete   = new JButton("Delete");
	JButton _btnMarkNext = new JButton("Mark/Next");
	JButton _btnMark     = new JButton("Mark");
	JButton _btnUnmark   = new JButton("Unmark");
	
	JTable _jtDatabase;
	
	private int[] _columnWidths;
	
	Sentence _currentSentence = new Sentence();
	
	public DatabaseDialog(MainFrame mf) {
		super(mf,false);
		
		this.setContentPane(this.createControlPane());
		this.establishAccessListeners();
		
		this.setSize(350, 300);
		this.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);

	}
	
	private JPanel createControlPane() {
		JPanel panel = new JPanel(new BorderLayout());
		
		JPanel bottomButtonPanel = new JPanel(new GridLayout(1, 5, 0, 0));
		bottomButtonPanel.add(_btnAdd);
		bottomButtonPanel.add(_btnDelete);
		bottomButtonPanel.add(_btnMarkNext);
		bottomButtonPanel.add(_btnMark);
		bottomButtonPanel.add(_btnUnmark);
		
		_jtDatabase = new JTable(this);
		JScrollPane scrollPane = new JScrollPane(_jtDatabase);
		_jtDatabase.setFillsViewportHeight(true);	
		_jtDatabase.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
		new DatabaseTableEditableCellFocusAction(_jtDatabase, KeyStroke.getKeyStroke("TAB"));
		
		panel.add(scrollPane,BorderLayout.CENTER);
		panel.add(bottomButtonPanel, BorderLayout.SOUTH);
		
		this.setupComboBoxEditors();
		_columnWidths = new int[12];
		
		return panel;
	}
	
	private void establishAccessListeners() {
		_btnAdd.addActionListener(this);
		_btnDelete.addActionListener(this);
		_btnMarkNext.addActionListener(this);
		_btnMark.addActionListener(this);
		_btnUnmark.addActionListener(this);
	}
	
	
	public void setupComboBoxEditors() {

		TableColumn columnRelationType = _jtDatabase.getColumnModel().getColumn(1);
		JComboBox<DatabaseRelationType> comboBoxRT = new JComboBox<DatabaseRelationType>();
		for (DatabaseRelationType drt: DatabaseRelationType.getSelectableList()) {comboBoxRT.addItem(drt); }
		columnRelationType.setCellEditor(new DefaultCellEditor(comboBoxRT));

		TableColumn columnRelationship = _jtDatabase.getColumnModel().getColumn(3);
		JComboBox<DatabaseRelationshipType> comboBoxRel = new JComboBox<DatabaseRelationshipType>();
		for (DatabaseRelationshipType si: DatabaseRelationshipType.getSelectableList()) {comboBoxRel.addItem(si); }
		columnRelationship.setCellEditor(new DefaultCellEditor(comboBoxRel));

		TableColumn columnFirstCard = _jtDatabase.getColumnModel().getColumn(5);
		JComboBox<DatabaseCardinalityType> comboBoxFirstCard = new JComboBox<DatabaseCardinalityType>();
		for (DatabaseCardinalityType sm: DatabaseCardinalityType.getSelectableList()) {comboBoxFirstCard.addItem(sm); }
		columnFirstCard.setCellEditor(new DefaultCellEditor(comboBoxFirstCard));		
		
		TableColumn columnSecondCard = _jtDatabase.getColumnModel().getColumn(8);
		JComboBox<DatabaseCardinalityType> comboBoxSecondCard = new JComboBox<DatabaseCardinalityType>();
		for (DatabaseCardinalityType sm: DatabaseCardinalityType.getSelectableList()) {comboBoxSecondCard.addItem(sm); }
		columnSecondCard.setCellEditor(new DefaultCellEditor(comboBoxSecondCard));		
				
	}	
	
	
	
	public void setCurrentSentence(Sentence s) {
		this.recordColumnWidths();
		
		_currentSentence = s;
		_jtDatabase.tableChanged(null);
		this.enableButtonsFromDatabaseComplete();
		this.setupComboBoxEditors();
		this.restoreColumnWidths();
	}

	private void enableButtonsFromDatabaseComplete() {
		if (_currentSentence.isDatabaseComplete()) {
			_btnMark.setEnabled(false);
			_btnMarkNext.setEnabled(false);
			_btnUnmark.setEnabled(true);	
		}
		else {
			_btnMark.setEnabled(true);
			_btnMarkNext.setEnabled(true);
			_btnUnmark.setEnabled(false);	
		}
	}
	
	
	@Override
	public int getRowCount() {
		return _currentSentence.getNumberOfDatabaseRelations();
	}

	@Override
	public int getColumnCount() {
		return 12;
	}

	@Override
	public String getColumnName(int columnIndex) {
		switch (columnIndex) {
		case 0: return "Ident. Node";
		case 1: return "Relation Type";
		case 2: return "Parent Node";
		case 3: return "Relationship";
		case 4: return "1st Entity";
		case 5: return "1st Cardinality";
		case 6: return "1st Card Nodes";
		case 7: return "2nd Entity";
		case 8: return "2nd Cardinality";
		case 9: return "2nd Card Nodes";
		case 10: return "Source";
		case 11: return "Correctly Derived?";
		
		default: return "UNDEFINED COLUMN NAME";
		}
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		if (columnIndex == 11) {return Boolean.class; }
		else {return String.class; }

	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		if (columnIndex == 10) {return false; }
		else {return true; }
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		DatabaseRelation dbr = _currentSentence.getDatabaseRelations().get(rowIndex);
		
		switch (columnIndex) {
		case 0:  return dbr.getIndentifyingNodeAsVertexIDs();
		case 1:  return dbr.getRelationType();
		case 2:  return dbr.getParentEntityNodeAsVertexIDs();
		case 3:  return dbr.getRelationshipType();
		case 4:  return dbr.getFirstEntityNodeAsVertexIDs();
		case 5:  return dbr.getFirstCardinality();
		case 6:  return dbr.getFirstCardinalityNodeAsVertexIDs();
		case 7:  return dbr.getSecondEntityNodeAsVertexIDs();
		case 8:  return dbr.getSecondCardinality();
		case 9:  return dbr.getSecondCardinalityNodeAsVertexIDs();
		case 10: return dbr.getSource().name();
		case 11: return dbr.isCorrectlyDerived();	

		default: return "UNDEFINED COLUMN";
		}
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		DatabaseRelation dbr = _currentSentence.getDatabaseRelations().get(rowIndex);
		
		if (columnIndex == 1) {
			dbr.setRelationType((DatabaseRelationType)aValue);
			return;
		}
		else if (columnIndex == 3) {
			dbr.setRelationshipType((DatabaseRelationshipType)aValue);
			return;
		}
		else if (columnIndex == 5) {
			dbr.setFirstCardinality( (DatabaseCardinalityType) aValue);
			return;
		}
		else if (columnIndex == 8) {
			dbr.setSecondCardinality( (DatabaseCardinalityType) aValue);
			return;
		}
		else if (columnIndex == 11){
			dbr.setCorrectlyDerived((Boolean) aValue);
			
			return;
		}

		
		//We shouldn't necessarily need the check to enter this block, but just being safe
		if (columnIndex == 0 || columnIndex == 2 || columnIndex == 4 || columnIndex == 6 ||
			columnIndex == 7 || columnIndex == 9) {
			
			List<WordVertex> newList = null;
			try {
				newList = _currentSentence.generateWordVertexListFromString(aValue.toString());
			}
			catch(Exception e) {
				GCController.getTheGCController().setStatusMessage(e.getMessage());
				return;
			}
			switch (columnIndex) {
			case 0: dbr.setIdentifyingNode(newList);       break;
			case 2: dbr.setParentEntityNode(newList);      break;
			case 4: dbr.setFirstEntityNode(newList);       break;
			case 6: dbr.setFirstCardinalityNode(newList);  break;
			case 7: dbr.setSecondEntityNode(newList);      break;
			case 9: dbr.setSecondCardinalityNode(newList); break;
			default: System.err.println("Undefined column passed in AccessControlDialog.setValueAt()"); break;
			}
		}
	}

	
	private ArrayList<TableModelListener> _listeners = new ArrayList<TableModelListener>();
	
	@Override
	public void addTableModelListener(TableModelListener l) {
		_listeners.add(l);
	}
	

	@Override
	public void removeTableModelListener(TableModelListener l) {
		_listeners.remove(l);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == _btnAdd) {
			DatabaseRelation dbr = new DatabaseRelation();
			dbr.setSource(RelationSource.USER);
			this.recordColumnWidths();
			_currentSentence.getDatabaseRelations().add(dbr);
			_jtDatabase.changeSelection(_currentSentence.getNumberOfDatabaseRelations()-1, 0, false, false);
			_jtDatabase.editCellAt(_currentSentence.getNumberOfDatabaseRelations()-1, 0);
			_jtDatabase.transferFocus();
			this.setupComboBoxEditors();
			this.restoreColumnWidths();
		}
		else if (e.getSource() == _btnDelete) {
			int currentRow = _jtDatabase.getSelectedRow();
			if (currentRow != -1) {
				this.recordColumnWidths();
				_currentSentence.getDatabaseRelations().remove(currentRow);
				_jtDatabase.tableChanged(null);
				_jtDatabase.changeSelection(0, 0, false, false);
				this.setupComboBoxEditors();
				this.restoreColumnWidths();
			}
		}
		else if (e.getSource() == _btnMarkNext) {
			_currentSentence.setDatabaseComplete(true,true);
			this.enableButtonsFromDatabaseComplete();
			GCController.getTheGCController().actionPerformed(new ActionEvent(this, 1, "Next"));
		}
		else if (e.getSource() == _btnMark) {
			_currentSentence.setDatabaseComplete(true,true);
			this.enableButtonsFromDatabaseComplete();
		}
		else if (e.getSource() == _btnUnmark) {
			_currentSentence.setDatabaseComplete(false,true);
			this.enableButtonsFromDatabaseComplete();
		}
		
	}
	
	private void recordColumnWidths() {
		TableColumnModel tcm = _jtDatabase.getColumnModel();
		
		for (int i=0;i < _columnWidths.length; i++) {
			TableColumn tc = tcm.getColumn(i);
			_columnWidths[i] = tc.getWidth();
		}
	}
	
	private void restoreColumnWidths() {
		TableColumnModel tcm = _jtDatabase.getColumnModel();
		
		for (int i= _columnWidths.length -1;i >= 0; i--) {
			TableColumn tc = tcm.getColumn(i);
			tc.setWidth(_columnWidths[i]);
			tc.setPreferredWidth(_columnWidths[i]);
		}		
	}	
	
}
