/*
* JBoss, the OpenSource J2EE webOS
*
* Distributable under LGPL license.
* See terms of license at gnu.org.
*/
package org.jboss.proxy;




import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Method;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import javax.ejb.EJBHome;
import javax.ejb.EJBHome;
import javax.ejb.EJBMetaData;
import javax.ejb.EJBObject;
import javax.ejb.Handle;
import javax.ejb.HomeHandle;
import javax.ejb.RemoveException;
import javax.management.MBeanServer;
import javax.management.MalformedObjectNameException;
import javax.management.ObjectName;
import javax.transaction.Transaction;
import javax.transaction.TransactionManager;
import org.jboss.invocation.Invocation;
import org.jboss.invocation.InvocationContext;
import org.jboss.invocation.Invoker;
import org.jboss.proxy.Interceptor;
import org.jboss.proxy.ejb.handle.HomeHandleImpl;
import org.jboss.system.client.Client;
import org.jboss.util.jmx.ObjectNameFactory;
import org.jboss.logging.Logger;

/*
import javax.naming.Name;
import org.jboss.ejb.plugins.jrmp.server.JRMPContainerInvoker;
*/
/**
* The client-side proxy for an EJB Home object.
*      
* @author <a href="mailto:marc.fleury@jboss.org">Marc Fleury</a>
* @version $Revision: 1.6 $
*
* <p><b>2001/11/21: marcf</b>
* <ol>
*   <li>Initial checkin
* </ol>
*/
public class TransactionInterceptor
extends Interceptor
{
   // Constants -----------------------------------------------------
   
   public final static String DEFAULT_TM_NAME = "jboss.tm:service=TransactionManagerService";

   public final static String DEFAULT_CLIENT_TM_NAME_STUB = "jboss.client:service=TransactionManagerService,";

   final static private Logger log = Logger.getLogger(TransactionInterceptor.class);

   /** Serial Version Identifier. */
   //   private static final long serialVersionUID = 432426690456622923L;
   
   //We assume that this interceptor can be used with only one tm for one client.
   private TransactionManager tm;

   //only look once for the tx manager.  It should always be there, but if not, don't waste time.
   private boolean noTransactionManager = false;
   
   // Constructors --------------------------------------------------
   
   /**
   * No-argument constructor for externalization.
   */
   public TransactionInterceptor() {}
   
   
   // Public --------------------------------------------------------
   
   public Object invoke(Invocation invocation) 
   throws Throwable
   {
      if (tm == null && !noTransactionManager)
      {
	 MBeanServer server = Client.getMBeanServer();
	 ObjectName transactionManagerServiceName = new ObjectName(DEFAULT_TM_NAME);
	 if (server.isRegistered(transactionManagerServiceName))
	 {
	    tm = (TransactionManager)server.getAttribute(transactionManagerServiceName, "TransactionManager");
	 }
	 else
	 {
	    InvocationContext context = invocation.getInvocationContext();
	    Invoker invoker = context.getInvoker();
	    try
	    {
	       transactionManagerServiceName = 
		  new ObjectName(DEFAULT_CLIENT_TM_NAME_STUB
				 + invoker.getServerID().toObjectNameClause());
	    }
	    catch (MalformedObjectNameException mone)
	    {
	       log.info("Could not construct object name for tm for invoker: " + invoker);
	    }
	    if (server.isRegistered(transactionManagerServiceName))
	    {
	       tm = (TransactionManager)server.getAttribute(transactionManagerServiceName, "TransactionManager");
	    }
	    else
	    {
	       noTransactionManager = true;
	    }
	 }
      }
      if (tm != null)
      {
	 Transaction tx = tm.getTransaction();
	 invocation.setTransaction(tx);
      }
      return getNext().invoke(invocation);
   }

}
