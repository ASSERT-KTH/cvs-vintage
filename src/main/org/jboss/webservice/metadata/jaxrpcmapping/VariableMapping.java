/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.webservice.metadata.jaxrpcmapping;

// $Id: VariableMapping.java,v 1.1 2004/05/14 18:34:23 tdiesler Exp $

/**
 * XML mapping of the java-wsdl-mapping/java-xml-type-mapping/varaible-mapping element.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 14-May-2004
 */
public class VariableMapping
{
   // The parent <java-wsdl-mapping> element
   private JavaXmlTypeMapping typeMapping;

   // The required <java-variable-name> element
   private String javaVariableName;
   // The optional <data-member> element
   private boolean dataMember;
   // The required <xml-element-name> element
   private String xmlElementName;

   public VariableMapping(JavaXmlTypeMapping typeMapping)
   {
      this.typeMapping = typeMapping;
   }

   public JavaXmlTypeMapping getTypeMapping()
   {
      return typeMapping;
   }

   public boolean isDataMember()
   {
      return dataMember;
   }

   public void setDataMember(boolean dataMember)
   {
      this.dataMember = dataMember;
   }

   public String getJavaVariableName()
   {
      return javaVariableName;
   }

   public void setJavaVariableName(String javaVariableName)
   {
      this.javaVariableName = javaVariableName;
   }

   public String getXmlElementName()
   {
      return xmlElementName;
   }

   public void setXmlElementName(String xmlElementName)
   {
      this.xmlElementName = xmlElementName;
   }
}
