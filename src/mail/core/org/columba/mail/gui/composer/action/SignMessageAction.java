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

import javax.swing.JCheckBoxMenuItem;

import org.columba.core.action.CheckBoxAction;
import org.columba.core.gui.util.ImageLoader;
import org.columba.core.logging.ColumbaLogger;
import org.columba.mail.config.PGPItem;
import org.columba.mail.gui.composer.ComposerController;
import org.columba.mail.util.MailResourceLoader;

/**
 * @author frd
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class SignMessageAction extends CheckBoxAction {
    
    private ComposerController composerController;

	public SignMessageAction(ComposerController composerController) {
		super(
				composerController,
				MailResourceLoader.getString(
					"menu", "composer", "menu_message_sign"));
        this.composerController = composerController;
		
		// tooltip text
		setTooltipText(
				MailResourceLoader.getString(
					"menu", "composer", "menu_message_sign"));
					
		// action command
		setActionCommand("SIGN");
		
		// small icon for menu
		setSmallIcon(ImageLoader.getSmallImageIcon("16_sign.png"));
			 
		//setEnabled(false);
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent evt) {
		ColumbaLogger.log.debug("start signing...");
		
		//ComposerModel model = (ComposerModel) ((ComposerController)getFrameController()).getModel();
		this.composerController.getModel().setSignMessage( getCheckBoxMenuItem().isSelected() );
	}
    
    /** 
     * Overwritten to initialize the selection state of the
     * CheckBoxMenuItem.
     * 
     * @see org.columba.core.action.CheckBoxAction#setCheckBoxMenuItem(javax.swing.JCheckBoxMenuItem)
     */
    public void setCheckBoxMenuItem(JCheckBoxMenuItem checkBoxMenuItem) {
        /** idea from kpeder ... i don't know why we must use this deprecated method */
        super.setCheckBoxMenuItem(checkBoxMenuItem);
        ColumbaLogger.log.debug(
                "Initializing selected state of AlwaysSignAction");
        
        PGPItem item = this.composerController.getModel().getAccountItem().getPGPItem();
        if (item.get("always_sign").equals("true")) {
            getCheckBoxMenuItem().setSelected(true);
        } else {
            getCheckBoxMenuItem().setSelected(false);
        }
        // let the model knowing if signing is preferred or not
        //ComposerModel model = (ComposerModel) ((ComposerController)getFrameController()).getModel();
        this.composerController.getModel().setSignMessage( getCheckBoxMenuItem().isSelected() );
     
    }

}
