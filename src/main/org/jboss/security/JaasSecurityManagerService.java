/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
 
package org.jboss.security;

import java.io.File;
import java.net.URL;
import java.rmi.server.UnicastRemoteObject;
import java.rmi.RemoteException;
import java.rmi.ServerException;
import java.util.Hashtable;
import java.util.ArrayList;
import java.util.Iterator;

import javax.naming.InitialContext;
import javax.naming.Context;
import javax.naming.Reference;
import javax.naming.RefAddr;
import javax.naming.StringRefAddr;
import javax.naming.Name;
import javax.naming.spi.ObjectFactory;
import javax.naming.spi.NamingManager;
import javax.naming.CommunicationException;
import javax.naming.CannotProceedException;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import javax.security.auth.login.Configuration;

import org.jboss.logging.Log;
import org.jboss.util.ServiceMBeanSupport;

import org.jboss.system.EJBSecurityManager;

import org.jnp.server.NamingServer;
import org.jnp.interfaces.NamingContext;

/**
 *   This is a JMX service which manages JaasSecurityManagers.
 *    JaasSecurityManagers are responsible for validating credentials
 *    associated with principals.
 *      
 *   @see JaasSecurityManager
 *   @author <a href="on@ibis.odessa.ua">Oleg Nitz</a>
 *   @author <a href="rickard@telkel.com">Rickard Oberg</a>
 */
public class JaasSecurityManagerService
        extends ServiceMBeanSupport
        implements JaasSecurityManagerServiceMBean, ObjectFactory {

   MBeanServer server;
   
   static NamingServer srv;
   static Hashtable jsmMap = new Hashtable();

   public String getName()
   {
      return "JAAS Security Manager";
   }

   protected ObjectName getObjectName(MBeanServer server, ObjectName name)
      throws javax.management.MalformedObjectNameException
   {
      this.server = server;
      return new ObjectName(OBJECT_NAME);
   }

   protected void initService() throws Exception
   {
      srv = new NamingServer();
   
      InitialContext ic = new InitialContext();

      // Bind reference to SM subcontext in JNDI
      // Uses JNDI federation to handle the "java:jaas" context ourselves
      RefAddr refAddr = new StringRefAddr("nns", "JSM");
      Reference jsmsRef = new Reference("javax.naming.Context", refAddr,getClass().getName(), null);
      Context ctx = (Context)new InitialContext();
      ctx.rebind("java:/jaas", jsmsRef);
   }

   protected void startService()
   throws Exception
   {
   }

   protected void stopService()
   {
      InitialContext ic;
      try
      {
         ic = new InitialContext();
         ic.unbind("java:/jaas");
      } catch (CommunicationException e)
      {
         // Do nothing, the naming services is already stopped   
      } catch (Exception e)
      {
         log.exception(e);
      }
   }

   // ObjectFactory implementation ----------------------------------

	/**
	 * Object factory implementation. This method is a bit tricky as it is called twice for each
    * JSM lookup. Let's say the lookup is for "java:jaas/MySecurity". Then this will first be 
    * called as JNDI starts the "jaas" federation. In that call we make sure that the next call
    * will go through, i.e. we check that the "MySecurity" binding is availble. Then we return 
    * the implementation of the "jaas" context. Then, when the "MySecurity" is dereferenced we 
    * look up the JSM from an internal static hash table.
    *
    * Note: it is possible to break this by doing the lookup in two phases: first lookup "java:jaas" 
    * and then do a second lookup of "MySecurity". If that is done then the first lookup has no way of
    * knowing what name to check (i.e. it can't make sure that "MySecurity" is available in the 
    * "java:jaas" context!
	 *
	 * @param   obj  
	 * @param   name  
	 * @param   nameCtx  
	 * @param   environment  
	 * @return     
	 * @exception   Exception  
	 */
   public Object getObjectInstance(Object obj, Name name, Context nameCtx, Hashtable environment)
      throws Exception
   {
      if (name != null)
      {
         // Handle JaasSecurityManager lookup
         if (name.size() == 0)
            return nameCtx;
      
         return jsmMap.get(name);
      } else
      {
         // Handle "java:jaas" context
         CannotProceedException cpe = (CannotProceedException)environment.get(NamingManager.CPE);
         Name remainingName = cpe.getRemainingName();
         
         Context ctx = new NamingContext(environment, null, srv);
         
         // Make sure that JSM is available
         try
         {
            srv.lookup(remainingName);
         } catch (Exception e)
         {
            // Not found - add reference to JNDI, and a real JSM to a map
            Reference jsmRef = new Reference(JaasSecurityManager.class.getName(), getClass().getName(), null);
            ctx.rebind(remainingName, jsmRef);
            jsmMap.put(remainingName, new JaasSecurityManager(remainingName.toString()));
         }
         
         return ctx;
      }
   }
}


