/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.proxy;

import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectInput;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.lang.reflect.Method;

import javax.ejb.EJBHome;
import javax.ejb.EJBMetaData;
import javax.ejb.RemoveException;
import javax.ejb.Handle;
import javax.ejb.EJBHome;
import javax.ejb.EJBObject;
import javax.ejb.HomeHandle;

import org.jboss.ejb.CacheKey;
import org.jboss.invocation.Invoker;
import org.jboss.proxy.ejb.handle.HomeHandleImpl;
import org.jboss.invocation.Invocation;

import org.jboss.security.SecurityAssociation;
import org.jboss.security.SimplePrincipal;
/*
import javax.naming.Name;
import org.jboss.ejb.plugins.jrmp.server.JRMPContainerInvoker;
*/
/**
* The client-side proxy for an EJB Home object.
*      
* @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
* @version $Revision: 1.1 $
*
* <p><b>2001/11/21: marcf</b>
* <ol>
*   <li>Initial checkin
* </ol>
*/
public class SecurityInterceptor
   extends Interceptor
{
   // Constants -----------------------------------------------------

   /** Serial Version Identifier. */
//   private static final long serialVersionUID = 432426690456622923L;
   
   // Static --------------------------------------------------------
   
   // Attributes ----------------------------------------------------
   
   // Constructors --------------------------------------------------
   
   /**
   * No-argument constructor for externalization.
   */
   public SecurityInterceptor() {}
   
 
   // Public --------------------------------------------------------
   
   public Object invoke(Invocation invocation)
   throws Throwable
   {
      // Get Principal and credentials 
      invocation.setPrincipal( SecurityAssociation.getPrincipal()); 
   
      invocation.setCredential(SecurityAssociation.getCredential());
      
      return getNext().invoke(invocation);
   }
}
