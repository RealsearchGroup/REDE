package edu.ncsu.csc.nl.view;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.factories.FormFactory;

import edu.ncsu.csc.nl.GCConstants;
import edu.ncsu.csc.nl.GCController;

import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.ButtonGroup;

import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class OptionsDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	
	
	private final JPanel contentPanel = new JPanel();
	private final ButtonGroup btPrimaryViewGroup = new ButtonGroup();

	private JRadioButton rdbtnClassifications = new JRadioButton("Classifications");
	private JRadioButton rdbtnAnnotations = new JRadioButton("Annotations");
	/**
	 * Create the dialog.
	 */
	public OptionsDialog() {
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new FormLayout(new ColumnSpec[] {
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,
				FormFactory.RELATED_GAP_COLSPEC,
				FormFactory.DEFAULT_COLSPEC,},
			new RowSpec[] {
				FormFactory.RELATED_GAP_ROWSPEC,
				FormFactory.DEFAULT_ROWSPEC,}));
		{
			JLabel lblNewLabel = new JLabel("Primary View:");
			lblNewLabel.setFont(new Font("Tahoma", Font.BOLD, 11));
			contentPanel.add(lblNewLabel, "2, 2");
		}
		{
			
			rdbtnAnnotations.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					GCController.getTheGCController().setCurrentView(GCConstants.VIEW_ANNOTATIONS);
				}
			});
			btPrimaryViewGroup.add(rdbtnAnnotations);
			contentPanel.add(rdbtnAnnotations, "6, 2");
		}
		{
			
			rdbtnClassifications.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					GCController.getTheGCController().setCurrentView(GCConstants.VIEW_CLASSIFICATIONS);
				}
			});
			btPrimaryViewGroup.add(rdbtnClassifications);
			contentPanel.add(rdbtnClassifications, "10, 2");
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						setVisible(false);
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
		this.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
	}

	@Override
	public void setVisible(boolean flag) {
		if (flag) {
			if (GCController.getTheGCController().getCurrentView() == GCConstants.VIEW_ANNOTATIONS) {
				rdbtnAnnotations.setSelected(true);
			}
			else {
				rdbtnClassifications.setSelected(true);
			}
		}
		super.setVisible(flag);
	}
}
