/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.webservice.metadata.jaxrpcmapping;

// $Id: ExceptionMapping.java,v 1.1 2004/05/14 18:34:23 tdiesler Exp $

import javax.xml.namespace.QName;
import java.util.ArrayList;

/**
 * XML mapping of the java-wsdl-mapping/exception-mapping element.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 14-May-2004
 */
public class ExceptionMapping
{
   // The parent <java-wsdl-mapping> element
   private JavaWsdlMapping javaWsdlMapping;

   // The required <exception-type> element
   private String exceptionType;
   // The required <wsdl-message> element
   private QName wsdlMessage;
   // The optional <constructor-parameter-order> element
   private ArrayList constructorParameterOrder = new ArrayList();

   public ExceptionMapping(JavaWsdlMapping javaWsdlMapping)
   {
      this.javaWsdlMapping = javaWsdlMapping;
   }

   public JavaWsdlMapping getJavaWsdlMapping()
   {
      return javaWsdlMapping;
   }

   public String getExceptionType()
   {
      return exceptionType;
   }

   public void setExceptionType(String exceptionType)
   {
      this.exceptionType = exceptionType;
   }

   public QName getWsdlMessage()
   {
      return wsdlMessage;
   }

   public void setWsdlMessage(QName wsdlMessage)
   {
      this.wsdlMessage = wsdlMessage;
   }

   public String[] getConstructorParameterOrder()
   {
      String[] arr = new String[constructorParameterOrder.size()];
      constructorParameterOrder.toArray(arr);
      return arr;
   }

   public void addConstructorParameter(String elementName)
   {
      constructorParameterOrder.add(elementName);
   }
}
