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

import org.columba.calendar.model.api.IDateRange;

public class DateRange implements IDateRange {

	private Calendar startTime;

	private Calendar endTime;

	public DateRange(Calendar startTime, Calendar endTime) {
		this.startTime = startTime;
		this.endTime = endTime;
	}

	public DateRange(long startMillis, long endMillis) {
		this.startTime = Calendar.getInstance();
		this.startTime.setTimeInMillis(startMillis);
		this.endTime = Calendar.getInstance();
		this.endTime.setTimeInMillis(endMillis);
	}

	/**
	 * @return Returns the endTime.
	 */
	public Calendar getEndTime() {
		return endTime;
	}

	/**
	 * @return Returns the startTime.
	 */
	public Calendar getStartTime() {
		return startTime;
	}

}
