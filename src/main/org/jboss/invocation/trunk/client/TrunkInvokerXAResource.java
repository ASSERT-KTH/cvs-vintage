
/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 *
 */

package org.jboss.invocation.trunk.client;


import java.lang.ThreadLocal;
import javax.management.ObjectName;
import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import org.jboss.system.ServiceMBean;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.tm.XAResourceFactory;
import javax.management.Attribute;


/**
 * TrunkInvokerXAResource.java
 *
 *
 * Created: Thu Oct  3 21:04:01 2002
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version
 *
 * @jmx.mbean extends="ServiceMBean"
 */

public class TrunkInvokerXAResource 
   extends ServiceMBeanSupport
   implements XAResource, XAResourceFactory, TrunkInvokerXAResourceMBean
{

   private TrunkInvokerProxy trunkInvokerProxy;

   private ThreadLocal xids;

   private int transactionTimeout = TrunkInvokerProxy.DEFAULT_TX_TIMEOUT;

   private ObjectName transactionManagerService;

   public TrunkInvokerXAResource() 
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
    * Get the TrunkInvokerProxy value.
    * @return the TrunkInvokerProxy value.
    *
    * @jmx.managed-attribute
    */
   public TrunkInvokerProxy getTrunkInvokerProxy() {
      return trunkInvokerProxy;
   }

   /**
    * Set the TrunkInvokerProxy value.
    * @param newTrunkInvokerProxy The new TrunkInvokerProxy value.
    *
    * @jmx.managed-attribute
    */
   public void setTrunkInvokerProxy(TrunkInvokerProxy trunkInvokerProxy) {
      this.trunkInvokerProxy = trunkInvokerProxy;
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


   protected void startService() throws Exception
   {
      try
      {
         getServer().invoke(transactionManagerService,
                            "registerXAResourceFactory",
                            new Object[] {this},
                            new String[] {XAResourceFactory.class.getName()});
      }
      catch (Exception e)
      {
         getLog().info("Could not register with transaction manager service, recovery impossible");
      }
      trunkInvokerProxy.setTrunkInvokerXAResource(this);
   }

   protected void stopService() throws Exception
   {
      try
      {
         getServer().invoke(transactionManagerService,
                            "unregisterXAResourceFactory",
                            new Object[] {this},
                            new String[] {XAResourceFactory.class.getName()});
      }
      catch (Exception e)
      {
         getLog().info("Could not register with transaction manager service, recovery impossible");
      }
      trunkInvokerProxy.setTrunkInvokerXAResource(null);
   }

   //XAResourceFactory interface
   public XAResource getXAResource()
   {
      return this;
   }

   public void returnXAResource()
   {
   }

   
   //Is this used?
   public Xid getXid()
   {
      return (Xid)xids.get();
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
      if (xids.get() != null)
      {
         throw new XAException("Trying to start a second tx!, old: " + xids.get() + ", new: " + xid);
      }
      xids.set(xid);
   }

   /**
    *
    * @param param1 <description>
    * @param param2 <description>
    * @exception javax.transaction.xa.XAException <description>
    */
   public void end(Xid xid, int flags) throws XAException
   {
      if (xid.equals(xids.get()))
      {
         xid = null;
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
      TrunkRequest request = new TrunkRequest();
      request.setOpTxOp(TrunkRequest.REQUEST_PREPARE, xid, transactionTimeout);
      Integer result = (Integer)issueUnwrapped(request);
      return result.intValue();
   }

   /**
    *
    * @param param1 <description>
    * @param param2 <description>
    * @exception javax.transaction.xa.XAException <description>
    */
   public void commit(Xid xid, boolean onePhase) throws XAException
   {
      TrunkRequest request = new TrunkRequest();
      request.setOpTxOp((onePhase)? TrunkRequest.REQUEST_COMMIT_1P : TrunkRequest.REQUEST_COMMIT_2P, xid, transactionTimeout);
      issueUnwrapped(request);
   }

   /**
    *
    * @param param1 <description>
    * @exception javax.transaction.xa.XAException <description>
    */
   public void rollback(Xid xid) throws XAException
   {
      TrunkRequest request = new TrunkRequest();
      request.setOpTxOp(TrunkRequest.REQUEST_ROLLBACK, xid, transactionTimeout);
      issueUnwrapped(request);
   }

   /**
    *
    * @param param1 <description>
    * @exception javax.transaction.xa.XAException <description>
    */
   public void forget(Xid xid) throws XAException
   {
      TrunkRequest request = new TrunkRequest();
      request.setOpTxOp(TrunkRequest.REQUEST_FORGET, xid, transactionTimeout);
      issueUnwrapped(request);
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
      byte op;
      if (flag == XAResource.TMSTARTRSCAN)
      {
         op = TrunkRequest.REQUEST_RECOVER_STARTRSCAN;
      }
      else if (flag == XAResource.TMNOFLAGS)
      {
         op = TrunkRequest.REQUEST_RECOVER_TMNOFLAGS;
      }
      else if (flag == XAResource.TMENDRSCAN)
      {
         op = TrunkRequest.REQUEST_RECOVER_ENDRSCAN;
      }
      else
      {
         throw new XAException("invalid flag: " + flag);
      }

      TrunkRequest request = new TrunkRequest();
      request.setOpTxOp(op, null, transactionTimeout);
      Object result = issueUnwrapped(request);
      return (Xid[])result;
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
      return otherRM instanceof TrunkInvokerXAResource && 
         ((TrunkInvokerXAResource)otherRM).trunkInvokerProxy == trunkInvokerProxy;
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

   private Object issueUnwrapped(TrunkRequest request) throws XAException
   {
      try 
      {
         return trunkInvokerProxy.issue(request);         
      }
      catch (XAException xae)
      {
         throw xae;
      } // end of catch
      catch (Exception e)
      {
         throw new RuntimeException("Unknown exception from xa operation" + e);
      } // end of try-catch
   }      

}// TrunkInvokerXAResource
