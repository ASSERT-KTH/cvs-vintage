/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.deployment;

import java.awt.Component;
import java.beans.beancontext.BeanContextChildComponentProxy;

import com.dreambean.awt.GenericCustomizer;
   
/**
 *	<description> 
 *      
 *	@see <related>
 *	@author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 *	@version $Revision: 1.3 $
 */
public class SingletonStatelessSessionInstancePoolConfiguration
   implements BeanContextChildComponentProxy
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   boolean isSynchronized = true;
   
   Component customizer;
    
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   // Public --------------------------------------------------------
   public void setSynchronized(boolean s) { this.isSynchronized = s; }
   public boolean getSynchronized() { return isSynchronized; }
   
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

