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
 *   This classloader is used to hold the java: JNDI-namespace root.
 *	  Each container has its own BCL. When a "java:" lookup is made
 *	  the JNDI-provider will use the root to lookup the values.
 *      
 *   @see org.jboss.naming.java.javaURLContextFactory
 *   @author Rickard Öberg (rickard.oberg@telkel.com)
 *   @version $Revision: 1.3 $
 */
public class BeanClassLoader
   extends URLClassLoader
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
	// This is the root of the "java:" JNDI-namespace
   Object jndiRoot;
   
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   public BeanClassLoader(ClassLoader parent)
   {
      super(new URL[0], parent);
   }
   
   // Public --------------------------------------------------------
   public void setJNDIRoot(Object root) 
	{ 
		this.jndiRoot = root; 
	}
	
   public Object getJNDIRoot() 
	{ 
		return jndiRoot; 
	}
}

