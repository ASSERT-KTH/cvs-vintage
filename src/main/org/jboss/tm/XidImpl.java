/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.tm;

import javax.transaction.xa.Xid;

/**
 *	<description> 
 *      
 *	@see <related>
 *	@author Rickard Öberg (rickard.oberg@telkel.com)
 *	@version $Revision: 1.2 $
 */
public class XidImpl
   implements Xid, java.io.Serializable
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   byte[] globalId;
   byte[] branchId;
    
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   public XidImpl(byte[] globalId, byte[] branchId)
   {
      this.globalId = globalId;
      this.branchId = branchId;
   }
   
   // Public --------------------------------------------------------
   
   // Xid implementation --------------------------------------------
   public byte[] getGlobalTransactionId()
   {
      return globalId;
   }
   
   public int getFormatId()
   {
      return 1; // TODO: what should be here?
   }
   
   public byte[] getBranchQualifier()
   {
      return branchId;
   }
   
   // Package protected ---------------------------------------------
    
   // Protected -----------------------------------------------------
    
   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}

