/*
 * Created on 30.03.2003
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.gui.attachment.action;

import java.awt.event.ActionEvent;
import java.util.LinkedList;

import org.columba.core.action.FrameAction;
import org.columba.core.command.DefaultCommandReference;
import org.columba.core.gui.frame.FrameController;
import org.columba.core.gui.util.ImageLoader;
import org.columba.core.main.MainInterface;
import org.columba.mail.gui.attachment.AttachmentController;
import org.columba.mail.gui.attachment.command.OpenAttachmentCommand;
import org.columba.mail.gui.frame.MailFrameController;
import org.columba.mail.message.MimePart;
import org.columba.mail.util.MailResourceLoader;

/**
 * @author frd
 *
 * To change this generated comment go to 
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class OpenAction extends FrameAction {

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
	public OpenAction(FrameController frameController) {
		super(frameController, MailResourceLoader.getString("menu", "mainframe", "attachmentopen"), //$NON-NLS-1$
		MailResourceLoader.getString("menu", "mainframe", "attachmentopen_tooltip"), //$NON-NLS-1$

		"OPEN", //$NON-NLS-1$
		ImageLoader.getSmallImageIcon("stock_open.png"), //$NON-NLS-1$
		ImageLoader.getImageIcon("stock_open.png"), //$NON-NLS-1$
		0, null);

	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent evt) {
		AttachmentController attachmentController = ((MailFrameController) getFrameController()).attachmentController;
		
		int[] selection = attachmentController.getView().getSelection();

		LinkedList list =
			attachmentController.getModel().getDisplayedMimeParts();

		for (int i = 0; i < selection.length; i++) {
			Integer[] address = new Integer[selection.length];

			MimePart mimePart = (MimePart) list.get(selection[i]);
			address = mimePart.getAddress();

			attachmentController
				.getAttachmentSelectionManager()
				.fireAttachmentSelectionEvent(null, address);

			DefaultCommandReference[] commandReference =
				attachmentController
					.getAttachmentSelectionManager()
					.getSelection();

			MainInterface.processor.addOp(
				new OpenAttachmentCommand(commandReference));
		}
	}

}
