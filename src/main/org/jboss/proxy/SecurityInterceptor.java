/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.proxy;

import org.jboss.invocation.Invocation;

import org.jboss.security.SecurityAssociation;
import org.jboss.security.SimplePrincipal;

/**
 * A interceptor which sets security related information on the
 * invocation.
 *      
 * @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
 * @version $Revision: 1.4 $
 *
 * <p><b>2001/11/21: marcf</b>
 * <ol>
 *   <li>Initial checkin
 * </ol>
 */
public class SecurityInterceptor
   extends Interceptor
{
   /** Serial Version Identifier. */
   // private static final long serialVersionUID = 432426690456622923L;
   
   /**
    * No-argument constructor for externalization.
    */
   public SecurityInterceptor()
   {
      super();
   }
   
   public Object invoke(Invocation invocation)
      throws Throwable
   {
      // Get Principal and credentials 
      invocation.setPrincipal(SecurityAssociation.getPrincipal()); 
      invocation.setCredential(SecurityAssociation.getCredential());
      
      return getNext().invoke(invocation);
   }
}
