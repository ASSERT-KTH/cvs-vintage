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

package org.columba.addressbook.gui.table;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ResourceBundle;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JTextField;
import javax.swing.JToolBar;

import org.columba.addressbook.gui.table.util.TableModelFilteredView;

public class FilterToolbar extends JToolBar implements ActionListener
{

	public JButton searchButton;

	private JComboBox comboBox;
	private JTextField textField;

	private TableView table;

	private ResourceBundle toolbarLabels;

	public FilterToolbar(TableView table)
	{
		super();

		this.table = table;

		//setMargin( new Insets(0,0,0,0) );

		addCButtons();

		setBorderPainted(false);
		setFloatable(false);

	}

	public void addCButtons()
	{

		/*
		//addSeparator();

		//HeaderTableItem list = mainInterface.config.getOptionsConfig().getHeaderTableItem();
		TableItem list = table.getHeaderTableItem();
		comboBox = new JComboBox();
		String name;

		for (int i = 0; i < list.count(); i++)
		{
			name = list.getName(i);
			boolean enabled = list.getEnabled(i);

			if (enabled == false)
				continue;

			if (!(name.equalsIgnoreCase("type")))
				comboBox.addItem(name);

		}

		//comboBox.setMaximumSize( new java.awt.Dimension( 100, 25 ) );
		comboBox.setSelectedIndex(0);
		comboBox.addActionListener(this);
		comboBox.setActionCommand("COMBO");
		add(comboBox);

		addSeparator();
		
		JLabel label = new JLabel("contains");
		add(label);
		
		addSeparator();

		textField = new JTextField(20);
		textField.addActionListener(this);
		textField.setActionCommand("TEXTFIELD");
		textField.addFocusListener(new FocusListener()
		{
			public void focusGained(FocusEvent e)
			{
			}

			public void focusLost(FocusEvent e)
			{
				TableModelFilteredView model = table.getTableModelFilteredView();
				try
				{
					model.setPatternString(textField.getText());
				}
				catch (Exception ex)
				{
					ex.printStackTrace();
				}
			}
		});
		//textField.setMaximumSize( new java.awt.Dimension( 600, 25 ) );

		add(textField);

		addSeparator();

		searchButton = new JButton("Search..");
		//searchButton.setMaximumSize( new java.awt.Dimension( 150, 25 ) );
		//attachmentButton.setSelected( false );
		searchButton.addActionListener(this);
		searchButton.setActionCommand("OK");
		add(searchButton);

		addSeparator();
			*/
	}

	public void update()
	{
		table.getTableModel().update();
	}

	public void actionPerformed(ActionEvent e)
	{
		String action = e.getActionCommand();

		try
		{

			//TableModelFilteredView model = mainInterface.headerTableViewer.getHeaderTable().getTableModelFilteredView();
			TableModelFilteredView model = table.getTableModelFilteredView();

			if (action.equals("COMBO"))
			{
				JComboBox cb = (JComboBox) e.getSource();
				model.setPatternItem((String) cb.getSelectedItem());
			}
			else if (action.equals("TEXTFIELD"))
			{
				model.setPatternString(textField.getText());
				//HeaderTableModel tableModel = mainInterface.headerTableViewer.getHeaderTable().getHeaderTableModel();
				// tableModel.update();
				//mainInterface.headerTableViewer.getHeaderTable().getIndexedTableModel().update();
				//update();
			}
			else if (action.equals("OK"))
			{
				if (model == null)
				{
					System.out.println("model is null");
					return;
				}
				
					//HeaderTableModel tableModel = mainInterface.headerTableViewer.getHeaderTable().getHeaderTableModel();
					//tableModel.update();
					model.setDataFiltering(true);
					//mainInterface.headerTableViewer.getHeaderTable().getIndexedTableModel().update();
					update();
				

			}
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}

	}

}