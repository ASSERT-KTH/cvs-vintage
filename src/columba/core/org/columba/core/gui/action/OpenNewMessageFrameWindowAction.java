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

import org.columba.core.action.FrameAction;
import org.columba.core.gui.frame.AbstractFrameController;
import org.columba.mail.gui.messageframe.MessageFrameController;

/**
 * @author frd
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class OpenNewMessageFrameWindowAction extends FrameAction {

	/**
	 * @param frameController
	 * @param name
	 * @param longDescription
	 * @param actionCommand
	 * @param small_icon
	 * @param big_icon
	 * @param mnemonic
	 * @param keyStroke
	 */
	public OpenNewMessageFrameWindowAction(
		AbstractFrameController frameController) {
			super(
				frameController,
				"Message Window",
				"Message Window",
				"OPEN_NEW_MESSAGEFRAME_WINDOW",
				null,
				null,
				' ',
				null);
	}

	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent evt) {
		MessageFrameController c = new MessageFrameController();
		c.selectInbox();
		//new NextMessageAction(c).actionPerformed(null);
	}
}
