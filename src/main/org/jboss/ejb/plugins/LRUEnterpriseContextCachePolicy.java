/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins;

import java.util.HashMap;
import java.util.Map;
import javax.jms.Message;
import javax.jms.JMSException;

import org.jboss.util.LRUCachePolicy;
import org.jboss.util.TimerTask;
import org.jboss.deployment.DeploymentException;
import org.jboss.ejb.EnterpriseContext;
import org.jboss.metadata.XmlLoadable;
import org.jboss.metadata.MetaData;
import org.w3c.dom.Element;

import org.jboss.monitor.Monitorable;
import org.jboss.monitor.client.BeanCacheSnapshot;

/**
 * Least Recently Used cache policy for EnterpriseContexts.
 *
 * @see AbstractInstanceCache
 * @author <a href="mailto:simone.bordet@compaq.com">Simone Bordet</a>
 * @version $Revision: 1.14 $
 */
public class LRUEnterpriseContextCachePolicy
   extends LRUCachePolicy
   implements EnterpriseContextCachePolicy, XmlLoadable, Monitorable
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   /** The AbstractInstanceCache that uses this cache policy */
   private AbstractInstanceCache m_cache;
   
   /** The period of the resizer's runs */
   private long m_resizerPeriod;
   
   /** The period of the overager's runs */
   private long m_overagerPeriod;
   
   /** The age after which a bean is automatically passivated */
   private long m_maxBeanAge;
   
   /**
    * Enlarge cache capacity if there is a cache miss every or less
    * this member's value
    */
   private long m_minPeriod;
   
   /**
    * Shrink cache capacity if there is a cache miss every or more
    * this member's value
    */
   private long m_maxPeriod;
   
   /**
    * The resizer will always try to keep the cache capacity so 
    * that the cache is this member's value loaded of cached objects
    */
   private double m_factor;

   /** The overager timer task */
   private TimerTask m_overager;

   /** The resizer timer task */
   private TimerTask m_resizer;
   
   /** Useful for log messages */
   private StringBuffer m_buffer = new StringBuffer();


   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   /**
    * Creates a LRU cache policy object given the instance cache that use
    * this policy object. 
    */
   public LRUEnterpriseContextCachePolicy(AbstractInstanceCache eic) 
   {	
      if (eic == null) 
         throw new IllegalArgumentException
            ("Instance cache argument cannot be null");
      
      m_cache = eic;
   }

   // Public --------------------------------------------------------

   // Monitorable implementation ------------------------------------

   public void sample(Object s)
   {
      BeanCacheSnapshot snapshot = (BeanCacheSnapshot)s;
      LRUList list = getList();
      synchronized (m_cache.getCacheLock())
      {
         snapshot.m_cacheMinCapacity = list.m_minCapacity;
         snapshot.m_cacheMaxCapacity = list.m_maxCapacity;
         snapshot.m_cacheCapacity = list.m_capacity;
         snapshot.m_cacheSize = list.m_count;
      }
   }

   // Z implementation ----------------------------------------------

   public void start() throws Exception 
   {
      if (m_resizerPeriod > 0)
      {
         m_resizer = new ResizerTask(m_resizerPeriod);
         scheduler.schedule(m_resizer, (long)(Math.random() * m_resizerPeriod));
      }
				
      if (m_overagerPeriod > 0) 
      {
         m_overager = new OveragerTask(m_overagerPeriod);
         scheduler.schedule(m_overager, (long)(Math.random() * m_overagerPeriod));
      }
		
      // TimerTask used only to debug
      //		scheduler.schedule(new CacheDumper(), 3000L);
   }
	
   public void stop() 
   {
      if (m_resizer != null) {m_resizer.cancel();}
      if (m_overager != null) {m_overager.cancel();}
      super.stop();
   }
   
   /**
    * Reads from the configuration the parameters for this cache policy, that are
    * all optionals.
    * FIXME 20010626 marcf: 
    *  Simone seriously arent' all the options overkill? give it another 6 month .
    *	 Remember you are exposing the guts of this to the end user, also provide defaults
    *  so that if an entry is not specified you can still work and it looks _much_ better in 
    *  the configuration files. 
    *
    */
   public void importXml(Element element) throws DeploymentException
   {
      String min = MetaData.getElementContent(MetaData.getOptionalChild(element, "min-capacity"));
      String max = MetaData.getElementContent(MetaData.getOptionalChild(element, "max-capacity"));
      String op = MetaData.getElementContent(MetaData.getOptionalChild(element, "overager-period"));
      String rp = MetaData.getElementContent(MetaData.getOptionalChild(element, "resizer-period"));
      String ma = MetaData.getElementContent(MetaData.getOptionalChild(element, "max-bean-age"));
      String map = MetaData.getElementContent(MetaData.getOptionalChild(element, "max-cache-miss-period"));
      String mip = MetaData.getElementContent(MetaData.getOptionalChild(element, "min-cache-miss-period"));
      String fa = MetaData.getElementContent(MetaData.getOptionalChild(element, "cache-load-factor"));
      try 
      {
         if (min != null)
         {
            int s = Integer.parseInt(min);
            if (s <= 0) 
            {
               throw new DeploymentException("Min cache capacity can't be <= 0");
            }
            m_minCapacity = s;
         }
         if (max != null)
         {
            int s = Integer.parseInt(max);
            if (s <= 0)
            {
               throw new DeploymentException("Max cache capacity can't be <= 0");
            }
            m_maxCapacity = s;
         }				
         if (op != null)
         {
            int p = Integer.parseInt(op);
            if (p <= 0) {throw new DeploymentException("Overager period can't be <= 0");}
            m_overagerPeriod = p * 1000;
         }
         if (rp != null)				
         {
            int p = Integer.parseInt(rp);
            if (p <= 0) {throw new DeploymentException("Resizer period can't be <= 0");}
            m_resizerPeriod = p * 1000;
         }
         if (ma != null)
         {
            int a = Integer.parseInt(ma);
            if (a <= 0) {throw new DeploymentException("Max bean age can't be <= 0");}
            m_maxBeanAge = a * 1000;
         }
         if (map != null)
         {
            int p = Integer.parseInt(map);
            if (p <= 0) {throw new DeploymentException("Max cache miss period can't be <= 0");}
            m_maxPeriod = p * 1000;
         }
         if (mip != null)
         {
            int p = Integer.parseInt(mip);
            if (p <= 0) {throw new DeploymentException("Min cache miss period can't be <= 0");}
            m_minPeriod = p * 1000;
         }
         if (fa != null)
         {
            double f = Double.parseDouble(fa);
            if (f <= 0.0) {throw new DeploymentException("Cache load factor can't be <= 0");}
            m_factor = f;
         }
      }
      catch (NumberFormatException x) 
      {
         throw new DeploymentException("Can't parse policy configuration", x);
      }
   }

   // Y overrides ---------------------------------------------------

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   protected LRUList createList() {
      return new ContextLRUList();
   }

   protected void ageOut(LRUCacheEntry entry) 
   {
      if (entry == null) {
         throw new IllegalArgumentException
            ("Cannot remove a null cache entry");
      }

      m_buffer.setLength(0);
      m_buffer.append("Aging out from cache bean ");
      m_buffer.append(m_cache.getContainer().getBeanMetaData().getEjbName());
      m_buffer.append("with id = ");
      m_buffer.append(entry.m_key);
      m_buffer.append("; cache size = ");
      m_buffer.append(getList().m_count);
      log.debug(m_buffer.toString());
		
      if (m_cache.isJMSMonitoringEnabled()) 
      {
         // Prepare JMS message
         Message message = m_cache.createMessage(entry.m_key);
         try 
         {
            message.setStringProperty("TYPE", "CACHE");
            message.setStringProperty("ACTIVITY", "AGE-OUT");
         }
         catch (JMSException x) 
         {
            log.error("createMessage failed", x);
         }
			
         // Send JMS Message
         m_cache.sendMessage(message);
      }

      // Debug code
      //		new Exception().printStackTrace();
      //		new CacheDumper().execute();

      // This will schedule the passivation
      m_cache.release((EnterpriseContext)entry.m_object);
   }
   
   protected void cacheMiss() 
   {
      LRUList list = getList();
      ++list.m_cacheMiss;

      if (m_cache.isJMSMonitoringEnabled()) 
      {
         // Prepare JMS message
         Message message = m_cache.createMessage(null);
         try 
         {
            message.setStringProperty("TYPE", "CACHE");
            message.setStringProperty("ACTIVITY", "CACHE-MISS");
         }
         catch (JMSException x) 
         {
            log.error("createMessage failed", x);
         }
			
         // Send JMS Message
         m_cache.sendMessage(message);
      }
   }

   // Private -------------------------------------------------------

   private LRUList getList() {
      return m_list;
   }
   
   // For debug purposes
   //	private java.util.Map getMap() {return m_map;}

   // Inner classes -------------------------------------------------
   
   /**
    * This TimerTask resizes the cache capacity using the cache miss frequency
    * algorithm, that is the more cache misses we have, the more the cache size
    * is enlarged, and viceversa. <p>
    * Of course, the maximum and minimum capacity are the bounds that this
    * resizer never passes.
    */
   protected class ResizerTask extends TimerTask
   {
      private String m_message;
      private StringBuffer m_buffer;

      protected ResizerTask(long resizerPeriod) 
      {
         super(resizerPeriod);
         m_message = "Resized cache for bean " +
            m_cache.getContainer().getBeanMetaData().getEjbName() +
            ": old capacity = ";
         m_buffer = new StringBuffer();
      }
      
      public void execute() 
      {
         // For now implemented as a Cache Miss Frequency algorithm

         LRUList list = getList();

         // Sync with the cache, since it is accessed also by another thread
         synchronized (m_cache.getCacheLock())
         {
            int period = list.m_cacheMiss == 0 ? Integer.MAX_VALUE : (int)(getPeriod() / list.m_cacheMiss);
            int cap = list.m_capacity;
            if (period <= m_minPeriod && cap < list.m_maxCapacity) 
            {
               // Enlarge cache capacity: if period == m_minPeriod then
               // the capacity is increased of the (1-m_factor)*100 %.
               double factor = 1.0 + ((double)m_minPeriod / period) * (1.0 - m_factor);
               int newCap = (int)(cap * factor);
               list.m_capacity = newCap < list.m_maxCapacity ? newCap : list.m_maxCapacity;
               log(cap, list.m_capacity);
            }
            else if (period >= m_maxPeriod && 
                     cap > list.m_minCapacity && 
                     list.m_count < (cap * m_factor))
            {
               // Shrink cache capacity
               int newCap = (int)(list.m_count / m_factor);
               list.m_capacity = newCap > list.m_minCapacity ? newCap : list.m_minCapacity;
               log(cap, list.m_capacity);
            }
            list.m_cacheMiss = 0;
         }
      }
      
      private void log(int oldCapacity, int newCapacity) 
      {
         m_buffer.setLength(0);
         m_buffer.append(m_message);
         m_buffer.append(oldCapacity);
         m_buffer.append(", new capacity = ");
         m_buffer.append(newCapacity);
         log.debug(m_buffer.toString());

         if (m_cache.isJMSMonitoringEnabled()) 
         {
            // Prepare JMS message
            Message message = m_cache.createMessage(null);
            try 
            {
               message.setStringProperty("TYPE", "RESIZER");
               message.setIntProperty("OLD_CAPACITY", oldCapacity);
               message.setIntProperty("NEW_CAPACITY", newCapacity);
               message.setIntProperty("SIZE", getList().m_count);
            }
            catch (JMSException x) 
            {
               log.error("createMessage failed", x);
            }
			
            // Send JMS Message
            m_cache.sendMessage(message);
         }
      }
   }
   
   /**
    * This TimerTask passivates cached beans that have not been called for a while.
    */
   protected class OveragerTask extends TimerTask
   {
      private String m_message;
      private StringBuffer m_buffer;

      protected OveragerTask(long period) 
      {
         super(period);
         m_message = getTaskLogMessage() + " " +
            m_cache.getContainer().getBeanMetaData().getEjbName() +
            " with id = ";
         m_buffer = new StringBuffer();
      }
      
      public void execute() 
      {
         LRUList list = getList();
         long now = System.currentTimeMillis();

         synchronized (m_cache.getCacheLock())
         {
            for (LRUCacheEntry entry = list.m_tail; entry != null; entry = list.m_tail)
            {
               if (now - entry.m_time >= getMaxAge())
               {
                  int initialSize = list.m_count;
						
                  // Log informations
                  log(entry.m_key, initialSize);
						
                  // Kick out of the cache this entry
                  kickOut(entry);
						
                  int finalSize = list.m_count;
						
                  if (initialSize == finalSize) 
                  {
                     // Here is a bug.
                     throw new IllegalStateException
                        ("Cache synchronization bug");
                  }
               }
               else {break;}
            }
         }
      }
      
      private void log(Object key, int count) 
      {
         m_buffer.setLength(0);
         m_buffer.append(m_message);
         m_buffer.append(key);
         m_buffer.append(" - Cache size = ");
         m_buffer.append(count);
         log.debug(m_buffer.toString());
			
         if (m_cache.isJMSMonitoringEnabled()) 
         {
            // Prepare JMS message
            Message message = m_cache.createMessage(key);
            try 
            {
               message.setStringProperty("TYPE", getJMSTaskType());
               message.setIntProperty("SIZE", count);
            }
            catch (JMSException x) 
            {
               log.error("createMessage failed", x);
            }
			
            // Send JMS Message
            m_cache.sendMessage(message);
         }
      }
      
      protected String getTaskLogMessage() {
         return "Scheduling for passivation overaged bean";
      }
      
      protected String getJMSTaskType() {
         return "OVERAGER";
      }
      
      protected void kickOut(LRUCacheEntry entry) {
         ageOut(entry);
      }
      
      protected long getMaxAge() {
         return m_maxBeanAge;
      }
   }

   /**
    * Subclass that send JMS messages on list activity events.
    */
   protected class ContextLRUList extends LRUList 
   {
      protected void entryAdded(LRUCacheEntry entry) 
      {
         if (m_cache.isJMSMonitoringEnabled()) 
         {
            // Prepare JMS message
            Message message = m_cache.createMessage(entry.m_key);
            try 
            {
               message.setStringProperty("TYPE", "CACHE");
               message.setStringProperty("ACTIVITY", "ADD");
               message.setIntProperty("CAPACITY", m_capacity);
               message.setIntProperty("SIZE", m_count);
            }
            catch (JMSException x) 
            {
               log.error("createMessage failed", x);
            }
			
            // Send JMS Message
            m_cache.sendMessage(message);
         }
      }
      
      protected void entryRemoved(LRUCacheEntry entry) 
      {
         if (m_cache.isJMSMonitoringEnabled()) 
         {
            // Prepare JMS message
            Message message = m_cache.createMessage(entry.m_key);
            try 
            {
               message.setStringProperty("TYPE", "CACHE");
               message.setStringProperty("ACTIVITY", "REMOVE");
               message.setIntProperty("CAPACITY", m_capacity);
               message.setIntProperty("SIZE", m_count);
            }
            catch (JMSException x) 
            {
               log.error("createMessage failed", x);
            }
			
            // Send JMS Message
            m_cache.sendMessage(message);
         }
      }
      
      protected void capacityChanged(int oldCapacity) 
      {
         if (m_cache.isJMSMonitoringEnabled()) 
         {
            // Prepare JMS message
            Message message = m_cache.createMessage(null);
            try 
            {
               message.setStringProperty("TYPE", "CACHE");
               message.setStringProperty("ACTIVITY", "CAPACITY");
               message.setIntProperty("OLD_CAPACITY", oldCapacity);
               message.setIntProperty("NEW_CAPACITY", m_capacity);
               message.setIntProperty("SIZE", m_count);
            }
            catch (JMSException x) 
            {
               log.error("createMessage failed", x);
            }
			
            // Send JMS Message
            m_cache.sendMessage(message);
         }
      }
   }
	
   /**
    * Class used only for debug purposes.
    */
   /*
   private class CacheDumper extends TimerTask
   {
      private java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(System.in));
      private CacheDumper() {super(5000L);}
      public void execute() 
      {
         synchronized (m_cache.getCacheLock())
         {
            if (getMap().size() > 0 && getList().m_count > 0) 
            {
               System.err.println();
               System.err.println("DUMPING CACHE FOR BEAN " + m_cache.getContainer().getBeanMetaData().getEjbName());
               System.err.println("THE MAP:");
               System.err.println(Integer.toHexString(getMap().hashCode()) + " size: " + getMap().size());
               for (java.util.Iterator i = getMap().values().iterator(); i.hasNext();) 
               {
                  System.err.println(i.next());
               }
               System.err.println("THE LIST:");
               System.err.println(getList());
               // readLine();
            }
         }
      }
      private void readLine() 
      {
         System.err.println("Hit a key...");
         try {reader.readLine();}
         catch (java.io.IOException x) {}
      }
   }
   */
}
