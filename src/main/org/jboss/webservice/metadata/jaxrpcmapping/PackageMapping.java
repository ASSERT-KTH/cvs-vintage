/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.webservice.metadata.jaxrpcmapping;

// $Id: PackageMapping.java,v 1.1 2004/05/14 18:34:23 tdiesler Exp $

/**
 * XML mapping of the java-wsdl-mapping/package-mapping element.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 14-May-2004
 */
public class PackageMapping
{
   // The parent <java-wsdl-mapping> element
   private JavaWsdlMapping javaWsdlMapping;

   // The required <package-type> element
   private String packageType;
   // The required <namespaceURI> element
   private String namespaceURI;

   public PackageMapping(JavaWsdlMapping javaWsdlMapping)
   {
      this.javaWsdlMapping = javaWsdlMapping;
   }

   public JavaWsdlMapping getJavaWsdlMapping()
   {
      return javaWsdlMapping;
   }

   public String getNamespaceURI()
   {
      return namespaceURI;
   }

   public void setNamespaceURI(String namespaceURI)
   {
      this.namespaceURI = namespaceURI;
   }

   public String getPackageType()
   {
      return packageType;
   }

   public void setPackageType(String packageType)
   {
      this.packageType = packageType;
   }

}
