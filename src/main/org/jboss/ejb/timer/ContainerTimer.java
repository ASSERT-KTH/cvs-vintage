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
 **/
public class ContainerTimer
    implements Timer
{
   private ContainerTimerService mTimerService;
   private String mId;
//AS   private EJBContext mContext;
   private Object mKey;
   private Serializable mInfo;
   
   public ContainerTimer(
      ContainerTimerService pTimerService,
      String pId,
      Object pKey,
      Serializable pInfo
   ) {
      mTimerService = pTimerService;
      mId = pId;
      mKey = pKey;
      mInfo = pInfo;
   }
   
   public void cancel()
      throws
         IllegalStateException,
         NoSuchObjectLocalException,
         EJBException
   {
       mTimerService.cancel( mId );
   }
   
   public long getTimeRemaining()
      throws
         IllegalStateException,
         NoSuchObjectLocalException,
         EJBException
   {
      return mTimerService.getTimeRemaining( mId );
   }
   
   public Date getNextTimeout()
      throws
         IllegalStateException,
         NoSuchObjectLocalException,
         EJBException
   {
      return mTimerService.getNextTimeout( mId );
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
      throw new RuntimeException( "Not implemented yet" );
      //AS TODO: The handle needs parameters
//AS      return new ContainerTimerHandle();
   }
   
   /**
    * @return The Key of the EJB it is assigned to (Entity Beans)
    **/
   public Object getKey() {
      return mKey;
   }
   
   public String getId() {
      return mId;
   }
}
