/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb;

import org.jboss.util.Service;

/**
 *   <description> 
 *      
 *   @see <related>
 *   @author Rickard Öberg (rickard.oberg@telkel.com)
 *   @version $Revision: 1.1 $
 */
public interface ContainerPlugin
   extends Service
{
   // Constants -----------------------------------------------------
    
   // Static --------------------------------------------------------

   // Public --------------------------------------------------------

   /**
    *
    *
    * @return     
    */
   public void setContainer(Container con);
   
   public void init()
      throws Exception;
      
   public void start()
      throws Exception;
   
   public void stop();
   
   public void destroy();
}
