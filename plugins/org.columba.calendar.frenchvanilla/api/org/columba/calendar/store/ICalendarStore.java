package org.columba.calendar.store;


import org.columba.calendar.model.ICalendarModel;
import org.columba.calendar.model.IHeaderItemList;
import org.columba.core.util.InternalException;

/**
 * Calendar store.
 * 
 * @author fdietz
 */
public interface ICalendarStore {

    ICalendarModel get(Object uid) throws IllegalArgumentException, InternalException;

	void add(ICalendarModel calendarModel) throws IllegalArgumentException,
			InternalException;

	void modify(Object uid, ICalendarModel calendarModel) throws IllegalArgumentException,
			InternalException;

	void remove(Object uid) throws IllegalArgumentException, InternalException;
	
	IHeaderItemList getHeaderItemList() throws IllegalArgumentException, InternalException;
	
	boolean exists(Object uid) throws IllegalArgumentException, InternalException;
}
