/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.jrmp.interfaces;

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

/**
 *  This Serializable object carries the method to invoke and an
 *  identifier for the target ojbect
 *
 *  @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 *  @author <a href="mailto:Richard.Monson-Haefel@jGuru.com">Richard Monson-Haefel</a>.
 *  @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>.
 *  @author <a href="mailto:docodan@nycap.rr.com">Daniel O'Connor</a>.
 *  @version $Revision: 1.15 $
 */
public final class RemoteMethodInvocation
   implements java.io.Externalizable
{
   // Constants -----------------------------------------------------

   /** Serial Version Identifier. */
   private static final long serialVersionUID = 6021873560918744612L;
    
   // Attributes ----------------------------------------------------
   Object id;
//   String className;
   long hash;
	
   Object[] args;

   private Object tpc; // Transaction propagation context.
   private Principal identity;
   private Object credential;
	
   transient Map methodMap;
   
   // Static --------------------------------------------------------
   static Map hashMap = new WeakHashMap();


	/**
	 * Calculate method hashes. This algo is taken from RMI.
	 *
	 * @param   intf  
	 * @return     
	 */
   public static Map getInterfaceHashes(Class intf)
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
            map.put(method, new Long(hash));
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
         methodHashes = getInterfaceHashes(method.getDeclaringClass());
         
         // Copy and add
         WeakHashMap newHashMap = new WeakHashMap();
         newHashMap.putAll(hashMap);
         newHashMap.put(method.getDeclaringClass(), methodHashes);
         hashMap = newHashMap;
      }
   
      return ((Long)methodHashes.get(method)).longValue();
   }
	
   // Constructors --------------------------------------------------
   public RemoteMethodInvocation()
   {
      // For externalization to work
   }
   
   public RemoteMethodInvocation(Method m, Object[] args)
   {
      this(null, m, args);
   }

   public RemoteMethodInvocation(Object id, Method m, Object[] args)
   {
      this.id = id;
      this.args = args;
      this.hash = calculateHash(m);
//      this.className = m.getDeclaringClass().getName();
   }
	
   // Public --------------------------------------------------------


   public Object getId() { return id; }

   public Method getMethod()
   {
      Method m = (Method)methodMap.get(new Long(hash));
      
      if (m == null)
         throw new NullPointerException("METHOD IS NOT FOUND:"+hash+" "+methodMap);
      
      return (Method)methodMap.get(new Long(hash));
   }

   public Object[] getArguments()
   {
      return args;
   }
	
   public void setMethodMap(Map methods)
   {
      methodMap = methods;
   }
	
   public void setTransactionPropagationContext(Object tpc)
   {
      this.tpc = tpc;
   }
	
   public Object getTransactionPropagationContext()
   {
      return tpc;
   }

   public void setPrincipal(Principal identity)
   {
      this.identity = identity;
   }

   public Principal getPrincipal()
   {
      return identity;
   }

   public Object getCredential()
   {
      return credential;
   }

   public void setCredential( Object credential )
   {
      this.credential = credential;
   }
	
   // Externalizable implementation ---------------------------------
   public void writeExternal(java.io.ObjectOutput out)
      throws IOException
   {
      out.writeObject(id);
      out.writeLong(hash);
      out.writeObject(args);

      out.writeObject(tpc);
      out.writeObject(identity);
      out.writeObject(credential);
   }
   
   public void readExternal(java.io.ObjectInput in)
      throws IOException, ClassNotFoundException
   {
      id = in.readObject();
      hash = in.readLong();
      args = (Object[])in.readObject();

      tpc = in.readObject();
      identity = (Principal)in.readObject();
      credential = in.readObject();
   }
}

