/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.webservice.metadata.jaxrpcmapping;

// $Id: JavaWsdlMappingFactory.java,v 1.1 2004/05/14 18:34:23 tdiesler Exp $

import org.jboss.logging.Logger;
import org.jboss.xml.binding.ContentNavigator;
import org.jboss.xml.binding.ObjectModelFactory;
import org.xml.sax.Attributes;

/**
 * A JBossXB factory for {@link org.jboss.webservice.metadata.jaxrpcmapping.JavaWsdlMapping}
 * 
 * @author Thomas.Diesler@jboss.org
 * @since 14-May-2004
 */
public class JavaWsdlMappingFactory implements ObjectModelFactory
{
   // provide logging
   private static final Logger log = Logger.getLogger(JavaWsdlMappingFactory.class);

   /**
    * This method is called on the factory by the object model builder when the parsing starts.
    */
   public Object startDocument()
   {
      return new JavaWsdlMapping();
   }

   /**
    * This method is called on the factory when the parsing is done.
    */
   public void endDocument(Object objectModel) throws Exception
   {
   }

   /**
    * Called when parsing of a new element started.
    */
   public Object newChild(JavaWsdlMapping javaWsdlMapping, ContentNavigator navigator, String namespaceURI, String localName, Attributes attrs)
   {
      log.trace("newChild: " + localName);
      if ("package-mapping".equals(localName))
         return new PackageMapping(javaWsdlMapping);
      if ("java-xml-type-mapping".equals(localName))
         return new JavaXmlTypeMapping(javaWsdlMapping);
      if ("exception-mapping".equals(localName))
         return new ExceptionMapping(javaWsdlMapping);
      if ("service-interface-mapping".equals(localName))
         return new ServiceInterfaceMapping(javaWsdlMapping);
      if ("service-endpoint-interface-mapping".equals(localName))
         return new ServiceEndpointInterfaceMapping(javaWsdlMapping);
      return null;
   }

   /**
    * Called when parsing character is complete.
    */
   public void addChild(JavaWsdlMapping javaWsdlMapping, PackageMapping packageMapping, ContentNavigator navigator)
   {
      log.trace("addChild: [obj=" + javaWsdlMapping + ",child=" + packageMapping + "]");
      javaWsdlMapping.addPackageMapping(packageMapping);
   }

   /**
    * Called when parsing character is complete.
    */
   public void addChild(JavaWsdlMapping javaWsdlMapping, JavaXmlTypeMapping typeMapping, ContentNavigator navigator)
   {
      log.trace("addChild: [obj=" + javaWsdlMapping + ",child=" + typeMapping + "]");
      javaWsdlMapping.addJavaXmlTypeMappings(typeMapping);
   }

   /**
    * Called when parsing character is complete.
    */
   public void addChild(JavaWsdlMapping javaWsdlMapping, ExceptionMapping exceptionMapping, ContentNavigator navigator)
   {
      log.trace("addChild: [obj=" + javaWsdlMapping + ",child=" + exceptionMapping + "]");
      javaWsdlMapping.addExceptionMappings(exceptionMapping);
   }

   /**
    * Called when parsing character is complete.
    */
   public void addChild(JavaWsdlMapping javaWsdlMapping, ServiceInterfaceMapping siMapping, ContentNavigator navigator)
   {
      log.trace("addChild: [obj=" + javaWsdlMapping + ",child=" + siMapping + "]");
      javaWsdlMapping.addServiceInterfaceMappings(siMapping);
   }

   /**
    * Called when parsing character is complete.
    */
   public void addChild(JavaWsdlMapping javaWsdlMapping, ServiceEndpointInterfaceMapping seiMapping, ContentNavigator navigator)
   {
      log.trace("addChild: [obj=" + javaWsdlMapping + ",child=" + seiMapping + "]");
      javaWsdlMapping.addServiceEndpointInterfaceMappings(seiMapping);
   }

   /**
    * Called when a new simple child element with text value was read from the XML content.
    */
   public void setValue(PackageMapping packageMapping, ContentNavigator navigator, String namespaceURI, String localName, String value)
   {
      log.trace("setValue: [obj=" + packageMapping + ",value=" + value + "]");
      if ("package-type".equals(localName))
         packageMapping.setPackageType(value);
      else if ("namespaceURI".equals(localName))
         packageMapping.setNamespaceURI(value);
   }

   /**
    * Called when parsing of a new element started.
    */
   public Object newChild(JavaXmlTypeMapping typeMapping, ContentNavigator navigator, String namespaceURI, String localName, Attributes attrs)
   {
      log.trace("newChild: " + localName);
      if ("variable-mapping".equals(localName))
         return new VariableMapping(typeMapping);
      return null;
   }

