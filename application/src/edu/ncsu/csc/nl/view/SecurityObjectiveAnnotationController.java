package edu.ncsu.csc.nl.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableModel;

import edu.ncsu.csc.nl.GCController;
import edu.ncsu.csc.nl.event.NLPEventManager;
import edu.ncsu.csc.nl.event.NLPEventSentenceDataEvent;
import edu.ncsu.csc.nl.event.NLPEventType;
import edu.ncsu.csc.nl.model.SecurityObjectiveAnnotation;
import edu.ncsu.csc.nl.model.Sentence;
import edu.ncsu.csc.nl.model.type.SecurityImpact;
import edu.ncsu.csc.nl.model.type.SecurityMitigation;
import edu.ncsu.csc.nl.model.type.SecurityObjective;


public class SecurityObjectiveAnnotationController implements TableModel , ActionListener {

	public static final String ACTION_ADD = "add";
	public static final String ACTION_DELETE = "delete";
	public static final String ACTION_CLOSE  = "close";
	public static final String ACTION_COMPLETE = "complete";
	public static final String ACTION_INCOMPLETE = "incomplete";
	
	/** */
	private SecurityObjectiveAnnotationDialog _soaDialog;;
	
	
	Sentence _currentSentence = new Sentence();
	
	public SecurityObjectiveAnnotationController(MainFrame mf) {
		_soaDialog = new SecurityObjectiveAnnotationDialog(mf, this);
		
	}
	
	public void setVisible(boolean visibility) {
		_soaDialog.setVisible(visibility);
	}
	
	public boolean isVisible() {
		return _soaDialog.isVisible();
	}
		
	public void setCurrentSentence(Sentence s) { 
		if (s == _currentSentence) {
			for (TableModelListener tml: _listeners ) {	
				tml.tableChanged(new TableModelEvent(this));			
			}
		}
		
		_soaDialog.prepareForCurrentSentenceChange();
		
		if (_currentSentence != null) {
			_currentSentence.removeEmptySecurityObjectiveRows();
		}	
		
		
		_currentSentence = s;
		_soaDialog.performCurrentSentenceChanged(s.isSecurityObjectiveAnnotationsDefined());
		if (!s.isSecurityObjectiveAnnotationsDefined()) {
			this.performAddAction();
		}
	}

	
	@Override
	public int getRowCount() {
		return _currentSentence.getNumberOfSecurityObjectiveAnnotations();
	}

	@Override
	public int getColumnCount() {
		return 5;
	}

	@Override
	public String getColumnName(int columnIndex) {
		switch (columnIndex) {
		case 0: return "Objective";
		case 1: return "Implied";
		case 2: return "Impact";
		case 3: return "Mitigation";
		case 4: return "Indicative Phrases";
		default: return "UNDEFINED COLUMN NAME";
		}
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
		case 0: return SecurityObjective.class;
		case 1: return Boolean.class;
		case 2: return SecurityImpact.class;
		case 3: return SecurityMitigation.class;
		case 4: return String.class;
		default: return Object.class;
		}
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return true;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		SecurityObjectiveAnnotation soa = _currentSentence.getSecurityObjectiveAnnotations().get(rowIndex);
		
		switch (columnIndex) {
		case 0: return soa.getSecurityObjective();
		case 1: return soa.isImplied();
		case 2: return soa.getSecurityImpact();
		case 3: return soa.getSecurityMitigation();
		case 4: return soa.getIndicativePhrase();

		default: return "UNDEFINED COLUMN";
		}
	}

	@Override
	public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
		if (aValue == null ) { return; } // don't allow null to be set

		SecurityObjectiveAnnotation soa = _currentSentence.getSecurityObjectiveAnnotations().get(rowIndex);
		
		
		switch (columnIndex) {
		case 0: soa.setSecurityObjective( (SecurityObjective) aValue ); break;
		case 1: soa.setImplied((Boolean) aValue); break;
		case 2: soa.setSecurityImpact( (SecurityImpact) aValue); break;
		case 3: soa.setSecurityMitigation( (SecurityMitigation) aValue);  break;
		case 4: soa.setIndicativePhrase(aValue.toString()); break;

		default: throw new  IllegalArgumentException("column index out of bounds in SecurityObjectiveAnnotationController:"+columnIndex);
		}
		NLPEventManager.getTheEventManager().sendEvent(NLPEventType.SENTENCE_DATA_CHANGE, new NLPEventSentenceDataEvent(null, "annotation"));

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
		switch (e.getActionCommand()) {
		case ACTION_ADD:    performAddAction();
					        break;
		case ACTION_DELETE: int currentRow = _soaDialog.getSelectedRow();
							if (currentRow != -1 && _currentSentence.getSecurityObjectiveAnnotations().size() > currentRow) {
								_currentSentence.getSecurityObjectiveAnnotations().remove(currentRow);
								_soaDialog.rowDeleted();
								NLPEventManager.getTheEventManager().sendEvent(NLPEventType.SENTENCE_DATA_CHANGE, new NLPEventSentenceDataEvent(null, "annotation"));
								GCController.getTheGCController().setStatusMessage("");
							}
							else {
								GCController.getTheGCController().setStatusMessage("No row selected.");
							}
							break;
		case ACTION_COMPLETE: if (_currentSentence != null) {
								  _currentSentence.removeEmptySecurityObjectiveRows();
							  }
							  String soaErrors = _currentSentence.validateSecurityObjectiveAnnotations();
		                      if (soaErrors.equals("")) {
		                    	  _currentSentence.processSentenceForSecurityObjectAnnotationsDefined();
		                          _soaDialog.enableButtonsFromSOADefined(true);
		                          GCController.getTheGCController().setStatusMessage("Sentence marked complete.");
		                          GCController.getTheGCController().gotoNextSOAUndefinedInDocument();
		                      }
		                      else {
		                    	  GCController.getTheGCController().setStatusMessage(soaErrors);
		                      }
		                      break;
		case ACTION_INCOMPLETE: _currentSentence.setSecurityObjectiveAnnotationsDefined(false);
		                        _soaDialog.enableButtonsFromSOADefined(false);
		                        GCController.getTheGCController().setStatusMessage("Sentence marked incomplete.");
		                        break;
		case ACTION_CLOSE: _soaDialog.setVisible(false);
		                   break;
		default: throw new  IllegalArgumentException("invalid action in securityObjectiveAnnotationController:"+e.getActionCommand());
		}
	}
	
	
	private void performAddAction() {
		SecurityObjectiveAnnotation soa = new SecurityObjectiveAnnotation();
        _currentSentence.getSecurityObjectiveAnnotations().add(soa);
        _soaDialog.changeSelectedRow(_currentSentence.getNumberOfSecurityObjectiveAnnotations()-1);
        GCController.getTheGCController().setStatusMessage("");
	}

}
