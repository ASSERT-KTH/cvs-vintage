/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.webservice.metadata.jaxrpcmapping;

// $Id: JavaWsdlMapping.java,v 1.2 2004/06/08 17:41:18 tdiesler Exp $

import java.util.ArrayList;

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
