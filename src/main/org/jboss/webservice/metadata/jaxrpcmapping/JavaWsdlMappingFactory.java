/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.webservice.metadata.jaxrpcmapping;

// $Id: JavaWsdlMappingFactory.java,v 1.15 2004/12/26 16:39:04 loubyansky Exp $

import org.jboss.logging.Logger;
import org.jboss.xml.binding.UnmarshallingContext;
import org.jboss.xml.binding.ObjectModelFactory;
import org.jboss.xml.binding.Unmarshaller;
import org.jboss.xml.binding.UnmarshallerFactory;
import org.xml.sax.Attributes;

import javax.xml.namespace.QName;
import java.io.InputStream;
import java.net.URL;

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

   // Hide constructor
   private JavaWsdlMappingFactory()
   {
   }

   /**
    * Create a new instance of a jaxrpc-mapping factory
    */
   public static JavaWsdlMappingFactory newInstance()
   {
      return new JavaWsdlMappingFactory();
   }

   /**
    * Factory method for JavaWsdlMapping
    */
   public JavaWsdlMapping parse(URL jaxrpcMappingFile) throws Exception
   {
      if(jaxrpcMappingFile == null)
      {
         throw new IllegalArgumentException("URL cannot be null");
      }

      // setup the XML binding Unmarshaller
      Unmarshaller unmarshaller = UnmarshallerFactory.newInstance().newUnmarshaller();
      InputStream is = jaxrpcMappingFile.openStream();
      try
      {
         JavaWsdlMapping javaWsdlMapping = (JavaWsdlMapping)unmarshaller.unmarshal(is, this, null);
         return javaWsdlMapping;
      }
      finally
      {
         is.close();
      }
   }

   /**
    * This method is called on the factory by the object model builder when the parsing starts.
    */
   public Object newRoot(Object root,
                         UnmarshallingContext navigator,
                         String namespaceURI,
                         String localName,
                         Attributes attrs)
   {
      return new JavaWsdlMapping();
   }

   public Object completeRoot(Object root, UnmarshallingContext ctx, String uri, String name)
   {
      return root;
   }

   /**
    * Called when parsing of a new element started.
    */
   public Object newChild(JavaWsdlMapping javaWsdlMapping,
                          UnmarshallingContext navigator,
                          String namespaceURI,
                          String localName,
                          Attributes attrs)
   {
      log.trace("newChild: " + localName);
      if("package-mapping".equals(localName))
      {
         return new PackageMapping(javaWsdlMapping);
      }
      if("java-xml-type-mapping".equals(localName))
      {
         return new JavaXmlTypeMapping(javaWsdlMapping);
      }
      if("exception-mapping".equals(localName))
      {
         return new ExceptionMapping(javaWsdlMapping);
      }
      if("service-interface-mapping".equals(localName))
      {
         return new ServiceInterfaceMapping(javaWsdlMapping);
      }
      if("service-endpoint-interface-mapping".equals(localName))
      {
         return new ServiceEndpointInterfaceMapping(javaWsdlMapping);
      }
      return null;
   }

   /**
    * Called when parsing character is complete.
    */
   public void addChild(JavaWsdlMapping javaWsdlMapping, PackageMapping packageMapping, UnmarshallingContext navigator, String namespaceURI, String localName)
   {
      log.trace("addChild: [obj=" + javaWsdlMapping + ",child=" + packageMapping + "]");
      javaWsdlMapping.addPackageMapping(packageMapping);
   }

   /**
    * Called when parsing character is complete.
    */
   public void addChild(JavaWsdlMapping javaWsdlMapping, JavaXmlTypeMapping typeMapping, UnmarshallingContext navigator, String namespaceURI, String localName)
   {
      log.trace("addChild: [obj=" + javaWsdlMapping + ",child=" + typeMapping + "]");
      javaWsdlMapping.addJavaXmlTypeMappings(typeMapping);
   }

   /**
    * Called when parsing character is complete.
    */
   public void addChild(JavaWsdlMapping javaWsdlMapping,
                        ExceptionMapping exceptionMapping,
                        UnmarshallingContext navigator, String namespaceURI, String localName)
   {
      log.trace("addChild: [obj=" + javaWsdlMapping + ",child=" + exceptionMapping + "]");
      javaWsdlMapping.addExceptionMappings(exceptionMapping);
   }

   /**
    * Called when parsing character is complete.
    */
   public void addChild(JavaWsdlMapping javaWsdlMapping,
                        ServiceInterfaceMapping siMapping,
                        UnmarshallingContext navigator, String namespaceURI, String localName)
   {
      log.trace("addChild: [obj=" + javaWsdlMapping + ",child=" + siMapping + "]");
      javaWsdlMapping.addServiceInterfaceMappings(siMapping);
   }

   /**
    * Called when parsing character is complete.
    */
   public void addChild(JavaWsdlMapping javaWsdlMapping,
                        ServiceEndpointInterfaceMapping seiMapping,
                        UnmarshallingContext navigator, String namespaceURI, String localName)
   {
      log.trace("addChild: [obj=" + javaWsdlMapping + ",child=" + seiMapping + "]");
      javaWsdlMapping.addServiceEndpointInterfaceMappings(seiMapping);
   }

   /**
    * Called when a new simple child element with text value was read from the XML content.
    */
   public void setValue(PackageMapping packageMapping,
                        UnmarshallingContext navigator,
                        String namespaceURI,
                        String localName,
                        String value)
   {
      log.trace("setValue: [obj=" + packageMapping + ",value=" + value + "]");
      if("package-type".equals(localName))
      {
         packageMapping.setPackageType(value);
      }
      else if("namespaceURI".equals(localName))
      {
         packageMapping.setNamespaceURI(value);
      }
   }

   /**
    * Called when parsing of a new element started.
    */
   public Object newChild(JavaXmlTypeMapping typeMapping,
                          UnmarshallingContext navigator,
                          String namespaceURI,
                          String localName,
                          Attributes attrs)
   {
      log.trace("newChild: " + localName);
      if("variable-mapping".equals(localName))
      {
         return new VariableMapping(typeMapping);
      }
      return null;
   }

   /**
    * Called when parsing character is complete.
    */
   public void addChild(JavaXmlTypeMapping typeMapping, VariableMapping variableMapping, UnmarshallingContext navigator, String namespaceURI, String localName)
   {
      log.trace("addChild: [obj=" + typeMapping + ",child=" + variableMapping + "]");
      typeMapping.addVariableMapping(variableMapping);
   }

   /**
    * Called when a new simple child element with text value was read from the XML content.
    */
   public void setValue(JavaXmlTypeMapping typeMapping,
                        UnmarshallingContext navigator,
                        String namespaceURI,
                        String localName,
                        String value)
   {
      log.trace("setValue: [obj=" + typeMapping + ",value=" + value + "]");
      if("java-type".equals(localName))
      {
         typeMapping.setJavaType(value);
      }
      else if("root-type-qname".equals(localName))
      {
         typeMapping.setRootTypeQName(navigator.resolveQName(value));
      }
      else if("anonymous-type-qname".equals(localName))
      {
         int index = value.lastIndexOf(':');
         if(index < 0)
            throw new IllegalArgumentException("Invalid anonymous qname: " + value);

         String nsURI = value.substring(0, index);
         String localPart = value.substring(index + 1);
         typeMapping.setAnonymousTypeQName(new QName(nsURI, localPart));
      }
      else if("qname-scope".equals(localName))
      {
         typeMapping.setQnameScope(value);
      }
   }

   /**
    * Called when a new simple child element with text value was read from the XML content.
    */
   public void setValue(ExceptionMapping exceptionMapping,
                        UnmarshallingContext navigator,
                        String namespaceURI,
                        String localName,
                        String value)
   {
      log.trace("setValue: [obj=" + exceptionMapping + ",value=" + value + "]");
      if("exception-type".equals(localName))
      {
         exceptionMapping.setExceptionType(value);
      }
      else if("wsdl-message".equals(localName))
      {
         exceptionMapping.setWsdlMessage(navigator.resolveQName(value));
      }
      else if("constructor-parameter-order".equals(localName))
      {
         exceptionMapping.addConstructorParameter(value);
      }
   }

   /**
    * Called when parsing of a new element started.
    */
   public Object newChild(ServiceInterfaceMapping siMapping,
                          UnmarshallingContext navigator,
                          String namespaceURI,
                          String localName,
                          Attributes attrs)
   {
      log.trace("newChild: " + localName);
      if("port-mapping".equals(localName))
      {
         return new PortMapping(siMapping);
      }
      return null;
   }

   /**
    * Called when parsing character is complete.
    */
   public void addChild(ServiceInterfaceMapping siMapping, PortMapping portMapping, UnmarshallingContext navigator, String namespaceURI, String localName)
   {
      log.trace("addChild: [obj=" + siMapping + ",child=" + portMapping + "]");
      siMapping.addPortMapping(portMapping);
   }

   /**
    * Called when a new simple child element with text value was read from the XML content.
    */
   public void setValue(ServiceInterfaceMapping siMapping,
                        UnmarshallingContext navigator,
                        String namespaceURI,
                        String localName,
                        String value)
   {
      log.trace("setValue: [obj=" + siMapping + ",value=" + value + "]");
      if("service-interface".equals(localName))
      {
         siMapping.setServiceInterface(value);
      }
      else if("wsdl-service-name".equals(localName))
      {
         siMapping.setWsdlServiceName(navigator.resolveQName(value));
      }
   }

   /**
    * Called when parsing of a new element started.
    */
   public Object newChild(ServiceEndpointInterfaceMapping seiMapping,
                          UnmarshallingContext navigator,
                          String namespaceURI,
                          String localName,
                          Attributes attrs)
   {
      log.trace("newChild: " + localName);
      if("service-endpoint-method-mapping".equals(localName))
      {
         return new ServiceEndpointMethodMapping(seiMapping);
      }
      return null;
   }

   /**
    * Called when parsing character is complete.
    */
   public void addChild(ServiceEndpointInterfaceMapping seiMapping,
                        ServiceEndpointMethodMapping seiMethodMapping,
                        UnmarshallingContext navigator, String namespaceURI, String localName)
   {
      log.trace("addChild: [obj=" + seiMapping + ",child=" + seiMapping + "]");
      seiMapping.addServiceEndpointMethodMapping(seiMethodMapping);
   }

   /**
    * Called when a new simple child element with text value was read from the XML content.
    */
   public void setValue(ServiceEndpointInterfaceMapping seiMapping,
                        UnmarshallingContext navigator,
                        String namespaceURI,
                        String localName,
                        String value)
   {
      log.trace("setValue: [obj=" + seiMapping + ",value=" + value + "]");
      if("service-endpoint-interface".equals(localName))
      {
         seiMapping.setServiceEndpointInterface(value);
      }
      else if("wsdl-port-type".equals(localName))
      {
         seiMapping.setWsdlPortType(navigator.resolveQName(value));
      }
      else if("wsdl-binding".equals(localName))
      {
         seiMapping.setWsdlBinding(navigator.resolveQName(value));
      }
   }

   /**
    * Called when a new simple child element with text value was read from the XML content.
    */
   public void setValue(VariableMapping variableMapping,
                        UnmarshallingContext navigator,
                        String namespaceURI,
                        String localName,
                        String value)
   {
      log.trace("setValue: [obj=" + variableMapping + ",value=" + value + "]");
      if("java-variable-name".equals(localName))
      {
         variableMapping.setJavaVariableName(value);
      }
      else if("data-member".equals(localName))
      {
         variableMapping.setDataMember(true);
      }
      else if("xml-attribute-name".equals(localName))
      {
         variableMapping.setXmlAttributeName(value);
      }
      else if("xml-element-name".equals(localName))
      {
         variableMapping.setXmlElementName(value);
      }
      else if("xml-wildcard".equals(localName))
      {
         variableMapping.setXmlWildcard(value);
      }
   }

   /**
    * Called when a new simple child element with text value was read from the XML content.
    */
   public void setValue(PortMapping portMapping,
                        UnmarshallingContext navigator,
                        String namespaceURI,
                        String localName,
                        String value)
   {
      log.trace("setValue: [obj=" + portMapping + ",value=" + value + "]");
      if("port-name".equals(localName))
      {
         portMapping.setPortName(value);
      }
      else if("java-port-name".equals(localName))
      {
         portMapping.setJavaPortName(value);
      }
   }

   /**
    * Called when parsing of a new element started.
    */
   public Object newChild(ServiceEndpointMethodMapping methodMapping,
                          UnmarshallingContext navigator,
                          String namespaceURI,
                          String localName,
                          Attributes attrs)
   {
      log.trace("newChild: " + localName);
      if("method-param-parts-mapping".equals(localName))
      {
         return new MethodParamPartsMapping(methodMapping);
      }
      if("wsdl-return-value-mapping".equals(localName))
      {
         return new WsdlReturnValueMapping(methodMapping);
      }
      return null;
   }

   /**
    * Called when parsing character is complete.
    */
   public void addChild(ServiceEndpointMethodMapping methodMapping,
                        MethodParamPartsMapping partsMapping,
                        UnmarshallingContext navigator, String namespaceURI, String localName)
   {
      log.trace("addChild: [obj=" + methodMapping + ",child=" + partsMapping + "]");
      methodMapping.addMethodParamPartsMapping(partsMapping);
   }

   /**
    * Called when parsing character is complete.
    */
   public void addChild(ServiceEndpointMethodMapping methodMapping,
                        WsdlReturnValueMapping returnValueMapping,
                        UnmarshallingContext navigator, String namespaceURI, String localName)
   {
      log.trace("addChild: [obj=" + methodMapping + ",child=" + returnValueMapping + "]");
      methodMapping.setWsdlReturnValueMapping(returnValueMapping);
   }

   /**
    * Called when a new simple child element with text value was read from the XML content.
    */
   public void setValue(ServiceEndpointMethodMapping methodMapping,
                        UnmarshallingContext navigator,
                        String namespaceURI,
                        String localName,
                        String value)
   {
      log.trace("setValue: [obj=" + methodMapping + ",value=" + value + "]");
      if("java-method-name".equals(localName))
      {
         methodMapping.setJavaMethodName(value);
      }
      else if("wsdl-operation".equals(localName))
      {
         methodMapping.setWsdlOperation(value);
      }
      else if("wrapped-element".equals(localName))
      {
         methodMapping.setWrappedElement(true);
      }
   }

   /**
    * Called when parsing of a new element started.
    */
   public Object newChild(MethodParamPartsMapping partsMapping,
                          UnmarshallingContext navigator,
                          String namespaceURI,
                          String localName,
                          Attributes attrs)
   {
      log.trace("newChild: " + localName);
      if("wsdl-message-mapping".equals(localName))
      {
         return new WsdlMessageMapping(partsMapping);
      }
      return null;
   }

   /**
    * Called when parsing character is complete.
    */
   public void addChild(MethodParamPartsMapping partsMapping,
                        WsdlMessageMapping msgMapping,
                        UnmarshallingContext navigator, String namespaceURI, String localName)
   {
      log.trace("addChild: [obj=" + partsMapping + ",child=" + msgMapping + "]");
      partsMapping.setWsdlMessageMapping(msgMapping);
   }

   /**
    * Called when a new simple child element with text value was read from the XML content.
    */
   public void setValue(MethodParamPartsMapping partsMapping,
                        UnmarshallingContext navigator,
                        String namespaceURI,
                        String localName,
                        String value)
   {
      log.trace("setValue: [obj=" + partsMapping + ",value=" + value + "]");
      if("param-position".equals(localName))
      {
         partsMapping.setParamPosition(new Integer(value).intValue());
      }
      else if("param-type".equals(localName))
      {
         partsMapping.setParamType(value);
      }
   }

   /**
    * Called when a new simple child element with text value was read from the XML content.
    */
   public void setValue(WsdlReturnValueMapping retValueMapping,
                        UnmarshallingContext navigator,
                        String namespaceURI,
                        String localName,
                        String value)
   {
      log.trace("setValue: [obj=" + retValueMapping + ",value=" + value + "]");
      if("method-return-value".equals(localName))
      {
         retValueMapping.setMethodReturnValue(value);
      }
      else if("wsdl-message".equals(localName))
      {
         retValueMapping.setWsdlMessage(navigator.resolveQName(value));
      }
      else if("wsdl-message-part-name".equals(localName))
      {
         retValueMapping.setWsdlMessagePartName(value);
      }
   }

   /**
    * Called when a new simple child element with text value was read from the XML content.
    */
   public void setValue(WsdlMessageMapping msgMapping,
                        UnmarshallingContext navigator,
                        String namespaceURI,
                        String localName,
                        String value)
   {
      log.trace("setValue: [obj=" + msgMapping + ",value=" + value + "]");
      if("wsdl-message".equals(localName))
      {
         msgMapping.setWsdlMessage(navigator.resolveQName(value));
      }
      else if("wsdl-message-part-name".equals(localName))
      {
         msgMapping.setWsdlMessagePartName(value);
      }
      else if("parameter-mode".equals(localName))
      {
         msgMapping.setParameterMode(value);
      }
      else if("soap-header".equals(localName))
      {
         msgMapping.setSoapHeader(true);
      }
   }
}
