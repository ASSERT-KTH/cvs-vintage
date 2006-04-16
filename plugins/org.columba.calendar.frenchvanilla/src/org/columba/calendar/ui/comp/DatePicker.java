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
package org.columba.calendar.ui.comp;

import java.awt.Dimension;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import org.columba.calendar.model.api.IDateRange;
import org.columba.calendar.ui.navigation.DateAreaBeanFactory;

import com.miginfocom.calendar.datearea.DateArea;
import com.miginfocom.util.dates.BoundaryRounder;
import com.miginfocom.util.dates.DateRangeI;
import com.miginfocom.util.dates.ImmutableDateRange;

public class DatePicker extends com.miginfocom.calendar.DatePicker {

	private com.miginfocom.beans.DateAreaBean dateAreaBean;

	public DatePicker() {
		super();

		dateAreaBean = DateAreaBeanFactory.initDateArea();

		// enable selection
		dateAreaBean.setSelectionType(DateArea.SELECTION_TYPE_NORMAL);

		long startMillis = new GregorianCalendar(2006, 0, 0).getTimeInMillis();
		long endMillis = new GregorianCalendar(2006, 12, 31).getTimeInMillis();
		ImmutableDateRange dr = new ImmutableDateRange(startMillis, endMillis,
				false, null, null);
		dateAreaBean.getDateArea().setVisibleDateRange(dr);
		dateAreaBean.setPreferredSize(new Dimension(200, 400));

		dateAreaBean.setSelectionBoundaryType(DateRangeI.RANGE_TYPE_DAY);
		dateAreaBean.getDateArea().setSelectionRounder(
				new BoundaryRounder(DateRangeI.RANGE_TYPE_DAY, true, true,
						false, 1, 1, null));
		dateAreaBean.repaint();
		setDateAreaContainer(dateAreaBean);

		setHomeButtonVisible(true);
		setLeftRightButtonsVisible(true);
		setDefaultDateStyle(DateFormat.DEFAULT);
		setHideEndDate(true);

		setDate(Calendar.getInstance());
	}

	public void setDate(Calendar date) {
		ImmutableDateRange dr = new ImmutableDateRange(date.getTimeInMillis(),
				date.getTimeInMillis(), false, null, null);

		setSelectedRange(dr);
	}

	public Calendar getDate() {
		DateRangeI range = getSelectedRange();

		return range.getStart();
	}

	public void setSelectedColumbaDateRange(IDateRange range) {
		ImmutableDateRange dr = new ImmutableDateRange(range.getStartTime()
				.getTimeInMillis(), range.getEndTime().getTimeInMillis(), true,
				null, null);

		setSelectedRange(dr);
	}

}
