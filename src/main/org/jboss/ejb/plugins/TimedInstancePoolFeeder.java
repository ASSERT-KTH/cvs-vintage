/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins;

import org.jboss.ejb.InstancePool;
import org.jboss.ejb.InstancePoolFeeder;

import org.jboss.logging.Logger;

import org.jboss.metadata.XmlLoadable;
import org.jboss.metadata.MetaData;

import org.jboss.deployment.DeploymentException;

import org.w3c.dom.Element;

import java.util.TimerTask;
import java.util.Timer;

/**
 * Implementation of Pool feeder that is a java.util.Timer that checks at
 * regular period the size of the pool and bring it closer to its capacity by
 * adding a number of new instances.
 *
 * @author <a href="mailto:vincent.harcq@hubmethods.com">Vincent Harcq</a>
 *
 * @version $Revision: 1.1 $
 */
public class TimedInstancePoolFeeder
      extends TimerTask
      implements XmlLoadable, InstancePoolFeeder
{

   // Private -------------------------------------------------------

   private InstancePool ip;

   private int rate;
   private int capacity;
   private int increment;

   private Timer timer;
   private boolean isStarted = false;

   // Constructor ---------------------------------------------------

   public TimedInstancePoolFeeder()
   {
   }

   // Public --------------------------------------------------------

   public void run()
   {
      try
      {
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
         // ignore here
      }
   }

   // InstancePoolFeeder Impl ---------------------------------------

   public void start()
   {
      //Logger.debug("Pool Feeder Start()");
      timer = new Timer();
      timer.schedule(this, 0, rate);
      isStarted = true;
   }

   public void stop()
   {
      //Logger.debug("Pool Feeder Stop()");
      if (timer != null)
      {
         timer.cancel();
      }
   }

   public void setInstancePool(InstancePool ip)
   {
      this.ip = ip;
   }

   public boolean isStarted()
   {
      return isStarted;
   }

   // XmlLoadable Impl ----------------------------------------------

   public void importXml(Element element)
   throws DeploymentException
   {
      try
      {
         Element instancePoolConf = MetaData.getUniqueChild(element, "feeder-policy-conf");
         Element tmp;
         tmp = MetaData.getUniqueChild(instancePoolConf,"capacity");
         capacity = Integer.parseInt(MetaData.getElementContent(tmp));
         tmp = MetaData.getUniqueChild(instancePoolConf,"increment");
         increment = Integer.parseInt(MetaData.getElementContent(tmp));
         tmp = MetaData.getUniqueChild(instancePoolConf,"period");
         rate = Integer.parseInt(MetaData.getElementContent(tmp));
      }
      catch (Exception e)
      {
         throw new DeploymentException("Can't read feeder-policy-conf",e);
      }
   }

}
