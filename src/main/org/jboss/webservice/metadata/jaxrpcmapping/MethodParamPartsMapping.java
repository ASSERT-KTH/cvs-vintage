/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.webservice.metadata.jaxrpcmapping;

// $Id: MethodParamPartsMapping.java,v 1.1 2004/05/14 18:34:23 tdiesler Exp $

/**
 * XML mapping of the java-wsdl-mapping/service-endpoint-interface-mapping/service-endpoint-method-mapping/method-param-parts-mapping element.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 14-May-2004
 */
public class MethodParamPartsMapping
{
   // The parent <service-endpoint-method-mapping> element
   private ServiceEndpointMethodMapping serviceEndpointMethodMapping;

   // The required <param-position> element
   private int paramPosition;
   // The required <param-type> element
   private String paramType;
   // The required <wsdl-message-mapping> element
   private WsdlMessageMapping wsdlMessageMapping;

   public MethodParamPartsMapping(ServiceEndpointMethodMapping serviceEndpointMethodMapping)
   {
      this.serviceEndpointMethodMapping = serviceEndpointMethodMapping;
   }

   public ServiceEndpointMethodMapping getServiceEndpointMethodMapping()
   {
      return serviceEndpointMethodMapping;
   }

   public int getParamPosition()
   {
      return paramPosition;
   }

   public void setParamPosition(int paramPosition)
   {
      this.paramPosition = paramPosition;
   }

   public String getParamType()
   {
      return paramType;
   }

   public void setParamType(String paramType)
   {
      this.paramType = paramType;
   }

   public WsdlMessageMapping getWsdlMessageMapping()
   {
      return wsdlMessageMapping;
   }

   public void setWsdlMessageMapping(WsdlMessageMapping wsdlMessageMapping)
   {
      this.wsdlMessageMapping = wsdlMessageMapping;
   }
}
