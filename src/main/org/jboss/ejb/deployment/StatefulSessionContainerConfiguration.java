/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.deployment;

import org.w3c.dom.Element;

/**
 *   StatefulSessionContainerConfiguration
 *
 *   We hold the stateful specific configuration in this class.
 *   It is an empty class at this point.
 *      
 *   @see ContainerConfiguration
 *   @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 *   @version $Revision: 1.4 $
 */
public class StatefulSessionContainerConfiguration
   extends ContainerConfiguration
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
    
   // Public --------------------------------------------------------

   // XmlExternalizable implementation ------------------------------
   public void importXml(Element elt)
      throws Exception
   {
   	super.importXml(elt);
		
		// Set default pool
		setInstancePool("org.jboss.ejb.plugins.StatefulSessionInstancePool");
   }
   
   // Package protected ---------------------------------------------
    
   // Protected -----------------------------------------------------
    
   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}
