/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.timer;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.ejb.EJBContext;
import javax.ejb.EJBException;
import javax.ejb.Timer;
import javax.ejb.TimerService;

import org.jboss.ejb.Container;

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
   private EJBContext mContext;
   
   private Map mTimerList = new HashMap();
   
   /**
    * Create a Container Timer Service
    *
    * @param ??
    **/
//AS   public ContainerTimerService( AbstractTimerSourceMBean pSource, String pName, Container pContainer ) {
   public ContainerTimerService( AbstractTimerSource pSource, String pName, Container pContainer, EJBContext pContext ) {
       mTimerSource = pSource;
       mContainerName = pName;
       mContainer = pContainer;
       mContext = pContext;
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
       Iterator i = getTimers().iterator();
       while( i.hasNext() ) {
           Timer lTimer = (Timer) i.next();
           lTimer.cancel();
       }
       mTimerSource.removeTimerService( mContainerName );
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
       String lId = mTimerSource.createTimer( mContainerName, pStartDate, pInterval );
       Timer lTimer = new ContainerTimer( this, lId, mContext, pInfo );
       mTimerList.put( lId, lTimer );
       
       return lTimer;
   }
}