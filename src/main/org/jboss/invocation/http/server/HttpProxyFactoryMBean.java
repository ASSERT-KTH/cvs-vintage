/*
* JBoss, the OpenSource J2EE WebOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.invocation.http.server;

import javax.management.ObjectName;

import org.jboss.system.Service;

/** An mbean interface for a proxy factory that can expose any interface
 * with RMI compatible semantics for access to remote clients using HTTP
 * as the transport.
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 1.2 $
 */
public interface HttpProxyFactoryMBean extends Service
{
   /** Get the server side mbean that exposes the invoke operation for the
    exported interface */
   public ObjectName getInvokerName();
   /** Set the server side mbean that exposes the invoke operation for the
    exported interface */
   public void setInvokerName(ObjectName jmxInvokerName);

   /** Get the JNDI name under which the HttpInvokerProxy will be bound */
   public String getJndiName();
   /** Set the JNDI name under which the HttpInvokerProxy will be bound */
   public void setJndiName(String jndiName);

   /** Get the http URL to the InvokerServlet */
   public String getInvokerURL();
   /** Set the http URL to the InvokerServlet */
   public void setInvokerURL(String invokerURL);

   /** Get the RMI compatible interface that the HttpInvokerProxy implements */
   public Class getExportedInterface();
   /** Set the RMI compatible interface that the HttpInvokerProxy implements */
   public void setExportedInterface(Class exportedInterface);
}
