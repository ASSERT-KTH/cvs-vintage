/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 *
 */
package org.jboss.copy;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.IdentityHashMap;

/**
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 */
public final class DeepCloneCopier implements Copier
{
   public final Method clone;

   public DeepCloneCopier(Class type)
   {
      try
      {
         clone = type.getMethod("clone", null);
      }
      catch(Exception e)
      {
         throw new CopyException("Class does not have a public clone method: "
               + type, 
               e);
      }
   }

   public Object copy(Object source, IdentityHashMap referenceMap)
   {
      // nothing returns nothing
      if(source == null)
      {
         return null;
      }
      
      // check the reference map first
      Object copy = referenceMap.get(source);
      if(copy != null)
      {
         return copy;
      }

      try
      {
         // use the copy constructor to copy the object
         copy = clone.invoke(source, null);

         // put the new copy in the reference map
         referenceMap.put(source, copy);
         
         return copy;
      }
      catch(InvocationTargetException e)
      {
         throw new CopyException("Exception in clone method", 
               e.getTargetException());
      }
      catch(Exception e)
      {
         throw new CopyException("Exception while invokeing clone method",
               e);
      }
   }
}

