/*
 * Created on 30.04.2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.gui.table.command;

import javax.swing.JOptionPane;

import org.columba.core.command.DefaultCommandReference;
import org.columba.core.command.Worker;
import org.columba.core.gui.frame.AbstractFrameController;
import org.columba.core.main.MainInterface;
import org.columba.mail.command.FolderCommand;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.composer.MessageBuilder;
import org.columba.mail.composer.MessageComposer;
import org.columba.mail.composer.SendableMessage;
import org.columba.mail.config.AccountItem;
import org.columba.mail.folder.Folder;
import org.columba.mail.folder.command.AddMessageCommand;
import org.columba.mail.gui.composer.ComposerModel;
import org.columba.mail.message.ColumbaHeader;
import org.columba.mail.message.Message;
import org.columba.mail.message.MimePartTree;
import org.columba.mail.smtp.SMTPException;
import org.columba.mail.smtp.SMTPServer;

/**
 * @author frd
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class BounceCommand extends FolderCommand {

	/**
	 * @param frame
	 * @param references
	 */
	public BounceCommand(
		AbstractFrameController frame,
		DefaultCommandReference[] references) {
		super(frame, references);

	}

	public BounceCommand(DefaultCommandReference[] references) {
		super(references);

	}

	/* (non-Javadoc)
	 * @see org.columba.core.command.Command#execute(org.columba.core.command.Worker)
	 */
	public void execute(Worker worker) throws Exception {
		Folder folder =
			(Folder) ((FolderCommandReference) getReferences()[0]).getFolder();
		Object[] uids = ((FolderCommandReference) getReferences()[0]).getUids();

		// create new message
		Message message = new Message();

		// copy header of message we want to bounce
		ColumbaHeader header =
			(ColumbaHeader) folder.getMessageHeader(uids[0], worker);
		message.setHeader(header);
		
		// copy mimeparts of bounce message
		MimePartTree mimePartTree = folder.getMimePartTree(uids[0], worker);
		message.setMimePartTree(mimePartTree);
		
		// copy message-source of bounce message
		String source = folder.getMessageSource(uids[0], worker);
		message.setSource(source);

		// create composer-model
		// this encapsulates the data we need to
		// create a new message
		// and keeps the gui separated from the data
		ComposerModel model = new ComposerModel();

		// use bounce message to pass the values
		// to the model
		MessageBuilder.bounceMessage(message, model);
		
		// create new message from model
		SendableMessage sendableMessage =
			new MessageComposer(model).compose(worker);

		// get user-configurable Sent-Folder
		AccountItem item = model.getAccountItem();
		Folder sentFolder =
			(Folder) MainInterface.treeModel.getFolder(
				item.getSpecialFoldersItem().getInteger("sent"));

		// the following code should be better put somewhere else
		// because it could be shared with SendMessageCommand
		
		// open connection to smtp-server
		SMTPServer server = new SMTPServer(item);
		boolean open = server.openConnection();

		if (open) {

			try {
				// send message
				server.sendMessage(sendableMessage, worker);

				FolderCommandReference[] ref = new FolderCommandReference[1];
				ref[0] = new FolderCommandReference(sentFolder);
				ref[0].setMessage(sendableMessage);

				// add message to Sent-Folder
				AddMessageCommand c = new AddMessageCommand(ref);

				MainInterface.processor.addOp(c);

				server.closeConnection();
			} catch (SMTPException e) {
				JOptionPane.showMessageDialog(
					null,
					e.getMessage(),
					"Error while sending",
					JOptionPane.ERROR_MESSAGE);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

}
