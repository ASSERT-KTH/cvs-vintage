/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
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

import org.jboss.security.EJBSecurityManager;
import org.jboss.security.RealmMapping;
import org.jboss.security.SecurityAssociation;


/**
 *   <description>
 *
 *   @see <related>
 *   @author Rickard �berg (rickard.oberg@telkel.com)
 *   @author <a href="mailto:docodan@nycap.rr.com">Daniel O'Connor</a>.
 *   @version $Revision: 1.9 $
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

   private void checkSecurityAssociation( MethodInvocation mi, boolean home)
    throws Exception
   {
      // if this isn't ok, bean shouldn't deploy
      if (securityManager == null) {
          return;
      }
      if (realmMapping == null) {
          throw new java.rmi.RemoteException("checkSecurityAssociation", new SecurityException("Role mapping manager has not been set"));
      }

      Principal principal = SecurityAssociation.getPrincipal();
      Object credential = SecurityAssociation.getCredential();
      if (principal == null)
      {
         principal = mi.getPrincipal();
         credential = mi.getCredential();
         if (principal == null || !securityManager.isValid( principal, credential ))
         {
            // should log illegal access
            throw new java.rmi.RemoteException("checkSecurityAssociation", new SecurityException("Authentication exception"));
         }
         else
         {
            SecurityAssociation.setPrincipal( principal );
            SecurityAssociation.setCredential( credential );
         }
      }
      Set methodPermissions = container.getMethodPermissions( mi.getMethod(), home );

      if (methodPermissions != null && !realmMapping.doesUserHaveRole( principal, methodPermissions ))
      {
        // should log illegal access
        throw new java.rmi.RemoteException("checkSecurityAssociation", new SecurityException("Illegal access exception"));
      }
   }

   public Object invokeHome(MethodInvocation mi)
      throws Exception
   {
      checkSecurityAssociation( mi, true );
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
      checkSecurityAssociation( mi, false );
      return getNext().invoke(mi);
   }

   // Private -------------------------------------------------------
}

