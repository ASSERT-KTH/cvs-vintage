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

import org.columba.core.config.DefaultItem;
import org.columba.core.config.IDefaultItem;
import org.columba.core.gui.util.CheckBoxWithMnemonic;
import org.columba.core.gui.util.LabelWithMnemonic;
import org.columba.mail.config.AccountItem;
import org.columba.mail.config.ImapItem;
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

		/*
		 * mainConstraints.gridwidth = GridBagConstraints.REMAINDER;
		 * mainConstraints.anchor = GridBagConstraints.NORTHWEST;
		 * mainConstraints.weightx = 1.0; mainConstraints.insets = new Insets(0,
		 * 10, 5, 0); mainLayout.setConstraints(defaultAccountCheckBox,
		 * mainConstraints); add(defaultAccountCheckBox);
		 */
		mainConstraints = new GridBagConstraints();
		mainConstraints.weighty = 1.0;
		mainConstraints.gridwidth = GridBagConstraints.REMAINDER;

		/*
		 * mainConstraints.fill = GridBagConstraints.BOTH;
		 * mainConstraints.insets = new Insets(0, 0, 0, 0);
		 * mainConstraints.gridwidth = GridBagConstraints.REMAINDER;
		 * mainConstraints.weightx = 1.0; mainConstraints.weighty = 1.0;
		 */
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

		/*
		 * setLayout(new BorderLayout()); add(builder.getPanel(),
		 * BorderLayout.CENTER);
		 */
		/*
		 * setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		 * 
		 * GridBagLayout mainLayout = new GridBagLayout(); GridBagConstraints
		 * mainConstraints = new GridBagConstraints();
		 * 
		 * mainConstraints.anchor = GridBagConstraints.NORTHWEST;
		 * mainConstraints.fill = GridBagConstraints.HORIZONTAL;
		 * mainConstraints.weightx = 1.0;
		 * 
		 * setLayout(mainLayout);
		 * 
		 * JPanel mailcheckPanel = new JPanel(); Border b1 =
		 * BorderFactory.createEtchedBorder(); Border b2 =
		 * BorderFactory.createTitledBorder( b1, MailResourceLoader.getString(
		 * "dialog", "account", "automatic_mailchecking")); Border emptyBorder =
		 * BorderFactory.createEmptyBorder(5, 5, 5, 5); Border border =
		 * BorderFactory.createCompoundBorder(b2, emptyBorder);
		 * mailcheckPanel.setBorder(border); GridBagLayout layout = new
		 * GridBagLayout(); GridBagConstraints c = new GridBagConstraints();
		 * mailcheckPanel.setLayout(layout);
		 * 
		 * 
		 * mainConstraints.gridwidth = GridBagConstraints.REMAINDER;
		 * mainConstraints.insets = new Insets(0, 0, 0, 0);
		 * mainLayout.setConstraints(mailcheckPanel, mainConstraints);
		 * add(mailcheckPanel);
		 * 
		 * JPanel panel = new JPanel(); panel.setLayout(layout);
		 * 
		 * c.fill = GridBagConstraints.NONE; c.anchor = GridBagConstraints.WEST;
		 * c.gridx = 0; //c.gridwidth = GridBagConstraints.RELATIVE; c.weightx =
		 * 0.0; layout.setConstraints(intervalCheckingLabel, c);
		 * panel.add(intervalCheckingLabel);
		 * 
		 * c.weightx = 0.0; c.gridx = 1; c.anchor = GridBagConstraints.WEST;
		 * //c.gridwidth = GridBagConstraints.RELATIVE; c.insets = new Insets(0,
		 * 5, 0, 0); layout.setConstraints(intervalCheckingTextField, c);
		 * panel.add(intervalCheckingTextField);
		 * 
		 * c.weightx = 0.0; c.gridx = 2; c.anchor = GridBagConstraints.WEST;
		 * //c.gridwidth = GridBagConstraints.REMAINDER;
		 * layout.setConstraints(intervalCheckingLabel2, c);
		 * panel.add(intervalCheckingLabel2);
		 * 
		 * c.gridwidth = GridBagConstraints.REMAINDER; c.insets = new Insets(0,
		 * 0, 0, 0); layout.setConstraints(intervalCheckingCheckBox, c);
		 * mailcheckPanel.add(intervalCheckingCheckBox);
		 * 
		 * c.gridwidth = GridBagConstraints.REMAINDER; c.insets = new Insets(0,
		 * 20, 0, 0); layout.setConstraints(panel, c);
		 * mailcheckPanel.add(panel);
		 * 
		 * c.gridwidth = GridBagConstraints.REMAINDER; //c.gridx = 0; c.insets =
		 * new Insets(0, 20, 0, 0); c.weightx = 1.0;
		 * layout.setConstraints(autodownloadCheckBox, c);
		 * mailcheckPanel.add(autodownloadCheckBox);
		 * 
		 * c.weightx = 1.0; c.gridwidth = GridBagConstraints.REMAINDER; c.insets =
		 * new Insets(0, 20, 0, 0); layout.setConstraints(playsoundCheckBox, c);
		 * mailcheckPanel.add(playsoundCheckBox);
		 * 
		 * c.weightx = 1.0; c.insets = new Insets(0, 40, 0, 0); c.gridwidth =
		 * GridBagConstraints.REMAINDER;
		 * layout.setConstraints(defaultRadioButton, c);
		 * mailcheckPanel.add(defaultRadioButton);
		 * 
		 * JPanel panel2 = new JPanel(); panel2.setLayout(layout);
		 * 
		 * c.gridwidth = GridBagConstraints.REMAINDER; c.weightx = 1.0; c.insets =
		 * new Insets(0, 40, 0, 0); //c.fill = GridBagConstraints.HORIZONTAL;
		 * layout.setConstraints(panel2, c); mailcheckPanel.add(panel2);
		 * 
		 * c.weightx = 0.1; c.gridx = 0; c.insets = new Insets(0, 0, 0, 0);
		 * c.fill = GridBagConstraints.HORIZONTAL; c.gridwidth =
		 * GridBagConstraints.RELATIVE; //c.fill = GridBagConstraints.NONE;
		 * c.anchor = GridBagConstraints.WEST;
		 * layout.setConstraints(chooseRadioButton, c);
		 * panel2.add(chooseRadioButton);
		 * 
		 * c.weightx = 0.9; c.gridx = 1; //c.insets = new Insets(0,5,0,0);
		 * //c.gridx = 1; c.gridwidth = GridBagConstraints.REMAINDER;
		 * layout.setConstraints(chooseButton, c); panel2.add(chooseButton);
		 * 
		 * JPanel attributPanel = new JPanel(); b1 =
		 * BorderFactory.createEtchedBorder(); b2 =
		 * BorderFactory.createTitledBorder( b1,
		 * MailResourceLoader.getString("dialog", "account", "options"));
		 * emptyBorder = BorderFactory.createEmptyBorder(5, 5, 5, 5); border =
		 * BorderFactory.createCompoundBorder(b2, emptyBorder);
		 * attributPanel.setBorder(border); attributPanel.setLayout(new
		 * BorderLayout());
		 * 
		 * if (item.isPopAccount()) { //popPanel = new
		 * PopAttributPanel(item.getPopItem()); attributPanel.add(popPanel,
		 * BorderLayout.CENTER); //mainLayout.setConstraints(popPanel,
		 * mainConstraints); //add(popPanel); } else { //imapPanel = new
		 * ImapAttributPanel(item.getImapItem()); attributPanel.add(imapPanel,
		 * BorderLayout.CENTER); //mainLayout.setConstraints(imapPanel,
		 * mainConstraints); //add(imapPanel); }
		 * 
		 * mainConstraints.gridheight = GridBagConstraints.REMAINDER;
		 * mainConstraints.weightx = 1.0;
		 * mainLayout.setConstraints(attributPanel, mainConstraints);
		 * add(attributPanel);
		 * 
		 * mainConstraints.gridheight = GridBagConstraints.REMAINDER;
		 * mainConstraints.weighty = 1.0; mainConstraints.fill =
		 * GridBagConstraints.VERTICAL; Component vglue =
		 * Box.createVerticalGlue(); mainLayout.setConstraints(vglue,
		 * mainConstraints); add(vglue);
		 */
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
						"automatically_download_new_messages"));

		playsoundCheckBox = new CheckBoxWithMnemonic(MailResourceLoader
				.getString("dialog", "account",
						"play_sound_when_new_messages_arrive"));

		playsoundCheckBox.setActionCommand("PLAYSOUND");
		playsoundCheckBox.addActionListener(this);

		ButtonGroup group = new ButtonGroup();
		defaultRadioButton = new JRadioButton(MailResourceLoader.getString(
				"dialog", "account", "default_soundfile"));

		//defaultRadioButton.setActionCommand("DEFAULT");
		//defaultRadioButton.addActionListener(this);
		group.add(defaultRadioButton);
		chooseRadioButton = new JRadioButton(MailResourceLoader.getString(
				"dialog", "account", "choose_soundfile"));
		group.add(chooseRadioButton);
		chooseButton = new JButton("..");
		chooseButton.setActionCommand("CHOOSE");
		chooseButton.addActionListener(this);

		if (item.isPopAccount()) {
			popPanel = new PopAttributPanel(dialog, item.getPopItem());

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
		IDefaultItem receiveItem;

		if (item.isPopAccount()) {
			receiveItem = item.getPopItem();
		} else {
			receiveItem = item.getImapItem();
		}

		if (b) {
			intervalCheckingCheckBox.setSelected(receiveItem
					.getBoolean("enable_mailcheck"));

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
					.getBoolean("enable_sound"));

			autodownloadCheckBox.setSelected(receiveItem
					.getBoolean("automatically_download_new_messages"));

			intervalCheckingSpinner.setValue(new Integer(receiveItem.getInteger("mailcheck_interval", 10)));

			String soundfile = receiveItem.get("sound_file");

			if (soundfile.equalsIgnoreCase("default")) {
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

			boolean useDefault = receiveItem.getBoolean("use_default_account");

			if (useDefault) {
				showDefaultAccountWarning();
			} else {
				layoutComponents();
			}
		} else {
			receiveItem.set("enable_mailcheck", intervalCheckingCheckBox
					.isSelected());

			receiveItem.set("enable_sound", playsoundCheckBox.isSelected());

			receiveItem.set("automatically_download_new_messages",
					autodownloadCheckBox.isSelected());

			receiveItem.set("mailcheck_interval",
					((Integer) intervalCheckingSpinner.getValue()).toString());

			if (defaultRadioButton.isSelected()) {
				receiveItem.set("sound_file", "default");
			} else {
				receiveItem.set("sound_file", chooseButton.getText());
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