/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.jaws.jdbc;

import java.lang.reflect.Method;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.DeploymentException;

import org.jboss.ejb.plugins.jaws.JPMCommandFactory;
import org.jboss.ejb.plugins.jaws.JPMInitCommand;
import org.jboss.ejb.plugins.jaws.JPMStartCommand;
import org.jboss.ejb.plugins.jaws.JPMStopCommand;
import org.jboss.ejb.plugins.jaws.JPMDestroyCommand;
import org.jboss.ejb.plugins.jaws.JPMFindEntityCommand;
import org.jboss.ejb.plugins.jaws.JPMFindEntitiesCommand;
import org.jboss.ejb.plugins.jaws.JPMCreateEntityCommand;
import org.jboss.ejb.plugins.jaws.JPMRemoveEntityCommand;
import org.jboss.ejb.plugins.jaws.JPMLoadEntityCommand;
import org.jboss.ejb.plugins.jaws.JPMLoadEntitiesCommand;
import org.jboss.ejb.plugins.jaws.JPMStoreEntityCommand;
import org.jboss.ejb.plugins.jaws.JPMActivateEntityCommand;
import org.jboss.ejb.plugins.jaws.JPMPassivateEntityCommand;

import org.jboss.metadata.ApplicationMetaData;

import org.jboss.ejb.plugins.jaws.metadata.JawsXmlFileLoader;
import org.jboss.ejb.plugins.jaws.metadata.JawsEntityMetaData;
import org.jboss.ejb.plugins.jaws.metadata.JawsApplicationMetaData;
import org.jboss.ejb.plugins.jaws.metadata.FinderMetaData;

import org.jboss.logging.Log;

/**
 * JAWSPersistenceManager JDBCCommandFactory
 *
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @author <a href="danch@nvisia.com">danch (Dan Christopherson</a>
 * @version $Revision: 1.8 $
 */
public class JDBCCommandFactory implements JPMCommandFactory
{
   // Attributes ----------------------------------------------------
   
   private EntityContainer container;
   private Context javaCtx;
   private JawsEntityMetaData metadata;
   private Log log;
   private boolean debug = false;
   
   // These support singletons (within the scope of this factory)
   private JDBCBeanExistsCommand beanExistsCommand;
   private JPMFindEntitiesCommand findEntitiesCommand;
   
   // Constructors --------------------------------------------------
   
   public JDBCCommandFactory(EntityContainer container,
                             Log log)
      throws Exception
   {
      this.container = container;
      this.log = log;
	  
      this.javaCtx = (Context)new InitialContext().lookup("java:comp/env");
      
	  String ejbName = container.getBeanMetaData().getEjbName();
	  ApplicationMetaData amd = container.getBeanMetaData().getApplicationMetaData();
	  JawsApplicationMetaData jamd = (JawsApplicationMetaData)amd.getPluginData("JAWS");
	  
	  if (jamd == null) {
	     // we are the first cmp entity to need jaws. Load jaws.xml for the whole application
		 JawsXmlFileLoader jfl = new JawsXmlFileLoader(amd, container.getClassLoader(), container.getLocalClassLoader(), log);
       jamd = jfl.load();
		 amd.addPluginData("JAWS", jamd);
	  }
     debug = jamd.getDebug();
		  
	  metadata = jamd.getBeanByEjbName(ejbName);
	  if (metadata == null) {
		  throw new DeploymentException("No metadata found for bean " + ejbName);
	  }
   }
   
   // Public --------------------------------------------------------
   
   public EntityContainer getContainer()
   {
      return container;
   }
   
   public Context getJavaCtx()
   {
      return javaCtx;
   }
   
   public JawsEntityMetaData getMetaData()
   {
      return metadata;
   }
   
   public Log getLog()
   {
      return log;
   }
   
   public boolean getDebug() 
   {
      return debug;
   }
   
   // Additional Command creation
   
   /**
    * Singleton: multiple callers get references to the 
    * same command instance.
    */
   public JDBCBeanExistsCommand createBeanExistsCommand()
   {
      if (beanExistsCommand == null)
      {
         beanExistsCommand = new JDBCBeanExistsCommand(this);
      }
      
      return beanExistsCommand;
   }
   
   public JPMFindEntitiesCommand createFindAllCommand(FinderMetaData f)
   {
      return new JDBCFindAllCommand(this, f);
   }
   
   public JPMFindEntitiesCommand createDefinedFinderCommand(FinderMetaData f)
   {
      return new JDBCDefinedFinderCommand(this, f);
   }
   
   public JPMFindEntitiesCommand createFindByCommand(Method finderMethod, FinderMetaData f)
      throws IllegalArgumentException
   {
      return new JDBCFindByCommand(this, finderMethod, f);
   }
   
   // JPMCommandFactory implementation ------------------------------
   
   // lifecycle commands
   
   public JPMInitCommand createInitCommand()
   {
      return new JDBCInitCommand(this);
   }
   
   public JPMStartCommand createStartCommand()
   {
      return new JDBCStartCommand(this);
   }
   
   public JPMStopCommand createStopCommand()
   {
      return new JDBCStopCommand(this);
   }
   
   public JPMDestroyCommand createDestroyCommand()
   {
      return new JDBCDestroyCommand(this);
   }
   
   // entity persistence-related commands
   
   public JPMFindEntityCommand createFindEntityCommand()
   {
      return new JDBCFindEntityCommand(this);
   }
   
   /**
    * Singleton: multiple callers get references to the 
    * same command instance.
    */
   public JPMFindEntitiesCommand createFindEntitiesCommand()
   {
      if (findEntitiesCommand == null)
      {
         findEntitiesCommand = new JDBCFindEntitiesCommand(this);
      }
      
      return findEntitiesCommand;
   }
   
   public JPMCreateEntityCommand createCreateEntityCommand()
   {
      return new JDBCCreateEntityCommand(this);
   }
   
   public JPMRemoveEntityCommand createRemoveEntityCommand()
   {
      return new JDBCRemoveEntityCommand(this);
   }
   
   public JPMLoadEntityCommand createLoadEntityCommand()
   {
      return new JDBCLoadEntityCommand(this);
   }
   
   public JPMLoadEntitiesCommand createLoadEntitiesCommand()
   {
      return new JDBCLoadEntitiesCommand(this);
   }
   
   public JPMStoreEntityCommand createStoreEntityCommand()
   {
      return new JDBCStoreEntityCommand(this);
   }
   
   // entity activation and passivation commands
   
   public JPMActivateEntityCommand createActivateEntityCommand()
   {
      return new JDBCActivateEntityCommand(this);
   }
   
   public JPMPassivateEntityCommand createPassivateEntityCommand()
   {
      return new JDBCPassivateEntityCommand(this);
   }
   
   
   // Private -------------------------------------------------------
}
