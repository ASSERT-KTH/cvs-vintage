/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.ejb.plugins.jms;

import java.security.PrivilegedAction;
import java.security.AccessController;

import org.jboss.security.SecurityAssociation;

/** The priviledged actions in used in this package
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 1.1 $
 */
class SecurityActions
{
   interface SubjectActions
   {
      SubjectActions PRIVILEGED = new SubjectActions()
      {
         private final PrivilegedAction clearAction = new PrivilegedAction()
         {
            public Object run()
            {
               SecurityAssociation.clear();
               return null;
            }
         };

         public void clear()
         {
            AccessController.doPrivileged(clearAction);
         }
      };

      SubjectActions NON_PRIVILEGED = new SubjectActions()
      {
         public void clear()
         {
            SecurityAssociation.clear();
         }
      };

      void clear();
   }

   static void clear()
   {
      if(System.getSecurityManager() == null)
      {
         SubjectActions.NON_PRIVILEGED.clear();
      }
      else
      {
         SubjectActions.PRIVILEGED.clear();
      }
   }
}
