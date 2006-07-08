package org.columba.mail.gui.message.viewer;

import java.util.TimerTask;

import org.columba.core.command.CommandProcessor;
import org.columba.mail.command.IMailFolderCommandReference;
import org.columba.mail.folder.command.MarkMessageCommand;
import org.columba.mail.gui.message.IMessageController;

public class MarkAsReadTimerTask extends TimerTask {

	private IMessageController controller;
	private IMailFolderCommandReference r;
	
	
	/**
	 * @param controller
	 * @param r
	 */
	public MarkAsReadTimerTask(IMessageController controller, IMailFolderCommandReference r) {
		this.controller = controller;
		this.r = r;
	}

	public void run() {
		// If the same message is still shown
		// Mark it as read
		if(controller.getSelectedFolder() != null && controller.getSelectedFolder().equals(r.getSourceFolder()) && controller.getSelectedMessageId() != null && controller.getSelectedMessageId().equals(r.getUids()[0])) {		
			r.setMarkVariant(MarkMessageCommand.MARK_AS_READ);

			MarkMessageCommand c = new MarkMessageCommand(r);

			CommandProcessor.getInstance().addOp(c);
		}
	}
}
