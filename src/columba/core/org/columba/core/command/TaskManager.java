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
package org.columba.core.command;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.columba.core.gui.statusbar.event.WorkerListChangeListener;
import org.columba.core.gui.statusbar.event.WorkerListChangedEvent;
import org.columba.core.util.Mutex;
import org.columba.core.util.SwingWorker.ThreadVar;

/**
 * TaskManager keeps a list of currently running {@link Worker} objects.
 * <p>
 * The {@link StatusBar} listens for {@link WorkerListChangedEvent} to 
 * provide visual feedback. This includes a status message text and
 * the progress bar.
 * <p>
 * In a model/view/controller pattern, the statusbar is the view, 
 * the taskmanager the underlying model.
 * 
 * 
 * @author fdietz
 */
public class TaskManager {
	
	/**
	 * List of currently running {@link Worker} objects
	 */
	private List workerList;

	/**
	 * We need a mutex to be sure that modifying the workerList is
	 * thread-safe at all time
	 */
	protected Mutex workerListMutex;

	/**
	 * Listeners which are interested in status changes
	 */
	protected List workerListChangeListeners;

	/**
	 * Default constructor
	 */
	public TaskManager() {
		workerList = new Vector();

		workerListMutex = new Mutex("workerListMutex");

		workerListChangeListeners = new Vector();
	}

	/**
	 * Get list of currently running workers.
	 * 
	 * @return	list of workers
	 */
	public List getWorkerList() {
		return workerList;
	}

	/**
	 * Get number of workers
	 * 
	 * @return	number of currenlty running workers
	 */
	public int count() {
		return workerList.size();
	}

	/**
	 * Add {@link Worker} to this manager
	 * 
	 * @param w		new worker
	 */
	private void addWorker(Worker w) {
		Worker compareWorker;
		int workerPriority = w.getPriority();
		for (int i = 0; i < workerList.size(); i++) {
			compareWorker = (Worker) workerList.get(i);
			if (compareWorker.getPriority() < workerPriority) {
				workerList.add(i, w);
				return;
			}
		}

		// Lowest Priority
		workerList.add(w);
	}

	/**
	 * Get worker with index.
	 * 
	 * @param index		index of worker
	 * @return			worker
	 */
	public Worker get(int index) {
		Worker w;
		boolean needToRelease = false;
		try {
			needToRelease = workerListMutex.getMutex();
			w = (Worker) workerList.get(index);
		} finally {
			if (needToRelease) {
				workerListMutex.releaseMutex();
			}
		}
		return w;
	}

	/**
	 * Register new {@link Worker} at TaskManager.
	 * 
	 * @param t		new worker
	 */
	public void register(Worker t) {
		WorkerListChangedEvent changeEvent = new WorkerListChangedEvent();
		boolean needToRelease = false;
		try {
			needToRelease = workerListMutex.getMutex();

			changeEvent.setOldValue(workerList.size());

			addWorker(t);

			changeEvent.setNewValue(workerList.size());
		} finally {
			if (needToRelease) {
				workerListMutex.releaseMutex();
			}
		}
		fireWorkerListChangedEvent(changeEvent);
	}

	/**
	 * Remove {@link Worker} from TaskManager
	 * 
	 * @param tvar		Thread encapusulated in Worker
	 */
	public void unregister(ThreadVar tvar) {
		Worker worker;
		boolean needToRelease = false;
		WorkerListChangedEvent e = new WorkerListChangedEvent();
		e.setType(WorkerListChangedEvent.SIZE_CHANGED);
		try {
			needToRelease = workerListMutex.getMutex();
			int size = workerList.size();
			e.setOldValue(size);
			for (Iterator it = workerList.iterator(); it.hasNext();) {
				worker = (Worker) it.next();

				if (tvar == worker.getThreadVar()) {
					workerList.remove(worker);
					e.setNewValue(workerList.size());
					break;
				}
			}
		} finally {
			if (needToRelease) {
				workerListMutex.releaseMutex();
			}
		}
		fireWorkerListChangedEvent(e);
	}

	/**
	 * Register interest on events.
	 * 
	 * @param l		listener
	 */
	public void addWorkerListChangeListener(WorkerListChangeListener l) {
		workerListChangeListeners.add(l);
	}

	/**
	 * Notify all listeners that something has changed.
	 * 
	 * @param e		event
	 */
	protected void fireWorkerListChangedEvent(WorkerListChangedEvent e) {
		for (Iterator it = workerListChangeListeners.iterator();
			it.hasNext();
			) {
			((WorkerListChangeListener) it.next()).workerListChanged(e);

		}
	}
}
