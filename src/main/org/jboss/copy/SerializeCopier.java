/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 *
 */
package org.jboss.copy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.IdentityHashMap;

import org.jboss.invocation.MarshalledValueInputStream;
import org.jboss.invocation.MarshalledValueOutputStream;

/**
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 */
public final class SerializeCopier implements Copier
{
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
         // write the object out
         ByteArrayOutputStream bOut = new ByteArrayOutputStream();
         MarshalledValueOutputStream mvOut = 
            new MarshalledValueOutputStream(bOut);
         mvOut.writeObject(source);
         mvOut.flush();

         // read it back in
         ByteArrayInputStream bIn = 
            new ByteArrayInputStream(bOut.toByteArray());
         MarshalledValueInputStream mIn = new MarshalledValueInputStream(bIn);
         copy =  mIn.readObject();

         // put the new copy in the reference map
         referenceMap.put(source, copy);

         return copy;
      } 
      catch(Exception e)
      {
         throw new CopyException(
               "Exception occured while serializing object: " + source, 
               e);
      }
   }
}

