/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */
package org.jboss.ejb;

import java.lang.reflect.Method;
import java.net.URL;
import java.net.MalformedURLException;
import java.security.Principal;
import java.util.Map;
import java.util.Iterator;

import javax.ejb.Handle;
import javax.ejb.HomeHandle;
import javax.ejb.EJBObject;
import javax.ejb.EJBMetaData;
import javax.ejb.CreateException;
import javax.ejb.RemoveException;
import javax.naming.Context;
import javax.naming.Name;
import javax.naming.InitialContext;
import javax.naming.LinkRef;
import javax.naming.NamingException;
import javax.naming.NameNotFoundException;
import javax.transaction.TransactionManager;

import org.jboss.ejb.deployment.jBossEnterpriseBean;
import com.dreambean.ejx.ejb.EnvironmentEntry;
import org.jboss.ejb.deployment.jBossEjbJar;
import org.jboss.ejb.deployment.jBossEjbReference;
import org.jboss.ejb.deployment.jBossResourceReference;
import org.jboss.ejb.deployment.ResourceManagers;
import org.jboss.ejb.deployment.ResourceManager;
import org.jboss.ejb.deployment.JDBCResource;
import org.jboss.ejb.deployment.URLResource;
import org.jboss.logging.Logger;

import org.jnp.interfaces.Naming;
import org.jnp.interfaces.java.javaURLContextFactory;
import org.jnp.server.NamingServer;

/**
 *   <description> 
 *      
 *   @see <related>
 *   @author Rickard Öberg (rickard.oberg@telkel.com)
 *   @version $Revision: 1.7 $
 */
public abstract class Container
{
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   protected Application application;
   protected ClassLoader classLoader;
   protected jBossEnterpriseBean metaData;
   protected InstancePool instancePool;
   protected ContainerInvoker containerInvoker;
   protected TransactionManager transactionManager;
   protected Interceptor interceptor;
   
   protected Class homeInterface;
   protected Class remoteInterface;
   protected Class beanClass;
   
   // Static --------------------------------------------------------

   // Constructors --------------------------------------------------
   
   // Public --------------------------------------------------------

   // Container implementation --------------------------------------
   public void setApplication(Application app) 
   { 
		if (app == null)
			throw new IllegalArgumentException("Null application");
			
      application = app; 
      app.addContainer(this);
   }
   
   public Application getApplication() 
   { 
      return application; 
   }
   
   public void setClassLoader(ClassLoader cl) 
   { 
      this.classLoader = cl; 
   }
   
   public void setMetaData(jBossEnterpriseBean metaData) 
   { 
      this.metaData = metaData; 
   }
   
   public void setContainerInvoker(ContainerInvoker ci) 
   { 
      if (ci == null)
      	throw new IllegalArgumentException("Null invoker");
			
      this.containerInvoker = ci; 
      ci.setContainer(this);
   }
	
   public void setInstancePool(InstancePool ip) 
   { 
      if (ip == null)
      	throw new IllegalArgumentException("Null pool");
			
      this.instancePool = ip; 
      ip.setContainer(this);
   }

   public void setInterceptor(Interceptor in) { this.interceptor = in; }

   public void addInterceptor(Interceptor in) 
   { 
      if (interceptor == null)
      {
         interceptor = in;
      } else
      {
         
         Interceptor current = interceptor;
         while ( current.getNext() != null)
         {
            current = current.getNext();
         }
            
         current.setNext(in);
      }
   }
   
   public ClassLoader getClassLoader(ClassLoader cl) { return classLoader; }
   public jBossEnterpriseBean getMetaData() { return metaData; }
   public ContainerInvoker getContainerInvoker() { return containerInvoker; }
   public InstancePool getInstancePool() { return instancePool; }
   public Interceptor getInterceptor() { return interceptor; }
      
   public TransactionManager getTransactionManager() { return transactionManager; }
   public void setTransactionManager(TransactionManager tm) { transactionManager = tm; }
   
   /*
   * init()
   *
   * The ContainerFactory calls this method.  The ContainerFactory has set all the 
   * plugins and interceptors that this bean requires and now proceeds to initialize
   * the chain.  The method looks for the standard classes in the URL, sets up
   * the naming environment of the bean.
   *
   */
   
