/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.util;

/**
 *   <description> 
 *      
 *   @see <related>
 *   @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>.
 *   @version $Revision: 1.3 $
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
