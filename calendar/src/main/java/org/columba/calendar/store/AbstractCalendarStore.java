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

import javax.swing.event.EventListenerList;

import org.columba.calendar.model.api.IComponent;
import org.columba.calendar.model.api.IComponentInfoList;
import org.columba.calendar.store.api.IStoreListener;
import org.columba.calendar.store.api.StoreEvent;
import org.columba.calendar.store.api.StoreException;

public abstract class AbstractCalendarStore {

	private EventListenerList listenerList = new EventListenerList();

	public AbstractCalendarStore() {
		super();

		// register interest on store changes
		// TODO the dependency should be the other way around
		addStorageListener(StoreEventDelegator.getInstance());

	}

	public abstract IComponent get(Object id) throws StoreException;

	public abstract void add(IComponent basicModel) throws StoreException;

	public void modify(Object id, IComponent basicModel) throws StoreException {
		if (id == null)
			throw new IllegalArgumentException("id == null");
		if (basicModel == null)
			throw new IllegalArgumentException("basicModel == null");

		fireItemChanged(id);
	}

	public void remove(Object id) throws StoreException {
		if (id == null)
			throw new IllegalArgumentException("id == null");
		fireItemRemoved(id);
	}

	public abstract IComponentInfoList getComponentInfoList()
			throws StoreException;

	public abstract boolean exists(Object id) throws StoreException;

	/** ********************** event ****************************** */

	/**
	 * Adds a listener.
	 */
	public void addStorageListener(IStoreListener l) {
		listenerList.add(IStoreListener.class, l);
	}

	/**
	 * Removes a previously registered listener.
	 */
	public void removeStorageListener(IStoreListener l) {
		listenerList.remove(IStoreListener.class, l);
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
			if (listeners[i] == IStoreListener.class) {
				((IStoreListener) listeners[i + 1]).itemAdded(e);
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
			if (listeners[i] == IStoreListener.class) {
				((IStoreListener) listeners[i + 1]).itemRemoved(e);
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
			if (listeners[i] == IStoreListener.class) {
				((IStoreListener) listeners[i + 1]).itemChanged(e);
			}
		}
	}
}