   public void init()
      throws Exception
   {
      ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(getClassLoader());
      
      homeInterface = classLoader.loadClass(metaData.getHome());
      remoteInterface = classLoader.loadClass(metaData.getRemote());
      beanClass = classLoader.loadClass(metaData.getEjbClass());
      
      setupEnvironment();
      
      instancePool.init();
      
      containerInvoker.init();
	  
	  
	  // Initialize the interceptor by calling the chain
      Interceptor in = interceptor;
      while (in != null)
      {
		
         in.setContainer(this);
         in.init();
         in = in.getNext();
      }
        
      Thread.currentThread().setContextClassLoader(oldCl);
   }
   
   public void start()
      throws Exception
   {
      ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(getClassLoader());
      
      instancePool.start();
      
      containerInvoker.start();
      
      Interceptor in = interceptor;
      while (in != null)
      {
         in.start();
         in = in.getNext();
      }
      
      Thread.currentThread().setContextClassLoader(oldCl);
   }
   
   public void stop() 
   {
      ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(getClassLoader());
      
      instancePool.stop();
      
      containerInvoker.stop();
      
      Interceptor in = interceptor;
      while (in != null)
      {
         in.stop();
         in = in.getNext();
      }
      
      Thread.currentThread().setContextClassLoader(oldCl);
   }
   
   public void destroy() 
   {
      ClassLoader oldCl = Thread.currentThread().getContextClassLoader();
      Thread.currentThread().setContextClassLoader(getClassLoader());
      
      instancePool.destroy();
      
      containerInvoker.destroy();
      
      Interceptor in = interceptor;
      while (in != null)
      {
         in.destroy();
         in = in.getNext();
      }
      
      Thread.currentThread().setContextClassLoader(oldCl);
   }
   
   /**
    *
    *
    * @return     
    */
   public ClassLoader getClassLoader() { return classLoader; }
   
   public Class getHomeClass()
   {
      if (homeInterface == null)
      {
         try
         {
            homeInterface = classLoader.loadClass(metaData.getHome());
         } catch (Exception e) {}
      }
      return homeInterface;
   }
   
   public Class getRemoteClass()
   {
      if (remoteInterface == null)
      {
         try
         {
            remoteInterface = classLoader.loadClass(metaData.getRemote());
         } catch (Exception e) {}
      }
      return remoteInterface;
   }
   
   public Class getBeanClass()
   {
      if (beanClass == null)
      {
         try
         {
            beanClass = classLoader.loadClass(metaData.getEjbClass());
         } catch (Exception e) {}
      }
      return beanClass;
   }
   
   public abstract Object invokeHome(Method method, Object[] args)
      throws Exception;
      
   public abstract Object invoke(Object id, Method method, Object[] args)
      throws Exception;
      
   // Protected -----------------------------------------------------
   
   // MF WHY: why the protected here, it gives a rather strange structure to the init
   protected abstract Interceptor createContainerInterceptor();
   
   // Private -------------------------------------------------------
   
   /*
   * setupEnvironment
   *
   * This method sets up the naming environment of the bean.
   * it sets the root it creates for the naming in the "BeanClassLoader"
   * that loader shares the root for all instances of the bean and 
   * is part of the "static" metaData of the bean.
   * We create the java: namespace with properties, EJB-References, and 
   * DataSource ressources.
   *
   */
   
   // MF WHY: part of the issue is that some meta data information is 
   // kept in many classes. There 2 repositories of metaData in this case.
   // One is the BeanClassLoader the other the "bean" in Container.
   
