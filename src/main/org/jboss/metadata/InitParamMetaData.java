/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.metadata;

// $Id: InitParamMetaData.java,v 1.1 2004/05/06 16:14:11 tdiesler Exp $

import org.jboss.deployment.DeploymentException;
import org.jboss.metadata.MetaData;
import org.jboss.metadata.QNameBuilder;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.util.ArrayList;

/**
 * XML Binding and ws4ee meta-data element for
 * <code>webservices/webservice-description/port-component/handler/init-param</code>
 *
 * @author Thomas.Diesler@jboss.org
 * @since 06-May-2004
 */
public class InitParamMetaData
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
