/*
 * Created on 18.07.2003
 *
 * To change the template for this generated file go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
package org.columba.mail.pop3.command;

import org.columba.core.command.CompoundCommand;
import org.columba.core.command.DefaultCommandReference;
import org.columba.core.command.Worker;
import org.columba.core.gui.frame.AbstractFrameController;
import org.columba.core.main.MainInterface;
import org.columba.mail.command.FolderCommand;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.filter.Filter;
import org.columba.mail.filter.FilterList;
import org.columba.mail.folder.Folder;
import org.columba.mail.gui.frame.TableUpdater;
import org.columba.mail.gui.table.TableChangedEvent;
import org.columba.mail.message.HeaderInterface;
import org.columba.mail.message.Message;

/**
 * @author frd
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class AddPOP3MessageCommand extends FolderCommand {

	Folder inboxFolder;
	HeaderInterface[] headerList;
	/**
	 * @param references
	 */
	public AddPOP3MessageCommand(DefaultCommandReference[] references) {
		super(references);

	}

	/**
	 * @param frame
	 * @param references
	 */
	public AddPOP3MessageCommand(
		AbstractFrameController frame,
		DefaultCommandReference[] references) {
		super(frame, references);

	}

	/* (non-Javadoc)
	 * @see org.columba.core.command.Command#execute(org.columba.core.command.Worker)
	 */
	public void execute(Worker worker) throws Exception {
		FolderCommandReference[] r = (FolderCommandReference[]) getReferences();

		inboxFolder = (Folder) r[0].getFolder();
		Message message = (Message) r[0].getMessage();

		// add message to folder
		Object uid = inboxFolder.addMessage(message, worker);
		Object[] uids = new Object[1];
		uids[0] = uid;

		// generate headerlist we need to update the table viewer
		headerList = new HeaderInterface[1];
		headerList[0] = message.getHeader();
		headerList[0].set("columba.uid", uid);

		// apply filter on message
		FilterList list = inboxFolder.getFilterList();
		for (int j = 0; j < list.count(); j++) {
			Filter filter = list.get(j);

			Object[] result = inboxFolder.searchMessages(filter, uids, worker);
			if (result.length != 0) {
				CompoundCommand command =
					filter.getCommand(inboxFolder, result);

				MainInterface.processor.addOp(command);
			}
		}

	}

	/* (non-Javadoc)
	 * @see org.columba.core.command.Command#updateGUI()
	 */
	public void updateGUI() throws Exception {
		// update table viewer
		TableChangedEvent ev =
			new TableChangedEvent(
				TableChangedEvent.UPDATE,
				inboxFolder);

		TableUpdater.tableChanged(ev);

		// update tree viewer
		MainInterface.treeModel.nodeChanged(inboxFolder);
	}

}
