/*
 * jBoss, the OpenSource EJB server
 *
 * Distributable under GPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.jaws.jdbc;

import java.lang.reflect.Method;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import java.rmi.RemoteException;

import javax.ejb.FinderException;

import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.plugins.jaws.JPMFindEntitiesCommand;
import org.jboss.ejb.plugins.jaws.deployment.Finder;

/**
 * Keeps a map from finder name to specific finder command, and
 * delegates to the relevant specific finder command.
 * The map is initially populated with the defined finders.
 * It is lazily populated with commands for the magic finders (findAll and
 * findByXXX) as and when they are called.
 *
 * @see <related>
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:shevlandj@kpi.com.au">Joe Shevland</a>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @version $Revision: 1.2 $
 */
public class JDBCFindEntitiesCommand implements JPMFindEntitiesCommand
{
   // Attributes ----------------------------------------------------
   
   private JDBCCommandFactory factory;
   private HashMap knownFinderCommands = new HashMap();
   
   // Constructors --------------------------------------------------
   
   public JDBCFindEntitiesCommand(JDBCCommandFactory factory)
   {
      this.factory = factory;
      
      // Make commands for the defined finders
      
      Iterator definedFinders = factory.getMetaInfo().getFinders();
      while(definedFinders.hasNext())
      {
         Finder f = (Finder)definedFinders.next();
         
         if ( !knownFinderCommands.containsKey(f.getName()) )
         {
            JPMFindEntitiesCommand finderCommand =
               factory.createDefinedFinderCommand(f);
               
            knownFinderCommands.put(f.getName(), finderCommand);
         }
      }
   }
   
   // JPMFindEntitiesCommand implementation -------------------------
   
   public Collection execute(Method finderMethod,
                             Object[] args,
                             EntityEnterpriseContext ctx)
      throws RemoteException, FinderException
   {
      String finderName = finderMethod.getName();
      
      JPMFindEntitiesCommand finderCommand = null;
      
      synchronized(this) {
         // JF: TODO: get rid of this lazy instantiation, which
         // requires synchronization, by doing all specific Finder
         // creation in the constructor (i.e. at deployment time).

         // Do we know a finder command for this method name?

         finderCommand = 
            (JPMFindEntitiesCommand)knownFinderCommands.get(finderName);

         // If we didn't get a finder command, see if we can make one

         if (finderCommand == null)      
         {
            try
            {
               if (finderName.equals("findAll"))
               {
                  finderCommand = factory.createFindAllCommand();
               } else if (finderName.startsWith("findBy"))
               {
                  finderCommand = factory.createFindByCommand(finderMethod);
               }

               // Remember the new finder command
               knownFinderCommands.put(finderName, finderCommand);

            } catch (IllegalArgumentException e)
            {
               factory.getLog().warning(e.getMessage());
            }
         }
      }
      
      // If we now have a finder command, delegate to it,
      // otherwise return an empty collection.
      
      return (finderCommand != null) ?
         finderCommand.execute(finderMethod, args, ctx) : new ArrayList();
   }
}
