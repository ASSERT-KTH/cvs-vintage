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

import java.io.File;
import java.util.Iterator;
import java.util.logging.Logger;

import org.columba.calendar.base.UUIDGenerator;
import org.columba.calendar.model.ComponentInfoList;
import org.columba.calendar.model.EventInfo;
import org.columba.calendar.model.api.IComponent;
import org.columba.calendar.model.api.IComponentInfoList;
import org.columba.calendar.model.api.IEvent;
import org.columba.calendar.model.api.IEventInfo;
import org.columba.calendar.parser.SyntaxException;
import org.columba.calendar.parser.VCalendarModelFactory;
import org.columba.calendar.store.api.ICalendarStore;
import org.columba.calendar.store.api.StoreException;
import org.columba.calendar.store.local.LocalXMLFileStore;
import org.columba.core.io.DiskIO;
import org.jdom.Document;

public class LocalCalendarStore extends AbstractCalendarStore implements
		ICalendarStore {

	/** JDK 1.4+ logging framework logger, used for logging. */
	private static final Logger LOG = Logger
			.getLogger("org.columba.calendar.store");

	/**
	 * folder where we save everything in
	 */
	private File directory;

	private LocalXMLFileStore dataStorage;

	public LocalCalendarStore(File directory) throws StoreException {
		super();

		if (directory == null)
			throw new IllegalArgumentException("directory == null");

		this.directory = directory;

		DiskIO.ensureDirectory(directory);

		dataStorage = new LocalXMLFileStore(directory);
	}

	/**
	 * @see org.columba.calendar.store.AbstractCalendarStore#add(org.columba.calendar.model.api.IComponent)
	 */
	public void add(IComponent basicModel) throws StoreException {

		if (basicModel == null)
			throw new IllegalArgumentException("basicModel == null");

		String id = basicModel.getId();
		// generate new UUID if it does not exist yet
		if (id == null)
			id = new UUIDGenerator().newUUID();

		Document document = null;
		try {
			document = VCalendarModelFactory.marshall(basicModel);
		} catch (SyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		dataStorage.save(id, document);

		fireItemAdded(id);
	}

	/**
	 * @see org.columba.calendar.store.AbstractCalendarStore#exists(java.lang.Object)
	 */
	public boolean exists(Object id) throws StoreException {

		if (id == null)
			throw new IllegalArgumentException("id == null");

		return dataStorage.exists(id);
	}

	/**
	 * @see org.columba.calendar.store.AbstractCalendarStore#get(java.lang.Object)
	 */
	public IComponent get(Object id) throws StoreException {

		if (id == null)
			throw new IllegalArgumentException("id == null");

		Document document = dataStorage.load(id);
		if (document == null)
			throw new StoreException("document == null, id=" + id);

		IComponent basicModel = null;
		try {
			basicModel = VCalendarModelFactory.unmarshall(document);
		} catch (SyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return basicModel;
	}

	/**
	 * @see org.columba.calendar.store.AbstractCalendarStore#modify(java.lang.Object,
	 *      org.columba.calendar.model.api.IComponent)
	 */
	public void modify(Object id, IComponent basicModel) throws StoreException {
		if (id == null)
			throw new IllegalArgumentException("id == null");

		if (basicModel == null)
			throw new IllegalArgumentException("basicModel == null");

		// remove old data
		dataStorage.remove(id);

		// generate xml document
		Document document = null;
		try {
			document = VCalendarModelFactory.marshall(basicModel);
		} catch (SyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// add new data to local store
		dataStorage.save(id, document);

		super.modify(id, basicModel);
	}

	/**
	 * @see org.columba.calendar.store.AbstractCalendarStore#remove(java.lang.Object)
	 */
	public void remove(Object id) throws StoreException {
		if (id == null)
			throw new IllegalArgumentException("id == null");

		dataStorage.remove(id);

		super.remove(id);
	}

	/**
	 * @see org.columba.calendar.store.AbstractCalendarStore#getComponentInfoList()
	 */
	public IComponentInfoList getComponentInfoList() throws StoreException {

		IComponentInfoList list = new ComponentInfoList();

		Iterator it = dataStorage.iterator();
		while (it.hasNext()) {
			Document document = (Document) it.next();

			IComponent basicModel = null;
			try {
				basicModel = VCalendarModelFactory.unmarshall(document);
			} catch (SyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (basicModel.getType() == IComponent.TYPE.EVENT) {
				IEvent event = (IEvent) basicModel;
				IEventInfo item = new EventInfo(event.getId(), event
						.getDtStart(), event.getDtEnt(), event.getSummary(), event.getCalendar());
				list.add(item);
			}
		}

		return list;
	}

}
