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
package org.columba.calendar.store;

import java.util.logging.Logger;

import javax.swing.event.EventListenerList;

import org.columba.calendar.model.ICalendarModel;
import org.columba.calendar.model.IHeaderItemList;
import org.columba.calendar.store.event.StoreEvent;
import org.columba.calendar.store.event.StoreEventDelegator;
import org.columba.calendar.store.event.StoreListener;
import org.columba.core.util.InternalException;

public abstract class AbstractCalendarStore {

	/** JDK 1.4+ logging framework logger, used for logging. */
	private static final Logger LOG = Logger
			.getLogger("org.columba.calendar.store");

	private EventListenerList listenerList = new EventListenerList();

	public AbstractCalendarStore() {
		super();

		// register interest on tree node changes
		addStorageListener(StoreEventDelegator.getInstance());

	}

	public abstract ICalendarModel get(Object id) throws IllegalArgumentException,
			InternalException;

	public abstract void add(ICalendarModel basicModel) throws IllegalArgumentException,
			InternalException;

	public void modify(Object id, ICalendarModel basicModel)
			throws IllegalArgumentException, InternalException {
		if (id == null)
			throw new IllegalArgumentException("id == null");
		if (basicModel == null)
			throw new IllegalArgumentException("basicModel == null");

		fireItemChanged(id);
	}

	public void remove(Object id) throws IllegalArgumentException, InternalException {
		if (id == null)
			throw new IllegalArgumentException("id == null");
		fireItemRemoved(id);
	}

	public abstract IHeaderItemList getHeaderItemList()
			throws IllegalArgumentException, InternalException;

	public abstract boolean exists(Object id) throws IllegalArgumentException,
			InternalException;

	/** ********************** event ****************************** */

	/**
	 * Adds a listener.
	 */
	public void addStorageListener(StoreListener l) {
		listenerList.add(StoreListener.class, l);
	}

	/**
	 * Removes a previously registered listener.
	 */
	public void removeStorageListener(StoreListener l) {
		listenerList.remove(StoreListener.class, l);
	}

	/**
	 * Propagates an event to all registered listeners notifying them of a item
	 * addition.
	 */
	protected void fireItemAdded(Object uid) {

		StoreEvent e = new StoreEvent(this, uid);
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();

		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == StoreListener.class) {
				((StoreListener) listeners[i + 1]).itemAdded(e);
			}
		}
	}

	/**
	 * Propagates an event to all registered listeners notifying them of a item
	 * removal.
	 */
	protected void fireItemRemoved(Object uid) {

		StoreEvent e = new StoreEvent(this, uid);
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();

		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == StoreListener.class) {
				((StoreListener) listeners[i + 1]).itemRemoved(e);
			}
		}
	}

	/**
	 * Propagates an event to all registered listeners notifying them of a item
	 * change.
	 */
	protected void fireItemChanged(Object uid) {

		StoreEvent e = new StoreEvent(this, uid);
		// Guaranteed to return a non-null array
		Object[] listeners = listenerList.getListenerList();

		// Process the listeners last to first, notifying
		// those that are interested in this event
		for (int i = listeners.length - 2; i >= 0; i -= 2) {
			if (listeners[i] == StoreListener.class) {
				((StoreListener) listeners[i + 1]).itemChanged(e);
			}
		}
	}
}