   private void setupEnvironment()
      throws DeploymentException
   {
		try
		{
	      NamingServer root = new NamingServer();
	      ((BeanClassLoader)getClassLoader()).setJNDIRoot(root);
	      Context ctx = (Context)new InitialContext().lookup("java:/");
	      ctx.createSubcontext("comp");
	      ctx = ctx.createSubcontext("comp/env");
	      
	      // Bind environment properties
	      {
	         Iterator enum = getMetaData().getEnvironmentEntries();
	         while(enum.hasNext())
	         {
	            EnvironmentEntry entry = (EnvironmentEntry)enum.next();
	            if (entry.getType().equals("java.lang.Integer"))
	            {
	               bind(ctx, entry.getName(), new Integer(entry.getValue()));
	            } else if (entry.getType().equals("java.lang.Long"))
	            {
	               bind(ctx, entry.getName(), new Long(entry.getValue()));
	            } else if (entry.getType().equals("java.lang.Double"))
	            {
	               bind(ctx, entry.getName(), new Double(entry.getValue()));
	            } else if (entry.getType().equals("java.lang.Float"))
	            {
	               bind(ctx, entry.getName(), new Float(entry.getValue()));
	            } else if (entry.getType().equals("java.lang.Byte"))
	            {
	               bind(ctx, entry.getName(), new Byte(entry.getValue()));
	            } else if (entry.getType().equals("java.lang.Short"))
	            {
	               bind(ctx, entry.getName(), new Short(entry.getValue()));
	            } else if (entry.getType().equals("java.lang.Boolean"))
	            {
	               bind(ctx, entry.getName(), new Boolean(entry.getValue()));
	            } else
	            {
	               // Default is string
	               bind(ctx, entry.getName(), entry.getValue());
	            }
	         }
	      }
	      
	      // Bind EJB references
	      {
	         Iterator enum = getMetaData().getEjbReferences();
	         while(enum.hasNext())
	         {
	            jBossEjbReference ref = (jBossEjbReference)enum.next();
	            
	            Name n = ctx.getNameParser("").parse(ref.getLink());
	            ContainerInvoker ci = getContainerInvoker();
	            
	            if (!ref.getJndiName().equals(""))
	            {
	               // External link
	               Logger.debug("Binding "+ref.getName()+" to external JNDI source: "+ref.getJndiName());
	               bind(ctx, ref.getName(), new LinkRef(ref.getJndiName()));
	            }
	            else
	            {
	               // Internal link
	               Logger.debug("Bind "+ref.getName() +" to "+getApplication().getContainer(ref.getLink()).getContainerInvoker().getEJBHome());
	               bind(ctx, ref.getName(), getApplication().getContainer(ref.getLink()).getContainerInvoker().getEJBHome());
	            }
	         }
	      }
	      
	      // Bind resource references
	      {
	         Iterator enum = getMetaData().getResourceReferences();
	         ResourceManagers rms = ((jBossEjbJar)getMetaData().getBeanContext().getBeanContext()).getResourceManagers();
	         while(enum.hasNext())
	         {
	            jBossResourceReference ref = (jBossResourceReference)enum.next();
	            
	            ResourceManager rm = rms.getResourceManager(ref.getResourceName());
	            
					if (rm == null)
						throw new DeploymentException("No resource manager named "+ref.getResourceName());
						
	            if (rm.getType().equals("javax.sql.DataSource"))
	            {
	               JDBCResource res = (JDBCResource)rm;
	               bind(ctx, res.getName(), new LinkRef(res.getJndiName()));
	            } else if (rm.getType().equals("java.net.URL"))
	            {
	               try
	               {
	                  URLResource res = (URLResource)rm;
	                  bind(ctx, res.getName(), new URL(res.getUrl()));
	               } catch (MalformedURLException e)
	               {
	                  throw new NamingException("Malformed URL:"+e.getMessage());
	               }
	            }
	         }
	      }
		} catch (NamingException e)
		{
			throw new DeploymentException("Could not set up environment", e);
		}
   }
   
   private void bind(Context ctx, String name, Object val)
      throws NamingException
   {
      // Bind val to name in ctx, and make sure that all intermediate contexts exist
      
      Name n = ctx.getNameParser("").parse(name);
      while (n.size() > 1)
      {
         String ctxName = n.get(0);
         try
         {
            ctx = (Context)ctx.lookup(ctxName);
         } catch (NameNotFoundException e)
         {
            ctx = ctx.createSubcontext(ctxName);
         }
         n = n.getSuffix(1);
      }
      
      ctx.bind(n.get(0), val);
   }
}
