/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.metadata;

// $Id: EjbPortComponentMetaData.java,v 1.3 2005/03/18 21:40:24 andd Exp $

import java.util.StringTokenizer;

import org.jboss.deployment.DeploymentException;
import org.w3c.dom.Element;

/** The metdata data for session/port-component element from jboss.xml
 *
 * @author Scott.Stark@jboss.org
 * @version $Revision: 1.3 $
 */
public class EjbPortComponentMetaData
{
   private SessionMetaData sessionMetaData;

   private String portComponentName;
   private String portComponentURI;
   private String authMethod;
   private String transportGuarantee;

   public EjbPortComponentMetaData(SessionMetaData sessionMetaData)
   {
      this.sessionMetaData = sessionMetaData;
   }

   public String getPortComponentName()
   {
      return portComponentName;
   }

   public String getPortComponentURI()
   {
      return portComponentURI;
   }

   public String getURLPattern()
   {
      String pattern = "/*";
      if (portComponentURI != null)
      {
         // URI: /context-root/url-pattern
         int slashIndex = portComponentURI.indexOf('/', 1);
         pattern = portComponentURI.substring(slashIndex);
      }
      return pattern;
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
      // port-component/port-component-uri?
      portComponentURI = MetaData.getOptionalChildContent(element, "port-component-uri");
      if (portComponentURI != null)
      {
         if (portComponentURI.charAt(0) != '/')
            portComponentURI = "/" + portComponentURI;

         // The first token is the webservice context root
         StringTokenizer st = new StringTokenizer(portComponentURI, "/");
         if (st.countTokens() < 2)
            throw new DeploymentException("Expected at least two tokens <port-component-uri>");

         String contextRoot = "/" + st.nextToken();
         String prevContextRoot = sessionMetaData.getApplicationMetaData().getWebServiceContextRoot();
         if (prevContextRoot != null && prevContextRoot.equals(contextRoot) == false)
            throw new DeploymentException("Invalid <port-component-uri>, expected to start with: " + prevContextRoot);

         sessionMetaData.getApplicationMetaData().setWebServiceContextRoot(contextRoot);
      }
      // port-component/auth-method?,
      authMethod = MetaData.getOptionalChildContent(element, "auth-method");
      // port-component/transport-guarantee?
      transportGuarantee = MetaData.getOptionalChildContent(element, "transport-guarantee");

      // Deprecated in jboss-4.0.1
      if (MetaData.getOptionalChildContent(element, "port-uri") != null)
         throw new DeploymentException("Deprecated element <port-uri>, use <port-component-uri> instead");
   }
}
