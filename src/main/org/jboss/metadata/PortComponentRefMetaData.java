/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.metadata;

// $Id: PortComponentRefMetaData.java,v 1.2 2004/05/07 14:58:49 tdiesler Exp $

import org.jboss.deployment.DeploymentException;
import org.w3c.dom.Element;

import java.io.Serializable;

/** The metdata data from service-ref/port-component-ref element in web.xml, ejb-jar.xml, and application-client.xml.
 *
 * @author Thomas.Diesler@jboss.org
 * @version $Revision: 1.2 $
 */
public class PortComponentRefMetaData implements Serializable
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
