/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
 
package org.jboss.web;

import java.util.Stack;

/**
 *   <description> 
 *      
 *   @see <related>
 *   @author Rickard Öberg (rickard.oberg@telkel.com)
 *   @version $Revision: 1.1 $
 */
public class ThreadPool
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   Stack pool = new Stack();
	
	int maxSize = 10;
	
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   public ThreadPool()
   {
   }
   
   // Public --------------------------------------------------------
	
	public synchronized void clear()
	{
		for (int i = 0; i < pool.size(); i++)
		{
			Worker w = (Worker)pool.get(i);
			w.stop();
		}
	}
	
	public void setMaximumSize(int size)
	{
		maxSize = size;
	}
	
	public synchronized void run(Runnable work)
	{
		if (pool.size() == 0)
		{
			new Worker().run(work);
		} else
		{
			Worker w = (Worker)pool.pop();
			w.run(work);
		}
	}
	
	
   // Package protected ---------------------------------------------
   synchronized void returnWorker(Worker w)
   {
		if (pool.size() < maxSize)
	   	pool.push(w);
		else
			w.die();	
   }
	
   // Inner classes -------------------------------------------------
	class Worker
		extends Thread
	{
		boolean running = true;
		
		Runnable runner;
		
		Worker()
		{
			start();
		}
		
		public synchronized void die()
		{
			running = false;
		}
		
		public synchronized void run(Runnable runner)
		{
			this.runner = runner;
			notifyAll();
		}
	
		public void run()
		{
			while (running)
			{
				// Wait for work to become available
				synchronized (this)
				{
					try
					{
						wait(5000);
					} catch (InterruptedException e)
					{
						// Ignore
					}
				}
				
				// If work is available then execute it
				if (runner != null)
				{
					try
					{
						runner.run();
					} catch (Exception e)
					{
						e.printStackTrace();
					}
					// Clear work
					runner = null;
				}
				
				// Return to pool
				returnWorker(this);
			}
		}
	}
}

