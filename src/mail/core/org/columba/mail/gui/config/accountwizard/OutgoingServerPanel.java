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

public class OutgoingServerPanel extends DefaultWizardPanel {
	private JLabel hostLabel;
	private JTextField hostTextField;

	public OutgoingServerPanel(
		JDialog dialog,
		ActionListener listener,
		String title,
		String description,
		ImageIcon icon) {
		super(dialog, listener, title, description, icon);
	}

	public OutgoingServerPanel(
		JDialog dialog,
		ActionListener listener,
		String title,
		String description,
		ImageIcon icon,
		boolean b) {
		super(dialog, listener, title, description, icon);
		
		JPanel panel = this;
			panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
			panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 20, 30));

			MultiLineLabel label = new MultiLineLabel(MailResourceLoader.getString("dialog", "accountwizard", "please_specify_your_outgoing_mail_server_properties")); //$NON-NLS-1$

			panel.add(label);

			panel.add(Box.createRigidArea(new java.awt.Dimension(0, 40)));

			WizardTextField middlePanel = new WizardTextField();

			JLabel addressLabel = new JLabel(MailResourceLoader.getString("dialog", "accountwizard", "host_smtp_server")); //$NON-NLS-1$
			addressLabel.setDisplayedMnemonic(
				MailResourceLoader.getMnemonic(
					"dialog",
					"accountwizard",
					"host_smtp_server"));
			middlePanel.addLabel(addressLabel);
			hostTextField = new JTextField("");
			hostTextField.requestFocusInWindow();
			addressLabel.setLabelFor(hostTextField);
			//register(hostTextField);
			middlePanel.addTextField(hostTextField);
			JLabel addressExampleLabel = new JLabel(MailResourceLoader.getString("dialog", "accountwizard", "example__mail.microsoft.com")); //$NON-NLS-1$
			middlePanel.addExample(addressExampleLabel);

			panel.add(middlePanel);
	}

	public String getHost() {
		return hostTextField.getText();
	}

	public JTextField getIncomingHostTextField() {
		/*
		IncomingServerPanel p = (IncomingServerPanel) prevPanel;
		
		JTextField host = p.getIncomingHostTextField();
		
		return host;
		*/

		return null;
	}

	public void select() {
		hostTextField.setCaretPosition(hostTextField.getText().length());
		hostTextField.selectAll();
	}

	/*
	public void setPrev(DefaultWizardPanel panel)
	{
		
		prevPanel = panel;
	
		getIncomingHostTextField()
			.getDocument()
			.addDocumentListener(new DocumentListener()
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
	
				hostTextField.setText(getIncomingHostTextField().getText());
			}
		});
		
	}

	*/
	
	/*
	protected JPanel createPanel(ActionListener listener) {
		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 20, 30));

		MultiLineLabel label = new MultiLineLabel(MailResourceLoader.getString("dialog", "accountwizard", "please_specify_your_outgoing_mail_server_properties")); //$NON-NLS-1$

		panel.add(label);

		panel.add(Box.createRigidArea(new java.awt.Dimension(0, 40)));

		WizardTextField middlePanel = new WizardTextField();

		JLabel addressLabel = new JLabel(MailResourceLoader.getString("dialog", "accountwizard", "host_smtp_server")); //$NON-NLS-1$
		addressLabel.setDisplayedMnemonic(
			MailResourceLoader.getMnemonic(
				"dialog",
				"accountwizard",
				"host_smtp_server"));
		middlePanel.addLabel(addressLabel);
		hostTextField = new JTextField("");
		addressLabel.setLabelFor(hostTextField);
		//register(hostTextField);
		middlePanel.addTextField(hostTextField);
		JLabel addressExampleLabel = new JLabel(MailResourceLoader.getString("dialog", "accountwizard", "example__mail.microsoft.com")); //$NON-NLS-1$
		middlePanel.addExample(addressExampleLabel);

		panel.add(middlePanel);

		return panel;
	}
	*/

	/**
	 * @see org.columba.core.gui.util.wizard.DefaultWizardPanel#getFocusComponent()
	 */
	public JComponent getFocusComponent() {
		return hostTextField;
	}

}
