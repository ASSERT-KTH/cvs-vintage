/*
* JBoss, the OpenSource EJB server
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

/**
 *
 */
public class TimedInstancePoolFeeder
      extends TimerTask
      implements XmlLoadable, InstancePoolFeeder
{

   // Private -------------------------------------------------------
   /** The instance pool we feed */
   private InstancePool ip;
   /** The rate in milliseconds between updates to the InstancePool */
   private int rate;
   /** The number of instances to add to the pool each period */
   private int increment;

   private Timer timer;
   private boolean isStarted = false;

   // Constructor ---------------------------------------------------

   public TimedInstancePoolFeeder(){}

   // Public --------------------------------------------------------

   public void run()
   {
      try
      {
         int capacity = ip.getMaxSize();
         if (ip.getCurrentSize() < capacity)
         {
            for (int i=0 ; i < increment ; i++)
            {
               // This may not work with secured stateless sessions
               ip.add(null);
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
