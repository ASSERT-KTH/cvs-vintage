/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.webservice.metadata.jaxrpcmapping;

// $Id: JavaWsdlMapping.java,v 1.7 2004/08/15 22:07:27 tdiesler Exp $

import javax.xml.namespace.QName;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * XML mapping of the java-wsdl-mapping root element in jaxrpc-mapping.xml
 * 
 * @author Thomas.Diesler@jboss.org
 * @since 14-May-2004
 */
public class JavaWsdlMapping
{
   // One or more <package-mapping> elements
   private ArrayList packageMappings = new ArrayList();
   // Zero or more <java-xml-type-mapping> elements
   private ArrayList javaXmlTypeMappings = new ArrayList();
   // Zero or more <exception-mapping> elements
   private ArrayList exceptionMappings = new ArrayList();
   // Zero or more <service-interface-mapping> elements
   private ArrayList serviceInterfaceMappings = new ArrayList();
   // Zero or more <service-endpoint-interface-mapping> elements
   private ArrayList serviceEndpointInterfaceMappings = new ArrayList();

   public PackageMapping[] getPackageMappings()
   {
      PackageMapping[] arr = new PackageMapping[packageMappings.size()];
      packageMappings.toArray(arr);
      return arr;
   }

   public JavaXmlTypeMapping[] getJavaXmlTypeMappings()
   {
      JavaXmlTypeMapping[] arr = new JavaXmlTypeMapping[javaXmlTypeMappings.size()];
      javaXmlTypeMappings.toArray(arr);
      return arr;
   }

   public ExceptionMapping[] getExceptionMappings()
   {
      ExceptionMapping[] arr = new ExceptionMapping[exceptionMappings.size()];
      exceptionMappings.toArray(arr);
      return arr;
   }

   public ServiceInterfaceMapping[] getServiceInterfaceMappings()
   {
      ServiceInterfaceMapping[] arr = new ServiceInterfaceMapping[serviceInterfaceMappings.size()];
      serviceInterfaceMappings.toArray(arr);
      return arr;
   }

   public ServiceEndpointInterfaceMapping[] getServiceEndpointInterfaceMappings()
   {
      ServiceEndpointInterfaceMapping[] arr = new ServiceEndpointInterfaceMapping[serviceEndpointInterfaceMappings.size()];
      serviceEndpointInterfaceMappings.toArray(arr);
      return arr;
   }

   // convenience methods ********************************************************************

   /** Get the package string for a given URI
    */
   public String getPackageTypeForURI(String uri) {
      String packageStr = null;
      for (int i = 0; packageStr == null && i < packageMappings.size(); i++)
      {
         PackageMapping mapping = (PackageMapping)packageMappings.get(i);
         if (mapping.getNamespaceURI().equals(uri))
            packageStr = mapping.getPackageType();
      }
      return packageStr;
   }


   /** Get the type mapping fo a given root-type-qname
    */
   public JavaXmlTypeMapping getTypeMappingForQName(QName typeQName)
   {
      JavaXmlTypeMapping typeMapping = null;

      if (typeQName != null)
      {
         Iterator it = javaXmlTypeMappings.iterator();
         while (it.hasNext())
         {
            JavaXmlTypeMapping mapping = (JavaXmlTypeMapping)it.next();
            if (typeQName.equals(mapping.getRootTypeQName()))
               typeMapping = mapping;
         }
      }

      return typeMapping;
   }

   /** Get the exception mapping fo a given wsdl message
    */
   public ExceptionMapping getExceptionMappingForMessageQName(QName wsdlMessage)
   {
      ExceptionMapping exMapping = null;

      if (wsdlMessage != null)
      {
         Iterator it = exceptionMappings.iterator();
         while (it.hasNext())
         {
            ExceptionMapping mapping = (ExceptionMapping)it.next();
            if (wsdlMessage.equals(mapping.getWsdlMessage()))
               exMapping = mapping;
         }
      }

      return exMapping;
   }

   /** Get the port type qname for a given service endpoint infterface
    */
   public QName getPortTypeQNameForServiceEndpointInterface(String seiName)
   {
      QName portTypeQName = null;

      ServiceEndpointInterfaceMapping[] seiMappings = getServiceEndpointInterfaceMappings();
      for (int i = 0; i < seiMappings.length; i++)
      {
         ServiceEndpointInterfaceMapping seiMapping = seiMappings[i];
         if (seiMapping.getServiceEndpointInterface().equals(seiName))
            portTypeQName = seiMapping.getWsdlPortType();
      }

      return portTypeQName;
   }

   /** Get the service endpoint infterfacemapping for a given port type qname
    */
   public ServiceEndpointInterfaceMapping getServiceEndpointInterfaceMappingByPortType(QName portType)
   {
      ServiceEndpointInterfaceMapping seiMapping = null;

      ServiceEndpointInterfaceMapping[] seiMappings = getServiceEndpointInterfaceMappings();
      for (int i = 0; seiMapping == null && i < seiMappings.length; i++)
      {
         ServiceEndpointInterfaceMapping aux = seiMappings[i];
         if (aux.getWsdlPortType().equals(portType))
            seiMapping = aux;
      }

      return seiMapping;
   }

   // factory methods ********************************************************************

   public void addPackageMapping(PackageMapping packageMapping)
   {
      packageMappings.add(packageMapping);
   }

   public void addJavaXmlTypeMappings(JavaXmlTypeMapping typeMapping)
   {
      javaXmlTypeMappings.add(typeMapping);
   }

   public void addExceptionMappings(ExceptionMapping exceptionMapping)
   {
      exceptionMappings.add(exceptionMapping);
   }

   public void addServiceInterfaceMappings(ServiceInterfaceMapping serviceInterfaceMapping)
   {
      serviceInterfaceMappings.add(serviceInterfaceMapping);
   }

   public void addServiceEndpointInterfaceMappings(ServiceEndpointInterfaceMapping serviceEndpointInterfaceMapping)
   {
      serviceEndpointInterfaceMappings.add(serviceEndpointInterfaceMapping);
   }
}
