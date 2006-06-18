package org.columba.core.main;

import org.columba.core.search.SearchManager;
import org.columba.core.search.api.ISearchManager;

/**
 * Main interface to all core services, factories and managers.
 * 
 * @author frd
 */
public class MainInterface {

	public static ISearchManager searchManager = new SearchManager();
	
	public MainInterface() {
		super();
	}

}
