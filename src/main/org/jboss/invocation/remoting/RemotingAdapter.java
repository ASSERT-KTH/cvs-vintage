
/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 *
 */

package org.jboss.invocation.remoting;

import java.io.ObjectStreamException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import javax.management.ObjectName;
import javax.transaction.Transaction;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationKey;
import org.jboss.invocation.InvocationResponse;
import org.jboss.invocation.InvocationResponse;
import org.jboss.invocation.Invoker;
import org.jboss.invocation.Invoker;
import org.jboss.invocation.MarshalledInvocation;
import org.jboss.invocation.ServerID;
import org.jboss.invocation.ServerID;
import org.jboss.remoting.Client;
import org.jboss.remoting.ClientInterceptor;
import org.jboss.remoting.InvokerLocator;
import org.jboss.remoting.ident.Identity;
import org.jboss.remoting.ident.Identity;
import org.jboss.system.ServiceMBean;
import org.jboss.system.client.ClientServiceMBeanSupport;
import org.jboss.tm.DTXAResourceInterceptor;
import org.jboss.util.jmx.ObjectNameFactory;



/**
 * RemotingAdapter.java
 *
 *
 * Created: Tue Apr 22 23:49:03 2003
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version 1.0
 *
 * @jmx.mbean extends="org.jboss.system.ServiceMBean"
 *            name="jboss.ejb:service=RemotingAdapter"
 */
public class RemotingAdapter
   extends ClientServiceMBeanSupport
   implements Invoker, RemotingAdapterMBean, Serializable
{


   private ObjectName nextInterceptorName;

   /**
    * The field <code>clientInterceptor</code> holds the next client remoting interceptor
    *
    */
   private ClientInterceptor next;


   public RemotingAdapter()
   {

   } // RemotingAdapter constructor

   protected void startService() throws Exception
   {
      if (next == null)
      {
         next = (ClientInterceptor)getManagedResource(nextInterceptorName);
      } // end of if ()

   }

   protected void stopService() throws Exception
   {
   }

   /**
    * Get the NextInterceptorName value.
    * @return the NextInterceptorName value.
    *
    * @jmx.managed-attribute
    */
   public ObjectName getNextInterceptorName()
   {
      return nextInterceptorName;
   }

   /**
    * Set the NextClientInterceptorName value. Changing it will have
    * no effect after the mbean is started.
    * @param newNextInterceptorName The new NextInterceptorName value.
    *
    * @jmx.managed-attribute
    */
   public void setNextInterceptorName(ObjectName nextInterceptorName)
   {
      this.nextInterceptorName = nextInterceptorName;
   }


   /**
    * The <code>invoke</code> method translates the ejb invocation
    * object into a remoting framework invocation.  I suggest these
    * could become better aligned to eliminate any translation.
    *
    * @param invocation an <code>Invocation</code> value
    * @return an <code>InvocationResponse</code> value
    * @exception Exception if an error occurs
    */
   public InvocationResponse invoke(Invocation invocation) throws Throwable
   {
      Map sendParams = new HashMap();
      InvokerLocator locator = (InvokerLocator)invocation.getInvocationContext().getValue(InvocationKey.LOCATOR);
      Transaction tx = invocation.getTransaction();
      sendParams.put(DTXAResourceInterceptor.TX_KEY, tx);
      Object result = next.invoke(locator, "EJB", "",
                                  new MarshalledInvocation(invocation),
                                  sendParams);
      return (InvocationResponse)result;
   }


   /**
    * The <code>internalSetServiceName</code> method sets the client
    * side object name based on the remoting identity object.
    *
    * @exception Exception if an error occurs
    */
   protected void internalSetServiceName() throws Exception
   {
      serviceName = ObjectNameFactory.create("jboss.ejb:service=RemotingAdapter");
      getLog().info("internalSetServiceName called: " + serviceName);
   }

   private Object readResolve() throws ObjectStreamException
   {
      return internalReadResolve();
   }

   /**
    * The <code>internalSetup</code> method method registers this as a
    * client-side mbean, then starts this.
    *
    * @exception Exception if an error occurs
    */
   protected void internalSetup() throws Exception
   {
      //register ourselves
      if (!server.isRegistered(getServiceName()))
      {
         super.internalSetup();
      } // end of if ()
      else
      {
         log.info("Got to internal setup even though already registered");
      } // end of else

      startService();
   }

   // Implementation of org.jboss.invocation.Invoker

   public ServerID getServerID() throws Exception
   {
      return null;
   }

   /**
    * The <code>getIdentity</code> method
    *
    * @return an <code>Identity</code> value
    * @exception Exception if an error occurs
    */
   public Identity getIdentity() throws Exception
   {
      return null;
   }


} // RemotingAdapter
