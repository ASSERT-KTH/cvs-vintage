package org.jboss.aspect.jmx;

import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.management.MBeanRegistration;
import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.dom4j.Branch;
import org.dom4j.Document;
import org.dom4j.DocumentFactory;
import org.dom4j.io.DOMReader;
import org.jboss.aspect.AspectFactory;
import org.jboss.aspect.spi.AspectDefinition;
import org.jboss.aspect.spi.AspectInterceptorHolder;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * @author <a href="mailto:hchirino@jboss.org">Hiram Chirino</a>
 */
public class JMXRegistered implements MBeanRegistration
{
   /** The MBeanServer which we have been register with. */
   protected MBeanServer server;
   /** The object name which we are registsred under. */
   protected ObjectName serviceName;

   /**
    * @see javax.management.MBeanRegistration#postDeregister()
    */
   public void postDeregister()
   {
   }

   /**
    * @see javax.management.MBeanRegistration#postRegister(java.lang.Boolean)
    */
   public void postRegister(Boolean arg0)
   {
   }

   /**
    * @see javax.management.MBeanRegistration#preDeregister()
    */
   public void preDeregister() throws Exception
   {
   }

   /**
    * @see javax.management.MBeanRegistration#preRegister(javax.management.MBeanServer, javax.management.ObjectName)
    */
   public ObjectName preRegister(MBeanServer arg0, ObjectName arg1) throws Exception
   {
   	server = arg0;
   	serviceName = arg1;
      return arg1;
   }

}
