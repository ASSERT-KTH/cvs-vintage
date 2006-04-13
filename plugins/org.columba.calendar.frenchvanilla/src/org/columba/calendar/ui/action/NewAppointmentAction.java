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
package org.columba.calendar.ui.action;

import java.awt.event.ActionEvent;
import java.util.Calendar;

import org.columba.api.gui.frame.IFrameMediator;
import org.columba.calendar.command.AddEventCommand;
import org.columba.calendar.command.CalendarCommandReference;
import org.columba.calendar.model.Event;
import org.columba.calendar.model.api.IDateRange;
import org.columba.calendar.model.api.IEvent;
import org.columba.calendar.resourceloader.IconKeys;
import org.columba.calendar.resourceloader.ResourceLoader;
import org.columba.calendar.store.CalendarStoreFactory;
import org.columba.calendar.store.api.ICalendarStore;
import org.columba.calendar.ui.dialog.EditEventDialog;
import org.columba.core.command.Command;
import org.columba.core.command.CommandProcessor;
import org.columba.core.gui.action.AbstractColumbaAction;

/**
 * @author fdietz
 * 
 */
public class NewAppointmentAction extends AbstractColumbaAction {

	private IDateRange range;

	public NewAppointmentAction(IFrameMediator frameMediator, IDateRange range) {
		this(frameMediator);

		this.range = range;
	}

	/**
	 * @param frameMediator
	 * @param name
	 */
	public NewAppointmentAction(IFrameMediator frameMediator) {
		super(frameMediator, "New Appointment");

		putValue(AbstractColumbaAction.TOOLBAR_NAME, "New Appointment");
		setShowToolBarText(true);

		putValue(AbstractColumbaAction.LARGE_ICON, ResourceLoader
				.getIcon(IconKeys.NEW_APPOINTMENT));
		putValue(AbstractColumbaAction.SMALL_ICON, ResourceLoader
				.getSmallIcon(IconKeys.NEW_APPOINTMENT));
	}

	/**
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		IEvent model = new Event();
		if (range != null) {
			Calendar start = range.getStartTime();
			int min = start.get(Calendar.MINUTE);
			if (min < 30)
				min = 0;
			else
				min = 30;
			start.set(Calendar.MINUTE, min);

			model.setDtStart(start);
			Calendar c = (Calendar) start.clone();
			c.add(Calendar.MINUTE, 30);
			model.setDtEnt(c);
		}

		EditEventDialog dialog = new EditEventDialog(null, model);

		if (dialog.success()) {

			ICalendarStore store = CalendarStoreFactory.getInstance()
					.getLocaleStore();

			Command command = new AddEventCommand(new CalendarCommandReference(
					store), model);
			CommandProcessor.getInstance().addOp(command);

		}
	}

}
