/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.webservice.metadata.jaxrpcmapping;

// $Id: ServiceInterfaceMapping.java,v 1.1 2004/05/14 18:34:23 tdiesler Exp $

import javax.xml.namespace.QName;
import java.util.ArrayList;

/**
 * XML mapping of the java-wsdl-mapping/service-interface-mapping element.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 14-May-2004
 */
public class ServiceInterfaceMapping
{
   // The parent <java-wsdl-mapping> element
   private JavaWsdlMapping javaWsdlMapping;

   // The required <service-interface> element
   private String serviceInterface;
   // The required <wsdl-service-name> element
   private QName wsdlServiceName;
   // Zero or more <port-mapping> elements
   private ArrayList portMappings = new ArrayList();

   public ServiceInterfaceMapping(JavaWsdlMapping javaWsdlMapping)
   {
      this.javaWsdlMapping = javaWsdlMapping;
   }

   public JavaWsdlMapping getJavaWsdlMapping()
   {
      return javaWsdlMapping;
   }

   public String getServiceInterface()
   {
      return serviceInterface;
   }

   public void setServiceInterface(String serviceInterface)
   {
      this.serviceInterface = serviceInterface;
   }

   public QName getWsdlServiceName()
   {
      return wsdlServiceName;
   }

   public void setWsdlServiceName(QName wsdlServiceName)
   {
      this.wsdlServiceName = wsdlServiceName;
   }

   public PortMapping[] getPortMappings()
   {
      PortMapping[] arr = new PortMapping[portMappings.size()];
      portMappings.toArray(arr);
      return arr;
   }

   public void addPortMapping(PortMapping portMapping)
   {
      portMappings.add(portMapping);
   }
}
