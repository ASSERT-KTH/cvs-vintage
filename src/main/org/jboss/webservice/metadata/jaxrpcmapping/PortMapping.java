/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.webservice.metadata.jaxrpcmapping;

// $Id: PortMapping.java,v 1.1 2004/05/14 18:34:23 tdiesler Exp $

/**
 * XML mapping of the java-wsdl-mapping/service-interface-mapping/port-mapping element.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 14-May-2004
 */
public class PortMapping
{
   // The parent <service-interface-mapping> element
   private ServiceInterfaceMapping serviceInterfaceMapping;

   // The required <port-name> element
   private String portName;
   // The required <java-port-name> element
   private String javaPortName;

   public PortMapping(ServiceInterfaceMapping serviceInterfaceMapping)
   {
      this.serviceInterfaceMapping = serviceInterfaceMapping;
   }

   public ServiceInterfaceMapping getServiceInterfaceMapping()
   {
      return serviceInterfaceMapping;
   }

   public String getJavaPortName()
   {
      return javaPortName;
   }

   public void setJavaPortName(String javaPortName)
   {
      this.javaPortName = javaPortName;
   }

   public String getPortName()
   {
      return portName;
   }

   public void setPortName(String portName)
   {
      this.portName = portName;
   }
}

