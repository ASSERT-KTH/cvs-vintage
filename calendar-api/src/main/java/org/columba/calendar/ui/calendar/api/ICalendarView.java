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
package org.columba.calendar.ui.calendar.api;

import javax.swing.JComponent;

import org.columba.calendar.base.api.IActivity;
import org.columba.calendar.model.api.IDateRange;


public interface ICalendarView {

	public static final int VIEW_MODE_DAY = 0;
	public static final int VIEW_MODE_WEEK = 1;
	public static final int VIEW_MODE_WORK_WEEK = 2;
	public static final int VIEW_MODE_MONTH = 3;

	public abstract IActivity getSelectedActivity();
	
	public abstract JComponent getView();
	
	public abstract void viewToday();
	public abstract void viewNext();
	public abstract void viewPrevious();
	
	public abstract void setViewMode(int mode);
	
	public abstract void setVisibleDateRange(IDateRange dateRange);
	
	public abstract void recreateFilterRows();
	
	public abstract void addSelectionChangedListener(IActivitySelectionChangedListener listener);
	public abstract void removeSelectionChangedListener(IActivitySelectionChangedListener listener);
}
