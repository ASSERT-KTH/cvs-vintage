/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.metadata;

// $Id: ServiceRefMetaData.java,v 1.3 2004/04/22 09:22:12 tdiesler Exp $

import org.jboss.deployment.DeploymentException;
import org.jboss.logging.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.util.HashMap;
import java.util.Iterator;
import java.net.URL;
import java.net.MalformedURLException;

/** The metdata data from service-ref in application-client.xml descriptor
 * 
 * @author Thomas.Diesler@jboss.org
 * @version $Revision: 1.3 $
 */
public class ServiceRefMetaData
{
   // provide logging
   private static final Logger log = Logger.getLogger(ServiceRefMetaData.class);

   /** The ClassLoader to load additional resources */
   private ClassLoader localCl;

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

   /** The URL of the actual WSDL to use */
   private URL wsdlOverride;

   // derived properties
   private Document wsdlDocument;
   private Definition wsdlDefinition;

   /**
    * Construct the service-ref meta
    * @param localCl A ClassLoader to load additional resources
    */
   public ServiceRefMetaData(ClassLoader localCl)
   {
      if (localCl == null)
         throw new IllegalArgumentException("ResourceClassLoader cannot be null");

      this.localCl = localCl;
   }

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

   public Class getServiceInterfaceClass() throws ClassNotFoundException
   {
      return localCl.loadClass(serviceInterface);
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

   public URL getWsdlOverride()
   {
      return wsdlOverride;
   }

   public Document getWsdlDocument()
   {
      if (wsdlDocument != null)
         return wsdlDocument;

      DocumentBuilder builder = null;
      try
      {
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         factory.setNamespaceAware(true);
         builder = factory.newDocumentBuilder();
         if (wsdlOverride != null)
            wsdlDocument = builder.parse(wsdlOverride.openStream());
         else
            wsdlDocument = builder.parse(localCl.getResourceAsStream(wsdlFile));
      }
      catch (Exception e)
      {
         log.error("Cannot obtain WSDL document", e);
         return null;
      }

      return wsdlDocument;
   }

   public Definition getWsdlDefinition()
   {
      if (wsdlDefinition != null)
         return wsdlDefinition;

      try
      {
         WSDLFactory wsdlFactory = WSDLFactory.newInstance();
         WSDLReader wsdlReader = wsdlFactory.newWSDLReader();
         wsdlDefinition = wsdlReader.readWSDL(null, getWsdlDocument());
      }
      catch (WSDLException e)
      {
         log.error("Cannot obtain WSDL definition", e);
         return null;
      }

      return wsdlDefinition;
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
      String wsdlOverrideOption = MetaData.getOptionalChildContent(element, "wsdl-override");
      try
      {
         if (wsdlOverrideOption != null)
            wsdlOverride = new URL(wsdlOverrideOption);
      }
      catch (MalformedURLException e)
      {
         throw new DeploymentException("Invalid WSDL override: " + wsdlOverrideOption);
      }
   }
}
