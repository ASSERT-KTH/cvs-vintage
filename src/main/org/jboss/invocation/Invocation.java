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
*
*   @see <related>
*   @author  <a href="mailto:marc@jboss.org">Marc Fleury</a>
*   @version $Revision: 1.1 $
*   Revisions:
*
*   <p><b>Revisions:</b>
*
*   <p><b>20211002 marc fleury:</b>
*   <ul>
*   <li> Initial check-in
*   </ul>
*/

public class Invocation
{

   // Attributes ----------------------------------------------------

   /**
    *  The transaction of this invocation.
    */
   public Transaction tx;

   /**
    *  The security identity of this invocation.
    */
   public Principal identity;

   /**
    *  The security credentials of this invocation.
    */
   public Object credential;


   /**
   * The linked list of object names, in String form, that the invocation must go through
   * Should move to a real linked list in the future (so we don't have to update the full
   * variable to include new interceptor flows
   * We could keep track of the mbeans to see in this object with an incremented index
   * there would be no central intelligence but this "invocation" that knows where to go next
   */
   public String[] mbeans;
   
   
   /**
    * The payload is a repository of everything associated with the invocation
    * with the exception of the generic transaction and security information above.
    */
   public Map payload;
   
     
   
   // Static --------------------------------------------------------


   /*
   * We are using the generic payload to store some of our data, we define the integer entries.
   */
   public static final Integer
      METHOD = new Integer(new String("METHOD").hashCode()), 
      ARGUMENTS = new Integer(new String("ARGUMENTS").hashCode());
 
   // Constructors --------------------------------------------------

   /**
    * Invocation creation
    */
   public Invocation(Transaction tx, 
                     Principal identity, 
                     Object credential,
                     String[] mbeans,
                     Method method,
                     Object[] arguments)
   {
      
      //The generic variables
      this.tx = tx;
      this.identity = identity;
      this.credential = credential;
      
      // The generic payload
      this.payload = new HashMap();
      
      // The invocation
      setMBeans(mbeans);
      setMethod(method);
      setArguments(arguments);
      
   }


   // Public --------------------------------------------------------

   /**
    * set and get on transaction
    */
   public void setTransaction(Transaction tx) { this.tx = tx; }

   public Transaction getTransaction() { return tx; }

   
   /**
    *  Change the security identity of this invocation.
    */
   public void setPrincipal(Principal identity) { this.identity = identity; }

   public Principal getPrincipal() { return identity;}

   
   /**
    *  Change the security credentials of this invocation.
    */
   public void setCredential(Object credential) { this.credential = credential; }

   public Object getCredential() { return credential; }
   
   
   /* 
   * The mbeans this invocation must go through (most cases will be one until we mbeanify all interceptors
   *
   * marcf fixme: I suspect it is the way to go but am open to "should the interceptors all be mbeans 
   * discussions"
   */
   public void setMBeans(String[] mbeans) { this.mbeans = mbeans; }
   
   public String[] getMBeans(String[] mbeans) { return mbeans; }

   
   /**
    *  set and get on method Return the invocation method.
    */
   public void setMethod(Method method) { payload.put(METHOD, method);}

   public Method getMethod() { return (Method) payload.get(METHOD);}

   
   /**
    *  Return the invocation argument list.
    */
   public void setArguments(Object[] arguments) { payload.put(ARGUMENTS, arguments); }
   
   public Object[] getArguments() { return (Object[]) payload.get(ARGUMENTS); }

      
   /*
   * The generic store of variables
   */
   public void setValue(Object key, Object value) { payload.put(key, value); }
   
   public Object getValue(Object key) { return payload.get(key); }
   
   // Package protected ---------------------------------------------

   // Protected -----------------------------------------------------

   // Private -------------------------------------------------------

   // Inner classes -------------------------------------------------

}
