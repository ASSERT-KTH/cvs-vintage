/*
 * Created on 25.03.2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
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
