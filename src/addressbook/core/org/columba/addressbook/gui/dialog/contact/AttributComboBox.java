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

package org.columba.addressbook.gui.dialog.contact;

import javax.swing.*;

import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.text.*;

import org.columba.core.config.AdapterNode;

import org.columba.addressbook.folder.*;
import org.columba.core.gui.util.*;

import java.util.*;

/**
 * @version 	1.0
 * @author
 */
public class AttributComboBox
	extends JPanel
	implements ActionListener, DocumentListener
{
	//JLabel label;
	JButton button;
	JPopupMenu menu;

	Vector list;
	String selection;

	Hashtable table;

	JTextComponent textField;

	String name;

	Vector menuList;

	public AttributComboBox(String name, Vector list, JTextComponent textField)
	{
		super();

		this.name = name;
		this.textField = textField;
		textField.getDocument().addDocumentListener(this);

		table = new Hashtable();

		this.list = list;

		menuList = new Vector();

		initComponents();
	}

	public void setEnabled(boolean b)
	{
		button.setEnabled(false);
		//label.setEnabled(false);
	}

	public Hashtable getResultTable()
	{
		return table;
	}

	public void setResultTable(Hashtable table)
	{
		this.table = table;
	}

	public void updateComponents(ContactCard card, boolean b)
	{

		if (b == true)
		{
			for (int i = 0; i < list.size(); i++)
			{
				String str = (String) list.get(i);
				table.put(str, card.get(name, str));

			}

			for (int i = 0; i < menuList.size(); i++)
			{
				JMenuItem item = (JMenuItem) menuList.get(i);
				String s = (String) table.get(item.getActionCommand());
				if ((s != null))
				{
					if (s.length() != 0)
						item.setSelected(true);
					else
						item.setSelected(false);
				}
				else
					item.setSelected(false);
			}

			String s = (String) table.get((String) list.get(0));
			if (s != null)
				textField.setText(s);
			else
				textField.setText(null);
		}
		else
		{

			for (Enumeration e = table.keys(); e.hasMoreElements();)
			{
				String key = (String) e.nextElement();
				String value = (String) table.get(key);
				card.set(name, key, value);

			}
		}

	}

	protected void initComponents()
	{
		setLayout(new BorderLayout());

		/*
		label = new JLabel(name + " (" + (String) list.get(0) + ")");
		add(label, BorderLayout.WEST);

		Component box = Box.createHorizontalStrut(20);
		add(box, BorderLayout.CENTER);
		*/

		//button = new ArrowButton(0);
		button = new JButton( name + " (" + (String) list.get(0) + "):", ImageLoader.getSmallImageIcon("stock_down-16.png") );
		button.setActionCommand("BUTTON");
		button.setMargin( new Insets(2,5,2,0) );
		button.setHorizontalTextPosition( SwingConstants.LEADING );
		//button.setIconTextGap(20);
		button.addActionListener(this);
		add(button, BorderLayout.WEST);

		menu = new JPopupMenu();

		selection = (String) list.get(0);

		for (int i = 0; i < list.size(); i++)
		{
			JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem((String) list.get(i));
			menuItem.setActionCommand((String) list.get(i));
			menuItem.addActionListener(this);
			menu.add(menuItem);
			menuList.add(menuItem);
		}
	}

	public void actionPerformed(ActionEvent event)
	{
		String action = event.getActionCommand();

		if (action.equals("BUTTON"))
		{

			for (int i = 0; i < menuList.size(); i++)
			{
				JMenuItem item = (JMenuItem) menuList.get(i);
				String s = (String) table.get(item.getActionCommand());
				if ((s != null))
				{
					if (s.length() != 0)
						item.setSelected(true);
					else
						item.setSelected(false);
				}
				else
					item.setSelected(false);
			}
			//menu.show(button, button.getX(), button.getY());
			menu.show(button, button.getWidth(),0 );
		}
		else
		{

			button.setText(name + " (" + action + "):");
			selection = action;

			String s = (String) table.get(action);
			if (s != null)
				textField.setText(s);
			else
				textField.setText(null);
		}
	}

	public String getSelected()
	{
		return selection;
	}

	public void insertUpdate(DocumentEvent e)
	{
		updateDoc(e.getDocument());
	}

	public void removeUpdate(DocumentEvent e)
	{
		updateDoc(e.getDocument());
	}

	public void changedUpdate(DocumentEvent e)
	{
		updateDoc(e.getDocument());
	}

	public void updateDoc(Document doc)
	{
		String s = getSelected();

		table.put(s, textField.getText());

	}

	

}