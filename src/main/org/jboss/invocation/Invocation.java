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
*   The Invocation object is the generic object flowing through our interceptors
*
*   The heart of it is the payload map that can contain anything we then put readers on them
*   The first "reader" is this "Invocation" object that can interpret the data in it. 
* 
*   Essentially we can carry ANYTHING from the client to the server, we keep a series of 
*   of predifined variables and method calls to get at the pointers.  But really it is just 
*   a repository of objects. 
*
*   @see <related>
*   @author  <a href="mailto:marc@jboss.org">Marc Fleury</a>
*   @version $Revision: 1.4 $
*   Revisions:
*
*   <p><b>Revisions:</b>
*
*   <p><b>2001114 marc fleury:</b>
*   <ul>
*   <li> Initial check-in
*   </ul>
*/

public class Invocation
{
   
   // Attributes ---------------------------------------------------- 
   
   /**
   * The payload is a repository of everything associated with the invocation
   * with the exception of the generic transaction and security information above.
   */
   public Map payload;
   
   
   
   // Static --------------------------------------------------------
   
   
   /**
   * We are using the generic payload to store some of our data, we define some integer entries.
   * These are just some variables that we define for use in "typed" getters and setters. 
   * One can define anything either in here explicitely or through the use of external calls to getValue
   */
   public static final Integer
   // Transactional information with the invocation
   TRANSACTION = new Integer(new String("TRANSACTION").hashCode()),
   PRINCIPAL = new Integer(new String("PRINCIPAL").hashCode()),
   CREDENTIAL = new Integer(new String("CREDENTIAL").hashCode()),
   
   // We can keep a reference to an abstract "container" this invocation is associated with
   CONTAINER = new Integer(new String("CONTAINER").hashCode()),
   // The type can be any qualifier for the invocation, anything (used in EJB)
   TYPE = new Integer(new String("TYPE").hashCode()),
   // The Cache-ID associates an instance in cache somewhere on the server with this invocation
   CACHE_ID = new Integer(new String("CACHE_ID").hashCode()),
   // The invocation can be a method invocation, we give the method to call
   METHOD = new Integer(new String("METHOD").hashCode()), 
   // The arguments of the method to call
   ARGUMENTS = new Integer(new String("ARGUMENTS").hashCode()),
   // Enterprise context
   ENTERPRISE_CONTEXT = new Integer(new String("ENTERPRISE_CONTEXT").hashCode());
  
   public static final int
   REMOTE = 0,
   LOCAL = 1,
   HOME = 2, 
   LOCALHOME = 3,
   GETHOME = 4,
   GETREMOTE = 5,
   GETLOCALHOME = 6,
   GETLOCAL = 7;
   
   // Constructors --------------------------------------------------
   
   public Invocation() 
   {
      //For externalization only
   }
   /**
   * Invocation creation
   */
   public Invocation(Map payload) 
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
      setId(id);
      setMethod(m);
      setArguments(args);    
      setTransaction(tx);
      setPrincipal(identity);
      setCredential(credential);
   }
   
   // Public --------------------------------------------------------
   
   /**
   * The generic getter and setter is really all that one needs to talk to this object
   * We introduce typed getters and setters for convenience and code readability in the codebase
   */
   
   //The generic store of variables
   public void setValue(Object key, Object value) { payload.put(key, value); }
   public Object getValue(Object key) { return payload.get(key); }
   
   /**
   * Convenience typed getters, use pre-declared keys in the store, but it all comes back to the payload  
   */
   
   // set and get on transaction
   public void setTransaction(Transaction tx) { payload.put(TRANSACTION, tx); }
   public Transaction getTransaction() { return (Transaction) getValue(TRANSACTION); }
   
   //  Change the security identity of this invocation.
   public void setPrincipal(Principal principal) { payload.put(PRINCIPAL, principal);}
   public Principal getPrincipal() { return (Principal) getValue(PRINCIPAL);}
   
   //  Change the security credentials of this invocation.
   public void setCredential(Object credential) { payload.put(CREDENTIAL, credential);}
   public Object getCredential() { return getValue(CREDENTIAL); }
   
   // A container for server side association
   public void setContainer(Object container) { payload.put(CONTAINER, container);}
   public Object getContainer() { return getValue(CONTAINER);}
   
   // An arbitrary type
   public void setType(int type) {payload.put(TYPE, new Integer(type));}
   public int getType() {return ((Integer) getValue(TYPE)).intValue();} 
   
   //Return the invocation target ID.  Can be used to identify a cached object
   public void setId(Object id) { payload.put(CACHE_ID, id);}
   public Object getId() { return getValue(CACHE_ID);}
   
   // set and get on method Return the invocation method.
   public void setMethod(Method method) { payload.put(METHOD, method);}
   public Method getMethod() { return (Method) getValue(METHOD);}
   
   // A list of arguments for the method
   public void setArguments(Object[] arguments) { payload.put(ARGUMENTS, arguments); } 
   public Object[] getArguments() { return (Object[]) getValue(ARGUMENTS); }
   
   /**
   * marcf: SCOTT WARNING! I removed the "setPrincipal" that was called here
   */
   public void setEnterpriseContext(Object ctx) { payload.put(ENTERPRISE_CONTEXT, ctx);}
      
   public Object getEnterpriseContext() { return (Object) payload.get(ENTERPRISE_CONTEXT);}
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------

}
