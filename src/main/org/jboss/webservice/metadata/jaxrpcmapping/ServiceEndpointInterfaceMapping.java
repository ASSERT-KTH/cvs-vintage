/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.webservice.metadata.jaxrpcmapping;

// $Id: ServiceEndpointInterfaceMapping.java,v 1.1 2004/05/14 18:34:23 tdiesler Exp $

import javax.xml.namespace.QName;
import java.util.ArrayList;

/**
 * XML mapping of the java-wsdl-mapping/service-endpoint-interface-mapping element.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 14-May-2004
 */
public class ServiceEndpointInterfaceMapping
{
   // The parent <java-wsdl-mapping> element
   private JavaWsdlMapping javaWsdlMapping;

   // The required <service-endpoint-interface> element
   private String serviceEndpointInterface;
   // The required <wsdl-port-type> element
   private QName wsdlPortType;
   // The required <wsdl-binding> element
   private QName wsdlBinding;
   // Zero or more <service-endpoint-method-mapping> elements
   private ArrayList serviceEndpointMethodMappings = new ArrayList();

   public ServiceEndpointInterfaceMapping(JavaWsdlMapping javaWsdlMapping)
   {
      this.javaWsdlMapping = javaWsdlMapping;
   }

   public JavaWsdlMapping getJavaWsdlMapping()
   {
      return javaWsdlMapping;
   }

   public String getServiceEndpointInterface()
   {
      return serviceEndpointInterface;
   }

   public void setServiceEndpointInterface(String serviceEndpointInterface)
   {
      this.serviceEndpointInterface = serviceEndpointInterface;
   }

   public QName getWsdlPortType()
   {
      return wsdlPortType;
   }

   public void setWsdlPortType(QName wsdlPortType)
   {
      this.wsdlPortType = wsdlPortType;
   }

   public QName getWsdlBinding()
   {
      return wsdlBinding;
   }

   public void setWsdlBinding(QName wsdlBinding)
   {
      this.wsdlBinding = wsdlBinding;
   }

   public ServiceEndpointMethodMapping[] getServiceEndpointMethodMappings()
   {
      ServiceEndpointMethodMapping[] arr = new ServiceEndpointMethodMapping[serviceEndpointMethodMappings.size()];
      serviceEndpointMethodMappings.toArray(arr);
      return arr;
   }

   public void addServiceEndpointMethodMapping(ServiceEndpointMethodMapping serviceEndpointMethodMapping)
   {
      serviceEndpointMethodMappings.add(serviceEndpointMethodMapping);
   }
}
