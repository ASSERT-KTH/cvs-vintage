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

package org.columba.core.gui.config.themes;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.columba.core.config.Config;
import org.columba.core.config.GuiItem;
import org.columba.core.gui.util.FontSelectionDialog;
import org.columba.core.util.GlobalResourceLoader;

public class FontPanel extends JPanel implements ActionListener {
	private static final String RESOURCE_PATH = "org.columba.mail.i18n.dialog";

	private JTextField mainFontTextField;
	private JButton mainFontButton;

	private JTextField textFontTextField;
	private JButton textFontButton;

	private JCheckBox overwriteCheckBox;

	private Font mainFont;
	private Font textFont;

	public FontPanel() {
		initComponent();
	}

	public Font getMainFont() {
		return mainFontTextField.getFont();
	}

	public Font getTextFont() {
		return textFontTextField.getFont();
	}

	public void updateComponents(boolean b) {
		GuiItem item = Config.getOptionsConfig().getGuiItem();
		mainFont = item.getMainFont();
		textFont = item.getTextFont();

		if (b) {
			mainFontTextField.setFont(mainFont);
			mainFontTextField.setText(mainFont.getFontName());
			textFontTextField.setFont(textFont);
			textFontTextField.setText(textFont.getFontName());

			overwriteCheckBox.setSelected(
				item.getBoolean("mainfont", "overwrite"));
                        actionPerformed(new ActionEvent(overwriteCheckBox, ActionEvent.ACTION_PERFORMED, null));
		} else {
			item.set("textfont", "name", getTextFont().getName());
			item.set("textfont", "size", getTextFont().getSize());
			item.set("mainfont", "name", getMainFont().getName());
			item.set("mainfont", "size", getMainFont().getSize());
			item.set("mainfont", "overwrite", overwriteCheckBox.isSelected());
		}
	}

	protected void initComponent() {
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(12, 12, 11, 11));

		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();

		JPanel fontPanel = new JPanel();
		fontPanel.setLayout(layout);

		/*
		JPanel mainFontPanel = new JPanel();
		mainFontPanel.setLayout(new BoxLayout(mainFontPanel, BoxLayout.X_AXIS));
		mainFontPanel.add(Box.createRigidArea(new java.awt.Dimension(5, 0)));
		//mainFontPanel.setAlignmentX(0);
		 * */

		c.gridx = 0;
		c.gridy = 0;
		c.insets = new Insets(0, 10, 0, 0);
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = 3;
		c.weightx = 1.0;

		overwriteCheckBox =
			new JCheckBox(
				GlobalResourceLoader.getString(
					RESOURCE_PATH,
					"general",
					"overwrite_main_font"));
                overwriteCheckBox.addActionListener(this);
		fontPanel.add(overwriteCheckBox);

		c.gridx = 0;
		c.gridy = 1;
		c.insets = new Insets(0, 0, 0, 0);
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		//c.gridwidth = GridBagConstraints.REMAINDER;
		c.gridwidth = 1;
		c.weightx = 0.0;

		JLabel mainFontLabel =
			new JLabel(
				GlobalResourceLoader.getString(
					RESOURCE_PATH,
					"general",
					"main_font"));
		//fontPanel.add(mainFontLabel);
		layout.setConstraints(mainFontLabel, c);
		fontPanel.add(mainFontLabel);

		//mainFontPanel.add(Box.createRigidArea(new java.awt.Dimension(5, 0)));

		mainFontTextField = new JTextField(30);
		c.insets = new Insets(0, 10, 0, 0);
		c.gridx = 1;
		c.gridy = 1;
		c.weightx = 1.0;
		//c.gridwidth = GridBagConstraints.RELATIVE;
		c.gridwidth = 1;
		layout.setConstraints(mainFontTextField, c);
		fontPanel.add(mainFontTextField);

		//mainFontPanel.add(Box.createRigidArea(new java.awt.Dimension(5, 0)));

