/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.webservice.metadata.jaxrpcmapping;

// $Id: ServiceEndpointMethodMapping.java,v 1.1 2004/05/14 18:34:23 tdiesler Exp $

import java.util.ArrayList;

/**
 * XML mapping of the java-wsdl-mapping/service-endpoint-interface-mapping/service-endpoint-method-mapping element.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 14-May-2004
 */
public class ServiceEndpointMethodMapping
{
   // The parent <service-endpoint-interface-mapping> element
   private ServiceEndpointInterfaceMapping serviceEndpointInterfaceMapping;

   // The required <java-method-name> element
   private String javaMethodName;
   // The required <wsdl-operation> element
   private String wsdlOperation;
   // The optional <wrapped-element> element
   private boolean wrappedElement;
   // Zero or more <method-param-parts-mapping> elements
   private ArrayList methodParamPartsMappings = new ArrayList();
   // The optional <wsdl-return-value-mapping> element
   private WsdlReturnValueMapping wsdlReturnValueMapping;

   public ServiceEndpointMethodMapping(ServiceEndpointInterfaceMapping serviceEndpointInterfaceMapping)
   {
      this.serviceEndpointInterfaceMapping = serviceEndpointInterfaceMapping;
   }

   public ServiceEndpointInterfaceMapping getServiceEndpointInterfaceMapping()
   {
      return serviceEndpointInterfaceMapping;
   }

   public String getJavaMethodName()
   {
      return javaMethodName;
   }

   public void setJavaMethodName(String javaMethodName)
   {
      this.javaMethodName = javaMethodName;
   }

   public MethodParamPartsMapping[] getMethodParamPartsMappings()
   {
      MethodParamPartsMapping[] arr = new MethodParamPartsMapping[methodParamPartsMappings.size()];
      methodParamPartsMappings.toArray(arr);
      return arr;
   }

   public void addMethodParamPartsMapping(MethodParamPartsMapping methodParamPartsMapping)
   {
      methodParamPartsMappings.add(methodParamPartsMapping);
   }

   public boolean isWrappedElement()
   {
      return wrappedElement;
   }

   public void setWrappedElement(boolean wrappedElement)
   {
      this.wrappedElement = wrappedElement;
   }

   public String getWsdlOperation()
   {
      return wsdlOperation;
   }

   public void setWsdlOperation(String wsdlOperation)
   {
      this.wsdlOperation = wsdlOperation;
   }

   public WsdlReturnValueMapping getWsdlReturnValueMapping()
   {
      return wsdlReturnValueMapping;
   }

   public void setWsdlReturnValueMapping(WsdlReturnValueMapping wsdlReturnValueMapping)
   {
      this.wsdlReturnValueMapping = wsdlReturnValueMapping;
   }
}
