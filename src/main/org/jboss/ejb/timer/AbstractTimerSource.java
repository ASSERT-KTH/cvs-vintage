/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.timer;

import org.jboss.ejb.Container;
import org.jboss.ejb.txtimer.TimedObjectId;
import org.jboss.system.ServiceMBeanSupport;

import javax.ejb.TimerService;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


/**
 * This class is the bridge between an (external) Timer Source
 * used to manage Timers and to provide the Container with the
 * timed event to invoke the Bean's ejbTimeout() method.
 *
 * The Timer Source implementation is reponsible to support that persistence
 * timers maintain their Id.
 *
 * @jmx.mbean name="jboss:service=EJBTimerSource"
 *            extends="org.jboss.system.ServiceMBean"
 *
 * @author <a href="mailto:andreas.schaefer@madplanet.com">Andreas Schaefer</a>
 **/
public abstract class AbstractTimerSource
   extends ServiceMBeanSupport
   implements AbstractTimerSourceMBean
{
   private static int sCounter;
   
   private HashMap mContainerTimerServices = new HashMap();
   protected ObjectName mPersistenceManager;
   
   /**
    * @return The next available persistent Id for a timer.
    **/
   protected static int getTimerId() {
      return sCounter++;
   }
   
   /**
    * Indicates if the Timer Source supports transaction. If it does then
    * it has to be able to participate in a Tx by ??
    * If it does not then this service will take care of it.
    *
    * @return True if the Timer Source is able to participate in a Tx
    **/
   public abstract boolean isTransactional();
   
   /**
    * Indicates if the Timer Source supports persistence of its timers
    * meaning that all active timers are restored when the server is
    * restarted after a shutdown or crash.
    * If it does not then this service will take care of it by using the
    * assigned Persistence Manager.
    *
    * @return True if the Timer Source is persistent and CAN recover
    *         after a crash or server restart
    *
    * @see #setPersistenceManagerName
    **/
   public abstract boolean isPersistent();
   
   /**
    * Set the name of an external Persistence Manager if the Timer Source
    * does not support persistence. If no persistence manager is set then
    * the internal file based implementation is used.
    *
    * @param pObjectName Object Name of the Persistence Manager Service
    *
    * @throws IllegalArgumentException When Object Name is null, the service
    *                                  is not active or does not implement the
    *                                  required methods
    **/
   public void setPersistenceManagerName( String pObjectName )
      throws IllegalArgumentException
   {
      if( mPersistenceManager != null ) {
          throw new IllegalArgumentException( "Persistence Manager Name is already set and cannot be changed" );
      }
      if( pObjectName == null ) {
          throw new IllegalArgumentException( "Persistence Manager Name must not be null" );
      }
      ObjectName lPersistenceManager = null;
      try {
          lPersistenceManager = new ObjectName( pObjectName );
      }
      catch( MalformedObjectNameException mone ) {
          throw new IllegalArgumentException( "Persistence Manager Name ( " + pObjectName +
             " ) is not valid: " + mone );
      }
      if( !server.isRegistered( lPersistenceManager ) ) {
          throw new IllegalArgumentException( "Persistence Manager ( " + pObjectName +
             " ) is not active" );
      }
      try {
         MBeanOperationInfo[] lOperations = server.getMBeanInfo( lPersistenceManager ).getOperations();
         boolean lAddFound = false;
         boolean lRemoveFound = false;
         boolean lRestoreFound = false;
         for( int i = 0; i < lOperations.length; i++ ) {
             if( "add".equals( lOperations[ i ].getName() ) ) {
                 MBeanParameterInfo[] lParameters = lOperations[ i ].getSignature();
                 if( lParameters.length == 6 &&
                    "java.lang.String".equals( lParameters[ 0 ].getType() ) &&
                    "java.lang.Integer".equals( lParameters[ 1 ].getType() ) &&
                    "java.lang.Object".equals( lParameters[ 2 ].getType() ) &&
                    "java.util.Date".equals( lParameters[ 3 ].getType() ) &&
                    Long.TYPE.getName().equals( lParameters[ 4 ].getType() ) &&
                    "java.io.Serializable".equals( lParameters[ 5 ].getType() )
                 ) {
                     lAddFound = true;
                 }
             }
             if( "remove".equals( lOperations[ i ].getName() ) ) {
                 MBeanParameterInfo[] lParameters = lOperations[ i ].getSignature();
                 if( lParameters.length == 2 &&
                    "java.lang.String".equals( lParameters[ 0 ].getType() ) &&
                    "java.lang.Integer".equals( lParameters[ 1 ].getType() )
                 ) {
                     lRemoveFound = true;
                 }
             }
             if( "restore".equals( lOperations[ i ].getName() ) ) {
                 MBeanParameterInfo[] lParameters = lOperations[ i ].getSignature();
                 if( lParameters.length == 2
                    && "java.lang.String".equals( lParameters[ 0 ].getType() )
                    && "org.jboss.ejb.timer.ContainerTimerService".equals( lParameters[ 1 ].getType() )
                 ) {
                    if( "java.util.List".equals( lOperations[ i ].getReturnType() ) ) {
                       lRestoreFound = true;
                    }
                 }
             }
         }
         if( !lAddFound ) {
             throw new IllegalArgumentException( "Persistence Manager ( " + pObjectName +
                " ) does not contain method: add( java.lang.String, javax.ejb.Timer )" );
         }
         if( !lRemoveFound ) {
             throw new IllegalArgumentException( "Persistence Manager ( " + pObjectName +
                " ) does not contain method: remove( java.lang.String, javax.ejb.Timer )" );
         }
         if( !lRestoreFound ) {
             throw new IllegalArgumentException( "Persistence Manager ( " + pObjectName +
                " ) does not contain method: restore( java.lang.String )" );
         }
      }
      catch( Exception e ) {
         throw new IllegalArgumentException(
            "Could not handle the persistence manager: " + pObjectName + ", exception: " + e
         );
      }
      mPersistenceManager = lPersistenceManager;
   }
   
   /**
    * Creates a new timer to be invoked once, a definte or infinite number
    * of times.
    *
    * @return Id of the Timer
    **/
   public abstract Integer createTimer(
      String pContainerId,
      Object pKey,
      Date pStartDate,
      long pInterval,
      Serializable pInfo
   );
   
   /**
    * Creates a timer but uses the given Id so that a recreation of a timer can keep its id
    **/
   public abstract Integer createTimer(
      String pContainerId,
      Integer pId,
      Object pKey,
      Date pStartDate,
      long pInterval,
      Serializable pInfo
   );
   
   /**
    * Removes the given Timer if still active or not
    *
    * @param pId Id returned from {@link #createTimer createTimer()}
    **/
   public abstract void removeTimer( Integer pId );
   
   /**
    * Recover all the timers for the given Container.
    *
    * @param pContainerId Id of the container to be recovered
    * @param pTimerService Timer Service handling the timer creation 
    **/
   public void recover(
      String pContainerId,
      ContainerTimerService pTimerService
   ) {
      log.debug( "recover(), id: " + pContainerId
         + ", timer service: " + pTimerService
      );
      try {
         List lReps = restoreTimers( pContainerId, pTimerService );
         if( lReps != null && !lReps.isEmpty() ) {
            // Restore the Timers
            Iterator i = lReps.iterator();
            while( i.hasNext() ) {
                ContainerTimerRepresentative lRep = (ContainerTimerRepresentative) i.next();
                Integer lId = lRep.getId();
                pTimerService.restoreTimer(
                   lRep.getId(),
                   lRep.getKey(),
                   lRep.getStartDate(),
                   lRep.getInterval(),
                   lRep.getInfo()
                );
                // Make sure that this Id is not used later
                sCounter = ( sCounter >= lId.intValue() ? sCounter : lId.intValue() );
            }
         }
      }
      catch( Exception e ) {
         log.error( "recover(), could not recover all timers", e );
         //AS ToDo: Ignore Exception for now 
      }
   }
   
   /**
    * Restore all the timer representatives from the persistence service
    * in order to recreate the timers afterwards.
    *
    * @param pContainerId Id of the container to be recovered
    * @param pTimerService Timer Service handling the timer creation
    *
    * @return A list of {@link ContainerTimerRepresentative Container Timer Representative}
    *         instances.
    **/
   public abstract List restoreTimers( String pContainerId, ContainerTimerService pTimerService );
    
   /**
    * Create a Timer Service for a Container
    *
    * @param pContainerId Identification of the Container
    *
    * @jmx.managed-operation
    **/
   public TimerService createTimerService(String pContainerId, Object pKey, Container pContainer) {
      TimerService lService = null;
      ContainerKey lContainerKey = new ContainerKey( pContainerId, pKey );
      if( mContainerTimerServices.containsKey( lContainerKey ) ) {
         lService = getTimerService( lContainerKey );
      } else {
         lService = new ContainerTimerService( this, pContainerId, pContainer, pKey );
         mContainerTimerServices.put( lContainerKey, lService );
      }
      return lService;
   }
    
   /**
    * Removes the Timer Service for the given Container
    *
    * @param pContainerId Id of the Container to be removed
    **/
   public void removeTimerService( String pContainerId, Object pKey ) {
      mContainerTimerServices.remove( new ContainerKey( pContainerId, pKey ) );
   }
   
   /**
    * Delivers the Timer Service for a given Container Id
    *
    * @param pContainerKey Id of the Container to look for its Timer Service
    *
    * @return Timer Service if found otherwise null
    **/
   protected TimerService getTimerService( ContainerKey pContainerKey ) {
      return (TimerService) mContainerTimerServices.get( pContainerKey );
   }
   
   /**
    * Start this service by setting up transaction and presistence support
    * if not provided by the Timer Source.
    * ATTENTION: You have to call this method if you overwrite this method
    **/
   protected void startService()
     throws Exception
   {
      // Install Persistence Service if necessary
      if( !isPersistent() ) {
         // Check if there is a Persistence Manager set otherwise use the
         // default, file-based PM
         if( mPersistenceManager == null ) {
            try {
               mPersistenceManager = server.createMBean(
                  "org.jboss.ejb.timer.FilePersistenceManager",
                  new ObjectName( "jboss:service=TimePersistenceManager,type=File" )
               ).getObjectName();
               server.invoke(
                  mPersistenceManager,
                  "create",
                  new Object[] {},
                  new String[] {}
               );
               server.invoke(
                  mPersistenceManager,
                  "start",
                  new Object[] {},
                  new String[] {}
               );
            }
            catch( Exception e ) {
               //AS ToDo: Handle an exception properly
               log.error( "Could not create the default Timer persistence manager", e );
            }
         }
      }
   }
   
   /**
    * This class contains a Container Id and a Key object to be
    * used as key for a Map to be looked up together
    **/
   protected static class ContainerKey {
      
      private String mId;
      private Object mKey;
      
      /**
       * Create an Instance to lookup a Timer Service
       * by the Container Id and Key (if available)
       *
       * @param pContainerId Id of the Container
       * @param pKey Key of the EJB instance the Timer belongs to
       *             or null if not applicable
       **/
      public ContainerKey( String pContainerId, Object pKey ) {
         mId = pContainerId;
         mKey = pKey;
      }
      
      public String getContainerId() {
         return mId;
      }
      
      public Object getKey() {
         return mKey;
      }
      
      public String toString() {
         return "AbstractTimerSource.ContainerKey [ "
            + ", container id: " + mId
            + ", key: " + mKey
            + " ]";
      }
      
      public boolean equals( Object pTest ) {
         if( pTest instanceof ContainerKey ) {
            ContainerKey lContainerKey = (ContainerKey) pTest;
            return mId.equals( lContainerKey.mId )
               && ( ( mKey == null && lContainerKey.mKey == null )
                      || ( mKey.equals( lContainerKey.mKey ) ) );
         } else {
            return false;
         }
      }
      
      public int hashCode() {
         return mId.hashCode()
            + ( mKey == null ? 0 : mKey.hashCode() );
      }
   }
}