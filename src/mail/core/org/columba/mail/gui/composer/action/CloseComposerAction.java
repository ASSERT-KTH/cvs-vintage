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
public class CloseComposerAction extends ComposerAction {

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
	public CloseComposerAction(ComposerController composerController) {
		super(
			composerController,
			MailResourceLoader.getString(
				"menu",
				"mainframe",
				"menu_file_close"),
			MailResourceLoader.getString(
				"menu",
				"mainframe",
				"menu_file_close"),
			MailResourceLoader.getString(
				"menu",
				"mainframe",
				"menu_file_close"),
			"EXIT",
			ImageLoader.getSmallImageIcon("stock_exit-16.png"),
			ImageLoader.getImageIcon("stock_exit.png"),
			MailResourceLoader.getMnemonic(
				"menu",
				"mainframe",
				"menu_file_close"),
			KeyStroke.getKeyStroke(KeyEvent.VK_W, ActionEvent.CTRL_MASK));

	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent evt) {
		// TODO composer doesn't close correctly
		
		//getFrameController().close();
		
		
		/*
		composerInterface.composerController.saveWindowPosition();
		composerInterface.composerController.hideComposerWindow();
		*/
	}

}
