/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.cmp.jdbc;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author <a href="alex@jboss.org">Alex Loubyansky and others</a>
 */
public class JDBCContext
{
   private Map data = new HashMap();

   public Object get(Object key)
   {
      return data.get(key);
   }

   public void put(Object key, Object value)
   {
      data.put(key, value);
   }

   public void remove(Object key)
   {
      data.remove(key);
   }
}
