/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.txtimer;

// $Id: TimedObjectId.java,v 1.2 2004/04/13 10:10:40 tdiesler Exp $

import java.io.Serializable;

/**
 * The combined TimedObjectId
 * 
 * @author Thomas.Diesler@jboss.org
 * @since 09-Apr-2004
 */
public class TimedObjectId implements Serializable
{
   private String containerId;
   private Object instancePk;
   private int hashCode;

   /**
    * Construct a combined TimedObjectId
    * @param containerId The TimedObject identifier
    * @param instancePk The TimedObject instance identifier, can be null
    */
   public TimedObjectId(String containerId, Object instancePk)
   {
      if (containerId == null)
         throw new IllegalArgumentException("containerId cannot be null");

      this.containerId = containerId;
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

   public String getContainerId()
   {
      return containerId;
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
         if (containerId.equals(other.containerId))
            return (instancePk != null ? instancePk.equals(other.instancePk) : other.instancePk == null);
      }
      return false;
   }

   public String toString()
   {
      return "[id=" + containerId + ",pk=" + instancePk + "]";
   }
}
