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

import org.columba.core.command.Command;
import org.columba.core.command.CompoundCommand;
import org.columba.core.command.ICommandReference;
import org.columba.core.command.StatusObservableImpl;
import org.columba.core.command.Worker;
import org.columba.core.command.WorkerStatusController;
import org.columba.core.filter.Filter;
import org.columba.core.filter.FilterList;
import org.columba.mail.command.MailFolderCommandReference;
import org.columba.mail.filter.FilterCompoundCommand;
import org.columba.mail.folder.AbstractMessageFolder;

/**
 * 
 * Apply all filters on this folder.
 * 
 * @author fdietz
 *  
 */
public class ApplyFilterCommand extends Command {
	/**
	 * Constructor for ApplyFilterCommand.
	 * 
	 * @param frameMediator
	 * @param references
	 */
	public ApplyFilterCommand(ICommandReference reference) {
		super(reference);
	}

	/**
	 * @see org.columba.core.command.Command#execute(Worker)
	 */
	public void execute(WorkerStatusController worker) throws Exception {
		// get references
		MailFolderCommandReference r = (MailFolderCommandReference) getReference();

		// get source folder
		AbstractMessageFolder srcFolder = (AbstractMessageFolder) r.getSourceFolder();

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
			
			// skip, if filter is disabled
			if ( filter.getEnabled() == false) continue;
			
			// search all messages which match this filter
			Object[] result = srcFolder.searchMessages(filter);

			if (result == null) {
				continue;
			}

			// if we have a result
			if (result.length != 0) {
				// create a Command for every action of this filter
				// -> create a compound object which encapsulates all commands
				CompoundCommand command = new FilterCompoundCommand(filter, srcFolder, result);

				// add command to scheduler
				//MainInterface.processor.addOp(command);

				command.execute(worker);
			}
		}
	}
}