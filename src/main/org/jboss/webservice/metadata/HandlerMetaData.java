/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.webservice.metadata;

// $Id: HandlerMetaData.java,v 1.1 2004/08/19 18:53:04 tdiesler Exp $

import org.jboss.deployment.DeploymentException;
import org.jboss.webservice.metadata.InitParamMetaData;
import org.jboss.metadata.MetaData;
import org.jboss.xml.QNameBuilder;
import org.jboss.xml.QNameBuilder;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.Serializable;

/** The unified metdata data for a handler element
 * 
 * @author Thomas.Diesler@jboss.org
 * @version $Revision: 1.1 $
 */
public class HandlerMetaData implements Serializable
{
   // The required <handler-name> element
   private String handlerName;
   // The required <handler-class> element
   private String handlerClass;
   // The optional <init-param> elements
   private ArrayList initParams = new ArrayList();
   // The optional <soap-header> elements
   private ArrayList soapHeaders = new ArrayList();
   // The optional <soap-role> elements
   private ArrayList soapRoles = new ArrayList();
   // The optional <port-name> elements, these only apply to webserve clients
   private ArrayList portNames = new ArrayList();

   public void setHandlerName(String value)
   {
      this.handlerName = value;
   }

   public String getHandlerName()
   {
      return handlerName;
   }

   public void setHandlerClass(String handlerClass)
   {
      this.handlerClass = handlerClass;
   }

   public String getHandlerClass()
   {
      return handlerClass;
   }

   public void addInitParam(InitParamMetaData param)
   {
      initParams.add(param);
   }

   public InitParamMetaData[] getInitParams()
   {
      InitParamMetaData[] array = new InitParamMetaData[initParams.size()];
      initParams.toArray(array);
      return array;
   }

   public void addSoapHeader(QName qName)
   {
      soapHeaders.add(qName);
   }

   public QName[] getSoapHeaders()
   {
      QName[] array = new QName[soapHeaders.size()];
      soapHeaders.toArray(array);
      return array;
   }

   public void addSoapRole(String value)
   {
      soapRoles.add(value);
   }

   public String[] getSoapRoles()
   {
      String[] array = new String[soapRoles.size()];
      soapRoles.toArray(array);
      return array;
   }

   public String[] getPortNames()
   {
      String[] array = new String[portNames.size()];
      portNames.toArray(array);
      return array;
   }

   public void importStandardXml(Element element)
           throws DeploymentException
   {
      handlerName = MetaData.getUniqueChildContent(element, "handler-name");
      
      handlerClass = MetaData.getUniqueChildContent(element, "handler-class");
      
      // Parse the init-param elements
      Iterator iterator = MetaData.getChildrenByTagName(element, "init-param");
      while (iterator.hasNext())
      {
         Element paramElement = (Element) iterator.next();
         InitParamMetaData param = new InitParamMetaData();
         param.setParamName(MetaData.getUniqueChildContent(paramElement, "param-name"));
         param.setParamValue(MetaData.getUniqueChildContent(paramElement, "param-value"));
         initParams.add(param);
      }

      // Parse the soap-header elements
      iterator = MetaData.getChildrenByTagName(element, "soap-header");
      while (iterator.hasNext())
      {
         Element headerElement = (Element) iterator.next();
         String content = MetaData.getElementContent(headerElement);
         QName qname = QNameBuilder.buildQName(headerElement, content);
         soapHeaders.add(qname);
      }

      // Parse the soap-role elements
      iterator = MetaData.getChildrenByTagName(element, "soap-role");
      while (iterator.hasNext())
      {
         Element roleElement = (Element) iterator.next();
         String content = MetaData.getElementContent(roleElement);
         soapRoles.add(content);
      }

      // Parse the port-name elements
      iterator = MetaData.getChildrenByTagName(element, "port-name");
      while (iterator.hasNext())
      {
         Element portElement = (Element) iterator.next();
         String content = MetaData.getElementContent(portElement);
         portNames.add(content);
      }
   }

   public void importJbossClientXml(Element element) throws DeploymentException
   {
   }
}
