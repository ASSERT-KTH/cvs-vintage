/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.timer;

import java.io.Serializable;
import java.util.Date;

/**
 * A container with all the data to store and recreate a timer in a persistence
 * service used then in {@link AbstractTimerSource#restore AbstractTimerSource.restore}
 * to restore it.
 *
 * @author <a href="mailto:andreas.schaefer@madplanet.com">Andreas Schaefer</a>
 **/
public class ContainerTimerRepresentative {
   private Integer mId;
   private Object mKey;
   private Date mStartDate;
   private long mInterval;
   private Serializable mInfo;
   
   public ContainerTimerRepresentative(
      Integer pId,
      Object pKey,
      Date pStartDate,
      long pInterval,
      Serializable pInfo
   ) {
      mId = pId;
      mKey = pKey;
      mStartDate = pStartDate;
      mInterval = pInterval;
      mInfo = pInfo;
   }
   
   public Integer getId() {
      return mId;
   }
   
   public Object getKey() {
      return mKey;
   }
   
   public Date getStartDate() {
      return mStartDate;
   }
   
   public long getInterval() {
      return mInterval;
   }
   
   public Serializable getInfo() {
      return mInfo;
   }
   
   public String toString() {
      return "ContainerTimerRepresentative [ Id: " + mId
         + ", key: " + mKey
         + ", start date: " + mStartDate
         + ", interval: " + ( mInterval < 0 ? "single timer" : mInterval + "" )
         + ", info: " + mInfo
         + " ]";
   }
}
