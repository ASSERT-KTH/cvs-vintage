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
import org.columba.calendar.model.api.IEvent;
import org.columba.calendar.store.api.ICalendarStore;
import org.columba.calendar.store.api.StoreException;
import org.columba.core.command.Command;

public class AddEventCommand extends Command {

	private IEvent event;
	
	public AddEventCommand(CalendarCommandReference ref, IEvent event) {
		super(ref);
		
		this.event = event;
	}
	
	@Override
	public void execute(IWorkerStatusController worker) throws Exception {
		ICalendarStore store = ((CalendarCommandReference)getReference()).getStore();
		
		try {
			store.add(event);
		} catch (StoreException e) {
			JOptionPane.showMessageDialog(null, e.getMessage());
			e.printStackTrace();
		}
	}
}
