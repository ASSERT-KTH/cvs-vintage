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

package org.columba.mail.gui.composer.action;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import org.columba.core.action.FrameAction;
import org.columba.core.gui.util.ImageLoader;
import org.columba.mail.gui.composer.ComposerController;
import org.columba.mail.util.MailResourceLoader;

/**
 * Add attachment to message.
 *
 * @author fdietz
 */
public class AttachFileAction extends FrameAction {

	public AttachFileAction(ComposerController composerController) {
		super(
				composerController,
				MailResourceLoader.getString(
					"menu", "composer", "menu_message_attachFile"));
		
		// tooltip text
		setTooltipText(
				MailResourceLoader.getString(
					"menu", "composer", "menu_message_attachFile_tooltip"));
		
		// toolbar text
		setToolBarName(
				MailResourceLoader.getString(
					"menu", "composer", "menu_message_attachFile_toolbar"));
		// large icon for toolbar
		setLargeIcon(ImageLoader.getImageIcon("stock_attach.png"));
		
		// small icon for menu
		setSmallIcon(ImageLoader.getImageIcon("stock_attach-16.png"));
		
                //shortcut key
		setAcceleratorKey(KeyStroke.getKeyStroke(KeyEvent.VK_A, ActionEvent.CTRL_MASK | ActionEvent.ALT_MASK));
	}

	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent evt) {
		ComposerController composerController = ((ComposerController)getFrameMediator());

		composerController.getAttachmentController().addFileAttachment();
		
	}
}
