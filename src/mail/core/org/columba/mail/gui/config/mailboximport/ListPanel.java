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
package org.columba.mail.gui.config.mailboximport;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.columba.core.gui.util.MultiLineLabel;
import org.columba.core.gui.util.wizard.DefaultWizardPanel;
import org.columba.core.main.MainInterface;
import org.columba.mail.plugin.ImportPluginHandler;

/**
 * @version 	1.0
 * @author
 */
public class ListPanel
	extends DefaultWizardPanel
	implements ListSelectionListener
{
	private JList list;
	private JLabel label;
	private MultiLineLabel descriptionLabel;
	
	/*
	private String[] importerList;
	private String[] name;
	private String[] description;
	*/
	
	public ListPanel(
		JDialog dialog,
		ActionListener listener,
		String title,
		String description,
		ImageIcon icon)
	{
		super(dialog, listener, title, description, icon);

	}

	public ListPanel(
		JDialog dialog,
		ActionListener listener,
		String title,
		String description,
		ImageIcon icon,
		boolean b)
	{
		super(dialog, listener, title, description, icon);
		
	}

	public String getSelection()
	{
		int index = list.getSelectedIndex();
		
		//String key = importerList[index];
		
		
		
		//return key;
		
		return ""; 
	}

	protected JPanel createPanel(ActionListener listener)
	{
		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createEmptyBorder(30, 30, 20, 30));
		//panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		panel.setLayout(new BorderLayout());

		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BorderLayout());
		topPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 30, 0));

		MultiLineLabel label =
			new MultiLineLabel("Please choose the mailbox format you want to import to Columba.");

		topPanel.add(label, BorderLayout.CENTER);
		//panel.add(label);

		//panel.add(Box.createRigidArea(new java.awt.Dimension(0, 40)));

		panel.add(topPanel, BorderLayout.NORTH);

		JPanel middlePanel = new JPanel();
		middlePanel.setAlignmentX(1);
		GridBagLayout layout = new GridBagLayout();
		middlePanel.setLayout(layout);

		generateList();
		list = new JList();

		JScrollPane scrollPane = new JScrollPane(list);
		//scrollPane.setPreferredSize( new Dimension(200,200) );
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.NORTHWEST;
		c.gridx = 0;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 0.4;
		//c.gridwidth = GridBagConstraints.RELATIVE;
		c.weighty = 1.0;
		layout.setConstraints(scrollPane, c);
		middlePanel.add(scrollPane);

		descriptionLabel = new MultiLineLabel("description");
		descriptionLabel.setWrapStyleWord(true);
		descriptionLabel.setLineWrap(true);
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 0.6;
		c.gridx = 1;
		c.anchor = GridBagConstraints.NORTHWEST;
		c.insets = new Insets(0, 10, 0, 0);
		JScrollPane scrollPane2 = new JScrollPane(descriptionLabel);
		//scrollPane2.setPreferredSize( new Dimension(200,100) );
		layout.setConstraints(scrollPane2, c);
		middlePanel.add(scrollPane2);

		panel.add(middlePanel, BorderLayout.CENTER);

		list.addListSelectionListener(this);
		list.setSelectedIndex(0);

		return panel;
	}

	protected void generateList()
	{
		ImportPluginHandler pluginHandler =
					(ImportPluginHandler) MainInterface.pluginManager.getHandler(
						"org.columba.mail.importer");
			
		String[] names = pluginHandler.getPluginIdList();
		
		list = new JList(names);
					
		/*
		importerList = new String[] { "MBOX",
						"PegasusMail",
						"Mozilla",
						"Evolution"};
		
	

		name = new String[ importerList.length ];
		description = new String[ importerList.length ];
		
		for ( int i=0; i<importerList.length; i++ )
		{
			name[i] = MailResourceLoader.getString("dialog", "mailboximport",importerList[i]+"_name");
			description[i] = MailResourceLoader.getString("dialog","mailboximport", importerList[i]+"_description");
		}
		*/
		
	}

	public void valueChanged(ListSelectionEvent event)
	{
		
		
		int index = list.getSelectedIndex();
		
		/*
		String str = description[index];
		
		descriptionLabel.setText( str );
		*/
	}

}
