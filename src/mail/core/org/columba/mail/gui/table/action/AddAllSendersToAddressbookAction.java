/*
 * Created on 11.03.2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.gui.table.action;

import java.awt.event.ActionEvent;

import org.columba.core.action.FrameAction;
import org.columba.core.gui.FrameController;
import org.columba.core.main.MainInterface;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.folder.command.AddAllSendersToAddressbookCommand;
import org.columba.mail.util.MailResourceLoader;

/**
 * @author frd
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class AddAllSendersToAddressbookAction extends FrameAction {

	/**
	 * @param frameController
	 * @param name
	 * @param longDescription
	 * @param actionCommand
	 * @param small_icon
	 * @param big_icon
	 * @param mnemonic
	 * @param keyStroke
	 */
	public AddAllSendersToAddressbookAction(FrameController frameController) {
		super(
			frameController,
			MailResourceLoader.getString(
				"menu",
				"mainframe",
				"menu_message_addallsenders"),
			MailResourceLoader.getString(
				"menu",
				"mainframe",
				"menu_message_addallsenders"),
			"ADD_ALLSENDERS",
			null,
			null,
			'0',
			null);

	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent evt) {
		FolderCommandReference[] r =
			(FolderCommandReference[]) getFrameController()
				.getSelectionManager()
				.getSelection("mail.table");

		MainInterface.processor.addOp(
			new AddAllSendersToAddressbookCommand(getFrameController(), r));
	}

}
