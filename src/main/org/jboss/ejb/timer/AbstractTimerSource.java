/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.timer;

import java.io.EOFException;
import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.ejb.EJBContext;
import javax.ejb.TimerService;
import javax.management.MalformedObjectNameException;
import javax.management.MBeanInfo;
import javax.management.MBeanOperationInfo;
import javax.management.MBeanParameterInfo;
import javax.management.ObjectName;

import org.jboss.ejb.Container;
import org.jboss.system.ServiceMBeanSupport;


/**
 * This class is the bridge between an (external) Timer Source
 * used to manage Timers and to provide the Container with the
 * timed event to invoke the Bean's ejbTimeout() method
 *
 * @jmx:mbean name="jboss:service=EJBTimerSource"
 *            extends="org.jboss.system.ServiceMBean"
 *
 * @author <a href="mailto:andreas.schaefer@madplanet.com">Andreas Schaefer</a>
 **/
public abstract class AbstractTimerSource
   extends ServiceMBeanSupport
   implements AbstractTimerSourceMBean
{   
   private HashMap mContainerTimerServices = new HashMap();
   protected ObjectName mPersistenceManager;
   
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
                    "java.lang.String".equals( lParameters[ 1 ].getType() ) &&
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
                    "java.lang.String".equals( lParameters[ 1 ].getType() )
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
    * @param ??
    *
    * @return Id of the Timer
    **/
   public abstract String createTimer( String pContainerId, Object pKey, Date pStartDate, long pInterval, Serializable pInfo );
   
   /**
    * Removes the given Timer if still active or not
    *
    * @param pId Id returned from {@link #createTimer createTimer()}
    **/
   public abstract void removeTimer( String pId );
   
   /**
    * Recover all the timers for the given Container.
    *
    * When the server goes down and comes back no timer are recovered
    * initially. Every container hosting a EJB implementing the Timed-
    * Object interface will call this method to recover its timers.
    * The implementation can choose to recover the timers immediately
    * but has to make sure that no timed events are sent to the Container
    * until it this method is called.
    *
    * @param pContainerId Id of the container to be recovered
    * @param pTimerService Timer Service handling the timer creation 
    **/
   public abstract void recover( String pContainerId, ContainerTimerService pTimerService );
    
   /**
    * Create a Timer Service for a Container
    *
    * @param pContainerId Identification of the Container
    *
    * @jmx:managed-operation
    **/
   public TimerService createTimerService( String pContainerId, Container pContainer, Object pKey ) {
      TimerService lService = null;
      Item lItem = new Item( pContainerId, pKey );
      if( mContainerTimerServices.containsKey( lItem ) ) {
         lService = getTimerService( lItem );
      } else {
         lService = new ContainerTimerService( this, pContainerId, pContainer, pKey );
         mContainerTimerServices.put( lItem, lService );
      }
      return lService;
   }
    
   /**
    * Removes the Timer Service for the given Container
    *
    * @param pContainerId Id of the Container to be removed
    **/
   public void removeTimerService( String pContainerId, Object pKey ) {
      mContainerTimerServices.remove( new Item( pContainerId, pKey ) );
   }
   
   /**
    * Delivers the Timer Service for a given Container Id
    *
    * @param pContainerId Id of the Container to look for its Timer Service
    *
    * @return Timer Service if found otherwise null
    **/
   protected TimerService getTimerService( Item pItem ) {
      return (TimerService) mContainerTimerServices.get( pItem );
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
   
   protected static class Item {
      
      private String mId;
      private Object mKey;
      
      public Item( String pId, Object pKey ) {
         mId = pId;
         mKey = pKey;
      }
      
      public String getId() {
         return mId;
      }
      
      public Object getKey() {
         return mKey;
      }
      
      public String toString() {
         return "AbstractTimerSource.Item [ "
            + ", id: " + mId
            + ", key: " + mKey
            + " ]";
      }
      
      public boolean equals( Object pTest ) {
         if( pTest instanceof Item ) {
            Item lItem = (Item) pTest;
            return mId.equals( lItem.mId )
               && ( ( mKey == null && lItem.mKey == null )
                      || ( mKey.equals( lItem.mKey ) ) );
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