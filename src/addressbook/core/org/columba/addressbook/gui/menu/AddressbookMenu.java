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

package org.columba.addressbook.gui.menu;

import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JRadioButtonMenuItem;

import org.columba.addressbook.main.AddressbookInterface;
import org.columba.addressbook.util.AddressbookResourceLoader;
import org.columba.core.gui.util.CMenuItem;
import org.columba.core.gui.util.ImageLoader;

public class AddressbookMenu
{
	JMenuBar menuBar;

	private AddressbookInterface addressbookInterface;
	private MouseAdapter handler;

	private JMenu fetchMessageSubmenu;
	private JMenu manageSubmenu;
	private JMenu sortSubMenu;

	public AddressbookMenu(AddressbookInterface i)
	{
		this.addressbookInterface = i;

		init();
	}

	// create the menu
	private void init()
	{
		//handler = mainInterface.statusBar.getHandler();

	
		JMenu menu, subMenu;
		JMenuItem menuItem;
		JRadioButtonMenuItem rbMenuItem;
		ButtonGroup group;

		menuBar = new JMenuBar();

		menu = new JMenu(AddressbookResourceLoader.getString("menu","mainframe", "menu_file"));
		menu.setMnemonic(KeyEvent.VK_F);
		menuBar.add(menu);

		menuItem = new JMenuItem(addressbookInterface.actionListener.addvcardAction);
		menuItem.addMouseListener(handler);
		menu.add(menuItem);
		
		menuItem = new JMenuItem(addressbookInterface.actionListener.addContactAction);
		menuItem.addMouseListener(handler);
		menu.add(menuItem);

		menuItem = new JMenuItem(addressbookInterface.actionListener.addGroupAction);
		menuItem.addMouseListener(handler);
		menu.add(menuItem);

		menuItem =
			new JMenuItem(addressbookInterface.actionListener.addAddressbookAction);

		menu.add(menuItem);

		

		menu.addSeparator();

		menuItem = new JMenuItem(addressbookInterface.actionListener.removeAction);
		menuItem.addMouseListener(handler);
		menu.add(menuItem);
		
		menuItem = new JMenuItem("Remove Addressbook");
		menuItem.setEnabled(false);
		menu.add(menuItem);

		menu.addSeparator();
		
		

		menuItem = new JMenuItem(addressbookInterface.actionListener.propertiesAction);
		menuItem.addMouseListener(handler);
		menu.add(menuItem);
		
		menu.addSeparator();

		menuItem = new CMenuItem(addressbookInterface.actionListener.closeAction);
		menuItem.addMouseListener(handler);
		menu.add(menuItem);

		menu = new JMenu(AddressbookResourceLoader.getString("menu","mainframe", "menu_edit"));
		menu.setMnemonic(KeyEvent.VK_E);
		menuBar.add(menu);

		menuItem = new CMenuItem(addressbookInterface.actionListener.cutAction);
		menuItem.addMouseListener(handler);
		menu.add(menuItem);

		menuItem = new CMenuItem(addressbookInterface.actionListener.copyAction);
		menuItem.addMouseListener(handler);
		menu.add(menuItem);

		menuItem = new CMenuItem(addressbookInterface.actionListener.pasteAction);
		menuItem.addMouseListener(handler);
		menu.add(menuItem);

		menuItem = new CMenuItem(addressbookInterface.actionListener.deleteAction);
		menuItem.addMouseListener(handler);
		menu.add(menuItem);

		menuItem = new CMenuItem(addressbookInterface.actionListener.selectAllAction);
		menuItem.addMouseListener(handler);
		menu.add(menuItem);

		menu.addSeparator();

		menuItem = new JMenuItem("Search ...");
		menuItem.setEnabled(false);
		menu.add(menuItem);

		

		menu = new JMenu(AddressbookResourceLoader.getString("menu","mainframe", "menu_view"));
		menu.setMnemonic(KeyEvent.VK_V);
		menuBar.add(menu);

		sortSubMenu = new JMenu("Sort View");
		sortSubMenu.setEnabled(false);
		menu.add(sortSubMenu);
		
		sortSubMenu = new JMenu("Filter View");
		sortSubMenu.setEnabled(false);
		menu.add(sortSubMenu);

		menu = new JMenu("Utilities");
		menu.setMnemonic('U');
		menuBar.add(menu);
		
		menuItem = new CMenuItem(addressbookInterface.actionListener.addressbookImportAction);
		menuItem.addMouseListener(handler);
		menu.add(menuItem);
		
		menu = new JMenu("Preferences");
		menu.setMnemonic('P');
		menuBar.add(menu);

		

		menuItem =
			new JMenuItem(
				AddressbookResourceLoader.getString("menu","mainframe", "menu_edit_preferences"),
				ImageLoader.getImageIcon("stock_preferences-16.png"));
		menuItem.setMnemonic(KeyEvent.VK_P);
		menuItem.setEnabled(false);
		menu.add(menuItem);

		menu = new JMenu("Help");
		menu.setMnemonic('H');
		menuBar.add(menu);

		menuItem =
			new JMenuItem(
				"About",
				ImageLoader.getImageIcon("stock_about-16.png"));

		menuItem.setActionCommand("ABOUT");
		menuItem.setEnabled(false);
		menu.add(menuItem);
		menuItem =
			new JMenuItem(
				"Help",
				ImageLoader.getImageIcon("stock_help_16.png"));
		menuItem.setEnabled(false);
		menu.add(menuItem);
		
	}

	public JMenuBar getMenuBar()
	{
		return menuBar;

	}

	public void updateSortMenu()
	{
	}

}