/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb;

import java.net.URL;
import java.net.URLClassLoader;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.Permissions;

/**
 *   URLClassLoader that sets the specified permissions for the loaded classes
 *      
 *   @see <related>
 *   @author Rickard �berg (rickard.oberg@telkel.com)
 *   @version $Revision: 1.1 $
 */
public class EJBClassLoader
   extends URLClassLoader
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   Permissions perms;
   
   boolean secure; // True -> Enforce EJB restrictions
   
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   public EJBClassLoader(URL[] urls, ClassLoader parent, boolean secure)
   {
      super(urls, parent);
      
      perms = new Permissions();
      perms.add(new java.util.PropertyPermission("*","read"));
      perms.add(new java.lang.RuntimePermission("queuePrintJob"));
      perms.add(new java.net.SocketPermission("*","connect"));
      
      this.secure = secure;
   }
   
   // Public --------------------------------------------------------
   protected PermissionCollection getPermissions(CodeSource source)
   {
      if (secure)
         return perms;
      else
         return super.getPermissions(source);
   }
}

