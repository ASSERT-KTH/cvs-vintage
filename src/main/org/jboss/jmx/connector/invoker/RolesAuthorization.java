/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.jmx.connector.invoker;

import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;
import java.security.Principal;
import java.security.acl.Group;

import javax.security.auth.Subject;

import org.jboss.security.SimplePrincipal;

/** A default authorization delegate used by the AuthorizationInterceptor. This
 * looks for a hard coded JBossAdmin role in the current authenticated Subject.
 * 
 * @author Scott.Stark@jboss.org
 * @version $Revision: 1.1 $
 */
public class RolesAuthorization
{
   private HashSet requiredRoles = new HashSet();

   public RolesAuthorization()
   {
      requiredRoles.add(new SimplePrincipal("JBossAdmin"));
   }
   public void setRequiredRoles(HashSet requiredRoles)
   {
      this.requiredRoles = requiredRoles;
   }
   public void authorize(Principal caller, Subject subject,
      String objectname, String opname)
   {
      Set groups = subject.getPrincipals(Group.class);
      Group roles = null;
      Iterator iter = groups.iterator();
      while( iter.hasNext() )
      {
         Group grp = (Group) iter.next();
         if( grp.getName().equals("Roles") )
         {
            roles = grp;
            break;
         }
      }
      if( roles == null )
      {
         throw new SecurityException("Subject has no Roles");
      }

      iter = requiredRoles.iterator();
      boolean hasRole = false;
      while( iter.hasNext() && hasRole == false )
      {
         Principal p = (Principal) iter.next();
         hasRole = roles.isMember(p);
      }
      if( hasRole == false )
      {
         throw new SecurityException("Authorization failure, requiredRoles="+requiredRoles
            +", callerRoles="+roles);
      }
   }
}
