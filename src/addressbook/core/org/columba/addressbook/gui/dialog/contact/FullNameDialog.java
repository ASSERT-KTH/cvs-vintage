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
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;

import net.javaprog.ui.wizard.plaf.basic.SingleSideEtchedBorder;

import org.columba.addressbook.model.Contact;
import org.columba.addressbook.model.VCARD;
import org.columba.addressbook.util.AddressbookResourceLoader;
import org.columba.core.gui.util.DefaultFormBuilder;

import com.jgoodies.forms.layout.FormLayout;

public class FullNameDialog extends JDialog implements ActionListener {
	private JLabel titleLabel;
	private JTextField titleTextField;
	private JLabel fornameLabel;
	private JTextField fornameTextField;
	private JLabel middlenameLabel;
	private JTextField middlenameTextField;
	private JLabel lastnameLabel;
	private JTextField lastnameTextField;
	private JLabel suffixLabel;
	private JTextField suffixTextField;
	private JButton okButton;
	private JButton changeButton;
	private IdentityPanel identityPanel;

	private Contact contact;

	public FullNameDialog(JDialog frame, IdentityPanel identityPanel,
			Contact contact) {
		super(frame, true);

		this.identityPanel = identityPanel;
		this.contact = contact;

		initComponents();

		layoutComponents();

		pack();
		setLocationRelativeTo(null);
	}

	public void updateComponents( boolean b) {
		if (b) {
			titleTextField.setText(contact.get(VCARD.N, VCARD.N_PREFIX)); //$NON-NLS-1$ //$NON-NLS-2$
			lastnameTextField.setText(contact.get(VCARD.N, VCARD.N_FAMILY)); //$NON-NLS-1$ //$NON-NLS-2$
			fornameTextField.setText(contact.get(VCARD.N, VCARD.N_GIVEN)); //$NON-NLS-1$ //$NON-NLS-2$
			middlenameTextField.setText(contact.get(VCARD.N, VCARD.N_MIDDLE)); //$NON-NLS-1$ //$NON-NLS-2$
			suffixTextField.setText(contact.get(VCARD.N, VCARD.N_SUFFIX)); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			contact.set(VCARD.N, VCARD.N_PREFIX, titleTextField.getText()); //$NON-NLS-1$ //$NON-NLS-2$
			contact.set(VCARD.N, VCARD.N_FAMILY, lastnameTextField.getText()); //$NON-NLS-1$ //$NON-NLS-2$
			contact.set(VCARD.N, VCARD.N_GIVEN, fornameTextField.getText()); //$NON-NLS-1$ //$NON-NLS-2$
			contact.set(VCARD.N, VCARD.N_MIDDLE, middlenameTextField.getText()); //$NON-NLS-1$ //$NON-NLS-2$
			contact.set(VCARD.N, VCARD.N_SUFFIX, suffixTextField.getText()); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}

	protected void layoutComponents() {
		JPanel mainPanel = new JPanel();
		mainPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

		FormLayout layout = new FormLayout("right:default, 3dlu, default:grow",
				"");

		DefaultFormBuilder b = new DefaultFormBuilder(mainPanel, layout);
		b.setRowGroupingEnabled(true);

		getContentPane().setLayout(new BorderLayout());
		getContentPane().add(mainPanel, BorderLayout.CENTER);

		b.append(titleLabel);
		b.append(titleTextField);

		b.append(fornameLabel);
		b.append(fornameTextField);

		b.append(middlenameLabel);
		b.append(middlenameTextField);

		b.append(lastnameLabel);
		b.append(lastnameTextField);

		b.append(suffixLabel);
		b.append(suffixTextField);

		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BorderLayout());
		bottomPanel.setBorder(new SingleSideEtchedBorder(SwingConstants.TOP));

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(0, 2, 10, 0));
		buttonPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
		bottomPanel.add(buttonPanel, BorderLayout.EAST);

		changeButton = new JButton(AddressbookResourceLoader.getString(
				"dialog", "contact", "change_formatted_name")); //$NON-NLS-1$
		changeButton.setActionCommand("CHANGE"); //$NON-NLS-1$
		changeButton.addActionListener(this);
		buttonPanel.add(changeButton);
		okButton = new JButton("Close"); //$NON-NLS-1$
		okButton.setActionCommand("OK"); //$NON-NLS-1$
		okButton.addActionListener(this);
		buttonPanel.add(okButton);

		getRootPane().registerKeyboardAction(this, "OK", //$NON-NLS-1$
				KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
				JComponent.WHEN_IN_FOCUSED_WINDOW);

		getRootPane().setDefaultButton(okButton);

		getContentPane().add(bottomPanel, BorderLayout.SOUTH);
	}

	protected void initComponents() {

		titleLabel = new JLabel(AddressbookResourceLoader.getString("dialog",
				"contact", "title")); //$NON-NLS-1$
		titleTextField = new JTextField(20);

		fornameLabel = new JLabel(AddressbookResourceLoader.getString(
				"dialog", "contact", "first_name")); //$NON-NLS-1$
		fornameTextField = new JTextField(20);

		middlenameLabel = new JLabel(AddressbookResourceLoader.getString(
				"dialog", "contact", "middle_name")); //$NON-NLS-1$
		middlenameTextField = new JTextField(20);

		lastnameLabel = new JLabel(AddressbookResourceLoader.getString(
				"dialog", "contact", "last_name")); //$NON-NLS-1$
		lastnameTextField = new JTextField(20);

		suffixLabel = new JLabel(AddressbookResourceLoader.getString("dialog",
				"contact", "suffix")); //$NON-NLS-1$
		suffixTextField = new JTextField(20);

	}

	public void actionPerformed(ActionEvent event) {
		String action = event.getActionCommand();

		if (action.equals("OK")) { //$NON-NLS-1$
			setVisible(false);

			if (identityPanel.fnIsEmpty() == true) {
				setFormattedName();
			}
		} else if (action.equals("CHANGE")) { //$NON-NLS-1$
			setFormattedName();
		}
	}

	protected void setFormattedName() {
		StringBuffer buf = new StringBuffer();

		if (titleTextField.getText().length() > 0) {
			buf.append(titleTextField.getText() + " "); //$NON-NLS-1$
		}

		if (fornameTextField.getText().length() > 0) {
			buf.append(fornameTextField.getText() + " "); //$NON-NLS-1$
		}

		if (middlenameTextField.getText().length() > 0) {
			buf.append(middlenameTextField.getText() + " "); //$NON-NLS-1$
		}

		if (lastnameTextField.getText().length() > 0) {
			buf.append(lastnameTextField.getText() + " "); //$NON-NLS-1$
		}

		if (suffixTextField.getText().length() > 0) {
			buf.append(suffixTextField.getText() + " "); //$NON-NLS-1$
		}

		identityPanel.setFn(buf.toString());
	}
}