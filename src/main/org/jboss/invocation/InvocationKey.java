/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.invocation;

import java.io.Serializable;
import java.io.ObjectStreamException;

import java.util.ArrayList;

/**
 * Type safe enumeration used for keys in the Invocation object.
 */
public final class InvocationKey implements Serializable {
   // these fields are used for serialization
   private static int nextOrdinal = 0;
   private static final ArrayList values = new ArrayList(10);

   /** 
    * Transactional information with the invocation. 
    */ 
   public static final InvocationKey TRANSACTION = 
         new InvocationKey("TRANSACTION");

   /** 
    * Security principal assocated with this invocation.
    */
   public static final InvocationKey PRINCIPAL = new InvocationKey("PRINCIPAL");

   /** 
    * Security credential assocated with this invocation. 
    */   
   public static final InvocationKey CREDENTIAL = 
         new InvocationKey("CREDENTIAL");
   
   /** 
    * We can keep a reference to an abstract "container" this invocation 
    * is associated with. 
    */
   public static final InvocationKey OBJECT_NAME = 
         new InvocationKey("CONTAINER");
   
   /** 
    * The type can be any qualifier for the invocation, anything (used in EJB). 
    */
   public static final InvocationKey TYPE = new InvocationKey("TYPE");
   
   /** 
    * The Cache-ID associates an instance in cache somewhere on the server 
    * with this invocation. 
    */
   public static final InvocationKey CACHE_ID = new InvocationKey("CACHE_ID");
   
   /** 
    * The invocation can be a method invocation, we give the method to call. 
    */
   public static final InvocationKey METHOD = new InvocationKey("METHOD");
   
   /** 
    * The arguments of the method to call. 
    */
   public static final InvocationKey ARGUMENTS = new InvocationKey("ARGUMENTS");

   /** 
    * The callback method that should be invoked on the implementation.
    */
   public static final InvocationKey CALLBACK_METHOD = 
         new InvocationKey("CALLBACK_METHOD");
   
   /** 
    * The arguments of the callback method to call. 
    */
   public static final InvocationKey CALLBACK_ARGUMENTS = 
         new InvocationKey("CALLBACK_ARGUMENTS");
    
   /** 
    * Invocation context
    */
   public static final InvocationKey INVOCATION_CONTEXT = 
         new InvocationKey("INVOCATION_CONTEXT");
   
   /** 
    * Enterprise context
    */
   public static final InvocationKey ENTERPRISE_CONTEXT = 
         new InvocationKey("ENTERPRISE_CONTEXT");

   /** 
    * The invoker-proxy binding name
    */
   public static final InvocationKey INVOKER_PROXY_BINDING = 
         new InvocationKey("INVOKER_PROXY_BINDING");

   /** 
    * The invoker 
    */
   public static final InvocationKey INVOKER = new InvocationKey("INVOKER");
 
   /**
    * The JNDI name of the EJB.
    */
   public static final InvocationKey JNDI_NAME = new InvocationKey("JNDI_NAME");

   /** 
    * The EJB meta-data for the {@link EJBHome} reference. 
    */
   public final static InvocationKey EJB_METADATA = 
         new InvocationKey("EJB_METADATA");
   
   public final static InvocationKey XID = new InvocationKey("XID");
   public final static InvocationKey TX_TIMEOUT = new InvocationKey("TX_TIMEOUT");
   public final static InvocationKey METHOD_TO_TX_SUPPORT_MAP = new InvocationKey("METHOD_TO_TX_SUPPORT_MAP");
   private final transient String name;

   // this is the only value serialized
   private final int ordinal;
 
   private InvocationKey(String name) {
      this.name = name;
      this.ordinal = nextOrdinal++;
      values.add(this);
   }

   public String toString() {
      return name;
   }

   Object readResolve() throws ObjectStreamException {
      return values.get(ordinal);
   }
}


