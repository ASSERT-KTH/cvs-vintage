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

import org.columba.core.action.AbstractSelectableAction;
import org.columba.core.gui.util.ImageLoader;
import org.columba.core.logging.ColumbaLogger;

import org.columba.mail.config.PGPItem;
import org.columba.mail.gui.composer.ComposerController;
import org.columba.mail.util.MailResourceLoader;

import java.awt.event.ActionEvent;

/**
 * @author frd
 *
 * To change this generated comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class SignMessageAction extends AbstractSelectableAction {
    private ComposerController composerController;

    public SignMessageAction(ComposerController composerController) {
        super(composerController,
            MailResourceLoader.getString("menu", "composer", "menu_message_sign"));
        this.composerController = composerController;

        // tooltip text
        putValue(SHORT_DESCRIPTION,
            MailResourceLoader.getString("menu", "composer", "menu_message_sign")
                              .replaceAll("&", ""));

        // small icon for menu
        putValue(SMALL_ICON, ImageLoader.getSmallImageIcon("16_sign.png"));

        PGPItem item = this.composerController.getModel().getAccountItem()
                                              .getPGPItem();
        String attribute = item.get("always_sign");
        setState(Boolean.valueOf(attribute).booleanValue());
        
        composerController.getModel().setSignMessage(getState());
        //setEnabled(false);
    }

    /* (non-Javadoc)
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(ActionEvent evt) {
        ColumbaLogger.log.info("start signing...");

        //ComposerModel model = (ComposerModel) ((ComposerController)getFrameController()).getModel();
        composerController.getModel().setSignMessage(getState());
    }
}
