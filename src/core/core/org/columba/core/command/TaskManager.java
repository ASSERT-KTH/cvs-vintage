// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Library General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.

package org.columba.core.command;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.Timer;
import org.columba.core.util.Mutex;
import org.columba.core.util.SwingWorker.ThreadVar;
import org.columba.main.MainInterface;
import org.columba.core.gui.statusbar.StatusBar;
import org.columba.core.gui.statusbar.event.WorkerListChangeListener;
import org.columba.core.gui.statusbar.event.WorkerListChangedEvent;

public class TaskManager {
	private Vector workerList;

	protected Mutex workerListMutex;

	protected Vector workerListChangeListeners;


	public TaskManager() {
		workerList = new Vector();

		workerListMutex = new Mutex();

		workerListChangeListeners = new Vector();
	}

	public Vector getWorkerList() {
		return workerList;
	}

	private void addWorker(Worker w) {
		Worker compareWorker;
		int workerPriority = w.getPriority();

		for (int i = 0; i < workerList.size(); i++) {
			compareWorker = (Worker) workerList.get(i);
			if (compareWorker.getPriority() < workerPriority) {
				workerList.insertElementAt(w, i);
				return;
			}
		}

		// Lowest Priority		
		workerList.add(w);
	}

	public Worker get(int index) {
		Worker w;

		workerListMutex.getMutex();
		w = (Worker) workerList.get(index);
		workerListMutex.releaseMutex();

		return w;
	}

	public void register(Worker t) {
		WorkerListChangedEvent changeEvent = new WorkerListChangedEvent();

		workerListMutex.getMutex();

		changeEvent.setOldValue(workerList.size());

		addWorker(t);

		changeEvent.setNewValue(workerList.size());

		workerListMutex.releaseMutex();

		fireWorkerListChangedEvent(changeEvent);
	}

	public void unregister(ThreadVar tvar) {
		Worker worker;
		WorkerListChangedEvent e = new WorkerListChangedEvent();
		e.setType(WorkerListChangedEvent.SIZE_CHANGED);

		workerListMutex.getMutex();
		int size = workerList.size();
		e.setOldValue(size);

		for (int i = 0; i < size; i++) {
			worker = (Worker) workerList.get(i);

			if (tvar == worker.getThreadVar()) {
				workerList.remove(i);
				e.setNewValue(workerList.size());
				break;
			}
		}

		workerListMutex.releaseMutex();

		fireWorkerListChangedEvent(e);
	}

	public void addWorkerListChangeListener(WorkerListChangeListener l) {
		workerListChangeListeners.add(l);
	}

	protected void fireWorkerListChangedEvent(WorkerListChangedEvent e) {
		for (int i = 0; i < workerListChangeListeners.size(); i++) {
			(
				(WorkerListChangeListener) workerListChangeListeners.get(
					i)).workerListChanged(
				e);
		}
	}
}