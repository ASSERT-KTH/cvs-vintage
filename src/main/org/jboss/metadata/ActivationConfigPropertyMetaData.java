/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.metadata;

import org.jboss.deployment.DeploymentException;
import org.jboss.util.Strings;
import org.w3c.dom.Element;

/**
 * Parse the activation-config-property element used in message driven bean.
 * It is a name/value pair
 * 
 * @author <a href="mailto:adrian@jboss.com">Adrian Brock</a>.
 * @version $Revision: 1.2 $
 */
public class ActivationConfigPropertyMetaData extends MetaData
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------

   /** The property name */
   private String name;

   /** The property value */
   private String value;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   /**
    * Create a new Activation Config Property MetaData object
    */
   public ActivationConfigPropertyMetaData()
   {
   }

   /**
    * Create a new Activation Config Property MetaData object
    *
    * @param name the name
    * @param value the value 
    */
   public ActivationConfigPropertyMetaData(String name, String value)
   {
      this.name = name;
      this.value = value;
   }
   
   // Public --------------------------------------------------------

   /**
    * Retrieve the property name
    */
   public String getName()
   {
      return name;
   }

   /**
    * Retrieve the property value
    */
   public String getValue()
   {
      return value;
   }

   public void importXml(Element element) throws DeploymentException
   {
      name = getElementContent(getUniqueChild(element, "activation-config-property-name"));
      value = getElementContent(getUniqueChild(element, "activation-config-property-value"));
      if (name == null || name.trim().length() == 0)
         throw new DeploymentException("activation-config-property doesn't have a name");
      if (Strings.isValidJavaIdentifier(name) == false)
         throw new DeploymentException("activation-config-property '" + name + "' is not a valid java identifier");
   }

   // Object overrides ----------------------------------------------
   
   public String toString()
   {
      return "ActivationConfigProperty(" + name + "=" + value + ")";
   }

   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}
