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
package org.columba.core.util;

import javax.swing.SwingUtilities;

import org.columba.core.command.TaskManager;

/**
 * This is the 3rd version of SwingWorker (also known as
 * SwingWorker 3), an abstract class that you subclass to
 * perform GUI-related work in a dedicated thread.  For
 * instructions on using this class, see:
 *
 * http://java.sun.com/docs/books/tutorial/uiswing/misc/threads.html
 *
 * Note that the API changed slightly in the 3rd version:
 * You must now invoke start() on the SwingWorker after
 * creating it.
 */
public abstract class SwingWorker
{
	protected Object value; // see getValue(), setValue()
	protected Thread thread;
	protected TaskManager taskManager;
	protected boolean cancel;


	/**
	 * Class to maintain reference to current worker thread
	 * under separate synchronization control.
	 */
	public static class ThreadVar
	{
		private Thread thread;
		ThreadVar(Thread t)
		{
			thread = t;
		}
		synchronized Thread get()
		{
			return new Thread(thread);
		}
		synchronized void clear()
		{
			thread = null;
		}
	}

	protected ThreadVar threadVar;

	/**
	 * Get the value produced by the worker thread, or null if it
	 * hasn't been constructed yet.
	 */
	protected synchronized Object getValue()
	{
		return value;
	}

	/**
	 * Set the value produced by worker thread
	 */
	private synchronized void setValue(Object x)
	{
		value = x;
	}


	public Thread getThread()
	{
		return threadVar.get();
	}

	public ThreadVar getThreadVar()
	{
		return threadVar;
	}

	public boolean getCancel()
	{
		return cancel;
	}

	public void setCancel(boolean b)
	{
		cancel = b;
	}

	protected boolean isCanceled() {
		return cancel;	
	}


	/**
	 * Compute the value to be returned by the <code>get</code> method.
	 */
	public abstract Object construct();
	

	/**
	 * Called on the event dispatching thread (not on the worker thread)
	 * after the <code>construct</code> method has returned.
	 */
	public void finished()
	{
	}

	/**
	 * A new method that interrupts the worker thread.  Call this method
	 * to force the worker to stop what it's doing.
	 */
	public void interrupt()
	{
		Thread t = threadVar.get();
		if (t != null)
		{
			t.interrupt();
		}
		threadVar.clear();
	}

	/**
	 * Return the value created by the <code>construct</code> method.
	 * Returns null if either the constructing thread or the current
	 * thread was interrupted before a value was produced.
	 *
	 * @return the value created by the <code>construct</code> method
	 */
	public Object get()
	{
		while (true)
		{
			Thread t = threadVar.get();
			if (t == null)
			{
				return getValue();
			}
			try
			{
				t.join();
			}
			catch (InterruptedException e)
			{
				Thread.currentThread().interrupt(); // propagate
				return null;
			}
		}
	}

	/**
	 * Start a thread that will call the <code>construct</code> method
	 * and then exit.
	 */
	public SwingWorker()
	{
		cancel = false;

		final Runnable doFinished = new Runnable()
		{
			public void run()
			{
				finished();
			}
		};

		Runnable doConstruct = new Runnable()
		{

			public void run()
			{
				try
				{
					setValue(construct());
				}
				finally
				{
					//threadVar;
				}

				SwingUtilities.invokeLater(doFinished);
			}
		};

		Thread t = new Thread(doConstruct);
		// following java guidelines I'm setting this to low priority
		//  -> this should make the gui more responsive, because the
		//  -> background worker has low priority whereas the gui worker
		//  -> has normal priority
		t.setPriority( Thread.MIN_PRIORITY );
		
		threadVar = new ThreadVar(t);
	}

	/**
	 * Start the worker thread.
	 */
	public Thread start()
	{
		thread = threadVar.get();
		if (thread != null)
		{
			thread.start();
			return thread;
		}
		return null;
	}

}