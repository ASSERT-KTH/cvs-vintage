/*
 * JBoss, the OpenSource EJB server
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.jboss.ejb.plugins.cmp.jdbc;

import java.lang.reflect.Method;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import java.rmi.RemoteException;

import javax.ejb.FinderException;

import org.jboss.ejb.EntityEnterpriseContext;
import org.jboss.ejb.plugins.cmp.FindEntitiesCommand;
import org.jboss.ejb.plugins.cmp.jdbc.metadata.JDBCQueryMetaData;
import org.jboss.ejb.plugins.cmp.bmp.CustomFindByEntitiesCommand;
import org.jboss.util.FinderResults;

/**
 * Keeps a map from finder name to specific finder command, and
 * delegates to the relevant specific finder command.
 *
 * @see org.jboss.ejb.plugins.cmp.FindEntitiesCommand
 * @author <a href="mailto:dain@daingroup.com">Dain Sundstrom</a>
 * @author <a href="mailto:rickard.oberg@telkel.com">Rickard Öberg</a>
 * @author <a href="mailto:marc.fleury@telkel.com">Marc Fleury</a>
 * @author <a href="mailto:shevlandj@kpi.com.au">Joe Shevland</a>
 * @author <a href="mailto:justin@j-m-f.demon.co.uk">Justin Forder</a>
 * @version $Revision: 1.1 $
 */
public class JDBCFindEntitiesCommand implements FindEntitiesCommand {
	// Attributes ----------------------------------------------------
	private Map knownFinderCommands;
	
	// Constructors --------------------------------------------------
	
	public JDBCFindEntitiesCommand(JDBCStoreManager manager) {
		JDBCCommandFactory factory = manager.getCommandFactory();		
		knownFinderCommands = new HashMap();
		
		//
		// Custom finders - Overrides defined and automatic finders.
		//
		try {
			Class ejbClass = manager.getContainer().getClassLoader().loadClass(
						manager.getMetaData().getEntity().getEjbClass());

			Method[] customMethods = ejbClass.getMethods();	      
			for (int i = 0; i < customMethods.length; i++) {
				Method m = customMethods[i];
				String name = m.getName();
				if(name.startsWith("ejbFindBy")) {
					knownFinderCommands.put(m, new CustomFindByEntitiesCommand(m));
					manager.getLog().debug("Added custom finder " + name +".");
				}
			}
		} catch (Exception e) {
			// for some reason, this failed; try to use defined or automatic instead
			manager.getLog().debug(e);
		}

		//
		// Defined finders - Overrides automatic finders.
		//
		try {
			Iterator definedFinders = manager.getMetaData().getQueries();
			while(definedFinders.hasNext()) {
				JDBCQueryMetaData q = (JDBCQueryMetaData)definedFinders.next();

				if(!knownFinderCommands.containsKey(q.getMethod()) ) {
					knownFinderCommands.put(q.getMethod(), factory.createDefinedFinderCommand(q));
				}
			}
		} catch (Exception e) {
			// for some reason, this failed; try to use automatic instead
			manager.getLog().debug(e);
		}
		
		//
		// Automatic finders - The last resort
		//
		Method[] homeMethods = manager.getContainer().getHomeClass().getMethods();
		for (int i = 0; i < homeMethods.length; i++) {
			Method m = homeMethods[i];
			
			if(!knownFinderCommands.containsKey(m)) {
				String name = m.getName();
				if(name.equals("findAll")) {
					JDBCQueryMetaData q = new JDBCQueryMetaData(m, manager.getMetaData());
					knownFinderCommands.put(m, factory.createFindAllCommand(q));
				} else if(name.startsWith("findBy")  && !name.equals("findByPrimaryKey")) {
					try {
						JDBCQueryMetaData q = new JDBCQueryMetaData(m, manager.getMetaData());
						knownFinderCommands.put(m, factory.createFindByCommand(q));
					} catch (IllegalArgumentException e) {
						manager.getLog().debug("Could not create the finder " + name +
								", because no matching CMP field was found.");
					}
				}
			}
		}
   }
   
   // FindEntitiesCommand implementation -------------------------
   
	public FinderResults execute(Method finderMethod,
			Object[] args,
			EntityEnterpriseContext ctx)
		throws RemoteException, FinderException
	{	
		FindEntitiesCommand finderCommand =
				(FindEntitiesCommand)knownFinderCommands.get(finderMethod);
	
		if(finderCommand == null) {
			throw new FinderException("Unknown finder: " + finderMethod.getName());
		}
		return finderCommand.execute(finderMethod, args, ctx);
	}
}
