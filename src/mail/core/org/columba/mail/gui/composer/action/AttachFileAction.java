/*
 * Created on 25.03.2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.gui.composer.action;

import java.awt.event.ActionEvent;

import org.columba.core.action.FrameAction;
import org.columba.core.gui.util.ImageLoader;
import org.columba.mail.gui.composer.ComposerController;
import org.columba.mail.util.MailResourceLoader;

/**
 * @author frd
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class AttachFileAction extends FrameAction {

	public AttachFileAction(ComposerController composerController) {
		super(
				composerController,
				MailResourceLoader.getString(
					"menu", "composer", "menu_message_attachFile"));
		
		// tooltip text
		setTooltipText(
				MailResourceLoader.getString(
					"menu", "composer", "menu_message_attachFile_tooltip"));
		
		// toolbar text
		setToolBarName(
				MailResourceLoader.getString(
					"menu", "composer", "menu_message_attachFile"));
		
		// action command
		setActionCommand("ATTACH");
		
		// large icon for toolbar
		setLargeIcon(ImageLoader.getImageIcon("stock_attach.png"));
		
		// small icon for menu
		setSmallIcon(ImageLoader.getImageIcon("stock_attach-16.png"));

		// TODO: Use & to define mnemonic instead
		setMnemonic(
				MailResourceLoader.getMnemonic(
					"menu", "composer", "menu_message_attachFile"));

	}

	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent evt) {
		ComposerController composerController = ((ComposerController)getFrameController());

		composerController.getAttachmentController().addFileAttachment();
		
	}

}
