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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;

import org.columba.core.config.Config;
import org.columba.core.config.ConfigPath;
import org.columba.core.config.ThemeItem;
import org.columba.core.gui.util.NotifyDialog;
import org.columba.core.io.DiskIO;

public class ThemePanel extends JPanel implements ActionListener
{

	private JButton themeButton;

	private JRadioButton nativeButton;
	private JRadioButton metalButton;
	private JRadioButton columbaButton;
	private ButtonGroup buttonGroup;

	private JComboBox selectorComboBox;
	private JComboBox iconsetComboBox;
	private JComboBox pulsatorComboBox;

	private JButton installiconsetButton;
	private JButton installpulsatorButton;

	private int status = -1;
	private int theme = -1;

	private JFrame frame;

	//private ThemeItem item;

	public ThemePanel()
	{

		init();
	}

	protected void init()
	{

		//JPanel centerPanel = new JPanel();
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(12,12,11,11));
		JPanel themePanel = new JPanel();
		themePanel.setBorder( BorderFactory.createEmptyBorder(0,0,10,0) );
		themePanel.setLayout(new BoxLayout(themePanel, BoxLayout.X_AXIS));
		//LOCALIZE
		JLabel themeLabel = new JLabel("Choose Theme:");
		themePanel.add(themeLabel);
		themePanel.add(Box.createHorizontalStrut(5));
		Vector list = new Vector();
		list.add("no theme");
		list.add("Java Look And Feel");
		list.add("Thin Columba");
		//list.add("Contrast Columba");
		list.add("Windows Look And Feel");
		list.add("MacOS Look And Feel");
		list.add("Motif Look And Feel");
		selectorComboBox = new JComboBox(list);
		selectorComboBox.setActionCommand("THEME");
		selectorComboBox.addActionListener(this);
		themePanel.add(selectorComboBox);
		themePanel.add(Box.createHorizontalGlue());
		//LOCALIZE
		themeButton = new JButton("Advanced Options..");
		themeButton.setEnabled(false);
		themePanel.add(themeButton);
		themePanel.add(Box.createHorizontalGlue());
		add(themePanel, BorderLayout.NORTH);

		JPanel iconPanel = new JPanel();
		GridBagLayout layout = new GridBagLayout();
		GridBagConstraints c = new GridBagConstraints();
		iconPanel.setLayout(layout);
		//LOCALIZE
		JLabel iconsetLabel = new JLabel("Iconset:");
		iconsetLabel.setEnabled(false);
		c.anchor = GridBagConstraints.WEST;
		c.gridx = 0;
		c.gridy = 0;
		c.insets = new Insets(0,0,0,0);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = 1;
		c.weightx = 0.0;

		layout.setConstraints(iconsetLabel, c);
		iconPanel.add(iconsetLabel);

		iconsetComboBox = new JComboBox();
		iconsetComboBox.setEnabled(false);
		iconsetLabel.setLabelFor(iconsetComboBox);
		c.gridx = 1;
		c.weightx = 1.0;
		c.insets = new Insets(0, 10, 0, 0);
		c.gridwidth = GridBagConstraints.RELATIVE;
		layout.setConstraints(iconsetComboBox, c);
		iconPanel.add(iconsetComboBox);
		//LOCALIZE
		installiconsetButton = new JButton("Install new Iconset..");
		installiconsetButton.setEnabled(false);
		installiconsetButton.setActionCommand("ICONSET");
		installiconsetButton.addActionListener(this);
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.gridx = 2;
		layout.setConstraints(installiconsetButton, c);
		iconPanel.add(installiconsetButton);

		JLabel pulsatorLabel = new JLabel("Pulsator:");
		pulsatorLabel.setEnabled(false);
		c.gridx = 0;
		c.gridy = 1;
		c.insets = new Insets(0,0,0,0);
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = 1;
		c.weightx = 0.0;

		layout.setConstraints(pulsatorLabel, c);
		iconPanel.add(pulsatorLabel);

		pulsatorComboBox = new JComboBox();
		pulsatorComboBox.setEnabled(false);
		pulsatorLabel.setLabelFor(pulsatorComboBox);
		c.gridx = 1;
		c.weightx = 1.0;
		c.insets = new Insets(0, 10, 0, 0);
		c.gridwidth = GridBagConstraints.RELATIVE;

