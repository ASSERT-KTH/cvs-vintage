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
public class CloseComposerAction extends FrameAction {

	public CloseComposerAction(ComposerController composerController) {
		
		super(
				composerController,
				MailResourceLoader.getString(
					"menu", "mainframe", "menu_file_close"));
		
		// tooltip text
		setTooltipText(
				MailResourceLoader.getString(
					"menu", "mainframe", "menu_file_close"));
		
		// action command
		setActionCommand("EXIT");
		
		// large icon for toolbar
		setLargeIcon(ImageLoader.getImageIcon("stock_exit.png"));
		
		// small icon for menu
		setSmallIcon(ImageLoader.getSmallImageIcon("stock_exit-16.png"));
		
		// shortcut key
		setAcceleratorKey(
				KeyStroke.getKeyStroke(
					KeyEvent.VK_W, ActionEvent.CTRL_MASK));
		
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent evt) {
		
		
		//getFrameController().close();
		
		
		/*
		composerInterface.composerController.saveWindowPosition();
		composerInterface.composerController.hideComposerWindow();
		*/
	}

}
