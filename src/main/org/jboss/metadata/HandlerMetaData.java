/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.metadata;

// $Id: HandlerMetaData.java,v 1.1 2004/04/20 16:57:32 tdiesler Exp $

import org.jboss.deployment.DeploymentException;
import org.jboss.logging.Logger;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;

/** The metdata data from a service-ref/handler in the application-client.xml descriptor
 * 
 * @author Thomas.Diesler@jboss.org
 * @version $Revision: 1.1 $
 */
public class HandlerMetaData
{
   private static Logger log = Logger.getLogger(HandlerMetaData.class);

   private String handlerName;
   private String handlerClass;
   private QName soapHeader;
   private String soapRole;
   private String portName;

   public String getHandlerClass()
   {
      return handlerClass;
   }

   public String getHandlerName()
   {
      return handlerName;
   }

   public String getPortName()
   {
      return portName;
   }

   public QName getSoapHeader()
   {
      return soapHeader;
   }

   public String getSoapRole()
   {
      return soapRole;
   }

   public void importClientXml(Element element)
           throws DeploymentException
   {
      handlerName = MetaData.getUniqueChildContent(element, "handler-name");
      
      handlerClass = MetaData.getUniqueChildContent(element, "handler-class");
      
      soapHeader = QNameBuilder.buildQName(element, MetaData.getOptionalChildContent(element, "soap-header"));

      soapRole = MetaData.getOptionalChildContent(element, "soap-role");

      portName = MetaData.getOptionalChildContent(element, "port-name");
   }

   public void importJbossClientXml(Element element) throws DeploymentException
   {
   }
}
