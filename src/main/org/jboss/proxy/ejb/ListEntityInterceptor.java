/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.proxy.ejb;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectInput;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import javax.naming.InitialContext;
import javax.ejb.EJBObject;
import javax.ejb.EJBHome;

import org.jboss.invocation.Invoker;
import org.jboss.ejb.ListCacheKey;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationResponse;

/**
* An EJB CMP entity bean proxy class holds info about the List that the entity belongs to,
* is used for reading ahead.
*
* @author <a href="mailto:on@ibis.odessa.ua">Oleg Nitz</a>
* @version $Revision: 1.2 $
*
* @todo: (marcf) methinks that this behavior should be moved to a REAL interceptor (i.e not as extends)
*/
public class ListEntityInterceptor
extends EntityInterceptor
{
   // Constants -----------------------------------------------------
   
   /** Serial Version Identifier. */
   //   private static final long serialVersionUID = -1523442773137704949L;
   
   protected static final Method GET_READ_AHEAD_VALUES;
   
   // Attributes ----------------------------------------------------
   
   /**
   * A List that this entity belongs to (used for reading ahead).
   */
   private List list;
   
   /**
   * A hash map of read ahead values, maps Methods to values.
   */
   private transient HashMap readAheadValues;
   
   // Static --------------------------------------------------------
   
   static {
      try {
         final Class[] empty = {};
         
         GET_READ_AHEAD_VALUES = ReadAheadBuffer.class.getMethod("getReadAheadValues", empty);
      }
      catch (Exception e) {
         e.printStackTrace();
         throw new ExceptionInInitializerError(e);
      }
   }
   
   // Constructors --------------------------------------------------
   
   /**
   * No-argument constructor for externalization.
   */
   public ListEntityInterceptor() {}
   
   /**
   * Construct a <tt>ListEntityProxy</tt>.
   *
   * @param name            The JNDI name of the container that we proxy for.
   * @param container       The remote interface of the invoker for which
   *                        this is a proxy for.
   * @param id              The primary key of the entity.
   * @param optimize        True if the proxy will attempt to optimize
   *                        VM-local calls.
   * @param list            A List that this entity belongs to (used for reading ahead).
   * @param listId The list id.
   * @param index The index of this entity in the list.
   *
   * @throws NullPointerException     Id may not be null.
   */
 
   public ListEntityInterceptor(List list) { this.list = list;}
 
   // Public --------------------------------------------------------
   
   public Map getReadAheadValues() {
      if (readAheadValues == null) {
         readAheadValues = new HashMap();
      }
      return readAheadValues;
   }
   
   
   /**
   * InvocationHandler implementation.
   *
   * @param proxy   The proxy object.
   * @param m       The method being invoked.
   * @param args    The arguments for the method.
   *
   * @throws Throwable    Any exception or error thrown while processing.
   */
   public InvocationResponse invoke(Invocation invocation)
   throws Throwable
   {
      ReadAheadResult raResult;
      Object[] aheadResult;
      int from;
      int to;
      ReadAheadBuffer buf;
   
      Method m = invocation.getMethod();
      
      if (m.equals(GET_READ_AHEAD_VALUES)) {
         return new InvocationResponse(getReadAheadValues());
      }
      
      // have we read ahead the result?
      if (readAheadValues != null) {
         Object result = readAheadValues.get(m);
         if (readAheadValues.containsKey(m)) {
            return new InvocationResponse(readAheadValues.remove(m));
         }
      }
      
      InvocationResponse response = super.invoke(invocation);
      Object result = response.getResponse();
      
      
      // marcf : I think all these will map nicely to the in/out of real interceptor, i.e. do not "extend"
      
      if (result instanceof ReadAheadResult) 
      {
         raResult = (ReadAheadResult) result;
         aheadResult = raResult.getAheadResult();
         from = ((ListCacheKey) invocation.getInvocationContext().getCacheId()).getIndex() + 1;
         to = Math.min(from + aheadResult.length, list.size());
         for (int i = from; i < to; i++) {
            buf = (ReadAheadBuffer) list.get(i);
            buf.getReadAheadValues().put(m, aheadResult[i - from]);
         }
         response.setResponse(raResult.getMainResult());
         return response;
      } else {
         return response;
      }
   }
   
   // Package protected ---------------------------------------------
   
   // Protected -----------------------------------------------------
   
   /**
   * Externalization support.
   *
   * @param out
   *
   * @throws IOException
   */
   public void writeExternal(final ObjectOutput out)
   throws IOException
   {
      super.writeExternal(out);
      out.writeObject(list);
   }
   
   /**
   * Externalization support.
   *
   * @param in
   *
   * @throws IOException
   * @throws ClassNotFoundException
   */
   public void readExternal(final ObjectInput in)
   throws IOException, ClassNotFoundException
   {
      super.readExternal(in);
      list = (List)in.readObject();
   }
   
   
   // Private -------------------------------------------------------
   
   // Inner classes -------------------------------------------------
}

