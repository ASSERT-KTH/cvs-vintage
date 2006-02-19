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

import org.columba.calendar.model.api.IEventInfo;
import org.columba.calendar.model.api.IComponent.TYPE;

public class EventInfo extends ComponentInfo implements IEventInfo {
	private Calendar dtStart;

	private Calendar dtEnt;

	private String summary;

	private String calendar;

	public EventInfo(String id, Calendar dtStart, Calendar dtEnd,
			String summary, String calendar) {
		super(id, TYPE.EVENT);

		if (dtStart == null)
			throw new IllegalArgumentException("dtStart == null");

		if (dtEnd == null)
			throw new IllegalArgumentException("dtEnd == null");

		if (summary == null)
			throw new IllegalArgumentException("summary == null");

		if (calendar == null)
			throw new IllegalArgumentException("calendar == null");

		this.dtStart = dtStart;
		this.dtEnt = dtEnd;
		this.summary = summary;
		this.calendar = calendar;
	}

	public Calendar getDtStart() {
		return dtStart;
	}

	public Calendar getDtEnt() {
		return dtEnt;
	}

	public String getSummary() {
		return summary;
	}

	public String getCalendar() {
		return calendar;
	}

}
