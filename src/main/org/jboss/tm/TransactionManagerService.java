/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
 
package org.jboss.tm;

import java.io.File;
import java.net.URL;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Hashtable;

import javax.naming.InitialContext;
import javax.naming.Context;
import javax.naming.Reference;
import javax.naming.Name;
import javax.naming.spi.ObjectFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import javax.transaction.TransactionManager;

import org.jboss.logging.Log;
import org.jboss.util.ServiceMBeanSupport;

/**
 *   This is a JMX service which manages the TransactionManager.
 *    The service creates it and binds a Reference to it into JNDI.
 *      
 *   @see TxManager
 *   @author Rickard Öberg (rickard.oberg@telkel.com)
 *   @version $Revision: 1.5 $
 */
public class TransactionManagerService
   extends ServiceMBeanSupport
   implements TransactionManagerServiceMBean, ObjectFactory
{
   // Constants -----------------------------------------------------
   public static String JNDI_NAME = "java:/TransactionManager";
    
   // Attributes ----------------------------------------------------
    MBeanServer server;
    
    int timeout;
   
   // Static --------------------------------------------------------
   static TxManager tm;

   // ServiceMBeanSupport overrides ---------------------------------
   public String getName()
   {
      return "Transaction manager";
    }
   
   protected ObjectName getObjectName(MBeanServer server, ObjectName name)
      throws javax.management.MalformedObjectNameException
   {
    this.server = server;
      return new ObjectName(OBJECT_NAME);
   }
    
   protected void startService()
      throws Exception
   {
       // Create a new TM
       tm = new TxManager();
       
       // Set timeout
       tm.setTransactionTimeout(timeout);
        
       // Bind reference to TM in JNDI
        // TODO: Move this to start when relationships are in place
       Reference ref = new Reference(tm.getClass().toString(), getClass().getName(), null);
       new InitialContext().bind(JNDI_NAME, ref);
   }
    
   protected void stopService()
   {
        try
        {
            // Remove TM from JNDI
            new InitialContext().unbind(JNDI_NAME);
        } catch (Exception e)
        {
            log.exception(e);
        }
   }
    
   public int getTransactionTimeout() {
      return timeout;
   }

   public void setTransactionTimeout(int timeout) {
      this.timeout = timeout;
   }

    // ObjectFactory implementation ----------------------------------
    public Object getObjectInstance(Object obj,
                                Name name,
                                Context nameCtx,
                                Hashtable environment)
                         throws Exception
    {
        // Return the transaction manager
        return tm;
    }
}

