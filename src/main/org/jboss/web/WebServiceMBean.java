/*
 * JBoss, the OpenSource J2EE webOS
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
 *   @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>.
 *   @author <a href="mailto:Scott_Stark@displayscape.com">Scott Stark</a>.
 *   @version $Revision: 1.6 $
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
   /** A flag indicating if the server should attempt to download classes from
    thread context class loader when a request arrives that does not have a
    class loader key prefix.
    */
   public boolean getDownloadServerClasses();
   public void setDownloadServerClasses(boolean flag);
}
