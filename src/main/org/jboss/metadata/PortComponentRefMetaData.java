/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.metadata;

// $Id: PortComponentRefMetaData.java,v 1.4 2004/05/29 19:17:13 tdiesler Exp $

import org.jboss.deployment.DeploymentException;
import org.w3c.dom.Element;

import javax.xml.rpc.JAXRPCException;
import java.io.Serializable;

/** The metdata data from service-ref/port-component-ref element in web.xml, ejb-jar.xml, and application-client.xml.
 *
 * @author Thomas.Diesler@jboss.org
 * @version $Revision: 1.4 $
 */
public class PortComponentRefMetaData implements Serializable
{
   // The parent service-ref
   private ServiceRefMetaData serviceRefMetaData;

   // The required <service-endpoint-interface> element
   private String serviceEndpointInterface;
   // The optional <port-component-link> element
   private String portComponentLink;

   public PortComponentRefMetaData(ServiceRefMetaData serviceRefMetaData)
   {
      this.serviceRefMetaData = serviceRefMetaData;
   }

   public ServiceRefMetaData getServiceRefMetaData()
   {
      return serviceRefMetaData;
   }

   public String getPortComponentLink()
   {
      return portComponentLink;
   }

   public String getServiceEndpointInterface()
   {
      return serviceEndpointInterface;
   }

   public Class getServiceEndpointInterfaceClass()
   {
      try
      {
         ClassLoader cl = serviceRefMetaData.getResourceCL();
         return cl.loadClass(serviceEndpointInterface);
      }
      catch (ClassNotFoundException e)
      {
         throw new JAXRPCException("Cannot load service endpoint interface: " + serviceEndpointInterface);
      }
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
