/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb.plugins;

import java.rmi.RemoteException;
import java.security.Principal;
import javax.ejb.EJBContext;
import javax.naming.InitialContext;

import org.apache.log4j.Category;

import org.jboss.ejb.Container;
import org.jboss.ejb.ContainerInvokerContainer;
import org.jboss.ejb.EnterpriseContext;
import org.jboss.ejb.MethodInvocation;

import org.jboss.security.EJBSecurityManager;
import org.jboss.security.SecurityProxy;
import org.jboss.security.SecurityProxyFactory;

/**
 * The SecurityProxyInterceptor is where the EJB custom security proxy
 * integration is performed. This interceptor is dynamically added to container
 * interceptors when the deployment descriptors specifies a security
 * proxy. It is added just before the container interceptor so that the
 * interceptor has access to the EJB instance and context.
 * 
 * @author <a href="mailto:Scott_Stark@displayscape.com">Scott Stark</a>.
 * @version $Revision: 1.7 $
 */
public class SecurityProxyInterceptor
   extends AbstractInterceptor
{
   /** 
    * The JNDI name of the SecurityProxyFactory used to wrap security
    * proxy objects that do not implement the SecurityProxy interface
    */
   public final String SECURITY_PROXY_FACTORY_NAME =
      "java:/SecurityProxyFactory";

   /** Instance logger. */
   protected Category log = Category.getInstance(this.getClass());
   
   /**
    * @clientCardinality 0..1
    * @supplierCardinality 1 
    */
   protected Container container;

   protected EJBSecurityManager securityManager;

   /**
    * @supplierCardinality 0..1
    * @clientCardinality 1 
    * @supplierQualifier custom security
    */
   protected SecurityProxy securityProxy;

   public SecurityProxyInterceptor()
   {
      super();
   }

   public void setContainer(Container container)
   {
      this.container = container;
      securityManager = container.getSecurityManager();
      Object secProxy = container.getSecurityProxy();
      if( secProxy != null )
      {
         // If this is not a SecurityProxy instance then use the default
         // SecurityProxy implementation
         if( (secProxy instanceof SecurityProxy) == false )
         {
            try
            {
               // Get default SecurityProxyFactory from JNDI at
               InitialContext iniCtx = new InitialContext();
               SecurityProxyFactory proxyFactory =
                  (SecurityProxyFactory)iniCtx.lookup(SECURITY_PROXY_FACTORY_NAME);
               securityProxy = proxyFactory.create(secProxy);
            }
            catch (Exception e)
            {
               log.error("Failed to initialze DefaultSecurityProxy", e);
            }
         }
         else
         {
            securityProxy = (SecurityProxy) secProxy;
         }

         // Initialize the securityProxy
         try
         {
            ContainerInvokerContainer ic =
               (ContainerInvokerContainer)container;
            Class beanHome = ic.getHomeClass();
            Class beanRemote = ic.getRemoteClass();
            securityProxy.init(beanHome, beanRemote, securityManager);
         }
         catch(Exception e)
         {
            log.error("Failed to initialze SecurityProxy", e);
         }
         log.info("Initialized SecurityProxy=" + securityProxy);
      }
   }

   public Container getContainer()
   {
      return container;
   }

   // Container implementation --------------------------------------
   
   public void start() throws Exception
   {
      super.start();
   }

   public Object invokeHome(MethodInvocation mi) throws Exception
   {
      // Apply any custom security checks
      if( securityProxy != null )
      {
         EJBContext ctx = null;
         EnterpriseContext ectx = mi.getEnterpriseContext();
         if( ectx != null )
            ctx = ectx.getEJBContext();
         Object[] args = mi.getArguments();
         securityProxy.setEJBContext(ctx);
         try
         {
            securityProxy.invokeHome(mi.getMethod(), args);
         }
         catch(SecurityException e)
         {
            Principal principal = mi.getPrincipal();
            String msg = "SecurityProxy.invokeHome exception, principal=" + principal;
            log.error(msg, e);
            SecurityException se = new SecurityException(msg);
            throw new RemoteException("SecurityProxy.invokeHome failure", se);
         }
      }
      return getNext().invokeHome(mi);
   }
   
   public Object invoke(MethodInvocation mi) throws Exception
   {
      // Apply any custom security checks
      if( securityProxy != null )
      {
         Object bean = mi.getEnterpriseContext().getInstance();
         EJBContext ctx = mi.getEnterpriseContext().getEJBContext();
         Object[] args = mi.getArguments();
         securityProxy.setEJBContext(ctx);
         try
         {
            securityProxy.invoke(mi.getMethod(), args, bean);
         }
         catch(SecurityException e)
         {
            Principal principal = mi.getPrincipal();
            String msg = "SecurityProxy.invoke exception, principal="+principal;
            log.error(msg, e);
            SecurityException se = new SecurityException(msg);
            throw new RemoteException("SecurityProxy.invoke failure", se);
         }
      }
      return getNext().invoke(mi);
   }
}
