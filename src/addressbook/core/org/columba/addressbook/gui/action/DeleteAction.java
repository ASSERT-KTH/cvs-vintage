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

package org.columba.addressbook.gui.action;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import org.columba.addressbook.util.AddressbookResourceLoader;
import org.columba.core.action.FrameAction;
import org.columba.core.gui.frame.FrameMediator;
import org.columba.core.gui.util.ImageLoader;

/**
 * @author frd
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class DeleteAction extends FrameAction {

	public DeleteAction(FrameMediator frameController) {
		super(
				frameController,
				AddressbookResourceLoader.getString(
					"menu", "mainframe", "menu_edit_delete"));
		
		// tooltip text
		setTooltipText(
				AddressbookResourceLoader.getString(
					"menu", "mainframe", "menu_edit_delete"));
					
		// action command
		setActionCommand("DELETE");
		
		// icons
		setSmallIcon(ImageLoader.getSmallImageIcon("stock_paste-16.png"));
		setLargeIcon(ImageLoader.getImageIcon("stock_paste.png"));
		
		// shortcut key
		setAcceleratorKey(
				KeyStroke.getKeyStroke(
					KeyEvent.VK_D, ActionEvent.CTRL_MASK));
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent evt) {
		// TODO implement action
	}
}
