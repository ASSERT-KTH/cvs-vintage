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
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

import org.columba.core.action.AbstractColumbaAction;
import org.columba.core.gui.frame.FrameMediator;
import org.columba.core.gui.util.ImageLoader;
import org.columba.core.main.MainInterface;
import org.columba.core.plugin.PluginLoadingFailedException;
import org.columba.mail.util.MailResourceLoader;

/**
 * Opens the composer window for creating a new message.
 */
public class NewMessageAction extends AbstractColumbaAction {
	
	public NewMessageAction() {
		super(null, "New Message Action");
	}
	
    public NewMessageAction(FrameMediator controller) {
        super(controller,
            MailResourceLoader.getString("menu", "mainframe", "menu_message_new"));
        putValue(TOOLBAR_NAME,
            MailResourceLoader.getString("menu", "mainframe",
                "menu_message_new_toolbar"));
        putValue(SHORT_DESCRIPTION,
            MailResourceLoader.getString("menu", "mainframe",
                "menu_message_new_tooltip").replaceAll("&", ""));
        putValue(SMALL_ICON, ImageLoader.getSmallImageIcon("stock_edit-16.png"));
        putValue(LARGE_ICON, ImageLoader.getImageIcon("stock_edit.png"));
        putValue(ACCELERATOR_KEY,
            KeyStroke.getKeyStroke(KeyEvent.VK_M, ActionEvent.CTRL_MASK));
    }

    public void actionPerformed(ActionEvent evt) {
    	try {
			MainInterface.frameModel.openView("Composer");
		} catch (PluginLoadingFailedException e) {
			e.printStackTrace();
		}
    }
}
