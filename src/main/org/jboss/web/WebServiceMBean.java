/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.web;

import java.net.UnknownHostException;
import java.net.URL;

/** An mbean for configuring the classloader web service.
 *      
 *   @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>.
 *   @author <a href="mailto:Scott.Stark@jboss.org">Scott Stark</a>.
 *   @version $Revision: 1.8 $
 */
public interface WebServiceMBean
   extends org.jboss.system.ServiceMBean
{
   // Constants -----------------------------------------------------
   public static final String OBJECT_NAME = ":service=Webserver";
    
   // Public --------------------------------------------------------
   public URL addClassLoader(ClassLoader cl);
   
   public void removeClassLoader(ClassLoader cl);

   /** Set the WebService listening port.
    @param part, the listening port, 0 == Anonymous.
    */
	public void setPort(int port);
   /** Get the WebService listening port.
    @return the WebService listening port, 0 == Anonymous.
    */
   public int getPort();

   /** Get the name of the public interface to use for the host portion of the
    RMI codebase URL.
    */
   public String getHost();
   /** Set the name of the public interface to use for the host portion of the
    RMI codebase URL.
    */
   public void setHost(String host);

   /** Get the specific address the WebService listens on.t
    @return the interface name or IP address the WebService binds to.
    */
   public String getBindAddress();
   /** Set the specific address the WebService listens on.  This can be used on
    a multi-homed host for a ServerSocket that will only accept connect requests
    to one of its addresses.
    @param host, the interface name or IP address to bind. If host is null,
    connections on any/all local addresses will be allowed.
    */
   public void setBindAddress(String host) throws UnknownHostException;

   /** Get the WebService listen queue backlog limit. The maximum queue length
    for incoming connection indications (a request to connect) is set to the
    backlog parameter. If a connection indication arrives when the queue is
    full, the connection is refused. 
    @return the queue backlog limit. 
    */
   public int getBacklog();
   /** Set the WebService listen queue backlog limit. The maximum queue length
    for incoming connection indications (a request to connect) is set to the
    backlog parameter. If a connection indication arrives when the queue is
    full, the connection is refused. 
    @param backlog, the queue backlog limit. 
    */
   public void setBacklog(int backlog);

   /** A flag indicating if the server should attempt to download classes from
    thread context class loader when a request arrives that does not have a
    class loader key prefix.
    */
   public boolean getDownloadServerClasses();
   public void setDownloadServerClasses(boolean flag);
}
