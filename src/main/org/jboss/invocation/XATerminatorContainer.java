
/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 *
 */

package org.jboss.invocation;

import java.lang.reflect.Method;
import javax.management.ObjectName;
import javax.resource.spi.XATerminator;
import javax.transaction.xa.Xid;
import org.jboss.system.ServiceMBean;
import org.jboss.system.ServiceMBeanSupport;
import org.jboss.tm.XATerminatorMethods;
import org.jboss.system.Registry;



/**
 * XATerminatorContainer.java
 *
 *
 * Created: Thu Feb  6 10:52:17 2003
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version
 * @todo finish making XATerminatorContainer an xmbean.
 *
 * @jmx.mbean extends="org.jboss.system.ServiceMBean"
 *            name="jboss.invokers:service=XATerminatorContainer" 
 *            display-name="XATerminator Container"
 *            description="MBean that accepts ejb-container-like invocations to the XATerminator"
 *
 * @jboss.service servicefile="invokers"
 * @jboss.xmbean 
 */

public class XATerminatorContainer
   extends ServiceMBeanSupport
   implements XATerminatorContainerMBean
{
   private ObjectName transactionManagerService;

   private XATerminator xat;

   public XATerminatorContainer()
   {
      
   }
   

   /**
    * Get the TransactionManagerService value.
    * @return the TransactionManagerService value.
    *
    * @jmx.managed-attribute description="The TransactionManagerService to get the XATerminator from"
    *                        access="read-write"
    *                        value="jboss.tm:service=TransactionManagerService"
    */
   public ObjectName getTransactionManagerService()
   {
      return transactionManagerService;
   }

   /**
    * Set the TransactionManagerService value.
    * @param transactionManagerService The new TransactionManagerService value.
    *
    * @jmx.managed-attribute
    */
   public void setTransactionManagerService(ObjectName transactionManagerService)
   {
      this.transactionManagerService = transactionManagerService;
   }

   

   /**
    * The <code>invoke</code> method 
    *
    * @param invocation an <code>Invocation</code> value
    * @return an <code>Object</code> value
    * @exception Exception if an error occurs
    *
    * @jmx.managed-operation description="This invoke method is the ejb-container-like way an invoker calls methods on the XATerminator exposed by the transaction manager."
    *      impact="ACTION"
    */
   public Object invoke(Invocation invocation) throws Exception
   {
      if (invocation instanceof MarshalledInvocation)
      {
	 ((MarshalledInvocation)invocation).setMethodMap(XATerminatorMethods.methodHashToMethodMap);
      } // end of if ()
      
      Method m = invocation.getMethod();
      Object[] args = invocation.getArguments();
      if (m.equals(XATerminatorMethods.PREPARE_METHOD))
      {
	 if (log.isTraceEnabled())
	 {
	    log.trace("prepare, xid: " + args[0]); 
	 } // end of if ()
	 int result = xat.prepare((Xid)args[0]);
	 return new Integer(result);
      } // end of if ()
      
      if (m.equals(XATerminatorMethods.COMMIT_METHOD))
      {
	 if (log.isTraceEnabled())
	 {
	    log.trace("commit, xid: " + args[0] + ", onePhase: " +  args[1]); 
	 } // end of if ()
	 xat.commit((Xid)args[0], ((Boolean)args[1]).booleanValue());
	 return null;
      } // end of if ()
      
      if (m.equals(XATerminatorMethods.ROLLBACK_METHOD))
      {
	 if (log.isTraceEnabled())
	 {
	    log.trace("rollback, xid: " + args[0]); 
	 } // end of if ()
	 xat.rollback((Xid)args[0]);
	 return null;
      } // end of if ()
      
      if (m.equals(XATerminatorMethods.FORGET_METHOD))
      {
	 if (log.isTraceEnabled())
	 {
	    log.trace("forget, xid: " + args[0]); 
	 } // end of if ()
	 xat.forget((Xid)args[0]);
	 return null;
      } // end of if ()
      
      if (m.equals(XATerminatorMethods.RECOVER_METHOD))
      {
	 if (log.isTraceEnabled())
	 {
	    log.trace("recover, flag: " + args[0]); 
	 } // end of if ()
	 Xid[] result = xat.recover(((Integer)args[0]).intValue());
	 return result;
      } // end of if ()

      throw new IllegalArgumentException("Unrecognized method: " + m);      
      
   }

   protected void startService() throws Exception
   {
      xat = (XATerminator)getServer().getAttribute(transactionManagerService, "XATerminator");
      //Bind ourself in the Registry so invokers can use the
      //XATerminator methods just as if we were an ejb.
      Integer hash = new Integer(getServiceName().hashCode());
      Registry.bind(hash, getServiceName());
   }

   protected void stopService() throws Exception
   {
      Integer hash = new Integer(getServiceName().hashCode());
      Registry.unbind(hash);
      xat = null;
   }

}// XATerminatorContainer
