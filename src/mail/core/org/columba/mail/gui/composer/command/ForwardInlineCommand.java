package org.columba.mail.gui.composer.command;

import org.columba.core.command.DefaultCommandReference;
import org.columba.core.command.Worker;
import org.columba.core.xml.XmlElement;
import org.columba.mail.command.FolderCommand;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.composer.MessageBuilder;
import org.columba.mail.config.MailConfig;
import org.columba.mail.folder.Folder;
import org.columba.mail.gui.composer.ComposerController;
import org.columba.mail.message.ColumbaHeader;
import org.columba.mail.message.Message;
import org.columba.mail.message.MimePart;
import org.columba.mail.message.MimePartTree;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>PgetReferences()>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class ForwardInlineCommand extends FolderCommand {

	ComposerController controller;

	/**
	 * Constructor for ForwardInlineCommand.
	 * @param frameController
	 * @param references
	 */
	public ForwardInlineCommand(DefaultCommandReference[] references) {
		super(references);
	}

	/**
	 * @see org.columba.core.command.Command#updateGUI()
	 */
	public void updateGUI() throws Exception {
		controller.showComposerWindow();
	}

	/**
	 * @see org.columba.core.command.Command#execute(Worker)
	 */
	public void execute(Worker worker) throws Exception {
		Folder folder =
			(Folder) ((FolderCommandReference) getReferences()[0]).getFolder();
		Object[] uids = ((FolderCommandReference) getReferences()[0]).getUids();

		Message message = new Message();

		ColumbaHeader header =
			(ColumbaHeader) folder.getMessageHeader(uids[0], worker);
		message.setHeader(header);
		MimePartTree mimePartTree = folder.getMimePartTree(uids[0], worker);
		message.setMimePartTree(mimePartTree);

		XmlElement html =
			MailConfig.getMainFrameOptionsConfig().getRoot().getElement(
				"/options/html");
		boolean viewhtml =
			new Boolean(html.getAttribute("prefer")).booleanValue();

		// Which Bodypart shall be shown? (html/plain)
		MimePart bodyPart = null;

		if (viewhtml)
			bodyPart = mimePartTree.getFirstTextPart("html");
		else
			bodyPart = mimePartTree.getFirstTextPart("plain");

		if (bodyPart == null) {
			bodyPart = new MimePart();
			bodyPart.setBody(new String("<No Message-Text>"));
		} else
			bodyPart =
				folder.getMimePart(uids[0], bodyPart.getAddress(), worker);

		message.setBodyPart(bodyPart);

		controller = new ComposerController();

		MessageBuilder.getInstance().createMessage(
			message,
			controller.getModel(),
			MessageBuilder.FORWARD_INLINE);

	}

	/**
	 * @see org.columba.core.command.Command#undo(Worker)
	 */
	public void undo(Worker worker) throws Exception {
	}

	/**
	 * @see org.columba.core.command.Command#redo(Worker)
	 */
	public void redo(Worker worker) throws Exception {
	}

}
