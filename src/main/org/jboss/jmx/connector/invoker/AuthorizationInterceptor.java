/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.jmx.connector.invoker;

import org.jboss.mx.server.Invocation;
import org.jboss.mx.interceptor.AbstractInterceptor;
import org.jboss.mx.interceptor.Interceptor;

import org.jboss.security.RealmMapping;
import org.jboss.security.SimplePrincipal;

import javax.management.ObjectName;
import javax.naming.InitialContext;
import java.security.Principal;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.Collection;

/**
 * An Interceptor that aids in providing Authorization to JMX Invocations
 * at an MBean Operations level.
 *
 * @author <mailto:Anil.Saldhana@jboss.org>Anil Saldhana
 */
public class AuthorizationInterceptor extends AbstractInterceptor
{
   private String azclassname = null;

   public AuthorizationInterceptor()
   {
      super();
   }

   private RealmMapping realm;

   public void setSecurityDomain(String securityDomain)
   {
      try
      {
         InitialContext ctx = new InitialContext();
         realm = (RealmMapping) ctx.lookup(securityDomain);
      }
      catch (Exception e)
      {

      }

   }

   /**
    * The Authorizing class must have a method called
    * public Boolean authorize( Principal caller, String mbean,String opname )
    *
    * @param az
    */
   public void setAuthorizingClass(String az)
   {
      System.out.println("Authorizing Class=" + az);
      azclassname = az;
   }

   /**
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
            if (inv == null) System.out.println("Invocation is null");
            Object[] obj = inv.getArguments();
            ObjectName objname = (ObjectName) obj[0];
            String opname = (String) obj[1];

            boolean auth = false;
            try
            {
               auth = checkAuthorization(caller, objname.getCanonicalName(), opname);
            }
            catch (Exception e)
            {
               e.printStackTrace();
               auth = false;
            }

            if (auth == false)
            {
               String msg = "Failed to authorize principal=" + caller
                  + ",MBean=" + objname + ", Operation=" + opname;
               SecurityException ex = new SecurityException(msg);
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
    * @return whether the caller has authorization to do the operation
    * @throws Exception
    */
   private boolean checkAuthorization(Principal caller, String objname, String opname)
      throws Exception
   {
      if (realm == null)
         throw new SecurityException("Security Domain not defined for Authorization Interceptor");
      //Get the Role from the security domain
      Set roles = realm.getUserRoles(caller);
      boolean hasRole = realm.doesUserHaveRole(caller, roles);
      if (hasRole == false)
         throw new SecurityException("Caller not defined in the roles");

      //Get a collection of roles that are defined for this user
      Collection rolenames = new ArrayList();
      Iterator iter = null;
      if (roles != null && !roles.isEmpty()) iter = roles.iterator();
      while (iter.hasNext())
      {
         SimplePrincipal rolename = (SimplePrincipal) iter.next();
         rolenames.add(rolename.getName());
      }

      //We will try to use the authorizing class
      Class cl = null;
      try
      {
         cl = Thread.currentThread().getContextClassLoader().loadClass(azclassname);
      }
      catch (Exception e)
      {
         String msg = "Define your own class which has a method authorize with signature";
         msg += "public Boolean authorize( Principal caller, Collection roles,String objectname,String opname)";
         msg += ". And replace " + azclassname + " its name";
         throw new Exception(msg);
      }
      Object obj = cl.newInstance();
      Method method = cl.getMethod("authorize",
         new Class[]{Principal.class, Collection.class, String.class, String.class});
      Boolean valid = (Boolean) method.invoke(obj, new Object[]{caller, rolenames, objname, opname});

      return valid.booleanValue();
   }
}
