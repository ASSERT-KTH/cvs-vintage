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
import java.util.Iterator;
import java.util.HashMap;

/**
 * Allows interceptors to communicate information back down the chain
 *   @see <related>
 *   @author  <a href="mailto:bill@jboss.org">Bill Burke</a>
 *   @version $Revision: 1.1 $
 *   Revisions:
 *
 *   <p><b>Revisions:</b>
 *
 */
public class InvocationResponse
   implements java.io.Externalizable
{
   // Constants -----------------------------------------------------
   
   /** Serial Version Identifier. */
   // REVISIT: need to generate serialVersionUID
   //static final long serialVersionUID = -718723094688127810L;
   
   // The Map of methods used by this Invocation
   protected HashMap contextInfo = null;
   protected Object response = null;

   // Constructors --------------------------------------------------
   public InvocationResponse()
   {
   }
   public InvocationResponse(Object obj)
   {
      if (obj instanceof InvocationResponse)
      {
         System.out.println("***********************");
         new Exception().printStackTrace();
         throw new RuntimeException("Stuffing an InvocationResponse within an InvocationResponse!!!!");
      }
      this.response = obj;
   }

   public Object getResponse() { return response; }
   public void setResponse(Object obj) 
   { 
      if (obj instanceof InvocationResponse)
      {
         System.out.println("***********************");
         new Exception().printStackTrace();
         throw new RuntimeException("Stuffing an InvocationResponse within an InvocationResponse!!!!");
      }
      response = obj; 
   }

   public void addAttachment(Object key, Object val)
   {
      if (contextInfo == null) contextInfo = new HashMap(1);
      contextInfo.put(key, val);
   }

   public Object getAttachment(Object key)
   {
      if (contextInfo == null) return null;
      return contextInfo.get(key);
   }
   
   // Externalizable implementation ---------------------------------
   public void writeExternal(java.io.ObjectOutput out)
   throws IOException
   {
      out.writeObject(response);
      if (contextInfo == null)
      {
         out.writeInt(0);
      }
      else
      {
         out.writeInt(contextInfo.size());
         Iterator keys = contextInfo.keySet().iterator();
         while (keys.hasNext())
         {
            Object currentKey = keys.next();
            out.writeObject(currentKey);
            out.writeObject(contextInfo.get(currentKey));
         }
      }
   }
   
   public void readExternal(java.io.ObjectInput in)
   throws IOException, ClassNotFoundException
   {
      response = in.readObject();

      // contextInfo
      int size = in.readInt();
      if (size == 0)
      {
         contextInfo = null;
      }
      else
      {
         contextInfo = new HashMap(size);
         for (int i = 0; i < size; i++)
         {
            Object key = in.readObject();
            Object value = in.readObject();
            contextInfo.put(key, value);
         }
      }
   }
}
