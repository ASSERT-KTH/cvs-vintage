/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.jdbc;

/**
 *   <description> 
 *      
 *   @see <related>
 *   @author Rickard Öberg (rickard.oberg@telkel.com)
 *   @version $Revision: 1.2 $
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
