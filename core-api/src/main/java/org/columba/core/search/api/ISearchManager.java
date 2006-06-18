package org.columba.core.search.api;

import java.util.List;

/**
 * Search Provider Registry.
 * 
 * @author frd
 */
public interface ISearchManager {

	public void register(ISearchProvider provider);
	public void unregister(ISearchProvider provider);
	public List<ISearchProvider> getAllProviders(); 
	
	public void executeSearch(String searchTerm);
	
	public void addResultListener(IResultListener listener);
	public void removeResultListener(IResultListener listener);
}
