/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.monitor;

import java.io.Serializable;

/**
 * Simple thread-safe POJO encapsulating locking stats.
 * 
 * Turned this class to Serializable to be able to
 * return copies of instances of this class over RMI.
 * 
 * In this case it becomes detached from the EntityLockMonitor
 * factory.
 * 
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @author <a href="mailto:dimitris@jboss.org">Dimitris Andreadis</a>
 * @version $Revision: 1.6 $
 */
public class LockMonitor implements Serializable
{
   // Private -------------------------------------------------------
   
   /** @since 4.0.2 */
   private static final long serialVersionUID = -6710878502772579272L;
   
   /* Lock Stats */
   private long totalTime;
   private long numContentions;
   private long timeouts;
   private long maxContenders;
   private long currentContenders;
   
   /** Back reference to the non-Serializable LockMonitor factory */
   private transient EntityLockMonitor parent;

   // Constructors --------------------------------------------------
   
   /**
    * @param parent factory
    */
   public LockMonitor(EntityLockMonitor parent)
   {
      this.parent = parent;
   }
   
   // Accessors -----------------------------------------------------
   
   public synchronized long getTotalTime()
   {
      return totalTime;
   }
   
   public synchronized long getNumContentions()
   {
      return numContentions;
   }
   
   public synchronized long getTimeouts()
   {
      return timeouts;
   }
   
   public synchronized long getMaxContenders()
   {
      return maxContenders;
   }

   public synchronized long getCurrentContenders()
   {
      return currentContenders;
   }
   
   // Modifiers -----------------------------------------------------
   
   /**
    * Adjust the counters to indicate a contetion conditions.
    * 
    * If the parent EntityLockMonitor has been initialized
    * the total stats will be updated, as well.
    */
   public void contending()
   {
   	  synchronized(this)
   	  {
   	     ++numContentions;
         ++currentContenders;
         
         if (currentContenders > maxContenders)
         {
            maxContenders = currentContenders;
         }
	  }
      
      // Remark Ulf Schroeter: DO NOT include following call into the
      // synchronization block because it will cause deadlocks between
      // LockMonitor methods and EntityLockMonitor.clearMonitor() call!
      if (parent != null)
      {
         parent.incrementContenders();
      }
   }

   /**
    * Adjust the counters to indicate that contention is over
    * 
    * If the parent EntityLockMonitor has been initialized
    * the total stats will be updated, too.
    */
   public void finishedContending(long time)
   {
      synchronized(this)
      {	
         totalTime += time;
         --currentContenders;
	  }

      // Remark Ulf Schroeter: DO NOT include following call into the
      // synchronization block because it will cause deadlocks between
      // LockMonitor methods and EntityLockMonitor.clearMonitor() call! 
      if (parent != null)
      {
         parent.decrementContenders(time);
      }
   }
   
   /**
    * Increase the timeouts on this lock
    */
   public void increaseTimeouts()
   {
      synchronized(this)
      {
         ++timeouts;
      }
   }
   
   /**
    * Reset the counters.
    * 
    * CurrentContenders stays unchanged and
    * MaxCondenders is set to CurrentContenders
    */
   public void reset()
   {
      synchronized(this)
      {
         timeouts = 0;
         totalTime = 0;
         numContentions = 0;
         // maxContenders always >= currentContenders
         maxContenders = currentContenders;
      }
   }
   
   // Object overrides ----------------------------------------------
   
   public String toString()
   {
      StringBuffer sbuf = new StringBuffer(128);
      
      sbuf.append(super.toString())
          .append("[ ")
          .append("totalTime=").append(getTotalTime())
          .append(", numContentions=").append(getNumContentions())
          .append(", timeouts=").append(getTimeouts())
          .append(", maxContenders=").append(getMaxContenders())
          .append(", currentContenders=").append(getCurrentContenders())
          .append(" ]");
                
      return sbuf.toString();
   }
}