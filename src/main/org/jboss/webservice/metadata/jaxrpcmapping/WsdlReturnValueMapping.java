/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.webservice.metadata.jaxrpcmapping;

// $Id: WsdlReturnValueMapping.java,v 1.1 2004/05/14 18:34:23 tdiesler Exp $

import javax.xml.namespace.QName;

/**
 * XML mapping of the java-wsdl-mapping/service-endpoint-interface-mapping/service-endpoint-method-mapping/wsdl-return-value-mapping element.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 14-May-2004
 */
public class WsdlReturnValueMapping
{
   // The parent <service-endpoint-method-mapping> element
   private ServiceEndpointMethodMapping serviceEndpointMethodMapping;

   // The required <method-return-value> element
   private String methodReturnValue;
   // The required <wsdl-message> element
   private QName wsdlMessage;
   // The optional <wsdl-message> element
   private String wsdlMessagePartName;

   public WsdlReturnValueMapping(ServiceEndpointMethodMapping serviceEndpointMethodMapping)
   {
      this.serviceEndpointMethodMapping = serviceEndpointMethodMapping;
   }

   public ServiceEndpointMethodMapping getServiceEndpointMethodMapping()
   {
      return serviceEndpointMethodMapping;
   }

   public String getMethodReturnValue()
   {
      return methodReturnValue;
   }

   public void setMethodReturnValue(String methodReturnValue)
   {
      this.methodReturnValue = methodReturnValue;
   }

   public QName getWsdlMessage()
   {
      return wsdlMessage;
   }

   public void setWsdlMessage(QName wsdlMessage)
   {
      this.wsdlMessage = wsdlMessage;
   }

   public String getWsdlMessagePartName()
   {
      return wsdlMessagePartName;
   }

   public void setWsdlMessagePartName(String wsdlMessagePartName)
   {
      this.wsdlMessagePartName = wsdlMessagePartName;
   }
}
