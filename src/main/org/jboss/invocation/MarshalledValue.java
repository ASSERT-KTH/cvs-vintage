/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.invocation;

import java.io.DataOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Arrays;

/** A simple replacement for the RMI MarshalledObject that uses the thread
 context class loader for resolving classes and proxies. This currently does
 not support class annotations and dynamic class loading.

@author Scott.Stark@jboss.org
@version $Revision: 1.1 $
*/
public class MarshalledValue implements java.io.Externalizable
{
   // Constants -----------------------------------------------------
   
   /** Serial Version Identifier. */
   static final long serialVersionUID = -1527598981234110311L;

   /** The serialized form of the value. If <code>serializedForm</code> is
   * <code>null</code> then the object marshalled was a <code>null</code>
   * reference.
   */
   private byte[] serializedForm = null;
   /** The RMI MarshalledObject hash of the serializedForm array
    */
   private int hashCode;

   public MarshalledValue()
   {
   }

   public MarshalledValue(Object obj) throws IOException
   {
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      MarshalledValueOutputStream mvos = new MarshalledValueOutputStream(baos);
      mvos.writeObject(obj);
      mvos.flush();
      serializedForm = baos.toByteArray();

      // Use the java.rmi.MarshalledObject hash code calculation
      int hash = 0;
      for (int i = 0; i < serializedForm.length; i++)
      {
          hash = 31 * hash + serializedForm[i];
      }
      hashCode = hash;
   }

   public Object get() throws IOException, ClassNotFoundException
   {
      if( serializedForm == null )
         return null;

      ByteArrayInputStream bais = new ByteArrayInputStream(serializedForm);
      MarshalledValueInputStream mvis = new MarshalledValueInputStream(bais);
      return mvis.readObject();
   }

   /** Return a hash code for the serialized form of the value.
   *
   * @return the serialized form value hash.
   */
   public int hashCode()
   {
      return hashCode;
   }

   public boolean equals(Object obj)
   {
      if( this == obj )
         return true;

      boolean equals = false;
      if( obj instanceof MarshalledValue )
      {
         MarshalledValue mv = (MarshalledValue) obj;
         if( serializedForm == mv.serializedForm )
         {
            equals = true;
         }
         else
         {
            equals = Arrays.equals(serializedForm, mv.serializedForm);
         }
      }
      return equals;
   }
   
   /**
    * The object implements the readExternal method to restore its
    * contents by calling the methods of DataInput for primitive
    * types and readObject for objects, strings and arrays.  The
    * readExternal method must read the values in the same sequence
    * and with the same types as were written by writeExternal.
    *
    * @param in the stream to read data from in order to restore the object
    * @exception IOException if I/O errors occur
    * @exception ClassNotFoundException If the class for an object being
    *             restored cannot be found.
    */
   public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException
   {
      int length = in.readInt();
      serializedForm = null;
      if( length > 0 )
      {
         serializedForm = new byte[length];
         in.readFully(serializedForm);
      }
      hashCode = in.readInt();
   }

   /**
    * The object implements the writeExternal method to save its contents
    * by calling the methods of DataOutput for its primitive values or
    * calling the writeObject method of ObjectOutput for objects, strings,
    * and arrays.
    *
    * @serialData Overriding methods should use this tag to describe
    *            the data layout of this Externalizable object.
    *            List the sequence of element types and, if possible,
    *            relate the element to a public/protected field and/or
    *            method of this Externalizable class.
    *
    * @param out the stream to write the object to
    * @exception IOException Includes any I/O exceptions that may occur
    */
   public void writeExternal(ObjectOutput out) throws IOException
   {
      int length = serializedForm != null ? serializedForm.length : 0;
      out.writeInt(length);
      if( length > 0 )
      {
         out.write(serializedForm);
      }
      out.writeInt(hashCode);
   }

}
