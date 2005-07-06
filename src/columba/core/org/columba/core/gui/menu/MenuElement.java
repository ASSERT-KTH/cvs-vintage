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
import java.util.Vector;

import javax.swing.Action;
import javax.swing.JMenuItem;

/**
 * Menu element.
 * 
 * @author fdietz
 */
public class MenuElement implements IMenuElement {

	private int type;

	private Action action;

	private String placeholderId;

	private String menuId;

	private String menuLabel;

	private IMenuElement parent;

	private JMenuItem menuItem;
	
	private Component component;

	private Vector children = new Vector();

	public MenuElement(int type) {
		this.type = type;
	}

	public boolean isSeparator() {
		return type == TYPE_SEPARATOR ? true : false;
	}

	public boolean isAction() {
		return type == TYPE_ACTION ? true : false;
	}

	public boolean isPlaceholder() {
		return type == TYPE_PLACEHOLDER ? true : false;
	}

	public boolean isMenu() {
		return type == TYPE_MENU ? true : false;
	}

	public boolean isComponent() {
		return type == TYPE_MENUITEM ? true : false;
	}

	/**
	 * @return Returns the action.
	 */
	public Action getAction() {
		return action;
	}

	/**
	 * @return Returns the placeholderId.
	 */
	public String getPlaceholderId() {
		return placeholderId;
	}

	public IMenuElement getParent() {
		return parent;
	}

	public Enumeration getChildren() {
		return children.elements();
	}

	public void add(IMenuElement child) {
		child.setParent(this);
		children.add(child);
	}

	public void setParent(IMenuElement parent) {
		this.parent = parent;
	}

	public void remove(IMenuElement child) {
		children.remove(child);
	}

	public void remove(int index) {
		children.remove(index);
	}

	/**
	 * @return Returns the menuId.
	 */
	public String getMenuId() {
		return menuId;
	}

	/**
	 * @param menuId
	 *            The menuId to set.
	 */
	public void setMenuId(String menuId) {
		this.menuId = menuId;
	}

	/**
	 * @param action
	 *            The action to set.
	 */
	public void setAction(Action action) {
		this.action = action;
	}

	/**
	 * @param placeholderId
	 *            The placeholderId to set.
	 */
	public void setPlaceholderId(String placeholderId) {
		this.placeholderId = placeholderId;
	}

	/**
	 * @return Returns the menuLabel.
	 */
	public String getMenuLabel() {
		return menuLabel;
	}

	/**
	 * @param menuLabel
	 *            The menuLabel to set.
	 */
	public void setMenuLabel(String menuLabel) {
		this.menuLabel = menuLabel;
	}

	public int indexOf(IMenuElement child) {
		return children.indexOf(child);
	}

	public void insert(IMenuElement child, int position) {
		child.setParent(this);

		children.insertElementAt(child, position);
	}

	public String toString() {
		StringBuffer buf = new StringBuffer();
		if (menuId != null)
			buf.append(menuId);
		if (placeholderId != null)
			buf.append(placeholderId);
		if (action != null)
			buf.append(action.toString());
		
		buf.append(" type="+type);
		
		return buf.toString();
	}

	public JMenuItem getMenuItem() {
		return menuItem;
	}

	/**
	 * @param component
	 *            The component to set.
	 */
	public void setMenuItem(JMenuItem menuItem) {
		this.menuItem = menuItem;
	}

	public Component getComponent() {
		return component;
	}

	/**
	 * @param component The component to set.
	 */
	public void setComponent(Component component) {
		this.component = component;
	}

}
