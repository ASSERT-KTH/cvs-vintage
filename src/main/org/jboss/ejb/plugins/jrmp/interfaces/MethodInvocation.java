/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins.jrmp.interfaces;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;

/**
 *	<description> 
 *      
 *	@see <related>
 *	@author Rickard Öberg (rickard.oberg@telkel.com)
 *	@version $Revision: 1.3 $
 */
public class MethodInvocation
   implements java.io.Serializable
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   Object id;
   String className;
   int hash = -9999;
   Object[] args;
   
   // Static --------------------------------------------------------
   static HashMap clazzMap = new HashMap();
   
   // Constructors --------------------------------------------------
   public MethodInvocation(Method m, Object[] args)
   {
      this(null, m, args);
   }
   
   public MethodInvocation(Object id, Method m, Object[] args)
   {
      this.id = id;
      this.className = m.getDeclaringClass().getName();
      this.hash = m.hashCode();
      this.args = args;
   }
   // Public --------------------------------------------------------
   
   
   public Object getId() { return id; }
   
   public Method getMethod() 
      throws NoSuchMethodException, ClassNotFoundException
   { 
      HashMap methodMap;
      Class clazz = Thread.currentThread().getContextClassLoader().loadClass(className);
      synchronized(clazzMap)
      {
         methodMap = (HashMap)clazzMap.get(clazz);
         
         if (methodMap == null)
         {
            // Create method mapping
            Method[] methods = clazz.getMethods();
            methodMap = new HashMap();
            for (int i = 0; i < methods.length; i++)
            {
               methodMap.put(new Integer(methods[i].hashCode()), methods[i]);
            }
            clazzMap.put(clazz, methodMap);
         }
      }
      
      synchronized(methodMap)
      {
         // Get method based on its hash value
         Method m = (Method)methodMap.get(new Integer(hash));
         if (m == null)
            throw new NoSuchMethodException(clazz+":"+hash);
         return m;
      }
   }
   
   public Object[] getArguments() 
      throws IOException, ClassNotFoundException
   { 
      return args;
   }
   
   // Package protected ---------------------------------------------
    
   // Protected -----------------------------------------------------
    
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
}

