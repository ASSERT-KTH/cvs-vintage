/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.metadata;

// $Id: ServiceRefMetaData.java,v 1.1 2004/04/20 16:57:33 tdiesler Exp $

import org.jboss.deployment.DeploymentException;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Iterator;

/** The metdata data from service-ref in application-client.xml descriptor
 * 
 * @author Thomas.Diesler@jboss.org
 * @version $Revision: 1.1 $
 */
public class ServiceRefMetaData
{
   /** The service-ref/service-ref-name element */
   private String serviceRefName;
   /** The service-ref/service-interface element */
   private String serviceInterface;
   /** The service-ref/wsdl-file element */
   private String wsdlFile;
   /** The service-ref/jaxrpc-mapping-file element */
   private String jaxrpcMappingFile;
   /** The service-ref/service-qname element */
   private QName serviceQName;
   /** The service-ref/port-component-ref/service-endpoint-interface element */
   private String serviceEndpointInterface;
   /** The service-ref/port-component-ref/port-component-link element */
   private String portComponentLink;
   /** The HashMap<HandlerMetaData> service-ref/handler element(s) */
   private HashMap handlers = new HashMap();

   /**
    * @return HashMap<HandlerMetaData>
    */
   public HashMap getHandlers()
   {
      return handlers;
   }

   public String getJaxrpcMappingFile()
   {
      return jaxrpcMappingFile;
   }

   public String getPortComponentLink()
   {
      return portComponentLink;
   }

   public String getServiceEndpointInterface()
   {
      return serviceEndpointInterface;
   }

   public String getServiceInterface()
   {
      return serviceInterface;
   }

   public QName getServiceQName()
   {
      return serviceQName;
   }

   public String getServiceRefName()
   {
      return serviceRefName;
   }

   public String getWsdlFile()
   {
      return wsdlFile;
   }

   public void importClientXml(Element element)
           throws DeploymentException
   {
      serviceRefName = MetaData.getUniqueChildContent(element, "service-ref-name");

      serviceInterface = MetaData.getUniqueChildContent(element, "service-interface");

      wsdlFile = MetaData.getOptionalChildContent(element, "wsdl-file");

      jaxrpcMappingFile = MetaData.getOptionalChildContent(element, "jaxrpc-mapping-file");

      serviceQName = QNameBuilder.buildQName(element, MetaData.getOptionalChildContent(element, "service-qname"));

      Element portComponentRef = MetaData.getOptionalChild(element, "port-component-ref");
      if (portComponentRef != null)
      {
         serviceEndpointInterface = MetaData.getUniqueChildContent(portComponentRef, "service-endpoint-interface");
         portComponentLink = MetaData.getOptionalChildContent(portComponentRef, "port-component-link");
      }

      // Parse the handler elements
      Iterator iterator = MetaData.getChildrenByTagName(element, "handler");
      while (iterator.hasNext())
      {
         Element handler = (Element) iterator.next();
         HandlerMetaData handlerMetaData = new HandlerMetaData();
         handlerMetaData.importClientXml(handler);
         handlers.put(handlerMetaData.getHandlerName(), handlerMetaData);
      }
   }

   public void importJbossClientXml(Element element) throws DeploymentException
   {
   }
}
