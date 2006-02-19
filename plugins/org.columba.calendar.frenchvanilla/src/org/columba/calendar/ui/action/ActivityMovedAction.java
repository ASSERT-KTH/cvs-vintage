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
import org.columba.calendar.model.api.IEvent;
import org.columba.calendar.store.CalendarStoreFactory;
import org.columba.calendar.store.api.ICalendarStore;
import org.columba.calendar.store.api.StoreException;
import org.columba.calendar.ui.base.api.IActivity;
import org.columba.calendar.ui.calendar.api.ICalendarView;
import org.columba.calendar.ui.dialog.EditEventDialog;
import org.columba.calendar.ui.frame.CalendarFrameMediator;
import org.columba.core.gui.action.AbstractColumbaAction;

/**
 * Update backend after an activity was moved in the calendar widget.
 * 
 * @author fdietz
 * 
 */
public class ActivityMovedAction extends AbstractColumbaAction {

	private Calendar start;

	private Calendar end;

	public ActivityMovedAction(IFrameMediator frameMediator, Calendar start,
			Calendar end) {
		super(frameMediator, "Activity Moved");

		this.start = start;
		this.end = end;
	}

	public void actionPerformed(ActionEvent e) {
		CalendarFrameMediator m = (CalendarFrameMediator) getFrameMediator();

		ICalendarView c = m.getCalendarView();

		IActivity activity = c.getSelectedActivity();

		String id = (String) activity.getId();

		ICalendarStore store = CalendarStoreFactory.getInstance()
				.getLocaleStore();

		// retrieve event from store
		try {
			IEvent model = (IEvent) store.get(id);

			// update start/end time
			model.setDtStart(start);
			model.setDtEnt(end);

			// update store
			store.modify(id, model);

		} catch (StoreException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

}
