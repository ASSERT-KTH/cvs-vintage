/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.timer;

import java.util.Date;
import java.util.HashMap;

import javax.management.JMException;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;

/**
 * This class is the bridge between an (external) Timer Source
 * used to manage Timers and to provide the Container with the
 * timed event to invoke the Bean's ejbTimeout() method
 *
 * @jmx:mbean extends="org.jboss.ejb.timer.AbstractTimerSourceMBean"
 *
 * @author <a href="mailto:andreas.schaefer@madplanet.com">Andreas Schaefer</a>
 **/
public class SchedulerTimerSource
   extends AbstractTimerSource
   implements SchedulerTimerSourceMBean
{
   
   protected HashMap mTimerList = new HashMap();
   private ObjectName mScheduleManagerName;
   
   public boolean isTransactional() {
       return false;
   }
   
   public boolean isPersistent() {
       return false;
   }
   
   public String createTimer( String pContainerId, Date pStartDate, long pInterval ) {
       try {
           int lId = addSchedule(
              serviceName,
              "handleTimedEvent",
              new String[] { "ID" },
              pStartDate,
              pInterval,
              -1
           );
           synchronized( mTimerList ) {
               mTimerList.put( new Integer( lId ), pContainerId );
           }
           return lId + "";
       }
       catch( JMException jme ) {
           throw new RuntimeException( "Could not create the required timer" );
       }
   }
   
   /**
    * Removes the given Timer if still active or not
    *
    * @param pId Id returned from {@link #createTimer createTimer()}
    **/
   public void removeTimer( String pId ) {
       try {
           synchronized( mTimerList ) {
               mTimerList.remove( pId );
           }
           removeSchedule( new Integer( pId ).intValue() );
       }
       catch( JMException jme ) {
           throw new RuntimeException( "Could not remove the required timer" );
       }
   }
   
   public void recover( long pContainerId ) {
   }
   
   /**
    * Timer Source send a timed event to be handled
    *
    * @param pId Id of the timer
    *
    * @jmx:managed-operation
    */
   public void handleTimedEvent( Integer pId ) {
      String lContainerId = null;
      synchronized( mTimerList ) {
         lContainerId = (String) mTimerList.get( pId );
      }
      if( lContainerId != null ) {
          ContainerTimerService lTarget = (ContainerTimerService) getTimerService( lContainerId );
          if( lTarget != null ) {
              lTarget.handleTimedEvent( pId + "" );
          } else {
              log.error( "Container found but no Timer Service. Container ID: " + lContainerId );
          }
      } else {
          // If no Container is found then container did not recover so far
      }
   }
   
   /**
    * Get the Schedule Manager Name
    *
    * @jmx:managed-operation
    */
   public String getScheduleManagerName() {
      return mScheduleManagerName.toString();
   }
   
   /**
    * Set the Schedule Manager Name
    *
    * @jmx:managed-operation
    */
   public void setScheduleManagerName( String pSchedulerManagerName )
      throws MalformedObjectNameException
   {
      mScheduleManagerName = new ObjectName( pSchedulerManagerName );
   }
   
   /**
    * Add the Schedules to the Schedule Manager.
    *
    * @jmx:managed-operation
    */
   public void startProviding()
      throws Exception
   {
       //Recover all timers, add them to the scheduler but
       //ignore the calls until the container recovered
   }
   
   /**
    * Stops the Provider from providing and
    * causing him to remove all Schedules
    *
    * @jmx:managed-operation
    */
   public void stopProviding() {
       //Unregister all timers, save them and unregister
       //this provider
   }
   
   protected int addSchedule(
      ObjectName pTarget,
      String pMethodName,
      String[] pMethodSignature,
      Date pStart,
      long pPeriod,
      int pRepetitions
   ) throws
      JMException
   {
      return ( (Integer) server.invoke(
         mScheduleManagerName,
         "addSchedule",
         new Object[] {
            serviceName,
            pTarget,
            pMethodName,
            pMethodSignature,
            pStart,
            new Long( pPeriod ),
            new Integer( (int) pRepetitions )
         },
         new String[] {
            ObjectName.class.getName(),
            ObjectName.class.getName(),
            String.class.getName(),
            String[].class.getName(),
            Date.class.getName(),
            Long.TYPE.getName(),
            Integer.TYPE.getName()
         }
      ) ).intValue();
   }
   
   protected void removeSchedule( int pID )
      throws JMException
   {
      server.invoke(
         mScheduleManagerName,
         "removeSchedule",
         new Object[] { new Integer( pID ) },
         new String[] { Integer.TYPE.getName() }
      );
   }
   
   protected void startService()
        throws Exception
   {
      server.invoke(
         mScheduleManagerName,
         "registerProvider",
         new Object[] { serviceName.toString() },
         new String[] { String.class.getName() }
      );
   }
   
   protected void stopService() {
      try {
         server.invoke(
            mScheduleManagerName,
            "unregisterProvider",
            new Object[] { serviceName.toString() },
            new String[] { String.class.getName() }
         );
      }
      catch( JMException jme ) {
         log.error( "Could not unregister the Provider from the Schedule Manager", jme );
      }
   }
}