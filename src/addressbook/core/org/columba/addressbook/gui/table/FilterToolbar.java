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

package org.columba.addressbook.gui.table;

import org.columba.core.config.*;
import org.columba.core.gui.util.*;
import org.columba.core.gui.util.ImageLoader;

import org.columba.addressbook.gui.table.util.*;
import org.columba.core.gui.util.ImageLoader;

import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.util.*;

public class FilterToolbar extends JToolBar implements ActionListener
{

	public JButton searchButton;

	private JComboBox comboBox;
	private JTextField textField;

	private AddressbookTable table;

	private ResourceBundle toolbarLabels;

	public FilterToolbar(AddressbookTable table)
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

		//addSeparator();

		//HeaderTableItem list = mainInterface.config.getOptionsConfig().getHeaderTableItem();
		HeaderTableItem list = table.getHeaderTableItem();
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