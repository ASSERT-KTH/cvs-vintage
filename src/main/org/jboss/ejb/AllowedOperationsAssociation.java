/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb;

// $Id: AllowedOperationsAssociation.java,v 1.1 2004/04/15 14:30:27 tdiesler Exp $

import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

/**
 * Associates the current thread with a stack of flags that
 * indicate the callers current ejb method.
 *
 * According to the EJB2.1 spec not all context methods can be accessed at all times
 * For example ctx.getPrimaryKey() should throw an IllegalStateException when called from within ejbCreate()
 *
 * @author Thomas.Diesler@jboss.org
 * @version $Revision: 1.1 $
 */
public final class AllowedOperationsAssociation
        implements AllowedOperationsFlags
{
   // provide logging
   private static final Logger log = Logger.getLogger(AllowedOperationsAssociation.class);

   // Constants -----------------------------------------------------

   public static final HashMap methodMap = new LinkedHashMap();
   static
   {
      methodMap.put(new Integer(IN_INTERCEPTOR_METHOD), "IN_INTERCEPTOR_METHOD");
      methodMap.put(new Integer(IN_EJB_ACTIVATE), "IN_EJB_ACTIVATE");
      methodMap.put(new Integer(IN_EJB_PASSIVATE), "IN_EJB_PASSIVATE");
      methodMap.put(new Integer(IN_EJB_REMOVE), "IN_EJB_REMOVE");
      methodMap.put(new Integer(IN_EJB_CREATE), "IN_EJB_CREATE");
      methodMap.put(new Integer(IN_EJB_POST_CREATE), "IN_EJB_POST_CREATE");
      methodMap.put(new Integer(IN_EJB_FIND), "IN_EJB_FIND");
      methodMap.put(new Integer(IN_EJB_HOME), "IN_EJB_HOME");
      methodMap.put(new Integer(IN_EJB_TIMEOUT), "IN_EJB_TIMEOUT");
      methodMap.put(new Integer(IN_EJB_LOAD), "IN_EJB_LOAD");
      methodMap.put(new Integer(IN_EJB_STORE), "IN_EJB_STORE");
      methodMap.put(new Integer(IN_SET_ENTITY_CONTEXT), "IN_SET_ENTITY_CONTEXT");
      methodMap.put(new Integer(IN_UNSET_ENTITY_CONTEXT), "IN_UNSET_ENTITY_CONTEXT");
      methodMap.put(new Integer(IN_SET_SESSION_CONTEXT), "IN_SET_SESSION_CONTEXT");
      methodMap.put(new Integer(IN_SET_MESSAGE_DRIVEN_CONTEXT), "IN_SET_MESSAGE_DRIVEN_CONTEXT");
      methodMap.put(new Integer(IN_AFTER_BEGIN), "IN_AFTER_BEGIN");
      methodMap.put(new Integer(IN_BEFORE_COMPLETION), "IN_BEFORE_COMPLETION");
      methodMap.put(new Integer(IN_AFTER_COMPLETION), "IN_AFTER_COMPLETION");
      methodMap.put(new Integer(IN_BUSINESS_METHOD), "IN_BUSINESS_METHOD");
      methodMap.put(new Integer(IN_SERVICE_ENDPOINT_METHOD), "IN_SERVICE_ENDPOINT_METHOD");
   }

   /**
    * Holds a stack of the IN_METHOD constants, to indicate that we are in an ejb method
    */
   private static ThreadLocal threadLocal = new ThreadLocal() {
      protected Object initialValue()
      {
         return new Stack();
      }
   };

   // Static --------------------------------------------------------

   /**
    * Set when the instance enters an ejb method, reset on exit
    *
    * @param inMethodFlag one of the IN_METHOD contants or null
    */
   public static void pushInMethodFlag(int inMethodFlag)
   {
      Stack inMethodStack = (Stack)threadLocal.get();
      inMethodStack.push(new Integer(inMethodFlag));
   }

   /**
    * Reset when the instance exits an ejb method
    */
   public static void popInMethodFlag()
   {
      Stack inMethodStack = (Stack)threadLocal.get();
      inMethodStack.pop();
   }


   /**
    * Throw an IllegalStateException if the current inMethodFlag
    * does not match the given flags
    */
   public static void assertAllowedIn(String ctxMethod, int flags)
   {
      Stack inMethodStack = (Stack)threadLocal.get();

      // Strict validation, the caller MUST set the in method flag
      if (inMethodStack.empty())
      {
         throw new IllegalStateException("Cannot obtain inMethodFlag for: " + ctxMethod);
      }

      // The container should push a method flag into the context just before
      // a call to the instance method
      if (inMethodStack.empty() == false)
      {
         // Check if the given ctxMethod can be called from the ejb instance
         // this relies on the inMethodFlag being pushed prior to the call to the ejb method
         Integer inMethodFlag = ((Integer) inMethodStack.peek());
         if ((inMethodFlag.intValue() & flags) == 0 && inMethodFlag.intValue() != IN_INTERCEPTOR_METHOD)
         {
            String message = ctxMethod + " should not be access from this bean method: " + methodMap.get(inMethodFlag);
            IllegalStateException ex = new IllegalStateException(message);
            log.error(message + ", allowed is " + getAllowedMethodList(flags), ex);
            throw ex;
         }
      }
   }

   /**
    * Get a list of strings corresponding to the given method flags
    */
   private static List getAllowedMethodList(int flags)
   {
      ArrayList allowed = new ArrayList();
      Iterator it = methodMap.entrySet().iterator();
      while (it.hasNext())
      {
         Map.Entry entry = (Map.Entry) it.next();
         Integer flag = (Integer) entry.getKey();
         if ((flag.intValue() & flags) > 0)
            allowed.add(entry.getValue());
      }
      return allowed;
   }
}
