// The contents of this file are subject to the Mozilla Public License Version
// 1.1
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
//The Initial Developers of the Original Code are Frederik Dietz and Timo
// Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003.
//
//All Rights Reserved.
package org.columba.mail.folder.command;

import java.awt.Color;

import org.columba.core.command.DefaultCommandReference;
import org.columba.core.command.StatusObservableImpl;
import org.columba.core.command.Worker;
import org.columba.core.command.WorkerStatusController;
import org.columba.core.gui.util.ColorFactory;
import org.columba.mail.command.FolderCommand;
import org.columba.mail.command.FolderCommandReference;
import org.columba.mail.folder.MessageFolder;

/**
 * Mark selected messages with specific variant.
 * <p>
 * 
 * Variant can be: - read/unread - flagged/unflagged - expunged/unexpunged -
 * answered
 * 
 * @author fdietz
 */
public class ColorMessageCommand extends FolderCommand {

	/**
	 * Constructor for MarkMessageCommand.
	 * 
	 * @param frameMediator
	 * @param references
	 */
	public ColorMessageCommand(DefaultCommandReference reference) {
		super(reference);
	}

	/**
	 * @see org.columba.core.command.Command#execute(Worker)
	 */
	public void execute(WorkerStatusController worker) throws Exception {

		// get array of source references
		FolderCommandReference r = (FolderCommandReference) getReference();

		// get array of message UIDs
		Object[] uids = r.getUids();

		// get source folder
		MessageFolder srcFolder = (MessageFolder) r.getFolder();

		// register for status events
		((StatusObservableImpl) srcFolder.getObservable()).setWorker(worker);

		// which kind of color?
		int rgbValue = r.getColorValue();

		// saving last selected Message to the folder
		srcFolder.setLastSelection(uids[0]);

		// get color from factory
		// ->factory shares color objects to save memory
		Color color = ColorFactory.getColor(rgbValue);

		// for each message
		for (int j = 0; j < uids.length; j++) {
			// set columba.color flag
			srcFolder.setAttribute(uids[j], "columba.color", color);
		}

	}
}