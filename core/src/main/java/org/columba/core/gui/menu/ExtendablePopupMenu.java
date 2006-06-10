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

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;


public class ExtendablePopupMenu extends JPopupMenu {

	private MenuModel model;

	private Hashtable map = new Hashtable();

	private String id;

	public ExtendablePopupMenu(String id, String label) {
		super(label);
		this.id = id;

		model = new MenuModel(id);

		map.put(id, this);
	}

	public ExtendablePopupMenu(String id) {
		super();

		this.id = id;

		model = new MenuModel(id);

		map.put(id, this);
	}

	public MenuModel getMenuModel() {
		return model;
	}

	/**
	 * @see javax.swing.JPopupMenu#add(javax.swing.Action)
	 */
	public JMenuItem add(Action action) {
		model.add(action);

		return super.add(action);
	}

	/**
	 * @see javax.swing.JPopupMenu#add(javax.swing.JMenuItem)
	 */
	public JMenuItem add(JMenuItem menuItem) {
		model.add(menuItem);

		return super.add(menuItem);
	}

	/**
	 * @see javax.swing.JPopupMenu#addSeparator()
	 */
	public void addSeparator() {
		model.addSeparator();

		super.addSeparator();
	}

	/**
	 * @see javax.swing.JPopupMenu#insert(javax.swing.Action, int)
	 */
	public void insert(Action action, int pos) {
		model.insert(action, pos);

		super.insert(action, pos);
	}

	public void addPlaceholder(String placeholderId) {
		model.appendPlaceholder(placeholderId);
	}

	public void insertPlaceholder(String placeholderId, int pos) {
		model.insertPlaceholder(placeholderId, pos);
	}

	public void insert(Action action, String placeholderId) {
		int index = model.insert(action, placeholderId);
		super.insert(action, index);
	}

	public void insert(JMenuItem menuItem, String placeholderId) {
		int index = model.insert(menuItem, placeholderId);
		super.insert(menuItem, index);
	}

	public void insertSeparator(String placeholderId) {
		int index = model.insertSeparator(placeholderId);
		super.insert(new JSeparator(), index);
	}

	public void add(ExtendableMenu submenu) {

		map.put(submenu.getId(), submenu);

		model.addSubmenu(submenu.getMenuModel());

		super.add((JMenu) submenu);
	}

	/**
	 * @return Returns the id.
	 */
	public String getId() {
		return id;
	}

	public Enumeration getSubmenuEnumeration() {
		return map.elements();
	}
}
