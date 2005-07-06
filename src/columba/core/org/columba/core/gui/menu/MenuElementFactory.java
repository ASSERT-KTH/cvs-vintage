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

import javax.swing.Action;
import javax.swing.JMenuItem;

/**
 * Menu element factory.
 * 
 * @author fdietz
 * 
 */
public class MenuElementFactory {

	public static IMenuElement createComponentElement(Component component) {
		MenuElement menuElement = new MenuElement(IMenuElement.TYPE_COMPONENT);
		menuElement.setComponent(component);
		return menuElement;
	}
	
	public static IMenuElement createMenuItemElement(JMenuItem menuItem) {
		MenuElement menuElement = new MenuElement(IMenuElement.TYPE_MENUITEM);
		menuElement.setMenuItem(menuItem);
		return menuElement;
	}
	
	public static IMenuElement createActionElement(Action action) {
		MenuElement menuElement = new MenuElement(IMenuElement.TYPE_ACTION);
		menuElement.setAction(action);
		return menuElement;
	}

	public static IMenuElement createSeparatorElement() {
		MenuElement menuElement = new MenuElement(IMenuElement.TYPE_SEPARATOR);
		return menuElement;
	}

	public static IMenuElement createPlaceholderElement(String placeholderId) {
		MenuElement menuElement = new MenuElement(IMenuElement.TYPE_PLACEHOLDER);
		menuElement.setPlaceholderId(placeholderId);
		return menuElement;
	}

	public static IMenuElement createMenuElement(String menuId, String menuLabel) {
		MenuElement menuElement = new MenuElement(IMenuElement.TYPE_MENU);
		menuElement.setMenuId(menuId);
		menuElement.setMenuLabel(menuLabel);
		return menuElement;
	}
}
