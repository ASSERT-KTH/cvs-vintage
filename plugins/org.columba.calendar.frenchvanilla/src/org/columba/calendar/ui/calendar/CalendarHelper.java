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
package org.columba.calendar.ui.calendar;

import org.columba.calendar.config.Config;
import org.columba.calendar.model.DateRange;
import org.columba.calendar.model.api.IEvent;
import org.columba.calendar.model.api.IEventInfo;

import com.miginfocom.calendar.activity.Activity;
import com.miginfocom.calendar.activity.DefaultActivity;
import com.miginfocom.util.dates.DateRangeI;
import com.miginfocom.util.dates.ImmutableDateRange;

public class CalendarHelper {

	public static Activity createActivity(IEventInfo model) {

		long startMillis = model.getDtStart().getTimeInMillis();
		long endMillis = model.getDtEnt().getTimeInMillis();
		ImmutableDateRange dr = new ImmutableDateRange(startMillis, endMillis,
				false, null, null);

		// A recurring event
		Activity act = new DefaultActivity(dr, model.getId());
		act.setSummary(model.getSummary());
		// act.setLocation(model.getLocation());
		// act.setDescription(model.getDescription());

		String calendar = model.getCalendar();
		act.setCategoryIDs(new Object[] { calendar });
		
		return act;
	}

	public static Activity createActivity(IEvent model) {

		long startMillis = model.getDtStart().getTimeInMillis();
		long endMillis = model.getDtEnt().getTimeInMillis();
		ImmutableDateRange dr = new ImmutableDateRange(startMillis, endMillis,
				false, null, null);

		// A recurring event
		Activity act = new DefaultActivity(dr, model.getId());
		act.setSummary(model.getSummary());
		act.setLocation(model.getLocation());
		act.setDescription(model.getDescription());

		String calendar = model.getCalendar();
		act.setCategoryIDs(new Object[] { calendar });
		
		return act;
	}

	public static void updateDateRange(final Activity activity, IEvent model) {
		DateRangeI dateRange = activity.getDateRangeForReading();
		DateRange cRange = new DateRange(dateRange.getStartMillis(), dateRange
				.getEndMillis(false));

		model.setDtStart(cRange.getStartTime());
		model.setDtEnt(cRange.getEndTime());

	}

}
