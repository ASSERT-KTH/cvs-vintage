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
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.columba.core.gui.util.MultiLineLabel;
import org.columba.core.gui.util.wizard.DefaultWizardPanel;
import org.columba.core.gui.util.wizard.WizardTextField;
import org.columba.mail.util.MailResourceLoader;
/**
 * @version 	1.0
 * @author
 */
public class AdvancedPanel extends DefaultWizardPanel {

	private JLabel nameLabel;
	private JTextField nameTextField;

	public AdvancedPanel(
		JDialog dialog,
		ActionListener listener,
		String title,
		String description,
		ImageIcon icon) {
		super(dialog, listener, title, description, icon);

		JPanel panel = this;
		panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 20, 30));
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		MultiLineLabel label = new MultiLineLabel(MailResourceLoader.getString("dialog", "accountwizard", "wizard_enter_the_name_by_which_you_like_to_refer_to_this_account")); //$NON-NLS-1$

		panel.add(label);

		//panel.add(Box.createRigidArea(new java.awt.Dimension(0, 40)));

		WizardTextField middlePanel = new WizardTextField();

		JLabel nameLabel = new JLabel(MailResourceLoader.getString("dialog", "accountwizard", "account_name")); //$NON-NLS-1$
		nameLabel.setDisplayedMnemonic(
			MailResourceLoader.getMnemonic(
				"dialog",
				"accountwizard",
				"account_name"));
		middlePanel.addLabel(nameLabel);
		nameTextField = new JTextField("");
		nameTextField.requestFocusInWindow();
		nameLabel.setLabelFor(nameTextField);
		//register(nameTextField);
		middlePanel.addTextField(nameTextField);
		JLabel nameExampleLabel = new JLabel(MailResourceLoader.getString("dialog", "accountwizard", "example__bill__s_private_mail")); //$NON-NLS-1$
		middlePanel.addExample(nameExampleLabel);

		panel.add(middlePanel);

		MultiLineLabel label2 = new MultiLineLabel(MailResourceLoader.getString("dialog", "accountwizard", "if_you_want_to,_you_can_directly_edit_your_advanced_account_preferences_now")); //$NON-NLS-1$

		panel.add(label2);

		JButton button = new JButton(MailResourceLoader.getString("dialog", "accountwizard", "advanced_preferences")); //$NON-NLS-1$
		button.setMnemonic(MailResourceLoader.getMnemonic("dialog", "accountwizard", "advanced_preferences")); //$NON-NLS-1$
		button.setActionCommand("ACCOUNT");
		button.addActionListener(listener);
		button.setAlignmentX(1);
		panel.add(button);
	}

	protected JTextField getAddressTextField() {
		/*
		IdentityPanel p = (IdentityPanel) prevPanel.prevPanel.prevPanel;
		
		JTextField address = p.getAddressTextField();
		
		return address;
		*/
		return null;
	}

	public void select() {
		nameTextField.setCaretPosition(nameTextField.getText().length());
		nameTextField.selectAll();
	}

	public String getAccountName() {
		return nameTextField.getText();
	}

	public void setPrev(DefaultWizardPanel panel) {
		/*
		prevPanel = panel;
		
		getAddressTextField().getDocument().addDocumentListener(new DocumentListener()
		{
			public void insertUpdate(DocumentEvent e)
			{
				update();
			}
		
			public void removeUpdate(DocumentEvent e)
			{
				update();
			}
		
			public void changedUpdate(DocumentEvent e)
			{
				update();
				//Plain text components don't fire these events
			}
			public void update()
			{
		
				nameTextField.setText(getAddressTextField().getText());
			}
		});
		*/
	}

	/*
	protected JPanel createPanel(ActionListener listener)
	{
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 20, 30));
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
	
		MultiLineLabel label =
			new MultiLineLabel(MailResourceLoader.getString("dialog","accountwizard","wizard_enter_the_name_by_which_you_like_to_refer_to_this_account")); //$NON-NLS-1$
	
		panel.add(label);
	
		//panel.add(Box.createRigidArea(new java.awt.Dimension(0, 40)));
	
		WizardTextField middlePanel = new WizardTextField();
	
		JLabel nameLabel = new JLabel(MailResourceLoader.getString("dialog","accountwizard","account_name")); //$NON-NLS-1$
		nameLabel.setDisplayedMnemonic( MailResourceLoader.getMnemonic("dialog","accountwizard","account_name"));
		middlePanel.addLabel(nameLabel);
		nameTextField = new JTextField("");
		nameLabel.setLabelFor(nameTextField);
		//register(nameTextField);
		middlePanel.addTextField(nameTextField);
		JLabel nameExampleLabel = new JLabel(MailResourceLoader.getString("dialog","accountwizard","example__bill__s_private_mail")); //$NON-NLS-1$
		middlePanel.addExample(nameExampleLabel);
	
		panel.add(middlePanel);
		
		MultiLineLabel label2 =
			new MultiLineLabel(MailResourceLoader.getString("dialog","accountwizard","if_you_want_to,_you_can_directly_edit_your_advanced_account_preferences_now")); //$NON-NLS-1$
			
		panel.add( label2 );
		
		JButton button = new JButton(MailResourceLoader.getString("dialog","accountwizard","advanced_preferences")); //$NON-NLS-1$
		button.setMnemonic(MailResourceLoader.getMnemonic("dialog","accountwizard","advanced_preferences")); //$NON-NLS-1$
		button.setActionCommand("ACCOUNT");
		button.addActionListener( listener );
		button.setAlignmentX(1);
		panel.add( button );
			
	
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
