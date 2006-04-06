/*

 The contents of this file are subject to the Mozilla Public License Version 1.1 
 (the "License") you may not use this file except in compliance with the License. 

 You may obtain a copy of the License at http://www.mozilla.org/MPL/

 Software distributed under the License is distributed on an "AS IS" basis,
 WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 for the specific language governing rights and limitations under the License.

 The Original Code is "BshInterpreter plugin for The Columba Project"

 The Initial Developer of the Original Code is Celso Pinto
 Portions created by Celso Pinto are Copyright (C) 2005.
 Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.

 All Rights Reserved.

 */
package org.columba.core.gui.globalactions;

import java.awt.event.ActionEvent;

import org.columba.api.gui.frame.IFrameMediator;
import org.columba.core.gui.action.AbstractColumbaAction;
import org.columba.core.gui.scripting.ScriptManager;
import org.columba.core.resourceloader.ImageLoader;
import org.columba.core.scripting.FileObserverThread;

public class BeanshellManagerAction extends AbstractColumbaAction {

	/* TODO should come up with a better name for the menu item */
	private static final String RES_MENU_ITEM = "Macros";

	public BeanshellManagerAction() {
		super(null, RES_MENU_ITEM);
		
		//putValue(SMALL_ICON, ImageLoader.getIcon("script.png"));
	}

	public BeanshellManagerAction(IFrameMediator mediator) {
		super(mediator, RES_MENU_ITEM);
		
		//putValue(SMALL_ICON, ImageLoader.getIcon("script.png"));
	}

	public void actionPerformed(ActionEvent aEvent) {
		new ScriptManager(getFrameMediator().getView().getFrame(),
				FileObserverThread.getInstance()).setVisible(true);
	}

}
