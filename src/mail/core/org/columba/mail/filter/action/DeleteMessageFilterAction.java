package org.columba.mail.filter.action;

import org.columba.core.command.Command;
import org.columba.core.gui.FrameController;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.filter.FilterAction;
import org.columba.mail.folder.Folder;
import org.columba.mail.folder.command.ExpungeFolderCommand;
import org.columba.mail.folder.command.MarkMessageCommand;
import org.columba.main.MainInterface;

/**
 * @author freddy
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class DeleteMessageFilterAction extends AbstractFilterAction {

	/**
	 * Constructor for DeleteMessageFilterAction.
	 * @param frameController
	 * @param filterAction
	 * @param srcFolder
	 * @param uids
	 */
	public DeleteMessageFilterAction(
		FrameController frameController,
		FilterAction filterAction,
		Folder srcFolder,
		Object[] uids) {
		super(frameController, filterAction, srcFolder, uids);
	}

	/**
	 * @see org.columba.modules.mail.filter.action.AbstractFilterAction#execute()
	 */
	public Command getCommand() throws Exception {
		FolderCommandReference[] r = new FolderCommandReference[1];
		r[0] = new FolderCommandReference(srcFolder, uids);
		r[0].setMarkVariant(MarkMessageCommand.MARK_AS_EXPUNGED);

		MarkMessageCommand c = new MarkMessageCommand(frameController, r);

		MainInterface.processor.addOp(c);

		r = new FolderCommandReference[1];
		r[0] = new FolderCommandReference(srcFolder);

		return new ExpungeFolderCommand(frameController, r);
	}

}
