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

	/**
	 * @param composerController
	 * @param name
	 * @param longDescription
	 * @param tooltip
	 * @param actionCommand
	 * @param small_icon
	 * @param big_icon
	 * @param mnemonic
	 * @param keyStroke
	 */
	public AttachFileAction(
		ComposerController composerController
		) {
		super(
			composerController,
			MailResourceLoader.getString(
					"menu",
					"composer",
					"menu_message_attachFile"),
				MailResourceLoader.getString(
					"menu",
					"composer",
					"menu_message_attachFile"),
				MailResourceLoader.getString(
					"menu",
					"composer",
					"menu_message_attachFile_tooltip"),
				"ATTACH",
				ImageLoader.getSmallImageIcon("stock_attach-16.png"),
				ImageLoader.getImageIcon("stock_attach.png"),
				MailResourceLoader.getMnemonic(
					"menu",
					"composer",
					"menu_message_attachFile"),
				null);
		
	}

	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent evt) {
		ComposerController composerController = ((ComposerController)getFrameController());

		composerController.getAttachmentController().addFileAttachment();
		
	}

}
