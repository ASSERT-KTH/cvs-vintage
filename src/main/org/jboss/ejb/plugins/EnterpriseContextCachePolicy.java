/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins;

import org.jboss.logging.Log;
import org.jboss.util.TimerQueue;
import org.jboss.util.TimerTask;
import org.jboss.util.CachePolicy;

/**
 * Interface that specifies a policy for caches. <p>
 * Implementation classes can implement a LRU policy, a random one, 
 * a MRU one, or any other suitable policy.
 * 
 * @see EnterpriseInstanceCache
 * @author Simone Bordet (simone.bordet@compaq.com)
 * @version $Revision: 1.2 $
 */
public interface EnterpriseContextCachePolicy extends CachePolicy
{
	// Constants -----------------------------------------------------

	// Static --------------------------------------------------------
	public static Log log = new Log("Bean Cache");
	public static Scheduler scheduler = new Scheduler();
	
	// Public --------------------------------------------------------
	/**
	 * Final class that hold a single instance of a {@link TimerQueue} object
	 * for all the CachePolicy instances, allowing the cache policies to 
	 * register {@link TimerTask}s that will be executed in a single background
	 * thread. <br>
	 * Usage:
	 * <pre>
	 * TimerTask task = new TimerTask() 
	 * {
	 * 		public void execute() throws Exception 
	 *		{
	 *			doSomething();
	 *		}
	 * };
	 * scheduler.schedule(task);
	 * </pre>
	 * 
	 * @see TimerQueue
	 */
	public static final class Scheduler
	{
		private static final TimerQueue m_scheduler = new TimerQueue();
		static
		{
			m_scheduler.start();
			log.log("Cache policy scheduler started");
		}
		public final void schedule(TimerTask t) 
		{
			schedule(t, 0);
		}
		public final void schedule(TimerTask t, long delay) 
		{
			m_scheduler.schedule(t, delay);
		}
	}
}
