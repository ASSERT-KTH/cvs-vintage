/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.metadata;

// $Id: ServiceRefMetaData.java,v 1.11 2004/05/14 18:34:20 tdiesler Exp $

import org.jboss.deployment.DeploymentException;
import org.jboss.webservice.WSDLDefinitionFactory;
import org.jboss.webservice.metadata.jaxrpcmapping.JavaWsdlMapping;
import org.jboss.webservice.metadata.jaxrpcmapping.JavaWsdlMappingFactory;
import org.jboss.xml.binding.Unmarshaller;
import org.w3c.dom.Element;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.xml.namespace.QName;
import javax.xml.rpc.JAXRPCException;
import java.io.Serializable;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Iterator;

/** The metdata data from service-ref element in web.xml, ejb-jar.xml, and application-client.xml.
 *
 * @author Thomas.Diesler@jboss.org
 * @version $Revision: 1.11 $
 */
public class ServiceRefMetaData implements Serializable
{
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

   /** The ClassLoader to load additional resources */
   private transient URLClassLoader resourceCL;
   // The wsdl definition, if we have one
   private transient Definition wsdlDefinition;
   // The java/wsdl mapping, if we have one
   private transient JavaWsdlMapping javaWsdlMapping;

   /** Default constructor, used when unmarshalling on the client side
    */
   public ServiceRefMetaData()
   {
   }

   /** Constructor with a given resource classloader, used on the server side
    */
   public ServiceRefMetaData(URLClassLoader resourceCl)
   {
      setResourceCl(resourceCl);
   }

   /** Set the resource classloader that can load the wsdl file
    * On the client side this is set expicitly after unmarshalling.
    */
   public void setResourceCl(URLClassLoader resourceCl)
   {
      if (resourceCl == null)
         throw new IllegalArgumentException("ResourceClassLoader cannot be null");

      this.resourceCL = resourceCl;
   }

   public URLClassLoader getResourceCL()
   {
      return resourceCL;
   }

   public String getJaxrpcMappingFile()
   {
      return jaxrpcMappingFile;
   }

   public JavaWsdlMapping getJavaWsdlMapping()
   {
      if (javaWsdlMapping == null)
      {
         try
         {
            // setup the XML binding Unmarshaller
            Unmarshaller unmarshaller = new Unmarshaller();
            JavaWsdlMappingFactory factory = new JavaWsdlMappingFactory();
            InputStream is = resourceCL.getResourceAsStream(jaxrpcMappingFile);
            javaWsdlMapping = (JavaWsdlMapping)unmarshaller.unmarshal(is, factory, null);
         }
         catch (Exception e)
         {
            throw new JAXRPCException("Cannot unmarshal jaxrpc-mapping-file: " + jaxrpcMappingFile, e);
         }
      }
      return javaWsdlMapping;
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
      if (resourceCL == null)
         throw new IllegalStateException("Resource class loader not set");
      return resourceCL.loadClass(serviceInterface);
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

   public Definition getWsdlDefinition()
   {
      if (wsdlDefinition != null)
         return wsdlDefinition;

      try
      {
         URL wsdlLocation = resourceCL.findResource(wsdlFile);
         wsdlDefinition = WSDLDefinitionFactory.readWSDL(wsdlLocation);
      }
      catch (WSDLException e)
      {
         throw new IllegalStateException("Cannot unmarshall wsdl, cause: " + e.toString());
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
         PortComponentRefMetaData pcrefMetaData = new PortComponentRefMetaData(this);
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
