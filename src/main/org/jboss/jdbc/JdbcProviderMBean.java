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
 *   @version $Revision: 1.1 $
 */
public interface JdbcProviderMBean
{
   // Constants -----------------------------------------------------
    
   // Public --------------------------------------------------------
   public void start()
      throws Exception;
   
   public void stop();
   // Protected -----------------------------------------------------
}

