/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb;

import java.lang.reflect.Constructor;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.rmi.RemoteException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import javax.ejb.EJBLocalHome;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.TransactionManager;

import org.jboss.deployment.DeploymentException;
import org.jboss.deployment.DeploymentInfo;
import org.jboss.deployment.MainDeployerMBean;

import org.jboss.ejb.plugins.AbstractInstanceCache;
import org.jboss.ejb.plugins.SecurityProxyInterceptor;
import org.jboss.ejb.plugins.StatefulSessionInstancePool;

import org.jboss.logging.Logger;

import org.jboss.management.j2ee.EJB;
import org.jboss.management.j2ee.EJBModule;

import org.jboss.metadata.ApplicationMetaData;
import org.jboss.metadata.BeanMetaData;
import org.jboss.metadata.ConfigurationMetaData;
import org.jboss.metadata.EntityMetaData;
import org.jboss.metadata.InvokerProxyBindingMetaData;
import org.jboss.metadata.MessageDrivenMetaData;
import org.jboss.metadata.MetaData;
import org.jboss.metadata.SessionMetaData;
import org.jboss.metadata.XmlFileLoader;
import org.jboss.metadata.XmlLoadable;

import org.jboss.mx.loading.UnifiedClassLoader;

import org.jboss.security.AuthenticationManager;
import org.jboss.security.RealmMapping;

import org.jboss.system.Registry;
import org.jboss.system.Service;
import org.jboss.system.ServiceControllerMBean;
import org.jboss.system.ServiceMBeanSupport;

import org.jboss.util.NullArgumentException;
import org.jboss.util.jmx.MBeanProxy;
import org.jboss.util.jmx.ObjectNameFactory;

import org.jboss.verifier.BeanVerifier;
import org.jboss.verifier.event.VerificationEvent;
import org.jboss.verifier.event.VerificationListener;

import org.jboss.web.WebClassLoader;
import org.jboss.web.WebServiceMBean;

import org.w3c.dom.Element;

/**
 * An EjbModule represents a collection of beans that are deployed as a
 * unit.
 *
 * <p>The beans may use the EjbModule to access other beans within the same
 *    deployment unit.
 *
 * @see Container
 * @see EJBDeployer
 * 
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:d_jencks@users.sourceforge.net">David Jencks</a>
 * @author <a href="mailto:reverbel@ime.usp.br">Francisco Reverbel</a>
 * @author <a href="mailto:Adrian.Brock@HappeningTimes.com">Adrian.Brock</a>
 * @author <a href="mailto:Scott.Stark@jboss.org">Scott Stark</a>
 * @version $Revision: 1.42 $
 *
 * @jmx:mbean extends="org.jboss.system.ServiceMBean"
 */
