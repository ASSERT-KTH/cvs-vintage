/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.metadata;


import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import org.jboss.deployment.DeploymentException;
import org.jboss.util.jmx.ObjectNameFactory;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/** The configuration information for invoker-proxy bindingss that may be tied to a EJB container.
 *   @author <a href="mailto:bill@jboss.org">Bill Burke</a>
 *   @version $Revision: 1.2 $
 *
 *  <p><b>Revisions:</b><br>
 *  <p><b>2002/04/21: billb</b>
 *  <ol>
 *   <li>initial version
 *  </ol>
 */
public class InvokerProxyBindingMetaData extends MetaData
{

   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------
   private String name;
   private String invokerName;
   private String connectorName;
   private String proxyFactory;
   private Element proxyFactoryConfig;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   public InvokerProxyBindingMetaData (String name)
   {
      this.name = name;
   }

   // Public --------------------------------------------------------

   public String getName() { return name; }

   public String getInvokerMBean() { return invokerName; }

   public String getConnectorMBean() { return connectorName; }

   public String getProxyFactory() { return proxyFactory; }

   public Element getProxyFactoryConfig() { return proxyFactoryConfig; }

   public void importJbossXml(Element element) throws DeploymentException
   {
      invokerName = getUniqueChildContent(element, "invoker-mbean");
      connectorName = getOptionalChildContent(element, "connector-mbean");
      proxyFactory = getUniqueChildContent(element, "proxy-factory");
      proxyFactoryConfig = getUniqueChild(element, "proxy-factory-config");
   }

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}
