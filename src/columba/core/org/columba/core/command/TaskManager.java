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

import org.columba.core.util.Mutex;
import org.columba.core.util.SwingWorker.ThreadVar;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.swing.event.EventListenerList;

/**
 * TaskManager keeps a list of currently running {@link Worker} objects.
 * <p>
 * The {@link StatusBar} listens for {@link TaskManagerEvent} to
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
    protected EventListenerList listenerList = new EventListenerList();

    /**
     * Default constructor
     */
    public TaskManager() {
        workerList = new Vector();

        workerListMutex = new Mutex();
    }

    /**
     * Get list of currently running workers.
     *
     * @return        list of workers
     */
    public Worker[] getWorkers() {
        return (Worker[])workerList.toArray(new Worker[0]);
    }
    
    public boolean exists(Worker worker) {
    	 try {
            workerListMutex.lock();

            if ( workerList.contains(worker)) return true;
            
        } finally {
            workerListMutex.release();
        }
    	
    	return false;
    }

    /**
     * Get number of workers
     *
     * @return        number of currenlty running workers
     */
    public int count() {
        return workerList.size();
    }

    /**
     * Register new {@link Worker} at TaskManager.
     *
     * @param t                new worker
     */
    public void register(Worker t) {
        try {
            workerListMutex.lock();

            workerList.add(t);
        } finally {
            workerListMutex.release();
        }

        fireWorkerAdded(t);
    }

    /**
     * Remove {@link Worker} from TaskManager
     *
     * @param tvar                Thread encapusulated in Worker
     */
    public void unregister(ThreadVar tvar) {
        Worker worker;

        try {
            workerListMutex.lock();

            for (Iterator it = workerList.iterator(); it.hasNext();) {
                worker = (Worker) it.next();

                if (tvar == worker.getThreadVar()) {
                    workerList.remove(worker);
                    fireWorkerRemoved(worker);
                    break;
                }
            }
        } finally {
           workerListMutex.release();
        }
    }

    /**
     * Register interest on events.
     *
     * @param l                listener
     */
    public void addTaskManagerListener(TaskManagerListener l) {
        listenerList.add(TaskManagerListener.class, l);
    }
    
    /**
     * Remove a previously registered listener.
     */
    public void removeTaskManagerListener(TaskManagerListener l) {
        listenerList.remove(TaskManagerListener.class, l);
    }

    /**
     * Notify all listeners that something has changed.
     *
     * @param e                event
     */
    protected void fireWorkerAdded(Worker w) {
        TaskManagerEvent e = new TaskManagerEvent(this, w);
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();

        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == TaskManagerListener.class) {
                ((TaskManagerListener) listeners[i + 1]).workerAdded(e);
            }
        }
    }
    
    protected void fireWorkerRemoved(Worker w) {
        TaskManagerEvent e = new TaskManagerEvent(this, w);
        // Guaranteed to return a non-null array
        Object[] listeners = listenerList.getListenerList();

        // Process the listeners last to first, notifying
        // those that are interested in this event
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == TaskManagerListener.class) {
                ((TaskManagerListener) listeners[i + 1]).workerRemoved(e);
            }
        }
    }
}
