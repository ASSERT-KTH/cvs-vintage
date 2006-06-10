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
import java.util.logging.Logger;

import javax.swing.Action;
import javax.swing.JMenuItem;

import org.columba.core.logging.Logging;

public class MenuModel {

	private static final Logger LOG = Logger
			.getLogger("org.columba.core.gui.menu"); //$NON-NLS-1$

	private IMenuElement rootElement;

	private Hashtable placeholders = new Hashtable();

	private Hashtable submenues = new Hashtable();

	private String id;

	public MenuModel(String id) {
		this.id = id;

		rootElement = MenuElementFactory.createMenuElement(id, "rootLabel");
		submenues.put(id, this);
	}

	public void insert(Action action, int position) {
		insert(position, MenuElementFactory.createActionElement(action));
	}

	public void insert(Component component, int position) {
		insert(position, MenuElementFactory.createComponentElement(component));
	}

	public void insert(JMenuItem menuItem, int position) {
		insert(position, MenuElementFactory.createMenuItemElement(menuItem));
	}

	public void insertSeparator(int position) {
		insert(position, MenuElementFactory.createSeparatorElement());
	}

	public void insertPlaceholder(String placeholderId, int position) {
		insert(position, MenuElementFactory
				.createPlaceholderElement(placeholderId));
	}

	protected void insert(int position, IMenuElement element) {
		rootElement.insert(element, position);

		if (element.isPlaceholder()) {
			String id = ((MenuElement) element).getPlaceholderId();
			if (placeholders.containsKey(id))
				throw new IllegalArgumentException("placeholder id <" + id
						+ "> already used.");

			placeholders.put(id, element);
		}
	}

	protected void append(IMenuElement element) {

		rootElement.add(element);

		if (element.isPlaceholder()) {
			String id = ((MenuElement) element).getPlaceholderId();
			if (placeholders.containsKey(id))
				throw new IllegalArgumentException("placeholder id <" + id
						+ "> already used.");

			placeholders.put(id, element);
		}
	}

	protected int insert(IMenuElement element, String placeholderId) {
		if (element == null)
			throw new IllegalArgumentException("element == null");
		if (placeholderId == null)
			throw new IllegalArgumentException(
					"placeholderId == null, for element " + element.toString());

		if (placeholders.containsKey(placeholderId) == false) {
			if (Logging.DEBUG)
				printDebugPlaceholders();

			LOG.severe("no matching placeholder with id <" + placeholderId
					+ "> in menu <" + getId() + "> found.");

		}

		IMenuElement placeholderElement = (IMenuElement) placeholders
				.get(placeholderId);

		int index = rootElement.indexOf(placeholderElement);

		if (index != -1)
			insert(index, element);

		return index;
	}

	/**
	 * 
	 */
	private void printDebugPlaceholders() {
		Enumeration e = placeholders.elements();
		while (e.hasMoreElements()) {
			LOG.info(((IMenuElement) e.nextElement()).toString());
		}
	}

	/**
	 * @return Returns the id.
	 */
	public String getId() {
		return id;
	}

	/** ****************** public helper methods ******************* */

	public void add(Component component) {
		append(MenuElementFactory.createComponentElement(component));
	}

	public void add(Action action) {
		append(MenuElementFactory.createActionElement(action));
	}

	public void add(JMenuItem menuItem) {
		append(MenuElementFactory.createMenuItemElement(menuItem));
	}

	public void addSeparator() {
		append(MenuElementFactory.createSeparatorElement());
	}

	public int insertSeparator(String placeholderId) {
		int index = insert(MenuElementFactory.createSeparatorElement(),
				placeholderId);
		return index;
	}

	public void appendPlaceholder(String placeholderId) {
		append(MenuElementFactory.createPlaceholderElement(placeholderId));
	}

	public MenuModel getSubmenu(String submenuId) {
		if (submenues.containsKey(submenuId) == false)
			throw new IllegalArgumentException("no matching menu with id "
					+ submenuId + " found.");
		return (MenuModel) submenues.get(submenuId);
	}

	public void addSubmenu(MenuModel submenu) {
		rootElement.add(submenu.getRootElement());
		submenues.put(submenu.getId(), submenu);

	}

	protected IMenuElement getRootElement() {
		return rootElement;
	}

	public int insert(Action action, String placeholderId) {
		int index = insert(MenuElementFactory.createActionElement(action),
				placeholderId);

		return index;
	}

	public int insert(JMenuItem menuItem, String placeholderId) {
		int index = insert(MenuElementFactory.createMenuItemElement(menuItem),
				placeholderId);
		return index;
	}

	public Enumeration getSubmenuEnumeration() {
		return submenues.elements();
	}

	public void insertPlaceholder(String placeholderId,
			String insertionPlaceholder) {
		insert(MenuElementFactory.createPlaceholderElement(placeholderId),
				insertionPlaceholder);
	}

	public int insert(Component component, String placeholderId) {
		int index = insert(
				MenuElementFactory.createComponentElement(component),
				placeholderId);
		return index;
	}
}
