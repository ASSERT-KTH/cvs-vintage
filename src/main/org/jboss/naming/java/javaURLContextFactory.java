/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.naming.java;

import java.util.Hashtable;
import javax.naming.*;
import javax.naming.spi.*;

import org.jnp.interfaces.NamingContext;
import org.jnp.interfaces.Naming;

import org.jboss.ejb.BeanClassLoader;

/**
 *   Implementation of "java:" namespace factory. The context is associated
 *   with the thread, so the root context must be set before this is used in a thread
 *      
 *   @see <related>
 *   @author Rickard Öberg (rickard.oberg@telkel.com)
 *   @version $Revision: 1.1 $
 */
public class javaURLContextFactory
   implements ObjectFactory
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
    
   // Static --------------------------------------------------------
   public static void setRoot(Naming srv)
   {
      BeanClassLoader bcl = (BeanClassLoader)Thread.currentThread().getContextClassLoader();
      bcl.setJNDIRoot(srv);
   }
   
   public static Naming getRoot()
   {
      BeanClassLoader bcl = (BeanClassLoader)Thread.currentThread().getContextClassLoader();
      return (Naming)bcl.getJNDIRoot();
   }
   
   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------

   // ObjectFactory implementation ----------------------------------
   public Object getObjectInstance(Object obj,
                                Name name,
                                Context nameCtx,
                                Hashtable environment)
                         throws Exception
   {
      if (obj == null)
         return new NamingContext(environment, name, getRoot());
      else if (obj instanceof String)
      {
         String url = (String)obj;
         Context ctx = new NamingContext(environment, name, getRoot());
         
         Name n = ctx.getNameParser(name).parse(url.substring(url.indexOf(":")+1));
         if (n.size() >= 3)
         {
            // Provider URL?
            if (n.get(0).toString().equals("") &&
                n.get(1).toString().equals(""))
            {
               ctx.addToEnvironment(Context.PROVIDER_URL, n.get(2));
            }
         }
         return ctx;
      } else
      {
         return null;
      }
   }
    
   // Y overrides ---------------------------------------------------

   // Package protected ---------------------------------------------
    
   // Protected -----------------------------------------------------
    
   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------
}
