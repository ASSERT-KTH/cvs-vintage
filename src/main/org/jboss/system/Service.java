/*
* JBoss, the OpenSource J2EE server
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.system;

/**
*   The Service interface for the JBOSS-SYSTEM
*      
*   @see <related>
*   @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>.
*   @version $Revision: 1.1 $
*
*   <p><b>20010830 marc fleury:</b>
*   <ul>
*      initial import
*   <li> 
*   </ul>
*/

public interface Service
{
   public void init()
   throws Exception;
   
   public void start()
   throws Exception;
   
   public void stop();
   
   public void destroy();
}
