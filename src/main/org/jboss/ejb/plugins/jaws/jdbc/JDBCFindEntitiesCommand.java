/*
 * JBoss, the OpenSource J2EE webOS
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.jaws.jdbc;

import java.lang.reflect.Method;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.plugins.jaws.JPMFindEntitiesCommand;
import org.jboss.ejb.plugins.jaws.metadata.FinderMetaData;
import org.jboss.ejb.plugins.jaws.bmp.CustomFindByEntitiesCommand;

import org.jboss.logging.Logger;

import org.jboss.util.FinderResults;

/**
 * Keeps a map from finder name to specific finder command, and
 * delegates to the relevant specific finder command.
 *
 * @see org.jboss.ejb.plugins.jaws.JPMFindEntitiesCommand
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard �berg</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:shevlandj@kpi.com.au">Joe Shevland</a>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @version $Revision: 1.16 $
 *
 *   <p><b>Revisions:</b>
 *
 *   <p><b>20010812 vincent.harcq@hubmethods.com:</b>
 *   <ul>
 *   <li> Get Rid of debug flag, use log4j instead
 *   <li> Make load of automated finder method work with local home interfaces
 *   </ul>
 *
 */
public class JDBCFindEntitiesCommand implements JPMFindEntitiesCommand
{
   // Attributes ----------------------------------------------------

   private JDBCCommandFactory factory;
   private HashMap knownFinderCommands = new HashMap();

   private Logger log = Logger.getLogger(this.getClass());

   // Constructors --------------------------------------------------

   public JDBCFindEntitiesCommand(JDBCCommandFactory factory)
   {
      this.factory = factory;

      boolean debug = log.isDebugEnabled();

      // If finder method uses custom implementation, then it is used. This overrides
      // defined and automatic finders.
      Class ejbClass = null;
      try {
      	  ejbClass =
             factory.getContainer()
                    .getClassLoader()
                    .loadClass(factory.getMetaData().getEntity().getEjbClass());

	      Method[] customMethods = ejbClass.getMethods();

	      for (int i = 0; i < customMethods.length; i++)
	      {
	         Method m = customMethods[i];
	         String name = m.getName();
			 if (name.startsWith("ejbFindBy")) {
				 String remoteName = "f"+name.substring(4);
				 try {
					 knownFinderCommands.put(remoteName, new CustomFindByEntitiesCommand(m));
           if (debug)
              log.debug("Added custom finder " + remoteName +".");
				 } catch (IllegalArgumentException e) {
				    log.error("Could not create the custom finder " + remoteName+".", e);
				 }
			 }
		  }
	  } catch (Exception e) {
        // for some reason, this failed; try to use defined or automatic instead
        log.warn("Error initializing custom finder", e);
	  }

      // Make commands for the defined finders

      Iterator definedFinders = factory.getMetaData().getFinders();
      while(definedFinders.hasNext())
      {
         FinderMetaData f = (FinderMetaData)definedFinders.next();

         if ( !knownFinderCommands.containsKey(f.getName()) )
         {
            JPMFindEntitiesCommand finderCommand = null;
            if (f.getName().equals("findAll")) {
               finderCommand = factory.createFindAllCommand(f);
            } else {
               finderCommand = factory.createDefinedFinderCommand(f);
            }

            knownFinderCommands.put(f.getName(), finderCommand);
         }
      }

      // Make commands for any autogenerated finders required
      Method[] homeMethods;
      Method[] localHomeMethods;
      if (debug)
         log.debug("AutoGenerated finders  - Home="
            + factory.getContainer().getHomeClass()
            + " -- LocalHome=" + factory.getContainer().getLocalHomeClass());
      if (factory.getContainer().getHomeClass() != null)
      {
         homeMethods = factory.getContainer().getHomeClass().getMethods();
      }
      else homeMethods = new Method[0] ;
      if (factory.getContainer().getLocalHomeClass() != null)
      {
          localHomeMethods = factory.getContainer().getLocalHomeClass().getMethods();
      }
      else localHomeMethods = new Method[0] ;
      Method[] allHomeMethods = new Method[homeMethods.length + localHomeMethods.length];

      for (int i = 0; i < homeMethods.length; i++)
      {
         allHomeMethods[i] = homeMethods[i];
      }
      for (int i = 0; i < localHomeMethods.length; i++)
      {
         allHomeMethods[homeMethods.length + i] = localHomeMethods[i];
      }

      for (int i = 0; i < allHomeMethods.length; i++)
      {

         Method m = allHomeMethods[i];
         String name = m.getName();

         if (!knownFinderCommands.containsKey(name))
         {
            if (name.equals("findAll"))
            {
               if (debug)
                  log.debug("Save AutoGenerated "+name+"  "+m);
               FinderMetaData f = new FinderMetaData("findAll");
               knownFinderCommands.put(name, factory.createFindAllCommand(f));
            } else if (name.startsWith("findBy")  && !name.equals("findByPrimaryKey"))
            {
               try
               {
                  if (debug)
                     log.debug("Save AutoGenerated "+name+"  "+m);
                  FinderMetaData f = new FinderMetaData(name);
                  knownFinderCommands.put(name, factory.createFindByCommand(m, f));
               } catch (IllegalArgumentException e)
               {
                  if (debug)
                     log.debug("Could not create the finder " + name +
                        ", because no matching CMP field was found.", e);
               }
            }
         }
      }

   }

   // JPMFindEntitiesCommand implementation -------------------------

   public FinderResults execute(Method finderMethod,
                             Object[] args,
                             EntityEnterpriseContext ctx)
      throws Exception
   {
      String finderName = finderMethod.getName();

      JPMFindEntitiesCommand finderCommand = null;

      finderCommand =
         (JPMFindEntitiesCommand)knownFinderCommands.get(finderName);

      // If we found a finder command, delegate to it,
      // otherwise return an empty collection.

      // JF: Shouldn't tolerate the "not found" case!

      return (finderCommand != null) ?
         finderCommand.execute(finderMethod, args, ctx) : new FinderResults(new ArrayList(),null,null,null);
   }
}
