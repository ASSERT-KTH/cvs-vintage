/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.invocation;

import java.util.Map;
import java.util.HashMap;

import java.lang.reflect.Method;

import java.security.Principal;

import javax.transaction.Transaction;

/**
 * The Invocation object is the generic object flowing through our interceptors.
 *
 * <p>The heart of it is the payload map that can contain anything we then 
 *    put readers on them.  The first <em>reader</em> is this 
 *    <em>Invocation</em> object that can interpret the data in it. 
 * 
 * <p>Essentially we can carry ANYTHING from the client to the server, we keep
 *    a series of of predifined variables and method calls to get at the 
 *    pointers.  But really it is just  a repository of objects. 
 *
 * @author  <a href="mailto:marc@jboss.org">Marc Fleury</a>
 * @version $Revision: 1.10 $
 */
public class Invocation
{
   /** The signature of the invoke() method */
   public static final String[] INVOKE_SIGNATURE = { "java.lang.Object" };
   
   // The payload is a repository of everything associated with the invocation
   // It is information that will need to travel 

   /** 
    * Contextual information to the invocation that is not part of the payload. 
    */
   public Map transient_payload = new HashMap();

   /**
    * as_is classes that will not be marshalled by the invocation
    * (java.* and javax.* or anything in system classpath is OK)
    */
   public Map as_is_payload = new HashMap();

   /** Payload will be marshalled for type hiding at the RMI layers. */
   public Map payload = new HashMap();
   
   // The variables used to indicate what type of data and where to put it.

   //
   // We are using the generic payload to store some of our data, we define 
   // some integer entries. These are just some variables that we define for 
   // use in "typed" getters and setters. One can define anything either in
   // here explicitely or through the use of external calls to getValue
   //

   /**
    * No-args constructor exposed for externalization only.
    */
   public Invocation() 
   {
      super();
   }
   
   /**
    * Invocation creation
    */
   public Invocation(final Map payload) 
   {   
      // The generic payload
      this.payload = payload; 
   }
   
   public Invocation(
      Object id, 
      Method m, 
      Object[] args, 
      Transaction tx, 
      Principal identity, 
      Object credential)
   {
      this.payload = new HashMap();
      this.as_is_payload = new HashMap();
      this.transient_payload = new HashMap();
      
      setId(id);
      setMethod(m);
      setArguments(args);    
      setTransaction(tx);
      setPrincipal(identity);
      setCredential(credential);
   }
   
   /**
    * The generic store of variables.
    *
    * <p>
    *    The generic getter and setter is really all that one needs to talk 
    *    to this object. We introduce typed getters and setters for 
    *    convenience and code readability in the codeba
    */
   public void setValue(Object key, Object value)
   {
      setValue(key, value, PayloadKey.PAYLOAD);
   }
   
   /**
    * Advanced store
    * Here you can pass a TYPE that indicates where to put the value.
    * TRANSIENT: the value is put in a map that WON'T be passed 
    * AS_IS: no need to marshall the value when passed (use for all JDK 
    *    java types)
    * PAYLOAD: we need to marshall the value as its type is application specific
    */
   public void setValue(Object key, Object value, PayloadKey type) 
   {
      if(type == PayloadKey.TRANSIENT) 
      {
          transient_payload.put(key,value);
      }
      else if(type == PayloadKey.AS_IS)
      {
          as_is_payload.put(key,value);
      }
      else if(type == PayloadKey.PAYLOAD)
      {
          payload.put(key,value);
      }
      else 
      {
         throw new IllegalArgumentException("Unknown PayloadKey: " + type);
      }
   }
 
   /**
    * Get a value from the stores.
    */
   public Object getValue(Object key) 
   { 
      // find where it is
      if (payload.containsKey(key))
      {
         return payload.get(key);
      }
      else if (as_is_payload.containsKey(key))
      {
         return as_is_payload.get(key);
      }
      else if (transient_payload.containsKey(key))
      {
         return transient_payload.get(key);
      }
      
      return null;
   }
   
   //
   // Convenience typed getters, use pre-declared keys in the store, 
   // but it all comes back to the payload, here you see the usage of the 
   // different payloads.  Anything that has a well defined type can go in as_is
   // Anything that is arbitrary and depends on the application needs to go in 
   // in the serialized payload.  The "Transaction" is known, the type of the 
   // method arguments are not for example and are part of the EJB jar.
   //
   
   /**
    * set the transaction.
    */
   public void setTransaction(Transaction tx)
   {
      as_is_payload.put(InvocationKey.TRANSACTION, tx);
   }
   
   /**
    * get the transaction.
    */
   public Transaction getTransaction()
   {
      return (Transaction) getValue(InvocationKey.TRANSACTION);
   }
   
   /**
    * Change the security identity of this invocation.
    */
   public void setPrincipal(Principal principal)
   {
      as_is_payload.put(InvocationKey.PRINCIPAL, principal);
   }
   
   public Principal getPrincipal()
   {
      return (Principal) getValue(InvocationKey.PRINCIPAL);
   }
   
   /**
    * Change the security credentials of this invocation.
    */
   public void setCredential(Object credential)
   {
      payload.put(InvocationKey.CREDENTIAL, credential);
   }
   
   public Object getCredential()
   {
      return getValue(InvocationKey.CREDENTIAL);
   }
   
   /**
    * container for server side association.
    */
   public void setObjectName(Object objectName)
   {
      payload.put(InvocationKey.OBJECT_NAME, objectName);
   }
   
   public Object getObjectName()
   {
      return getValue(InvocationKey.OBJECT_NAME);
   }
   
   /**
    * An arbitrary type.
    */
   public void setType(InvocationType type)
   {
      as_is_payload.put(InvocationKey.TYPE, type);
   }
   
   public InvocationType getType()
   {
      InvocationType type = InvocationType.LOCAL;
      InvocationType invType = (InvocationType) getValue(InvocationKey.TYPE);
      if( invType != null )
         type = invType;
      return type;
   } 

   /**
    * Return the invocation target ID.  Can be used to identify a cached object
    */
   public void setId(Object id)
   {
      payload.put(InvocationKey.CACHE_ID, id);
   }
   
   public Object getId()
   {
      return getValue(InvocationKey.CACHE_ID);
   }
   
   /**
    * set on method Return the invocation method.
    */
   public void setMethod(Method method)
   {
      payload.put(InvocationKey.METHOD, method);
   }
   
   /**
    * get on method Return the invocation method.
    */
   public Method getMethod()
   {
      return (Method) getValue(InvocationKey.METHOD);
   }
   
   /**
    * A list of arguments for the method.
    */
   public void setArguments(Object[] arguments)
   {
      payload.put(InvocationKey.ARGUMENTS, arguments);
   }
   
   public Object[] getArguments()
   {
      return (Object[]) getValue(InvocationKey.ARGUMENTS);
   }
   
   /**
    * marcf: SCOTT WARNING! I removed the "setPrincipal" that was called here
    */
   public void setInvocationContext(InvocationContext ctx)
   {
      transient_payload.put(InvocationKey.INVOCATION_CONTEXT, ctx);
   }
   
   public void setEnterpriseContext(Object ctx)
   {
      transient_payload.put(InvocationKey.ENTERPRISE_CONTEXT, ctx);
   }
      
   public Object getEnterpriseContext()
   {
      return (Object) transient_payload.get(InvocationKey.ENTERPRISE_CONTEXT);
   }

   public InvocationContext getInvocationContext()
   {
      return (InvocationContext) transient_payload.get(
            InvocationKey.INVOCATION_CONTEXT);
   }

}
