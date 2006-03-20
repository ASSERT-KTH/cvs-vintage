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
package org.columba.calendar.command;

import java.io.File;

import javax.swing.JOptionPane;

import org.columba.api.command.IWorkerStatusController;
import org.columba.calendar.base.api.IActivity;
import org.columba.calendar.model.api.IComponent;
import org.columba.calendar.parser.CalendarExporter;
import org.columba.calendar.store.api.ICalendarStore;
import org.columba.calendar.store.api.StoreException;
import org.columba.core.command.Command;

public class SaveEventToFileCommand extends Command {

	private File file;

	public SaveEventToFileCommand(CalendarCommandReference ref, File file) {
		super(ref);

		this.file = file;
	}

	@Override
	public void execute(IWorkerStatusController worker) throws Exception {
		ICalendarStore store = ((CalendarCommandReference) getReference())
				.getStore();

		IActivity eventItem = ((CalendarCommandReference) getReference())
				.getActivity();

		try {
			IComponent c = store.get(eventItem.getId());

			new CalendarExporter().exportSingleEvent(file, c, store);
		} catch (StoreException e) {
			JOptionPane.showMessageDialog(null, e.getMessage());
			e.printStackTrace();
		}

	}
}
