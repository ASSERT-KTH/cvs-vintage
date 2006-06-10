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

import org.columba.core.gui.action.AbstractColumbaAction;
import org.columba.core.resourceloader.ImageLoader;
import org.columba.mail.gui.composer.ComposerController;
import org.columba.mail.gui.composer.HeaderController;
import org.columba.mail.gui.composer.contact.SelectAddressDialog;
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
		putValue(SMALL_ICON, ImageLoader.getSmallIcon("contact-new.png"));

		// small icon for menu
		putValue(LARGE_ICON, ImageLoader.getIcon("contact-new.png"));

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

		HeaderController hc = composerController.getHeaderController();
		JFrame frame = null;
		SelectAddressDialog dialog = new SelectAddressDialog(frame, hc
				.getToHeaderItemList(), hc.getCcHeaderItemList(), hc
				.getBccHeaderItemList());

		if (dialog.isSuccess()) {
			hc.setToHeaderItemList(dialog.getToList());
			hc.setCcHeaderItemList(dialog.getCcList());
			hc.setBccHeaderItemList(dialog.getBccList());
			// update view accordingly
			hc.updateComponents(true);
		}
	}
}