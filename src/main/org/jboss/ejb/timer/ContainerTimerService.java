/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.timer;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

//AS import javax.ejb.EJBContext;
import javax.ejb.EJBException;
import javax.ejb.Timer;
import javax.ejb.TimerService;

import org.jboss.ejb.Container;
import org.jboss.logging.Logger;
import org.jboss.mx.util.SerializationHelper;

/**
 * Timer Service of a Container acting also as bridge between
 * the Container and the Timer Source.
 **/
public class ContainerTimerService
    implements TimerService
{
//AS   private AbstractTimerSourceMBean mTimerSource;
   private AbstractTimerSource mTimerSource;
   private String mContainerName;
   private Container mContainer;
//AS   private EJBContext mContext;
   private Object mKey;
   
   private Map mTimerList = new HashMap();
   private Logger mLog = Logger.getLogger( this.getClass().getName() );
   
   /**
    * Create a Container Timer Service
    *
    * @param ??
    **/
//AS   public ContainerTimerService( AbstractTimerSourceMBean pSource, String pName, Container pContainer ) {
   public ContainerTimerService( AbstractTimerSource pSource, String pName, Container pContainer, Object pKey ) {
       mTimerSource = pSource;
       mContainerName = pName;
       mContainer = pContainer;
//AS       mContext = pContext;
       mKey = pKey;
   }
   
   public void startRecovery() {
      // Start Recover Process 
      mTimerSource.recover( mContainerName, this );
   }
   //--------------- Timer Service Implementation -----------------------------------
   
   public Timer createTimer( long duration, Serializable info )
      throws
         IllegalArgumentException,
         IllegalStateException,
         EJBException
   {
       if( duration < 0 ) {
           throw new IllegalArgumentException( "Duration until this single time timer is called " +
              "must not be negative" );
       }
       Date lStartDate = new Date( new Date().getTime() + duration );
       return createInternalTimer( lStartDate, -1, info );
   }
   
   public Timer createTimer( long initialDuration, long intervalDuration, Serializable info )
      throws
         IllegalArgumentException,
         IllegalStateException,
         EJBException
   {
       if( initialDuration < 0 ) {
           throw new IllegalArgumentException( "Initial Duration until this single timer is called " +
              "the first time must not be negative" );
       }
       if( intervalDuration < 0 ) {
           throw new IllegalArgumentException( "Interval between two timer calls " +
              "must not be negative" );
       }
       Date lStartDate = new Date( new Date().getTime() + initialDuration );
       return createInternalTimer( lStartDate, intervalDuration, info );
   }
   
   public Timer createTimer( Date expiration, Serializable info )
      throws
         IllegalArgumentException,
         IllegalStateException,
         EJBException
   {
       if( expiration == null || expiration.getTime() < 0 ) {
           throw new IllegalArgumentException( "Experation Date for this single timer " +
              "must not be null or time negative" );
       }
       return createInternalTimer( expiration, -1, info );
   }
   
   public Timer createTimer( Date initialExpiration, long intervalDuration, Serializable info )
      throws
         IllegalArgumentException,
         IllegalStateException,
         EJBException
   {
       if( initialExpiration == null || initialExpiration.getTime() < 0 ) {
           throw new IllegalArgumentException( "Initial Experation Date for this timer " +
              "must not be null or time negative" );
       }
       if( intervalDuration < 0 ) {
           throw new IllegalArgumentException( "Interval between two timer calls " +
              "must not be negative" );
       }
       return createInternalTimer( initialExpiration, intervalDuration, info );
   }
   
   public Collection getTimers()
      throws
         IllegalStateException,
         EJBException
   {
       return mTimerList.values();
   }
   
   //--------------- Timer Callback Methods -----------------------------------------
   
   /**
    * Cancel a Timer by removing from the Timer Source
    *
    * @param pId Id of the timer to be removed
    *
    * @see javax.ejb.TimerService#cancel
    **/
   protected void cancel( String pId ) {
      mLog.debug( "cancel(), cancel timer: " + pId );
      mTimerSource.removeTimer( pId );
      mTimerList.remove( pId );
   }
   
   /**
    * Retrieves the time remaining until the next timed event
    *
    * @param pId Id of the Timer
    *
    * @see javax.ejb.TimerService#getTimeRemaining
    **/
   protected long getTimeRemaining( String pId ) {
      //AS TODO: Finish the implemenation
      throw new RuntimeException( "Not implemented yet" );
   }
   
   /**
    * Retrieves the date of the next timed event
    *
    * @param pId Id of the Timer
    *
    * @see javax.ejb.TimerService#getNextTimeout
    **/
   protected Date getNextTimeout( String pId ) {
      //AS TODO: Finish the implemenation
      throw new RuntimeException( "Not implemented yet" );
   }
   
   //--------------- Timed Notification Methods -------------------------------------
   
   public void handleTimedEvent( String pId ) {
       Timer lTimer = (Timer) mTimerList.get( pId );
       if( lTimer != null ) {
           mContainer.handleEjbTimeout( lTimer );
       }
   }
   
   /**
    * Stops this Timer Service by removing all its timers
    * and remove itself from the Timer Source.
    **/
   public void stopService() {
       Timer[] lTimers = (Timer[]) getTimers().toArray( new Timer[] {} );
       for( int i = 0; i < lTimers.length; i++ ) {
           lTimers[ i ].cancel();
       }
       mTimerSource.removeTimerService( mContainerName, mKey );
   }
   
   //--------------- Protected Methods ---------------------------------------------
   
   /**
    * Restore a Timer. If the given key is not null this method will also create a
    * new Timer Service with the given key and inform the Entity Container about it.
    *
    * @param pKey Primary Key if it is a Entity Timer otherwise null
    * @param pStartDate Date when the timer is the first time invoked.
    * @param pInterval Time between interval calls. A negative value indicates
    *                  a single time timer
    * @param pInfo User defined object passed back by the timer
    **/
   protected Timer restoreTimer( Object pKey, Date pStartDate, long pInterval, Object pInfo )
      throws
         IllegalArgumentException,
         IllegalStateException,
         EJBException
   {
       Timer lTimer = null;
       Serializable lInfo = (Serializable) recreateObject( pInfo );
       if( pKey != null ) {
          // First we have to try to restore the key if a byte arrray
          pKey = recreateObject( pKey );
          if( pKey instanceof byte[] ) {
             try {
                pKey = SerializationHelper.deserialize( (byte[]) pKey, mContainer.getClassLoader() );
             }
             catch( IOException ioe ) {
                ioe.printStackTrace();
                // Ingore it
             }
             catch( ClassNotFoundException cnfe ) {
                cnfe.printStackTrace();
                // Ingore it
             }
          }
          // If a Key is given then let the Container to create a new Timer Service and
          // then let this timer service create the timer
          TimerService lTimerService = mContainer.createTimerService(
             pKey
          );
          if( pInterval < 0 ) {
             lTimer = lTimerService.createTimer( pStartDate, lInfo );
             mLog.debug( "restoreTimer(), created entity timer: " + lTimer
                + ", timer source: " + lTimerService );
          } else {
             lTimer = lTimerService.createTimer( pStartDate, pInterval, lInfo );
          }
       } else {
           String lId = mTimerSource.createTimer( mContainerName, mKey, pStartDate, pInterval, lInfo );
           lTimer = new ContainerTimer( this, lId, mKey, lInfo );
           mTimerList.put( lId, lTimer );
       }
       
       return lTimer;
   }
   
   //--------------- Private Methods ------------------------------------------------
   
   /**
    * Create a timer by using the Timer Source
    *
    * @param pStartDate Date when the timer is the first time invoked.
    * @param pInterval Time between interval calls. A negative value indicates
    *                  a single time timer
    * @param pInfo User defined object passed back by the timer
    **/
   private Timer createInternalTimer( Date pStartDate, long pInterval, Serializable pInfo )
      throws
         IllegalArgumentException,
         IllegalStateException,
         EJBException
   {
       String lId = mTimerSource.createTimer( mContainerName, mKey, pStartDate, pInterval, pInfo );
       Timer lTimer = new ContainerTimer( this, lId, mKey, pInfo );
       mTimerList.put( lId, lTimer );
       
       return lTimer;
   }
   
   /**
    * Tries to recreate an object if the given object is a byte array
    *
    * @param pBytes Object that could represents a byte array that could represent an object
    *
    * @return Recreated Object or the given object if the given object is not an byte array
    *         or the recreation failed
    **/
   private Object recreateObject( Object pBytes ) {
      Object lReturn = pBytes;
      if( pBytes instanceof byte[] ) {
         try {
            lReturn = SerializationHelper.deserialize( (byte[]) pBytes, mContainer.getClassLoader() );
         }
         catch( IOException ioe ) {
            ioe.printStackTrace();
            // Ingore it
         }
         catch( ClassNotFoundException cnfe ) {
            cnfe.printStackTrace();
            // Ingore it
         }
      }
      return lReturn;
   }
}