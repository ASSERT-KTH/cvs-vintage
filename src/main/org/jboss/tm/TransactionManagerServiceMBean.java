/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.tm;

/**
 *   <description> 
 *      
 *   @see <related>
 *   @author Rickard Öberg (rickard.oberg@telkel.com)
 *   @version $Revision: 1.4 $
 */
public interface TransactionManagerServiceMBean
   extends org.jboss.util.ServiceMBean
{
   // Constants -----------------------------------------------------
   public static final String OBJECT_NAME = ":service=TransactionManager";
    
   // Public --------------------------------------------------------
   public int getTransactionTimeout();

   public void setTransactionTimeout(int timeout);

   public String getXidClassName();

   public void setXidClassName(String name);
}

