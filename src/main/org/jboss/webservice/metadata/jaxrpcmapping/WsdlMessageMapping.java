/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.webservice.metadata.jaxrpcmapping;

import javax.xml.namespace.QName;

// $Id: WsdlMessageMapping.java,v 1.1 2004/05/14 18:34:23 tdiesler Exp $

/**
 * Created by IntelliJ IDEA.
 * 
 * @author Thomas.Diesler@jboss.org
 * @since 14-May-2004
 */
public class WsdlMessageMapping
{
   // The parent <method-param-parts-mapping> element
   private MethodParamPartsMapping methodParamPartsMapping;
   // The required <wsdl-message> element
   private QName wsdlMessage;
   // The required <wsdl-message-part-name> element
   private String wsdlMessagePartName;
   // The required <parameter-mode> element
   private String parameterMode;
   // The optional <soap-header> element
   private boolean soapHeader;

   public WsdlMessageMapping(MethodParamPartsMapping methodParamPartsMapping)
   {
      this.methodParamPartsMapping = methodParamPartsMapping;
   }

   public MethodParamPartsMapping getMethodParamPartsMapping()
   {
      return methodParamPartsMapping;
   }

   public String getParameterMode()
   {
      return parameterMode;
   }

   public void setParameterMode(String parameterMode)
   {
      if ("IN".equals(parameterMode) == false && "OUT".equals(parameterMode) == false && "INOUT".equals(parameterMode) == false)
         throw new IllegalArgumentException("Invalid parameter mode: " + parameterMode);
      this.parameterMode = parameterMode;
   }

   public boolean isSoapHeader()
   {
      return soapHeader;
   }

   public void setSoapHeader(boolean soapHeader)
   {
      this.soapHeader = soapHeader;
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
