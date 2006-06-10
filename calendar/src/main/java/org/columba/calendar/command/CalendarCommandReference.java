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

import org.columba.calendar.base.api.IActivity;
import org.columba.calendar.base.api.ICalendarItem;
import org.columba.calendar.store.api.ICalendarStore;
import org.columba.core.command.DefaultCommandReference;

public class CalendarCommandReference extends DefaultCommandReference {

	private ICalendarStore store;

	private ICalendarItem srcCalendar;

	private IActivity activity;

	public CalendarCommandReference(ICalendarStore store) {
		this.store = store;
	}

	public CalendarCommandReference(ICalendarStore store,
			ICalendarItem srcCalendar) {
		this(store);

		this.srcCalendar = srcCalendar;
	}

	public CalendarCommandReference(ICalendarStore store, IActivity activity) {
		this(store);

		this.activity = activity;
	}

	public CalendarCommandReference(ICalendarStore store,
			ICalendarItem srcCalendar, IActivity activity) {
		this(store, srcCalendar);

		this.activity = activity;
	}

	/**
	 * @return Returns the store.
	 */
	public ICalendarStore getStore() {
		return store;
	}

	/**
	 * @return Returns the calendarId.
	 */
	public ICalendarItem getSrcCalendar() {
		return srcCalendar;
	}

	/**
	 * @return Returns the eventId.
	 */
	public IActivity getActivity() {
		return activity;
	}

}
