/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.invocation;

import java.util.Map;
import java.util.HashMap;

import javax.transaction.TransactionManager;

import org.jboss.invocation.Invoker;

/*
import java.lang.reflect.Method;
import java.security.Principal;

import javax.transaction.Transaction;
*/

/**
 * The Invocation Context
 *
 * Describes the context in which this Invocation is being executed in the interceptors
 *
 * <p>The heart of it is the payload map that can contain anything we then put readers on them
 *    The first "reader" is this "Invocation" object that can interpret the data in it. 
 * 
 * <p>Essentially we can carry ANYTHING from the client to the server, we keep a series of 
 *    of predifined variables and method calls to get at the pointers.  But really it is just 
 *    a repository of objects. 
 *
 * @author  <a href="mailto:marc@jboss.org">Marc Fleury</a>
 * @version $Revision: 1.3 $
 * 
 * Revisions:
 *
 * <p><b>Revisions:</b>
 *
 * <p><b>2001114 marc fleury:</b>
 * <ul>
 *    <li>Initial check-in
 * </ul>
 */
public class InvocationContext
implements java.io.Serializable
{

   // Context is a map
   public Map context;

   /**
    * We are using the generic payload to store some of our data, we define some integer entries.
    * These are just some variables that we define for use in "typed" getters and setters. 
    * One can define anything either in here explicitely or through the use of external calls to getValue
    */
   public static final Integer
      // We keep these around in case one wants to cache the data at the context level
      PRINCIPAL = new Integer(new String("PRINCIPAL").hashCode()),
      CREDENTIAL = new Integer(new String("CREDENTIAL").hashCode()),
      // We can keep a reference to an abstract "container" 
      OBJECT_NAME = new Integer(new String("OBJECT_NAME").hashCode()),
      // The Cache-ID associates an instance in cache somewhere on the server with this invocation
      CACHE_ID = new Integer(new String("CACHE_ID").hashCode()),
      // The invoker-proxy binding name
      INVOKER_PROXY_BINDING = new Integer(new String("INVOKER_PROXY_BINDING").hashCode()),
      // The invoker
      INVOKER = new Integer(new String("INVOKER").hashCode());
      
   /**
    * Exposed for externalization only.
    */
   public InvocationContext() { context = new HashMap();}
   
   /**
    * Invocation creation
    */
   public InvocationContext(Map context) { this.context = context;}
      
   // 
   // The generic getter and setter is really all that one needs to talk to this object
   // We introduce typed getters and setters for convenience and code readability in the codebase
   //
   
   //The generic store of variables
   public void setValue(Object key, Object value) { context.put(key,value); }
   
   // Get a value from the stores 
   public Object getValue(Object key) 
   { 
      return context.get(key);
   }
   
   // A container for server side association
   public void setObjectName(Object objectName) { context.put(OBJECT_NAME, objectName);}
   public Object getObjectName() { return context.get(OBJECT_NAME);}
   
   // Return the invocation target ID.  Can be used to identify a cached object
   public void setCacheId(Object id) { context.put(CACHE_ID, id);}
   public Object getCacheId() { return context.get(CACHE_ID);}
   
   
   public void setInvoker(Invoker invoker) { context.put(INVOKER, invoker);}
   public Invoker getInvoker() { return (Invoker) context.get(INVOKER);}
   
   public void setInvokerProxyBinding(String binding) { context.put(INVOKER_PROXY_BINDING, binding);}
   public String getInvokerProxyBinding() { return (String) context.get(INVOKER_PROXY_BINDING);}
   
}
