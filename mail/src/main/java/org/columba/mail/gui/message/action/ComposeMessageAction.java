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
package org.columba.mail.gui.message.action;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import org.columba.core.gui.frame.DefaultContainer;
import org.columba.mail.gui.composer.ComposerController;
import org.columba.mail.gui.composer.ComposerModel;
import org.columba.mail.gui.message.util.ColumbaURL;
import org.columba.mail.util.MailResourceLoader;

/**
 * Compose message using selected address.
 * 
 * @author fdietz
 */

public class ComposeMessageAction extends AbstractAction {

	private String emailAddress;

	private ColumbaURL url = null;

	/**
	 * 
	 */
	public ComposeMessageAction(String emailAddress) {
		super(MailResourceLoader.getString("menu", "mainframe",
				"viewer_compose"));

		this.emailAddress = emailAddress;

		setEnabled( emailAddress != null);
	}

	public ComposeMessageAction(ColumbaURL url) {
		super(MailResourceLoader.getString("menu", "mainframe",
				"viewer_compose"));
		this.url = url;
		setEnabled( url != null);
		
		if ( url != null)
		setEnabled( url.isMailTo());
	}

	public void actionPerformed(ActionEvent evt) {
		ComposerController controller = new ComposerController();
		new DefaultContainer(controller);

		ComposerModel model = new ComposerModel();
		if (emailAddress != null)
			model.setTo(emailAddress);
		else
			model.setTo(url.getEmailAddress());

		// apply model
		controller.setComposerModel(model);

		controller.updateComponents(true);

	}

}