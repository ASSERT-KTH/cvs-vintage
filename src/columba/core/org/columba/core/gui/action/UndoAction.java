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

package org.columba.core.gui.action;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import org.columba.core.action.FrameAction;
import org.columba.core.gui.frame.AbstractFrameController;
import org.columba.core.gui.statusbar.event.WorkerListChangeListener;
import org.columba.core.gui.statusbar.event.WorkerListChangedEvent;
import org.columba.core.gui.util.ImageLoader;
import org.columba.core.main.MainInterface;
import org.columba.core.util.GlobalResourceLoader;

public class UndoAction extends FrameAction implements WorkerListChangeListener {

	public UndoAction(AbstractFrameController controller) {
		super(
				controller,
				GlobalResourceLoader.getString(
					null, null, "menu_edit_undo"));
		
		// tooltip text
		setTooltipText(
				GlobalResourceLoader.getString(
					null, null, "menu_edit_undo"));
		
		// action command
		setActionCommand("UNDO");

		// small icon for menu
		setSmallIcon(ImageLoader.getSmallImageIcon("stock_undo-16.png"));
		
		// large icon for toolbar
		setLargeIcon(ImageLoader.getImageIcon("stock_undo.png"));
		
		// shortcut key
		setAcceleratorKey(
				KeyStroke.getKeyStroke(
					KeyEvent.VK_Z, ActionEvent.CTRL_MASK));

		// disable toolbar text
		enableToolBarText(false);
		
		// TODO: Use & to define mnemonic
		setMnemonic('U');
		
		setEnabled(false);
			
		MainInterface.processor.getTaskManager().addWorkerListChangeListener(this);
		
		MainInterface.focusManager.setUndoAction(this);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent evt) {
		MainInterface.focusManager.undo();
	}

	/* (non-Javadoc)
	 * @see org.columba.core.gui.statusbar.event.WorkerListChangeListener#workerListChanged(org.columba.core.gui.statusbar.event.WorkerListChangedEvent)
	 */
	public void workerListChanged(WorkerListChangedEvent e) {
		setEnabled(e.getNewValue() != 0);
	}
}
