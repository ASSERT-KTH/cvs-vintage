/*
 * Created on 25.03.2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.gui.composer.action;

import java.awt.event.ActionEvent;

import org.columba.core.gui.util.ImageLoader;
import org.columba.mail.action.ComposerAction;
import org.columba.mail.gui.composer.ComposerController;
import org.columba.mail.util.MailResourceLoader;

/**
 * @author frd
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class EncryptMessageAction extends ComposerAction {

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
	public EncryptMessageAction(ComposerController composerController) {
		super(
			composerController,
			MailResourceLoader.getString(
				"menu",
				"composer",
				"menu_message_encrypt"),
			MailResourceLoader.getString(
				"menu",
				"composer",
				"menu_message_encrypt"),
			MailResourceLoader.getString(
				"menu",
				"composer",
				"menu_message_encrypt"),
			"ENCRYPT",
			ImageLoader.getSmallImageIcon("encrypt_small.png"),
			null,
			MailResourceLoader.getMnemonic(
				"menu",
				"composer",
				"menu_message_encrypt"),
			null);

	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent evt) {

	}

}
