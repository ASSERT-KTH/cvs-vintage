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

package org.columba.core.gui.util;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import org.columba.core.command.TaskManager;
import org.columba.core.command.TaskManagerEvent;
import org.columba.core.command.TaskManagerListener;
import org.columba.core.gui.base.AnimatedGIFComponent;
import org.columba.core.resourceloader.ImageLoader;

/**
 * Animated image showing background activity.
 * <p>
 * Can be found in the menubar in the right topmost corner of Columba.
 * <p>
 * ImageSequenceTimer actually only listens for {@link TaskManagerEvent} and
 * starts/stops as appropriate.
 * 
 * @author fdietz
 */
public class ThrobberIcon extends JPanel implements TaskManagerListener {

	private TaskManager taskManager;

	private AnimatedGIFComponent comp;

	public ThrobberIcon() {
		super();

		setLayout(new BorderLayout());

		comp = new AnimatedGIFComponent(ImageLoader
				.getMiscIcon("Throbber.gif").getImage(), ImageLoader
				.getMiscIcon("Throbber.png").getImage());
		add(comp, BorderLayout.CENTER);

		// register interested on changes in the running worker list
		taskManager = TaskManager.getInstance();
		taskManager.addTaskManagerListener(this);
	}

	/**
	 * Its an element of the toolbar, and therefor can't have the focus.
	 */
	public boolean isFocusTraversable() {
		return isRequestFocusEnabled();
	}

	public void start() {
		comp.go();
	}

	public void stop() {
		comp.stop();

	}

	public void workerAdded(TaskManagerEvent e) {
		update();
	}

	public void workerRemoved(TaskManagerEvent e) {
		update();
	}

	protected void update() {
		// just the animation, if there are more than zero
		// workers running
		if (taskManager.count() > 0) {
			start();
		} else {
			stop();
		}
	}
}
