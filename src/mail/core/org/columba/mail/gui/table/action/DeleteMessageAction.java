/*
 * Created on 11.03.2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.gui.table.action;

import java.awt.event.ActionEvent;

import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import org.columba.core.action.FrameAction;
import org.columba.core.gui.FrameController;
import org.columba.core.gui.util.ImageLoader;
import org.columba.mail.util.MailResourceLoader;

/**
 * @author frd
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class DeleteMessageAction extends FrameAction {

	/**
	 * @param frameController
	 * @param name
	 * @param longDescription
	 * @param tooltip
	 * @param actionCommand
	 * @param small_icon
	 * @param big_icon
	 * @param mnemonic
	 * @param keyStroke
	 * @param showToolbarText
	 */
	public DeleteMessageAction(
		FrameController frameController,
		String name,
		String longDescription,
		String tooltip,
		String actionCommand,
		ImageIcon small_icon,
		ImageIcon big_icon,
		int mnemonic,
		KeyStroke keyStroke,
		boolean showToolbarText) {
		super(
			frameController,
			MailResourceLoader.getString(
				"menu",
				"mainframe",
				"menu_message_delete"),
			MailResourceLoader.getString(
				"menu",
				"mainframe",
				"menu_message_delete_toolbar"),
			MailResourceLoader.getString(
				"menu",
				"mainframe",
				"menu_message_delete_tooltip"),
			"DELETE_MESSAGE",
			ImageLoader.getSmallImageIcon("stock_delete-16.png"),
			ImageLoader.getImageIcon("stock_delete.png"),
			'0',
			null,
			false);

	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent evt) {
		// TODO Auto-generated method stub
		super.actionPerformed(evt);
	}

}
