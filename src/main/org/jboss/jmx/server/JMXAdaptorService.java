/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.jmx.server;

import java.io.File;
import java.net.URL;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.util.ArrayList;
import java.util.Iterator;

import javax.management.Attribute;
import javax.management.ObjectName;
import javax.management.QueryExp;
import javax.management.ObjectInstance;
import javax.management.AttributeNotFoundException;
import javax.management.InstanceNotFoundException;
import javax.management.InvalidAttributeValueException;
import javax.management.MBeanException;
import javax.management.ReflectionException;
import javax.management.MBeanServer;
import javax.naming.InitialContext;

import org.jboss.system.ServiceMBeanSupport;

/**
 *   <description>
 *
 *   @see <related>
 *   @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 *   @version $Revision: 1.7 $
 */
public class JMXAdaptorService
   extends ServiceMBeanSupport
   implements JMXAdaptorServiceMBean
{
   // Constants -----------------------------------------------------
    public static String JNDI_NAME = "jmx";

   // Attributes ----------------------------------------------------
    MBeanServer server;
    JMXAdaptorImpl adaptor;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   // Public --------------------------------------------------------
   public ObjectName getObjectName(MBeanServer server, ObjectName name)
      throws javax.management.MalformedObjectNameException
   {
        this.server = server;
      return new ObjectName(OBJECT_NAME);
   }

   public String getName()
   {
      return "JMX RMI Adaptor";
    }

   // Protected -----------------------------------------------------
   protected void initService()
      throws Exception
   {
    adaptor = new JMXAdaptorImpl(server);
   }
    
   protected void startService()
      throws Exception
   {
        new InitialContext().bind(JNDI_NAME, adaptor);
   }

   protected void stopService()
   {
        try
        {
            new InitialContext().unbind(JNDI_NAME);
        } catch (Exception e)
        {
            log.error("unbind failed", e);
        }
   }
}

