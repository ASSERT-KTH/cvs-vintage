/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.tm;

import javax.transaction.xa.Xid;


/**
 *  This object encapsulates the global transaction ID of a transaction.
 *  It is similar to an Xid, but holds only the GlobalId part.
 *  This implementation is immutable and always serializable at runtime.
 *
 *  @see XidImpl
 *  @author <a href="mailto:osh@sparre.dk">Ole Husgaard</a>
 *  @version $Revision: 1.1 $
 */
class GlobalId
   implements java.io.Serializable
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   /**
    *  Hash code of this instance. This is really a sequence number.
    */
   private int hash;

   /**
    *  Global transaction id of this instance.
    *  The coding of this class depends on the fact that this variable is
    *  initialized in the constructor and never modified. References to
    *  this array are never given away, instead a clone is delivered.
    */
   private byte[] globalId;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   /**
    *  Create a new instance.
    */
   GlobalId(Xid xid)
   {
      if (xid.getFormatId() != XidImpl.JBOSS_FORMAT_ID)
        throw new IllegalArgumentException("Bad transaction format id");

      hash = xid.hashCode();
      if (xid instanceof XidImpl)
        globalId = ((XidImpl)xid).getInternalGlobalTransactionId();
      else
        globalId = xid.getGlobalTransactionId();
   }

   // Public --------------------------------------------------------

   // Xid implementation --------------------------------------------

   /**
    *  Compare for equality.
    *
    *  Instances are considered equal if they both refer to the same
    *  global transaction id.
    */
   public boolean equals(Object obj)
   {
      if (obj instanceof GlobalId) {
         GlobalId other = (GlobalId)obj;

         if (hash != other.hash)
            return false;

         if (globalId == other.globalId)
            return true;

         if (globalId.length != other.globalId.length)
            return false;

         for (int i = 0; i < globalId.length; ++i)
            if (globalId[i] != other.globalId[i])
               return false;

         return true;
      }
      return false;
   }

   public int hashCode()
   {
      return hash;
   }

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}

