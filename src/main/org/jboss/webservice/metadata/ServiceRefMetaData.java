/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.webservice.metadata;

// $Id: ServiceRefMetaData.java,v 1.2 2005/01/04 13:30:38 tdiesler Exp $

import org.jboss.deployment.DeploymentException;
import org.jboss.metadata.MetaData;
import org.jboss.webservice.WSDLDefinitionFactory;
import org.jboss.webservice.metadata.jaxrpcmapping.JavaWsdlMapping;
import org.jboss.webservice.metadata.jaxrpcmapping.JavaWsdlMappingFactory;
import org.jboss.xml.QNameBuilder;
import org.w3c.dom.Element;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.xml.namespace.QName;
import javax.xml.rpc.JAXRPCException;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Properties;

/** The metdata data from service-ref element in web.xml, ejb-jar.xml, and
 * application-client.xml.
 *
 * @author Thomas.Diesler@jboss.org
 * @version $Revision: 1.2 $
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
   // The LinkedHashMap<String, PortComponentRefMetaData> for <port-component-ref> elements
   private LinkedHashMap portComponentRefs = new LinkedHashMap();
   // The optional <handler> elements
   private ArrayList handlers = new ArrayList();

   /** The URL of the actual WSDL to use, <wsdl-override> */
   private URL wsdlOverride;
   /** Arbitrary proxy properties given by <call-property> */
   private Properties callProperties;

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
      if (javaWsdlMapping == null && jaxrpcMappingFile != null)
      {
         try
         {
            // setup the XML binding Unmarshaller
            URL location = resourceCL.findResource(jaxrpcMappingFile);
            JavaWsdlMappingFactory mappingFactory = JavaWsdlMappingFactory.newInstance();
            javaWsdlMapping = mappingFactory.parse(location);
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
      portComponentRefs.values().toArray(array);
      return array;
   }

   public HandlerMetaData[] getHandlers()
   {
      HandlerMetaData[] array = new HandlerMetaData[handlers.size()];
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

   public Properties getCallProperties()
   {
      return callProperties;
   }

   public Definition getWsdlDefinition()
   {
      if (wsdlDefinition == null && (wsdlOverride != null || wsdlFile != null))
      {
         try
         {
            URL wsdlURL = (wsdlOverride != null ? wsdlOverride : resourceCL.findResource(wsdlFile));
            WSDLDefinitionFactory factory = WSDLDefinitionFactory.newInstance();
            wsdlDefinition = factory.parse(wsdlURL);
         }
         catch (WSDLException e)
         {
            throw new IllegalStateException("Cannot unmarshall wsdl, cause: " + e.toString());
         }
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

      Element qnameElement = MetaData.getOptionalChild(element, "service-qname");
      if (qnameElement != null)
         serviceQName = QNameBuilder.buildQName(qnameElement, MetaData.getElementContent(qnameElement));
      
      // Parse the port-component-ref elements
      Iterator iterator = MetaData.getChildrenByTagName(element, "port-component-ref");
      while (iterator.hasNext())
      {
         Element pcrefElement = (Element)iterator.next();
         PortComponentRefMetaData pcrefMetaData = new PortComponentRefMetaData(this);
         pcrefMetaData.importStandardXml(pcrefElement);
         portComponentRefs.put(pcrefMetaData.getServiceEndpointInterface(), pcrefMetaData);
      }

      // Parse the handler elements
      iterator = MetaData.getChildrenByTagName(element, "handler");
      while (iterator.hasNext())
      {
         Element handlerElement = (Element)iterator.next();
         HandlerMetaData handlerMetaData = new HandlerMetaData();
         handlerMetaData.importStandardXml(handlerElement);
         handlers.add(handlerMetaData);
      }
   }

   /** Parse jboss specific service-ref child elements
    * @param element
    * @throws DeploymentException
    */
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

      // Parse the port-component-ref elements
      Iterator iterator = MetaData.getChildrenByTagName(element, "port-component-ref");
      while (iterator.hasNext())
      {
         Element pcrefElement = (Element)iterator.next();
         String name = MetaData.getOptionalChildContent(pcrefElement, "service-endpoint-interface");
         if (name != null)
         {
            PortComponentRefMetaData pcrefMetaData = (PortComponentRefMetaData)portComponentRefs.get(name);
            if (pcrefMetaData == null)
            {
               // Its ok to only have the <port-component-ref> in jboss.xml and not in ejb-jar.xml 
               pcrefMetaData = new PortComponentRefMetaData(this);
               pcrefMetaData.importStandardXml(pcrefElement);
               portComponentRefs.put(pcrefMetaData.getServiceEndpointInterface(), pcrefMetaData);
            }

            pcrefMetaData.importJBossXml(pcrefElement);
         }
      }

      // Parse the call-property elements
      iterator = MetaData.getChildrenByTagName(element, "call-property");
      while (iterator.hasNext())
      {
         Element propElement = (Element)iterator.next();
         String name = MetaData.getUniqueChildContent(propElement, "prop-name");
         String value = MetaData.getUniqueChildContent(propElement, "prop-value");
         if (callProperties == null)
            callProperties = new Properties();
         callProperties.setProperty(name, value);
      }
   }
}
