// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Library General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

package org.columba.mail.gui.config.accountwizard;

import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.columba.core.gui.util.MultiLineLabel;
import org.columba.core.gui.util.wizard.DefaultWizardPanel;
import org.columba.core.gui.util.wizard.WizardTextField;
import org.columba.mail.util.MailResourceLoader;

public class IdentityPanel extends DefaultWizardPanel {
	private JLabel nameLabel;
	private JTextField nameTextField;
	private JLabel addressLabel;
	private JTextField addressTextField;

	private JLabel accountnameLabel;
	private JTextField accountnameTextField;

	public IdentityPanel(
		JDialog dialog,
		ActionListener listener,
		String title,
		String description,
		ImageIcon icon) {
		super(dialog, listener, title, description, icon);

		JPanel panel = this;
		panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 20, 30));
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		MultiLineLabel label = new MultiLineLabel(MailResourceLoader.getString("dialog", "accountwizard", "if_you_send_a_message")); //$NON-NLS-1$

		panel.add(label);

		panel.add(Box.createRigidArea(new java.awt.Dimension(0, 40)));

		WizardTextField middlePanel = new WizardTextField();
		JLabel nameLabel = new JLabel(MailResourceLoader.getString("dialog", "accountwizard", "your_name")); //$NON-NLS-1$
		nameLabel.setDisplayedMnemonic(
			MailResourceLoader.getMnemonic(
				"dialog",
				"accountwizard",
				"your_name"));
		middlePanel.addLabel(nameLabel);
		nameTextField = new JTextField("");
		
		//register( nameTextField );
		nameLabel.setLabelFor(nameTextField);
		middlePanel.addTextField(nameTextField);
		JLabel exampleLabel = new JLabel(MailResourceLoader.getString("dialog", "accountwizard", "example__bill_gates")); //$NON-NLS-1$
		middlePanel.addExample(exampleLabel);

		JLabel addressLabel = new JLabel(MailResourceLoader.getString("dialog", "accountwizard", "email_address")); //$NON-NLS-1$
		addressLabel.setDisplayedMnemonic(
			MailResourceLoader.getMnemonic(
				"dialog",
				"accountwizard",
				"email_address"));
		middlePanel.addLabel(addressLabel);
		addressTextField = new JTextField("");
		//register( addressTextField );
		addressLabel.setLabelFor(addressTextField);
		middlePanel.addTextField(addressTextField);
		JLabel addressExampleLabel = new JLabel(MailResourceLoader.getString("dialog", "accountwizard", "example__billgates_address")); //$NON-NLS-1$
		middlePanel.addExample(addressExampleLabel);

		panel.add(middlePanel);
	}

	/*
	public IdentityPanel(
		JDialog dialog,
		ActionListener listener,
		String title,
		String description,
		ImageIcon icon,
		boolean b) {
		super(dialog, listener, title, description, icon);
	}
	*/

	public String getName() {
		return nameTextField.getText();
	}

	public String getAddress() {
		return addressTextField.getText();
	}

	public JTextField getAddressTextField() {
		return addressTextField;
	}

	/*
	protected JPanel createPanel(ActionListener listener) {
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 20, 30));
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
	
		MultiLineLabel label =
			new MultiLineLabel(MailResourceLoader.getString("dialog","accountwizard","if_you_send_a_message")); //$NON-NLS-1$
	
		panel.add(label);
	
		panel.add(Box.createRigidArea(new java.awt.Dimension(0, 40)));
	
		WizardTextField middlePanel = new WizardTextField();
		JLabel nameLabel = new JLabel(MailResourceLoader.getString("dialog","accountwizard","your_name")); //$NON-NLS-1$
		nameLabel.setDisplayedMnemonic(MailResourceLoader.getMnemonic("dialog","accountwizard","your_name") );
		middlePanel.addLabel(nameLabel);
		nameTextField = new JTextField("");
		//register( nameTextField );
		nameLabel.setLabelFor(nameTextField);
		middlePanel.addTextField(nameTextField);
		JLabel exampleLabel = new JLabel(MailResourceLoader.getString("dialog","accountwizard","example__bill_gates")); //$NON-NLS-1$
		middlePanel.addExample(exampleLabel);
	
		JLabel addressLabel = new JLabel(MailResourceLoader.getString("dialog","accountwizard","email_address")); //$NON-NLS-1$
		addressLabel.setDisplayedMnemonic(MailResourceLoader.getMnemonic("dialog","accountwizard","email_address") );
		middlePanel.addLabel(addressLabel);
		addressTextField = new JTextField("");
		//register( addressTextField );
		addressLabel.setLabelFor( addressTextField );
		middlePanel.addTextField(addressTextField);
		JLabel addressExampleLabel = new JLabel(MailResourceLoader.getString("dialog","accountwizard","example__billgates_address")); //$NON-NLS-1$
		middlePanel.addExample(addressExampleLabel);
	
		panel.add(middlePanel);
	
		return panel;
	}
	*/

	/**
	 * @see org.columba.core.gui.util.wizard.DefaultWizardPanel#getFocusComponent()
	 */
	public JComponent getFocusComponent() {
		return nameTextField;
	}

}
