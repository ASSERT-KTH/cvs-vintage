/*
 * Created on 08.04.2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.gui.table.action;

import java.awt.event.ActionEvent;

import org.columba.core.action.FrameAction;
import org.columba.core.gui.frame.AbstractFrameController;
import org.columba.core.gui.frame.FrameModel;
import org.columba.core.main.MainInterface;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.gui.frame.MailFrameController;
import org.columba.mail.gui.message.command.ViewMessageCommand;
import org.columba.mail.gui.messageframe.MessageFrameController;

/**
 * @author frd
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class OpenMessageWithMessageFrameAction extends FrameAction {

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
	public OpenMessageWithMessageFrameAction(
		AbstractFrameController frameController) {
		super(
			frameController,
			"Open Message in New Window",
			"Open Message in New Window",
			"OPEN_MESSAGE_IN_NEW_WINDOW",
			null,null,
			0,
			null);
		
	}

	
	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent evt) {
		MessageFrameController c = (MessageFrameController) FrameModel.openView("MessageFrame");
		
		FolderCommandReference[] r = ((MailFrameController)getFrameController()).getTableSelection();
		c.getSelectionManager().getHandler("mail.table").setSelection(r);
		
		MainInterface.processor.addOp(new ViewMessageCommand(c, r));
	}

}
