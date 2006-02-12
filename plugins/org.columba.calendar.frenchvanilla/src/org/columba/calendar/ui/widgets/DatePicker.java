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
package org.columba.calendar.ui.widgets;

import java.io.InputStream;
import java.util.Calendar;

import org.columba.calendar.model.ColumbaDateRange;

import com.miginfocom.calendar.ThemeDatePicker;
import com.miginfocom.theme.Themes;
import com.miginfocom.util.dates.DateRangeI;
import com.miginfocom.util.dates.ImmutableDateRange;

public class DatePicker extends ThemeDatePicker {

	private static final String DP_THEME_CTX1 = "datePicker1";

	public DatePicker() {
		super();

		try {
			InputStream is = getClass().getResourceAsStream(
					"/org/columba/calendar/themes/DatePicker1.tme");
			Themes.loadTheme(is, DP_THEME_CTX1, true, true);
			is.close();

		} catch (Exception e) {
			e.printStackTrace();
		}

		setThemeContext(DP_THEME_CTX1);

		// setHomeButtonVisible(true);
		setLeftRightButtonsVisible(true);

		setSelectedRange(new ImmutableDateRange());
	}

	public void setDate(Calendar date) {
		ImmutableDateRange dr = new ImmutableDateRange(date.getTimeInMillis(), date.getTimeInMillis(), false, null, null);

		setSelectedRange(dr);
	}
	
	public Calendar getDate() {
		DateRangeI range = getSelectedRange();
		
		return range.getStart();
	}

	public void setSelectedColumbaDateRange(ColumbaDateRange range) {
		ImmutableDateRange dr = new ImmutableDateRange(range.getStartTime()
				.getTimeInMillis(), range.getEndTime().getTimeInMillis(),
				false, null, null);

		setSelectedRange(dr);
	}

}
