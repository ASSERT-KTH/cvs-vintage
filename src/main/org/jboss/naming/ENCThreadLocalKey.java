/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.naming;

import java.util.Hashtable;
import java.util.WeakHashMap;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.LinkRef;
import javax.naming.Reference;
import javax.naming.RefAddr;
import javax.naming.spi.ObjectFactory;

import org.jnp.server.NamingServer;
import org.jnp.interfaces.NamingContext;
import javax.naming.InitialContext;

/**
 *   Return a LinkRef based on a ThreadLocal key.
 *   
 *     
 *   @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 *   @version $Revision: 1.1 $
 */
public class ENCThreadLocalKey
   implements ObjectFactory
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   // We need all the weak maps to make sure everything is released properly
   // and we don't have any memory leaks

   private static ThreadLocal key = new ThreadLocal();
   private static InitialContext ctx;

   private static InitialContext getInitialContext() throws Exception
   {
      if (ctx == null) ctx = new InitialContext();
      return ctx;
   }
   // Static --------------------------------------------------------
   public static void setKey(String tlkey)
   {
      key.set(tlkey);
   }
   public static String getKey()
   {
      return (String)key.get();
   }
   
   

   // Constructors --------------------------------------------------

   // Public --------------------------------------------------------

   // ObjectFactory implementation ----------------------------------
   public Object getObjectInstance(Object obj, Name name, Context nameCtx,
      Hashtable environment)
      throws Exception
   {
      Reference ref = (Reference)obj;
      String reftype = (String)key.get();
      if (reftype == null)
      {
         System.out.println("using default in ENC");
         reftype = "default";
      }

      RefAddr addr = ref.get(reftype);
      if (addr == null)
      {
         System.out.println("using default in ENC");
         addr = ref.get("default"); // try to get default linking
      }
      if (addr != null)
      {
         System.out.println("-------- found Reference " + reftype + " with content " + (String)addr.getContent());
         InitialContext ctx = getInitialContext();
         return ctx.lookup((String)addr.getContent());
      }
      return null;
   }


}
