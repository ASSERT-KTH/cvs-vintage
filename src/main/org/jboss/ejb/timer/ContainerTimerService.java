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
import java.util.Map;
import java.lang.reflect.Method;

import javax.ejb.EJBException;
import javax.ejb.Timer;
import javax.ejb.TimerService;
import javax.ejb.TimedObject;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;

import org.jboss.ejb.Container;
import org.jboss.logging.Logger;
import org.jboss.mx.util.SerializationHelper;
import org.jboss.invocation.LocalEJBInvocation;
import org.jboss.invocation.InvocationType;

/**
 * Timer Service of a Container acting also as bridge between
 * the Container and the Timer Source.
 *
 * @author <a href="mailto:andreas.schaefer@madplanet.com">Andreas Schaefer</a>
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 1.7 $
 **/
public class ContainerTimerService
    implements TimerService
{
//AS   private AbstractTimerSourceMBean mTimerSource;
   private AbstractTimerSource mTimerSource;
   private String mContainerName;
   private Container mContainer;
   private Object mKey;
   
   private Map mTimerList = new HashMap();
   private Logger mLog = Logger.getLogger( this.getClass().getName() );
   
    /**
     * Create a Container Timer Service
     *
     * @param pSource Timer Source Instance
     * @param pName Name of the EJB Container
     * @param pContainer Container this Service is assigned to
     * @param pKey Key of the Entity or null otherwise
     **/
//AS   public ContainerTimerService( AbstractTimerSourceMBean pSource, String pName, Container pContainer ) {
   public ContainerTimerService( AbstractTimerSource pSource, String pName, Container pContainer, Object pKey ) {
       mTimerSource = pSource;
       mContainerName = pName;
       mContainer = pContainer;
//AS       mContext = pContext;
       mKey = pKey;
   }
   
    /**
     * Start the recovery of the timers
     */
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
      synchronized( mTimerList ) {
         return mTimerList.values();
      }
   }
   
   //--------------- Timer Callback Methods -----------------------------------------
   
   /**
    * Cancel a Timer by removing from the Timer Source
    *
    * @param pId Id of the timer to be removed
    *
    * @see javax.ejb.Timer#cancel
    **/
   protected void cancel( Integer pId ) {
      mLog.debug( "cancel(), cancel timer: " + pId );
      mTimerSource.removeTimer( pId );
      mTimerList.remove( pId );
   }
   
   //--------------- Timed Notification Methods -------------------------------------
   
    /**
     * Handles a Timer Event by looking up the appropriate timer and
     * invoking the appropriate method on the container
     *
     * @param pId Timer Id
     * @param pNextEvent Date of the Next event or null if a single time timer
     */
    public void handleTimedEvent(Integer pId, Date pNextEvent)
    {
       ContainerTimer lTimer = (ContainerTimer) mTimerList.get(pId);
       if (lTimer != null)
       {
          mLog.debug("handleTimedEvent(), this: " + this
                  + ", call timer: " + lTimer
                  + ", container: " + mContainer);
          handleEjbTimeout(mContainer, lTimer);
          if (lTimer.isSingleAction())
             lTimer.cancel();
          else
             lTimer.setNextTimeout(pNextEvent);
       }
    }
   
   /**
    * Handles an Timed Event by gettting the appropriate EJB instance,
    * invoking the "ejbTimeout()" method on it with the given timer
    *
    * @param pTimer Timer causing this event
    **/
   private void handleEjbTimeout(Container container, Timer pTimer)
   {
      ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(container.getClassLoader());

      Object id = ((ContainerTimer) pTimer).getKey();
      try
      {
         Method ejbTimeout = TimedObject.class.getMethod("ejbTimeout", new Class[]{Timer.class});
         LocalEJBInvocation invocation = new LocalEJBInvocation(
                 id,
                 ejbTimeout,
                 new Object[]{pTimer},
                 null,
                 null,
                 null
         );
         invocation.setType(InvocationType.LOCAL);

         container.invoke(invocation);
         TransactionManager transactionManager = container.getTransactionManager();
         if (transactionManager.getTransaction() != null)
         {
            Transaction tx = transactionManager.getTransaction();
            mLog.error("TRANSACTION IS STILL ALIVE!!!!!" + tx.getStatus());
         }
      }
      catch (Exception e)
      {
         e.printStackTrace();
         throw new RuntimeException("call ejbTimeout() failed: " + e);
      }
              /*AS TODO: Manage the exceptions properly
                catch (AccessException ae)
                {
                throw new AccessLocalException( ae.getMessage(), ae );
                }
                catch (NoSuchObjectException nsoe)
                {
                throw new NoSuchObjectLocalException( nsoe.getMessage(), nsoe );
                }
                catch (TransactionRequiredException tre)
                {
                throw new TransactionRequiredLocalException( tre.getMessage() );
                }
                catch (TransactionRolledbackException trbe)
                {
                throw new TransactionRolledbackLocalException(
                trbe.getMessage(), trbe );
                }
              */
      finally
      {
         Thread.currentThread().setContextClassLoader(oldCl);
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
    * Looks up for a timer with the given Id
    *
    * @param pId Id of the Timer managed by this Timer Service
    *
    * @return Timer if found otherwise null
    **/
   protected Timer getTimer( Integer pId ) {
      return (Timer) mTimerList.get( pId );
   }
   
    /**
     * @return Container Id of the Container this Timer Service is assigned to
     */
   protected String getContainerId() {
      return mContainerName;
   }
   
    /**
     * Restore a Timer. If the given key is not null this method will also create a
     * new Timer Service with the given key and inform the Entity Container about it.
     *
     * @param pId Timer Id that has to be kept
     * @param pKey Primary Key if it is a Entity Timer otherwise null
     * @param pStartDate Date when the timer is the first time invoked.
     * @param pInterval Time between interval calls. A negative value indicates
     *                        a single time timer
     * @param pInfo User defined object passed back by the timer
     * @return Restored Timer
     *
     * @throws IllegalArgumentException If timer could not be created
     * @throws IllegalStateException If the creation of the timer is not allowed
     * @throws EJBException If the system could not recreate the timer
     **/
   protected Timer restoreTimer( Integer pId, Object pKey, Date pStartDate, long pInterval, Object pInfo )
      throws
         IllegalArgumentException,
         IllegalStateException,
         EJBException
   {
       Timer lTimer = null;
       Serializable lInfo = (Serializable) recreateObject( pInfo );
       // Only recreate Timer Service if not already done
       Object lKey = recreateObject( pKey );
       if( lKey != null && mKey == null ) {
          // If a Key is given then let the Container to create a new Timer Service and
          // then let this timer service create the timer
          ContainerTimerService lTimerService = (ContainerTimerService) mContainer.getTimerService(
             lKey
          );
           mLog.debug( "restoreTimer(), AFTER CTS is created, container: " + mContainerName
              + ", id: " + pId + ", key: " + lKey
              + ", start date: " + pStartDate
              + ", interval: " + pInterval + ", info: " + lInfo );
          lTimer = lTimerService.restoreTimer( pId, lKey, pStartDate, pInterval, (Object) lInfo );
       } else {
           mLog.debug( "restoreTimer(), container: " + mContainerName + ", id: " + pId
              + ", key: " + lKey + ", start date: " + pStartDate
              + ", interval: " + pInterval + ", info: " + lInfo );
           mTimerSource.createTimer( mContainerName, pId, mKey, pStartDate, pInterval, lInfo );
           //AS TODO: The next timeout (last parameter) does not have to be true here when the
           //AS       timer was already called (then the start date is in the past)
           lTimer = new ContainerTimer( this, pId, mKey, lInfo, pStartDate, (pInterval == -1) );
           mTimerList.put( pId, lTimer );
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
       synchronized( mTimerList ) {
          Integer lId = mTimerSource.createTimer( mContainerName, mKey, pStartDate, pInterval, pInfo );
          Timer lTimer = new ContainerTimer( this, lId, mKey, pInfo, pStartDate, (pInterval == -1) );
          mTimerList.put( lId, lTimer );
          return lTimer;
       }
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