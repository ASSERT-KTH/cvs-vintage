/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.txtimer;

// $Id: TimedObjectId.java,v 1.1 2004/04/09 22:47:01 tdiesler Exp $

import java.io.Serializable;

/**
 * The combined TimedObjectId
 * 
 * @author Thomas.Diesler@jboss.org
 * @since 09-Apr-2004
 */
public class TimedObjectId implements Serializable
{
   private String timedObjectId;
   private Object instancePk;
   private int hashCode;

   /**
    * Construct a combined TimedObjectId
    * @param timedObjectId The TimedObject identifier
    * @param instancePk The TimedObject instance identifier, can be null
    */
   public TimedObjectId(String timedObjectId, Object instancePk)
   {
      if (timedObjectId == null)
         throw new IllegalArgumentException("timedObjectId cannot be null");

      this.timedObjectId = timedObjectId;
      this.instancePk = instancePk;
   }

   /**
    * Construct a TimedObjectId
    * @param timedObjectId The TimedObject identifier
    */
   public TimedObjectId(String timedObjectId)
   {
      this(timedObjectId, null);
   }

   public String getTimedObjectId()
   {
      return timedObjectId;
   }

   public Object getInstancePk()
   {
      return instancePk;
   }

   public int hashCode()
   {
      if (hashCode == 0)
         hashCode = toString().hashCode();
      return hashCode;
   }

   public boolean equals(Object obj)
   {
      if (obj == this) return true;
      if (obj instanceof TimedObjectId)
      {
         TimedObjectId other = (TimedObjectId)obj;
         if (timedObjectId.equals(other.timedObjectId))
            return (instancePk != null ? instancePk.equals(other.instancePk) : other.instancePk == null);
      }
      return false;
   }

   public String toString()
   {
      return "[id=" + timedObjectId + ",pk=" + instancePk + "]";
   }
}
