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

import java.awt.Component;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JSeparator;

import org.columba.core.gui.base.CMenu;


public class ExtendableMenu extends CMenu {

	private MenuModel model;

	private Hashtable<String, ExtendableMenu> map = new Hashtable<String, ExtendableMenu>();

	private String id;

	public ExtendableMenu(String id, String label) {
		super(label, id);

		this.id = id;

		model = new MenuModel(id);

		map.put(id, this);
	}

	public MenuModel getMenuModel() {
		return model;
	}

	/**
	 * @see javax.swing.JMenu#add(javax.swing.Action)
	 */
	public JMenuItem add(Action action) {
		model.add(action);

		return super.add(action);

	}

	/**
	 * @see javax.swing.JMenu#add(javax.swing.JMenuItem)
	 */
	public JMenuItem add(JMenuItem menuItem) {
		model.add(menuItem);

		return super.add(menuItem);
	}

	/**
	 * @see javax.swing.JMenu#addSeparator()
	 */
	public void addSeparator() {
		int index = getComponentCount();

		// make sure that we don't end up with two separators
		if (index > 0 && (index - 1 < getComponentCount())) {
			Component prev = getComponent(index - 1);
			if (prev instanceof JSeparator)
				return;
		}

		model.addSeparator();

		super.addSeparator();
	}

	/**
	 * @see javax.swing.JMenu#insert(javax.swing.Action, int)
	 */
	public JMenuItem insert(Action action, int pos) {
		model.insert(action, pos);

		return super.insert(action, pos);
	}

	/**
	 * @see javax.swing.JMenu#insert(javax.swing.JMenuItem, int)
	 */
	public JMenuItem insert(JMenuItem menuItem, int pos) {
		model.insert(menuItem, pos);

		return super.insert(menuItem, pos);
	}

	/**
	 * @see javax.swing.JMenu#insertSeparator(int)
	 */
	public void insertSeparator(int index) {
		model.insertSeparator(index);

		super.insertSeparator(index);
	}

	public void addPlaceholder(String placeholderId) {
		model.appendPlaceholder(placeholderId);
	}

	public void insertPlaceholder(String placeholderId, int pos) {
		model.insertPlaceholder(placeholderId, pos);
	}

	public void insert(Action action, String placeholderId) {
		int index = model.insert(action, placeholderId);
		if (index != -1)
			super.insert(action, index);
	}

	public void insert(Component component, String placeholderId) {
		int index = model.insert(component, placeholderId);
		if (index != -1)
			super.add(component, index);
	}

	public void insert(JMenuItem menuItem, String placeholderId) {
		if (menuItem == null)
			throw new IllegalArgumentException("menuItem == null");
		if (placeholderId == null)
			throw new IllegalArgumentException("placeholderId == null");

		int index = model.insert(menuItem, placeholderId);
		if (index != -1)
			super.insert(menuItem, index);
	}

	public void insertSeparator(String placeholderId) {
		int index = model.insertSeparator(placeholderId);
		// make sure that we don't end up with two separators
		if (index > 0 && (index - 1 < getComponentCount())) {
			Component prev = getComponent(index - 1);
			if (prev instanceof JSeparator)
				return;
		}

		if (index != -1)
			super.insertSeparator(index);
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

	public Enumeration<ExtendableMenu> getSubmenuEnumeration() {
		return map.elements();
	}

	/**
	 * @see javax.swing.JMenu#add(java.awt.Component, int)
	 */
	public Component add(Component component, int index) {
		model.insert(component, index);

		return super.add(component, index);
	}

	/**
	 * @see javax.swing.JMenu#add(java.awt.Component)
	 */
	public Component add(Component component) {
		model.add(component);

		return super.add(component);
	}

	public void insertPlaceholder(String placeholderId,
			String insertionPlaceholder) {
		model.insertPlaceholder(placeholderId, insertionPlaceholder);
	}
}
