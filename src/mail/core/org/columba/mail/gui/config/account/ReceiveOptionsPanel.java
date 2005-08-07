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
package org.columba.mail.gui.config.account;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

import org.columba.core.config.IDefaultItem;
import org.columba.core.gui.base.CheckBoxWithMnemonic;
import org.columba.core.gui.base.LabelWithMnemonic;
import org.columba.mail.config.AccountItem;
import org.columba.mail.config.ImapItem;
import org.columba.mail.config.IncomingItem;
import org.columba.mail.config.PopItem;
import org.columba.mail.util.MailResourceLoader;

import com.jgoodies.forms.builder.DefaultFormBuilder;
import com.jgoodies.forms.layout.FormLayout;

/**
 * @author frd
 * 
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates. To enable and disable the creation of type
 * comments go to Window>Preferences>Java>Code Generation.
 */
public class ReceiveOptionsPanel extends DefaultPanel implements ActionListener {

	private AccountItem item;

	private JCheckBox downloadnewCheckBox;

	private JCheckBox playsoundCheckBox;

	private JCheckBox autodownloadCheckBox;

	private JSpinner intervalCheckingSpinner;

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

	private JDialog dialog;

	public ReceiveOptionsPanel(JDialog dialog, AccountItem item) {
		this.item = item;
		this.dialog = dialog;

		if (item.isPopAccount()) {
			popItem = item.getPopItem();
		} else {
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

		mainConstraints = new GridBagConstraints();
		mainConstraints.weighty = 1.0;
		mainConstraints.gridwidth = GridBagConstraints.REMAINDER;

		JLabel label = new JLabel(MailResourceLoader.getString("dialog",
				"account", "using_default_account_settings"));
		Font newFont = label.getFont().deriveFont(Font.BOLD);
		label.setFont(newFont);
		mainLayout.setConstraints(label, mainConstraints);
		add(label);
	}

	protected void layoutComponents() {
		//		Create a FormLayout instance.
		FormLayout layout = new FormLayout(
				"10dlu, 10dlu, max(100;default), 3dlu, fill:max(150dlu;default):grow",

				// 2 columns
				""); // rows are added dynamically (no need to define them here)

		DefaultFormBuilder builder = new DefaultFormBuilder(this, layout);
		builder.setLeadingColumnOffset(1);

		// create EmptyBorder between components and dialog-frame
		builder.setDefaultDialogBorder();

		// Add components to the panel:
		builder.appendSeparator(MailResourceLoader.getString("dialog",
				"account", "automatic_mailchecking"));

		builder.append(intervalCheckingCheckBox, 4);
		builder.nextLine();

		//builder.setLeadingColumnOffset(2);
		builder.setLeadingColumnOffset(2);
		builder.append(autodownloadCheckBox, 3);
		builder.nextLine();

		builder.append(playsoundCheckBox, 3);
		builder.nextLine();

		JPanel panel = new JPanel();
		FormLayout l = new FormLayout("default, 3dlu, default, 3dlu, default",

		// 2 columns
				""); // rows are added dynamically (no need to define them here)

		// create a form builder
		DefaultFormBuilder b = new DefaultFormBuilder(panel, l);
		b.append(intervalCheckingLabel, intervalCheckingSpinner,
				intervalCheckingLabel2);

		builder.append(panel, 3);

		//b2.nextLine();
		builder.setLeadingColumnOffset(1);

		if (item.isPopAccount()) {
			popPanel.createPanel(builder);
		} else {
			imapPanel.createPanel(builder);

			//attributPanel.add(imapPanel, BorderLayout.CENTER);
		}

	}

	protected void initComponents() {
		intervalCheckingLabel = new LabelWithMnemonic(MailResourceLoader
				.getString("dialog", "account", "check_for_new_messages_every"));

		intervalCheckingSpinner = new JSpinner(new SpinnerNumberModel(5, 1,
				100, 1));
		intervalCheckingLabel.setLabelFor(intervalCheckingSpinner);

		intervalCheckingLabel2 = new JLabel(MailResourceLoader.getString(
				"dialog", "account", "minutes"));

		intervalCheckingCheckBox = new CheckBoxWithMnemonic(MailResourceLoader
				.getString("dialog", "account",
						"enable_interval_message_checking"));

		intervalCheckingCheckBox.setActionCommand("ENABLE");
		intervalCheckingCheckBox.addActionListener(this);

		autodownloadCheckBox = new CheckBoxWithMnemonic(MailResourceLoader
				.getString("dialog", "account",
						IncomingItem.AUTOMATICALLY_DOWNLOAD_NEW_MESSAGES));

		playsoundCheckBox = new CheckBoxWithMnemonic(MailResourceLoader
				.getString("dialog", "account",
						"play_sound_when_new_messages_arrive"));

		playsoundCheckBox.setActionCommand("PLAYSOUND");
		playsoundCheckBox.addActionListener(this);

		ButtonGroup group = new ButtonGroup();
		defaultRadioButton = new JRadioButton(MailResourceLoader.getString(
				"dialog", "account", "default_soundfile"));

		group.add(defaultRadioButton);
		chooseRadioButton = new JRadioButton(MailResourceLoader.getString(
				"dialog", "account", "choose_soundfile"));
		group.add(chooseRadioButton);
		chooseButton = new JButton("..");
		chooseButton.setActionCommand("CHOOSE");
		chooseButton.addActionListener(this);

		if (item.isPopAccount()) {
			popPanel = new PopAttributPanel(dialog, item.getPopItem());

		} else {
			imapPanel = new ImapAttributPanel(item.getImapItem());

		}
	}

	public void updateComponents(boolean b) {
		IDefaultItem receiveItem;

		if (item.isPopAccount()) {
			receiveItem = item.getPopItem();
		} else {
			receiveItem = item.getImapItem();
		}

		if (b) {
			intervalCheckingCheckBox.setSelected(receiveItem
					.getBoolean(IncomingItem.ENABLE_MAILCHECK));

			if (!intervalCheckingCheckBox.isSelected()) {
				// disable components
				defaultRadioButton.setEnabled(false);
				autodownloadCheckBox.setEnabled(false);
				playsoundCheckBox.setEnabled(false);
				chooseButton.setEnabled(false);
				intervalCheckingLabel.setEnabled(false);
				intervalCheckingLabel2.setEnabled(false);
				intervalCheckingSpinner.setEnabled(false);
				chooseRadioButton.setEnabled(false);
			}

			playsoundCheckBox.setSelected(receiveItem
					.getBoolean(IncomingItem.ENABLE_SOUND));

			autodownloadCheckBox
					.setSelected(receiveItem
							.getBoolean(IncomingItem.AUTOMATICALLY_DOWNLOAD_NEW_MESSAGES));

			intervalCheckingSpinner.setValue(new Integer(receiveItem
					.getIntegerWithDefault(IncomingItem.MAILCHECK_INTERVAL,
							IncomingItem.MAIL_CHECK_INTERVAL_DEFAULT_INT)));

			String soundfile = receiveItem.get(IncomingItem.SOUND_FILE);

			if (soundfile.equalsIgnoreCase(IncomingItem.DEFAULT)) {
				defaultRadioButton.setSelected(true);
			} else {
				chooseRadioButton.setSelected(true);
			}

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

			boolean useDefault = receiveItem
					.getBoolean(IncomingItem.USE_DEFAULT_ACCOUNT);

			if (useDefault) {
				showDefaultAccountWarning();
			} else {
				layoutComponents();
			}
		} else {
			receiveItem.setBoolean(IncomingItem.ENABLE_MAILCHECK,
					intervalCheckingCheckBox.isSelected());

			receiveItem.setBoolean(IncomingItem.ENABLE_SOUND, playsoundCheckBox
					.isSelected());

			receiveItem.setBoolean(
					IncomingItem.AUTOMATICALLY_DOWNLOAD_NEW_MESSAGES,
					autodownloadCheckBox.isSelected());

			receiveItem.setString(IncomingItem.MAILCHECK_INTERVAL,
					((Integer) intervalCheckingSpinner.getValue()).toString());

			if (defaultRadioButton.isSelected()) {
				receiveItem.setString(IncomingItem.SOUND_FILE,
						IncomingItem.DEFAULT);
			} else {
				receiveItem.setString(IncomingItem.SOUND_FILE, chooseButton
						.getText());
			}
		}

		if (item.isPopAccount()) {
			popPanel.updateComponents(b);
		} else {
			imapPanel.updateComponents(b);
		}
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("ENABLE")) {
			boolean doIntervalChecking = intervalCheckingCheckBox.isSelected();
			defaultRadioButton.setEnabled(doIntervalChecking);
			autodownloadCheckBox.setEnabled(doIntervalChecking);
			playsoundCheckBox.setEnabled(doIntervalChecking);
			chooseButton.setEnabled(doIntervalChecking);
			chooseRadioButton.setEnabled(doIntervalChecking);
			intervalCheckingLabel.setEnabled(doIntervalChecking);
			intervalCheckingLabel2.setEnabled(doIntervalChecking);
			intervalCheckingSpinner.setEnabled(doIntervalChecking);

			boolean playSound = playsoundCheckBox.isSelected();
			defaultRadioButton.setEnabled(playSound);
			chooseRadioButton.setEnabled(playSound);
			chooseButton.setEnabled(playSound);
		} else if (e.getActionCommand().equals("PLAYSOUND")) {
			boolean playSound = playsoundCheckBox.isSelected();
			defaultRadioButton.setEnabled(playSound);
			chooseButton.setEnabled(playSound);
			chooseRadioButton.setEnabled(playSound);
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