/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.jaws.jdbc;

import java.lang.reflect.Method;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.jboss.ejb.EntityContainer;
import org.jboss.ejb.plugins.jaws.MetaInfo;
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
import org.jboss.ejb.plugins.jaws.JPMStoreEntityCommand;
import org.jboss.ejb.plugins.jaws.JPMActivateEntityCommand;
import org.jboss.ejb.plugins.jaws.JPMPassivateEntityCommand;

import org.jboss.ejb.plugins.jaws.deployment.Finder;

import org.jboss.logging.Log;

/**
 * JAWSPersistenceManager JDBCCommandFactory
 *
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @version $Revision: 1.1 $
 */
public class JDBCCommandFactory implements JPMCommandFactory
{
   // Attributes ----------------------------------------------------
   
   private EntityContainer container;
   private Context javaCtx;
   private MetaInfo metaInfo;
   private Log log;
   
   // These support singletons (within the scope of this factory)
   private JDBCBeanExistsCommand beanExistsCommand;
   private JPMFindEntitiesCommand findEntitiesCommand;
   
   /**
    * Gives compile-time control of tracing.
    */
   public static boolean debug = true;
   
   // Constructors --------------------------------------------------
   
   public JDBCCommandFactory(EntityContainer container,
                             Log log)
      throws Exception
   {
      this.container = container;
      this.javaCtx = (Context)new InitialContext().lookup("java:comp/env");
      this.metaInfo = new MetaInfo(container);
      this.log = log;
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
   
   public MetaInfo getMetaInfo()
   {
      return metaInfo;
   }
   
   public Log getLog()
   {
      return log;
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
   
   public JPMFindEntitiesCommand createFindAllCommand()
   {
      return new JDBCFindAllCommand(this);
   }
   
   public JPMFindEntitiesCommand createDefinedFinderCommand(Finder f)
   {
      return new JDBCDefinedFinderCommand(this, f);
   }
   
   public JPMFindEntitiesCommand createFindByCommand(Method finderMethod)
      throws IllegalArgumentException
   {
      return new JDBCFindByCommand(this, finderMethod);
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
}
