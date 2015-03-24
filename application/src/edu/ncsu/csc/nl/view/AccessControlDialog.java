package edu.ncsu.csc.nl.view;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

import edu.ncsu.csc.nl.GCController;
import edu.ncsu.csc.nl.model.Sentence;
import edu.ncsu.csc.nl.model.WordVertex;
import edu.ncsu.csc.nl.model.relation.AccessControlRelation;
import edu.ncsu.csc.nl.model.type.RelationSource;

public class AccessControlDialog extends JDialog implements TableModel, ActionListener {

	/** */
	private static final long serialVersionUID = 1L;
	
	JButton _btnAdd      = new JButton("Add");		
	JButton _btnDelete   = new JButton("Delete");
	JButton _btnMarkNext = new JButton("Mark/Next");
	JButton _btnMark     = new JButton("Mark");
	JButton _btnUnmark   = new JButton("Unmark");
	
	JTable _jtAccessControl;
	
	Sentence _currentSentence = new Sentence();
	
	public AccessControlDialog(MainFrame mf) {
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
		
		_jtAccessControl = new JTable(this);
		JScrollPane scrollPane = new JScrollPane(_jtAccessControl);
		_jtAccessControl.setFillsViewportHeight(true);	
		_jtAccessControl.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
		new AccessControlTableEditableCellFocusAction(_jtAccessControl, KeyStroke.getKeyStroke("TAB"));
		
		panel.add(scrollPane,BorderLayout.CENTER);
		panel.add(bottomButtonPanel, BorderLayout.SOUTH);
		
		return panel;
	}
	
	private void establishAccessListeners() {
		_btnAdd.addActionListener(this);
		_btnDelete.addActionListener(this);
		_btnMarkNext.addActionListener(this);
		_btnMark.addActionListener(this);
		_btnUnmark.addActionListener(this);
	}
	
	
	public void setCurrentSentence(Sentence s) {
		_currentSentence = s;
		_jtAccessControl.tableChanged(null);
		this.enableButtonsFromAccessDefined();
	}

	private void enableButtonsFromAccessDefined() {
		if (_currentSentence.isAccessControlDefined()) {
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
		return _currentSentence.getNumberOfAccessControlRelations();
	}

	@Override
	public int getColumnCount() {
		return 8;
	}

	@Override
	public String getColumnName(int columnIndex) {
		switch (columnIndex) {
		case 0: return "Subject";
		case 1: return "Action";
		case 2: return "Object";
		case 3: return "Permissions";
		case 4: return "Negative";
		case 5: return "LimitToSubject";
		case 6: return "Source";
		case 7: return "Correctly Derived?";
		default: return "UNDEFINED COLUMN NAME";
		}
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		if (columnIndex == 7) {return Boolean.class; }
		else {return String.class; }

	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		if (columnIndex == 6) {return false; }
		else {return true; }
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		AccessControlRelation ac = _currentSentence.getAccessControlRelations().get(rowIndex);
		
		switch (columnIndex) {
		case 0: return ac.getSubjectAsVertexIDs();
		case 1: return ac.getActionAsVertexIDs();
		case 2: return ac.getObjectAsVertexIDs();
		case 3: return ac.getPermissions();
		case 4: return ac.getNegativeVertexAsID();
		case 5: return ac.getLimitToSubjectVertexAsID();	
		case 6: return ac.getSource().name();
		case 7: return ac.isCorrectlyDerived();	

		default: return "UNDEFINED COLUMN";
		}
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		// TODO Auto-generated method stub
		AccessControlRelation ac = _currentSentence.getAccessControlRelations().get(rowIndex);
		
		if (columnIndex == 3) {
			String permissions = aValue.toString();
			try {
				ac.setPermissions(permissions);
			}
			catch (Exception e) {
				GCController.getTheGCController().setStatusMessage(e.getMessage());
			}
			return;
		}
		
		if (columnIndex == 7){
			ac.setCorrectlyDerived((Boolean) aValue);
			
			return;
		}
		
		List<WordVertex> newList = null;
		try {
			newList = _currentSentence.generateWordVertexListFromString(aValue.toString());
		}
		catch(Exception e) {
			GCController.getTheGCController().setStatusMessage(e.getMessage());
			return;
		}
		switch (columnIndex) {
		case 0: ac.setSubjectVertexList(newList); break;
		case 1: ac.setActionVertexList(newList); break;
		case 2: ac.setObjectVertexList(newList); break;
		case 4: if (newList.size() >1) {
					GCController.getTheGCController().setStatusMessage("Negative permissions only take a single vertex");
					return;
				}
				else if(newList.size() ==0) {
					ac.setNegativeVertex(null);
				}
				else {
					ac.setNegativeVertex(newList.get(0));
				}
				break;
		case 5: if (newList.size() >1) {
					GCController.getTheGCController().setStatusMessage("Limit to subject only take a single vertex");
					return;
				}
				else if(newList.size() ==0) {
					ac.setLimitToSubjectVertex(null);
				}
				else {
					ac.setLimitToSubjectVertex(newList.get(0));
				}
				break;
		default: System.err.println("Undefined column passed in AccessControlDialog.setValueAt()"); break;
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
			AccessControlRelation ac = new AccessControlRelation();
			ac.setSource(RelationSource.USER);
			_currentSentence.getAccessControlRelations().add(ac);
			_jtAccessControl.changeSelection(_currentSentence.getNumberOfAccessControlRelations()-1, 0, false, false);
			_jtAccessControl.editCellAt(_currentSentence.getNumberOfAccessControlRelations()-1, 0);
			_jtAccessControl.transferFocus();
		}
		else if (e.getSource() == _btnDelete) {
			int currentRow = _jtAccessControl.getSelectedRow();
			if (currentRow != -1) {
				_currentSentence.getAccessControlRelations().remove(currentRow);
				_jtAccessControl.tableChanged(null);
				_jtAccessControl.changeSelection(0, 0, false, false);
			}
		}
		else if (e.getSource() == _btnMarkNext) {
			_currentSentence.setAccessControlDefined(true,true);
			this.enableButtonsFromAccessDefined();
			GCController.getTheGCController().actionPerformed(new ActionEvent(this, 1, "Next"));
		}
		else if (e.getSource() == _btnMark) {
			_currentSentence.setAccessControlDefined(true,true);
			this.enableButtonsFromAccessDefined();
		}
		else if (e.getSource() == _btnUnmark) {
			_currentSentence.setAccessControlDefined(false,true);
			this.enableButtonsFromAccessDefined();
		}
		
	}
}
