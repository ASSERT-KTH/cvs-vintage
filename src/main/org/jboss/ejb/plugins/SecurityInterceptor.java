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
import java.util.Iterator;
import java.util.Set;

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

import org.jboss.system.EJBSecurityManager;
import org.jboss.system.RealmMapping;

import com.dreambean.ejx.ejb.AssemblyDescriptor;


/**
 *   <description>
 *
 *   @see <related>
 *   @author Rickard Öberg (rickard.oberg@telkel.com)
 *   @version $Revision: 1.4 $
 */
public class SecurityInterceptor
   extends AbstractInterceptor
{
   // Constants -----------------------------------------------------

   // Attributes ----------------------------------------------------
   protected Container container;
   protected EJBSecurityManager securityManager;
   protected RealmMapping realmMapping;

   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------

   // Public --------------------------------------------------------
   public void setContainer(Container container)
   {
   	this.container = container;
    securityManager = container.getSecurityManager();
    realmMapping = container.getRealmMapping();
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
      if (!(mi.getPrincipal() == null)) // for now, security is optional
      {
        if (!securityManager.isValid( mi.getPrincipal(), mi.getCredential() ))
        {
          // should log illegal access
          throw new java.rmi.RemoteException("Authentication exception");
        }

        Set methodPermissions = container.getMethodPermissions( mi.getMethod(), true );
        if (!realmMapping.doesUserHaveRole( mi.getPrincipal(), methodPermissions ))
        {
          // should log illegal access
          throw new java.rmi.RemoteException("Illegal access exception");
        }
      }

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
      if (!(mi.getPrincipal() == null)) // for now, security is optional
      {
        if (!securityManager.isValid( mi.getPrincipal(), mi.getCredential() ))
        {
          // should log illegal access
          throw new java.rmi.RemoteException("Authentication exception");
        }

        Set methodPermissions = container.getMethodPermissions( mi.getMethod(), false );
        if (!realmMapping.doesUserHaveRole( mi.getPrincipal(), methodPermissions ))
        {
          // should log illegal access
          throw new java.rmi.RemoteException("Illegal access exception");
        }
      }

      return getNext().invoke(mi);
   }

   // Private -------------------------------------------------------
}

