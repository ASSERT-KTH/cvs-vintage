/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.jmx.connector.invoker;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.UndeclaredThrowableException;
import java.security.Principal;
import javax.management.ObjectName;
import javax.security.auth.Subject;

import org.jboss.mx.interceptor.AbstractInterceptor;
import org.jboss.mx.interceptor.Interceptor;
import org.jboss.mx.server.Invocation;

/**
 * An Interceptor that aids in providing Authorization to JMX Invocations
 * at an MBean Operations level. This must be placed after the
 * AuthenticationInterceptor to ensure a valid caller context exists
 *
 *          String msg = "Define your own class which has a method authorize with signature";
         msg += "public void authorize( Principal caller, Subject subject,
 String objectname,String opname)";
         msg += ". And replace " + azclassname + " its name";
 
 * @see AuthenticationInterceptor
 *
 * @author <mailto:Anil.Saldhana@jboss.org>Anil Saldhana
 * @author Scott.Stark@jboss.org
 * @version $Revision: 1.3 $
 */
public class AuthorizationInterceptor extends AbstractInterceptor
{
   private Object authenticator = null;
   private Method authorize;

   public AuthorizationInterceptor()
   {
      super();
      // Install the default
      try
      {
         setAuthorizingClass(RolesAuthorization.class);
      }
      catch(Exception e)
      {
         // Can't happen
      }
   }

   /**
    * The Authorizing class must have a method called
    * public Boolean authorize( Principal caller, String mbean,String opname )
    *
    * @param clazz
    */
   public void setAuthorizingClass(Class clazz)
      throws Exception
   {
      authenticator = clazz.newInstance();
      log.debug("Loaded authenticator: "+authenticator);
      Class[] sig = {Principal.class, Subject.class, String.class, String.class};
      authorize = clazz.getMethod("authorize", sig);
      log.debug("Found authorize(Principal, Subject, String, String)");
   }

   /**
    * Intercept the invoke(Invocation) operations 
    * @param invocation
    * @return
    * @throws Throwable
    */
   public Object invoke(Invocation invocation) throws Throwable
   {
      String type = invocation.getType();
      if (type == Invocation.OP_INVOKE)
      {
         String opName = invocation.getName();
         if (opName.equals("invoke"))
         {
            Object[] args = invocation.getArgs();
            org.jboss.invocation.Invocation inv = (org.jboss.invocation.Invocation) args[0];
            // Authenticate the caller based on the security association
            Principal caller = inv.getPrincipal();
            //Get the Method Name
            Object[] obj = inv.getArguments();
            ObjectName objname = (ObjectName) obj[0];
            String opname = (String) obj[1];

            try
            {
               checkAuthorization(caller, objname.getCanonicalName(), opname);
            }
            catch(SecurityException e)
            {
               throw e;
            }
            catch(Exception e)
            {
               String msg = "Failed to authorize principal=" + caller
                  + ",MBean=" + objname + ", Operation=" + opname;
               SecurityException ex = new SecurityException(msg);
               ex.initCause(e);
               throw ex;
            }
         }
      }

      Interceptor i = invocation.nextInterceptor();
      return i.invoke(invocation);
   }

   /**
    * Method that delegates authorization to the custom class
    *
    * @param caller
    * @param objname
    * @param opname
    * @throws Exception - A SecurityException on authorization failure
    */
   private void checkAuthorization(Principal caller, String objname, String opname)
      throws Exception
   {
      // Get the active Subject
      Subject subject = SecurityActions.getActiveSubject();
      if( subject == null )
         throw new SecurityException("No active Subject found, add th AuthenticationInterceptor");

      //We will try to use the authorizing class
      try
      {
         Object[] args = {caller, subject, objname, opname};
         authorize.invoke(authenticator, args);
      }
      catch(InvocationTargetException e)
      {
         Throwable t = e.getTargetException();
         if( t instanceof Exception )
            throw (Exception) t;
         else
            throw new UndeclaredThrowableException(t);
      }
   }
}
