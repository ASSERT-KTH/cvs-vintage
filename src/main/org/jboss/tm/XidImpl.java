/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.tm;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.transaction.xa.Xid;


/**
 *  This object encapsulates the ID of a transaction.
 *
 *  @see TransactionImpl
 *  @author Rickard Öberg (rickard.oberg@telkel.com)
 *  @author <a href="mailto:osh@sparre.dk">Ole Husgaard</a>
 *  @version $Revision: 1.5 $
 */
class XidImpl
   implements Xid, java.io.Serializable
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   /**
    *  Hash code of this instance. This is really a sequence number.
    */
   int hash;

   /**
    *  Global transaction id of this instance.
    */
   byte[] globalId;

   /**
    *  Branch qualifier of this instance.
    *  This identifies the branch of a transaction.
    */
   byte[] branchId;

   // Static --------------------------------------------------------

   /**
    *  A cache for the <code>getHostName()</code> function.
    */
   private static String hostName;

   /**
    *  Return the host name of this host, followed by a slash.
    *
    *  This is used for building globally unique transaction identifiers.
    *  It would be safer to use the IP address, but a host name is better
    *  for humans to read and will do for now.
    */
   static private String getHostName()
   {
      if (hostName == null) {
         try {
            hostName = InetAddress.getLocalHost().getHostName() + "/";
         } catch (UnknownHostException e) {
            hostName = "localhost/";
         }
      }

      return hostName;
   }

   /**
    *  The next transaction id to use on this host.
    */
   static private int nextId = 0;

   /**
    *  Return a new unique transaction id to use on this host.
    */
   static synchronized int getNextId()
   {
      return nextId++;
   }

   /**
    *  Singleton for no branch qualifier.
    */
    static byte[] noBranchQualifier = new byte[MAXBQUALSIZE];
    static {
        for(int i=0; i<noBranchQualifier.length; i++)
            noBranchQualifier[i] = 0;
    }

   // Constructors --------------------------------------------------

   /**
    *  Create a new unique branch qualifier.
    */
   public XidImpl(int formatId, byte[] globalId, byte[] branchId)
   {
      this.hash = getNextId();
      this.globalId = globalId;
      this.branchId = branchId;
   }

   /**
    *  Create a new branch of an existing global transaction id.
    */
   public XidImpl(XidImpl xid, byte[] branchId)
   {
      hash = xid.hash;
      this.globalId = xid.globalId;
      this.branchId = branchId;
   }

   // Public --------------------------------------------------------

   // Xid implementation --------------------------------------------

   /**
    *  Return the global transaction id of this transaction.
    */
   public byte[] getGlobalTransactionId()
   {
      return (byte[])globalId.clone();
   }

   /**
    *  Return the branch qualifier of this transaction.
    */
   public byte[] getBranchQualifier()
   {
      if (branchId.length == 0)
         return branchId; // Zero length arrays are immutable.
      else
         return (byte[])branchId.clone();
   }

   /**
    *  Return the format identifier of this transaction.
    *
    *  The format identifier augments the global id and specifies
    *  how the global id and branch qualifier should be interpreted.
    */
   public int getFormatId() {
       return getJbossFormatId();
   }

   public static int getJbossFormatId()
   {
      // The id we return here should be different from all other transaction
      // implementations.
      // Known IDs are:
      // -1:     Sometimes used to denote a null transaction id.
      // 0:      OSI TP (javadoc states OSI CCR, but that is a bit misleading
      //         as OSI CCR doesn't even have ACID properties. But OSI CCR and
      //         OSI TP do have the same id format.)
      // 0xBB14: Used by JONAS
      // 0xBB20: Used by JONAS
      return 0x0101;
   }

   /**
    *  Compare for equality.
    *
    *  This checks the format id and the global transaction ID, but
    *  ignores the branch qualifier.
    */
   public boolean equals(Object obj)
   {
      // OSH: Should we also compare the branch ID ?

      if (obj != null && obj instanceof XidImpl) {
         XidImpl other = (XidImpl)obj;

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

   public String toString()
   {
      return "XidImpl:" + getGlobalTransactionId();
   }

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------

   static byte[] getGlobalIdString(int hash)
   {
      String value;
      String s = getHostName();
      String h = Integer.toString(hash);
      if(s.length() + h.length() > 64) {
          value = s.substring(0, 64-h.length())+h;
      } else value = s+h;
      byte b[] = new byte[MAXGTRIDSIZE];
      System.arraycopy(value.getBytes(), 0, b, 0, value.length());
      return b;
   }

   // Inner classes -------------------------------------------------
}

