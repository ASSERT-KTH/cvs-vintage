/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.timer;

import javax.ejb.EJBException;
import javax.ejb.NoSuchObjectLocalException;
import javax.ejb.Timer;
import javax.ejb.TimerHandle;
import javax.management.JMException;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

/**
 * The TimerHandle interface is implemented by all EJB timer handles.
 *
 * @author <a href="mailto:andreas.schaefer@madplanet.com">Andreas Schaefer</a>
 * @version $Revision: 1.2 $
 **/
public class ContainerTimerHandle
    implements TimerHandle
{
   private String mContainerId;
   private Object mKey;
   private Integer mTimerId;
   private transient ContainerTimer mTimer;
   
   /**
    * Creates a Timer Handle
    *
    * @param pTimer Timer this instance is a handle of
    **/
   public ContainerTimerHandle( ContainerTimer pTimer ) {
      // Save it so that it can be returned if not
      // serialized
      mTimer = pTimer;
      mContainerId = mTimer.getContainerId();
      mTimerId = mTimer.getId();
      mKey = mTimer.getKey();
   }
   
   public Timer getTimer()
      throws
         IllegalStateException,
         NoSuchObjectLocalException,
         EJBException
   {
      if( mTimer != null ) {
         if( mTimer.isValid() ) {
            throw new NoSuchObjectLocalException( "Timer is cancelled and no longer available" );
         }
      } else {
         try {
            // First get the MBean Server and retrieve the container
            MBeanServer lServer = (MBeanServer) MBeanServerFactory.findMBeanServer( null ).iterator().next();
            // Get the Timer Service
            ContainerTimerService lTimerService = (ContainerTimerService) lServer.invoke(
               new ObjectName( mContainerId ),
               "getTimerService",
               new Object[] { mKey },
               new String[] { Object.class.getName() }
            );
            mTimer = (ContainerTimer) lTimerService.getTimer( mTimerId );
         }
         catch( JMException jme ) {
            throw new EJBException( "Could not retrieve the requested timer" );
         }
      }
      return mTimer;
   }
}
