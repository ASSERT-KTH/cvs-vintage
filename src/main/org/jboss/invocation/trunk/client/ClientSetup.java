
/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 *
 */

package org.jboss.invocation.trunk.client;

import javax.management.Attribute;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import org.jboss.proxy.ProxyXAResource;
import org.jboss.system.client.Client;
import org.jboss.tm.JBossXidFactory;
import org.jboss.tm.client.ClientTransactionManager;
import org.jboss.tm.client.ClientUserTransaction;
import org.jboss.util.jmx.ObjectNameFactory;





/**
 * ClientSetup.java has static methods to create the client side
 * mbeans needed to make the Trunk invoker work.
 *
 *
 * Created: Sun Dec  1 15:38:29 2002
 *
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version
 */

public class ClientSetup {

   private static final ObjectName TRANSACTION_MANAGER_SERVICE = ObjectNameFactory.create("jboss.tm:service=TransactionManagerService");

   private static final Object[] noArgs = new Object[0];
   private static final String[] noTypes = new String[0];


   private ClientSetup() {
      
   }

   public static TrunkInvokerProxy setUpClient(TrunkInvokerProxy tip) throws Exception
   {
      //Get our mbean server.
      MBeanServer server = Client.getMBeanServer();
      //Where are we coming from?
      String serverIdObjectNameClause = tip.getServerID().toObjectNameClause();

      ObjectName trunkInvokerProxyName = ObjectNameFactory.create("jboss.client:service=TrunkInvokerProxy," + serverIdObjectNameClause);

      //Have we already been set up? If so, return the previous instance.
      if (server.isRegistered(trunkInvokerProxyName))
      {
         return (TrunkInvokerProxy)server.getAttribute(trunkInvokerProxyName, "TrunkInvokerProxy");
      }


      //This part is independent of which invoker we are using...
      //Set up the client transaction manager for this vm and remote server, if there is no "real" jboss tm.
      ObjectName tmName = TRANSACTION_MANAGER_SERVICE;
      if (!server.isRegistered(tmName))
      {
         ObjectName xidFactoryName = ObjectNameFactory.create("jboss.client:service=XidFactory," + serverIdObjectNameClause);
         //Yikes it's an xmbean!
         Client.createXMBean(JBossXidFactory.class.getName(), xidFactoryName, "org/jboss/tm/JBossXidFactory.xml");
         //needs work
         server.setAttribute(xidFactoryName, new Attribute("BaseGlobalId", serverIdObjectNameClause + "_client"));
         //server.setAttribute(xidFactoryName, new Attribute("TxLoggerName", ??));

         tmName = ObjectNameFactory.create("jboss.client:service=TransactionManager," + serverIdObjectNameClause);
         server.createMBean(ClientTransactionManager.class.getName(), tmName);
         server.setAttribute(tmName, new Attribute("XidFactoryName", xidFactoryName));

         ObjectName clientUTName = ObjectNameFactory.create("jboss.client:service=UserTransaction," + serverIdObjectNameClause);
         server.createMBean(ClientUserTransaction.class.getName(), clientUTName);
         server.setAttribute(clientUTName, new Attribute("TransactionManagerName", tmName));

         //create everything
         server.invoke(xidFactoryName, "create", noArgs, noTypes);
         server.invoke(tmName, "create", noArgs, noTypes);
         server.invoke(clientUTName, "create", noArgs, noTypes);

         //start everything
         server.invoke(xidFactoryName, "start", noArgs, noTypes);
         server.invoke(tmName, "start", noArgs, noTypes);
         server.invoke(clientUTName, "start", noArgs, noTypes);
      }


      ObjectName proxyXAResourceName = ObjectNameFactory.create("jboss.client:service=ProxyXAResource," + serverIdObjectNameClause);
      if (!server.isRegistered(proxyXAResourceName))
      {
	 server.createMBean(ProxyXAResource.class.getName(), proxyXAResourceName);
      }
      server.setAttribute(proxyXAResourceName, new Attribute("Invoker", tip));
      server.setAttribute(proxyXAResourceName, new Attribute("TransactionManagerService", tmName));


      ObjectName workManagerName = ObjectNameFactory.create("jboss.client:service=TrunkInvokerWorkManager," + serverIdObjectNameClause);
      if (!server.isRegistered(workManagerName))
      {
         server.createMBean("org.jboss.resource.work.BaseWorkManager", 
                            workManagerName);
      }
      server.setAttribute(workManagerName, new Attribute("MaxThreads", new Integer(50)));

      ObjectName trunkInvokerConnectionManagerName = ObjectNameFactory.create("jboss.client:service=TrunkInvokerConnectionManager," + serverIdObjectNameClause);
      if (!server.isRegistered(trunkInvokerConnectionManagerName))
      {
         server.createMBean(ConnectionManager.class.getName(), trunkInvokerConnectionManagerName);
      }
      server.setAttribute(trunkInvokerConnectionManagerName, new Attribute("WorkManagerName", workManagerName));


      //register the trunk invoker proxy
      server.registerMBean(tip, trunkInvokerProxyName);

      server.setAttribute(trunkInvokerProxyName, new Attribute("ConnectionManagerName", trunkInvokerConnectionManagerName));
      server.setAttribute(trunkInvokerProxyName, new Attribute("XAResourceFactoryName", proxyXAResourceName));


      //create everything
      server.invoke(workManagerName, "create", noArgs, noTypes);
      server.invoke(trunkInvokerConnectionManagerName, "create", noArgs, noTypes);
      server.invoke(proxyXAResourceName, "create", noArgs, noTypes);
      server.invoke(trunkInvokerProxyName, "create", noArgs, noTypes);

      //start everything
      server.invoke(workManagerName, "start", noArgs, noTypes);
      server.invoke(trunkInvokerConnectionManagerName, "start", noArgs, noTypes);
      server.invoke(proxyXAResourceName, "start", noArgs, noTypes);
      server.invoke(trunkInvokerProxyName, "start", noArgs, noTypes);

      return tip;
   }
   
}// ClientSetup
