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

import org.jboss.logging.Logger;

/**
 * Return a LinkRef based on a ThreadLocal key.
 *     
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1.2 $
 */
public class ENCThreadLocalKey
   implements ObjectFactory
{
   private static final Logger log = Logger.getLogger(ENCThreadLocalKey.class);
   
   // We need all the weak maps to make sure everything is released properly
   // and we don't have any memory leaks

   private static ThreadLocal key = new ThreadLocal();
   private static InitialContext ctx;

   private static InitialContext getInitialContext() throws Exception
   {
      if (ctx == null) ctx = new InitialContext();
      return ctx;
   }

   public static void setKey(String tlkey)
   {
      key.set(tlkey);
   }
   public static String getKey()
   {
      return (String)key.get();
   }
   
   public Object getObjectInstance(Object obj,
                                   Name name,
                                   Context nameCtx,
                                   Hashtable environment)
      throws Exception
   {
      Reference ref = (Reference)obj;
      String reftype = (String)key.get();
      if (reftype == null)
      {
         log.debug("using default in ENC");
         reftype = "default";
      }

      RefAddr addr = ref.get(reftype);
      if (addr == null)
      {
         log.debug("using default in ENC");
         addr = ref.get("default"); // try to get default linking
      }
      if (addr != null)
      {
         log.debug("found Reference " + reftype + " with content " + (String)addr.getContent());
         InitialContext ctx = getInitialContext();
         return ctx.lookup((String)addr.getContent());
      }
      return null;
   }


}
