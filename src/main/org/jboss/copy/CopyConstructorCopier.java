/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 *
 */
package org.jboss.copy;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.IdentityHashMap;

/**
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 */
public final class CopyConstructorCopier implements Copier
{
   private final Constructor constructor;

   public CopyConstructorCopier(Class type)
   {
      try
      {
         constructor = type.getConstructor(new Class[] {type});
      }
      catch(Exception e)
      {
         throw new CopyException("Class does not have a copy constructor: "
               + type.getName(), 
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
         copy = constructor.newInstance(new Object[] {source});
         
         // put the new copy in the reference map
         referenceMap.put(source, copy);

         return copy;
      }
      catch(InvocationTargetException e)
      {
         throw new CopyException("Exception in copy constructor", 
               e.getTargetException());
      }
      catch(Exception e)
      {
         throw new CopyException("Exception while invokeing copy constructor",
               e);
      }
   }
}

