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

/**
 * The Invocation Context
 *
 * <p>Describes the context in which this Invocation is being executed in 
 *    the interceptors
 *
 * <p>The heart of it is the payload map that can contain anything we then 
 *    put readers on them. The first "reader" is this "Invocation" object that
 *    can interpret the data in it. 
 * 
 * <p>Essentially we can carry ANYTHING from the client to the server, we 
 *    keep a series of redefined variables and method calls to get at the 
 *    pointers.  But really it is just a repository of objects. 
 *
 * <p>The generic getter and setter is really all that one needs to talk to 
 * this object.  We introduce typed getters and setters for convenience 
 *  and code readability in the codebase
 * @author  <a href="mailto:marc@jboss.org">Marc Fleury</a>
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version $Revision: 1.6 $
 */
public class InvocationContext
   implements java.io.Serializable
{
   // Context is a map
   private final Map context;

   public InvocationContext()
   {
      context = new HashMap();
   }
   
  
   /**
    * The generic store of variables
    */
   public void setValue(Object key, Object value)
   {
      context.put(key,value);
   }
   
   /**
    * Get a value from the stores.
    */
   public Object getValue(Object key) 
   { 
      return context.get(key);
   }
   
   /**
    * A container for server side association.
    */
   public void setObjectName(Object objectName)
   {
      context.put(InvocationKey.OBJECT_NAME, objectName);
   }
   
   public Object getObjectName()
   {
      return context.get(InvocationKey.OBJECT_NAME);
   }
   
   /**
    * Return the invocation target ID.  Can be used to identify a cached object.
    */
   public void setCacheId(Object id)
   {
      context.put(InvocationKey.CACHE_ID, id);
   }
   
   public Object getCacheId()
   {
      return context.get(InvocationKey.CACHE_ID);
   }
   
   public void setInvoker(Invoker invoker)
   {
      context.put(InvocationKey.INVOKER, invoker);
   }
   
   public Invoker getInvoker()
   {
      return (Invoker) context.get(InvocationKey.INVOKER);
   }
   
   public void setInvokerProxyBinding(String binding)
   {
      context.put(InvocationKey.INVOKER_PROXY_BINDING, binding);
   }
   
   public String getInvokerProxyBinding()
   {
      return (String) context.get(InvocationKey.INVOKER_PROXY_BINDING);
   }

   public void setMethodHashToTxSupportMap(Map methodHashToTxSupportMap)
   {
      context.put(InvocationKey.METHOD_TO_TX_SUPPORT_MAP, methodHashToTxSupportMap);
   }

   public Map getMethodHashToTxSupportMap()
   {
      return (Map) context.get(InvocationKey.METHOD_TO_TX_SUPPORT_MAP);
   }
}
