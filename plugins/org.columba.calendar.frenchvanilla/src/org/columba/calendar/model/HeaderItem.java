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
package org.columba.calendar.model;

import java.util.Calendar;

public class HeaderItem implements IHeaderItem {

	private String summary;
	
	private String location;
	
	private String description;

	private Calendar startTimeCalendar;

	private Calendar endTimeCalendar;
	
	private String id;

	public HeaderItem(String summary) {
		this.summary = summary;
	}

	public HeaderItem(String id, String summary, Calendar startTimeCalendar,
			Calendar endTimeCalendar) {
		this.id = id;
		this.summary = summary;
		this.startTimeCalendar = startTimeCalendar;
		this.endTimeCalendar = endTimeCalendar;
	}

	public HeaderItem(ICalendarModel model) {

		// TODO: fill-in parameters
		this.summary = "summary";

		this.startTimeCalendar = Calendar.getInstance();
		this.endTimeCalendar = Calendar.getInstance();
	}

	/**
	 * @see org.columba.calendar.model.IHeaderItem#getEndTimeCalendar()
	 */
	public Calendar getEndTimeCalendar() {
		return endTimeCalendar;
	}

	/**
	 * @see org.columba.calendar.model.IHeaderItem#getStartTimeCalendar()
	 */
	public Calendar getStartTimeCalendar() {
		return startTimeCalendar;
	}

	/**
	 * @see org.columba.calendar.model.IHeaderItem#getSummary()
	 */
	public String getSummary() {
		return summary;
	}

	/**
	 * @return Returns the id.
	 */
	public String getId() {
		return id;
	}

	public String getLocation() {
		return location;
	}

	public String getDescription() {
		return description;
	}
}
