/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins;

import java.lang.reflect.Method;
import java.security.Principal;
import java.util.Map;
import java.util.HashMap;
import java.util.Enumeration;

import javax.ejb.Handle;
import javax.ejb.HomeHandle;
import javax.ejb.EJBObject;
import javax.ejb.EJBMetaData;
import javax.ejb.CreateException;
import javax.ejb.FinderException;
import javax.ejb.RemoveException;

import org.jboss.ejb.Container;
import org.jboss.ejb.EnterpriseContext;
import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.ContainerInvoker;
import org.jboss.ejb.MethodInvocation;

import org.jboss.ejb.plugins.jrmp.interfaces.SecureSocketFactory;

import org.jboss.logging.Log;

/**
 *   <description> 
 *      
 *   @see <related>
 *   @author Rickard Öberg (rickard.oberg@telkel.com)
 *   @version $Revision: 1.3 $
 */
public class SecurityInterceptor
   extends AbstractInterceptor
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   protected Container container;
   
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------
   public void setContainer(Container container) 
   { 
   	this.container = container; 
   }

   public  Container getContainer()
   {
   	return container;
   }

   // Container implementation --------------------------------------
   public void start()
      throws Exception
   {
      super.start();
   }
   
   public Object invokeHome(MethodInvocation mi)
      throws Exception
   {
		// TODO security checks
	
      return getNext().invokeHome(mi);
   }

   /**
    *   This method does invocation interpositioning of tx and security, 
    *   retrieves the instance from an object table, and invokes the method
    *   on the particular instance
    *
    * @param   id  
    * @param   m  
    * @param   args  
    * @return     
    * @exception   Exception  
    */
   public Object invoke(MethodInvocation mi)
      throws Exception
   {
		// TODO security checks
      return getNext().invoke(mi);
   }
   
   // Private -------------------------------------------------------
}

