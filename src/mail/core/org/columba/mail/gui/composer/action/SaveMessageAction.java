/*
 * Created on 25.03.2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.gui.composer.action;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.KeyStroke;

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
public class SaveMessageAction extends FrameAction {

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
	public SaveMessageAction(ComposerController composerController) {
		super(
			composerController,
			MailResourceLoader.getString("menu", "mainframe", "menu_file_save"),
			MailResourceLoader.getString("menu", "mainframe", "menu_file_save"),
			MailResourceLoader.getString("menu", "mainframe", "menu_file_save"),
			"SAVE",
			ImageLoader.getSmallImageIcon("stock_save-16.png"),
			ImageLoader.getImageIcon("stock_save.png"),
			MailResourceLoader.getMnemonic(
				"menu",
				"mainframe",
				"menu_file_save"),
			KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.CTRL_MASK));

	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent evt) {
		
	}

}
