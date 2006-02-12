package org.columba.calendar.model;

import java.util.Calendar;



/**
 * Holds data necessary for viewing an activity in the calendar 
 * component.
 * 
 * @author fdietz
 */
public interface IHeaderItem {

	/**
	 * @return Returns the endTimeCalendar.
	 */
	Calendar getEndTimeCalendar();

	/**
	 * @return Returns the startTimeCalendar.
	 */
	Calendar getStartTimeCalendar();

	/**
	 * @return Returns the summary.
	 */
	String getSummary();
	
	String getId();
	
	String getLocation();
	
	String getDescription();

}