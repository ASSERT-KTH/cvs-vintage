/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.metadata;

import org.w3c.dom.Element;

import org.jboss.deployment.DeploymentException;

/** 
 * Message Destination Metadata
 * 
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>.
 * @version $Revision: 1.1 $
 */
public class MessageDestinationMetaData extends MetaData
{
   // Constants -----------------------------------------------------
   
   // Attributes ----------------------------------------------------

   /** The destination name */
   private String name;
   
   /** The jndi name */
   private String jndiName;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   public MessageDestinationMetaData()
   {
   }

   // Public --------------------------------------------------------

   public String getName()
   {
      return name;
   }

   public String getJNDIName()
   {
      return jndiName;
   }

   public void importEjbJarXml(Element element) throws DeploymentException
   {
      name = getElementContent(getUniqueChild(element, "message-destination-name"));
   }

   public void importJbossXml(Element element) throws DeploymentException
   {
      jndiName = getElementContent(getUniqueChild(element, "jndi-name"));
   }

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}
