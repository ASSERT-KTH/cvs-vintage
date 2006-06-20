package org.columba.core.search.api;

import java.util.List;

import org.columba.api.plugin.IExtensionInterface;

/**
 * Provider does the actual search and furthermore contains a description
 * of the search criteria.
 * 
 * @author frd
 */
public interface ISearchProvider extends IExtensionInterface{

	/**
	 * Return Unique technical name.
	 * @return
	 */
	public String getName();
	
	/**
	 * Return namespace. This is used to group search criteria.
	 * 
	 * @return
	 */
	public String getNamespace();
	
	/**
	 * Retrieve search criteria for given search term.
	 * 
	 * @param searchTerm
	 * @return
	 */
	public ISearchCriteria getCriteria(String searchTerm);

	/**
	 * Execute query and retrieve pageable search result for given search term.
	 * <p>
	 * The query returns <code>resultCount</code> individual results, from
	 * a given <code>startIndex</code>. Paging should be supported, so its
	 * up to the underlying implementation to use an intelligent caching 
	 * strategy or whatsoever.
	 *  
	 * @param searchTerm		search term
	 * @param startIndex		start index of search results
	 * @param resultCount		total count of results
	 * 
	 * @return	
	 */
	public List<ISearchResult> query(String searchTerm, int startIndex, int resultCount);
	
	/**
	 * Return total number of search results. Method only returns valid result after calling
	 * <code>query</code> first.
	 * 
	 * @return	total number of search results. <code>-1</code>, in case <code>query</code> was not called, yet
	 */
	public int getTotalResultCount();
}
