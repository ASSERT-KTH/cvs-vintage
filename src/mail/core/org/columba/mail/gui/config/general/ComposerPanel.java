//The contents of this file are subject to the Mozilla Public License Version 1.1
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
//The Initial Developers of the Original Code are Frederik Dietz and Timo Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003. 
//
//All Rights Reserved.
package org.columba.mail.gui.config.general;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import org.columba.mail.config.MailConfig;

public class ComposerPanel extends JPanel implements ActionListener {

	JLabel spellLabel;
	JButton spellButton;

	JCheckBox emptySubjectCheckBox;

	public ComposerPanel() {
		initComponent();
	}

	public void updateComponents(boolean b) {

		if (b == true) {
			String path =
				MailConfig.getComposerOptionsConfig().getSpellcheckItem().get(
					"executable");
			spellButton.setText(path);
		} else {
			MailConfig.getComposerOptionsConfig().getSpellcheckItem().set(
				"executable",
				spellButton.getText());
		}
	}

	protected void initComponent() {
		setLayout(new BorderLayout(0, 5));
		setBorder(BorderFactory.createEmptyBorder(12, 12, 11, 11));
		JPanel spellPanel = new JPanel();
		spellPanel.setLayout(new BoxLayout(spellPanel, BoxLayout.X_AXIS));
		//LOCALIZE
		spellLabel = new JLabel("Path to aspell executable:");
		spellPanel.add(spellLabel);
		spellPanel.add(Box.createHorizontalStrut(5));
		spellButton = new JButton("aspell.exe");
		spellButton.setActionCommand("PATH");
		spellButton.addActionListener(this);
		spellLabel.setLabelFor(spellButton);
		spellPanel.add(spellButton);
		add(spellPanel, BorderLayout.NORTH);
		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
		//LOCALIZE
		emptySubjectCheckBox =
			new JCheckBox("Ask when sending message with empty subject");
		emptySubjectCheckBox.setEnabled(false);
		centerPanel.add(emptySubjectCheckBox);
		centerPanel.add(Box.createVerticalGlue());
		add(centerPanel, BorderLayout.CENTER);
		//LOCALIZE
		JLabel restartLabel =
			new JLabel(
				"These options affect only new composer windows.",
				SwingConstants.CENTER);
		add(restartLabel, BorderLayout.SOUTH);
	}

	public void actionPerformed(ActionEvent event) {
		String action = event.getActionCommand();

		if (action.equals("PATH")) {
			final JFileChooser fc = new JFileChooser();

			int returnVal = fc.showOpenDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				File file = fc.getSelectedFile();

				spellButton.setText(file.getPath());

			}

		}
	}
}