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
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.columba.core.config.Config;
import org.columba.core.config.GuiItem;
import org.columba.core.gui.util.FontSelectionDialog;

public class FontPanel extends JPanel implements ActionListener
{
	private JLabel mainFontLabel;
	private JTextField mainFontTextField;
	private JButton mainFontButton;

	private JLabel textFontLabel;
	private JTextField textFontTextField;
	private JButton textFontButton;

	private Font mainFont;
	private Font textFont;

	public FontPanel()
	{
		initComponent();
	}

	public Font getMainFont()
	{
		return mainFontTextField.getFont();
	}

	public Font getTextFont()
	{
		return textFontTextField.getFont();
	}


	public void updateComponents( boolean b )
	{
		GuiItem item = Config.getOptionsConfig().getGuiItem();

		mainFont = item.getMainFont();
		textFont = item.getTextFont();

		if (b == true)
		{
			mainFontTextField.setFont(mainFont);
			mainFontTextField.setText(mainFont.getFontName());

			textFontTextField.setFont(textFont);
			textFontTextField.setText(textFont.getFontName());
		}
		else
		{
			item.set("textfont","name", getTextFont().getName());
			item.set("textfont","size", getTextFont().getSize());
			item.set("mainfont","name", getMainFont().getName());
			item.set("mainfont","size", getMainFont().getSize());
			
		}
	}

	protected void initComponent()
	{
		setLayout( new BorderLayout() );
		setBorder(BorderFactory.createEmptyBorder(12,12,11,11));

		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();

		JPanel fontPanel = new JPanel();
		fontPanel.setLayout( layout );

		/*
		JPanel mainFontPanel = new JPanel();
		mainFontPanel.setLayout(new BoxLayout(mainFontPanel, BoxLayout.X_AXIS));
		mainFontPanel.add(Box.createRigidArea(new java.awt.Dimension(5, 0)));
		//mainFontPanel.setAlignmentX(0);
		 * */

		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = 1;
		c.weightx = 0.0;

		mainFontLabel = new JLabel("MainFont:");
		fontPanel.add(mainFontLabel);
		layout.setConstraints( mainFontLabel, c );
		fontPanel.add( mainFontLabel );

		//mainFontPanel.add(Box.createRigidArea(new java.awt.Dimension(5, 0)));

		mainFontTextField = new JTextField(30);
		c.insets = new Insets(0,10,0,0);
		c.gridx = 1;
		c.weightx = 1.0;
		c.gridwidth = GridBagConstraints.RELATIVE;
		layout.setConstraints( mainFontTextField, c );
		fontPanel.add(mainFontTextField);


		//mainFontPanel.add(Box.createRigidArea(new java.awt.Dimension(5, 0)));

		mainFontButton = new JButton("Choose MainFont..");
		mainFontButton.setActionCommand("MAINFONT");
		mainFontButton.addActionListener(this);
		c.gridx = 2;
		c.weightx = 0.0;
		c.gridwidth = GridBagConstraints.REMAINDER;
		layout.setConstraints( mainFontButton, c );
		fontPanel.add( mainFontButton );
		//mainFontPanel.add(mainFontButton);

		//mainFontPanel.add(Box.createRigidArea(new java.awt.Dimension(5, 0)));

		//fontPanel.add(mainFontPanel);


		/*
		JPanel textFontPanel = new JPanel();
		textFontPanel.setLayout(new BoxLayout(textFontPanel, BoxLayout.X_AXIS));
		textFontPanel.add(Box.createRigidArea(new java.awt.Dimension(10, 0)));
		//textFontPanel.setAlignmentX(0);
		 * */

		textFontLabel = new JLabel("TextFont:");
		c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 1;
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = 1;
		c.weightx = 0.0;

		layout.setConstraints( textFontLabel, c );
		fontPanel.add(textFontLabel);

		//textFontPanel.add(Box.createRigidArea(new java.awt.Dimension(5, 0)));

		textFontTextField = new JTextField(30);
		c.gridx = 1;
		c.weightx = 1.0;
		c.insets = new Insets(0,10,0,0);
		c.gridwidth = GridBagConstraints.RELATIVE;
		layout.setConstraints( textFontTextField, c );
		fontPanel.add(textFontTextField);

		//textFontPanel.add(Box.createRigidArea(new java.awt.Dimension(5, 0)));

		textFontButton = new JButton("Choose TextFont..");
		textFontButton.setActionCommand("TEXTFONT");
		textFontButton.addActionListener(this);
		c.gridx = 2;
		c.weightx = 0.0;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.gridheight = GridBagConstraints.REMAINDER;
		layout.setConstraints( textFontButton, c );
		fontPanel.add(textFontButton);

		//textFontPanel.add(Box.createRigidArea(new java.awt.Dimension(10, 0)));

		//fontPanel.add(textFontPanel);

		add(fontPanel, BorderLayout.NORTH );
	}

	public void actionPerformed( ActionEvent ev )
	{
		String command = ev.getActionCommand();

		if (command.equals("MAINFONT"))
		{
			FontSelectionDialog fontDialog = new FontSelectionDialog(null);
			fontDialog.showDialog();

			if (fontDialog.getStatus() == 0)
			{
				mainFont = fontDialog.getSelectedFont();
				mainFontTextField.setFont(mainFont);
				mainFontTextField.setText(mainFont.getFontName());

			}

		}
		else if (command.equals("TEXTFONT"))
		{
			FontSelectionDialog fontDialog = new FontSelectionDialog(null);
			fontDialog.showDialog();

			if (fontDialog.getStatus() == 0)
			{
				textFont = fontDialog.getSelectedFont();
				textFontTextField.setFont(textFont);
				textFontTextField.setText(textFont.getFontName());

			}

		}
	}
}
