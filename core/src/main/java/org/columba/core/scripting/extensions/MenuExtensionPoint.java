/*
  The contents of this file are subject to the Mozilla Public License Version 1.1
  (the "License"); you may not use this file except in compliance with the 
  License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
  
  Software distributed under the License is distributed on an "AS IS" basis,
  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License 
  for the specific language governing rights and
  limitations under the License.

  The Original Code is "The Columba Project"
  
  The Initial Developers of the Original Code are Frederik Dietz and Timo Stich.
  Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003. 
  
  All Rights Reserved.
*/
package org.columba.core.scripting.extensions;

import java.util.logging.Logger;

import javax.swing.JFrame;

import org.columba.api.gui.frame.IContainer;
import org.columba.core.gui.action.AbstractColumbaAction;
import org.columba.core.gui.frame.FrameManager;
import org.columba.core.gui.menu.ExtendableMenuBar;

/**
 * Insert action in menu.
 * <p>
 * Lookup <code>menuId</code> and <code>placeholderId</code> in
 * <code>org.columba.core.action.menu.xml</code>.
 * <p>
 * TODO (@author fdietz):  we should consider adding public constants here or to
 *         ExtendableMenuBar to make it easier for users to find insertion
 *         points.
 * 
 * @author Celso Pinto (cpinto@yimports.com)
 */
public class MenuExtensionPoint extends AbstractExtensionPoint {

	public static final String EXTENSION_POINT_ID = "main_menu";

	private static final Logger LOG = Logger.getLogger(MenuExtensionPoint.class
			.getName());

	public MenuExtensionPoint() {
		super(EXTENSION_POINT_ID);
	}

	/**
	 * Add an action at the end or beginning of the menu.
	 * 
	 * @param action
	 *            Your action
	 * @param menuId
	 *            A menu identifier that will be used to get the menu where the
	 *            action will be added.
	 * @param position
	 *            Use AbstractExtensionPoint.POSITION_BEGINNING or POSITION_END
	 *            values
	 */
	public void addAction(AbstractColumbaAction action, String menuId,
			String placeholderId) {

		ExtendableMenuBar menu = getDefaultMenubar();
		if (menu == null) {
			/* TODO create exception for this */
			throw new RuntimeException("Could not retrieve default menu bar");
		}

		menu.insertAction(menuId, placeholderId, action);

	}

	private IContainer getFirstContainer() {
		IContainer[] frames = FrameManager.getInstance().getOpenFrames();
		if (frames.length == 0) {
			LOG.warning(getClass().getName() + ": Not enough open frames!");
			return null;
		}
		return frames[0];
	}

	private ExtendableMenuBar getDefaultMenubar() {
		JFrame frame = (JFrame) getFirstContainer();
		return (ExtendableMenuBar) frame.getJMenuBar();
	}
}