   /**
    * Called when parsing character is complete.
    */
   public void addChild(JavaXmlTypeMapping typeMapping, VariableMapping variableMapping, ContentNavigator navigator)
   {
      log.trace("addChild: [obj=" + typeMapping + ",child=" + variableMapping + "]");
      typeMapping.addVariableMapping(variableMapping);
   }

   /**
    * Called when a new simple child element with text value was read from the XML content.
    */
   public void setValue(JavaXmlTypeMapping typeMapping, ContentNavigator navigator, String namespaceURI, String localName, String value)
   {
      log.trace("setValue: [obj=" + typeMapping + ",value=" + value + "]");
      if ("class-type".equals(localName))
         typeMapping.setClassType(value);
      else if ("root-type-qname".equals(localName))
         typeMapping.setRootTypeQName(navigator.resolveQName(value));
      else if ("qname-scope".equals(localName))
         typeMapping.setQnameScope(value);
   }

   /**
    * Called when a new simple child element with text value was read from the XML content.
    */
   public void setValue(ExceptionMapping exceptionMapping, ContentNavigator navigator, String namespaceURI, String localName, String value)
   {
      log.trace("setValue: [obj=" + exceptionMapping + ",value=" + value + "]");
      if ("exception-type".equals(localName))
         exceptionMapping.setExceptionType(value);
      else if ("wsdl-message".equals(localName))
         exceptionMapping.setWsdlMessage(navigator.resolveQName(value));
      else if ("constructor-parameter-order".equals(localName))
         exceptionMapping.addConstructorParameter(value);
   }

   /**
    * Called when parsing of a new element started.
    */
   public Object newChild(ServiceInterfaceMapping siMapping, ContentNavigator navigator, String namespaceURI, String localName, Attributes attrs)
   {
      log.trace("newChild: " + localName);
      if ("port-mapping".equals(localName))
         return new PortMapping(siMapping);
      return null;
   }

   /**
    * Called when parsing character is complete.
    */
   public void addChild(ServiceInterfaceMapping siMapping, PortMapping portMapping, ContentNavigator navigator)
   {
      log.trace("addChild: [obj=" + siMapping + ",child=" + portMapping + "]");
      siMapping.addPortMapping(portMapping);
   }

   /**
    * Called when a new simple child element with text value was read from the XML content.
    */
   public void setValue(ServiceInterfaceMapping siMapping, ContentNavigator navigator, String namespaceURI, String localName, String value)
   {
      log.trace("setValue: [obj=" + siMapping + ",value=" + value + "]");
      if ("service-interface".equals(localName))
         siMapping.setServiceInterface(value);
      else if ("wsdl-service-name".equals(localName))
         siMapping.setWsdlServiceName(navigator.resolveQName(value));
   }

   /**
    * Called when parsing of a new element started.
    */
   public Object newChild(ServiceEndpointInterfaceMapping seiMapping, ContentNavigator navigator, String namespaceURI, String localName, Attributes attrs)
   {
      log.trace("newChild: " + localName);
      if ("service-endpoint-method-mapping".equals(localName))
         return new ServiceEndpointMethodMapping(seiMapping);
      return null;
   }

   /**
    * Called when parsing character is complete.
    */
   public void addChild(ServiceEndpointInterfaceMapping seiMapping, ServiceEndpointMethodMapping seiMethodMapping, ContentNavigator navigator)
   {
      log.trace("addChild: [obj=" + seiMapping + ",child=" + seiMapping + "]");
      seiMapping.addServiceEndpointMethodMapping(seiMethodMapping);
   }

   /**
    * Called when a new simple child element with text value was read from the XML content.
    */
   public void setValue(ServiceEndpointInterfaceMapping seiMapping, ContentNavigator navigator, String namespaceURI, String localName, String value)
   {
      log.trace("setValue: [obj=" + seiMapping + ",value=" + value + "]");
      if ("service-endpoint-interface".equals(localName))
         seiMapping.setServiceEndpointInterface(value);
      else if ("wsdl-port-type".equals(localName))
         seiMapping.setWsdlPortType(navigator.resolveQName(value));
      else if ("wsdl-binding".equals(localName))
         seiMapping.setWsdlBinding(navigator.resolveQName(value));
   }

   /**
    * Called when a new simple child element with text value was read from the XML content.
    */
   public void setValue(VariableMapping variableMapping, ContentNavigator navigator, String namespaceURI, String localName, String value)
   {
      log.trace("setValue: [obj=" + variableMapping + ",value=" + value + "]");
      if ("java-variable-name".equals(localName))
         variableMapping.setJavaVariableName(value);
      else if ("data-member".equals(localName))
         variableMapping.setDataMember(true);
      else if ("xml-element-name".equals(localName))
         variableMapping.setXmlElementName(value);
   }

