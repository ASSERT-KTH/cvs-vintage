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
import java.util.Iterator;
import java.util.List;

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
   /** Map from EJB Timer Id and the Scheduler Id returned by the Scheduler Manager **/
   protected HashMap mTimerToSchedulerIdMap = new HashMap();
   /** Map from Scheduler Id to Timer Id returned by the Timer Source **/
   protected HashMap mSchedulerToTimerIdMap = new HashMap();
   /** Map from EJB Timer Id to Container/Key Item **/
   protected HashMap mTimerList = new HashMap();
   private ObjectName mScheduleManagerName;
   
   public boolean isTransactional() {
       return false;
   }
   
   public boolean isPersistent() {
       return false;
   }
   
   public Integer createTimer( String pContainerId, Object pKey, Date pStartDate, long pInterval, Serializable pInfo ) {
      return createTimer( pContainerId, null, pKey, pStartDate, pInterval, pInfo );
   }
   
   public Integer createTimer( String pContainerId, Integer pId, Object pKey, Date pStartDate, long pInterval, Serializable pInfo ) {
       try {
           Integer lTimerId = pId;
           int lId = addSchedule(
              serviceName,
              "handleTimedEvent",
              new String[] { "ID", "NEXT_DATE" },
              pStartDate,
              pInterval,
              -1
           );
           synchronized( mTimerList ) {
              // When Timer Id is null the get the next available Id otherwise reuse it
              lTimerId = ( lTimerId == null ? new Integer( getTimerId() ) : lTimerId );
              mTimerToSchedulerIdMap.put( lTimerId, new Integer( lId ) );
              mSchedulerToTimerIdMap.put( new Integer( lId ), lTimerId );
              mTimerList.put( lTimerId, new ContainerKey( pContainerId, pKey ) );
              log.info( "Call add() on : " + mPersistenceManager
                 + ", container id: " + pContainerId
                 + ", id: " + lTimerId
              );
              server.invoke(
                 mPersistenceManager,
                 "add",
                 new Object[] { pContainerId, lTimerId, pKey, pStartDate, new Long( pInterval ), pInfo },
                 new String[] {
                     String.class.getName(),
                     Integer.class.getName(),
                     Object.class.getName(),
                     Date.class.getName(),
                     Long.TYPE.getName(),
                     Serializable.class.getName()
                 }
              );
           }
           return lTimerId;
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
   public void removeTimer( Integer pId ) {
      try {
         synchronized( mTimerList ) {
            ContainerKey lContainerKey = (ContainerKey) mTimerList.remove( pId );
            log.info( "Remove timer: " + pId +
               ", of container: " + ( lContainerKey == null ? "item not found" : lContainerKey.getContainerId() ) );
            if( lContainerKey != null ) {
               server.invoke(
                  mPersistenceManager,
                  "remove",
                  new Object[] { lContainerKey.getContainerId(), pId },
                  new String[] {
                     String.class.getName(),
                     Integer.class.getName()
                  }
               );
            }
         }
         Integer lId = (Integer) mTimerToSchedulerIdMap.remove( pId );
         mSchedulerToTimerIdMap.remove( lId );
         removeSchedule( lId.intValue() );
      }
      catch( JMException jme ) {
         jme.printStackTrace();
         throw new RuntimeException( "Could not remove the required timer" );
      }
   }
   
   public List restoreTimers( String pContainerId, ContainerTimerService pTimerService ) {
      List lReturn = null;
      try {
         lReturn = (List) server.invoke(
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
      return lReturn;
   }
   
   /**
    * Timer Source send a timed event to be handled
    *
    * @param pId Scheduler Id
    *
    * @jmx:managed-operation
    */
   public void handleTimedEvent( Integer pId, Date pNextEvent ) {
      ContainerKey lContainerKey = null;
      Integer lId = null;
      synchronized( mTimerList ) {
         lId = (Integer) mSchedulerToTimerIdMap.get( pId );
         lContainerKey = (ContainerKey) mTimerList.get( lId );
      }
      if( lContainerKey != null ) {
          ContainerTimerService lTarget = (ContainerTimerService) getTimerService( lContainerKey );
          if( lTarget != null ) {
              lTarget.handleTimedEvent( lId, pNextEvent );
          } else {
              log.error( "Container found but no Timer Service. Container Key: " + lContainerKey );
          }
      } else {
         // If no Container is found then container did not recover so far
         log.debug( "SchedulerTimerSource.handleTimedEvent(), no timer found for scheduler id: " + pId );
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
      long lPeriod = ( pPeriod < 0 ? 0 : pPeriod );
      long lRepetitions = ( pPeriod < 0 ? 1 : pRepetitions );
      return ( (Integer) server.invoke(
         mScheduleManagerName,
         "addSchedule",
         new Object[] {
            serviceName,
            pTarget,
            pMethodName,
            pMethodSignature,
            pStart,
            new Long( lPeriod ),
            new Integer( (int) lRepetitions )
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