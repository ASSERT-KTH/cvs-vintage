// The contents of this file are subject to the Mozilla Public License Version
// 1.1
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
//The Initial Developers of the Original Code are Frederik Dietz and Timo
// Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.columba.core.gui.menu;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.logging.Logger;

import javax.swing.JMenuBar;
import javax.swing.JMenuItem;

import org.columba.core.gui.action.AbstractColumbaAction;


/**
 * Extendable menubar.
 * <p>
 * Simply example showing how-to add a new action to the menu:
 * <pre>
 *	ColumbaMenu menu = (ColumbaMenu) frame.getJMenuBar();
 *	menu.addMenuItem("my_reply_action_id", new ReplyAction(this),
 *		              ColumbaMenu.MENU_VIEW, ColumbaMenu.PLACEHOLDER_BOTTOM);
 * </pre>
 * @author fdietz
 *
 */
public class ExtendableMenuBar extends JMenuBar {

	private static final Logger LOG = Logger
			.getLogger("org.columba.core.gui.menu");

	private Hashtable<String, ExtendableMenu> map = new Hashtable<String, ExtendableMenu>();

	
	public ExtendableMenuBar() {
		super();
	}
	
	public void add(ExtendableMenu menu) {
		if ( menu == null ) throw new IllegalArgumentException("menu == null");
		Enumeration<ExtendableMenu> e = menu.getSubmenuEnumeration();
		while (e.hasMoreElements()) {
			ExtendableMenu submenu =  e.nextElement();
			map.put(submenu.getId(), submenu);
		}

		super.add(menu);
	}
	
	public void insert(ExtendableMenu menu) {
		if ( menu == null ) throw new IllegalArgumentException("menu == null");
		
		Enumeration<ExtendableMenu> e = menu.getSubmenuEnumeration();
		while (e.hasMoreElements()) {
			ExtendableMenu submenu =  e.nextElement();
			map.put(submenu.getId(), submenu);
		}
		
		// we insert new menus between the "Edit" and the "Utilities, Help" menu
		super.add(menu, getMenuCount()-2);
	}

	public boolean exists(String menuId) {
		if ( menuId == null ) throw new IllegalArgumentException("menuId == null");
		
		if ( map.containsKey(menuId)) return true;
		
		return false;
	}
	
	public ExtendableMenu getMenu(String menuId) {
		if ( menuId == null ) throw new IllegalArgumentException("menuId == null");
		
		if (map.containsKey(menuId) == false) 
			throw new IllegalArgumentException("no menu for "+menuId+" found");
		

		ExtendableMenu menu = (ExtendableMenu) map.get(menuId);

		return menu;
	}

	public void insertMenuItem(String menuId, String placeholderId,
			JMenuItem menuItem) {
		if ( menuId == null ) throw new IllegalArgumentException("menuId == null");
		
		if (map.containsKey(menuId) == false)
			throw new IllegalArgumentException("no menu with id " + menuId
					+ " found");

		ExtendableMenu menu = (ExtendableMenu) map.get(menuId);
		menu.insert(menuItem, placeholderId);
	}

	public void insertAction(String menuId, String placeholderId,
			AbstractColumbaAction action) {
		if ( menuId == null ) throw new IllegalArgumentException("menuId == null");
		
		if (map.containsKey(menuId) == false)
			throw new IllegalArgumentException("no menu with id " + menuId
					+ " found");

		ExtendableMenu menu = (ExtendableMenu) map.get(menuId);
		menu.insert(action, placeholderId);
    /*TODO before inserting, find out if there's already a menu item
     * with the same action command. if so, replace it, otherwise insert new
     */
	}

}
