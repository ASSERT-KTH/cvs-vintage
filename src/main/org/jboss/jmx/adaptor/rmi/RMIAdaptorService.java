/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.jmx.adaptor.rmi;

import java.net.InetAddress;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.MalformedObjectNameException;

import javax.naming.InitialContext;

import org.jboss.system.ServiceMBeanSupport;

/**
 * A JMX RMI Adapter service.
 *
 * @jmx:mbean name="jboss.jmx:type=adaptor,protocol=RMI"
 *            extends="org.jboss.system.ServiceMBean"
 *
 * @version <tt>$Revision: 1.4 $</tt>
 * @author  <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author  <A href="mailto:andreas.schaefer@madplanet.com">Andreas &quot;Mad&quot; Schaefer</A>
 * @author  <a href="mailto:jason@planet57.com">Jason Dillon</a>
 **/
public class RMIAdaptorService
   extends ServiceMBeanSupport
   implements RMIAdaptorServiceMBean
{
   //AS I am not quite sure if this works but somehow the protocol should become
   //AS part of the JNDI name because there could be more than one protcol
   
   public static final String JNDI_NAME = "jmx:rmi";
   public static final String JMX_NAME = "jmx";
   public static final String PROTOCOL_NAME = "rmi";

   private RMIAdaptor adaptor;
   private String mHost;
   private String mName;

   public RMIAdaptorService() {
      mName = null;
   }

   public RMIAdaptorService(String name)
   {
      mName = name;
   }

   protected ObjectName getObjectName(MBeanServer server, ObjectName name)
      throws MalformedObjectNameException
   {
      return name == null ? OBJECT_NAME : name;
   }

   /**
    * @jmx:managed-attribute
    */
   public String getJNDIName() {
      if (mName != null) {
         return JMX_NAME + ":" + mHost + ":" + PROTOCOL_NAME + ":" + mName;
      }
      else {
         return JMX_NAME + ":" + mHost + ":" + PROTOCOL_NAME;
      }
   }

   protected void startService() throws Exception
   {
      mHost = InetAddress.getLocalHost().getHostName();
      adaptor = new RMIAdaptorImpl(server);

      InitialContext ctx = new InitialContext();

      try {
         ctx.bind(getJNDIName(), adaptor);
      }
      finally {
         ctx.close();
      }
   }

   protected void stopService() throws Exception
   {
      InitialContext ctx = new InitialContext();

      try {
         ctx.unbind(getJNDIName());
      }
      finally {
         ctx.close();
      }
   }
}
