/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.timer;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;

import javax.ejb.TimerService;
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
   
   public String createTimer( String pContainerId, Object pKey, Date pStartDate, long pInterval, Serializable pInfo ) {
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
              mTimerList.put( new Integer( lId ), new Item( pContainerId, pKey ) );
              log.info( "Call add() on : " + mPersistenceManager
                 + ", container id: " + pContainerId
                 + ", id: " + lId
              );
              server.invoke(
                 mPersistenceManager,
                 "add",
                 new Object[] { pContainerId, lId + "", pKey, pStartDate, new Long( pInterval ), pInfo },
                 new String[] {
                     String.class.getName(),
                     String.class.getName(),
                     Object.class.getName(),
                     Date.class.getName(),
                     Long.TYPE.getName(),
                     Serializable.class.getName()
                 }
              );
           }
           return lId + "";
       }
       catch( JMException jme ) {
           log.error( "Could not create a timer", jme );
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
            Item lItem = (Item) mTimerList.remove( new Integer( pId ) );
            log.info( "Remove timer: " + pId + ", of container: " + lItem.getId() );
            server.invoke(
               mPersistenceManager,
               "remove",
               new Object[] { lItem.getId(), pId },
               new String[] {
                  String.class.getName(),
                  String.class.getName()
               }
            );
         }
         removeSchedule( new Integer( pId ).intValue() );
      }
      catch( JMException jme ) {
         throw new RuntimeException( "Could not remove the required timer" );
      }
   }
   
   public void recover( String pContainerId, ContainerTimerService pTimerService ) {
      log.info( "===============> recover(), id: " + pContainerId
         + ", timer service: " + pTimerService
      );
      try {
         server.invoke(
            mPersistenceManager,
            "restore",
            new Object[] { pContainerId, pTimerService },
            new String[] {
               String.class.getName(),
               ContainerTimerService.class.getName()
            }
         );
      }
      catch( Exception e ) {
         log.error( "recover(), could not recover all timers", e );
         //AS ToDo: Ignore Exception for now 
      }
   }
   
   /**
    * Timer Source send a timed event to be handled
    *
    * @param pId Id of the timer
    *
    * @jmx:managed-operation
    */
   public void handleTimedEvent( Integer pId ) {
      Item lItem = null;
      synchronized( mTimerList ) {
         lItem = (Item) mTimerList.get( pId );
      }
      if( lItem != null ) {
          ContainerTimerService lTarget = (ContainerTimerService) getTimerService( lItem );
          if( lTarget != null ) {
              lTarget.handleTimedEvent( pId + "" );
          } else {
              log.error( "Container found but no Timer Service. Container ID: " + lItem );
          }
      } else {
         // If no Container is found then container did not recover so far
         log.info( "SchedulerTimerSource.handleTimedEvent(), no timer found for id: " + pId );
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
      super.startService();
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