/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.metadata;

// $Id: ServiceRefMetaData.java,v 1.7 2004/05/06 16:14:11 tdiesler Exp $

import org.jboss.deployment.DeploymentException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;

/** The metdata data from service-ref element in web.xml, ejb-jar.xml, and application-client.xml.
 *
 * @author Thomas.Diesler@jboss.org
 * @version $Revision: 1.7 $
 */
public class ServiceRefMetaData
{
   /** The ClassLoader to load additional resources */
   private ClassLoader localCl;

   // The required <service-ref-name> element
   private String serviceRefName;
   // The required <service-interface> element
   private String serviceInterface;
   // The optional <wsdl-file> element
   private String wsdlFile;
   // The optional <jaxrpc-mapping-file> element
   private String jaxrpcMappingFile;
   // The optional <service-qname> element
   private QName serviceQName;
   // The optional <port-component-ref> elements
   private ArrayList portComponentRefs = new ArrayList();
   // The optional <handler> elements
   private ArrayList handlers = new ArrayList();

   // The URL of the actual WSDL to use
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

   public String getJaxrpcMappingFile()
   {
      return jaxrpcMappingFile;
   }

   public PortComponentRefMetaData[] getPortComponentRefs()
   {
      PortComponentRefMetaData[] array = new PortComponentRefMetaData[portComponentRefs.size()];
      portComponentRefs.toArray(array);
      return array;
   }

   public ServiceRefHandlerMetaData[] getHandlers()
   {
      ServiceRefHandlerMetaData[] array = new ServiceRefHandlerMetaData[handlers.size()];
      handlers.toArray(array);
      return array;
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

   public Document getWsdlDocument() throws DeploymentException
   {
      if (wsdlDocument != null)
         return wsdlDocument;

      try
      {
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         factory.setNamespaceAware(true);
         DocumentBuilder builder = factory.newDocumentBuilder();

         InputStream wsdlInputStream = null;
         if (wsdlOverride != null)
         {
            wsdlInputStream = wsdlOverride.openStream();
            if (wsdlInputStream == null)
               throw new DeploymentException("Cannot open WSDL at: " + wsdlOverride);
         }
         else
         {
            wsdlInputStream = localCl.getResourceAsStream(wsdlFile);
            if (wsdlInputStream == null)
               throw new DeploymentException("Cannot open WSDL at: " + wsdlFile);
         }
         wsdlDocument = builder.parse(wsdlInputStream);
      }
      catch (DeploymentException e)
      {
         throw e;
      }
      catch (Exception e)
      {
         throw new DeploymentException(e);
      }

      return wsdlDocument;
   }

   public Definition getWsdlDefinition() throws DeploymentException
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
         throw new DeploymentException(e);
      }

      return wsdlDefinition;
   }

   public void importStandardXml(Element element)
           throws DeploymentException
   {
      serviceRefName = MetaData.getUniqueChildContent(element, "service-ref-name");

      serviceInterface = MetaData.getUniqueChildContent(element, "service-interface");

      wsdlFile = MetaData.getOptionalChildContent(element, "wsdl-file");

      jaxrpcMappingFile = MetaData.getOptionalChildContent(element, "jaxrpc-mapping-file");

      serviceQName = QNameBuilder.buildQName(element, MetaData.getOptionalChildContent(element, "service-qname"));

      // Parse the port-component-ref elements
      Iterator iterator = MetaData.getChildrenByTagName(element, "port-component-ref");
      while (iterator.hasNext())
      {
         Element pcrefElement = (Element) iterator.next();
         PortComponentRefMetaData pcrefMetaData = new PortComponentRefMetaData();
         pcrefMetaData.importStandardXml(pcrefElement);
         portComponentRefs.add(pcrefMetaData);
      }

      // Parse the handler elements
      iterator = MetaData.getChildrenByTagName(element, "handler");
      while (iterator.hasNext())
      {
         Element handlerElement = (Element) iterator.next();
         ServiceRefHandlerMetaData handlerMetaData = new ServiceRefHandlerMetaData();
         handlerMetaData.importStandardXml(handlerElement);
         handlers.add(handlerMetaData);
      }
   }

   public void importJBossXml(Element element) throws DeploymentException
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
