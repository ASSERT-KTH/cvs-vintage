package org.jboss.monitor;

public class LockMonitor
{
   long total_time = 0;
   long num_contentions = 0;
   public long timeouts = 0;
   long maxContenders = 0;
   long currentContenders = 0;
   private EntityLockMonitor monitor;

   public LockMonitor(EntityLockMonitor monitor)
   {
      this.monitor = monitor;
   }

   public synchronized void contending()
   {
      num_contentions++;
      currentContenders++;
      if (currentContenders > maxContenders) maxContenders = currentContenders;
      monitor.incrementContenders();
   }

   public synchronized void finishedContending(long time)
   {
      total_time += time;
      currentContenders--;
      monitor.decrementContenders(time);
   }
}

