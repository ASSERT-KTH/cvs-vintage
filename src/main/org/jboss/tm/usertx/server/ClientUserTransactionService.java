/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
 
package org.jboss.tm.usertx.server;

import java.rmi.server.UnicastRemoteObject;

import javax.naming.InitialContext;
import javax.naming.Context;
import javax.naming.Reference;
import javax.naming.Name;
import javax.naming.spi.ObjectFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import javax.transaction.TransactionManager;

import org.jboss.system.ServiceMBeanSupport;

import org.jboss.tm.usertx.client.ClientUserTransaction;
import org.jboss.tm.usertx.client.ServerVMClientUserTransaction;
import org.jboss.tm.usertx.interfaces.UserTransactionSessionFactory;
import org.jboss.tm.usertx.interfaces.UserTransactionStartedListener;

import org.jboss.management.j2ee.JTAResource;

/**
 *  This is a JMX service handling the serverside of UserTransaction
 *  usage for standalone clients.
 *      
 * @author <a href="mailto:osh@sparre.dk">Ole Husgaard</a>
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @version $Revision: 1.7 $
 *
 * @jmx:mbean name="jboss.tm.jca:service=ClientUserTransactionService"
 *            extends="org.jboss.system.ServiceMBean"
 */
public class ClientUserTransactionService
   extends ServiceMBeanSupport
   implements ClientUserTransactionServiceMBean
{

   // Constants -----------------------------------------------------

   public static String JNDI_NAME = "UserTransaction";
   public static String FACTORY_NAME = "UserTransactionSessionFactory";
    

   // Attributes ----------------------------------------------------

   private ObjectName cachedConnectionManager;

   private ObjectName transactionManagerService;

   private ServerVMClientUserTransaction inVMut;

   // Keep a reference to avoid DGC.
   private UserTransactionSessionFactory factory;

    
   
   
   /**
    * mbean get-set pair for field cachedConnectionManager
    * Get the value of cachedConnectionManager
    * @return value of cachedConnectionManager
    *
    * @jmx:managed-attribute
    */
   public ObjectName getCachedConnectionManager()
   {
      return cachedConnectionManager;
   }
   
   
   /**
    * Set the value of cachedConnectionManager
    * @param cachedConnectionManager  Value to assign to cachedConnectionManager
    *
    * @jmx:managed-attribute
    */
   public void setCachedConnectionManager(ObjectName cachedConnectionManager)
   {
      this.cachedConnectionManager = cachedConnectionManager;
   }
   
   
   
   /**
    * mbean get-set pair for field transactionManagerService
    * Get the value of transactionManagerService
    * @return value of transactionManagerService
    *
    * @jmx:managed-attribute
    */
   public ObjectName getTransactionManagerService()
   {
      return transactionManagerService;
   }
   
   
   /**
    * Set the value of transactionManagerService
    * @param transactionManagerService  Value to assign to transactionManagerService
    *
    * @jmx:managed-attribute
    */
   public void setTransactionManagerService(ObjectName transactionManagerService)
   {
      this.transactionManagerService = transactionManagerService;
   }
   
   



   // ServiceMBeanSupport overrides ---------------------------------

   public String getName()
   {
      return "Client UserTransaction service";
   }
   
   
   protected void createService()
      throws Exception
   {
      JTAResource.create( getServer(), "ClientUserTransactionService", getServiceName() );
   }
   
   protected void destroyService()
   {
      JTAResource.destroy( getServer(), "ClientUserTransactionService" );
   }
   
   protected void startService()
      throws Exception
   {
      UserTransactionStartedListener utsl = (UserTransactionStartedListener)getServer().getAttribute(cachedConnectionManager, "Instance");

      TransactionManager tm = (TransactionManager)getServer().getAttribute(transactionManagerService, "TransactionManager");

      inVMut =  new ServerVMClientUserTransaction(tm, utsl);

      factory = new UserTransactionSessionFactoryImpl();
      
      Context ctx = new InitialContext();
      ctx.bind(FACTORY_NAME, factory);
      ctx.bind(JNDI_NAME, ClientUserTransaction.getSingleton());
   }
    
   protected void stopService()
   {
      try {
         Context ctx = new InitialContext();
         ctx.unbind(FACTORY_NAME);
         ctx.unbind(JNDI_NAME);

         inVMut.clearSingleton();
         inVMut = null;
      
         // Force unexport, and drop factory reference.
         try {
            UnicastRemoteObject.unexportObject(factory, true);
         } catch (Exception ex) {
            log.error("Failed to unexportObject", ex);
         }
         factory = null;
      } catch (Exception e) {
          log.error("Failed to unbind", e);
      }
   }
    
}
