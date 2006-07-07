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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.Timer;

import org.columba.calendar.model.api.IComponent;
import org.columba.calendar.model.api.IEvent;
import org.columba.calendar.store.api.ICalendarStore;
import org.columba.calendar.store.api.IStoreListener;
import org.columba.calendar.store.api.StoreEvent;
import org.columba.calendar.store.api.StoreException;
import org.columba.calendar.ui.base.CalendarHelper;
import org.columba.core.base.Mutex;

import com.miginfocom.calendar.activity.Activity;
import com.miginfocom.calendar.activity.ActivityDepository;
import com.miginfocom.calendar.category.CategoryStructureEvent;

/**
 * StoreEventDelegator class
 * @author fdietz
 */
public class StoreEventDelegator implements IStoreListener, ActionListener {

	/** JDK 1.4+ logging framework logger, used for logging. */
	private static final Logger LOG = Logger
			.getLogger("org.columba.calendar.store.event");
	private static final int UPDATE_DELAY = 50;
	private static StoreEventDelegator instance;
	private Timer timer;
	private Mutex mutex;
	private int swap = 0;
	private List[] itemRemovedList;
	private List[] itemChangedList;
	private List[] itemAddedList;

	/**
	 * StoreEventDelegator method
	 */
	public StoreEventDelegator() {
		super();

		itemRemovedList = new List[] { new ArrayList(500), new ArrayList(500) };
		itemChangedList = new List[] { new ArrayList(500), new ArrayList(500) };
		itemAddedList = new List[] { new ArrayList(500), new ArrayList(500) };

		mutex = new Mutex();

		timer = new Timer(UPDATE_DELAY, this);
		timer.start();
	}

	/**
	 * getInstance method
	 * @return instance
	 */
	public static StoreEventDelegator getInstance() {
		if (instance == null)
			instance = new StoreEventDelegator();

		return instance;
	}

	/**
	 * clearAllLists method
	 */
	private void clearAllLists() {
		itemAddedList[swap].clear();
		itemRemovedList[swap].clear();
		itemChangedList[swap].clear();
	}

	/**
	 * processCalendarEvents method
	 */
	public void processCalendarEvents() {
		StoreEventComparator instance2 = StoreEventComparator
							.getInstance();
		StoreEventComparator storeEventComparator = instance2;
		StoreEventComparator storeEventComparator2 = storeEventComparator;
		List list = itemAddedList[swap];
		if (list.size() > 0) {
			LOG.info("process item added calendar events");

			Collections.sort(list, storeEventComparator2);

			// Process the events
			for (int i = 0; i < list.size(); i++) {
				StoreEvent next = (StoreEvent) list.get(i);

				ICalendarStore store = (ICalendarStore) next.getSource();
				try {
					IComponent model = store.get(next.getChanges());
					// we only update changes for events currently
					if (model.getType() == IComponent.TYPE.EVENT) {
						Activity act = CalendarHelper
								.createActivity((IEvent) model);

						ActivityDepository.getInstance().addBrokedActivity(act,
								this, CategoryStructureEvent.ADDED_CREATED);
					}
				} catch (StoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		if (itemRemovedList[swap].size() > 0) {
			Collections.sort(itemRemovedList[swap], storeEventComparator2);

			// Process the events
			for (int i = 0; i < itemRemovedList[swap].size(); i++) {
				StoreEvent next = (StoreEvent) itemRemovedList[swap].get(i);
				//ICalendarStore store = (ICalendarStore) next.getSource();
				String activityId = (String) next.getChanges();

				// remove old activity
				ActivityDepository.getInstance().removeBrokedActivityById(
						activityId);
			}
		}
		if (itemChangedList[swap].size() > 0) {
			Collections.sort(itemChangedList[swap], storeEventComparator2);

			// Process the events
			for (int i = 0; i < itemChangedList[swap].size(); i++) {
				StoreEvent next = (StoreEvent) itemChangedList[swap].get(i);

				ICalendarStore store = (ICalendarStore) next.getSource();
				try {
					IComponent model = store.get(next.getChanges());
					// we only update changes for events currently
					if (model.getType() == IComponent.TYPE.EVENT) {

						String activityId = model.getId();
						// remove old activity
						ActivityDepository.getInstance()
								.removeBrokedActivityById(activityId);

						// create new activity
						Activity act = CalendarHelper
								.createActivity((IEvent) model);
						ActivityDepository.getInstance().addBrokedActivity(act,
								this, CategoryStructureEvent.ADDED_CREATED);
					}
				} catch (StoreException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.columba.calendar.store.api.IStoreListener#itemAdded(org.columba.calendar.store.api.StoreEvent)
	 */
	public void itemAdded(StoreEvent e) {
		LOG.info(e.toString());

		mutex.lock();

		itemAddedList[1 - swap].add(e);

		mutex.release();
	}

	/* (non-Javadoc)
	 * @see org.columba.calendar.store.api.IStoreListener#itemRemoved(org.columba.calendar.store.api.StoreEvent)
	 */
	public void itemRemoved(StoreEvent e) {
		LOG.info(e.toString());

		mutex.lock();

		itemRemovedList[1 - swap].add(e);

		mutex.release();
	}

	/* (non-Javadoc)
	 * @see org.columba.calendar.store.api.IStoreListener#itemChanged(org.columba.calendar.store.api.StoreEvent)
	 */
	public void itemChanged(StoreEvent e) {
		LOG.info(e.toString());

		mutex.lock();

		itemChangedList[1 - swap].add(e);

		mutex.release();
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {

		// process all events collected until now
		mutex.lock();

		swap = 1 - swap;

		mutex.release();

		processCalendarEvents();

		clearAllLists();
	}
}
