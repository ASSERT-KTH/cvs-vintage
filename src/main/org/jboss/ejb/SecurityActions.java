package org.jboss.ejb;

import java.security.PrivilegedAction;
import java.security.AccessController;
import javax.security.jacc.PolicyContext;

import org.jboss.security.RunAsIdentity;
import org.jboss.security.SecurityAssociation;

/** A collection of privileged actions for this package
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revison:$
 */
public class SecurityActions
{
   private static class GetTCLAction implements PrivilegedAction
   {
      static PrivilegedAction ACTION = new GetTCLAction();
      public Object run()
      {
         ClassLoader loader = Thread.currentThread().getContextClassLoader();
         return loader;
      }
   }
   private static class SetTCLAction implements PrivilegedAction
   {
      ClassLoader loader;
      SetTCLAction(ClassLoader loader)
      {
         this.loader = loader;
      }
      public Object run()
      {
         Thread.currentThread().setContextClassLoader(loader);
         loader = null;
         return null;
      }
   }

   private static class SetContextID implements PrivilegedAction
   {
      String contextID;
      SetContextID(String contextID)
      {
         this.contextID = contextID;
      }
      public Object run()
      {
         String previousID = PolicyContext.getContextID();
         PolicyContext.setContextID(contextID);
         return previousID;
      }
   }

   private static class PeekRunAsRoleAction implements PrivilegedAction
   {
      int depth;
      PeekRunAsRoleAction(int depth)
      {
         this.depth = depth;
      }
      public Object run()
      {
         RunAsIdentity principal = SecurityAssociation.peekRunAsIdentity(depth);
         return principal;
      }
   }

   static ClassLoader getContextClassLoader()
   {
      ClassLoader loader = (ClassLoader) AccessController.doPrivileged(GetTCLAction.ACTION);
      return loader;
   }
   static void setContextClassLoader(ClassLoader loader)
   {
      PrivilegedAction action = new SetTCLAction(loader);
      AccessController.doPrivileged(action);
   }
   static String setContextID(String contextID)
   {
      PrivilegedAction action = new SetContextID(contextID);
      String previousID = (String) AccessController.doPrivileged(action);
      return previousID;
   }
   static RunAsIdentity peekRunAsIdentity(int depth)
   {
      PrivilegedAction action = new PeekRunAsRoleAction(depth);
      RunAsIdentity principal = (RunAsIdentity) AccessController.doPrivileged(action);
      return principal;
   }

}
