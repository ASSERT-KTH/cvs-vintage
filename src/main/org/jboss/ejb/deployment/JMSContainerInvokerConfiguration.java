/*
 * jBoss, the OpenSource EJB server
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
 *	Based on the JRMPContainerInvokerConfiguration
 *      
 *	@see <related>
 *	@author Rickard Öberg (rickard.oberg@telkel.com)
 *      @author Peter Antman (peter.antman@tim.se)
 *	@version $Revision: 1.2 $
 */
public class JMSContainerInvokerConfiguration
   implements BeanContextChildComponentProxy
{
    // Constants -----------------------------------------------------
    
    // Attributes ----------------------------------------------------
    boolean optimize = false;
    int maxMessagesNr = 1;
    int maxSize = 15;
    Component customizer;
    String jMSProviderAdapterJNDI;
    String serverSessionPoolFactoryJNDI;
    
   // Static --------------------------------------------------------
   
   // Constructors --------------------------------------------------
   // Public --------------------------------------------------------
   public void setOptimized(boolean optimize)
   {
      this.optimize = optimize;
   }
   
   public boolean isOptimized()
   {
      return optimize;
   }
   
    public String getJMSProviderAdapterJNDI() {return jMSProviderAdapterJNDI;}
    
    public void setJMSProviderAdapterJNDI(String adapterJNDI) {this.jMSProviderAdapterJNDI = adapterJNDI;}
    
    public String getServerSessionPoolFactoryJNDI() {return serverSessionPoolFactoryJNDI;}
    
    public void setServerSessionPoolFactoryJNDI(String poolFactoryJNDI) {this.serverSessionPoolFactoryJNDI=poolFactoryJNDI;}

    public void setMaxMessages(int maxMessagesNr) { this.maxMessagesNr = maxMessagesNr; }
    public int getMaxMessages() { return maxMessagesNr; }
    
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
