/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
 
package org.jboss.jms.asf;

import java.util.Stack;
import org.jboss.logging.Logger;

/**
 *   Thread pool for JMS ASF. I am sorry Richard, I really tried to use
 *   the one in jboss.web, but it did not work, and I have to say I still
 *   does not understand how it ever could work, but I guess it did for you
 *   but not for me; hope mine work better for me. Basically its a total rip
 *   of with some modifications stolen from another place - Paul Hyden's 
 *   Java Thread Programming (SAMS)
 *      
 *   @see <related>
 *   @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 *   @author <a href="mailto:peter.antman@tim.se">Peter Antman</a>.
 *   @version $Revision: 1.4 $
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
	// FIXME We should probably have a maximum of threads allowed to be
	// created too. Here we just naively hands out
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
	else {

	    w.die();	
	}
    }
    
    // Inner classes -------------------------------------------------
    class Worker
	extends Thread
	{
	    boolean running = true;
	    
	    Slot slot = new Slot();
	    
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
		try {
		    slot.add(runner);
		}catch(InterruptedException e) {
		    // If this happend there is something wring with the pooling
		    // there should never be one here before the object was removed
		    
		}

	    }
	    
	    public void run()
	    {
		while (running)
		    {
			try {
			    // We use al slot we can wait on instead
			    Runnable r = slot.remove();
			    
			    // If work is available then execute it
			    //if (runner != null - should never happe, but
			    // what the heck)
			    if (r != null)
				{
				    try{
					r.run();
				    } catch (Exception e) {
					Logger.exception(e);
				    } finally {
					Thread.interrupted();
				    }
				}
			    
			}catch(InterruptedException e) {
			    Thread.currentThread().interrupt();
			} finally {		    
			    returnWorker(this);
				//}
			}
		    }
	    }

    }
    	    
	    /**
	     * A synchronized cubby hole - or Slot, only one thing
	     * can ever be in there. And you may use it to synchronize
	     * and wait on. Seems like the right thing for me.
	     * 
	     * This is a trimed down version of the ObjectFIFO in
	     * Java Thread Programming
	     */ 
	    class Slot {
		Runnable slot;
		int size = 0;
		public synchronized void add(Runnable r) throws InterruptedException{
		    waitWhileFull();
		    slot = r;
		    size = 1;
		    this.notifyAll();
		}
		public synchronized Runnable remove() throws InterruptedException{
		    waitWhileEmpty();
		    Runnable r= slot;
		    slot = null;
		    size = 0;
		    this.notifyAll();
		    return r;
		}
		
		public synchronized void waitWhileFull() throws InterruptedException{
		    while( isFull() ) {
			this.wait();
		    }
		}
		
		public synchronized void waitWhileEmpty() throws InterruptedException{
		    while( isEmpty() ) {
			this.wait();
		    }
		}
		
		public synchronized boolean isEmpty() {
		    return(size == 0);
		}
		
		public synchronized boolean isFull() {
		    return(size == 1);
		}
	    }
}
    

