// The contents of this file are subject to the Mozilla Public License Version
// 1.1
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
//The Initial Developers of the Original Code are Frederik Dietz and Timo
// Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.columba.mail.gui.action;

import java.awt.event.ActionEvent;

import org.columba.api.gui.frame.IFrameMediator;
import org.columba.core.gui.action.AbstractColumbaAction;
import org.columba.core.resourceloader.ImageLoader;
import org.columba.mail.gui.config.accountlist.AccountListDialog;
import org.columba.mail.util.MailResourceLoader;

public class MailAccountAction extends AbstractColumbaAction {
	public MailAccountAction(IFrameMediator controller) {
		super(controller, MailResourceLoader.getString("menu", "mainframe",
				"menu_edit_accountconfig"));

		// tooltip text
		putValue(SHORT_DESCRIPTION, MailResourceLoader.getString("menu",
				"mainframe", "menu_edit_accountconfig").replaceAll("&", ""));

		// small icon for menu
//		putValue(SMALL_ICON, ImageLoader
//				.getSmallImageIcon("configure_16_mail.png"));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent evt) {
		new AccountListDialog(getFrameMediator());
	}
}