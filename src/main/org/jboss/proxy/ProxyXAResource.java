
/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 *
 */

package org.jboss.proxy;


import java.lang.ThreadLocal;
import java.lang.reflect.Method;
import javax.management.ObjectName;
import javax.resource.spi.XATerminator;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationKey;
import org.jboss.invocation.Invoker;
import org.jboss.invocation.PayloadKey;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.tm.XAResourceFactory;


/**
 * ProxyXAResource.java
 *
 *
 * Created: Thu Oct  3 21:04:01 2002
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version
 *
 * @jmx.mbean extends="org.jboss.system.ServiceMBean"
 */

public class ProxyXAResource 
   extends ServiceMBeanSupport
   implements XAResource, XAResourceFactory, ProxyXAResourceMBean
{


   private static final Method PREPARE_METHOD;

   private static final Method COMMIT_METHOD;

   private static final Method ROLLBACK_METHOD;

   private static final Method FORGET_METHOD;

   private static final Method RECOVER_METHOD;

   static 
   {
      try
      {
	 PREPARE_METHOD = XATerminator.class.getMethod("prepare", new Class[] {Xid.class});

	 COMMIT_METHOD = XATerminator.class.getMethod("commit", new Class[] {Xid.class, boolean.class});

	 ROLLBACK_METHOD = XATerminator.class.getMethod("rollback", new Class[] {Xid.class});

	 FORGET_METHOD = XATerminator.class.getMethod("forget", new Class[] {Xid.class});

	 RECOVER_METHOD = XATerminator.class.getMethod("recover", new Class[] {int.class});
      }
      catch (NoSuchMethodException nsme)
      {
	 throw new RuntimeException("Could not initialize ProxyXAResource with XATerminator methods");
      }
   }

   private ThreadLocal xids = new ThreadLocal();

   private ThreadLocal invocations = new ThreadLocal();

   private int transactionTimeout = 6;//Gotta pick something for a default

   private ObjectName transactionManagerService;

   private TransactionManager tm;

   /**
    * The variable <code>invoker</code> tells the prepare/commit
    * etc. methods where to send their invocation.  It can't be put in
    * a threadlocal to share one ProxyXAResource among many invokers
    * because the TransactionManager needs to be able to ask if two
    * ProxyXAResources represent the same resource manager.  Without
    * this variable there is no basis for answering.
    *
    */
   private Invoker invoker;

   public ProxyXAResource() 
   {
   }


   /**
    * Get the TransactionManagerService value.
    * @return the TransactionManagerService value.
    *
    * @jmx.managed-attribute
    */
   public ObjectName getTransactionManagerService() {
      return transactionManagerService;
   }


   
   /**
    * Set the TransactionManagerService value.
    * @param newTransactionManagerService The new TransactionManagerService value.
    * @return the TransactionManagerService value.
    *
    * @jmx.managed-attribute
    */
   public void setTransactionManagerService(ObjectName transactionManagerService) {
      this.transactionManagerService = transactionManagerService;
   }


   /**
    * Get the Invoker value.
    * @return the Invoker value.
    *
    * @jmx.managed-attribute
    */
   public Invoker getInvoker() {
      return invoker;
   }

   /**
    * Set the Invoker value.
    * @param newInvoker The new Invoker value.
    *
    * @jmx.managed-attribute
    */
   public void setInvoker(Invoker invoker) {
      this.invoker = invoker;
   }

   
   protected void startService() throws Exception
   {
      try
      {
         tm = (TransactionManager)getServer().getAttribute(transactionManagerService,
                            "TransactionManager");
      }
      catch (Exception e)
      {
         getLog().info("Could not find transaction manager, transactions will not work.", e);
      }
      try
      {
         getServer().invoke(transactionManagerService,
                            "registerXAResourceFactory",
                            new Object[] {this},
                            new String[] {XAResourceFactory.class.getName()});
      }
      catch (Exception e)
      {
         getLog().info("Could not register with transaction manager service, recovery impossible", e);
      }
   }

   protected void stopService() throws Exception
   {
      tm = null;
      try
      {
         getServer().invoke(transactionManagerService,
                            "unregisterXAResourceFactory",
                            new Object[] {this},
                            new String[] {XAResourceFactory.class.getName()});
      }
      catch (Exception e)
      {
         getLog().info("Could not unregister with transaction manager service");
      }
   }

   //XAResourceFactory interface
   /**
    * The <code>getXAResource</code> method is used by the
    * TransactionManager to get a XAResource for recovery.  Here,we
    * are also abusing it by using it from the invoker to get instance
    * to call.  This should not really be a managed attribute, this
    * xaresource should be an interceptor at the invoker, so when the
    * call goes through it gets the invocation directly.
    *
    * @return a <code>XAResource</code> value
    *
    * @jmx.managed-attribute
    */
   public XAResource getXAResource()
   {
      return this;
   }

   /**
    * The <code>returnXAResource</code> method is called by the tm
    * when it is done with an XAResource after recovery is complete.
    *
    */
   public void returnXAResource()
   {
   }

   /**
    * The <code>setInvocation</code> method is called by the invoker
    * just before this XAResource is enrolled in a transaction.  It
    * provides the context for the start method to operate on.
    *
    * @param invocation an <code>Invocation</code> value
    */
   public void setInvocation(Invocation invocation)
   {
      invocations.set(invocation);
   }
   
   // implementation of javax.transaction.xa.XAResource interface

   /**
    *
    * @param param1 <description>
    * @param param2 <description>
    * @exception javax.transaction.xa.XAException <description>
    */
   public void start(Xid xid, int flags) throws XAException
   {
      log.info("Starting xid: " + xid);
      if (xids.get() != null)
      {
         throw new XAException("Trying to start a second tx!, old: " + xids.get() + ", new: " + xid);
      }
      xids.set(xid);
      Invocation invocation = (Invocation)invocations.get();
      invocation.setValue(InvocationKey.XID, xid, PayloadKey.PAYLOAD);
      invocation.setValue(InvocationKey.TX_TIMEOUT, new Integer(transactionTimeout), PayloadKey.AS_IS);
   }

   /**
    *
    * @param param1 <description>
    * @param param2 <description>
    * @exception javax.transaction.xa.XAException <description>
    */
   public void end(Xid xid, int flags) throws XAException
   {
      log.info("Ending xid; " + xid);
      if (xid.equals(xids.get()))
      {
         xids.set(null);
      }
      //What do we do about ending TMSUCCESS a suspended tx? Do we
      //send a end message?  It's not supported by XATerminator
      //interface, maybe it is unnecessary
   }

   /**
    *
    * @param param1 <description>
    * @return <description>
    * @exception javax.transaction.xa.XAException <description>
    */
   public int prepare(Xid xid) throws XAException
   {
      log.info("preparing xid: " + xid);
      Invocation invocation = new Invocation();
      invocation.setMethod(PREPARE_METHOD);
      invocation.setArguments(new Object[] {xid});
      try
      {
	 Integer result = (Integer)invoker.invoke(invocation);
	 return result.intValue();
      }
      catch (Exception e)
      {
	 if (e instanceof XAException)
	 {
	    throw (XAException)e;
	 }
	 throw new RuntimeException("Unexpected exception in prepare of xid: " + xid + ", exception: " + e);
      }
   }

   /**
    *
    * @param param1 <description>
    * @param param2 <description>
    * @exception javax.transaction.xa.XAException <description>
    */
   public void commit(Xid xid, boolean onePhase) throws XAException
   {
      log.info("Committing xid: " + xid);
      Invocation invocation = new Invocation();
      invocation.setMethod(COMMIT_METHOD);
      invocation.setArguments(new Object[] {xid, new Boolean(onePhase)});
      try
      {
	 invoker.invoke(invocation);
      }
      catch (Exception e)
      {
	 if (e instanceof XAException)
	 {
	    throw (XAException)e;
	 }
	 throw new RuntimeException("Unexpected exception in commit of xid: " + xid + ", exception: " + e);
      }

   }

   /**
    *
    * @param param1 <description>
    * @exception javax.transaction.xa.XAException <description>
    */
   public void rollback(Xid xid) throws XAException
   {
      log.info("Rolling back xid: " + xid);
      Invocation invocation = new Invocation();
      invocation.setMethod(ROLLBACK_METHOD);
      invocation.setArguments(new Object[] {xid});
      try
      {
	 invoker.invoke(invocation);
      }
      catch (Exception e)
      {
	 if (e instanceof XAException)
	 {
	    throw (XAException)e;
	 }
	 throw new RuntimeException("Unexpected exception in rollback of xid: " + xid + ", exception: " + e);
      }

   }

   /**
    *
    * @param param1 <description>
    * @exception javax.transaction.xa.XAException <description>
    */
   public void forget(Xid xid) throws XAException
   {
      Invocation invocation = new Invocation();
      invocation.setMethod(FORGET_METHOD);
      invocation.setArguments(new Object[] {xid});
      try
      {
	 invoker.invoke(invocation);
      }
      catch (Exception e)
      {
	 if (e instanceof XAException)
	 {
	    throw (XAException)e;
	 }
	 throw new RuntimeException("Unexpected exception in forget of xid: " + xid + ", exception: " + e);
      }

   }

   /**
    *
    * @param param1 <description>
    * @return <description>
    * @exception javax.transaction.xa.XAException <description>
    * @todo implement recover.
    */
   public Xid[] recover(int flag) throws XAException
   {
      Invocation invocation = new Invocation();
      invocation.setMethod(RECOVER_METHOD);
      invocation.setArguments(new Object[] {new Integer(flag)});
      try
      {
	 return (Xid[])invoker.invoke(invocation);
      }
      catch (Exception e)
      {
	 if (e instanceof XAException)
	 {
	    throw (XAException)e;
	 }
	 throw new RuntimeException("Unexpected exception in recover, exception: " + e);
      }

   }

   /**
    *
    * @param param1 <description>
    * @return <description>
    * @exception javax.transaction.xa.XAException <description>
    */
   public boolean isSameRM(XAResource otherRM)
   {
      //this could be object name equality? or just ==.
      return otherRM == this;
   }

   /**
    *
    * @return <description>
    * @exception javax.transaction.xa.XAException <description>
    */
   public int getTransactionTimeout()
   {
      return transactionTimeout;
   }

   /**
    *
    * @param param1 <description>
    * @return <description>
    * @exception javax.transaction.xa.XAException <description>
    */
   public boolean setTransactionTimeout(int transactionTimeout)
   {
      this.transactionTimeout = transactionTimeout;
      return true;
   }

}// ProxyXAResource
