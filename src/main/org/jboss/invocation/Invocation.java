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
 * <p>The heart of it is the payload map that can contain anything we then put readers on them
 *    The first <em>reader</em> is this <em>Invocation</em> object that can interpret the data in it. 
 * 
 * <p>Essentially we can carry ANYTHING from the client to the server, we keep a series of 
 *    of predifined variables and method calls to get at the pointers.  But really it is just 
 *    a repository of objects. 
 *
 * @author  <a href="mailto:marc@jboss.org">Marc Fleury</a>
 * @version $Revision: 1.8 $
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
public class Invocation
{
   /** The signature of the invoke() method */
   public static final String[] INVOKE_SIGNATURE = { "java.lang.Object" };
   
   // The payload is a repository of everything associated with the invocation
   // It is information that will need to travel 

   /** Contextual information to the invocation that is not part of the payload. */
   public Map transient_payload = new HashMap();

   /**
    * as_is classes that will not be marshalled by the invocation
    * (java.* and javax.* or anything in system classpath is OK)
    */
   public Map as_is_payload = new HashMap();

   /** Payload will be marshalled for type hiding at the RMI layers. */
   public Map payload = new HashMap();
   
   // The variables used to indicate what type of data and where to put it.

   /** Put me in the transient map, not part of payload. */
   public final static int TRANSIENT = 1;
   
   /** Do not serialize me, part of payload as is. */
   public final static int AS_IS = 0;

   /** Put me in the payload map. */
   public final static int PAYLOAD = 2;

   //
   // We are using the generic payload to store some of our data, we define some integer entries.
   // These are just some variables that we define for use in "typed" getters and setters. 
   // One can define anything either in here explicitely or through the use of external calls to
   // getValue
   //

   /** Transactional information with the invocation. */
   public static final Integer TRANSACTION = new Integer("TRANSACTION".hashCode());

   /** Security principal assocated with this invocation. */
   public static final Integer PRINCIPAL = new Integer("PRINCIPAL".hashCode());

   /** Security credential assocated with this invocation. */   
   public static final Integer CREDENTIAL = new Integer("CREDENTIAL".hashCode());
   
   /** We can keep a reference to an abstract "container" this invocation is associated with. */
   public static final Integer OBJECT_NAME = new Integer("CONTAINER".hashCode());
   
   /** The type can be any qualifier for the invocation, anything (used in EJB). */
   public static final Integer TYPE = new Integer("TYPE".hashCode());
   
   /** The Cache-ID associates an instance in cache somewhere on the server with this invocation. */
   public static final Integer CACHE_ID = new Integer("CACHE_ID".hashCode());
   
   /** The invocation can be a method invocation, we give the method to call. */
   public static final Integer METHOD = new Integer("METHOD".hashCode());
   
   /** The arguments of the method to call. */
   public static final Integer ARGUMENTS = new Integer("ARGUMENTS".hashCode());
   
   /** Invocation context. */
   public static final Integer INVOCATION_CONTEXT = new Integer("INVOCATION_CONTEXT".hashCode());
   
   /** Enterprise context. */
   public static final Integer ENTERPRISE_CONTEXT = new Integer("ENTERPRISE_CONTEXT".hashCode());

   public static final int REMOTE = 0;
   public static final int LOCAL = 1;
   public static final int HOME = 2;
   public static final int LOCALHOME = 3;
   public static final int GETHOME = 4;
   public static final int GETREMOTE = 5;
   public static final int GETLOCALHOME = 6;
   public static final int GETLOCAL = 7;

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
    * The generic getter and setter is really all that one needs to talk to this object
    * We introduce typed getters and setters for convenience and code readability in the codebase
    */
   public void setValue(Object key, Object value) {
      setValue(key, value, PAYLOAD);
   }
   
   /**
    * Advanced store
    * Here you can pass a TYPE that indicates where to put the value.
    * TRANSIENT: the value is put in a map that WON'T be passed 
    * AS_IS: no need to marshall the value when passed (use for all JDK java types)
    * PAYLOAD: we need to marshall the value as its type is application specific
    */
   public void setValue(Object key, Object value, int type) 
   {
      switch (type)
      {
       case TRANSIENT:
          transient_payload.put(key,value);
          break;

       case AS_IS:
          as_is_payload.put(key,value);
          break;
            
       case PAYLOAD:
          payload.put(key,value);
          break;
      }
   }
 
   /**
    * Get a value from the stores.
    */
   public Object getValue(Object key) 
   { 
      // find where it is
      if (payload.containsKey(key)) {
         return payload.get(key);
      }
      else if (as_is_payload.containsKey(key)) {
         return as_is_payload.get(key);
      }
      else if (transient_payload.containsKey(key)) {
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
   public void setTransaction(Transaction tx) {
      as_is_payload.put(TRANSACTION, tx);
   }
   
   /**
    * get the transaction.
    */
   public Transaction getTransaction() {
      return (Transaction) getValue(TRANSACTION);
   }
   
   /**
    * Change the security identity of this invocation.
    */
   public void setPrincipal(Principal principal) {
      as_is_payload.put(PRINCIPAL, principal);
   }
   
   public Principal getPrincipal() {
      return (Principal) getValue(PRINCIPAL);
   }
   
   /**
    * Change the security credentials of this invocation.
    */
   public void setCredential(Object credential) {
      payload.put(CREDENTIAL, credential);
   }
   
   public Object getCredential() {
      return getValue(CREDENTIAL);
   }
   
   /**
    * container for server side association.
    */
   public void setObjectName(Object objectName) {
      payload.put(OBJECT_NAME, objectName);
   }
   
   public Object getObjectName() {
      return getValue(OBJECT_NAME);
   }
   
   /**
    * An arbitrary type.
    */
   public void setType(int type) {
      as_is_payload.put(TYPE, new Integer(type));
   }
   
   public int getType() {
      return ((Integer) getValue(TYPE)).intValue();
   } 
   
   /**
    * Return the invocation target ID.  Can be used to identify a cached object
    */
   public void setId(Object id) {
      payload.put(CACHE_ID, id);
   }
   
   public Object getId() {
      return getValue(CACHE_ID);
   }
   
   /**
    * set on method Return the invocation method.
    */
   public void setMethod(Method method) {
      payload.put(METHOD, method);
   }
   
   /**
    * get on method Return the invocation method.
    */
   public Method getMethod() {
      return (Method) getValue(METHOD);
   }
   
   /**
    * A list of arguments for the method.
    */
   public void setArguments(Object[] arguments) {
      payload.put(ARGUMENTS, arguments);
   }
   
   public Object[] getArguments() {
      return (Object[]) getValue(ARGUMENTS);
   }
   
   /**
    * marcf: SCOTT WARNING! I removed the "setPrincipal" that was called here
    */
   public void setInvocationContext(InvocationContext ctx) {
      transient_payload.put(INVOCATION_CONTEXT, ctx);
   }
   
   public void setEnterpriseContext(Object ctx) {
      transient_payload.put(ENTERPRISE_CONTEXT, ctx);
   }
      
   public Object getEnterpriseContext() {
      return (Object) transient_payload.get(ENTERPRISE_CONTEXT);
   }

   public InvocationContext getInvocationContext() {
      return (InvocationContext) transient_payload.get(INVOCATION_CONTEXT);
   }
}
