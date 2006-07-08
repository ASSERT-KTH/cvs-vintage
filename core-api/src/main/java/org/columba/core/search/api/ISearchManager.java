package org.columba.core.search.api;

import java.util.List;

/**
 * Search Provider Registry.
 * 
 * @author frd
 */
public interface ISearchManager {

	public ISearchProvider getProvider(String technicalName);
	
	public List<ISearchProvider> getAllProviders(); 
	
	/**
	 * Execute query and retrieve pageable search result for given search term.
	 * <p>
	 * The query returns <code>resultCount</code> individual results, from
	 * a given <code>startIndex</code>. Paging should be supported, so its
	 * up to the underlying implementation to use an intelligent caching 
	 * strategy or whatsoever.
	 * 
	 * @param searchTerm
	 * @param startIndex		start index of search results
	 * @param resultCount		total count of results
	 */
	public void executeSearch(String searchTerm, int startIndex, int resultCount);
	
	
	/**
	 * Execute query and retrieve pageable search result for given search term.
	 * <p>
	 * The query returns <code>resultCount</code> individual results, from
	 * a given <code>startIndex</code>. Paging should be supported, so its
	 * up to the underlying implementation to use an intelligent caching 
	 * strategy or whatsoever.
	 * 
	 * @param searchTerm
	 * @param providerName
	 * @param startIndex		start index of search results
	 * @param resultCount		total count of results
	 * 
	 */
	public void executeSearch(String searchTerm, String providerName, int startIndex, int resultCount);
	
	/**
	 * Execute query and retrieve pageable search result for given search term.
	 * <p>
	 * The query returns <code>resultCount</code> individual results, from
	 * a given <code>startIndex</code>. Paging should be supported, so its
	 * up to the underlying implementation to use an intelligent caching 
	 * strategy or whatsoever.
	 * 
	 * @param searchTerm
	 * @param providerName
	 * @param criteriaName
	 * @param startIndex		start index of search results
	 * @param resultCount		total count of results
	 * 
	 */
	public void executeSearch(String searchTerm, String providerName, String criteriaName, int startIndex, int resultCount);
	
	
	/**
	 * Clear a search and discard all cached data for this <code>searchTerm</code>.
	 * 
	 * @param searchTerm	search term
	 */
	public void clearSearch(String searchTerm);
	
	public void reset();
	
	public void addResultListener(IResultListener listener);
	public void removeResultListener(IResultListener listener);
}
