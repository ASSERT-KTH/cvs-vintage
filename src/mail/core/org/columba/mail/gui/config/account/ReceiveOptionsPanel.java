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

package org.columba.mail.gui.config.account;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.border.Border;

import org.columba.mail.config.AccountItem;
import org.columba.mail.config.ImapItem;
import org.columba.mail.config.MailCheckInterface;
import org.columba.mail.config.PopItem;
import org.columba.mail.util.MailResourceLoader;

/**
 * @author frd
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class ReceiveOptionsPanel
	extends DefaultPanel
	implements ActionListener {

	private AccountItem item;

	private JCheckBox downloadnewCheckBox;
	private JCheckBox playsoundCheckBox;
	private JCheckBox autodownloadCheckBox;
	private JTextField intervalCheckingTextField;
	private JLabel intervalCheckingLabel;
	private JLabel intervalCheckingLabel2;
	private JCheckBox intervalCheckingCheckBox;

	private JRadioButton defaultRadioButton;
	private JRadioButton chooseRadioButton;
	private JButton chooseButton;

	private PopItem popItem = null;
	private ImapItem imapItem = null;

	private PopAttributPanel popPanel;
	private ImapAttributPanel imapPanel;

	public ReceiveOptionsPanel(AccountItem item) {
		this.item = item;

		if (item.isPopAccount())
			popItem = item.getPopItem();
		else {
			imapItem = item.getImapItem();
		}

		initComponents();

		

		updateComponents(true);

	}

	protected void showDefaultAccountWarning() {

		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		GridBagLayout mainLayout = new GridBagLayout();
		GridBagConstraints mainConstraints = new GridBagConstraints();

		setLayout(mainLayout);

		/*
		mainConstraints.gridwidth = GridBagConstraints.REMAINDER;
		mainConstraints.anchor = GridBagConstraints.NORTHWEST;
		mainConstraints.weightx = 1.0;
		mainConstraints.insets = new Insets(0, 10, 5, 0);
		mainLayout.setConstraints(defaultAccountCheckBox, mainConstraints);
		add(defaultAccountCheckBox);
		*/

		mainConstraints = new GridBagConstraints();
		mainConstraints.weighty = 1.0;
		mainConstraints.gridwidth = GridBagConstraints.REMAINDER;
		/*
		mainConstraints.fill = GridBagConstraints.BOTH;
		mainConstraints.insets = new Insets(0, 0, 0, 0);
		mainConstraints.gridwidth = GridBagConstraints.REMAINDER;
		mainConstraints.weightx = 1.0;
		mainConstraints.weighty = 1.0;
		*/

		JLabel label =
			new JLabel(
				MailResourceLoader.getString(
					"dialog",
					"account",
					"using_default_account_settings"));
		Font newFont = label.getFont().deriveFont(Font.BOLD);
		label.setFont(newFont);
		mainLayout.setConstraints(label, mainConstraints);
		add(label);

	}

	protected void layoutComponents() {
		setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		GridBagLayout mainLayout = new GridBagLayout();
		GridBagConstraints mainConstraints = new GridBagConstraints();

		mainConstraints.anchor = GridBagConstraints.NORTHWEST;
		mainConstraints.fill = GridBagConstraints.HORIZONTAL;
		mainConstraints.weightx = 1.0;

		setLayout(mainLayout);

		JPanel mailcheckPanel = new JPanel();
		Border b1 = BorderFactory.createEtchedBorder();
		Border b2 =
			BorderFactory.createTitledBorder(
				b1,
				MailResourceLoader.getString(
					"dialog",
					"account",
					"automatic_mailchecking"));
		Border emptyBorder = BorderFactory.createEmptyBorder(5, 5, 5, 5);
		Border border = BorderFactory.createCompoundBorder(b2, emptyBorder);
		mailcheckPanel.setBorder(border);
		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		mailcheckPanel.setLayout(layout);

		/*
		mainConstraints.gridwidth = GridBagConstraints.REMAINDER;
		mainConstraints.insets = new Insets(0, 5, 0, 0);
		mainLayout.setConstraints(intervalCheckingCheckBox, mainConstraints);
		add(intervalCheckingCheckBox);
		*/

		mainConstraints.gridwidth = GridBagConstraints.REMAINDER;
		mainConstraints.insets = new Insets(0, 0, 0, 0);
		mainLayout.setConstraints(mailcheckPanel, mainConstraints);
		add(mailcheckPanel);

		JPanel panel = new JPanel();
		panel.setLayout(layout);

		c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.WEST;
		c.gridx = 0;
		//c.gridwidth = GridBagConstraints.RELATIVE;
		c.weightx = 0.0;
		layout.setConstraints(intervalCheckingLabel, c);
		panel.add(intervalCheckingLabel);

		c.weightx = 0.0;
		c.gridx = 1;
		c.anchor = GridBagConstraints.WEST;
		//c.gridwidth = GridBagConstraints.RELATIVE;
		c.insets = new Insets(0, 5, 0, 0);
		layout.setConstraints(intervalCheckingTextField, c);
		panel.add(intervalCheckingTextField);

		c.weightx = 0.0;
		c.gridx = 2;
		c.anchor = GridBagConstraints.WEST;
		//c.gridwidth = GridBagConstraints.REMAINDER;
		layout.setConstraints(intervalCheckingLabel2, c);
		panel.add(intervalCheckingLabel2);

		c.gridwidth = GridBagConstraints.REMAINDER;
		c.insets = new Insets(0, 0, 0, 0);
		layout.setConstraints(intervalCheckingCheckBox, c);
		mailcheckPanel.add(intervalCheckingCheckBox);

		c.gridwidth = GridBagConstraints.REMAINDER;
		c.insets = new Insets(0, 20, 0, 0);
		layout.setConstraints(panel, c);
		mailcheckPanel.add(panel);

		c.gridwidth = GridBagConstraints.REMAINDER;
		//c.gridx = 0;
		c.insets = new Insets(0, 20, 0, 0);
		c.weightx = 1.0;
		layout.setConstraints(autodownloadCheckBox, c);
		mailcheckPanel.add(autodownloadCheckBox);

		c.weightx = 1.0;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.insets = new Insets(0, 20, 0, 0);
		layout.setConstraints(playsoundCheckBox, c);
		mailcheckPanel.add(playsoundCheckBox);

		c.weightx = 1.0;
		c.insets = new Insets(0, 40, 0, 0);
		c.gridwidth = GridBagConstraints.REMAINDER;
		layout.setConstraints(defaultRadioButton, c);
		mailcheckPanel.add(defaultRadioButton);

		JPanel panel2 = new JPanel();
		panel2.setLayout(layout);

		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 1.0;
		c.insets = new Insets(0, 40, 0, 0);
		//c.fill = GridBagConstraints.HORIZONTAL;		
		layout.setConstraints(panel2, c);
		mailcheckPanel.add(panel2);

		c.weightx = 0.1;
		c.gridx = 0;
		c.insets = new Insets(0, 0, 0, 0);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = GridBagConstraints.RELATIVE;
		//c.fill = GridBagConstraints.NONE;
		c.anchor = GridBagConstraints.WEST;
		layout.setConstraints(chooseRadioButton, c);
		panel2.add(chooseRadioButton);

		c.weightx = 0.9;
		c.gridx = 1;
		//c.insets = new Insets(0,5,0,0);
		//c.gridx = 1;
		c.gridwidth = GridBagConstraints.REMAINDER;
		layout.setConstraints(chooseButton, c);
		panel2.add(chooseButton);

		JPanel attributPanel = new JPanel();
		b1 = BorderFactory.createEtchedBorder();
		b2 =
			BorderFactory.createTitledBorder(
				b1,
				MailResourceLoader.getString("dialog", "account", "options"));
		emptyBorder = BorderFactory.createEmptyBorder(5, 5, 5, 5);
		border = BorderFactory.createCompoundBorder(b2, emptyBorder);
		attributPanel.setBorder(border);
		attributPanel.setLayout(new BorderLayout());

		if (item.isPopAccount()) {
			//popPanel = new PopAttributPanel(item.getPopItem());
			attributPanel.add(popPanel, BorderLayout.CENTER);
			//mainLayout.setConstraints(popPanel, mainConstraints);
			//add(popPanel);
		} else {
			//imapPanel = new ImapAttributPanel(item.getImapItem());
			attributPanel.add(imapPanel, BorderLayout.CENTER);
			//mainLayout.setConstraints(imapPanel, mainConstraints);
			//add(imapPanel);
		}

		mainConstraints.gridheight = GridBagConstraints.REMAINDER;
		mainConstraints.weightx = 1.0;
		mainLayout.setConstraints(attributPanel, mainConstraints);
		add(attributPanel);

		mainConstraints.gridheight = GridBagConstraints.REMAINDER;
		mainConstraints.weighty = 1.0;
		mainConstraints.fill = GridBagConstraints.VERTICAL;
		Component vglue = Box.createVerticalGlue();
		mainLayout.setConstraints(vglue, mainConstraints);
		add(vglue);
	}

	protected void initComponents() {

		intervalCheckingLabel = new JLabel();

		intervalCheckingLabel.setText(
			MailResourceLoader.getString(
				"dialog",
				"account",
				"check_for_new_messages_every"));
		intervalCheckingLabel.setDisplayedMnemonic(
			MailResourceLoader.getMnemonic(
				"dialog",
				"account",
				"check_for_new_messages_every"));

		intervalCheckingTextField = new JTextField(3);
		intervalCheckingLabel.setLabelFor(intervalCheckingTextField);

		intervalCheckingTextField.setText("10");

		intervalCheckingLabel2 = new JLabel();

		intervalCheckingLabel2.setText(
			MailResourceLoader.getString("dialog", "account", "minutes"));

		intervalCheckingCheckBox = new JCheckBox();
		intervalCheckingCheckBox.setText(
			MailResourceLoader.getString(
				"dialog",
				"account",
				"enable_interval_message_checking"));
		intervalCheckingCheckBox.setMnemonic(
			MailResourceLoader.getMnemonic(
				"dialog",
				"account",
				"enable_interval_message_checking"));

		intervalCheckingCheckBox.setActionCommand("ENABLE");
		intervalCheckingCheckBox.addActionListener(this);

		autodownloadCheckBox =
			new JCheckBox(
				MailResourceLoader.getString(
					"dialog",
					"account",
					"automatically_download_new_messages"));
		autodownloadCheckBox.setMnemonic(
			MailResourceLoader.getMnemonic(
				"dialog",
				"account",
				"automatically_download_new_messages"));

		playsoundCheckBox =
			new JCheckBox(
				MailResourceLoader.getString(
					"dialog",
					"account",
					"play_sound_when_new_messages_arrive"));
		playsoundCheckBox.setMnemonic(
			MailResourceLoader.getMnemonic(
				"dialog",
				"account",
				"play_sound_when_new_messages_arrive"));

		playsoundCheckBox.setActionCommand("PLAYSOUND");
		playsoundCheckBox.addActionListener(this);

		ButtonGroup group = new ButtonGroup();
		defaultRadioButton =
			new JRadioButton(
				MailResourceLoader.getString(
					"dialog",
					"account",
					"default_soundfile"));
		//defaultRadioButton.setActionCommand("DEFAULT");
		//defaultRadioButton.addActionListener(this);
		group.add(defaultRadioButton);
		chooseRadioButton =
			new JRadioButton(
				MailResourceLoader.getString(
					"dialog",
					"account",
					"choose_soundfile"));
		group.add(chooseRadioButton);
		chooseButton = new JButton("..");
		chooseButton.setActionCommand("CHOOSE");
		chooseButton.addActionListener(this);

		if (item.isPopAccount()) {
			popPanel = new PopAttributPanel(item.getPopItem());
			//attributPanel.add(popPanel, BorderLayout.CENTER);
			//mainLayout.setConstraints(popPanel, mainConstraints);
			//add(popPanel);
		} else {
			imapPanel = new ImapAttributPanel(item.getImapItem());
			//attributPanel.add(imapPanel, BorderLayout.CENTER);
			//mainLayout.setConstraints(imapPanel, mainConstraints);
			//add(imapPanel);
		}
	}

	public void updateComponents(boolean b) {
		MailCheckInterface mailcheckInterface = null;

		if (item.isPopAccount())
			mailcheckInterface = item.getPopItem();
		else 
			mailcheckInterface = item.getImapItem();
		

		if (b) {

			intervalCheckingCheckBox.setSelected(
				mailcheckInterface.isMailCheck());

			playsoundCheckBox.setSelected(mailcheckInterface.isPlaysound());

			autodownloadCheckBox.setSelected(
				mailcheckInterface.isAutoDownload());

			intervalCheckingTextField.setText(mailcheckInterface.getInterval());

			String soundfile = mailcheckInterface.getSoundfile();

			if (soundfile.equalsIgnoreCase("default"))
				defaultRadioButton.setSelected(true);
			else
				chooseRadioButton.setSelected(true);

			if (playsoundCheckBox.isSelected()) {
				defaultRadioButton.setEnabled(true);
				chooseRadioButton.setEnabled(true);
				chooseButton.setEnabled(true);
			} else {
				defaultRadioButton.setEnabled(false);
				chooseRadioButton.setEnabled(false);
				chooseButton.setEnabled(false);
			}

			chooseButton.setText(soundfile);

			boolean useDefault = false;

			if (item.isPopAccount()) {
				PopItem popItem = item.getPopItem();
				if (popItem.isUseDefaultAccount())
					useDefault = true;
			} else {
				ImapItem imapItem = item.getImapItem();
				if (imapItem.isUseDefaultAccount())
					useDefault = true;
			}

			if (useDefault)
				showDefaultAccountWarning();
			else
			{
				layoutComponents();
			}

		} else {

			if (intervalCheckingCheckBox.isSelected() == true)
				mailcheckInterface.setMailCheck("true");
			else
				mailcheckInterface.setMailCheck("false");

			if (playsoundCheckBox.isSelected() == true)
				mailcheckInterface.setPlaysound(true);
			else
				mailcheckInterface.setPlaysound(false);

			if (autodownloadCheckBox.isSelected() == true)
				mailcheckInterface.setAutodownload(true);
			else
				mailcheckInterface.setAutodownload(false);

			mailcheckInterface.setInterval(intervalCheckingTextField.getText());

			if (defaultRadioButton.isSelected())
				mailcheckInterface.setSoundfile("default");
			else
				mailcheckInterface.setSoundfile(chooseButton.getText());
		}

		if (item.isPopAccount()) {
			popPanel.updateComponents(b);
		} else {
			imapPanel.updateComponents(b);
		}
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("ENABLE")) {
			if (intervalCheckingCheckBox.isSelected()) {
				defaultRadioButton.setEnabled(true);
				autodownloadCheckBox.setEnabled(true);
				playsoundCheckBox.setEnabled(true);
				chooseButton.setEnabled(true);
				chooseRadioButton.setEnabled(true);
				intervalCheckingLabel.setEnabled(true);
				intervalCheckingLabel2.setEnabled(true);
				intervalCheckingTextField.setEnabled(true);
			} else {
				defaultRadioButton.setEnabled(false);
				autodownloadCheckBox.setEnabled(false);
				playsoundCheckBox.setEnabled(false);
				chooseButton.setEnabled(false);
				intervalCheckingLabel.setEnabled(false);
				intervalCheckingLabel2.setEnabled(false);
				intervalCheckingTextField.setEnabled(false);
				chooseRadioButton.setEnabled(false);
			}

			if (playsoundCheckBox.isSelected()) {
				defaultRadioButton.setEnabled(true);
				chooseRadioButton.setEnabled(true);
				chooseButton.setEnabled(true);
			} else {
				defaultRadioButton.setEnabled(false);
				chooseButton.setEnabled(false);
				chooseRadioButton.setEnabled(false);
			}

		} else if (e.getActionCommand().equals("PLAYSOUND")) {

			if (playsoundCheckBox.isSelected()) {
				defaultRadioButton.setEnabled(true);
				chooseRadioButton.setEnabled(true);
				chooseButton.setEnabled(true);
			} else {
				defaultRadioButton.setEnabled(false);
				chooseButton.setEnabled(false);
				chooseRadioButton.setEnabled(false);
			}
		} else if (e.getActionCommand().equals("CHOOSE")) {
			JFileChooser fc = new JFileChooser();
			int returnVal = fc.showOpenDialog(this);

			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();
				chooseButton.setText(file.getPath());
			}
		}

	}
}
