//The contents of this file are subject to the Mozilla Public License Version 1.1
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
//The Initial Developers of the Original Code are Frederik Dietz and Timo Stich.
//Portions created by Frederik Dietz and Timo Stich are Copyright (C) 2003. 
//
//All Rights Reserved.
package org.columba.core.command;

import java.util.List;
import java.util.Vector;

import org.columba.core.logging.ColumbaLogger;
import org.columba.core.util.Mutex;

public class DefaultProcessor extends Thread {

	private final static int MAX_WORKERS = 5;

	private List operationQueue;
	private List worker;

	private Mutex operationMutex;
	private Mutex workerMutex;

	private boolean isBusy;
	
	private UndoManager undoManager;
	
	private TaskManager taskManager;
	
	private int timeStamp;

	public DefaultProcessor() {
		operationQueue = new Vector(10);

		worker = new Vector();

		for (int i = 0; i < MAX_WORKERS; i++) {
			worker.add(new Worker(this));
		}
		
		isBusy = true;
		
		operationMutex = new Mutex("operationMutex");
		workerMutex = new Mutex("workerMutex");

		taskManager = new TaskManager();
		
		undoManager = new UndoManager( this );
		timeStamp = 0;
		
	}

	public void addOp( Command op ) {
		addOp( op, Command.FIRST_EXECUTION );	
	}

	synchronized void addOp(Command op, int operationMode) {		
		//ColumbaLogger.log.debug( "Adding Operation..." );
        boolean needToRelease = false;
        try {
            needToRelease = operationMutex.getMutex();
		
		int p = operationQueue.size() - 1;
		OperationItem nextOp;

		// Sort in with respect to priority
		while (p != -1) {
			nextOp = (OperationItem) operationQueue.get(p);
			if ((nextOp.operation.getPriority() < op.getPriority()) && !nextOp.operation.isSynchronize())
				p--;
			else
				break;
		}

		operationQueue.add( p + 1, new OperationItem(op,operationMode));
        } finally {
            if (needToRelease) {
		operationMutex.releaseMutex();
            }
        }
		//ColumbaLogger.log.debug( "Operation added" );
		
        notify();
	}

	private boolean canBeProcessed(OperationItem opItem) {
		return opItem.operation.canBeProcessed(opItem.operationMode);
	}

	private OperationItem getNextOpItem() {
		OperationItem nextOp = null;
        boolean needToRelease = false;
        try {
		    needToRelease = operationMutex.getMutex();
		 for (int i = 0; i < operationQueue.size(); i++) {
			 nextOp = (OperationItem) operationQueue.get(i);
			if( ( i!=0 ) && (nextOp.operation.isSynchronize()) ) {
				nextOp = null;
				break;	
                } else if (canBeProcessed(nextOp)) {
				operationQueue.remove(i);
				break;
			} else {
				nextOp.operation.incPriority();
				if( nextOp.operation.getPriority() >= Command.DEFINETLY_NEXT_OPERATION_PRIORITY ) {					
					nextOp = null;
					break;
				} else {
					nextOp = null;					
				}
			}
		}
        } finally {
            if (needToRelease) {
		operationMutex.releaseMutex();
            }
        }
		return nextOp;
	}


	public synchronized void operationFinished(Command op, Worker w) {
        boolean needToRelease = false;
        try {
    		needToRelease = workerMutex.getMutex();
		
		worker.add(w);
        } finally {
            if (needToRelease) {
		workerMutex.releaseMutex();
            }
        }
		//ColumbaLogger.log.debug( "Operation finished" );
		notify();
	}

	private Worker getWorker() {
		Worker result = null;
        boolean needToRelease = false;
        try {
    		needToRelease = workerMutex.getMutex();

		if (worker.size() > 0)
			result = (Worker) worker.remove(0);
        } finally {
            if (needToRelease) {
		workerMutex.releaseMutex();
            }
        }
		return result;
	}

	private synchronized void waitForNotify() {
		isBusy = false;
		try {
			wait();
		} catch (InterruptedException e) {
		}
		isBusy = true;
		//ColumbaLogger.log.debug( "Operator woke up" );
	}

	public void run() // Scheduler
	{
		OperationItem opItem = null;
		Worker worker = null;

		while (true) {
			while ((opItem = getNextOpItem()) == null)
				waitForNotify();

			//ColumbaLogger.log.debug( "Processing new Operation" );

			while ((worker = getWorker()) == null)
				waitForNotify();

			//ColumbaLogger.log.debug( "Found Worker for new Operation" );
			worker.process(opItem.operation, opItem.operationMode, timeStamp++);
			//ColumbaLogger.log.debug( "Worker initilized" );
			worker.register(taskManager);
			//ColumbaLogger.log.debug( "Starting Worker" );
			worker.start();
			
			//ColumbaLogger.log.debug( "New Operation started..." );
		}
	}
	
	public boolean isBusy() {
		return isBusy();
	}
	
	public boolean hasFinishedCommands() {
		boolean result;
        boolean needToRelease = false;
        try {
		    needToRelease = workerMutex.getMutex();
		result = (worker.size() == MAX_WORKERS);
        } finally {
            if (needToRelease) {
		workerMutex.releaseMutex();
            }
        }
        try {
		    needToRelease = operationMutex.getMutex();
		result = result && (operationQueue.size() == 0);
        } finally {
            if (needToRelease) {
		operationMutex.releaseMutex();
            }
        }
		return result;
	}

	/**
	 * Returns the undoManager.
	 * @return UndoManager
	 */
	public UndoManager getUndoManager() {
		return undoManager;
	}

	/**
	 * Returns the taskManager.
	 * @return TaskManager
	 */
	public TaskManager getTaskManager() {
		return taskManager;
	}

	/**
	 * Returns the operationQueue. This method is for testing only!
	 * @return Vector
	 */
	public List getOperationQueue() {
		return operationQueue;
	}

}

class OperationItem {
	public Command operation;
	public int operationMode;		
	
	public OperationItem( Command op, int opMode ) {
		operation = op;
		operationMode = opMode;	
	}
}