		layout.setConstraints(pulsatorComboBox, c);
		iconPanel.add(pulsatorComboBox);
		//LOCALIZE
		installpulsatorButton = new JButton("Install new Pulsator..");
		installpulsatorButton.setActionCommand("PULSATOR");
		installpulsatorButton.setEnabled(false);
		installpulsatorButton.addActionListener(this);
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.gridx = 2;
		layout.setConstraints(installpulsatorButton, c);
		iconPanel.add(installpulsatorButton);
		add(iconPanel, BorderLayout.CENTER);
		//LOCALIZE
		JLabel restartLabel = new JLabel("You have to restart for the changes to take effect.",SwingConstants.CENTER);
		add( restartLabel, BorderLayout.SOUTH );

	}

	public void updateComponents(boolean b)
	{
		// FIXME
		/*
		ThemeItem item = Config.getOptionsConfig().getThemeItem();

		theme = item.getTheme();

		if (b == true)
		{
			//java.util.Vector v = new java.util.Vector();
			iconsetComboBox.removeAllItems();
			iconsetComboBox.addItem("Default");
			//v.add("Default");
			File iconsetDirectory =
				new File(ConfigPath.getConfigDirectory(), "iconsets");

			if (iconsetDirectory.exists())
			{
				File[] fileList = iconsetDirectory.listFiles();
				for (int i = 0; i < fileList.length; i++)
				{
					File file = fileList[i];
					String name = file.getName();
					int index = name.toLowerCase().indexOf(".jar");
					if (index != -1)
					{
						iconsetComboBox.addItem(name.substring(0, index));
						//v.add(name.substring(0, index));
					}
				}
			}

			//v = new java.util.Vector();
			//v.add("Default");
			pulsatorComboBox.removeAllItems();
			pulsatorComboBox.addItem("Default");
			File pulsatorDirectory =
				new File(ConfigPath.getConfigDirectory(), "pulsators");

			if (pulsatorDirectory.exists())
			{
				File[] pulsatorList = pulsatorDirectory.listFiles();
				for (int i = 0; i < pulsatorList.length; i++)
				{
					File file = pulsatorList[i];
					String name = file.getName();
					int index = name.toLowerCase().indexOf(".jar");
					if (index != -1)
					{
						//v.add(name.substring(0, index));
						pulsatorComboBox.addItem(name.substring(0, index));
					}
				}
			}

			iconsetComboBox.setSelectedItem(item.getIconset());

			pulsatorComboBox.setSelectedItem(item.getPulsator());

			selectorComboBox.setSelectedIndex(theme);
		}
		else
		{
			item.setPulsator((String) pulsatorComboBox.getSelectedItem());
			item.setIconset((String) iconsetComboBox.getSelectedItem());

			item.setTheme(getTheme());

		}
		*/
	}

	public int getStatus()
	{
		return status;
	}

	public int getTheme()
	{
		return selectorComboBox.getSelectedIndex();
	}

	public void actionPerformed(ActionEvent e)
	{
		String command;

		command = e.getActionCommand();

		if (command.equals("THEME"))
		{
			int index = selectorComboBox.getSelectedIndex();
			themeButton.setEnabled(false);
			/*
			if ((index == 2) || (index == 3))
			{
				themeButton.setEnabled(true);
			}
			else
				themeButton.setEnabled(false);
			*/
		}
		else if (command.equals("ICONSET"))
		{
			JFileChooser fc = new JFileChooser();
			int returnVal = fc.showOpenDialog(this);

			if (returnVal == JFileChooser.APPROVE_OPTION)
			{
				File file = fc.getSelectedFile();

				File iconsetDirectory =
					new File(ConfigPath.getConfigDirectory(), "iconsets");

                DiskIO.ensureDirectory( iconsetDirectory );
				File destFile = new File(iconsetDirectory, file.getName());

				try
				{
					DiskIO.copyFile( file, destFile );
				}
				catch (Exception ex)
				{
					ex.printStackTrace();

					NotifyDialog dialog = new NotifyDialog();
					dialog.showDialog(ex);
				}

				updateComponents(true);
			}
		}
		else if (command.equals("PULSATOR"))
		{
			JFileChooser fc = new JFileChooser();
			int returnVal = fc.showOpenDialog(this);

			if (returnVal == JFileChooser.APPROVE_OPTION)
			{
				File file = fc.getSelectedFile();

				File iconsetDirectory =
					new File(ConfigPath.getConfigDirectory(), "pulsators");

                DiskIO.ensureDirectory( iconsetDirectory );
				File destFile = new File(iconsetDirectory, file.getName());

				try
				{
					DiskIO.copyFile( file, destFile );
				}
				catch (Exception ex)
				{
					ex.printStackTrace();

					NotifyDialog dialog = new NotifyDialog();
					dialog.showDialog(ex);
				}

				updateComponents(true);
			}
		}

	}

}