		mainFontButton =
			new JButton(
				GlobalResourceLoader.getString(
					RESOURCE_PATH,
					"general",
					"choose"));
		mainFontButton.addActionListener(this);
		c.gridx = 2;
		c.gridy = 1;
		c.weightx = 0.0;
		//c.gridwidth = GridBagConstraints.REMAINDER;
		c.gridwidth = 1;
		layout.setConstraints(mainFontButton, c);
		fontPanel.add(mainFontButton);
		//mainFontPanel.add(mainFontButton);

		//mainFontPanel.add(Box.createRigidArea(new java.awt.Dimension(5, 0)));

		//fontPanel.add(mainFontPanel);

		/*
		JPanel textFontPanel = new JPanel();
		textFontPanel.setLayout(new BoxLayout(textFontPanel, BoxLayout.X_AXIS));
		textFontPanel.add(Box.createRigidArea(new java.awt.Dimension(10, 0)));
		//textFontPanel.setAlignmentX(0);
		 * */

		JLabel textFontLabel =
			new JLabel(
				GlobalResourceLoader.getString(
					RESOURCE_PATH,
					"general",
					"text_font"));
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 2;
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = 1;
		c.weightx = 0.0;

		layout.setConstraints(textFontLabel, c);
		fontPanel.add(textFontLabel);

		//textFontPanel.add(Box.createRigidArea(new java.awt.Dimension(5, 0)));

		textFontTextField = new JTextField(30);
		c.gridx = 1;
		c.gridy = 2;
		c.weightx = 1.0;
		c.insets = new Insets(0, 10, 0, 0);
		//c.gridwidth = GridBagConstraints.RELATIVE;
		c.gridwidth = 1;
		layout.setConstraints(textFontTextField, c);
		fontPanel.add(textFontTextField);

		//textFontPanel.add(Box.createRigidArea(new java.awt.Dimension(5, 0)));

		textFontButton =
			new JButton(
				GlobalResourceLoader.getString(
					RESOURCE_PATH,
					"general",
					"choose"));
		textFontButton.addActionListener(this);
		c.gridx = 2;
		c.gridy = 2;
		c.weightx = 0.0;
		//c.gridwidth = GridBagConstraints.REMAINDER;
		c.gridwidth = 1;
		c.gridheight = GridBagConstraints.REMAINDER;
		layout.setConstraints(textFontButton, c);
		fontPanel.add(textFontButton);

		//textFontPanel.add(Box.createRigidArea(new java.awt.Dimension(10, 0)));

		//fontPanel.add(textFontPanel);

		add(fontPanel, BorderLayout.NORTH);

		JLabel restartLabel =
			new JLabel(
				"You have to restart for the changes to take effect.",
				SwingConstants.CENTER);
		add(restartLabel, BorderLayout.SOUTH);
	}

	public void actionPerformed(ActionEvent ev) {
		Object source = ev.getSource();
		if (source == mainFontButton) {
			FontSelectionDialog fontDialog = new FontSelectionDialog(null);
			fontDialog.showDialog();

			if (fontDialog.getStatus() == 0) {
				mainFont = fontDialog.getSelectedFont();
				mainFontTextField.setFont(mainFont);
				mainFontTextField.setText(mainFont.getFontName());
			}

		} else if (source == textFontButton) {
			FontSelectionDialog fontDialog = new FontSelectionDialog(null);
			fontDialog.showDialog();

			if (fontDialog.getStatus() == 0) {
				textFont = fontDialog.getSelectedFont();
				textFontTextField.setFont(textFont);
				textFontTextField.setText(textFont.getFontName());
			}
		} else if (source == overwriteCheckBox) {
                        boolean enabled = overwriteCheckBox.isSelected();
                        mainFontTextField.setEnabled(enabled);
                        mainFontButton.setEnabled(enabled);
                        textFontTextField.setEnabled(enabled);
                        textFontButton.setEnabled(enabled);
                }
	}
}
