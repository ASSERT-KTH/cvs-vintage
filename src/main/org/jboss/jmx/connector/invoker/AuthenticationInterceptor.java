/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.jmx.connector.invoker;

import java.security.Principal;
import javax.naming.InitialContext;

import org.jboss.mx.server.Invocation;
import org.jboss.mx.interceptor.AbstractInterceptor;
import org.jboss.mx.interceptor.Interceptor;
import org.jboss.security.SubjectSecurityManager;


/** A security interceptor that requires an authorized user for invoke(Invocation)
 * operation calls when the SecurityDomain and SecurityMgr attributes are
 * specified. Access to attributes and the MBeanInfo are not intercepted.
 *
 * @see Interceptor
 *
 * @author <a href="mailto:juha@jboss.org">Juha Lindfors</a>.
 * @author Scott.Stark@jboss.org
 * @version $Revision: 1.3 $
 *   
 */
public final class AuthenticationInterceptor
   extends AbstractInterceptor
{
   private SubjectSecurityManager securityMgr;

   public void setSecurityDomain(String securityDomain)
   {
      try
      {
         InitialContext ctx = new InitialContext();
         securityMgr = (SubjectSecurityManager) ctx.lookup(securityDomain);
      }
      catch(Exception e)
      {
         
      }
      
   }

   /**
    * 
    * @param invocation
    * @return
    * @throws InvocationException
    */ 
   public Object invoke(Invocation invocation) throws Throwable
   {
      String type = invocation.getType();
      if( type == Invocation.OP_INVOKE && securityMgr != null )
      {
         String opName = invocation.getName();
         if( opName.equals("invoke") )
         {
            Object[] args = invocation.getArgs();
            org.jboss.invocation.Invocation inv = (org.jboss.invocation.Invocation) args[0];
            // Authenticate the caller based on the security association
            Principal caller = inv.getPrincipal();
            Object credential = inv.getCredential();
            boolean isValid = securityMgr.isValid(caller, credential);
            if( isValid == false )
            {
               String msg = "Failed to authenticate principal="+caller
                  +", securityDomain="+securityMgr.getSecurityDomain();
               throw new SecurityException(msg);
            }
         }
      }

      Interceptor i = invocation.nextInterceptor();
      return i.invoke(invocation);
   }
}
