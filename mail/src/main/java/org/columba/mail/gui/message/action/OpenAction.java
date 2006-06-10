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

import org.columba.core.desktop.ColumbaDesktop;
import org.columba.core.resourceloader.IconKeys;
import org.columba.core.resourceloader.ImageLoader;
import org.columba.mail.gui.message.util.ColumbaURL;
import org.columba.mail.util.MailResourceLoader;

/**
 * Open link in external browser.
 * 
 * @author fdietz
 */
public class OpenAction extends AbstractAction {
	ColumbaURL url = null;

	/**
	 * 
	 */
	public OpenAction(ColumbaURL url) {
		super(MailResourceLoader.getString("menu", "mainframe",
				"viewer_openlink"));

		putValue(SMALL_ICON, ImageLoader.getSmallIcon(IconKeys.FOLDER_OPEN));

		this.url = url;
		setEnabled( url != null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent evt) {
		ColumbaDesktop.getInstance().browse(url.getRealURL());
	}

}