   /**
    * Called when a new simple child element with text value was read from the XML content.
    */
   public void setValue(PortMapping portMapping, ContentNavigator navigator, String namespaceURI, String localName, String value)
   {
      log.trace("setValue: [obj=" + portMapping + ",value=" + value + "]");
      if ("port-name".equals(localName))
         portMapping.setPortName(value);
      else if ("java-port-name".equals(localName))
         portMapping.setJavaPortName(value);
   }

   /**
    * Called when parsing of a new element started.
    */
   public Object newChild(ServiceEndpointMethodMapping methodMapping, ContentNavigator navigator, String namespaceURI, String localName, Attributes attrs)
   {
      log.trace("newChild: " + localName);
      if ("method-param-parts-mapping".equals(localName))
         return new MethodParamPartsMapping(methodMapping);
      if ("wsdl-return-value-mapping".equals(localName))
         return new WsdlReturnValueMapping(methodMapping);
      return null;
   }

   /**
    * Called when parsing character is complete.
    */
   public void addChild(ServiceEndpointMethodMapping methodMapping, MethodParamPartsMapping partsMapping, ContentNavigator navigator)
   {
      log.trace("addChild: [obj=" + methodMapping + ",child=" + partsMapping + "]");
      methodMapping.addMethodParamPartsMapping(partsMapping);
   }

   /**
    * Called when parsing character is complete.
    */
   public void addChild(ServiceEndpointMethodMapping methodMapping, WsdlReturnValueMapping returnValueMapping, ContentNavigator navigator)
   {
      log.trace("addChild: [obj=" + methodMapping + ",child=" + returnValueMapping + "]");
      methodMapping.setWsdlReturnValueMapping(returnValueMapping);
   }

   /**
    * Called when a new simple child element with text value was read from the XML content.
    */
   public void setValue(ServiceEndpointMethodMapping methodMapping, ContentNavigator navigator, String namespaceURI, String localName, String value)
   {
      log.trace("setValue: [obj=" + methodMapping + ",value=" + value + "]");
      if ("java-method-name".equals(localName))
         methodMapping.setJavaMethodName(value);
      else if ("wsdl-operation".equals(localName))
         methodMapping.setWsdlOperation(value);
      else if ("wrapped-element".equals(localName))
         methodMapping.setWrappedElement(true);
   }

   /**
    * Called when parsing of a new element started.
    */
   public Object newChild(MethodParamPartsMapping partsMapping, ContentNavigator navigator, String namespaceURI, String localName, Attributes attrs)
   {
      log.trace("newChild: " + localName);
      if ("wsdl-message-mapping".equals(localName))
         return new WsdlMessageMapping(partsMapping);
      return null;
   }

   /**
    * Called when parsing character is complete.
    */
   public void addChild(MethodParamPartsMapping partsMapping, WsdlMessageMapping msgMapping, ContentNavigator navigator)
   {
      log.trace("addChild: [obj=" + partsMapping + ",child=" + msgMapping + "]");
      partsMapping.setWsdlMessageMapping(msgMapping);
   }

   /**
    * Called when a new simple child element with text value was read from the XML content.
    */
   public void setValue(MethodParamPartsMapping partsMapping, ContentNavigator navigator, String namespaceURI, String localName, String value)
   {
      log.trace("setValue: [obj=" + partsMapping + ",value=" + value + "]");
      if ("param-position".equals(localName))
         partsMapping.setParamPosition(new Integer(value).intValue());
      else if ("param-type".equals(localName))
         partsMapping.setParamType(value);
   }

   /**
    * Called when a new simple child element with text value was read from the XML content.
    */
   public void setValue(WsdlReturnValueMapping retValueMapping, ContentNavigator navigator, String namespaceURI, String localName, String value)
   {
      log.trace("setValue: [obj=" + retValueMapping + ",value=" + value + "]");
      if ("method-return-value".equals(localName))
         retValueMapping.setMethodReturnValue(value);
      else if ("wsdl-message".equals(localName))
         retValueMapping.setWsdlMessage(navigator.resolveQName(value));
      else if ("wsdl-message-part-name".equals(localName))
         retValueMapping.setWsdlMessagePartName(value);
   }

   /**
    * Called when a new simple child element with text value was read from the XML content.
    */
   public void setValue(WsdlMessageMapping msgMapping, ContentNavigator navigator, String namespaceURI, String localName, String value)
   {
      log.trace("setValue: [obj=" + msgMapping + ",value=" + value + "]");
      if ("wsdl-message".equals(localName))
         msgMapping.setWsdlMessage(navigator.resolveQName(value));
      else if ("wsdl-message-part-name".equals(localName))
         msgMapping.setWsdlMessagePartName(value);
      else if ("parameter-mode".equals(localName))
         msgMapping.setParameterMode(value);
      else if ("soap-header".equals(localName))
         msgMapping.setSoapHeader(true);
   }
}
