/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.webservice.metadata.jaxrpcmapping;

// $Id: JavaXmlTypeMapping.java,v 1.4 2004/06/11 14:53:31 tdiesler Exp $

import javax.xml.namespace.QName;
import java.util.ArrayList;

/**
 * XML mapping of the java-wsdl-mapping/java-xml-type-mapping element.
 *
 * @author Thomas.Diesler@jboss.org
 * @since 14-May-2004
 */
public class JavaXmlTypeMapping
{
   // The parent <java-wsdl-mapping> element
   private JavaWsdlMapping javaWsdlMapping;

   // The required <class-type> element
   private String javaType;
   // The choice <root-type-qname> element
   private QName rootTypeQName;
   // The choice <anonymous-type-qname> element
   private QName anonymousTypeQName;
   // The required <qname-scope> element
   private String qnameScope;
   // Zero or more <variable-mapping> elements
   private ArrayList variableMappings = new ArrayList();

   public JavaXmlTypeMapping(JavaWsdlMapping javaWsdlMapping)
   {
      this.javaWsdlMapping = javaWsdlMapping;
   }

   public JavaWsdlMapping getJavaWsdlMapping()
   {
      return javaWsdlMapping;
   }

   public String getJavaType()
   {
      return javaType;
   }

   public void setJavaType(String javaType)
   {
      this.javaType = javaType;
   }

   public String getQnameScope()
   {
      return qnameScope;
   }

   public void setQnameScope(String qnameScope)
   {
      this.qnameScope = qnameScope;
   }

   public QName getRootTypeQName()
   {
      return rootTypeQName;
   }

   public void setRootTypeQName(QName rootTypeQName)
   {
      this.rootTypeQName = rootTypeQName;
   }

   public QName getAnonymousTypeQName()
   {
      return anonymousTypeQName;
   }

   public void setAnonymousTypeQName(QName anonymousTypeQName)
   {
      this.anonymousTypeQName = anonymousTypeQName;
   }

   public VariableMapping[] getVariableMappings()
   {
      VariableMapping[] arr = new VariableMapping[variableMappings.size()];
      variableMappings.toArray(arr);
      return arr;
   }

   public void addVariableMapping(VariableMapping variableMapping)
   {
      variableMappings.add(variableMapping);
   }
}
