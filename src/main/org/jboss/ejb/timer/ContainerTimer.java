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
 * @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 * @version $Revision: 1.5 $
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
   private boolean singleAction = false;

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
           Date pNextTimeout,
           boolean singleAction
           )
   {
      mTimerService = pTimerService;
      mId = pId;
      mKey = pKey;
      mInfo = pInfo;
      mNextTimeout = pNextTimeout;
      this.singleAction = singleAction;
   }

   public void cancel()
           throws
           IllegalStateException,
           NoSuchObjectLocalException,
           EJBException
   {
      if (!mIsActive) throw new NoSuchObjectLocalException("Timer is inactive");
      mTimerService.cancel(mId);
      mIsActive = false;
   }

   public long getTimeRemaining()
           throws
           IllegalStateException,
           NoSuchObjectLocalException,
           EJBException
   {
      if (!mIsActive) throw new NoSuchObjectLocalException("Timer is inactive");
      if (mNextTimeout == null)
      {
         if (isValid())
         {
            throw new NoSuchObjectLocalException("Timer expired or is cancelled");
         }
         else
         {
            throw new EJBException("Could not retrieve next timeout");
         }
      }
      else
      {
         return mNextTimeout.getTime() - System.currentTimeMillis();
      }
   }

   public Date getNextTimeout()
           throws
           IllegalStateException,
           NoSuchObjectLocalException,
           EJBException
   {
      if (!mIsActive) throw new NoSuchObjectLocalException("Timer is inactive");
      return mNextTimeout;
   }

   public Serializable getInfo()
           throws
           IllegalStateException,
           NoSuchObjectLocalException,
           EJBException
   {
      if (!mIsActive) throw new NoSuchObjectLocalException("Timer is inactive");
      return mInfo;
   }

   public TimerHandle getHandle()
           throws
           IllegalStateException,
           NoSuchObjectLocalException,
           EJBException
   {
      if (!mIsActive) throw new NoSuchObjectLocalException("Timer is inactive");
      return new ContainerTimerHandle(this);
   }

   /**
    * @return The Key of the EJB it is assigned to (Entity Beans)
    **/
   public Object getKey()
   {
      if (!mIsActive) throw new NoSuchObjectLocalException("Timer is inactive");
      return mKey;
   }

   /**
    * @return Timer Id
    */
   public Integer getId()
   {
      if (!mIsActive) throw new NoSuchObjectLocalException("Timer is inactive");
      return mId;
   }

   /**
    * @return Container Id this Timer belongs to
    * @deprecated
    **/
   protected String getContainerId()
   {
      if (!mIsActive) throw new NoSuchObjectLocalException("Timer is inactive");
      return mTimerService.getContainerId();
   }

   /**
    * @return True if this Timer did not expire or was cancelled
    **/
   protected boolean isValid()
   {
      //AS TODO: Make it work so that it return false when cancelled
      return mIsActive;
   }

   /**
    * Set the date of the next timeout
    *
    * @param pNextTimeout Date of the next timeout
    **/
   public void setNextTimeout(Date pNextTimeout)
   {
      if (!mIsActive) throw new NoSuchObjectLocalException("Timer is inactive");
      mNextTimeout = pNextTimeout;
   }

   public boolean isSingleAction()
   {
      return singleAction;
   }
}
