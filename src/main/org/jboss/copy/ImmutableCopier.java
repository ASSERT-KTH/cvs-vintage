/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 *
 */
package org.jboss.copy;

import java.util.IdentityHashMap;

/**
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 */
public final class ImmutableCopier implements Copier
{
   public final static ImmutableCopier COPIER = new ImmutableCopier();

   public Object copy(Object source, IdentityHashMap referenceMap)
   {
      // nothing returns nothing
      if(source == null)
      {
         return null;
      }
      
      // just put source->source in the reference map as it should be faster
      // then check if there and then insert
      referenceMap.put(source, source);

      // return souce... it is immutable after all
      return source;
   }
}

