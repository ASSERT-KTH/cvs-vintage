/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.deployment;

import java.awt.Component;
import java.beans.beancontext.BeanContextChildComponentProxy;

import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.dreambean.awt.GenericCustomizer;
   
/**
 *	<description> 
 *      
 *	@see <related>
 *	@author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 *	@version $Revision: 1.4 $
 */
public abstract class InstancePoolSupportConfiguration
   implements BeanContextChildComponentProxy
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   int minSize = 10;
   int maxSize = 100;
   
   Component customizer;
    
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   // Public --------------------------------------------------------
   public void setMinimumSize(int minSize) { this.minSize = minSize; }
   public int getMinimumSize() { return minSize; }
   
   public void setMaximumSize(int maxSize) { this.maxSize = maxSize; }
   public int getMaximumSize() { return maxSize; }
   
   // BeanContextChildComponentProxy implementation -----------------
   public Component getComponent()
   {
      if (customizer == null)
          customizer = new GenericCustomizer(this);
      return customizer;
   }

   // Package protected ---------------------------------------------
    
   // Protected -----------------------------------------------------
    
   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}

