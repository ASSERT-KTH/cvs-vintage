package org.columba.core.main;

import org.columba.core.context.base.ContextFactory;
import org.columba.core.context.base.api.IContextFactory;
import org.columba.core.search.api.ISearchManager;

/**
 * Main interface to all core services, factories and managers.
 * 
 * @author frd
 */
public class MainInterface {

	public static ISearchManager searchManager;
	
	public static IContextFactory contextFactory = new ContextFactory();

}
