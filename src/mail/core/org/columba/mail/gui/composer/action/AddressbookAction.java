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

import javax.swing.JFrame;

import org.columba.addressbook.facade.IDialogFacade;
import org.columba.addressbook.gui.ISelectAddressDialog;
import org.columba.core.action.AbstractColumbaAction;
import org.columba.core.gui.util.ImageLoader;
import org.columba.core.services.ServiceNotFoundException;
import org.columba.mail.connector.ServiceConnector;
import org.columba.mail.gui.composer.ComposerController;
import org.columba.mail.util.MailResourceLoader;

/**
 * @author frd
 * 
 * To change this generated comment go to Window>Preferences>Java>Code
 * Generation>Code and Comments
 */
public class AddressbookAction extends AbstractColumbaAction {
	public AddressbookAction(ComposerController composerController) {
		super(composerController, MailResourceLoader.getString("menu",
				"composer", "menu_message_addressbook"));

		// tooltip text
		putValue(SHORT_DESCRIPTION, MailResourceLoader.getString("menu",
				"composer", "menu_message_addressbook_tooltip").replaceAll("&",
				""));

		// large icon for toolbar
		putValue(LARGE_ICON, ImageLoader.getImageIcon("contact.png"));

		// small icon for menu
		putValue(SMALL_ICON, ImageLoader.getImageIcon("contact_small.png"));

		// disable text in toolbar
		setShowToolBarText(false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent evt) {
		ComposerController composerController = ((ComposerController) getFrameMediator());

		IDialogFacade dialogFacade = null;
		try {
			dialogFacade = ServiceConnector.getDialogFacade();
		} catch (ServiceNotFoundException e) {
			e.printStackTrace();
			return;
		}
		
		JFrame frame = null;
		ISelectAddressDialog dialog = dialogFacade.getSelectAddressDialog(
				frame, composerController.getHeaderController()
						.getHeaderItemLists());

		if (dialog.isSuccess()) {

			composerController.getHeaderController().setHeaderItemLists(
					dialog.getHeaderItemLists());
		}
	}
}