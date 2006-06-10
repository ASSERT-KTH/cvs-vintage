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
package org.columba.calendar.ui.navigation;

import java.util.GregorianCalendar;

import javax.swing.JComponent;
import javax.swing.event.EventListenerList;

import org.columba.calendar.model.DateRange;
import org.columba.calendar.model.api.IDateRange;
import org.columba.calendar.ui.navigation.api.DateRangeChangedEvent;
import org.columba.calendar.ui.navigation.api.ICalendarNavigationView;
import org.columba.calendar.ui.navigation.api.IDateRangeChangedListener;

import com.miginfocom.calendar.datearea.DateArea;
import com.miginfocom.util.dates.BoundaryRounder;
import com.miginfocom.util.dates.DateChangeEvent;
import com.miginfocom.util.dates.DateChangeListener;
import com.miginfocom.util.dates.DateRangeI;
import com.miginfocom.util.dates.ImmutableDateRange;

/**
 * @author fdietz
 * 
 */
public class NavigationController implements ICalendarNavigationView {

	public static final String MINI_CONTEXT = "mini";

	public static final int SELECTION_MODE_DAY = 0;

	public static final int SELECTION_MODE_WEEK = 1;

	public static final int SELECTION_MODE_WORK_WEEK = 2;

	public static final int SELECTION_MODE_MONTH = 3;

	private EventListenerList listenerList = new EventListenerList();

	private com.miginfocom.beans.DateAreaBean dateAreaBean;

	public NavigationController() {

		dateAreaBean = DateAreaBeanFactory.initDateArea();

		// enable selection
		dateAreaBean.setSelectionType(DateArea.SELECTION_TYPE_NORMAL);

		long startMillis = new GregorianCalendar(2006, 0, 0).getTimeInMillis();
		long endMillis = new GregorianCalendar(2006, 12, 31).getTimeInMillis();
		ImmutableDateRange dr = new ImmutableDateRange(startMillis, endMillis,
				false, null, null);

		dateAreaBean.getDateArea().setVisibleDateRange(dr);
		dateAreaBean.repaint();

		dateAreaBean.getDateArea().addDateChangeListener(
				new DateChangeListener() {
					public void dateRangeChanged(DateChangeEvent e) {
						if (e.getType() == DateChangeEvent.SELECTED) {
							

							fireSelectionChanged(new DateRange(e.getNewRange()
									.getStartMillis(), e.getNewRange()
									.getEndMillis(false)));

						}
					}
				}, false);
	}

	public JComponent getView() {
		return dateAreaBean;
	}

	/**
	 * Adds a listener.
	 */
	public void addSelectionChangedListener(IDateRangeChangedListener listener) {
		listenerList.add(IDateRangeChangedListener.class, listener);
	}

	/**
	 * Removes a previously registered listener.
	 */
	public void removeSelectionChangedListener(
			IDateRangeChangedListener listener) {
		listenerList.remove(IDateRangeChangedListener.class, listener);
	}

	/**
	 * Propagates an event to all registered listeners notifying them that this
	 * folder has been renamed.
	 */
	public void fireSelectionChanged(IDateRange dateRange) {
		DateRangeChangedEvent e = new DateRangeChangedEvent(this, dateRange);
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();

		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == IDateRangeChangedListener.class) {
				((IDateRangeChangedListener) listeners[i + 1])
						.selectionChanged(e);
			}
		}
	}

	public void setSelectionMode(int mode) {

		int selectionBoundary = -1;
		int unitCount = -1;

		switch (mode) {
		case SELECTION_MODE_DAY:
			selectionBoundary = DateRangeI.RANGE_TYPE_DAY;
			unitCount = 1;

			break;
		case SELECTION_MODE_WEEK:
			selectionBoundary = DateRangeI.RANGE_TYPE_WEEK;
			unitCount = 1;

			break;
		case SELECTION_MODE_WORK_WEEK:
			selectionBoundary = DateRangeI.RANGE_TYPE_DAY;
			unitCount = 5;

			break;
		case SELECTION_MODE_MONTH:
			selectionBoundary = DateRangeI.RANGE_TYPE_MONTH;
			unitCount = 1;

			break;
		}

		dateAreaBean.setSelectionBoundaryType(selectionBoundary);
		dateAreaBean.getDateArea().setSelectionRounder(
				new BoundaryRounder(selectionBoundary, true, true, false,
						unitCount, unitCount, null));

	}

}