/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.metadata;

// $Id: PortComponentRefMetaData.java,v 1.1 2004/05/06 16:14:11 tdiesler Exp $

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

/** The metdata data from service-ref/port-component-ref element in web.xml, ejb-jar.xml, and application-client.xml.
 *
 * @author Thomas.Diesler@jboss.org
 * @version $Revision: 1.1 $
 */
public class PortComponentRefMetaData
{
   // The required <service-endpoint-interface> element
   private String serviceEndpointInterface;
   // The optional <port-component-link> element
   private String portComponentLink;

   public String getPortComponentLink()
   {
      return portComponentLink;
   }

   public String getServiceEndpointInterface()
   {
      return serviceEndpointInterface;
   }

   public void importStandardXml(Element element)
           throws DeploymentException
   {
      serviceEndpointInterface = MetaData.getUniqueChildContent(element, "service-endpoint-interface");

      portComponentLink = MetaData.getOptionalChildContent(element, "port-component-link");
   }

   public void importJBossXml(Element element) throws DeploymentException
   {
   }
}
