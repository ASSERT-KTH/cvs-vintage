/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.web;

import java.net.URL;

/**
 *   <description> 
 *      
 *   @see <related>
 *   @author Rickard Öberg (rickard.oberg@telkel.com)
 *   @author Scott_Stark@displayscape.com
 *   @version $Revision: 1.3 $
 */
public interface WebServiceMBean
   extends org.jboss.util.ServiceMBean
{
   // Constants -----------------------------------------------------
   public static final String OBJECT_NAME = ":service=Webserver";
    
   // Public --------------------------------------------------------
   public URL addClassLoader(ClassLoader cl);
   
   public void removeClassLoader(ClassLoader cl);

	public void setPort(int port);
	
   public int getPort();
}
