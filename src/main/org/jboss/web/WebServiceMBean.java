/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.web;

/**
 *   <description> 
 *      
 *   @see <related>
 *   @author Rickard Öberg (rickard.oberg@telkel.com)
 *   @version $Revision: 1.1 $
 */
public interface WebServiceMBean
   extends org.jboss.util.ServiceMBean
{
   // Constants -----------------------------------------------------
   public static final String OBJECT_NAME = ":service=Webserver";
    
   // Public --------------------------------------------------------
   public void addClassLoader(ClassLoader cl);
   
   public void removeClassLoader(ClassLoader cl);

	public void setPort(int port);
	
   public int getPort();
}
