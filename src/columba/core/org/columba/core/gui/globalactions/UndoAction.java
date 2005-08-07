//The contents of this file are subject to the Mozilla Public License Version 1.1
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
//The Initial Developers of the Original Code are Frederik Dietz and Timo Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003. 
//
//All Rights Reserved.

package org.columba.core.gui.globalactions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import org.columba.api.gui.frame.IFrameMediator;
import org.columba.core.command.TaskManager;
import org.columba.core.command.TaskManagerEvent;
import org.columba.core.command.TaskManagerListener;
import org.columba.core.gui.action.AbstractColumbaAction;
import org.columba.core.resourceloader.GlobalResourceLoader;
import org.columba.core.resourceloader.ImageLoader;

public class UndoAction extends AbstractColumbaAction implements
		TaskManagerListener {

	protected TaskManager taskManager;

	public UndoAction(IFrameMediator controller) {
		super(controller, GlobalResourceLoader.getString(null, null,
				"menu_edit_undo"));

		// tooltip text
		putValue(SHORT_DESCRIPTION, GlobalResourceLoader.getString(null, null,
				"menu_edit_undo_tooltip").replaceAll("&", ""));

		// small icon for menu
		putValue(SMALL_ICON, ImageLoader.getSmallImageIcon("stock_undo-16.png"));

		// large icon for toolbar
		putValue(LARGE_ICON, ImageLoader.getImageIcon("stock_undo.png"));

		// shortcut key
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_Z,
				ActionEvent.CTRL_MASK));

		// disable toolbar text
		setShowToolBarText(false);

		setEnabled(true);

		taskManager = TaskManager.getInstance();
		taskManager.addTaskManagerListener(this);

	}

	public void actionPerformed(ActionEvent evt) {

	}

	public void workerAdded(TaskManagerEvent e) {
		setEnabled(taskManager.count() > 0);
	}

	public void workerRemoved(TaskManagerEvent e) {
		setEnabled(taskManager.count() > 0);
	}

}
