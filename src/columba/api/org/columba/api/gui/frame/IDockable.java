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
package org.columba.api.gui.frame;

import javax.swing.JComponent;
import javax.swing.JPopupMenu;

/**
 * A dockable view which can be registered in a <code>IDock</code>.
 * 
 * @author fdietz
 */
public interface IDockable {

	/**
	 * Return dockable id;
	 * 
	 * @return dockable id
	 */
	public String getId();

	/**
	 * Return dockable human-readable name. This name is used in the Show/Hide
	 * menu and is also the initially displayed title.
	 * 
	 * @return dockable name
	 */
	public String resolveName();

	/**
	 * Return view component.
	 * 
	 * @return view component
	 */
	public JComponent getView();

	/**
	 * Return popup menu instance.
	 * 
	 * @return popup menu
	 */
	public JPopupMenu getPopupMenu();

	/**
	 * Set new dockable title.
	 * 
	 * @param title
	 *            new dockable title
	 */
	public void setTitle(String title);
}
