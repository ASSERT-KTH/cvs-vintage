/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins;

import java.util.TimerTask;
import java.util.Timer;

import org.w3c.dom.Element;

import org.jboss.deployment.DeploymentException;

import org.jboss.ejb.InstancePool;
import org.jboss.ejb.InstancePoolFeeder;

import org.jboss.metadata.XmlLoadable;
import org.jboss.metadata.MetaData;

import org.jboss.logging.Logger;

/**
 * An instance pool feeder which periodically adds instances to the pool.
 * 
 * @author ???
 * @version $Revision: 1.8 $
 */
public class TimedInstancePoolFeeder
   extends TimerTask
   implements XmlLoadable, InstancePoolFeeder
{
   private static final Logger log = Logger.getLogger(TimedInstancePoolFeeder.class);
   
   /** The instance pool we feed */
   private InstancePool ip;
   
   /** The rate in milliseconds between updates to the InstancePool */
   private int rate;
   
   /** The number of instances to add to the pool each period */
   private int increment;

   private Timer timer;

   public TimedInstancePoolFeeder() {}

   // TimerTask -----------------------------------------------------

   public void run()
   {
      try
      {
         int capacity = ip.getMaxSize();
         if (ip.getCurrentSize() < capacity)
         {
            for (int i=0 ; i < increment ; i++)
            {
               ip.add();
            }
         }
      }
      catch(Exception e)
      {
         log.error("Unexpected failure; stopping feeder", e);
         stop();
      }
   }

   // InstancePoolFeeder Impl ---------------------------------------

   public void start()
   {
      if (isStarted()) {
         log.error("Already started");
         return;
      }
      
      log.debug("Starting");
      
      timer = new Timer();
      timer.schedule(this, 0, rate);
   }

   public void stop()
   {
      if (!isStarted()) {
         log.error("Not started");
         return;
      }
      
      log.debug("Stopping");
      
      if (timer != null)
      {
         timer.cancel();
         timer = null;
      }
   }

   public void setInstancePool(final InstancePool ip)
   {
      this.ip = ip;
   }

   public boolean isStarted()
   {
      return timer != null;
   }

   // XmlLoadable Impl ----------------------------------------------

   public void importXml(Element element)
      throws DeploymentException
   {
      try
      {
         Element instancePoolConf = MetaData.getUniqueChild(element, "feeder-policy-conf");
         Element tmp;
         tmp = MetaData.getUniqueChild(instancePoolConf,"increment");
         increment = Integer.parseInt(MetaData.getElementContent(tmp));
         tmp = MetaData.getUniqueChild(instancePoolConf,"period");
         rate = Integer.parseInt(MetaData.getElementContent(tmp));
      }
      catch (Exception e)
      {
         throw new DeploymentException("Failed to process feeder-policy-conf", e);
      }
   }
}
