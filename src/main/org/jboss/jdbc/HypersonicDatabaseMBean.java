/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.jdbc;

/**
 *   <description> 
 *      
 *   @see <related>
 *   @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 *   @version $Revision: 1.4 $
 */
public interface HypersonicDatabaseMBean
   extends org.jboss.util.ServiceMBean
{
   // Constants -----------------------------------------------------
   public static final String OBJECT_NAME = ":service=Hypersonic";
    
   // Public --------------------------------------------------------
   public void setDatabase(String name);
   public String getDatabase();

   public void setPort(int port);
   public int getPort();

   public void setSilent(boolean silent);
   public boolean getSilent();
   
   public void setTrace(boolean trace);
   public boolean getTrace();
}
