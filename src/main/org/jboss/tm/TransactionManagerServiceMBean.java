/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.tm;

/**
 *   <description> 
 *      
 *   @see <related>
 *   @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 *   @author <a href="mailto:toby.allsopp@peace.com">Toby Allsopp</a>
 *   @version $Revision: 1.7 $
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

