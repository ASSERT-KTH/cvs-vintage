/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.invocation;

import java.util.Map;
import java.util.Iterator;
import java.util.HashMap;
import java.util.WeakHashMap;
import java.lang.reflect.Method;
import java.io.DataOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.rmi.MarshalledObject;


import java.security.Principal;
import java.security.MessageDigest;
import java.security.DigestOutputStream;

import javax.transaction.Transaction;

import org.jboss.invocation.Invocation;

/*
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.HashMap;

import java.security.Principal;
import java.security.MessageDigest;
import java.security.DigestOutputStream;

import javax.transaction.Transaction;
*/

/**
*
* The MarshalledInvocation is an invocation that travels.  As such it serializes its payload because of lack of ClassLoader visibility.
* As such it contains Marshalled data representing the byte[] of the Invocation object it extends
* Besides handling the specifics of "marshalling" the payload, which could be done at the Invocation level
* the Marshalled Invocation can hold optimization and needed code for distribution for example the 
* TransactionPropagationContext which is a serialization of the TX for distribution purposes as
* well as the "hash" for the methods that we send, as opposed to sending Method objects. 
* Serialization "optimizations" should be coded here in the externalization implementation of the class
*
*   @see <related>
*   @author  <a href="mailto:marc@jboss.org">Marc Fleury</a>
*   @version $Revision: 1.2 $
*   Revisions:
*
*   <p><b>Revisions:</b>
*
*   <p><b>2001120 marc fleury:</b>
*   <ul>
*   <li> Initial check-in
*   </ul>
*/
public class MarshalledInvocation
extends Invocation
implements java.io.Externalizable
{
   // Constants -----------------------------------------------------
   
   /** Serial Version Identifier. */
   // FIXME TODO 
   
   // The Transaction Propagation Context for distribution
   Object tpc;
   
   // The Map of methods used by this Invocation
   transient Map methodMap;
   
   
   // Static --------------------------------------------------------
   static Map hashMap = new WeakHashMap();
   
   
   /**
   * Calculate method hashes. This algo is taken from RMI.
   *
   * @param   intf  
   * @return     
   */
   public static Map getInterfaceHashes(Class intf, boolean methodToLong)
   {
      // Create method hashes
      Method[] methods = intf.getDeclaredMethods();
      HashMap map = new HashMap();
      for (int i = 0; i < methods.length; i++)
      {
         Method method = methods[i];
         Class[] parameterTypes = method.getParameterTypes();
         String methodDesc = method.getName()+"(";
         for(int j = 0; j < parameterTypes.length; j++)
         {
            methodDesc += getTypeString(parameterTypes[j]);
         }
         methodDesc += ")"+getTypeString(method.getReturnType());
         
         try
         {
            long hash = 0;
            ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream(512);
            MessageDigest messagedigest = MessageDigest.getInstance("SHA");
            DataOutputStream dataoutputstream = new DataOutputStream(new DigestOutputStream(bytearrayoutputstream, messagedigest));
            dataoutputstream.writeUTF(methodDesc);
            dataoutputstream.flush();
            byte abyte0[] = messagedigest.digest();
            for(int j = 0; j < Math.min(8, abyte0.length); j++)
               hash += (long)(abyte0[j] & 0xff) << j * 8;
            if (methodToLong)
               map.put(method, new Long(hash));
            else 
               map.put(new Long(hash), method);
               
         } catch (Exception e)
         {
            e.printStackTrace();
         }
      }
      
      return map;
   }
   
   static String getTypeString(Class cl)
   {
      if (cl == Byte.TYPE)
      {
         return "B";
      } else if (cl == Character.TYPE)
      {
         return "C";
      } else if (cl == Double.TYPE)
      {
         return "D";
      } else if (cl == Float.TYPE)
      {
         return "F";
      } else if (cl == Integer.TYPE)
      {
         return "I";
      } else if (cl == Long.TYPE)
      {
         return "J";
      } else if (cl == Short.TYPE)
      {
         return "S";
      } else if (cl == Boolean.TYPE)
      {
         return "Z";
      } else if (cl == Void.TYPE)
      {
         return "V";
      } else if (cl.isArray())
      {
         return "["+getTypeString(cl.getComponentType());
      } else
      {
         return "L"+cl.getName().replace('.','/')+";";
      }
   }
   
   /*
   * The use of hashCode is not enough to differenciate methods
   * we override the hashCode
   *
   * The hashes are cached in a static for efficiency
   * RO: WeakHashMap needed to support undeploy
   */
   public static long calculateHash(Method method)
   {
      Map methodHashes = (Map)hashMap.get(method.getDeclaringClass());
      
      if (methodHashes == null)
      {
         methodHashes = getInterfaceHashes(method.getDeclaringClass(), true);
         
         // Copy and add
         WeakHashMap newHashMap = new WeakHashMap();
         newHashMap.putAll(hashMap);
         newHashMap.put(method.getDeclaringClass(), methodHashes);
         hashMap = newHashMap;
      }
      
      return ((Long)methodHashes.get(method)).longValue();
   }
   
   // Constructors --------------------------------------------------
   public MarshalledInvocation()
   {
      // For externalization to work
   }
   
   public MarshalledInvocation(Map payload) 
   {   
      super(payload);
   }
   
   public MarshalledInvocation(
      Object id, 
      Method m, 
      Object[] args, 
      Transaction tx, 
      Principal identity, 
      Object credential)
   {
       super(id, m, args, tx, identity, credential);
   }
   // Public --------------------------------------------------------
   
   
   public Method getMethod()
   {
      Object value = getValue(METHOD);
      
      if (value instanceof Method) return (Method) value;
         
      else {
         
         // Try the hash, the methodMap should be set
         Method m = (Method)methodMap.get(value);
         
         // Keep it in the payload
         if (m != null)  
         {
            payload.put(METHOD, m);
            
            return m;
         }
         
         // This is a bug barf
         else 
         {
            throw new NullPointerException("METHOD IS NOT FOUND: "+value);
         }
      
      }
   }
   
   
   public void setMethodMap(Map methods) { methodMap = methods; }
   
   
   // The transaction propagation context for the Invocation that travels (distributed tx only)
   public void setTransactionPropagationContext(Object tpc) { this.tpc = tpc; }
   public Object getTransactionPropagationContext() { return tpc; }
   
   // Invocation overwrite -----------------------------------------
   
   // A Marshalled invocation has serialized data in the form of java.rmi.Marshalled 
   // We overwrite the "getValue" to deserialize the data, this assume that the thread context
   // class loader has visibility on the classes.
   public Object getValue(Object key) 
   { 
      Object value = payload.get(key);
      
      // The map may contain serialized values of the fields
      if (value instanceof java.rmi.MarshalledObject) { 
         
         try { return ((MarshalledObject) value).get(); }
            
         // Barf and return null
         catch (Exception e) { e.printStackTrace();return null; }
      }
      
      else return value;
   }
   
   // Externalizable implementation ---------------------------------
   public void writeExternal(java.io.ObjectOutput out)
   throws IOException
   {
      
      // Write the TPC, not the local transaction
      out.writeObject(tpc);
      payload.remove(TRANSACTION);
   
      // We write the hash instead of the method
      payload.put(METHOD, new Long(calculateHash((Method) payload.remove(METHOD))));
      
      // Everything else is possibly tied to classloaders that exist inside the server but not in 
      // the generic JMX land. 
      // they will travel in the  payload as Marshalled Object, see the Invocation getter logic
      
      // FIXME MARCF: we can put some optimizations in what we serialize and not, for example
      // classes that come from the JDK will be present at deserialization on the server side. 
      // So there is no need to marshal a "Principal" for example. 
      // This code could be "if class.getName().startsWith("java") then don't serialize. 
      // think about time, test these string manipulation to see if it is good (test method arguments
      // with native object and with extensions... compare)
      Iterator keys = payload.keySet().iterator();
      while (keys.hasNext()) {
         Object currentKey = keys.next();
         
         // This code could be if (object.getClass().getName().startsWith("java")) then don't serialize. 
         // Bench the above for speed.
         
         //Replace the current object with a Marshalled representation
         payload.put(currentKey, new MarshalledObject(payload.get(currentKey)));
      }
      
      // The map contains only serialized representations of every other object
      out.writeObject(payload);
   }
   
   public void readExternal(java.io.ObjectInput in)
   throws IOException, ClassNotFoundException
   {
      // Read TPC
      tpc = in.readObject();
      
      // The map contains only serialized representations of every other object
      payload = (Map) in.readObject();
   }
}
