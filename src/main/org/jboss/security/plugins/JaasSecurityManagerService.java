/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
 
package org.jboss.security.plugins;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Hashtable;
import java.util.ArrayList;
import java.util.Iterator;

import javax.naming.InitialContext;
import javax.naming.Context;
import javax.naming.Reference;
import javax.naming.RefAddr;
import javax.naming.StringRefAddr;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.spi.ObjectFactory;
import javax.naming.spi.NamingManager;
import javax.naming.CommunicationException;
import javax.naming.CannotProceedException;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.jboss.logging.Log;
import org.jboss.security.SecurityProxyFactory;
import org.jboss.util.ServiceMBeanSupport;

import org.jnp.server.NamingServer;
import org.jnp.interfaces.NamingContext;
import org.jboss.util.CachePolicy;

/**
 *   This is a JMX service which manages JAAS based SecurityManagers.
 *    JAAS SecurityManagers are responsible for validating credentials
 *    associated with principals. The service defaults to the 
 *    org.jboss.security.plugins.JaasSecurityManager implementation but
 *    this can be changed via the securityManagerClass property.
 *      
 *   @see JaasSecurityManager
 *   @see SubjectSecurityManager
 *   @author <a href="on@ibis.odessa.ua">Oleg Nitz</a>
 *   @author <a href="rickard@telkel.com">Rickard Oberg</a>
 *   @author <a href="mailto:Scott_Stark@displayscape.com">Scott Stark</a>
 */
public class JaasSecurityManagerService
        extends ServiceMBeanSupport
        implements JaasSecurityManagerServiceMBean, ObjectFactory
{
    /** The class that provides the security manager implementation */
    private static String securityMgrClassName;
    /** The loaded securityMgrClassName */
    private static Class securityMgrClass;
    /** The security credential cache policy, shared by all security mgrs */
    private static CachePolicy cachePolicy;
    private static String cacheJndiName;
    /** The class that provides the SecurityProxyFactory implementation */
    private static String securityProxyFactoryClassName;
    private static Class securityProxyFactoryClass;

    static NamingServer srv;
    static Hashtable jsmMap = new Hashtable();

    public JaasSecurityManagerService()
    {
        try
        {   // Use JaasSecurityManager as the default 
            setSecurityManagerClassName("org.jboss.security.plugins.JaasSecurityManager");
            // Use SubjectSecurityProxyFactory as the default SecurityProxyFactory
            setSecurityProxyFactoryClassName("org.jboss.security.SubjectSecurityProxyFactory");
        }
        catch(ClassNotFoundException e)
        {
        }
    }

    public String getSecurityManagerClassName()
    {
        return securityMgrClassName;
    }
    public void setSecurityManagerClassName(String className)
        throws ClassNotFoundException
    {
        securityMgrClassName = className;
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        securityMgrClass = loader.loadClass(securityMgrClassName);
    }
    public String getSecurityProxyFactoryClassName()
    {
        return securityProxyFactoryClassName;
    }
    public void setSecurityProxyFactoryClassName(String className)
        throws ClassNotFoundException
    {
        securityProxyFactoryClassName = className;
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        securityProxyFactoryClass = loader.loadClass(securityProxyFactoryClassName);
    }
   /** Get the jndi name under which the authentication cache policy is found
    */
    public String getAuthenticationCacheJndiName()
    {
        return cacheJndiName;
    }
   /** Set the jndi name under which the authentication cache policy is found
    */
    public void setAuthenticationCacheJndiName(String jndiName)
    {
        this.cacheJndiName = jndiName;
    }

    public String getName()
    {
        return "JAAS Security Manager";
    }

   protected ObjectName getObjectName(MBeanServer server, ObjectName name)
      throws javax.management.MalformedObjectNameException
   {
      return new ObjectName(OBJECT_NAME);
   }

    protected void startService() throws Exception
    {
        srv = new NamingServer();

        InitialContext ic = new InitialContext();

        // Bind reference to SM subcontext in JNDI
        // Uses JNDI federation to handle the "java:jaas" context ourselves
        RefAddr refAddr = new StringRefAddr("nns", "JSM");
        Reference jsmsRef = new Reference("javax.naming.Context", refAddr,getClass().getName(), null);
        Context ctx = new InitialContext();
        ctx.rebind("java:/jaas", jsmsRef);

        try
        {
            if( cacheJndiName != null )
                cachePolicy = (CachePolicy) ctx.lookup(cacheJndiName);
        }
        catch(NamingException e)
        {
        }
        System.out.println("JAAS.startService, cachePolicy="+cachePolicy);
        // Bind the default SecurityProxyFactory instance under java:/SecurityProxyFactory
        SecurityProxyFactory proxyFactory = (SecurityProxyFactory) securityProxyFactoryClass.newInstance();
        ctx.bind("java:/SecurityProxyFactory", proxyFactory);
        System.out.println("JAAS.startService, SecurityProxyFactory="+proxyFactory);
    }

    protected void stopService()
    {
        InitialContext ic;
        try
        {
            ic = new InitialContext();
            ic.unbind("java:/jaas");
        }
        catch (CommunicationException e)
        {
         // Do nothing, the naming services is already stopped   
        }
        catch (Exception e)
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
            // Handle securityManager lookup
            if (name.size() == 0)
                return nameCtx;
            return jsmMap.get(name);
        }
        else
        {
            // Handle "java:jaas" context
            CannotProceedException cpe = (CannotProceedException)environment.get(NamingManager.CPE);
            Name remainingName = cpe.getRemainingName();

            Context ctx = new NamingContext(environment, null, srv);
            // Make sure that JSM is available
            try
            {
                srv.lookup(remainingName);
            }
            catch(Exception e)
            {
                // Not found - add reference to JNDI, and a real security mgr to a map
                Reference jsmRef = new Reference(securityMgrClass.getName(), getClass().getName(), null);
                ctx.rebind(remainingName, jsmRef);
                String securityDomain = remainingName.toString();
                try
                {   // Create instance of securityMgrClass
                    Class[] parameterTypes = {String.class};
                    Constructor ctor = securityMgrClass.getConstructor(parameterTypes);
                    Object[] args = {securityDomain};
                    Object securityMgr = ctor.newInstance(args);
System.out.println("JAAS.Created securityMgr="+securityMgr);
                    // See if the security mgr supports an externalized cache policy
                    try
                    {
                        parameterTypes[0] = CachePolicy.class;
                        Method m = securityMgrClass.getMethod("setCachePolicy", parameterTypes);
                        args[0] = cachePolicy;
System.out.println("JAAS.setCachePolicy, c="+args[0]);
                       m.invoke(securityMgr, args);
                    }
                    catch(Exception e2)
                    {   // No cache policy support, this is ok
                    }
System.out.println("JAAS.Added "+remainingName+", "+securityMgr+" to map");
                    jsmMap.put(remainingName, securityMgr);
                }
                catch(Exception e2)
                {
e2.printStackTrace();
                    log.exception(e2);
                    throw e2;
                }
            }
            return ctx;
        }
    }
}
