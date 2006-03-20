package org.columba.calendar.store.api;

import java.util.Iterator;

import org.columba.calendar.model.api.IComponent;
import org.columba.calendar.model.api.IComponentInfoList;

/**
 * Calendar store.
 * 
 * @author fdietz
 */
public interface ICalendarStore {

	IComponent get(Object uid) throws StoreException;

	void add(IComponent calendarModel) throws StoreException;

	void modify(Object uid, IComponent calendarModel) throws StoreException;

	void remove(Object uid) throws StoreException;

	IComponentInfoList getComponentInfoList() throws StoreException;

	Iterator<String> getIdIterator() throws StoreException;
	
	Iterator<String> getIdIterator(String calendarId) throws StoreException;
	
	boolean exists(Object uid) throws StoreException;
}
