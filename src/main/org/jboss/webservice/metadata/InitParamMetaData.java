/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.webservice.metadata;

// $Id: InitParamMetaData.java,v 1.1 2004/08/19 18:53:04 tdiesler Exp $

import java.io.Serializable;

/**
 * XML Binding and ws4ee meta-data element for
 * <code>webservices/webservice-description/port-component/handler/init-param</code>
 *
 * @author Thomas.Diesler@jboss.org
 * @since 06-May-2004
 */
public class InitParamMetaData implements Serializable
{
   // The required <handler-name> element
   private String paramName;
   // The required <handler-class> element
   private String paramValue;

   public String getParamName()
   {
      return paramName;
   }

   public void setParamName(String paramName)
   {
      this.paramName = paramName;
   }

   public String getParamValue()
   {
      return paramValue;
   }

   public void setParamValue(String paramValue)
   {
      this.paramValue = paramValue;
   }
}
