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
 * @version <tt>$Revision: 1.5 $</tt>
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

   /**
    * This is where the local adapter will be bound into JNDI.
    *
    * <p>
    * Not using <em>localhost</em> as {@link #mHost} could potentially
    * return that, so <em>local</em> is a little safer... I guess.
    */
   public static final String LOCAL_NAME = "jmx:local:rmi";

   /** The RMI adapter instance. */
   private RMIAdaptor adaptor;

   /** Cached host name. */
   private String mHost;

   /** JNDI prefix or null for none. */
   private String mName;

   /** The user supplied JNDI name or null for the default. */
   private String jndiName;

   /** Flag to enable or disable binding to LOCAL_NAME. */
   private boolean bindLocal = true;

   /**
    * @jmx:managed-constructor
    */
   public RMIAdaptorService(String name)
   {
      mName = name;
   }

   /**
    * @jmx:managed-constructor
    */
   public RMIAdaptorService()
   {
      this(null);
   }

   /**
    * @jmx:managed-attribute
    */
   public void setJNDIName(final String jndiName)
   {
      this.jndiName = jndiName;
   }
   
   /**
    * @jmx:managed-attribute
    */
   public String getJNDIName()
   {
      if (jndiName == null) {
         if (mName != null) {
            return JMX_NAME + ":" + mHost + ":" + PROTOCOL_NAME + ":" + mName;
         }
         // else
         
         return JMX_NAME + ":" + mHost + ":" + PROTOCOL_NAME;
      }
      // else
      
      return jndiName;
   }

   /**
    * @jmx:managed-attribute
    */
   public void setBindLocal(final boolean flag)
   {
      this.bindLocal = flag;
   }
   
   /**
    * @jmx:managed-attribute
    */
   public boolean getBindLocal()
   {
      return this.bindLocal;
   }


   ///////////////////////////////////////////////////////////////////////////
   //                    ServiceMBeanSupport Overrides                      //
   ///////////////////////////////////////////////////////////////////////////
   
   protected ObjectName getObjectName(MBeanServer server, ObjectName name)
      throws MalformedObjectNameException
   {
      return name == null ? OBJECT_NAME : name;
   }

   protected void startService() throws Exception
   {
      mHost = InetAddress.getLocalHost().getHostName();
      adaptor = new RMIAdaptorImpl(server);

      InitialContext ctx = new InitialContext();

      try {
         ctx.bind(getJNDIName(), adaptor);

         if (bindLocal) {
            ctx.bind(LOCAL_NAME, adaptor);
         }
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
         ctx.unbind(LOCAL_NAME);
      }
      finally {
         ctx.close();
      }
   }
}
