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

import org.columba.core.util.Mutex;

/**
 * Scheduler for background threads
 * <p>
 * DefaultProcessor keeps a pool of {@link Worker} instances, which are
 * assigned to {@link Command}, when executed.
 *
 * @author tstich
 */
public class DefaultCommandProcessor implements CommandProcessor, Runnable {
    private final static int MAX_WORKERS = 5;
    private List operationQueue;
    private List worker;
    private Mutex operationMutex;
    private Mutex workerMutex;
    
    
    /**
     * Processor is busy or waiting.
     */
    private boolean isBusy;
    
  
    private TaskManager taskManager;
    private int timeStamp;

    /**
     * Constructs a DefaultProcessor.
     */
    public DefaultCommandProcessor() {
        operationQueue = new Vector(10);

        worker = new Vector(MAX_WORKERS);

        // Create the workers
        for (int i = 0; i < MAX_WORKERS; i++) {
            worker.add(new Worker(this));
        }

        isBusy = true;

        operationMutex = new Mutex("operationMutex");
        workerMutex = new Mutex("workerMutex");

        taskManager = new TaskManager();

        timeStamp = 0;
        
        new Thread(this).start();
    }

    /**
     * Add a Command to the Queue.
     * Calls {@link #addOp(Command, int)} with Command.FIRST_EXECUTION.
     *
     * @param op the command to add
     */
    public void addOp(final Command op) {
        addOp(op, Command.FIRST_EXECUTION);
    }

    /**
     * Adds a Command to the queue.
     *
     * @param op the command
     * @param operationMode the mode in wich the command should be processed
     */
    synchronized void addOp(final Command op, final int operationMode) {
        //ColumbaLogger.log.info( "Adding Operation..." );
        boolean needToRelease = false;

        try {
            needToRelease = operationMutex.getMutex();

            int p = operationQueue.size() - 1;
            OperationItem nextOp;

            // Sort in with respect to priority and synchronize:
            // Commands with higher priority will be processed
            // before commands with lower priority.
            // If there is a command that is of type synchronize
            // don't put this command in front.
            while (p != -1) {
                nextOp = (OperationItem) operationQueue.get(p);

                if ((nextOp.getOperation().getPriority() < op.getPriority()) &&
                        !nextOp.getOperation().isSynchronize()) {
                    p--;
                } else {
                    break;
                }
            }

            operationQueue.add(p + 1, new OperationItem(op, operationMode));
        } finally {
            if (needToRelease) {
                operationMutex.releaseMutex();
            }
        }

        //ColumbaLogger.log.info( "Operation added" );
        notify();
    }

    /**
     * Checks if the command can be processed. This is true
     * if all references are not blocked.
     *
     * @param opItem the internal command structure
     * @return true if the operation will not be blocked
     */
    private boolean canBeProcessed(final OperationItem opItem) {
        return opItem.getOperation().canBeProcessed(opItem.getOperationMode());
    }

    /**
     * Get the next Operation from the queue.
     *
     * @return the next non-blocking operation or null if none found.
     */
    private OperationItem getNextOpItem() {
        OperationItem nextOp = null;
        boolean needToRelease = false;

        try {
            needToRelease = operationMutex.getMutex();

            for (int i = 0; i < operationQueue.size(); i++) {
                nextOp = (OperationItem) operationQueue.get(i);

                if ((i != 0) && (nextOp.getOperation().isSynchronize())) {
                    nextOp = null;

                    break;
                } else if (canBeProcessed(nextOp)) {
                    operationQueue.remove(i);

                    break;
                } else {
                    nextOp.getOperation().incPriority();

                    if (nextOp.getOperation().getPriority() >= Command.DEFINETLY_NEXT_OPERATION_PRIORITY) {
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

    /**
     * Called by the worker to signal that his operation has finished.
     *
     * @param op the command the worker has processed
     * @param w the worker himself
     */
    public synchronized void operationFinished(final Command op, final Worker w) {
        boolean needToRelease = false;

        // add the worker to the workerlist
        try {
            needToRelease = workerMutex.getMutex();

            worker.add(w);
        } finally {
            if (needToRelease) {
                workerMutex.releaseMutex();
            }
        }

        // notify that a new worker is available
        notify();
    }

    /**
     * Get an available Worker from the workerpool.
     *
     * @return a available worker or null if none available.
     */
    private Worker getWorker() {
        Worker result = null;
        boolean needToRelease = false;

        try {
            needToRelease = workerMutex.getMutex();

            if (worker.size() > 0) {
                result = (Worker) worker.remove(0);
            }
        } finally {
            if (needToRelease) {
                workerMutex.releaseMutex();
            }
        }

        return result;
    }

    /**
     * Wait until a worker is available or a new command is added.
     */
    private synchronized void waitForNotify() {
        isBusy = false;

        try {
            wait();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        isBusy = true;

        //ColumbaLogger.log.info( "Operator woke up" );
    }

    /**
     * 
     * @see java.lang.Runnable#run()
     */
    public void run()
     {
        OperationItem opItem = null;
        Worker worker = null;

        while (true) {        	
            while ((opItem = getNextOpItem()) == null)
                waitForNotify();

            //ColumbaLogger.log.info( "Processing new Operation" );
            while ((worker = getWorker()) == null)
                waitForNotify();

            //ColumbaLogger.log.info( "Found Worker for new Operation" );
            worker.process(opItem.getOperation(), opItem.getOperationMode(), timeStamp++);

            //ColumbaLogger.log.info( "Worker initilized" );
            worker.register(taskManager);

            //ColumbaLogger.log.info( "Starting Worker" );
            worker.start();

            //ColumbaLogger.log.info( "New Operation started..." );
        }
    }

    /**
     * Is the processor busy?
     * 
     * @return processor busy
     */
    public boolean isBusy() {
        return isBusy;
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


/**
 * Intern represenation of the Commands.
 * 
 * @author Timo Stich <tstich@users.sourceforge.net>
 */
class OperationItem {
    private Command operation;
    
    private int operationMode;

    public OperationItem(Command op, int opMode) {
        operation = op;
        operationMode = opMode;
    }

    public Command getOperation() {
        return operation;
    }

    public int getOperationMode() {
        return operationMode;
    }
}
