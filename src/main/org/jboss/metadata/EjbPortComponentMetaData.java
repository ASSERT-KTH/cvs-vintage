/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.metadata;

// $Id: EjbPortComponentMetaData.java,v 1.1 2004/06/19 20:41:58 starksm Exp $

import org.jboss.deployment.DeploymentException;
import org.w3c.dom.Element;

import javax.xml.rpc.JAXRPCException;
import java.io.Serializable;
import java.util.Properties;
import java.util.Iterator;

/** The metdata data for session/port-component element from jboss.xml
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 1.1 $
 */
public class EjbPortComponentMetaData implements Serializable
{
   private String portComponentName;
   private String portURI;
   private String authMethod;
   private String transportGuarantee;

   public String getPortComponentName()
   {
      return portComponentName;
   }
   public String getPortURI()
   {
      return portURI;
   }
   public String getAuthMethod()
   {
      return authMethod;
   }
   public String getTransportGuarantee()
   {
      return transportGuarantee;
   }

   public void importStandardXml(Element element)
           throws DeploymentException
   {
   }

   /** Parse the port-component contents
    * @param element
    * @throws DeploymentException
    */ 
   public void importJBossXml(Element element) throws DeploymentException
   {
      // port-component/port-component-name
      portComponentName = MetaData.getUniqueChildContent(element, "port-component-name");
      // port-component/port-uri?
      portURI = MetaData.getOptionalChildContent(element, "port-uri");
      if( portURI != null && portURI.charAt(0) != '/' )
         portURI = "/" + portURI;
      // port-component/auth-method?,
      authMethod = MetaData.getOptionalChildContent(element, "auth-method");
      // port-component/transport-guarantee?
      transportGuarantee = MetaData.getOptionalChildContent(element, "transport-guarantee");
   }
}
