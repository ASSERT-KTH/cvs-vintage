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

package org.columba.mail.gui.header.util;

import org.columba.mail.gui.header.*;
import org.columba.mail.gui.message.*;
import org.columba.mail.gui.util.*;
import org.columba.addressbook.gui.*;

import org.columba.mail.config.*;
import org.columba.core.gui.util.*;
import org.columba.main.*;
import org.columba.mail.folder.*;

import org.columba.addressbook.parser.*;
import org.columba.mail.gui.composer.*;

import javax.swing.tree.*;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;

import java.awt.*;
import java.awt.event.*;

import java.util.StringTokenizer;

public class HeaderFieldTree
	extends JPanel
	implements MouseListener, ActionListener
{
	private String s;
	private DefaultTreeModel model;

	private JTree tree;
	private DefaultMutableTreeNode rootNode;

	//private HeaderView messageHeader;

	private JScrollPane scrollPane;

	private JPopupMenu popup;
	private JMenuItem menuItem;

	private int size;

	private String contact;

	public HeaderFieldTree(String s)
	{

		super();
		this.s = s;
		parse(s);
		addMouseListener(this);
		setBorder(null);
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

	}

	protected void parse(String str)
	{
		Vector v = ListParser.parseString(str);

		if (v.size() > 1)
		{
			String address = (String) v.get(0);

			AddressLabel jLabel = new AddressLabel(address);

			jLabel.setIcon(ImageLoader.getSmallImageIcon("stock_down-16.png"));
			add(jLabel);
		}

		/*
		int index = str.indexOf(",");

		if ( index != -1 )
		{
		    String label = str.substring( 0, index );
		    label = label.trim();
		    label = label+", ..";
		    AddressLabel jLabel = new AddressLabel(
		                                        mainInterface,
		                                        label );

		    jLabel.setIcon( ImageLoader.getImageIcon("","TipOfTheDay16") );
		    add( jLabel );
		}
		*/

	}

	public void mousePressed(MouseEvent e)
	{
	}

	public void mouseReleased(MouseEvent e)
	{

	}

	public void mouseEntered(MouseEvent e)
	{

	}

	public void mouseExited(MouseEvent e)
	{

	}

	public void mouseClicked(MouseEvent e)
	{
		System.out.println("mouse clicked");

		popup = createMenu();

		Point point = e.getPoint();

		popup.show(e.getComponent(), 0,0);
	}

	protected JPopupMenu createMenu()
	{
		String address = s;

		JPopupMenu popup = new JPopupMenu();
		menuItem = new JMenuItem("Reply to All");
		menuItem.addActionListener(this);
		menuItem.setActionCommand("REPLY_ALL");
		menuItem.setEnabled(false);
		popup.add(menuItem);
		menuItem = new JMenuItem("Add all Contacts to Addressbook");
		menuItem.addActionListener(this);
		menuItem.setActionCommand("CONTACT_ALL");
		menuItem.setEnabled(false);
		popup.add(menuItem);
		popup.addSeparator();

		Vector v = ListParser.parseString(s);
		JMenu pop2 = null;
		JMenu pop3 = null;
		JMenu selected = null;
		int counter = 0;
		boolean p = false;
		for (int i = 0; i < v.size(); i++)
		{
			String str = (String) v.get(i);
			System.out.println("count:" + str);

			if (p == false)
				addMenuItem(popup, str);
			else
				addMenuItem(selected, str);

			counter++;

			if (counter > 10)
			{
				counter = 0;
				p = true;

				if (pop2 == null)
				{
					pop2 = new JMenu("more..");
					System.out.println("added pop2");
					if (selected == null)
						popup.add(pop2);
					else
						selected.add(pop2);

					selected = pop2;
					pop3 = null;
				}

				else
				{
					pop3 = new JMenu("more..");
					System.out.println("added pop3");
					if (selected == null)
						popup.add(pop3);
					else
						selected.add(pop3);

					selected = pop3;
					pop2 = null;
				}

			}
		}

		return popup;
	}

	protected void addMenuItem(JMenu pop, String str)
	{
		menuItem = new JMenuItem(str.trim());
		menuItem.addActionListener(this);
		pop.add(menuItem);
	}

	protected void addMenuItem(JPopupMenu pop, String str)
	{
		menuItem = new JMenuItem(str.trim());
		menuItem.addActionListener(this);
		pop.add(menuItem);
	}

	public void setContact(String s)
	{
		contact = s;
	}

	public void actionPerformed(ActionEvent e)
	{

		String action = e.getActionCommand();

		if (action.equals("CONTACT"))
		{
			URLController controller = new URLController();
			controller.contact(contact);

		}
		else if (action.equals("COMPOSE"))
		{
			URLController controller = new URLController();
			controller.compose(contact);

		}
		else
		{
			System.out.println("contact: " + action);
			setContact(action);
			popup.setVisible(false);

			URLController controller = new URLController();
			controller.setAddress(action);
			popup = controller.createContactMenu(action);
			popup.show(this, 0, 0);

		}

	}
}