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
package org.columba.mail.folder.command;

import java.util.ArrayList;
import java.util.List;

import org.columba.core.command.Command;
import org.columba.core.command.CompoundCommand;
import org.columba.core.command.DefaultCommandReference;
import org.columba.core.command.StatusObservableImpl;
import org.columba.core.command.Worker;
import org.columba.core.command.WorkerStatusController;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.filter.Filter;
import org.columba.mail.filter.FilterList;
import org.columba.mail.folder.MessageFolder;

/**
 * 
 * Apply all filters on this folder.
 * 
 * @author fdietz
 *  
 */
public class ApplyFilterCommand extends Command {
	private List commandList;

	/**
	 * Constructor for ApplyFilterCommand.
	 * 
	 * @param frameMediator
	 * @param references
	 */
	public ApplyFilterCommand(DefaultCommandReference reference) {
		super(reference);
		commandList = new ArrayList();
	}

	/**
	 * @see org.columba.core.command.Command#execute(Worker)
	 */
	public void execute(WorkerStatusController worker) throws Exception {
		// get references
		FolderCommandReference r = (FolderCommandReference) getReference();

		// get source folder
		MessageFolder srcFolder = (MessageFolder) r.getFolder();

		// register for status events
		((StatusObservableImpl) srcFolder.getObservable()).setWorker(worker);

		// display status message
		worker.setDisplayText("Applying filter to " + srcFolder.getName()
				+ "...");

		// get filter list from folder
		FilterList list = srcFolder.getFilterList();

		if (list == null) {
			return;
		}

		// initialize progressbar
		worker.setProgressBarMaximum(list.count());

		// for each filter
		for (int i = 0; i < list.count(); i++) {
			// update progressbar
			worker.setProgressBarValue(i);

			// get filter
			Filter filter = list.get(i);

			// search all messages which match this filter
			Object[] result = srcFolder.searchMessages(filter);

			if (result == null) {
				continue;
			}

			// if we have a result
			if (result.length != 0) {
				// create a Command for every action of this filter
				// -> create a compound object which encapsulates all commands
				CompoundCommand command = filter.getCommand(srcFolder, result);

				// add command to scheduler
				//MainInterface.processor.addOp(command);

				command.execute(worker);

				commandList.add(command);

			}
		}
	}
}