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

package org.columba.mail.gui.util;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;
import javax.swing.border.AbstractBorder;

import org.columba.core.gui.button.CancelButton;
import org.columba.core.gui.button.HelpButton;
import org.columba.core.gui.button.OkButton;
import org.columba.core.gui.util.ImageLoader;
import org.columba.mail.util.MailResourceLoader;

public class PasswordDialog implements ActionListener {
	private char[] password;
	private JFrame frame;
	private JDialog dialog;
	private boolean bool = false;
	private JPasswordField passwordField;
	//private JTextField loginTextField;
	private JCheckBox checkbox;
	//private JLabel checkLabel;

	//private String user;
	//private String host;
	private String emailAddress;

	private boolean save;

	private OkButton okButton;
	private CancelButton cancelButton;
	private HelpButton helpButton;

	//private JComboBox loginMethodComboBox;
	//String loginMethod;

	public PasswordDialog() {

	}

	protected JPanel createButtonPanel() {
		JPanel bottom = new JPanel();
		bottom.setLayout(new BorderLayout());
		//bottom.setLayout( new BoxLayout( bottom, BoxLayout.X_AXIS ) );
		bottom.setBorder(BorderFactory.createEmptyBorder(10, 5, 10, 5));

		//bottom.add( Box.createHorizontalStrut());

		//cancelButton = new JButton(GlobalResourceLoader.getString("dialog", "cancel"));
		cancelButton = new CancelButton();
		//$NON-NLS-1$ //$NON-NLS-2$
		cancelButton.addActionListener(this);
		cancelButton.setActionCommand("CANCEL"); //$NON-NLS-1$

		//okButton = new JButton(GlobalResourceLoader.getString("dialog", "ok"));
		//$NON-NLS-1$ //$NON-NLS-2$
		okButton = new OkButton();
		okButton.addActionListener(this);
		okButton.setActionCommand("OK"); //$NON-NLS-1$
		okButton.setDefaultCapable(true);
		dialog.getRootPane().setDefaultButton(okButton);

		//helpButton = new JButton(GlobalResourceLoader.getString("dialog", "help"));
		helpButton = new HelpButton();
		//$NON-NLS-1$ //$NON-NLS-2$

		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(1, 3, 10, 0));
		buttonPanel.add(helpButton);
		buttonPanel.add(cancelButton);
		buttonPanel.add(okButton);

		//bottom.add( Box.createHorizontalGlue() );

		bottom.add(buttonPanel, BorderLayout.EAST);

		return bottom;
	}

	public void showDialog(
		String emailAddress,
		String password,
		boolean save) {

		this.emailAddress = emailAddress;

		//JButton[] buttons = new JButton[2];

		JLabel hostLabel =
			new JLabel(
				MailResourceLoader.getString(
					"dialog",
					"password",
					"enter_password")
					+ " "
					+ emailAddress
					+ ":");

		JLabel passwordLabel = new JLabel("Password:");

		/*
		buttons[0] = new JButton("Cancel");
		buttons[0].addActionListener( this );
		buttons[0].setActionCommand( "CANCEL" );
		
		buttons[1] = new JButton("Ok");
		buttons[1].addActionListener( this );
		buttons[1].setActionCommand( "OK" );
			buttons[1].setDefaultCapable(true);
		buttons[1].setSelected(true);
		*/

		passwordField = new JPasswordField(password, 40);

		checkbox =
			new JCheckBox(
				MailResourceLoader.getString(
					"dialog",
					"password",
					"save_password"));
		if (save)
			checkbox.setSelected(true);
		else
			checkbox.setSelected(false);

		dialog = new JDialog(frame, true);
		dialog.setTitle(
			MailResourceLoader.getString("dialog", "password", "dialog_title"));

		dialog.getContentPane().setLayout(new BorderLayout());

		JPanel centerPanel = new JPanel();
		centerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		dialog.getContentPane().add(centerPanel, BorderLayout.CENTER);

		GridBagLayout mainLayout = new GridBagLayout();
		centerPanel.setLayout(mainLayout);
		GridBagConstraints mainConstraints = new GridBagConstraints();

		JLabel iconLabel =
			new JLabel(ImageLoader.getImageIcon("pgp-signature-nokey.png"));
		mainConstraints.anchor = GridBagConstraints.NORTHWEST;
		mainConstraints.weightx = 1.0;
		mainConstraints.gridwidth = GridBagConstraints.RELATIVE;
		mainConstraints.fill = GridBagConstraints.HORIZONTAL;
		mainLayout.setConstraints(iconLabel, mainConstraints);
		centerPanel.add(iconLabel);

		mainConstraints.gridwidth = GridBagConstraints.REMAINDER;
		mainConstraints.anchor = GridBagConstraints.WEST;
		mainConstraints.insets = new Insets(0, 5, 0, 0);
		mainLayout.setConstraints(hostLabel, mainConstraints);
		centerPanel.add(hostLabel);

		mainConstraints.insets = new Insets(5, 5, 0, 0);
		mainLayout.setConstraints(passwordField, mainConstraints);
		centerPanel.add(passwordField);

		mainConstraints.insets = new Insets(5, 5, 0, 0);
		mainLayout.setConstraints(checkbox, mainConstraints);
		centerPanel.add(checkbox);

		JPanel bottomPanel = new JPanel();
		bottomPanel.setBorder(new WizardTopBorder());
		bottomPanel.setLayout(new BorderLayout());

		JPanel buttonPanel = createButtonPanel();
		bottomPanel.add(buttonPanel, BorderLayout.CENTER);

		dialog.getContentPane().add(bottomPanel, BorderLayout.SOUTH);

		dialog.pack();

		dialog.getRootPane().setDefaultButton(okButton);

		java.awt.Dimension dim = dialog.getSize();

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

		dialog.setLocation(
			screenSize.width / 2 - dim.width / 2,
			screenSize.height / 2 - dim.height / 2);

		dialog.show();

		passwordField.requestFocus();

	}

	public char[] getPassword() {
		return password;
	}

	public boolean success() {
		return bool;
	}

	public boolean getSave() {
		return save;
	}

	/*
	public String getUser()
	{
	    return user;
	}
	
	
	public String getLoginMethod()
	{
		return loginMethod;
	}
	*/

	public void actionPerformed(ActionEvent e) {
		String action = e.getActionCommand();

		if (action.equals("OK")) {
			password = passwordField.getPassword();
			//user = loginTextField.getText();
			save = checkbox.isSelected();
			//loginMethod = (String) loginMethodComboBox.getSelectedItem();
			bool = true;
			dialog.dispose();
		} else if (action.equals("CANCEL")) {
			bool = false;
			dialog.dispose();
		}
	}

	public class WizardTopBorder extends AbstractBorder {
		protected Insets borderInsets = new Insets(2, 0, 0, 0);

		public void paintBorder(
			Component c,
			Graphics g,
			int x,
			int y,
			int w,
			int h) {
			g.setColor(UIManager.getColor("Button.darkShadow"));
			g.drawLine(x, y, x + w - 1, y);
			g.setColor(Color.white);
			g.drawLine(x, y + 1, x + w - 1, y + 1);
		}

		public Insets getBorderInsets(Component c) {
			return borderInsets;
		}
	}
}
