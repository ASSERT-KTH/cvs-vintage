//The contents of this file are subject to the Mozilla Public License Version 1.1
//(the "License"); you may not use this file except in compliance with the 
//License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
//
//Software distributed under the License is distributed on an "AS IS" basis,
//WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License 
//for the specific language governing rights and
//limitations under the License.
//
//The Original Code is "The Columba Project"
//
//The Initial Developers of the Original Code are Frederik Dietz and Timo Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003. 
//
//All Rights Reserved.
package org.columba.mail.gui.table.command;

import org.columba.core.command.Command;
import org.columba.core.command.DefaultCommandReference;
import org.columba.core.command.StatusObservableImpl;
import org.columba.core.command.Worker;
import org.columba.core.command.WorkerStatusController;
import org.columba.core.gui.frame.FrameMediator;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.folder.AbstractMessageFolder;
import org.columba.mail.gui.frame.TableViewOwner;
import org.columba.mail.message.HeaderList;

/**
 * @author Timo Stich (tstich@users.sourceforge.net)
 *  
 */
public class ViewHeaderListCommand extends Command {
	private HeaderList headerList;

	private AbstractMessageFolder folder;

	public ViewHeaderListCommand(FrameMediator frame,
			DefaultCommandReference reference) {
		super(frame, reference);

		priority = Command.REALTIME_PRIORITY;

	}

	/**
	 * @see org.columba.core.command.Command#updateGUI()
	 */
	public void updateGUI() throws Exception {

		((TableViewOwner) frameMediator).getTableController().showHeaderList(
				folder, headerList);

	}

	/**
	 * @see org.columba.core.command.Command#execute(Worker)
	 */
	public void execute(WorkerStatusController worker) throws Exception {
		FolderCommandReference r = (FolderCommandReference) getReference();

		folder = (AbstractMessageFolder) r.getFolder();

		//		register for status events
		((StatusObservableImpl) folder.getObservable()).setWorker(worker);

		// fetch the headerlist
		headerList = (folder).getHeaderList();
		//TODO: Handle CommandCancelledException

	}
}