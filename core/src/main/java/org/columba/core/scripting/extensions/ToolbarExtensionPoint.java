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

import org.columba.api.gui.frame.IContainer;
import org.columba.core.gui.action.AbstractColumbaAction;
import org.columba.core.gui.frame.FrameManager;
import org.columba.core.gui.toolbar.ExtendableToolBar;

/**
 * @author Celso Pinto (cpinto@yimports.com)
 */
public class ToolbarExtensionPoint extends AbstractExtensionPoint {
	
	private static final Logger LOG = Logger.getLogger(MenuExtensionPoint.class
			.getName());

	public static final String EXTENSION_POINT_ID = "main_toolbar";

	public ToolbarExtensionPoint() {
		super(EXTENSION_POINT_ID);
	}

	/**
	 * Add an action at the end or beginning of the toolbar.
	 * 
	 * @param action
	 *            Your action
	 * @param position
	 *            Use AbstractExtensionPoint.POSITION_BEGINNING or POSITION_END
	 *            values
	 */
	public void addAction(AbstractColumbaAction action) {
		
		// add toolbar button between last button and cancel button
		getToolBar().add(action);
	}

	private IContainer getFirstContainer() {
		IContainer[] frames = FrameManager.getInstance().getOpenFrames();
		if (frames.length == 0) {
			LOG.warning(getClass().getName() + ": Not enough open frames!");
			return null;
		}
		return frames[0];
	}

	private ExtendableToolBar getToolBar() {
		// oh oh! casting to a specific implementation here ;-)
		return (ExtendableToolBar) getFirstContainer().getToolBar();
	}

}
