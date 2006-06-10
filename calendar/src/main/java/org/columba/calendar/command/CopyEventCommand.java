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

import javax.swing.JOptionPane;

import org.columba.api.command.IWorkerStatusController;
import org.columba.calendar.base.api.IActivity;
import org.columba.calendar.base.api.ICalendarItem;
import org.columba.calendar.model.api.IEvent;
import org.columba.calendar.store.api.ICalendarStore;
import org.columba.calendar.store.api.StoreException;
import org.columba.core.command.Command;

public class CopyEventCommand extends Command {

	public CopyEventCommand(CalendarCommandReference ref) {
		super(ref);

	}

	@Override
	public void execute(IWorkerStatusController worker) throws Exception {
		ICalendarStore store = ((CalendarCommandReference) getReference())
				.getStore();

		ICalendarItem calendar = ((CalendarCommandReference) getReference())
				.getSrcCalendar();

		IActivity eventItem = ((CalendarCommandReference) getReference())
				.getActivity();

		try {
			// retrieve event from store
			IEvent event = (IEvent) store.get(eventItem.getId());

			IEvent copy = (IEvent) event.createCopy();

			// set new calendar id
			copy.setCalendar(calendar.getId());

			// persist modified calendar
			store.add(copy);

		} catch (StoreException e1) {
			JOptionPane.showMessageDialog(null, e1.getMessage());
			e1.printStackTrace();
		}
	}
}
