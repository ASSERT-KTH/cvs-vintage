// The contents of this file are subject to the Mozilla Public License Version
// 1.1
//(the "License"); you may not use this file except in compliance with the
//License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
//for the specific language governing rights and
//limitations under the License.
//
//The Original Code is "The Columba Project"
//
//The Initial Developers of the Original Code are Frederik Dietz and Timo
// Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.

package org.columba.addressbook.gui.dialog.contact;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;

import org.columba.addressbook.util.AddressbookResourceLoader;
import org.columba.core.gui.base.ButtonWithMnemonic;
import org.columba.core.gui.base.LabelWithMnemonic;
import org.columba.core.gui.base.SingleSideEtchedBorder;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * A dialog for editing a contact's full name.
 */

public class FullNameDialog extends JDialog implements ActionListener {
	private JLabel titleLabel;

	private JTextField namePrefixTextField;

	private JLabel fornameLabel;

	private JTextField firstNameTextField;

	private JLabel middlenameLabel;

	private JTextField middleNameTextField;

	private JLabel lastnameLabel;

	private JTextField lastNameTextField;

	private JLabel suffixLabel;

	private JTextField nameSuffixTextField;

	private JButton okButton;

	private JButton cancelButton;

	private boolean success = false;

	public FullNameDialog(JDialog frame) {
		super(frame, AddressbookResourceLoader.getString("dialog", "contact",
				"edit_fullname"), true);

		initComponents();
		layoutComponents();

		pack();
		setLocationRelativeTo(null);
	}

	public boolean getResult() {
		return success;
	}

	/**
	 * @return Returns the firstNameTextField.
	 */
	public JTextField getFirstNameTextField() {
		return firstNameTextField;
	}

	/**
	 * @return Returns the lastNameTextField.
	 */
	public JTextField getLastNameTextField() {
		return lastNameTextField;
	}

	/**
	 * @return Returns the middleNameTextField.
	 */
	public JTextField getMiddleNameTextField() {
		return middleNameTextField;
	}

	/**
	 * @return Returns the namePrefixTextField.
	 */
	public JTextField getNamePrefixTextField() {
		return namePrefixTextField;
	}

	/**
	 * @return Returns the nameSuffixTextField.
	 */
	public JTextField getNameSuffixTextField() {
		return nameSuffixTextField;
	}

	protected void layoutComponents() {
		JPanel mainPanel = new JPanel();
		mainPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

		FormLayout layout = new FormLayout("right:default, 3dlu, default:grow",
				"");

		DefaultFormBuilder b = new DefaultFormBuilder(layout, mainPanel);
		b.setRowGroupingEnabled(true);

		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(mainPanel, BorderLayout.CENTER);

		b.append(titleLabel);
		b.append(namePrefixTextField);

		b.append(fornameLabel);
		b.append(firstNameTextField);

		b.append(middlenameLabel);
		b.append(middleNameTextField);

		b.append(lastnameLabel);
		b.append(lastNameTextField);

		b.append(suffixLabel);
		b.append(nameSuffixTextField);

		JPanel bottomPanel = new JPanel(new BorderLayout());
		bottomPanel.setBorder(new SingleSideEtchedBorder(SwingConstants.TOP));

		JPanel buttonPanel = new JPanel(new GridLayout(1, 2, 10, 0));
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
		bottomPanel.add(buttonPanel, BorderLayout.EAST);

		cancelButton = new ButtonWithMnemonic(AddressbookResourceLoader
				.getString(null, "cancel"));
		cancelButton.setActionCommand("CANCEL");
		cancelButton.addActionListener(this);
		buttonPanel.add(cancelButton);
		okButton = new ButtonWithMnemonic(AddressbookResourceLoader.getString(
				null, "ok"));
		okButton.setActionCommand("OK");
		okButton.addActionListener(this);
		buttonPanel.add(okButton);

		getRootPane().registerKeyboardAction(this, "CANCEL",
				KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);

		getRootPane().setDefaultButton(okButton);
		getContentPane().add(bottomPanel, BorderLayout.SOUTH);
	}

	protected void initComponents() {
		titleLabel = new LabelWithMnemonic(AddressbookResourceLoader.getString(
				"dialog", "contact", "title"));
		namePrefixTextField = new JTextField(20);
		titleLabel.setLabelFor(namePrefixTextField);
		fornameLabel = new LabelWithMnemonic(AddressbookResourceLoader
				.getString("dialog", "contact", "first_name"));
		firstNameTextField = new JTextField(20);
		fornameLabel.setLabelFor(firstNameTextField);
		middlenameLabel = new LabelWithMnemonic(AddressbookResourceLoader
				.getString("dialog", "contact", "middle_name"));
		middleNameTextField = new JTextField(20);
		middlenameLabel.setLabelFor(middleNameTextField);
		lastnameLabel = new LabelWithMnemonic(AddressbookResourceLoader
				.getString("dialog", "contact", "last_name"));
		lastNameTextField = new JTextField(20);
		lastnameLabel.setLabelFor(lastNameTextField);
		suffixLabel = new LabelWithMnemonic(AddressbookResourceLoader
				.getString("dialog", "contact", "suffix"));
		nameSuffixTextField = new JTextField(20);
		suffixLabel.setLabelFor(nameSuffixTextField);
	}

	public void actionPerformed(ActionEvent event) {
		if (event.getActionCommand().equals("OK")) {

			// check if firstname, middlename or lastname contain only valid
			// characters

			if (firstNameTextField.getText() != null) {
				// contains "," comma character
				if (firstNameTextField.getText().indexOf(",") != -1) {
					JOptionPane.showInternalMessageDialog(this,
							"Firstname can't contain comma character.");
					return;
				}
			}
			
			if (middleNameTextField.getText() != null) {
				// contains "," comma character
				if (middleNameTextField.getText().indexOf(",") != -1) {
					JOptionPane.showInternalMessageDialog(this,
							"Middle name can't contain comma character.");
					return;
				}
			}
			
			if (lastNameTextField.getText() != null) {
				// contains "," comma character
				if (lastNameTextField.getText().indexOf(",") != -1) {
					JOptionPane.showInternalMessageDialog(this,
							"Lastname can't contain comma character.");
					return;
				}
			}

			success = true;
			setVisible(false);
		} else if (event.getActionCommand().equals("CANCEL")) {
			success = false;
			setVisible(false);
		}

	}

	public String getFormattedName() {
		StringBuffer buf = new StringBuffer();

		if (namePrefixTextField.getText().length() > 0) {
			buf.append(namePrefixTextField.getText() + " ");
		}

		if (firstNameTextField.getText().length() > 0) {
			buf.append(firstNameTextField.getText() + " ");
		}

		if (middleNameTextField.getText().length() > 0) {
			buf.append(middleNameTextField.getText() + " ");
		}

		if (lastNameTextField.getText().length() > 0) {
			buf.append(lastNameTextField.getText() + " ");
		}

		if (nameSuffixTextField.getText().length() > 0) {
			buf.append(nameSuffixTextField.getText() + " ");
		}

		return buf.toString();
	}
}