public class EjbModule 
   extends ServiceMBeanSupport
   implements EjbModuleMBean
{
   public static final String BASE_EJB_MODULE_NAME ="jboss.j2ee:service=EjbModule";

   public static final ObjectName EJB_MODULE_QUERY_NAME = ObjectNameFactory.create(BASE_EJB_MODULE_NAME + ",*");

   public static String DEFAULT_STATELESS_CONFIGURATION = "Default Stateless SessionBean";
   public static String DEFAULT_STATEFUL_CONFIGURATION = "Default Stateful SessionBean";
   public static String DEFAULT_ENTITY_BMP_CONFIGURATION = "Default BMP EntityBean";
   public static String DEFAULT_ENTITY_CMP_CONFIGURATION = "Default CMP EntityBean";
   public static String DEFAULT_MESSAGEDRIVEN_CONFIGURATION = "Default MesageDriven Bean";
   
   // Constants uses with container interceptor configurations
   public static final int BMT = 1;
   public static final int CMT = 2;
   public static final int ANY = 3;
   
   static final String BMT_VALUE = "Bean";
   static final String CMT_VALUE = "Container";
   static final String ANY_VALUE = "Both";
   
   /** Class logger. */
   private static final Logger log = Logger.getLogger(EjbModule.class);
   
   // Constants -----------------------------------------------------
    
   // Attributes ----------------------------------------------------
   
   /** Stores the containers for this deployment unit. */
   HashMap containers = new HashMap();
   HashMap localHomes = new HashMap();
   
   /** Class loader of this deployment unit. */
   ClassLoader classLoader = null;
   
   /** Name of this deployment unit, url it was deployed from */
   final String name;
   
   private final DeploymentInfo deploymentInfo;   

   /** Module Object Name (JSR-77) **/
   private ObjectName moduleName;

   private ServiceControllerMBean serviceController;

   private final Map moduleData = 
      Collections.synchronizedMap(new HashMap());
   
   // Static --------------------------------------------------------
   
   /**
    * Stores a map of DeploymentInfos to EjbModules.
    * 
    * @todo this is silly, do something else.
    */
   private static HashMap ejbModulesByDeploymentInfo = new HashMap();

   // Public --------------------------------------------------------

   //constructor with mbeanserver

   public EjbModule(final DeploymentInfo di)
   {
      this.deploymentInfo = di;
      String name = deploymentInfo.url.toString();
      if (name.endsWith("/"))
      {
         name = name.substring(0, name.length() - 1);
      }
      this.name = name;
   }

   public Map getModuleDataMap()
   {
      return moduleData;
   }
   
   public Object getModuleData(Object key)
   {
      return moduleData.get(key);
   }
   
   public void putModuleData(Object key, Object value)
   {
      moduleData.put(key, value);
   }
   
   public void removeModuleData(Object key)
   {
      moduleData.remove(key);
   }
 
   /**
    * Add a container to this deployment unit.
    *
    * @param   con  
    */
   private void addContainer(Container con)
   {
      String ejbName = con.getBeanMetaData().getEjbName();
      if(containers.containsKey(ejbName))
         log.warn("Duplicate ejb-name. Container for " + ejbName + " already exists.");
      containers.put(ejbName, con);
      con.setEjbModule(this);
   }

   /**
    * Remove a container from this deployment unit.
    *
    * @param   con  
    */
   public void removeContainer(Container con)
   {
      containers.remove(con.getBeanMetaData().getEjbName());
   }
   
   public void addLocalHome(Container con, EJBLocalHome localHome)
   {
      localHomes.put(con.getBeanMetaData().getEjbName(), localHome);
   }
   
   public void removeLocalHome(Container con)
   {
      localHomes.remove(con.getBeanMetaData().getEjbName());
   }
   
   public EJBLocalHome getLocalHome(Container con)
   {
      return (EJBLocalHome)localHomes.get(con.getBeanMetaData().getEjbName());
   }

   /**
    * Get a container from this deployment unit that corresponds to a given name
    *
    * @param   name  ejb-name name defined in ejb-jar.xml
    *
    * @return  container for the named bean, or null if the container was
    *          not found   
    */
   public Container getContainer(String name)
   {
      return (Container)containers.get(name);
   }

   /**
    * Get all containers in this deployment unit.
    *
    * @return  a collection of containers for each enterprise bean in this 
    *          deployment unit.
    * @jmx:managed-attribute
    */
   public Collection getContainers()
   {
      return containers.values();
   }

   /**
    * Get the class loader of this deployment unit. 
    *
    * @return     
    */
   public ClassLoader getClassLoader()
   {
      return classLoader;
   }

   /**
    * Set the class loader of this deployment unit
    *
    * @param   name  
    */
   public void setClassLoader(ClassLoader cl)
   {
      this.classLoader = cl;
   }
   
   /**
    * Get the URL from which this deployment unit was deployed
    *
    * @return    The URL from which this Application was deployed.
    */
   public URL getURL()
   {
      return deploymentInfo.url;
   }
        
   public ObjectName getModuleName() 
   {
      return moduleName;
   }
   
   public void setModuleName(final ObjectName moduleName) 
   {
      if (moduleName == null)
         throw new NullArgumentException("moduleName");
      
      this.moduleName = moduleName;
   }
        
   // Service implementation ----------------------------------------
   
   protected void createService() throws Exception 
   {
      // Keep track of which deployments are ejbModules
      synchronized(ejbModulesByDeploymentInfo)
      {
         ejbModulesByDeploymentInfo.put(deploymentInfo, this);
      }

      serviceController = (ServiceControllerMBean)
         MBeanProxy.create(ServiceControllerMBean.class,
                           ServiceControllerMBean.OBJECT_NAME,
                           server);
      boolean debug = log.isDebugEnabled();
      log.debug( "Application.start(), begin" );
  
      // Create JSR-77 EJB-Module
      int sepPos = name.lastIndexOf( "/" );
      String lName = name.substring(sepPos >= 0 ? sepPos + 1 : 0);
      
      ObjectName lModule = EJBModule.create(
            server,
            ( deploymentInfo.parent == null ? null : deploymentInfo.parent.shortName ),
            lName,
            deploymentInfo.localUrl,
            getServiceName()
            );
      log.debug("Created module: " + lModule);
      
      if( lModule != null ) 
      {
         setModuleName( lModule );
      }
      
      //Set up the beans in this module.
      try 
      {
         for (Iterator beans = ((ApplicationMetaData) deploymentInfo.metaData).getEnterpriseBeans(); beans.hasNext(); ) 
         {
            BeanMetaData bean = (BeanMetaData) beans.next();
            
            log.info( "Deploying " + bean.getEjbName() );

            Container con = createContainer(bean);
            con.setDeploymentInfo(deploymentInfo);//new daje
            con.setEjbModule(this);//new daje
            con.setBeanMetaData( bean );//new daje
            addContainer(con);
            ObjectName jmxName= con.getJmxName();
            /* Add the container mbean to the deployment mbeans so the state
             of the deployment can be tracked.
            */
            deploymentInfo.addMBean(jmxName);
            server.registerMBean(con, jmxName);
            BeanMetaData metaData = con.getBeanMetaData();
            Collection depends = metaData.getDepends();
            //depend on the ejbModule so the ServiceController will take
            //care of other lifecyle events for us
            depends.add(getServiceName());
            serviceController.create(jmxName, depends);
         }
      }
      catch (Exception e)
      {
         destroyService();
         throw e;
      } // end of try-catch
      
   }

   /*
    * startService and stopService do nothing. The ejbs are started and stopped
    * by ServiceController because they depend on this mbean.
    */

   /**
    * The <code>destroyService</code> method removes the ejb container 
    * mbeans from the mbean server and cleans up the jsr-77 mbean and
    * the lists set up in createService.
    *
    * @exception Exception if an error occurs
    */
   protected void destroyService() throws Exception
   {
      for (Iterator i = containers.values().iterator(); i.hasNext();)
      {
         Container con = (Container)i.next();
         ObjectName jmxName =  con.getJmxName();
         try 
         {
            serviceController.remove(jmxName);
         }
         catch (Throwable e)
         {
            log.error("unexpected exception destroying Container: " + jmxName, e);
         } // end of try-catch
      }
      
      log.info( "Remove JSR-77 EJB Module: " + getModuleName() );
      if (getModuleName() != null) 
      {  
         EJBModule.destroy(server, getModuleName().toString() );
      }

      // Keep track of which deployments are ejbModules
      synchronized(ejbModulesByDeploymentInfo)
      {
         ejbModulesByDeploymentInfo.remove(deploymentInfo);
      }

      this.containers.clear();
      this.localHomes.clear();
   }

   // ******************
   // Container Creation
   // ******************
   
   private Container createContainer(BeanMetaData bean)
      throws Exception
   {
      Container container = null;
      if( bean.isMessageDriven() )
      {
         container = new MessageDrivenContainer();
      }
      else if( bean.isSession() )   // Is session?
      {
         if( ( (SessionMetaData) bean ).isStateless() )   // Is stateless?
         {
            container = new StatelessSessionContainer();
         }
         else   // Stateful
         {
            container = new StatefulSessionContainer();
         }
      }
      else   // Entity
      {
         container = new EntityContainer();
      }
      return container;
   }

}
