/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.timer;

import java.io.Serializable;
import java.util.Date;

import javax.ejb.EJBContext;
import javax.ejb.EJBException;
import javax.ejb.NoSuchObjectLocalException;
import javax.ejb.Timer;
import javax.ejb.TimerHandle;

/**
 * The Timer interface contains information about a timer that was created
 * through the EJB Timer Service
 *
 * @author <a href="mailto:andreas.schaefer@madplanet.com">Andreas Schaefer</a>
 * @version $Revision: 1.3 $
 **/
public class ContainerTimer
    implements Timer
{
   private ContainerTimerService mTimerService;
   private Integer mId;
//AS   private EJBContext mContext;
   private Object mKey;
   private Serializable mInfo;
   private boolean mIsActive = true;
   private Date mNextTimeout;
   
   /**
    * Creates a Timer
    *
    * @param pTimerService Timer Service this Timer belongs to
    * @param pId Timer Id
    * @param pKey Primary Key if it belongs to an Entity otherwise null
    * @param pInfo User Object
    * @param pNextTimeout Date of the next timeout or null if a single time timer
    **/
   public ContainerTimer(
      ContainerTimerService pTimerService,
      Integer pId,
      Object pKey,
      Serializable pInfo,
      Date pNextTimeout
   ) {
      mTimerService = pTimerService;
      mId = pId;
      mKey = pKey;
      mInfo = pInfo;
      mNextTimeout = pNextTimeout;
   }
   
   public void cancel()
      throws
         IllegalStateException,
         NoSuchObjectLocalException,
         EJBException
   {
       mTimerService.cancel( mId );
       mIsActive = false;
   }
   
   public long getTimeRemaining()
      throws
         IllegalStateException,
         NoSuchObjectLocalException,
         EJBException
   {
      if( mNextTimeout == null ) {
         if( isValid() ) {
            throw new NoSuchObjectLocalException( "Timer expired or is cancelled" );
         } else {
            throw new EJBException( "Could not retrieve next timeout" );
         }
      } else {
         return new Date().getTime() - mNextTimeout.getTime();
      }
   }
   
   public Date getNextTimeout()
      throws
         IllegalStateException,
         NoSuchObjectLocalException,
         EJBException
   {
      return mNextTimeout;
   }
   
   public Serializable getInfo()
      throws
         IllegalStateException,
         NoSuchObjectLocalException,
         EJBException
   {
       return mInfo;
   }
   
   public TimerHandle getHandle()
      throws
         IllegalStateException,
         NoSuchObjectLocalException,
         EJBException
   {
      return new ContainerTimerHandle( this );
   }
   
   /**
    * @return The Key of the EJB it is assigned to (Entity Beans)
    **/
   public Object getKey() {
      return mKey;
   }
   
   /**
    * @return Timer Id
    */
   public Integer getId() {
      return mId;
   }
   
   /**
    * @return Container Id this Timer belongs to
    * @deprecated
    **/
   protected String getContainerId() {
      return mTimerService.getContainerId();
   }
   
   /**
    * @return True if this Timer did not expire or was cancelled
    **/
   protected boolean isValid() {
      //AS TODO: Make it work so that it return false when cancelled
      return mIsActive;
   }
   
   /**
    * Set the date of the next timeout
    *
    * @param pNextTimeout Date of the next timeout
    **/
   public void setNextTimeout( Date pNextTimeout ) {
      mNextTimeout = pNextTimeout;
   }
}
