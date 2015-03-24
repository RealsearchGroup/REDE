package edu.ncsu.csc.nl.view;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import edu.ncsu.csc.nl.model.type.SecurityImpact;
import edu.ncsu.csc.nl.model.type.SecurityMitigation;
import edu.ncsu.csc.nl.model.type.SecurityObjective;


public class SecurityObjectiveAnnotationDialog extends JDialog {

	/** */
	private static final long serialVersionUID = 1L;
	
	JButton _btnAdd          = new JButton("Add");		
	JButton _btnDelete       = new JButton("Delete");
	JButton _btnMark         = new JButton("Mark");
	JButton _btnRqmtTemplate = new JButton("Rqmt Template");
	JButton _btnClose        = new JButton("Close");
	
	JTable _jtSecurityOjbectiveAnnotation;
	
	
	private int[] _columnWidths;
	
	public SecurityObjectiveAnnotationDialog(MainFrame mf, SecurityObjectiveAnnotationController controller) {
		super(mf,false);
		
		
		this.setContentPane(this.createControlPane(controller));
		this.establishAccessListeners(controller);
		
		this.setSize(300, 250);
		this.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		
		this.setupComboBoxEditors();
		_columnWidths = new int[controller.getColumnCount()];
	}
	
	public void setupComboBoxEditors() {

		TableColumn columnObjective = _jtSecurityOjbectiveAnnotation.getColumnModel().getColumn(0);
		JComboBox<SecurityObjective> comboBoxSO = new JComboBox<SecurityObjective>();
		for (SecurityObjective so: SecurityObjective.getSelectableList()) {comboBoxSO.addItem(so); }
		columnObjective.setCellEditor(new DefaultCellEditor(comboBoxSO));

		TableColumn columnImpact = _jtSecurityOjbectiveAnnotation.getColumnModel().getColumn(2);
		JComboBox<SecurityImpact> comboBoxImp = new JComboBox<SecurityImpact>();
		for (SecurityImpact si: SecurityImpact.getSelectableList()) {comboBoxImp.addItem(si); }
		columnImpact.setCellEditor(new DefaultCellEditor(comboBoxImp));

		TableColumn columnMitigation = _jtSecurityOjbectiveAnnotation.getColumnModel().getColumn(3);
		JComboBox<SecurityMitigation> comboBoxMitigation = new JComboBox<SecurityMitigation>();
		for (SecurityMitigation sm: SecurityMitigation.getSelectableList()) {comboBoxMitigation.addItem(sm); }
		columnMitigation.setCellEditor(new DefaultCellEditor(comboBoxMitigation));		
		
	}
		
	
	
	private JPanel createControlPane( SecurityObjectiveAnnotationController controller) {
		JPanel panel = new JPanel(new BorderLayout());
		
		JPanel bottomButtonPanel = new JPanel(new GridLayout(1, 5, 0, 0));
		bottomButtonPanel.add(_btnAdd);
		bottomButtonPanel.add(_btnDelete);
		bottomButtonPanel.add(_btnMark);
		bottomButtonPanel.add(_btnRqmtTemplate);
		bottomButtonPanel.add(_btnClose);
		
		_jtSecurityOjbectiveAnnotation = new JTable(controller);
		JScrollPane scrollPane = new JScrollPane(_jtSecurityOjbectiveAnnotation);
		_jtSecurityOjbectiveAnnotation.setFillsViewportHeight(true);	
		_jtSecurityOjbectiveAnnotation.setAutoResizeMode(JTable.AUTO_RESIZE_NEXT_COLUMN);
		new AccessControlTableEditableCellFocusAction(_jtSecurityOjbectiveAnnotation, KeyStroke.getKeyStroke("TAB"));
		
		_jtSecurityOjbectiveAnnotation.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
		
		panel.add(scrollPane,BorderLayout.CENTER);
		panel.add(bottomButtonPanel, BorderLayout.SOUTH);
		
		return panel;
	}
	
	private void establishAccessListeners(SecurityObjectiveAnnotationController controller) {
		_btnAdd.addActionListener(controller);
		_btnDelete.addActionListener(controller);
		_btnMark.addActionListener(controller);
		_btnClose.addActionListener(controller);
		
		_btnAdd.setActionCommand(SecurityObjectiveAnnotationController.ACTION_ADD);
		_btnDelete.setActionCommand(SecurityObjectiveAnnotationController.ACTION_DELETE);
		_btnMark.setActionCommand(SecurityObjectiveAnnotationController.ACTION_COMPLETE);
		_btnClose.setActionCommand(SecurityObjectiveAnnotationController.ACTION_CLOSE);
	}
	
	
	public void tableDataChanged() {
		_jtSecurityOjbectiveAnnotation.tableChanged(null);
	}
	
	public void enableButtonsFromSOADefined(boolean sentenceComplete) {
		if (sentenceComplete) {
			_btnMark.setText("Incomplete");
			_btnMark.setActionCommand(SecurityObjectiveAnnotationController.ACTION_INCOMPLETE);
		}
		else {
			_btnMark.setText("Complete");
			_btnMark.setActionCommand(SecurityObjectiveAnnotationController.ACTION_COMPLETE);
		}
	}
	
	public void changeSelectedRow(int row) {
		_jtSecurityOjbectiveAnnotation.changeSelection(row, 0, false, false);
		_jtSecurityOjbectiveAnnotation.editCellAt(row, 0);
		_jtSecurityOjbectiveAnnotation.transferFocus();
	}
	
	public int getSelectedRow() {
		return _jtSecurityOjbectiveAnnotation.getSelectedRow();
	}
	
	public void prepareForCurrentSentenceChange() {
		if (_jtSecurityOjbectiveAnnotation.isEditing()) {
			TableCellEditor editor = _jtSecurityOjbectiveAnnotation.getCellEditor();
			if (editor != null) {
			  editor.stopCellEditing();
			}
		}
	}
	
	public void performCurrentSentenceChanged(boolean isDefined) {
		this.recordColumnWidths();
		this.tableDataChanged();
		this.enableButtonsFromSOADefined(isDefined);
		this.setupComboBoxEditors();
		this.restoreColumnWidths();
	}
	
	public void rowDeleted() {
		this.recordColumnWidths();
		this.tableDataChanged();
		this.changeSelectedRow(0);
		this.setupComboBoxEditors();
		this.restoreColumnWidths();		
	}
	
	private void recordColumnWidths() {
		TableColumnModel tcm = _jtSecurityOjbectiveAnnotation.getColumnModel();
		
		for (int i=0;i < _columnWidths.length; i++) {
			TableColumn tc = tcm.getColumn(i);
			_columnWidths[i] = tc.getWidth();
		}
	}
	
	private void restoreColumnWidths() {
		TableColumnModel tcm = _jtSecurityOjbectiveAnnotation.getColumnModel();
		
		for (int i= _columnWidths.length -1;i >= 0; i--) {
			TableColumn tc = tcm.getColumn(i);
			tc.setWidth(_columnWidths[i]);
			tc.setPreferredWidth(_columnWidths[i]);
		}		
	}
}
