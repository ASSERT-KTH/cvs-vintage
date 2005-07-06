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

import javax.swing.JMenuItem;

public interface IMenuElement {

	public static final int TYPE_ACTION = 0;
	public static final int TYPE_MENUITEM = 1;
	public static final int TYPE_COMPONENT = 2;
	public static final int TYPE_SEPARATOR = 3;
	public static final int TYPE_PLACEHOLDER = 4;
	public static final int TYPE_MENU = 5;
	
	public boolean isSeparator();
	public boolean isAction();
	public boolean isPlaceholder();
	public boolean isMenu();
	public boolean isComponent();
	
	public JMenuItem getMenuItem();
	public Component getComponent();
	
	public IMenuElement getParent();
	public void setParent(IMenuElement parent);
	public Enumeration getChildren();
	
	public void add(IMenuElement child);
	public void insert(IMenuElement child, int position);
	public void remove(IMenuElement child);
	public void remove(int index);
	public int indexOf(IMenuElement child);
}
